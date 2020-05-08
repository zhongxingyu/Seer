 package org.duncavage.volumemiser;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 
 public class VolumeMiserReceiver extends BroadcastReceiver {
 	private AudioManager audio_manager;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		audio_manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 		
 		if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
 			int connected = intent.getIntExtra("state", 0);
 			
 			if(connected == 0) {
 				audio_manager.setStreamVolume(AudioManager.STREAM_MUSIC, 
						PreferenceHelper.getSpeakerVolume(context), 
 						AudioManager.FLAG_SHOW_UI);
 			} else {
 				audio_manager.setStreamVolume(AudioManager.STREAM_MUSIC, 
						PreferenceHelper.getHeadsetVolume(context), 
 						AudioManager.FLAG_SHOW_UI);
 			}
 		}
 	}
 }
