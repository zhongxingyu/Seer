 package Process;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 
 
 public class Event {
 	  public String myName;
 	    public String myLocation;
 	    public String myDescription;
 	    public String myURL;
 	    public DateTime myStartTime;
 	    public DateTime myEndTime;
 	    public Interval myInterval;
 	    
 	    public Event(String name, String location, String description, DateTime startTime, DateTime endTime, String URL) {
 	        myName = name;
 	        myLocation = location;
 	        myDescription = description;
 	        myStartTime=startTime;
 	        myEndTime=endTime;
 	        myInterval = new Interval(startTime,endTime);
 	        myURL = URL;
 	    }
 	    
 	    public String toString(){
 	    	return "Event[" + myName + ", " + myLocation + ", " + myDescription + "]\n";
 	    }
 	    
 	    public DateTime getStartTime(){
 	    	return myStartTime;
 	    }
 	    public DateTime getEndTime(){
 	    	return myEndTime;
 	    }
 	    public String getEventDescription(){
 	    	return myDescription; 
 	    }
 	    public String getLocation(){
 	    	return myLocation;
 	    }
 	    public String getName(){
 	    	return myName;
 	    }
 	    public Interval getInterval(){
 	    	return myInterval;
 	    }
 	    public String getURL() {
 	        return myURL;
 	    }
 	    public String getStartYear(){
	    	return "myStartTime.getYear()";
 	    }
 	    public String getStartMonth(){
	    	return "myStartTime.getMonthOfYear()";
 	    }
 	    public String getStartDayOfMonth(){
	    	return "myStartTime.getDayOfMonth()";
 	    }
 	    public String getStartHourOfDay(){
	    	return "myStartTime.getHourOfDay()";    	
 	    }
 	    public String getStartMinuteOfHour(){
	    	return "myStartTime.getMinuteOfHour()";
 	    }
 	    public String getEndYear(){
	    	return "myEndTime.getYear()";
 	    }
 	    public String getEndMonth(){
	    	return "myEndTime.getMonthOfYear()";
 	    }
 	    public String getEndDayOfMonth(){
	    	return "myEndTime.getDayOfMonth()";
 	    }
 	    public String getEndHourOfDay(){
	    	return "myEndTime.getHourOfDay()";    	
 	    }
 	    public String getEndMinuteOfHour(){
	    	return "myEndTime.getMinuteOfHour()";
 	    }
     
 }
