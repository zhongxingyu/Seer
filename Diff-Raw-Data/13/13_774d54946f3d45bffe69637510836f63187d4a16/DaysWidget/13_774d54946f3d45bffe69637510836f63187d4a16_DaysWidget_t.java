 package fi.wegar.android.dayssince;
 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.widget.RemoteViews;
 
 public class DaysWidget extends AppWidgetProvider {
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		
 		final int N = appWidgetIds.length;
 		
 		// calculate the number of days since new years
 		GregorianCalendar now = new GregorianCalendar();
 		
		// create a date and string representing the new year
		GregorianCalendar newYearsDay = new GregorianCalendar( now.get(GregorianCalendar.YEAR), 0, 1);
		DateFormat fmt = new SimpleDateFormat("MMM dd, yyyy ");
		
 		// perform updates on each running widget instance
 		for(int i = 0; i < N; i++) {
 			
 			int appWidgetId = appWidgetIds[i];
 			
 			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
 			remoteViews.setTextViewText(R.id.widget_textview, ""+now.get(GregorianCalendar.DAY_OF_YEAR) );
			
			remoteViews.setTextViewText(R.id.widget_header, context.getText(R.string.headerDisplay)+" "+fmt.format( newYearsDay.getTime() ) );
 
 			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
 
 		}
 	}
 }
