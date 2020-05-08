 package org.duncavage.volumemiser;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.media.AudioManager;
 import android.os.Handler;
 import android.os.IBinder;
 import android.provider.Settings.System;
 
 public class VolumeMiserService extends Service {
 
 	public static final int VOLUMEMISER_NOTIF_ID = 1000;
 	
 	private AudioManager audio_manager;
 	private VolumeMiserReceiver volume_receiver;
 	
 	private final ContentObserver volume_observer = new ContentObserver(new Handler()) {
 		@Override
 		public void onChange(boolean selfChange) {
 			super.onChange(selfChange);
 			if(audio_manager.isWiredHeadsetOn()) {
 				PreferenceHelper.saveCurrentVolumeAsHeadsetPref(VolumeMiserService.this);
 			} else {
 				PreferenceHelper.saveCurrentVolumeAsSpeakerPref(VolumeMiserService.this);
 			}
 		}
 	};
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		getContentResolver().registerContentObserver(System.getUriFor(System.VOLUME_SETTINGS[AudioManager.STREAM_MUSIC]), false,
                 volume_observer);
 		
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(Intent.ACTION_HEADSET_PLUG);
 		// register our receiver
 		volume_receiver = new VolumeMiserReceiver();
 		registerReceiver(volume_receiver, filter);
 		
 		// if no values exist in preferences, store the current volume
 		if(PreferenceHelper.getHeadsetVolume(this) == -1 || 
 				PreferenceHelper.getSpeakerVolume(this) == -1) {
 			PreferenceHelper.saveCurrentVolumeAsHeadsetPref(this);
 			PreferenceHelper.saveCurrentVolumeAsSpeakerPref(this);
 		}
 		
 		showNotification();
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		return START_STICKY;
 	}
 	
 	@Override
 	public void onDestroy() {
 		unregisterReceiver(volume_receiver);
 		stopForeground(true);
 	}
 	
 	private void showNotification() {
         CharSequence text = getString(R.string.notification_text);
         Notification notification = new Notification(R.drawable.ic_launcher, text,
                 java.lang.System.currentTimeMillis());
 
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, VolumeMiserActivity.class), 0);
 
         notification.setLatestEventInfo(this, getString(R.string.app_name), 
         		getString(R.string.notification_action_text), contentIntent);
         // So we don't get killed
         startForeground(VOLUMEMISER_NOTIF_ID, notification);
     }
 }
