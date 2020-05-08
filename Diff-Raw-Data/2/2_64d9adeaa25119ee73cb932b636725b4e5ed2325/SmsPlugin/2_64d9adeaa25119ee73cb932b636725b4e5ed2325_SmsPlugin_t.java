 package com.webshrub.moonwalker.androidapp;
 
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.provider.CallLog;
 import android.telephony.SmsManager;
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.apache.cordova.api.PluginResult.Status;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 public class SmsPlugin extends Plugin {
     public final String ACTION_SEND_SMS = "SendSMS";
     public final String CALL = "call";
     public final String SMS = "sms";
     public final String ON = "on";
     public final String OFF = "off";
     public static final String ADDRESS = "address";
     public static final String PERSON = "person";
     public static final String DATE = "date";
     public static final String READ = "read";
     public static final String STATUS = "status";
     public static final String TYPE = "type";
     public static final String BODY = "body";
     public static final int MESSAGE_TYPE_INBOX = 1;
     public static final int MESSAGE_TYPE_SENT = 2;
 
     @Override
     public PluginResult execute(String action, JSONArray arg1, String callbackId) {
         PluginResult result = new PluginResult(Status.INVALID_ACTION);
         if (action.equalsIgnoreCase(ACTION_SEND_SMS)) {
             try {
                 String phoneNumber = arg1.getString(0);
                 String message = arg1.getString(1);
                 sendSMS(phoneNumber, message);
                 String reportType = arg1.getString(2);
                 String deleteSMSFlag = arg1.getString(3);
                 String deleteSentSMSFlag = arg1.getString(4);
                 String spamNumber = arg1.getString(5);
 
                 if (ON.equalsIgnoreCase(deleteSMSFlag)) {
                     if (CALL.equalsIgnoreCase(reportType)) {
                         deleteCallLogByNumber(spamNumber);
                     } else if (SMS.equalsIgnoreCase(reportType)) {
                         deleteSmsByNumber(spamNumber);
                     }
                 }
                 if (OFF.equalsIgnoreCase(deleteSentSMSFlag)) {
                     saveSentSms(phoneNumber, message);
                 }
 
                 result = new PluginResult(Status.OK);
             } catch (JSONException ex) {
                 result = new PluginResult(Status.JSON_EXCEPTION, ex.getMessage());
             }
         }
         return result;
     }
 
     private void sendSMS(String phoneNumber, String message) {
         SmsManager manager = SmsManager.getDefault();
         PendingIntent sentIntent = PendingIntent.getActivity(this.ctx.getContext(), 0, new Intent(), 0);
         manager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
     }
 
     public void deleteCallLogByNumber(String number) {
         try {
             String queryString = CallLog.Calls.NUMBER + " = '" + number + "'";
             ctx.getContext().getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void deleteSmsByNumber(String number) {
         try {
            String queryString = "address" + " = '" + number + "'";
             ctx.getContext().getContentResolver().delete(Uri.parse("content://sms"), queryString, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void saveSentSms(String phoneNumber, String message) {
         ContentValues values = new ContentValues();
         values.put(ADDRESS, phoneNumber);
         values.put(DATE, System.currentTimeMillis());
         values.put(READ, 1);
         values.put(STATUS, -1);
         values.put(TYPE, 2);
         values.put(BODY, message);
         Uri inserted = ctx.getContext().getContentResolver().insert(Uri.parse("content://sms"), values);
     }
 }
