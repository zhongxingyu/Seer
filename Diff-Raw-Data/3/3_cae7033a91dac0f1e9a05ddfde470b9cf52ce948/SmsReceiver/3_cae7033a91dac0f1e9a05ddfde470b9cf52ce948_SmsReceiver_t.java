 package com.marwinxxii.ccardstats;
 
 import com.marwinxxii.ccardstats.db.DBHelper;
 import com.marwinxxii.ccardstats.gui.CardListActivity;
 import com.marwinxxii.ccardstats.notifications.SmsNotification;
 import com.marwinxxii.ccardstats.notifications.SmsParser;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsMessage;
 
 public class SmsReceiver extends BroadcastReceiver {
 
     @Override
     public void onReceive(Context context, Intent intent) {
         Bundle bundle = intent.getExtras();
         if (bundle == null)
             return;
         Object[] pdus = (Object[]) bundle.get("pdus");
         DBHelper helper = new DBHelper(context);
         for (int i = 0; i < pdus.length; i++) {
             SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
             SmsNotification notif = SmsParser.parse(
                     sms.getDisplayOriginatingAddress(), sms.getDisplayMessageBody());
             if (notif != null) {
                 helper.saveCard(notif.card, notif.card, notif.balance);
                 helper.addNotification(notif);
             }
         }
         CardListActivity.prepareCardsInfo(helper, helper.getCards());
        helper.close();
     }
 
 }
