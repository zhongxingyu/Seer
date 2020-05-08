 /*
  * Chris Card
  * Nathan Harvey
  * 11/19/2012
  * This class will set and reinitialize alarms from the todohelper database
  */
 
 package csci422.CandN.to_dolist;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 
 public class OnBootReceiver extends BroadcastReceiver {
 
 	public static final String NOTIFY_EXTRA = "csci422.CandN.to_dolist.notice";
 	
 	private static Calendar nowDate;
 	
 	@Override
 	public void onReceive(Context ctxt, Intent intent) 
 	{
 		ToDoHelper helper = new ToDoHelper(ctxt);
 		
 		Cursor c = helper.getAll("date");
 		
 		while(c.moveToNext())
 		{
 			if(!helper.getNotified(c))
 			{
 				setAlarm(ctxt,helper,c);
 			}
 		}
 
 	}
 	
 	public static void setAlarm(Context ctxt, ToDoHelper h, Cursor c)
 	{
 		nowDate = Calendar.getInstance();
 		
 		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
 		
 		AlarmManager mgr = (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
 		
 		if(!h.getDate(c).isEmpty())
 		{
 			String words[] = h.getDate(c).split("\\s+");
 			if(words.length == 3)
 			{
 				cal.set(getYear(words[0]), getMonth(words[0]), getDay(words[0]), getHour(words[1]), getMin(words[1]));
 				
 				if("PM".equals(words[2]))
 				{
 					cal.set(Calendar.AM_PM, Calendar.PM);
 				}
 				else
 				{
 					cal.set(Calendar.AM_PM, Calendar.AM);
 				}
 				
 				if(cal.getTimeInMillis() < System.currentTimeMillis())
 				{
 					h.notified(c.getString(0), true);
 				}
 				else
 				{
 					Intent i = new Intent(ctxt,AlarmReceiver.class);
 					
 					i.putExtra(NOTIFY_EXTRA, c.getString(0));
 					
 					mgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), PendingIntent.getBroadcast(ctxt, c.getInt(0), i, 0));
 				}
 			}
 		}
 		
 	}
 	
 	public static void cancelAlarm(Context ctxt, ToDoHelper h, Cursor c)
 	{
 		AlarmManager mgr = (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
 		
 		Intent i = new Intent(ctxt,AlarmReceiver.class);
 		
 		i.putExtra(NOTIFY_EXTRA, c.getString(0));
 		
 		mgr.cancel(PendingIntent.getBroadcast(ctxt, c.getInt(0), i, 0));
 	}
 	
 	
 	private static int getYear(String date)
 	{
 		String words[] = date.split("/");
 		
 		if(words.length == 3)
 		{
 			String tempString = "20"+words[2];
 			return (1990+Integer.parseInt(tempString));
 		}
 		else
 		{
 			return nowDate.get(Calendar.YEAR);
 		}
 	}
 	
 	private static int getMonth(String date)
 	{
 		String words[] = date.split("/");
 		
 		if(words.length == 3)
 		{
 			return Integer.parseInt(words[0]);
 		}
 		else
 		{
 			return nowDate.get(Calendar.MONTH);
 		}
 	}
 	
 	private static int getDay(String date)
 	{
 		String words[] = date.split("/");
 		
 		if(words.length == 3)
 		{
 			return Integer.parseInt(words[0]);
 		}
 		else
 		{
 			return nowDate.get(Calendar.DAY_OF_MONTH);
 		}
 	}
 	
 	private static int getHour(String time)
 	{
 		String words[] = time.split(":");
 		
 		if(words.length == 2)
 		{
 			return Integer.parseInt(words[0]);
 		}
 		else
 		{
 			return nowDate.get(Calendar.HOUR_OF_DAY);
 		}
 	}
 	
 	private static int getMin(String time)
 	{
 		String words[] = time.split(":");
 		
 		if(words.length == 2)
 		{
 			return Integer.parseInt(words[1]);
 		}
 		else
 		{
 			return nowDate.get(Calendar.MINUTE);
 		}
 	}
 
 }
