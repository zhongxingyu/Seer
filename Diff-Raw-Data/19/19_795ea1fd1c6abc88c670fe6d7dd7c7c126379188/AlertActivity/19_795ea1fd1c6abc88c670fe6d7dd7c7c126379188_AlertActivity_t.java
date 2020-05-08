 package org.tamanegi.parasiticalarm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.os.PowerManager;
 import android.os.SystemClock;
 import android.os.Vibrator;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AlertActivity extends Activity
 {
     private static final long[] VIBRATE_PATTERN = { 500, 500 };
 
     private static final int AUDIO_MSGID = 1;
     private static final long AUDIO_INTERVAL = 800;
     private static final long AUDIO_TOTAL_DURATION = 120000;
 
     private PowerManager.WakeLock wakelock;
     private Vibrator vibrator;
     private MediaPlayer mediaplayer;
     private Handler handler;
 
     private boolean isInAlert;
     private boolean isAfter;
     private long alertStartTime;
 
     private int alarmId;
     private boolean snoozeEnabled;
     private List<Uri> alertAudio;
     private int alertAudioCur;
     private Uri afterAudio;
     private Uri afterImage;
     private boolean vibrationEnabled;
 
     private ImageView image;
     private TextView messageText;
     private View stopArea;
     private View snoozeArea;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
         wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                   PowerManager.ACQUIRE_CAUSES_WAKEUP, "Alert");
         wakelock.acquire();
         AlertWakeLock.release();     // acquired at AlarmService#alarm
 
         vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
 
         mediaplayer = new MediaPlayer();
         mediaplayer.setOnPreparedListener(onPlayerPreparedListener);
         mediaplayer.setOnCompletionListener(onPlayerCompletionListener);
         mediaplayer.setOnErrorListener(onPlayerErrorListener);
 
         handler = new Handler(delayedPlayCallback);
 
         getWindow().addFlags(
             WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
             WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
             WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
 
         setContentView(R.layout.activity_alert);
         image = (ImageView)findViewById(R.id.alert_image);
         messageText = (TextView)findViewById(R.id.alert_message);
         stopArea = findViewById(R.id.alert_stop_area);
         snoozeArea = findViewById(R.id.alert_snooze_area);
 
         findViewById(R.id.alert_stop_button)
             .setOnClickListener(onClickStopListener);
         findViewById(R.id.alert_dismiss_button)
             .setOnClickListener(onClickDismissListener);
         findViewById(R.id.alert_snooze_button)
             .setOnClickListener(onClickSnoozeListener);
 
         registerReceiver(screenOffReceiver,
                          new IntentFilter(Intent.ACTION_SCREEN_OFF));
 
         applyParameters(getIntent());
     }
 
     @Override
     protected void onNewIntent(Intent intent)
     {
         super.onNewIntent(intent);
 
         AlertWakeLock.release();     // acquired at AlarmService#alarm
 
         applyParameters(intent);
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         if(isInAlert) {
             startAlert();
         }
     }
 
     @Override
     protected void onPause()
     {
         super.onPause();
 
         handler.removeMessages(AUDIO_MSGID);
         mediaplayer.reset();
 
         if(vibrationEnabled) {
             vibrator.cancel();
         }
     }
 
     @Override
     protected void onDestroy()
     {
         super.onDestroy();
 
         unregisterReceiver(screenOffReceiver);
         mediaplayer.release();
 
         wakelock.release();
     }
 
     @Override
     public void onBackPressed()
     {
         if(isInAlert) {
             // do nothing: ignore back key
             return;
         }
 
         finish();
     }
 
     private void applyParameters(Intent intent)
     {
         isInAlert = true;
         isAfter = false;
 
         alarmId = intent.getIntExtra(AlarmService.EXTRA_ALARM_ID, -1);
         snoozeEnabled =
             intent.getBooleanExtra(AlarmService.EXTRA_SNOOZE_ENABLED, false);
         {
             Parcelable[] alertAudioArray = (Parcelable[])
                 intent.getParcelableArrayExtra(AlarmService.EXTRA_ALERT_AUDIO);
             alertAudio = new ArrayList<Uri>();
             for(Parcelable uri : alertAudioArray) {
                 alertAudio.add((Uri)uri);
             }
 
             Collections.shuffle(alertAudio);
             alertAudioCur = 0;
         }
         afterAudio = (Uri)
             intent.getParcelableExtra(AlarmService.EXTRA_AFTER_AUDIO);
         afterImage = (Uri)
             intent.getParcelableExtra(AlarmService.EXTRA_AFTER_IMAGE);
         vibrationEnabled =
             intent.getBooleanExtra(AlarmService.EXTRA_VIBRATION_ENABLED, false);
 
         Uri alertImage = (Uri)
             intent.getParcelableExtra(AlarmService.EXTRA_ALERT_IMAGE);
         image.setImageURI(alertImage);
 
         String message =
             intent.getStringExtra(AlarmService.EXTRA_ALERT_MESSAGE);
         if(message != null) {
             messageText.setText(message);
         }
         else {
             messageText.setVisibility(View.GONE);
         }
     }
 
     private void startAlert()
     {
         isInAlert = true;
         isAfter = false;
 
         stopArea.setVisibility(View.VISIBLE);
         snoozeArea.setVisibility(View.GONE);
 
         // todo: set volume
         playAudio(getNextAlertAudio());
 
         if(vibrationEnabled) {
             vibrator.vibrate(VIBRATE_PATTERN, 0);
         }
 
         alertStartTime = SystemClock.elapsedRealtime();
     }
 
     private void stopAlert()
     {
         isInAlert = false;
 
         stopArea.setVisibility(View.GONE);
         snoozeArea.setVisibility(View.VISIBLE);
 
         handler.removeMessages(AUDIO_MSGID);
         mediaplayer.reset();
 
         if(vibrationEnabled) {
             vibrator.cancel();
         }
 
         if(! snoozeEnabled) {
             startAfter();
         }
     }
 
     private void cancelSnooze()
     {
         startService(new Intent(this, AlarmService.class)
                      .setAction(AlarmService.ACTION_CANCEL_SNOOZE)
                      .putExtra(AlarmService.EXTRA_ALARM_ID, alarmId));
         startAfter();
     }
 
     private void startAfter()
     {
         isAfter = true;
         stopArea.setVisibility(View.GONE);
         snoozeArea.setVisibility(View.GONE);
 
         image.setImageURI(afterImage);
         playAudio(afterAudio);
     }
 
     private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 finish();
             }
         };
 
     private View.OnClickListener onClickStopListener =
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 stopAlert();
             }
         };
 
     private View.OnClickListener onClickDismissListener =
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 snoozeArea.setVisibility(View.GONE);
             }
         };
 
     private View.OnClickListener onClickSnoozeListener =
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 cancelSnooze();
             }
         };
 
     private Uri getNextAlertAudio()
     {
         Uri uri = alertAudio.get(alertAudioCur);
         alertAudioCur = (alertAudioCur + 1) % alertAudio.size();
 
         return uri;
     }
 
     private void playAudio(Uri uri)
     {
         mediaplayer.reset();
 
         if(uri == null) {
             return;
         }
 
         try {
             mediaplayer.setDataSource(this, uri);
             mediaplayer.prepareAsync();
         }
         catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     private MediaPlayer.OnPreparedListener onPlayerPreparedListener =
         new MediaPlayer.OnPreparedListener() {
             @Override
             public void onPrepared(MediaPlayer mp) {
                 mp.start();
             }
         };
 
     private MediaPlayer.OnCompletionListener onPlayerCompletionListener =
         new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mp) {
                 long cur = SystemClock.elapsedRealtime();

                if(! isAfter) {
                    if(cur > alertStartTime + AUDIO_TOTAL_DURATION) {
                        stopAlert();
                    }
                    else {
                        handler.sendMessageDelayed(
                            Message.obtain(
                                handler, AUDIO_MSGID, getNextAlertAudio()),
                            AUDIO_INTERVAL);
                    }
                 }
                 else {
                     mp.reset();
                 }
             }
         };
 
     private MediaPlayer.OnErrorListener onPlayerErrorListener =
         new MediaPlayer.OnErrorListener() {
             @Override
             public boolean onError(MediaPlayer mp, int what, int extra) {
                 mp.reset();
                 return true;
             }
         };
 
     private Handler.Callback delayedPlayCallback = new Handler.Callback() {
             @Override
             public boolean handleMessage(Message msg) {
                 playAudio((Uri)msg.obj);
                 return true;
             }
         };
 }
