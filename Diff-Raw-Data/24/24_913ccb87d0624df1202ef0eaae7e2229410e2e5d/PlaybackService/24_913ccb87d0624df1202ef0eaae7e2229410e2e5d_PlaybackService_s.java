 /**
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  */
 
 package com.yohpapa.research.simplemusicplayer;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.provider.MediaStore;
 import android.support.v4.app.NotificationCompat;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.yohpapa.research.simplemusicplayer.plugins.events.PauseEvent;
 import com.yohpapa.research.simplemusicplayer.plugins.events.PlayEvent;
 import com.yohpapa.research.simplemusicplayer.plugins.events.PlayPauseEvent;
 import com.yohpapa.research.simplemusicplayer.plugins.events.PlayStateChangedEvent;
 import com.yohpapa.research.simplemusicplayer.plugins.events.PrepareEvent;
 import com.yohpapa.research.simplemusicplayer.plugins.events.TrackChangedEvent;
 import com.yohpapa.research.simplemusicplayer.plugins.tools.CursorHelper;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.greenrobot.event.EventBus;
 
 public class PlaybackService extends Service
         implements MediaPlayer.OnCompletionListener,
                    MediaPlayer.OnPreparedListener,
                    MediaPlayer.OnErrorListener,
                    MediaPlayer.OnSeekCompleteListener {
 
     private static final String TAG = PlaybackService.class.getSimpleName();
     private static final String URI_BASE = PlaybackService.class.getName() + ".";
     private static final String ACTION_PAUSE = URI_BASE + "ACTION_PAUSE";
     private static final String ACTION_PLAY = URI_BASE + "ACTION_PLAY";
     private static final String ACTION_STOP = URI_BASE + "ACTION_STOP";
 
     public static final String ACTION_SELECT = URI_BASE + "ACTION_SELECT";
     public static final String PRM_START_INDEX = URI_BASE + "PRM_START_INDEX";
     public static final String PRM_TRACK_LIST = URI_BASE + "PRM_TRACK_LIST";
 
     private static final float DUCKING_VOLUME_LEVEL = 0.3f;
 
     private EventBus eventBus = null;
 
     private long[] trackIds = null;
     private int currentIndex = -1;
 
     private int positionToRestore = -1;
 
     private static final int STATE_IDLE = 0;
     private static final int STATE_PREPARING = 1;
     private static final int STATE_PREPARED = 2;
     private static final int STATE_SEEKING = 3;
     private int state = STATE_IDLE;
 
     private MediaPlayer player = null;
     private List<Runnable> postProcesses = new ArrayList<Runnable>();
 
     private class CurrentTrackInfo {
         private String title = null;
         private String artistName = null;
         private String albumName = null;
         private Bitmap artwork = null;
 
         public void setTitle(String title) {
             this.title = title;
         }
 
         public String getTitle() {
             return title;
         }
 
         public void setArtistName(String artistName) {
             this.artistName = artistName;
         }
 
         public String getArtistName() {
             return artistName;
         }
 
         public void setAlbumName(String albumName) {
             this.albumName = albumName;
         }
 
         public String getAlbumName() {
             return albumName;
         }
 
         public void setArtwork(Bitmap artwork) {
             if(this.artwork != null) {
                 if(this.artwork != artwork && !this.artwork.isRecycled()) {
                     this.artwork.recycle();
                 }
             }
 
             this.artwork = artwork;
         }
 
         public Bitmap getArtwork() {
             return this.artwork;
         }
     }
     private CurrentTrackInfo currentTrackInfo = new CurrentTrackInfo();
 
     // --------------------------------------------------------------------------------------------
     // Service lifecycle event methods block
     // --------------------------------------------------------------------------------------------
 
     @Override
     public void onCreate() {
         Log.d(TAG, "onCreate");
         super.onCreate();
 
         player = initializePlayer();
         eventBus = EventBus.getDefault();
         eventBus.registerSticky(this);
     }
 
     @Override
     public void onDestroy() {
         Log.d(TAG, "onDestroy");
         super.onDestroy();
 
         eventBus.unregister(this);
 
         finalizePlayer();
 
         NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         manager.cancel(R.id.notification_id);
 
         abandonAudioFocus();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         Log.d(TAG, "onBind");
         return null;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.d(TAG, "onStartCommand");
 
         if(intent == null) {
             Log.d(TAG, "The intent is null.");
             return START_REDELIVER_INTENT;
         }
 
         String action = intent.getAction();
 
         if(ACTION_PAUSE.equals(action)) {
             pauseTrack();
         } else if(ACTION_PLAY.equals(action)) {
             playTrack();
         } else if(ACTION_STOP.equals(action)) {
             stopTrack();
         } else if(ACTION_SELECT.equals(action)) {
             int startIndex = intent.getIntExtra(PRM_START_INDEX, 0);
             long[] trackList = intent.getLongArrayExtra(PRM_TRACK_LIST);
             eventBus.post(new PrepareEvent(trackList, startIndex));
         }
 
         return START_REDELIVER_INTENT;
     }
 
     private boolean isStarted() {
         return player != null;
     }
 
     private MediaPlayer initializePlayer() {
         MediaPlayer player = new MediaPlayer();
         player.setAudioStreamType(AudioManager.STREAM_MUSIC);
         player.setOnPreparedListener(this);
         player.setOnCompletionListener(this);
         player.setOnErrorListener(this);
         player.setOnSeekCompleteListener(this);
         player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
 
         IntentFilter filter = new IntentFilter();
         filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
         registerReceiver(noisyEventReceiver, filter);
 
         return player;
     }
 
     private void finalizePlayer() {
         if(player != null) {
             player.pause();
             positionToRestore = player.getCurrentPosition();
             player.release();
             player = null;
 
             unregisterReceiver(noisyEventReceiver);
         }
     }
 
     // --------------------------------------------------------------------------------------------
     // Event handler for external and internal methods block
     // --------------------------------------------------------------------------------------------
 
     public void onEventMainThread(PrepareEvent event) {
         Log.d(TAG, "onEventMainThread: PrepareEvent");
 
         if(event == null) {
             Log.d(TAG, "The event is null.");
             return;
         }
 
         int newIndex = event.getStartIndex();
         long[] newTrackIds = event.getTrackIds();
 
         if(newTrackIds == null) {
             Log.d(TAG, "The new track ID list is null.");
             return;
         }
 
         if(newIndex >= newTrackIds.length) {
             Log.d(TAG, "The new index is over.");
             return;
         }
 
         if(trackIds != null) {
             long newTrackId = newTrackIds[newIndex];
             long nowTrackId = trackIds[currentIndex];
 
             if(nowTrackId == newTrackId) {
                 Log.d(TAG, "The track ID is not changed.");
                 trackIds = newTrackIds;
                 currentIndex = newIndex;
                 return;
             }
         }
 
         trackIds = newTrackIds;
         currentIndex = newIndex;
         positionToRestore = -1;
 
         prepareToPlay(trackIds[currentIndex]);
     }
 
     private void prepareToPlay(long trackId) {
         Log.d(TAG, "prepareToPlay trackId: " + trackId);
 
         try {
             player.reset();
             Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
             player.setDataSource(this, uri);
             player.prepareAsync();
 
             state = STATE_PREPARING;
 
         } catch (IOException e) {
             Log.e(TAG, e.toString());
         }
     }
 
     @Override
     public void onPrepared(MediaPlayer mp) {
         Log.d(TAG, "onPrepared");
 
         if(positionToRestore != -1) {
             player.seekTo(positionToRestore);
             state = STATE_SEEKING;
             return;
         }
 
         for(Runnable postProcess : postProcesses) {
             postProcess.run();
         }
         postProcesses.clear();
 
         state = STATE_PREPARED;
     }
 
     @Override
     public void onCompletion(MediaPlayer mp) {
         Log.d(TAG, "onCompletion");
 
         currentIndex = (currentIndex + 1) % trackIds.length;
         positionToRestore = -1;
         prepareToPlay(trackIds[currentIndex]);
 
         postProcesses.add(new Runnable() {
             @Override
             public void run() {
                 playTrack();
                 eventBus.post(new TrackChangedEvent(currentIndex));
             }
         });
     }
 
     @Override
     public void onSeekComplete(MediaPlayer mp) {
         Log.d(TAG, "onSeekComplete");
 
         for(Runnable postProcess : postProcesses) {
             postProcess.run();
         }
         postProcesses.clear();
 
         state = STATE_PREPARED;
     }
 
     @Override
     public boolean onError(MediaPlayer mp, int what, int extra) {
         Log.e(TAG, "onError what: " + what + ", extra" + extra);
         return false;
     }
 
     public void onEventMainThread(PlayEvent event) {
         Log.d(TAG, "onEventMainThread: PlayEvent");
 
         if(!isStarted()) {
             Log.d(TAG, "The service has not been started yet.");
             postProcesses.add(new Runnable() {
                 @Override
                 public void run() {
                     playTrack();
                 }
             });
             return;
         }
 
         if(state != STATE_PREPARED) {
             postProcesses.add(new Runnable() {
                 @Override
                 public void run() {
                     playTrack();
                 }
             });
             return;
         }
 
         playTrack();
     }
 
     public void onEventMainThread(PauseEvent event) {
         Log.d(TAG, "onEventMainThread: PauseEvent");
 
         if(!isStarted()) {
             Log.d(TAG, "The service has not been started yet.");
             postProcesses.add(new Runnable() {
                 @Override
                 public void run() {
                     pauseTrack();
                 }
             });
             return;
         }
 
         if(!player.isPlaying()) {
             Log.d(TAG, "The player is not playing now.");
             return;
         }
         pauseTrack();
     }
 
     public void onEventMainThread(PlayPauseEvent event) {
         Log.d(TAG, "onEventMainThread: PlayPauseEvent");
 
         if(!isStarted()) {
             Log.d(TAG, "The service has not been started yet.");
             postProcesses.add(new Runnable() {
                 @Override
                 public void run() {
                     if(player.isPlaying()) {
                         pauseTrack();
                     } else {
                         playTrack();
                     }
                 }
             });
             return;
         }
 
         if(player.isPlaying()) {
             pauseTrack();
         } else {
             if(state == STATE_PREPARED) {
                 playTrack();
             } else {
                 postProcesses.add(new Runnable() {
                     @Override
                     public void run() {
                         playTrack();
                     }
                 });
             }
         }
     }
 
     // --------------------------------------------------------------------------------------------
     // Private playback methods block
     // --------------------------------------------------------------------------------------------
     private void playTrack() {
         if(!player.isPlaying()) {
             boolean result = requestAudioFocus();
             if(!result) {
                 Log.d(TAG, "Requesting the audio focus has failed.");
                 return;
             }
 
             player.start();
            eventBus.post(new PlayStateChangedEvent(PlayStateChangedEvent.STATE_PLAYING, currentIndex));
 
             updateNotification();
         }
     }
 
     private void pauseTrack() {
         if(player.isPlaying()) {
             abandonAudioFocus();
 
             player.pause();
            eventBus.post(new PlayStateChangedEvent(PlayStateChangedEvent.STATE_PAUSED, currentIndex));
 
             showNotification(
                     NOTIFICATION_TYPE_PLAY,
                     currentTrackInfo.getTitle(),
                     currentTrackInfo.getArtistName(),
                     currentTrackInfo.getAlbumName(),
                     currentTrackInfo.getArtwork());
         }
     }
 
     private void stopTrack() {
         if(player.isPlaying()) {
             player.pause();
         }
 
        eventBus.post(new PlayStateChangedEvent(PlayStateChangedEvent.STATE_STOPPED, currentIndex));
         stopSelf();
     }
 
     // --------------------------------------------------------------------------------------------
     // Audio focus control block
     // --------------------------------------------------------------------------------------------
     private boolean requestAudioFocus() {
         AudioManager manager = (AudioManager)getSystemService(AUDIO_SERVICE);
         int result = manager.requestAudioFocus(
                                 onAudioFocusChangeListener,
                                 AudioManager.STREAM_MUSIC,
                                 AudioManager.AUDIOFOCUS_GAIN);
 
         return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
     }
 
     private void abandonAudioFocus() {
         AudioManager manager = (AudioManager)getSystemService(AUDIO_SERVICE);
         manager.abandonAudioFocus(onAudioFocusChangeListener);
     }
 
     private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
         @Override
         public void onAudioFocusChange(int focusChange) {
 
             switch(focusChange) {
                 case AudioManager.AUDIOFOCUS_GAIN:
                     if(player == null) {
                         player = initializePlayer();
                         player.setVolume(1.0f, 1.0f);
                         postProcesses.add(new Runnable() {
                             @Override
                             public void run() {
                                 playTrack();
                             }
                         });
                         prepareToPlay(trackIds[currentIndex]);
                     } else {
                         if(!player.isPlaying()) {
                             player.start();
                         }
                     }
                     break;
 
                 case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                     if(player != null) {
                         player.setVolume(1.0f, 1.0f);
                     }
                     break;
 
                 case AudioManager.AUDIOFOCUS_LOSS:
                     finalizePlayer();
                     break;
 
                 case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                     if(player != null) {
                         player.pause();
                     }
                     break;
 
                 case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                     if(player != null) {
                         player.setVolume(DUCKING_VOLUME_LEVEL, DUCKING_VOLUME_LEVEL);
                     }
                     break;
 
                 default:
                     Log.d(TAG, "Unknown focus change type: " + focusChange);
                     break;
             }
         }
     };
 
     // --------------------------------------------------------------------------------------------
     // Notification control block
     // --------------------------------------------------------------------------------------------
     private void updateNotification() {
         eventBus.post(new NotificationPrepareEvent(trackIds[currentIndex]));
     }
 
     public class NotificationPrepareEvent {
         private final long trackId;
         public NotificationPrepareEvent(long trackId) {
             this.trackId  = trackId;
         }
 
         public long getTrackId() {
             return trackId;
         }
     }
 
     public class NotificationPreparedEvent {
         private final long trackId;
         private final String title;
         private final String artist;
         private final String album;
         private final Bitmap artwork;
 
         public NotificationPreparedEvent(long trackId, String title, String artist, String album, Bitmap artwork) {
             this.trackId = trackId;
             this.title = title;
             this.artist = artist;
             this.album = album;
             this.artwork = artwork;
         }
 
         public long getTrackId() { return trackId; }
         public String getTitle() { return title; }
         public String getArtist() { return artist; }
         public String getAlbum() { return album; }
         public Bitmap getArtwork() { return artwork; }
     }
 
     public void onEventAsync(NotificationPrepareEvent event) {
         Log.d(TAG, "onEventAsync: NotificationPrepareEvent");
 
         Context context = getApplicationContext();
         ContentResolver resolver = context.getContentResolver();
         Resources resources = context.getResources();
 
         Cursor trackCursor = null;
         Cursor albumCursor = null;
         try {
             Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, event.getTrackId());
             if(uri == null) {
                 eventBus.post(new NotificationPreparedEvent(event.getTrackId(), null, null, null, null));
                 return;
             }
 
             trackCursor = resolver.query(
                                 uri,
                                 new String[] {
                                     MediaStore.Audio.Media.TITLE,
                                     MediaStore.Audio.Media.ARTIST,
                                     MediaStore.Audio.Media.ALBUM,
                                     MediaStore.Audio.Media.ALBUM_ID,
                                 }, null, null, null);
 
             if(trackCursor == null || !trackCursor.moveToFirst() || trackCursor.getCount() != 1) {
                 eventBus.post(new NotificationPreparedEvent(event.getTrackId(), null, null, null, null));
                 return;
             }
 
             String title = CursorHelper.getString(trackCursor, MediaStore.Audio.Media.TITLE);
             String artist = CursorHelper.getString(trackCursor, MediaStore.Audio.Media.ARTIST);
             String album = CursorHelper.getString(trackCursor, MediaStore.Audio.Media.ALBUM);
             long albumId = CursorHelper.getLong(trackCursor, MediaStore.Audio.Media.ALBUM_ID);
             Bitmap artwork = null;
 
             if(albumId != -1L) {
                 uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                 if(uri == null) {
                     eventBus.post(new NotificationPreparedEvent(event.getTrackId(), null, null, null, null));
                     return;
                 }
                 albumCursor = resolver.query(
                                     uri,
                                     new String[] {
                                         MediaStore.Audio.Albums.ALBUM_ART,
                                     }, null, null, null);
 
                 if(albumCursor == null || !albumCursor.moveToFirst() || albumCursor.getCount() != 1) {
                     eventBus.post(new NotificationPreparedEvent(event.getTrackId(), title, artist, album, null));
                     return;
                 }
 
                 String artworkPath = CursorHelper.getString(albumCursor, MediaStore.Audio.Albums.ALBUM_ART);
                 Bitmap buffer = BitmapFactory.decodeFile(artworkPath);
                 artwork = Bitmap.createScaledBitmap(
                                     buffer,
                                     resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width),
                                     resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height),
                                     false);
                 if(artwork != buffer) {
                     buffer.recycle();
                 }
             }
 
             eventBus.post(new NotificationPreparedEvent(event.getTrackId(), title, artist, album, artwork));
 
         } finally {
             if(trackCursor != null) {
                 trackCursor.close();
             }
             if(albumCursor != null) {
                 albumCursor.close();
             }
         }
     }
 
     public void onEventMainThread(NotificationPreparedEvent event) {
         Log.d(TAG, "onEventMainThread: NotificationPreparedEvent");
 
         String title = event.getTitle();
         String artist = event.getArtist();
         String album = event.getAlbum();
         Bitmap artwork = event.getArtwork();
 
         currentTrackInfo.setTitle(title);
         currentTrackInfo.setAlbumName(album);
         currentTrackInfo.setArtistName(artist);
         currentTrackInfo.setArtwork(artwork);
 
         showNotification(NOTIFICATION_TYPE_PAUSE, title, artist, album, artwork);
     }
 
     private static final int NOTIFICATION_TYPE_PAUSE = 0;
     private static final int NOTIFICATION_TYPE_PLAY = 1;
     private void showNotification(int type, String title, String artist, String album, Bitmap artwork) {
         Log.d(TAG, "showNotification type: " + type);
 
         NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
         builder.setWhen(System.currentTimeMillis());
         builder.setContentTitle(title);
 
         if(artwork != null) {
             builder.setLargeIcon(artwork);
         }
         builder.setSmallIcon(R.drawable.ic_launcher);
         builder.setTicker(title);
         builder.setPriority(NotificationCompat.PRIORITY_HIGH);
         builder.setContentText(artist);
         builder.setSubText(album);
 
         Intent actionIntent = new Intent(this, PlaybackService.class);
         String keyTop;
         int icon;
         if(type == NOTIFICATION_TYPE_PAUSE) {
             actionIntent.setAction(ACTION_PAUSE);
             keyTop = "PAUSE";
             icon = android.R.drawable.ic_media_pause;
         } else {
             actionIntent.setAction(ACTION_PLAY);
             keyTop = "PLAY";
             icon = android.R.drawable.ic_media_play;
         }
         PendingIntent actionPendingIntent = PendingIntent.getService(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         builder.addAction(icon, keyTop, actionPendingIntent);
 
         Intent stopIntent = new Intent(this, PlaybackService.class);
         stopIntent.setAction(ACTION_STOP);
         PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         builder.addAction(android.R.drawable.ic_delete, "STOP", stopPendingIntent);
 
         Intent intent = new Intent(this, SimpleMusicPlayer.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
         builder.setContentIntent(pendingIntent);
 
         startForeground(R.id.notification_id, builder.build());
     }
 
     // --------------------------------------------------------------------------------------------
     // Audio output hardware handling
     // --------------------------------------------------------------------------------------------
     private final BroadcastReceiver noisyEventReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if(intent == null)
                 return;
 
             String action = intent.getAction();
             if(TextUtils.isEmpty(action))
                 return;
 
             if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                 pauseTrack();
             }
         }
     };
 }
