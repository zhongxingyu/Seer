 package org.macno.puma.util;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
import java.util.TimeZone;
 
 import org.macno.puma.R;
 
 import android.content.Context;
 
 public class DateUtils {
 
 	public static String getRelativeDate(Context context, Date date) {
 
 		Date now = new Date();
		
		
 		// Seconds.
 		float diff = (now.getTime() - date.getTime()) / 1000;
 		if (diff < 60) {
 			return context.getString(R.string.few_seconds);
 		} else if (diff < 92) {
 			return context.getString(R.string.one_minute_ago);
 		} else if (diff < 3300) {
 			return context.getString(R.string.minutes_ago, Math.round(diff/60));
 		} else if (diff < 5400) {
 			return context.getString(R.string.one_hour_ago);
 		} else if (diff < 22 * 3600) {
 			return context.getString(R.string.hours_ago, Math.round(diff/3600));
 		} else if (diff < 37 * 3600) {
 			return context.getString(R.string.one_day_ago);
 		} else if (diff < 24 * 24 * 3600) {
 			return context.getString(R.string.days_ago, Math.round(diff/(24*3600)));
 		} else if (diff < 46 * 24 * 3600) {
 			return context.getString(R.string.one_month_ago);
 		} else if (diff < 330 * 24 * 3600) {
 			return context.getString(R.string.months_ago, Math.round(diff/(30*24*3600)));
 		} else if (diff < 480 * 24 * 3600) {
 			return context.getString(R.string.one_year_ago);
 		} else {
 			return new SimpleDateFormat("d MM yyyy",Locale.US).format(date);
 		}				
 	}
 
 	public static Date parseRFC3339Date(String datestring) throws java.text.ParseException, IndexOutOfBoundsException{
 		Date d = new Date();
 
 		//if there is no time zone, we don't need to do any special parsing.
 		if(datestring.endsWith("Z")){
 			try{
				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);//spec for RFC3339
				s.setTimeZone(TimeZone.getTimeZone("UTC"));
 				d = s.parse(datestring);		  
 			}
 			catch(java.text.ParseException pe){//try again with optional decimals
 				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",Locale.US);//spec for RFC3339 (with fractional seconds)
 				s.setLenient(true);
				s.setTimeZone(TimeZone.getTimeZone("UTC"));
 				d = s.parse(datestring);		  
 			}
 			return d;
 		}
 
 		//step one, split off the timezone. 
 		String firstpart = datestring.substring(0,datestring.lastIndexOf('-'));
 		String secondpart = datestring.substring(datestring.lastIndexOf('-'));
 
 		//step two, remove the colon from the timezone offset
 		secondpart = secondpart.substring(0,secondpart.indexOf(':')) + secondpart.substring(secondpart.indexOf(':')+1);
 		datestring  = firstpart + secondpart;
 		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US);//spec for RFC3339		
 		try{
 			d = s.parse(datestring);		  
 		}
 		catch(java.text.ParseException pe){//try again with optional decimals
 			s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ",Locale.US);//spec for RFC3339 (with fractional seconds)
 			s.setLenient(true);
 			d = s.parse(datestring);		  
 		}
 		return d;
 	}
 }
