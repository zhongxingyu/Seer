 package midend;
 
 import java.io.FileNotFoundException;
 
 import comparison.scheduleComparison;
 
 import data.ReadAndWrite;
 import data.Time;
 import entities.EntityGroup;
 
 public class Database{
 	
 	EntityGroup grouplist;
 	
 	public String freeNow(){
 		Time now = MidEndFormatting.systemTime();
 		return freeDuring(now);
 	}
 	
 	public String busyNow(){
 		Time now = MidEndFormatting.systemTime();
 		return busyDuring(now);
 	}
 	
	public String openNow(String type){
		Time now = MidEndFormatting.systemTime();
		return openDuring(now, type);
	}
	
 	public String freeDuring(Time t){
 		scheduleComparison sC = new scheduleComparison(grouplist);
 		EntityGroup free = sC.freeMembers(t);
 		String output = MidEndFormatting.freePeople(free, t);
 		return output;
 	}
 	
 	public String busyDuring(Time t){
 		scheduleComparison sC = new scheduleComparison(grouplist);
 		EntityGroup free = sC.freeMembers(t);
 		String output = MidEndFormatting.busyPeople(free, t);
 		return output;
 	}
 	
	public String openDuring(Time t, String type){
 		scheduleComparison sC = new scheduleComparison(grouplist);
 		EntityGroup free = sC.freeMembers(t);
		String output = MidEndFormatting.openBuildings(free, t, type);
 		return output;
 	}
 	
 	public void changeEntityGroup(String newEntityGroup){
 		try {
 			grouplist = ReadAndWrite.readEntityGroupFromFile(newEntityGroup);
 		} catch (FileNotFoundException e) {
 			
 		}
 	}
 }
