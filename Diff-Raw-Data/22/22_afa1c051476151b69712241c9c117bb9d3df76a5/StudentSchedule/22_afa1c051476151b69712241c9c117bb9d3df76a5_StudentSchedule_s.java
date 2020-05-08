 package scheduleGenerator.student;
 
 import java.util.ArrayList;
 
 import scheduleGenerator.Constants;
 import scheduleGenerator.Constants.CourseType;
 import scheduleGenerator.Constants.Day;
 import scheduleGenerator.Constants.Season;
 import scheduleGenerator.course.CourseId;
 import scheduleGenerator.course.Section;
 import scheduleGenerator.time.TimeSlot;
 
 public class StudentSchedule {
 	private ArrayList<SelectedCourse> selectedCourses;
 	private Season season;
 	
 	
 	public StudentSchedule(Season season) {
 		this.season = season;
 		this.selectedCourses = new ArrayList<SelectedCourse>();
 	}
 	
 	
 	public void setSelectedCourses(ArrayList<SelectedCourse> selectedCourses) {
 		this.selectedCourses = selectedCourses;
 	}
 	
 	
 	public void addSelectedCourse(SelectedCourse selectedCourse) {
 		selectedCourses.add(selectedCourse);
 	}
 	
 	public void addSelectedCourse(CourseId courseId, Section section) {
 		SelectedCourse selectedCourse = new SelectedCourse(courseId, section);
 		selectedCourses.add(selectedCourse);
 	}
 	
 	
 	public ArrayList<SelectedCourse> getSelectedCourses(){
 		return selectedCourses;
 	}
 	
 	public String toString() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("Season: " + season + "\n");
 		for (int i = 0; i< selectedCourses.size(); i++) {
 			buffer.append(selectedCourses.get(i).getSection() + "\n");
 		}
 		return buffer.toString();
 	}
 	
 	static public StudentSchedule getFakeSchedule(){
 		StudentSchedule sched = new StudentSchedule(Constants.Season.FALL);
 		//course 1
 		CourseId courseID = new CourseId("SOEN","341");
 		Section sect = new Section("ty");
 		TimeSlot ts = new TimeSlot("14:00", "15:15", "akdjflk",
 				"efawef", Constants.CourseType.LECT, Constants.Day.MONDAY,
 				"TY");
 		TimeSlot ts2 = new TimeSlot("14:00", "15:15", "akdjflk",
 				"efawef", Constants.CourseType.TUT, Constants.Day.WEDNESDAY,
 				"TY");
 		sect.getTimeSlotList().add(ts);
 		sect.getTimeSlotList().add(ts2);
 		sched.addSelectedCourse(courseID, sect);
 		
 		//course2
 		CourseId courseID2 = new CourseId("COEN","490");
 		Section sect2 = new Section("fg");
 		TimeSlot ts21 = new TimeSlot("16:00", "17:15", "akdjflk",
 				"efawef", Constants.CourseType.LECT, Constants.Day.MONDAY,
 				"fg");
 		TimeSlot ts22 = new TimeSlot("14:00", "15:15", "akdjflk",
 				"efawef", Constants.CourseType.TUT, Constants.Day.FRIDAY,
 				"fg");
 		sect.getTimeSlotList().add(ts21);
 		sect.getTimeSlotList().add(ts22);
 		sched.addSelectedCourse(courseID2, sect2);
 		
 		return sched;
 	}
 }
