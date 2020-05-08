 package scheduleGenerator;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import scheduleGenerator.Constants.EntryMonth;
 import scheduleGenerator.Constants.Season;
 import scheduleGenerator.course.Course;
 import scheduleGenerator.course.CourseId;
 import scheduleGenerator.course.Section;
 import scheduleGenerator.courseSequence.CourseSequence;
 import scheduleGenerator.courseSequence.Semester;
 import scheduleGenerator.student.Preference;
 import scheduleGenerator.student.SelectedCourse;
 import scheduleGenerator.student.StudentRecord;
 import scheduleGenerator.student.StudentSchedule;
 import scheduleGenerator.time.TimeSlot;
 
 import database.Database;
 
 /**
  * This class is the factory that will create the schedule for the students
  * 
  * @author s0m31
  *
  */
 public class ScheduleFactory {
 	
 	/*
 	 *Location of all the xml files 
 	 */
 	private String studentRecordFile = "http://localhost:8080/persSchedulerWeb/xmldb/studentRecord.xml";
 	private String sequenceFile = "http://localhost:8080/persSchedulerWeb/xmldb/sequence.xml";
 	private String courseScheduleFile = "http://localhost:8080/persSchedulerWeb/xmldb/courses.xml";
 
 	/*
 	 * Database objects containing the data from the xml
 	 */
 	private Database studentRecordDb = null;
 	private Database sequenceDb = null;
 	private Database courseScheduleDb = null;
 
 	/*
 	 * Objects that are needed to create the schedule
 	 */
 	private StudentRecord studentRecord;
 	private CourseSequence sequence;
 	private List<Course> courseList;
 
 	/**
 	 * Constructor of the factory
 	 */
 	public ScheduleFactory() {
 
 		// initialize database
 		this.studentRecordDb = new Database(this.studentRecordFile);
 		this.sequenceDb = new Database(this.sequenceFile);
 		this.courseScheduleDb = new Database(courseScheduleFile);
 
 		// get the list of courses form the xml
 		getCourseList();
 	}
 	
 	
 	/**
 	 * 
 	 * @param courseId
 	 * @return
 	 */
 	public Course getCourseTechnicalElective(CourseId courseId) {
 		Course course = new Course();
 		
 		return course;
 	}
 	
 	
 	
 	
 	/**
 	 * Searches for Course based on courseId
 	 * 
 	 * @param courseId
 	 * @return course
 	 */
 	public Course getCourseFromCourseId(CourseId courseId, Season season) {
 		Course course = new Course();
 		if (courseId.getDepartment().equals("TECHNICALELECTIVE")) {
 			course = getCourseTechnicalElective(courseId);
 		}
 		else {
 			for (int i = 0; i < courseList.size(); i++) {
 				Course courseIdFromList = courseList.get(i);
 			
 				if (courseId.toString().equals(courseIdFromList.getCourseId().toString()) &&
 						(season ==  courseList.get(i).getSeason())) {
 					course = courseIdFromList;
 					return course;
 				}
 				
 			}
 		}
 		return course;
 	}
 
 	
 	/**
 	 * Verifies if course is overlapping with preference.
 	 * @param preference
 	 * @param course
 	 * @return
 	 */
 	public boolean isOverlappingWithPreference(Preference preference, Course course) {
 		boolean isOverlappingWithWithPreference = false;
 		for (int i = 0; i < preference.getTimeSlotList().size(); i++) {
 			TimeSlot timeSlotPref = preference.getTimeSlotList().get(i);
 			for(int j = 0; j < preference.getTimeSlotList().size(); j++) {
 				for(int k = 0; k < preference.getTimeSlotList().size(); k++) {
 					for(int m = 0; m < preference.getTimeSlotList().size(); m++) {
 
 						TimeSlot timeSlotCourse = course.getSections().get(k).getTimeSlotList().get(m);
 						if(TimeSlot.isTimeSlotOverlapping(timeSlotPref, timeSlotCourse)) {
 							isOverlappingWithWithPreference = true;
 							return isOverlappingWithWithPreference;
 						}
 					}
 				}
 			}
 		}
 		return isOverlappingWithWithPreference;
 	}
 	
 	
 	/**
 	 * Attempts at adding course to list of courses. Need to also check for
 	 * prerequisites.
 	 * 
 	 * @param preferece
 	 * @param selectedCourses
 	 * @param course
 	 * @return
 	 */
 	public SelectedCourse getNonOverlappingCourse(	Preference preference,
 													ArrayList<SelectedCourse> selectedCourses,
 													CourseId courseId) {
 		SelectedCourse selectedCourse = null;
 		Section section;
 		
 		Season season = preference.getSeason();
 		Course course = getCourseFromCourseId(courseId, season);
 		if (!isOverlappingWithPreference(preference, course)) { 
 			if (selectedCourses.size() <= 0) {
 				try {
 					section = course.getSections().get(0);
 					selectedCourse = new SelectedCourse(courseId, section);
 				}
 				catch(Exception e) {
 					System.err.println("Could not create selected course for " + courseId.toString());
 				}
 			}
 			else {
 				for (int i = 0; i < selectedCourses.size(); i++) {
 					ArrayList<TimeSlot> timeSlotList = selectedCourses.get(i).getSection().getTimeSlotList();
 					for (int j = 0; j < timeSlotList.size(); j++) {
 						TimeSlot timeSlotCourseList = selectedCourses.get(i).getSection().getTimeSlotList().get(j);
 						for (int k = 0; k < course.getSections().size(); k++) {
 							if(!isSectionOverlapping(course.getSections().get(k), timeSlotCourseList)) {
 								section = course.getSections().get(k);
 								selectedCourse = new SelectedCourse(courseId, section);
								return selectedCourse;
 							}
 						}
 					}
 				}
 			}
 		}
 		else {
 			return null;
 		}
 		return selectedCourse;
 
 	}
 	
 	
 	/**
 	 * Checks if section is overlapping
 	 * @param section
 	 * @param timeSlot
 	 * @return
 	 */
 	private boolean isSectionOverlapping(Section section, TimeSlot timeSlot) {
 		boolean isOverlappingWithCourses = false;
 		for (int m = 0; m < section.getTimeSlotList().size(); m++) {
 			TimeSlot timeSlotCourse = section.getTimeSlotList().get(m);
 			if(TimeSlot.isTimeSlotOverlapping(timeSlot, timeSlotCourse)) {
 				isOverlappingWithCourses = true;
 				return isOverlappingWithCourses;
 			}
 		}
 		return isOverlappingWithCourses;
 	}
 
 	
 	/**
 	 * This method will retrieve the courseList from the database and store it in the
 	 * courseList object
 	 * 
 	 * @return true if successful
 	 * 			false otherwise
 	 */
 	private boolean getCourseList() {
 		// get the document object
 		Document doc = courseScheduleDb.readDoc();
 		// get a nodelist of all the courses
 		NodeList courseNodeList = doc.getChildNodes().item(0).getChildNodes();
 
 		// initilize the courseList
 		courseList = new ArrayList<Course>();
 
 		Node node = null;
 		try {
 			for (int i = 0; i < courseNodeList.getLength(); i++) {
 				node = courseNodeList.item(i);
 				// String temp = node.getTextContent();
 				// only do for fall right now change after
 				if (node.getNodeName().equals("COURSE")) {
 					for (Constants.Season season : Constants.Season.values()) {
 						Course tempCourse = Course.createCourseFromDoc(
 								courseNodeList.item(i), season);
 						if (tempCourse.getSections().size() != 0) {
 							courseList.add(tempCourse);
 						}
 					}
 
 				}
 			}
 			return true;
 		} catch (Exception e) {
 			if (node != null)
 				System.out.println(node.getTextContent());
 			return false;
 		}
 	}
 
 	
 	/**
 	 * This method will retrieve the courseList from the database and store it in the
 	 * courseList object
 	 * 
 	 * returns the courseslist
 	 * @return
 	 */
 	/*
 	private boolean getCourseList() {
 		// get the document object
 		Document doc = courseScheduleDb.readDoc();
 		// get a nodelist of all the courses
 		NodeList courseNodeList = doc.getChildNodes().item(0).getChildNodes();
 
 		// initilize the courseList
 		courseList = new ArrayList<Course>();
 
 		Node node = null;
 		try {
 			for (int i = 0; i < courseNodeList.getLength(); i++) {
 				node = courseNodeList.item(i);
 				// String temp = node.getTextContent();
 				// only do for fall right now change after
 				if (node.getNodeName().equals("COURSE")) {
 					for (Constants.Season season : Constants.Season.values()) {
 						Course tempCourse = Course.createCourseFromDoc(
 								courseNodeList.item(i), season);
 						if (tempCourse.getSections().size() != 0) {
 							courseList.add(tempCourse);
 						}
 					}
 
 				}
 			}
 			return true;
 		} catch (Exception e) {
 			if (node != null)
 				System.out.println(node.getTextContent());
 			return false;
 		}	
 	}*/
 	
 	public List<Course> getScheduleCourseList() {
 		return courseList;
 	}
 
 	/**
 	 * Retrieve the student record from the database and returns a student
 	 * object
 	 * 
 	 * @return
 	 */
 	public StudentRecord getStudentRecord(String userID) {
 		StudentRecord record;
 		Document doc = studentRecordDb.readDoc();
 		NodeList recordList = doc.getChildNodes().item(0).getChildNodes();
 
 		Node studentNode = null;
 		for (int i = 0; i < recordList.getLength(); i++) {
 			boolean foundRecord = false;
 			studentNode = recordList.item(i);
 			NodeList studentNodeList = studentNode.getChildNodes();
 			for (int j = 0; j < studentNodeList.getLength(); j++) {
 				if (studentNodeList.item(j).getNodeName().equals("STUDENTID")) {
 					if (studentNodeList.item(j).getFirstChild().getNodeValue()
 							.equals(userID)) {
 						foundRecord = true;
 						break;
 					} else {
 						continue;
 					}
 				}
 			}
 			if (foundRecord) {
 				break;
 			}
 		}
 		record = StudentRecord.getStudentRecordFromNode(studentNode);
 		return record;
 	}
 
 	/**
 	 * Retrieve the course sequence from the database and returns a Course
 	 * sequence object
 	 * 
 	 * @return
 	 */
 	public CourseSequence getCourseSequence(String concentration,
 			Constants.EntryMonth entryMonth) {
 		CourseSequence courseSequence;
 		Document doc = sequenceDb.readDoc();
 		NodeList sequenceList = doc.getChildNodes().item(0).getChildNodes();
 
 		Node sequenceNode = null;
 		for (int i = 0; i < sequenceList.getLength(); i++) {
 			boolean foundConcentration = false;
 			boolean foundEntryMonth = false;
 			sequenceNode = sequenceList.item(i);
 			NodeList sequencetNodeList = sequenceNode.getChildNodes();
 			for (int j = 0; j < sequencetNodeList.getLength(); j++) {
 				if (sequencetNodeList.item(j).getNodeName()
 						.equals("CONCENTRATION")) {
 					if (sequencetNodeList.item(j).getFirstChild()
 							.getNodeValue().equals(concentration)) {
 						foundConcentration = true;
 					}
 				} else if (sequencetNodeList.item(j).getNodeName()
 						.equals("ENTRYMONTH")) {
 					String monthString = sequencetNodeList.item(j)
 							.getFirstChild().getNodeValue();
 					EntryMonth month = Constants
 							.getEntryMonthFromString(monthString);
 					if (entryMonth == month) {
 						foundEntryMonth = true;
 					}
 				}
 			}
 			if (foundConcentration && foundEntryMonth) {
 				break;
 			}
 		}
 		courseSequence = CourseSequence.getCourseSequenceFromNode(sequenceNode);
 		return courseSequence;
 	}
 
 	/**
 	 * Determines courses to be taken based on semester
 	 * 
 	 * @param studentId
 	 * @param preference
 	 * @return
 	 */
 //	public ArrayList<CourseId> getStudentScheduleFromPreference(
 //			String studentId, Preference preference) {
 //		ArrayList<CourseId> coursesToTake = new ArrayList<CourseId>();
 //		StudentRecord record = getStudentRecord(studentId);
 //		Season semester = record.getStartingSemester();
 //		EntryMonth month = Constants.convertSeasonToMonth(semester);
 //
 //		Season scheduleSeason = preference.getSeason();
 //		CourseSequence sequence = getCourseSequence(record.getConcentration(),
 //				month);
 //		
 //		StudentSchedule studentSchedule = new StudentSchedule(scheduleSeason);
 //
 //		for (int i = 0; i < record.getCoursesCompleted().size(); i++) {
 //
 //			for (int j = 0; j < sequence.getAcademicYears().size(); j++) {
 //				for (int k = 0; k < sequence.getAcademicYears().get(j)
 //						.getSemesters().size(); k++) {
 //					Semester semesterSequence = sequence.getAcademicYears()
 //							.get(j).getSemesters().get(k);
 //					Season seasonSequence = semesterSequence.getSeason();
 //					if (seasonSequence == scheduleSeason) {
 //						ArrayList<CourseId> coursesOffered = semesterSequence
 //								.getCourseIds();
 //						for (int m = 0; m < coursesOffered.size(); m++) {
 //							if (coursesToTake.size() >= preference
 //									.getMaxCourseNum()) {
 //								return coursesToTake;
 //							} else if (!record.getCoursesCompleted().get(i)
 //									.toString()
 //									.equals(coursesOffered.get(m).toString())) {
 //								coursesToTake.add(coursesOffered.get(m));
 //							}
 //						}
 //
 //					}
 //				}
 //			}
 //
 //		}
 //
 //		return coursesToTake;
 //	}
 	
 
 	/**
 	 * Creates schedule.  Will compare preference time slots and courses already added.
 	 * 
 	 * @param studentId
 	 * @param preference
 	 * @return
 	 */
 	public StudentSchedule makeStudentSchedule(String studentId,
 			Preference preference) {
 		StudentRecord record = getStudentRecord(studentId);
 		Season semester = record.getStartingSemester();
 		EntryMonth month = Constants.convertSeasonToMonth(semester);
 
 		Season scheduleSeason = preference.getSeason();
 		CourseSequence sequence = getCourseSequence(record.getConcentration(),
 				month);
 		StudentSchedule studentSchedule = new StudentSchedule(scheduleSeason);
 		for (int i = 0; i < record.getCoursesCompleted().size(); i++) {
 
 			for (int j = 0; j < sequence.getAcademicYears().size(); j++) {
 				for (int k = 0; k < sequence.getAcademicYears().get(j)
 						.getSemesters().size(); k++) {
 					Semester semesterSequence = sequence.getAcademicYears()
 							.get(j).getSemesters().get(k);
 					Season seasonSequence = semesterSequence.getSeason();
 					if (seasonSequence == scheduleSeason) {
 						ArrayList<CourseId> coursesOffered = semesterSequence
 								.getCourseIds();
 						for (int m = 0; m < coursesOffered.size(); m++) {
 							if (studentSchedule.getSelectedCourses().size() >= preference
 									.getMaxCourseNum()) {
 								return studentSchedule;
 							} else if (!record.getCoursesCompleted().get(i)
 									.toString()
 									.equals(coursesOffered.get(m).toString())) {
 								SelectedCourse selectedCourse = getNonOverlappingCourse(	preference,
 																							studentSchedule.getSelectedCourses(),
 																							coursesOffered.get(m));
 								if (selectedCourse != null) {
 									System.out.println("courses added");
 									studentSchedule.addSelectedCourse(selectedCourse);
 
 								}
 							}
 						}
 					}
 				}
 			}
 
 		}
 		System.out.println("ending schedule make");
 		return studentSchedule;
 	}
 }
