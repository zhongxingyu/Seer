 /**
  * A time slot corresponds to the start and end time of an Appointment Block. 
  */
 
 package gui;
 
 public class TimeSlot {
 	/** The starting time of an appointment, in minutes since 12AM. */
 	private int startTime;
 	/** The ending time of an appointment, in minutes since 12AM. */
 	private int endTime;
 	
 	/** Constructs a time slot object given starting and ending times. */
 	public TimeSlot(int st, int et) {
 		startTime = st;
 		endTime = et;
 	}
 	
 	/** Returns the starting time of an appointment. */
 	public int getStartTime() {
 		return startTime;
 	}
 	
 	/** Returns the ending time of an appointment. */
 	public int getEndTime() {
 		return endTime;
 	}
 	
 	/** Returns the duration of an appointment. */
 	public int length() {
 		return endTime - startTime;
 	}
 	
 	/** Returns the number of hours given time in minutes. */
 	public int getHours(int time) {
 		return time/60;
 	}
 	
 	/** Returns the remainder number of minutes in an hour, given time in minutes. */
 	public int getMinutes(int time) {
 		return time%60;
 	}
 	
 	/** Displays the time slot as a string. */
 	public String toString() {
 		String string= "" + getHours(startTime) + ":" + getMinutes(startTime) + " - " + getHours(endTime) + ":" + getMinutes(endTime);
 		return string;
 	}
         
         /** Displays the time slot as a string. */
 	public String to24HrStringStart() {
		String string= "" + getHours(startTime) + ":" + getMinutes(startTime);
		return string;
 	}
         
          /** Displays the time slot as a string. */
 	public String to24HrStringEnd() {
		String string= "" + getHours(endTime) + ":" + getMinutes(endTime);
		return string;
 	}        
 	
 }
