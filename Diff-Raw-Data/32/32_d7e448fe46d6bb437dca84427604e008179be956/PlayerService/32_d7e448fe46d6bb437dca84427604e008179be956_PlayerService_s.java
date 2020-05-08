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
 
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.muckebox.android.R;
 import org.muckebox.android.audio.PlayerWrapper;
 import org.muckebox.android.db.MuckeboxContract.PlaylistEntry;
 import org.muckebox.android.db.MuckeboxProvider;
 import org.muckebox.android.db.PlaylistHelper;
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
 import android.media.RemoteControlClient;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 public class PlayerService extends Service
     implements PlayerWrapper.Listener {
 	private final static String LOG_TAG = "PlayerService";
 	
 	private final static int
 	    NOTIFICATION_ID = 23,
 	    PREFETCH_INTERVAL = 15;
 	
 	public final static String EXTRA_PLAYLIST_ITEM_ID = "playlist_item_id";
 	
 	public final static String
 	    ACTION_LOAD = "load",
 	    ACTION_PREVIOUS = "previous",
 	    ACTION_PAUSE = "pause",
 	    ACTION_RESUME = "resume",
 	    ACTION_NEXT = "next";
 	
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
 
 	private Handler mMainHandler;
 	
 	private HandlerThread mHelperThread;
 	private Handler mHelperHandler;
 
     private ComponentName mRemoteEventReceiver = null;
     private RemoteControlClient mRemoteControlClient = null;
     private AudioManager mAudioManager = null;
     
     private boolean mReceiverRegistered = false;
     private boolean mHasAudioFocus = false;
     
     private DownloadService mDownloadService;
     
     private PlayerWrapper mCurrentPlayer = null;
     private Timer mTimer = null;
     
     NotificationManager mNotificationManager = null;
 
     private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
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
     
     private class PrefetchTask extends TimerTask {
         public void run() {
             if (mCurrentPlayer != null) {
                 mCurrentPlayer.prefetchNextTrack();
             }
         }
     }
     
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
 		
 		bindService(new Intent(this, DownloadService.class),
             mConnection, Context.BIND_AUTO_CREATE);
         
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
 
 	    getBaseContext().unbindService(mConnection);
 
 		Log.d(LOG_TAG, "Service destroyed.");
 	}
 
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
 	       
 	    if (getAudioFocus()) {
             mState = State.BUFFERING;
             
             for (PlayerListener l: mListeners) {
                 if (l != null)
                     l.onStartBuffering();
             }
             
             mHelperHandler.post(new Runnable() {
                 public void run() {
                     int trackId = PlaylistHelper.getTrackId(getApplicationContext(),
                         Uri.withAppendedPath(MuckeboxProvider.URI_PLAYLIST_ENTRY,
                             Integer.toString(playlistEntryId)));
     
                     mCurrentPlayer = new PlayerWrapper(getApplicationContext(),
                         mDownloadService, playlistEntryId, trackId);
                     mCurrentPlayer.setListener(PlayerService.this);
                     mCurrentPlayer.play();          
                 }
             });
 	    } else {
 	        Toast.makeText(this, getText(R.string.error_audiofocus),
 	            Toast.LENGTH_LONG).show();
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
 	
 	private void updateRemoteControl(PlayerWrapper.TrackInfo trackInfo)
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
 	    PlayerWrapper.TrackInfo trackInfo = null;
 	    
 	    if (mCurrentPlayer != null)
 	        trackInfo = mCurrentPlayer.getTrackInfo();
 	    
 	    if (trackInfo != null)
 	        mNotificationManager.notify(NOTIFICATION_ID, getNotification(trackInfo));
 	    else
 	        mNotificationManager.cancel(NOTIFICATION_ID);
 	}
 	
 	private void startAndNotify(PlayerWrapper.TrackInfo trackInfo) {
         startForeground(NOTIFICATION_ID, getNotification(trackInfo));
 	}
 	
 	private Notification getNotification(PlayerWrapper.TrackInfo trackInfo) {
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
 
 	public void stop() {
 	    stop(false);
 	}
 	
 	public void stop(boolean keepFocus) {
 	    mState = State.STOPPED;
 
 	    if (mCurrentPlayer != null)
 	        mCurrentPlayer.destroy();
 	    
 	    mCurrentPlayer = null;
 
 	    clearCurrent();
 
 	    for (PlayerListener l: mListeners)
 	        if (l != null)
 	            l.onStopPlaying();
 
 	    if (! keepFocus)
 	        dropAudioFocus();
 	    
         stopForeground(true);
         stopPrefetchTimer();
         
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
 			
 			mCurrentPlayer.resume();
 
 			for (PlayerListener l: mListeners)
 				if (l != null) l.onPlayResumed();
 			
 			onResumed();
 		}
 	}
 	
 	public void pause() {
 		if (mState == State.PLAYING)
 		{
 			mState = State.PAUSED;
 			
 			mCurrentPlayer.pause();
 			
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
         stopPrefetchTimer();
         
         Log.d(LOG_TAG, "Paused");
 	}
 	
 	private void onResumed() {
         if (mRemoteControlClient != null)
             mRemoteControlClient.setPlaybackState(
                 RemoteControlClient.PLAYSTATE_PLAYING);
         
         updateNotification();
         startPrefetchTimer();
         
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
 	                        Long.toString(mCurrentPlayer.getPlaylistEntryId())),
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
 	        mCurrentPlayer.seek(targetSeconds);
 	        startPrefetchTimer();
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
 	    if (mCurrentPlayer != null)
 	        return mCurrentPlayer.getTrackLength();
 	    
 	    return 0;
 	}
 	
 	public Integer getCurrentPlayPosition() {
 	    if (mState == State.PLAYING || mState == State.PAUSED)
 	        return mCurrentPlayer.getPlayPosition();
 	    
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
 	    if (mCurrentPlayer != null)
 	        return mCurrentPlayer.getTrackTitle();
 	    
 	    return null;
 	}
 	
 	public PlayerWrapper.TrackInfo getCurrentTrackInfo() {
 	    if (mCurrentPlayer != null)
 	        return mCurrentPlayer.getTrackInfo();
 	    
 	    return null;
 	}
 	
 	private void startPrefetchTimer() {
	    long delay = mCurrentPlayer.getTrackLength() -
	        mCurrentPlayer.getPlayPosition() - PREFETCH_INTERVAL;
	    
	    delay *= 1000;
 	    
	    if (delay > 0) {
	        stopPrefetchTimer();
	        
            mTimer = new Timer();
            mTimer.schedule(new PrefetchTask(), delay);
 	    }
 	}
 	
 	private void stopPrefetchTimer() {
 	    if (mTimer != null) {
 	        mTimer.cancel();
 	        mTimer = null;
 	    }
 	}
 
 	@Override
 	public void onStartPlaying(PlayerWrapper player) {
         for (PlayerListener l: mListeners)
             l.onStartPlaying();
         
         if (mRemoteControlClient != null)
             mRemoteControlClient.setPlaybackState(
                 RemoteControlClient.PLAYSTATE_PLAYING);
         
         mState = State.PLAYING;
         
         updateNotification();
         startPrefetchTimer();
     }
 
     @Override
     public void onCompletion(PlayerWrapper player) {
         if (player.getTrackInfo().hasNext)
             next();
         else
             stop();
     }
     
     @Override
     public void onBufferingStarted(PlayerWrapper player) {
         for (PlayerListener l: mListeners) {
             if (l != null)
                 l.onStartBuffering();
         }
         
         onPaused();
     }
     
     @Override
     public void onBufferingFinished(PlayerWrapper player) {
         for (PlayerListener l: mListeners) {
             if (l != null)
                 l.onPlayResumed();
         }
         
         onResumed();
     }
 
     @Override
     public void onTrackInfo(PlayerWrapper wrapper, PlayerWrapper.TrackInfo trackInfo) {
         updateRemoteControl(trackInfo);
         
         for (PlayerListener l: mListeners)
         {
             if (l != null)
             {
                 l.onNewTrack(trackInfo);
             }
         }
         
         PlayerService.this.startAndNotify(trackInfo);
         
         setAsCurrent(mCurrentPlayer.getPlaylistEntryUri());
     }
     
     @Override
     public void onStop(PlayerWrapper player) {
         stop();
     }
 }
