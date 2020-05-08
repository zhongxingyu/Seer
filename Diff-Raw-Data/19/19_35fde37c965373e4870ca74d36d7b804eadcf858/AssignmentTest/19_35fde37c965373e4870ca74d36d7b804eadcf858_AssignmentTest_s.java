 package edu.gatech.arktos;
 
 import edu.gatech.arktos.Assignment;
 import junit.framework.TestCase;
 
 /* Test cases for Assignment class */
 public class AssignmentTest extends TestCase {
 
 	// Object to access Assignment class through
 	private Assignment aTest;
 	
 	public AssignmentTest(String name) {
 		super(name);
 	}
 
 	/* Setup environment - called before any test method */
 	protected void setUp() throws Exception {
 		super.setUp();
 		// Call constructor with values from spreadsheet
 		aTest = new Assignment("Freddie Catlay", "WordCount in Java", 1, "100");
 	}
 
 	/* Release environment - called after any test method */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/* Test constructor to see if values are assigned correctly
 	 * This also provides coverage for all getter functions
 	 */
 	public void testAssignment() {
 		assertEquals("Freddie Catlay", aTest.getName());
 		assertEquals("WordCount in Java", aTest.getDesc());
 		assertEquals(1, aTest.getNumber());
 		assertEquals("100", aTest.getGrade());
 	}
 
 	/* Test setter functions */
 	public void testSetters() {
 		aTest.setName("Carl Sagan");
 		aTest.setDesc("A very interesting description");
 		aTest.setNumber(2);
 		aTest.setGrade("99");
 		assertEquals("Carl Sagan", aTest.getName());
 		assertEquals("A very interesting description", aTest.getDesc());
 		assertEquals(2, aTest.getNumber());
 		assertEquals("99", aTest.getGrade());
 	}
 
 	/* Test toString() function */
 	public void testToString() {
 		String result = aTest.toString();
		assertEquals("1. Description", result);
 	}
 
 }
