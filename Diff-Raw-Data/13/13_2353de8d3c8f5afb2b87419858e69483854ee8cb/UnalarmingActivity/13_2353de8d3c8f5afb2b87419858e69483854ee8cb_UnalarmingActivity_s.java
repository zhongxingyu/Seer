 /*
  * This file incorporates work covered by the following copyright and
  * permission notice:
  *
  *   Copyright (C) 2010 Tedd Scofield
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.sixfifty;
 
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import com.android.internal.telephony.ITelephony;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 /**
  * Central user interface activity for the application.
  * 
  * @author lenards
  * 
  */
 public class UnalarmingActivity extends Activity {
 	private static final String TAG = UnalarmingActivity.class.getSimpleName();
 	private static final String ALARM_ACTION = "com.sixfifty.actions.ALARM_RECEIVED";
 	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
 			"h:mm a z");
 
 	/**
 	 * Request code constant for pending intent of alarm receiver
 	 */
 	protected static final int REQUEST_CODE = 0;
 
 	/**
 	 * Internal identifier for the TimePicker Dialog
 	 */
 	private static final int TIME_DIALOG_ID = 0;
 	/**
 	 * Internal identifier for the MinutesSelector Dialog
 	 */
 	private static final int MINUTES_DIALOG_ID = 1;
 	private static final int MISSED_CALLS_DIALOG_ID = 2;
 
 	private Calendar calendar;
 	private AlarmManager alarmMgr;
 	private AudioManager audioMgr;
	private MediaPlayer mPlayer; 
 	private IntentFilter alarmIntentFilter;
 	private int hourOfDay;
 	private int minOfDay;
 	private int previousRingerMode = -1;
 	private boolean missedCalls;
 
 	/**
 	 * Receiver for alarm intent that ends meditation period.
 	 */
 	private BroadcastReceiver onetimeAlarm = null;
 	
 	/**
 	 * Receiver for decline calls during meditation period. 
 	 */ 	// null used as a flag for showing an 'active' receiver in the activity.
 	private BroadcastReceiver declineCalls = null;
 	
 	
 	/**
 	 * Handler for setting alarm time via the TimePicker.
 	 */
 	private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		@Override
 		public void onTimeSet(TimePicker view, int hour, int minute) {
 			hourOfDay = hour;
 			minOfDay = minute;
 			scheduleAlarm();
 		}
 	};
 
 	/**
 	 * Text for the minutes values in the MinutesSelector.
 	 */
 	private final CharSequence[] minuteValues = { "5", "10", "15", "20", "25",
 			"30", "35", "40", "45", "50", "55", "60", "65", "70", "75" };
 
 	/**
 	 * Handler for setting alarm time via the MinutesSelector.
 	 */
 	private DialogInterface.OnClickListener minutesSelectedListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			if (which >= 0) {
 				CharSequence selected = minuteValues[which];
 				Calendar c = Calendar.getInstance();
 				c.add(Calendar.MINUTE, Integer.valueOf(selected.toString()));
 				hourOfDay = c.get(Calendar.HOUR_OF_DAY);
 				minOfDay = c.get(Calendar.MINUTE);
 				scheduleAlarm();
 				dialog.dismiss();
 			}
 		}
 	};
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setActivityAttributes();
 		initializeFields();
		initializeMediaPlayer();
 		wireupListeners();
 		
 		if (savedInstanceState != null && savedInstanceState.getBoolean("alarmFired")) {
 			Log.i(TAG, "Clean-up");
 			Log.i(TAG, "Is declineCalls null? " + (declineCalls == null));
 			unregisterReceiver(declineCalls);
 		}
 	}
 
	private void initializeMediaPlayer() {
		mPlayer = MediaPlayer.create(UnalarmingActivity.this, R.raw.mbell);
 		mPlayer.setOnCompletionListener(new OnCompletionListener() {
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 				mp.release();
 			}
 		});
 	}
 
 	/**
 	 * Defines values for attributes of the Activity.
 	 */
 	private void setActivityAttributes() {
 		setContentView(R.layout.main);
 		setTitle(R.string.title);
 	}
 
 	/**
 	 * Initializes an instance variables of the Activity.
 	 */
 	private void initializeFields() {
 		missedCalls = false;
 		calendar = Calendar.getInstance();
 		alarmIntentFilter = new IntentFilter(ALARM_ACTION);
 		alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
 		audioMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
 
 		// determine current ringer mode setting
 		Log.i(TAG, "initial-ringer-mode:" + audioMgr.getRingerMode());
 		previousRingerMode = audioMgr.getRingerMode();
 	}
 
 	/**
 	 * Attaches the listeners for any widgets of the Activity. 
 	 */
 	private void wireupListeners() {
 		// grab the set-alarm button (for time-picker) and attach listener
 		Button btnSetAlarm = (Button) findViewById(R.id.btn_set_alarm);
 		btnSetAlarm.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(TIME_DIALOG_ID);
 			}
 		});
 		// grab the set-alarm button (for minute selector) and attach listener
 		Button btnSetAlarmDuration = (Button) findViewById(R.id.btn_set_alarm_duration);
 		btnSetAlarmDuration.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(MINUTES_DIALOG_ID);
 			}
 		});
 		SharedPreferences sPrefs = getSharedPreferences("Foo", MODE_WORLD_READABLE);
 		sPrefs.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
 			
 			@Override
 			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 					String key) {
 				if (key.equals("ALARM")) {
 					//unregisterReceiver(declineCalls);
 					Log.i(TAG, "Whoa!");
 					int calls = sharedPreferences.getInt("CALLS", 0);
 					if (calls > 0) {
 						/*
 						 * ... actually - we call onCreateDialog with the right 
 						 * ID for this dialog... 
 						 * 
 						 * We'll want to show a dialog that asks if they want to 
 						 * see the calls they missed... and it will need to take 
 						 * them to their "missed calls" list
 						 */		
 						showDialog(MISSED_CALLS_DIALOG_ID);
 					}
 				}
 			}
 		});
 	}
 
 	/**
 	 * Executes the clean-up code for when it is destroyed. 
 	 */
 	@Override
 	protected void onDestroy() {
 		if (declineCalls != null) {
 			Log.i(TAG, "Unregister broadcast receivers...");
 			//unregisterReceiver(declineCalls);
 			// ensure that the receiver reference is nullified. 
 			declineCalls = null;
 			// should onetimeAlarm be unregistered? BroadcastReceiver is 
 			// such that it is probably dead and eligible for GC. 
 		}
 		// we did our work, not let the base class destroy.
 		super.onDestroy();
 	}
 	
 	/**
 	 * Creates dialogs associated with this activity.
 	 * 
 	 * @param id Unique identifier assigned to the dialog to create.
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case TIME_DIALOG_ID:
 			hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
 			minOfDay = calendar.get(Calendar.MINUTE);
 			return new TimePickerDialog(this, timeSetListener, hourOfDay,
 					minOfDay, false);
 		case MINUTES_DIALOG_ID:
 			AlertDialog.Builder minutes = new AlertDialog.Builder(this);
 			minutes.setTitle("Select duration (in minutes)");
 			minutes.setSingleChoiceItems(minuteValues, -1,
 					minutesSelectedListener);
 			return minutes.create();
 		case MISSED_CALLS_DIALOG_ID:
 			AlertDialog.Builder missedcalls = new AlertDialog.Builder(this);
 			missedcalls.setMessage("You missed a call. Do you want to see the call log?")
 			       .setCancelable(false)
 			       .setPositiveButton("Sure!", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 							/* That dialog do this if they want to see the calls... */
 //							Intent i = new Intent();
 //							i.setAction(Intent.ACTION_VIEW);
 //							i.setData(android.provider.Contacts.People.CONTENT_URI);
 //							i.setType("vnd.android.cursor.dir/calls");
 //							startActivity(i);
 			        	   	UnalarmingActivity.this.finish();
 			           }
 			       })
 			       .setNegativeButton("Nah", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			                UnalarmingActivity.this.finish();
 			           }
 			       });
 			return missedcalls.create();
 		default:
 			return null;
 		}
 	}
 
 	/**
 	 * Handles creating the pending intent and scheduling the alarm.
 	 */
 	private void scheduleAlarm() {
 		registerReceiver();
 		// this should be how we turn off the ringer during meditation
 		audioMgr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
 
 		Log.i(TAG, "ringer-mode: " + audioMgr.getRingerMode());
 		Intent intent = createAlarmAction();
 
 		Log.i(TAG, "intent: " + intent.toString());
 
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(
 				UnalarmingActivity.this, REQUEST_CODE, intent, 0);
 
 		Log.i(TAG, "pending-intent: " + pendingIntent.toString());
 
 		// TODO: Pull out the calendar/datetime calculation to another class,
 		// likely TimeHelper
 		/* START */
 		Calendar alarm = Calendar.getInstance();
 		Calendar now = (Calendar) alarm.clone();
 
 		alarm.set(Calendar.HOUR_OF_DAY, hourOfDay);
 		alarm.set(Calendar.MINUTE, minOfDay);
 
 		Log.i(TAG, "alarm-set: " + alarm.getTimeInMillis());
 		Log.i(TAG, "system-time: " + System.currentTimeMillis());
 
 		int hourDelta = TimeHelper.elapsed(now, alarm, Calendar.HOUR_OF_DAY);
 		int minDelta = TimeHelper.elapsed(now, alarm, Calendar.MINUTE);
 		/* END */
 
 		String msg = getAlarmMessage(hourDelta, minDelta, alarm.getTime());
 		alarmMgr.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(),
 				pendingIntent);
 		Toast.makeText(UnalarmingActivity.this, msg, Toast.LENGTH_SHORT).show();
 		// add guard based on Prefs
		mPlayer.start();
 	}
 
 	private Intent createAlarmAction() {
 		Intent intent = new Intent(ALARM_ACTION);
 		intent.setClass(UnalarmingActivity.this, OnetimeAlarmReceiver.class);
 		intent.putExtra("ringerMode", previousRingerMode);
 		return intent;
 	}
 
 	/**
 	 * Registers broadcast receiver for handling incoming phone calls. 
 	 * 
 	 * Incoming phone calls will be declined during the meditation period.
 	 */
 	private void registerReceiver() {
         //declineCalls = new DeclineCallsReceiver();
         //registerReceiver(declineCalls, new IntentFilter("android.intent.action.PHONE_STATE"));
         onetimeAlarm = new OnetimeAlarmReceiver();
         registerReceiver(onetimeAlarm, alarmIntentFilter);
 	}
 
 	/**
 	 * Provides formatted message for the confirmation toast-popup.
 	 * 
 	 * @param hourDelta
 	 *            number of hours between now and the alarm time.
 	 * @param minDelta
 	 *            number of minutes between now and the alarm time.
 	 * @param alarmTime
 	 *            Date object representing the alarm time.
 	 * @return a formatted message ready for display.
 	 */
 	private String getAlarmMessage(int hourDelta, int minDelta, Date alarmTime) {
 		return String.format(
 				"Alarm set for %d hours and %d minutes from now: %s.  Enjoy!",
 				hourDelta, minDelta, FORMAT.format(alarmTime));
 	}
 	
 	/**
 	 * Handles receiving alarms for the application. 
 	 * 
 	 * A short, one-time vibration pattern is used when an alarm intent is fired.
 	 * This approach is less jarring to someone meditating then the default alarm 
 	 * in Android 2.2.2 which vibrates repetitively until dismissed.  
 	 * 
 	 * @author lenards
 	 *
 	 */
 	public static class OnetimeAlarmReceiver extends BroadcastReceiver {
 		private static final String TAG = OnetimeAlarmReceiver.class.getSimpleName();
 		
 		public static final int DO_NOT_REPEAT = -1;
 		
 		private Vibrator vib;
 
 		/**
 		 * Handles receiving a "one-time" alarm event. 
 		 * 
 		 * @param ctxt context from the Activity.
 		 * @param intent intent associated with the alarm event.
 		 */
 		@Override
 		public void onReceive(Context ctxt, Intent intent) {
 			Log.d(TAG, "in method onReceive");
 			
 			restoreRingerModeToPreviousState(ctxt, intent);		
 	        fireVibrationEvent(ctxt);
 	        signalAlarmFired(ctxt);
 		}
 
 		private void signalAlarmFired(Context ctxt) {
 			SharedPreferences sPrefs = ctxt.getSharedPreferences("Foo", MODE_WORLD_WRITEABLE);
 			SharedPreferences.Editor editor = sPrefs.edit();
 			editor.putBoolean("ALARM", true);
 			editor.commit();
 		}
 
 		/**
 		 * Triggers the phone vibration event when the alarm is raised. 
 		 * 
 		 * @param ctxt The Activity's context.
 		 */
 		private void fireVibrationEvent(final Context ctxt) {
 			// Get instance of Vibrator from current Context
 	        vib = (Vibrator) ctxt.getSystemService(Context.VIBRATOR_SERVICE);
 	    	long[] pattern = { 0, 190, 350, 190, 350, 190, 350, 190 };
 	    	vib.vibrate(pattern, DO_NOT_REPEAT);
 	    	Toast.makeText(ctxt, "Meditation period over...", Toast.LENGTH_LONG).show();
 	    	MediaPlayer bell = MediaPlayer.create(ctxt, R.raw.mbell);
 	    	bell.setOnCompletionListener(new OnCompletionListener() {
 				@Override
 				public void onCompletion(MediaPlayer mp) {
 					MediaPlayer tmp = MediaPlayer.create(ctxt, R.raw.mbell);
 					tmp.setOnCompletionListener(new OnCompletionListener() {
 						@Override
 						public void onCompletion(MediaPlayer mp) {
 							mp.release();
 						}
 					});
 					tmp.start();
 					mp.release();
 				}
 			});
 	    	bell.start();
 		}
 
 		/**
 		 * Restores the original state of the ringer mode. 
 		 * 
 		 * @param ctxt the Activity's context.
 		 * @param intent the received intent containing data.
 		 */
 		private void restoreRingerModeToPreviousState(Context ctxt, Intent intent) {
 			AudioManager audio = (AudioManager) ctxt.getSystemService(Context.AUDIO_SERVICE);
 			int ringerMode = intent.getIntExtra("ringerMode", AudioManager.RINGER_MODE_VIBRATE);
 			Log.i(TAG, "Ringer Mode stored in the intent was: " + ringerMode);
 			audio.setRingerMode(ringerMode);
 		}
 	}
 	
 	/**
 	 * Handles phone calls received during the meditation period. 
 	 * 
 	 * Any phone call received will be declined. 
 	 * 
 	 * This class includes code from Tedd Scofield's Droid Tools project.
 	 * 
 	 * @author lenards, tedd
 	 *
 	 */
 	public static class DeclineCallsReceiver extends BroadcastReceiver {
     	private static final String TAG = DeclineCallsReceiver.class.getSimpleName();
 
 		/**
 		 * Handles receiving a phone call during meditation. 
 		 * 
 		 * During the meditation period, any received phone calls will be declined.
 		 * 
 		 * @param ctxt context from the Activity.
 		 * @param intent intent associated with the alarm event.
 		 */    	
     	@Override 
 		public void onReceive(Context ctxt, Intent intent) {
 			String phone_state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
 			if (!phone_state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
 				try {
 					declinePhoneCall(ctxt);
 					markCallReceived(ctxt);
 				} catch (Exception e) {
 					Log.e(TAG, "ERROR: " + e.getMessage());
 				}
 			}
 		}
 
 		private void markCallReceived(Context ctxt) {
 			SharedPreferences sPrefs = ctxt.getSharedPreferences("Foo", MODE_WORLD_WRITEABLE);
 			SharedPreferences.Editor editor = sPrefs.edit();
 			int calls = sPrefs.getInt("CALLS", 0);
 			editor.putInt("CALLS", ++calls);
 			editor.commit();
 		}
 		
 		/**
 		 * Creates and returns an instance of the internal telephony service. 
 		 * 
 		 * Returns null if there is an issue trying to create the internal telephony service.  
 		 * The creation is done with reflection and may cause an exception on some version 
 		 * of the Android OS platform.  If so, we simply return null (callers of this method 
 		 * are expected to check for null). 
 		 * 
 		 * @return an instance of ITelephony or null to indicate failure to create instance.
 		 */
 		
 		private ITelephony createTelephonyService(Context ctxt) {
 			try {
 				// Create the telephony service (thanks to Tedd's Droid Tools!)
 				TelephonyManager tm = (TelephonyManager)ctxt.getSystemService(TELEPHONY_SERVICE);
 		
 				// Suppress the warning of Class not be a specific generic type.
 				@SuppressWarnings("unchecked") 
 				Class c = Class.forName(tm.getClass().getName());
 				
 				Method m = c.getDeclaredMethod("getITelephony");
 				m.setAccessible(true);
 				return (ITelephony) m.invoke(tm);
 			} catch (Exception e) {
 				Log.e(TAG, "ERROR: Unable to create instance of Telephony Service.");
 				Log.e(TAG, "Exception throw: " + e.getMessage());
 				return null;
 			}
 		}
 
 		/**
 		 * Performs operation to decline a phone call. 
 		 * 
 		 * @throws Exception
 		 */
 		private void declinePhoneCall(Context ctxt) throws Exception {
 			// only call this
 			ITelephony telephonyService = createTelephonyService(ctxt);
 			if (telephonyService != null) {
 				// Silence the ringer and end the call!
 				telephonyService.silenceRinger();
 				telephonyService.endCall();
 				// note that we missed a call so we can inform once alarm is
 				// trigger.
 				// ghetto - but doable is using "shared preferences"	
 			}
 		}
     };
 }
