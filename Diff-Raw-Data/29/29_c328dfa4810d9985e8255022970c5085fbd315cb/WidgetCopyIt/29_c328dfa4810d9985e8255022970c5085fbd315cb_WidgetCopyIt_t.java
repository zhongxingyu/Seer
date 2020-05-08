 package net.mms_projects.copyit.ui.android.widgets;
 
 import net.mms_projects.copyit.R;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.RemoteViews;
 
 public class WidgetCopyIt extends WidgetProvider {
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		final int N = appWidgetIds.length;
 
 		for (int i = 0; i < N; i++) {
 			int appWidgetId = appWidgetIds[i];
 
			Intent intentCopyIt = new Intent(context, this.getClass());
 			intentCopyIt.setAction(ACTION_COPYIT);
 			PendingIntent pendingIntentCopyIt = PendingIntent.getBroadcast(
 					context, 0, intentCopyIt, 0);
 
 			RemoteViews views = new RemoteViews(context.getPackageName(),
 					R.layout.widget_copyit);
 			views.setOnClickPendingIntent(R.id.button_copy, pendingIntentCopyIt);
 
 			appWidgetManager.updateAppWidget(appWidgetId, views);
 		}
 	}
 	
 }
