 package com.optedoblivion.MinimalNewsWidget.widget;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
import com.optedoblivion.MinimalNewsWidget.R;
import com.optedoblivion.MinimalNewsWidget.R.layout;
import com.optedoblivion.MinimalNewsWidget.data.DBAdapter;
import com.optedoblivion.MinimalNewsWidget.service.UpdaterService;
import com.optedoblivion.MinimalNewsWidget.settings.MinimalNewsWidgetConfigure;

 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.TextView;
 
 public class MinimalNewsWidgetProvider extends AppWidgetProvider {
     private static HashMap<String, Timer> timers = null;
     private static final String PREFS_NAME
             = "com.optedoblivion.MinimalNewsWidget.MinimalNewsWidgetProvider";
     private static SharedPreferences prefs;
     private static final String COMPLETED_PREFIX_KEY = "completed_";
 
     public static void cancelTimer(String key){
         if (timers.containsKey(key) && timers.get(key) != null){
             timers.get(key).cancel();
             timers.remove(key);
         }
     }
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, 
                                                           int[] appWidgetIds){
         super.onUpdate(context, appWidgetManager, appWidgetIds);
         if (timers == null){
             timers = new HashMap<String, Timer>();
         }
         if (appWidgetIds.length > 0){
             int appWidgetId = appWidgetIds[0];
             String key = String.valueOf(appWidgetId);
             cancelTimer(key);
             Timer timer = new Timer();
             timer.scheduleAtFixedRate(new WidgetUpdateTimer(context, 
                                     appWidgetManager, appWidgetId), 1, 15000);
             timer.scheduleAtFixedRate(new ServiceTimer(context, 
                                                      appWidgetId), 1, 3600000);
             timers.put(key, timer);
         }
     }
 
     @Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         if(appWidgetIds.length > 0){
             int appWidgetId = appWidgetIds[0];
             String key = String.valueOf(appWidgetId);
             MinimalNewsWidgetConfigure.deleteTitlePref(
                                                     context, appWidgetIds[0]);
             cancelTimer(key);
             DBAdapter dbAdapter = new DBAdapter(context);
             dbAdapter.clearFeeds(appWidgetId);
         }
     }
 
 
     class ServiceTimer extends TimerTask {
         private static final String TAG = "ServiceTimer";
         Context mContext;
         int appWidgetId;
         public ServiceTimer(Context context, int appWidgetId){
             mContext = context;
             this.appWidgetId = appWidgetId;
         }
 
         @Override
         public void run() {
           Intent updaterServiceIntent = new Intent(mContext, 
                                                         UpdaterService.class);
           updaterServiceIntent.setAction(
                          "com.optedoblivion.MinimalNewsWidget.START_SERVICE");
           updaterServiceIntent.addFlags(this.appWidgetId);
           mContext.startService(updaterServiceIntent);
         }
     }
 
     class WidgetUpdateTimer extends TimerTask {
         private static final String TAG = "WidgetUpdateTimer";
         AppWidgetManager appWidgetManager;
         Context mContext;
         int appWidgetId;
 
         TextView tView = null;
         DBAdapter dbAdapter = null;
         // Use this to emulate caching, so we don't hit the db every time we 
         // update.
         ArrayList<ArrayList<String>> feeds = 
                                            new ArrayList<ArrayList<String>>();
         int currentFeed = 0;
         int maxFeeds = 0;
 
         public WidgetUpdateTimer(Context context, 
                       AppWidgetManager appWidgetManager, int appWidgetId) {
             this.appWidgetManager = appWidgetManager;
             this.appWidgetId = appWidgetId;
             mContext = context;
             currentFeed = 0;
             maxFeeds = 0;
         }
 
         @Override
         public void run() {
             if (currentFeed >= maxFeeds){
                 ArrayList<String> tmp;
                 dbAdapter = new DBAdapter(mContext);
                 Cursor items = dbAdapter.getFeeds(appWidgetId);
                 if (items!=null){
                     int itemCount = items.getCount();
                     int c;
                     for(c=0;c<feeds.size();c++){
                         feeds.remove(c);
                     }
                     for(c=0;c<itemCount;c++){
                         items.moveToPosition(c);
                         String title = items.getString(
                                            items.getColumnIndex("title"));
                         String link = items.getString(
                                             items.getColumnIndex("link"));
                         tmp = new ArrayList<String>();
                         tmp.add(title);
                         tmp.add(link);
                         feeds.add(tmp);
                     }
                     maxFeeds = itemCount;
                     currentFeed = 0;
                 }
                 items.close();
                 dbAdapter.closeDb();
             }
             RemoteViews views = new RemoteViews(mContext.getPackageName(),
                     R.layout.minimal_news_widget);
             if (!feeds.isEmpty()){
                 String displayTitle = feeds.get(currentFeed).get(0);
                 String displayLink = feeds.get(currentFeed).get(1);
                 currentFeed++;
                 if (displayLink != null){
                     Intent intent = new Intent(Intent.ACTION_VIEW, 
                                                       Uri.parse(displayLink));
                     PendingIntent pendingIntent = PendingIntent.getActivity(
                                                   mContext, 0, intent, 0);
                     views.setOnClickPendingIntent(R.id.TextView01, 
                                                                pendingIntent);
                 }
                 views.setTextViewText(R.id.TextView01, displayTitle);
             }
             appWidgetManager.updateAppWidget(appWidgetId, views);
         }
     
     }
 }
