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
		assertTrue(validator.isEntryYOrN("y").equals("y"));
		assertTrue(validator.isEntryYOrN("n").equals("n"));
		assertTrue(validator.isEntryYOrN("Y").equals("y"));
		assertTrue(validator.isEntryYOrN("N").equals("n"));
		String message = "Invalid entry. Try again! New Budget? (y/n)";
		assertTrue(validator.isEntryYOrN("jfkdsl").equals(message));		
 	}
 
 }
