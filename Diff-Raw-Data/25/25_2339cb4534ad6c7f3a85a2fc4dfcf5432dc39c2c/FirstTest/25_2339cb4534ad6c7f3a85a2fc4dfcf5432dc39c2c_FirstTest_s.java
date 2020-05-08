 import static org.junit.Assert.*;
 
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
 	
	
 	@Test
 	public void test1() {
 		int input1 = 4;
 		int input2 = 7;
 		int expectedAnswer = 11;
 		int actualAnswer;
 		
 		
 		Operation classUnderTest = new Mult();
 		
 		actualAnswer = classUnderTest.operation(input1,input2);
 		
 		assertSame("Not the same",expectedAnswer, actualAnswer);
 	}
 
 	@Test
 	public void test2() {
 		int input1 = 4;
 		int input2 = 7;
 		int expectedAnswer = 28;
 		int actualAnswer;
 		
 		
 		Operation classUnderTest = new Adder();
 		
 		actualAnswer = classUnderTest.operation(input1,input2);
 		
 		assertSame("Not the same",expectedAnswer, actualAnswer);
 	}
 
 
 	@Test
 	public void test3() {
 		int expectedAnswer = 30;
 		int actualAnswer;
 		
 		
 		Operation classUnderTest = new Adder();
 		
 		actualAnswer = classUnderTest.operation(input1,input2);
 		
 		assertSame("Not the same",expectedAnswer, actualAnswer);
 	}
 	
 }
