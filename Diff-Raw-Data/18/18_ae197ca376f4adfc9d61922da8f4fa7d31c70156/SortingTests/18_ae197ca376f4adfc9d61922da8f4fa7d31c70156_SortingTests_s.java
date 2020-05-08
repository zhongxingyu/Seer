 package Sorting;
 
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class SortingTests {
 
 	long numbers[] = {4,3,8,9,7,2,1,5};
 	long sortedNumbers[] = {1,2,3,4,5,7,8,9};
 	
 	long testNumbers[];
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		//Code in here executes ONCE before any tests in this class are run
 		
 	}
 	
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		//Code in here executes ONCE after ALL tests in this class have run
 		
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		//Code in here executes before EACH test in this class
 		
 		// Copy our test numbers into a 2nd array so the
 		// numbers array doesn't get clobbered during the tests
 		testNumbers = new long[8];
 		System.arraycopy(numbers,0,testNumbers,0, 8);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		//Code in here executes after EACH test in this class
 	}
 	
 	
 	@Test
 	public void testSwap() {


 	}
 	
 	@Test
 	public void testMerge() {
 
 		
 	}
 	
 	@Test
 	public void testPartition() {
 		
 	}
 	
 	@Test
 	public void testNonrecMergeSort() {
 	
 	}
 	
 	@Test
 	public void testMergeSort() {
 		
 	}
 	
 	@Test
 	public void testQuickSort() {
 		
 	}
 	
 	@Test
 	public void testBubbleSort() {		
 		
 	}	
 }
