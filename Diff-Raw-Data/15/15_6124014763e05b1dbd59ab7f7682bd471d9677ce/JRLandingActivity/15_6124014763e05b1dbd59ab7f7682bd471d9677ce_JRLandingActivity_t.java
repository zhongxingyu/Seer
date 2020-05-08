 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Copyright (c) 2010, Janrain, Inc.
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation and/or
    other materials provided with the distribution.
  * Neither the name of the Janrain, Inc. nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.
 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.session.JRSessionData;
 
 /**
  * Landing Page Activity
  */
 public class JRLandingActivity extends Activity implements View.OnClickListener {
 
     // ------------------------------------------------------------------------
     // TYPES
     // ------------------------------------------------------------------------
 
     /**
      * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
      * for iPhone-like ability to close this activity from the maestro class.
      */
     private class FinishReceiver extends BroadcastReceiver {
 
         private final String TAG = JRLandingActivity.TAG + "-" + FinishReceiver.class.getSimpleName();
 
         @Override
         public void onReceive(Context context, Intent intent) {
             String target = intent.getStringExtra(
                     JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
            if (JRLandingActivity.class.toString().equals(target)) {
                 tryToFinishActivity();
                 Log.i(TAG, "[onReceive] handled");
             } else if (Config.LOGD) {
                 Log.i(TAG, "[onReceive] ignored");
             }
         }
     }
 
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
     private static final String TAG = JRLandingActivity.class.getSimpleName();
 
     // I find the auto-show keyboard really annoying, so I am turning
     // it off until someone asks for it...
     private static final boolean SHOW_KEYBOARD_ON_LAUNCH = false;
 
     // ------------------------------------------------------------------------
     // STATIC INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     private SharedLayoutHelper mLayoutHelper;
     private JRSessionData mSessionData;
     private FinishReceiver mFinishReceiver;
 
     private boolean mIsAlertShowing;
     private boolean mIsFinishPending;
 
     private ImageView mImageView;
     private EditText mEditText;
 
     private Button mLeftButton;     // iPhone: signInButton
     private Button mMiddleButton;   // iPhone: bigSignInButton
     private Button mRightButton;    // iPhone: backToProvidersButton
 
     // ------------------------------------------------------------------------
     // INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     public JRLandingActivity() {
         mLayoutHelper = new SharedLayoutHelper(this);
         mSessionData = JRSessionData.getInstance();
         mIsAlertShowing = false;
         mIsFinishPending = false;
     }
 
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
     /**
      * Called when the activity is first created.
      *
      * @param savedInstanceState
      *      If the activity is being re-initialized after previously being shut down then this
      *      Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
      *      Note: Otherwise it is null.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.provider_landing);
 
         mImageView = (ImageView)findViewById(R.id.landing_logo);
         mEditText = (EditText)findViewById(R.id.landing_edit);
 
         mLeftButton = (Button)findViewById(R.id.landing_left_btn);
         mLeftButton.setOnClickListener(this);
         mMiddleButton = (Button)findViewById(R.id.landing_middle_btn);
         mMiddleButton.setOnClickListener(this);
         mRightButton = (Button)findViewById(R.id.landing_right_btn);
         mRightButton.setOnClickListener(this);
 
         prepareUserInterface();
     }
 
     @Override
     protected void onStart() {
         super.onResume();
 
         if (mFinishReceiver == null) {
             mFinishReceiver = new FinishReceiver();
             registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         unregisterReceiver(mFinishReceiver);
     }
 
     public void onClick(View view) {
         if (view.equals(mRightButton) || view.equals(mMiddleButton)) {
             handlePrimaryButtonClick();
         } else if (view.equals(mLeftButton)) {
             handleSecondaryButtonClick();
         }
     }
 
     private void handlePrimaryButtonClick() {
         String text = mEditText.getText().toString();
         if (TextUtils.isEmpty(text)) {
             showAlertDialog("Invalid Input",
                     "The input you have entered is not valid. Please try again.");
         } else {
            showHideKeyboard(false);
             mSessionData.getCurrentProvider().setUserInput(text);
             JRUserInterfaceMaestro.getInstance().showWebView();
         }
     }
 
     private void handleSecondaryButtonClick() {
 
     }
 
     private void showAlertDialog(String title, String message) {
         mIsAlertShowing = true;
         new AlertDialog.Builder(this)
             .setTitle(title)
             .setMessage(message)
             .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     mIsAlertShowing = false;
                     if (mIsFinishPending) {
                         mIsFinishPending = false;
                         finish();
                     }
                 }
             })
             .show();
     }
 
     public void tryToFinishActivity() {
         Log.i(TAG, "[tryToFinishActivity]");
         if (mIsAlertShowing) {
             mIsFinishPending = true;
         } else {
             finish();
         }
     }
 
 
 
     private boolean prepareUserInterface() {
 
         if (mSessionData.getCurrentProvider() == null) {
             JREngageError error = new JREngageError(
                 "There was an error authenticating with the selected provider.",
                 JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                 JREngageError.ErrorType.AUTHENTICATION_FAILED);
 
             mSessionData.triggerAuthenticationDidFail(error);
 
             // TODO:  toast or error dialog???
 
             return false;
         }
 
         JRProvider currentProvider = mSessionData.getCurrentProvider();
 
         mLayoutHelper.setHeaderText(getCustomTitle());
 
         mImageView.setImageResource(
                 ResourceHelper.providerNameToLogoResourceId(currentProvider.getName()));
 
         if (currentProvider.requiresInput()) {
             if (Config.LOGD) {
                 Log.d(TAG, "[prepareUserInterface] current provider requires input");
             }
 
             String userInput = currentProvider.getUserInput();
             if (!TextUtils.isEmpty(userInput)) {
                 mEditText.setText(userInput);
                 configureButtonVisibility(false);
             } else {
                 mEditText.setText("");
                 configureButtonVisibility(true);
             }
 
             mEditText.setHint(currentProvider.getPlaceholderText());
 
             // TODO:
             // welcome label?
             // forget user button?
 
         } else {
             if (Config.LOGD) {
                 Log.d(TAG, "[prepareUserInterface] current provider does not require input");
             }
 
             // TODO:
             // Need to implement this.  Is this the table view?
         }
 
         return true;
     }
 
     private void configureButtonVisibility(boolean isSingleButtonLayout) {
         if (isSingleButtonLayout) {
             mLeftButton.setVisibility(View.GONE);
             mRightButton.setVisibility(View.GONE);
             mMiddleButton.setVisibility(View.VISIBLE);
         } else {
             mMiddleButton.setVisibility(View.GONE);
             mLeftButton.setVisibility(View.VISIBLE);
             mRightButton.setVisibility(View.VISIBLE);
         }
     }
 
     private String getCustomTitle() {
         JRProvider provider = mSessionData.getCurrentProvider();
         return provider.requiresInput()
                 ? provider.getShortText()
                 : getString(R.string.landing_default_custom_title);
     }
 
    private void showHideKeyboard(boolean show) {
        if (show) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(mEditText, 0);
        } else {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

     private void focusEditAndPopKeyboard() {
         if ((TextUtils.isEmpty(mEditText.getText()) && (mEditText.requestFocus()))) {
             if (SHOW_KEYBOARD_ON_LAUNCH) {
                 InputMethodManager imm =
                     (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 
                 imm.toggleSoftInput(
                     InputMethodManager.SHOW_FORCED,
                     InputMethodManager.HIDE_IMPLICIT_ONLY);
             }
         } else {
             Log.d(TAG, "[focusEditAndPopKeyboard] FAIL");
         }
     }
 
 
 }
