 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2013, Janrain, Inc.
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
 
 package com.janrain.android;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import com.janrain.android.capture.CaptureApiConnection;
 import com.janrain.android.capture.CaptureApiError;
 import com.janrain.android.capture.CaptureRecord;
 import com.janrain.android.capture.Capture;
 import com.janrain.android.engage.ui.JRCustomInterfaceConfiguration;
 import com.janrain.android.engage.ui.JRCustomInterfaceView;
 import com.janrain.android.utils.LogUtils;
 
 import static com.janrain.android.R.string.jr_capture_trad_signin_bad_password;
 import static com.janrain.android.R.string.jr_capture_trad_signin_unrecognized_error;
 import static com.janrain.android.R.string.jr_dialog_dismiss;
 
 public class TradSignInUi extends JRCustomInterfaceConfiguration {
     public TradSignInUi() {
         this.mProviderListHeader = new TradSignInView();
     }
 
     private static class TradSignInView extends JRCustomInterfaceView {
         private EditText userName, password;
 
         @Override
         public View onCreateView(Context context,
                                  LayoutInflater inflater,
                                  ViewGroup container,
                                  Bundle savedInstanceState) {
             View v = inflater.inflate(R.layout.jr_capture_trad_signin, container, false);
             userName = (EditText) v.findViewById(R.id.username_edit);
             password = (EditText) v.findViewById(R.id.password_edit);
 
             v.findViewById(R.id.custom_signin_button).setOnClickListener(new SignInButtonHandler());
             return v;
         }
 
         private class SignInButtonHandler implements View.OnClickListener {
             public void onClick(View v) {
                 final Capture.SignInRequestHandler handler = new Capture.SignInRequestHandler() {
                     public void onSuccess(CaptureRecord record) {
                         Jump.state.signedInUser = record;
                         Jump.fireHandlerOnSuccess();
                         dismissProgressIndicator();
                         finishJrSignin();
                     }
 
                     public void onFailure(CaptureApiError error) {
                         dismissProgressIndicator();
                         AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                         b.setNeutralButton(jr_dialog_dismiss, null);
                        boolean badPassword = false;
                         if (badPassword) {
                             b.setMessage(jr_capture_trad_signin_bad_password);
                         } else {
                             b.setMessage(jr_capture_trad_signin_unrecognized_error);
                             LogUtils.loge(error.toString());
                         }
                         b.show();
                     }
                 };
                 final CaptureApiConnection d =
                         //Capture.performTraditionalSignIn(userName.getText().toString(),
                         Capture.performLegacyTraditionalSignIn(userName.getText().toString(),
                                 password.getText().toString(),
                                 Jump.state.traditionalSignInType, handler);
                 showProgressIndicator(true, new DialogInterface.OnCancelListener() {
                     public void onCancel(DialogInterface dialog) {
                         handler.cancel();
                         d.stopConnection();
                     }
                 });
             }
         }
     }
 }
