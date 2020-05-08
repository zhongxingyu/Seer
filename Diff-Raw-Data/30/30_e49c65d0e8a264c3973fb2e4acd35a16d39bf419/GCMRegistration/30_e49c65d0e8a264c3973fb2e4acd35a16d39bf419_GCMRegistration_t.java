 /*
  * Copyright 2012 Google Inc.
  * Copyright (c) 2013 Mark Prichard, CloudBees
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cloudbees.gasp.gcm;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.cloudbees.gasp.activity.MainActivity;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 
 /**
  * Helper class used to communicate with the demo server.
  */
 public final class GCMRegistration {
 
     static final String TAG = GCMRegistration.class.getName();
 
     // Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     static final String SERVER_URL = "http://gasp-push-server.partnerdemo.cloudbees.net/gcm";
 
    //public static String getServerUrl() {
    //    return SERVER_URL;
    //}
 
     // Google API project id registered to use GCM.
     private static final String SENDER_ID = "960428562804";
 
     public static String getSenderId() {
         return SENDER_ID;
     }
 
     // Intent used to display a message in the screen.
    //private static final String DISPLAY_MESSAGE_ACTION =
    //        "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";
 
    //public static String getDisplayMessageAction() {
    //    return DISPLAY_MESSAGE_ACTION;
    //}
 
     //Intent's extra that contains the message to be displayed.
    //private static final String EXTRA_MESSAGE = "message";
 
 
    //public static String getExtraMessage() {
    //    return EXTRA_MESSAGE;
    //}
 
     /**
      * Notifies UI to display a message.
      * <p>
      * This method is defined in the common helper because it's used both by
      * the UI and the background service.
      *
      * @param context application's context.
      * @param message message to be displayed.
      */
     public static void displayMessage(Context context, String message) {
         Intent broadcastIntent = new Intent();
         broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
         broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
         broadcastIntent.putExtra(MainActivity.ResponseReceiver.PARAM_OUT_MSG, message);
         context.sendBroadcast(broadcastIntent);
     }
 
     private static final int MAX_ATTEMPTS = 5;
     private static final int BACKOFF_MILLI_SECONDS = 2000;
     private static final Random random = new Random();
 
     /**
      * Register this account/device pair within the server.
      *
      * @return whether the registration succeeded or not.
      */
     public static boolean register(final Context context, final String regId) {
         Log.i(TAG, "Registering device (regId = " + regId + ")");
         String serverUrl = SERVER_URL + "/register";
         Map<String, String> params = new HashMap<String, String>();
         params.put("regId", regId);
         long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
 
         for (int i = 1; i <= MAX_ATTEMPTS; i++) {
             try {
                 post(serverUrl, params);
                 Log.d(TAG, "Registered: " + regId);
                 String message = context.getString(R.string.server_registered);
                 displayMessage(context, message);
                 return true;
             } catch (IOException e) {
                 Log.e(TAG, "Failed to register on attempt " + i, e);
                 if (i == MAX_ATTEMPTS) {
                     break;
                 }
                 try {
                     Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                     Thread.sleep(backoff);
                 } catch (InterruptedException e1) {
                     // Activity finished before we complete - exit.
                     Log.d(TAG, "Thread interrupted: abort remaining retries!");
                     Thread.currentThread().interrupt();
                     return false;
                 }
                 // increase backoff exponentially
                 backoff *= 2;
             }
         }
         String message = context.getString(R.string.server_register_error,
                 MAX_ATTEMPTS);
         displayMessage(context, message);
         return false;
     }
 
     /**
      * Unregister this account/device pair within the server.
      */
     public static void unregister(final Context context, final String regId) {
         Log.i(TAG, "Unregistering device (regId = " + regId + ")");
         String serverUrl = SERVER_URL + "/unregister";
         Map<String, String> params = new HashMap<String, String>();
         params.put("regId", regId);
 
         String message;
         long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
 
         for (int i = 1; i <= MAX_ATTEMPTS; i++) {
             try {
                 post(serverUrl, params);
                 Log.d(TAG, "Unregistered: " + regId);
                 message = context.getString(R.string.server_unregistered);
                 displayMessage(context, message);
                 return;
 
             } catch (IOException e) {
                 Log.e(TAG, "Failed to unregister on attempt " + i, e);
                 if (i == MAX_ATTEMPTS) {
                     message = context.getString(R.string.server_unregister_error, e.getMessage());
                    displayMessage(context, message);
                     break;
                 }
                 try {
                     Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                     Thread.sleep(backoff);
                 } catch (InterruptedException e1) {
                     // Activity finished before we complete - exit.
                     Log.d(TAG, "Thread interrupted: abort remaining retries!");
                     Thread.currentThread().interrupt();
                     return;
                 }
                 // increase backoff exponentially
                 backoff *= 2;
             }
         }
     }
 
     /**
      * Issue a POST request to the server.
      *
      * @param endpoint POST address.
      * @param params request parameters.
      *
      * @throws IOException propagated from POST.
      */
     private static void post(String endpoint, Map<String, String> params)
             throws IOException {
         URL url;
         try {
             url = new URL(endpoint);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("invalid url: " + endpoint);
         }
         StringBuilder bodyBuilder = new StringBuilder();
         Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
         // constructs the POST body using the parameters
         while (iterator.hasNext()) {
             Entry<String, String> param = iterator.next();
             bodyBuilder.append(param.getKey()).append('=')
                     .append(param.getValue());
             if (iterator.hasNext()) {
                 bodyBuilder.append('&');
             }
         }
         String body = bodyBuilder.toString();
         Log.v(TAG, "Posting '" + body + "' to " + url);
         byte[] bytes = body.getBytes();
         HttpURLConnection conn = null;
         try {
             conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             conn.setUseCaches(false);
             conn.setFixedLengthStreamingMode(bytes.length);
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
             // post the request
             OutputStream out = conn.getOutputStream();
             out.write(bytes);
             out.close();
             // handle the response
             int status = conn.getResponseCode();
             if (status != 200) {
               throw new IOException("Post failed with error code " + status);
             }
         } finally {
             if (conn != null) {
                 conn.disconnect();
             }
         }
       }
 }
