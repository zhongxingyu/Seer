 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package com.koushikdutta.klaxon;
 
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 /**
  * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert activity. Passes
  * through Alarm ID.
  */
 public class AlarmReceiver extends BroadcastReceiver
 {
 	final static String LOGTAG = "Klaxon";
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
		Log.i(LOGTAG, "AlarmReceiver received has received a broadcast.");
 		try
 		{
 			long alarmTime = intent.getLongExtra("AlarmTime", 0);
 			long alarmId = intent.getLongExtra(AlarmSettings.GEN_FIELD__id, -1);
 			GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());
 			if (alarmId == -1 || alarmTime > cal.getTimeInMillis())
 			{
 				Log.e(LOGTAG, "Invalid alarmId (" + alarmId + ") or alarmTime" + alarmTime);
 				return;
 			}
 			
 			AlarmAlertWakeLock.acquire(context);
 			AlarmSettings settings = AlarmSettings.getAlarmSettingsById(context, alarmId);
			Log.i(LOGTAG, "Sounding alarm " + settings.getName());
 			if (settings.isOneShot())
 				settings.setEnabled(false);
 			Intent alarmIntent = new Intent(context, AlarmActivity.class);
 			alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			alarmIntent.putExtra(AlarmSettings.GEN_FIELD__id, alarmId);
 			context.startActivity(alarmIntent);
 		}
 		finally
 		{
 			AlarmSettings.scheduleNextAlarm(context);
 		}
 	}
 }
