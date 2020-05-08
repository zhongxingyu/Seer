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
 
 package org.muckebox.android.audio;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import org.muckebox.android.R;
 import org.muckebox.android.db.MuckeboxProvider;
 import org.muckebox.android.db.PlaylistHelper;
 import org.muckebox.android.db.MuckeboxContract.AlbumEntry;
 import org.muckebox.android.db.MuckeboxContract.ArtistEntry;
 import org.muckebox.android.db.MuckeboxContract.CacheEntry;
 import org.muckebox.android.db.MuckeboxContract.TrackEntry;
 import org.muckebox.android.net.DownloadServer;
 import org.muckebox.android.net.PreannounceTask;
 import org.muckebox.android.services.DownloadListener;
 import org.muckebox.android.services.DownloadService;
 import org.muckebox.android.utils.Preferences;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.PowerManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class PlayerWrapper
     implements  MediaPlayer.OnPreparedListener, 
     MediaPlayer.OnCompletionListener, 
     MediaPlayer.OnErrorListener, 
     MediaPlayer.OnInfoListener,
     DownloadListener {
     
     private final static String LOG_TAG = "PlayerWrapper";
     
     public interface Listener {
         public abstract void onTrackInfo(PlayerWrapper player, TrackInfo trackInfo);
         public abstract void onStartPlaying(PlayerWrapper player);
         public abstract void onBufferingStarted(PlayerWrapper player);
         public abstract void onBufferingFinished(PlayerWrapper player);
         public abstract void onCompletion(PlayerWrapper player);
         public abstract void onStop(PlayerWrapper player);
     };
     
     public class TrackInfo {
         public int trackId;
         public long playlistEntryId;
         
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
     
     private Context mContext;
     
     private MediaPlayer mMediaPlayer;
     private boolean mStopRequested = false, mPreparing = false;
     
     private long mPlaylistEntryId;
     private Uri mPlaylistEntryUri;
     
     private int mTrackId;
     
     private DownloadService mDownloadService;
     private DownloadServer mServer;
     
     private TrackInfo mTrackInfo;
 
     private Handler mMainHandler;
     private HandlerThread mHelperThread;
     private Handler mHelperHandler;
 
     private Listener mListener = null;
     
     public PlayerWrapper(Context context, DownloadService downloadService, long playlistTrackId, int trackId) {
         mContext = context;
         
         mDownloadService = downloadService;
         
         mPlaylistEntryId = playlistTrackId;
         mTrackId = trackId;
 
         mMediaPlayer = new MediaPlayer();
         mMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
         
         mMediaPlayer.setOnPreparedListener(this);
         mMediaPlayer.setOnCompletionListener(this);
         mMediaPlayer.setOnInfoListener(this);
         mMediaPlayer.setOnErrorListener(this);
         
         mMainHandler = new Handler(context.getMainLooper());
         
         mHelperThread = new HandlerThread("PlayerServiceHelper");
         mHelperThread.start();
         
         mHelperHandler = new Handler(mHelperThread.getLooper());
     }
     
     public void setListener(Listener listener) {
         mListener = listener;
     }
     
     public void destroy() {
         if (mMediaPlayer != null) {
             if (mPreparing)
                 mStopRequested = true;
             else
                 mMediaPlayer.release();
         }
         
         if (mServer != null)
             mServer.quit();
         
         mDownloadService.removeListener(this);
     }
     
     public void play() {
         mHelperHandler.post(new Runnable() {
             public void run() {
                 boolean isStreaming = playTrackFromAnywhere(mTrackId);
                                
                 fetchTrackInfo(mPlaylistEntryId, mTrackId, isStreaming);     
             }
         });
     }
     
     public void resume() {
         mMediaPlayer.start();
     }
     
     public void pause() {
         mMediaPlayer.pause();
     }
     
     public void stop() {
         if (mMediaPlayer.isPlaying())
             mMediaPlayer.stop();
         
         if (mListener != null)
             mListener.onStop(this);
     }
     
     public void seek(int seconds) {
         mMediaPlayer.seekTo(seconds * 1000);
     }
     
     public long getPlaylistEntryId() {
         return mPlaylistEntryId;
     }
     
     public Uri getPlaylistEntryUri() {
         return mPlaylistEntryUri;
     }
     
     public TrackInfo getTrackInfo() {
         return mTrackInfo;
     }
     
     public String getTrackTitle() {
         if (mTrackInfo != null)
             return mTrackInfo.title;
         
         return null;
     }
     
     public Integer getTrackLength() {
         if (mTrackInfo != null)
             return mTrackInfo.duration;
         
         return null;
     }
     
     public Integer getPlayPosition() {
         return mMediaPlayer.getCurrentPosition() / 1000;
     }
     
     public void prefetchNextTrack() {
         if (mTrackInfo.hasNext && ! mTrackInfo.nextTrackRequested) {
             Log.v(LOG_TAG, "Requesting next track (" +
                 mTrackInfo.nextTrackId + ")");
             mDownloadService.startDownload(
                 mTrackInfo.nextTrackId, false, false);
             mTrackInfo.nextTrackRequested = true;
         }
     }
     
     private boolean playTrackFromAnywhere(final int trackId) {
         Cursor c = mContext.getContentResolver().query(
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
     
     protected void playTrackFromFile(final String filename, final int trackId) {
         Log.d(LOG_TAG, "Playing from local file " + filename);
         
         try {
             FileInputStream input = mContext.openFileInput(filename);
             
             mMediaPlayer.reset();
             mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
             mMediaPlayer.setDataSource(input.getFD());
             mMediaPlayer.prepareAsync();
             
             input.close();
             
             mPreparing = true;
         } catch (IOException e) {
             Log.e(LOG_TAG, "Could not open file " + filename);
 
             DownloadService.discardTrack(mContext, trackId);
             
             Toast.makeText(mContext, mContext.getText(
                 R.string.error_local_playback), Toast.LENGTH_LONG).show();
 
             stop();
         }
     }
     
     protected void playTrackFromStream(final int trackId) {
         Log.d(LOG_TAG, "Start playing streamed track " + trackId);
         
         mDownloadService.registerListener(this, trackId);
         mDownloadService.startDownload(trackId, false, true);
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
                 mMediaPlayer.setDataSource(mServer.getUrl());
                 mMediaPlayer.prepareAsync();
                 
                 mPreparing = true;
             } catch (IOException e) {
                 stop();
             }   
         }
     }
     
     private void fetchTrackInfo(long mPlaylistEntryId2, int trackId, boolean isStreaming) {
         mPlaylistEntryUri = Uri.withAppendedPath(
             MuckeboxProvider.URI_PLAYLIST_ENTRY, Long.toString(mPlaylistEntryId2));
         
         final TrackInfo trackInfo = new TrackInfo();
         
         trackInfo.playlistEntryId = mPlaylistEntryId;
         trackInfo.trackId = trackId;
         
         trackInfo.isStreaming = isStreaming;
         trackInfo.position = 0;
         trackInfo.hasNext = ! PlaylistHelper.isLast(mContext, mPlaylistEntryUri);
         trackInfo.hasPrevious = ! PlaylistHelper.isFirst(mContext, mPlaylistEntryUri);
         
         if (trackInfo.hasNext) {
             trackInfo.nextTrackId = PlaylistHelper.getNextTrackId(
                 mContext, mPlaylistEntryUri);
             
            if (isStreaming && Preferences.getTranscodingEnabled())
                 new PreannounceTask().execute(trackInfo.nextTrackId);
         }
   
         Cursor c = mContext.getContentResolver().query(Uri.withAppendedPath(
             MuckeboxProvider.URI_TRACKS_WITH_DETAILS, Integer.toString(trackId)), null, null, null, null);
         
         try {
             if (c.moveToFirst())
             {
                 trackInfo.album = c.getString(c.getColumnIndex(AlbumEntry.ALIAS_TITLE));
                 trackInfo.artist = c.getString(c.getColumnIndex(ArtistEntry.ALIAS_NAME));
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
                     mTrackInfo = trackInfo;
                     
                     if (mListener != null)
                         mListener.onTrackInfo(PlayerWrapper.this, trackInfo);
                 }
             });
         } finally {
             c.close();
         }
     }
 
     @Override
     public void onPrepared(MediaPlayer mp) {
         if (mStopRequested) {
             mp.reset();
             mp.release();
         } else {
             mp.start();
         }
         
         mPreparing = false;
         
         mMainHandler.post(new Runnable() {
             public void run() {
                 if (mListener != null)
                     mListener.onStartPlaying(PlayerWrapper.this);
             }
         });
     }
     
     @Override
     public void onCompletion(MediaPlayer mp) {
         mMainHandler.post(new Runnable() {
             public void run() {
                 if (mListener != null)
                     mListener.onCompletion(PlayerWrapper.this);
             }
         });
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
         
         Toast.makeText(mContext,
             String.format((String) mContext.getText(R.string.error_playback) +
                 "(" + whatStr + ", " + extraStr + ")", what, extra),
             Toast.LENGTH_SHORT).show();
         
         Log.e(LOG_TAG, "Error " + what + " (" + whatStr +
             "), extra " + extra + " (" + extraStr + ")");
         
         mMainHandler.post(new Runnable() {
             public void run() {
                 mPreparing = false;
                 stop();
             }
         });
         
         return false;
     }
     
     @Override
     public boolean onInfo(MediaPlayer mp, int what, int extra) {
         switch (what) {
         case MediaPlayer.MEDIA_INFO_BUFFERING_START:
             mMainHandler.post(new Runnable() {
                 public void run() {
                     if (mListener != null)
                         mListener.onBufferingStarted(PlayerWrapper.this);
                 }
             });
 
             return true;
             
         case MediaPlayer.MEDIA_INFO_BUFFERING_END:
             mMainHandler.post(new Runnable() {
                 public void run() {
                     if (mListener != null)
                         mListener.onBufferingFinished(PlayerWrapper.this);
                 }
             });
             
             return true;
         }
         
         return false;
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
