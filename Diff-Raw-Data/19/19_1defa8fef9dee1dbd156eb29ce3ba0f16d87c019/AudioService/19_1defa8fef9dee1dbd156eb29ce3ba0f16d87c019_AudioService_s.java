 /**
  Copyright 2013 Vikram Aggarwal
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 package com.eggwall.BabyMusic;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.os.Build;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Random;
 
 /**
  * Runs the music in the background and holds a wake lock during the duration of music playing.
  */
 public class AudioService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
     /** For logging */
     private static final String TAG = "AudioService";
     /** The tag used to pass the music type. Can only be MUSIC or WHITE_NOISE */
     public static final String TYPE = "type";
     /** Stop playing any audio. */
     public static final int SILENCE = 0;
     /** Play music from the SD card */
     public static final int MUSIC = 1;
     /** Play standard white noise file (included in the application */
     public static final int WHITE_NOISE = 2;
 
     /** This represents in invalid position in the list and also an invalid resource. */
     private static final int INVALID_POSITION = -1;
     /** Name of the directory in the main folder containing baby music */
     private final static String BABY_MUSIC_DIR = "babysong";
     /** The ID for the global notification we post. */
     private final static int NOTIFICATION_ID = 0;
 
     /** Single instance of random number generator */
     private final Random mRandom = new Random();
     /** The SDK version */
     private final static int SDK = Build.VERSION.SDK_INT;
 
     /** The object that actually plays the music on our behalf. */
     private MediaPlayer mPlayer;
     /** Set to MUSIC or WHITE_NOISE */
     private int mTypePlaying = 0;
     /** The actual directory that corresponds to the external SD card. */
     private File mBabyDir;
     /** Names of all the songs */
     private String[] mFilenames;
     /** The global manager for notifications */
     private NotificationManager mNotificationManager;
 
     @Override
     public boolean onError(MediaPlayer mp, int what, int extra) {
         releasePlayer();
         Log.e(TAG, "BabyMusic.AudioService encountered onError");
         // Propagate the error up.
         return false;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (mNotificationManager == null) {
             mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         }
         // If we don't get an extra (impossible), play white noise.
         final int typeOfResource = intent.getIntExtra("type", WHITE_NOISE);
         if (mTypePlaying == typeOfResource || typeOfResource == SILENCE) {
             // Pressing the same button twice is an instruction to stop playing this music.
             mTypePlaying = SILENCE;
             stopSelf();
             return 0;
         }
         // Switch to the other type of music
         mTypePlaying = typeOfResource;
         releasePlayer();
         play(mTypePlaying);
         return 0;
     }
 
     /**
      * Start playing the resource specified here.
      *
      * @param type Either MUSIC, or WHITE_NOISE. Passing the same ID twice
      *             is a signal to stop playing music altogether.
      */
     private void play(int type) {
         if (type == SILENCE) {
             // Nothing to do here. Just quit
             Log.v(TAG, "Stopping the music");
             return;
         }
         mPlayer = new MediaPlayer();
         // Keep the CPU awake while playing music.
         mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
         mPlayer.setOnPreparedListener(this);
         final int resourceToPlay;
         final int nextPosition;
         if (type == WHITE_NOISE) {
             Log.v(TAG, "Playing browninan noise");
             resourceToPlay = R.raw.noise;
             nextPosition = INVALID_POSITION;
         } else {
             // Try to open the SD card and read from there. If nothing is found, play the
             // default music.
             nextPosition = nextTrackFromCard();
             if (nextPosition == INVALID_POSITION) {
                 Log.v(TAG, "No SD card music found, playing default music");
                 resourceToPlay = R.raw.all_of_me;
             } else {
                 resourceToPlay = INVALID_POSITION;
             }
         }
         try {
             if (resourceToPlay == INVALID_POSITION) {
                 // Play files, not resources. Play the music file given here.
                 final String file = mBabyDir.getAbsolutePath() + File.separator + mFilenames[nextPosition];
                 Log.d(TAG, "Now playing " + file);
                 mPlayer.setDataSource(file);
                 mPlayer.setOnCompletionListener(this);
                 // Play this song, and a different one.
                 mPlayer.setLooping(false);
             } else {
                 final AssetFileDescriptor d = getResources().openRawResourceFd(resourceToPlay);
                 if (d == null) {
                     Log.wtf(TAG, "Could not open the file to play");
                     return;
                 }
                 final FileDescriptor fd = d.getFileDescriptor();
                 mPlayer.setDataSource(fd, d.getStartOffset(), d.getLength());
                 d.close();
                 // White noise or the default song is looped forever.
                 mPlayer.setLooping(true);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         mPlayer.prepareAsync();
     }
 
     /**
      * Returns the position of the next track to play. Returns -1 if nothing could be
      * played.
      */
     private int nextTrackFromCard() {
         if (mFilenames == null || mFilenames.length <= 0) {
             // Fill the filename list and return the first position.
             mFilenames = getMusicList();
             Log.d(TAG, "All filenames: " + Arrays.toString(mFilenames));
             // Still nothing? Go back with an invalid position.
             if (mFilenames.length <= 0) {
                 Log.e(TAG, "Baby music has no files.");
                 return INVALID_POSITION;
             }
         }
         return mRandom.nextInt(mFilenames.length);
     }
 
     /**
      * Returns the names of all the music files available to the user.
      * @return list of all the files in the baby music directory.
      */
     private String[] getMusicList() {
         if (mBabyDir == null) {
             mBabyDir = getBabyDir();
         }
         // Still nothing? We don't have a valid baby music directory.
         if (mBabyDir == null) {
             return new String[0];
         }
         final String[] noFiles = new String[0];
         final String[] filenames = mBabyDir.list();
         Log.e(TAG, "All filenames: " + Arrays.toString(filenames));
         if (filenames.length <= 0) {
             Log.e(TAG, "Baby music has no files." + mBabyDir);
             return noFiles;
         }
         return filenames;
     }
 
     /**
      * Returns the location of the baby music directory which is
      * sdcard/music/babysong.
      * @return the file representing the baby music directory.
      */
     private static File getBabyDir() {
         final String state = Environment.getExternalStorageState();
         if (!Environment.MEDIA_MOUNTED.equals(state)) {
             // If we don't have an SD card, cannot do anything here.
             Log.e(TAG, "SD card root directory is not available");
             return null;
         }
         final File rootSdLocation;
         if (SDK >= 8) {
             rootSdLocation = getBabyDirAfterV8();
         } else {
             rootSdLocation = getBabyDirTillV7();
         }
         if (rootSdLocation == null) {
             // Not a directory? Completely unexpected.
             Log.e(TAG, "SD card root directory is NOT a directory: " + rootSdLocation);
             return null;
         }
         // Navigate over to the baby music directory.
         final File babyMusicDir = new File(rootSdLocation, BABY_MUSIC_DIR);
         if (!babyMusicDir.isDirectory()) {
             Log.e(TAG, "Baby music directory does not exist." + rootSdLocation);
             return null;
         }
         return babyMusicDir;
     }
 
     /**
      * sdcard/music in SDK >= 8
      * @return the sdcard/music path in sdk version >= 8
      */
     private static File getBabyDirAfterV8() {
         return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
     }
 
     /**
      * sdcard/music in SDK < 8
      * @return the sdcard/music path in sdk version < 8
      */
     private static File getBabyDirTillV7() {
         return new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_MUSIC);
     }
 
     /**
      * The idea here is to set the notification so that the service can always run. However, ths is not
      * happening correctly right now.
      */
     private void setForegroundService() {
         final Intent startBabyActivity = new Intent(this, BabyActivity.class);
         final PendingIntent pending = PendingIntent.getActivity(this, 0, startBabyActivity, PendingIntent.FLAG_UPDATE_CURRENT);
         if (SDK >= 11) {
        final Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Playing music")
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true);
        builder.setContentIntent(pending);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
         } else {
             final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                     .setContentTitle("Playing music")
                     .setSmallIcon(R.drawable.ic_launcher)
                     .setOngoing(true);
             builder.setContentIntent(pending);
             mNotificationManager.notify(NOTIFICATION_ID, builder.build());
         }
     }
 
     /**
      * Close the music player, if any, and remove our reference to it.
      */
     private void releasePlayer() {
         if (mPlayer != null) {
             mPlayer.stop();
             mPlayer.release();
             mPlayer = null;
         }
     }
 
     /**
      * Remove the persistent notification.
      */
     private void removeNotification() {
         // Get rid of our notification
         mNotificationManager.cancel(NOTIFICATION_ID);
     }
 
     @Override
     public void onDestroy() {
         Log.v(TAG, "bye bye");
         removeNotification();
         releasePlayer();
         super.onDestroy();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onPrepared(MediaPlayer mp) {
         setForegroundService();
         mPlayer.start();
     }
 
     @Override
     public void onCompletion(MediaPlayer mp) {
         // This method is only called for songs, since white noise is on endless loop, and will never get this event.
         releasePlayer();
         // Play the next song.
         play(mTypePlaying);
     }
 }
