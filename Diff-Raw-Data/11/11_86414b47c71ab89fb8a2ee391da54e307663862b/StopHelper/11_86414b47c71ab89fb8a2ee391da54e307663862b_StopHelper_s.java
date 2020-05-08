 /*
  * Copyright 2012 Laurent Constantin <android@blackcrowsteam.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.blackcrowsteam.musicstop;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.SystemClock;
 import android.view.KeyEvent;
 
 /**
  * Useful procedures to stop the music
  * <ul>
  * <li>- Press the MEDIA_PLAY_PAUSE key</li>
  * <li>- Press the MEDIA_STOP key</li>
  * <li>- Plug and un-plug some virtual headset</li>
  * <li>- Un-Plug and plug some virtual headset</li>
  * <li>- Use the ACTION_AUDIO_BECOMING_NOISY functionality (See
  * #android.media.AudioManager)</li>
  * </ul>
  * 
  * @author Constantin Laurent
  * 
  */
 public class StopHelper {
 
 	private static void sendKey(Context c, int action, int code) {
 		long eventtime = SystemClock.uptimeMillis();
 		Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
 		KeyEvent downEvent = new KeyEvent(eventtime, eventtime, action, code, 0);
 
 		downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
 
 		c.sendOrderedBroadcast(downIntent, null);
 
 	}
 
 	private static void sendKey(Context c, int code) {
 		sendKey(c, KeyEvent.ACTION_DOWN, code);
 		sendKey(c, KeyEvent.ACTION_UP, code);
 	}
 
 	public static void sendStopKey(Context c) {
 		sendKey(c, KeyEvent.KEYCODE_MEDIA_STOP);
 	}
 
 	public static void sendPlayPauseKey(Context c) {
 		sendKey(c, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
 	}
 
 	public static void audioBecomingNoisy(Context c) {
 		Intent i = new Intent();
 		i.setAction(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY);
 		c.sendBroadcast(i);
 	}
 
 	public static void changeHeadSetState(Context c, String name,
 			Boolean connected) {
 		Intent i2 = new Intent();
 		i2.setAction(Intent.ACTION_HEADSET_PLUG);
 		i2.putExtra("state", (connected ? 1 : 0));
 		i2.putExtra("name", name);
 		i2.putExtra("microphone", 0);
 		c.sendBroadcast(i2);
 	}
 
 	/**
 	 * Stop the music with the selected method
 	 * 
 	 * @param c
 	 *            Context
 	 * @param method
 	 *            The selected method (see array.xml)
 	 * @return True if the method is known, false otherwise
 	 */
 	public static boolean stopMusic(Context c, int method) {
 		switch (method) {
 		default:
 			return false;
 		case 1:
 			StopHelper.sendStopKey(c);
 			break;
 		case 2:
 			StopHelper.sendPlayPauseKey(c);
 			break;
 		case 3:
 			StopHelper.headsetPlugAndUnplug(c);
 			break;
 		case 4:
 			StopHelper.headsetUnPlugAndPlug(c);
 			break;
 		case 5:
 			StopHelper.audioBecomingNoisy(c);
 			break;
 		}
 		return true;
 	}
 
 	/**
 	 * Simulation of a handset plug-out and plug-in Useful for songbird
 	 */
 	public static void headsetUnPlugAndPlug(Context c) {
 		StopHelper.changeHeadSetState(c, "Simulated-Headset", false);
 
 		StopHelper.changeHeadSetState(c, "Simulated-Headset", true);
 	}
 
 	/**
 	 * Simulation of a handset plug-in and plug-out Useful for songbird
 	 */
 	public static void headsetPlugAndUnplug(Context c) {
 		StopHelper.changeHeadSetState(c, "Simulated-Headset", true);
 
 		StopHelper.changeHeadSetState(c, "Simulated-Headset", false);
 	}
 }
