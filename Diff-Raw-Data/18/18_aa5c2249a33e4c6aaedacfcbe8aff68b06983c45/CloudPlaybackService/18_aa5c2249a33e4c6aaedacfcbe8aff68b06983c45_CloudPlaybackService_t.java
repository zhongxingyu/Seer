 package com.soundcloud.android.service.playback;
 
 import static com.soundcloud.android.service.playback.State.COMPLETED;
 import static com.soundcloud.android.service.playback.State.EMPTY_PLAYLIST;
 import static com.soundcloud.android.service.playback.State.ERROR;
 import static com.soundcloud.android.service.playback.State.PAUSED;
 import static com.soundcloud.android.service.playback.State.PAUSED_FOCUS_LOST;
 import static com.soundcloud.android.service.playback.State.PAUSED_FOR_BUFFERING;
 import static com.soundcloud.android.service.playback.State.PLAYING;
 import static com.soundcloud.android.service.playback.State.PREPARED;
 import static com.soundcloud.android.service.playback.State.PREPARING;
 import static com.soundcloud.android.service.playback.State.STOPPED;
 
 import com.soundcloud.android.service.LocalBinder;
 import com.soundcloud.android.streaming.StreamProxy;
 import com.wehack.syncedQ.LLApplication;
 import com.wehack.syncedQ.LLQueue;
 import com.wehack.syncedQ.Track;
 import org.jetbrains.annotations.Nullable;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.util.Log;
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 
 public class CloudPlaybackService extends Service  {
     private static String TAG = "PlaybackService";
 
     private static @Nullable Track currentTrack;
     public  static @Nullable Track getCurrentTrack()  { return currentTrack; }
     public static long getCurrentTrackId() { return currentTrack == null ? -1 : currentTrack.getId(); }
     public static boolean isTrackPlaying(long id) { return getCurrentTrackId() == id && state.isSupposedToBePlaying(); }
 
     private static @Nullable CloudPlaybackService instance;
     public static @Nullable CloudPlaybackService getInstance() { return instance; }
     public static @Nullable LLQueue getPlaylistManager() { return instance == null ? null : instance.getPlayQueueManager(); }
     public static long getCurrentProgress() { return instance == null ? -1 : instance.getProgress(); }
     public static int getLoadingPercent() { return instance == null ? -1 : instance.loadPercent(); }
 
     private static State state = STOPPED;
     public static State getState() { return state; }
 
     // public service actions
     public static final String PLAY_ACTION          = "com.soundcloud.android.playback.start";
     public static final String PAUSE_ACTION         = "com.soundcloud.android.playback.pause";
     public static final String NEXT_ACTION          = "com.soundcloud.android.playback.next";
     public static final String PREVIOUS_ACTION      = "com.soundcloud.android.playback.previous";
 
     // broadcast notifications
     public static final String PLAYSTATE_CHANGED  = "com.soundcloud.android.playstatechanged";
     public static final String META_CHANGED       = "com.soundcloud.android.metachanged";
     public static final String PLAYQUEUE_CHANGED  = "com.soundcloud.android.playlistchanged";
     public static final String PLAYBACK_COMPLETE  = "com.soundcloud.android.playbackcomplete";
     public static final String PLAYBACK_ERROR     = "com.soundcloud.android.trackerror";
     public static final String SEEKING            = "com.soundcloud.android.seeking";
     public static final String SEEK_COMPLETE      = "com.soundcloud.android.seekcomplete";
     public static final String BUFFERING          = "com.soundcloud.android.buffering";
     public static final String BUFFERING_COMPLETE = "com.soundcloud.android.bufferingcomplete";
     public static final String PROGRESS           = "com.soundcloud.android.progress";
 
     // private stuff
     private static final int TRACK_ENDED      = 1;
     private static final int SERVER_DIED      = 2;
     private static final int FADE_IN          = 3;
     private static final int FADE_OUT         = 4;
     private static final int DUCK             = 5;
     private static final int CLEAR_LAST_SEEK  = 6;
     private static final int STREAM_EXCEPTION = 7;
     private static final int CHECK_TRACK_EVENT = 8;
     private static final int NOTIFY_META_CHANGED = 9;
     private static final int CHECK_BUFFERING   = 10;
 
     private static final float FADE_CHANGE = 0.02f; // change to fade faster/slower
 
     private @Nullable MediaPlayer mMediaPlayer;
     private int mLoadPercent = 0;       // track buffer indicator
     private boolean mAutoPause = true;  // used when svc is first created and playlist is resumed on start
     private boolean mAutoAdvance = true;// automatically skip to next track
 
     /* package */ LLQueue mPlayQueueManager;
 
     public void setResumeInfo(long resumeTrackId, long resumeTime) {
         Log.i("asdf","Set Resume time " + resumeTrackId + " " + resumeTime);
         mResumeTime = resumeTime;
         mResumeTrackId = resumeTrackId;
     }
 
     private long mResumeTime = -1;      // time of played track
     private long mResumeTrackId = -1;   // id of last played track
     private long mSeekPos = -1;         // desired seek position
     private long mLastRefresh;          // time last refresh hit was sent
 
     private int mServiceStartId = -1;
     private boolean mServiceInUse;
 
     private static final int IDLE_DELAY = 60*1000;  // interval after which we stop the service when idle
     private static final long CHECK_TRACK_EVENT_DELAY = 1000; // check for track timestamp events at this frequency
 
     private boolean mWaitingForSeek;
 
     private StreamProxy mProxy;
 
     private final IBinder mBinder = new LocalBinder<CloudPlaybackService>() {
         @Override public CloudPlaybackService getService() {
             return CloudPlaybackService.this;
         }
     };
 
     public interface PlayExtras{
         String trackId = "track_id";
         String playPosition = "play_position";
         String playFromXferCache = "play_from_xfer_cache";
     }
 
     public interface BroadcastExtras{
         String id = "id";
         String title = "title";
         String user_id = "user_id";
         String username = "username";
         String isPlaying = "isPlaying";
         String isSupposedToBePlaying = "isSupposedToBePlaying";
         String isBuffering = "isBuffering";
         String position = "position";
         String queuePosition = "queuePosition";
         String isLike = "isLike";
         String isRepost = "isRepost";
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         mPlayQueueManager = LLQueue.get();
         IntentFilter commandFilter = new IntentFilter();
         commandFilter.addAction(PLAY_ACTION);
         commandFilter.addAction(PAUSE_ACTION);
         commandFilter.addAction(NEXT_ACTION);
         commandFilter.addAction(PREVIOUS_ACTION);
         commandFilter.addAction(PLAYQUEUE_CHANGED);
 
         // If the service was idle, but got killed before it stopped itself, the
         // system will relaunch it. Make sure it gets stopped again in that case.
         scheduleServiceShutdownCheck();
 
         try {
             mProxy = new StreamProxy(getApp()).init().start();
         } catch (IOException e) {
             Log.e(TAG, "Unable to start service ", e);
         }
 
         instance = this;
     }
 
     @Override
     public void onDestroy() {
         instance = null;
 
         super.onDestroy();
         stop();
         // make sure there aren't any other messages coming
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mPlayerHandler.removeCallbacksAndMessages(null);
         unregisterReceiver(mNoisyReceiver);
         if (mProxy != null && mProxy.isRunning()) mProxy.stop();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mServiceInUse = true;
         return mBinder;
     }
 
     @Override
     public void onRebind(Intent intent) {
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mServiceInUse = true;
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         mServiceInUse = false;
 
         if (state.isSupposedToBePlaying() || state == PAUSED_FOCUS_LOST) {
             // something is currently playing, or will be playing once
             // an in-progress call ends, so don't stop the service now.
             return true;
 
             // If there is a playlist but playback is paused, then wait a while
             // before stopping the service, so that pause/resume isn't slow.
             // Also delay stopping the service if we're transitioning between
             // tracks.
         } else if (!mPlayQueueManager.isEmpty() || mPlayerHandler.hasMessages(TRACK_ENDED)) {
             mDelayedStopHandler.sendEmptyMessageDelayed(0, IDLE_DELAY);
             return true;
 
         } else {
             // No active playlist, OK to stop the service right now
             stopSelf(mServiceStartId);
             return true;
         }
     }
 
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         mServiceStartId = startId;
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         scheduleServiceShutdownCheck();
         // make sure the service will shut down on its own if it was
         // just started but not bound to and nothing is playing
         return START_STICKY;
     }
 
     private void scheduleServiceShutdownCheck() {
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "scheduleServiceShutdownCheck()");
         }
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mDelayedStopHandler.sendEmptyMessageDelayed(0, IDLE_DELAY);
     }
 
     private void notifyChange(String what) {
         Log.d("asdf", "notifyChange(" + what + ") " + currentTrack.getId());
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "notifyChange(" + what + ")");
         }
         Intent i = new Intent(what)
                 .putExtra(BroadcastExtras.id, currentTrack.getId())
                 .putExtra(BroadcastExtras.isPlaying, isPlaying())
                 .putExtra(BroadcastExtras.isSupposedToBePlaying, state.isSupposedToBePlaying())
                 .putExtra(BroadcastExtras.isBuffering, _isBuffering())
                 .putExtra(BroadcastExtras.position, getProgress())
                 .putExtra(BroadcastExtras.queuePosition, mPlayQueueManager.getPosition());
         mPlayQueueManager.onPlaybackServiceChanged(i);
         if (what.equals(META_CHANGED) || what.equals(PLAYBACK_ERROR) || what.equals(PLAYBACK_COMPLETE)) {
             saveQueue();
         }
     }
 
     private void saveQueue(){
         //mPlayQueueManager.saveQueue(currentTrack == null ? 0 : getProgress());
     }
 
     public void openCurrent() {
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "openCurrent(state=" + state + ")");
         }
 
         final Track track = mPlayQueueManager.getCurrentTrack();
         Log.i("asdf","Get Current Track " + track.getTitle());
         if (track != null) {
             if (mAutoPause) {
                 mAutoPause = false;
             }
             mLoadPercent = 0;
             if (track.equals(currentTrack)) {
                 if (!isPlaying()) {
                     notifyChange(META_CHANGED);
                     startTrack(track);
                 }
             } else { // new track
                 currentTrack = track;
                 notifyChange(META_CHANGED);
                 startTrack(track);
 
             }
         } else {
             Log.d(TAG, "playlist is empty");
             state = EMPTY_PLAYLIST;
         }
     }
 
 
 
     private void startTrack(Track track) {
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "startTrack("+track.getTitle()+")");
         }
         if (mMediaPlayer == null) {
             mMediaPlayer = new MediaPlayer();
         }
 
         if (mWaitingForSeek) {
             mWaitingForSeek = false;
             releaseMediaPlayer(true);
         } else {
             switch (state) {
                 case PREPARING:
                 case PAUSED_FOR_BUFFERING:
                     releaseMediaPlayer(true);
                     break;
                 case PLAYING:
                     mPlayerHandler.removeMessages(CHECK_TRACK_EVENT);
                     try {
                         if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mp.stop");
                         mMediaPlayer.stop();
                         state = STOPPED;
                     } catch (IllegalStateException e) {
                         Log.w(TAG, e);
                     }
                     break;
             }
         }
         state = PREPARING;
 
         try {
             if (mProxy == null) {
                 mProxy = new StreamProxy(getApp()).init().start();
             }
             if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mp.reset");
             mMediaPlayer.reset();
             mMediaPlayer.setWakeMode(CloudPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
             mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
             mMediaPlayer.setOnPreparedListener(preparedlistener);
             mMediaPlayer.setOnSeekCompleteListener(seekListener);
             mMediaPlayer.setOnCompletionListener(completionListener);
             mMediaPlayer.setOnErrorListener(errorListener);
             mMediaPlayer.setOnBufferingUpdateListener(bufferingListener);
             mMediaPlayer.setOnInfoListener(infolistener);
             notifyChange(BUFFERING);
             Track next = mPlayQueueManager.getNext();
 
             // if this comes from a shortcut, we may not have the stream url yet. we should get it on info load
             if (currentTrack != null) {
                mMediaPlayer.setDataSource(mProxy.createUri(currentTrack.getStreamUrl(), null).toString());
             }
 
             mMediaPlayer.prepareAsync();
 
         } catch (IllegalStateException e) {
             Log.e(TAG, "error", e);
             gotoIdleState(ERROR);
         } catch (IOException e) {
             Log.e(TAG, "error", e);
             errorListener.onError(mMediaPlayer, 0, 0);
         }
     }
 
     private void releaseMediaPlayer(boolean refresh) {
         Log.w(TAG, "stuck in preparing state!");
         final MediaPlayer old = mMediaPlayer;
         if (old != null){
             new Thread() {
                 @Override
                 public void run() {
                     old.reset();
                     old.release();
                 }
             }.start();
         }
         mMediaPlayer = refresh ? new MediaPlayer() : null;
     }
 
 
     public void play() {
         if (state.isSupposedToBePlaying()) return;
 
         if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "play(state=" + state + ")");
         mLastRefresh = System.currentTimeMillis();
 
         if (currentTrack != null) {
             if (mMediaPlayer != null && state.isStartable()) {
                 // resume
                 if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "mp.start");
                 mMediaPlayer.start();
                 state = PLAYING;
                 mPlayerHandler.removeMessages(CHECK_TRACK_EVENT);
                 mPlayerHandler.sendEmptyMessageDelayed(CHECK_TRACK_EVENT, CHECK_TRACK_EVENT_DELAY);
                 notifyChange(PLAYSTATE_CHANGED);
 
             } else if (state != PLAYING) {
                 // must have been a playback error
                 openCurrent();
             }
         }
     }
 
     // Pauses playback (call play() to resume)
     public void pause() {
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "pause(state="+state+")");
         }
         if (!state.isSupposedToBePlaying()) return;
 
         safePause();
         notifyChange(PLAYSTATE_CHANGED);
     }
 
     private void safePause() {
         if (mMediaPlayer != null) {
             if (state.isPausable()) {
                 if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                 gotoIdleState(PAUSED);
             } else {
                 // get into a determined state
                 stop();
             }
         }
     }
 
     /* package */ void stop() {
         // this is not usually called due to errors, not user interaction
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "stop(state="+state+")");
         }
         if (state != STOPPED ) {
             saveQueue();
 
             if (mMediaPlayer != null) {
                 if (state.isStoppable()) {
                     mMediaPlayer.stop();
                 }
                 releaseMediaPlayer(false);
             }
             gotoIdleState(STOPPED);
         }
     }
 
     public void togglePlayback() {
         if (state.isSupposedToBePlaying()) {
             pause();
         } else if (currentTrack != null) {
             play();
         } else {
             openCurrent();
         }
     }
 
 
     private void gotoIdleState(State newState) {
         if (!newState.isInIdleState()) throw new IllegalArgumentException(newState + " is not a valid idle state");
 
         state = newState;
         mPlayerHandler.removeMessages(FADE_OUT);
         mPlayerHandler.removeMessages(FADE_IN);
         mPlayerHandler.removeMessages(CHECK_TRACK_EVENT);
         scheduleServiceShutdownCheck();
     }
 
     /* package */ boolean prev() {
         if (mPlayQueueManager.prev()) {
             openCurrent();
             return true;
         } else {
             return false;
         }
     }
 
     /* package */ boolean next() {
         if (mPlayQueueManager.next()) {
             openCurrent();
             return true;
         } else {
             return false;
         }
     }
 
     /* package */
     public void setQueuePosition(int pos) {
         if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "setQueuePosition("+pos+")");
 
         if (mPlayQueueManager.getPosition() != pos &&
                 mPlayQueueManager.setPosition(pos)) {
             openCurrent();
         }
     }
 
     /* package */ int getDuration() {
         return currentTrack == null ? -1 : currentTrack.getDuration();
     }
 
     /* package */ boolean _isBuffering() {
         return state == PAUSED_FOR_BUFFERING || state == PREPARING || mWaitingForSeek;
     }
 
     /*
      * Returns the current playback position in milliseconds
      */
     /* package */
     public long getProgress() {
 
         if (currentTrack != null && mResumeTrackId == currentTrack.getId()) {
             return mResumeTime; // either -1 or a valid resume time
         } else if (mWaitingForSeek && mSeekPos > 0) {
             return mSeekPos;
         } else if (mMediaPlayer != null && !state.isError() && state != PREPARING) {
             return mMediaPlayer.getCurrentPosition();
         } else {
             return 0;
         }
     }
 
     /* package */
     public int loadPercent() {
         return mMediaPlayer != null && !state.isError() ? mLoadPercent : 0;
     }
 
     /* package */ boolean _isSeekable() {
         return (mMediaPlayer != null
                 && state.isSeekable()
                 && currentTrack != null);
     }
 
     /* package */ boolean isNotSeekablePastBuffer() {
         // Some phones on 2.2 ship with broken opencore
         return Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO && StreamProxy.isOpenCore();
     }
 
     public long seek(float percent, boolean performSeek) {
         return seek((long) (getDuration() * percent), performSeek);
     }
 
     /* package */
     public long seek(long pos, boolean performSeek) {
 
         if (pos <= 0) {
             pos = 0;
         }
 
         final long currentPos = (mMediaPlayer != null && !state.isError()) ? mMediaPlayer.getCurrentPosition() : 0;
 
         long duration = getDuration();
 
         final long newPos;
         // don't go before the playhead if they are trying to seek
         // beyond, just maintain their current position
         if (pos > currentPos && currentPos > duration) {
             newPos = currentPos;
         } else if (pos > duration) {
             newPos = duration;
         } else {
             newPos = pos;
         }
 
         if (performSeek && newPos != currentPos) {
             if (Log.isLoggable(TAG, Log.DEBUG)) {
                 Log.d(TAG, "seeking to " + newPos);
             }
             mSeekPos = newPos;
             mWaitingForSeek = true;
             notifyChange(SEEKING);
 
             mMediaPlayer.seekTo((int) newPos);
         } else {
             if (Log.isLoggable(TAG, Log.DEBUG)) {
                 Log.d(TAG, "NOT seeking to " + newPos);
             }
         }
         return newPos;
 
 
     }
 
     private boolean isMediaPlayerPlaying() {
         try {
             return mMediaPlayer != null && mMediaPlayer.isPlaying();
         } catch (IllegalStateException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     /* package */
     public boolean isPlaying() {
         return isMediaPlayerPlaying() && state.isSupposedToBePlaying();
     }
 
     public void restartTrack() {
         openCurrent();
     }
 
     public LLQueue getPlayQueueManager() {
         return mPlayQueueManager;
     }
 
     private void setVolume(float vol) {
         if (mMediaPlayer != null && !state.isError()) {
             try {
                 mMediaPlayer.setVolume(vol, vol);
             } catch (IllegalStateException ignored) {
                 Log.w(TAG, ignored);
             }
         }
     }
 
 
 
     private LLApplication getApp() {
         return (LLApplication) getApplication();
     }
 
     private static final class DelayedStopHandler extends Handler {
         private WeakReference<CloudPlaybackService> serviceRef;
 
         private DelayedStopHandler(CloudPlaybackService service) {
             serviceRef = new WeakReference<CloudPlaybackService>(service);
         }
 
         @Override
         public void handleMessage(Message msg) {
             CloudPlaybackService service = serviceRef.get();
             // Check again to make sure nothing is playing right now
             if (service != null && !state.isSupposedToBePlaying()
                     && state != PAUSED_FOCUS_LOST
                     && !service.mServiceInUse
                     && !service.mPlayerHandler.hasMessages(TRACK_ENDED)) {
 
                 if (Log.isLoggable(TAG, Log.DEBUG)) {
                     Log.d(TAG, "DelayedStopHandler: stopping service");
                 }
 
                 if (state != STOPPED) {
                     service.saveQueue();
                 }
 
                 service.stopSelf(service.mServiceStartId);
             }
         }
     }
 
     private final Handler mDelayedStopHandler = new DelayedStopHandler(this);
 
     private final BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
         @Override public void onReceive(Context context, Intent intent) {
             pause();
         }
     };
 
     private static final class PlayerHandler extends Handler {
         private static final float DUCK_VOLUME = 0.1f;
 
         private WeakReference<CloudPlaybackService> serviceRef;
         private float mCurrentVolume = 1.0f;
 
         private PlayerHandler(CloudPlaybackService service) {
             this.serviceRef = new WeakReference<CloudPlaybackService>(service);
         }
 
         @Override
         public void handleMessage(Message msg) {
             final CloudPlaybackService service = serviceRef.get();
             if (service == null) {
                 return;
             }
 
             if (Log.isLoggable(TAG, Log.DEBUG)) {
                 Log.d(TAG, "handleMessage(" + msg.what + ", state=" + state + ")");
             }
 
             switch (msg.what) {
                 case CHECK_BUFFERING:
                     if (!state.equals(State.PAUSED_FOR_BUFFERING)) {
                         service.notifyChange(BUFFERING_COMPLETE);
                     }
                     break;
 
                 case NOTIFY_META_CHANGED:
                     service.notifyChange(META_CHANGED);
                     break;
                 case FADE_IN:
                     removeMessages(FADE_OUT);
 
                     if (!state.isSupposedToBePlaying()) {
                         mCurrentVolume = 0f;
                         service.setVolume(0f);
                         service.play();
                         sendEmptyMessageDelayed(FADE_IN, 10);
                     } else {
                         mCurrentVolume += FADE_CHANGE;
                         if (mCurrentVolume < 1.0f) {
                             sendEmptyMessageDelayed(FADE_IN, 10);
                         } else {
                             mCurrentVolume = 1.0f;
                         }
                         service.setVolume(mCurrentVolume);
                     }
                     break;
                 case FADE_OUT:
                     removeMessages(FADE_IN);
                     if (service.isPlaying()) {
                         mCurrentVolume -= FADE_CHANGE;
                         if (mCurrentVolume > 0f) {
                             sendEmptyMessageDelayed(FADE_OUT, 10);
                         } else {
                             if (service != null && service.mMediaPlayer != null) service.mMediaPlayer.pause();
                             mCurrentVolume = 0f;
                             state = PAUSED_FOCUS_LOST;
                         }
                         service.setVolume(mCurrentVolume);
                     } else {
                         service.setVolume(0f);
                     }
                     break;
                 case DUCK:
                     removeMessages(FADE_IN);
                     removeMessages(FADE_OUT);
                     service.setVolume(DUCK_VOLUME);
                     break;
                 case SERVER_DIED:
                     if (state == PLAYING && service.mAutoAdvance) service.next();
                     break;
                 case TRACK_ENDED:
                     if (!service.mAutoAdvance || !service.next()) {
                         service.notifyChange(PLAYBACK_COMPLETE);
                         service.gotoIdleState(COMPLETED);
                     }
                     break;
                 case CLEAR_LAST_SEEK:
                     service.mSeekPos = -1;
                     break;
                 case CHECK_TRACK_EVENT:
                     if (currentTrack != null) {
                         if (state.isSupposedToBePlaying()) {
                             long now = System.currentTimeMillis();
                             if (now - service.mLastRefresh > 1000) {
                                 service.notifyChange(PROGRESS);
                                 service.mLastRefresh = now;
                             }
                         }
                         sendEmptyMessageDelayed(CHECK_TRACK_EVENT, CHECK_TRACK_EVENT_DELAY);
                     } else {
                         removeMessages(CHECK_TRACK_EVENT);
                     }
                     break;
             }
         }
     }
 
     private final Handler mPlayerHandler = new PlayerHandler(this);
 
     final MediaPlayer.OnInfoListener infolistener = new MediaPlayer.OnInfoListener() {
         @Override
         public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
             if (Log.isLoggable(TAG, Log.DEBUG)) {
                 Log.d(TAG, "onInfo(" + what + "," + extra + ", state=" + state + ")");
             }
 
             switch (what) {
                 case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                     mPlayerHandler.removeMessages(CLEAR_LAST_SEEK);
                     state = PAUSED_FOR_BUFFERING;
                     notifyChange(BUFFERING);
                     break;
 
                 case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                     if (mSeekPos != -1 && !mWaitingForSeek) {
                         mPlayerHandler.removeMessages(CLEAR_LAST_SEEK);
                         mPlayerHandler.sendEmptyMessageDelayed(CLEAR_LAST_SEEK, 3000);
                     } else if (Log.isLoggable(TAG, Log.DEBUG)) {
                         Log.d(TAG, "Not clearing seek, waiting for seek to finish");
                     }
                     if (!state.isSupposedToBePlaying()) {
                         safePause();
                     } else {
                         // still playing back, set proper state after buffering state
                         state = PLAYING;
                     }
                     notifyChange(BUFFERING_COMPLETE);
                     break;
                 default:
             }
             return true;
         }
     };
 
     final MediaPlayer.OnBufferingUpdateListener bufferingListener = new MediaPlayer.OnBufferingUpdateListener() {
         public void onBufferingUpdate(MediaPlayer mp, int percent) {
             //noinspection ObjectEquality
             if (mMediaPlayer == mp) {
                 if (Log.isLoggable(TAG, Log.DEBUG) && mLoadPercent != percent) {
                     Log.d(TAG, "onBufferingUpdate("+percent+")");
                 }
 
                 mLoadPercent = percent;
             }
         }
     };
 
     final MediaPlayer.OnSeekCompleteListener seekListener = new MediaPlayer.OnSeekCompleteListener() {
         public void onSeekComplete(MediaPlayer mp) {
             if (Log.isLoggable(TAG, Log.DEBUG)) {
                 Log.d(TAG, "onSeekComplete(state="+state+")");
             }
             //noinspection ObjectEquality
             if (mMediaPlayer == mp) {
                 // only clear seek if we are not buffering. If we are buffering, it will be cleared after buffering completes
                 if (state != State.PAUSED_FOR_BUFFERING){
                     // keep the last seek time for 3000 ms because getCurrentPosition will be incorrect at first
                     mPlayerHandler.removeMessages(CLEAR_LAST_SEEK);
                     mPlayerHandler.sendEmptyMessageDelayed(CLEAR_LAST_SEEK, 3000);
 
                 } else if (Log.isLoggable(TAG, Log.DEBUG)) {
                     Log.d(TAG, "Not clearing seek, waiting for buffer");
                 }
 
 
                 mWaitingForSeek = false;
                 notifyChange(SEEK_COMPLETE);
 
                 // respect pauses during seeks
                 if (!state.isSupposedToBePlaying()) safePause();
             }
         }
     };
 
     final MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
         public void onCompletion(MediaPlayer mp) {
             if (Log.isLoggable(TAG, Log.DEBUG)) {
                 Log.d(TAG, "onCompletion(state="+state+")");
             }
             mPlayerHandler.sendEmptyMessage(TRACK_ENDED);
         }
     };
 
     MediaPlayer.OnPreparedListener preparedlistener = new MediaPlayer.OnPreparedListener() {
         public void onPrepared(MediaPlayer mp) {
             //noinspection ObjectEquality
             if (mp == mMediaPlayer) {
                 if (Log.isLoggable(TAG, Log.DEBUG)) {
                     Log.d(TAG, "onPrepared(state=" + state + ")");
                 }
 
                 if (state == PREPARING) {
                     state = PREPARED;
                     // do we need to resume a track position ?
                     if (getCurrentTrackId() == mResumeTrackId && mResumeTime > 0) {
                         Log.i("asdf","RESUME " + mResumeTime);
                         if (Log.isLoggable(TAG, Log.DEBUG)) {
                             Log.d(TAG, "resuming to "+mResumeTime);
                         }
 
                         // play before seek to prevent ANR
                         play();
                         seek(mResumeTime, true);
                         mResumeTime = mResumeTrackId = -1;
 
 
                         // normal play, unless first start (autopause=true)
                     } else {
                         // sometimes paused for buffering happens right after prepare, so check buffering on a delay
                         mPlayerHandler.sendEmptyMessageDelayed(CHECK_BUFFERING,500);
 
                         //  FADE_IN will call play()
                         if (!mAutoPause) {
                             mPlayerHandler.removeMessages(FADE_OUT);
                             mPlayerHandler.removeMessages(FADE_IN);
                             setVolume(1.0f);
                             play();
                         }
                     }
 
                 } else {
                     stop();
                 }
             }
         }
     };
 
     MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
         public boolean onError(MediaPlayer mp, int what, int extra) {
             Log.e(TAG, "onError("+what+ ", "+extra+", state="+state+")");
             return true;
         }
     };
 
 
 }
