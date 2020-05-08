 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package isad.w2;
 
import isad.w2.ArrayList;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jappie
  */
 public class intArrayTest {
 	
 	public intArrayTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 	
 	@Before
 	public void setUp() {
 	}
 	
 	@After
 	public void tearDown() {
 	}
 
 	/**
 	 * Test of add method, of class intArray.
 	 */
 	@Test
 	public void testAdd() throws Exception {
 		System.out.println("add");
 		int n = 4;
		ArrayList<Integer> instance = new ArrayList<Integer>(1);
 		instance.add(n);
 		
 		if(instance.get(0) != n){
 			fail("expected result not returned");
 		}
 	}
 
 
 	/**
 	 * Test of set method, of class intArray.
 	 */
 	@Test
 	public void testSet() throws Exception {
 		System.out.println("set");
 		int index = 2;
 		int n = 3;
 		int size = 5;
 		int last = 5;
		ArrayList<Integer> instance = new ArrayList<Integer>(size);
 		instance.add(1);
 		instance.add(4);
 		instance.add(9);
 		instance.add(last);
 		
 		
 		instance.set(index, n);
 		
 		if(instance.get(index) != n){
 			fail("expected result not returned");
 		}
 		
 		if(instance.get(size-1) != last){
 			fail("last value was not setted, the numbers did not shift");
 		}
 	}
 
 
 
 	/**
 	 * Test of countOccurences method, of class intArray.
 	 */
 	@Test
 	public void testCountOccurences() throws Exception {
 		System.out.println("countOccurences");
 		int n = 3;
 		int size = 5;
		ArrayList<Integer> instance = new ArrayList<Integer>(size);
 		instance.add(3);
 		instance.add(4);
 		instance.add(3);
		instance.add(3);
		int expResult = 3;
 		int result = instance.countOccurences(n);
 		assertEquals(expResult, result);
 	}
 }
