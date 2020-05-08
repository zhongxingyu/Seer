 package ru.shutoff.caralarm;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.RemoteViews;
 
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalDateTime;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class CarWidget extends AppWidgetProvider {
 
     CarDrawable drawable;
 
     static final int STATE_UPDATE = 1;
     static final int STATE_ERROR = 2;
 
     static Map<String, Integer> states;
 
     static final String HEIGHT = "Height_";
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         super.onUpdate(context, appWidgetManager, appWidgetIds);
         Intent intent = new Intent(context, WidgetService.class);
         context.startService(intent);
         for (int id : appWidgetIds) {
             updateWidget(context, appWidgetManager, id);
         }
     }
 
     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         super.onDeleted(context, appWidgetIds);
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
         SharedPreferences.Editor ed = preferences.edit();
         for (int id : appWidgetIds) {
             ed.remove(HEIGHT + id);
             ed.remove(Names.WIDGET + id);
         }
         ed.commit();
     }
 
     @Override
     public void onEnabled(Context context) {
         super.onEnabled(context);
         Intent intent = new Intent(context, WidgetService.class);
         context.startService(intent);
         updateWidgets(context, null);
     }
 
     @Override
     public void onDisabled(Context context) {
         super.onDisabled(context);
         Intent i = new Intent(context, WidgetService.class);
         i.setAction(WidgetService.ACTION_STOP);
         context.startService(i);
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         if (intent != null) {
             String action = intent.getAction();
             if (action != null) {
                 if (states == null)
                     states = new HashMap<String, Integer>();
                 String car_id = intent.getStringExtra(Names.ID);
                 if (action.equalsIgnoreCase(FetchService.ACTION_UPDATE)) {
                     states.remove(car_id);
                     updateWidgets(context, car_id);
                     Intent i = new Intent(context, WidgetService.class);
                     i.setAction(WidgetService.ACTION_UPDATE);
                     context.startService(i);
                 }
                 if (action.equalsIgnoreCase(FetchService.ACTION_NOUPDATE)) {
                     if (states.containsKey(car_id)) {
                         states.remove(car_id);
                         updateWidgets(context, car_id);
                     }
                 }
                 if (action.equalsIgnoreCase(FetchService.ACTION_ERROR)) {
                     if (!states.containsKey(car_id) || (states.get(car_id) != STATE_ERROR)) {
                         states.put(car_id, STATE_ERROR);
                         updateWidgets(context, car_id);
                     }
                 }
                 if (action.equalsIgnoreCase(FetchService.ACTION_START)) {
                     if (!states.containsKey(car_id) || (states.get(car_id) != STATE_UPDATE)) {
                         states.put(car_id, STATE_UPDATE);
                         updateWidgets(context, car_id);
                     }
                 }
                 if (action.equalsIgnoreCase(FetchService.ACTION_START_UPDATE)) {
                     if (!states.containsKey(car_id) || (states.get(car_id) != STATE_UPDATE)) {
                         states.put(car_id, STATE_UPDATE);
                         updateWidgets(context, car_id);
                         Intent i = new Intent(context, FetchService.class);
                         i.putExtra(Names.ID, car_id);
                         context.startService(i);
                     }
                 }
                 if (action.equalsIgnoreCase(Intent.ACTION_TIMEZONE_CHANGED)) {
                    DateTimeZone tz = DateTimeZone.getDefault();
                     DateTimeZone.setDefault(tz);
                     updateWidgets(context, null);
                 }
             }
         }
         super.onReceive(context, intent);
     }
 
     @Override
     public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                           int appWidgetId, Bundle newOptions) {
         int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
         SharedPreferences.Editor ed = preferences.edit();
         ed.putInt(HEIGHT + appWidgetId, minHeight);
         ed.commit();
         updateWidget(context, appWidgetManager, appWidgetId);
     }
 
     void updateWidgets(Context context, String car_id) {
         ComponentName thisAppWidget = new ComponentName(
                 context.getPackageName(), getClass().getName());
         AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         if (appWidgetManager != null) {
             int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
             SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
             for (int appWidgetID : ids) {
                 if ((car_id == null) || preferences.getString(Names.WIDGET + appWidgetID, "").equals(car_id))
                     updateWidget(context, appWidgetManager, appWidgetID);
             }
         }
     }
 
     void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID) {
 
         RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
 
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
         String car_id = Preferences.getCar(preferences, preferences.getString(Names.WIDGET + widgetID, ""));
 
         Intent configIntent = new Intent(context, MainActivity.class);
         configIntent.putExtra(Names.ID, car_id);
         configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         PendingIntent pIntent = PendingIntent.getActivity(context, widgetID, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         widgetView.setOnClickPendingIntent(R.id.widget, pIntent);
 
         configIntent = new Intent(FetchService.ACTION_START_UPDATE);
         configIntent.putExtra(Names.ID, car_id);
         pIntent = PendingIntent.getBroadcast(context, widgetID, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         widgetView.setOnClickPendingIntent(R.id.update_block, pIntent);
 
         long last = preferences.getLong(Names.EVENT_TIME + car_id, 0);
         Date now = new Date();
         if (last > now.getTime() - 24 * 60 * 60 * 1000) {
             LocalDateTime d = new LocalDateTime(last);
             widgetView.setTextViewText(R.id.last, d.toString("HH:mm"));
         } else {
             widgetView.setTextViewText(R.id.last, "??:??");
         }
 
         widgetView.setTextViewText(R.id.voltage, preferences.getString(Names.VOLTAGE_MAIN + car_id, "--") + " V");
         widgetView.setTextViewText(R.id.reserve, preferences.getString(Names.VOLTAGE_RESERVED + car_id, "--") + " V");
         widgetView.setTextViewText(R.id.balance, preferences.getString(Names.BALANCE + car_id, "---.--"));
         String temperature = Preferences.getTemperature(preferences, car_id);
         if (temperature == null) {
             widgetView.setViewVisibility(R.id.temperature_block, View.GONE);
         } else {
             widgetView.setTextViewText(R.id.temperature, temperature);
             widgetView.setViewVisibility(R.id.temperature_block, View.VISIBLE);
         }
 
         int height = preferences.getInt(HEIGHT + widgetID, 40);
         widgetView.setViewVisibility(R.id.reserve_block, (height < 60) ? View.GONE : View.VISIBLE);
 
         if ((states == null) || !states.containsKey(car_id)) {
             widgetView.setViewVisibility(R.id.refresh, View.VISIBLE);
             widgetView.setViewVisibility(R.id.update, View.GONE);
             widgetView.setViewVisibility(R.id.error, View.GONE);
         } else {
             int state = states.get(car_id);
             if (state == STATE_ERROR) {
                 widgetView.setViewVisibility(R.id.refresh, View.GONE);
                 widgetView.setViewVisibility(R.id.update, View.GONE);
                 widgetView.setViewVisibility(R.id.error, View.VISIBLE);
             } else {
                 widgetView.setViewVisibility(R.id.refresh, View.GONE);
                 widgetView.setViewVisibility(R.id.update, View.VISIBLE);
                 widgetView.setViewVisibility(R.id.error, View.GONE);
             }
         }
 
         if (drawable == null)
             drawable = new CarDrawable(context);
 
         drawable.update(preferences, car_id);
         widgetView.setImageViewBitmap(R.id.car, drawable.getBitmap());
 
         appWidgetManager.updateAppWidget(widgetID, widgetView);
     }
 
 }
