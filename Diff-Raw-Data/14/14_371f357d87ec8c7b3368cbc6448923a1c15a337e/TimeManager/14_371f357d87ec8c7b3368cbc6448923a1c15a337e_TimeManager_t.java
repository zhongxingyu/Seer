 package com.canyonsappclub.app;
 
 
 
 
 /* *************************************************************************************
    *************************************************************************************
    *  WARNING: This is some pretty hack-ish code. I don't really advocate its use.	   *
    *  																				   *
    *  Created by Isaac Parker on June 14th 2013 for Canyons App Club 				   *
    *************************************************************************************
    ************************************************************************************* */
 public class TimeManager {
 	
	// This will not work for numbers above 100!
	/*
	 * More technically, this will not work for 112 which SHOULD be 112th, but WILL
	 * return a 'nd' rather than 'th'. We're using this for dates, so there are no real
	 * concerns, just be aware that is this kinda crappy code. Also, dealing with dates
	 * and the English are never fun outside of good frameworks.
	 */
	
 	static String appendExt(int num) {
		
		if(num > 9 && num < 21) {
			return "th";
		}
		
 		String str = String.valueOf(num);
 		int parsed = Integer.parseInt(String.valueOf(str.charAt(str.length()-1)));
 		if(parsed == 1) {
 			return "st";
 		}
 		if(parsed == 2) {
 			return "nd";
 		}
 		if(parsed == 3) {
 			return "rd";
 		}
 		if(parsed >= 4 && parsed <= 9) {
 			return "th";
 		}
 		return "";
 	}
 	
 	public static String convertFromIsoFormat(String iso8601timeperiod) {
 		
 		String[] months = {
 				"Jan",
 				"Feb",
 				"Mar",
 				"Apr",
 				"May",
 				"Jun",
 				"Jul",
 				"Aug",
 				"Sep",
 				"Oct",
 				"Nov",
 				"Dec",
 		};
 		
 		boolean startIsAm = false;
 		boolean endIsAm = false;
 		
 		//Used for testing...
 		//String timeString = "2013-06-19T13:40:00/2013-06-19T15:03:00";
 		
 		String rawStartDate = iso8601timeperiod.split("/")[0];
 		String rawEndDate = iso8601timeperiod.split("/")[1];
 		
 		String[] startDateTime = rawStartDate.split("T");
 		
 		String[] startDate = startDateTime[0].split("-");
 		int startYear = Integer.parseInt(startDate[0]);
 		int startMonth = Integer.parseInt(startDate[1]);
 		int startDay = Integer.parseInt(startDate[2]);
 		
 		String[] startTime = startDateTime[1].split(":");
 		int startHours = Integer.parseInt(startTime[0]);
 		String startMinutes = startTime[1];
 		
 		
 		String[] endDateTime = rawEndDate.split("T");
 		
 		String[] endDate = endDateTime[0].split("-");
 		int endYear = Integer.parseInt(endDate[0]);
 		int endMonth = Integer.parseInt(endDate[1]);
 		int endDay = Integer.parseInt(endDate[2]);
 		
 		String[] endTime = endDateTime[1].split(":");
 		int endHours = Integer.parseInt(endTime[0]);
 		String endMinutes = endTime[1];
 		
 		
 		if(startHours > 12) {
 			startIsAm = false;
 			startHours = startHours - 12;
 		} else if(startHours == 12) {
 			endIsAm = false;
 		} else {
 			if(startHours == 0) {
 				startHours = 12;
 			}
 			startIsAm = true;
 		}
 		
 		
 		if(endHours > 12) {
 			endIsAm = false;
 			endHours = endHours - 12;
 		} else if(endHours == 12) {
 			endIsAm = false;
 		} else {
 			if(endHours == 0) {
 				endHours = 12;
 			}
 			endIsAm = true;
 		}
 //		
 //		if(endHours == 0) {
 //			
 //		}
 		
 		String formattedTimePeriod = "";
 		if(startIsAm && endIsAm || !startIsAm && !endIsAm) {
 			if(startIsAm && endIsAm) {
 				formattedTimePeriod = startHours + ":" + startMinutes + "AM - " + endHours + ":" + endMinutes + "AM";
 			}
 			if(!startIsAm && !endIsAm) {
 				formattedTimePeriod = startHours + ":" + startMinutes + "PM - " + endHours + ":" + endMinutes + "PM";
 			}
 		} else {
 			if(startIsAm) {
 				formattedTimePeriod = startHours + ":" + startMinutes + "AM - " + endHours + ":" + endMinutes + "PM";
 			} else {
 				formattedTimePeriod = startHours + ":" + startMinutes + "PM - " + endHours + ":" + endMinutes + "AM";
 			}
 		}
 		
 		
 		//TODO: correct for time zone
 		//Right now, the timezone is assumed to be local time
 		//
 		// TimeZone is part of java.util
 		//		TimeZone timeZone = Calendar.getInstance().getTimeZone();
 		//		int tzOffset = timeZone.getRawOffset();
 		//		if(timeZone.inDaylightTime(new Date())) {
 		//			tzOffset = tzOffset + timeZone.getDSTSavings();
 		//		}
 		//		int offsetHours = tzOffset / 1000 / 60 / 60;
 		//		
 		//		startHours = startHours - offsetHours;
 		//		endHours = endHours - offsetHours;
 		
 		//This is done because integers are never remembered as '004' -- that would get changed to '4'. Also the 'quotes' are just for this example
 		if(startMinutes.length() < 2) {
 			startMinutes = "0" + startMinutes;
 		}
 	
 		if(endMinutes.length() < 2) {
 			endMinutes = "0" + endMinutes;
 		}
 		
 		//Some would say I should use StringBuilder, I say whatever :)
 		// 							//Arrays start at 0!
 		String prettyStuff = months[startMonth-1] + " " + startDay + appendExt(startDay) + " " + formattedTimePeriod;
 		
 		return prettyStuff;
 	}
 }
