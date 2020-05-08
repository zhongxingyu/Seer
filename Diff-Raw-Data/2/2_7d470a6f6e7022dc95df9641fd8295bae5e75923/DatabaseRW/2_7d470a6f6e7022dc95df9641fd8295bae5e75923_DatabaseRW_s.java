 import java.util.Calendar;
 import java.util.ArrayList;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 
 //call this thingy with DatabaseRW.[functionwhatever] from anywhar
 
 public class DatabaseRW
 {
 	public static int answer;
 	public static String name, desc;
 	public static int status, priority;
 	public static boolean isAllDay;
 	public static int endYear, endMonth, endDay, endHour, endMinute, repeating;
 	public static Timestamp startTime, endTime, timestamp;
 
 	static Derby database = null;
 
 	public static void setDatabase(Derby db)
 	{
 		database = db;
 	}
 
 	public static void addTask(String name, String desc, int year, int month, int day, int hour, int minute, int status, int priority)
 	{
 		//String str = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00.000"; 
         //System.out.println(str);
 		name = name;
 		desc = desc;
 		status = status;
 		priority = priority;
 		timestamp = intToTimestamp(year, month, day, hour, minute);
 
 		answer = 11;
 
 		System.out.println("Task table has been updated.");
 		database.go();
 	}
 	
 	public static void updateTask(int id, String name, String desc, int year, int month, int day, int hour, int minute, int status, int priority)
 	{
 		
 	}
 	
 	public static void deleteTask(int id)
 	{
 
 	}
 
 	public static void addEvent(String name, String desc, int year, int month, int day, int hour, int minute, boolean isAllDay, int endYear, int endMonth, int endDay, int endHour, int endMinute, int repeating)
 	{
 		//String str = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00.000"; 
        System.out.println(str);
 		name = name;
 		desc = desc;
 		
 		//String str2 = endYear + "-" + endMonth + "-" + endDay + " " + endHour + ":" + endMinute + ":00.000"; 
 
 		startTime = intToTimestamp(year, month, day, hour, minute);
 		endTime = intToTimestamp(endYear, endMonth, endDay, endHour, endMinute);
 
 		isAllDay = isAllDay;
 
 		repeating = repeating;
 
 		answer = 12;
 
 		System.out.println("Event table has been updated.");
 		database.go();
 	}
 	
 	public static void updateEvent(int id, String name, String desc, int year, int month, int day, int hour, int minute, boolean isAllDay, int endYear, int endMonth, int endDay, int endHour, int endMinute, int repeating)
 	{
 
 	}
 	
 	public static void deleteEvent(int id)
 	{
 		
 	}
 
 	public static void queryTask(){
 		answer = 41;
 		database.go();
 	}
 
 	/*==================================*/
 	/* Beginning of getValue() methods */
 	/*================================*/
 
 	public static int getAnswer(){
 		return answer;
 	}
 
 	public static String getName(){
 		return name;
 	}
 
 	public static String getDesc(){
 		return desc;
 	}
 
 	public static int getStatus(){
 		return status;
 	}
 
 	public static int getPriority(){
 		return priority;
 	}
 
 	public static boolean getIsAllDay(){
 		return isAllDay;
 	}
 
 	public static int getRepeating(){
 		return repeating;
 	}
 
 	public static Timestamp getStartTime(){
 		return startTime;
 	}
 
 	public static Timestamp getEndTime(){
 		return endTime;
 	}
 
 	public static Timestamp getTimestamp(){
 		return timestamp;
 	}
 	
 	  /*========================*/
 	 /*Conveniece methods! Yay!*/
 	/*========================*/
 	
 	public static Timestamp intToTimestamp(int year, int month, int day, int hour, int minute)
 	{
 		Calendar tempCal = Calendar.getInstance();
 		tempCal.set(year, month, day, hour, minute);
 		
 		return new Timestamp(tempCal.getTimeInMillis());
 	}
 }
