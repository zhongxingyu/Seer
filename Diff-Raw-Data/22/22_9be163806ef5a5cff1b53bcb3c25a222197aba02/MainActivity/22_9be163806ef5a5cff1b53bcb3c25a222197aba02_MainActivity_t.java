 /*
  * Copyright (C) 2008 ZXing authors
  * and Common Good Finance (for the modifications)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.rcredits.pos;
 
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
import android.widget.Button;
 import android.widget.TextView;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.rcredits.zxing.client.android.CaptureActivity;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Give the user (usually a cashier) a button to press, to scan rCards at POS.
  * When appropriate, also offer a button to show the customer's balance and a button to undo the last transaction.
  * This activity restarts (and cancels all child processes) before each new scan. See Act.restart().
  * @todo: rework deprecated KeyguardManager lines with WindowManager.FLAG_DISMISS_KEYGUARD (see zxing code for tips)
  */
     public final class MainActivity extends Act {
     private final Act act = this;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
         KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
         lock.disableKeyguard();
 
         findViewById(R.id.undo_last).setVisibility((A.agentCan(A.CAN_REFUND) && !A.lastTx.equals("")) ? View.VISIBLE : View.GONE);
         findViewById(R.id.show_balance).setVisibility(A.balance.equals("") ? View.GONE : View.VISIBLE);
         if (A.agent.equals("")) {
             act.mention("Welcome!\n\nPress the SCAN button to sign in with your Company Agent rCard.");
         } else {
            ((Button) findViewById(R.id.signed_as)).setText("You: " + A.agentName);
         }
 
         if (!A.failMessage.equals("")) {
             act.sayError(A.failMessage, null); // show failure message from previous (failed) activity
             A.failMessage = "";
         }
 
         if (A.update.equals("1")) {
             act.askOk("Okay to update now? (takes a couple minutes)", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     dialog.cancel();
                     A.update = "";
                     Intent intent = new Intent(Intent.ACTION_VIEW);
                     intent.setDataAndType(Uri.fromFile(getFileStreamPath(A.APP_FILENAME)), "application/vnd.android.package-archive");
                     startActivity(intent);
                 }
             });
         } else if (!A.update.equals("")) {
             new downloadUpdate().execute(A.update); // download the update in the background
         }
     }
 
     /**
      * Download and install the latest app update, mostly in the background.
      * (untested)
      */
     private class downloadUpdate extends AsyncTask<String, Void, String> {
         @Override
         protected String doInBackground(String... urls) { // must be "String... something"
             String url = urls[0];
 
             HttpClient http = new DefaultHttpClient();
             HttpGet request = new HttpGet(url);
 
             try {
                 HttpResponse response = http.execute(request);
                 InputStream fileStream = response.getEntity().getContent();
                 FileOutputStream output = openFileOutput(A.APP_FILENAME, MODE_WORLD_READABLE); // deprecated but doesn't work otherwise
 
                 byte[] buffer = new byte[1024];
                 int len;
                 while ((len = fileStream.read(buffer)) > 0) output.write(buffer, 0, len);
                 fileStream.close();
                 output.close();
             } catch (ClientProtocolException e) {
                 e.printStackTrace();
                 return e.getMessage();
             } catch (IOException e) {
                 e.printStackTrace();
                 return e.getMessage();
             }
 
             return A.MSG_DOWNLOAD_SUCCESS;
         }
 
         @Override
         protected void onPostExecute(String msg) {
             if (msg.equals(A.MSG_DOWNLOAD_SUCCESS)) {
                 A.update = "1";
                 act.mention(msg);
             } else act.sayError("Download failed: " + msg, null);
         }
     }
 
     /**
      * Start the Capture activity (when the user presses the SCAN button).
      * @param v
      */
     public void doScan(View v) {
         Intent intent = new Intent(this, CaptureActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
         startActivity(intent);
     }
 
     /**
      * Show the last customer's balance (when the user presses the "Balance" button).
      * @param v
      */
     public void doShowBalance(View v) {
         act.sayOk("Customer Balance", A.balance, null);
     }
 
     /**
      * Undo the last transaction after confirmation (when the user presses the "Undo" button).
      * @param v
      */
     public void doUndo(View v) {
         act.askOk(A.undo, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
                 A.doTx(act, "undo", A.auPair(null, "tx", A.lastTx));
             }
         });
     }
 
     /**
     * Sign the cashier out after confirmation.
     */
    public void doSignOut(View v) {
        if (A.agent.equals("")) {
            act.mention("You are not signed in.");
            return; // not signed in
        }
        act.askOk("Sign out?", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                A.agent = A.agentName = ""; // sign out
                ((Button) findViewById(R.id.signed_as)).setText(R.string.not_signed_in);
            }
        });
    }

    /**
      * Just pretend to scan, when using AVD (since the webcam feature doesn't work). For testing, of course.
      * @param v
      */
     public void onFakeScan(View v) {
         if (!A.FAKE_SCAN) return;
         try {
             act.onScan(new rCard("HTTP://NEW.RC2.ME/I/" + (A.agent.equals("") ? "ZZD-" : "ZZA.") + "zot"));
         } catch (Exception e) {}
     }
 }
