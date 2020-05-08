 package com.twofivesix.pt.alarms;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
import android.util.Log;
 
import com.twofivesix.pt.activities.ReportingActivity;
 import com.twofivesix.pt.R;
 
 public class QuestionPromptAlarm extends Service {
     
 	private static final int NOTE_ICON = android.R.drawable.ic_popup_reminder;
 	NotificationManager mNM;
 
     @Override
     public void onCreate() {
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         showNotification();
         stopSelf();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     /**
      * Show a notification while this service is running.
      */
     private void showNotification() {
    	Log.d("SPENCER", "Reminder fired.");
         // In this sample, we'll use the same text for the ticker and the expanded notification
         CharSequence text = getText(R.string.reminder_service_started);
 
         // Set the icon, scrolling text and timestamp
         Notification notification = buildNotification(text, text, getText(R.string.reminder_service_label));
         // Send the notification.
         // We use a layout id because it is a unique number.  We use it later to cancel.
         mNM.notify(R.string.reminder_service_started, notification);
     }
     
     private Notification buildNotification(CharSequence titckerText, CharSequence noteTitle, CharSequence text)
     {
     	Notification notification = new Notification(NOTE_ICON, titckerText, System.currentTimeMillis());
     	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ReportingActivity.class), 0);
     	notification.setLatestEventInfo(this, noteTitle, text, contentIntent);
     	return notification;
     }
     
     public static void closeNotification(Context context) {
 		NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
 		nm.cancel(R.string.reminder_service_started);
 	}
 }
