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
 package com.janrain.android.simpledemo;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import com.janrain.android.Jump;
 import com.janrain.android.capture.CaptureApiError;
 import com.janrain.android.capture.Capture;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.utils.LogUtils;
 import org.json.JSONException;
 
 import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
 import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
 import static com.janrain.android.capture.Capture.CaptureApiRequestCallback;
 
 public class MainActivity extends FragmentActivity {
     private final Jump.SignInResultHandler signInResultHandler = new Jump.SignInResultHandler() {
         public void onSuccess() {
             AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
             b.setMessage("Sign-in complete.");
             b.setNeutralButton("Dismiss", null);
             b.show();
         }
 
         public void onFailure(SignInError error) {
             if (error.reason == SignInError.FailureReason.CAPTURE_API_ERROR &&
                     error.captureApiError.isMergeFlowError()) {
                 // Called below is the default merge-flow handler. Merge behavior may also be implemented by
                 // headless-native-API for more control over the user experience.
                 //
                 // To do so, call Jump.showSignInDialog or Jump.performTraditionalSignIn directly, and
                 // pass in the merge-token and existing-provider-name retrieved from `error`.
                 //
                 // String mergeToken = error.captureApiError.getMergeToken();
                 // String existingProvider = error.captureApiError.getExistingAccountIdentityProvider()
                 //
                 // (An existing-provider-name of "capture" indicates a conflict with a traditional-sign-in
                 // account. You can handle this case yourself, by displaying a dialog and calling
                 // Jump.performTraditionalSignIn, or you can call Jump.showSignInDialog(..., "capture") and
                 // a library-provided dialog will be provided.)
 
                 Jump.startDefaultMergeFlowUi(MainActivity.this, error, signInResultHandler);
             } else {
                 AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                 b.setMessage("Sign-in failure:" + error);
                 b.setNeutralButton("Dismiss", null);
                 b.show();
             }
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         //elgfmldanecpmanecfok <- no pub_stream
 
         //capture testing/staging
         //String engageAppId = "appcfamhnpkagijaeinl";
         //String captureDomain = "mobile-testing.janraincapture.com";
         //String captureClientId = "atasaz59p8cyecmbzmcwkbthsyq3wrxh";
         //String captureLocale = "en-US";
         //String captureSignInFormName = "signinForm";
         //Jump.TraditionalSignInType signInType = Jump.TraditionalSignInType.EMAIL;
 
         //capture prod
         String engageAppId = "appcfamhnpkagijaeinl";
         String captureDomain = "mobile-dev.janraincapture.com";
         String captureClientId = "gpy4j6d8bcsepkb2kzm7zp5qkk8wrza6";
         String captureLocale = "en-US";
         String captureSignInFormName = "signinForm";
         Jump.TraditionalSignInType signInType = Jump.TraditionalSignInType.EMAIL;
 
         Jump.init(this, engageAppId, captureDomain, captureClientId, captureLocale, captureSignInFormName,
                 signInType);
 
         enableStrictMode();
 
         LinearLayout linearLayout = new LinearLayout(this);
         linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
         linearLayout.setOrientation(LinearLayout.VERTICAL);
 
         Button testAuth = makeButton(linearLayout, "Test Capture Auth");
         Button dumpRecord = makeButton(linearLayout, "Dump Record to Log");
         Button touchRecord = makeButton(linearLayout, "Edit About Me Attribute");
         Button syncRecord = makeButton(linearLayout, "Sync Record");
         makeButton(linearLayout, "Test Share").setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 JREngage.getInstance().showSocialPublishingDialog(MainActivity.this,
                        new JRActivityObject("aslkdfj", "google.com"));
             }
         });
         //Button refreshAccesstoken = makeButton(linearLayout, "Refresh Access Token");
 
         setContentView(linearLayout);
 
         testAuth.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Jump.showSignInDialog(MainActivity.this, null, signInResultHandler, null);
             }
         });
 
         dumpRecord.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 LogUtils.logd(String.valueOf(Jump.getSignedInUser()));
             }
         });
 
         touchRecord.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (Jump.getSignedInUser() == null) {
                     Toast.makeText(MainActivity.this, "Can't edit without record instance.",
                             Toast.LENGTH_LONG).show();
                     return;
                 }
                 AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
 
                 alert.setTitle("About Me");
                 alert.setMessage(Jump.getSignedInUser().optString("aboutMe"));
 
                 final EditText input = new EditText(MainActivity.this);
                 alert.setView(input);
 
                 alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         try {
                             Jump.getSignedInUser().put("aboutMe", input.getText().toString());
                         } catch (JSONException e) {
                             throw new RuntimeException("Unexpected", e);
                         }
                     }
                 });
 
                 alert.setNegativeButton("Cancel", null);
                 alert.show();
             }
         });
 
         syncRecord.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 try {
                     if (Jump.getSignedInUser() == null) {
                         Toast.makeText(MainActivity.this, "Can't sync without record instance.",
                                 Toast.LENGTH_LONG).show();
                         return;
                     }
 
                     Jump.getSignedInUser().synchronize(new CaptureApiRequestCallback() {
                         public void onSuccess() {
                             Toast.makeText(MainActivity.this, "Record updated", Toast.LENGTH_LONG).show();
                         }
 
                         public void onFailure(CaptureApiError e) {
                             Toast.makeText(MainActivity.this, "Record update failed, error logged",
                                     Toast.LENGTH_LONG).show();
                             LogUtils.loge(e.toString());
                         }
                     });
                 } catch (Capture.InvalidApidChangeException e) {
                     throw new RuntimeException("Unexpected", e);
                 }
             }
         });
     }
 
     private Button makeButton(LinearLayout linearLayout, String label) {
         Button button = new Button(this);
         button.setText(label);
         button.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
         linearLayout.addView(button);
         return button;
     }
 
     @Override
     protected void onPause() {
         Jump.saveToDisk(this);
         super.onPause();
     }
 
     private static void enableStrictMode() {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectAll()
         //        .detectDiskReads()
         //        .detectDiskWrites()
         //        .detectNetwork()   // or .detectAll() for all detectable problems
                 .penaltyLog()
         //        .penaltyDeath()
                 .build());
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                 //.detectAll()
                 //.detectActivityLeaks()
                 //.detectLeakedSqlLiteObjects()
                 //.detectLeakedClosableObjects()
                 .penaltyLog()
                 //.penaltyDeath()
                 .build());
     }
 }
