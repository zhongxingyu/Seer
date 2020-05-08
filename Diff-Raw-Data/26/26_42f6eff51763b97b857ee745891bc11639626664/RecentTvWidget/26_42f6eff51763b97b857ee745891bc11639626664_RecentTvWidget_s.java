 /*
  * Copyright (C) 2011 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.anderspersson.xbmcwidget.recenttv;
 
 import com.anderspersson.xbmcwidget.common.Timer;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 
 public class RecentTvWidget extends AppWidgetProvider {
 
 	public static final String RECENT_TV_UPDATE_WIDGET = "com.anderspersson.xbmcwidget.recenttv.UPDATE";
	public static final String RECENT_TV_REFRESH = "com.anderspersson.xbmcwidget.recenttv.UPDATE_EPISODES";
 	
 	@Override
     public void onDeleted(Context context, int[] appWidgetIds) {
         super.onDeleted(context, appWidgetIds);
     }
 
     @Override
     public void onDisabled(Context context) {
         super.onDisabled(context);
         Timer timer = new Timer(createRefreshIntent(context));
         timer.disable(context);
     }
 
     @Override
     public void onEnabled(Context context) {
        super.onEnabled(context);
        Timer timer = new Timer(createRefreshIntent(context));
        timer.enable(context);
     }
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) { 
     	super.onUpdate(context, appWidgetManager, appWidgetIds);
     	
     	for (int i = 0; i < appWidgetIds.length; ++i) {
     		Intent updateIntent = createUpdateIntent(context);
     		updateIntent.setAction(RECENT_TV_UPDATE_WIDGET);
     		updateIntent.putExtra("widgetId", appWidgetIds[i]);
     		context.startService(updateIntent);
     	}   	
     }
     
 	private Intent createUpdateIntent(Context context) {
 		return new Intent(context, getRecentTvWidgetRenderIntentService());
 	}
 	
 	private PendingIntent createRefreshIntent(Context context) {
        Intent intent = new Intent(context, getRecentTvWidgetRenderIntentService());
        intent.setAction(RECENT_TV_REFRESH);
         return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 	}
 	
 	private Class<?> getRecentTvWidgetRenderIntentService() {
 		int honeycombVersion = 11;
 		return Build.VERSION.SDK_INT < honeycombVersion 
 				? RecentTvWidgetRenderIntentService.class
 				: RecentTvWidgetRenderIntentServiceHC.class; 
 	}
 }
