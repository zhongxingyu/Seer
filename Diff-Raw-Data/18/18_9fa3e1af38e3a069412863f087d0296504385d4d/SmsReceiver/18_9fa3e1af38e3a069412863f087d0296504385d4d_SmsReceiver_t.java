 package com.nebkat.smsalarm;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.SmsMessage;
 
 public class SmsReceiver extends BroadcastReceiver {
     // Statics
     private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
     private static final String EXTRA_SMS_PDUS = "pdus";
 
     @Override
     public void onReceive(Context context, Intent intent) {
         if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {
             Bundle extras = intent.getExtras();
             if (extras != null) {
                 SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
 
                 SmsMessage[] messages = getMessagesFromIntent(intent);
                 for (SmsMessage sms : messages) {
                     String body = sms.getMessageBody();
                     // TODO: whitelist/blacklist of allowed senders
                     // String address = sms.getOriginatingAddress();
 
                     String activationSms = preferences.getString(SetupActivity.PREFERENCES_ACTIVATION_SMS,
                             context.getResources().getString(R.string.config_default_activation_sms));
 
                     if (body.equals(activationSms)) {
                         Intent alarmIntent = new Intent(context, AlarmDialogActivity.class);
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                         context.startActivity(alarmIntent);
                     }
                 }
             }
         }
     }
 
     public SmsMessage[] getMessagesFromIntent(Intent intent) {
         Object[] messages = (Object[]) intent.getSerializableExtra(EXTRA_SMS_PDUS);
         byte[][] pduObjs = new byte[messages.length][];
 
         for (int i = 0; i < messages.length; i++) {
             pduObjs[i] = (byte[]) messages[i];
         }
         byte[][] pdus = new byte[pduObjs.length][];
         int pduCount = pdus.length;
         SmsMessage[] msgs = new SmsMessage[pduCount];
         for (int i = 0; i < pduCount; i++) {
             pdus[i] = pduObjs[i];
             msgs[i] = SmsMessage.createFromPdu(pdus[i]);
         }
         return msgs;
     }
 }
