 package com.kole.junit.extensions.test;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.kole.junit.extensions.MultithreadedRunner;
 import com.kole.junit.extensions.ThreadedTest;
 
 @RunWith(MultithreadedRunner.class)
 public class MultithreadTest {
 	private int number1;
 	private int number2;
 	NamePrinter printer;
 	
 	static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 	static String nowString = "27/02/2012";	
 	
 	@Before
 	public void testBefore() {
 		number1 = 22;
 		number2 = 33;			
 		printer = new NamePrinter("Ed", "Lee");		
 	}
 
 	/**
 	 * Positive test. Testing that the normal @Test annotation works even with the 
 	 * new RunnerBuilder
 	 */
 	@Test
 	public void testAddition() {
 		int result = number1 + number2;
 		Assert.assertTrue("Fail additions", result == 55);
 	}
 	
 	/**
 	 * Test that a threadsafe test case works
 	 */
 	@ThreadedTest(numberOfThreads = 10)
 	public void testNamePrinter() {		
 		printer.print();
 		Assert.assertTrue("Output is: " + printer.getOutput(), printer.getOutput().equals("Ed Lee"));
 	}
 	
 	/**
 	 * This is a negative test. We know SimpleDateFormat is not thread safe
 	 */
 	@ThreadedTest(numberOfThreads = 100)	
 	public void testDateFormat() throws ParseException {
 		Date result = df.parse(nowString);
 		String resultString = df.format(result);
		System.out.println("Result: " + resultString.equals(nowString) +  ", resultString: " + resultString + ", nowString: " + nowString);
 	}
 }
