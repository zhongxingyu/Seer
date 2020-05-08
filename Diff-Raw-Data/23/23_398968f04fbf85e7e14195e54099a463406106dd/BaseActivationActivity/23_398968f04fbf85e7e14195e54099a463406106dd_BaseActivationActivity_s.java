 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
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
 package edu.chalmers.dat255.group09.Alarmed.activity;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 
 public abstract class BaseActivationActivity extends Activity {
 
 	private final static String WAKE_LOCK_TAG = "edu.chalmers.dat255.group09.Alarmed.activity.BaseActivationActivity";
 	private WakeLock wakeLock;
 	private AudioManager audioManager;
 	private MediaPlayer mediaPlayer;
 	private Vibrator vibrator;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		enterFullScreen();
 		initWakeLock();
 		initServices();
 		startAlarm();
 	}
 
 	private void enterFullScreen() {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		Window window = getWindow();
 		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
 	}
 
 	private void initWakeLock() {
 		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 
 		wakeLock = powerManager.newWakeLock(
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
 						| PowerManager.FULL_WAKE_LOCK
 						| PowerManager.ACQUIRE_CAUSES_WAKEUP, WAKE_LOCK_TAG);
 
 		wakeLock.acquire();
 	}
 
 	private void initServices() {
 		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		mediaPlayer = new MediaPlayer();
 		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 	}
 
 	private void startAlarm() {
 		startVibration();
 		startAudio();
 	}
 
 	private void startVibration() {
 		long[] vibPattern = { 0, 200, 500 };
 		vibrator.vibrate(vibPattern, 0);
 	}
 
 	private void startAudio() {
 		try {
 			mediaPlayer.setDataSource(this, getAlarmTone());
 			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
 				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
 				mediaPlayer.prepare();
 				mediaPlayer.start();
 			}
 		} catch (IOException e) {
 			Log.d("Sound", "Sound I/O error");
 		}
 
 	}
 
 	private Uri getAlarmTone() {
 		Uri alertTone = RingtoneManager
 				.getDefaultUri(RingtoneManager.TYPE_ALARM);
 		if (alertTone == null) {
 			alertTone = RingtoneManager
 					.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
 			if (alertTone == null) {
 				alertTone = RingtoneManager
 						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 			}
 		}
 		return alertTone;
 	}
 
 	public void stopAlarm() {
 		vibrator.cancel();
 		mediaPlayer.stop();
 		wakeLock.release();
 		finish();
 	}

 	@Override
 	public void onBackPressed() {
 		// The user is not aloud to use the backbutton
 	}
 
 }
