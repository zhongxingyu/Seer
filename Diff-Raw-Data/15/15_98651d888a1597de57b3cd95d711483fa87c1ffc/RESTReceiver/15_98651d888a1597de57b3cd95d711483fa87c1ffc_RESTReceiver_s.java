 package com.jtbdevelopment.rest;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import com.jtbdevelopment.rest.helpers.NextReminderCalculator;
 import org.joda.time.LocalDateTime;
 
 /**
  * Created by IntelliJ IDEA.
  * JTB Development
  * Date: 7/16/12
  * Time: 9:18 PM
  */
 public class RESTReceiver extends BroadcastReceiver {
    static boolean first = true;
     @Override
     public void onReceive(final Context context, final Intent intent) {
        if(!first) {
            return;
        }
        first = false;
         setAnAlarm(context, NextReminderCalculator.getNextDailyReminderTime());
     }
 
     public static void setAnAlarm(final Context context, LocalDateTime alarmTime) {
         Log.d("REST", "Next trigger at " + alarmTime);
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, RESTReceiver.class), 0);
         Intent serviceIntent = new Intent(context, RESTService.class);
         context.startService(serviceIntent);
         alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.toDateTime().getMillis(), pendingIntent);
     }
 }
