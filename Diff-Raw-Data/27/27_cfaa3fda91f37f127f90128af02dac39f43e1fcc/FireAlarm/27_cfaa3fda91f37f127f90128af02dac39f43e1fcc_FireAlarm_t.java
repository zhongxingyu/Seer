 /**
  * @file   FireAlarm.java
  * @author josh <yenliangl at gmail dot com>
  * @date   Tue Nov  3 20:33:08 2009
  *
  * @brief An activity that is launched when an alarm goes off.
  *
  *
  */
 package org.startsmall.openalarm;
 
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.content.Context;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.Vibrator;
 import android.net.Uri;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.io.IOException;
 import java.io.FileDescriptor;
 import java.util.Calendar;
 
 public class FireAlarm extends Activity {
     // MediaPlayer enters Prepared state and now a
     // Handler should be setup to stop playback of
     // ringtone after some period of time.
     private class StopPlayback implements Handler.Callback {
         @Override
         public boolean handleMessage(Message msg) {
             switch (msg.what) {
             case STOP_PLAYBACK:
                 // This callback is executed because user doesn't
                 // tell me what to do, i.e., dimiss or snooze. I decide
                 // to snooze it or when the FireAlarm is paused.
                 FireAlarm.this.snoozeAlarm();
                 FireAlarm.this.finish();
                 return true;
             default:
                 break;
             }
             return true;
         }
     }
 
     private class OnPlaybackErrorListener implements MediaPlayer.OnErrorListener {
         @Override
         public boolean onError(MediaPlayer mp, int what, int extra) {
             Log.d(TAG, "========================> onError(): " + what + "====> " + extra);
             return true;
         }
     }
 
     private static final String TAG = "FireAlarm";
 
     /// An ID used to identify the Message sent to stop the playback of ringtone in the Handler.Callback.
     private static final int STOP_PLAYBACK = 1;
 
     /// Copied from Android's source code which is claimed recommended values by media team.
     private static final float IN_CALL_VOLUME = 0.125f;
 
     private static final int PLAYBACK_TIMEOUT = 60000; // 1 minute
 
     private MediaPlayer mMediaPlayer;
     private Vibrator mVibrator;
     private Handler mHandler;
 
     private PowerManager mPowerManager;
     private PowerManager.WakeLock mWakeLock;
     private KeyguardManager mKeyguardManager;
     private KeyguardManager.KeyguardLock mKeyguardLock;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Log.d(TAG, "==============================> onCreate()");
 
         // Wakeup the device and release keylock.
         mPowerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
         mKeyguardManager = (KeyguardManager)this.getSystemService(Context.KEYGUARD_SERVICE);
 
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
 
         // Start playing ringtone loop.
         Intent intent = getIntent();
         if(intent.hasExtra("ringtone")) {
             mMediaPlayer = new MediaPlayer();
             mMediaPlayer.setOnErrorListener(new OnPlaybackErrorListener());
 
             try {
                 // Detects if we are in a call when this alarm goes off.
                 TelephonyManager tm = (TelephonyManager)getSystemService(
                     Context.TELEPHONY_SERVICE);
                 if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                     String rtUri = intent.getStringExtra("ringtone");
                     Log.d(TAG, "Play ringtone: " + rtUri);
                     mMediaPlayer.setDataSource(this, Uri.parse(rtUri));
                 } else {
                     Log.d(TAG, "We're in a call. Lower volume and use fallback ringtone!");
                     // This raw media must be supported by
                     // Android and no errors thrown from it.
                     AssetFileDescriptor afd =
                         getResources().openRawResourceFd(R.raw.in_call_ringtone);
                     mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                                                afd.getStartOffset(),
                                                afd.getLength());
                     afd.close();
                     mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                 }
             } catch (Exception e1) {
                 // mMediaPlayer had entered Error state and
                 // OnErrorListener was called asynchronously.
                 // mMediaPlayer.reset();
                 // AssetFileDescriptor afd =
                 //     getResources().openRawResourceFd(R.raw.in_call_ringtone);
                 // mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                 //                            afd.getStartOffset(),
                 //                            afd.getLength());
                 // afd.close();
             }
 
             mMediaPlayer.setLooping(true);
 
             try {
                 mMediaPlayer.prepare();
             } catch (Exception e) {
                 Log.d(TAG, "=========> mMediaPlayer.prepare(): " + e);
                 return;
             }
 
             // Set a 3-minutes one-shot stop timer for stopping
             // the MediaPlayer.
             mHandler = new Handler(new StopPlayback());
             Message stopPlaybackMessage = mHandler.obtainMessage(STOP_PLAYBACK, mMediaPlayer);
             if (mHandler.sendMessageDelayed(stopPlaybackMessage,
                                             PLAYBACK_TIMEOUT)) {
                 // Play ringtone now.
                 mMediaPlayer.start();
                vibrate(true);
             } else {
                 Log.d(TAG, "===> Unable to enqueue message");
             }
         }
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         Log.d(TAG, "===============================> onDestroy()");
 
         if (mHandler != null) {
             mHandler.removeMessages(STOP_PLAYBACK, mMediaPlayer);
             mHandler = null;
         }
 
         // Stop and release MediaPlayer object.
         if (mMediaPlayer != null) {
             mMediaPlayer.stop();
             mMediaPlayer.release();
             mMediaPlayer = null;
             Log.d(TAG, "===> MediaPlayer stopped and released");
         }
 
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
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         Log.d(TAG, "===============================> onPause()");
 
         // Returns to keyguarded mode if the phone was in this
         // mode.
         enableKeyguard();
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event)  {
         if (keyCode == KeyEvent.KEYCODE_BACK &&
             event.getRepeatCount() == 0) { // don't handle BACK key.
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     public void onNewIntent(Intent newIntent) {
         Log.d(TAG, "===============================> onNewIntent()");
 
         // Dismiss the old alarm.
         dismissAlarm();
 
         setIntent(newIntent);
 
         // Refresh UI of the existing instance.
         setLabelFromIntent();
         setWindowTitleFromIntent();
     }
 
     private void setWindowTitleFromIntent() {
         Intent i = getIntent();
         final String label = i.getStringExtra(Alarms.AlarmColumns.LABEL);
         setTitle(label);
     }
 
     private void setLabelFromIntent() {
         Intent i = getIntent();
         // Recalculate the new time of the alarm.
         final int hourOfDay = i.getIntExtra(Alarms.AlarmColumns.HOUR, -1);
         final int minutes = i.getIntExtra(Alarms.AlarmColumns.MINUTES, -1);
         final String label =
             Alarms.formatTime(Alarms.is24HourMode(this), hourOfDay, minutes, true);
         TextView labelView = (TextView)findViewById(R.id.label);
         labelView.setText(label);
     }
 
     private void snoozeAlarm() {
         Intent i = getIntent();
         final int alarmId = i.getIntExtra(Alarms.AlarmColumns._ID, -1);
         final String label = i.getStringExtra(Alarms.AlarmColumns.LABEL);
         final int repeatOnDays = i.getIntExtra(Alarms.AlarmColumns.REPEAT_DAYS, -1);
         final String handlerClassName = i.getStringExtra(Alarms.AlarmColumns.HANDLER);
         final String extraData = i.getStringExtra(Alarms.AlarmColumns.EXTRA);
 
         Log.d(TAG, "===============================> snoozeAlarm(): alarm id=" + alarmId);
 
         Alarms.snoozeAlarm(FireAlarm.this, alarmId, label, repeatOnDays,
                            handlerClassName, extraData, 2);

        // Disable vibrator
        vibrate(false);
     }
 
     private void dismissAlarm() {
         final Intent intent = getIntent();
         final int alarmId = intent.getIntExtra(Alarms.AlarmColumns._ID, -1);
         final Uri alarmUri = Alarms.getAlarmUri(alarmId);
 
         Log.d(TAG, "===============================> dismissAlarm(): alarm uri=" + alarmUri);
 
         // Disable the old alert. The explicit class field of
         // the Intent was set to this activity when setting alarm
         // in AlarmManager..
         final String handlerClassName =
             intent.getStringExtra(Alarms.AlarmColumns.HANDLER);
         Alarms.disableAlarm(this, alarmId, handlerClassName);
 
         // Recalculate the new time of the alarm.
         final int hourOfDay = intent.getIntExtra(Alarms.AlarmColumns.HOUR, -1);
 
         // If user clicks dimiss button in the same minute as
         // this alarm, the calculateAlarmAtTimeInMillis() will
         // return the same hour and minutes which causes this
         // Activity to show up continuously.
         final int minutes = intent.getIntExtra(Alarms.AlarmColumns.MINUTES, -1) - 1;
         final int repeatOnDaysCode = intent.getIntExtra(Alarms.AlarmColumns.REPEAT_DAYS, -1);
         final long atTimeInMillis =
             Alarms.calculateAlarmAtTimeInMillis(hourOfDay, minutes, repeatOnDaysCode);
 
         final String label = intent.getStringExtra(Alarms.AlarmColumns.LABEL);
         final String extraData = intent.getStringExtra(Alarms.AlarmColumns.EXTRA);
         Alarms.enableAlarm(this, alarmId, label, atTimeInMillis, repeatOnDaysCode, handlerClassName, extraData);
 
         // Update the new time into database.
         ContentValues newValues = new ContentValues();
         newValues.put(Alarms.AlarmColumns.AT_TIME_IN_MILLIS, atTimeInMillis);
         Alarms.updateAlarm(this, alarmUri, newValues);
 
         // Notify the system that this alarm is changed.
         Alarms.setNotification(this, true);
 
        // Disable vibrator
        vibrate(false);
    }
 
    private void vibrate(boolean enabled) {
         Intent intent = getIntent();
         if (intent.hasExtra("vibrate") &&
             intent.getBooleanExtra("vibrate", false)) {
             if (mVibrator == null) {
                mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            }
            if (enabled) {
                mVibrator.vibrate(new long[]{500, 500}, 0);
            } else {
                mVibrator.cancel();
             }
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
 }
