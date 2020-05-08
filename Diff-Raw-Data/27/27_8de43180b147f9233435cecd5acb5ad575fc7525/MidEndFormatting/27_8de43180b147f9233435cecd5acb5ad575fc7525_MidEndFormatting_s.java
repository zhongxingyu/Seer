 package midend;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import comparison.scheduleComparison;
 import data.Time;
 import entities.Entity;
 import entities.EntityGroup;
 
 public class MidEndFormatting {
 
 	/**
 	 * returns a formatted String, which contains all the free people for a given time.
 	 * @param Entity Group eg
 	 * @param Time t
 	 * @return Schedule
 	 */
 	static String freePeople(EntityGroup eg, Time t){
 		String toReturn = "<html>";
 		
 		scheduleComparison sc = new scheduleComparison(eg);
 		EntityGroup eg2 = sc.freeMembers(t);
 		
 		
 		for (Entity e:eg2.getEntities()){
 			ArrayList<String> tags = e.getTags();
 			boolean person = true;
 			for (String s : tags){
 				if(s.equalsIgnoreCase("building")){
 					person = false;
 				}
 			}
 			
 			if(person){
 				toReturn += e.getName() + " is free right now<br>";
 			}
 		}
 		
 		toReturn += "</html>";
 		return toReturn;
 	}
 	
 	/**
 	 * returns a formatted String, which contains all the free people for a given time.
 	 * @param Entity Group eg
 	 * @param Time t
 	 * @return Schedule
 	 */
 	static String busyPeople(EntityGroup eg, Time t){
 		String toReturn = "<html>";
 		
 		scheduleComparison sc = new scheduleComparison(eg);
 		EntityGroup eg2 = sc.freeMembers(t);
 		EntityGroup eg3 = new EntityGroup();
 		
 		
 		for (Entity e : eg.getEntities()){
 			ArrayList<String> tags = e.getTags();
 			boolean person = true;
 			for (String s : tags){
 				if(s.equalsIgnoreCase("building")){
 					person = false;
 				}
 			}
 			if(person && !eg2.getEntities().contains(e)){
 				eg3.addEntity(e);
 			}
 		}
 		
 		for (Entity e : eg3.getEntities()){
 			toReturn += e.getName() + " is busy doing " + e.eventAtTime(t) + " and will be free at " + e.nextFree(t) + "<br>";
 		}
 		
 		toReturn += "</html>";
 		return toReturn;
 	}
 
	static String openBuildings(EntityGroup buildings, Time t){
 		String toReturn = "<html>";
 		
 		scheduleComparison sc = new scheduleComparison(buildings);
 		EntityGroup buildings2 = sc.freeMembers(t);
 		
 		
 		for (Entity e:buildings2.getEntities()){
 			ArrayList<String> tags = e.getTags();
 			boolean building = false;
 			for (String s : tags){
 				if(s.equalsIgnoreCase("building")){
 					building = true;
 				}
 			}
 			
			if(building){
 				toReturn += e.getName() + " is currently open<br>";
 			}
 		}
 		toReturn += "</html>";
 		return toReturn;
 	}
 	
 	static Time systemTime(){
 		Time toReturn = new Time();
 		
 		Calendar c = Calendar.getInstance();
 		toReturn.setWeekday(c.get(Calendar.DAY_OF_WEEK));
 		toReturn.setHour(c.get(Calendar.HOUR_OF_DAY));
 		toReturn.setMinute(c.get(Calendar.MINUTE));
 		
 		return toReturn;
 	}
 	
 	public static String[] listOfEntityGroups(){
 		File f = new File("EntityGroups");
 		return f.list();
 	}
 	
 	public static String[] listOfEntites(){
 		File f = new File("Entities");
 		return f.list();
 	}
 
 }
