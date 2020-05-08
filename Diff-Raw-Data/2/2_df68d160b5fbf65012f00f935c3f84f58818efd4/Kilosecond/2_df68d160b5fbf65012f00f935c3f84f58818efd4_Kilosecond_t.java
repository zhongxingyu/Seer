 package com.thelastcitadel;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.widget.RemoteViews;
 
 public class Kilosecond extends AppWidgetProvider {
 	
 	public static String CLOCK_TICK = "com.thelastcitadel.CLOCK_TICK";
 	public static PendingIntent createClockTick(Context context){
 		Intent intent = new Intent(CLOCK_TICK);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		return pendingIntent;
 	}
 
 	@Override
 	public void onEnabled(Context context){
 		super.onEnabled(context);
 		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTimeInMillis(System.currentTimeMillis());
 		calendar.add(Calendar.SECOND, 1);
 		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 1000L,
 				createClockTick(context));
 	}
 	
 	@Override  
 	public void onDisabled(Context context) {  
 	        super.onDisabled(context);  
 	        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);  
 	        alarmManager.cancel(createClockTick(context));  
 	} 
 	
 	@Override
 	public void onReceive(Context context, Intent intent){
 		super.onReceive(context, intent);
 		if(CLOCK_TICK.equals(intent.getAction())){
 			ComponentName thisAppWidget = new ComponentName(context.getPackageName(),getClass().getName());
 			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
 			int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
 			this.onUpdate(context, appWidgetManager, ids);
 		}
 	}
 	  @Override
 	  public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
 	  {
 		  GregorianCalendar calendar = new GregorianCalendar();
 		  double hours, minutes, seconds, kiloseconds;
 	
 		  hours = calendar.get(GregorianCalendar.HOUR_OF_DAY);
 		  minutes = calendar.get(GregorianCalendar.MINUTE);
 		  seconds = calendar.get(GregorianCalendar.SECOND);
 		  kiloseconds = (hours*3600 + minutes*60 + seconds) / 1000;
 		  RemoteViews remoteViews;
 		  ComponentName watchWidget;
 		  remoteViews = new RemoteViews( context.getPackageName(), R.layout.main );
 		  watchWidget = new ComponentName( context, Kilosecond.class );
		  remoteViews.setTextViewText( R.id.TextView01, String.format("%1$06.3f", kiloseconds));
 		  appWidgetManager.updateAppWidget( watchWidget, remoteViews );
 	  }
 }
