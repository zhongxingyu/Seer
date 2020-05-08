 package it.giacomos.android.osmer.observations;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
import android.util.Log;

 public class SunsetCalculator {
 
 	
 	public boolean isDark()
 	{
 		boolean night;
 		Calendar cal = Calendar.getInstance(TimeZone.getDefault());

		Calendar sunset = Calendar.getInstance(TimeZone.getDefault());
		sunset.set(Calendar.HOUR, 0);
		sunset.set(Calendar.MINUTE, 0);
		sunset.set(Calendar.SECOND, 0);
		Calendar dawn = Calendar.getInstance(TimeZone.getDefault());
		dawn.set(Calendar.HOUR, 0);
		dawn.set(Calendar.MINUTE, 0);
		dawn.set(Calendar.SECOND, 0);
		
 		Date now = cal.getTime();
 		
		int month = cal.get(Calendar.MONTH) + 1; /* JANUARY is 0 */
 		if((month > 2 && month < 5) || month > 8 && month < 11)
 		{
 			dawn.set(Calendar.HOUR, 6);
 			sunset.set(Calendar.HOUR, 19);
 		}
 		else if(month > 4 && month < 9)
 		{
 			dawn.set(Calendar.HOUR, 5);
 			sunset.set(Calendar.HOUR, 21);
 		}
 		else if(month == 12)
 		{
 			dawn.set(Calendar.HOUR, 7);
 			dawn.set(Calendar.MINUTE, 30);
 			sunset.set(Calendar.HOUR, 16);
 			sunset.set(Calendar.MINUTE, 30);
 		}
 		else if(month == 1)
 		{
 			dawn.set(Calendar.HOUR, 7);
 			dawn.set(Calendar.MINUTE, 15);
 			sunset.set(Calendar.HOUR, 17);
 		}
 		else
 		{
 			dawn.set(Calendar.HOUR, 7);
 			sunset.set(Calendar.HOUR, 18);
 		}
 		
 		/* compareTo returns the value 0 if the argument Date is equal to this Date; a value less than 0 if this Date 
 		 * is before the Date argument; and a value greater than 0 if this Date is after the Date argument.
 		 */
		
 		if(now.compareTo(dawn.getTime()) > 0 && now.compareTo(sunset.getTime()) < 0)
 			night = false;
 		else
 			night = true;
		Log.e("SunsetCalculator isDark", " now " + now + ", dawn " + dawn.getTime() + " sunset " + sunset.getTime() 
				+ ", nite " + night +  " month " + month);
 		return night;
 	}
 }
