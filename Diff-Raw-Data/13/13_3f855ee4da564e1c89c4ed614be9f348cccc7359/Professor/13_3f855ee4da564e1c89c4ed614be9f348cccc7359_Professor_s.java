 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class Professor extends Person implements PrimaryKeyManager{
 	private static final long serialVersionUID = 1L;
 	// Attributes
 	public static int pk = 1;
 	private int pid;
 	private String pmail;
 	private String office;
 	private String officeContact;
 	private School school;
 	private String position;
 	private HashMap<Integer, Course> courses; // Coordinator of courses
 	private HashMap<Integer, Group> classes; // Tutor or lecturer of classes.
 	
 	// Constructors
 	public Professor(String name, Gender gender, School school) {
 		super(name, gender);
 		this.pid = pk;
 		autoIncrement(this.pid);
 		this.pmail = null;
 		this.office = null;
 		this.officeContact = null;
 		this.school = school;
 		this.position = null;
 		this.courses = new HashMap<Integer, Course>();
 		this.classes = new HashMap<Integer, Group>();
 	}
 	public Professor(int id, String name, Gender gender, School school) {
 		super(name, gender);
 		this.pid = id;
 		autoIncrement(this.pid);
 		this.pmail = null;
 		this.office = null;
 		this.officeContact = null;
 		this.school = school;
 		this.position = null;
 		this.courses = new HashMap<Integer, Course>();
 		this.classes = new HashMap<Integer, Group>();
 	}
 	
 	// Getters and Setters
 	@Override
 	public int getID() { return pid; }
 	public String getPmail() { return pmail; }
 	public String getOffice() { return office; }
 	public String getOfficeContact() { return officeContact; }
 	public School getSchool() { return school; }
 	public String getPosition() { return position; }
 	public HashMap<Integer, Course> getCourses() { return courses; }
 	public HashMap<Integer, Group> getClasses() { return classes; }
 	
 	@Override
 	public void setID(int id) { this.pid = id; }
 	public void setPmail(String email) { this.pmail = email; }
 	public void setOffice(String address) { this.office = address; }
 	public void setOfficeContact(String contact) { this.officeContact = contact; }
 	public void setSchool(School school) { this.school = school; }
 	public void setPosition(String position) { this.position = position; }
 	public void setCourses(HashMap<Integer, Course> courses) { this.courses = courses; }
 	public void setClasses(HashMap<Integer, Group> classes) { this.classes = classes; }
 	
 	// Specific methods
 	public void addCourse(Course course) throws DuplicateKeyException {
 		if (courses.containsKey(course.getID())) throw new DuplicateKeyException();
 		else courses.put(course.getID(), course);
 	}
 	public void addClass(Group group) throws DuplicateKeyException {
 		if (classes.containsKey(group.getID())) throw new DuplicateKeyException();
 		else classes.put(group.getID(), group);
 	}
 	public void rmCourse(Course course) throws KeyErrorException {
 		if (courses.containsKey(course.getID())) courses.remove(course.getID());
 		else throw new KeyErrorException();
 	}
 	public void rmClass(Group group) throws KeyErrorException {
 		if (classes.containsKey(group.getID())) classes.remove(group.getID());
 		else throw new KeyErrorException();
 	}
 	
 	public void printParticulars() {
 		System.out.println(printParticulars(""));
 	}
 	public String printParticulars(String tabs) {
 		String msg = new String();
 		msg += String.format("%sGeneral Particulars: \n", tabs);
 		msg += printPersonParticulars(tabs+"\t");
 		msg += String.format("%sProfessor Particulars: \n", tabs);
 		msg += printProfessorParticulars(tabs+"\t");
 		return msg;
 	}
 	public String printProfessorParticulars(String tabs) {
 		String msg = new String();
 		msg += String.format("%sProfessor Particulars: \n", tabs);
 		msg += String.format("%sProfessor Mail: %s\n", tabs, getPmail());
 		msg += String.format("%sSchool: %s\n", tabs, getSchool().getName());
 		msg += String.format("%sOffice Location: %s\n", tabs, getOffice());
 		msg += String.format("%sOffice Contact: %s\n", tabs, getOfficeContact());
 		msg += String.format("%sPosition: %s\n", tabs, getPosition());
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
 		autoIncrement(this.getID());
 	}
 	
 	public static void main(School school, Scanner scan) {
 		// Declarations
 		int choice = 0;
 		int profID = 0;
 		boolean done = false;
 		char gender;
 		String stringInput = null;
 		Professor prof = null;
 		ArrayList<Professor> profList = null;
 		
 		while(!done) {
 			System.out.println("Welcome to Professor Manager.");
 			System.out.println("\t1. Print professors list by ID.");
 			System.out.println("\t2. Print professors list by Name.");
 			System.out.println("\t3. Create a new professor.");
 			System.out.println("\t4. Edit a professor.");
 			System.out.println("\t5. Delete a professor.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Please choose a choice: ");
 			choice = scan.nextInt(); scan.nextLine();
 			
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 				case 2:
 					profList = new ArrayList<Professor>(school.getProfessors().values());
 					if (choice == 1) Collections.sort(profList);
 					else Collections.sort(profList, new SortByNameComparator());
 					System.out.println("Professors List:");
 					System.out.printf("%-5s | %-12s | %-60s\n", "NO", "PROFESSOR ID", "NAME");
 					System.out.println(new String(new char[83]).replace('\0', '-'));
 					for (Professor professor : profList) {
 						System.out.printf("%-5d | %-10d | %-60s\n", profList.indexOf(professor)+1, professor.getID(), professor.getName());
 					}
 					break;
 				case 3:
 					prof = null;
 					System.out.print("Please input professor name: ");
 					stringInput = scan.nextLine();
 					System.out.print("Please choose gender (M/F): ");
 					gender = scan.nextLine().charAt(0);
 					switch (gender) {
 						case 'M':
 						case 'm':
 							prof = new Professor(stringInput, Gender.MALE, school);
 							break;
 						case 'F':
 						case 'f':
 							prof = new Professor(stringInput, Gender.FEMALE, school);
 							break;
 						default:
 							System.out.println("Error: Invalid selection.");
 							break;
 					}
 					if (prof != null) {
 						try {
 							school.addProfessor(prof);
 						} catch (DuplicateKeyException e) {
 							System.out.println(e.getMessage());
 						}
 					} else System.out.println("Error: Professor not created.");
 					break;
 				case 4:
 				case 5:
 					prof = null;
 					System.out.print("Please input Professor ID / Name: ");
 					stringInput = scan.nextLine();
 					try {
 						profID = Integer.parseInt(stringInput);
 						if (choice == 4) Professor.main(school.getProfessor(profID), scan);
 						else {
 							prof = school.getProfessor(profID);
 							school.rmProfessor(prof);
 							System.out.printf("Professor %d, %s removed.\n", prof.getID(), prof.getName());
 						}
 					} catch (NumberFormatException e) {
 						for (Professor professor : school.getProfessors().values()) {
 							if (professor.getName().equals(stringInput)) {
 								prof = professor;
 								if (choice == 4) Professor.main(professor, scan);
 								else {
 									try {
 										school.rmProfessor(professor);
 									} catch (KeyErrorException f) {
 										//pass
 									}
 									System.out.printf("Professor %d, %s removed.\n", professor.getID(), professor.getName());
 								}
 								break;
 							}
 						}
 						if (prof == null) System.out.printf("Error: Professor %s does not exists.\n", stringInput);
 					} catch (KeyErrorException e) {
 						System.out.println(e.getMessage());
 					}
 				default:
 					System.out.println("Error: Invalid choice.");
 					break;
 			}
 		}
 	}
 	public static void main(Professor prof, Scanner scan) {
 		int choice = 0;
 		boolean done = false;
 		String stringInput = null;
 		ArrayList<School> schoolList = null;
 		
 		while (!done) {
 			System.out.printf("Professor: %d, %s\n", prof.getID(), prof.getName());
 			System.out.println(prof.printParticulars("\t"));
 			System.out.println("\t1. Edit General Particulars.");
 			System.out.println("\t2. Edit Professor Particulars.");
 			System.out.println("\t0. Go back to previous menu.");
 			System.out.print("Please choose an option: ");
 			choice = scan.nextInt(); scan.nextLine();
 			switch (choice) {
 				case 0:
 					done = true;
 					break;
 				case 1:
 					Person.main(prof, scan);
 					break;
 				case 2:
 					while (!done) {
 						System.out.printf("Professor: %d, %s\n", prof.getID(), prof.getName());
 						System.out.println(prof.printProfessorParticulars("\t"));
 						System.out.println("\t1. Edit Professor eMail.");
 						System.out.println("\t2. Edit School.");
 						System.out.println("\t3. Edit Office Location.");
 						System.out.println("\t4. Edit Office Contact.");
 						System.out.println("\t5. Edit Position.");
 						System.out.println("\t0. Go back to previous menu.");
 						System.out.print("Please choose an option: ");
 						choice = scan.nextInt(); scan.nextLine();
 						
 						switch (choice) {
 							case 0:
 								done = true;
 								break;
 							case 1:
 								System.out.print("Please input professor eMail: ");
 								stringInput = scan.nextLine();
 								prof.setPmail(stringInput);
 								break;
 							case 2:
 								System.out.println("Please choose the following schools: ");
 								schoolList = new ArrayList<School>(SCRAME.data.getSchools());
 								for (School sch : schoolList) {
 									System.out.printf("\t%d. %s\n", (schoolList.indexOf(sch))+1, sch.getName());
 								}
 								System.out.print("Choice: ");
 								choice = scan.nextInt(); scan.nextLine();
 								try {
 									schoolList.get(choice-1).addProfessor(prof);
 									prof.getSchool().rmProfessor(prof);
 									prof.setSchool(schoolList.get(choice-1));
 								} catch (IndexOutOfBoundsException e) {
 									System.out.println("Error: Invalid choice.");
 								} catch (DuplicateKeyException f) {
 									System.out.println(f.getMessage());
 								} catch (KeyErrorException g) {
 									System.out.println(g.getMessage());
 								}
 								break;
 							case 3:
 								System.out.print("Please input office location: ");
 								stringInput = scan.nextLine();
 								prof.setOffice(stringInput);
 								break;
 							case 4:
 								System.out.print("Please input office contact: ");
 								stringInput = scan.nextLine();
 								prof.setOfficeContact(stringInput);
 								break;
 							case 5:
 								System.out.print("Please input position: ");
 								stringInput = scan.nextLine();
 								prof.setPosition(stringInput);
 								break;
 							default:
 								System.out.println("Error: Invalid choice.");
 								break;
 						}
						done = false;
 					}
 				default:
 					System.out.println("Error: Invalid choice.");
 					break;
 			}
 		}
 	}
 }
