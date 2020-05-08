 package nl.q42.hue2.widgets;
 
 import java.util.Calendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class WidgetProvider extends AppWidgetProvider {	
 	private final static int UPDATE_INTERVAL = 5000;
 	
	private PendingIntent service = null;
 	
 	@Override
	public void onUpdate(Context context, AppWidgetManager widgetManager, int[] widgetIds) {
 		super.onUpdate(context, widgetManager, widgetIds);
 		
 		// Start alarm for running light state update service periodically
 		final Intent intent = new Intent(context, WidgetUpdateService.class);
		
		// Update service needs all widget ids
		int[] allWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
 		
 		if (service == null) {
 			service = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		}
 		
 		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); 
 		
 		Calendar time = Calendar.getInstance();
 		time.set(Calendar.MINUTE, 0);
 		time.set(Calendar.SECOND, 0);
 		time.set(Calendar.MILLISECOND, 0);
 		
 		alarmManager.cancel(service);
 		alarmManager.setRepeating(AlarmManager.RTC, time.getTime().getTime(), UPDATE_INTERVAL, service);
 	}
 	
 	@Override
 	public void onDeleted(Context context, int[] widgetIds) {
 		super.onDeleted(context, widgetIds);
 		
 		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
 		
 		for (int id : widgetIds) {
 			prefsEdit.remove("widget_" + id + "_ip");
 			prefsEdit.remove("widget_" + id + "_ids");
 		}
 		
 		prefsEdit.commit();
 	}
 	
 	@Override
 	public void onDisabled(Context context) {
 		super.onDisabled(context);
 		
 		// Stop running update service
 		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		alarmManager.cancel(service);
 	}
 }
