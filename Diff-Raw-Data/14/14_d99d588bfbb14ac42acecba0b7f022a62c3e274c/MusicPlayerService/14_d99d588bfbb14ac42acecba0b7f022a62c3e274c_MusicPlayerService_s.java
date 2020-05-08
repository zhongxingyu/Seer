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
 
 	private static final int NOTIFICATION_ID = 100500;
 	MediaPlayer mp;
 	TrackList trackList;
 	// MusicTrack currentTrack;
 	String currentTitle;
 	private final IBinder mBinder = new LocalBinder();
 	boolean isPaused = false;
 	NotificationManager nm;
 	TelephonyManager tm;
 	Notification notification;
 	String currentStatusDesc;
 	BroadcastReceiver audioReceiver = new AudioBroacastReciever();
 
 	// private Handler updatesHandler;
 
 	public class LocalBinder extends Binder {
 		MusicPlayerService getService() {
 			return MusicPlayerService.this;
 		}
 	}
 
 	/**
 	 * Reciever for AudioManager.ACTION_AUDIO_BECOMING_NOISY
 	 * it pauses if headphones disable
 	 * @author shingrus
 	 */
 	public class AudioBroacastReciever extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
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
 		mp = new MediaPlayer();
 		currentTitle = "";
 		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		trackList = TrackList.getInstance();
 		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		notification = new Notification(R.drawable.ringtone, "", System.currentTimeMillis());
 		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
 		updateNotification(NotificationStatus.Stopped);
		
		tm.listen(new PhoneStateListener() {
 
 			@Override
 			public void onCallStateChanged(int state, String incomingNumber) {
 				switch (state) {
 				case TelephonyManager.CALL_STATE_IDLE:
 					playPaused();
 					break;
 				case TelephonyManager.CALL_STATE_OFFHOOK:
 					pause();
 					break;
 				case TelephonyManager.CALL_STATE_RINGING:
 					pause();
 					break;
 				}
 			}
 
		}, PhoneStateListener.LISTEN_CALL_STATE);
 		registerReceiver(audioReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
 		super.onCreate();
 	}
 
 	private final void updateNotification(NotificationStatus nStatus) {
 		CharSequence nTitle = "";
 		switch (nStatus) {
 		case Paused:
 			nTitle = getText(R.string.NotificationTitle_Paused);
 			break;
 		case Playing:
 			nTitle = getText(R.string.NotificationTitle_Playing);
 			trackList.notifyPlayStarted();
 			break;
 		case Stopped:
 			nTitle = getText(R.string.NotificationTitle_Stopped);
 			trackList.notifyPlayStopped();
 			break;
 		}
 
 		Intent i = new Intent(this, MyPlayerActivity.class);
 		// i.setFlags(Intent.FLAG_ACTIVITY_SIN
 		// GLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
 		notification.setLatestEventInfo(this, "MyPlayer - " + nTitle, currentTitle, pi);
 		nm.notify(NOTIFICATION_ID, notification);
 		switch (nStatus) {
 		case Paused:
 		case Playing:
 			trackList.notifyPlayStarted();
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
 		if (mp != null) {
 			if (mp.isPlaying())
 				mp.stop();
 			mp.release();
 		}
 		if (tm!=null) 
			tm.listen(null, PhoneStateListener.LISTEN_NONE);
 		unregisterReceiver(audioReceiver);
 
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
 			mp.reset();
 			updateNotification(NotificationStatus.Stopped);
 			isPaused = false;
 			try {
 				mp.setDataSource(mt.filename);
 				currentTitle = mt.getTitle();
 				mp.setOnPreparedListener(this);
 				mp.setOnCompletionListener(this);
 				mp.prepareAsync();
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
 		if (mp.isPlaying()) {
 			mp.pause();
 			updateNotification(NotificationStatus.Paused);
 			isPaused = true;
 		}
 	}
 
 	private void playPaused() {
 		if (isPaused) {
 			mp.start();
 			updateNotification(NotificationStatus.Playing);
 			isPaused = false;
 		}
 	}
 
 	public void playPause() {
 		if (mp.isPlaying()) {
 			pause();
 		} else
 			playPaused();
 	}
 
 	public void stopMusic() {
 		mp.stop();
 		updateNotification(NotificationStatus.Stopped);
 		isPaused = false;
 	}
 
 }
