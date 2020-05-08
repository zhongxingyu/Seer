 package com.richardhalldev.stackwidgetexample;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.widget.RemoteViews;
 
 import com.richardhalldev.stackwidgetexample.R;
 
 public class WidgetProvider extends AppWidgetProvider {
 	
 	public static final String EXTRA_ITEM = "com.example.android.stackwidget.EXTRA_ITEM";
 
 	@Override
 	public void onUpdate (Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		for (int appWidgetId : appWidgetIds) {
 			// Set up the intent to load the views
        		Intent intent = new Intent(context, WidgetService.class);
        		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
             
         		// Instantiate the RemoteViews
         		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
         		rv.setRemoteAdapter(appWidgetId, R.id.stack_view, intent);
         		// Set up the empty view for an empty collection (Here we are just using the loading view)
         		rv.setEmptyView(R.id.stack_view, R.id.empty_view);
             
         		appWidgetManager.updateAppWidget(appWidgetId, rv);
 		}
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 	}
}
