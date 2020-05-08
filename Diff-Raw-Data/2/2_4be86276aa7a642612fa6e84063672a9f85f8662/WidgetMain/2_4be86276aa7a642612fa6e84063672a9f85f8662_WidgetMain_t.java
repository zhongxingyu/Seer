 package mobi.wiegandtech.countingtheomer;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.RemoteViews;
 
 public class WidgetMain extends AppWidgetProvider {
 	protected static final int DIALOG_MENU = 435668;
 
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds) {
 		final int N = appWidgetIds.length;
 
 		// Perform this loop procedure for each App Widget that belongs to this
 		// provider
 		for (int i = 0; i < N; i++) {
 			int appWidgetId = appWidgetIds[i];
 			updateAppWidget(context, appWidgetId);
 		}
 	}
 
 	public static void updateAppWidget(Context context, int appWidgetId) {
 		RemoteViews remoteView = new RemoteViews(context.getPackageName(),
 				R.layout.main);
 
 		remoteView.setTextViewText(R.id.TextView01, getOmerText());
 
 		Intent it = new Intent(context, Blessing.class);
 		PendingIntent pi = PendingIntent.getActivity(context, 0, it, 0);
 		remoteView.setOnClickPendingIntent(R.id.TextView01, pi);
 
 		// have to call this AFTER setOnClickPendingIntent!
 		AppWidgetManager mgr = AppWidgetManager.getInstance(context);
 		if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
 			ComponentName me = new ComponentName(context, WidgetMain.class);
 			mgr.updateAppWidget(me, remoteView);
 		} else {
 			mgr.updateAppWidget(appWidgetId, remoteView);
 		}
 	}
 
 	public static String getOmerText() {
 		long dayOfOmer = getDayOfOmer();
 		if (dayOfOmer == 0)
 			return "We start to count the Omer tomorrow!";
 		if (dayOfOmer < 1)
 			return Math.abs(dayOfOmer - 1) + " days until the Omer!";
 		if (dayOfOmer > 49)
 			return "Done counting the omer for this year! Congrats!";
 		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 16) // 4pm+
 			return "Tonight is day " + (dayOfOmer) + " of the omer.";
		else if (dayOfOmer == 0 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 16)
			return "Tonight is the first day of the omer.";
 		else
 			return "Last night / today is day " + (dayOfOmer - 1)
 					+ " of the omer.";
 	}
 
 	public static int getDayOfOmer() {
 		long now = convertMillisToDays(Calendar.getInstance());
 
 		//Calendar testCal = GregorianCalendar.getInstance();
 		//testCal.set(2011, 3, 20, 9, 10, 10); // test - last night of omer
 		//now = convertMillisToDays(testCal);
 
 		Calendar startOfOmerCal = GregorianCalendar.getInstance();
 		switch (startOfOmerCal.get(Calendar.YEAR)) {
 		case 2011:
 			startOfOmerCal.set(2011, 3, 19); // April 19th
 			break;
 		case 2012:
 			startOfOmerCal.set(2012, 3, 7); // April 7th
 			break;
 		case 2013:
 			startOfOmerCal.set(2013, 2, 26); // March 26th
 			break;
 		case 2014:
 			startOfOmerCal.set(2014, 3, 15); // April 15th
 			break;
 		case 2015:
 			startOfOmerCal.set(2015, 3, 4); // April 4th
 			break;
 		}
 		// I HATE JAVA MONTHS STARTING AT 0!!!
 		long startOfOmer = convertMillisToDays(startOfOmerCal);
 		// get days
 		return (int) (now - startOfOmer) + 1;
 	}
 
 	private static long convertMillisToDays(Calendar value) {
 		value.set(Calendar.HOUR_OF_DAY, 0);
 		value.set(Calendar.MINUTE, 0);
 		value.set(Calendar.SECOND, 0);
 		return value.getTimeInMillis() / 1000 / 60 / 60 / 24;
 	}
 }
