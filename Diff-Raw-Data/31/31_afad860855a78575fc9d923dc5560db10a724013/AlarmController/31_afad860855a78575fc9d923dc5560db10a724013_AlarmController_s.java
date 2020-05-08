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
 
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import edu.chalmers.dat255.group09.Alarmed.database.AlarmHandler;
 import edu.chalmers.dat255.group09.Alarmed.database.DatabaseHandler;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 import edu.chalmers.dat255.group09.Alarmed.receiver.AlarmReceiver;
 
 /**
  * Controller that handles all the alarms.
  * 
  * @author Daniel Augurell
  * 
  */
 public final class AlarmController {
 
 	private static AlarmController instance;
 	private AlarmHandler alarmHandler;
 	private NotificationController notificationController;
 
 	private Context context;
 
 	/**
 	 * A singleton shouldn't be accessed outside of the class.
 	 * 
 	 */
 	private AlarmController() {
 	}
 
 	/**
 	 * Gets the instance of the AlarmController.
 	 * 
 	 * @return An instance of AlarmController
 	 */
 	public static AlarmController getInstance() {
 		if (instance == null) {
 			instance = new AlarmController();
 		}
 		return instance;
 	}
 
 	/**
 	 * Init method to initiate the controller.
 	 * 
 	 * @param ctx
 	 *            The android context
 	 */
 	public void init(Context ctx) {
 		this.context = ctx;
 		alarmHandler = new DatabaseHandler(context).openCon();
 		notificationController = new NotificationController(context);
 	}
 
 	/**
 	 * Init method the change the handler to a handler that is not the default.
 	 * 
 	 * @param ctx
 	 *            The android context
 	 * @param handler
 	 *            The handler to handle the alarms
 	 */
 	public void init(Context ctx, AlarmHandler handler) {
 		this.context = ctx;
 		alarmHandler = handler.openCon();
 		notificationController = new NotificationController(context);
 	}
 
 	/**
 	 * Add the alarm and then sets the first enabled alarm.
 	 * 
 	 * @param alarm
 	 *            The alarm to set.
 	 */
 	public void addAlarm(Alarm alarm) {
 		alarmHandler.addAlarm(alarm);
 		setAlarm();
 	}
 
 	/**
 	 * Gets the first enabled alarm from the handler and then adds it to the
 	 * AlarmManager.
 	 */
 	private void setAlarm() {
 		Alarm nextAlarm = alarmHandler.fetchFirstEnabledAlarm();
 		if (nextAlarm != null) {
 			addAlarmToAlarmManager(nextAlarm);
 		}
 		notificationController.addNotification(nextAlarm);
 	}
 
 	/**
 	 * Adds an alarm to the AlarmManager with the alarms specified time.
 	 * 
 	 * @param alarm
 	 *            The alarm that would be added
 	 */
 	private void addAlarmToAlarmManager(Alarm alarm) {
 		PendingIntent sender = createAlarmPendingIntent(alarm);
 		AlarmManager alarmManager = (AlarmManager) context
 				.getSystemService(Context.ALARM_SERVICE);
 		alarmManager.set(AlarmManager.RTC_WAKEUP,
 				alarm.getTimeInMilliSeconds(), sender);
 
 	}
 
 	/**
 	 * Removes an alarm from the AlarmManager with the alarms specified time.
 	 * 
 	 * @param alarm
 	 *            The alarm that would be removed.
 	 */
 	private void removeAlarmFromAlarmManager(Alarm alarm) {
 		PendingIntent sender = createAlarmPendingIntent(alarm);
 		AlarmManager alarmManager = (AlarmManager) context
 				.getSystemService(Context.ALARM_SERVICE);
 		alarmManager.cancel(sender);
 	}
 
 	/**
 	 * Creates an PendingIntent to be sent with the AlarmManager.
 	 * 
 	 * @param alarm
 	 *            The alarm to to be associated with the PendingIntent.
 	 * @return A PendingIntent with the data from the alarm.
 	 */
 	private PendingIntent createAlarmPendingIntent(Alarm alarm) {
 		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setData(Uri.parse(alarm.getId() + ":" + alarm.getModule() + ":"
				+ alarm.getVolume() + ":" + alarm.getToneUri()+ ":" + alarm.isVibrationEnabled()));
 
 		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
 				Intent.FILL_IN_DATA);
 		return sender;
 	}
 
 	/**
 	 * Disables the received alarm, if it is not recurring, and then sets a new
 	 * alarm.
 	 * 
 	 * @param id
 	 *            The id of the alarm
 	 * @return true if there is an alarm, else false
 	 */
 	public boolean alarmReceived(int id) {
 		Alarm alarm = alarmHandler.fetchAlarm(id);
 		if (alarm != null && alarm.isEnabled() && alarm.getDaysOfWeek() == 0) {
 			alarmHandler.setAlarmEnabled(id, false);
 		}
 		setAlarm();
 		if (alarm == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Deletes the alarm and then sets a new alarm.
 	 * 
 	 * @param id
 	 *            The id of the alarm
 	 * @return true if deleted, else false
 	 */
 	public boolean deleteAlarm(int id) {
 		Alarm alarm = alarmHandler.fetchAlarm(id);
 		if (alarm != null) {
 			removeAlarmFromAlarmManager(alarm);
 		}
 		boolean removed = alarmHandler.deleteAlarm(id);
 		setAlarm();
 		return removed;
 	}
 
 	/**
 	 * 
 	 * @param id
 	 *            The id of the alarm
 	 * @return true if the alarm is enabled
 	 */
 	public boolean isAlarmEnabled(int id) {
 		return alarmHandler.isEnabled(id);
 	}
 
 	/**
 	 * Disable an alarm and then sets a new alarm.
 	 * 
 	 * @param id
 	 *            The id of the alarm
 	 * @param enable
 	 *            the new state of the alarm
 	 * @return true if the state is changed
 	 */
 	public boolean enableAlarm(int id, boolean enable) {
 		Alarm alarm = alarmHandler.fetchAlarm(id);
 		if (alarm != null) {
 			removeAlarmFromAlarmManager(alarm);
 		}
 		boolean enabled = alarmHandler.setAlarmEnabled(id, enable);
 		setAlarm();
 		return enabled;
 	}
 
 	/**
 	 * 
 	 * @return A list of all the alarms
 	 */
 	public List<Alarm> getAllAlarms() {
 		return alarmHandler.fetchAllAlarms();
 	}
 
 	/**
 	 * Closes the connection to the handler.
 	 */
 	public void destroy() {
 		alarmHandler.closeCon();
 	}
 
 }
