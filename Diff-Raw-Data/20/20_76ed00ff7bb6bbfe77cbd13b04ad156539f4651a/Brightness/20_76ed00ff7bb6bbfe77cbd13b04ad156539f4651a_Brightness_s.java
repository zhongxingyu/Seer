 package com.svenhenrik.brightnesswidget;
 
 import android.app.PendingIntent;
 import android.app.PendingIntent.CanceledException;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class Brightness extends AppWidgetProvider {
 
 	public static String ACTION_WIDGET_RECEIVER = "com.svenhenrik.brightnesswidget.ActionReceiverWidget";
 	public static String ACTION_WIDGET_SETBRIGHTNESS = "com.svenhenrik.brightnesswidget.ActionSetBrightness";
	public static boolean auto_brightness; 
 	
     private static final String SCREEN_BRIGHTNESS_MODE = "screen_brightness_mode";
     private static final int SCREEN_BRIGHTNESS_MODE_MANUAL = 0;
     private static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1;
     private int m_appWidgetId;
 	
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
     	int brightness_mode = SCREEN_BRIGHTNESS_MODE_MANUAL;
     	try {
     		brightness_mode = Settings.System.getInt(context.getContentResolver(), SCREEN_BRIGHTNESS_MODE);
     	} catch (SettingNotFoundException e) {
    		auto_brightness = false;
     	}
     	auto_brightness = (brightness_mode == SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
     	
         //final int N = appWidgetIds.length;
 
         // Perform this loop procedure for each App Widget that belongs to this provider
         //for (int i=0; i<N; i++) {
             m_appWidgetId = appWidgetIds[0]; //i
 
             Intent toggleIntent = new Intent(context, Brightness.class);
             toggleIntent.setAction(ACTION_WIDGET_RECEIVER);
             
 			PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, toggleIntent, 0);
 
             // Get the layout for the App Widget and attach an on-click listener to the button
             RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
             views.setOnClickPendingIntent(R.id.ImageView01, actionPendingIntent);
             if (auto_brightness) {
             	views.setTextViewText(R.id.TextView01, context.getText(R.string.auto_on));
             	views.setImageViewResource(R.id.ImageView01, R.drawable.light_on);
 
             }
             else { 
             	views.setTextViewText(R.id.TextView01, context.getText(R.string.auto_off));
            	views.setImageViewResource(R.id.ImageView01, R.drawable.light_off);            }
 
             // Tell the AppWidgetManager to perform an update on the current App Widget
             appWidgetManager.updateAppWidget(m_appWidgetId, views);
         //}
     }
     
     @Override
     public void onReceive(Context context, Intent intent) {
     	ContentResolver resolver = context.getContentResolver();
     	// check, if our Action was called
     	if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
             RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
     		if (auto_brightness) {
                 views.setTextViewText(R.id.TextView01, context.getText(R.string.auto_off));
             	views.setImageViewResource(R.id.ImageView01, R.drawable.light_off);
             	Settings.System.putInt(resolver, SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
     			Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 1);
     			Toast.makeText(context, R.string.turning_off_auto, Toast.LENGTH_SHORT).show();
 
     			// Start an activity to change brightness
     			Intent hiddenIntent = new Intent(context, HiddenActivity.class);
     			PendingIntent hiddenPendingIntent = PendingIntent.getActivity(context, 0, hiddenIntent, 0);
     			try {
 					hiddenPendingIntent.send();
 				} catch (CanceledException e) {
 					// Not a big deal
 				}
     		} else {
                 views.setTextViewText(R.id.TextView01, context.getText(R.string.auto_on));
             	views.setImageViewResource(R.id.ImageView01, R.drawable.light_on);
     			Settings.System.putInt(resolver, SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
     			Toast.makeText(context, R.string.turning_on_auto, Toast.LENGTH_SHORT).show();
     		}
     		auto_brightness = !auto_brightness;
     		
     		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
     		ComponentName cn = new ComponentName(context, Brightness.class);
     		appWidgetManager.updateAppWidget(cn, views);
     	}
     	super.onReceive(context, intent);
     }
 
 }
