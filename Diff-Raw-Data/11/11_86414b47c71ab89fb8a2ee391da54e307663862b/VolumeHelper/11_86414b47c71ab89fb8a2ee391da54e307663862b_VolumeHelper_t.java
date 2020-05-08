 package com.blackcrowsteam.musicstop;
 
 import android.content.Context;
 import android.media.AudioManager;
 
 public class VolumeHelper {
 	private static AudioManager audio;
 	/**
 	 * Get the music manager
 	 * @param context
 	 */
 	private static void init(Context context){
 		if(audio == null)
 			audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 	}
 	/**
 	 * Get the media volume
 	 * @param c Context
 	 * @return Volume
 	 */
 	public static synchronized int getMediaVolume(Context c){
 		init(c);
		return audio.getStreamVolume(AudioManager.STREAM_MUSIC);
 	}
 	/**
 	 * Set media volume
 	 * @param c Context
 	 * @param volume Volume
 	 */
 	public static synchronized void setMediaVolume(Context c, int volume){
 		init(c);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC,volume,AudioManager.FLAG_SHOW_UI);
 		Debug.Log.v("Set volume to : " + volume);
 
 	}
 	/**
 	 * Mute the volume in a loop.
 	 * 
 	 * @param c Context
 	 * @return Initial volume
 	 */
 	public static synchronized int muteMediaVolume(Context c){
 		Debug.Log.v("MUTE!");
 		final int STEP = 5;
 		final int v = getMediaVolume(c);
 		int z = v;
 		int ratio = v/STEP;
 		if(v >= STEP){
 			for(int i = 0 ; i < STEP && v >= STEP ; i++){
 				z -= ratio;
 				setMediaVolume(c, z);
 				sleep(500);	
 			};
 		}
 		setMediaVolume(c, 0);
 		
 		return v;
 	}
 	public static void sleep(int ms){
 		try {
 			Thread.sleep(ms);
 		} catch (InterruptedException e) {}
 	}
 }
