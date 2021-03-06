 package de.podfetcher.service;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.R;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.AudioManager.OnAudioFocusChangeListener;
 import android.media.MediaPlayer;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.SurfaceHolder;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.IBinder;
 
 import de.podfetcher.PodcastApp;
 import de.podfetcher.activity.MediaplayerActivity;
 import de.podfetcher.feed.FeedMedia;
 import de.podfetcher.feed.Feed;
 import de.podfetcher.feed.FeedManager;
 
 /** Controls the MediaPlayer that plays a FeedMedia-file */
 public class PlaybackService extends Service {
 	/** Logging tag */
 	private static final String TAG = "PlaybackService";
 
 	/** Contains the id of the media that was played last. */
 	public static final String PREF_LAST_PLAYED_ID = "de.podfetcher.preferences.lastPlayedId";
 	/** Contains the feed id of the last played item. */
 	public static final String PREF_LAST_PLAYED_FEED_ID = "de.podfetcher.preferences.lastPlayedFeedId";
 	/** True if last played media was streamed. */
 	public static final String PREF_LAST_IS_STREAM = "de.podfetcher.preferences.lastIsStream";
 
 	/** Contains the id of the FeedMedia object. */
 	public static final String EXTRA_MEDIA_ID = "extra.de.podfetcher.service.mediaId";
 	/** Contains the id of the Feed object of the FeedMedia. */
 	public static final String EXTRA_FEED_ID = "extra.de.podfetcher.service.feedId";
 	/** True if media should be streamed. */
 	public static final String EXTRA_SHOULD_STREAM = "extra.de.podfetcher.service.shouldStream";
 	/**
 	 * True if playback should be started immediately after media has been
 	 * prepared.
 	 */
 	public static final String EXTRA_START_WHEN_PREPARED = "extra.de.podfetcher.service.startWhenPrepared";
 
 	public static final String ACTION_PLAYER_STATUS_CHANGED = "action.de.podfetcher.service.playerStatusChanged";
 	
 	public static final String ACTION_PLAYER_NOTIFICATION = "action.de.podfetcher.service.playerNotification";
 	public static final String EXTRA_NOTIFICATION_CODE = "extra.de.podfetcher.service.notificationCode";
 	public static final String EXTRA_NOTIFICATION_TYPE = "extra.de.podfetcher.service.notificationType";
 	
 	public static final int NOTIFICATION_TYPE_ERROR = 0;
 	public static final int NOTIFICATION_TYPE_INFO = 1;
 	public static final int NOTIFICATION_TYPE_BUFFER_UPDATE = 2;
 
 
 	/** Is true if service is running. */
 	public static boolean isRunning = false;
 
 	private static final int NOTIFICATION_ID = 1;
 	private NotificationCompat.Builder notificationBuilder;
 
 	private AudioManager audioManager;
 	private ComponentName mediaButtonReceiver;
 
 	private MediaPlayer player;
 
 	private FeedMedia media;
 	private Feed feed;
 	/** True if media should be streamed (Extracted from Intent Extra) . */
 	private boolean shouldStream;
 	private boolean startWhenPrepared;
 	private boolean playingVideo;
 	private FeedManager manager;
 	private PlayerStatus status;
 	private PositionSaver positionSaver;
 
 	private PlayerStatus statusBeforeSeek;
 
 	private final IBinder mBinder = new LocalBinder();
 
 	public class LocalBinder extends Binder {
 		public PlaybackService getService() {
 			return PlaybackService.this;
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		isRunning = true;
 		status = PlayerStatus.STOPPED;
 		Log.d(TAG, "Service created.");
 		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		manager = FeedManager.getInstance();
 		player = new MediaPlayer();
 		player.setOnPreparedListener(preparedListener);
 		player.setOnCompletionListener(completionListener);
 		player.setOnSeekCompleteListener(onSeekCompleteListener);
 		player.setOnErrorListener(onErrorListener);
 		player.setOnBufferingUpdateListener(onBufferingUpdateListener);
 		mediaButtonReceiver = new ComponentName(getPackageName(),
 				MediaButtonReceiver.class.getName());
 		audioManager.registerMediaButtonEventReceiver(mediaButtonReceiver);
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		isRunning = false;
 		Log.d(TAG, "Service is about to be destroyed");
 		audioManager.unregisterMediaButtonEventReceiver(mediaButtonReceiver);
 		audioManager.abandonAudioFocus(audioFocusChangeListener);
 		player.release();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	private final OnAudioFocusChangeListener audioFocusChangeListener = new OnAudioFocusChangeListener() {
 
 		@Override
 		public void onAudioFocusChange(int focusChange) {
 			switch (focusChange) {
 			case AudioManager.AUDIOFOCUS_LOSS:
 				Log.d(TAG, "Lost audio focus");
 				pause();
 				stopSelf();
 				break;
 			case AudioManager.AUDIOFOCUS_GAIN:
 				Log.d(TAG, "Gained audio focus");
 				play();
 				break;
 			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
 				Log.d(TAG, "Lost audio focus temporarily. Ducking...");
 				audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
 						AudioManager.ADJUST_LOWER, 0);
 				break;
 			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
 				Log.d(TAG, "Lost audio focus temporarily. Pausing...");
 				pause();
 			}
 		}
 	};
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		int keycode = intent.getIntExtra(MediaButtonReceiver.EXTRA_KEYCODE, -1);
 		if (keycode != -1) {
 			Log.d(TAG, "Received media button event");
 			handleKeycode(keycode);
 		} else {
 
 			long mediaId = intent.getLongExtra(EXTRA_MEDIA_ID, -1);
 			long feedId = intent.getLongExtra(EXTRA_FEED_ID, -1);
 			boolean playbackType = intent.getBooleanExtra(EXTRA_SHOULD_STREAM,
 					true);
 			if (mediaId == -1 || feedId == -1) {
 				Log.e(TAG,
 						"Media ID or Feed ID wasn't provided to the Service.");
 				if (media == null || feed == null) {
 					stopSelf();
 				}
 				// Intent values appear to be valid
 				// check if already playing and playbackType is the same
 			} else if (media == null || mediaId != media.getId()
 					|| playbackType != shouldStream) {
 				pause();
 				player.reset();
 				if (media == null || mediaId != media.getId()) {
 					feed = manager.getFeed(feedId);
 					media = manager.getFeedMedia(mediaId, feed);
 				}
 
 				if (media != null) {
 					shouldStream = playbackType;
 					startWhenPrepared = intent.getBooleanExtra(
 							EXTRA_START_WHEN_PREPARED, false);
 					setupMediaplayer();
 
 				} else {
 					Log.e(TAG, "Media is null");
 					stopSelf();
 				}
 
 			} else if (media != null) {
 				if (status == PlayerStatus.PAUSED) {
 					play();
 				}
 
 			} else {
 				Log.w(TAG, "Something went wrong. Shutting down...");
 				stopSelf();
 			}
 		}
 		return Service.START_NOT_STICKY;
 	}
 
 	/** Handles media button events */
 	private void handleKeycode(int keycode) {
 		switch (keycode) {
 		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
 			if (status == PlayerStatus.PLAYING) {
 				pause();
 			} else if (status == PlayerStatus.PAUSED) {
 				play();
 			}
 			break;
 		case KeyEvent.KEYCODE_MEDIA_PLAY:
 			if (status == PlayerStatus.PAUSED) {
 				play();
 			}
 			break;
 		case KeyEvent.KEYCODE_MEDIA_PAUSE:
 			if (status == PlayerStatus.PLAYING) {
 				pause();
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Called by a mediaplayer Activity as soon as it has prepared its
 	 * mediaplayer.
 	 */
 	public void setVideoSurface(SurfaceHolder sh) {
 		Log.d(TAG, "Setting display");
 		player.setDisplay(null);
 		player.setDisplay(sh);
 		if (status == PlayerStatus.STOPPED
 				|| status == PlayerStatus.AWAITING_VIDEO_SURFACE) {
 			try {
 				if (shouldStream) {
 					player.setDataSource(media.getDownload_url());
 					setStatus(PlayerStatus.PREPARING);
 					player.prepareAsync();
 				} else {
 					player.setDataSource(media.getFile_url());
 					setStatus(PlayerStatus.PREPARING);
 					player.prepare();
 				}
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/** Called when the surface holder of the mediaplayer has to be changed. */
 	public void resetVideoSurface() {
 		positionSaver.cancel(true);
 		player.setDisplay(null);
 		player.reset();
 		player.release();
 		player = new MediaPlayer();
 		player.setOnPreparedListener(preparedListener);
 		player.setOnCompletionListener(completionListener);
 		player.setOnSeekCompleteListener(onSeekCompleteListener);
 		player.setOnErrorListener(onErrorListener);
 		player.setOnBufferingUpdateListener(onBufferingUpdateListener);
 		status = PlayerStatus.STOPPED;
 		setupMediaplayer();
 	}
 
 	/** Called after service has extracted the media it is supposed to play. */
 	private void setupMediaplayer() {
 		try {
 			if (media.getMime_type().startsWith("audio")) {
 				playingVideo = false;
 				if (shouldStream) {
 					player.setDataSource(media.getDownload_url());
 					setStatus(PlayerStatus.PREPARING);
 					player.prepareAsync();
 				} else {
 					player.setDataSource(media.getFile_url());
 					setStatus(PlayerStatus.PREPARING);
 					player.prepare();
 				}
 			} else if (media.getMime_type().startsWith("video")) {
 				playingVideo = true;
 				setStatus(PlayerStatus.AWAITING_VIDEO_SURFACE);
 				player.setScreenOnWhilePlaying(true);
 			}
 
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void setupPositionSaver() {
 		if (positionSaver == null) {
 			positionSaver = new PositionSaver() {
 				@Override
 				protected void onCancelled(Void result) {
 					super.onCancelled(result);
 					positionSaver = null;
 				}
 
 				@Override
 				protected void onPostExecute(Void result) {
 					super.onPostExecute(result);
 					positionSaver = null;
 				}
 			};
 			positionSaver.execute();
 		}
 	}
 
 	private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
 		@Override
 		public void onPrepared(MediaPlayer mp) {
 			Log.d(TAG, "Resource prepared");
 			mp.seekTo(media.getPosition());
 			setStatus(PlayerStatus.PREPARED);
 			if (startWhenPrepared) {
 				play();
 			}
 		}
 	};
 
 	private MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
 
 		@Override
 		public void onSeekComplete(MediaPlayer mp) {
 			if (status == PlayerStatus.SEEKING) {
 				setStatus(statusBeforeSeek);
 			}
 
 		}
 	};
 	
 	private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
 		private static final String TAG = "PlaybackService.onErrorListener";
 		@Override
 		public boolean onError(MediaPlayer mp, int what, int extra) {
 			Log.w(TAG, "An error has occured: " + what);
 			if (mp.isPlaying()) {
 				pause();
 			}
 			sendNotificationBroadcast(NOTIFICATION_TYPE_ERROR, what);
 			stopSelf();
 			return true;
 		}
 	};
 
 	private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
 
 		@Override
 		public void onCompletion(MediaPlayer mp) {
 			Log.d(TAG, "Playback completed");
 			positionSaver.cancel(true);
 			media.setPosition(0);
 			manager.markItemRead(PlaybackService.this, media.getItem(), true);
 			if (manager.isInQueue(media.getItem())) {
 				manager.removeQueueItem(PlaybackService.this, media.getItem());
 			}
 			manager.setFeedMedia(PlaybackService.this, media);
 			setStatus(PlayerStatus.STOPPED);
 			stopForeground(true);
 
 		}
 	};
 	
 	private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
 		
 		@Override
 		public void onBufferingUpdate(MediaPlayer mp, int percent) {
 			sendNotificationBroadcast(NOTIFICATION_TYPE_BUFFER_UPDATE, percent);
 			
 		}
 	};
 
 	public void pause() {
 		if (player.isPlaying()) {
 			Log.d(TAG, "Pausing playback.");
 			player.pause();
 			if (positionSaver != null) {
 				positionSaver.cancel(true);
 			}
 			saveCurrentPosition();
 			setStatus(PlayerStatus.PAUSED);
 			stopForeground(true);
 		}
 	}
 	
 	/** Pauses playback and destroys service. Recommended for video playback. */
 	public void stop() {
 		pause();
 		stopSelf();
 	}
 
 	public void play() {
 		if (status == PlayerStatus.PAUSED || status == PlayerStatus.PREPARED
 				|| status == PlayerStatus.STOPPED) {
 			int focusGained = audioManager.requestAudioFocus(
 					audioFocusChangeListener, AudioManager.STREAM_MUSIC,
 					AudioManager.AUDIOFOCUS_GAIN);
 
 			if (focusGained == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
 				Log.d(TAG, "Audiofocus successfully requested");
 				Log.d(TAG, "Resuming/Starting playback");
 				SharedPreferences.Editor editor = getApplicationContext()
 						.getSharedPreferences(PodcastApp.PREF_NAME, 0).edit();
 				editor.putLong(PREF_LAST_PLAYED_ID, media.getId());
 				editor.putLong(PREF_LAST_PLAYED_FEED_ID, feed.getId());
 				editor.putBoolean(PREF_LAST_IS_STREAM, shouldStream);
 				editor.commit();
 
 				player.start();
 				player.seekTo((int) media.getPosition());
 				setStatus(PlayerStatus.PLAYING);
 				setupPositionSaver();
 				setupNotification();
 			} else {
 				Log.d(TAG, "Failed to request Audiofocus");
 			}
 		}
 	}
 
 	private void setStatus(PlayerStatus newStatus) {
 		Log.d(TAG, "Setting status to " + newStatus);
 		status = newStatus;
 		sendBroadcast(new Intent(ACTION_PLAYER_STATUS_CHANGED));
 	}
 	
 	private void sendNotificationBroadcast(int type, int code) {
 		Intent intent = new Intent(ACTION_PLAYER_NOTIFICATION);
 		intent.putExtra(EXTRA_NOTIFICATION_TYPE, type);
 		intent.putExtra(EXTRA_NOTIFICATION_CODE, code);
 		sendBroadcast(intent);
 	}
 
 	/** Prepares notification and starts the service in the foreground. */
 	private void setupNotification() {
 		PendingIntent pIntent = PendingIntent.getActivity(this, 0, new Intent(
 				this, MediaplayerActivity.class),
 				PendingIntent.FLAG_UPDATE_CURRENT);
 
 		Bitmap icon = BitmapFactory.decodeResource(null,
 				R.drawable.stat_notify_sdcard);
 		notificationBuilder = new NotificationCompat.Builder(this)
 				.setContentTitle("Mediaplayer Service")
 				.setContentText("Click here for more info").setOngoing(true)
 				.setContentIntent(pIntent).setLargeIcon(icon)
 				.setSmallIcon(R.drawable.stat_notify_sdcard);
 
 		startForeground(NOTIFICATION_ID, notificationBuilder.getNotification());
 		Log.d(TAG, "Notification set up");
 	}
 
 	/**
 	 * Seek a specific position from the current position
 	 * 
 	 * @param delta
 	 *            offset from current position (positive or negative)
 	 * */
 	public void seekDelta(int delta) {
 		seek(player.getCurrentPosition() + delta);
 	}
 
 	public void seek(int i) {
 		Log.d(TAG, "Seeking position " + i);
 		if (shouldStream) {
 			statusBeforeSeek = status;
 			setStatus(PlayerStatus.SEEKING);
 		}
 		player.seekTo(i);
 		saveCurrentPosition();
 	}
 
 	/** Saves the current position of the media file to the DB */
 	private synchronized void saveCurrentPosition() {
 		Log.d(TAG, "Saving current position to " + player.getCurrentPosition());
 		media.setPosition(player.getCurrentPosition());
 		manager.setFeedMedia(this, media);
 	}
 
 	public PlayerStatus getStatus() {
 		return status;
 	}
 
 	public FeedMedia getMedia() {
 		return media;
 	}
 
 	public MediaPlayer getPlayer() {
 		return player;
 	}
 
 	/** Periodically saves the position of the media file */
 	class PositionSaver extends AsyncTask<Void, Void, Void> {
 		private static final int WAITING_INTERVALL = 5000;
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			while (!isCancelled() && player.isPlaying()) {
 				try {
 					Thread.sleep(WAITING_INTERVALL);
 				} catch (InterruptedException e) {
 					Log.d(TAG,
 							"Thread was interrupted while waiting. Finishing now...");
 					return null;
 				}
				saveCurrentPosition();
 			}
 			return null;
 		}
 
 	}
 
 	public boolean isPlayingVideo() {
 		return playingVideo;
 	}
 
 	public boolean isShouldStream() {
 		return shouldStream;
 	}
 
 }
