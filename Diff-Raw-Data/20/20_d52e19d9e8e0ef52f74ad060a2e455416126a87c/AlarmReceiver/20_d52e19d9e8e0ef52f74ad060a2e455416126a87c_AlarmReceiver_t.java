 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012 John Lawson
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at  
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package org.nof1trial.nof1;
 
 import org.nof1trial.nof1.activities.Questionnaire;
 import org.nof1trial.nof1.services.Saver;
 import org.nof1trial.nof1.services.Scheduler;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 /**
  * Gets the Boot_complete broadcast and starts up Scheduler service
  * 
  * @author John Lawson
  * 
  */
 public class AlarmReceiver extends BroadcastReceiver {
 
 	private static final String TAG = "AlarmReceiver";
 	private static final boolean DEBUG = false;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 
 		// intent.getAction() could be null
 		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
 			if (DEBUG) Log.d(TAG, "Boot_complete broadcast caught");
 
 			Intent i = new Intent(context, Scheduler.class);
 			i.putExtra(Keys.INTENT_BOOT, true);
 			context.startService(i);
 
 			Intent saver = new Intent(context, Saver.class);
 			saver.setAction(Intent.ACTION_BOOT_COMPLETED);
 			context.startService(saver);
 
 		} else if (intent.getBooleanExtra(Keys.INTENT_ALARM, false)) {
 			if (DEBUG) Log.d(TAG, "AlarmManager alarm caught");
 
 			// Show notification
 			setRepeatNotification(context);
 
 			// Pass to scheduler
 			Intent i = new Intent(context, Scheduler.class);
 			i.putExtra(Keys.INTENT_BOOT, false);
 			i.putExtra(Keys.INTENT_ALARM, true);
 			context.startService(i);
 
 		} else if (intent.getBooleanExtra(Keys.INTENT_FIRST, false)) {
 			if (DEBUG) Log.d(TAG, "First time run alarm caught");
 
 			// Show first run notification
 			setFirstNotification(context);
 
 			// Pass to scheduler
 			Intent i = new Intent(context, Scheduler.class);
 			i.putExtra(Keys.INTENT_BOOT, false);
 			i.putExtra(Keys.INTENT_ALARM, true); // Really do want to use INTENT_ALARM, not INTENT_FIRST
 			i.putExtra(Keys.INTENT_FIRST, true); // Both tells scheduler to set up medicine alarms
 			context.startService(i);
 
 		} else if (intent.getBooleanExtra(Keys.INTENT_MEDICINE, false)) {
 			if (DEBUG) Log.d(TAG, "Medicine alarm caught");
 
 			// Show medicine reminder notification
 			setMedicineNotification(context);
 		}
 	}
 
 	/** Set notification reminding patient to input data */
 	private void setRepeatNotification(Context context) {
 		Intent intent = new Intent(context, Questionnaire.class);
 		intent.putExtra(Keys.INTENT_PREVIEW, false);
 		intent.putExtra(Keys.INTENT_SCHEDULED, true);
 		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
		setNotification(context, intent, R.string.noti_repeat_title, R.string.noti_repeat_text, 0x101);
 	}
 
 	/** Set the first notification at start of trial */
 	private void setFirstNotification(Context context) {
 		// TODO What should first notification do?
 
		setNotification(context, new Intent(), R.string.noti_first_title, R.string.noti_first_text, 0x102);
 
 	}
 
 	private void setMedicineNotification(Context context) {
 
		setNotification(context, new Intent(), R.string.noti_medicine_title, R.string.noti_medicine_text, 0x103);
 	}
 
 	/**
 	 * Make and post a notification with specified params
 	 * 
 	 * @param context
 	 * @param intent Intent to fire on notification click
 	 * @param title Title of notification
 	 * @param text Text in notification
 	 */
	private void setNotification(Context context, Intent intent, String title, String text, int id) {
 
 		SharedPreferences sp = context.getSharedPreferences(Keys.DEFAULT_PREFS, Context.MODE_PRIVATE);
 
 		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
 		builder.setContentIntent(pi).setContentTitle(title).setContentText(text).setAutoCancel(true).setSmallIcon(R.drawable.ic_noti)
 				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_noti));
 
 		int style = 0;
 
 		if (sp.getBoolean(Keys.DEFAULT_LOUD, false)) {
 			// Notification makes noise
 			style ^= Notification.DEFAULT_SOUND;
 			if (DEBUG) Log.d(TAG, "Loud Notification");
 		}
 
 		if (sp.getBoolean(Keys.DEFAULT_FLASH, false)) {
 			// Notification to flash
 			style ^= Notification.DEFAULT_LIGHTS;
 			if (DEBUG) Log.d(TAG, "Flashy Notification");
 		}
 
 		if (sp.getBoolean(Keys.DEFAULT_VIBE, false)) {
 			// Notification to vibrate
 			style ^= Notification.DEFAULT_VIBRATE;
 			if (DEBUG) Log.d(TAG, "Vibrating Notification");
 		}
 
 		builder.setDefaults(style);
 
 		Notification noti = builder.build();
 
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, noti);
 
 		if (DEBUG) Log.d(TAG, "Notification posted");
 
 	}
 
 	/**
 	 * Make and post a notification with specified params
 	 * 
 	 * @param context
 	 * @param intent Intent to fire on notification click
 	 * @param title Resource with title of notification
 	 * @param text Resource with text in notification
 	 */
	private void setNotification(Context context, Intent intent, int title, int text, int id) {
 		Resources res = context.getResources();
 		String titleStr = res.getString(title);
 		String textStr = res.getString(text);
		setNotification(context, intent, titleStr, textStr, id);
 	}
 }
