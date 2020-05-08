 package com.merono.g;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class ChooseBoardDialogFragment extends DialogFragment {
     private static final String TITLE = "Choose Board";
 
     private EditText mEditText;
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         mEditText = new EditText(getActivity());
        mEditText.setId(1); // give id so that it maintains its state
         mEditText.setInputType(InputType.TYPE_CLASS_TEXT); // force one-line
 
         mEditText
                 .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                     public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                         if (actionId == EditorInfo.IME_ACTION_DONE) {
                             switchBoard(mEditText.getText().toString());
                             ChooseBoardDialogFragment.this.dismiss();
                         }
                         return false;
                     }
                 });
 
         return new AlertDialog.Builder(getActivity())
                 .setTitle(TITLE)
                 .setView(mEditText)
                 .setPositiveButton(android.R.string.ok,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface d, int button) {
                                 switchBoard(mEditText.getText().toString());
                             }
                         }).create();
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         // make keyboard appear when dialog appears (only works in portrait)
         mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
             public void onFocusChange(View v, boolean hasFocus) {
                 if (hasFocus) {
                     Window window = getDialog().getWindow();
                     window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                 }
             }
         });
     }
 
     private void switchBoard(String boardName) {
         ((GActivity) getActivity()).switchBoard(boardName);
     }
 }
