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
			System.out.println("GOT HERE");
			System.out.println(intent.getAction().toString());
 			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
 				Log.d("Next Alarm", "CANCELED");
				System.out.println("testing");
 				// show user activity
 				MainActivity.setPreferences("userActivity", "yes", context);
 				pauseAlarm(context);
 				return;
 			}
 			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
 				Log.d("ScreenOff", "Restarting Alarm");
				nextAlarm(context, randomizeAlarm(30, 1, 1)); // TODO create interface that allows user to set longestSpan, shortestSpan, and severity
 				
 			}
 		}
 	}
 }
