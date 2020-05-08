 package hms.models;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class NurseTest {
 
 	/*@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}*/
 
 	@Test
 	public void test_createNurse() throws SQLException{
		Nurse nurse = new Nurse("Jane Doe", "555-555-5555", "123-456-7890", "Jane@nurses.com", "123, 4th avenue", "123-456-789", 1234, "f", 50000);
 		assertTrue(nurse.create());
 		nurse.delete();
 	}
 	
 	@Test
 	public void test_cannotCreateNurseWithSameIDNumber() throws SQLException{
		Nurse nurse1 = new Nurse("Jane Doe", "555-555-5555", "123-456-7890", "Jane@nurses.com", "123, 4th avenue", "123-456-789", 1234, "f", 50000);
 		assertTrue(nurse1.create());
		Nurse nurse2 = new Nurse("John Doe", "555-555-5555", "123-456-7890", "John@nurses.com", "123, 4th avenue", "123-456-789", 1234, "m", 50000);
 		assertFalse(nurse2.create());
 	}
 
 }
