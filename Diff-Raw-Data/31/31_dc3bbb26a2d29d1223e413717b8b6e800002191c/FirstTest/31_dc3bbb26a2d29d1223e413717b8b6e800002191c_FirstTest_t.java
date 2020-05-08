 import static org.junit.Assert.*;
 
import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 
 /**
  * @author Oded
  *
  */
 
 public class FirstTest {
 
 	private static int input1;
 	private static int input2;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		input1 = 5;
 		input2 = 6;
 	}
 	
 	@Before
 	public void setUpBefore() throws Exception {
 		input1 += 10 ;
 		input2 += 9;
 	}
 
 
	@After
	public void tearDown() throws Exception {
		input1 -= 10 ;
		input2 -= 9;
	}


 	@Test
 	public void test4() {
 		int expectedAnswer = 30;
 		int actualAnswer;
 		
 		
 		Operation classUnderTest = new Mult();
 		
 		actualAnswer = classUnderTest.operation(input1,input2);
 		
		assertEquals("Not the same",expectedAnswer, actualAnswer);
	}	
	
 	@Test
 	public void test1() {
 		int input1 = 4;
 		int input2 = 7;
 		int expectedAnswer = 11;
 		int actualAnswer;
 		
 		
 		Operation classUnderTest = new Mult();
 		
 		actualAnswer = classUnderTest.operation(input1,input2);
 		
		assertEquals("Not the same",expectedAnswer, actualAnswer);
 	}
 
	

 	@Test
 	public void test2() {
 		int input1 = 4;
 		int input2 = 7;
 		int expectedAnswer = 28;
 		int actualAnswer;
 		
 		
 		Operation classUnderTest = new Adder();
 		
 		actualAnswer = classUnderTest.operation(input1,input2);
 		
		assertEquals("Not the same",expectedAnswer, actualAnswer);
 	}
 
 
 	@Test
 	public void test3() {
		int expectedAnswer = 225;
 		int actualAnswer;
 		
 		
 		Operation classUnderTest = new Adder();
 		
 		actualAnswer = classUnderTest.operation(input1,input2);
 		
		assertEquals("Not the same",expectedAnswer, actualAnswer);
 	}
 	
 
 }
