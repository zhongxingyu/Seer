 package com.omgren.apps.smsgcm.client;
 
 import static com.omgren.apps.smsgcm.client.CommonUtilities.SENDER_ID;
 import static com.omgren.apps.smsgcm.client.CommonUtilities.displayMessage;
 import static com.omgren.apps.smsgcm.client.ServerUtilities.downloadMessages;
 //import android.app.Notification;
 //import android.app.NotificationManager;
 //import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 import com.google.android.gcm.GCMRegistrar;
 import com.omgren.apps.smsgcm.common.SmsMessageDummy;
 
 /**
  * IntentService responsible for handling GCM messages.
  */
 public class GCMIntentService extends GCMBaseIntentService {
 
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
 
     /**
      * Event when message is queued on GCM server. Download queued messages
      * from the server, then send them off.
      */
     @Override
     protected void onMessage(Context context, Intent intent) {
         String message = getString(R.string.gcm_message);
         Log.i(TAG, "Received GCM message");
        displayMessage(context, message);
 
         //generateNotification(context, message);
 
         SmsMessageDummy[] msgs = downloadMessages(context);
         if( msgs != null )
           for(SmsMessageDummy msg : msgs)
             (new SmsSender()).send(context, msg.address, msg.message);
 
     }
 
     @Override
     protected void onDeletedMessages(Context context, int total) {
         Log.i(TAG, "Received deleted messages notification");
         String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
         // notifies user
         //generateNotification(context, message);
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
      *
     @SuppressWarnings("deprecation")
     private static void generateNotification(Context context, String message) {
         int icon = R.drawable.ic_stat_gcm;
         long when = System.currentTimeMillis();
         NotificationManager notificationManager = (NotificationManager)
                 context.getSystemService(Context.NOTIFICATION_SERVICE);
         Notification notification = new Notification(icon, message, when);
         String title = context.getString(R.string.app_name);
         Intent notificationIntent = new Intent(context, MainActivity.class);
         // set intent so it does not start a new activity
         notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                 Intent.FLAG_ACTIVITY_SINGLE_TOP);
         PendingIntent intent =
                 PendingIntent.getActivity(context, 0, notificationIntent, 0);
         notification.setLatestEventInfo(context, title, message, intent);
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notificationManager.notify(0, notification);
     }
     /**/
 
 }
