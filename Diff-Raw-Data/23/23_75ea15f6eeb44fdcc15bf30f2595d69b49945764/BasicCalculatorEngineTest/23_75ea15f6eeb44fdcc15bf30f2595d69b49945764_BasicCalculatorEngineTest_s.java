 package com.aolchik.tdd.simple;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class BasicCalculatorEngineTest {

 	@Before
 	public void setUp() {
 		this.calc = new BasicCalculatorEngine();	
 	}
 	
 	@Test
 	public void acceptsNewNumber() {	
 		// Intent revealing name
 		
 		// Arrange
 		double num = 25.35;
 
 		// Act, Assert
		assertEquals("through enter", num, calc.enter(num), 
 				deltaForDoubleComparison);		
 			// message to easily find where the test breaks
 		
		assertEquals("through result", num, calc.result(), 
 				deltaForDoubleComparison);		
 	
 		// Reset
 	}
 	
 	@Test
 	public void shouldAdd() {
 		// BDD style name
 		
 		double numToAdd1 = 33.23;
 		double numToAdd2 = 21.2134;
 		
		assertEquals("initial add", numToAdd1, 
 				calc.add(numToAdd1), deltaForDoubleComparison);
		assertEquals("cumulative add", numToAdd1 + numToAdd2, 
 				calc.add(numToAdd2), deltaForDoubleComparison);
		assertEquals("stores result", numToAdd1 + numToAdd2, 
 				calc.result(), deltaForDoubleComparison);
 	}
 		
 
 	@Test
 	public void multiplies() { 
 		double num1 = 3;
 		double num2 = 6;
 		
 		calc.enter(num1);
 		
		assertEquals("as multiply return", num1 * num2, 
 				calc.multiply(num2), deltaForDoubleComparison);
		assertEquals("stores result", num1 * num2, 
 				calc.result(), deltaForDoubleComparison);
 	}
 	
 	@Ignore
 	@Test
 	public void subtracts() { }
 		
 	@Ignore
 	@Test
 	public void clears() { }
 
 	@Ignore
 	@Test
 	public void divides() { }
 
 	@Ignore
 	@Test
 	public void throwsExceptionOnDivisionByZero() { }
 	
 	private static final double deltaForDoubleComparison = 0;
 	private BasicCalculatorEngine calc;	
 }
