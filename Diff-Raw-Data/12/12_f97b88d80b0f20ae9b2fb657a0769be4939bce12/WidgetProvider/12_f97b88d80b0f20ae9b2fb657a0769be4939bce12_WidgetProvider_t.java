 package com.anirudhr.timeMan.widget;
 
import com.anirudhr.timeMan.GlobalAccess;
 import com.anirudhr.timeMan.Main;
 import com.anirudhr.timeMan.R;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.RemoteViews;
 
 public class WidgetProvider extends AppWidgetProvider {
 		@Override
 		public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 				int[] appWidgetIds) {
 			super.onUpdate(context, appWidgetManager, appWidgetIds);
 			
 			for (int i=0; i<appWidgetIds.length; i++) {
 				int appWidgetId = appWidgetIds[i];
 			    Intent intent;
 			    PendingIntent pendingIntent;
 			    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
 			    
 			    
 				/*now set Update action*/			
 				intent = new Intent(context, WidgetService.class);
 			    intent.setAction(WidgetService.UPDATEWIDGET);
 			    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 			    pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 			    views.setOnClickPendingIntent(R.id.widget_rest, pendingIntent);
 			    appWidgetManager.updateAppWidget(appWidgetId, views);
 			    
 			    
 				/*On-click on entire widget*/
 				intent = new Intent(context, Main.class);
 				pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 				views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
 				appWidgetManager.updateAppWidget(appWidgetId, views);
				
				GlobalAccess.updateWidget(context);
 			}
 		
 		}
 }
