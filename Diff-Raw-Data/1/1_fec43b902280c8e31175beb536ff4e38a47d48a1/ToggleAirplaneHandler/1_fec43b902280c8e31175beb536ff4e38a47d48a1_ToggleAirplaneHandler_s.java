 package com.carolineleung.clickcontrols.handler;
 
 import android.content.Context;
 import android.content.Intent;
 import android.provider.Settings;
 import android.widget.RemoteViews;
 
 import com.carolineleung.clickcontrols.R;
 
 public class ToggleAirplaneHandler implements WidgetActionHandler {
 
 	@Override
 	public void run(Context context, Intent intent, RemoteViews remoteViews) {
 		boolean isEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
 		if (isEnabled) {
 			remoteViews.setImageViewResource(R.id.toggleAirplaneMode, R.drawable.toggle_airplane_on);
 		} else {
 			remoteViews.setImageViewResource(R.id.toggleAirplaneMode, R.drawable.toggle_airplane_off);
 		}
 		Intent airplaneSettingsIntent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
 		airplaneSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(airplaneSettingsIntent);
 	}
 
 }
