 package com.codingspezis.android.metalonly.player.stream;
 
 import android.content.*;
 import android.media.*;
 import android.media.AudioManager.OnAudioFocusChangeListener;
 
 /**
  * 
  * broadcast receiver that handles messages for this service
  * 
  */
 public class PlayerBCReceiver extends BroadcastReceiver {
 
 	/**
 	 * 
 	 */
 	private final PlayerService playerService;
 
 	AudioManager audioManager;
 
 	private OnAudioFocusChangeListener afChangeListener;
 
 	/**
 	 * @param playerService
 	 */
 	PlayerBCReceiver(PlayerService playerService) {
 		this.playerService = playerService;
 		audioManager = (AudioManager) playerService
 				.getSystemService(Context.AUDIO_SERVICE);
 
 		afChangeListener = new OnAudioFocusChangeListener() {
 
 			@Override
 			public void onAudioFocusChange(int focusChange) {
 				if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
 					stop();
 				}
 				else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
 					play();
 				}
 				else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
 					audioManager.abandonAudioFocus(this);
 					stop();
 				}
 			}
 		};
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		String action = intent.getAction();
 		if (action.equals(PlayerService.INTENT_PLAY)) {
 			play();
 			sendPlayerStatus();
 		}
 		else if (action.equals(PlayerService.INTENT_STOP)) {
 			stop();
 			sendPlayerStatus();
 		}
 		else if (action.equals(PlayerService.INTENT_STATUS_REQUEST)) {
 			sendPlayerStatus();
 		}
 		else if (action.equals(PlayerService.INTENT_EXIT)) {
 			exit();
 		}
 	}
 
 	void play() {
 		stop();
 
 		int result = audioManager.requestAudioFocus(afChangeListener,
 				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
 
 		if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result) {
 			playerService.instantiateSelectedPlayer();
 			playerService.streamPlaying = true;
 			playerService.audioStream
 					.setOnStreamListener(playerService.streamWatcher);
 
 			playerService.audioStream.startPlaying();
 		}
 		else {
 			stop();
 		}
 	}
 
 	void stop() {
 		if (playerService.audioStream != null) {
 			playerService.audioStream.stopPlaying();
 		}
 		playerService.clear();
 	}
 
 	private void sendPlayerStatus() {
 		playerService.sendPlayerStatus();
 	}
 
 	private void exit() {
 		if (!playerService.streamPlaying) {
 			playerService.stopSelf();
 		}
 	}
 
 }
