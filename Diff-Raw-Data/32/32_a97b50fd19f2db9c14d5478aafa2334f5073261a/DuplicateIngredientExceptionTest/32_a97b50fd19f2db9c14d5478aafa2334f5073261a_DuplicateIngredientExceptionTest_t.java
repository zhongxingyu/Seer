 package ca.ualberta.team2recipefinder.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import ca.ualberta.team2recipefinder.model.DuplicateIngredientException;
 import ca.ualberta.team2recipefinder.model.Ingredient;
 import ca.ualberta.team2recipefinder.model.MyKitchen;
 
 public class DuplicateIngredientExceptionTest {
 
	private void throwAnException(String msg) throws DuplicateIngredientException {
 		throw new DuplicateIngredientException(msg);
 	}
 	
	private void throwAnException() throws DuplicateIngredientException {
		throw new DuplicateIngredientException();
	}	
 	
 	@Test
 	public void testConstructor() {
 		boolean caughtException = false;
 		
 		try {
 			throwAnException("test");
 		}
 		catch (DuplicateIngredientException e){
 			caughtException = true;
 			
 			// ensure the Exception has ingredient type "test"
 			assertEquals("test", e.getIngredientType());
 		}
 	
 		// ensure a DuplicateIngredientException was caught
 		assertTrue(caughtException);
 		
 		
 		// now try the default no argument constructor
 		try {
 			throwAnException();
 		}
 		catch (DuplicateIngredientException e){
 			caughtException = true;
 			
 			// ensure the Exception has ingredient type "test"
 			assertEquals("", e.getIngredientType());
 		}
 	
 		// ensure a DuplicateIngredientException was caught
 		assertTrue(caughtException);
 	}
 	
 	@Test
 	public void testGetIngredientType() {
 		boolean caughtException = false;
 		
 		try {
 			throwAnException("abcdefg");
 		}
 		catch (DuplicateIngredientException e){
 			caughtException = true;
 			
 			// ensure the Exception has ingredient type "abcdefg"
 			assertEquals("abcdefg", e.getIngredientType());
 		}
 	}
 	
 	@Test
 	public void addDuplicateIngredients() {
 		Ingredient i1 = new Ingredient("test", 0d, "g");
 		Ingredient i2 = new Ingredient("test", 0d, "g");
		MyKitchen my = new MyKitchen("ingredients.sav");		
 		
 		boolean caughtFirstExp = false;
 		boolean caughtSecondExp = false;
 		
 		try {
 			my.add(i1);
 		}
 		catch (DuplicateIngredientException e){
 			// shouldn't reach this
 			caughtFirstExp = true;
 		}
 		try {
 			my.add(i2);
 		}
 		catch (DuplicateIngredientException e){
 			caughtSecondExp = true;
 		}
 		
 		assertTrue(!caughtFirstExp);
 		assertTrue(caughtSecondExp);
 	}
 }	
 	
 
