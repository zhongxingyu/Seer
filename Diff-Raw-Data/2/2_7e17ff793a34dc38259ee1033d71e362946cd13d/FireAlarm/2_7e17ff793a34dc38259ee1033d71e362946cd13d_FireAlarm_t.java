 package org.startsmall.openalarm;
 
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.Vibrator;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.telephony.TelephonyManager;
 import android.text.format.DateUtils;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Animation;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.io.IOException;
 import java.io.FileDescriptor;
 import java.util.Calendar;
 
 public class FireAlarm extends Activity {
     // MediaPlayer enters Prepared state and now a
     // Handler should be setup to stop playback of
     // ringtone after some period of time.
     private static class StopPlayback implements Handler.Callback {
         @Override
         public boolean handleMessage(Message msg) {
             FireAlarm app;
             if (msg.obj instanceof FireAlarm) {
                 app = (FireAlarm)msg.obj;
             } else {
                 return true;
             }
 
             switch (msg.what) {
             case MESSAGE_ID_STOP_PLAYBACK:
                 // This callback is executed because user doesn't
                 // tell me what to do, i.e., dimiss or snooze. I decide
                 // to snooze it or when the FireAlarm is paused.
                 app.snoozeAlarm();
                 app.finish();
                 return true;
             default:
                 break;
             }
             return true;
         }
     }
 
     private static class OnPlaybackErrorListener implements MediaPlayer.OnErrorListener {
         @Override
         public boolean onError(MediaPlayer mp, int what, int extra) {
             Log.d(TAG, "===> onError(): " + what + "====> " + extra);
             return true;
         }
     }
 
     private static final String TAG = "FireAlarm";
 
     private static final String EXTRA_KEY_VIBRATE = "vibrate";
     private static final String EXTRA_KEY_RINGTONE = "ringtone";
     private static final String EXTRA_KEY_SNOOZE_DURATION = "snooze_duration";
     private static final int DEFAULT_SNOOZE_DURATION = 2; // 2 minutes
     private static final int MESSAGE_ID_STOP_PLAYBACK = 1;
     private static final float IN_CALL_VOLUME = 0.125f;
    private static final int PLAYBACK_TIMEOUT = 60000; // 1 minute
 
     private MediaPlayer mMediaPlayer;
     private Vibrator mVibrator;
     private Handler mHandler;
     private PowerManager mPowerManager;
     private TelephonyManager mTelephonyManager;
     private PowerManager.WakeLock mWakeLock;
     private KeyguardManager mKeyguardManager;
     private KeyguardManager.KeyguardLock mKeyguardLock;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Log.d(TAG, "===> onCreate()");
 
         // Wakeup the device and release keylock.
         mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
         mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
         mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
         mHandler = new Handler(new StopPlayback());
 
         acquireWakeLock();
 
         // requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.fire_alarm);
 
         setLabelFromIntent();
         setWindowTitleFromIntent();
 
         // Snooze this alarm makes the alarm postponded and saved
         // as a SharedPreferences.
         Button snoozeButton = (Button)findViewById(R.id.snooze);
         snoozeButton.setOnClickListener(
             new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     FireAlarm.this.snoozeAlarm();
                     FireAlarm.this.finish();
                 }
             });
 
         // Dismiss the alarm causes the ringtone playback of this
         // alarm stopped and reschudiling of this alarm happens.
         Button dismissButton = (Button)findViewById(R.id.dismiss);
         dismissButton.setOnClickListener(
             new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     FireAlarm.this.dismissAlarm();
                     FireAlarm.this.finish();
                 }
             });
 
         // Prepare MediaPlayer for playing ringtone.
         if (prepareMediaPlayer()) {
             mMediaPlayer.start();
         }
 
         startVibration();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         Log.d(TAG, "===> onDestroy()");
 
         if (mHandler != null) {
             mHandler.removeMessages(MESSAGE_ID_STOP_PLAYBACK, this);
             mHandler = null;
         }
 
         // Stop and release MediaPlayer object.
         if (mMediaPlayer != null) {
             mMediaPlayer.stop();
             mMediaPlayer.release();
             mMediaPlayer = null;
             Log.d(TAG, "===> MediaPlayer stopped and released");
         }
 
         stopVibration();
 
         releaseWakeLock();
     }
 
     // FireAlarm comes to the foreground
     @Override
     public void onResume() {
         super.onResume();
 
         Log.d(TAG, "===> onResume()");
 
         // FireAlarm goes back to interact to user. But, Keyguard
         // may be in front.
         disableKeyguard();
 
         Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
         findViewById(R.id.icon).startAnimation(shake);
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         Log.d(TAG, "===> onPause()");
 
         // Returns to keyguarded mode if the phone was in this
         // mode.
         enableKeyguard();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)  {
         if (keyCode == KeyEvent.KEYCODE_BACK &&
             event.getRepeatCount() == 0) { // don't handle BACK key.
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public void onNewIntent(Intent newIntent) {
         Log.v(TAG, "===> onNewIntent()");
 
         // Dismiss the old alarm.
         dismissAlarm();
         stopVibration();
 
         // New intent comes. Replace the old one.
         setIntent(newIntent);
 
         // Refresh UI of the existing instance.
         setLabelFromIntent();
         setWindowTitleFromIntent();
 
         // The ringtone uri might be different and timeout of
         // playback needs to be recounted.
         if (prepareMediaPlayer()) {
             mMediaPlayer.start();
         }
 
         startVibration();
     }
 
     private void setWindowTitleFromIntent() {
         Intent i = getIntent();
         final String label = i.getStringExtra(AlarmColumns.LABEL);
         setTitle(label);
     }
 
     private void setLabelFromIntent() {
         Intent i = getIntent();
         final int hourOfDay = i.getIntExtra(AlarmColumns.HOUR_OF_DAY, -1);
         final int minutes = i.getIntExtra(AlarmColumns.MINUTES, -1);
 
         Calendar calendar = Alarms.getCalendarInstance();
         calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
         calendar.set(Calendar.MINUTE, minutes);
 
         final String label =
             DateUtils.formatDateTime(this,
                                      calendar.getTimeInMillis(),
                                      DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_CAP_AMPM);
         TextView labelView = (TextView)findViewById(R.id.label);
         labelView.setText(label);
     }
 
     private void snoozeAlarm() {
         Intent i = getIntent();
         final int alarmId = i.getIntExtra(AlarmColumns._ID, -1);
         final int snoozeDuration = i.getIntExtra(EXTRA_KEY_SNOOZE_DURATION, DEFAULT_SNOOZE_DURATION);
 
         // It is not impossible that OpenAlarm is killed by task
         // manager and FireAlam is brought up by an alarm. At
         // this moment, there is no alarm in the cache map. We
         // need to load it from DB.
         Alarm alarm = Alarm.getInstance(this, alarmId);
 
         Log.d(TAG, "===> before snoozed: " + Alarms.formatDateTime(this, alarm));
 
         alarm.snooze(this, snoozeDuration);
 
         Log.d(TAG, "===> after snoozed: " + Alarms.formatDateTime(this, alarm));
     }
 
     private void dismissAlarm() {
         Intent scheduleIntent = new Intent(getIntent());
         scheduleIntent.setAction(Alarm.ACTION_SCHEDULE);
         scheduleIntent.setComponent(null);
         sendBroadcast(scheduleIntent);
 
         Log.d(TAG, "===> dismissed alarm");
     }
 
     private void startVibration() {
         Intent intent = getIntent();
         if (intent.getBooleanExtra("vibrate", false)) {
             if (mVibrator == null) {
                 mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
             }
             mVibrator.vibrate(new long[]{500, 500}, 0);
         }
     }
 
     private void stopVibration() {
         if (mVibrator != null) {
             mVibrator.cancel();
         }
     }
 
     private void acquireWakeLock() {
         if (mWakeLock == null) {
             mWakeLock =
                 mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|
                                           PowerManager.FULL_WAKE_LOCK,
                                           TAG);
         }
         mWakeLock.acquire();
     }
 
     private void releaseWakeLock() {
         if (mWakeLock != null && mWakeLock.isHeld()) {
             mWakeLock.release();
         }
     }
 
     private void enableKeyguard() {
         if (mKeyguardLock == null) {
             mKeyguardLock = mKeyguardManager.newKeyguardLock(TAG);
         }
         mKeyguardLock.reenableKeyguard();
     }
 
     private void disableKeyguard() {
         if (mKeyguardLock == null) {
             mKeyguardLock = mKeyguardManager.newKeyguardLock(TAG);
         }
         mKeyguardLock.disableKeyguard();
     }
 
     private boolean prepareMediaPlayer() {
         if (mMediaPlayer == null) {
             mMediaPlayer = new MediaPlayer();
         } else {
             mMediaPlayer.stop();
             mMediaPlayer.reset();
         }
 
         Intent intent = getIntent();
         if (intent.hasExtra(EXTRA_KEY_RINGTONE)) {
             String uriString = intent.getStringExtra(EXTRA_KEY_RINGTONE);
             if (TextUtils.isEmpty(uriString)) {
                 return false;
             }
 
             Log.d(TAG, "===> Play ringtone: " + uriString);
 
             try {
                 // Detects if we are in a call when this alarm goes off.
                 if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                     mMediaPlayer.setDataSource(this, Uri.parse(uriString));
                 } else {
                     Log.d(TAG, "===> We're in a call. Lower volume and use fallback ringtone!");
 
                     // This raw media must be supported by Android
                     // and no errors thrown from it.
                     AssetFileDescriptor afd =
                         getResources().openRawResourceFd(R.raw.in_call_ringtone);
                     mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                                                afd.getStartOffset(),
                                                afd.getLength());
                     afd.close();
                     mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                 }
             } catch (java.io.IOException e) {
                 return false;
             }
 
             mMediaPlayer.setLooping(true);
 
             // Prepare MediaPlayer into Prepared state and
             // MediaPlayer is ready to play.
             try {
                 mMediaPlayer.prepare();
             } catch (java.io.IOException e) {
                 return false;
             }
 
             // Setup a one-shot message to stop playing of
             // ringtone after TIMEOUT minutes. If it is
             // established successfully, start playing
             // ringtone and vibrate if necessary.
             mHandler.removeMessages(MESSAGE_ID_STOP_PLAYBACK, this);
             Message msg = mHandler.obtainMessage(MESSAGE_ID_STOP_PLAYBACK, this);
             return mHandler.sendMessageDelayed(msg, PLAYBACK_TIMEOUT);
         }
 
         return false;
     }
 }
