 /*
  *  YAMMP - Yet Another Multi Media Player for android
  *  Copyright (C) 2011-2012  Mariotaku Lee <mariotaku.lee@gmail.com>
  *
  *  This file is part of YAMMP.
  *
  *  YAMMP is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  YAMMP is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with YAMMP.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.mariotaku.harmony;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.AudioManager.OnAudioFocusChangeListener;
 import android.media.MediaPlayer;
 import android.media.audiofx.AudioEffect;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.SystemClock;
 import android.provider.MediaStore;
 import android.support.v4.app.NotificationCompat;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.widget.Toast;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.Calendar;
 import java.util.Random;
 import java.util.Vector;
 import org.mariotaku.harmony.model.TrackInfo;
 import org.mariotaku.harmony.util.MusicUtils;
 import org.mariotaku.harmony.util.PreferencesEditor;
 import android.content.res.Resources;
 import org.mariotaku.harmony.model.AlbumInfo;
 
 /**
  * Provides "background" audio playback capabilities, allowing the user to
  * switch between activities without stopping playback.
  */
 public class MusicPlaybackService extends Service implements Constants {
 
 	/**
 	 * used to specify whether enqueue() should start playing the new list of
 	 * files right away, next or once all the currently queued files have been
 	 * played
 	 */
 
 	private static final int TRACK_ENDED = 1;
 	private static final int RELEASE_WAKELOCK = 2;
 	private static final int SERVER_DIED = 3;
 	private static final int FOCUSCHANGE = 4;
 	private static final int FADEDOWN = 5;
 	private static final int FADEUP = 6;
 
 	private static final int START_SLEEP_TIMER = 1;
 	private static final int STOP_SLEEP_TIMER = 2;
 
 	private MultiPlayer mPlayer;
 	private ContentResolver mResolver;
 	private TelephonyManager mTelephonyManager;
 
 	private TrackInfo mTrackInfo;
 	private NotificationManager mNotificationManager;
 	private int mShuffleMode = SHUFFLE_MODE_NONE;
 
 	private int mRepeatMode = REPEAT_MODE_NONE;
 	private long[] mPlayList = null;
 	private int mPlayListLen = 0;
 	private Vector<Integer> mHistory = new Vector<Integer>();
 
 	//private Cursor mCursor;
 	private int mPlayPos = -1;
 
 	private final Shuffler mShuffler = new Shuffler();
 	private int mOpenFailedCounter = 0;
 	private static final String[] CURSOR_COLUMNS = new String[] { MediaStore.Audio.Media._ID,
 			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
 			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
 			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ARTIST_ID,
 			MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.IS_PODCAST,
 			MediaStore.Audio.Media.BOOKMARK };
 	private final static int IDCOLIDX = 0;
 	private final static int PODCASTCOLIDX = 8;
 	private final static int BOOKMARKCOLIDX = 9;
 	private BroadcastReceiver mUnmountReceiver = null;
 	private BroadcastReceiver mA2dpReceiver = null;
 	private WakeLock mWakeLock;
 	private int mServiceStartId = -1;
 	private boolean mServiceInUse = false;
 	private boolean mIsSupposedToBePlaying = false;
 	private boolean mQuietMode = false;
 	private AudioManager mAudioManager;
 	private boolean mQueueIsSaveable = true;
 	// used to track what type of audio focus loss caused the playback to pause
 	private boolean mPausedByTransientLossOfFocus = false;
 	// used to track current volume
 	private float mCurrentVolume = 1.0f;
 	private PreferencesEditor mPreferences;
 	// We use this to distinguish between different cards when saving/restoring
 	// playlists.
 	// This will have to change if we want to support multiple simultaneous
 	// cards.
 	private int mCardId;
 	// interval after which we stop the service when idle
 	private static final int IDLE_DELAY = 60000;
 	private boolean mGentleSleepTimer, mSleepTimerTimedUp;
 	private long mCurrentTimestamp, mStopTimestamp;
 	
 	private boolean mExternalAudioDeviceConnected = false;
 
 	private Handler mMediaplayerHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			MusicUtils.debugLog("mMediaplayerHandler.handleMessage " + msg.what);
 			switch (msg.what) {
 				case FADEDOWN:
 					mCurrentVolume -= 0.05f;
 					if (mCurrentVolume > 0.2f) {
 						mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
 					} else {
 						mCurrentVolume = 0.2f;
 					}
 					mPlayer.setVolume(mCurrentVolume);
 					break;
 				case FADEUP:
 					mCurrentVolume += 0.01f;
 					if (mCurrentVolume < 1.0f) {
 						mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 10);
 					} else {
 						mCurrentVolume = 1.0f;
 					}
 					mPlayer.setVolume(mCurrentVolume);
 					break;
 				case SERVER_DIED:
 					if (mIsSupposedToBePlaying) {
 						next(true);
 					} else {
 						// the server died when we were idle, so just
 						// reopen the same song (it will start again
 						// from the beginning though when the user
 						// restarts)
 						openCurrent();
 					}
 					break;
 				case TRACK_ENDED:
 					if (mRepeatMode == REPEAT_MODE_CURRENT) {
 						seek(0);
 						play();
 					} else {
 						next(false);
 					}
 					break;
 				case RELEASE_WAKELOCK:
 					mWakeLock.release();
 					break;
 				case FOCUSCHANGE:
 					// This code is here so we can better synchronize it with
 					// the code that handles fade-in
 					switch (msg.arg1) {
 						case AudioManager.AUDIOFOCUS_LOSS:
 							Log.v(LOGTAG_SERVICE, "AudioFocus: received AUDIOFOCUS_LOSS");
 							if (isPlaying()) {
 								mPausedByTransientLossOfFocus = false;
 							}
 							pause();
 							break;
 						case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
 							mMediaplayerHandler.removeMessages(FADEUP);
 							mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
 							break;
 						case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
 							Log.v(LOGTAG_SERVICE, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
 							if (isPlaying()) {
 								mPlayer.setVolume((float) Math.pow(10.0, -8 / 20.0));
 							}
 							break;
 						case AudioManager.AUDIOFOCUS_GAIN:
 							Log.v(LOGTAG_SERVICE, "AudioFocus: received AUDIOFOCUS_GAIN");
 							if (isPlaying() || mPausedByTransientLossOfFocus) {
 								mPausedByTransientLossOfFocus = false;
 								mCurrentVolume = 0f;
 								mPlayer.setVolume(mCurrentVolume);
 								play(); // also queues a fade-in
 							} else {
 								mMediaplayerHandler.removeMessages(FADEDOWN);
 								mMediaplayerHandler.sendEmptyMessage(FADEUP);
 							}
 							break;
 						default:
 							Log.e(LOGTAG_SERVICE, "Unknown audio focus change code");
 					}
 					break;
 
 				default:
 					break;
 			}
 		}
 	};
 
 	private BroadcastReceiver mExternalAudioDeviceStatusReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
 				int state = intent.getIntExtra("state", 0);
 				mExternalAudioDeviceConnected = (state == 1);
 			}
 		}
 	};
 
 	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			String action = intent.getAction();
 			String cmd = intent.getStringExtra(CMDNAME);
 			MusicUtils.debugLog("mIntentReceiver.onReceive " + action + " / " + cmd);
 			if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
 				next(true);
 			} else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
 				prev();
 			} else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
 				if (isPlaying()) {
 					pause();
 					mPausedByTransientLossOfFocus = false;
 				} else {
 					play();
 				}
 			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
 				pause();
 				mPausedByTransientLossOfFocus = false;
 			} else if (CMDSTOP.equals(cmd)) {
 				pause();
 				mPausedByTransientLossOfFocus = false;
 				seek(0);
 			} else if (CMDTOGGLEFAVORITE.equals(cmd)) {
 				if (!isFavorite()) {
 					addToFavorites();
 				} else {
 					removeFromFavorites();
 				}
 			}
 		}
 	};
 	private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
 
 		@Override
 		public void onAudioFocusChange(int focusChange) {
 
 			mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
 		}
 	};
 	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
 
 		@Override
 		public void onCallStateChanged(int state, String incomingNumber) {
 
 			switch (state) {
 				case TelephonyManager.CALL_STATE_RINGING:
 					Log.v(LOGTAG_SERVICE, "PhoneState: received CALL_STATE_RINGING");
 					if (isPlaying()) {
 						mPausedByTransientLossOfFocus = true;
 						pause();
 					}
 					break;
 
 				case TelephonyManager.CALL_STATE_OFFHOOK:
 					Log.v(LOGTAG_SERVICE, "PhoneState: received CALL_STATE_OFFHOOK");
 					mPausedByTransientLossOfFocus = false;
 					if (isPlaying()) {
 						pause();
 					}
 					break;
 			}
 		}
 	};
 
 	private static final char hexdigits[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
 			'a', 'b', 'c', 'd', 'e', 'f' };
 	private Handler mDelayedStopHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			// Check again to make sure nothing is playing right now
 			if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse
 					|| mMediaplayerHandler.hasMessages(TRACK_ENDED)) return;
 			// save the queue again, because it might have changed
 			// since the user exited the music app (because of
 			// party-shuffle or because the play-position changed)
 			saveQueue(true);
 			stopSelf(mServiceStartId);
 		}
 	};
 	private Handler mSleepTimerHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 
 			switch (msg.what) {
 				case START_SLEEP_TIMER:
 					mSleepTimerHandler.removeMessages(START_SLEEP_TIMER, null);
 					if (mGentleSleepTimer) {
 						if (isPlaying()) {
 							mSleepTimerTimedUp = true;
 						} else {
 							pause();
 							mNotificationManager.cancel(ID_NOTIFICATION_SLEEPTIMER);
 						}
 					} else {
 						pause();
 						mNotificationManager.cancel(ID_NOTIFICATION_SLEEPTIMER);
 					}
 					mStopTimestamp = 0;
 					break;
 				case STOP_SLEEP_TIMER:
 					mStopTimestamp = 0;
 					mSleepTimerHandler.removeMessages(START_SLEEP_TIMER, null);
 					mNotificationManager.cancel(ID_NOTIFICATION_SLEEPTIMER);
 					break;
 			}
 		}
 	};
 	private final IBinder mBinder = new ServiceStub(this);
 
 	private Resources mResources;
 
 	public MusicPlaybackService() {
 
 	}
 
 	public TrackInfo getTrackInfo() {
 		return mTrackInfo;
 	}
 
 	public void addToFavorites() {
 	}
 
 	public void addToFavorites(long id) {
 		MusicUtils.addToFavorites(this, id);
 		notifyChange(BROADCAST_FAVORITESTATE_CHANGED);
 	}
 
 	/**
 	 * Called when we receive a ACTION_MEDIA_EJECT notification.
 	 * 
 	 * @param storagePath
 	 *            path to mount point for the removed media
 	 */
 	public void closeExternalStorageFiles(String storagePath) {
 
 		stop(true);
 		notifyChange(BROADCAST_QUEUE_CHANGED);
 		notifyChange(BROADCAST_MEDIA_CHANGED);
 	}
 
 
 	/**
 	 * Returns the duration of the file in milliseconds. Currently this method
 	 * returns -1 for the duration of MIDI files.
 	 */
 	public long getDuration() {
 		if (mPlayer.isInitialized()) return mPlayer.getDuration();
 		return -1;
 	}
 
 	/**
 	 * Appends a list of tracks to the current playlist. If nothing is playing
 	 * currently, playback will be started at the first track. If the action is
 	 * NOW, playback will switch to the first of the new tracks immediately.
 	 * 
 	 * @param list
 	 *            The list of tracks to append.
 	 * @param action
 	 *            NOW, NEXT or LAST
 	 */
 	public void enqueue(long[] list, int action) {
 
 		synchronized (this) {
 			if (action == ACTION_NEXT && mPlayPos + 1 < mPlayListLen) {
 				addToPlayList(list, mPlayPos + 1);
 				notifyChange(BROADCAST_QUEUE_CHANGED);
 			} else {
 				// action == LAST || action == NOW || mPlayPos + 1 ==
 				// mPlayListLen
 				addToPlayList(list, Integer.MAX_VALUE);
 				notifyChange(BROADCAST_QUEUE_CHANGED);
 				if (action == ACTION_NOW) {
 					mPlayPos = mPlayListLen - list.length;
 					openCurrent();
 					play();
 					notifyChange(BROADCAST_MEDIA_CHANGED);
 					return;
 				}
 			}
 			if (mPlayPos < 0) {
 				mPlayPos = 0;
 				openCurrent();
 				play();
 				notifyChange(BROADCAST_MEDIA_CHANGED);
 			}
 		}
 	}
 
 	/**
 	 * Returns the audio session ID.
 	 */
 	public int getAudioSessionId() {
 
 		synchronized (this) {
 			return mPlayer.getAudioSessionId();
 		}
 	}
 
 	/**
 	 * Returns the current play list
 	 * 
 	 * @return An array of integers containing the IDs of the tracks in the play
 	 *         list
 	 */
 	 //FIXME
 	public long[] getQueue() {
 		synchronized (this) {
 			final int len = mPlayListLen;
 			final long[] list = new long[len];
 			for (int i = 0; i < len; i++) {
 				list[i] = mPlayList[i];
 			}
 			return list;
 		}
 	}
 
 	/**
 	 * Returns the position in the queue
 	 * 
 	 * @return the position in the queue
 	 */
 	public int getQueuePosition() {
 		synchronized (this) {
 			return mPlayPos;
 		}
 	}
 
 	public int getRepeatMode() {
 		return mRepeatMode;
 	}
 
 	public int getShuffleMode() {
 		return mShuffleMode;
 	}
 
 	public boolean isFavorite() {
 		return false;
 	}
 
 	public boolean isFavorite(long id) {
 		return MusicUtils.isFavorite(this, id);
 	}
 
 	/**
 	 * Returns whether something is currently playing
 	 * 
 	 * @return true if something is playing (or will be playing shortly, in case
 	 *         we're currently transitioning between tracks), false if not.
 	 */
 	public boolean isPlaying() {
 		return mIsSupposedToBePlaying;
 	}
 
 	/**
 	 * Moves the item at index1 to index2.
 	 * 
 	 * @param from
 	 * @param to
 	 */
 	public synchronized void moveQueueItem(int from, int to) {
 		if (from >= mPlayListLen) {
 			from = mPlayListLen - 1;
 		}
 		if (to >= mPlayListLen) {
 			to = mPlayListLen - 1;
 		}
 		if (from < to) {
 			final long tmp = mPlayList[from];
 			for (int i = from; i < to; i++) {
 				mPlayList[i] = mPlayList[i + 1];
 			}
 			mPlayList[to] = tmp;
 			if (mPlayPos == from) {
 				mPlayPos = to;
 			} else if (mPlayPos >= from && mPlayPos <= to) {
 				mPlayPos--;
 			}
 		} else if (to < from) {
 			long tmp = mPlayList[from];
 			for (int i = from; i > to; i--) {
 				mPlayList[i] = mPlayList[i - 1];
 			}
 			mPlayList[to] = tmp;
 			if (mPlayPos == from) {
 				mPlayPos = to;
 			} else if (mPlayPos >= to && mPlayPos <= from) {
 				mPlayPos++;
 			}
 		}
 		notifyChange(BROADCAST_QUEUE_CHANGED);
 	}
 
 	public void next(boolean force) {
 
 		synchronized (this) {
 			if (mSleepTimerTimedUp) {
 				pause();
 				mNotificationManager.cancel(ID_NOTIFICATION_SLEEPTIMER);
 				mSleepTimerTimedUp = false;
 				return;
 			}
 
 			if (mPlayListLen <= 0) {
 				Log.d(LOGTAG_SERVICE, "No play queue");
 				return;
 			}
 
 			if (mShuffleMode == SHUFFLE_MODE_ALL) {
 				if (mPlayPos >= 0) {
 					if (!mHistory.contains(mPlayPos)) {
 						mHistory.add(mPlayPos);
 					}
 				}
 
 				int numTracks = mPlayListLen;
 				int[] tracks = new int[numTracks];
 				for (int i = 0; i < numTracks; i++) {
 					tracks[i] = i;
 				}
 
 				int numHistory = mHistory.size();
 				int numUnplayed = numTracks;
 				for (int i = 0; i < numHistory; i++) {
 					int idx = mHistory.get(i).intValue();
 					if (idx < numTracks && tracks[idx] >= 0) {
 						numUnplayed--;
 						tracks[idx] = -1;
 					}
 				}
 
 				// 'numUnplayed' now indicates how many tracks have not yet
 				// been played, and 'tracks' contains the indices of those
 				// tracks.
 				if (numUnplayed <= 0) {
 					// everything's already been played
 					if (mRepeatMode == REPEAT_MODE_ALL || force) {
 						// pick from full set
 						numUnplayed = numTracks;
 						for (int i = 0; i < numTracks; i++) {
 							tracks[i] = i;
 						}
 					} else {
 						// all done
 						gotoIdleState();
 						if (mIsSupposedToBePlaying) {
 							mIsSupposedToBePlaying = false;
 							notifyChange(BROADCAST_PLAY_STATE_CHANGED);
 						}
 						return;
 					}
 				}
 				int skip = mShuffler.shuffle(numUnplayed);
 				int cnt = -1;
 				while (true) {
 					while (tracks[++cnt] < 0) {
 						;
 					}
 					skip--;
 					if (skip < 0) {
 						break;
 					}
 				}
 				mPlayPos = cnt;
 			} else {
 				if (mPlayPos >= mPlayListLen - 1) {
 					// we're at the end of the list
 					if (mRepeatMode == REPEAT_MODE_NONE && !force) {
 						// all done
 						gotoIdleState();
 						mIsSupposedToBePlaying = false;
 						notifyChange(BROADCAST_PLAY_STATE_CHANGED);
 						return;
 					} else if (mRepeatMode == REPEAT_MODE_ALL || force) {
 						mPlayPos = 0;
 					}
 				} else {
 					mPlayPos++;
 				}
 			}
 			saveBookmarkIfNeeded();
 			stop(false);
 			openCurrent();
 			play();
 			notifyChange(BROADCAST_MEDIA_CHANGED);
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 		mServiceInUse = true;
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		// Needs to be done in this thread, since otherwise
 		// ApplicationContext.getPowerManager() crashes.
 		mResources = getResources();
 		mPlayer = new MultiPlayer(this);
 		mResolver = getContentResolver();
 		mPlayer.setHandler(mMediaplayerHandler);
 
 		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
 		mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
 				MediaButtonIntentReceiver.class.getName()));
 
 		mPreferences = new PreferencesEditor(this);
 
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 		mCardId = MusicUtils.getCardId(this);
 
 		registerExternalStorageListener();
 		registerA2dpServiceListener();
 
 
 		reloadQueue();
 
 		IntentFilter commandFilter = new IntentFilter();
 		commandFilter.addAction(SERVICECMD);
 		commandFilter.addAction(TOGGLEPAUSE_ACTION);
 		commandFilter.addAction(PAUSE_ACTION);
 		commandFilter.addAction(NEXT_ACTION);
 		commandFilter.addAction(PREVIOUS_ACTION);
 		commandFilter.addAction(CYCLEREPEAT_ACTION);
 		commandFilter.addAction(TOGGLESHUFFLE_ACTION);
 		commandFilter.addAction(BROADCAST_PLAYSTATUS_REQUEST);
 		registerReceiver(mIntentReceiver, commandFilter);
 
 		registerReceiver(mExternalAudioDeviceStatusReceiver, new IntentFilter(
 				Intent.ACTION_HEADSET_PLUG));
 
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
 		mWakeLock.setReferenceCounted(false);
 
 		// If the service was idle, but got killed before it stopped itself, the
 		// system will relaunch it. Make sure it gets stopped again in that
 		// case.
 		Message msg = mDelayedStopHandler.obtainMessage();
 		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
 	}
 
 	@Override
 	public void onDestroy() {
 
 		// Check that we're not being destroyed while something is still
 		// playing.
 		if (isPlaying()) {
 			Log.e(LOGTAG_SERVICE, "Service being destroyed while still playing.");
 		}
 
 		mPlayer.release();
 		mPlayer = null;
 
 		mAudioManager.abandonAudioFocus(mAudioFocusListener);
 
 		final TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
 
 		// make sure there aren't any other messages coming
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 		mMediaplayerHandler.removeCallbacksAndMessages(null);
 
 		mTrackInfo = null;
 
 		unregisterReceiver(mIntentReceiver);
 		unregisterReceiver(mA2dpReceiver);
 		unregisterReceiver(mExternalAudioDeviceStatusReceiver);
 		if (mUnmountReceiver != null) {
 			unregisterReceiver(mUnmountReceiver);
 			mUnmountReceiver = null;
 		}
 		mWakeLock.release();
 		mNotificationManager.cancelAll();
 		super.onDestroy();
 	}
 
 	@Override
 	public void onRebind(Intent intent) {
 
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 		mServiceInUse = true;
 	}
 
 	/*
 	 * Desired behavior for prev/next/shuffle:
 	 * 
 	 * - NEXT will move to the next track in the list when not shuffling, and to
 	 * a track randomly picked from the not-yet-played tracks when shuffling. If
 	 * all tracks have already been played, pick from the full set, but avoid
 	 * picking the previously played track if possible. - when shuffling, PREV
 	 * will go to the previously played track. Hitting PREV again will go to the
 	 * track played before that, etc. When the start of the history has been
 	 * reached, PREV is a no-op. When not shuffling, PREV will go to the
 	 * sequentially previous track (the difference with the shuffle-case is
 	 * mainly that when not shuffling, the user can back up to tracks that are
 	 * not in the history).
 	 * 
 	 * Example: When playing an album with 10 tracks from the start, and
 	 * enabling shuffle while playing track 5, the remaining tracks (6-10) will
 	 * be shuffled, e.g. the final play order might be 1-2-3-4-5-8-10-6-9-7.
 	 * When hitting 'prev' 8 times while playing track 7 in this example, the
 	 * user will go to tracks 9-6-10-8-5-4-3-2. If the user then hits 'next', a
 	 * random track will be picked again. If at any time user disables shuffling
 	 * the next/previous track will be picked in sequential order again.
 	 */
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 
 		mServiceStartId = startId;
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 
 		if (intent != null) {
 			String action = intent.getAction();
 			String cmd = intent.getStringExtra("command");
 			MusicUtils.debugLog("onStartCommand " + action + " / " + cmd);
 
 			if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
 				next(true);
 			} else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
 				prev();
 			} else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
 				if (isPlaying()) {
 					pause();
 					mPausedByTransientLossOfFocus = false;
 				} else {
 					play();
 				}
 			} else if (CMDTOGGLEFAVORITE.equals(cmd)) {
 				if (!isFavorite()) {
 					addToFavorites();
 				} else {
 					removeFromFavorites();
 				}
 			} else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
 				pause();
 				mPausedByTransientLossOfFocus = false;
 			} else if (CMDSTOP.equals(cmd)) {
 				pause();
 				mPausedByTransientLossOfFocus = false;
 				seek(0);
 			} else if (BROADCAST_PLAYSTATUS_REQUEST.equals(action)) {
 				notifyChange(BROADCAST_PLAYSTATUS_RESPONSE);
 			}
 		}
 
 		// make sure the service will shut down on its own if it was
 		// just started but not bound to and nothing is playing
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 		Message msg = mDelayedStopHandler.obtainMessage();
 		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
 		return START_STICKY;
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 
 		mServiceInUse = false;
 
 		// Take a snapshot of the current playlist
 		saveQueue(true);
 
 		if (isPlaying() || mPausedByTransientLossOfFocus) // something is
 															// currently
 															// playing, or will
 															// be playing once
 			// an in-progress action requesting audio focus ends, so don't stop
 			// the service now.
 			return true;
 
 		// If there is a playlist but playback is paused, then wait a while
 		// before stopping the service, so that pause/resume isn't slow.
 		// Also delay stopping the service if we're transitioning between
 		// tracks.
 		if (mPlayListLen > 0 || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
 			Message msg = mDelayedStopHandler.obtainMessage();
 			mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
 			return true;
 		}
 
 		// No active playlist, OK to stop the service right now
 		stopSelf(mServiceStartId);
 		return true;
 	}
 
 	/**
 	 * Replaces the current playlist with a new list, and prepares for starting
 	 * playback at the specified position in the list, or a random position if
 	 * the specified position is 0.
 	 * 
 	 * @param list
 	 *            The new list of tracks.
 	 */
 	public void open(long[] list, int position) {
 
 		synchronized (this) {
 			final long oldId = mTrackInfo != null ? mTrackInfo.id : -1;
 			final int listlength = list.length;
 			boolean newlist = true;
 			if (mPlayListLen == listlength) {
 				// possible fast path: list might be the same
 				newlist = false;
 				for (int i = 0; i < listlength; i++) {
 					if (list[i] != mPlayList[i]) {
 						newlist = true;
 						break;
 					}
 				}
 			}
 			if (newlist) {
 				addToPlayList(list, -1);
 				notifyChange(BROADCAST_QUEUE_CHANGED);
 			}
 			if (position >= 0) {
 				mPlayPos = position;
 			} else {
 				mPlayPos = mShuffler.shuffle(mPlayListLen);
 			}
 			mHistory.clear();
 			saveBookmarkIfNeeded();
 			openCurrent();
 			final long currentId = mTrackInfo != null ? mTrackInfo.id : -1;
 			if (oldId != currentId) {
 				notifyChange(BROADCAST_MEDIA_CHANGED);
 			}
 		}
 	}
 
 	/**
 	 * Opens the specified file and readies it for playback.
 	 * 
 	 * @param path
 	 *            The full path of the file to be opened.
 	 */
 	public synchronized void openUri(final Uri uri) {
 		mTrackInfo = null;
 		if (uri == null) return;
 		final Cursor cur = mResolver.query(uri, CURSOR_COLUMNS, null, null, null);
 		if (cur == null) return;
 		if (cur.getCount() > 0) {
 			cur.moveToFirst();
 			mTrackInfo = new TrackInfo(cur);
 		}
 		cur.close();
 		if (mTrackInfo == null) return;
 		mPlayer.setDataSource(mTrackInfo.data);
 		if (!mPlayer.isInitialized()) {
 			stop(true);
 			if (mOpenFailedCounter++ < 10 && mPlayListLen > 1) {
 				// beware: this ends up being recursive because next() calls
 				// open() again.
 				next(false);
 			}
 			if (!mPlayer.isInitialized() && mOpenFailedCounter != 0) {
 				// need to make sure we only shows this once
 				mOpenFailedCounter = 0;
 				if (!mQuietMode) {
 					Toast.makeText(this, R.string.playback_failed, Toast.LENGTH_SHORT).show();
 				}
 				Log.d(LOGTAG_SERVICE, "Failed to open file for playback");
 			}
 		} else {
 			mOpenFailedCounter = 0;
 		}
 	}
 
 	/**
 	 * Pauses playback (call play() to resume)
 	 */
 	public void pause() {
 
 		synchronized (this) {
 			mMediaplayerHandler.removeMessages(FADEUP);
 			if (isPlaying()) {
 
 				mPlayer.pause();
 				gotoIdleState();
 				mIsSupposedToBePlaying = false;
 				notifyChange(BROADCAST_PLAY_STATE_CHANGED);
 				saveBookmarkIfNeeded();
 			}
 		}
 	}
 
 	/**
 	 * Starts playback of a previously opened file.
 	 */
 	public void play() {
 		final TrackInfo track = getTrackInfo();
 		if (track == null) return;
 
 		if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) return;
 
 		mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
 				AudioManager.AUDIOFOCUS_GAIN);
 		mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
 				MediaButtonIntentReceiver.class.getName()));
 
 		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
 
 		if (mPlayer.isInitialized()) {
 			// if we are at the end of the song, go to the next song first
 			long duration = mPlayer.getDuration();
 			if (mRepeatMode != REPEAT_MODE_CURRENT && duration > 2000
 					&& mPlayer.getPosition() >= duration - 2000) {
 				next(true);
 			}
 
 			mPlayer.start();
 
 			// make sure we fade in, in case a previous fadein was stopped
 			// because
 			// of another focus loss
 			mMediaplayerHandler.removeMessages(FADEDOWN);
 			mMediaplayerHandler.sendEmptyMessage(FADEUP);
 			
 			final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
 			final NotificationCompat.Style style = new NotificationCompat.BigPictureStyle();
 			builder.setSmallIcon(R.drawable.ic_stat_playback);
 			builder.setContentTitle(track.title);
 			if (!TrackInfo.isUnknownArtist(track)) {
 				builder.setContentText(track.artist);
 			} else if (!TrackInfo.isUnknownAlbum(track)) {
 				builder.setContentText(track.album);
 			} else {
 				builder.setContentText(getString(R.string.unknown_artist));
 			}
 			final AlbumInfo album = AlbumInfo.getAlbumInfo(this, track);
 			builder.setLargeIcon(getAlbumArtForNotification(album != null ? album.album_art : null));
 			builder.setStyle(style);
 			builder.setOngoing(true);
 			builder.setOnlyAlertOnce(true);
 			builder.setWhen(0);
 			builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(INTENT_PLAYBACK_VIEWER), 0));
 
			startForeground(ID_NOTIFICATION_PLAYBACK, builder.getNotification());
 
 			if (!mIsSupposedToBePlaying) {
 				mIsSupposedToBePlaying = true;
 				notifyChange(BROADCAST_PLAY_STATE_CHANGED);
 			}
 
 		} else if (mPlayListLen <= 0) {
 			// This is mostly so that if you press 'play' on a bluetooth headset
 			// without every having played anything before, it will still play
 			// something.
 			setShuffleMode(SHUFFLE_MODE_ALL);
 		}
 	}
 	
 	private Bitmap getAlbumArtForNotification(final String path) {
 		if (path == null) return null;
 		final BitmapFactory.Options opts = new BitmapFactory.Options();
 		opts.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(path, opts);
 		final float bmp_size = Math.max(opts.outWidth, opts.outHeight);
 		if (bmp_size == 0) return null;
 		final int width = mResources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
 		final int height = mResources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
 		opts.inSampleSize = (int) Math.floor(bmp_size / Math.max(width, height));
 		opts.inJustDecodeBounds = false;
 		final Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
 		if (bitmap == null) return null;
 		final Bitmap scaled;
 		if (bitmap.getWidth() > bitmap.getHeight()) {
 			scaled = Bitmap.createScaledBitmap(bitmap, height * bitmap.getWidth() / bitmap.getHeight(), height, true);
 		} else {
 			scaled = Bitmap.createScaledBitmap(bitmap, width, width * bitmap.getHeight() / bitmap.getWidth(), true);
 		}
 		bitmap.recycle();
 		final int x = Math.max(0, (scaled.getWidth() - width) / 2), y = Math.max(0, (scaled.getHeight() - height) / 2);
 		final Bitmap cropped = Bitmap.createBitmap(scaled, x, y, Math.min(width, scaled.getWidth()), Math.min(height, scaled.getHeight()));
 		scaled.recycle();
 		return cropped;
 	}
 
 	/**
 	 * Returns the current playback position in milliseconds
 	 */
 	public long getPosition() {
 		if (mPlayer.isInitialized()) return mPlayer.getPosition();
 		return -1;
 	}
 
 	public void prev() {
 
 		synchronized (this) {
 			if (mShuffleMode == SHUFFLE_MODE_ALL) {
 				// go to previously-played track and remove it from the history
 				int histsize = mHistory.size();
 				if (histsize == 0) // prev is a no-op
 					return;
 				Integer pos = mHistory.remove(histsize - 1);
 				mPlayPos = pos.intValue();
 			} else {
 				if (mPlayPos > 0) {
 					mPlayPos--;
 				} else {
 					mPlayPos = mPlayListLen - 1;
 				}
 			}
 			saveBookmarkIfNeeded();
 			stop(false);
 			openCurrent();
 			play();
 			notifyChange(BROADCAST_MEDIA_CHANGED);
 		}
 	}
 
 	public void registerA2dpServiceListener() {
 
 		mA2dpReceiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 
 				String action = intent.getAction();
 				if (BROADCAST_PLAYSTATUS_REQUEST.equals(action)) {
 					notifyChange(BROADCAST_PLAYSTATUS_RESPONSE);
 				}
 			}
 		};
 		final IntentFilter filter = new IntentFilter();
 		filter.addAction(BROADCAST_PLAYSTATUS_REQUEST);
 		registerReceiver(mA2dpReceiver, filter);
 	}
 
 	/**
 	 * Registers an intent to listen for ACTION_MEDIA_EJECT notifications. The
 	 * intent will call closeExternalStorageFiles() if the external media is
 	 * going to be ejected, so applications can clean up any files they have
 	 * open.
 	 */
 	public void registerExternalStorageListener() {
 		if (mUnmountReceiver == null) {
 			mUnmountReceiver = new BroadcastReceiver() {
 
 				@Override
 				public void onReceive(Context context, Intent intent) {
 
 					String action = intent.getAction();
 					if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
 						saveQueue(true);
 						mQueueIsSaveable = false;
 						closeExternalStorageFiles(intent.getData().getPath());
 					} else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
 						mCardId = MusicUtils.getCardId(context);
 						reloadQueue();
 						mQueueIsSaveable = true;
 						notifyChange(BROADCAST_QUEUE_CHANGED);
 						notifyChange(BROADCAST_MEDIA_CHANGED);
 					}
 				}
 			};
 			final IntentFilter filter = new IntentFilter();
 			filter.addAction(Intent.ACTION_MEDIA_EJECT);
 			filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
 			filter.addDataScheme(ContentResolver.SCHEME_FILE);
 			registerReceiver(mUnmountReceiver, filter);
 		}
 	}
 
 	public void removeFromFavorites() {
 	}
 
 	public void removeFromFavorites(long id) {
 		MusicUtils.removeFromFavorites(this, id);
 		notifyChange(BROADCAST_FAVORITESTATE_CHANGED);
 	}
 
 	/**
 	 * Removes all instances of the track with the given id from the playlist.
 	 * 
 	 * @param id
 	 *            The id to be removed
 	 * @return how many instances of the track were removed
 	 */
 	public int removeTrack(long id) {
 
 		int numremoved = 0;
 		synchronized (this) {
 			for (int i = 0; i < mPlayListLen; i++) {
 				if (mPlayList[i] == id) {
 					numremoved += removeTracksInternal(i, i);
 					i--;
 				}
 			}
 		}
 		if (numremoved > 0) {
 			notifyChange(BROADCAST_QUEUE_CHANGED);
 		}
 		return numremoved;
 	}
 
 	/**
 	 * Removes the range of tracks specified from the play list. If a file
 	 * within the range is the file currently being played, playback will move
 	 * to the next file after the range.
 	 * 
 	 * @param first
 	 *            The first file to be removed
 	 * @param last
 	 *            The last file to be removed
 	 * @return the number of tracks deleted
 	 */
 	public int removeTracks(int first, int last) {
 
 		int numremoved = removeTracksInternal(first, last);
 		if (numremoved > 0) {
 			notifyChange(BROADCAST_QUEUE_CHANGED);
 		}
 		return numremoved;
 	}
 
 	/**
 	 * Seeks to the position specified.
 	 * 
 	 * @param pos
 	 *            The position to seek to, in milliseconds
 	 */
 	public long seek(long pos) {
 
 		if (mPlayer.isInitialized()) {
 			if (pos < 0) {
 				pos = 0;
 			}
 			if (pos > mPlayer.getDuration()) {
 				pos = mPlayer.getDuration();
 			}
 			long result = mPlayer.seek(pos);
 			notifyChange(BROADCAST_SEEK_CHANGED);
 			return result;
 		}
 		return 0;
 	}
 
 	/**
 	 * Starts playing the track at the given id in the queue.
 	 * 
 	 * @param id
 	 *            The id in the queue of the track that will be played.
 	 */
 	public void setQueueId(long id) {
 		int pos = -1;
 
 		for (int i = 0; i < mPlayList.length; i++) {
 			if (id == mPlayList[i]) {
 				pos = i;
 			}
 		}
 		if (pos < 0) return;
 
 		setQueuePosition(pos);
 	}
 
 	/**
 	 * Starts playing the track at the given position in the queue.
 	 * 
 	 * @param pos
 	 *            The position in the queue of the track that will be played.
 	 */
 	public synchronized void setQueuePosition(final int pos) {
 		stop(false);
 		mPlayPos = pos;
 		openCurrent();
 		play();
 		notifyChange(BROADCAST_MEDIA_CHANGED);
 	}
 
 	public synchronized void setRepeatMode(int mode) {
 		if (mRepeatMode == mode) return;
 		if (mShuffleMode == SHUFFLE_MODE_ALL && mode == REPEAT_MODE_CURRENT) {
 			setShuffleMode(SHUFFLE_MODE_NONE);
 		}
 		mRepeatMode = mode;
 		notifyChange(BROADCAST_REPEAT_MODE_CHANGED);
 		saveQueue(false);
 		mPreferences.setIntPref(PREFERENCE_KEY_REPEAT_MODE, mRepeatMode);
 	}
 
 	public synchronized void setShuffleMode(int shufflemode) {
 		if (mShuffleMode == shufflemode || mPlayListLen < 1) return;
 		if (mRepeatMode == REPEAT_MODE_CURRENT) {
 			mRepeatMode = REPEAT_MODE_NONE;
 		}
 		mShuffleMode = shufflemode;
 		notifyChange(BROADCAST_SHUFFLE_MODE_CHANGED);
 		saveQueue(false);
 		mPreferences.setIntPref(PREFERENCE_KEY_SHUFFLE_MODE, mShuffleMode);
 	}
 
 	/**
 	 * Stops playback.
 	 */
 	public void stop() {
 		stop(true);
 	}
 
 	public void toggleFavorite() {
 		if (!isFavorite()) {
 			addToFavorites();
 		} else {
 			removeFromFavorites();
 		}
 	}
 
 	public boolean togglePause() {
 		if (isPlaying()) {
 			pause();
 		} else {
 			play();
 		}
 		return isPlaying();
 	}
 
 	// insert the list of songs at the specified position in the playlist
 	private void addToPlayList(long[] list, int position) {
 		final int addlen = list.length;
 		if (position < 0) { // overwrite
 			mPlayListLen = 0;
 			position = 0;
 		}
 		ensurePlayListCapacity(mPlayListLen + addlen);
 		if (position > mPlayListLen) {
 			position = mPlayListLen;
 		}
 
 		// move part of list after insertion point
 		final int tailsize = mPlayListLen - position;
 		for (int i = tailsize; i > 0; i--) {
 			mPlayList[position + i] = mPlayList[position + i - addlen];
 		}
 
 		// copy list into playlist
 		for (int i = 0; i < addlen; i++) {
 			mPlayList[position + i] = list[i];
 		}
 		mPlayListLen += addlen;
 		if (mPlayListLen == 0) {
 			mTrackInfo = null;
 			notifyChange(BROADCAST_MEDIA_CHANGED);
 		}
 	}
 
 	private void ensurePlayListCapacity(int size) {
 
 		if (mPlayList == null || size > mPlayList.length) {
 			// reallocate at 2x requested size so we don't
 			// need to grow and copy the array for every
 			// insert
 			long[] newlist = new long[size * 2];
 			int len = mPlayList != null ? mPlayList.length : mPlayListLen;
 			for (int i = 0; i < len; i++) {
 				newlist[i] = mPlayList[i];
 			}
 			mPlayList = newlist;
 		}
 	}
 
 	private long getBookmark() {
 		return 0;
 	}
 
 	private long getSleepTimerRemained() {
 
 		Calendar now = Calendar.getInstance();
 		long mCurrentTimestamp = now.getTimeInMillis();
 		if (mStopTimestamp != 0)
 			return mStopTimestamp - mCurrentTimestamp;
 		else
 			return 0;
 	}
 
 	private void gotoIdleState() {
 
 		mDelayedStopHandler.removeCallbacksAndMessages(null);
 		Message msg = mDelayedStopHandler.obtainMessage();
 		mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
 		mNotificationManager.cancel(ID_NOTIFICATION_PLAYBACK);
 	}
 
 	private boolean isPodcast() {
 		return false;
 	}
 
 	/**
 	 * Notify the change-receivers that something has changed. The intent that
 	 * is sent contains the following data for the currently playing track: "id"
 	 * - Integer: the database row ID "artist" - String: the name of the artist
 	 * "album_artist" - String: the name of the album artist "album" - String:
 	 * the name of the album "track" - String: the name of the track The intent
 	 * has an action that is one of "org.mariotaku.harmony.metachanged"
 	 * "org.mariotaku.harmony.queuechanged", "org.mariotaku.harmony.playbackcomplete"
 	 * "org.mariotaku.harmony.playstatechanged" respectively indicating that a new track has
 	 * started playing, that the playback queue has changed, that playback has
 	 * stopped because the last file in the list has been played, or that the
 	 * play-state changed (paused/resumed).
 	 */
 	private void notifyChange(String action) {
 		final Intent intent = new Intent(action);
 		final TrackInfo track = getTrackInfo();
 		if (track != null) {
 		intent.putExtra(BROADCAST_KEY_ID, track.id);
 		intent.putExtra(BROADCAST_KEY_ARTIST, track.artist);
 		intent.putExtra(BROADCAST_KEY_ALBUM, track.album);
 		intent.putExtra(BROADCAST_KEY_TRACK, track.title);
 		intent.putExtra(BROADCAST_KEY_SONGID, track.id);
 		intent.putExtra(BROADCAST_KEY_ALBUMID, track.album_id);
 		intent.putExtra(BROADCAST_KEY_PLAYING, isPlaying());
 		intent.putExtra(BROADCAST_KEY_DURATION, getDuration());
 		intent.putExtra(BROADCAST_KEY_POSITION, getPosition());
 		intent.putExtra(BROADCAST_KEY_SHUFFLEMODE, getShuffleMode());
 		intent.putExtra(BROADCAST_KEY_REPEATMODE, getRepeatMode());
 		}
 		if (mPlayList != null) {
 			intent.putExtra(BROADCAST_KEY_LISTSIZE, Long.valueOf(mPlayList.length));
 		} else {
 			intent.putExtra(BROADCAST_KEY_LISTSIZE, Long.valueOf(mPlayListLen));
 		}
 		sendBroadcast(intent);
 
 		if (BROADCAST_MEDIA_CHANGED.equals(action)) {
 			if (isPlaying()) {
 				sendScrobbleBroadcast(SCROBBLE_PLAYSTATE_START);
 			} else {
 				sendScrobbleBroadcast(SCROBBLE_PLAYSTATE_COMPLETE);
 			}
 		} else if (BROADCAST_PLAY_STATE_CHANGED.equals(action)) {
 			if (isPlaying()) {
 				sendScrobbleBroadcast(SCROBBLE_PLAYSTATE_RESUME);
 			} else {
 				sendScrobbleBroadcast(SCROBBLE_PLAYSTATE_PAUSE);
 			}
 		}
 		if (BROADCAST_QUEUE_CHANGED.equals(action)) {
 			saveQueue(true);
 		} else {
 			saveQueue(false);
 		}
 	}
 
 	private synchronized void openCurrent() {
 		mTrackInfo = null;
 		stop(false);
 		if (mPlayListLen == 0) return;
 		final long id = mPlayList[mPlayPos];
 		openUri(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));
 		// go to bookmark if needed
 		if (isPodcast()) {
 			final long bookmark = getBookmark();
 			// Start playing a little bit before the bookmark,
 			// so it's easier to get back in to the narrative.
 			seek(bookmark - 5000);
 		}
 	}
 
 	private void reloadQueue() {
 		String q = null;
 
 		final int id = mPreferences.getIntState(STATE_KEY_CARDID, mCardId);
 		if (id == mCardId) {
 			// Only restore the saved playlist if the card is still
 			// the same one as when the playlist was saved
 			q = mPreferences.getStringState(STATE_KEY_QUEUE, "");
 		}
 		int qlen = q != null ? q.length() : 0;
 		if (qlen > 1) {
 			// Log.i("@@@@ service", "loaded queue: " + q);
 			int plen = 0;
 			int n = 0;
 			int shift = 0;
 			for (int i = 0; i < qlen; i++) {
 				char c = q.charAt(i);
 				if (c == ';') {
 					ensurePlayListCapacity(plen + 1);
 					mPlayList[plen] = n;
 					plen++;
 					n = 0;
 					shift = 0;
 				} else {
 					if (c >= '0' && c <= '9') {
 						n += c - '0' << shift;
 					} else if (c >= 'a' && c <= 'f') {
 						n += 10 + c - 'a' << shift;
 					} else {
 						// bogus playlist data
 						plen = 0;
 						break;
 					}
 					shift += 4;
 				}
 			}
 			mPlayListLen = plen;
 
 			int pos = mPreferences.getIntState(STATE_KEY_CURRPOS, 0);
 			if (pos < 0 || pos >= mPlayListLen) {
 				// The saved playlist is bogus, discard it
 				mPlayListLen = 0;
 				return;
 			}
 			mPlayPos = pos;
 
 			// When reloadQueue is called in response to a card-insertion,
 			// we might not be able to query the media provider right away.
 			// To deal with this, try querying for the current file, and if
 			// that fails, wait a while and try again. If that too fails,
 			// assume there is a problem and don't restore the state.
 			Cursor crsr = MusicUtils.query(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
 					new String[] { "_id" }, "_id=" + mPlayList[mPlayPos], null, null);
 			if (crsr == null || crsr.getCount() == 0) {
 				// wait a bit and try again
 				SystemClock.sleep(3000);
 				crsr = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
 						CURSOR_COLUMNS, "_id=" + mPlayList[mPlayPos], null, null);
 			}
 			if (crsr != null) {
 				crsr.close();
 			}
 
 			mOpenFailedCounter = 20;
 			mQuietMode = true;
 			openCurrent();
 			mQuietMode = false;
 			if (!mPlayer.isInitialized()) {
 				// couldn't restore the saved state
 				mPlayListLen = 0;
 				return;
 			}
 
 			long seekpos = mPreferences.getLongState(STATE_KEY_SEEKPOS, 0);
 			seek(seekpos >= 0 && seekpos < getDuration() ? seekpos : 0);
 			Log.d(LOGTAG_SERVICE, "restored queue, currently at position " + getPosition() + "/"
 					+ getDuration() + " (requested " + seekpos + ")");
 
 			setRepeatMode(mPreferences.getIntPref(PREFERENCE_KEY_REPEAT_MODE, REPEAT_MODE_NONE));
 			setShuffleMode(mPreferences.getIntPref(PREFERENCE_KEY_SHUFFLE_MODE, SHUFFLE_MODE_NONE));
 			if (mShuffleMode != SHUFFLE_MODE_NONE) {
 				// in shuffle mode we need to restore the history too
 				q = mPreferences.getStringState(STATE_KEY_HISTORY, "");
 				qlen = q != null ? q.length() : 0;
 				if (qlen > 1) {
 					plen = 0;
 					n = 0;
 					shift = 0;
 					mHistory.clear();
 					for (int i = 0; i < qlen; i++) {
 						char c = q.charAt(i);
 						if (c == ';') {
 							if (n >= mPlayListLen) {
 								// bogus history data
 								mHistory.clear();
 								break;
 							}
 							if (!mHistory.contains(mPlayPos)) {
 								mHistory.add(mPlayPos);
 							}
 							n = 0;
 							shift = 0;
 						} else {
 							if (c >= '0' && c <= '9') {
 								n += c - '0' << shift;
 							} else if (c >= 'a' && c <= 'f') {
 								n += 10 + c - 'a' << shift;
 							} else {
 								// bogus history data
 								mHistory.clear();
 								break;
 							}
 							shift += 4;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private int removeTracksInternal(int first, int last) {
 
 		synchronized (this) {
 			if (last < first) return 0;
 			if (first < 0) {
 				first = 0;
 			}
 			if (last >= mPlayListLen) {
 				last = mPlayListLen - 1;
 			}
 
 			boolean gotonext = false;
 			if (first <= mPlayPos && mPlayPos <= last) {
 				mPlayPos = first;
 				gotonext = true;
 			} else if (mPlayPos > last) {
 				mPlayPos -= last - first + 1;
 			}
 			int num = mPlayListLen - last - 1;
 			for (int i = 0; i < num; i++) {
 				mPlayList[first + i] = mPlayList[last + 1 + i];
 			}
 			mPlayListLen -= last - first + 1;
 
 			if (gotonext) {
 				if (mPlayListLen == 0) {
 					stop(true);
 					mPlayPos = -1;
 				} else {
 					if (mPlayPos >= mPlayListLen) {
 						mPlayPos = 0;
 					}
 					final boolean wasPlaying = isPlaying();
 					stop(false);
 					openCurrent();
 					if (wasPlaying) {
 						play();
 					}
 				}
 				notifyChange(BROADCAST_MEDIA_CHANGED);
 			}
 			return last - first + 1;
 		}
 	}
 
 	private void saveBookmarkIfNeeded() {
 
 		try {
 			if (isPodcast()) {
 				long pos = getPosition();
 				long bookmark = getBookmark();
 				long duration = getDuration();
 				if (pos < bookmark && pos + 10000 > bookmark || pos > bookmark
 						&& pos - 10000 < bookmark) // The existing bookmark is
 													// close to the current
 					// position, so don't update it.
 					return;
 				if (pos < 15000 || pos + 10000 > duration) {
 					// if we're near the start or end, clear the bookmark
 					pos = 0;
 				}
 
 				// write 'pos' to the bookmark field
 				ContentValues values = new ContentValues();
 				values.put(MediaStore.Audio.Media.BOOKMARK, pos);
 				Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
 						mTrackInfo.id);
 				mResolver.update(uri, values, null, null);
 			}
 		} catch (SQLiteException ex) {
 		}
 	}
 
 	private void saveQueue(boolean full) {
 
 		if (!mQueueIsSaveable) return;
 
 		// long start = System.currentTimeMillis();
 		if (full) {
 			StringBuilder q = new StringBuilder();
 
 			// The current playlist is saved as a list of "reverse hexadecimal"
 			// numbers, which we can generate faster than normal decimal or
 			// hexadecimal numbers, which in turn allows us to save the playlist
 			// more often without worrying too much about performance.
 			// (saving the full state takes about 40 ms under no-load conditions
 			// on the phone)
 			int len = mPlayListLen;
 			for (int i = 0; i < len; i++) {
 				long n = mPlayList[i];
 				if (n < 0) {
 					continue;
 				} else if (n == 0) {
 					q.append("0;");
 				} else {
 					while (n != 0) {
 						int digit = (int) (n & 0xf);
 						n >>>= 4;
 						q.append(hexdigits[digit]);
 					}
 					q.append(";");
 				}
 			}
 			// Log.i("@@@@ service", "created queue string in " +
 			// (System.currentTimeMillis() - start) + " ms");
 			mPreferences.setStringState(STATE_KEY_QUEUE, q.toString());
 			mPreferences.setIntState(STATE_KEY_CARDID, mCardId);
 			if (mShuffleMode != SHUFFLE_MODE_NONE) {
 				// In shuffle mode we need to save the history too
 				len = mHistory.size();
 				q.setLength(0);
 				for (int i = 0; i < len; i++) {
 					int n = mHistory.get(i);
 					if (n == 0) {
 						q.append("0;");
 					} else {
 						while (n != 0) {
 							int digit = n & 0xf;
 							n >>>= 4;
 							q.append(hexdigits[digit]);
 						}
 						q.append(";");
 					}
 				}
 				mPreferences.setStringState(STATE_KEY_HISTORY, q.toString());
 			}
 		}
 		mPreferences.setIntState(STATE_KEY_CURRPOS, mPlayPos);
 		if (mPlayer.isInitialized()) {
 			mPreferences.setLongState(STATE_KEY_SEEKPOS, mPlayer.getPosition());
 		}
 		// Log.i("@@@@ service", "saved state in " + (System.currentTimeMillis()
 		// - start) + " ms");
 	}
 
 	private void sendScrobbleBroadcast(int state) {
 		final boolean enabled = mPreferences.getBooleanPref(KEY_ENABLE_SCROBBLING, false);
 		final TrackInfo track = getTrackInfo();
 		if (!enabled || track == null) return;
 		final Intent i = new Intent(SCROBBLE_SLS_API);
 		i.putExtra(BROADCAST_KEY_APP_NAME, getString(R.string.app_name));
 		i.putExtra(BROADCAST_KEY_APP_PACKAGE, getPackageName());
 		i.putExtra(BROADCAST_KEY_STATE, state);
 		i.putExtra(BROADCAST_KEY_ARTIST, track.artist);
 		i.putExtra(BROADCAST_KEY_ALBUM, track.album);
 		i.putExtra(BROADCAST_KEY_TRACK, track.title);
 		i.putExtra(BROADCAST_KEY_DURATION, (int) (getDuration() / 1000));
 		sendBroadcast(i);
 	}
 
 	private void startSleepTimer(long milliseconds, boolean gentle) {
 
 		Calendar now = Calendar.getInstance();
 		mCurrentTimestamp = now.getTimeInMillis();
 		mStopTimestamp = mCurrentTimestamp + milliseconds;
 
 		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL
 				| DateUtils.FORMAT_CAP_AMPM;
 
 		format_flags |= DateUtils.FORMAT_SHOW_TIME;
 		String time = DateUtils.formatDateTime(this, mStopTimestamp, format_flags);
 
 		CharSequence contentTitle = getString(R.string.sleep_timer_enabled);
 		CharSequence contentText = getString(R.string.notification_sleep_timer, time);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
 		Notification notification = new Notification(R.drawable.ic_stat_playback, null, 0);
 		notification.flags = Notification.FLAG_ONGOING_EVENT;
 		notification.icon = R.drawable.ic_stat_sleeptimer;
 		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
 
 		mGentleSleepTimer = gentle;
 		mNotificationManager.notify(ID_NOTIFICATION_SLEEPTIMER, notification);
 		mSleepTimerHandler.sendEmptyMessageDelayed(START_SLEEP_TIMER, milliseconds);
 		final int nmin = (int) milliseconds / 60 / 1000;
 		Toast.makeText(this, getResources().getQuantityString(R.plurals.NNNminutes_notif, nmin, nmin), Toast.LENGTH_SHORT).show();
 	}
 
 	private void stop(boolean remove_status_icon) {
 
 		if (mPlayer.isInitialized()) {
 			mPlayer.stop();
 		}
 		mTrackInfo = null;
 		if (remove_status_icon) {
 			gotoIdleState();
 		} else {
 			mNotificationManager.cancel(ID_NOTIFICATION_PLAYBACK);
 		}
 		if (remove_status_icon) {
 			mIsSupposedToBePlaying = false;
 		}
 	}
 
 	private void stopSleepTimer() {
 
 		mSleepTimerHandler.sendEmptyMessage(STOP_SLEEP_TIMER);
 		Toast.makeText(this, R.string.sleep_timer_disabled, Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * Provides a unified interface for dealing with midi files and other media
 	 * files.
 	 */
 	private class MultiPlayer {
 
 		private final Context mContext;
 		private MediaPlayer mMediaPlayer;
 		private Handler mHandler;
 		private boolean mIsInitialized = false;
 
 		private MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
 
 			@Override
 			public void onCompletion(MediaPlayer mp) {
 
 				// Acquire a temporary wakelock, since when we return from
 				// this callback the MediaPlayer will release its wakelock
 				// and allow the device to go to sleep.
 				// This temporary wakelock is released when the RELEASE_WAKELOCK
 				// message is processed, but just in case, put a timeout on it.
 				mWakeLock.acquire(30000);
 				mHandler.sendEmptyMessage(TRACK_ENDED);
 				mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
 			}
 		};
 
 		private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
 
 			@Override
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 
 				switch (what) {
 					case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
 						mIsInitialized = false;
 						release();
 						// Creating a new MediaPlayer and settings its wakemode
 						// does not require the media service, so it's OK to do
 						// this now, while the service is still being restarted
 						initMediaPlayer();
 						mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
 						return true;
 					default:
 						Log.d("MultiPlayer", "Error: " + what + "," + extra);
 						break;
 				}
 				return false;
 			}
 		};
 
 		public MultiPlayer(Context context) {
 			mContext = context;
 			initMediaPlayer();
 		}
 		
 		void initMediaPlayer() {
 			mIsInitialized = false;
 			mMediaPlayer = new MediaPlayer();
 			mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
 			final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
 			intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
 			intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
 			intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
 			mContext.sendBroadcast(intent);
 		}
 
 		public long getDuration() {
 			return mMediaPlayer.getDuration();
 		}
 
 		public int getAudioSessionId() {
 			return mMediaPlayer.getAudioSessionId();
 		}
 
 		public boolean isInitialized() {
 			return mIsInitialized;
 		}
 
 		public boolean isPlaying() {
 			return mMediaPlayer.isPlaying();
 		}
 
 		public void pause() {
 			mMediaPlayer.pause();
 		}
 
 		public long getPosition() {
 			return mMediaPlayer.getCurrentPosition();
 		}
 
 		/**
 		 * You CANNOT use this player anymore after calling release()
 		 */
 		public void release() {
 			final Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
 			intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
 			intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
 			intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
 			mContext.sendBroadcast(intent);
 			mIsInitialized = false;
 			if (isInitialized() || isPlaying()) {
 				stop();
 			}
 			mMediaPlayer.release();
 		}
 
 		public long seek(long whereto) {
 			mMediaPlayer.seekTo((int) whereto);
 			return whereto;
 		}
 
 		public void setDataSource(String path) {
 			try {
 				mMediaPlayer.reset();
 				mMediaPlayer.setOnPreparedListener(null);
 				if (path.startsWith("content://")) {
 					mMediaPlayer.setDataSource(mContext, Uri.parse(path));
 				} else {
 					mMediaPlayer.setDataSource(path);
 				}
 				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 				mMediaPlayer.prepare();
 			} catch (IOException ex) {
 				mIsInitialized = false;
 				return;
 			} catch (IllegalArgumentException ex) {
 				mIsInitialized = false;
 				return;
 			}
 			mMediaPlayer.setOnCompletionListener(listener);
 			mMediaPlayer.setOnErrorListener(errorListener);
 			mIsInitialized = true;
 		}
 
 		public void setHandler(Handler handler) {
 			mHandler = handler;
 		}
 
 		public void setVolume(float vol) {
 			mMediaPlayer.setVolume(vol, vol);
 			mCurrentVolume = vol;
 		}
 
 		public void start() {
 			MusicUtils.debugLog(new Exception("MultiPlayer.start called"));
 			mMediaPlayer.start();
 		}
 
 		public void stop() {
 			mMediaPlayer.reset();
 			mIsInitialized = false;
 		}
 
 	}
 
 	// A simple variation of Random that makes sure that the
 	// value it returns is not equal to the value it returned
 	// previously, unless the interval is 1.
 	private class Shuffler {
 
 		private int mPrevious;
 		private Random mRandom = new Random();
 
 		public int shuffle(int interval) {
 
 			int ret;
 			long ret_id;
 			do {
 				ret = mRandom.nextInt(interval);
 				ret_id = mPlayList[ret];
 			} while (ret == mPrevious && interval > 1 || !isFavorite(ret_id)
 					&& mHistory.contains(ret_id));
 
 			mPrevious = ret;
 			return ret;
 		}
 	}
 
 	/*
 	 * By making this a static class with a WeakReference to the Service, we
 	 * ensure that the Service can be GCd even when the system process still has
 	 * a remote reference to the stub.
 	 */
 	private static class ServiceStub extends IMusicPlaybackService.Stub {
 
 		private final WeakReference<MusicPlaybackService> mService;
 
 		private ServiceStub(final MusicPlaybackService service) {
 			mService = new WeakReference<MusicPlaybackService>(service);
 		}
 
 		@Override
 		public long getDuration() {
 			return mService.get().getDuration();
 		}
 
 		@Override
 		public void enqueue(long[] list, int action) {
 			mService.get().enqueue(list, action);
 		}
 
 		@Override
 		public int getAudioSessionId() {
 			return mService.get().getAudioSessionId();
 		}
 
 		@Override
 		public TrackInfo getTrackInfo() {
 			return mService.get().getTrackInfo();
 		}
 
 		@Override
 		public long[] getQueue() {
 			return mService.get().getQueue();
 		}
 
 		@Override
 		public int getQueuePosition() {
 			return mService.get().getQueuePosition();
 		}
 
 		@Override
 		public int getRepeatMode() {
 			return mService.get().getRepeatMode();
 		}
 
 		@Override
 		public int getShuffleMode() {
 			return mService.get().getShuffleMode();
 		}
 
 		@Override
 		public long getSleepTimerRemained() {
 			return mService.get().getSleepTimerRemained();
 		}
 
 		@Override
 		public boolean isPlaying() {
 			return mService.get().isPlaying();
 		}
 
 		@Override
 		public void moveQueueItem(int from, int to) {
 			mService.get().moveQueueItem(from, to);
 		}
 
 		@Override
 		public void next() {
 			mService.get().next(true);
 		}
 
 		@Override
 		public void open(long[] list, int position) {
 			mService.get().open(list, position);
 		}
 
 		@Override
 		public void pause() {
 			mService.get().pause();
 		}
 
 		@Override
 		public void play() {
 			mService.get().play();
 		}
 
 		@Override
 		public long getPosition() {
 			return mService.get().getPosition();
 		}
 
 		@Override
 		public void prev() {
 			mService.get().prev();
 		}
 
 		@Override
 		public int removeTrack(long id) {
 			return mService.get().removeTrack(id);
 		}
 
 		@Override
 		public int removeTracks(int first, int last) {
 			return mService.get().removeTracks(first, last);
 		}
 
 		@Override
 		public long seek(long pos) {
 			return mService.get().seek(pos);
 		}
 
 		@Override
 		public void setQueueId(long id) {
 			mService.get().setQueueId(id);
 		}
 
 		@Override
 		public void setQueuePosition(int pos) {
 			mService.get().setQueuePosition(pos);
 		}
 
 		@Override
 		public void setRepeatMode(final int mode) {
 			mService.get().setRepeatMode(mode);
 		}
 
 		@Override
 		public void setShuffleMode(final int mode) {
 			mService.get().setShuffleMode(mode);
 		}
 
 		@Override
 		public void startSleepTimer(long milliseconds, boolean gentle) {
 			mService.get().startSleepTimer(milliseconds, gentle);
 		}
 
 		@Override
 		public void stop() {
 			mService.get().stop();
 		}
 
 		@Override
 		public void stopSleepTimer() {
 			mService.get().stopSleepTimer();
 		}
 
 		@Override
 		public boolean togglePause() {
 			return mService.get().togglePause();
 		}
 
 	}
 }
