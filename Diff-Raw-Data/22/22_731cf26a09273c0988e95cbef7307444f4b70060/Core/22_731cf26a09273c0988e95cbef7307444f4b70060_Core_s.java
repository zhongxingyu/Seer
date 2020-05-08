 package com.sobinary.base;
 
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.util.Calendar;
 import java.util.Timer;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class Core 
 {
 	public static final String SIZE_KEY = "widthkey";
 	public static final String TEXT_COL_KEY = "textcol";
 
 	private static final boolean debug = true;
 	
 	public static void printe(String custom, Throwable e)
 	{
 		if( e == null) Core.print("Could not present null exception");
 		java.io.StringWriter traceText = new StringWriter();
 		java.io.PrintWriter pWriter = new PrintWriter(traceText,true);
 		e.printStackTrace(pWriter);
 		pWriter.close();
 		print( custom + traceText.toString() ); 
 	}
 	
 	public static void printe(Throwable e)  
 	{
 		printe("", e);
 	}
   
 	public static void print(String s)
 	{
		if( debug ) Log.d("socialwid", s);
 	}
	
 	public static boolean isOnline(Context cont)
 	{
 		ConnectivityManager cm = (ConnectivityManager) cont.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		return  netInfo != null && netInfo.isConnectedOrConnecting() ;
 	}
 
 	public static int toCelcius(int far, boolean convert)
 	{
 		if( !convert ) return far;
 		int result = (int)( (far - 32) / 9 * 5 );
 		Core.print(far+ " fahren = " + result + " celcius");
 		return result;
 	}
 	
 
 	public static String getMinute(boolean euroClock)
 	{
 		Calendar now = Calendar.getInstance();
 		String hour;
 		if(euroClock) hour = now.get( Calendar.HOUR_OF_DAY )+"";
 		else hour = betterHour( now.get(Calendar.HOUR) );
 		String minute = betterMinute( now.get(Calendar.MINUTE) );
 		return hour + ":" + minute;	
 	}
 	
 	private static String betterHour(int hour)
 	{
 		if( hour == 0 ) return "12";
 		else return hour+"";
 	}
 	    
 	private static String betterMinute(int min) 
 	{
 		if( min < 10 ) return "0"+min;
 		else return min+"";
 	}
 
 	public static int millisToZero()
 	{
 		int secs = Calendar.getInstance().get( Calendar.SECOND );
 		int remain = 60 - secs;
 		Core.print("Seconds to Zero = " + remain);
 		return remain*1000;
 	}
 	
 	public static void killThread(Timer t)
 	{
 		if( t != null )
 		{
 			t.cancel();
 			Core.print("Thread" + t.getClass().getName() + " Cancelled");
 			t = null;
 		}
 		else Core.print("Thread not cancelled because it was null");
 	}
 	
 	public static final class ViewState implements Serializable
 	{
 		private static final long serialVersionUID = 1L;
 		public String date, weather, temperature, person, action, tweet1, tweet2;
 	}
 
 	public static int max(int a, int b)
 	{
 		return a > b ? a : b; 
 	}
 
 	public static int min(int a, int b)
 	{
 		return a < b ? a : b; 
 	} 
 	
 	public static String bleach(String raw)
 	{
 		return raw.replaceAll("[\n a-zA-Z	]|", "");
 	}
 	
 	public static String weekdayToString(Calendar now)
 	{
 		String day = "XXX";
 		
 		switch( now.get( Calendar.DAY_OF_WEEK ) )
 		{
 			case Calendar.MONDAY: day = "Monday"; break;
 			case Calendar.TUESDAY: day = "Tuesday"; break; 
 			case Calendar.WEDNESDAY: day = "Wednesday"; break;
 			case Calendar.THURSDAY: day = "Thursday"; break;
 			case Calendar.FRIDAY: day = "Friday"; break; 
 			case Calendar.SATURDAY: day = "Saturday"; break;
 			case Calendar.SUNDAY: day = "Sunday"; break;
 		}
 
 		return day+" "+ now.get( Calendar.DAY_OF_MONTH );
 	}
 
 	private static boolean isPrimitive(Field field)
 	{
 		if( field.getType().equals(Integer.TYPE) ) return true;
 		if( field.getType().equals(Float.TYPE) ) return true;
 		if( field.getType().equals(Boolean.TYPE) ) return true;
 		if( field.getType().equals(String.class) ) return true;
 		
 		return false;
 	}
 	
 	public static void printPrimState(Object inst)
 	{
 		try
 		{
 			Field[] fields = inst.getClass().getDeclaredFields();
 			for(Field field: fields)
 				if(isPrimitive(field))
 					print(field.getName() + " => " + field.get(inst).toString());
 		}
 		catch(Exception e)
 		{
 			Core.print("Could not print primitive state...");
 		}
 	}
 
 }
 
 
 
 
 
