 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.gl2jni;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.util.Log;
 
 // Wrapper for native library
 
 public class GL2JNILib {
 	private SoundPool soundPool;
 	private int soundID;
 	private int soundIDA;
 	private int soundIDB;
 	private int soundIDG;
 	private int soundIDCSharp;
 	private int soundIDC;
 	private int soundIDD;
 	private int soundIDE;
 	private int soundIDFSharp;
 
 	boolean loaded = false;
 	Context context;
 
 	/**
 	 * @return the context
 	 */
 	public Context getContext() {
 		return context;
 	}
 
 	/**
 	 * @param context
 	 *            the context to set
 	 */
 	public void setContext(Context context) {
 		this.context = context;
 	}
 
 	private AudioManager audioManager;
 	static {
 		System.loadLibrary("gl2jni");
 	}
 
 	/**
 	 * @param width
 	 *            the current view width
 	 * @param height
 	 *            the current view height
 	 */
 	public static native void init(int width, int height);
 
 	public native void touch(float x, float y);
 
	public static native void touchUp(float x, float y);
 
 	public static native void step();
 
 	public void playA() {
 		// setSoundID(getSoundPool().load(context, R.raw.a, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDA, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 
 	public void playG() {
 		// setSoundID(getSoundPool().load(context, R.raw.g, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDG, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 
 	public void playB() {
 		// setSoundID(getSoundPool().load(context, R.raw.b, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDB, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 
 	public void playCSharp() {
 		// setSoundID(getSoundPool().load(context, R.raw.b, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDCSharp, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 	
 	public void playC() {
 		// setSoundID(getSoundPool().load(context, R.raw.b, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDC, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 
 	public void playD() {
 		// setSoundID(getSoundPool().load(context, R.raw.b, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDD, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 	
 	public void playE() {
 		// setSoundID(getSoundPool().load(context, R.raw.b, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDE, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 	public void playFSharp() {
 		// setSoundID(getSoundPool().load(context, R.raw.b, 1));
 		float actualVolume = (float) audioManager
 				.getStreamVolume(AudioManager.STREAM_MUSIC);
 		float maxVolume = (float) audioManager
 				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		float volume = actualVolume / maxVolume;
 		// Is the sound loaded already?
 		if (loaded) {
 			soundPool.play(soundIDFSharp, volume, volume, 1, 0, 1f);
 			Log.e("Test", "Played sound");
 		}
 	}
 
 	/**
 	 * @return the audioManager
 	 */
 	public AudioManager getAudioManager() {
 		return audioManager;
 	}
 
 	/**
 	 * @param audioManager
 	 *            the audioManager to set
 	 */
 	public void setAudioManager(AudioManager audioManager) {
 		this.audioManager = audioManager;
 	}
 
 	/**
 	 * @return the soundPool
 	 */
 	public SoundPool getSoundPool() {
 		return soundPool;
 	}
 
 	/**
 	 * @param soundPool
 	 *            the soundPool to set
 	 */
 	public void setSoundPool(SoundPool soundPool) {
 		this.soundPool = soundPool;
 	}
 
 	/**
 	 * @return the soundID
 	 */
 	public int getSoundID() {
 		return soundID;
 	}
 
 	/**
 	 * @param soundID
 	 *            the soundID to set
 	 */
 	public void setSoundID(int soundID) {
 		this.soundID = soundID;
 	}
 
 	/**
 	 * @return the loaded
 	 */
 	public boolean isLoaded() {
 		return loaded;
 	}
 
 	/**
 	 * @param loaded
 	 *            the loaded to set
 	 */
 	public void setLoaded(boolean loaded) {
 		this.loaded = loaded;
 	}
 
 	/**
 	 * @return the soundIDA
 	 */
 	public int getSoundIDA() {
 		return soundIDA;
 	}
 
 	/**
 	 * @param soundIDA
 	 *            the soundIDA to set
 	 */
 	public void setSoundIDA(int soundIDA) {
 		this.soundIDA = soundIDA;
 	}
 
 	/**
 	 * @return the soundIDB
 	 */
 	public int getSoundIDB() {
 		return soundIDB;
 	}
 
 	/**
 	 * @param soundIDB
 	 *            the soundIDB to set
 	 */
 	public void setSoundIDB(int soundIDB) {
 		this.soundIDB = soundIDB;
 	}
 
 	/**
 	 * @return the soundIDG
 	 */
 	public int getSoundIDG() {
 		return soundIDG;
 	}
 
 	/**
 	 * @param soundIDG
 	 *            the soundIDG to set
 	 */
 	public void setSoundIDG(int soundIDG) {
 		this.soundIDG = soundIDG;
 	}
 
 	/**
 	 * @return the soundIDCSharp
 	 */
 	public int getSoundIDCSharp() {
 		return soundIDCSharp;
 	}
 
 	/**
 	 * @param soundIDCSharp
 	 *            the soundIDCSharp to set
 	 */
 	public void setSoundIDCSharp(int soundIDCSharp) {
 		this.soundIDCSharp = soundIDCSharp;
 	}
 
 	/**
 	 * @return the soundIDD
 	 */
 	public int getSoundIDD() {
 		return soundIDD;
 	}
 
 	/**
 	 * @param soundIDD
 	 *            the soundIDD to set
 	 */
 	public void setSoundIDD(int soundIDD) {
 		this.soundIDD = soundIDD;
 	}
 
 	/**
 	 * @return the soundIDC
 	 */
 	public int getSoundIDC() {
 		return soundIDC;
 	}
 
 	/**
 	 * @param soundIDC the soundIDC to set
 	 */
 	public void setSoundIDC(int soundIDC) {
 		this.soundIDC = soundIDC;
 	}
 
 	/**
 	 * @return the soundIDE
 	 */
 	public int getSoundIDE() {
 		return soundIDE;
 	}
 
 	/**
 	 * @param soundIDE the soundIDE to set
 	 */
 	public void setSoundIDE(int soundIDE) {
 		this.soundIDE = soundIDE;
 	}
 
 	/**
 	 * @return the soundIDFSharp
 	 */
 	public int getSoundIDFSharp() {
 		return soundIDFSharp;
 	}
 
 	/**
 	 * @param soundIDFSharp the soundIDFSharp to set
 	 */
 	public void setSoundIDFSharp(int soundIDFSharp) {
 		this.soundIDFSharp = soundIDFSharp;
 	}
 }
