 package com.github.marwinxxii.ccardstats;
 
 import com.github.marwinxxii.ccardstats.db.DBHelper;
 import com.github.marwinxxii.ccardstats.gui.CardListActivity;
 import com.github.marwinxxii.ccardstats.notifications.SmsNotification;
 import com.github.marwinxxii.ccardstats.notifications.SmsParser;
 
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
        String address = null;
        StringBuilder builder = new StringBuilder();
         for (int i = 0; i < pdus.length; i++) {
             SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
            if (address == null) {
                address = sms.getDisplayOriginatingAddress();
            } else if (!address.equals(sms.getDisplayOriginatingAddress())) {
                //error
                return;
             }
            builder.append(sms.getDisplayMessageBody());
        }
        SmsNotification notif = SmsParser.parse(address, builder.toString());
        if (notif != null) {
            helper.saveCard(notif.card, notif.card, notif.balance);
            helper.addNotification(notif);
         }
         CardListActivity.prepareCardsInfo(helper, helper.getCards());
         helper.close();
     }
 
 }
