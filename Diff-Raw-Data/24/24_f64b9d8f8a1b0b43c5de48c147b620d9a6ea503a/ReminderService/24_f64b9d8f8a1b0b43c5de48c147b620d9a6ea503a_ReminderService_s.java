 package com.t2.vas;
 
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 import com.t2.vas.activity.StartupActivity;
 import com.t2.vas.activity.preference.ReminderActivity;
 
 public class ReminderService extends Service {
 	private static final String TAG = ReminderService.class.getSimpleName();
 	private static boolean isRunning = false;
 	
 	public static final int NOTIFICATION_ID = 230952309;
 	private Timer timer;
 	private NotificationManager notificationManager;
 	private Context thisContext;
 	
 	private static final int SCHEDULE_NEXT_REMINDER = 434;
 	private static final int CANCEL_NEXT_REMINDER = 435;
 	private static final int SHOW_NOTIFICATION = 436;
 	private static final int HIDE_NOTIFICATION = 437;
 	
 	private Handler opsHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch(msg.what) {
 			case SCHEDULE_NEXT_REMINDER:
 				scheduleNextReminder();
 				break;
 			case CANCEL_NEXT_REMINDER:
 				cancelNextReminder();
 				break;
 			case SHOW_NOTIFICATION:
 				showNotification();
 				break;
 			case HIDE_NOTIFICATION:
 				hideNotification();
 				break;
 			}
 		}
 	};
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 //		Log.v(TAG, "onCreate");
 		
 		thisContext = this;
 		isRunning = true;
 		
 		// Init the global variables.
 		notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
 		
 		if(!scheduleNextReminder()) {
 			this.stopSelf();
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 //		Log.v(TAG, "onDestroy");
 		
 		// Stop any pending reminders.
 		cancelNextReminder();
 		
 		// Hide the notification (if visible)
 		hideNotification();
 		
 		isRunning = false;
 	}
 	
 	private void cancelNextReminder() {
 //		Log.v(TAG, "cancelNextReminder");
 		if(timer != null) {
 			timer.cancel();
 			timer.purge();
 		}
 		timer = null;
 	}
 	
 	private boolean scheduleNextReminder() {
 //		Log.v(TAG, "scheduleNextReminder");
 		// Ensure the timer is cancelled and nullified.
 		cancelNextReminder();
 		
 		// Get the next time a reminder should be shown.
 		long nextRemindTime = ReminderActivity.getNextRemindTime(thisContext);
 		/*nextRemindTime = ReminderActivity.getNextRemindTimeSince(
 				thisContext,
 				Calendar.getInstance().getTimeInMillis() - 86400000
 		);
 		Log.v(TAG, "Next remind time:"+ new Date(nextRemindTime));*/
 		
 		if(nextRemindTime == -1) {
 			return false;
 		}
 		
 //		Log.v(TAG, "date:"+ new Date(nextRemindTime));
 		
 		// Schedule a task to be run on a seperate thread.
 		timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				// Show the notification.
 				opsHandler.sendEmptyMessage(HIDE_NOTIFICATION);
 				opsHandler.sendEmptyMessage(SHOW_NOTIFICATION);
 				
 				// Schedule the next notification.
 				opsHandler.sendEmptyMessage(SCHEDULE_NEXT_REMINDER);
 				/*
 				
 				hideNotification();
 				showNotification();
 				
 				// Schedule the next notification.
 				scheduleNextReminder();*/
 			}
 		}, new Date(nextRemindTime));
 		
 		return true;
 	}
 	
 	private void showNotification() {
 //		Log.v(TAG, "showNotification");
 		
 		// Build the notification and the status bar text.
 		Notification notification = new Notification(
 				R.drawable.icon,
 				getText(R.string.remind_notification_bar_text),
 				System.currentTimeMillis()
 		);
 		
 		
 		
 		notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
 		
 		// Prepare the intent to load.
 		PendingIntent contentIntent = PendingIntent.getActivity(
 				this, 
 				0,
 				new Intent(this, StartupActivity.class), 
 				0
 		);
 		
 		// Set the title and details of the notification.
 		notification.setLatestEventInfo(
 				this, 
 				getText(R.string.remind_notification_title), 
 				getText(R.string.remind_notification_desc), 
 				contentIntent
 		);
 		
 		// Show the notification.
 		notificationManager.notify(NOTIFICATION_ID, notification);
 	}
 	
 	private void hideNotification() {
 //		Log.v(TAG, "hideNotification");
 		notificationManager.cancel(NOTIFICATION_ID);
 	}
 	
 	public static void stopRunning(Context c) {
 //		Log.v(TAG, "stopRunning");
 		c.stopService(
 				new Intent(c, ReminderService.class)
 		);
 	}
 	
 	public static void startRunning(Context c) {
 //		Log.v(TAG, "startRunning");
 		if(!isRunning) {
			c.startService(
					new Intent(c, ReminderService.class)
			);
 		}
 	}
 	
 	public static void restart(final Context c) {
 		stopRunning(c);
 		
 		// Delay the start to allow the stop to complete. Otherwise it won't start.
 		new Timer().schedule(new TimerTask() {
 			@Override
 			public void run() {
 				startRunning(c);
 			}
 		}, 3000);
 	}
 	
 	public static boolean isRunning() {
 //		Log.v(TAG, "isRunning");
 		return isRunning;
 	}
 	
 	public static void clearNotification(Context c) {
 //		Log.v(TAG, "clearNotification");
 		NotificationManager nm = (NotificationManager)c.getSystemService(NOTIFICATION_SERVICE);
 		nm.cancel(NOTIFICATION_ID);
 	}
 }
