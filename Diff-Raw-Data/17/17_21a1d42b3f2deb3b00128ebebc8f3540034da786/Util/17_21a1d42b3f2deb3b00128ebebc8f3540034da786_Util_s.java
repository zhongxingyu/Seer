 package org.computer.knauss.reqtDiscussion.io;
 
 import java.sql.Date;
 import java.sql.Time;
 
 public class Util {
 
 	public static Date parseDate(String dateString) {
		String split = null;
 		
 		// lets see what kind of format we have here...
 		if (dateString.trim().indexOf(" ") >-1) {
 			// this is the format I encounter in the PSQL dump...
 			split = " ";
 		} else if (dateString.trim().indexOf("T") >-1) {
 			// this is the format returned by jazz
 			split = "T";
 			// remove the trailing Z...
 			dateString = dateString.substring(0, dateString.length()-1);
 		}
 		
 		
 		String[] splitDateString = dateString.trim().split(split);
 		// System.out.println(splitDateString[0] + "----" + splitDateString[1]);
 		Date day = Date.valueOf(splitDateString[0]);
 		// Date day = Date.valueOf(splitDateString[0]);
 		int indexOfTimeMillis = splitDateString[1].indexOf('.');
 		Date date = null;
 		if (indexOfTimeMillis > 0) {
 			Time time = Time.valueOf(splitDateString[1].substring(0,
 					indexOfTimeMillis));
 			date = new Date(day.getTime()
 					+ time.getTime()
 					+ Integer.valueOf((splitDateString[1]
 							.substring(indexOfTimeMillis + 1))));
 		} else {
 			Time time = Time.valueOf(splitDateString[1]);
 			date = new Date(day.getTime() + time.getTime());
 		}
 		return date;
 	}
 }
