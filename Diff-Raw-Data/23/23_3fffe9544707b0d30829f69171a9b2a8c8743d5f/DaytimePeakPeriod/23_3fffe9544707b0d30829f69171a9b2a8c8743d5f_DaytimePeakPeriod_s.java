 package com.acmetelecom.Utils;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class DaytimePeakPeriod {
 
 	public final int PeakPeriodStart = 7;
 	public final int PeakPeriodEnd = 19;
 	
 	public int getPeakPeriodDuration(){
 		return this.PeakPeriodEnd - this.PeakPeriodStart;
 	}
 	
     public boolean offPeak(Date time) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(time);
         int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 7 || hour >= 19;
     }
 }
