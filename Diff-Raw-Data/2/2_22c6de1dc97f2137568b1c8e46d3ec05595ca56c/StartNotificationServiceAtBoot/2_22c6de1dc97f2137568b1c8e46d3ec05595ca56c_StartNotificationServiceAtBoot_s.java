 package com.appchallenge.android;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 /**
  * Allows our NotificationService to continue running after a reboot, as
  * AlarmManager alarms disappear when the device powers off.
  */
 public class StartNotificationServiceAtBoot extends BroadcastReceiver {
 
     @Override
     public void onReceive(Context context, Intent intent) {
         if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
         	Intent serviceIntent = new Intent(context, NotificationService.class);
     		PendingIntent pintent = PendingIntent.getService(context, 0, serviceIntent, 0);
 
     		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 
     		if (prefs.getBoolean("notificationsEnabled", false)) {
     			// Start the NotificationService on a fixed interval.
     			Calendar cal = Calendar.getInstance();
     			AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    			Integer interval = Integer.getInteger(prefs.getString("notificationCheckInterval", "300000"));
     			alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pintent); 
     		}
         }
     }
 }
