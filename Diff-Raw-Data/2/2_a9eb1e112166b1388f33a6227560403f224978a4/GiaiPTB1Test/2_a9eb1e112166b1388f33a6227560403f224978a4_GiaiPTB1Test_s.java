 import static org.junit.Assert.*;
 import org.junit.Test;
 
 
 public class GiaiPTB1Test {
 
 	@Test
 	public void test1() {
 		GiaiPTB1 tester = new GiaiPTB1();
 		
		assertEquals("Must be -1", 1, tester.giaiPTB1(1, -1));
 	}
 	
 	@Test
 	public void test2() {
 		GiaiPTB1 tester = new GiaiPTB1();
 		
 		assertEquals("Must be 9", 9, tester.giaiPTB1(10, -90));
 	}
 
 }
