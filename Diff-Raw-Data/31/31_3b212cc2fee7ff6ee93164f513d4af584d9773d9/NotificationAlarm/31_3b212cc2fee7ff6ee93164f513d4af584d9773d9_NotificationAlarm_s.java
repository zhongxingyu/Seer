 package com.corridor9design.abandonedandroid;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class NotificationAlarm extends AbandonedAlarm {
 
 	public NotificationAlarm() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public void onReceive(Context context, Intent intent){
 		if (intent.getAction()!=null){
 			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
 				Log.d("Next Alarm", "CANCELED");
 				// show user activity
 				MainActivity.setPreferences("userActivity", "yes", context);
 				pauseAlarm(context);
 				return;
 			}
 			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
 				Log.d("ScreenOff", "Restarting Alarm");
				nextAlarm(context, randomizeAlarm(60, 1, 1)); // TODO create interface that allows user to set longestSpan, shortestSpan, and severity
 				
 			}
 		}
 	}
 }
