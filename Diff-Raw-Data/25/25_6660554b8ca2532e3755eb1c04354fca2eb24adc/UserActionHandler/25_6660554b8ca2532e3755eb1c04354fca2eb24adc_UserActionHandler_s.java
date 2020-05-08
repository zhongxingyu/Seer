 /***************************************************************************
  * Copyright (c) 2011, 2012 by Sonr Labs Inc (http://www.sonrlabs.com)
  *
  *You can redistribute this program and/or modify it under the terms of the GNU General Public License v. 2.0 as published by the Free Software Foundation
  *This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
  *
  **************************************************************************/
 
 package com.sonrlabs.test.sonr;
 
 //import org.acra.ErrorReporter;
 
 //import com.flurry.android.FlurryAgent;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import android.app.Instrumentation;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.media.AudioManager;
 import android.os.SystemClock;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 import com.sonrlabs.v96.sonr.R;
 
 class UserActionHandler {
 
    private static final String TAG = "SONR audio processor";
 
    // SONR commands ******************
    private static final int SONR_PLAY_PAUSE = 0x85;
    private static final int SONR_FAST_FORWARD = -2;
    private static final int SONR_REWIND = -3;
    private static final int SONR_NEXT_TRACK = 0x45;
    private static final int SONR_PREVIOUS_TRACK = 0x79;
    private static final int SONR_VOLUME_UP = 0x15;
    private static final int SONR_VOLUME_DOWN = 0xe5;
    private static final int SONR_MUTE = 0x25;
   private static final int SONR_THUMBS_UP = 0x9;
   private static final int SONR_THUMBS_DOWN = 0xa;
   private static final int SONR_FAVORITE = 0x6;
   private static final int SONR_DPAD_UP = 0xc;
   private static final int SONR_DPAD_DOWN = 0xf;
   private static final int SONR_DPAD_LEFT = 0x11;
   private static final int SONR_DPAD_RIGHT = 0x12;
   private static final int SONR_DPAD_SELECT = 0x14;
   private static final int SONR_POWER_ON = 0x1;
   private static final int SONR_POWER_OFF = 0x5;
    private static final int SONR_HOME = 0xb9;
   private static final int SONR_SHARE = 0x28;
   private static final int SONR_SEARCH = 0x24;
    // end SONR commands
    // ****************************************************************************************************************
 
    // for button repeats, in milliseconds, transmission length is ~40ms so anything less is probably meaningless
    private static final int REPEAT_TIME = 40;
    private static final int SKIP_TIME = 40;
    private static final int BACK_TIME = 40;
    private static final int VOL_TIME = 20;
    private static final String CURRENT_VOLUME = "CURRENT_VOLUME";
 
    private final AudioManager manager;
    private final Context appContext;
 
    private long lastPlayTime = 0;
    private long lastMuteTime = 0;
    private long lastSkipTime = 0;
    private long lastVolumeTime = 0;
    private long lastBackTime = 0;
    private int parsePlayPauseCount = 0;
    private int volume = -1;
    private boolean muted = false;
    
    UserActionHandler(Context appContext) {
       SonrLog.d(TAG, "RemoteListener started");
       this.manager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
       this.appContext = appContext;
    }
 
    void processAction(int receivedByte) {
       try {
          processUserCommand(receivedByte);
       } catch (RuntimeException e) {
          e.printStackTrace();
          // ErrorReporter.getInstance().handleException(e);
       }
    }
 
    private void processUserCommand(int receivedByte) {
       checkAutoUnmute(receivedByte);
       int key = Integer.MIN_VALUE;
       long elapsedTime = SystemClock.elapsedRealtime();
       switch (receivedByte) {
          case SONR_PLAY_PAUSE:
             if (lastPlayTime < elapsedTime - REPEAT_TIME) {
                lastPlayTime = elapsedTime;
                key = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                Log.d(TAG, "PLAY");
                // FlurryAgent.logEvent("PLAY_PRESSED");
                notifyParsePlayPause();
             }
             break;
          case SONR_FAST_FORWARD:
             key = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
             Log.d(TAG, "FAST_FORWARD");
             // FlurryAgent.logEvent("FAST_FORWARD_PRESSED");
             break;
          case SONR_REWIND:
             key = KeyEvent.KEYCODE_MEDIA_REWIND;
             Log.d(TAG, "REWIND");
             // FlurryAgent.logEvent("REWIND_PRESSED");
             break;
          case SONR_NEXT_TRACK:
             if (lastSkipTime < elapsedTime - SKIP_TIME) {
                lastSkipTime = elapsedTime;
                key = KeyEvent.KEYCODE_MEDIA_NEXT;
                Log.d(TAG, "NEXT_TRACK");
                // FlurryAgent.logEvent("NEXT_TRACK_PRESSED");
             }
             break;
          case SONR_PREVIOUS_TRACK:
             if (lastBackTime < elapsedTime - BACK_TIME) {
                lastBackTime = elapsedTime;
                key = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
                Log.d(TAG, "PREVIOUS_TRACK");
                // FlurryAgent.logEvent("PREVIOUS_TRACK_PRESSED");
             }
             break;
          case SONR_VOLUME_UP:
             if (lastVolumeTime < elapsedTime - VOL_TIME) {
                lastVolumeTime = elapsedTime;
                manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                Log.d(TAG, "VOLUME_UP");
                // FlurryAgent.logEvent("VOLUME_UP_PRESSED");
             }
             break;
          case SONR_VOLUME_DOWN:
             if (lastVolumeTime < elapsedTime - VOL_TIME) {
                lastVolumeTime = elapsedTime;
                manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                Log.d(TAG, "VOLUME_DOWN");
                // FlurryAgent.logEvent("VOLUME_DOWN_PRESSED");
             }
             break;
          case SONR_MUTE:
             if (lastMuteTime < elapsedTime - REPEAT_TIME) {
                lastMuteTime = elapsedTime;
                if (muted) {
                   int defaultLevel = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
                   volume = Preferences.getPreference(appContext, CURRENT_VOLUME, defaultLevel); 
                   manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                   muted = false;
                } else {
                   volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
                   Preferences.savePreference(appContext, CURRENT_VOLUME, volume);
                   volume = 0;
                   manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                   muted = true;
                }
                Log.d(TAG, "MUTE");
             }
             
             /*PackageManager pm = appContext.getPackageManager();
             List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
             if (activities.size() != 0) {
                SonrLog.d(TAG, "VR SUPPORTED YAHOO!");
                SonrLog.d(TAG, "broadcasting speech recognizer");
                Intent speechRecognizerIntent = new Intent("android.intent.action.SPEECH_RECOGNIZER");
                speechRecognizerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.sendOrderedBroadcast(speechRecognizerIntent, null);
             } else {
                 SonrLog.e(TAG, "VR NOT SUPPORTED!");
                 Toast.makeText(appContext, "Voice Recognition Not Supported!", Toast.LENGTH_LONG).show();
             }*/                   
             break;
          case SONR_THUMBS_UP:
             Log.d(TAG, "THUMBS_UP");
             key = SONR_THUMBS_UP;
             // FlurryAgent.logEvent("THUMBS_UP_PRESSED");
             break;
          case SONR_THUMBS_DOWN:
             Log.d(TAG, "THUMBS_DOWN");
             key = SONR_THUMBS_DOWN;
             // FlurryAgent.logEvent("THUMBS_DOWN_PRESSED");
             break;
          case SONR_FAVORITE:
             Log.d(TAG, "FAVORITE");
             key = SONR_FAVORITE;
             // FlurryAgent.logEvent("FAVORITE_PRESSED");
             break;
          case SONR_DPAD_UP:
             key = KeyEvent.KEYCODE_DPAD_UP;
             Log.d(TAG, "UP");
             instrumentKey(key);
             break;
          case SONR_DPAD_DOWN:
             key = KeyEvent.KEYCODE_DPAD_DOWN;
             Log.d(TAG, "DOWN");
             instrumentKey(key);
             break;
          case SONR_DPAD_LEFT:
             key = KeyEvent.KEYCODE_DPAD_LEFT;
             Log.d(TAG, "LEFT");
             instrumentKey(key);
             break;
          case SONR_DPAD_RIGHT:
             key = KeyEvent.KEYCODE_DPAD_RIGHT;
             Log.d(TAG, "RIGHT");
             instrumentKey(key);
             break;
          case SONR_DPAD_SELECT:
             key = KeyEvent.KEYCODE_DPAD_CENTER;
             Log.d(TAG, "CENTER");
             instrumentKey(key);
             break;
          case SONR_SHARE:
             Log.d(TAG, "SHARE");
             key = SONR_SHARE;
             // FlurryAgent.logEvent("SHARE_PRESSED");
             break;
          case SONR_POWER_ON:
             Log.d(TAG, "POWER_ON");
             key = SONR_POWER_ON;
             // FlurryAgent.logEvent("POWER_ON_PRESSED");
             break;
          case SONR_POWER_OFF:
             Log.d(TAG, "POWER_OFF");
             key = SONR_POWER_OFF;
             // FlurryAgent.logEvent("POWER_OFF_PRESSED");
             break;
          case SONR_HOME:
             Log.d(TAG, "SONR HOME");
             Intent launchSonrHome = new Intent(appContext, SonrActivity.class);
             launchSonrHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
             Preferences.savePreference(appContext, appContext.getString(R.string.DEFAULT_PLAYER_SELECTED), false);
             appContext.startActivity(launchSonrHome);
             break;
          case SONR_SEARCH:
             Log.d(TAG, "SEARCH");
             key = SONR_SEARCH;
 
             PackageManager pm = appContext.getPackageManager();
             List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
             if (activities.size() != 0) {
                SonrLog.d(TAG, "VR SUPPORTED YAHOO!");
                SonrLog.d(TAG, "broadcasting speech recognizer");
                Intent speechRecognizerIntent = new Intent("android.intent.action.SPEECH_RECOGNIZER");
                speechRecognizerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.sendOrderedBroadcast(speechRecognizerIntent, null);
             } else {
                 SonrLog.e(TAG, "VR NOT SUPPORTED!");
                 Toast.makeText(appContext, "Voice Recognition Not Supported!", Toast.LENGTH_LONG).show();
             } 
      
             break;
 
          case 0:
             // consider this a no-op for now.
             break;
 
          default:
             SonrLog.d(TAG, "Unknown signal " + Integer.toHexString(receivedByte));
       }
 
       if (key != Integer.MIN_VALUE) {
          sendbroadcast(key);
       }
    }
 
    /*
     * If currently muted, unmute on any action other mute itself, but leave the
     * volume at 0.
     */
    private void checkAutoUnmute(int receivedByte) {
       if (muted && receivedByte != SONR_MUTE) {
          volume = 0;
          muted = false;
       }
    }
 
    private void notifyParsePlayPause()
    {
       if (++parsePlayPauseCount != 3) return;
 
       Context applicationContext = appContext.getApplicationContext();
       String playPause = appContext.getString(R.string.PLAY_PAUSE);
       SonrAppInformationLogger logger = new SonrAppInformationLogger();
       logger.uploadErrorAppInformationWithErrorString(applicationContext, playPause);
    }
    
    private void sendbroadcast(int keyEvent) {
       String playerPackage = Preferences.getPreference(appContext, appContext.getString(R.string.APP_PACKAGE_NAME), null);
       
       if (playerPackage != null) {
          Log.d(TAG, playerPackage);
          
          Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
          i.setPackage(playerPackage);
          
          i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
          appContext.sendOrderedBroadcast(i, null);
          
          i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyEvent));
          appContext.sendOrderedBroadcast(i, null);
       }
    }
 
    private void instrumentKey(int keyEvent) {
       Instrumentation instrumentation = new Instrumentation();
       try {
          instrumentation.sendKeyDownUpSync(keyEvent);
       } catch (SecurityException e) {
          // some other app is front, ignore.
       }
    }
    
    /**
     * set device connection state through reflection for Android 2.1, 2.2, 2.3,
     * maybe others. Thanks Adam King!
     * 
     * @param device
     * @param state
     * @param address
     */
    static void setDeviceConnectionState(final int device, final int state, final String address) {
       try {
          Class<?> audioSystem = Class.forName("android.media.AudioSystem");
          Method setDeviceConnectionState = audioSystem.getMethod("setDeviceConnectionState", int.class, int.class, String.class);
 
          setDeviceConnectionState.invoke(audioSystem, device, state, address);
       } catch (Exception e) {
          SonrLog.e(TAG, "setDeviceConnectionState failed: " + e);
       } 
    }
 }
