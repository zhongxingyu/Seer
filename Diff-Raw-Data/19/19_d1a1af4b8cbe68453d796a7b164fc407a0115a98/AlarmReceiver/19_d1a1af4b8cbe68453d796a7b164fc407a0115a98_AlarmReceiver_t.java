 package me.mad4a.calendarwatcher;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import static me.mad4a.calendarwatcher.constants.Constants.*;
 
 /**
  * User: chsc4698@gmail.com
  * Date: 12-3-16
  * all rights reserved
  */
 public class AlarmReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 
 		if (intent.getAction().equals(ACTION_RESTORE_RINGER_MODE)) {
			Log.v(APPLICATION_TAG, "Received alarm intent (restore ringer mode) from event " + intent.getStringExtra(KEY_EVENT_TITLE));
 
			int originalRingerMode = (Integer)intent.getExtras().get(KEY_ORIGINAL_RINGER_MODE);
 			if (originalRingerMode != DEFAULT_VALUE_ORIGINAL_RINGER_MODE) {
 				audioManager.setRingerMode(originalRingerMode);
				Log.v(APPLICATION_TAG, "Ringer mode restored to " + originalRingerMode);
 			}
 		} else if (intent.getAction().equals(ACTION_SET_RINGER_MODE)) {
			Log.v(APPLICATION_TAG, "Received alarm intent (set ringer mode) from event " + intent.getStringExtra(KEY_EVENT_TITLE));
 
 			String ringerMode = intent.getStringExtra(KEY_RINGER_MODE);
 			int iRingerMode = AudioManager.RINGER_MODE_SILENT;
 
 			if (ringerMode == null) {
 				return;
 			}
 
 			if (ringerMode.equals(context.getString(R.string.dont_modify))) {
				Log.v(APPLICATION_TAG, "Ringer mode kept unmodified");

 				return;
 			} else if (ringerMode.equals(context.getString(R.string.vibrate))) {
 				iRingerMode = AudioManager.RINGER_MODE_VIBRATE;
 			}
 			audioManager.setRingerMode(iRingerMode);
			Log.v(APPLICATION_TAG, "Ringer mode set to " + iRingerMode + " (" + ringerMode + ")");
 
 			Toast.makeText(context, "Event occurs", Toast.LENGTH_SHORT).show();
 		} else if (intent.getAction().equals(ACTION_DAILY_FETCH)) {
			Log.v(APPLICATION_TAG, "Received alarm intent (daily fetch)");
 
 			Intent toService = new Intent().setClass(context, me.mad4a.calendarwatcher.WatcherService.class).setAction(ACTION_DAILY_FETCH);
 			context.startService(toService);
 		}
 	}
 
 }
