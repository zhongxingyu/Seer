 package com.mytree.utility;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.telephony.SmsManager;
 import android.widget.Toast;
 import android.Manifest;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Jeanma
  * Date: 13-4-11
  * Time: 下午6:33
  * To change this template use File | Settings | File Templates.
  */
 public class SMS {
 
     private static final String ACTION_SMS_SENT = "ACTION_SMS_SENT";
     private static final String ACTION_SMS_DELIVERED = "ACTION_SMS_SENT";
 
     public static void quickSend(final Context context, final String phoneNumber, String message) {
         send(context, phoneNumber, message, null, null);
     }
 
     public static void send(final Context context, final String phoneNumber, String message) {
         send(context, phoneNumber, message, new CallbackInterface() {
                     @Override
                     public void callback() {
                         //To change body of implemented methods use File | Settings | File Templates.
                     }
 
                     @Override
                     public void callback(Object args) {
                         //To change body of implemented methods use File | Settings | File Templates.
                         Integer code = Integer.valueOf(args.toString());
                         switch (code) {
                             case Activity.RESULT_OK:
                                 Toast.makeText(context, String.format("短信已送出,%s", phoneNumber), Toast.LENGTH_LONG).show();
                                 break;
                             default:
                                 Toast.makeText(context, String.format("短信送出失败,%s", phoneNumber), Toast.LENGTH_LONG).show();
                                 break;
                         }
                     }
                 }, new CallbackInterface() {
                     @Override
                     public void callback() {
                         //To change body of implemented methods use File | Settings | File Templates.
                     }
 
                     @Override
                     public void callback(Object args) {
                         //To change body of implemented methods use File | Settings | File Templates.
                         Toast.makeText(context, String.format("短信已送达至%s", phoneNumber), Toast.LENGTH_LONG).show();
                     }
                 }
         );
     }
 
     public static void send(Context context,
                             String phoneNumber,
                             String message,
                             final CallbackInterface sent,
                             final CallbackInterface delivered) {
         if (!Permission.hasPermission(context, Manifest.permission.SEND_SMS, true)) {
             return;
         }
 
         SmsManager sms = SmsManager.getDefault();
 
         //注册BroadcastReceiver
         Intent sentIntent = new Intent(ACTION_SMS_SENT);
         PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, 0);
         context.registerReceiver(new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 if (sent != null) {
                     sent.callback(getResultCode());
                 }
             }
         }, new IntentFilter(ACTION_SMS_SENT));
         Intent deliveredIntent = new Intent(ACTION_SMS_DELIVERED);
         PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, deliveredIntent, 0);
         context.registerReceiver(new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 if (delivered != null) {
                     delivered.callback(getResultCode());
                 }
             }
         }, new IntentFilter(ACTION_SMS_DELIVERED));
 
        if (message.length() > 70) {
             //直接发送单个短信
             sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
         } else {
             //发送多个短信
             ArrayList<String> multipleMessage = sms.divideMessage(message);
             ArrayList<PendingIntent> sentPIS = new ArrayList<PendingIntent>();
             sentPIS.add(sentPI);
             ArrayList<PendingIntent> deliveredPIS = new ArrayList<PendingIntent>();
             deliveredPIS.add(deliveredPI);
             sms.sendMultipartTextMessage(phoneNumber, null, multipleMessage, sentPIS, deliveredPIS);
         }
     }
 
     public static boolean isPhoneNumber(String phoneNumber) {
         Pattern pattern = Pattern.compile("^1[0-9]{10}$");
         Matcher matcher = pattern.matcher(phoneNumber);
         if (matcher.matches()) {
             return true;
         }
         return false;
     }
 
     public static boolean isEmptyMessage(String message) {
         message = message.trim();
         return message.isEmpty();
     }
 }
