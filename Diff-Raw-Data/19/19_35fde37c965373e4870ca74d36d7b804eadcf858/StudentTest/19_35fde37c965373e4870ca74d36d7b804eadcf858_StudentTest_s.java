 package edu.gatech.arktos;
 
 import edu.gatech.arktos.Student;
 import java.util.ArrayList;
 import junit.framework.TestCase;
 
 /* Test cases for Student class */
 public class StudentTest extends TestCase {
 
 	// Object to access Student class through
 	private Student sTest;
 	
 	public StudentTest(String name) {
 		super(name);
 	}
 
 	/* Setup environment - called before any test method */
 	protected void setUp() throws Exception {
 		super.setUp();
 		// Call constructor with values from spreadsheet
 		sTest = new Student("Josepha Jube", "901234502", "jj@gatech.edu", 80);
 	}
 
 	/* Release environment - called after any test method */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/* Test constructor to see if values are assigned correctly
 	 * This also provides coverage for all the initial getter functions
 	 */
 	public void testStudent() {
 		assertEquals("Josepha Jube", sTest.getName());
 		assertEquals("901234502", sTest.getGtid());
 		assertEquals("jj@gatech.edu", sTest.getEmail());
 		assertEquals(80, sTest.getAttendance());
 	}
 
 	/* Test initial setter functions */
 	public void testSetters() {
 		sTest.setName("Neil deGrasse Tyson");
 		sTest.setGtid("123456789");
 		sTest.setEmail("neil@tysontest.com");
 		sTest.setAttendance(95);
 		assertEquals("Neil deGrasse Tyson", sTest.getName());
 		assertEquals("123456789", sTest.getGtid());
 		assertEquals("neil@tysontest.com", sTest.getEmail());
 		assertEquals(95, sTest.getAttendance());
 	}
 
 	/* Test function that returns an ArrayList<Integer> of the student's assignment grades */
 	public void testGetAssignmentGrades() {
 		ArrayList<Integer> assignmentGrades = sTest.getAssignmentGrades();
		Integer[] grades = {100, 100, 71};
		assertEquals(grades[0], assignmentGrades.get(0));
		assertEquals(grades[1], assignmentGrades.get(1));
		assertEquals(grades[2], assignmentGrades.get(2));
 	}
 
 	/* Test get and set functions for assignments (ArrayList<Assignment>) */
 	public void testAssignments() {
 		// Test set function
 		ArrayList<Assignment> assignments1 = new ArrayList<Assignment>();
 		assignments1.add(new Assignment("Freddie Catlay", "WordCount in Java", 1, "100"));
 		assignments1.add(new Assignment("Freddie Catlay", "GroceryList App", 2, "95"));
 		assignments1.add(new Assignment("Freddie Catlay", "GroceryList Manager", 3, "75"));
 		sTest.setAssignments(assignments1);
 		// Test get function
 		ArrayList<Assignment> assignments2 = sTest.getAssignments();
 		for(Assignment a: assignments2) {
 			assertTrue(!a.getName().equals(null));
 		}
 	}
 
 	/* Test get and add functions for projects (ArrayList<ProjectTeam>) */
 	public void testProjects() {
 		// Create and populate sample ProjectTeam object
 		ArrayList<String> teamMembers = new ArrayList<String>();
 		teamMembers.add("Freddie Catlay");
 		teamMembers.add("Shevon Wise");
 		teamMembers.add("Kym Hiles");
 		teamMembers.add("Ernesta Anderson");
 		teamMembers.add("Sheree Gadow");
 		ProjectTeam project1 = new ProjectTeam(1, "Team 1", "Description", teamMembers);
 		// Test addProject function
 		sTest.addProjects(project1);
 		// Test getProject fiunction
 		ArrayList<ProjectTeam> project2 = sTest.getProjects();
 		for(ProjectTeam p: project2) {
 			assertTrue(!p.getTeamName().equals(null));
 		}
 	}
 
 	/* Test set and get functions for averageAssignmentGrade */
 	public void testAverageAssignmentGrade() {
 		// Test set function
 		sTest.setAverageAssignmentGrade(90);
 		// Test get function
 		assertEquals(90, sTest.getAverageAssignmentGrade());
 	}
 
 	/* Test function toString() */
 	public void testToString() {
 		assertEquals("Josepha Jube", sTest.toString());
 	}
 
 }
