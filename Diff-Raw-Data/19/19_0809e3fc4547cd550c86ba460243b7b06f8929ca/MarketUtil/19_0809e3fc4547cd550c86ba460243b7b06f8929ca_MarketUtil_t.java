 package com.cspinformatique.csptrading.util;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import com.cspinformatique.csptrading.entity.Market;
 
 public abstract class MarketUtil {
 	public static Date getClosingTime(Market market, Date date){
 		Calendar closingTime = Calendar.getInstance();
 		closingTime.setTime(date);
 		closingTime.set(Calendar.HOUR_OF_DAY, market.getClosingHour());
 		closingTime.set(Calendar.MINUTE, market.getClosingMinute());
 		closingTime.set(Calendar.SECOND, 0);
 		closingTime.set(Calendar.MILLISECOND, 0);
 		
 		return closingTime.getTime();
 	}
 	
 	public static Date getOpeningTime(Market market, Date date){
 		Calendar openingTime = Calendar.getInstance();
 		openingTime.setTime(date);
 		openingTime.set(Calendar.MINUTE, market.getOpeningHour());
 		openingTime.set(Calendar.HOUR_OF_DAY, market.getOpeningMinute());
 		openingTime.set(Calendar.SECOND, 0);
 		openingTime.set(Calendar.MILLISECOND, 0);
 		
 		return openingTime.getTime();
 	}
 	
 	public static List<Date> getOpenedDatesSinceDays(int days){
 		Calendar calendar = Calendar.getInstance();
 		
 		calendar.set(Calendar.HOUR_OF_DAY, 9);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 		
 		List<Date> openedDates = new ArrayList<Date>();
 		
 		while(days > 0){
 			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
 			if(dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY){
 				openedDates.add(calendar.getTime());
 				--days;
 			}
			
			calendar.add(Calendar.DAY_OF_MONTH, -1);
 		}
 		
 		return openedDates;
 	}
 }
