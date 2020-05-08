 package com.foldor.wordclock;
 
 import android.appwidget.AppWidgetManager;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 import android.view.KeyEvent;
 
 public class Preferences extends PreferenceActivity {
     private static final String TAG = "WordClock";
     private static final String CONFIGURE_ACTION = "android.appwidget.action.APPWIDGET_CONFIGURE";
     
    @SuppressWarnings("deprecation")
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             Intent activityIntent = getIntent();
             
             if (CONFIGURE_ACTION.equals(activityIntent.getAction())) {
                 Bundle extras = activityIntent.getExtras();
                 
                 if (extras != null) {
                     int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                     
                     //Put the widget ID into the extras and let the activity caller know the result is ok.
                     Intent result = new Intent();
                     result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                     
                     setResult(RESULT_OK, result);
                     
                     //Start the widget timer.
                     Intent enableWidget = new Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_ENABLED, Uri.EMPTY, this,
                             WordClockReceiver.class);
                     
                     sendBroadcast(enableWidget);
                 }
                 else {
                     Log.d(TAG, "Intent Extras is null");
                 }
             }
             else {
                 Log.d(TAG, "Intent Action is: {" + activityIntent.getAction() + "} and not: " + CONFIGURE_ACTION);
             }
         }
         
         return (super.onKeyDown(keyCode, event));
     }
 }
