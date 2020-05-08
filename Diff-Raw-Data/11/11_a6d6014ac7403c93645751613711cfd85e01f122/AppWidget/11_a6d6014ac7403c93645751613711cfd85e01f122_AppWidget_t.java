 package com.pTricKg.PhoneVolumeToggle;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.widget.RemoteViews;
 
 public class AppWidget extends AppWidgetProvider {
 	
 @Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getAction() == null) {
 			context.startService(new Intent(context, ToggleService.class));
 		}else {
 			super.onReceive(context, intent);
 		}
 }
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 	int[] appWidgetIds) {
 		
 		context.startService(new Intent(context, ToggleService.class));
 	}
 	
 	public static class ToggleService extends IntentService {
 		public ToggleService() {
 			super("AppWidget$ToggleService");
 		}
 		
 		@Override
 		protected void onHandleIntent(Intent intent) {
 			ComponentName me = new ComponentName(this, AppWidget.class);
 			AppWidgetManager mgr = AppWidgetManager.getInstance(this);
 			mgr.updateAppWidget(me, buildUpdate(this));
 		}
 		
 		private RemoteViews buildUpdate(Context context) {
 			RemoteViews updateViews = new
 			RemoteViews(context.getPackageName(),R.layout.widget);
 			
 			AudioManager audioManager = (AudioManager)context.getSystemService(Activity.AUDIO_SERVICE);
 				
				if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
 					updateViews.setImageViewResource(R.id.phoneState, R.drawable.phone_state_silent);
 					
 					audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				}else {
					updateViews.setImageViewResource(R.id.phoneState, R.drawable.phone_state_normal);
					
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
 				}
 				Intent i = new Intent(this, AppWidget.class);
 				
 				PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
 				updateViews.setOnClickPendingIntent(R.id.phoneState, pi);
 				return updateViews;
 		}
 	}
 }
