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
 
 package com.eggwall.SoundSleep;
 
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
 
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Random;
 
 /**
  * Runs the music in the background and holds a wake lock during the duration of music playing.
  */
 public class AudioService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
         MediaPlayer.OnCompletionListener {
     /** The namespace of our application, used for qualifying local messages. */
     private static String NAMESPACE = "com.eggwall.SoundSleep";
 
     /** For logging */
     private static final String TAG = "AudioService";
     /** The tag used to pass the request. Can only be {@link #GET_STATUS}, {@link #MUSIC}, or {@link #WHITE_NOISE}*/
     public static final String REQUEST = "request";
 
     /** Stop playing any audio. */
     public static final int SILENCE = 0;
     /** Broadcast message that says were were successful in starting silence. */
     public static final String MESSAGE_SILENCE = NAMESPACE + ".message.silence";
 
     /** Play music from the SD card */
     public static final int MUSIC = 1;
     /** Broadcast message that says were were successful in starting music. */
     public static final String MESSAGE_MUSIC = NAMESPACE + ".message.music";
 
     /** Play standard white noise file (included in the application */
     public static final int WHITE_NOISE = 2;
     /** Broadcast message that says were were successful in starting white noise. */
     public static final String MESSAGE_WHITE_NOISE = NAMESPACE + ".message.white-noise";
 
     /** Just return the current status without changing any state. */
     public static final int GET_STATUS = 3;
 
     /** Map to perform type -> message lookups */
     public static final String[] typeToMessage = {
             MESSAGE_SILENCE, MESSAGE_MUSIC, MESSAGE_WHITE_NOISE
     };
     /** Map to perform message -> type lookups. */
     public static final HashMap <String, Integer> messageToType = new HashMap<String, Integer>(3);
     static {
         messageToType.put(MESSAGE_SILENCE, SILENCE);
         messageToType.put(MESSAGE_MUSIC, MUSIC);
         messageToType.put(MESSAGE_WHITE_NOISE, WHITE_NOISE);
     }
 
     /** This represents in invalid position in the list and also an invalid resource. */
     private static final int INVALID_POSITION = -1;
     /** Name of the directory in the main folder containing sleeping music */
     private final static String MUSIC_DIR = "sleeping";
 
     /** The ID for the global notification we post. */
     private final static int NOTIFICATION_ID = 0;
 
     /** Single instance of random number generator */
     private final Random mRandom = new Random();
     /** The SDK version */
     private final static int SDK = Build.VERSION.SDK_INT;
 
     /** The object that actually plays the music on our behalf. */
     private MediaPlayer mPlayer;
     /** Set to {@link #SILENCE}, {@link #MUSIC}, or {@link #WHITE_NOISE}. */
     private int mTypePlaying = SILENCE;
     /** The actual directory that corresponds to the external SD card. */
     private File mMusicDir;
     /** Names of all the songs */
     private String[] mFilenames;
     /** The global manager for notifications */
     private NotificationManager mNotificationManager;
 
     @Override
     public boolean onError(MediaPlayer mp, int what, int extra) {
         releasePlayer();
         Log.e(TAG, "SleepActivity.AudioService encountered onError");
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
         // If this is a call to get the status, just return right here.
         if (typeOfResource == GET_STATUS) {
             postSuccessMessage(mTypePlaying);
             return 0;
         }
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
      * Posts a message back to the activity that it was successful in either playing music, in playing white
      * noise, or becoming silent.
      */
     private void postSuccessMessage(int actionSuccessful) {
         final Intent i = new Intent();
         i.setAction(typeToMessage[actionSuccessful]);
         final LocalBroadcastManager m = LocalBroadcastManager.getInstance(this);
         m.sendBroadcast(i);
     }
 
     /**
      * Start playing the resource specified here.
      *
      * @param type Either MUSIC, or WHITE_NOISE. Passing the same ID twice
      *             is a signal to stop playing music altogether.
      */
     private void play(int type) {
         if (type == MUSIC) {
             final MediaPlayer player = tryStartingMusic();
             if (player != null) {
                 mPlayer = player;
                 mPlayer.prepareAsync();
                 // onPrepared will get called when the media player is ready to play.
                 return;
             }
         }
         // Either we weren't able to play custom music, or we were asked to play white noise.
         final int resourceToPlay;
         if (type == WHITE_NOISE) {
             Log.v(TAG, "Playing browninan noise.");
             resourceToPlay = R.raw.noise;
         } else {
             Log.v(TAG, "Playing all of me.");
             resourceToPlay = R.raw.jingle;
         }
         try {
             final AssetFileDescriptor d = getResources().openRawResourceFd(resourceToPlay);
             if (d == null) {
                 Log.wtf(TAG, "Could not open the file to play");
                 return;
             }
             final FileDescriptor fd = d.getFileDescriptor();
             mPlayer = getGenericMediaPlayer();
             mPlayer.setDataSource(fd, d.getStartOffset(), d.getLength());
             d.close();
             // White noise or the default song is looped forever.
             mPlayer.setLooping(true);
         } catch (IOException e) {
             Log.e(TAG, "Could not create a media player instance. Full error below.");
             e.printStackTrace();
             return;
         }
         postSuccessMessage(mTypePlaying);
         mPlayer.prepareAsync();
     }
 
     /**
      * Create a media player with the standard configuration both for white noise and music.
      * @return a generic Media player suitable for this application.
      */
     private MediaPlayer getGenericMediaPlayer() {
         final MediaPlayer player = new MediaPlayer();
         // Keep the CPU awake while playing music.
         player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
         player.setOnPreparedListener(this);
         return player;
     }
 
     /**
      * Try getting music from the [sdcard]/music/sleeping
      * @return a valid media player if we can play from it. Returns null if we didn't find music, or we can't play
      * custom music for any reason.
      */
     private MediaPlayer tryStartingMusic() {
         final MediaPlayer player = getGenericMediaPlayer();
         // Try to open the SD card and read from there. If nothing is found, play the
         // default music.
         final int nextPosition = nextTrackFromCard();
         if (nextPosition == INVALID_POSITION) {
             return null;
         }
         // Play files, not resources. Play the music file given here.
         final String file = mMusicDir.getAbsolutePath() + File.separator + mFilenames[nextPosition];
         Log.d(TAG, "Now playing " + file);
         try {
             player.setDataSource(file);
         } catch (IOException e) {
             Log.e(TAG, "Could not create a media player instance. Full error below.");
             e.printStackTrace();
             return null;
         }
         player.setOnCompletionListener(this);
         // Play this song, and a different one when done.
         player.setLooping(false);
         return player;
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
                 Log.e(TAG, "Music directory has no files.");
                 return INVALID_POSITION;
             }
         }
         return mRandom.nextInt(mFilenames.length);
     }
 
     /**
      * Returns the names of all the music files available to the user.
      * @return list of all the files in the music directory.
      */
     private String[] getMusicList() {
         if (mMusicDir == null) {
             mMusicDir = getMusicDir();
         }
         // What we return when we don't find anything. It is safer to return a zero length array than null.
         final String[] foundNothing = new String[0];
         // Still nothing? We don't have a valid music directory.
         if (mMusicDir == null) {
             return foundNothing;
         }
         final String[] filenames = mMusicDir.list();
         Log.e(TAG, "All filenames: " + Arrays.toString(filenames));
         if (filenames.length <= 0) {
             Log.e(TAG, "Music directory has no files." + mMusicDir);
             return foundNothing;
         }
         return filenames;
     }
 
     /**
      * Returns the location of the music directory which is
      * [sdcard]/music/sleeping.
      * @return the file representing the music directory.
      */
     private static File getMusicDir() {
         final String state = Environment.getExternalStorageState();
         if (!Environment.MEDIA_MOUNTED.equals(state)) {
             // If we don't have an SD card, cannot do anything here.
             Log.e(TAG, "SD card root directory is not available");
             return null;
         }
         final File rootSdLocation;
         if (SDK >= 8) {
             rootSdLocation = getMusicDirAfterV8();
         } else {
             rootSdLocation = getMusicDirTillV7();
         }
         if (rootSdLocation == null) {
             // Not a directory? Completely unexpected.
             Log.e(TAG, "SD card root directory is NOT a directory: " + rootSdLocation);
             return null;
         }
         // Navigate over to the music directory.
         final File musicDir = new File(rootSdLocation, MUSIC_DIR);
         if (!musicDir.isDirectory()) {
             Log.e(TAG, "Music directory does not exist." + rootSdLocation);
             return null;
         }
         return musicDir;
     }
 
     /**
      * [sdcard]/music in SDK >= 8
      * @return the [sdcard]/music path in sdk version >= 8
      */
     private static File getMusicDirAfterV8() {
         return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
     }
 
     /**
      * [sdcard]/music in SDK < 8
      * @return the [sdcard]/music path in sdk version < 8
      */
     private static File getMusicDirTillV7() {
         return new File(Environment.getExternalStorageDirectory(), "music");
     }
 
     /**
      * The idea here is to set the notification so that the service can always run. However, ths is not
      * happening correctly right now.
      */
     private void setForegroundService() {
         final Intent showClock = new Intent(this, SleepActivity.class);
         final PendingIntent pending = PendingIntent.getActivity(this, 0, showClock, PendingIntent.FLAG_UPDATE_CURRENT);
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
         // Indicate that the service is quitting.
         postSuccessMessage(SILENCE);
         super.onDestroy();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onPrepared(MediaPlayer mp) {
         setForegroundService();
         postSuccessMessage(mTypePlaying);
         mPlayer.start();
     }
 
     @Override
     public void onCompletion(MediaPlayer mp) {
         // This method is only called for songs, since white noise is on endless loop, and will never get this event.
         releasePlayer();
         // Play the next song.  Should only be called for mTypePlaying == MUSIC
         if (mTypePlaying == MUSIC) {
             play(mTypePlaying);
         }
     }
 }
