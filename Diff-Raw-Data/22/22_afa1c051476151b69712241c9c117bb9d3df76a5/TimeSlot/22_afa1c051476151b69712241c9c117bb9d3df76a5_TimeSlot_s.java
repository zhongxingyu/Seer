 package scheduleGenerator.time;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import scheduleGenerator.Constants;
 import scheduleGenerator.Constants.CourseType;
 import scheduleGenerator.Constants.Day;
 
 public class TimeSlot {
 
 	private String startTime;
 	private String endTime;
 	private String location;
 	private String teacher;
 	private String section;
 	private Constants.CourseType courseType;
 	private ArrayList<Constants.Day> day;
 
 	/**
 	 * Constructor for preference TimeSlot
 	 * 
 	 * @param startTime
 	 * @param endTime
 	 * @param day
 	 */
 	public TimeSlot(String startTime, String endTime, Constants.Day day) {
 		this.day= new ArrayList<Constants.Day>();
 		this.startTime = startTime;
 		this.endTime = endTime;
 		this.day.add(day);
 		this.courseType = Constants.CourseType.PREFERENCE;
 	}
 
 	/**
 	 * constructor for course TimeSlot
 	 * 
 	 * @param startTime
 	 * @param endTime
 	 * @param location
 	 * @param teacher
 	 * @param courseType
 	 * @param day
 	 */
 	public TimeSlot(String startTime, String endTime, String location,
 			String teacher, Constants.CourseType courseType, Constants.Day day,
 			String section) {
 		this.day = new ArrayList<Constants.Day>();
 		this.startTime = startTime;
 		this.endTime = endTime;
 		this.location = location;
 		this.teacher = teacher;
 		this.courseType = courseType;
 		this.day.add(day);
 		this.section = section;
 	}
 
 	/**
 	 * 
 	 * @param startTime
 	 * @param endTime
 	 * @param location
 	 * @param teacher
 	 * @param courseType
 	 * @param day
 	 * @param section
 	 */
 	public TimeSlot(String startTime, String endTime, String location,
 			String teacher, Constants.CourseType courseType,
 			ArrayList<Constants.Day> day, String section) {
 		this.day = new ArrayList<Constants.Day>();
 		this.startTime = startTime;
 		this.endTime = endTime;
 		this.location = location;
 		this.teacher = teacher;
 		this.courseType = courseType;
 		this.day = day;
 		this.section = section;
 	}
 
 	public String getStartTime() {
 		return startTime;
 	}
 
 	public String getEndTime() {
 		return endTime;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public String getTeacher() {
 		return teacher;
 	}
 
 	public Constants.CourseType getCourseType() {
 		return courseType;
 	}
 
 	public ArrayList<Constants.Day> getDay() {
 		return day;
 	}
 
 	public void setTeacher(String teacher) {
 		this.teacher = teacher;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 	
 	public String getSection(){
 		return section;
 	}
 	
 	public String toString(){
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(startTime + " ");
 		buffer.append(endTime+ " ");
 		buffer.append(location+ " ");
 		buffer.append(teacher+ " ");
 		buffer.append(section+ " ");
		buffer.append(courseType+ " ");
 		return buffer.toString();
 	}
 	
 	/**
 	 * Compares 2 time slots to see if they're overlapping
 	 * @param timeSlot1
 	 * @param timeSlot2
 	 * @return
 	 */
 	public static boolean isTimeSlotOverlapping(TimeSlot timeSlot1, TimeSlot timeSlot2) {
 		boolean isOverlapping = false;
 		
 	    Date startDate1 = null;
 	    Date endDate1 = null;
 	    Date startDate2 = null;
 	    Date endDate2 = null;		
 	    		
 	    String startString1 = timeSlot1.getStartTime();
 	    String endString1 = timeSlot1.getEndTime();
 	    
 	    String startString2 = timeSlot2.getStartTime();
 	    String endString2 = timeSlot2.getEndTime();
 	    for (int i = 0; i < timeSlot1.getDay().size(); i++) {
 	    	for (int j = 0; j < timeSlot2.getDay().size(); j++) {
 				if(timeSlot1.getDay().get(i) == timeSlot2.getDay().get(j)) {
 				    try {
 				      SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
 				      startDate1 = formatter.parse(startString1);
 				      endDate1 = formatter.parse(endString1);
 				      startDate2 = formatter.parse(startString2);
 				      endDate2 = formatter.parse(endString2);
 				      if (startDate1.before(startDate2) && endDate1.after(startDate2)) {
 				    	  isOverlapping = true;
 				      }
 				      else if (startDate1.before(endDate2) && endDate1.after(endDate2)) {
 				    	  isOverlapping = true;
 			
 				      }
 				      else if (startDate1.equals(startDate2) || endDate1.equals(endDate2)) {
 				    	  isOverlapping = true;
 				      }
 				    } catch (ParseException e) {
 				    	System.out.println(e.toString());
 				    	e.printStackTrace();
 				    }
 		    	}
 	    	}
 	    }
 		return isOverlapping;
 	}
 }
