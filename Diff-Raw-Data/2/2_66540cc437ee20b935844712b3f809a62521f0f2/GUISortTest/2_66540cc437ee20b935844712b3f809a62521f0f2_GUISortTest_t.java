 /*
  * JUnit test case for the GUISort program.
  */
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.Before;
 
 
 public class GUISortTest {
 	  
 	GUISort sort;
 	
 	//@Before - run before any tests are run
 	@Before
 	public void setupSort(){
 		sort = new GUISort();
 	}
 	
 	//@Ignore - ignore for now, will write a test at a later time
 	@Ignore
 	public void testGUISort() {
 		fail("Not yet implemented");
 	}
 	
 	//@Ignore - ignore for now, will write a test at a later time
 	@Ignore
 	public void testDraw() {
 		fail("Not yet implemented");
 	}
 	
 	//@Test - run as a unit test
 	@Test
 	public void testSelectionSort() {
 		//Check sort method
 		while(sort.finished() != true){
 			sort.nextPass();
 		} 
 		
 		int[] expected = {95,85,77,45,40,39,30,25,20,8};
 		assertArrayEquals(expected, sort.data);
 	}
 	
 	//@Ignore - ignore for now, will write a test at a later time
 	@Ignore
 	public void testNextPass() {
 		fail("Not yet implemented");
 	}
 	
 	//@Test - run as a unit test
 	@Test
 	public void testFinished() {
 		//Check sort method
 		 while(sort.finished() != true){
 			sort.nextPass();
 		}
 		 
		 assertTrue("GUISort finished() method should be true once done sorting", sort.finished()); 
 	}
 }
