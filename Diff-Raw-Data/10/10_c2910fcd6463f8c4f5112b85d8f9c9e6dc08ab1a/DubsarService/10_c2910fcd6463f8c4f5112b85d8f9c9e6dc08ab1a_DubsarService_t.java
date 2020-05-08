 /*
  Dubsar Dictionary Project
  Copyright (C) 2010-11 Jimmy Dee
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.dubsar_dictionary.Dubsar;
 
 import java.io.BufferedInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.Calendar;
 import java.util.Formatter;
 import java.util.Random;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.BaseColumns;
 
 public class DubsarService extends Service implements CommsMonitor.CommsSubscriber {
 
 	public static final int WOTD_ID=1;
 	public static final int MILLIS_PER_DAY=86400000;
 	
	public static final String ACTION_WOTD = "com.dubsar_dictionary.WOTD";
	public static final String ACTION_WOTD_NOTIFICATION = "com.dubsar_dictionary.WOTD_NOTIFICATION";
	public static final String ACTION_WOTD_TIME = "com.dubsar_dictionary.WOTD_TIME";
	public static final String ACTION_WOTD_PURGE = "com.dubsar_dictionary.WOTD_PURGE";
	public static final String ACTION_WOTD_MOCK = "com.dubsar_dictionary.WOTD_MOCK";
 	public static final String WOTD_TEXT = "wotd_text";
 	public static final String ERROR_MESSAGE = "error_message";
 	public static final String WOTD_TIME = "wotd_time";
 	
 	public static final String WOTD_FILE_NAME = "wotd.txt";
 
 	private volatile Timer mTimer=new Timer(true);
 	private volatile NotificationManager mNotificationMgr=null;
 	private volatile long mNextWotdTime=0;
 	
 	private volatile CommsMonitor mCommsMonitor=null;
 	
 	private volatile int mWotdId=0;
 	private volatile String mWotdText=null;
 	private volatile String mWotdNameAndPos=null;
 	private volatile String mErrorMessage=null;
 	
 	private volatile Random mGenerator = new Random(System.currentTimeMillis());
 	
 	private boolean mTestMode=false;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		// Log.i(getString(R.string.app_name), getTimestamp() + ": DubsarService created");
 		
 		mNotificationMgr =
 				(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		mCommsMonitor = new CommsMonitor(this);
 
 		loadWotdData();
 
 		/*
 		 * mNextWotdTime is 0 on error or it holds the value of mNextWotdTime
 		 * from the last time the service ran. If this is the first time the
 		 * application has run, mNextWotdTime will be 0, since the file won't
 		 * exist.
 		 */
 		
 		if (mNextWotdTime > 0) {
 			/*
 			Log.i(getString(R.string.app_name), "loaded WOTD time from storage: " +
 					formatTime(mNextWotdTime));
 			 */
 		}
 		// Log.d(getString(R.string.app_name), "Finished onCreate()");
 	}
 
 	@Override
 	public void onDestroy() {
 		if (mCommsMonitor != null) {
 			mCommsMonitor.teardownReceiver(this);
 		}
 
 		// Log.i(getString(R.string.app_name), getTimestamp() + ": DubsarService destroyed");
 		super.onDestroy();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		super.onStartCommand(intent, flags, startId);
 		/*
 		Log.i(getString(R.string.app_name), getTimestamp() + ": start command received, action=" +
 				(intent != null ? intent.getAction() : "(null intent)"));
 		 */
 		
 		/*
 		 * Special non-sticky actions for testing and maintenance.
 		 */
 		
 		/* Ignore redelivery and retry of purge intents */
 		if (flags == 0 && intent != null && intent.getAction() != null &&
 			intent.getAction().equals(ACTION_WOTD_PURGE)) {
 			// Log.d(getString(R.string.app_name), "WOTD PURGE");
 
 			purgeData();
 			return START_NOT_STICKY;
 		}
 		/* mock for test mode */
 		else if (intent != null && intent.getAction() != null &&
 			intent.getAction().equals(ACTION_WOTD_MOCK)) {
 			setupMock(intent);
 			return START_NOT_STICKY;
 		}
 		else if (mTestMode) {
 			return START_NOT_STICKY;
 		}
 
 		if (intent != null && intent.getAction() != null &&
 				ACTION_WOTD_TIME.equals(intent.getAction())) {
 			/*
 			 * This is not necessary unless background data usage is disabled.
 			 */
 			mNextWotdTime = computeNextWotdTime(getWotdHour(), getWotdMinute()) +
 					(int)(60000f*mGenerator.nextFloat());
 			
 			saveWotdData();
 			// Log.i(getString(R.string.app_name), "Next WOTD at " + formatTime(mNextWotdTime));
 		}
 
 		/*
 		 * Issue #5 (https://github.com/jdee/dubsar_android/issues/5)
 		 * The timer frequently does not fire if the service has been running
 		 * for a long time. As a workaround, we reset the timer any time we
 		 * receive any start command. If the service is running, but the app
 		 * is not, if the user starts the app, this will have the effect of
 		 * kicking the timer. We also check to see if the timer didn't fire.
 		 */
 		long now = System.currentTimeMillis();
 
 		/*
 		 * Check to see if the timer has fired. We add a 2-second cushion
 		 * to avoid a duplicate request, in the event that the timer just fired
 		 * now. mNextWotdTime is updated whenever a response is received.
 		 */
 		boolean requestNow = !hasError() &&	now > mNextWotdTime + 2000;
 
 		/*
 		 * First reset the timer.
 		 * 
 		 * If background data usage is disabled, but we are initializing from
 		 * scratch, we still need to set the next WOTD time, to know when the
 		 * timer would fire if it were set. The call to setNextWotdTime() will
 		 * not schedule the timer if background data usage is disabled.
 		 */
 		if (!hasError() && (mNextWotdTime == 0 || backgroundDataUsageAllowed())) {
 			/*
 			 * If the service is in an error state, it will keep trying to
 			 * recover and eventually compute the correct new time once it has
 			 * recovered the connection.
 			 */
 
 			/* Only reset the time if we need to or are told to */
 			if (mNextWotdTime <= now ||
 				(intent != null &&
 				intent.getAction() != null &&
 				intent.getAction().equals(ACTION_WOTD_TIME))) {
 				setNextWotdTime();
 			}
 			else {
 				/*
 				 * Avoid unnecessary timer jitter. Don't recompute (with random
 				 * delay) each time we reschedule the timer. This is at any rate
 				 * a kluge to address Issue #5.
 				 */
 				scheduleNextWotd();
 			}
 		}
 
 		/*
 		 * Now make the immediate request if the data are not current, and the
 		 * timer we just set is not about to fire, or if background data usage
 		 * is turned off, in which case there's no timer, and this is our 
 		 * chance to do it in the foreground (more or less).
 		 */
 		requestNow = requestNow && (!backgroundDataUsageAllowed() || mNextWotdTime > now + 2000);
 		if (requestNow) {
 			/*
 			Log.d(getString(R.string.app_name),
 				"requesting now; next WOTD time: " + formatTime(mNextWotdTime));
 			 */
 			requestNow();
 		}
 
 		if (intent == null || intent.getAction() == null) {
 			if (!backgroundDataUsageAllowed()) {
 				if (!requestNow) {
 					stopSelf();
 				}
 				return START_NOT_STICKY;
 			}
 			else {
 				return START_STICKY;
 			}
 		}
 
 		/*
 		 * If we just requested this, don't bother.
 		 */
 		if (!requestNow && ACTION_WOTD_NOTIFICATION.equals(intent.getAction())) {
 			/*
 			 * If in an error state, and the user activates notifications,
 			 * we pop up the last WOTD, with the appropriate time.
 			 */
 			long lastWotdTime = mNextWotdTime > now ?
 					mNextWotdTime - MILLIS_PER_DAY : mNextWotdTime;
 			generateNotification(lastWotdTime);
 		}
 		else if (ACTION_WOTD.equals(intent.getAction())) {
 			generateBroadcast();
 		}
 		
 		if (!backgroundDataUsageAllowed()) {
 			if (!requestNow) {
 				stopSelf();
 			}
 			return START_NOT_STICKY;
 		}
 		else {
 			return START_STICKY;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onLowMemory() {
 		/* hardly a mission-critical component */
 		/*
 		Log.i(getString(R.string.app_name), getTimestamp() +
 				": Exiting due to low memory.");
 		 */
 		stopSelf();
 	}
 
 	@Override
 	public Context getContext() {
 		return this;
 	}
 
 	@Override
 	public void onBackgroundDataSettingChanged() {
 		/*
 		 * Ordinarily the service shuts down as soon as it is finished with its
 		 * current task if background data usage is disabled. It should not
 		 * already be running unless background data usage is enabled. The
 		 * BACKGROUND_DATA_SETTING_CHANGED broadcast is not sticky, so these
 		 * notifications only arrive live, and are never stale. In the
 		 * event of a race, two closely-spaced events could arrive before the
 		 * service finishes shutting down, the second event indicating that
 		 * background data usage is enabled again. However, this service instance
 		 * would already be shutting down after the first event arrived. The
 		 * CommsReceiver will restart the service on receipt of the second event.
 		 * So any time we receive this callback, we simply exit.
 		 */
 		stopSelf();
 	}
 	
 	public int getWotdHour() {
 		SharedPreferences preferences =
 				getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, MODE_PRIVATE);
 		return preferences.getInt(PreferencesActivity.WOTD_HOUR,
 				PreferencesActivity.WOTD_HOUR_DEFAULT);		
 	}
 	
 	public int getWotdMinute() {
 		SharedPreferences preferences =
 				getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, MODE_PRIVATE);
 		return preferences.getInt(PreferencesActivity.WOTD_MINUTE,
 				PreferencesActivity.WOTD_MINUTE_DEFAULT);		
 	}
 	
 	public long getNextWotdTime() {
 		return mNextWotdTime;
 	}
 
 	public boolean notificationsEnabled() {
 		SharedPreferences preferences =
 				getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, MODE_PRIVATE);
 		return preferences.getBoolean(PreferencesActivity.WOTD_NOTIFICATIONS, true);
 	}
 
 	public boolean hasError() {
 		return mErrorMessage != null;
 	}
 	
 	public final String getErrorMessage() {
 		return mErrorMessage;
 	}
 	
 	public void setErrorMessage(String errorMessage) {
 		mErrorMessage = errorMessage;
 	}
 	
 	public boolean isNetworkAvailable() {
 		return mCommsMonitor.networkAvailable;
 	}
 	
 	public boolean backgroundDataUsageAllowed() {
 		return mCommsMonitor.backgroundDataUsageAllowed;
 	}
 
 	protected void clearError() {
 		// Log.d(getString(R.string.app_name), "clearing error, resetting timer");
 		mErrorMessage = null;
 	}
 
 	protected void resetTimer() {
 
 		mTimer.cancel();
 		mTimer = new Timer(true);
 	}
 
 	protected void setupMock(Intent intent) {
 		Bundle extras = intent.getExtras();
 
 		if (intent.hasExtra(WOTD_TIME)) {
 			/*
 			 Log.d(getString(R.string.app_name),
 				"using time extra " + formatTime(mNextWotdTime));
 			 */
 			mNextWotdTime = extras.getLong(WOTD_TIME);
 		}
 		mWotdId = extras.getInt(BaseColumns._ID);
 		mWotdText = extras.getString(WOTD_TEXT);
 		mWotdNameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
 
 		/*
 		Log.d(getString(R.string.app_name), "mock service with ID=" +
 			mWotdId + ", text=\"" + mWotdText + "\", name and pos=\"" +
 			mWotdNameAndPos + "\"");
 		 */
 
 		generateBroadcast();
 		saveWotdData();
 		resetTimer();
 
 		mTestMode = true;
 	}
 	
 	/**
 	 * Store WOTD data on the local filesystem.
 	 */
 	protected void saveWotdData() {
 		try {
 			FileOutputStream fos=null;
 			try {
 				fos = openFileOutput(WOTD_FILE_NAME, MODE_PRIVATE);
 	
 				fos.write(encodeLong(mNextWotdTime));
 				fos.write(encodeLong(mWotdId));
 
 				byte[] data;
 				
 				if (mWotdText != null) {
 					data = mWotdText.getBytes();
 					fos.write(encodeLong(data.length));
 					fos.write(data);
 				}
 				else {
 					fos.write(encodeLong(0));
 				}
 				
 				if (mWotdNameAndPos != null) {
 					data = mWotdNameAndPos.getBytes();
 					fos.write(encodeLong(data.length));
 					fos.write(data);
 				}
 				else {
 					fos.write(encodeLong(0));
 				}
 				
 				/*
 				Log.i(getString(R.string.app_name), "Wrote WOTD data to " +
 						WOTD_FILE_NAME);
 				 */
 			}
 			catch (FileNotFoundException e) {
 				/*
 				Log.e(getString(R.string.app_name),
 						"OPEN " + WOTD_FILE_NAME + ": " + e.getMessage());
 				 */			
 			}
 			finally {
 				if (fos != null) fos.close();
 			}
 		}
 		catch (IOException e) {
 			/*
 			Log.e(getString(R.string.app_name),
 					"WRITE " + WOTD_FILE_NAME + ": " + e.getMessage());
 			 */
 		}
 	}
 
 	/**
 	 * Load saved data from storage.
 	 */
 	protected void loadWotdData() {
 		// Log.i(getString(R.string.app_name), "Loading WOTD data from " + WOTD_FILE_NAME);
 		try {
 			BufferedInputStream input=null;
 			try {
 				input = new BufferedInputStream(openFileInput(WOTD_FILE_NAME), 256);
 				// Log.d(getString(R.string.app_name), " Opened " + WOTD_FILE_NAME);
 				
 				byte[] lbuffer = new byte[8];
 				byte[] sbuffer;
 				int length;
 	
 				/* next WOTD time */
 				input.read(lbuffer);
 				mNextWotdTime = decodeLong(lbuffer);
 				
 				/*
 				Log.d(getString(R.string.app_name), " loaded WOTD time: " +
 						formatTime(mNextWotdTime));
 				 */
 				
 				/* WOTD ID */
 				input.read(lbuffer);
 				mWotdId = (int)decodeLong(lbuffer);
 				
 				// Log.d(getString(R.string.app_name), " loaded WOTD ID: " + mWotdId);
 				
 				/* WOTD text */
 				input.read(lbuffer);
 				length = (int)decodeLong(lbuffer);
 				
 				//  Log.d(getString(R.string.app_name), " length of WOTD text is " + length);
 				if (length > 256) {
 					throw new Exception("invalid data length: " + length);
 				}
 
 				if (length > 0) {
 					sbuffer = new byte[length];
 					input.read(sbuffer);
 					mWotdText = new String(sbuffer);
 					// Log.d(getString(R.string.app_name), " loaded WOTD text: " + mWotdText);
 				}
 				
 				/* WOTD name and pos */
 				input.read(lbuffer);
 				length = (int)decodeLong(lbuffer);
 				
 				// Log.d(getString(R.string.app_name), " length of WOTD name and pos is " + length);
 				if (length > 256) {
 					throw new Exception("invalid data length: " + length);
 				}
 
 				if (length > 0) {
 					sbuffer = new byte[length];
 					input.read(sbuffer);
 					mWotdNameAndPos = new String(sbuffer);
 					// Log.d(getString(R.string.app_name), " loaded WOTD name and pos: " + mWotdNameAndPos);
 				}
 			}
 			catch (FileNotFoundException e) {
 				/*
 				Log.w(getString(R.string.app_name),
 						"OPEN " + WOTD_FILE_NAME + ": " + e.getMessage());
 				 */
 			}
 			finally {
 				if (input != null) input.close();
 			}
 		}
 		catch (Exception e) {
 			/*
 			Log.e(getString(R.string.app_name),
 					"READ " + WOTD_FILE_NAME + ": " + e.getMessage());
 			 */
 			mNextWotdTime = mWotdId = 0;
 			mWotdText = mWotdNameAndPos = null;
 		}
 
 		/*
 		 * If anything fails to load, invalidate it all and force a new request.
 		 */
 		if (mWotdId == 0 || mWotdText == null || mWotdNameAndPos == null)
 			mNextWotdTime = 0;
 	}
 	
 	protected void purgeData() {
 		deleteFile(WOTD_FILE_NAME);
 	}
 	
 	protected void saveResults(Cursor cursor) {
 		int idColumn = cursor.getColumnIndex(BaseColumns._ID);
 		int nameAndPosColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
 		int freqCntColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_FREQ_CNT);
 		
 		cursor.moveToFirst();
 		
 		mWotdId = cursor.getInt(idColumn);
 		mWotdNameAndPos = cursor.getString(nameAndPosColumn);
 		
 		int freqCnt = cursor.getInt(freqCntColumn);
 		mWotdText = new String(mWotdNameAndPos);
 		if (freqCnt > 0) {
 			mWotdText += " freq. cnt.: " + freqCnt;
 		}
 		
 		/*
 		Log.d(getString(R.string.app_name), "WOTD ID = " + mWotdId);
 		Log.d(getString(R.string.app_name), "WOTD TEXT = " + mWotdText);
 		Log.d(getString(R.string.app_name), "WOTD NAME AND POS = " + mWotdNameAndPos);
 		 */
 	}
 	
 	protected void generateNotification(long time) {
 		if (notificationsEnabled() && !hasError()) {
 			Notification notification = new Notification(R.drawable.ic_dubsar_rounded,
 					getString(R.string.dubsar_wotd), time);
 			notification.flags = Notification.FLAG_AUTO_CANCEL;
 			
 			Intent wordIntent = new Intent(this, WordActivity.class);
 			wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, mWotdText);
 			Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
 					DubsarContentProvider.WORDS_URI_PATH + "/" + mWotdId);
 			wordIntent.setData(uri);
 			
 			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, wordIntent, 0);
 			
 			notification.setLatestEventInfo(this, getString(R.string.dubsar_wotd), 
 					mWotdText, contentIntent);
 			if (!mTestMode) {
 				/* obnoxious in automated testing */
 				notification.defaults = Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
 				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 				notification.ledARGB = 0xff0000ff;
 				notification.ledOnMS = 800;
 				notification.ledOffMS = 600;
 			}
 			
 			mNotificationMgr.notify(WOTD_ID, notification);
 		}
 		
 		generateBroadcast();
 	}
 
 	protected void generateBroadcast() {
 		if (!hasError() &&
 			(mWotdId == 0 || mWotdText == null || mWotdNameAndPos == null)) {
 			return;
 		}
 		
 		Intent broadcastIntent = new Intent();
 		broadcastIntent.setAction(ACTION_WOTD);
 		if (!hasError()) {
 			/*
 			Log.i(getString(R.string.app_name), "Broadcast: ID=" +
 				mWotdId + ", text=\"" + mWotdText + "\", name and pos=\"" +
 				mWotdNameAndPos + "\"");
 			 */
 			broadcastIntent.putExtra(BaseColumns._ID, mWotdId);
 			broadcastIntent.putExtra(WOTD_TEXT, mWotdText);
 			broadcastIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS,
 					mWotdNameAndPos);
 		}
 		else {
 			// Log.i(getString(R.string.app_name), "Broadcast: error=" + mErrorMessage);
 			broadcastIntent.putExtra(ERROR_MESSAGE, mErrorMessage);
 		}
 		
 		sendStickyBroadcast(broadcastIntent);
 	}
 
 	/**
 	 * Compute the next word of the day time and schedule the timer. The
 	 * timer will have an additional random delay between [0, 60000) ms, to
 	 * avoid spiking the server.
 	 */
 	protected void setNextWotdTime() {
 		mNextWotdTime = computeNextWotdTime(getWotdHour(), getWotdMinute()) +
 			(int)(60000f*mGenerator.nextFloat());
 		
 		scheduleNextWotd();
 		saveWotdData();
 	}
 
 	protected void scheduleNextWotd() {
 		resetTimer();
 		if (!backgroundDataUsageAllowed()) return;
 
 		mTimer.schedule(new WotdTimer(this), mNextWotdTime - System.currentTimeMillis());
 
 		// Log.i(getString(R.string.app_name), "Next WOTD at " + formatTime(mNextWotdTime));
 	}
 	
 	protected static String getTimestamp() {
 		return formatTime(System.currentTimeMillis());
 	}
 
 	/**
 	 * Return a string with the provided time formatted as yyyy-mm-dd HH:MM:SS.
 	 * @param time the time to format, in milliseconds
 	 * @return a string containing the formatted time
 	 */
 	protected static String formatTime(long time) {
 		StringBuffer output = new StringBuffer();
 		Formatter formatter = new Formatter(output);
 		formatter.format("%tY-%tm-%td %tT", time, time, time, time);
 		return output.toString();
 	}
 
 	public static long computeNextWotdTime(int hourUtc, int minuteUtc) {
 		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
 		int _amPm = time.get(Calendar.AM_PM);
 		int hour = time.get(Calendar.HOUR);
 		int minute = time.get(Calendar.MINUTE);
 		int second = time.get(Calendar.SECOND);
 		int millis = time.get(Calendar.MILLISECOND);
 		
 		if (_amPm == Calendar.PM) hour += 12;
 		
 		/* 
 		 * This may be an earlier time of day (e.g., the time is now 19:30,
 		 * and the timer is set to fire at 14:00).
 		 */
 		if (hour > hourUtc ||
 		    (hour == hourUtc && minute >= minuteUtc)) hourUtc += 24;
 		
 		long millisTillNext = (hourUtc-hour)*3600000 + 
 				(minuteUtc-minute-1)*60000 + 
 				(59-second)*1000 +
 				(1000-millis);
 		
 		time.setTimeInMillis(time.getTimeInMillis() + millisTillNext);
 		
 		return time.getTimeInMillis();
 	}
 	
 	protected void requestNow() {
 		/*
 		 * Use the TimerTask as an AsyncTask, in effect.
 		 */
 		long lastWotdTime;
 		if (mNextWotdTime > 0) {
 			lastWotdTime = mNextWotdTime > System.currentTimeMillis() ?
 				mNextWotdTime - MILLIS_PER_DAY : mNextWotdTime;
 		}
 		else {
 			lastWotdTime = computeNextWotdTime(getWotdHour(), getWotdMinute()) -
 					MILLIS_PER_DAY;
 		}
 		
 		mTimer.schedule(new WotdTimer(this, lastWotdTime), 0);
 	}
 
 	protected void startRerequesting() {
 		resetTimer();
 		if (!backgroundDataUsageAllowed()) {
 			stopSelf();
 			return;
 		}
 
 		// begin rechecking every 5 seconds
 		mTimer.scheduleAtFixedRate(new WotdTimer(this), 5000, 5000);
 	}
 
 	protected void noNetworkError() {
 		if (!hasError() || !mErrorMessage.equals(getString(R.string.no_network))) {
 			mErrorMessage = getString(R.string.no_network);
 			// Log.e(getString(R.string.app_name), mErrorMessage);
 
 			generateBroadcast();
 			startRerequesting();
 		}
 	}
 	
 	/*
 	 * This is when it really sucks to use Java
 	 */
 	public static byte[] encodeLong(long data) {
 		byte[] buffer = new byte[8];
 		long mask = 0x00000000000000ff;
 		for (int j=0; j<8; ++j) {
 			long shifted = data >> (8*(8-j));
 			buffer[j] = (byte)(shifted&mask);
 		}
 
 		return buffer;
 	}
 	
 	public static long decodeLong(byte[] buffer) {
 		long data=0x0000000000000000;
 		for (int j=0; j<8; ++j) {
 			long _byte = (long)buffer[j];
 			if (_byte < 0) _byte = 256 + _byte;
 
 			data |= (_byte << (8*(8-j)));
 		}
 
 		return data;
 	}
 	
 	static class WotdTimer extends TimerTask {
 		
 		private final WeakReference<DubsarService> mServiceReference;
 		private long mWotdTime=0;
 		
 		public WotdTimer(DubsarService service) {
 			mServiceReference = new WeakReference<DubsarService>(service);
 		}
 		
 		public WotdTimer(DubsarService service, long wotdTime) {
 			mServiceReference = new WeakReference<DubsarService>(service);
 			mWotdTime = wotdTime;
 		}
 		
 		public DubsarService getService() {
 			return mServiceReference != null ? mServiceReference.get() : null;
 		}
 
 		@Override
 		public void run() {
 			// Log.d("Dubsar", getTimestamp() + ": timer fired");
 			if (getService() == null) return;
 
 			if (!getService().isNetworkAvailable()) {
 				getService().noNetworkError();
 				return;
 			}
 			/* If I'm recovering from a network outage, clear my state */
 			else if (getService().hasError() &&
 				getService().getErrorMessage().equals(getService().getString(R.string.no_network))) {
 				getService().clearError();
 				getService().setNextWotdTime();
 				
 				/*
 				 * As in onStartCommand(), if the expiration time is very near,
 				 * just let the timer fire, to avoid a couple of closely-
 				 * spaced status bar notifications or even possibly out-of-
 				 * order responses in case this first one takes a long time.
 				 */
 				if (getService().getNextWotdTime() -
 						System.currentTimeMillis() <= 2000) return;
 			}
 
 			Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
 					DubsarContentProvider.WOTD_URI_PATH);
 		
 			/*
 			Log.i(getService().getString(R.string.app_name),
 					"requesting WOTD, URI " + uri);
 			 */
 			
 			ContentResolver resolver = getService().getContentResolver();
 			Cursor cursor = resolver.query(uri, null, null, null, null);
 			
 			// Log.i("Dubsar", getTimestamp() + ": Request completed");
 			
 			// the request should not take long, but since we have a weak
 			// reference:
 			if (getService() == null) return;
 			
 			if (cursor == null) {
 				if (!getService().hasError() || !getService().getErrorMessage().equals(R.string.search_error)) {
 					getService().setErrorMessage(getService().getString(R.string.search_error));
 					getService().generateBroadcast();
 					getService().startRerequesting();
 				}
 				return;
 			}
 			
 			/* Success */
 			
 			/* If I'm recovering from a search error, reset my state */
 			if (getService().hasError()) {
 				getService().clearError();
 				getService().setNextWotdTime();
 			}
 			
 			long notificationTime = mWotdTime != 0 ? mWotdTime : System.currentTimeMillis();
 			
 			getService().saveResults(cursor);
 			getService().generateNotification(notificationTime);
 			getService().setNextWotdTime();
 
 			cursor.close();
 			
 			if (!getService().backgroundDataUsageAllowed()) {
 				getService().stopSelf();
 			}
 		}
 	}
 }
