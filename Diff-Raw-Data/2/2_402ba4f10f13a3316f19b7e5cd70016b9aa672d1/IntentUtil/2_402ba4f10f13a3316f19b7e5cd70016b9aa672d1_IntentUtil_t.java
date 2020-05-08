 /*
  * Copyright (C) 2011-2012 AlarmApp.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.alarmapp.util;
 
 import org.alarmapp.Actions;
 import org.alarmapp.AlarmApp;
 import org.alarmapp.activities.AccountCreateActivity;
 import org.alarmapp.activities.AlarmActivity;
 import org.alarmapp.activities.AlarmCreateActivity;
 import org.alarmapp.activities.AlarmListActivity;
 import org.alarmapp.activities.AlarmPreferenceActivity;
 import org.alarmapp.activities.AlarmStatusActivity;
 import org.alarmapp.activities.InformationActivity;
 import org.alarmapp.activities.LoginActivity;
 import org.alarmapp.activities.MainActivity;
 import org.alarmapp.activities.map.AlarmMapActivity;
 import org.alarmapp.model.Alarm;
 import org.alarmapp.model.WayPoint;
 import org.alarmapp.services.AudioPlayerService;
 import org.alarmapp.services.PositionService;
 import org.alarmapp.services.SyncService;
 
 import android.content.Context;
 import android.content.Intent;
 
 public class IntentUtil {
 	public static void sendToSyncService(Context c, Alarm a) {
 		Ensure.notNull(a);
 
 		Intent statusUpdateIntent = new Intent(c, SyncService.class);
 		statusUpdateIntent.putExtras(a.getBundle());
 		statusUpdateIntent.setAction(Actions.UPDATE_ALARM_STATUS);
 		c.startService(statusUpdateIntent);
 	}
 
 	public static void sendToSyncService(Context c, WayPoint w) {
 		Ensure.notNull(c);
 		Ensure.notNull(w);
 
 		Intent statusUpdateIntent = new Intent(c, SyncService.class);
 		statusUpdateIntent.putExtras(w.getBundle());
 		statusUpdateIntent.setAction(Actions.START_TRACKING);
 		c.startService(statusUpdateIntent);
 	}
 
 	public static void displayAccountCreateActivity(Context context) {
 		Ensure.notNull(context);
 
 		Intent alarmIntent = new Intent(context, AccountCreateActivity.class);
 		context.startActivity(alarmIntent);
 	}
 
 	public static void startPositionService(Context c, Alarm alarm) {
 		Ensure.notNull(c);
 		Ensure.notNull(alarm);
 
 		Intent startPositionServiceIntent = new Intent(c, PositionService.class);
 		startPositionServiceIntent.setAction(Actions.START_TRACKING);
 		startPositionServiceIntent.putExtras(alarm.getBundle());
 
 		c.startService(startPositionServiceIntent);
 	}
 
 	public static void startAudioPlayerService(Context context, Alarm alarm) {
 		Ensure.notNull(context);
 		Ensure.notNull(alarm);
 
 		Intent intent = new Intent(context, AudioPlayerService.class);
 		intent.setAction(AudioPlayerService.START_PLAYING);
 		intent.putExtras(alarm.getBundle());
 		context.startService(intent);
 	}
 
 	public static void stopAudioPlayerService(Context context) {
 		Ensure.notNull(context);
 
 		Intent intent = new Intent(context, AudioPlayerService.class);
 		intent.setAction(AudioPlayerService.STOP_PLAYING);
 
 		context.startService(intent);
 	}
 
 	public static void displayAlarmMapActivity(Context context, Alarm alarm) {
 		Ensure.notNull(alarm);
 
 		Intent intent = new Intent(context, AlarmMapActivity.class);
 		intent.putExtras(alarm.getBundle());
 		context.startActivity(intent);
 	}
 
 	public static void displayAlarmStatusUpdateIntent(Context context,
 			Alarm alarm) {
 		Ensure.notNull(alarm);
 
 		Intent alarmIntent = new Intent(context, AlarmStatusActivity.class);
 		// alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		alarmIntent.putExtras(alarm.getBundle());
 		context.startActivity(alarmIntent);
 	}
 
 	public static void displayAlarmActivity(Context context, Alarm alarm) {
 		Ensure.notNull(alarm);
 
 		Intent alarmIntent = new Intent(context, AlarmActivity.class);
 		alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		alarmIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 		alarmIntent.putExtras(alarm.getBundle());
 		context.startActivity(alarmIntent);
 	}
 
 	public static void sendFeedbackEmailIntent(Context context) {
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.putExtra(Intent.EXTRA_EMAIL,
 				new String[] { "android@alarmapp.org" });
 		intent.putExtra(Intent.EXTRA_SUBJECT,
 				"Feedback für die Android AlarmApp von "
 						+ AlarmApp.getUser().getFullName());
 		intent.setType("message/rfc822");
 		context.startActivity(Intent.createChooser(intent,
 				"Bitte wählen Sie eine Anwendung"));
 	}
 
 	public static void displayMainActivity(Context context) {
 		Intent mainIntent = new Intent(context, MainActivity.class);
 		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		// mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		context.startActivity(mainIntent);
 	}
 
 	public static void displayAlarmListActivity(Context context) {
 		Intent alarmListIntent = new Intent(context, AlarmListActivity.class);
 		context.startActivity(alarmListIntent);
 	}
 
 	public static void displayInformationsActivity(Context context) {
 		Intent intent = new Intent(context, InformationActivity.class);
 		context.startActivity(intent);
 	}
 
 	public static void displayPreferencesActivity(Context context) {
 		Intent intent = new Intent(context, AlarmPreferenceActivity.class);
 		context.startActivity(intent);
 	}
 
 	public static void displayLoginActivity(Context context) {
 		Intent i = new Intent(context, LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		// i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		context.startActivity(i);
 	}
 
 	public static void displayAlarmCreateActivity(Context context) {
 		Intent intent = new Intent(context, AlarmCreateActivity.class);
 		context.startActivity(intent);
 	}
 }
