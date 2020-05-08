 /*
  * Copyright 2012 Google Inc.
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
 
 import android.annotation.TargetApi;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.util.Log;
 
 import com.cloudbees.gasp.activity.MainActivity;
 import com.cloudbees.gasp.service.RestaurantUpdateService;
 import com.cloudbees.gasp.service.ReviewUpdateService;
 import com.cloudbees.gasp.service.SyncIntentParams;
 import com.cloudbees.gasp.service.UserUpdateService;
 import com.google.android.gcm.GCMBaseIntentService;
 import com.google.android.gcm.GCMRegistrar;
 
 import static com.cloudbees.gasp.gcm.CommonUtilities.SENDER_ID;
 import static com.cloudbees.gasp.gcm.CommonUtilities.displayMessage;
 
 /**
  * IntentService responsible for handling GCM messages.
  */
 public class GCMIntentService extends GCMBaseIntentService {
 
     @SuppressWarnings("hiding")
     private static final String TAG = "GCMIntentService";
 
     public GCMIntentService() {
         super(SENDER_ID);
     }
 
     @Override
     protected void onRegistered(Context context, String registrationId) {
         Log.i(TAG, "Device registered: regId = " + registrationId);
         displayMessage(context, getString(R.string.gcm_registered));
         ServerUtilities.register(context, registrationId);
     }
 
     @Override
     protected void onUnregistered(Context context, String registrationId) {
         Log.i(TAG, "Device unregistered");
         displayMessage(context, getString(R.string.gcm_unregistered));
         if (GCMRegistrar.isRegisteredOnServer(context)) {
             ServerUtilities.unregister(context, registrationId);
         } else {
             // This callback results from the call to unregister made on
             // ServerUtilities when the registration to the server failed.
             Log.i(TAG, "Ignoring unregister callback");
         }
     }
 
     @Override
     protected void onMessage(Context context, Intent intent) {
         int index = Integer.parseInt(intent.getStringExtra("id"));
         String table = intent.getStringExtra("table");
        Log.i(TAG, "New" + table + ": " + index);
 
         Intent updateIntent;
         try {
             if (table.matches("reviews")) {
                updateIntent = new Intent(this, ReviewUpdateService.class);
                updateIntent.putExtra(SyncIntentParams.PARAM_ID, index);
                startService(updateIntent);
             }
             else if (table.matches("restaurants")) {
                updateIntent = new Intent(this, RestaurantUpdateService.class);
                updateIntent.putExtra(SyncIntentParams.PARAM_ID, index);
                startService(updateIntent);
             }
             else if (table.matches("users")) {
                updateIntent = new Intent(this, UserUpdateService.class);
                updateIntent.putExtra(SyncIntentParams.PARAM_ID, index);
                startService(updateIntent);
             }
             else {
                 Log.e(TAG, "Error: unknown table: " + table);
                 return;
             }
 
             // Send notification message for message bar display etc
            generateNotification(context, "New" + table + ": " + index);
 
         } catch (Exception e) {
             Log.e(TAG, e.getStackTrace().toString());
         }
     }
 
     @Override
     protected void onDeletedMessages(Context context, int total) {
         Log.i(TAG, "Received deleted messages notification");
         String message = getString(R.string.gcm_deleted, total);
 
         displayMessage(context, message);
         generateNotification(context, message);
     }
 
     @Override
     public void onError(Context context, String errorId) {
         Log.i(TAG, "Received error: " + errorId);
         displayMessage(context, getString(R.string.gcm_error, errorId));
     }
 
     @Override
     protected boolean onRecoverableError(Context context, String errorId) {
         // log message
         Log.i(TAG, "Received recoverable error: " + errorId);
         displayMessage(context, getString(R.string.gcm_recoverable_error,
                 errorId));
         return super.onRecoverableError(context, errorId);
     }
 
     /**
      * Issues a notification to inform the user that server has sent a message.
      */
     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     // TODO Fix Notification version issues
     private static void generateNotification(Context context, String message) {
         int icon = R.drawable.ic_stat_gcm;
         long when = System.currentTimeMillis();
         NotificationManager notificationManager = (NotificationManager)
                 context.getSystemService(Context.NOTIFICATION_SERVICE);
         Notification notification = new Notification.
                                         Builder(context).
                                         setContentTitle("New Gasp! Update").
                                         setSmallIcon(R.drawable.ic_stat_gcm).
                                         build();
         String title = context.getString(R.string.app_name);
         Intent notificationIntent = new Intent(context, MainActivity.class);
 
         notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                 Intent.FLAG_ACTIVITY_SINGLE_TOP);
         PendingIntent intent =
                 PendingIntent.getActivity(context, 0, notificationIntent, 0);
 
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notificationManager.notify(0, notification);
     }
 
 }
