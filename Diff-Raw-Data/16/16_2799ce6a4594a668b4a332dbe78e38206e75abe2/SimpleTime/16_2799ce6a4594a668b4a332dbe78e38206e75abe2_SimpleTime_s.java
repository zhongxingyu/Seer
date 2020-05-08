 package quizweb;
 
 import java.sql.Timestamp;
 import java.util.Date;
 
 public class SimpleTime {
 	
 	public static String[] monthStr = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
 	
 	public static String getTime(Timestamp time) {
 		return getTime(time.getTime());
 	}
 	
 	public static String getTime(long time) {
 		long curTime = new Date().getTime();
 		return getTimeByDuration(curTime - time);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public static String getTimeByDuration(long durationTime) {
 		long duration = durationTime;
 		String retStr = new String();
 		while (true) {
 			duration = duration / 1000;
 			duration = duration / 60;
 			if (duration == 0) {
 				retStr = "a few seconds ago";
 				break;
 			}
 			
 			long min = duration % 60;
 			duration = duration / 60;
 			if (duration == 0) {
 				if (min == 1)
 					retStr = "1 minute ago";
 				else 
 					retStr = min + " minutes ago";
 				break;
 			}
 			
 			long hour = duration % 24;
 			duration = duration / 24;
 			if (duration == 0) {
 				if (hour == 1) {
 					retStr = "1 hour ago";
 				} else {
 					retStr = hour + " hours ago";
 				}
 				break;
 			}
 			
 			long day = duration % 30;
 			duration = duration / 30;
 			if (duration == 0) {
 				if (day == 1)
 					retStr = "1 day ago";
 				else if (day < 7)
 					retStr = day + " days ago";
 				else if (day < 14)
 					retStr = "1 week ago";
 				else 
 					retStr = day/7 + " weeks ago";
 				break;
 			}
 			if (duration > 12) {
 				retStr = "more than 1 year ago";
 				break;
 			}
 			Date oldDate = new Date(new Date().getTime() - durationTime); 
 			retStr = monthStr[oldDate.getMonth()] + "." + " " + oldDate.getDate();
 			break;
 		}
		return String.format("%-20s", retStr);
 	}
 }
