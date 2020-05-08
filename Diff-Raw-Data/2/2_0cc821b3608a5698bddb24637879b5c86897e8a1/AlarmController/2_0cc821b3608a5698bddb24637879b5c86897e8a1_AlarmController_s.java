 /**
  * Copyright (C) 2012 Emil Edholm, Emil Johansson, Johan Andersson, Johan Gustafsson
  * 
  * This file is part of dat255-bearded-octo-lama
  *
  *  dat255-bearded-octo-lama is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  dat255-bearded-octo-lama is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with dat255-bearded-octo-lama.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package it.chalmers.dat255_bearded_octo_lama;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 /**
  * Manages all the alarms.
  * @author Emil Edholm
  * @date 1 okt 2012
  */
 public enum AlarmController {
 	INSTANCE;
 	
 	/**
 	 * Add a new alarm to the database.
 	 * @param c the context
 	 * @param enabled whether or not the alarm should be enabled
 	 * @param hour the hour the alarm is to activate
 	 * @param minute the minute the alarm is to activate
 	 * @return the uri to the newly added alarm
 	 */
 	public Uri addAlarm(Context c, boolean enabled, int hour, int minute) {
 		ContentResolver cr = c.getContentResolver();
 		ContentValues values = new ContentValues();
 		
 		
 		Calendar then = Calendar.getInstance();
 		then.set(Calendar.HOUR_OF_DAY, hour);
 		then.set(Calendar.MINUTE, minute);
 		then.set(Calendar.SECOND, 0); // This makes the alarm go of at the right time
 		
 		if(then.before(Calendar.getInstance())) // Before "now" means we have to add a day
 			then.add(Calendar.DAY_OF_YEAR, 1);
 		
 		long time = then.getTimeInMillis();
 		
 		values.put(Alarm.AlarmColumns.HOUR, hour);
 		values.put(Alarm.AlarmColumns.MINUTE, minute);
 		values.put(Alarm.AlarmColumns.ENABLED, enabled ? 1 : 0);
 		values.put(Alarm.AlarmColumns.TIME, time);
 		
 		
 		Uri uri = cr.insert(Alarm.AlarmColumns.CONTENT_URI, values);
 		renewAlarmQueue(c);
 		return uri;
 	}
 	
 	public Uri addTestAlarm(Context c) {
 		ContentResolver cr = c.getContentResolver();
 		ContentValues values = new ContentValues();
 		
 		
 		Calendar then = Calendar.getInstance();
 		then.add(Calendar.SECOND, 5);
 		
 		long time = then.getTimeInMillis();
 		
 		values.put(Alarm.AlarmColumns.HOUR, then.get(Calendar.HOUR_OF_DAY));
 		values.put(Alarm.AlarmColumns.MINUTE, then.get(Calendar.MINUTE));
 		values.put(Alarm.AlarmColumns.ENABLED, 1);
 		values.put(Alarm.AlarmColumns.TIME, time);
 		
 		Uri uri = cr.insert(Alarm.AlarmColumns.CONTENT_URI, values);
 		renewAlarmQueue(c);
 		return uri;
 	}
 	
 	/**
 	 * Removes a specific alarm from the database
 	 * @param c the context
 	 * @param alarmID the ID of the alarm to remove.
 	 */
 	public void deleteAlarm(Context c, int alarmID) {
 		ContentResolver cr = c.getContentResolver();
 		cr.delete(Uri.withAppendedPath(Alarm.AlarmColumns.CONTENT_URI, alarmID + ""), "", null);
 		
 		renewAlarmQueue(c);
 	}
 	
 	public void toggleAlarm(Context c, int alarmID) {
 		ContentResolver cr = c.getContentResolver();
 		Alarm alarm = getAlarm(c, alarmID);
 		
 		Uri uri = Alarm.AlarmColumns.CONTENT_URI.buildUpon().appendPath(alarmID + "").build();
 		ContentValues values = new ContentValues();
 		
 		// Reverse enabled
 		values.put(Alarm.AlarmColumns.ENABLED, alarm.isEnabled() ? 0 : 1);
 		
 		cr.update(uri, values, null, null);
 		renewAlarmQueue(c);
 	}
 	
 	/**
 	 * Returns the alarm with the specified ID.
 	 * @param c the context
 	 * @param alarmID the alarm to fetch
 	 * @return the alarm with the specified ID if found, else null.
 	 */
 	public Alarm getAlarm(Context c, int alarmID) {
 		ContentResolver cr = c.getContentResolver();
 		
 		Uri uri = Uri.withAppendedPath(Alarm.AlarmColumns.CONTENT_URI, alarmID + "");
 		Cursor cur = cr.query(uri, Alarm.AlarmColumns.ALL_COLUMNS, null, null, null);
 		
 		Alarm a = null;
 		if(cur != null && cur.moveToFirst()) {
 			a = new Alarm(cur);
 			cur.close();
 		}
 		
 		return a;
 	}
 	
 	/**
 	 * Returns all alarms in the database sorted by hour and minute in ascending order.
 	 * @param c the context
 	 * @return a list of all alarms in the database.
 	 */
 	public List<Alarm> getAllAlarms(Context c) {
 		return getAlarms(c, null, null, "HOUR, MINUTE ASC");
 	}
 	
 	private List<Alarm> getAlarms(Context c, String where, String[] args, String sortOrder) {
 		ContentResolver cr = c.getContentResolver();
 		
 		Uri uri = Alarm.AlarmColumns.CONTENT_URI;
 		Cursor cur = cr.query(uri, Alarm.AlarmColumns.ALL_COLUMNS, where, args, sortOrder);
 		
 		
 		if(cur != null && cur.moveToFirst()) {
 			List<Alarm> alarms = new ArrayList<Alarm>();
 			
 			do {
 				alarms.add(new Alarm(cur));
 			} while(cur.moveToNext());
 			
 			return alarms;
 		}
 		
 		return Collections.emptyList();
 	}
 	
 	/**
 	 * @return given an uri that points to a specific alarm, returns the id of that Alarm
 	 */
 	public int extractIDFromUri(Uri uri) {
 		String segment = uri.getPathSegments().get(1);
         return Integer.parseInt(segment);
 	}
 	
 	/** 
 	 * Activates next alarm in the database if one exists.
 	 * @param c the context
 	 */
 	public void renewAlarmQueue(Context c) {		
 		Alarm a = getNextInQueue(c);
 		
 		if(a != null) {
 			enableAlarmManager(c, a);
 		}
 		
 		disableAlarmManager(c);
 	}
 	/** Get the next alarm that is enabled and nearest in time to now */
 	private Alarm getNextInQueue(Context context) {
 		// Get all enabled alarms in descending order.
 		List<Alarm> alarms = getAlarms(context, "Enabled=?", new String[] {"1"}, "TIME_IN_MS DESC");
 		
 		long minTime = Long.MAX_VALUE, now = System.currentTimeMillis();
 		Alarm theAlarm = null;
 		for(Alarm a : alarms) {
 			
 			// If alarm time has passed, disable alarm.
 			if(a.getTimeInMS() < now) {
 				toggleAlarm(context, a.getId());
 				continue;
 			}
 			
 			if(a.getTimeInMS() < minTime) {
 				minTime = a.getTimeInMS();
 				theAlarm = a;
 			}
 		}
 		Log.d("queue", "Next in queue: " + theAlarm.toString());
 		return theAlarm;
 	} 
 	
 	/** Remove all alarms that have expired (time for their alert has passed) from the database */ 
 	public void clearExpired(Context c) {
 		ContentResolver cr = c.getContentResolver();
 		long now = System.currentTimeMillis();
 		
 		// Removes all alarms from the database whoose time has passed.
 		cr.delete(Alarm.AlarmColumns.CONTENT_URI, "TIME_IN_MS <= ?", new String[]{now + ""});
 	}
 	
 	/** Enable an alarm in the AlarmManager. Only one alarm should be activated at a time */
 	private void enableAlarmManager(Context c, Alarm a) {
 		AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
 		
 		// Append the alarm ID to the intent, then the receiving class can fetch the alarm.
 		Intent intent = new Intent(c, AlarmReceiver.class);
 		intent.putExtra(Alarm.AlarmColumns._ID, a.getId());
 		
		PendingIntent alarmIntent = PendingIntent.getBroadcast(c, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		
 		am.set(AlarmManager.RTC_WAKEUP, a.getTimeInMS(), alarmIntent);
 	}
 	
 	/** Disable/cancel the pending intent in the AlarmManager */
 	private void disableAlarmManager(Context c) {
 		AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
 		PendingIntent alarmIntent = PendingIntent.getBroadcast(c, 0, new Intent(c, AlarmReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
 	
 		am.cancel(alarmIntent);
 	}
 }
