 package com.BudgetBoss.tests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.BudgetBoss.main.InputValidator;
 
 public class InputValidatorTests {
 
 	@Test
 	public void testRecognizeYOrN() {
 		//Given: An Input Validator
 		InputValidator validator = new InputValidator();
 		//When: An input needs to be checked to see if it is "Y" or "y"
 		//And: An input needs to be checked to see if it is "N" or "n"
 		//Then: The input can be validated, or a message will be displayed to the user
 		//And: A validated input will be returned, lower-cased
		assertTrue(validator.inputIsEitherYOrN("y").equals("y"));
		assertTrue(validator.inputIsEitherYOrN("n").equals("n"));
		assertTrue(validator.inputIsEitherYOrN("Y").equals("y"));
		assertTrue(validator.inputIsEitherYOrN("N").equals("n"));
		String message = "invalidEntry";
		assertTrue(validator.inputIsEitherYOrN("jfkdsl").equals(message));		
 	}
 
 }
