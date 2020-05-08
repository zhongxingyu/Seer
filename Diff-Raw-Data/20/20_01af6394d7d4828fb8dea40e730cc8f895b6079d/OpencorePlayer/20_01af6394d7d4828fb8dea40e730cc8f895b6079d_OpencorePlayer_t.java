 package com.codingspezis.android.metalonly.player.stream;
 
 import android.os.Process;
 import android.util.*;
 
 import com.spoledge.aacdecoder.*;
 
 /**
  * adds isPlaying functionality to MultiPlayer sets priority higher
  * 
  */
 class OpencorePlayer extends MultiPlayer {
 
 	/**
 	 * 
 	 */
 	private final StreamPlayerOpencore streamPlayerOpencore;
 
 	/**
 	 * same as super(cb)
 	 * 
 	 * @see AACPlaye(PlayerCallback cb)
 	 * @param cb
 	 *            the callback, can be null
 	 * @param streamPlayerOpencore
 	 *            TODO
 	 */
 	public OpencorePlayer(StreamPlayerOpencore streamPlayerOpencore,
 			PlayerCallback cb) {
 		super(cb);
 		this.streamPlayerOpencore = streamPlayerOpencore;
 	}
 
 	/**
 	 * is AACPlayer playing?
 	 * 
 	 * @return true if playing loop is still working - false otherwise
 	 */
 	public boolean isPlaying() {
 		return !stopped;
 	}
 
 	/**
 	 * difference to original method:
 	 * Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
 	 */
 	@Override
 	public void playAsync(final String url, final int expectedKBitSecRate) {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
 				OpencorePlayer.this.streamPlayerOpencore.wakeLock.acquire();
				OpencorePlayer.this.streamPlayerOpencore.wifiLock.acquire();
 				while (OpencorePlayer.this.streamPlayerOpencore.shouldPlay) {
 					try {
 						play(url, expectedKBitSecRate);
 					} catch (Exception e) {
 						// Log.e( LOG, "playAsync():", e);
 						if (e.getMessage() != null
 								&& e.getMessage().equals(
 										Decoder.WORKAROUND_EXCEPTION)) {
 							// reconnection max number?
 							Log.e("AACDecoder",
 									"stream could not be started -> trying again");
 						} else {
 							if (playerCallback != null) {
 								playerCallback.playerException(e);
 							}
 							break;
 						}
 					}
 				}
 				OpencorePlayer.this.streamPlayerOpencore.wakeLock.release();
				OpencorePlayer.this.streamPlayerOpencore.wifiLock.release();
 			}
 		}).start();
 	}
 }
