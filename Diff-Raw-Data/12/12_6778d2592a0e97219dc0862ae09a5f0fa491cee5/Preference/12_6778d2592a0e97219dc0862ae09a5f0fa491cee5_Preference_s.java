 package scheduleGenerator.student;
 
 import scheduleGenerator.Constants;
 import scheduleGenerator.time.TimeSlot;
 import scheduleGenerator.time.TimeSlotList;
 
 public class Preference extends TimeSlotList{
 	private int maxCourseNum;
 	private Constants.Season season;
 	
 	public Preference(int maxCourseNum, Constants.Season semester) {
 		this.maxCourseNum = maxCourseNum;
 		this.season = semester;
 	}
 	
 	public int getMaxCourseNum() {
 		return maxCourseNum;
 	}
 	
 	public void setMaxCourseNum(int maxCourses) {
 		this.maxCourseNum = maxCourses;
 		System.out.println("Pref Max Course Num changed to: "+maxCourses);
 	}
 	
 	public Constants.Season getSeason() {
 		return season;
 	}	
 	
 	public void setSeason(Constants.Season season) {
 		this.season = season;
 		System.out.println("Pref Season/semester changed to: "+season);
 	}
 
 	public boolean addBreak(String startTime, String endTime, Constants.Day day) {
 		try{
 			//verify if they are valid time inputs
 			if (startTime.length()!=4 || endTime.length()!=4)
 				return false;
 						
 			int startHour = Integer.parseInt(startTime.substring(0, 2));
 			int startMin = Integer.parseInt(startTime.substring(2));			
 			int endHour = Integer.parseInt(endTime.substring(0, 2));
 			int endMin = Integer.parseInt(endTime.substring(2));
 			System.out.println("Start = "+startHour+":"+startMin);
 			System.out.println("End = "+endHour+":"+endMin);
 			
 			if (startHour > 23 || startHour < 0 || endHour > 23 || endHour < 0)
 				return false;
 			
 			if (startMin > 59 || startMin < 0 || endMin > 59 || endMin < 0)
 				return false;
 			
 			// Add break operations
 			double start = Double.parseDouble(startTime);
 			double end = Double.parseDouble(endTime);					
 			
 			if (end > start && checkBreakConflicts(startTime,endTime,day)==true) {
				TimeSlot ts = new TimeSlot(startTime, endTime, day);
 				timeSlots.add(ts);
 				System.out.println("Add break successful: "+startTime+" to "+endTime+" on "+day);
 				System.out.println("Array size: "+timeSlots.size());
 				return true;
 			}
 			
 			else{
 				System.out.println("Add break rejected: "+startTime+" to "+endTime+" on "+day);
 				return false;
 			}
 			
 		}
 		
 		catch(Exception e) {
 			System.err.println("Add break timeslot system failed");
 		}
 		return false;
 	}
 	
 	public boolean removeBreak(int index) {
 		try {
 		// verify that index number is valid			
 			if (index <= timeSlots.size()) {
 				String start = timeSlots.get(index).getStartTime();
 				String end = timeSlots.get(index).getEndTime();
 				Constants.Day day = timeSlots.get(index).getDay().get(0);
 				
 				timeSlots.remove(index);
 				System.err.println("Removing break at index "+index+" successful");
 				System.err.println("Timeslot was: "+start+" to "+end+" on "+day);
 				return true;
 			}
 			else{
 				System.err.println("Removing break at index "+index+" unsuccessful");
 				return false;
 			}
 		}
 		
 		catch(Exception e) {
 			System.err.println("Removing break timeslot system failed");
 		}
 		return false;
 	}
 	
 	public void clearBreaks() {
 		timeSlots.clear();
 		System.err.println("Break timeslots cleared");
 	}
 	
 	private boolean checkBreakConflicts(String startTime, String endTime, Constants.Day day) {
 		double start = Double.parseDouble(startTime);
 		double end = Double.parseDouble(endTime);
 		
 		for(int i=0; i<timeSlots.size();i++) {
 			double start2 = Double.parseDouble(timeSlots.get(i).getStartTime());
 			double end2 = Double.parseDouble(timeSlots.get(i).getEndTime());
 			Constants.Day day2 = timeSlots.get(i).getDay().get(0);
 			
 			if (day == day2) {
 				if (start >= start2 && start < end2) {
 					System.err.println("CONFLICT with: "+start+" to "+end+" and "+start2+" to "+end2+" on "+day);
 					return false;
 				}
 				
 				else if (end > start2 && end <=end2) {
 					System.err.println("CONFLICT with: "+start+" to "+end+" and "+start2+" to "+end2+" on "+day);
 					return false;
 				}
 				
 				else if (start <= start2 && end >= end2) {
 					System.err.println("CONFLICT with: "+start+" to "+end+" and "+start2+" to "+end2+" on "+day);
 					return false;
 				}
 			}
 		}
 		
 		System.err.println("Break check accepted for: "+startTime+" to "+endTime+" on "+day);
 		return true;		
 	}
 }
