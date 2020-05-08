 /**
  *   Copyright 2012 Francesco Balducci
  *
  *   This file is part of FakeDawn.
  *
  *   FakeDawn is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   FakeDawn is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.balau.fakedawn;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Alarm extends Service {
 
 	public static final String EXTRA_SHOW_TOAST = "org.balau.fakedawn.Alarm.EXTRA_SHOW_TOAST";
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {

		boolean showToast;
		if(intent != null)
		{
			showToast = intent.getBooleanExtra(EXTRA_SHOW_TOAST, false);
		}
		else // intent is null when Service is restarted
		{
			showToast = false;
		}

 		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
 
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 
 		Intent openDawn = new Intent(getApplicationContext(), Dawn.class);
 		openDawn.setFlags(
 				Intent.FLAG_ACTIVITY_NEW_TASK|
 				Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|
 				Intent.FLAG_FROM_BACKGROUND);
 		PendingIntent openDawnPendingIntent = PendingIntent.getActivity(
 				getApplicationContext(), 
 				0, 
 				openDawn,
 				0);

 		am.cancel(openDawnPendingIntent);
 		String message;
 		if(pref.getBoolean("enabled", false))
 		{
 			Calendar nextAlarmTime = Calendar.getInstance();
 			nextAlarmTime.set(Calendar.HOUR_OF_DAY, pref.getInt("dawn_start_hour", 8));
 			nextAlarmTime.set(Calendar.MINUTE, pref.getInt("dawn_start_minute", 0));
 			nextAlarmTime.set(Calendar.SECOND, 0);
 			if(nextAlarmTime.getTimeInMillis() < System.currentTimeMillis())
 			{
 				nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1);
 				//TODO: check if enough?
 			}
 
 			am.setRepeating(
 					AlarmManager.RTC_WAKEUP, 
 					nextAlarmTime.getTimeInMillis(),
 					AlarmManager.INTERVAL_DAY,
 					openDawnPendingIntent);
 			message = String.format("Fake Dawn Alarm set for %02d:%02d.",
 					nextAlarmTime.get(Calendar.HOUR_OF_DAY),
 					nextAlarmTime.get(Calendar.MINUTE));
 		}
 		else
 		{
 			message = "Fake Dawn Alarm Disabled.";
 		}
 		Log.d("FakeDawn", message);
 		if(showToast)
 		{
 			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
 		}
 		// If we get killed, after returning from here, restart
 		return START_STICKY;
 	}
 }
