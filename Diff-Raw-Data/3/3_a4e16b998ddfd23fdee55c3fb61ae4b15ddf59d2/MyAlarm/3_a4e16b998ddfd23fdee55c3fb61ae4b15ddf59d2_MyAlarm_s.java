 package com.tzwm.deadalarm;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.Toast;
 
 import java.util.Calendar;
 
 /**
  * Created by tzwm on 10/7/13.
  */
 public class MyAlarm {
     static String WAKEUP_SERVICE = "android.tzwm.wakeup";
 
     int id;
     int hourOfDay, minute;
     boolean isOpened;
 
     private Context mContext;
     private static int number = 0;
 
     public MyAlarm(Context _context, int _hourOfDay, int _minute) {
         mContext = _context;
         id = number++;
         hourOfDay = _hourOfDay;
         minute = _minute;
         isOpened = false;
     }
 
     public void open() {
         Intent intent = new Intent(WAKEUP_SERVICE);
         PendingIntent pi = PendingIntent.getBroadcast(mContext,
                 id, intent,
                 PendingIntent.FLAG_ONE_SHOT);
         AlarmManager arm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(System.currentTimeMillis());
         cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
         cal.set(Calendar.MINUTE, minute);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 //        arm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
 //        arm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(10*1000), 24*60*60*1000, pi);
         arm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 24*60*60*1000, pi);
 
         Toast toast = Toast.makeText(mContext, "New Alarm.", Toast.LENGTH_SHORT);
         toast.show();
 
         isOpened = true;
     }
 
     public void close() {
         Intent intent = new Intent(WAKEUP_SERVICE);
         PendingIntent pi = PendingIntent.getBroadcast(mContext, id, intent,
                 PendingIntent.FLAG_ONE_SHOT);
         AlarmManager arm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
         arm.cancel(pi);
 
         isOpened = false;
     }
 }
