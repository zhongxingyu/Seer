 /*
  * Copyright (c) 2010 Sony Ericsson
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * 
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.sonyericsson.extras.liveview.plugins.sandbox;
 
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 import com.makingiants.liveview.funny.model.SoundManager;
 import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
 import com.sonyericsson.extras.liveview.plugins.PluginConstants;
 import com.sonyericsson.extras.liveview.plugins.PluginUtils;
 
 public class SoundPluginService extends AbstractPluginService {
 	
 	// ****************************************************************
 	// Attributes
 	// ****************************************************************
 	
 	// Used to update the LiveView screen
 	private Handler handler;
 	
 	// Workers
 	private SoundManager soundManager = null;
 	
 	// ****************************************************************
 	// Service Overrides
 	// ****************************************************************
 	
 	public void onStart(final Intent intent, final int startId) {
 		super.onStart(intent, startId);
 		
 		if (soundManager == null) {
 			soundManager = new SoundManager(this);
 		}
 		
 		if (handler == null) {
 			handler = new Handler();
 		}
 		
 		//mLiveViewAdapter.screenOnAuto(mPluginId);
 	}
 	
 	public void onCreate() {
 		super.onCreate();
 		
 	}
 	
 	public void onDestroy() {
 		super.onDestroy();
 		
 		stopWork();
 	}
 	
 	/**
 	 * Plugin is sandbox.
 	 */
 	protected boolean isSandboxPlugin() {
 		return true;
 	}
 	
 	/**
 	 * Must be implemented. Starts plugin work, if any.
 	 */
 	protected void startWork() {
 		
 		showTextDelayed(
 		        String.format("%s - %s", soundManager.getActualCategory(),
 		                soundManager.getActualSound()), 100, 10);
 	}
 	
 	/**
 	 * Must be implemented. Stops plugin work, if any.
 	 */
 	protected void stopWork() {
 	}
 	
 	/**
 	 * Must be implemented.
 	 * 
 	 * PluginService has done connection and registering to the LiveView
 	 * Service.
 	 * 
 	 * If needed, do additional actions here, e.g. starting any worker that is
 	 * needed.
 	 */
 	protected void onServiceConnectedExtended(final ComponentName className, final IBinder service) {
 		
 	}
 	
 	/**
 	 * Must be implemented.
 	 * 
 	 * PluginService has done disconnection from LiveView and service has been
 	 * stopped.
 	 * 
 	 * Do any additional actions here.
 	 */
 	protected void onServiceDisconnectedExtended(final ComponentName className) {
 		
 	}
 	
 	/**
 	 * Must be implemented.
 	 * 
 	 * PluginService has checked if plugin has been enabled/disabled.
 	 * 
 	 * The shared preferences has been changed. Take actions needed.
 	 */
 	protected void onSharedPreferenceChangedExtended(final SharedPreferences prefs, final String key) {
 		
 	}
 	
 	protected void startPlugin() {
 		// Log.d(PluginConstants.LOG_TAG, "startPlugin");
 		startWork();
 	}
 	
 	protected void stopPlugin() {
 		// Log.d(PluginConstants.LOG_TAG, "stopPlugin");
 		stopWork();
 	}
 	
 	// ****************************************************************
 	// GUI Changes
 	// ****************************************************************
 	
 	private void showTextDelayed(final String text, final int bitmapSizeX, final int fontSize) {
 		handler.postDelayed(new Runnable() {
 			
 			public void run() {
 				// First message to LiveView
 				try {
 					
 					mLiveViewAdapter.clearDisplay(mPluginId);
 				} catch (final Exception e) {
 					Log.e(PluginConstants.LOG_TAG, "Failed to clear display.");
 				}
 				
 				PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId, text, bitmapSizeX, fontSize);
 				
 			}
 		}, 1000);
 		
 	}
 	
 	private void showText(final String text, final int bitmapSizeX, final int fontSize) {
 		
 		handler.post(new Runnable() {
 			
 			public void run() {
 				// First message to LiveView
 				try {
 					
 					mLiveViewAdapter.clearDisplay(mPluginId);
 				} catch (final Exception e) {
 					Log.e(PluginConstants.LOG_TAG, "Failed to clear display.");
 				}
 				
 				PluginUtils.sendTextBitmap(mLiveViewAdapter, mPluginId, text, bitmapSizeX, fontSize);
 				
 			}
 		});
 		
 	}
 	
 	// ****************************************************************
 	// Events
 	// ****************************************************************
 	
 	protected void button(final String buttonType, final boolean doublepress, final boolean longpress) {
 		Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress
 		        + ", longpress " + longpress);
 		
 		if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_UP)) {
 			//showText(soundManager.jumpPastCategory(), 220, 65);
 			showText(
 			        String.format("%s - %s", soundManager.jumpPastCategory(),
 			                soundManager.getActualSound()), 100, 10);
 		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_DOWN)) {
 			showText(
 			        String.format("%s - %s", soundManager.jumpNextCategory(),
 			                soundManager.getActualSound()), 100, 10);
 		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_LEFT)) {
 			showText(
 			        String.format("%s - %s", soundManager.getActualCategory(),
 			                soundManager.jumpPastSound()), 100, 10);
 		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_RIGHT)) {
 			showText(
 			        String.format("%s - %s", soundManager.getActualCategory(),
 			                soundManager.jumpNextSound()), 100, 10);
 		} else if (buttonType.equalsIgnoreCase(PluginConstants.BUTTON_SELECT)) {
 			
 			// Play the sound
 			soundManager.playSound();
 			
 		}
 		
 	}
 	
 	protected void displayCaps(final int displayWidthPx, final int displayHeigthPx) {
 		// Log.d(PluginConstants.LOG_TAG, "displayCaps - width " +
 		// displayWidthPx+ ", height " + displayHeigthPx);
 	}
 	
 	protected void onUnregistered() throws RemoteException {
 		// Log.d(PluginConstants.LOG_TAG, "onUnregistered");
 		stopWork();
 	}
 	
 	protected void openInPhone(final String openInPhoneAction) {
 		// Log.d(PluginConstants.LOG_TAG, "openInPhone: " + openInPhoneAction);
 	}
 	
 	protected void screenMode(final int mode) {
		Log.d(PluginConstants.LOG_TAG, "screenMode: screen is now " + ((mode == 0) ? "OFF" : "ON"));
 		
 		/*if (mode != PluginConstants.LIVE_SCREEN_MODE_ON) {
 			mLiveViewAdapter.screenOn(mPluginId);
 		}*/
 		if (mode != PluginConstants.LIVE_SCREEN_MODE_ON) {
			startUpdates();
 		}
 	}
 	
	private void startUpdates() {
		
		// Play the sound
		soundManager.playSound();
	}
	
 }
