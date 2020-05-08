 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.editors;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.media.AudioManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Gravity;
 import android.view.View;
 import android.view.WindowManager;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.annotations.MainActivityScope;
 import com.zapta.apps.maniana.main.MainActivityState;
 import com.zapta.apps.maniana.model.ItemColor;
 import com.zapta.apps.maniana.util.PopupsTracker.TrackablePopup;
 import com.zapta.apps.maniana.view.ExtendedEditText;
 
 //import android.content.DialogInterface.OnDismissListener;
 
 /**
  * Item text editor dialog. Used to create new itmes and to edit existing ones.
  * <p>
  * TODO: see if we can use the info in http://tinyurl.com/8yucabx to show the Done button on the
  * soft keyboard.
  * 
  * @author Tal Dayan
  */
 @MainActivityScope
 public class ItemTextEditor extends Dialog implements TrackablePopup {
    
    private static final boolean PRE_API_11 = android.os.Build.VERSION.SDK_INT < 11;
 
     public interface ItemEditorListener {
         void onDismiss(String finalText, ItemColor finalColor);
     }
 
     private final MainActivityState mMainState;
 
     private final ItemEditorListener mListener;
 
     /** The text edit field of the dialog. */
     private final ExtendedEditText mExtendedEditTextView;
 
     /** The view whose background is used to display the color */
     private final View mColorView;
 
     /** Used to avoid double reporting of dismissal. */
     private boolean dismissAlreadyReported = false;
 
     private ItemColor mItemColor;
 
     /** Private constructor. Use startEditor() to create and launch an editor. */
     private ItemTextEditor(final MainActivityState mainActivityState, String title,
             String initialText, ItemColor initialItemColor, ItemEditorListener listener) {
         // TODO: reorder and organize the statements below for better reliability.
         super(mainActivityState.context());
         mMainState = mainActivityState;
         mListener = listener;
         mItemColor = initialItemColor;
         setContentView(R.layout.editor_layout);
         setTitle(title);
         setOwnerActivity(mainActivityState.mainActivity());
 
         // Get sub views
         mExtendedEditTextView = (ExtendedEditText) findViewById(R.id.editor_text);
         mColorView = findViewById(R.id.editor_color);
 
         // Set text and style. Always using non completed variation, even if the
         // item is completed.
         mExtendedEditTextView.setText(initialText);
         
        // For API 11+ these values come from the default theme. For holo, 
        // this will be white text on dark gray background. This makes sure the text
        // cursor is at at color visible over the background.:w
        if (PRE_API_11) {
            final View topView = findViewById(R.id.editor_top);
            topView.setBackgroundColor(0xffffffff);            
            mExtendedEditTextView.setBackgroundColor(0xffffffff);
            mExtendedEditTextView.setTextColor(0xff000000);            
        }
        
         mainActivityState.prefTracker().getPageItemFontVariation()
                 .apply(mExtendedEditTextView, false, false);
 
         // EditorEventAdapter eventAdapter = new EditorEventAdapter();
         setOnDismissListener(new OnDismissListener() {
             @Override
             public void onDismiss(DialogInterface arg0) {
                 handleOnDismiss();
             }
         });
 
         mExtendedEditTextView.addTextChangedListener(new TextWatcher() {
             @Override
             public void afterTextChanged(Editable arg0) {
                 handleTextChanged();
             }
 
             @Override
             public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
             }
 
             @Override
             public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
             }
         });
 
         getWindow().setGravity(Gravity.TOP);
 
        mColorView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 handleColorClicked();
             }
         });
 
         updateColorView();
 
         // TODO: why this setting does not work when done in the layout XML?
         mExtendedEditTextView.setHorizontallyScrolling(false);
         mExtendedEditTextView.setSingleLine(false);
         mExtendedEditTextView.setMinLines(3);
         mExtendedEditTextView.setMaxLines(5);
 
         // Position cursor at the end of the text
         mExtendedEditTextView.setSelection(initialText.length());
 
         // TODO: this does not open automatically the keyaobrd when in landscape mode.
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
     }
 
     /** Called when the dialog get dismissed. */
     private final void handleOnDismiss() {
         mMainState.popupsTracker().untrack(this);
         // If not already reported during the close leftover.
         if (!dismissAlreadyReported) {
             mListener.onDismiss(mExtendedEditTextView.getText().toString(), mItemColor);
         }
     }
 
     /** Called when text field value changes. */
     private final void handleTextChanged() {
         final String value = mExtendedEditTextView.getText().toString();
         // We detect the Enter key by the '\n' char it inserts.
         if (value.contains("\n")) {
             final String cleanedValue = value.replace("\n", "").trim();
             mExtendedEditTextView.setText(cleanedValue);
             // This will trigger the handleOnDismiss above that will call the listener of this
             // editor.
             dismiss();
         }
     }
 
     private final void handleColorClicked() {
         mItemColor = mItemColor.nextCyclicColor();
         updateColorView();
         mMainState.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_SPACEBAR, false);
     }
 
     private final void updateColorView() {
         mColorView.setBackgroundColor(mItemColor.getColor(0xffaaaaaa));
     }
 
     /** Called when the dialog was left open and the main activity pauses. */
     @Override
     public final void closeLeftOver() {
         if (isShowing()) {
             // We provide an early dismiss event. Otherwise, this would be reported later when the
             // UI thread will get to handle the queued event. The controller rely on the fact that
             // the editor was dismissed and any pending new item text was submitted to the model.
             dismissAlreadyReported = true;
             mListener.onDismiss(mExtendedEditTextView.getText().toString(), mItemColor);
             dismiss();
         }
     }
 
     /**
      * Show an item editor.
      * 
      * @param mainActivityState mainActivityState context.
      * @param title title to display in the editor.
      * @param initialText initial edited item text
      * @param listener listener to callback on changes and on end.
      */
     public static void startEditor(final MainActivityState mainActivityState, String title,
             String initialText, ItemColor initialItemColor, final ItemEditorListener listener) {
         final ItemTextEditor dialog = new ItemTextEditor(mainActivityState, title, initialText,
                 initialItemColor, listener);
         mainActivityState.popupsTracker().track(dialog);
         dialog.show();
     }
 }
