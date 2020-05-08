 package objects;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.text.DecimalFormat;
 
 /**
  * Course defines a actual teacher's course, with name, course ID, course number, section number,
  * building, room number, meeting time, an ArrayList of Students, an ArrayList of AssignmentCategories,
  * and a gradeBook object to hold all of the students grades as they will appear on the final html5 document
  * screen in the application
  * 
  * @Jesse W. Milburn
  * @date 01 October, 2013
  */
 
 public class MyCourse {
     //course data kept private so only method calls can change them
     private String courseName;
     private String courseID;
     private int courseNumber;
     private String section;
     private String building;
     private String roomID;
     private String meetingTime;
     private String semester;
     private Integer lastAssignmentIndex;
     private Integer lastCategoryIndex;
     private List<Student> students = new ArrayList<Student>();
     private List<AssignmentCategory> categories = new ArrayList<AssignmentCategory>();
     private List<GhostStudent> ghostStudents = new ArrayList<GhostStudent>();
     private List<Object> allStudents = new ArrayList<Object>();
     private final PseudoNameGenerator pnGenerator;
     private DecimalFormat decimalFormat = new DecimalFormat("#.#");
     private boolean isGradingWeighted;
     private boolean isNewCourse = false;
     
     /**
      * Constructs a new MyCourse object, note there is no 'empty' constructor
      * 
      * @param   cn  string denoting what the callee has named the course
      */
     public MyCourse(String cn) {
         this.pnGenerator = new PseudoNameGenerator();
         courseName = cn;
         lastAssignmentIndex = null;
         lastCategoryIndex = null;
     }
     
     /**
      * Takes an Assignment object as a parameter and assigns initial ghost grades
      * to all of the ghost students based on the mean of the real students while
      * keeping the ranges similar 
      * 
      * @param assignment
      */
     public void assignGhostGrades(Assignment assignment) {
    	    String[] ghostNames = new String[ghostStudents.size()];
     	populateNames(ghostNames);
     	assignment.setGhostGrades(ghostNames);
     }
     
     /**
      * 
      * 
      * @param names
      */
     private void populateNames(String[] names) {
     	for (int i = 0; i < names.length; i++) {
     		names[i] = ghostStudents.get(i).getPseudoName();
     	}
     }
     
     
     /**
      * Set the name of the course.
      * 
      * @param   cn  set the courseName
      */
     public void setName(String cn) {
         courseName = cn;
     }
     
     /**
      * Set the course ID of the course
      * 
      * @param   cid set the courseID
      */
     public void setCourseID(String cid) {
         courseID = cid;
     }
     
     /**
      * Set the course number of the course
      * 
      * @param   cnum    set the course number
      */
     public void setCourseNumber(int cnum) {
         courseNumber = cnum;
     }
     
     /**
      * Set the section identification of the course.
      * 
      * @param   sec    set the section identification
      */
     public void setSection(String sec) {
         section = sec;
     }
     
     /**
      * Set the building name
      * 
      * @param   bn    set the building name
      */
     public void setBuilding(String bn) {
         building = bn;
     }
     
     /**
      * Set the room number identification
      * 
      * @param   rn    set the room identification
      */
     public void setRoomID(String rn) {
         roomID = rn;
     }
     
     /**
      * Set the meeting time 
      * 
      * @param	mt      set the meeting time
      */
     public void setMeetingTime(String mt) {
         meetingTime = mt;
     }
     
     /**
      * Set the meeting time 
      * 
      * @param   sm   set the semester
      */
     public void setSemester(String sm) {
         semester = sm;
     }
     
     /**
      * Sets last assignment edited
      * 
      * @param   i	index of last assignment
      */
     public void setLastAssignmentIndex(Integer i) {
         lastAssignmentIndex = i;
     }
     
     /**
      * Sets last category edited
      * 
      * @param   i	index of last category
      */
     public void setLastCategoryIndex(Integer i) {
         lastCategoryIndex = i;
     }
     
     /**
      * Gets identifying name for file names and buttons in menu
      * 
      * @return		gets identifying string
      */
     public String getIdentifier() {
     	return getCourseID() + getCourseNumber() + "-" + 
     			getSection() + " " +  getSemester();
     }
     
     /**
      * Get the name of the course
      * 
      * @return      get the name of the course
      */
     public String getName() {
         return courseName;
     }
     
     /**
      * Get the course identification
      * 
      * @return      course identification
      */
     public String getCourseID() {
         return courseID;
     }
     
     /**
      * Get the course number
      * 
      * @return      course number
      */
     public int getCourseNumber() {
         return courseNumber;
     }
     
     /**
      * Returns the course grade average, all return values are
      * as a percentage not the actual grade.
      * helper functions trail the main function
      * 
      * @return			Average course grade as a percentage
      */
     public Double getCourseAverageGrade() {
     	double average = 0.0;
     	
     	if (isGradingWeighted) {
     		return this.getCourseWeightedGrade();
     	}
     	
     	double studentPoints = getTotalStudentPoints();
     	double totalPoints = getTotalCoursePoints();
     	average = studentPoints / totalPoints;
     	
     	return Double.parseDouble(decimalFormat.format(average * 100));
     }
     public Double getTotalStudentPoints() {
     	double total = 0.0;
     	
     	for (int i = 0; i < categories.size(); i++) {
     		total += categories.get(i).getTotalCategoryStudentPoints(students);
     	}
     	
     	return total;
     }
     public Double getTotalCoursePoints() {
     	double total = 0.0;
     	
     	for (int i = 0; i < categories.size(); i++) {
     		total += categories.get(i).getTotalPoints();
     	}
     	
     	return total * students.size();
     }
     
     /**
      * Returns the total course average grade if the grade is weighted flag
      * is set to true
      * 
      * @return			the courses average grade.
      */
     private Double getCourseWeightedGrade() {
     	double weightedGrade = 0.0;
     	if (!verifyWeights()) return -1.0;
     	for (int i = 0; i < categories.size(); i++) {
     		weightedGrade += this.getAssignmentCategory(i).getAssignmentCategoryAverageGrade(students) * 
     						 (this.getAssignmentCategory(i).getGradingWeight() / 100); 
     	}
     	return weightedGrade;
     }
     private boolean verifyWeights() {
     	double total = 0.0;
     	for (int i = 0; i < categories.size(); i++) {
     		total += categories.get(i).getGradingWeight();
     	}
     	if (total != 100.0) return false;
     	return true;
     }
     
     /**
      * Get the section identification of the course
      * 
      * @return      section identification
      */
     public String getSection() {
         return section;
     }
     
     /**
      * Get the name of the building where the course is held
      * 
      * @return      building name
      */
     public String getBuilding() {
         return building;
     }
     
     /**
      * Get the room number of the course
      * 
      * @return      room number
      */
     public String getRoomID() {
         return roomID;
     }
     
     /**
      * Get meeting time of the course
      * 
      * @return      meeting time
      */
     public String getMeetingTime() {
         return meetingTime;
     }
     
     /**
      * Get meeting time of the course
      * 
      * @return      meeting time
      */
     public String getSemester() {
         return semester;
     }
     
     /**
      * Get index of last assignment edited
      * 
      * @return      last assignment index
      */
     public Integer getLastAssignmentIndex() {
         return lastAssignmentIndex;
     }
     
     /**
      * Get index of last category edited
      * 
      * @return      last category index
      */
     public Integer getLastCategoryIndex() {
         return lastCategoryIndex;
     }
     
     /**
      * Returns a string of all of the information about a course
      * 
      * @return        complete course overview of description, location, time
      */
     public String toString() {
         return (courseName + ": " + courseID + " " + courseNumber + "-" + section + ", " + building +
                 " " + roomID + ", " + meetingTime);
     }
     
     /**
      * Contstucts a new AssignmentCategory object and adds it into the categegories
      * ArrayList structure.
      * 
      * @param   ac  The name of the assignment category
      */
     public void addAssignmentCategory(String ac) {
         categories.add(new AssignmentCategory(ac));
     }
     
     /**
      * Returns the AssignmentCategory object from the index in the categories ArrayList
      * 
      * @return      the requested AssignmentCategory object
      */    
     public AssignmentCategory getAssignmentCategory(int index) {
         return categories.get(index);
     }
     
     /**
      * Returns the List of AssignmentCategory objects
      * 
      * @return      the requested AssignmentCategory List
      */    
     public List<AssignmentCategory> getCategories() {
         return categories;
     }
     
     /**
      * Returns the index of the AssignmentCategory in the categories arrayList
      * 
      * @param   name    the name of the AssignmentCategory
      * @return          the index of the AssignmentCategory object
      */
     public int getAssignmentCategoryIndex(String name) {
         //iterates through AssignmentCategor objects and performs name checking, 
         //returns index if successful else returns -1
         for (int i = 0; i < categories.size(); i++) {
             if (name.equals(categories.get(i).getName())) return i;
         }
         
         return -1;
     }
     
     /**
      * Gets the total number of Assignment Categories in the Course
      *
      * @return          the number of assignment categories
      */
     public int getNumberOfAssignmentCategories() {
     	return categories.size();
     }
     
     /**
      * Should always be used prior to adding a student to see if the name
      * is available so there are no duplicates. The teacher will have to
      * supply a slightly different name if they have two students with the
      * same names.
      * 
      * @param fn	The first name of the student
      * @param ln	The last name of the student
      * @return		Returns true if the name is available, else false
      */
     private boolean nameAvailable(String fn, String ln) {
     	for(Student x: students) {
     		if (fn.equals(x.getFirstName()) && ln.equals(x.getLastName())) {
     			return false;
     		}
     	}
     	return true;
     }
     
     /**
      * Removes the AssignmentCategory object from the categories ArrayList
      * 
      * @param   name    the string name of the object
      * @return          the AssignmentCategory object removed
      */
     public AssignmentCategory removeAssignmentCategory(String name) {
         try {
             return categories.remove(getAssignmentCategoryIndex(name));
         } catch (ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
     
     /**
      * Checks name availability, if name is available constructs a new 
      * Student object and adds it into the students ArrayList structure
      * and returns true. If the name is takene the function returns false.
      * 
      * @param   sn  students actual name
      * @param   pn  students pseudo-name
      */
     public boolean addStudent(String fn, String ln) {
     	if (!nameAvailable(fn, ln)) {
     		return false;
     	}
         students.add(new Student(fn, ln, pnGenerator.generateName()));
         
         Random generator = new Random();
        int ghostAmount = generator.nextInt(5) + 5; //Random number between 5 and 10
         
         for (int i = 0; i < ghostAmount; i++) {
         	addGhostStudent();
         }
         
         return true;
     }
     
     /**
      * Checks name availability, if name is available constructs a new 
      * Student object and adds it into the students ArrayList structure
      * and returns true. If the name is takene the function returns false.
      * Only takes input directly from XML.
      * 
      * @param   fn  students first name
      * @param	ln	students last name
      * @param   pn  students pseudo-name
      */
     public boolean addStudentXML(String fn, String ln, String pn) {
     	if (!nameAvailable(fn, ln)) {
     		return false;
     	}
         students.add(new Student(fn, ln, pn));
         return true;
     }
         
     /**
      * Returns the Student object at the specified index
      * 
      * @param   index   the index of a Student object
      * @return          the Student object
      */
     public Student getStudent(int index) {
         return students.get(index);
     }
 
     /**
      * Returns the List of Student objects
      * 
      * @return          the Student object
      */
     public List<Student> getStudents() {
         return students;
     }
     
     public List<Object> getAllStudents() {
     	return allStudents;
     }
     
     /**
      * Returns the index of the Student in the student arrayList
      * 
      * @param   name    the name of the student
      * @return          the index of the Student object
      */
     public int getStudentIndex(String name) {
         //iterates through Student objects and performs name checking, returns index if successful else returns -1
         for (int i = 0; i < students.size(); i++) {
             if (name.equals(students.get(i).getFullName())) return i;
         }
         
         return -1;
     }
     
     /**
      * Removes a Student object from the students ArrayList by a String name param by
      * getting their index and calling removeStudent using that index.
      */
     public void removeStudent(String name) {
     	removeStudent(getStudentIndex(name));
     }
     
     /**
      * Removes a Student object from the students arrayList by the index number
      * Archive's that students grades for the teacher's future records
      * Turns student and grades into a ghost student
      * 
      * @param   index   the integer index of the Student object in the students ArrayList
      */
     public void removeStudent(int index) {
     	Student currentStudent = null;
         try {
             currentStudent = students.remove(index);
         } catch (IndexOutOfBoundsException e) {
         	System.err.println("Caught IndexOutOfBoundsException: " + e.getMessage());
         	return;
         }
         currentStudent.archiveStudent(this);
         ghostStudents.add(new GhostStudent(currentStudent.getPseudoName()));
     }
     
     /**
      * Gets the total number of students in a course
      *
      * @return  number of students
      */
     public int getNumberOfStudents() {
     	return students.size();
     }
     
     /**
      * Constructs a new GhostStudent object and adds it into the fakeStudents ArrayList structure
      */
     public void addGhostStudent() {
         ghostStudents.add(new GhostStudent(pnGenerator.generateName()));
     }
     
     /**
      * Constructs a new GhostStudent object and adds it into the fakeStudents ArrayList structure
      * using a predefined psuedoName from the XML file
      * @param   pn  ghost students pseudo-name
      */
     public void addGhostStudentXML(String pn) {
         ghostStudents.add(new GhostStudent(pn));
     }
         
     /**
      * Returns the GhostStudent object at the specified index
      * 
      * @param   index   the index of a GhostStudent object
      * @return          the GhostStudent object
      */
     public GhostStudent getGhostStudent(int index) {
         return ghostStudents.get(index);
     }
     
     /**
      * Returns the index of the GhostStudent in the student arrayList
      * 
      * @param   name    the name of the ghost student
      * @return          the index of the GhostStudent object
      */
     public int getGhostStudentIndex(String name) {
         //iterates through GhostStudent objects and performs name checking, 
     	//returns index if successful else returns -1
         for (int i = 0; i < ghostStudents.size(); i++) {
             if (name.equals(ghostStudents.get(i).getPseudoName())) return i;
         }
         return -1;
     }
     
     /**
      * Removes a GhostStudent object from the students ArrayList
      * 
      * @param   name    the String name of the ghost student in the GhostStudent object
      * @return          the GhostStudent object removed, null on the object was not in the list
      */
     public GhostStudent removeGhostStudent(String name) {
         try {
             return ghostStudents.remove(getGhostStudentIndex(name));
         } catch (ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
     
     /**
      * Removes a GhostStudent object from the students arrayList by the index number
      * 
      * @param   index   the integer index of the GhostStudent object in the ghostStudents ArrayList
      * @return          the GhostStudent object removed, null if the index passed is out of bounds
      */
     public GhostStudent removeGhostStudent(int index) {
         try {
             return ghostStudents.remove(index);
         } catch (IndexOutOfBoundsException e) {
             return null;
         }
     }
     
     public int getNumberOfGhostStudents() {
    		return ghostStudents.size();
     }
     
     public int getTotalStudents() {
     	return ghostStudents.size() + students.size();
     }
     
     public void setIsGradingWeighted(boolean is) {
     	isGradingWeighted = is;
     }
     
     public void setGhostGrades() {
     	allStudents.removeAll(allStudents);
     	Collections.sort(students, Student.PseudoNameComparator);
     	Collections.sort(ghostStudents, GhostStudent.PseudoNameComparator);
     	allStudents.addAll(students);
     	
     	System.out.println("First");
     	
     	for (int j = 0; j < ghostStudents.size(); j++) {
     		for (int i = 0; i < allStudents.size(); i++) {
     			if (allStudents.get(i) instanceof Student) {
     				Student stud = (Student) allStudents.get(i);
     				if (ghostStudents.get(j).getPseudoName().compareTo(stud.getPseudoName()) < 0) {
     					allStudents.add(i, ghostStudents.get(j));
     			    	System.out.println("Second");
     			    	break;
     				}
     			}
     			else {
     				GhostStudent stud = (GhostStudent) allStudents.get(i);
     				if (ghostStudents.get(j).getPseudoName().compareTo(stud.getPseudoName()) < 0) {
     					allStudents.add(i, ghostStudents.get(j));
     			    	System.out.println("Third");
     			    	break;
     				}
     			}
     		}
     	}
     	
     	System.out.println("Fourth");
     	
     	String[] ghostNames = new String[getNumberOfGhostStudents()];
 		for (int k = 0; k < getNumberOfGhostStudents(); k++) {
 			ghostNames[k] = getGhostStudent(k).getPseudoName();
 		}
     	for (int i = 0; i < getCategories().size(); i++) {
     		for (int j = 0; j < getAssignmentCategory(i).getAssignments().size(); j++) {
     			getAssignmentCategory(i).getAssignment(j).setGhostGrades(ghostNames);
     		}
     	}
     }
     
     /**
      * if the course is created in the UI, then set to ture
      * @param newCourse set the true if the course is created in the UI
      */
     public void setNewCourse(boolean newCourse) {
         isNewCourse = newCourse;
     }
     
     /**
      * Check if the course is new
      * @return true if the course is new, otherwise false
      */
     public boolean isNewCourse() {
         return isNewCourse;
     }
 }
