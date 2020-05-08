 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
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
 package uk.co.jwlawson.nof1;
 
 import java.util.Calendar;
 
 import uk.co.jwlawson.nof1.preferences.TimePreference;
 import android.annotation.TargetApi;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.app.backup.BackupManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Handles scheduling next notification.
  * 
  * @author John Lawson
  * 
  */
 public class Scheduler extends Service {
 
 	private static final String TAG = "Scheduler";
 	private static final boolean DEBUG = true && BuildConfig.DEBUG;
 
 	private static final int REQUEST_QUES = 0;
 	private static final int REQUEST_MED = 1;
 
 	private AlarmManager mAlarmManager;
 
 	private BackupManager mBackupManager;
 
 	@TargetApi(8)
 	public Scheduler() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			mBackupManager = new BackupManager(this);
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
 		if (DEBUG) Log.d(TAG, "Service created");
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 
 		if (intent.getBooleanExtra(Keys.INTENT_BOOT, false)) {
 			if (DEBUG) Log.d(TAG, "Scheduler started after boot");
 
 			Thread thread = new Thread(mBootSchedule);
 			thread.start();
 
 		} else if (intent.getBooleanExtra(Keys.INTENT_ALARM, false)) {
 			if (DEBUG) Log.d(TAG, "Scheduler started to schedule new alarm");
 
 			Thread thread = new Thread(mNotiSchedule);
 			thread.start();
 
 			if (intent.getBooleanExtra(Keys.INTENT_FIRST, false)) {
 				// If first is sent as well, then need to set up medicine reminders
 				Thread t = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						setMedicineAlarm();
 					}
 				});
 				t.start();
 			}
 
 		} else if (intent.getBooleanExtra(Keys.INTENT_FIRST, false)) {
 			if (DEBUG) Log.d(TAG, "Scheduler run for the first time");
 
 			Thread thread = new Thread(mFirstRun);
 			thread.start();
 
 		} else if (intent.hasExtra(Keys.INTENT_RESCHEDULE)) {
 			if (DEBUG) Log.d(TAG, "Rescheduling alarm");
 			final int mins = intent.getIntExtra(Keys.INTENT_RESCHEDULE, 0);
 
 			Thread thread = new Thread(new Runnable() {
 				@Override
 				public void run() {
 					reschedule(mins);
 				}
 			});
 			thread.start();
 
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if (DEBUG) Log.d(TAG, "Service destroyed");
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	private void reschedule(int mins) {
 		// Work out when next to set the alarm, set it and save in preferences
 		SharedPreferences schedPrefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
 		SharedPreferences.Editor schedEdit = schedPrefs.edit();
 
 		// Roll back settings in preferences
 		schedEdit.putInt(Keys.SCHED_NEXT_DAY, schedPrefs.getInt(Keys.SCHED_LAST_DAY, 1));
 		schedEdit.putInt(Keys.SCHED_CUR_PERIOD, schedPrefs.getInt(Keys.SCHED_LAST_PERIOD, 1));
 		schedEdit.putString(Keys.SCHED_NEXT_DATE, schedPrefs.getString(Keys.SCHED_LAST_DATE, null));
		schedEdit.putInt(Keys.SCHED_NEXT_CUMULATIVE_DAY, schedPrefs.getInt(Keys.SCHED_CUMULATIVE_DAY, 1));
 
 		// Get calendar for this time + mins
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, mins);
 
 		schedEdit.commit();
 		backup();
 
 		// Finally, use the new values to set an alarm
 		Intent intent = new Intent(Scheduler.this, Receiver.class);
 		intent.putExtra(Keys.INTENT_ALARM, true);
 		setAlarm(intent, cal);
 
 		// Close service once done
 		Scheduler.this.stopSelf();
 	}
 
 	/** Load next date to set alarm from preferences and set alarm for then */
 	private void setRepeatAlarm() {
 		Intent intent = new Intent(Scheduler.this, Receiver.class);
 		intent.putExtra(Keys.INTENT_ALARM, true);
 
 		setAlarmFromPrefs(intent);
 	}
 
 	/** Load first date to set alarm from preferences and set alarm for then */
 	private void setFirstAlarm() {
 		Intent intent = new Intent(Scheduler.this, Receiver.class);
 		intent.putExtra(Keys.INTENT_FIRST, true);
 
 		setAlarmFromPrefs(intent);
 	}
 
 	/** Sets an alarm for time saved in prefs which fires off the supplied intent */
 	private void setAlarmFromPrefs(Intent intent) {
 		SharedPreferences sp = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
 
 		SharedPreferences userPrefs = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);
 
 		String dateStr = sp.getString(Keys.SCHED_NEXT_DATE, null);
 		String timeStr = userPrefs.getString(Keys.DEFAULT_TIME, "12:00");
 		if (dateStr == null) {
 			Log.d(TAG, "Config not yet run");
 			return;
 		}
 
 		setAlarm(intent, dateStr, timeStr);
 	}
 
 	/** Set an alarm to fire off specified intent at time stored in calendar */
 	private void setAlarm(Intent intent, Calendar cal) {
 		PendingIntent pi = PendingIntent.getBroadcast(Scheduler.this, REQUEST_QUES, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		mAlarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
 	}
 
 	/**
 	 * Set an alarm at specified date and time.
 	 * 
 	 * @param intent
 	 * @param dateStr DD:MM:YYYY
 	 * @param timeStr HH:MM
 	 */
 	private void setAlarm(Intent intent, String dateStr, String timeStr) {
 
 		String[] dateArr = dateStr.split(":");
 		String[] timeArr = timeStr.split(":");
 
 		int[] dateInt = new int[] { Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]) };
 		int[] timeInt = new int[] { Integer.parseInt(timeArr[0]), Integer.parseInt(timeArr[1]) };
 
 		Calendar cal = Calendar.getInstance();
 		cal.set(dateInt[2], dateInt[1], dateInt[0], timeInt[0], timeInt[1]);
 
 		PendingIntent pi = PendingIntent.getBroadcast(Scheduler.this, REQUEST_QUES, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 		mAlarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
 		if (DEBUG) Log.d(TAG, "Scheduled alarm for: " + dateInt[2] + " " + dateInt[1] + " " + dateInt[0] + " " + timeInt[0] + " " + timeInt[1]);
 	}
 
 	private void setMedicineAlarm() {
 		SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 		Calendar now = Calendar.getInstance();
 
 		for (int i = 0; sp.contains(Keys.CONFIG_TIME + i); i++) {
 
 			Calendar cal = Calendar.getInstance();
 			String time = sp.getString(Keys.CONFIG_TIME + i, "12:00");
 			int hour = TimePreference.getHour(time);
 			int min = TimePreference.getMinute(time);
 
 			cal.set(Calendar.HOUR_OF_DAY, hour);
 			cal.set(Calendar.MINUTE, min);
 
 			if (cal.before(now)) {
 				// If we would be setting a notification in the past, add an extra day to ensure it is only called in
 				// the future
 				cal.add(Calendar.DAY_OF_MONTH, 1);
 			}
 
 			Intent intent = new Intent(this, Receiver.class);
 			intent.putExtra(Keys.INTENT_MEDICINE, true);
 
 			// Make sure each medicine notification gets a different request id
 			PendingIntent pi = PendingIntent.getBroadcast(this, REQUEST_MED + i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
 			if (DEBUG) Log.d(TAG, "Scheduling a repeating medicine alarm at " + time);
 		}
 	}
 
 	private Runnable mNotiSchedule = new Runnable() {
 		@Override
 		public void run() {
 			// Work out when next to set the alarm, set it and save in preferences
 			SharedPreferences schedPrefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
 			SharedPreferences.Editor schedEdit = schedPrefs.edit();
 
 			SharedPreferences configPrefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 			int lastDay = schedPrefs.getInt(Keys.SCHED_NEXT_DAY, 1);
 			int periodLength = configPrefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 1);
 			int nextDay = -1;
 			int add;
 
 			for (add = 1; add < periodLength + 1; add++) {
 				nextDay = (lastDay + add) % (periodLength + 1);
 
 				if (configPrefs.contains(Keys.CONFIG_DAY + nextDay)) {
 					if (DEBUG) Log.d(TAG, "Found the next day to send notification: " + nextDay);
 					break;
 				}
 			}
 			if (nextDay < 0) {
 				throw new RuntimeException("Invalid config settings");
 			}
 			int period = schedPrefs.getInt(Keys.SCHED_CUR_PERIOD, 1);
 			if (nextDay < lastDay) {
 				// Moving into next treatment period
 				if (period + 1 > 2 * configPrefs.getInt(Keys.CONFIG_NUMBER_PERIODS, Integer.MAX_VALUE)) {
 					// TODO Finished trial!
 
 					schedEdit.putBoolean(Keys.SCHED_FINISHED, true);
 					return;
 				}
 				schedEdit.putInt(Keys.SCHED_CUR_PERIOD, period + 1);
 			}
 			schedEdit.putInt(Keys.SCHED_LAST_PERIOD, period);
 
 			// Save next day to set alarm
 			schedEdit.putInt(Keys.SCHED_NEXT_DAY, nextDay);
 			schedEdit.putInt(Keys.SCHED_LAST_DAY, lastDay);
 
 			// Get the new date to set the alarm on
 			String lastDate = schedPrefs.getString(Keys.SCHED_NEXT_DATE, null);
 			String[] lastArr = lastDate.split(":");
 			int[] lastInt = new int[] { Integer.parseInt(lastArr[0]), Integer.parseInt(lastArr[1]), Integer.parseInt(lastArr[2]) };
 			Calendar cal = Calendar.getInstance();
 			cal.set(lastInt[2], lastInt[1], lastInt[0]);
 			cal.add(Calendar.DAY_OF_MONTH, add);
 
 			StringBuilder sb = new StringBuilder();
 			sb.append(cal.get(Calendar.DAY_OF_MONTH)).append(":");
 			sb.append(cal.get(Calendar.MONTH)).append(":");
 			sb.append(cal.get(Calendar.YEAR));
 
 			schedEdit.putString(Keys.SCHED_NEXT_DATE, sb.toString());
 			schedEdit.putString(Keys.SCHED_LAST_DATE, lastDate);
 
 			// Increment cumulative day counter
 			int cumDay = schedPrefs.getInt(Keys.SCHED_NEXT_CUMULATIVE_DAY, 1);
 			schedEdit.putInt(Keys.SCHED_NEXT_CUMULATIVE_DAY, cumDay + add);
 			schedEdit.putInt(Keys.SCHED_CUMULATIVE_DAY, cumDay);
 
 			schedEdit.commit();
 			backup();
 
 			// Finally, use the new values to set an alarm
 			setRepeatAlarm();
 
 			// Close service once done
 			Scheduler.this.stopSelf();
 		}
 	};
 
 	private Runnable mFirstRun = new Runnable() {
 		@Override
 		public void run() {
 			// Load preferences to hold information.
 			// Set alarm for start date of trial
 
 			SharedPreferences configPrefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 			SharedPreferences schedPrefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
 			SharedPreferences.Editor schedEdit = schedPrefs.edit();
 
 			// Load start date as next time for notification
 			String start = configPrefs.getString(Keys.CONFIG_START, null);
 			if (start == null) {
 				Log.e(TAG, "Start date not initialised, config needs to be run");
 				Toast.makeText(Scheduler.this, R.string.config_not_run, Toast.LENGTH_SHORT).show();
 
 			} else {
 				schedEdit.putString(Keys.SCHED_NEXT_DATE, start);
 				schedEdit.putInt(Keys.SCHED_NEXT_DAY, 1);
 				schedEdit.putInt(Keys.SCHED_CUR_PERIOD, 1);
 
 				schedEdit.commit();
 
 				backup();
 
 				// Set up first time run notification
 				setFirstAlarm();
 			}
 			// Close service once done
 			Scheduler.this.stopSelf();
 		}
 	};
 
 	private Runnable mBootSchedule = new Runnable() {
 		@Override
 		public void run() {
 			// Get the next alarm time from preferences and set it
 			setRepeatAlarm();
 			setMedicineAlarm();
 
 			// Close service once done
 			Scheduler.this.stopSelf();
 		}
 
 	};
 
 	@TargetApi(8)
 	private void backup() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			mBackupManager.dataChanged();
 		}
 	}
 
 }
