 /*   
  * Copyright 2013 Karsten Patzwaldt
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.muckebox.android.services;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.muckebox.android.R;
 import org.muckebox.android.db.MuckeboxContract.AlbumEntry;
 import org.muckebox.android.db.MuckeboxContract.CacheEntry;
 import org.muckebox.android.db.MuckeboxContract.PlaylistEntry;
 import org.muckebox.android.db.MuckeboxContract.TrackEntry;
 import org.muckebox.android.db.MuckeboxProvider;
 import org.muckebox.android.db.PlaylistHelper;
 import org.muckebox.android.net.DownloadServer;
 import org.muckebox.android.ui.activity.BrowseActivity;
 import org.muckebox.android.utils.Preferences;
 import org.muckebox.android.utils.RemoteControlReceiver;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.AudioManager.OnAudioFocusChangeListener;
 import android.media.MediaMetadataRetriever;
 import android.media.MediaPlayer;
 import android.media.RemoteControlClient;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Parcelable;
 import android.os.PowerManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 public class PlayerService extends Service
     implements MediaPlayer.OnPreparedListener, 
     MediaPlayer.OnCompletionListener, 
     MediaPlayer.OnErrorListener, 
     MediaPlayer.OnInfoListener,
     DownloadListener {
 	private final static String LOG_TAG = "PlayerService";
 	
 	private final static int
 	    NOTIFICATION_ID = 23,
	    PREFETCH_INTERVAL = 10;
 	
 	public final static String EXTRA_PLAYLIST_ITEM_ID = "playlist_item_id";
 	
 	public final static String
 	    ACTION_LOAD = "load",
 	    ACTION_PREVIOUS = "previous",
 	    ACTION_PAUSE = "pause",
 	    ACTION_RESUME = "resume",
 	    ACTION_NEXT = "next";
 	
 	public class TrackInfo {
 	    public int trackId;
 	    public int playlistEntryId;
 	    
 	    public String album;
 	    public String artist;
 	    public String shortTitle;
 	    public String title;
 	    
 	    public int duration;
 	    public int trackNumber;
 	    
 	    public int position;
 	    
 	    public boolean isStreaming;
 	    
 	    public boolean hasPrevious;
 	    public boolean hasNext;
 	    
 	    public int nextTrackId;
 	    public boolean nextTrackRequested = false;
 	}
 	
 	private enum State {
 		STOPPED, 
 		PAUSED,
 		BUFFERING,
 		PLAYING
 	};
 	
 	private final IBinder mBinder = new PlayerBinder();
 	
 	private State mState = State.STOPPED;
 	private Set<PlayerListener> mListeners =
 	    new CopyOnWriteArraySet<PlayerListener>();
 	
 	private MediaPlayer mMediaPlayer;
 	
 	private DownloadService mDownloadService;
 	
 	private DownloadServer mServer;
 
 	private Handler mMainHandler;
 	
 	private HandlerThread mHelperThread;
 	private Handler mHelperHandler;
 	
 	private TrackInfo mTrackInfo;
 	
 	private Timer mTimer;
 	
 	private FileInputStream mCurrentFile = null;
 	
     private ComponentName mRemoteEventReceiver = null;
     private RemoteControlClient mRemoteControlClient = null;
     private AudioManager mAudioManager = null;
     
     private boolean mReceiverRegistered = false;
     private boolean mHasAudioFocus = false;
     
     NotificationManager mNotificationManager = null;
 	
 	private class ElapsedTimeTask extends TimerTask {
 	    private Runnable mNotifyTask = new Runnable() {
 	        public void run() {
 	            TrackInfo trackInfo = mTrackInfo;
 	            
 	            if (trackInfo != null) {
     	            for (PlayerListener l: mListeners) {
     	                if (l != null)
     	                    l.onPlayProgress(trackInfo.position);
     	            }
 	            }
 	        }
 	    };
 	    
         public void run() {
             if (mState != State.STOPPED)
             {
                 if (mTrackInfo != null) {
                     if (mMediaPlayer.isPlaying()) {
                         mTrackInfo.position = getCurrentPlayPosition();
                         
                         if (getCurrentTimeLeft() < PREFETCH_INTERVAL &&
                             mTrackInfo.hasNext &&
                             ! mTrackInfo.nextTrackRequested)
                         {
                             Log.d(LOG_TAG, "Prefetching next track");
                             requestNextTrack(mTrackInfo.nextTrackId);
                             mTrackInfo.nextTrackRequested = true;
                         }
                     }
                 }
 
                 mMainHandler.post(mNotifyTask);
             }
         }
 	}
 
     OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
         @Override
         public void onAudioFocusChange(int focusChange) {
             Log.d(LOG_TAG, "onAudioFocusChange");
             
             if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                 onFocusLoss();
             } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                 onFocusGained();
             } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                 Log.d(LOG_TAG, "audio focus lost, stopping");
                 onFocusLoss();
                 stop();
             }
         }
     };
     
     private void onFocusGained() {
         if (! mReceiverRegistered) {
             Log.d(LOG_TAG, "audio focus gained, registering remote");
             
             mAudioManager.registerMediaButtonEventReceiver(mRemoteEventReceiver);
             mAudioManager.registerRemoteControlClient(mRemoteControlClient);
             
             mReceiverRegistered = true;
         }
         
         if (mState == State.PAUSED)
             resume();
     }
     
     private void onFocusLoss() {
         Log.d(LOG_TAG, "audio focus lost transient, pausing");
         
         if (mState == State.PLAYING)
             pause();
         
         if (mReceiverRegistered) {
             mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
             mAudioManager.unregisterMediaButtonEventReceiver(mRemoteEventReceiver);
             
             mReceiverRegistered = false;
         }
     }
 	
 	@Override
 	public void onCreate() {
 		Log.d(LOG_TAG, "Service created.");
 		
 		bindService(new Intent(getBaseContext(), DownloadService.class),
             mConnection, Context.BIND_AUTO_CREATE);
 
         mMediaPlayer = new MediaPlayer();
         
         mMediaPlayer.setWakeMode(getApplicationContext(),
             PowerManager.PARTIAL_WAKE_LOCK);
         
         mMediaPlayer.setOnPreparedListener(this);
         mMediaPlayer.setOnCompletionListener(this);
         mMediaPlayer.setOnInfoListener(this);
         mMediaPlayer.setOnErrorListener(this);
         
         mMainHandler = new Handler(getMainLooper());
         
         mHelperThread = new HandlerThread("PlayerServiceHelper");
         mHelperThread.start();
         
         mHelperHandler = new Handler(mHelperThread.getLooper());
         
         String packageName = getPackageName();
         String className = RemoteControlReceiver.class.getName();
         
         Log.w(LOG_TAG, "package '" + packageName + "', class '" + className + "'");
         
         mRemoteEventReceiver = new ComponentName(packageName, className);
         mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
         mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 	}
 	
 	@Override
 	public void onDestroy() {
 	    if (isPlaying()) {
 	        Log.e(LOG_TAG, "Player service still playing when destroyed");
 	        stop();
 	    }
 	    
 	    mHelperThread.quit();
 	    
 	    if (mMediaPlayer != null)
 	        mMediaPlayer.release();
 
 	    getBaseContext().unbindService(mConnection);
 
 		Log.d(LOG_TAG, "Service destroyed.");
 	}
 	
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service)
         {
             DownloadService.DownloadBinder binder =
                     (DownloadService.DownloadBinder) service;
             mDownloadService = binder.getService();
 
             Log.d(LOG_TAG, "Bound to download service");
         }
 
         @Override
         public void onServiceDisconnected(ComponentName className) {
             mDownloadService = null;
             
             Log.d(LOG_TAG, "Unbound from download service");
         }
     };
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 	    if (intent != null)
 	    {
 	        int playlistItem = intent.getIntExtra(EXTRA_PLAYLIST_ITEM_ID, 0);
 	        
 	        Log.d(LOG_TAG, "Got play intent for item " + playlistItem);
 	        
     	    playTrack(playlistItem);
 	    }
         
 		return Service.START_STICKY;
 	}
 	
 	public static void playPlaylistItem(Context context, int playlistItemId) {
         Intent intent = new Intent(context, PlayerService.class);
         
         intent.putExtra(PlayerService.EXTRA_PLAYLIST_ITEM_ID, playlistItemId);
         
         context.startService(intent);
 	}
 	
 	private boolean getAudioFocus() {
 	    if (mHasAudioFocus)
 	        return mHasAudioFocus;
 	    
 	    Log.d(LOG_TAG, "Requesting audio focus");
 	    
 	    mHasAudioFocus = (mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
             AudioManager.STREAM_MUSIC,
             AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
 	    
 	    if (mHasAudioFocus)
 	        onFocusGained();
 	    
 	    return mHasAudioFocus;
 	}
 	
 	private void dropAudioFocus() {
 	    if (mHasAudioFocus) {
 	        Log.d(LOG_TAG, "Abandoning audio focus");
 
 	        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
 	        mHasAudioFocus = false;
 	          
             onFocusLoss();
 	    }
 	}
 	
 	protected void playTrack(final int playlistEntryId) {
 	    if (mState != State.STOPPED)
 	        stop();
 	    
 	    ensureRemoteControl();
 	        
         mState = State.BUFFERING;
         
         for (PlayerListener l: mListeners) {
             if (l != null)
                 l.onStartBuffering();
         }
  
         mHelperHandler.post(new Runnable() {
             public void run() {
                 int trackId = PlaylistHelper.getTrackId(getApplicationContext(),
                     Uri.withAppendedPath(MuckeboxProvider.URI_PLAYLIST_ENTRY, Integer.toString(playlistEntryId)));
                 boolean isStreaming = playTrackFromAnywhere(trackId);
                 
                 fetchTrackInfo(playlistEntryId, trackId, isStreaming);
             }
         });
 	}
 	
 	private boolean playTrackFromAnywhere(final int trackId) {
         Cursor c = getContentResolver().query(
             Uri.withAppendedPath(MuckeboxProvider.URI_CACHE_TRACK, Integer.toString(trackId)),
                 null, null, null, null);
         
         try {
             if (c.moveToFirst()) {
                 final String filename = c.getString(c.getColumnIndex(CacheEntry.ALIAS_FILENAME));
                 
                 mMainHandler.post(new Runnable() {
                     public void run() {
                         playTrackFromFile(filename, trackId);
                     }
                 });
                 
                 return false;
             } else {
                 mMainHandler.post(new Runnable() {
                     public void run() {
                         playTrackFromStream(trackId);
                     }
                 });
                 
                 return true;
             }
         } finally {
             c.close();
         }
 	}
 	
 	private void ensureRemoteControl() {
 	    if (mRemoteControlClient == null) {
             Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
             mediaButtonIntent.setComponent(mRemoteEventReceiver);
             
             PendingIntent remotePendingIntent = PendingIntent.getBroadcast(
                 getApplicationContext(), 0, mediaButtonIntent, 0);
             
             mRemoteControlClient = new RemoteControlClient(remotePendingIntent);
             mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
 	    }
 	}
 	
 	private void updateRemoteControl(TrackInfo trackInfo)
 	{
 	    if (mRemoteControlClient == null)
 	    {
 	        Log.e(LOG_TAG, "Remote control missing!");
 	        return;
 	    }
 	    
 	    mRemoteControlClient.setTransportControlFlags(
 	        (trackInfo.hasPrevious ? RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS : 0) +
 	        (trackInfo.hasNext ? RemoteControlClient.FLAG_KEY_MEDIA_NEXT : 0) +
 	        RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE +
 	        RemoteControlClient.FLAG_KEY_MEDIA_STOP);
 	        
 	    mRemoteControlClient.editMetadata(true).
 	        putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, trackInfo.artist).
 	        putString(MediaMetadataRetriever.METADATA_KEY_TITLE, trackInfo.title).
 	        putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, trackInfo.album).
 	        putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, trackInfo.duration * 1000).
 	        putLong(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER, trackInfo.trackNumber).
 	        apply();
 	}
 	
 	private void fetchTrackInfo(int playlistEntryId, int trackId, boolean isStreaming) {
         final Uri playlistEntryUri = Uri.withAppendedPath(
             MuckeboxProvider.URI_PLAYLIST_ENTRY, Integer.toString(playlistEntryId));
         
         final TrackInfo trackInfo = new TrackInfo();
         
         trackInfo.playlistEntryId = playlistEntryId;
         trackInfo.trackId = trackId;
         
         trackInfo.isStreaming = isStreaming;
         trackInfo.position = 0;
         trackInfo.hasNext = ! PlaylistHelper.isLast(getApplicationContext(), playlistEntryUri);
         trackInfo.hasPrevious = ! PlaylistHelper.isFirst(getApplicationContext(), playlistEntryUri);
         
         if (trackInfo.hasNext)
             trackInfo.nextTrackId = PlaylistHelper.getNextTrackId(
                 getApplicationContext(), playlistEntryUri);
   
         Cursor c = getContentResolver().query(Uri.withAppendedPath(
             MuckeboxProvider.URI_TRACKS_WITH_DETAILS, Integer.toString(trackId)), null, null, null, null);
         
         try {
             if (c.moveToFirst())
             {
                 trackInfo.album = c.getString(c.getColumnIndex(AlbumEntry.ALIAS_TITLE));
                 trackInfo.artist = c.getString(c.getColumnIndex(TrackEntry.ALIAS_DISPLAY_ARTIST));
                 trackInfo.shortTitle = c.getString(c.getColumnIndex(TrackEntry.ALIAS_TITLE));
                 trackInfo.title = trackInfo.artist + " - " + trackInfo.shortTitle;
                 trackInfo.duration = c.getInt(c.getColumnIndex(TrackEntry.ALIAS_LENGTH));
                 trackInfo.trackNumber = c.getInt(c.getColumnIndex(TrackEntry.ALIAS_TRACKNUMBER));
                 
                 Log.d(LOG_TAG, "Title is " + trackInfo.title);
             } else {
                 Log.e(LOG_TAG, "Could not fetch track info for " + trackId);
             }
 
             mMainHandler.post(new Runnable() {
                 public void run() {
                     if (mState != State.STOPPED) {
                         mTrackInfo = trackInfo;
                         
                         updateRemoteControl(trackInfo);
                         
                         for (PlayerListener l: mListeners)
                         {
                             if (l != null)
                             {
                                 l.onNewTrack(trackInfo);
                             }
                         }
                         
                         PlayerService.this.startAndNotify(trackInfo);
                         
                         setAsCurrent(playlistEntryUri);
                         
                         mTimer = new Timer();
                         mTimer.scheduleAtFixedRate(new ElapsedTimeTask(), 1000, 1000);
                     }
                 }
             });
         } finally {
             c.close();
         }
 	}
 	
 	private void setAsCurrent(final Uri playlistEntryUri) {
 	    mHelperHandler.post(new Runnable() {
 	        public void run() {
 	            Uri uri = (playlistEntryUri == null) ?
 	                Uri.withAppendedPath(MuckeboxProvider.URI_PLAYLIST,
 	                    Integer.toString(Preferences.getCurrentPlaylistId())) :
 	                playlistEntryUri;
 	            
         	    ContentValues values = new ContentValues();
         	    
         	    values.put(PlaylistEntry.SHORT_IS_CURRENT, (playlistEntryUri == null ? 0 : 1));
         	    
         	    getContentResolver().update(uri, values, null, null);
 	        }
 	    });
 	}
 	
 	private void clearCurrent() {
 	    setAsCurrent(null);
 	}
 	
 	private void updateNotification() {
 	    TrackInfo trackInfo = mTrackInfo;
 	    
 	    if (trackInfo != null)
 	        mNotificationManager.notify(NOTIFICATION_ID, getNotification(trackInfo));
 	    else
 	        mNotificationManager.cancel(NOTIFICATION_ID);
 	}
 	
 	private void startAndNotify(TrackInfo trackInfo) {
         startForeground(NOTIFICATION_ID, getNotification(trackInfo));
 	}
 	
 	private Notification getNotification(TrackInfo trackInfo) {
 	    Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
 
 	    Notification.Builder builder =
 	        new Notification.Builder(this).
 	        setLargeIcon(bm).
 	        setOngoing(true).
 	        setContentTitle(trackInfo.shortTitle).
 	        setContentText(trackInfo.artist).
 	        setTicker(trackInfo.title);
 	    
 	    if (trackInfo.hasPrevious) {
 	        builder.addAction(R.drawable.av_previous_dark,
 	            getResources().getText(R.string.previous),
 	            getPendingIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS));
 	    }
 	    
 	    if (mState == State.PLAYING) {
     	    builder.setSmallIcon(R.drawable.av_play_dark);
     	    builder.addAction(R.drawable.av_pause_dark,
     	        getResources().getText(R.string.pause),
     	        getPendingIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
 	    } else {
 	        builder.setSmallIcon(R.drawable.av_pause_dark);
 	        builder.addAction(R.drawable.av_play_dark,
 	            getResources().getText(R.string.resume),
 	            getPendingIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
 	    }
     	    
 	    if (trackInfo.hasNext) {
 	        builder.addAction(R.drawable.av_next_dark,
 	            getResources().getText(R.string.next),
 	            getPendingIntent(KeyEvent.KEYCODE_MEDIA_NEXT));
 	    }
 	    
 	    builder.setContentIntent(PendingIntent.getActivity(
 	        getApplicationContext(), 42, new Intent(this, BrowseActivity.class), 0));
 	    
 	    return builder.build();
 	}
 	
 	private PendingIntent getPendingIntent(int keyCode) {
         Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
         
         mediaButtonIntent.setComponent(mRemoteEventReceiver);
         mediaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT,
             (Parcelable) new KeyEvent(KeyEvent.ACTION_UP, keyCode));
         
         return PendingIntent.getBroadcast(
             getApplicationContext(), 23 + keyCode, mediaButtonIntent, 0);
 	}
 	
 	protected void playTrackFromFile(final String filename, final int trackId) {
 	    Log.d(LOG_TAG, "Playing from local file " + filename);
 	    
 	    try {
             mCurrentFile = openFileInput(filename);
             
             mMediaPlayer.reset();
             mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
             mMediaPlayer.setDataSource(mCurrentFile.getFD());
             mMediaPlayer.prepareAsync();
         } catch (IOException e) {
             Log.e(LOG_TAG, "Could not open file " + filename);
             
             try {
                 mCurrentFile.close();
                 mCurrentFile = null;
                 
                 DownloadService.discardTrack(this, trackId);
                 
                 Toast.makeText(this, getText(
                     R.string.error_local_playback), Toast.LENGTH_LONG).show();
             } catch (IOException e1) {
                 // srsly?
             }
             
             next();
         }
 	}
 	
 	protected void requestNextTrack(final int trackId) {
         if (mDownloadService == null) {
             Log.d(LOG_TAG, "Download service not bound yet, waiting...");
             
             mMainHandler.postDelayed(new Runnable() {
                 public void run() {
                    playTrackFromStream(trackId);
                 }
             }, 100);
         } else {
             mDownloadService.startDownload(trackId, false, false);
         }
 	}
 	
 	protected void playTrackFromStream(final int trackId) {
         if (mDownloadService == null) {
             Log.d(LOG_TAG, "Download service not bound yet, waiting...");
             
             mMainHandler.postDelayed(new Runnable() {
                 public void run() {
                     playTrackFromStream(trackId);
                 }
             }, 100);
         } else {
             Log.d(LOG_TAG, "Start playing streamed track " + trackId);
             
             mDownloadService.registerListener(this, trackId);
             mDownloadService.startDownload(trackId, false, true);
         }
 	}
 	
 	public void stop() {
 	    stop(false);
 	}
 	
 	public void stop(boolean keepFocus) {
 	    mState = State.STOPPED;
 
 	    mMediaPlayer.reset();
 	    
 	    clearCurrent();
 
 	    for (PlayerListener l: mListeners)
 	        if (l != null)
 	            l.onStopPlaying();
 
 	    if (mCurrentFile != null) {
 	        try {
 	            mCurrentFile.close();
 	        } catch (IOException e) {
 	            // WTF?
 	            e.printStackTrace();
 	        }
 	    }
 
 	    mCurrentFile = null;
 
 	    if (mServer != null)
 	        mServer.quit();
 
 	    mServer = null;
 
 	    mDownloadService.removeListener(this);
 
 	    if (mTimer != null) {
 	        mTimer.cancel();
 	        mTimer = null;
 	    }
 
 	    mTrackInfo = null;
 	    
 	    if (! keepFocus)
 	        dropAudioFocus();
 	    
         stopForeground(true);
         
 	    Log.d(LOG_TAG, "Stopped playing");
  	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 	
 	public class PlayerBinder extends Binder {
 		public PlayerService getService() {
 			return PlayerService.this;
 		}
 	}
 	
 	// public interface
 	
 	public void registerListener(PlayerListener listener)
 	{
 		mListeners.add(listener);
 		
 		listener.onConnected();
 	}
 	
 	public void removeListener(PlayerListener listener) {
 	    mListeners.remove(listener);
 	}
 
 	public void resume() {
 		if (mState == State.PAUSED)
 		{
 			mState = State.PLAYING;
 			
 			mMediaPlayer.start();
 			
 			for (PlayerListener l: mListeners)
 				if (l != null) l.onPlayResumed();
 			
 			onResumed();
 		}
 	}
 	
 	public void pause() {
 		if (mState == State.PLAYING)
 		{
 			mState = State.PAUSED;
 			
 			mMediaPlayer.pause();
 			
 			for (PlayerListener l: mListeners)
 				if (l != null) l.onPlayPaused();
 			
 			onPaused();
 		}
 	}
 	
 	private void onPaused() {
         if (mRemoteControlClient != null)
             mRemoteControlClient.setPlaybackState(
                 RemoteControlClient.PLAYSTATE_PAUSED);
         
         updateNotification();
         
         Log.d(LOG_TAG, "Paused");
 	}
 	
 	private void onResumed() {
         if (mRemoteControlClient != null)
             mRemoteControlClient.setPlaybackState(
                 RemoteControlClient.PLAYSTATE_PLAYING);
         
         updateNotification();
         
         Log.d(LOG_TAG, "Resuming");
 	}
 	
 	public void previous() {
 	    prevNext(false);
 	}
 	
 	public void next() {
 	    prevNext(true);
 	}
 	
 	public void prevNext(final boolean isNext) {
 	    if (mState == State.PLAYING || mState == State.PAUSED) {
 	        mHelperHandler.post(new Runnable() {
 	            public void run() {
 	                Cursor c = getContentResolver().query(
 	                    Uri.withAppendedPath(
 	                        (isNext ? MuckeboxProvider.URI_PLAYLIST_AFTER : MuckeboxProvider.URI_PLAYLIST_BEFORE),
 	                        Integer.toString(mTrackInfo.playlistEntryId)),
 	                        null, null, null, null);
 
 	                try {
 	                    if (isNext ? c.moveToFirst() : c.moveToLast()) {
 	                        final int nextPlaylistEntryId = c.getInt(c.getColumnIndex(PlaylistEntry.SHORT_ID));
 	                        
 	                        mMainHandler.post(new Runnable() {
 	                            public void run() {
 	                                stop(true);
 	                                playTrack(nextPlaylistEntryId);
 	                            }
 	                        });
 	                    }
 	                } finally {
 	                    c.close();
 	                }
 
 	            }
 	        });
 	    }
 
 	    Log.d(LOG_TAG, "Skip to " + (isNext ? "next" : "previous") + " track");
 	}
 	
 	public void seek(int targetSeconds) {
 	    if (mState == State.PLAYING || mState == State.PAUSED) {
 	        mMediaPlayer.seekTo(targetSeconds * 1000);
 	    }
 	}
 
 	public boolean isPlaying() {
 		return mState == State.PLAYING;
 	}
 	
 	public boolean isStopped() {
 	    return mState == State.STOPPED;
 	}
 	
 	public boolean isPaused() {
 	    return mState == State.PAUSED;
 	}
 	
 	public boolean isBuffering() {
 	    return mState == State.BUFFERING;
 	}
 	
 	public Integer getCurrentTrackLength() {
 	    if (mTrackInfo != null)
 	        return mTrackInfo.duration;
 	    
 	    return null;
 	}
 	
 	public Integer getCurrentPlayPosition() {
 	    if (mState == State.PLAYING || mState == State.PAUSED)
 	        return mMediaPlayer.getCurrentPosition() / 1000;
 	    
 	    return 0;
 	}
 	
 	public Integer getCurrentTimeLeft() {
 	    int ret = getCurrentTrackLength() - getCurrentPlayPosition();
 	    
 	    if (ret < 0) {
 	        Log.e(LOG_TAG, "Current time remaining < 0");
 	        ret = 0;
 	    }
 	    
 	    return ret;
 	}
 	
 	public String getCurrentTrackTitle() {
 	    if (mTrackInfo != null)
 	        return mTrackInfo.title;
 	    
 	    return null;
 	}
 	
 	public TrackInfo getCurrentTrackInfo() {
 	    return mTrackInfo;
 	}
 
     @Override
     public void onPrepared(MediaPlayer mp) {
         if (getAudioFocus()) {
             mp.start();
             
             for (PlayerListener l: mListeners)
                 l.onStartPlaying();
             
             if (mRemoteControlClient != null)
                 mRemoteControlClient.setPlaybackState(
                     RemoteControlClient.PLAYSTATE_PLAYING);
             
             mState = State.PLAYING;
             
             
             updateNotification();
         } else {
             Toast.makeText(getApplicationContext(),
                 getText(R.string.error_audiofocus), Toast.LENGTH_LONG).show();
             
             stop();
         }
     }
 
     @Override
     public boolean onInfo(MediaPlayer mp, int what, int extra) {
         switch (what) {
         case MediaPlayer.MEDIA_INFO_BUFFERING_START:
             for (PlayerListener l: mListeners) {
                 if (l != null)
                     l.onStartBuffering();
             }
             
             onPaused();
             
             return true;
             
         case MediaPlayer.MEDIA_INFO_BUFFERING_END:
             for (PlayerListener l: mListeners) {
                 if (l != null)
                     l.onPlayResumed();
             }
             
             onResumed();
             
             return true;
         }
         
         return false;
     }
 
     @Override
     public boolean onError(MediaPlayer mp, int what, int extra) {
 
         
         String whatStr, extraStr;
         
         switch (what) {
         case MediaPlayer.MEDIA_ERROR_UNKNOWN:
             whatStr = "Unknown error";
             break;
         case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
             whatStr = "Server died";
             break;
         default:
             whatStr = "Unknown error (" + what + ")";
             break;
         }
         
         switch (extra) {
         case MediaPlayer.MEDIA_ERROR_IO:
             extraStr = "IO Error";
             break;
         case MediaPlayer.MEDIA_ERROR_MALFORMED:
             extraStr = "Malformed media";
             break;
         case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
             extraStr = "Unsupported format";
             break;
         case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
             extraStr = "Timed out";
             break;
         default:
             extraStr = "Unknown extra (" + extra + ")";
             break;
         }
         
         Toast.makeText(this,
             String.format((String) getText(R.string.error_playback) +
                 "(" + whatStr + ", " + extraStr + ")", what, extra),
             Toast.LENGTH_SHORT).show();
         
         Log.e(LOG_TAG, "Error " + what + " (" + whatStr +
             "), extra " + extra + " (" + extraStr + ")");
         
         stop();
         
         return false;
     }
 
     @Override
     public void onCompletion(MediaPlayer mp) {
         if (mTrackInfo != null)
         {
             if (mTrackInfo.hasNext)
                 next();
             else
                 stop();
         } else {
             stop();
         }
     }
 
     @Override
     public void onDownloadStarted(long trackId, String mimeType) {
         Log.d(LOG_TAG, "Download started for " + trackId + " (" + mimeType + ")");
 
         assert(mServer == null);
         
         if (mServer != null) {
             Log.w(LOG_TAG, "Got download started message while server running, killing old server");
             mServer.quit();
             
             try {
                 mServer.join();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
         
         mServer = new DownloadServer(mimeType);
         mServer.start();
         
         startStreamMediaPlayer();
     }
     
     private void startStreamMediaPlayer() {
         if (mServer == null) {
             Log.e(LOG_TAG, "HTTP server missing");
             stop();
             return;
         }
         
         if (! mServer.isReady()) {
             Log.d(LOG_TAG, "HTTP server not ready, retrying");
             
             if (! mServer.isAlive())
             {
                 Log.e(LOG_TAG, "HTTP server died! Cannot play.");
                 stop();
             } else {
                 mMainHandler.postDelayed(new Runnable() {
                     public void run() {
                         startStreamMediaPlayer();
                     }
                 }, 50);
             }
         } else {
             try
             {
                 Log.d(LOG_TAG, "Start playing!");
                 
                 mMediaPlayer.reset();
                 
                 mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                 mMediaPlayer.setDataSource(DownloadServer.getUrl());
                 mMediaPlayer.prepareAsync();
             } catch (IOException e) {
                 stop();
             }   
         }
     }
 
     @Override
     public void onDataReceived(long trackId, ByteBuffer buffer) {
         if (mServer != null)
             mServer.feed(buffer);
     }
 
     @Override
     public void onDownloadFinished(long trackId) {
         if (mServer != null)
             mServer.finish();
         
         mDownloadService.removeListener(this);
     }
 
     @Override
     public void onDownloadCanceled(long trackId) {
         mDownloadService.removeListener(this);
         stop();
     }
 
     @Override
     public void onDownloadFailed(long trackId) {
         mDownloadService.removeListener(this);
         stop(); 
     }
 
 }
