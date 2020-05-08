 package org.ggj2013;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnInfoListener;
 import android.util.Log;
 
 public class SoundManager {
 	private final Context appContext;
 
 	public static final float[] BALANCE_FULL_RIGHT = new float[] { 0.0f, 1.0f };
 	public static final float[] BALANCE_HALF_RIGHT = new float[] { 0.5f, 1.0f };
 	public static final float[] BALANCE_FULL_LEFT = new float[] { 1.0f, 0.0f };
 	public static final float[] BALANCE_HALF_LEFT = new float[] { 0.5f, 1.0f };
 	public static final float[] BALANCE_CENTER = new float[] { 1.0f, 1.0f };
 
 	public static final float VOLUME_100 = 1f;
 	public static final float VOLUME_50 = 0.5f;
 
 	public static final int LOOPS_0 = 0;
 	public static final int LOOPS_INFINITE = -1;
 
 	public Map<String, Integer> sounds = new HashMap<String, Integer>();
 	public Map<String, MediaPlayer> streams = new HashMap<String, MediaPlayer>();
 	public Map<String, Boolean> streamActive = new HashMap<String, Boolean>();
 
 	public SoundManager(Context appContext) {
 		this.appContext = appContext;
 	}
 
 	public void play(final String ident, final String soundKey,
 			float[] balance, final float masterVolume, final int loops) {
 
 		Log.d("SoundManager", "Start playing sound " + ident + " from "
 				+ soundKey);
 
 		if (streams.get(ident) != null) {
 			streams.get(ident).release();
 		}
 		MediaPlayer mediaPlayer = MediaPlayer.create(this.appContext,
 				sounds.get(soundKey));
 
 		mediaPlayer.setOnErrorListener(new OnErrorListener() {
 
 			@Override
 			public boolean onError(MediaPlayer mp, int what, int extra) {
 				Log.e("SoundManager",
 						"Mediaplayer caused error (" + soundKey + "): "
 								+ Integer.toString(what) + " "
 								+ Integer.toString(extra));
 				return true;
 			}
 		});
 
 		mediaPlayer.setOnInfoListener(new OnInfoListener() {
 
 			@Override
 			public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
 				Log.i("SoundManager",
 						"Mediaplayer caused error (" + soundKey + "): "
 								+ Integer.toString(arg1) + " "
 								+ Integer.toString(arg2));
 				return true;
 			}
 		});
 
 		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
 			@Override
 			public void onPrepared(MediaPlayer mp) {
				// streamActive.put(ident, true);
 			}
 		});
 
 		// Media buffer listener
 		mediaPlayer
 				.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
 					@Override
 					public void onBufferingUpdate(MediaPlayer mp, int percent) {
 						streamActive.put(ident, true);
 					}
 				});
 
 		if (loops == SoundManager.LOOPS_INFINITE) {
 			mediaPlayer.setLooping(true);
 		} else {
 			mediaPlayer.setLooping(false);
 		}
 
 		mediaPlayer.setVolume(masterVolume * balance[0], masterVolume
 				* balance[1]);
 		mediaPlayer.start();
 
 		streams.put(ident, mediaPlayer);
 	}
 
 	public void loadSoundPack(SoundPack soundPack) {
 		for (Map.Entry<String, Integer> sound : soundPack.getAllSounds()
 				.entrySet()) {
 
 			sounds.put(sound.getKey(), sound.getValue());
 		}
 	}
 
 	public void changeVolume(String streamIdent, float[] balance,
 			float masterVolume) {
 
 		MediaPlayer mp = streams.get(streamIdent);
 		if (mp != null && streamActive.containsKey(streamIdent)
 				&& streamActive.get(streamIdent) == true)
 			mp.setVolume(masterVolume * balance[0], masterVolume * balance[1]);
 	}
 
 	public void stopAll() {
 
 		for (String id : streams.keySet()) {
 			MediaPlayer mp = streams.get(id);
 			mp.release();
 
 		}
 
 		streams.clear();
 
 	}
 
 	public void unloadAll() {
 		// stopAll();
 		//
 		// for (String id : sounds.keySet()) {
 		// Integer soundId = sounds.get(id);
 		// sndPool.unload(soundId);
 		// }
 		//
 		// soundLoaded.clear();
 		// sounds.clear();
 	}
 
 	public void release() {
 		// stopAll();
 		// unloadAll();
 		// sndPool.release();
 	}
 
 	public void autoPause() {
 		for (Map.Entry<String, MediaPlayer> sound : streams.entrySet()) {
 			sound.getValue().pause();
 		}
 	}
 
 	public void autoResume() {
 		for (Map.Entry<String, MediaPlayer> sound : streams.entrySet()) {
 			sound.getValue().start();
 		}
 	}
 
 	// Sample calls
 	/*
 	 * soundManager.changeVolume("CatBackground",
 	 * SoundManager.BALANCE_FULL_LEFT, 0.1f);
 	 * 
 	 * soundManager.play("CatBackground", SoundPackStandard.CAT_MEOW,
 	 * SoundManager.BALANCE_FULL_LEFT, SoundManager.VOLUME_100,
 	 * SoundManager.LOOPS_0);
 	 */
 
 }
