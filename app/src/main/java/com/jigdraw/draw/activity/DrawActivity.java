/*
 * Copyright (c) 2018. Jay Paulynice (jay.paulynice@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jigdraw.draw.activity;

import static com.jigdraw.draw.util.ToastUtil.shortToast;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jigdraw.draw.R;
import com.jigdraw.draw.model.enums.Difficulty;
import com.jigdraw.draw.tasks.JigsawGenerator;
import com.jigdraw.draw.views.DrawingView;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Main activity class represents all the activities that a user starts with such as draw, create a new drawing, save
 * the current drawing, choose eraser, brush sizes and create a jigsaw puzzle.
 *
 * @author Jay Paulynice (jay.paulynice@gmail.com)
 */
public class DrawActivity
        extends BaseJigsawActivity
        implements OnClickListener {
    /** Class name for logging */
    private static final String TAG = "DrawActivity";

    /** Custom view for drawing */
    private DrawingView drawView;

    /**
     * View of selected brush
     */
    private View selectedBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Starting draw activity...");
        super.onCreate(savedInstanceState);
        init();
    }

    /**
     * Initialize all the ui components
     */
    private void init() {
        setContentView(R.layout.activity_main);
        initViews();
        setBrushColor(drawView.getPaintColor());
    }

    /**
     * Initialize all the views
     */
    private void initViews() {
        drawView = findViewById(R.id.drawing);
        drawView.setBrushSize(getResources().getInteger(R.integer.medium_size));
        setSelectedBrush(R.id.medium_brush);
        setEraseSelected(false);

        for (View v : getMenuOptions()) {
            v.setOnClickListener(this);
        }
    }

    /**
     * Set the brushes to the color chosen
     *
     * @param color the chosen color
     */
    private void setBrushColor(int color) {
        for (View v : getBrushes()) {
            ImageButton im = (ImageButton) v;
            GradientDrawable d = (GradientDrawable) im.getDrawable();
            d.setColor(color);
        }
    }

    /**
     * Get the top level view image buttons
     *
     * @return the top layout views
     */
    public List<View> getMenuOptions() {
        return getLayoutChildren(R.id.top_options);
    }

    /**
     * Get the brush size image buttons
     *
     * @return the top level views
     */
    public List<View> getBrushes() {
        return getLayoutChildren(R.id.all_brushes);
    }

    /**
     * Helper method to get all the views in a layout given the layout id.
     *
     * @param layoutId the id of the layout
     * @return list of views in the layout
     */
    private List<View> getLayoutChildren(final int layoutId) {
        List<View> views = new ArrayList<>();
        LinearLayout layout = findViewById(layoutId);
        int count = layout.getChildCount();
        for (int i = 0; i < count; i++) {
            views.add(layout.getChildAt(i));
        }

        return views;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "view id clicked: " + view.getId());
        switch(view.getId()) {
            case R.id.color_pick:
                handleColorPick();
                break;
            case R.id.erase_btn:
                handleEraseButton();
                break;
            case R.id.new_btn:
                openNewDrawingDialog();
                break;
            default:
                openCreateJigsawDialog();
        }
    }

    /**
     * Handle the paint bucket click
     */
    private void handleColorPick() {
        drawView.setErase(false);
        setEraseSelected(false);
        openColorPickerDialog();
    }

    /**
     * Set erase to true on eraser click
     */
    private void handleEraseButton() {
        drawView.setErase(true);
        setEraseSelected(true);
    }

    /**
     * Handle the new button click and open dialog
     */
    private void openNewDrawingDialog() {
        new MaterialDialog.Builder(this)
                .onPositive(newDialogClick())
                .onNegative(newDialogClick())
                .title(R.string.action_new_drawing)
                .positiveText(R.string.action_ok)
                .negativeText(R.string.action_cancel)
                .content(R.string.action_new_q)
                .show();
    }

    /**
     * Handle the create jigsaw button click and open dialog to choose difficulty level
     */
    private void openCreateJigsawDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.level_difficulty)
                .items("Easy", "Medium", "Hard")
                .itemsCallbackSingleChoice(0, (dialog, view, which, text) -> {
                    createJigsaw(which);
                    return true;
                })
                .positiveText(R.string.action_ok)
                .negativeText(R.string.action_cancel)
                .show();
    }

    /**
     * Create new dialog for color picker and show it
     *
     */
    private void openColorPickerDialog() {
        Log.d(TAG, "show color picker dialog...");
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, drawView.getPaintColor(), false, getColorPickerCallback());
        dialog.show();
    }

    /**
     * Call back to handle the positive and negative clicks on the dialog.  On positive click start a new drawing and
     * negative click just cancel.
     *
     * @return button call back
     */
    private MaterialDialog.SingleButtonCallback newDialogClick() {
        return (dialog, which) -> {
            if (DialogAction.POSITIVE.equals(which)) {
                drawView.startNew();
            }
            dialog.dismiss();
        };
    }

    /**
     * Call back to handle the color picked and update the view and brush size colors.
     *
     * @return button call back
     */
    private AmbilWarnaDialog.OnAmbilWarnaListener getColorPickerCallback() {
        return new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Log.d(TAG, "cancel clicked...");
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                Log.d(TAG, "selected color: " + color);
                drawView.setColor(color);
                setBrushColor(color);
            }
        };
    }

    /**
     * Create jigsaw and give user feedback
     *
     * @param which the selected option in dialog
     */
    private void createJigsaw(int which) {
        drawView.setDrawingCacheEnabled(true);
        Bitmap bitmap = drawView.getDrawingCache();

        JigsawGenerator task = new JigsawGenerator(getApplicationContext(), Difficulty.fromValue(which));

        shortToast(getApplicationContext(), "Loading...");
        task.execute(bitmap.copy(bitmap.getConfig(), true));
        drawView.destroyDrawingCache();
    }

    /**
     * Handle the change of the brush size
     *
     * @param view the current brush view
     */
    public void handleBrushSize(View view) {
        drawView.setErase(false);
        setEraseSelected(false);
        setSelectedBrush(view.getId());

        switch (view.getId()) {
            case R.id.small_brush:
                drawView.setBrushSize(getResources().getInteger(R.integer.small_size));
                break;
            case R.id.large_brush:
                drawView.setBrushSize(getResources().getInteger(R.integer.large_size));
                break;
            case R.id.largest_brush:
                drawView.setBrushSize(getResources().getInteger(R.integer.largest_size));
                break;
            default:
                drawView.setBrushSize(getResources().getInteger(R.integer.medium_size));
        }
    }

    /**
     * change background of current brush view
     *
     * @param id the id of current brush view
     */
    private void setSelectedBrush(int id) {
        if (selectedBrush != null) {
            selectedBrush.setBackground(getDrawable(R.drawable.btn_default_normal_holo_dark));
        }
        selectedBrush = findViewById(id);
        selectedBrush.setBackground(getDrawable(R.drawable.btn_default_normal_holo_light));
    }

    /**
     * change background of erase and color buttons
     *
     * @param selected the state of erase button
     */
    private void setEraseSelected(boolean selected){
        if(selected){
            findViewById(R.id.erase_btn).setBackgroundColor(getColor(android.R.color.darker_gray));
            findViewById(R.id.color_pick).setBackgroundColor(getColor(android.R.color.transparent));
        } else {
            findViewById(R.id.erase_btn).setBackgroundColor(getColor(android.R.color.transparent));
            findViewById(R.id.color_pick).setBackgroundColor(getColor(android.R.color.darker_gray));
        }
    }
}
