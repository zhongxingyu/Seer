 package com.corridor9design.abandonedandroid;
 
 import java.util.Calendar;
 import java.util.Random;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class AbandonedAlarm extends BroadcastReceiver {
 	
 	NotificationHandler notifications = new NotificationHandler();
 	
 	//****** Try declaring the intent, pendingintent, and alarmmanager objects here. then cancel the alarm on every nextAlarm.
 	
 	public AbandonedAlarm() {
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getAction()!=null){
			System.out.println("GOT HERE");
			System.out.println(intent.getAction().toString());
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				notifications.launchNotification(context, 1);
			}
 		}
 	}
 	
 	protected void nextAlarm(Context context, int seconds){
 		Calendar nextAlarm = Calendar.getInstance();
 
 		Intent intent = new Intent(context, AbandonedAlarm.class);
 		PendingIntent pending = PendingIntent.getBroadcast(context, MainActivity.REPEATING_ALARM, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		
 		AlarmManager amanager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		nextAlarm.add(Calendar.SECOND, seconds);
 		
 		amanager.set(AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), pending);
 		
 	}
 	
 	protected void pauseAlarm(Context context){
 		Intent intent = new Intent(context, AbandonedAlarm.class);
 		PendingIntent pending = PendingIntent.getBroadcast(context, MainActivity.REPEATING_ALARM, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		
 		AlarmManager amanager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 
 		amanager.cancel(pending);
 	}
 
 	
 	protected int randomizeAlarm(int longestSpan, int shortestSpan, double severity){
 		Random rand = new Random();
 		// longestSpan = the longest length of time the device will go without alarming
 		// shortestSpan = the shortest length of time the device will go before alarming again
 		// severity = Divider that will shorten the longestSpan depending on sliding scale
 		
 		int initialLength = rand.nextInt((longestSpan - shortestSpan + 1) + shortestSpan);
 		int finalCountdown = (int) (initialLength / severity);
 		
 		
 		System.out.println(initialLength + "     " + severity);
 		Log.d("Next Alarm", finalCountdown + " seconds remaining");
 		return finalCountdown;
 	}
 }
