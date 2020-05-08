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
 
 package com.janrain.android.simpledemo;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import com.janrain.android.Jump;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Map;
 
 import static com.janrain.android.simpledemo.R.id.trad_reg_display_name;
 import static com.janrain.android.simpledemo.R.id.trad_reg_email;
 import static com.janrain.android.simpledemo.R.id.trad_reg_first_name;
 import static com.janrain.android.simpledemo.R.id.trad_reg_last_name;
 import static com.janrain.android.simpledemo.R.id.trad_reg_password;
 import static com.janrain.android.utils.CollectionUtils.collectionToHumanReadableString;
 
 public class RegistrationActivity extends Activity {
 
     private View registerButton;
     private JSONObject newUser = new JSONObject();
     private String socialRegistrationToken = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.registration_activity);
         registerButton = findViewById(R.id.trad_reg_button);
 
         String preregJson = getIntent().getStringExtra("preregistrationRecord");
         String socialRegToken = getIntent().getStringExtra("socialRegistrationToken");
 
         if (preregJson != null) {
             JSONObject preregistrationRecord;
             try {
                 preregistrationRecord = new JSONObject(preregJson);
             } catch (JSONException e) {
                 throw new RuntimeException("Unexpected", e);
             }
 
             newUser = preregistrationRecord;
             socialRegistrationToken = socialRegToken;
             setTitle("Almost Done!");
             setEditTextString(trad_reg_email, newUser.optString("email"));
             setEditTextString(trad_reg_display_name, newUser.optString("displayName"));
             setEditTextString(trad_reg_first_name, newUser.optString("givenName"));
             setEditTextString(trad_reg_last_name, newUser.optString("familyName"));
             setEditTextString(trad_reg_password, newUser.optString("password"));
         } else {
             setTitle("Sign Up");
         }
     }
 
     public void register(View view) {
         String email, displayName, firstName, lastName, password;
         email = getEditTextString(trad_reg_email);
         displayName = getEditTextString(trad_reg_display_name);
         firstName = getEditTextString(trad_reg_first_name);
         lastName = getEditTextString(trad_reg_last_name);
         password = getEditTextString(trad_reg_password);
         try {
             newUser.put("email", email)
                     .put("displayName", displayName)
                     .put("givenName", firstName)
                     .put("familyName", lastName)
                     .put("password", password);
         } catch (JSONException e) {
             throw new RuntimeException("Unexpected", e);
         }
 
         Jump.registerNewUser(newUser, socialRegistrationToken, new MySignInResultHandler());
 
         registerButton.setEnabled(false);
     }
 
     private String getEditTextString(int layoutId) {
         return ((EditText) findViewById(layoutId)).getText().toString();
     }
 
     private void setEditTextString(int layoutId, String value) {
         ((EditText) findViewById(layoutId)).setText(value);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         // Don't need to call Jump.saveToDisk here, there's no state since the user isn't signed in until
         // after they are registered.
     }
 
     private class MySignInResultHandler implements Jump.SignInResultHandler, Jump.SignInCodeHandler {
         public void onSuccess() {
             Toast.makeText(RegistrationActivity.this, "Registration Complete", Toast.LENGTH_LONG).show();
             finish();
         }
 
         public void onCode(String code) {
            Toast.makeText(RegistrationActivity.this, "Authorization Code: " + code, Toast.LENGTH_LONG).show();
         }
 
         public void onFailure(SignInError error) {
             AlertDialog.Builder adb = new AlertDialog.Builder(RegistrationActivity.this);
             if (error.captureApiError.isFormValidationError()) {
                 adb.setTitle("Invalid Fields");
                 Map<String, Object> messages =
                         (Map) error.captureApiError.getLocalizedValidationErrorMessages();
                 String message = collectionToHumanReadableString(messages);
                 adb.setMessage(message);
             } else {
                 adb.setTitle("Unrecognized error");
                 adb.setMessage(error.toString());
             }
             adb.show();
             registerButton.setEnabled(true);
         }
     }
 }
