 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class Course implements PrimaryKeyManager, Serializable, SortByName, Comparable<Course> {
 	private static final long serialVersionUID = 1L;
 	// Attributes
 	public static int pk; // To ensure id is unique.
 	private int id;
 	private String name;
 	private int AU;
 	private CourseType type;
 	private HashMap<String, Double> weights;
 	private CourseGroup groups;
 	private Professor coordinator;
 	private Semester semester;
 	
 	// Constructors
 	public Course(String name, CourseType type, Professor prof, int capacity) {
 		this.id = pk;
 		autoIncrement(this.id);
 		this.name = name;
 		this.AU = 1;
 		this.type = type;
 		this.weights = new HashMap<String, Double>();
 		this.coordinator = prof;
 		this.semester = null;
 		this.groups = new CourseGroup(this, capacity);
 	}
 	public Course(
 			String name, 
 			int AU, 
 			CourseType type, 
 			Professor prof, 
 			int capacity, 
 			HashMap<String, Double> weights) 
 	{
 		this.id = pk;
 		autoIncrement(this.id);
 		this.name = name;
 		this.AU = AU;
 		this.type = type;
 		this.weights = weights;
 		this.coordinator = prof;
 		this.semester = null;
 		this.groups = new CourseGroup(this, capacity);
 	}
 	public Course(Course course) { // Shallow copy purpose.
 		this.id = course.getID();
 		this.name = course.getName();
 		this.AU = course.getAU();
 		this.type = course.getType();
 		this.weights = course.getWeights();
 		this.coordinator = course.getCoordinator();
 		this.semester = course.getSemester();
 		this.groups = course.getGroups();
 	}
 	
 	// Getters and Setters
 	public int getID() { return id; }
 	public String getName() { return name; }
 	public int getAU() { return AU; }
 	public CourseType getType() { return this.type; }
 	public HashMap<String, Double> getWeights() { return this.weights; }
 	public CourseGroup getGroups() { return this.groups; }
 	public Professor getCoordinator() { return this.coordinator; }
 	public Semester getSemester() { return this.semester; }
 	
 	public void setID(int id) { this.id = id; }
 	public void setName(String name) { this.name = name; }
 	public void setAU(int number) { this.AU = number; }
 	public void setType(CourseType type) { this.type = type; groups.resetGroups(type); }
 	public void setWeights(HashMap<String, Double> weights) { this.weights = weights; }
 	public void setGroups(CourseGroup groups) { this.groups = groups; }
 	public void setCoordinator(Professor prof) { this.coordinator = prof; }
 	public void setSemester(Semester sem) { this.semester = sem; }
 	
 	// Specific methods
 	public void printResults() {
 		System.out.println(printResults(""));
 	}
 	public String printResults(String tabs) {
 		String msg = new String();
 		Grade mark = null;
 		double marks = 0.0;
 		HashMap<String, Double> components = new HashMap<String,Double>();
 		int studentCount = 0;
 		ArrayList<Student> students = new ArrayList<Student>(getGroups().getStudents());
 		Collections.sort(students);
 		msg += String.format("%sCourse Statistics:\n", tabs);
 		msg += String.format("%s\tStudents: \n", tabs);
 		for (Student student : students) {
 			msg += String.format("%s\t%d, %s: \n", tabs, student.getID(), student.getName());
 			try {
 				mark = student.getGrade(this);
 				marks += mark.getOverall();
 				for (String type : mark.getComponents()) {
 					if (components.containsKey(type)) {
 						components.put(type, components.get(type)+mark.getMark(type));
 					}
 					else components.put(type, mark.getMark(type));
 				}
 				msg += mark.printMarks(tabs+"\t\t");
 				studentCount++;
 			} catch (KeyErrorException e){
 				msg += String.format("%s\t\t%s\n", tabs, e.getMessage());
 			}
  		}
 		msg += String.format("%s\tMean Overall: %.2f / %d = %.2f", tabs, marks, studentCount, marks/studentCount);
 		for (String type : components.keySet()) {
 			msg += String.format("%s\tMean %s: %.2f / %d = %.2f", tabs, type, components.get(type), studentCount, components.get(type)/studentCount);
 		}
 		return msg+'\n';
 	}
 	public String print(String tabs) {
 		String msg = new String();
 		double total = 0;
 		msg += String.format("%sCourse ID: %d\n", tabs, getID());
 		msg += String.format("%sCourse Name: %s\n", tabs, getName());
 		msg += String.format("%sCourse AU: &d\n", tabs, getAU());
 		msg += String.format("%sCourse Type: &d\n", tabs, getType().getDescription());
 		msg += String.format("%sCourse Capacity: %d\n", tabs, getGroups().getCapacity());
 		msg += String.format("%sCourse Weights: \n", tabs);
 		for (String type : getWeights().keySet()) {
 			msg += String.format("%s\t%s: &.2f\n", tabs, type, getWeights().get(type));
 			total += getWeights().get(type);
 		}
 		msg += String.format("%s\tTotal Weights: &.2f\n", tabs, total);
 		msg += String.format("%sCourse Groups: \n", tabs);
 		msg += String.format("%s\t%s: \n", tabs, getGroups().print(tabs+'\t'));
 		msg += String.format("%sCourse Coordinator: %d, %s\n", tabs, getCoordinator().getID(), getCoordinator().getName());
 		return msg;
 	}
 	
 	@Override
 	public void autoIncrement(int id) {
 		if (id < pk) return;
 		pk = ++id;
 	}
 	@Override
 	public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
 		stream.defaultReadObject();
 		autoIncrement(this.id);
 	}
 	@Override
 	public int compareTo(Course obj) {
 		return this.getID() - obj.getID();
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Course)) return false;
 		Course course = (Course) obj;
 		if (course.getID() == this.id) return true;
 		if (course.getName().equals(this.getName())) return true;
 		return false;
 	}
 	
 	public static void main(School school, Scanner scan) {
 		int choice = 0;
 		char ch = 0;
 		boolean done = false;
 		String stringInput = null;
 		Course course = null;
 		ArrayList<Course> courseList = null;
 		
 		while (!done) {
 			System.out.println("Welcome to Course Manager.");
 			System.out.println("\t1. Print courses list by ID.");
 			System.out.println("\t2. Print courses list by Name.");
 			System.out.println("\t3. Create a new course.");
 			System.out.println("\t4. Edit a course.");
 			System.out.println("\t5. Delete a course.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Please choose a choice: ");
 			choice = scan.nextInt(); scan.nextLine();
 			
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 				case 2:
 					courseList = new ArrayList<Course>(school.getCourses().values());
 					if (choice == 1) Collections.sort(courseList);
 					else Collections.sort(courseList, new SortByNameComparator());
 					System.out.println("Courses List:");
					System.out.printf("%-5s | %-10s | %-60s\n", "NO", "COURSE NAME", "COURSE ID");
 					System.out.println(new String(new char[81]).replace('\0', '-'));
 					for (Course cs : courseList) {
 						System.out.printf("%-5d | %-10d | %-60s\n", courseList.indexOf(cs)+1, cs.getID(), cs.getName());
 					}
 					break;
 				case 3:
 					course = null;
 					System.out.print("Please input course name: ");
 					String name = scan.nextLine();
 					System.out.print("Please input academic units: ");
 					int au = scan.nextInt(); scan.nextLine();
 					
 					// Setting CourseType
 					CourseType[] courseTypes = CourseType.values();
 					System.out.println("Please choose the following Course Types: ");
 					for (int i=0; i<courseTypes.length; i++)
 						System.out.printf("\t%d. %s\n", i+1, courseTypes[i].getDescription());
 					choice = scan.nextInt(); scan.nextLine();
 					CourseType courseType;
 					try {
 						courseType = courseTypes[choice-1];
 					} catch (IndexOutOfBoundsException e) {
 						System.out.println("Error: Invalid choice.");
 						System.out.println("Error: Creation of course failed.");
 						break;
 					}
 					
 					// Setting Professor as course coordinator
 					System.out.print("Please input ID of professor as Course Coordinator: ");
 					choice = scan.nextInt(); scan.nextLine();
 					Professor professor;
 					try {
 						professor = school.getProfessor(choice);
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 						System.out.println("Error: Creation of course failed.");
 						break;
 					}
 					
 					// Weight allocation for course assessments
 					HashMap<String, Double> weights =  new HashMap<String, Double>();
 					while (ch != 'Y' && ch != 'N') {
 						System.out.print("Does this course constitutes a final exam? (Y/N): ");
 						ch = scan.nextLine().toUpperCase().charAt(0);
 						if (ch == 'Y') weights.put("Final Exam", 100.0);
 					}
 					
 					// Capacity
 					System.out.print("Please input course capacity: ");
 					int capacity = scan.nextInt(); scan.nextLine();
 					
 					course = new Course(name, au, courseType, professor, capacity, weights);
 					try {
 						school.addCourse(course);
 						professor.addCourse(course);
 					} catch (DuplicateKeyException e) {
 						System.out.println(e.getMessage());
 						try {
 							school.rmCourse(course);
 						} catch (KeyErrorException f) {
 							// pass
 						}
 					}
 					break;
 					
 				case 4:
 				case 5:
 					course = null;
 					System.out.print("Please input Course ID / Name: ");
 					stringInput = scan.nextLine();
 					try {
 						int courseID = Integer.parseInt(stringInput);
 						if (choice == 4) Course.main(school.getCourse(courseID), school, scan);
 						else {
 							course = school.getCourse(courseID);
 							school.rmCourse(course);
 							course.getCoordinator().rmCourse(course);
 							System.out.printf("Course %d, %s removed.\n", course.getID(), course.getName());
 						}
 					} catch (NumberFormatException e) {
 						for (Course cs : school.getCourses().values()) {
 							if (cs.getName() == stringInput) {
 								course = cs;
 								if (choice == 4) Course.main(course, school, scan);
 								else {
 									try {
 										school.rmCourse(course);
 										course.getCoordinator().rmCourse(course);
 									} catch (KeyErrorException f) {
 										//pass
 									}
 									System.out.printf("Course %d, %s removed.\n", course.getID(), course.getName());
 								}
 								break;
 							}
 						}
 						if (course == null) System.out.printf("Error: Course %s does not exists.\n", stringInput);
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					}
 				default:
 					System.out.println("Error: Invalid choice.");
 					break;
 			}
 		}
 	}
 	
 	public static void main(Course course, School school, Scanner scan) {
 		// Declarations
 		int choice = 0;
 		boolean done = false;
 		String stringInput = null;
 		
 		while (!done) {
 			System.out.printf("Course: %d, %s\n", course.getID(), course.getName());
 			System.out.println(course.print("\t"));
 			System.out.println("\t1. Edit Course Name.");
 			System.out.println("\t2. Edit Course AU.");
 			System.out.println("\t3. Edit Course Type.");
 			System.out.println("\t4. Edit Course Capacity.");
 			System.out.println("\t5. Edit Course Assessments Weights.");
 			System.out.println("\t6. Edit Course Groups.");
 			System.out.println("\t7. Edit Course Coordinator.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Please choose an option: ");
 			choice = scan.nextInt(); scan.nextLine();
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 					System.out.print("Please input new course name: ");
 					stringInput = scan.nextLine();
 					course.setName(stringInput);
 					break;
 				case 2:
 					System.out.print("Please input academic units: ");
 					choice = scan.nextInt(); scan.nextLine();
 					course.setAU(choice);
 					break;
 				case 3:
 					CourseType[] courseTypes = CourseType.values();
 					System.out.println("Please choose the following Course Types: ");
 					for (int i=0; i<courseTypes.length; i++)
 						System.out.printf("\t%d. %s\n", i+1, courseTypes[i].getDescription());
 					choice = scan.nextInt(); scan.nextLine();
 					try {
 						course.setType(courseTypes[choice-1]);
 					} catch (IndexOutOfBoundsException e) {
 						System.out.println("Error: Invalid choice.");
 					}
 					break;
 				case 4:
 					System.out.print("Please input new course capacity: ");
 					choice = scan.nextInt(); scan.nextLine();
 					try {
 						course.getGroups().setCapacity(choice);
 					} catch (MaxCapacityException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				case 5:
 					System.out.println("Please choose the following: ");
 					System.out.println("\t1. Add assessment.");
 					System.out.println("\t2. Edit assessment.");
 					System.out.println("\t3. Delete assessment.");
 					System.out.print("Choice: ");
 					choice = scan.nextInt(); scan.nextLine();
 					switch(choice) {
 						case 1:
 						case 2:
 							System.out.print("Please input name of assessment: ");
 							stringInput = scan.nextLine();
 							System.out.print("Please input name of assessment: ");
 							double weight = scan.nextDouble(); scan.nextLine();
 							course.getWeights().put(stringInput, weight);
 							break;
 						case 3:
 							System.out.print("Please input name of assessment: ");
 							stringInput = scan.nextLine();
 							course.getWeights().remove(stringInput);
 							break;
 					}
 					break;
 				case 6:
 					CourseGroup.main(course.getGroups(), school, scan);
 					break;
 				case 7:
 					System.out.print("Please input new Professor ID: ");
 					choice = scan.nextInt(); scan.nextLine();
 					try {
 						course.setCoordinator(school.getProfessor(choice));
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					}
 					break;
 				default:
 					System.out.println("Error: Invalid choice.");
 					break;
 			}
 		}
 	}
 }
