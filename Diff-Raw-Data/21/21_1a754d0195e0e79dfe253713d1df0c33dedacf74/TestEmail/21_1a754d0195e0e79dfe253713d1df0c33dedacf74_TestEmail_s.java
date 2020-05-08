 package assignments.FRS.chap2;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class TestEmail {
 
 
 	@Test
 	public void shouldBeABeginningIdentifier() {
 		
 		String testString = "@smumn.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldBeABeginningAndEndingIdentifier() {
 		
 		String testString = "@smumn";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldBeABeginningAndMiddleIdentifier() {
 		
 		String testString = "@.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldNotBeNumbersAtStartOfBeginningIdentifier() {
 		
 		String testString = "11jpbudi@smumn.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 
 	@Test
 	public void shouldNotBeADotIdentifierAtBeginning() {
 		
 		String testString = "jpbudi.jpb11@smumn.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldBeWordsInBeginningIdentifier() {
 		
 		String testString = "11@smumn.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldBeAtSignInEmail() {
 		
 		String testString = "jpbudi11smumn.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldBeAMiddleAndEndingIdentifier() {
 		
 		String testString = "jpbudi11@";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldNotBeAEndingIdentifierInTheBeginning() {
 		
 		String testString = ".edu@smumn.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldBeAnEndingIdentifier() {
 		
 		String testString = "jpbudi11@smumn";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void shouldBeAMiddleIdentifier() {
 		
 		String testString = "jpbudi11@.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertFalse(emailAddress.isValid());
 	}
 	
 	@Test
 	public void canBeOnlyOneCharacterIdentifier() {
 		
 		String testString = "j@s.e";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertTrue(emailAddress.isValid());
 	}
 	
 	@Test
 	public void allComponentsPresent() {
 		
 		String testString = "jpbudi11@smumn.edu";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertTrue(emailAddress.isValid());
 	}
 	
 	@Test
 	public void allComponentsPresentWithMultipleEndingIdentifiers() {
 		
 		String testString = "jpbudi11@smumn.edu.mn.us";
 		EmailAddress emailAddress = new EmailAddress(testString);
 		assertTrue(emailAddress.isValid());
 	}
 
 }
