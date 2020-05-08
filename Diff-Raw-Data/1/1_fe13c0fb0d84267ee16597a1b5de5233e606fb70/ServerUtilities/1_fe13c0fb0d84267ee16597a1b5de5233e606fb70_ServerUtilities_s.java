 package com.omgren.apps.smsgcm.client;
 
 import static com.omgren.apps.smsgcm.client.CommonUtilities.SERVER_URL;
 import static com.omgren.apps.smsgcm.client.CommonUtilities.TAG;
 import static com.omgren.apps.smsgcm.client.CommonUtilities.displayMessage;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.google.gson.Gson;
 import com.omgren.apps.smsgcm.common.SmsMessageDummy;
 
 /**
  * Helper class used to communicate with the SMSGCM server.
  */
 public final class ServerUtilities {
 
   private static final int MAX_ATTEMPTS = 5;
   private static final int BACKOFF_MILLI_SECONDS = 2000;
   private static final Random random = new Random();
 
   /**
    * Register this account/device pair within the server.
    *
    * @param context
    * @param regId
    *
    * @return whether the registration succeeded or not.
    */
   public static boolean register(final Context context, final String regId) {
     Log.i(TAG, "registering device (regId = " + regId + ")");
     String serverUrl = SERVER_URL + "/register";
     Map<String, String> params = new HashMap<String, String>();
     params.put("regId", regId);
     long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
     // Once GCM returns a registration id, we need to register it in the
     // demo server. As the server might be down, we will retry it a couple
     // times.
     for (int i = 1; i <= MAX_ATTEMPTS; i++) {
       Log.d(TAG, "Attempt #" + i + " to register");
       try {
         displayMessage(context, context.getString(
               R.string.server_registering, i, MAX_ATTEMPTS));
         HttpUtilities.post(context, serverUrl, params);
         GCMRegistrar.setRegisteredOnServer(context, true);
         String message = context.getString(R.string.server_registered);
         displayMessage(context, message);
         return true;
       } catch (CertException e) {
         // having problems using ssl
         return false;
       } catch (IOException e) {
         // Here we are simplifying and retrying on any error; in a real
         // application, it should retry only on unrecoverable errors
         // (like HTTP error code 503).
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
    *
    * @param context
    * @param regId
    */
   public static void unregister(final Context context, final String regId) {
     Log.i(TAG, "unregistering device (regId = " + regId + ")");
     String serverUrl = SERVER_URL + "/unregister";
     Map<String, String> params = new HashMap<String, String>();
     params.put("regId", regId);
     try {
       HttpUtilities.post(context, serverUrl, params);
       GCMRegistrar.setRegisteredOnServer(context, false);
       String message = context.getString(R.string.server_unregistered);
       displayMessage(context, message);
     } catch (IOException e) {
       // At this point the device is unregistered from GCM, but still
       // registered in the server.
       // We could try to unregister again, but it is not necessary:
       // if the server tries to send a message to the device, it will get
       // a "NotRegistered" error message and should unregister the device.
       String message = context.getString(R.string.server_unregister_error,
           e.getMessage());
       displayMessage(context, message);
     }
   }
 
 
   /**
    * Downloads messages queued by server to be sent over SMS.
    *
    * @return list of messages.
    */
   public static SmsMessageDummy[] downloadMessages(final Context context){
     String contents = "[]";
     try {
       contents = HttpUtilities.get(context, SERVER_URL + "/messages");
       Log.i(TAG, "downloaded messages: " + contents);
     } catch (IOException e) {
       String message = context.getString(R.string.server_connect_error, e.getMessage());
       displayMessage(context, message);
     }
 
     SmsMessageDummy[] derps = (new Gson()).fromJson(contents, SmsMessageDummy[].class);
 
     return derps;
   }
 
   /**
    * Uploads messages to server received by phone's SMS.
    *
    * @param msg message received by phone
    */
   public static void uploadMessage(final Context context, SmsMessageDummy msg){
     // post uses a HashMap
     Map<String, String> args = new HashMap<String, String>();
     args.put("name", msg.name);
     args.put("address", msg.address);
     args.put("message", msg.message);
     args.put("time", msg.time.toString());
 
     try {
       HttpUtilities.post(context, SERVER_URL + "/receiveMessage", args);
     } catch(IOException e){
       Log.e(TAG, "Error uploading message: " + e);
       String message = context.getString(R.string.server_connect_error, e.getMessage());
       displayMessage(context, message);
     }
   }
 }
