 package com.npl.simplemusicplayer;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class MusicIntentReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		String action = intent.getAction();
 
 		// If unplug headphone
 		if (action.equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			Intent service = new Intent();
			service.setAction(MusicService.ACTION_PAUSE);
			context.startService(service);
 		}
 
 	}
 
 }
