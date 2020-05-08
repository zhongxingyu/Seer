 package com.loki2302;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.Test;
 
 public class AppTest {	
 	@Test
 	public void calculatorIsOk() {
 		assertEquals(123, calculate("  123 "));
 		assertEquals(3, calculate("1 + 2"));
 		assertEquals(6, calculate(" 2*3  "));
 		assertEquals(7, calculate(" 1 + 2 * 3 "));
 		assertEquals(10, calculate("2 * 3 + 4"));
 		assertEquals(26, calculate("2 * 3 + 4 * 5"));
 		assertEquals(9, calculate(" ( 1 + 2 ) * 3 "));
 		assertEquals(10, calculate("(1 + 2) * 3 + 1"));
 		assertEquals(53, calculate("(1 + 2) * 3 + 4 * (5 + 6)"));	
 		assertEquals(123, calculate("(((((123)))))"));
 		assertEquals(246, calculate(" ( ( ( ( ( 123 ) ) ) * 2 ) ) "));
 	}    
 	
 	@Test
 	public void canHandleDivisionByZero() {
 		EvaluationResult evaluationResult = Calculator.calculate("1 / 0");
 		assertFalse(evaluationResult.isOk());
 		
 		List<FailureReason> failureReasons = evaluationResult.getFailureReasons();
 		assertEquals(1, failureReasons.size());		
 		assertTrue(failureReasons.get(0).getClass() == DivisionByZeroFailureReason.class);
 	}
 	
 	private static int calculate(String expression) {
 		EvaluationResult evaluationResult = Calculator.calculate(expression);
 		assertTrue(evaluationResult.isOk());
 		return evaluationResult.getValue();
 	}
 }
