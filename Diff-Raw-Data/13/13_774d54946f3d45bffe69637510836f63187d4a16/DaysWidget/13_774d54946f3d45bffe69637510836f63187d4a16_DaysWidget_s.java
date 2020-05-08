 package fi.wegar.android.dayssince;
 
import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
import android.util.Log;
 import android.widget.RemoteViews;
 
 public class DaysWidget extends AppWidgetProvider {
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		
 		final int N = appWidgetIds.length;
 		
 		// calculate the number of days since new years
 		GregorianCalendar now = new GregorianCalendar();
 		
 		// perform updates on each running widget instance
 		for(int i = 0; i < N; i++) {
 			
 			int appWidgetId = appWidgetIds[i];
 			
 			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
 			remoteViews.setTextViewText(R.id.widget_textview, ""+now.get(GregorianCalendar.DAY_OF_YEAR) );
 
 			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
 
 		}
 	}
 }
