 /* 
  * Copyright 2013 Wouter Pinnoo
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.pinnoo.garbagecalendar.ui.widget;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.widget.RemoteViews;
 import eu.pinnoo.garbagecalendar.R;
 import eu.pinnoo.garbagecalendar.data.AreaType;
 import static eu.pinnoo.garbagecalendar.data.AreaType.L;
 import static eu.pinnoo.garbagecalendar.data.AreaType.V;
 import eu.pinnoo.garbagecalendar.data.Collection;
 import eu.pinnoo.garbagecalendar.data.CollectionsData;
 import eu.pinnoo.garbagecalendar.data.LocalConstants;
 import eu.pinnoo.garbagecalendar.data.Type;
 import eu.pinnoo.garbagecalendar.data.UserData;
 import eu.pinnoo.garbagecalendar.data.caches.AddressCache;
 import eu.pinnoo.garbagecalendar.data.caches.CollectionCache;
 import eu.pinnoo.garbagecalendar.data.caches.UserAddressCache;
 import eu.pinnoo.garbagecalendar.ui.CollectionListActivity;
 import eu.pinnoo.garbagecalendar.ui.preferences.AddressListActivity;
 import eu.pinnoo.garbagecalendar.util.parsers.CalendarParser;
 import eu.pinnoo.garbagecalendar.util.parsers.Parser.Result;
 import static eu.pinnoo.garbagecalendar.util.parsers.Parser.Result.UNKNOWN_ERROR;
 import eu.pinnoo.garbagecalendar.util.tasks.CacheTask;
 import eu.pinnoo.garbagecalendar.util.tasks.ParserTask;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Wouter Pinnoo <pinnoo.wouter@gmail.com>
  */
 public class WidgetProvider extends AppWidgetProvider {
 
     private final String SET_BACKGROUND_COLOR = "setBackgroundColor";
     private final String SET_BACKGROUND_RES = "setBackgroundResource";
     private Context c;
     private AppWidgetManager appWidgetManager;
 
     @Override
     public void onUpdate(Context c, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         this.c = c;
         this.appWidgetManager = appWidgetManager;
 
         AddressCache.initialize(c);
         CollectionCache.initialize(c);
         UserAddressCache.initialize(c);
 
         if (c.getSharedPreferences("PREFERENCE", Activity.MODE_PRIVATE).getBoolean(LocalConstants.CacheName.COL_REFRESH_NEEDED.toString(), true)
                 || !CollectionsData.getInstance().isSet()) {
             initializeCacheAndLoadData();
         } else {
             updateWidgetView();
         }
     }
 
     public void updateWidgetErrorView(String msg, Class target) {
         ComponentName thisWidget = new ComponentName(c, WidgetProvider.class);
         int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
         for (int widgetId : allWidgetIds) {
             RemoteViews remoteViews = new RemoteViews(c.getPackageName(), R.layout.widget_error_layout);
             remoteViews.setTextViewText(R.id.widget_error_message, msg);
 
             Intent intent = new Intent(c, target);
             PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);
 
             remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);
             appWidgetManager.updateAppWidget(widgetId, remoteViews);
         }
     }
 
     public void updateWidgetView() {
         ComponentName thisWidget = new ComponentName(c, WidgetProvider.class);
         int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
         for (int widgetId : allWidgetIds) {
             List<Collection> cols = CollectionsData.getInstance().getCollections();
             Iterator<Collection> it = cols.iterator();
             Calendar dayToBeShown = Calendar.getInstance();
             dayToBeShown.add(Calendar.DATE, -1);
             Collection col = null;
             while (it.hasNext()) {
                 col = it.next();
                if (col.getDate().after(dayToBeShown.getTime())) {
                     break;
                 }
             }
 
             RemoteViews remoteViews = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
 
             if (col != null && UserData.getInstance().isSet()) {
                 String date = LocalConstants.DateFormatType.WIDGET.getDateFormatter(c).format(col.getDate());
                 remoteViews.setTextViewText(R.id.widget_date, date);
 
                 AreaType currentAreaType = UserData.getInstance().getAddress().getSector().getType();
                 int backgroundColor = Color.argb(0, 0, 0, 0);
 
                 boolean hasType = col.hasType(Type.REST);
                 remoteViews.setTextViewText(R.id.widget_rest, hasType ? Type.REST.shortStrValue(c) : "");
                 if (hasType) {
                     switch (currentAreaType) {
                         case L:
                             remoteViews.setInt(R.id.widget_rest, SET_BACKGROUND_RES, R.drawable.widget_rest_l_activated_shape);
                             break;
                         case V:
                         default:
                             remoteViews.setInt(R.id.widget_rest, SET_BACKGROUND_RES, R.drawable.widget_rest_v_activated_shape);
                     }
                 } else {
                     remoteViews.setInt(R.id.widget_rest, SET_BACKGROUND_COLOR, backgroundColor);
                 }
 
                 hasType = col.hasType(Type.GFT);
                 remoteViews.setTextViewText(R.id.widget_gft, hasType ? Type.GFT.shortStrValue(c) : "");
                 remoteViews.setInt(R.id.widget_gft, SET_BACKGROUND_COLOR, hasType ? Type.GFT.getColor(c, currentAreaType) : backgroundColor);
 
                 hasType = col.hasType(Type.PMD);
                 remoteViews.setTextViewText(R.id.widget_pmd, hasType ? Type.PMD.shortStrValue(c) : "");
                 remoteViews.setInt(R.id.widget_pmd, SET_BACKGROUND_COLOR, hasType ? Type.PMD.getColor(c, currentAreaType) : backgroundColor);
 
                 hasType = col.hasType(Type.PK);
                 remoteViews.setTextViewText(R.id.widget_pk, hasType ? Type.PK.shortStrValue(c) : "");
                 remoteViews.setInt(R.id.widget_pk, SET_BACKGROUND_COLOR, hasType ? Type.PK.getColor(c, currentAreaType) : backgroundColor);
 
                 hasType = col.hasType(Type.GLAS);
                 remoteViews.setTextViewText(R.id.widget_glas, hasType ? Type.GLAS.shortStrValue(c) : "");
                 if (hasType) {
                     remoteViews.setInt(R.id.widget_glas, SET_BACKGROUND_RES, R.drawable.widget_glas_activated_shape);
                 } else {
                     remoteViews.setInt(R.id.widget_glas, SET_BACKGROUND_COLOR, backgroundColor);
                 }
             } else {
                 remoteViews.setTextViewText(R.id.widget_date, c.getString(R.string.none));
             }
 
             Intent intent = new Intent(c, CollectionListActivity.class);
             PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);
             remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);
             appWidgetManager.updateAppWidget(widgetId, remoteViews);
         }
     }
 
     private void setLoadingView() {
         ComponentName thisWidget = new ComponentName(c, WidgetProvider.class);
         int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
         for (int widgetId : allWidgetIds) {
             RemoteViews remoteViews = new RemoteViews(c.getPackageName(), R.layout.widget_loading_layout);
             appWidgetManager.updateAppWidget(widgetId, remoteViews);
         }
     }
 
     private void initializeCacheAndLoadData() {
         new CacheTask() {
             @Override
             protected void onPreExecute() {
                 super.onPreExecute();
                 setLoadingView();
             }
 
             @Override
             protected void onPostExecute(Integer[] result) {
                 super.onPostExecute(result);
                 checkAddress();
             }
         }.execute(CollectionsData.getInstance(), UserData.getInstance());
     }
 
     private void checkAddress() {
         if (UserData.getInstance().isSet()) {
             loadCollections(UserData.getInstance().isChanged());
         } else {
             updateWidgetErrorView(c.getString(R.string.widget_setAddress), AddressListActivity.class);
         }
     }
 
     private void loadCollections(boolean force) {
         if (!force && CollectionsData.getInstance().isSet()) {
             updateWidgetView();
         } else {
             if (!UserData.getInstance().isSet()) {
                 updateWidgetErrorView(c.getString(R.string.widget_setAddress), AddressListActivity.class);
             }
             new ParserTask(c) {
                 @Override
                 protected void onPostExecute(Result[] result) {
                     super.onPostExecute(result);
                     switch (result[0]) {
                         case SUCCESSFUL:
                             UserData.getInstance().changeCommitted();
                             updateWidgetView();
                             break;
                         case NO_INTERNET_CONNECTION:
                             updateWidgetErrorView(c.getString(R.string.widget_noAvailableConnection), CollectionListActivity.class);
                             break;
                         case EMPTY_RESPONSE:
                         case CONNECTION_FAIL:
                         case UNKNOWN_ERROR:
                             updateWidgetErrorView(c.getString(R.string.widget_unknownError), CollectionListActivity.class);
                             break;
                     }
                 }
             }.execute(new CalendarParser());
         }
     }
 }
