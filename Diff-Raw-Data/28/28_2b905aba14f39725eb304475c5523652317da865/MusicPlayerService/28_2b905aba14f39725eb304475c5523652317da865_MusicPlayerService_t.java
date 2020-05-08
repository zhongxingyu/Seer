 package com.shingrus.myplayer;
 
 import java.io.IOException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.*;
 import android.media.MediaPlayer;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.MediaController.MediaPlayerControl;
 import android.media.*;
 
 public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
 
 	enum NotificationStatus {
 		Stopped, Playing, Paused
 	}
 
 	private static final int NOTIFICATION_ID = 11;
 	MediaPlayer mPlayer;
 	TrackList trackList;
 	String currentTitle;
 	private final IBinder mBinder = new LocalBinder();
 	boolean isPaused = false;
 	boolean isPausedDurinngCall = false;
 	NotificationManager nm;
 	TelephonyManager tm;
 	PhoneStateListener mPhoneListener;
 	Notification notification;
 	String currentStatusDesc;
 	BroadcastReceiver audioReceiver;
 	MyPlayerPreferences mpf;
 	private PlayingEventsListener eventsListener = null;
 
 	// private Handler progressHandler = new Handler();
 
 	// Runnable notifyAboutProgressJob = new Runnable() {
 	// public void run() {
 	// PlayingEventsListener listener = MusicPlayerService.this.eventsListener;
 	// if (listener != null) {
 	// if (mPlayer != null && mPlayer.isPlaying()) {
 	// double curPos = mPlayer.getCurrentPosition();
 	// double duration = mPlayer.getDuration();
 	//
 	// int position = (int) (curPos / duration * 100);
 	// listener.onPlayedPositionProgress(position);
 	// // progressHandler.postDelayed(this, UPDATE_PLAYING_STATUS_MS);
 	// }
 	// else {
 	// listener.onPlayedPositionProgress(0);
 	// }
 	// }
 	// }
 	// };
 
 	public class LocalBinder extends Binder {
 		MusicPlayerService getService() {
 			return MusicPlayerService.this;
 		}
 	}
 
 	/**
 	 * Receiver for AudioManager.ACTION_AUDIO_BECOMING_NOISY it pauses if
 	 * headphones disable
 	 * 
 	 * @author shingrus
 	 */
 	public class AudioBroacastReciever extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (mpf.doPauseOnLoud() && intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
 				pause();
 			}
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		mPlayer = new MediaPlayer();
 		currentTitle = "";
 		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		trackList = TrackList.getInstance();
 		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		notification = new Notification(R.drawable.ringtone, "", System.currentTimeMillis());
 		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
 		updateNotification(NotificationStatus.Stopped);
 		audioReceiver = new AudioBroacastReciever();
 		mpf = MyPlayerPreferences.getInstance(null);
 		mPhoneListener = new PhoneStateListener() {
 
 			@Override
 			public void onCallStateChanged(int state, String incomingNumber) {
 				if (mpf.doPauseOnCall()) {
 					switch (state) {
 					case TelephonyManager.CALL_STATE_IDLE:
 						if (isPausedDurinngCall)
 							playPaused();
 						break;
 					case TelephonyManager.CALL_STATE_OFFHOOK:
 						if (!isPaused) {
 							pause();
 							isPausedDurinngCall = true;
 						}
 						break;
 					case TelephonyManager.CALL_STATE_RINGING:
 						if (!isPaused) {
 							pause();
 							isPausedDurinngCall = true;
 						}
 						break;
 					}
 				}
 			}
 
 		};
 		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
 		registerReceiver(audioReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
 		super.onCreate();
 	}
 
 	private final void updateNotification(NotificationStatus nStatus) {
 		CharSequence nTitle = "";
 		switch (nStatus) {
 		case Paused:
 			nTitle = getText(R.string.NotificationTitle_Paused);
			if (eventsListener != null)
				eventsListener.onPause();
 			break;
 		case Playing:
 			nTitle = getText(R.string.NotificationTitle_Playing);
 			trackList.notifyPlayStarted();
 			if (eventsListener != null)
 				eventsListener.onPlay();
 			break;
 		case Stopped:
 			nTitle = getText(R.string.NotificationTitle_Stopped);
 			trackList.notifyPlayStopped();
 			if (eventsListener != null)
 				eventsListener.onStop();
 			break;
 		}
 
 		// Intent i = new Intent(this, MyPlayerActivity.class);
 		Intent i = new Intent(this, LauncherActivity.class);
 		// i.setFlags(Intent.FLAG_ACTIVITY_SIN
 		// GLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
 		notification.setLatestEventInfo(this, "MyPlayer - " + nTitle, currentTitle, pi);
 		nm.notify(NOTIFICATION_ID, notification);
 		switch (nStatus) {
 		case Paused:
 		case Playing:
 			// TODO remove this strange notify throw tracklist scheme
 			trackList.notifyPlayStarted();
 			if (this.eventsListener != null) {
 				eventsListener.onChangePlayingItem(trackList.getIteratePosition());
 			}
 			break;
 		case Stopped:
 			trackList.notifyPlayStopped();
 			break;
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.d("shingrus", "Destroy: MusicPlayerService");
 		stopForeground(true);
 		nm.cancelAll();
 		if (mPlayer != null) {
 			if (mPlayer.isPlaying())
 				mPlayer.stop();
 			mPlayer.release();
 		}
 		if (tm != null)
 			tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
 		unregisterReceiver(audioReceiver);
 		super.onDestroy();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		nm.cancel(NOTIFICATION_ID);
 		return Service.START_NOT_STICKY;
 	}
 
 	/**
 	 * Method from OnPreparedListener
 	 */
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		mp.start();
 		isPaused = false;
 		isPausedDurinngCall = false;
 		updateNotification(NotificationStatus.Playing);
 
 	}
 
 	/**
 	 * Method from OnErrorListener
 	 */
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		Log.i("shingrus", "MP error: " + what);
 		updateNotification(NotificationStatus.Stopped);
 		return false;
 	}
 
 	private void playMusic(MusicTrack mt) {
 		if (mt != null && mt.filename.length() > 0) {
 			mPlayer.reset();
 			updateNotification(NotificationStatus.Stopped);
 			isPaused = false;
 			isPausedDurinngCall = false;
 			try {
 				mPlayer.setDataSource(mt.filename);
 				currentTitle = mt.getTitle();
 				mPlayer.setOnPreparedListener(this);
 				mPlayer.setOnCompletionListener(this);
 				mPlayer.prepareAsync();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else if (mt == null)
 			Log.d("shingrus", "MusicPlayerService: mt is null");
 	}
 
 	/**
 	 * Method from OnCompletionListener
 	 */
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		playNext();
 	}
 
 	// my methods
 
 	public void startPlayFrom(int position) {
 		playMusic(trackList.startIterateFrom(position));
 
 	}
 
 	public void playNext() {
 		MusicTrack mt = trackList.getNextTrack();
 		playMusic(mt);
 
 	}
 
 	public void playPrevious() {
 		MusicTrack mt = trackList.getPreviuosTrack();
 		playMusic(mt);
 
 	}
 
 	private void pause() {
 		if (mPlayer.isPlaying()) {
 			mPlayer.pause();
 			updateNotification(NotificationStatus.Paused);
 			isPaused = true;
 		}
 	}
 
 	private void playPaused() {
 		if (isPaused) {
 			mPlayer.start();
 			updateNotification(NotificationStatus.Playing);
 			isPaused = false;
 			isPausedDurinngCall = false;
 
 		}
 	}
 
 	public void playPause() {
 		if (mPlayer.isPlaying()) {
 			pause();
 		} else
 			playPaused();
 	}
 
 	public void stopMusic() {
 		mPlayer.stop();
 		updateNotification(NotificationStatus.Stopped);
 		isPaused = false;
 		isPausedDurinngCall = false;
 	}
 
 	public void setEventsListener(PlayingEventsListener listener) {
 		this.eventsListener = listener;
 		// progressHandler.postDelayed(notifyAboutProgressJob,
 		// UPDATE_PLAYING_STATUS_MS);
 	}
 
 	public void unsetEventsListener() {
 		this.eventsListener = null;
 	}
 
 	public void setPosition(int percent) {
 		if (mPlayer !=null && mPlayer.isPlaying()) {
 			double mult = (double)percent / 100;
 			int msec = (int) (mPlayer.getDuration()*mult);
 			Log.d("shingrus", "New position: "+ msec);
 			mPlayer.seekTo(msec);
 		}
 	}
 	public int getCurrentPosition() {
 		int result = 0;
		if (mPlayer != null && (isPaused ||  mPlayer.isPlaying())) {
 			double currentPosition = mPlayer.getCurrentPosition();
 			double duration = mPlayer.getDuration();
 			if (duration != 0)
				result = (int) (currentPosition / duration * 100)+1;
 		}
 		return result;
 
 	}
 
 	public boolean isPlaying() {
 		if (mPlayer != null)
 			return mPlayer.isPlaying();
 		return false;
 	}
 }
