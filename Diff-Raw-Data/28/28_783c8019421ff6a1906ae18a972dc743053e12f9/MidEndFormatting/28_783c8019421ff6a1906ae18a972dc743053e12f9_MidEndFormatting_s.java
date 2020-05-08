 package midend;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import comparison.scheduleComparison;
 import data.Time;
 import entities.Entity;
 import entities.EntityGroup;
 
 public class MidEndFormatting {
 
 	/**
 	 * returns a formatted String, which contains all the free people for a given time.
 	 * @param Entity Group eg
 	 * @param Time t
 	 * @return String
 	 */
 	public static String freePeople(EntityGroup eg, Time t){
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
 	 * @return String
 	 */
 	public static String busyPeople(EntityGroup eg, Time t){
 		String toReturn = "<html>";
 		
 		scheduleComparison sc = new scheduleComparison(eg);
 		EntityGroup eg2 = sc.freeMembers(t);
 		EntityGroup eg3 = new EntityGroup("Busy People", null, new ArrayList<Entity>());
 		
 		
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
			toReturn += e.getName() + " is busy doing " + e.getEventAtTime(t) + " and will be free at " + e.nextFree(t) + "<br>";
 		}
 		
 		toReturn += "</html>";
 		return toReturn;
 	}
 
 	/**
 	 * returns a formatted String, which contains all the open buildings of a given "identifier".
 	 * @param EntityGroup buildings
 	 * @param Time t
 	 * @param String type
 	 * @return String
 	 */
 	public static String openBuildings(EntityGroup buildings, Time t, String type){
 		String toReturn = "<html>";
 		
 		scheduleComparison sc = new scheduleComparison(buildings);
 		EntityGroup buildings2 = sc.freeMembers(t);
 		
		
 		for (Entity e:buildings2.getEntities()){
 			ArrayList<String> tags = e.getTags();
 			boolean building = false;
 			boolean typeCheck = false;
 			for (String s : tags){
 				if(s.equalsIgnoreCase("building")){
 					building = true;
 				}
 				if(s.equalsIgnoreCase(type)){
 					typeCheck = true;
 				}
 			}
 			
 			if(building && typeCheck){
 				toReturn += e.getName() + " is currently open<br>";
 			}
 		}
 		toReturn += "</html>";
 		return toReturn;
 	}
 	
 	/**
 	 * returns a Time object which contains the current system time.
 	 * @return toReturn
 	 */
 	public static Time systemTime(){
 		Time toReturn = new Time();
 		
 		Calendar c = Calendar.getInstance();
 		toReturn.setWeekday(c.get(Calendar.DAY_OF_WEEK));
 		toReturn.setHour(c.get(Calendar.HOUR_OF_DAY));
 		toReturn.setMinute(c.get(Calendar.MINUTE));
 		
 		return toReturn;
 	}
 	
 	/**
 	 * returns an array of strings, which contains the list of Entity groups on the file.
 	 * @return toReturn
 	 */
 	public static String[] listOfEntityGroups(){
 		File f = new File("EntityGroups");
 		String[] strArr = f.list();
 		if (strArr == null)
 			strArr = new String[0];
 		return strArr;		
 	}
 
 }
