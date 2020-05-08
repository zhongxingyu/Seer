 package com.codingspezis.android.metalonly.player.stream;
 
 import android.content.*;
import android.net.wifi.*;
 import android.os.*;
 
 import com.spoledge.aacdecoder.*;
 
 /**
  * 
  * stream player that is using opencore port aacdecoder-android
  * 
  */
 public class StreamPlayerOpencore implements AudioStream {
 
 	private OpencorePlayer ocPlayer;
 	private OnStreamListener streamListener;
 	boolean shouldPlay = false;
 	private String url;
 
 	PowerManager.WakeLock wakeLock;
	WifiManager.WifiLock  wifiLock;
	
 	/**
 	 * constructor
 	 * 
 	 * @param url
 	 *            stream URL
 	 */
 	public StreamPlayerOpencore(Context context) {
 		PowerManager pm = (PowerManager) context
 				.getSystemService(Context.POWER_SERVICE);
 		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
 				"WAKE_LOCK_STREAM_DECODER");
 		createPlayer();
 	}
 
 	private void createPlayer() {
 		if (ocPlayer != null) {
 			ocPlayer.stop();
 		}
 		ocPlayer = new OpencorePlayer(this, callback); // playerCallback is
 														// defined bellow
 		ocPlayer.setMetadataEnabled(true);
 	}
 
 	/**
 	 * setter for url
 	 * 
 	 * @param url
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	@Override
 	public void startPlaying() {
 		stopPlaying();
 		shouldPlay = true;
 		ocPlayer.playAsync(url);
 	}
 
 	@Override
 	public void stopPlaying() {
 		shouldPlay = false;
 		ocPlayer.stop();
 	}
 
 	@Override
 	public void setOnStreamListener(OnStreamListener streamListener) {
 		this.streamListener = streamListener;
 	}
 
 	@Override
 	public boolean isPlaying() {
 		return ocPlayer.isPlaying();
 	}
 
 	/**
 	 * this is the callback object for instances of AACPlayerModified
 	 */
 	private final PlayerCallback callback = new PlayerCallback() {
 		@Override
 		public void playerStarted() {
 			if (shouldPlay) {
 				if (streamListener != null) {
 					streamListener.streamConnected();
 				}
 			} else {
 				ocPlayer.stop();
 			}
 		}
 
 		@Override
 		public void playerMetadata(String arg0, String arg1) {
 			if (arg0 != null && arg0.equals("StreamTitle")) {
 				if (streamListener != null) {
 					streamListener.metadataReceived(arg1);
 				}
 			}
 		}
 
 		@Override
 		public void playerPCMFeedBuffer(boolean arg0, int arg1, int arg2) {
 		}
 
 		@Override
 		public void playerStopped(int arg0) {
 			// if(shouldPlay)
 			// ocPlayer.playAsync(url);
 		}
 
 		@Override
 		public void playerException(Throwable arg0) {
 			createPlayer();
 			if (streamListener != null) {
 				streamListener.errorOccurred(
 						"AAC+ Decoder Error " + arg0.getMessage(), false);
 			}
 		}
 	};
 
 }
