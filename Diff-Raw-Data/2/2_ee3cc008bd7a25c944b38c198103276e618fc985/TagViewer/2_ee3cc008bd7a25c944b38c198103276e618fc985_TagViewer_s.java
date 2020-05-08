 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 package org.thinkfree.NFC;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Parcelable;
 import android.util.Log;
 
 import org.thinkfree.NFC.record.ParsedNdefRecord;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 
 /**
  * An {@link Activity} which handles a broadcast of a new tag that the device
  * just discovered.
  */
 public class TagViewer extends Activity {
 
     static final String TAG = "NFC";
     Activity mAct;
 
     /**
      * This activity will finish itself in this amount of time if the user
      * doesn't do anything.
      */
     static final int ACTIVITY_TIMEOUT_MS = 1 * 1000;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 
         //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         //StrictMode.setThreadPolicy(policy);
 
         super.onCreate(savedInstanceState);
         resolveIntent(getIntent());
 
         mAct = this;
     }
 
     void resolveIntent(Intent intent) {
 
         Log.e(TAG, "Tag detected");
         String action = intent.getAction();
 
         Log.e(TAG, action);
 
         if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
 
             Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
             NdefMessage[] msgs;
 
             if (rawMsgs != null) {
                 msgs = new NdefMessage[rawMsgs.length];
                 for (int i = 0; i < rawMsgs.length; i++) {
                     msgs[i] = (NdefMessage) rawMsgs[i];
                 }
             } else {
                 // Unknown tag type
                 Log.e(TAG, "Unknown tag type");
                 byte[] empty = new byte[] {};
                 NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                 NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                 msgs = new NdefMessage[] {msg};
             }
             // Setup the views
             processMessage(msgs);
         }
         else {
 
             Log.e(TAG, "Unknown intent " + intent);
             finish();
             return;
         }
     }
 
     void processMessage(NdefMessage[] msgs) {
 
         Log.e(TAG, "Processing message");
 
         if (msgs == null || msgs.length == 0) {
             finish();
             return;
         }
         List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
         final int size = records.size();
 
         for (int i = 0; i < size; i++) {
 
             ParsedNdefRecord record = records.get(i);
 
             String str = record.getText();
             String key = str.split("=")[0];
 
             Log.e(TAG, "Key : " + key);
 
             if ( key.equals("Scene")){
 
                 String value = str.split("=")[1];
                 Log.e(TAG,"Making a request to : " + value);
 
                 new MakeDomoLoadSceneRestRequest().execute(value);
             }
         }
 
         return;
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         setIntent(intent);
         resolveIntent(intent);
     }
 
     class MakeDomoLoadSceneRestRequest extends AsyncTask<String, Object, Object> {
 
         @Override
         protected Object doInBackground(String... params) {
 
             Properties prop = new Properties();
             try {
 
                prop.load(new FileInputStream(Environment.getExternalStorageDirectory().toString() + "/.on-domo-qt/config.ini"));
             }
             catch (FileNotFoundException e1) {
 
                 e1.printStackTrace();
             }
             catch (IOException e1) {
 
                 e1.printStackTrace();
             }
 
             String url = prop.getProperty("sceneUrl") + params[0];
 
             Log.e(TAG, "Requesting to url : " + url);
 
             HttpClient httpclient = new DefaultHttpClient();
             HttpResponse response = null;
 
             try {
                 HttpGet get = new HttpGet(url);
                 response = httpclient.execute(get);
             }
             catch (ClientProtocolException e) {
 
                 Log.e(TAG, "Error 1");
                 e.printStackTrace();
             }
             catch (IOException e) {
 
                 Log.e(TAG, "Error 2");
                 e.printStackTrace();
             }
             catch(Exception e){
 
                 Log.e(TAG, "Exception executing request");
                 e.printStackTrace();
             }
 
             try{
 
                 StatusLine statusLine = response.getStatusLine();
 
                 if(statusLine.getStatusCode() == HttpStatus.SC_OK){
 
                     Log.e(TAG,"Request ok");
                 }
                 else{
 
                     Log.e(TAG,"Request failed");
                 }
             }
             catch(Exception e){
 
                 Log.e(TAG, "Exception getting status");
                 e.printStackTrace();
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
 
             Log.e(TAG, "Closing activity");
 
             mAct.finish();
         }
     }
 }
