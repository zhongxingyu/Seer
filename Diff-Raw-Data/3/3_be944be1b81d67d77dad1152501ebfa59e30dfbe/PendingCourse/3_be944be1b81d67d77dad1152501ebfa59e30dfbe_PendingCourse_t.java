 package helper;
 
 import java.util.ArrayList;
 
 public class PendingCourse {
 
 	//Declaration of Variables
 	private String coursePrefix;
 	private String courseNumber;
 	private String courseTitle;
 	private int callNumber;
 	private ArrayList<ClassMeeting> classMeetingsList;
 	
 	/**
 	 * 
 	 * @param coursePrefix
 	 * @param courseNumber
 	 * @param callNumber
 	 */
 	public PendingCourse(String coursePrefix, String courseNumber, int callNumber) {
 		this.coursePrefix = coursePrefix;
 		this.courseNumber = courseNumber;
 		this.callNumber = callNumber;
		CourseDAO course = new CourseDAO();
		classMeetingsList = course.getMeetings(new Requirement(coursePrefix, courseNumber), callNumber);
 	}	
 
 	/**
 	 * 
 	 * @param coursePrefix
 	 * @param courseNumber
 	 * @param courseTitle
 	 * @param callNumber
 	 */
 	public PendingCourse(String coursePrefix, String courseNumber, String courseTitle,
 			int callNumber) {
 		this.coursePrefix = coursePrefix;
 		this.courseNumber = courseNumber;
 		this.callNumber = callNumber;
 		this.courseTitle = courseTitle;
 		classMeetingsList = new ArrayList<ClassMeeting>();
 	}	
 
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getCallNumber() {
 		return callNumber;
 	}
 
 	/**
 	 * 
 	 * @param callNumber
 	 */
 	public void setCallNumber(int callNumber) {
 		this.callNumber = callNumber;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public ArrayList<ClassMeeting> getClassMeetingsList() {
 		return classMeetingsList;
 	}
 	
 	/**
 	 * 
 	 * @param classMeeting
 	 */
 	public void addClassMeetingList(ClassMeeting classMeeting){
 		classMeetingsList.add(classMeeting);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getCoursePrefix() {
 		return coursePrefix;
 	}
 
 	/**
 	 * 
 	 * @param coursePrefix
 	 */
 	public void setCoursePrefix(String coursePrefix) {
 		this.coursePrefix = coursePrefix;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getCourseNumber() {
 		return courseNumber;
 	}
 
 	/**
 	 * 
 	 * @param courseNumber
 	 */
 	public void setCourseNumber(String courseNumber) {
 		this.courseNumber = courseNumber;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getCourseTitle() {
 		return courseTitle;
 	}
 
 	/**
 	 * 
 	 * @param courseTitle
 	 */
 	public void setCourseTitle(String courseTitle) {
 		this.courseTitle = courseTitle;
 	}
 
 	
 }
