 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 package com.janrain.android.engage.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.utils.AndroidUtils;
 
 /**
  * @internal
  *
  * @class JRLandingFragment
  **/
 public class JRLandingFragment extends JRUiFragment {
     public static final String KEY_ALERT_DIALOG_TITLE = "jr_alert_dialog_title";
     public static final String KEY_ALERT_DIALOG_MESSAGE = "jr_alert_dialog_message";
     public static final int DIALOG_GENERIC_ALERT = 1;
     public static final int RESULT_SWITCH_ACCOUNTS = 1;
     public static final int RESULT_RESTART = 2;
 
     private View.OnClickListener mButtonListener = new View.OnClickListener() {
         public void onClick(View view) {
             JREngage.logd(TAG, "[onClick] handled");
 
             if (view.equals(mSignInButton)) {
                 onSignInClick();
             } else if (view.equals(mSwitchAccountButton)) {
                 onSwitchAccountsClick();
             }
         }
     };
 
     private boolean mIsAlertShowing = false;
     private boolean mIsFinishPending = false;
 
     private ImageView mLogo;
     private EditText mUserInput;
 
     private TextView mWelcomeLabel;
 
     private Button mSwitchAccountButton;
     private ColorButton mSignInButton;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if (mSession == null) return null;
         View view = inflater.inflate(R.layout.jr_provider_landing, container, false);
 
         mLogo = (ImageView)view.findViewById(R.id.jr_landing_logo);
         mUserInput = (EditText)view.findViewById(R.id.jr_landing_edit);
         mWelcomeLabel = (TextView)view.findViewById(R.id.jr_landing_welcome_label);
         mSwitchAccountButton = (Button)view.findViewById(R.id.jr_landing_switch_account_button);
         mSignInButton = (ColorButton)view.findViewById(R.id.jr_landing_small_signin_button);
 
         prepareUserInterface();
 
         return view;
     }
 
     private void onSignInClick() {
         if (mSession.getCurrentlyAuthenticatingProvider().requiresInput()) {
             //TODO validate OpenID URLs so they don't hang the WebView
             String text = mUserInput.getText().toString().trim();
             if (TextUtils.isEmpty(text)) {
                 String title = getString(R.string.jr_landing_bad_user_input);
                 String message = getString(R.string.jr_landing_bad_input_long);
                 Bundle options = new Bundle();
                 options.putString(KEY_ALERT_DIALOG_TITLE, title);
                 options.putString(KEY_ALERT_DIALOG_MESSAGE, message);
                 showDialog(DIALOG_GENERIC_ALERT, options);
                 mIsAlertShowing = true;
             } else {
                 mSession.getCurrentlyAuthenticatingProvider().setUserInput(text);
                 showWebView();
             }
         } else {
             showWebView();
         }
     }
 
     private void onSwitchAccountsClick() {
         JREngage.logd(TAG, "[onSwitchAccountsClick]");
 
         mSession.getCurrentlyAuthenticatingProvider().setForceReauth(true);
         mSession.setReturningBasicProvider("");
         mSession.triggerAuthenticationDidRestart();
         getActivity().setResult(RESULT_SWITCH_ACCOUNTS);
         getActivity().finish();
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog d, Bundle options) {
         if (id == DIALOG_GENERIC_ALERT) {
             AlertDialog d_ = (AlertDialog) d;
             d_.setTitle(options.getString(KEY_ALERT_DIALOG_TITLE));
             d_.setMessage(options.getString(KEY_ALERT_DIALOG_MESSAGE));
             return;
         }
 
         super.onPrepareDialog(id, d, options);
     }
 
     @Override
     protected Dialog onCreateDialog(int id, Bundle options) {
         if (id == DIALOG_GENERIC_ALERT) {
             return new AlertDialog.Builder(getActivity())
                 .setTitle(options.getString(KEY_ALERT_DIALOG_TITLE))
                 .setMessage(options.getString(KEY_ALERT_DIALOG_MESSAGE))
                 .setPositiveButton(getString(R.string.jr_dialog_ok), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         mIsAlertShowing = false;
                         if (mIsFinishPending) {
                             mIsFinishPending = false;
                             /* If there is a pending Finish that we deferred because the dialog was displayed
                              * it is called here, now. */
                             tryToFinishActivity();
                         }
                     }
                 }).create();
         }
 
         return super.onCreateDialog(id, options);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_WEBVIEW) {
             switch (resultCode) {
                 case Activity.RESULT_OK:
                     getActivity().setResult(Activity.RESULT_OK);
                     getActivity().finish();
                     break;
                 case JRWebViewFragment.RESULT_RESTART:
                     getActivity().setResult(RESULT_RESTART);
                     getActivity().finish();
                     break;
                 case JRWebViewFragment.RESULT_BAD_OPENID_URL:
                     break;
                 default:
                     throw new RuntimeException("unrecognized result code: " + resultCode);
             }
         } else {
             throw new RuntimeException("unrecognized request code: " + requestCode);
         }
     }
 
     @Override
     protected void tryToFinishActivity() {
         JREngage.logd(TAG, "[tryToFinishActivity]");
         if (mIsAlertShowing) {
             mIsFinishPending = true;
         } else {
             getActivity().finish();
         }
     }
 
     private void prepareUserInterface() {
         mSwitchAccountButton.setOnClickListener(mButtonListener);
         mSignInButton.setOnClickListener(mButtonListener);
         mSignInButton.setColor(getColor(R.color.jr_janrain_darkblue_light_100percent));
         if (AndroidUtils.SDK_INT <= 10) {
             /* Todo this should really test for Theme.Holo or it's descendants */
             mSignInButton.setTextColor(getColor(android.R.color.white));
         }
 
         JRProvider currentlyAuthenticatingProvider = mSession.getCurrentlyAuthenticatingProvider();
         getActivity().setTitle(getCustomTitle());
         mLogo.setImageDrawable(currentlyAuthenticatingProvider.getProviderLogo(getActivity()));
 
         if (currentlyAuthenticatingProvider.getName().equals("openid")) {
             mUserInput.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_URI);
         }
 
         if (currentlyAuthenticatingProvider.requiresInput()) {
             JREngage.logd(TAG, "[prepareUserInterface] current provider requires input");
             configureButtonVisibility(true); // one button UI
 
             mWelcomeLabel.setVisibility(View.GONE);
 
             mUserInput.setVisibility(View.VISIBLE);
             mUserInput.setText(currentlyAuthenticatingProvider.getUserInput());
            mUserInput.setHint(currentlyAuthenticatingProvider.getUserInputHint());
         } else { // doesn't require input
             configureButtonVisibility(false); // = two button UI -> Switch Accounts is showing
             JREngage.logd(TAG, "[prepareUserInterface] current provider doesn't require input");
 
             mUserInput.setVisibility(View.GONE);
 
             mWelcomeLabel.setVisibility(View.VISIBLE);
             mWelcomeLabel.setText(mSession.getAuthenticatedUserForProvider(
                     currentlyAuthenticatingProvider).getWelcomeMessage());
         }
     }
 
     private void configureButtonVisibility(boolean isSingleButtonLayout) {
         if (isSingleButtonLayout) {
             mSwitchAccountButton.setVisibility(View.INVISIBLE);
             mSignInButton.setVisibility(View.VISIBLE);
         } else {
             mSwitchAccountButton.setVisibility(View.VISIBLE);
             mSignInButton.setVisibility(View.VISIBLE);
         }
     }
 
     private String getCustomTitle() {
         JRProvider provider = mSession.getCurrentlyAuthenticatingProvider();
         if (provider.requiresInput()) {
             return provider.getUserInputDescriptor();
         } else {
             return getString(R.string.jr_landing_default_custom_title);
         }
     }
 
     @Override
     protected void onBackPressed() {
         mSession.triggerAuthenticationDidRestart();
         getActivity().setResult(JRLandingFragment.RESULT_RESTART);
         getActivity().finish();
     }
 }
