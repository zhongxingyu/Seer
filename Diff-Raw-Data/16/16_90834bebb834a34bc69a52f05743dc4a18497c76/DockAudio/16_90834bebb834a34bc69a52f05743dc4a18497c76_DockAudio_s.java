 package com.android.dockaudio;
 
 import android.app.NotificationManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.AudioSystem;
 import android.util.Log;
 
 /**************************************************
  * Intents ------------------------> Numeric code *
  *                                                *
 * MOT_BASIC_DOCK_AUDIO_PLUG ------> 0x400        *
 * MOT_SMART_DOCK_SPDIF_PLUG ------> 0x800        *
  * EXTDISP_STATUS_DISPLAY (HDMI) --> 0x1000       *
  **************************************************/
 
 
 public class DockAudio extends BroadcastReceiver {
     private static final String LOG_TAG = "DockAudio";
 
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.i(LOG_TAG, "Got intent from dock");
         if(intent.getExtras().containsKey("android.intent.extra.DOCK_STATE")){
             AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
             int state = intent.getExtras().getInt("android.intent.extra.DOCK_STATE",1);
             if(state == 0){
                 Log.i(LOG_TAG, "Removed from dock!");
                 AudioSystem.setDeviceConnectionState(0x400, 0x00, "");
                 AudioSystem.setDeviceConnectionState(0x800, 0x00, "");
                 AudioSystem.setDeviceConnectionState(0x1000, 0x00, "");
                 am.setParameters("DockState=0");
             } else if(state == 1) {
                 Log.i(LOG_TAG, "Docked on desktop!");
                 am.setParameters("DockState=1");
                AudioSystem.setDeviceConnectionState(0x800, 0x01, "");
             } else {
                 Log.i(LOG_TAG, "Docked on car!");
                 am.setParameters("DockState=2");
                AudioSystem.setDeviceConnectionState(0x400, 0x01, "");
             }
 
         }
     }
 }
