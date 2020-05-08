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
 import com.janrain.android.utils.LogUtils;
 import org.json.JSONException;
 
 import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
 import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
 import static com.janrain.android.capture.Capture.CaptureApiRequestCallback;
 
 public class MainActivity extends FragmentActivity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //enableStrictMode();
         LinearLayout linearLayout = new LinearLayout(this);
         linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
         linearLayout.setOrientation(LinearLayout.VERTICAL);
 
         Button testAuth = new Button(this);
         testAuth.setText("Test Capture Auth");
         testAuth.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
         linearLayout.addView(testAuth);
 
         Button dumpRecord = new Button(this);
         dumpRecord.setText("Dump Record to Log");
         dumpRecord.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
         linearLayout.addView(dumpRecord);
 
         Button touchRecord = new Button(this);
         touchRecord.setText("Edit About Me Attribute");
         touchRecord.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
         linearLayout.addView(touchRecord);
 
         Button syncRecord = new Button(this);
         syncRecord.setText("Sync Record");
         syncRecord.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
         linearLayout.addView(syncRecord);
 
         setContentView(linearLayout);
 
         Jump.init(this, "appcfamhnpkagijaeinl", "mobile-dev.janraincapture.com",
                 "gpy4j6d8bcsepkb2kzm7zp5qkk8wrza6", Jump.TraditionalSignInType.EMAIL);
 
         testAuth.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Jump.showSignInDialog(MainActivity.this, null, new Jump.SignInResultHandler(){
                     public void onSuccess() {
                         AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                         b.setMessage("success");
                         b.setNeutralButton("Dismiss", null);
                         b.show();
                     }
 
                    public void onFailure(SignInError error) {
                         AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                         b.setMessage("error:" + error);
                         b.setNeutralButton("Dismiss", null);
                         b.show();
                     }
                 });
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
 
     @Override
     protected void onPause() {
         Jump.maybeSaveUserToDisk(this);
         super.onPause();
     }
 
     private static void enableStrictMode() {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectAll()
                 .detectDiskReads()
                 .detectDiskWrites()
                 .detectNetwork()   // or .detectAll() for all detectable problems
                 .penaltyLog()
                 .penaltyDeath()
                 .build());
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                 .detectAll()
                 .detectLeakedSqlLiteObjects()
                 //.detectLeakedClosableObjects()
                 .penaltyLog()
                 .penaltyDeath()
                 .build());
     }
 }
