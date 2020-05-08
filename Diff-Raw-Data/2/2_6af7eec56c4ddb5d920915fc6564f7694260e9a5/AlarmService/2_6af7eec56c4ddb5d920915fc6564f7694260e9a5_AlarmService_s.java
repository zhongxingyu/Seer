 package com.koushikdutta.klaxon;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.app.KeyguardManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Vibrator;
 
 public class AlarmService extends Service {
 	
 	MediaPlayer mPlayer;
 	KlaxonSettings mKlaxonSettings;
 	AlarmSettings mSettings;
 	SQLiteDatabase mDatabase;
 	KeyguardManager mKeyguardManager;
 	KeyguardManager.KeyguardLock mKeyguardLock;
 	Vibrator mVibrator;
 	AudioManager mAudioManager;
 	final int AUDIO_STREAM = AudioManager.STREAM_MUSIC;
 	GregorianCalendar mSnoozeEnd;
 	GregorianCalendar mExpireTime;
 	boolean mStopVolumeAdjust = false;
 	static final int EXPIRE_TIME = 30;
 	Handler mHandler = new Handler();
 	double mCurVolume = 0;
 	Runnable mVolumeRunnable;
 	NotificationManager mNotificationManager;
 	long mId = -1;
 	int mSnoozeTime = 10;
 	String mName = "Alarm";
 	boolean mVibrateEnabled = true;
 	int mVolumeRamp = 20;
 	int mMaxVolume = 100;
 
 	@Override
 	public void onCreate() {
 		AlarmAlertWakeLock.acquire(this);
 		super.onCreate();
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		
 		init(intent);
 		
 		if (intent.getBooleanExtra("snooze", false))
 		{
 			snoozeAlarm();
 		}
 		else
 		{
 			startAlarm();
 		}
 	}
 	
 	void init(Intent intent)
 	{
 		if (mDatabase == null)
 		{
 			mId = intent.getLongExtra(AlarmSettings.GEN_FIELD__ID, -1);
 			Log.i(String.format("Initializing Alarm Service with Alarm ID %d", mId));
 			mDatabase = AlarmSettings.getDatabase(this);
 			mSettings = AlarmSettings.getAlarmSettingsById(this, mDatabase, mId);
 			mKlaxonSettings = new KlaxonSettings(this);
 			mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
 			mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
 			mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 			mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 
 			Uri ringtoneUri = null; 
 			if (mSettings != null)
 			{
				mSettings.getRingtone();
 				mSnoozeTime = mSettings.getSnoozeTime();
 				mName = mSettings.getName();
 				if (mName == null)
 					mName = "Alarm";
 				mVibrateEnabled = mSettings.getVibrateEnabled();
 				mVolumeRamp = mSettings.getVolumeRamp();
 				mMaxVolume = mSettings.getVolume();
 			}
 			try
 			{
 				if (ringtoneUri != null)
 					mPlayer = MediaPlayer.create(this, ringtoneUri);
 				else
 					mPlayer = MediaPlayer.create(this, R.raw.klaxon);
 				mPlayer.setLooping(true);
 			}
 			catch (Exception e)
 			{
 				mPlayer = MediaPlayer.create(this, R.raw.klaxon);
 				mPlayer.setLooping(true);
 			}
 			mPlayer.setAudioStreamType(AUDIO_STREAM);
 			
 			String notifytext = getString(R.string.app_name);
 			String title = getString(R.string.alarm_ticker);
 			String content = mName + " - " + AlarmSettings.formatLongDayAndTime(this, new Date(intent.getLongExtra("AlarmTime", 0)));
 			Notification notification = new Notification(R.drawable.icon, notifytext, 0);
 			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;
 			PendingIntent pending = PendingIntent.getActivity(this, 0, getAlarmActivityIntent(), Intent.FLAG_ACTIVITY_NEW_TASK);
 			notification.setLatestEventInfo(this, title, content, pending);
 			mNotificationManager.notify(1234, notification);
 		}
 	}
 
 	void snoozeAlarm()
 	{
 		int snoozeTime = mSnoozeTime;
 		if (snoozeTime == 0)
 			return;
 		Log.v("snoozeAlarm");
 		AlarmAlertWakeLock.release();
 		mVibrator.cancel();
 		mPlayer.pause();
 		mStopVolumeAdjust = true;
 		mVibrator.vibrate(300);
 		mExpireTime = null;
 		mSnoozeEnd = new GregorianCalendar();
 		mSnoozeEnd.add(Calendar.MINUTE, snoozeTime);
 		if (mSettings != null)
 		{
 			mSettings.setNextSnooze(mSnoozeEnd.getTimeInMillis());
 			mSettings.setEnabled(true);
 			mSettings.update();
 		}
 		AlarmSettings.scheduleNextAlarm(this);
 	}
 
 	
 	void startAlarm()
 	{
 		disableKeyguard();
 		final double streamMaxVolume = mAudioManager.getStreamMaxVolume(AUDIO_STREAM);
 		if (mVolumeRamp == 0)
 		{
 			double convertedVolume = streamMaxVolume * mMaxVolume / 100d;
 			mAudioManager.setStreamVolume(AUDIO_STREAM, (int)convertedVolume, 0);
 		}
 		else
 		{
 			mAudioManager.setStreamVolume(AUDIO_STREAM, 0, 0);
 			mStopVolumeAdjust = false;
 			final double volumeStep = mMaxVolume / mVolumeRamp;
 			mCurVolume = 0;
 
 			if (mVolumeRunnable == null)
 			{
 				mVolumeRunnable = new Runnable()
 				{
 					public void run()
 					{
 						try
 						{
 							if (mStopVolumeAdjust)
 								return;
 							mCurVolume += volumeStep;
 							double convertedVolume = streamMaxVolume * mCurVolume / 100d;
 							mAudioManager.setStreamVolume(AUDIO_STREAM, (int)convertedVolume, 0);
 							if (mCurVolume >= mMaxVolume)
 								return;
 							mHandler.postDelayed(this, 1000);
 						}
 						catch (Exception ex)
 						{
 							ex.printStackTrace();
 						}
 					}
 				};
 			}
 			mHandler.postDelayed(mVolumeRunnable, 1000);
 		}
 
 		if (mVibrateEnabled)
 			mVibrator.vibrate(new long[] { 5000, 1000 }, 0);
 
 		AlarmAlertWakeLock.acquire(this);
 		mExpireTime = new GregorianCalendar();
 		mExpireTime.add(Calendar.MINUTE, EXPIRE_TIME);
 		mPlayer.start();
 		mPlayer.seekTo(0);
 		
 		startActivity(getAlarmActivityIntent());
 	}
 	
 	Intent getAlarmActivityIntent()
 	{
 		Intent i = new Intent(this, AlarmActivity.class);
 		i.putExtra(AlarmSettings.GEN_FIELD__ID, mId);
 		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		return i;
 	}
 	
 	void cleanupAlarm()
 	{
 		Log.v("cleanupAlarm");
 		mStopVolumeAdjust = true;
 		mVibrator.cancel();
 		if (mPlayer != null)
 		{
 			mPlayer.stop();
 			mPlayer.release();
 			mPlayer = null;
 		}
 		AlarmAlertWakeLock.release();
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		Log.v("onDestroy");
 		if (mSettings != null)
 		{
 			mSettings.setNextSnooze(0);
 			if (mSettings.isOneShot())
 				mSettings.setEnabled(false);
 			mSettings.update();
 		}
 		AlarmSettings.scheduleNextAlarm(this);
 		cleanupAlarm();
 		enableKeyguard();
 		mNotificationManager.cancelAll();
 		mDatabase.close();
 	}
 
     private synchronized void enableKeyguard() {
         if (mKeyguardLock != null) {
             mKeyguardLock.reenableKeyguard();
             mKeyguardLock = null;
         }
     }
 
     private synchronized void disableKeyguard() {
         if (mKeyguardLock == null) {
             mKeyguardLock = mKeyguardManager.newKeyguardLock(Log.LOGTAG);
             mKeyguardLock.disableKeyguard();
         }
     }
 	
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 }
