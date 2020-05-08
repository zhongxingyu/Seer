 package com.example;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class StartupReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context c, Intent intent) {
         AlarmManager alarmManager = (AlarmManager)c.getSystemService(c.ALARM_SERVICE);
 
         long time = System.currentTimeMillis()/1000;
         long last_hour = time - (time % 3600);
         PendingIntent sender = PendingIntent.getBroadcast(c, 0, new Intent(c, Alarm.class), 0);
 
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, last_hour * 1000, 5 * 60 * 1000, sender);    }
 }
