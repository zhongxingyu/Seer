 package omalleyland.my.simple.tools;
 
import omalleyland.my.simple.tools.mysimplealarmclock.R;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.app.NotificationManager;
 import android.app.Notification;
 import android.app.PendingIntent;
 
 /** Class for receiving broadcast when the Alarm occurs */
 public class AlarmReceiver extends BroadcastReceiver
 {
     public static final String ALARM_ALERT_ACTION = "com.android.alarmclock.ALARM_ALERT";
     public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";
     
     @Override
     public void onReceive(Context context, Intent intent) { 
       //Create Intent to Start the AlarmActivity "Snooze" Activity
   	  Intent myIntent = new Intent(context, AlarmActivity.class);
   	  myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   	  context.startActivity(myIntent);
   	  
   	  //Build pending intent from calling information to display Notification
   	  PendingIntent Sender = PendingIntent.getBroadcast(context, 0, intent, 0);
       NotificationManager manager = (NotificationManager)context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
       Notification notification = new Notification(R.drawable.icon, "Wake up alarm", System.currentTimeMillis());
       notification.setLatestEventInfo(context, "Simple Alarm Clock", "WAKE UP!!!", Sender);
       notification.flags = Notification.FLAG_NO_CLEAR;
       manager.notify(R.string.app_name, notification);  
     }
 }
