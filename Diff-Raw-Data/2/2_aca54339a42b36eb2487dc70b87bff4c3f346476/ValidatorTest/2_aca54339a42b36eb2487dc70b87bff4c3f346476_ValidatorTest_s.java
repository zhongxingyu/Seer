 package edu.fcla.da.xml;
 
 import static org.junit.Assert.*;
 
 import java.io.InputStream;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.xml.sax.SAXParseException;
 
 public class ValidatorTest {
 
 	protected Validator validator;
 	private InputStream inputStream;
 	
 	@Before
 	public void makeValidator() throws Exception {
 		validator = new Validator();
 	}
 	
 	@After
 	public void closeStream() throws Exception { 
 		inputStream.close();
 	}
 	
 	@Test
 	public void illFormed() throws Exception {
 		openResource("illformed.xml");
 		Checker checker = validator.validate(inputStream);
		int errors = checker.getErrors() + checker.getFatals().size();
 		assertNotSame("ill formed xml validated", 0, errors);
 	}
 
 	@Test
 	public void invalid() throws Exception {
 		openResource("invalid.xml");
 		Checker checker = validator.validate(inputStream);
 		int errors = checker.getErrors().size() + checker.getFatals().size();
 		assertNotSame("invalid xml validated", 0, errors);
 	}
 	
 	@Test
 	public void valid() throws Exception {
 		openResource("valid.xml");
 		Checker checker = validator.validate(inputStream);
 		assertEquals("valid xml has errors", 0, checker.getErrors().size());
 		assertEquals("valid xml has fatal errors", 0, checker.getFatals().size());
 	}
 	
 	@Test
 	public void dateTime() throws Exception {
 		openResource("bad_date_time.xml");
 		Checker checker = validator.validate(inputStream);
 		assertFalse("valid xml has no errors", 0 == checker.getErrors().size());
 		for(SAXParseException error : checker.getErrors()) {				
 			assertTrue("non dateTime error: " + error.getMessage(), 
 					error.getMessage().endsWith("is not a valid value for 'dateTime'.") || 
 					error.getMessage().endsWith("is not valid with respect to its type, 'dateTime'."));
 		}		
 
 	}
 
 	private void openResource(String name) {
 		inputStream = getClass().getResourceAsStream("files/" + name);
 	}
 
 }
