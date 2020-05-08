 package net.guto.steamer;
 
 import java.util.Date;
 
 public class Achievement {
 
 	String iconClosed;
 	String iconOpen;
 	String name;
 	String apiname;
 	String description;
 	Date timestamp;
 
 	public String getIconClosed() {
 		return iconClosed;
 	}
 
 	public String getIconOpen() {
 		return iconOpen;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getApiname() {
 		return apiname;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public Date getTimestamp() {
 		return timestamp;
 	}
 	
	public boolean isAchieved(){
 		return timestamp != null;
 	}
 }
