 /*
  * Copyright (c) 2011 James Johnson
  * Permission is hereby granted, free of charge, to any person obtaining a copy 
  * of this software and associated documentation files (the "Software"), to 
  * deal in the Software without restriction, including without limitation the 
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
  * sell copies of the Software, and to permit persons to whom the Software is 
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
  * IN THE SOFTWARE.
  */
 
 package org.robobrain.sdk.audio;
 
 import java.io.IOException;
 
 import org.robobrain.sdk.GameActivity;
 
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.content.res.AssetManager;
 import android.media.MediaPlayer;
 import android.util.Log;
 
 /**
  * Manages streaming music.
  * @author James Johnson
  */
 public class MusicManager {
 	private static Context mContext;
 	private static float mVolume;
 	private static MediaPlayer mPlayer;
 	private static boolean mSet;
 	private static boolean mReset;
 	private static boolean mInitialized = false;
 	private static boolean mStopped;
 	
 	// Don't let users instantiate this class
 	private MusicManager() {
 		
 	}
 	
 	// Initialize this player
 	private static void init() {
 		mContext = GameActivity.getContext();
         mVolume = 1.0f;
         mPlayer = new MediaPlayer();
         mSet = false;
         mReset = false;
         mInitialized = true;
         mStopped = false;
 	}
 	 
 	/**
 	 * Loads a music file out of the assets folder. Loads OGG, MP3 and WAV files.
 	 * @param file 
 	 * Path to the file.
 	 */
 	public static void loadMusic(String file) {
 		if (!mInitialized) {
 			init();
 		}
 		
 		if (mPlayer == null) {
 			return;
 		}
 		
 		if (mSet) {
 			clear();
 		}
 		try {
 	            AssetManager assetManager = mContext.getAssets();
 	            AssetFileDescriptor descriptor = assetManager.openFd(file);
 	            mPlayer.setDataSource(descriptor.getFileDescriptor(), 
 	            					  descriptor.getStartOffset(), 
 	            					  descriptor.getLength());
 	            mPlayer.prepare();
 	            mPlayer.setLooping(true);
 	            mPlayer.setVolume(mVolume, mVolume);
 	            mSet = true;
 	            mReset = false;
 	        } catch (IOException e) {
 	            Log.e("Music Loading Error", e.getMessage());
 	            mPlayer = null;
 	        } catch (IllegalStateException e) {
 	            Log.e("MusicManager Error", e.getMessage());
 	            mPlayer = null;
 	        }
 	}
 	
 	/**
 	 * Plays the music.
 	 */
 	public static void play() {
 		if (!mInitialized) {
 			return;
 		}
 		if (mPlayer == null) {
 			return;
 		}
 		if (mStopped) {
 			try {
 				mPlayer.prepare();
 				mStopped = false;
 			} catch (IOException e) {
 	            Log.e("Music File Error", e.getMessage());
 	            return;
 	        } catch (IllegalStateException e) {
 	            Log.e("MusicManager Error", e.getMessage());
 	            return;
 	        }
 		}
 		mPlayer.start();
 	}
 	
 	/**
 	 * Stops the music.
 	 */
 	public static void stop() {
 		if (!mInitialized) {
 			return;
 		}
 		if (mReset) {
 			return;
 		}
 		if (mPlayer == null) {
 			return;
 		}
 		if (mPlayer.isPlaying()) {
 			mPlayer.stop();
 			mStopped = true;
 		}
 	}
 	
 	/**
 	 * Pauses the music.
 	 */
 	public static void pause() {
 		if (!mInitialized) {
 			return;
 		}
 		if (mPlayer == null) {
 			return;
 		}
 		if (mStopped) {
 			return;
 		}
 		mPlayer.pause();
 	}
 	
 	/**
 	 * Unloads the music file and resets the player. Call this before loading a 
 	 * new music file.
 	 */
 	public static void clear() {
 		if (!mInitialized) {
 			return;
 		}
 		if (mPlayer == null) {
 			return;
 		}
 		if (mPlayer.isPlaying()) {
 			mPlayer.stop();
 		}
 		mSet = false;
 		mReset = true;
 		mPlayer.reset();
 	}
 	
 	/**
 	 * Clears and releases any resources used by the player. Should be called once before
 	 * shutting the game down.
 	 */
 	public static void release() {
 		if (!mInitialized) {
 			return;
 		}
 		if (mPlayer == null) {
 			return;
 		}
 		if (mSet) {
 			clear();
 		}
 		mPlayer.release();
 		mInitialized = false;
 	}
 	
 	/**
 	 * Sets the music's volume.
 	 * @param value
 	 */
 	public static void setVolume(float value) {
 		if (!mInitialized) {
 			return;
 		}
 		if (mPlayer == null) {
 			return;
 		}
 		if (value < 0.0f)
 			value = 0.0f;
 		if (value > 1.0f)
 			value = 1.0f;
 		mVolume = value;
 		mPlayer.setVolume(value, value);
 	}
 	
 	/**
 	 * Gets the music's volume.
 	 * @return
 	 * The music's volume.
 	 */
 	public static float getVolume() { return mVolume; }
 
	public static boolean isStoped(){ return mStopped;}
 }

