 /*
  * *********************************************************
  * Copyright (c) 2012 - 2012, DHBW Mannheim
  * Project: SoS
  * Date: Apr 14, 2012
  * Author(s): DirkK
  * 
  * *********************************************************
  */
 package edu.dhbw.sos.course;
 
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import edu.dhbw.sos.course.influence.EInfluenceType;
 import edu.dhbw.sos.course.influence.Influence;
 import edu.dhbw.sos.course.lecture.BlockType;
 import edu.dhbw.sos.course.lecture.Lecture;
 import edu.dhbw.sos.course.student.EmptyPlace;
 import edu.dhbw.sos.course.student.IPlace;
 import edu.dhbw.sos.course.student.Student;
 import edu.dhbw.sos.helper.CalcVector;
 import edu.dhbw.sos.simulation.SimController;
 
 
 /**
  * This class is a data holder for the data belonging to the course
  * 
  * @author DirkK
  */
 public class Course {
 	private static final Logger						logger						= Logger.getLogger(Course.class);
 	private LinkedList<IStudentsObserver>			studentsObservers			= new LinkedList<IStudentsObserver>();
 	private LinkedList<ISelectedCourseObserver>	selectedCourseObservers	= new LinkedList<ISelectedCourseObserver>();
 	private IPlace[][]									students						= new IPlace[0][0];
 	private Influence										influence					= new Influence();
 	private String											name							= "";
 	private Lecture										lecture;
 	private SimController								simController;
 	
 	private LinkedList<String>							properties					= new LinkedList<String>();
 	// place here? not implemented yet, so do not know...
 	private LinkedHashMap<String, String>			statistics					= new LinkedHashMap<String, String>();
 	private LinkedList<String>							suggestions					= new LinkedList<String>();
 	
 	// the student and property that was selected in the GUI (by hovering over the student)
 	private IPlace											selectedStudent			= null;
 	private int												selectedProperty			= 0;
 	
 	
 	public Course(String name) {
 		this.name = name;
 		simController = new SimController(this);
 		lecture = new Lecture(new Date());
 		
 		// some dummy data
 		for (int i = 0; i < 5; i++) {
 			statistics.put("Test" + i, "" + i * 42);
 		}
 		
 		suggestions.add("Sug1");
 		suggestions.add("Sug2");
 		suggestions.add("Sug3");
 		suggestions.add("Sug4");
 	}
 	
 	
 	/**
 	 * notify all subscribers of the students array
 	 */
 	public void notifyStudentsObservers() {
 		for (IStudentsObserver so : studentsObservers) {
 			so.updateStudents();
 		}
 	}
 	
 	
 	/**
 	 * notify all subscribers of the students array
 	 */
 	public void notifySelectedCourseObservers() {
 		for (ISelectedCourseObserver so : selectedCourseObservers) {
 			so.updateSelectedCourse();
 		}
 	}
 	
 	
 	/**
 	 * 
 	 * objects interested in the students field can subscribe here
 	 * the object will be notified if the field changes
 	 * 
 	 * @param so the object which needs to be informed
 	 * @author dirk
 	 */
 	public void subscribeStudents(IStudentsObserver so) {
 		studentsObservers.add(so);
 	}
 	
 	
 	public void subscribeSelectedCourse(ISelectedCourseObserver so) {
 		selectedCourseObservers.add(so);
 	}
 	
 	
 	/**
 	 * 
 	 * Gets a certain student-instance based on x- and y-coordinates
 	 * 
 	 * @param row
 	 * @param column
 	 * @return
 	 * @author SebastianN
 	 */
 	public IPlace getPlace(int row, int column) {
 		IPlace[][] students = this.getStudents();
 		if (students != null) {
 			return students[row][column];
 		}
 		throw new IllegalStateException();
 	}
 	
 	
 	/**
 	 * 
 	 * Assigns a student-instance to a place (based on row/column)
 	 * 
 	 * @param row
 	 * @param column
 	 * @param student
 	 * @return TRUE if assignment was successful, otherwise FALSE
 	 * @author SebastianN
 	 */
 	public boolean setPlace(int row, int column, IPlace student) {
 		IPlace[][] students = this.getStudents();
 		if (students != null) {
 			students[row][column] = student;
 			return true;
 		}
 		throw new IllegalStateException();
 	}
 	
 	
 	/**
 	 * 
 	 * @brief Checks whether a place is empty of not - based on x,y
 	 * 
 	 * @param row
 	 * @param column
 	 * @return TRUE if student is an instance of "EmptyPlace", otherwise FALSE.
 	 * @author SebastianN
 	 */
 	public boolean IsPlaceEmpty(int row, int column) {
 		IPlace[][] students = this.getStudents();
 		if (students != null) {
 			if (students[row][column] instanceof EmptyPlace) {
 				return true;
 			}
 			return false;
 		}
 		throw new IllegalStateException();
 	}
 	
 	
 	/**
 	 * calculates the next step of the simulation
 	 * calculate for every student the next state
 	 * 
 	 * @param currentTime current time of the lecture
 	 * @param speed speed of simulation
 	 * @author dirk
 	 */
 	public void simulationStep(int currentTime, int speed) {
 		
 		students[0][0].printAcutalState();
 		
 		// -------------------------------------------------
 		// -------------- pre conditions -------------------
 		// -------------------------------------------------
 		
 		// the new array for the calculated students, fill it with EmptyPalce
 		IPlace[][] newState = new IPlace[students.length][students[0].length];
 		for (int y = 0; y < 5; y++) {
 			for (int x = 0; x < 7; x++) {
 				newState[y][x] = new EmptyPlace(properties.size());
 			}
 		}
 		
 		// -------------------------------------------------
 		// -------- student independent calculations -------
 		// -------------------------------------------------
 		
 		CalcVector preChangeVector = new CalcVector(properties.size());
 		preChangeVector.printCalcVector("Init");
 		
 		// breakReaction ( inf(Break) * breakInf )
 		double breakInf = 0.01;
 		if (lecture.getTimeBlocks().getTimeBlockAtTime(currentTime / 60000).getType() == BlockType.pause) {
 			logger.info("Influenced by break");
 			preChangeVector.addCalcVector(influence.getEnvironmentVector(EInfluenceType.BREAK_REACTION, breakInf));
 		}
 		preChangeVector.printCalcVector("after break");
 		
 		// timeDending ( inf(Time) * currentTime/1000 * timeInf )
 		double timeInf = 0.001;
 		double timeTimeInf = timeInf * currentTime / 1000;
 		preChangeVector.addCalcVector(influence.getEnvironmentVector(EInfluenceType.TIME_DEPENDING, timeTimeInf));
 		preChangeVector.printCalcVector("after time depending");
 		
 		// -------------------------------------------------
 		// ---------- iterate over all students ------------
 		// -------------------------------------------------
 		for (int y = 0; y < students.length; y++) {
 			for (int x = 0; x < students[y].length; x++) {
 				if (students[y][x] instanceof Student) {
 					Student student = (Student) students[y][x];
 					// check if there was an interaction from the don
 					Entry<Integer, Student> donInteraction = student.historyDonInputInInterval(currentTime - speed,
 							currentTime);
 					if (donInteraction != null) {
 						student = donInteraction.getValue();
 					}
 					
 					// influence of the surrounding students
 					CalcVector neighborInfl = getNeighborsInfluence(student, x, y);
 					// output for one student (0,0) -> only for analyzing the simulation behavior
 					if (y == 0 && x == 0)
 						neighborInfl.printCalcVector("Neighbor");
 					
 					// create a new vector which contains the pre calculates vector and the neighbor vector
 					CalcVector preChangeVectorSpecial = neighborInfl.addCalcVector(preChangeVector).addCalcVector(
 							neighborInfl);
 					// output for one student (0,0) -> only for analyzing the simulation behavior
 					if (y == 0 && x == 0)
 						neighborInfl.printCalcVector("preChangeVectorSpecial = Neighbor + preChangeVector");
 					
 					// create a new student and let him calculate a new change vector
 					newState[y][x] = student.clone();
					((Student) newState[y][x]).calcNextSimulationStep(preChangeVectorSpecial, influence, x, y, currentTime);
 					if (y == 0 && x == 0)
 						((Student) newState[y][x]).printAcutalState();
 					((Student) newState[y][x]).saveHistoryStates(currentTime);
 				}
 			}
 		}
 		
 		// -------------------------------------------------
 		// -------------- post simulation ------------------
 		// -------------------------------------------------
 		
 		// give the reference from newState to real students array
 		students = newState;
 		
 		// notify all subscribers of the students array
 		notifyStudentsObservers();
 	}
 	
 	
 	/**
 	 * calculates the changeVector concerning the neighbors for one student
 	 * @param student
 	 * @param x position of the student
 	 * @param y position of the student
 	 * @return changeVector
 	 * @author dirk
 	 */
 	private CalcVector getNeighborsInfluence(Student student, int x, int y) {
 		// neighbor ( inf(Neighbor) * state(studentLeft) * neighbor + inf(Neighbor) * state(studentRight) * ... )
 		
 		// x x x factor for each relative position to the student
 		// x o x
 		// x x x
 		CalcVector changeVector = new CalcVector(student.getActualState().size());
		double[][] neighborInf = { { 0.0001, 0.0001, 0.0001 }, { 0.0010, 0.00, 0.0010 }, { 0.000010, 0.000010, 0.000010 } };// new double[3][3];
 		// for (int i = 0; i < 3; i++) {
 		// for (int j = 0; j < 3; j++) {
 		// neighborInf[j][i] = 0.01;
 		// }
 		// }
		if(x==0 && y==0)
 		influence.getEnvironmentVector(EInfluenceType.NEIGHBOR, neighborInf[0][0])
 				.printCalcVector("Influence neighbor: ");
 		
 		// System.out.println("x: "+x+" / y: "+y);
 		for (int i = -1; i <= 1; i++) {
 			for (int j = -1; j <= 1; j++) {
 				if (i != 0 || j != 0) {
 					int newx = x + i;
 					int newy = y + j;
 					if (newx < students[0].length && newx >= 0 && newy < students.length && newy >= 0) {
 						IPlace s = students[newy][newx];
 						CalcVector studentsState = s.getActualState();
 						changeVector.addCalcVector(influence.getEnvironmentVector(EInfluenceType.NEIGHBOR,
 								neighborInf[j + 1][i + 1]).multiplyWithVector(studentsState));
 						
 					}
 				}
 			}
 		}
 		return changeVector;
 	}
 	
 	
 	/**
 	 * print statistical information concerning the field
 	 * @author dirk
 	 */
 	public void printFieldInformation() {
 		int isnull = 0;
 		int studentsA = 0;
 		int empty = 0;
 		for (int a = 0; a < 5; a++) {
 			for (int b = 0; b < 7; b++) {
 				if (students[a][b] == null) {
 					isnull++;
 					System.out.println(a + " / " + b);
 				} else if (students[a][b] instanceof EmptyPlace)
 					empty++;
 				else if (students[a][b] instanceof Student)
 					studentsA++;
 			}
 		}
 		System.out.println("null: " + isnull);
 		System.out.println("empty: " + empty);
 		System.out.println("studentsA: " + studentsA);
 	}
 	
 	
 	// --- GETTERS and SETTERS ---
 	
 	public IPlace[][] getStudents() {
 		return students;
 	}
 	
 	
 	public void setStudents(IPlace[][] students) {
 		this.students = students;
 	}
 	
 	
 	public Influence getInfluence() {
 		return influence;
 	}
 	
 	
 	public void setInfluence(Influence influence) {
 		this.influence = influence;
 	}
 	
 	
 	public Lecture getLecture() {
 		return lecture;
 	}
 	
 	
 	public void setLecture(Lecture lecture) {
 		this.lecture = lecture;
 	}
 	
 	
 	public LinkedList<String> getProperties() {
 		return properties;
 	}
 	
 	
 	public void setProperties(LinkedList<String> properties) {
 		this.properties = properties;
 	}
 	
 	
 	public String getName() {
 		return name;
 	}
 	
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	
 	public SimController getSimController() {
 		return simController;
 	}
 	
 	
 	public String toString() {
 		return getName();
 	}
 	
 	
 	public LinkedHashMap<String, String> getStatistics() {
 		return statistics;
 	}
 	
 	
 	public void setStatistics(LinkedHashMap<String, String> statistics) {
 		this.statistics = statistics;
 	}
 	
 	
 	public LinkedList<String> getSuggestions() {
 		return suggestions;
 	}
 	
 	
 	public void setSuggestions(LinkedList<String> suggestions) {
 		this.suggestions = suggestions;
 	}
 	
 	
 	public IPlace getSelectedStudent() {
 		return selectedStudent;
 	}
 	
 	
 	public void setSelectedStudent(IPlace selectedStudent) {
 		this.selectedStudent = selectedStudent;
 		notifySelectedCourseObservers();
 	}
 	
 	
 	public int getSelectedProperty() {
 		return selectedProperty;
 	}
 	
 	
 	public void setSelectedProperty(int selectedProperty) {
 		this.selectedProperty = selectedProperty;
 	}
 }
