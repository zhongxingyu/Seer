 package net.mms_projects.copyit.ui.android.widgets;
 
 import net.mms_projects.copyit.R;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.RemoteViews;
 
 public class WidgetPasteIt extends WidgetProvider {
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		final int N = appWidgetIds.length;
 
 		for (int i = 0; i < N; i++) {
 			int appWidgetId = appWidgetIds[i];
 
			Intent intentPasteIt = new Intent(context, WidgetProvider.class);
 			intentPasteIt.setAction(ACTION_PASTEIT);
 			PendingIntent pendingIntentPasteIt = PendingIntent.getBroadcast(
 					context, 0, intentPasteIt, 0);
 
 			RemoteViews views = new RemoteViews(context.getPackageName(),
 					R.layout.widget_pasteit);
 			views.setOnClickPendingIntent(R.id.button_paste, pendingIntentPasteIt);
 
 			appWidgetManager.updateAppWidget(appWidgetId, views);
 		}
 	}
 	
 }
