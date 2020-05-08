 package com.github.lazycure;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import com.github.lazycure.activities.Activity;
 
 public class OutputManager {
 	
 	public final static String EMPTY_STRING = "";
 	public final static String ACTIVITY_PREFIX = "	";
 	public final static String DELIMITER = " - ";
 	public final static String TEST_ACTIVITY_NAME = "test";
 	public final static String DAY_SEPARATION_LINE = "=====";
 	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
 	public static int lastDayNumber;
 	
 	public static String FormatActivitiesList(List<Activity> activities){
 		StringBuffer buffer = new StringBuffer();
 		String activitiesList = EMPTY_STRING;
 		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 
 		for (int i=0; i<activities.size(); i++) {
 			buffer.append(FormatActivityString(activities.get(i)));
 			buffer.append("\n");
 			if (i<activities.size()-1){
 				if (getFinishDayNumber(activities.get(i)) != getFinishDayNumber(activities.get(i+1))){
 					buffer.append(ACTIVITY_PREFIX + DAY_SEPARATION_LINE);
 					Date previousActivityDate = activities.get(i).getFinishTime();
 					buffer.append(Time.GetYYYYMMDD(previousActivityDate));
 					buffer.append(DAY_SEPARATION_LINE);
 					buffer.append("\n");
 				}
 			}
 		}
 		activitiesList = buffer.toString();
 		return activitiesList;
 	}
 
 	public static String FormatActivityString(Activity activity){
 		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(ACTIVITY_PREFIX);
 		buffer.append(activity.getName());
 		buffer.append(DELIMITER);
 		Date delta = activity.getDuration();
		buffer.append(Time.format(delta));
 		return buffer.toString();
 	}
 
 	public static List<Activity> UpdateActivitiesWithStartTime(List<Activity> activities){
         //Update missing start time records for all activities
 		for (int i=1;i<activities.size();i++){
 			if (activities.get(i-1).getFinishTime() != null){
 				activities.get(i).setStartTime(activities.get(i-1).getFinishTime());
 			}
 		}
 		return activities;
 	}
 
 	public static int getFinishDayNumber(Activity activity){
 		int dayNumber = activity.getFinishTime().getDay();
 		return dayNumber;
 	}
 }
