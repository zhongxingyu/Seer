 package org.bxmy.shiftclock;
 
 import java.io.IOException;
 
 import org.bxmy.shiftclock.shiftduty.Alarm;
 import org.bxmy.shiftclock.shiftduty.ShiftDuty;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ShiftClockActivity extends Activity {
 
     private Alarm mCurrentAlarm;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         ShiftDuty.getInstance().init(this);
 
         binds();
 
         // 启动分两种情况：闹铃启动和桌面启动
         mCurrentAlarm = ShiftDuty.getInstance().getNextAlarmTime();
         if (mCurrentAlarm != null) {
             setAlarmTime(this, mCurrentAlarm.getNextAlarmSeconds(),
                     mCurrentAlarm.getIntervalSeconds());
             setNextWatch(mCurrentAlarm.getWatchTime());
         } else {
             cancelAlarmTime(this);
             setNextWatch(null);
         }
 
         IntentFilter filter = new IntentFilter();
         filter.addAction(ACTION_SHUTDOWN);
         registerReceiver(this.mShutdownReceiver, filter);
 
         checkAlarm(getIntent());
     }
 
     private void binds() {
         Button disable = (Button) findViewById(R.id.button_disable);
         disable.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 cancelAlarmTime();
                 stopAlarmRing();
                 findViewById(R.id.button_pauseAlarm).setEnabled(false);
                 if (mCurrentAlarm != null) {
                     mCurrentAlarm.disable();
                     updateCurrentAlarm();
                 }
             }
         });
 
         Button pause = (Button) findViewById(R.id.button_pauseAlarm);
         pause.setEnabled(false);
         pause.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 findViewById(R.id.button_pauseAlarm).setEnabled(false);
                 stopAlarmRing();
                 if (mCurrentAlarm != null) {
                     mCurrentAlarm.pause();
                     updateCurrentAlarm();
                 }
             }
         });
 
         Button changeJob = (Button) findViewById(R.id.button_changeJob);
         changeJob.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent();
                 intent.setClass(ShiftClockActivity.this, JobActivity.class);
 
                 startActivity(intent);
             }
         });
 
         Button setDuty = (Button) findViewById(R.id.button_setDuty);
         setDuty.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent();
                 intent.setClass(ShiftClockActivity.this, DutyListActivity.class);
 
                 startActivity(intent);
             }
         });
 
         Button setWatch = (Button) findViewById(R.id.button_setWatch);
         setWatch.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 Intent intent = new Intent();
                 intent.setClass(ShiftClockActivity.this, WatchActivity.class);
 
                 startActivity(intent);
             }
         });
 
         Button about = (Button) findViewById(R.id.button_about);
         about.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 Intent intent = new Intent();
                 intent.setClass(ShiftClockActivity.this, AboutActivity.class);
 
                 startActivity(intent);
             }
         });
 
         Button regularDuty = (Button) findViewById(R.id.button_regularDuty);
         regularDuty.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO: 固定值班设置
             }
         });
 
         Button adjustHoliday = (Button) findViewById(R.id.button_adjustHoliday);
         adjustHoliday.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // TODO: 节假日值班调整
             }
         });
 
         Button setConfig = (Button) findViewById(R.id.button_config);
         setConfig.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent();
                 intent.setClass(ShiftClockActivity.this, ConfigActivity.class);
 
                 startActivity(intent);
             }
         });
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
 
         Log.d("shiftclock", "newIntent " + intent.toString());
         checkAlarm(intent);
     }
 
     @Override
     public void onResume() {
         Log.d("shiftclock", "onResume");
         super.onResume();
 
         Alarm nextAlarm = ShiftDuty.getInstance().getNextAlarmTime();
         if (nextAlarm != null && nextAlarm.isSame(mCurrentAlarm)) {
             mCurrentAlarm = nextAlarm;
             setNextWatch(mCurrentAlarm.getWatchTime());
             return;
         }
 
         mCurrentAlarm = nextAlarm;
         if (mCurrentAlarm != null) {
             setAlarmTime(this, mCurrentAlarm.getNextAlarmSeconds(),
                     mCurrentAlarm.getIntervalSeconds());
             setNextWatch(mCurrentAlarm.getWatchTime());
         } else {
             cancelAlarmTime(this);
             setNextWatch(null);
         }
     }
 
     @Override
     protected void onDestroy() {
         shutdown(false);
 
         stopAlarmRing();
         ShiftDuty.getInstance().close();
 
         super.onDestroy();
     }
 
     private void checkAlarm(Intent intent) {
         if (mCurrentAlarm == null)
             return;
 
         if (intent.hasExtra("alarmTime")) {
             long alarmTime = intent.getLongExtra("alarmTime", 0);
             if (mCurrentAlarm.isValidAlarm(alarmTime)) {
                 findViewById(R.id.button_pauseAlarm).setEnabled(true);
                 playAlarmRing(this);
             }
         }
     }
 
     private void updateCurrentAlarm() {
         if (mCurrentAlarm != null) {
             setAlarmTime(this, mCurrentAlarm.getNextAlarmSeconds(),
                     mCurrentAlarm.getIntervalSeconds());
             setAlarmTime(mCurrentAlarm.getNextAlarmSeconds());
         } else {
             cancelAlarmTime();
             setAlarmTime(0);
         }
     }
 
     private void setAlarmTime(long timeInSeconds) {
         TextView label = (TextView) findViewById(R.id.text_alarmTime);
         label.setText(R.string.label_alarmTime);
         if (timeInSeconds != 0) {
             String t = Util.formatTimeToNow(timeInSeconds);
             label.append(t);
         } else {
             label.append("无");
         }
     }
 
     private void setAlarmTime(Context context, long timeInSeconds,
             long intervalSeconds) {
         Log.i("shiftclock", "set alarm " + this);
        cancelAlarmTime(this);
 
         AlarmManager am = (AlarmManager) context
                 .getSystemService(Context.ALARM_SERVICE);
         Intent intent = new Intent(ACTION_ALARM);
         PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
                 PendingIntent.FLAG_CANCEL_CURRENT);
         am.setRepeating(AlarmManager.RTC_WAKEUP, timeInSeconds * 1000,
                 intervalSeconds * 1000, sender);
         this.mAlarmSender = sender;
         setAlarmTime(timeInSeconds);
     }
 
     private void setNextWatch(String date) {
         TextView nextWatch = (TextView) findViewById(R.id.text_nextWatch);
         nextWatch.setText(R.string.label_nextWatch);
         if (!TextUtils.isEmpty(date))
             nextWatch.append(date);
         else
             nextWatch.append("尚未设置新的值班");
     }
 
     private void cancelAlarmTime() {
        Log.i("shiftclock", "cancel alarm");
         cancelAlarmTime(this);
     }
 
     private void cancelAlarmTime(Context context) {
         if (this.mAlarmSender == null)
             return;
 
         AlarmManager am = (AlarmManager) context
                 .getSystemService(Context.ALARM_SERVICE);
         am.cancel(this.mAlarmSender);
         this.mAlarmSender = null;
         setAlarmTime(0);
     }
 
     public static class AlarmReceiver extends BroadcastReceiver {
         public void onReceive(Context context, Intent intent) {
             if (ACTION_ALARM.equals(intent.getAction())) {
                 Log.i("shiftclock", "alarm");
                if (mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
                    Intent alarmIntent = new Intent();
                    alarmIntent.setClass(context, ShiftClockActivity.class);
                    alarmIntent.putExtra("alarmTime", Util.now());
                    alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(alarmIntent);
                } else {
                    Log.i("shiftclock", "alarm while playing");
                }
             }
         }
     }
 
     public static class BootReceiver extends BroadcastReceiver {
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                 Log.i("shiftclock", "receive boot completed");
             } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
                 Log.i("shiftclock", "receive system shutdown");
                 Intent shutdownIntent = new Intent();
                 shutdownIntent.setAction(ACTION_SHUTDOWN);
                 context.sendBroadcast(shutdownIntent);
             }
         }
     }
 
     private void shutdown(boolean broadcast) {
         Log.i("shiftclock", "call shutdown() " + this);
         synchronized (ACTION_SHUTDOWN) {
             if (this.mShutdownReceiver != null) {
                 unregisterReceiver(this.mShutdownReceiver);
                 this.mShutdownReceiver = null;
             }
         }
 
         if (broadcast) {
             finish();
         }
     }
 
     private static void playAlarmRing(Context context) {
         Log.d("shiftclock", "play alarm ring");
         stopAlarmRing();
 
         Uri uri = RingtoneManager
                 .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
         try {
             mMediaPlayer = new MediaPlayer();
             mMediaPlayer.setDataSource(context, uri);
             final AudioManager audioManager = (AudioManager) context
                     .getSystemService(Context.AUDIO_SERVICE);
             if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                 mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                 mMediaPlayer.setLooping(true);
                 mMediaPlayer.prepare();
             }
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         mMediaPlayer.start();
     }
 
     private static void stopAlarmRing() {
         Log.d("shiftclock", "stop alarm ring");
         if (mMediaPlayer != null) {
             mMediaPlayer.stop();
         }
     }
 
     private static String ACTION_ALARM = "org.bxmy.shiftclock.action.alarm";
 
     private static String ACTION_SHUTDOWN = "org.bxmy.shiftclock.action.shutdown";
 
     private PendingIntent mAlarmSender;
 
     private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             Log.i("shiftclock", "shutdownReceiver::onReceive()");
             shutdown(true);
         }
     };
 
     private static MediaPlayer mMediaPlayer;
 }
