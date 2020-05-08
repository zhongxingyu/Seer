 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.services;
 
 import java.util.List;
 import java.util.Random;
 
import javax.annotation.Nullable;

 import android.app.backup.BackupManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.speech.RecognizerIntent;
 import android.view.HapticFeedbackConstants;
 import android.view.LayoutInflater;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.main.AppContext;
 import com.zapta.apps.maniana.util.LogUtil;
 import com.zapta.apps.maniana.util.PackageUtil;
 
 /**
  * Provides common app services.
  * 
  * @author Tal Dayan
  */
 public class AppServices {
 
     /** A combined listener. */
     private static interface MediaPlayerListener extends OnCompletionListener, OnErrorListener {
     }
 
     /** The app context. */
     private final AppContext mApp;
 
     private final int mAppVersionCode;
 
     private final String mAppVersionName;
 
     /** Cached window manager for this app. */
     private final WindowManager mWindowManager;
 
     private final LayoutInflater mLayoutInflater;
 
     private final MediaPlayerListener mMediaPlayerListener = new MediaPlayerListener() {
         @Override
         public void onCompletion(MediaPlayer mp) {
             releaseMediaPlayer();
         }
 
         @Override
         public boolean onError(MediaPlayer mp, int what, int extra) {
             LogUtil.error("Error when playing applause track (%d, %d)", what, extra);
             releaseMediaPlayer();
             return true;
         }
     };
 
     /** In the range [0, 1] */
     private final float mNormalizedSoundEffectVolume;
 
     private final Random mRandom;
 
     private final BackupManager mBackupManager;
 
    @Nullable
     private MediaPlayer mMediaPlayer;
 
     public AppServices(AppContext app) {
         this.mApp = app;
 
         PackageInfo packageInfo = PackageUtil.getPackageInfo(mApp.context());
         mAppVersionCode = packageInfo.versionCode;
         mAppVersionName = packageInfo.versionName;
 
         mWindowManager = (WindowManager) app.context().getSystemService(Context.WINDOW_SERVICE);
         mLayoutInflater = (LayoutInflater) app.context().getSystemService(
                         Context.LAYOUT_INFLATER_SERVICE);
 
         mNormalizedSoundEffectVolume = mApp.context().getResources().getInteger(
                         R.integer.sound_effect_volume_percent) / 100.0f;
 
         mRandom = new Random();
 
         mBackupManager = new BackupManager(mApp.context());
     }
 
     public int getAppVersionCode() {
         return mAppVersionCode;
     }
 
     public String getAppVersionName() {
         return mAppVersionName;
     }
 
     public final WindowManager windowManager() {
         return mWindowManager;
     }
 
     public final BackupManager backupManager() {
         return mBackupManager;
     }
 
     public final LayoutInflater layoutInflater() {
         return mLayoutInflater;
     }
 
     /** Activate a medium length vibration */
     public final void vibrateForLongPress() {
         mApp.view().getRootView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                         HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
     }
 
     /**
      * Start a sound affect if allowed.
      * 
      * @param fxEffectType the sound effect to use (one of )
      * @param fallBackToShortVibration indicates what to do if sound effects are disabled in
      *            settings. If true then activate a short vibration instead, otherwise do nothing.
      */
     public final void maybePlayStockSound(int fxEffectType, boolean fallBackToShortVibration) {
         if (mApp.pref().getSoundEnabledPreference()) {
             // TODO: cache volume (float) in the constructor.
             // final int soundEffectVolumePrecent = mApp.context().getResources().getInteger(
             // R.integer.sound_effect_volume_percent);
             mApp.resources().getAudioManager().playSoundEffect(fxEffectType,
                             mNormalizedSoundEffectVolume);
         } else if (fallBackToShortVibration) {
             vibrateForLongPress();
         }
     }
 
     private final void releaseMediaPlayer() {
         if (mMediaPlayer != null) {
             mMediaPlayer.release();
             mMediaPlayer = null;
         }
     }
 
     public final void maybePlayApplauseSoundClip(int fallbackFxEffectType,
                     boolean fallBackToShortVibration) {
         if (shouldPlayApplauseSoundClip()) {
             // releaseMediaPlayer();
 
             // Determine sound track to play
             final int rand = mRandom.nextInt(100);
             final int trackResourceId = (rand < 90) ? R.raw.applause_normal
                             : R.raw.applause_special;
 
             startPlayingSoundClip(trackResourceId);
             return;
         }
         maybePlayStockSound(fallbackFxEffectType, fallBackToShortVibration);
     }
 
     /**
      * Determine if a request for an applause should play an applause or should fall back.
      * 
      * @return true if applause should be played.
      */
     private final boolean shouldPlayApplauseSoundClip() {
         if (!mApp.pref().getSoundEnabledPreference()) {
             return false;
         }
         switch (mApp.pref().getApplauseLevelPreference()) {
             case NEVER:
                 return false;
             case ALWAYS:
                 return true;
             default:
                 // 20% probability
                 return mRandom.nextInt(100) < 20;
         }
     }
 
 
     private final void startPlayingSoundClip(int rawResourceId) {
         releaseMediaPlayer();
         mMediaPlayer = MediaPlayer.create(mApp.context(), rawResourceId);
        
        // Added as a response for this FC report from a user
        // https://code.google.com/p/maniana/issues/detail?id=8
        // Apparently MediaPlayer.create() may fail for unspecified reasons.
        if (mMediaPlayer == null) {
            LogUtil.error("Creation of a media player failed. Resource id = 0x%x", rawResourceId);
            return;
        }
        
         mMediaPlayer.setOnCompletionListener(mMediaPlayerListener);
         mMediaPlayer.setOnErrorListener(mMediaPlayerListener);
         mMediaPlayer.start();
     }
 
 
     /** Show a brief popup message with given formatted string */
     public final void toast(String format, Object... args) {
         toast(String.format(format, args));
     }
 
     /**
      * Show a brief popup message with given string. More efficient than the vararg one since it
      * does not allocate a vararg array
      */
     public final void toast(String message) {
         Toast.makeText(mApp.context(), message, Toast.LENGTH_SHORT).show();
     }
 
     public static boolean isVoiceRecognitionSupported(Context context) {
         // Check to see if a recognition activity is present
         final PackageManager pm = context.getPackageManager();
         List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                 RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
         return activities.size() != 0;
     }
 }
