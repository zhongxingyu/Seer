 package com.weirdapparatus.guardingsound.service;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.widget.Toast;
 import com.weirdapparatus.guardingsound.common.EnergySavingType;
 import com.weirdapparatus.guardingsound.common.PlaybackCommand;
 import com.weirdapparatus.guardingsound.common.SoundType;
 
 import java.io.IOException;
 
 /**
  * Service to do playback on a separate stream other than UI.
  *
  * @author memphis
  */
 public class PlaybackService extends Service {
 
    private static EnergySavingType playbackType;

     private AssetFileDescriptor descriptor;
     private MediaPlayer mediaPlayer;
 
     private final Handler playbackHandler = new Handler();
     private final Handler resumeHandler = new Handler();
 
     private final Runnable pauseSound = new Runnable() {
         @Override
         public void run() {
             playResume(true);
            pauseFor(playbackType);
         }
     };
     private final Runnable resumeSound = new Runnable() {
         @Override
         public void run() {
             playResume(false);
            playFor(playbackType);
         }
     };
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         mediaPlayer = new MediaPlayer();
         try {
             descriptor = getAssets().openFd(SoundType.DRAGON_FLY.getSound());
             mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
             descriptor.close();
             mediaPlayer.prepare();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Bundle extras = intent.getExtras();
 
         PlaybackCommand action = (PlaybackCommand) extras.get("action"); // start, pause, stop
         SoundType soundType = (SoundType) extras.get("soundType");
        playbackType = (EnergySavingType) extras.get("playbackType");
 
         if (PlaybackCommand.START.equals(action)) {
             prepareMedia(soundType);
             playResume(false);
             playFor(playbackType);
 
         } else if (PlaybackCommand.STOP.equals(action)) {
             mediaPlayer.stop();
             mediaPlayer = null;
             stopAll();
         }
 
         return START_STICKY; // running until we are stopped
     }
 
     private void prepareMedia(SoundType soundType) {
         try {
             mediaPlayer = new MediaPlayer();
             descriptor = getAssets().openFd(soundType.getSound());
             mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
             descriptor.close();
             mediaPlayer.prepare();
 
         } catch (IOException e) {
             Toast.makeText(this, "Cannot initialise playback", Toast.LENGTH_SHORT);
             e.printStackTrace();
         }
     }
 
     private void playResume(boolean pause) {
         if (pause) {
             mediaPlayer.pause();
         } else {
             mediaPlayer.setLooping(true);
             mediaPlayer.start();
         }
     }
 
     private void playFor(EnergySavingType energySavingType) {
         if (!EnergySavingType.ENDLESS.equals(energySavingType)) {
             playbackHandler.postDelayed(pauseSound, energySavingType.getPlayTime());
         }
     }
 
     private void pauseFor(EnergySavingType energySavingType) {
         if (!EnergySavingType.ENDLESS.equals(energySavingType)) {
             resumeHandler.postDelayed(resumeSound, energySavingType.getDelayTime());
         }
     }
 
     private void stopAll() {
         playbackHandler.removeCallbacks(pauseSound);
         resumeHandler.removeCallbacks(resumeSound);
     }
 }
