 package com.samteladze.delta.statistics;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.SystemClock;
 
 import com.samteladze.delta.statistics.utils.LogManager;
 
 public class OnBootReceiver extends BroadcastReceiver
 {
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{		
 		// Get AlarmManager
 		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		
 		// Create an Intent to start OnAlarmReceiver
 		Intent alarmIntent = new Intent(context, OnAlarmReceiver.class);
 		// Create a PendingIntent from alarmIntent
 		PendingIntent alarmPendingIntent = 
 				PendingIntent.getBroadcast(context, DeltaStatisticsActivity.INTENT_REQUEST_CODE, alarmIntent, 0);
 		
 		// Set repeating alarm that will invoke OnAlarmReceiver
 		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
 								  SystemClock.elapsedRealtime() + DeltaStatisticsActivity.ALARM_DELAY, 
 								  DeltaStatisticsActivity.ALARM_PERIOD, alarmPendingIntent);
 		
 		LogManager.Log(OnBootReceiver.class.getSimpleName(), "Repeating alarm was set");
 	}
 }
