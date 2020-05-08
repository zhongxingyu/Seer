 package com.example.rest;
 import java.util.Calendar;
  
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
  
 public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
  
 public void SetAlarm(Context context, int startHours, int startMinute, int endHours, int endMinute, int periodicHours, int periodicMinute)
     {
         AlarmManager startAm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         AlarmManager endAm =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         
         Intent intent = new Intent(context, TimerService.class);
         
         Calendar calendarStart = Calendar.getInstance();
         Calendar calendarEnd = Calendar.getInstance();
         
         calendarStart.set(Calendar.MINUTE, startMinute);
         calendarStart.set(Calendar.HOUR_OF_DAY, startHours);
         	
         calendarEnd.set(Calendar.MINUTE, endMinute);
         calendarEnd.set(Calendar.HOUR_OF_DAY, endHours);
         	
         calendarStart.set(Calendar.SECOND, 0);
         calendarEnd.set(Calendar.SECOND, 0);
         
         PendingIntent piStart = PendingIntent.getService(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
         PendingIntent piEnd = PendingIntent.getService(context, 1, intent,PendingIntent.FLAG_UPDATE_CURRENT);
         
         int differenceHours = endHours - startHours;
         if (startHours>endHours) {
         	differenceHours = endHours+24-startHours;
         }
         
         int difference = differenceHours*60 + endMinute - startMinute;
         int periodic = periodicHours*60 + periodicMinute + difference;
         startAm.setRepeating(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), 1000 * 60 * periodic , piStart);
         endAm.setRepeating(AlarmManager.RTC_WAKEUP, calendarEnd.getTimeInMillis(), 1000 * 60 *  periodic , piEnd);
    }
  
     public void CancelAlarm(Context context)
     {
         Intent intent = new Intent(context, TimerService.class);
         PendingIntent senderStart = PendingIntent.getService(context, 0, intent, 0);
         PendingIntent senderStop = PendingIntent.getService(context, 1, intent, 0);
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         alarmManager.cancel(senderStart);
         alarmManager.cancel(senderStop);
         
     }
 
 	@Override
 	public void onReceive(Context arg0, Intent arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 }
