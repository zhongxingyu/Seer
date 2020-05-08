 package jsportsreg.entity;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 /**
  * Ryan M. Kinsley
  * MIST 7570, Spring 2013
  * Dr. Dan Everett
  * @author rkinsley
  *
  */
 
 public class DivisionTest {
 
 	@Test
 	public void testDivision() {
 			
 		Division instance0 = new Division();
 			
 		assertEquals("Division Test 1: Division Description","",instance0.getDivisionDescription());
 		assertEquals("Division Test 1: Division Name","",instance0.getDivisionName());
 		// Don't test the season creation, separate tests for Season class.
 		// Don't test the sport creation, separate tests for Sport class.
 		// Don't test the playerRegistration creation, separate tests for Player Registration class.
 		
 		Season season0 = new Season();
 		Sport sport0 = new Sport();
 		
 		ArrayList<Player_Registration> pList = new ArrayList<Player_Registration>();
 		Player_Registration playerregistration0 = new Player_Registration();
 		
 		pList.add(playerregistration0);
 		
 		instance0.setDivisionID(1);
 		instance0.setDivisionDescription("Fast Pitch: 9 & 10 yoa");
 		instance0.setDivisionName("9 & 10 years old");
 		
 		assertEquals("Division Test 1: Division Description","Fast Pitch: 9 & 10 yoa",instance0.getDivisionDescription());
 		assertEquals("Division Test 1: Division Name","9 & 10 years old",instance0.getDivisionName());
 		
 		Division instance1 = new Division(1, "T-Ball: 5 & 6 yoa","5 & 6 years old", season0, sport0, 
 				pList);
 		
 		assertEquals("Division Test 1: Division Description","T-Ball: 5 & 6 yoa",instance1.getDivisionDescription());
 		assertEquals("Division Test 1: Division Name","5 & 6 years old",instance1.getDivisionName());
 		
 		
 		
 
 	}
 
 }
