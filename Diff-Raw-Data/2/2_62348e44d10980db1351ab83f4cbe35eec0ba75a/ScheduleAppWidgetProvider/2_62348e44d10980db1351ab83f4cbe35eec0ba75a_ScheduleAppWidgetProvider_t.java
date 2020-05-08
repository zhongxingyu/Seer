 package com.wimy.android.calendarwidget;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
import com.wimy.android.calendarwidget.R;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.BroadcastReceiver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.net.Uri.Builder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 class DayEvent
 {
 	public String date;
 	private java.util.ArrayList<String> mTitles = new ArrayList<String>();
 	
 	public void appendTitle(String title)
 	{
 		mTitles.add(title);
 	}
 	
 	public ArrayList<String> getTitles()
 	{
 		return mTitles;
 	}
 }
 
 class CalendarDataProviderObserver extends ContentObserver
 {
 	public static CalendarDataProviderObserver sInstance = null;
 	private Context mContext;
 	private AppWidgetManager mMgr;
 	private int[] ids;
 	private ScheduleAppWidgetProvider mPr;
 
 	public CalendarDataProviderObserver(Handler handler, ScheduleAppWidgetProvider pr, Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds)
 	{
 		super(handler);
 		
 		Log.i("zelon", "CalendarDataProviderObserver()");
 		
 		mContext = context;
 		mMgr = appWidgetManager;
 		ids = appWidgetIds;
 		mPr = pr;
 		
 		sInstance = this;
 	}
 
 	@Override
 	public void onChange(boolean selfChange)
 	{
 		Log.i("zelon", "onChange");
 		
 		mPr.onUpdate(mContext, mMgr, ids);
 	}
 	
 }
 
 public class ScheduleAppWidgetProvider extends AppWidgetProvider
 {
 	private CalendarDataProviderObserver mObserver;
 
 	public ScheduleAppWidgetProvider()
 	{
 		Log.i("zelon","ScheduleAppWidgetProvider() constructor");
 	}
 	
 	@Override
 	public IBinder peekService(Context myContext, Intent service)
 	{
 		Log.i("zelon","peekService");
 
 		return super.peekService(myContext, service);
 	}
 
 	@Override
 	public void onDeleted(Context context, int[] appWidgetIds)
 	{
 		Log.i("zelon","onDeleted");
 		super.onDeleted(context, appWidgetIds);
 	}
 
 	@Override
 	public void onDisabled(Context context)
 	{
 		Log.i("zelon","onDisabled");
 		
 		if ( null != mObserver )
 		{
 			context.getContentResolver().unregisterContentObserver(mObserver);
 		}
 		
 		super.onDisabled(context);
 	}
 
     private BroadcastReceiver mIntentReceiver = new BroadcastReceiver()
     {
         @Override
         public void onReceive(Context context, Intent intent)
         {
             String action = intent.getAction();
             Log.i("zelon", "recv action : " + action);
             if (action.equals(Intent.ACTION_TIME_CHANGED)
                     || action.equals(Intent.ACTION_DATE_CHANGED)
                     || action.equals(Intent.ACTION_TIMEZONE_CHANGED))
             {
     			Log.i("zelon", "on recv DateChangeReceiver.onReceive date_changed");
     			if ( null != CalendarDataProviderObserver.sInstance )
     			{
     				Log.i("zelon", "on recv DateChangeReceiver.onReceive make change");
     				CalendarDataProviderObserver.sInstance.onChange(true);
     			}
             }
         }
     };
 	
 	@Override
 	public void onEnabled(Context context)
 	{
 		super.onEnabled(context);
 		Log.i("zelon","onEnabled");
 		
         IntentFilter filter = new IntentFilter();
         filter.addAction(Intent.ACTION_DATE_CHANGED);
         context.getApplicationContext().registerReceiver(mIntentReceiver, filter);
 	}
 
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
 			int[] appWidgetIds)
 	{
 		Log.i("zelon", "onUpdate");
 		final int N = appWidgetIds.length;
 
 		// Perform this loop procedure for each App Widget that belongs to this
 		// provider
 		for (int i = 0; i < N; i++)
 		{
 			Log.i("zelon", "update app widget");
 
 			int appWidgetId = appWidgetIds[i];
 
 			// Create an Intent to launch ExampleActivity
 			Intent intent = makeIntentForStartingCalendar(context);
 			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
 					intent, 0);
 
 			// Get the layout for the App Widget and attach an on-click listener
 			// to the button
 			RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.schedule_appwidget);
 			views.setOnClickPendingIntent(R.id.content, pendingIntent);
 			views.removeAllViews(R.id.content);
 			
 			ArrayList<DayEvent> dayEvents = getCalendarEvents(context);
 			
 			if ( dayEvents.size() == 0 )
 			{
 				DayEvent dayEvent = new DayEvent();
 				dayEvent.date = "";
 				dayEvent.appendTitle("There is no calendar." + Calendar.getInstance().getTime().toGMTString());
 				
 				dayEvents.add(dayEvent);
 			}
 
 			for ( DayEvent dayEvent : dayEvents )
 			{
 				RemoteViews newView = new RemoteViews(context.getPackageName(), R.layout.widget_small_view);
 				
 				StringBuilder sb = new StringBuilder();
 				sb.append(dayEvent.date);
 				
 				for ( String title : dayEvent.getTitles())
 				{
 					sb.append("\r\n    - ");
 					sb.append(title);
 				}
 				
 				newView.setTextViewText(R.id.small_textview, sb.toString());
 				views.addView(R.id.content, newView);
 				
 			}
 			
 
 			// Tell the AppWidgetManager to perform an update on the current App Widget
 			appWidgetManager.updateAppWidget(appWidgetId, views);
 			
 			mObserver = new CalendarDataProviderObserver(new Handler(), this, context, appWidgetManager, appWidgetIds );
 
 			context.getContentResolver().registerContentObserver(Uri.parse("content://com.android.calendar/events"), true, mObserver); 
 		}
 	}
 
 	public static String getDateString(Date date)
 	{
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 		
 		return DateFormat.format("yyyy-MM-dd EEEE", c).toString();
 	}
 	
 	private static void ShowEventLog(Cursor c)
 	{
 		String title = c.getString(c.getColumnIndex("title"));
 		long begin = c.getLong(c.getColumnIndex("begin"));
 		long end = c.getLong(c.getColumnIndex("end"));
 		
 		String log = String.format("%s from %d to %d", title, begin, end);
 		
 		Log.i("zelon", log);
 	}
 	
 	public static ArrayList<DayEvent> getCalendarEvents(Context context)
 	{
 		ArrayList<DayEvent> ret = new ArrayList<DayEvent>();
 
 		String [] stringUris = new String [] {
 				"content://com.android.calendar/",	///< For 2.3 and above
 				//"content://calendar/"					///< For 2.1 and below
 				};
 
 		int index = 0;
 		
 		final int ONE_DAY_MILLI = 1000 * 60 * 60 * 24;
 
 		Calendar cal_today = Calendar.getInstance(TimeZone.getTimeZone("GMT+09:00"));
 		cal_today.setTimeInMillis(getTodayStartTime());
 
 		for ( String stringUri : stringUris )
 		{
 			for ( index = 0; index < 7; ++index )
 			{
 				Log.i("zelon", "{");
 				boolean bFound = false;
 				Uri uri = Uri.parse(stringUri);
 				
 				
 				long today = cal_today.getTimeInMillis() + ( cal_today.getTimeZone().getOffset(cal_today.getTimeInMillis()));
 				
 				long startUnixtime = today + ( ONE_DAY_MILLI * index );
 				long endUnixtime = startUnixtime + ( ONE_DAY_MILLI )-1;
 
 				Calendar startDay = Calendar.getInstance(TimeZone.getTimeZone("GMT+09:00"));
 				startDay.setTimeInMillis(startUnixtime);
 				Calendar endDay = Calendar.getInstance(TimeZone.getTimeZone("GMT+09:00"));
 				endDay.setTimeInMillis(endUnixtime);
 
 				Log.i("zelon", "StartDay : " + startDay.getTime().toGMTString());
 				Log.i("zelon", "EndDay : " + endDay.getTime().toGMTString());
 				
 				
 				Builder builder = Uri.parse(stringUri +"instances/when").buildUpon();
 				ContentUris.appendId(builder, startUnixtime+1);
 				ContentUris.appendId(builder, endUnixtime);
 				uri = builder.build();
 				Log.i("zelon", "URI : " + uri.toString());
 				
 				Cursor c = context.getContentResolver().query(uri, new String[] { "_id", "title", "begin", "end", "allDay" }, null, null, "startDay ASC, startMinute ASC");
 				
 				if ( null == c )
 				{
 					Log.i("zelon", "There is no calendar in this uri : " + uri.toString());
 
 					continue;
 				}
 
 				DayEvent dayEvent = new DayEvent();
 				dayEvent.date = getDateString(startDay.getTime());
 				
 				while ( c.moveToNext() )
 				{
 					ShowEventLog(c);
 					bFound = true;
 
 					dayEvent.appendTitle(c.getString(c.getColumnIndex("title")));
 
 					if ( c.getLong(c.getColumnIndex("begin")) > endUnixtime )
 					{
 						assert(false);
 					}
 					if ( c.getLong(c.getColumnIndex("end")) < startUnixtime )
 					{
 						assert(false);
 					}
 				}
 				
 				if ( bFound == false )
 				{
 					dayEvent.appendTitle(context.getResources().getString(R.string.no_event));
 				}
 				c.close();
 				
 				ret.add(dayEvent);
 				
 				Log.i("zelon", "}");
 
 			}
 		}
 		
 		return ret;
 	}
 
 	public static long getTodayStartTime()
 	{
 		Calendar rightNow = Calendar.getInstance(TimeZone.getTimeZone("GMT+09:00"));
 		rightNow.set(Calendar.HOUR, 0);
 		rightNow.set(Calendar.SECOND, 0);
 		rightNow.set(Calendar.MINUTE, 0);
 		rightNow.set(Calendar.MILLISECOND, 0);
 		
 		return rightNow.getTime().getTime();
 	}
 
 	private Intent makeIntentForStartingCalendar(Context context)
 	{
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 		intent.setClassName("com.google.android.calendar", "com.android.calendar.AgendaActivity");
 
 		return intent;
 	}
 
 }
