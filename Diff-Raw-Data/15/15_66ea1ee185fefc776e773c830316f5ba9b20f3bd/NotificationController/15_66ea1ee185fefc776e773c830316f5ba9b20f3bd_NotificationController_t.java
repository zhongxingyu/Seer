 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.controller;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
import android.util.Log;
 import edu.chalmers.dat255.group09.Alarmed.R;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 
 /**
  * Controller class for the notifications.
  * 
  * @author Andreas Rolen
  * 
  */
 public class NotificationController {
 
 	private NotificationManager notificationManager;
 	private Context context;
 
 	/**
 	 * Constructor for the NotificationController.
 	 * 
 	 * @param ctx
 	 *            The android context
 	 */
 	public NotificationController(Context ctx) {
 		this.context = ctx;
 		this.notificationManager = (NotificationManager) ctx
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 	}
 
 	/**
 	 * Adds a new notification with the specified alarm.
 	 * 
 	 * @param alarm
 	 *            The alarm which should be set as the current notification.
 	 */
 	public void addNotification(Alarm alarm) {
 		deleteCurrentNotification();
 		if (alarm != null) {
 			this.notificationManager.notify(alarm.getId(),
 					buildNotification(alarm));
 		}
 	}
 
 	/**
 	 * Creates a new Notification with the data from the alarm.
 	 * 
 	 * @param alarm
 	 *            The alarm to be set as the current notification.
 	 * @return a Notification with the data from the alarm.
 	 */
 	private Notification buildNotification(Alarm alarm) {
		// context.getClass() gives us the context of the MainClass,
		// MainActivity
 		Intent notificationIntent = new Intent(context, context.getClass());
 		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
 				notificationIntent, 0);
 		String contentTitle = "Next alarm set at: " + alarm.getAlarmHours()
 				+ ":" + alarm.getAlarmMinutes();
 		String contentText = "Click here to see all your alarms.";
 		String tickerText = "A New Alarm is Scheduled!";
 
 		Notification notification = new NotificationCompat.Builder(context)
 				.setContentIntent(pendingIntent)
 				.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true)
 				.setTicker(tickerText).setContentTitle(contentTitle)
 				.setContentText(contentText).setOngoing(true)
 				.setOnlyAlertOnce(true).build();
 
 		return notification;
 	}
 
 	/**
 	 * Deletes the current Notification.
 	 */
 	private void deleteCurrentNotification() {
		notificationManager.cancelAll();

 	}
 
 }
