 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CalculatorTest {
 
 	private Calculator calculator;
 
 	@Before
 	public void setUp() throws Exception {
 		this.calculator = new Calculator();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		this.calculator = null;
 	}
 
 	@Test
 	public void shouldReturn0WhenPassingEmptyString() {
 		assertEquals(0, calculator.add(""));
 	}
 
 	@Test
 	public void shouldReturn0WhenPassingNull() {
 		assertEquals(0, calculator.add(null));
 	}
 
 	@Test
 	public void shouldReturn1WhenPassing1() {
 		assertEquals(1, calculator.add("1"));
 	}
 	
 	@Test
 	public void shouldReturn2WhenPassing2() {
 		assertEquals(2, calculator.add("2"));
 	}
 	
 	@Test
 	public void shouldCorrectlySumTwoNumbers(){
 		assertEquals(3, calculator.add("1,2"));
 	}
 	
 	@Test
 	public void shouldSumNDifferentNumbers(){
 		assertEquals(9, calculator.add("1,2,6"));
 		assertEquals(11, calculator.add("1,2,1,3,4"));
 	}
 	
 	@Test
 	public void shouldAllowNewlineCharsInsteadOfCommas(){
 		assertEquals(6, calculator.add("1\n2,3"));
 	}
 	
 	@Test
 	public void shouldAllowToSpecifyADifferentDelimiters(){
 		assertEquals(3, calculator.add("//;\n1;2"));
 	}
 	
 	@Test
 	public void shouldAllowToSpecifyACustomDelimiterOfAnyLength(){
		assertEquals(6, calculator.add("//[***]\n1***2***3"));
 	}
 	
 	@Test
 	public void shouldThrowExceptionIfNegativeNumbersArePassed(){
 		boolean exceptionFired = false;
 		
 		try{
 			assertEquals(3, calculator.add("//;\n1;-2"));			
 		}catch(Exception e){
 			assertEquals("Negatives not allowed: -2", e.getMessage());
 			exceptionFired = true;
 		}
 		
 		assertTrue("An exception should have been fired", exceptionFired);
 	}
 	
 	@Test
 	public void shouldThrowExceptionWithMoreNegatives(){
 		boolean exceptionFired = false;
 		
 		try{
 			assertEquals(3, calculator.add("//;\n1;-2;4;-1"));			
 		}catch(Exception e){
 			assertEquals("Negatives not allowed: -2, -1", e.getMessage());
 			exceptionFired = true;
 		}
 		
 		assertTrue("An exception should have been fired", exceptionFired);
 	}
 	
 	@Test
 	public void shouldNotAddNumbersBiggerThan1000(){
 		assertEquals(2, calculator.add("2,1001"));
 		assertEquals(1002, calculator.add("2,1000"));
 	}
 	
 
 }
