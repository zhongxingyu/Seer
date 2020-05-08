 package x464010.teamb.srs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 /**
  * Registrar() class is the controller for scheduling
  * students for courses and saving/retrieving registration
  * records for the system.
  * 
  * @author William Crews
  * @author Amit Dhamija
  * @version	 1.6		
  * @revision 1.0 	William Crews	Initial version		
  * @revision 1.1 	William Crews	Added the following methods:
  * 									saveRegistration, saveCoursesAll, myCourseSchedule
  * @revision 1.2 	Amit Dhamija	Extended to Console class
  * 									Added/implemented show() method
  * @revision 1.3	Amit Dhamija	Updated the show() method to handle "register" and "unregister" options more efficiently
  * @revision 1.4	William Crews	Updated the isValidCourseID, isAlreadyRegistered, isCourseFull, incrementCourseEnrollment
  * 									to take additional parameters.
  * @revision 1.5	Amit Dhamija	Fixed the bug where it was multiplying my course list for every run;
  * 									clear the studentRegistrations in loadRegistrationFile() method
  * @revision 1.6	William Crews	Fixed unregisterFromCourse to correctly unregister from a course and decrement
  *                                  the course studentsEnrolled counter before writing out the updated registration records
  *                                  to the Registration.txt file.
  */
 public class Registrar extends Console
 {
 	protected ArrayList<Registration> studentRegistrations;
 	protected ArrayList<Course> courses;
 
 	public static String REGISTER = "register";
 	public static String UNREGISTER = "unregister";
 
 	public Registrar()
 	{
 		studentRegistrations = new ArrayList<Registration>();
 		courses = new ArrayList<Course>();
 	}
 	
 	public void show(String option) {
 		try {
 			Scanner inputScanner = Console.getInputScanner();
 			String courseId = "";
 			int studentId = 0;
 			
 			System.out.println();
 			studentId = StudentRegistrationSystem.getLogin().getStudent().getStudentID();
 			
 			if (option == REGISTER) {
 				System.out.println(Constants.STARS + Constants.OPTION_REGISTER_COURSE + Constants.STARS);
 				
 				System.out.print(Constants.COURSE_ID);
 				courseId = inputScanner.nextLine();
 				
 				if (registerForCourse(studentId,courseId)) {
 					StudentRegistrationSystem.getCourseCatalog().show();
 				}
 				else {
 					show(REGISTER);
 				}
 			}
 			else if (option == UNREGISTER) {
 				System.out.println(Constants.STARS + Constants.OPTION_UNREGISTER_COURSE + Constants.STARS);
 				
 				System.out.print(Constants.COURSE_ID);
 				courseId = inputScanner.nextLine();
 				
 				if (unregisterFromCourse(studentId,courseId)) {
					StudentRegistrationSystem.getMyCourseSchedule().show();
 				}
 				else {
 					show(UNREGISTER);
 				}
 			}
 		} catch (InputMismatchException e) {
 			System.out.println(this.getClass().getName() + ": Error! " + e.getMessage());
 		}
 	}
 	
 	@Override
 	protected void showOptionList() {
 		
 	}
 	
 	@Override
 	protected void selectOption(int option) {
 		
 	}
 	
 	/**
 	 * loadRegistrationFile() method load the student registration files into the 
 	 * system by reading the Registration.txt file and adding records from each line.
 	 * 
 	 * @author William Crews	
 	 */
 	public ArrayList<Registration> loadRegistrationFile()
 	{
 		try {
 			File studentRegListFile = new File(Constants.REGISTRATION_FILE_PATH);
 			Scanner fileScanner = new Scanner(studentRegListFile);
 			Registration tempRegistration;
 			
 			// reset the list
 			studentRegistrations.clear();
 
 			while (fileScanner.hasNextLine()) {
 				String[] studentRegAttributes = fileScanner.nextLine().split(",");
 				tempRegistration = new Registration(studentRegAttributes);
 				studentRegistrations.add(tempRegistration);
 			}
 
 			Collections.sort(studentRegistrations, new Registration() );
 			fileScanner.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return studentRegistrations;
 	}
 
 	/**
 	 * loadCourseFile() method loads the Course File and all
 	 * the course info into the system so that it can be used
 	 * with the registration system for reading and writing
 	 * registration info.
 	 * 
 	 * @author	William Crews
 	 */
 	public ArrayList<Course> loadCourseFile() 
 	{
 		ArrayList<Course> regCourseList = new ArrayList<Course>();
 		
 		try {
 			File courseListFile = new File(Constants.COURSE_LIST_FILE_PATH);
 			Scanner fileScanner = new Scanner(courseListFile);
 			Course tempCourse;
 			while (fileScanner.hasNextLine()) {
 				String[] courseAttributes = fileScanner.nextLine().split(",");
 				tempCourse = new Course(courseAttributes[0], 
 						courseAttributes[1], 
 						courseAttributes[2], 
 						courseAttributes[3],
 						courseAttributes[4],
 						new Integer(courseAttributes[5]).intValue(),
 						new Integer(courseAttributes[6]).intValue());
 				regCourseList.add(tempCourse);
 			}
 
 			Collections.sort(regCourseList);
 			fileScanner.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return regCourseList;
 	}
 
 	/**
 	 * registerForCourse() method is used to register a student for a course
 	 * after passing a few checks.  The checks implemented are:
 	 *        1) Is course number entered in the system?
 	 *        2) Is student already registered for course?
 	 *        3) Is the class full, has it reached its max limit?
 	 *        
 	 *  After passing these checks the student is registered for the course
 	 *  and the information is saved in the Registration.txt file.
 	 *  
 	 * @author William Crews
 	 * @param studentID
 	 * @param courseID
 	 * @return boolean	true/false
 	 */
 	public boolean registerForCourse(int studentID, String courseID)
 	{
 		int regNum = getNewRegNum();
 		Registration newStudentReg = new Registration(regNum, studentID, courseID);
 		ArrayList<Registration> studentReg = new ArrayList<Registration>();
 		ArrayList<Course> regCourseList = new ArrayList<Course>();
 		
 				
 		if(regCourseList.isEmpty()){
 			regCourseList = loadCourseFile();
 		}
 		if(studentReg.isEmpty()){
 			studentReg = loadRegistrationFile();
 		}
 		
 		// We need to check for a couple of things:
 		// 1) Is course number entered in the system? 
 		// 2) Is student already registered for course?
 		// 3) Is the class full, has it reached it max limit?
 
 		if(!isValidCourseID(courseID, regCourseList)) {
 			System.out.println("Unable to find course listing with that ID number!");
 			return false;
 		}
 		else if(isAlreadyRegistered(studentReg, newStudentReg)) { 
 			System.out.println("Cannot register twice for same course.");
 			return false;
 		}
 		else if(isCourseFull(courseID, regCourseList)) {
 			System.out.println("Sorry, the class has reached its maximum enrollment limit.");
 			return false;
 		}
 		// Add Registration Record
 		studentRegistrations.add(newStudentReg);
 
 		// Don't forget to increase the enrollment counter on the course
 		incrementCourseEnrollment(courseID, regCourseList);
 
 		// Save Registration Record to Registration File
 		saveRegistration(newStudentReg);
 
 		// Save Courses with updated course info
 		saveCoursesAll(regCourseList);
 		
 		// We needed some output to the user that
 		// the course was successfully registered.
 		System.out.println(StudentRegistrationSystem.getLogin().getStudent().getFirstName() + " " +
 						   StudentRegistrationSystem.getLogin().getStudent().getLastName()  + " " +
 				           "with Student ID: " + newStudentReg.getStudentID() + "\nYou have been registered for\n" +
 						   "Course ID: " + newStudentReg.getCourseID() + " on " + newStudentReg.getRegDate());
 		
 		return true;
 
 	}	
 
 	/**
 	 * getNewRegNum() method searches through the student registrations 
 	 * looking for the highest assigned RegNum then increments it and
 	 * returns this value to be used for new RegNum values.
 	 * 
 	 * @author William Crews
 	 * @return newRegNum	next available RegNum value incremented by 1.
 	 */
 	private int getNewRegNum() 
 	{
 		int newRegNum = 0;
 		if (!studentRegistrations.isEmpty())
 			for (Registration r : studentRegistrations)
 				if (newRegNum < r.getRegNum())
 					newRegNum = r.getRegNum();
 		return ++newRegNum;
 	}     
 
 	/**
 	 * idValidCourseID() method checks if the passed courseID is 
 	 * found in the courses list and returns true if found or false
 	 * if not found.
 	 * 
 	 * @author William Crews
 	 * @param id
 	 * @param tempCourseList	ArrayList of type Course to allow a course list
 	 * 							to be passed into the method.
 	 * @return	boolean	true/false
 	 */
 	public boolean isValidCourseID(String id, ArrayList<Course> tempCourseList) 
 	{
 		for (Course c : tempCourseList)
 			if (c.getCourseID().equals(id))
 				return true;
 		return false;
 	}
 
 	/**
 	 * isAlreadyRegistered(Registration checkReg) method is used to
 	 * check if the student has already registered for the course
 	 * before.
 	 * 
 	 * @author William Crews
 	 * @param studentReg		An ArrayList of type Registration
 	 * @param checkReg			Registration to be checked
 	 * @return boolean	true/false
 	 */
 	protected boolean isAlreadyRegistered(ArrayList<Registration> studentReg, Registration checkReg)
 	{
 		for (Registration r : studentReg)
 			if ((r.getStudentID() == checkReg.getStudentID()) && (r.getCourseID().equals(checkReg.getCourseID())))
 				return true;
 		return false;
 	}
 
 	/**
 	 * isCourseFull(String courseID) method is used to 
 	 * check if the course selected for registration is
 	 * full.
 	 * 
 	 * @author William Crews
 	 * @param courseID
 	 * @param regCourseList
 	 * @return boolean	true/false
 	 */
 	protected boolean isCourseFull(String courseID, ArrayList<Course> regCourseList)
 	{
 		for (Course c: regCourseList) {
 			if(c.getCourseID().equals(courseID)) {
 				if(c.isCourseFilled(c.getCourseLimit(), c.getStudentsEnrolled())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * incrementCourseEnrollment(String courseID) method is used to
 	 * increment the studentEnrolled counter in the courses attributes
 	 * to keep track of how many students have enrolled in the course.
 	 * 
 	 * @author William Crews
 	 * @param courseID
 	 * @param regCourseList
 	 */
 	public void incrementCourseEnrollment(String courseID, ArrayList<Course> regCourseList)
 	{
 		for(Course c: regCourseList) {
 			if(c.getCourseID().equals(courseID)) {
 				int enrolled = c.getStudentsEnrolled();
 				c.setStudentsEnrolled(++enrolled);
 			}
 		}
 	}
 
 	/**
 	 * decrementCourseEnrollment(String courseID) method is used to
 	 * decrement the studentEnrolled counter in the courses attributes
 	 * to keep track of how many students have unregistered from the course.
 	 * 
 	 * @author William Crews
 	 * @param courseID			String containing the courseID.
 	 * @param regCourseList
 	 */
 	protected void decrementCourseEnrollment(String courseID, ArrayList<Course> regCourseList)
 	{
 		for(Course c: regCourseList) {
 			if(c.getCourseID().equals(courseID)) {
 				int enrolled = c.getStudentsEnrolled();
 				c.setStudentsEnrolled(--enrolled);
 			}
 		}
 	}
 
 	/**
 	 * saveRegistration(Registration record) method saves a students
 	 * registration to the Registration.txt file.
 	 * 
 	 * @author William Crews	
 	 * @param record		 Registration record to be written to file.
 	 */
 	public void saveRegistration(Registration record ) 
 	{
 		BufferedWriter buffWriter = null;
 		try {
 			// Open file with append flag set to true will cause string to append to file.
 			buffWriter = new BufferedWriter(new FileWriter(Constants.REGISTRATION_FILE_PATH,true));
 			buffWriter.write( record.getRegNum()    + "," +
 					record.getStudentID() + "," +
 					record.getCourseID()  + "," +
 					record.getRegDate() );
 			buffWriter.newLine();
 			buffWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 
 	/**
 	 * saveCoursesAll(ArrayList<Course> regCourseList) method saves the 
 	 * entire course list object array to the file system overwriting
 	 * any contents that was there.  This is used after the courses
 	 * student enrollment count has been update and needs to be saved
 	 * back to the CourseList.txt file.
 	 * 
 	 * @author William Crews
 	 * @param course		ArrayList of Course object records.
 	 */
 	public void saveCoursesAll(ArrayList<Course> regCourseList)
 	{
 		BufferedWriter buffWriter = null;
 		try {
 			// Open file with boolean flag set to false will cause file to be overwritten
 			// with new data.
 			buffWriter = new BufferedWriter(new FileWriter(Constants.COURSE_LIST_FILE_PATH,false));
 			for (Course c: regCourseList) {
 				buffWriter.write(  c.getCourseID() + "," +
 						c.getStartDate() + "," +
 						c.getEndDate()   + "," +
 						c.getCourseName() + "," +
 						c.getCourseDescription() + "," +
 						c.getCourseLimit() + "," +
 						c.getStudentsEnrolled() );
 				buffWriter.newLine();
 			}
 			buffWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 
 	/**
 	 * saveRegistrationsAll(ArrayList<Registration> studentRegList) method is used
 	 * to save all student registrations in an ArrayList of type Registration to
 	 * the Registration.txt file with records formated on each line using a 
 	 * comma delimiter format.
 	 * 
 	 * @author William Crews
 	 * @param studentRegList	An ArrayList of type Registration.
 	 */
 	public void saveRegistrationsAll(ArrayList<Registration> studentRegList)
 	{
 		BufferedWriter buffWriter = null;
 		try {
 			// Open file with boolean flag set to false will cause file to be overwritten
 			// with new data.
 			buffWriter = new BufferedWriter(new FileWriter(Constants.REGISTRATION_FILE_PATH,false));
 			for (Registration r: studentRegList) {
 				buffWriter.write(   r.getRegNum() + "," +
 						r.getStudentID() + "," + 
 						r.getCourseID() +  "," +
 						r.getRegDate() );
 				buffWriter.newLine();
 			}
 			buffWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * myCourseSchedule(int studentID) method is used to display the courses
 	 * the student is currently registered for.  It will return a list with
 	 * details about the course(s).
 	 * 
 	 * @author William Crews
 	 * @param studentID			Students ID
 	 */
 	public void myCourseSchedule(int studentID)
 	{
 		ArrayList<Registration> regStudentList = new ArrayList<Registration>();
 		ArrayList<Course> regCourseList = new ArrayList<Course>();
 		
 		if(regCourseList.isEmpty()){
 			regCourseList = loadCourseFile();
 		}
 		if(regStudentList.isEmpty()){
 			regStudentList = loadRegistrationFile();
 		}
 		// Loop through registrations looking for students id
 		// and listing out the course info they are registered for.
 		for (Registration r : regStudentList) {
 			if (r.getStudentID() == studentID) {
 				for (Course c : regCourseList) {
 					if (c.getCourseID().equals(r.getCourseID())) {
 						System.out.println(c.toStringCourse());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * unregisterFromCourse(int studentID, String courseID) method is used to
 	 * unregister a student from a course.
 	 * 
 	 * @author William Crews
 	 * @param studentID			Student ID
 	 * @param courseID			Course ID
 	 * @return	boolean			true/false
 	 */
 	public boolean unregisterFromCourse(int studentID, String courseID)
 	{
 		ArrayList<Registration> studentReg = new ArrayList<Registration>();
 		ArrayList<Course> regCourseList = new ArrayList<Course>();
 		
 		// Use 0 as the regNum, it's been reserved for special use
 		// since all assigned registrations start at 1 and go up from there.
 		Registration regToDelete = new Registration(0, studentID, courseID);
 
 		if(regCourseList.isEmpty()){
 			regCourseList = loadCourseFile();
 		}
 		if(studentReg.isEmpty()){
 			studentReg = loadRegistrationFile();
 		}
 
 		if(!studentReg.isEmpty()) {
 			for(Registration r: studentReg) {
 				if((r.getStudentID() == studentID) && (r.getCourseID().equals(courseID.trim()))) {
 					// We've found the record matching studentID & courseID
 					// now we need to lookup the regNum and assign it to the regToDelete object.
 					regToDelete.setRegNum(r.getRegNum());
 
 					// Remove selected registration object from studentRegistration list
 					studentReg.remove(r);
 
 					// Decrement the StudentEnrollment Counter for the Course
 					decrementCourseEnrollment(r.getCourseID(), regCourseList);
 
 					// Resort the student registrations based on regNum
 					Collections.sort(studentReg, new Registration());
 
 					// Save registrations to file
 					saveRegistrationsAll(studentReg);
 
 					// Save courses to file
 					saveCoursesAll(regCourseList);
 
 					// Found and deleted record, return true
 					System.out.println(StudentRegistrationSystem.getLogin().getStudent().getFirstName() + " " +
 							   StudentRegistrationSystem.getLogin().getStudent().getLastName()  + " " +
 					           "with Student ID: " + regToDelete.getStudentID() + "\nYou have been unregistered for\n" +
 							   "Course ID: " + regToDelete.getCourseID() + " on " + regToDelete.getRegDate());
 					
 					return true;
 				}
 			}
 			// If we're here we haven't found a matching record.
 			System.out.println("No matching registration records found.");
 			return false;
 		}
 		return false;
 	}
 }	
