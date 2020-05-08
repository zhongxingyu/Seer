 package com.webshrub.moonwalker.androidapp;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.telephony.TelephonyManager;
 
 public class DNDManagerBroadCastReceiver extends BroadcastReceiver {
     private static final String ANDROID_PROVIDER_TELEPHONY_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
     private static final String ANDROID_INTENT_ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
 
     @Override
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
         if (action != null && !action.equals("")) {
             if (action.equals(ANDROID_PROVIDER_TELEPHONY_SMS_RECEIVED)) {
                 String incomingNumber = DNDManagerUtil.getIncomingNumberFromSms(intent);
                 checkAndShowNotification(context, incomingNumber);
             } else if (action.equals(ANDROID_INTENT_ACTION_PHONE_STATE)) {
                 String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                 String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                 if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                     checkAndShowNotification(context, incomingNumber);
                 }
             }
         }
     }
 
 
     public void checkAndShowNotification(Context context, String incomingNumber) {
         String contactName = DNDManagerUtil.getContactName(context, incomingNumber);
         if (contactName.equals("")) {
             buildNotification(context);
         } else if (DNDManagerHtmlHelper.getContactLogFlag(context)) {
             buildNotification(context);
         }
     }
 
     public void buildNotification(Context context) {
         NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
             Intent notificationIntent = new Intent(context, DNDManagerDialogBox.class);
             PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
             int notificationId = 1;
             Notification notification = new Notification.Builder(context)
                     .setSmallIcon(R.drawable.dnd_icon)
                     .setContentTitle("DND Manager")
                    .setContentText("You have spam calls and sms in your inbox. Report now?")
                     .setContentIntent(pendingIntent).build();
             notificationManager.notify(notificationId, notification);
         } else {
            Notification notification = new Notification(R.drawable.dnd_icon, "You have spam calls and sms in your inbox. Report now?", System.currentTimeMillis());
             Intent notificationIntent = new Intent(context, DNDManagerDialogBox.class);
             PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            notification.setLatestEventInfo(context, "DND Manager", "You have spam calls and sms in your inbox. Report now?", pendingIntent);
             notificationManager.notify(1, notification);
         }
     }
 }
