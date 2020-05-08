 package com.varunarl.invisibletouch.internal;
 
import android.app.AlarmManager;
import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;

import com.varunarl.invisibletouch.utils.Log;
 import com.varunarl.invisibletouch.view.MainMenuActivity;
 
 /**
  * Created by vlekamwasam on 6/10/13.
  */
 public class SignalReceiver extends BroadcastReceiver {
 
     public static final String BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;
 
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
        if (action.equals(BOOT_COMPLETED)) {
             Intent i = new Intent(context, MainMenuActivity.class);
             i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent pi = PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+1000,pi);
         }
     }
 }
