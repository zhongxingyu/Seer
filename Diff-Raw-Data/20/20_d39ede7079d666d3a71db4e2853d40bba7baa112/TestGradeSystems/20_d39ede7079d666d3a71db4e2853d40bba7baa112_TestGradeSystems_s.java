 import static org.junit.Assert.*;
 
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 
 import org.junit.Test;
 
 public class TestGradeSystems
 {
 	
 	@Test
 	public void testGradeSystems()
 	/*
 	 * This test method is for testing whether GradeSystems()
 	 * can input a file correctly.
 	 * if GradeSystems() throws an exception 
 	 * then the case fails
 	 * 
 	 */
 	{
 		boolean noFileNotFoundException = true;
 
 			try {
 				new GradeSystems();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				noFileNotFoundException = false;
 			}
 			
 			assertTrue(noFileNotFoundException);
 	}
 	
 	@Test
 	public void testShowGrade1()
 	/*
 	 * Test Case 1GinputG 985002513 expected outputG
 	//	lab1		85
 	//	lab2		86
 	//	lab3		94
 	//	mid-term	80
 	//	final exam	85
 	//	total grade	85
 	 */
 	{
 		ByteArrayOutputStream result = new ByteArrayOutputStream();
 		GradeSystems gradeSystem = null;
 		try {
 			gradeSystem = new GradeSystems();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.setOut(new PrintStream(result));
 
 		gradeSystem.showGrade("985002513");
 
 		String expect = "\tlab1\t\t85\n"+
 						"\tlab2\t\t86\n"+
 						"\tlab3\t\t94\n"+
 						"\tmid-term\t80\n"+
 						"\tfinal exam\t85\n"+
 						"\ttotal grade\t85\n";
 		
 		assertEquals(result.toString(), expect);
 	}
 	
 	@Test
 	public void testShowGrade2()
 	/*
 	 * Test Case 1GinputG 985002510 expected outputG
 	//	lab1		91
 	//	lab2		97
 	//	lab3		82
 	//	mid-term	98
 	//	final exam	81
 	//	total grade	89
 	 */
 	{
 		ByteArrayOutputStream result = new ByteArrayOutputStream();
 		GradeSystems gradeSystem = null;
 		try {
 			gradeSystem = new GradeSystems();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.setOut(new PrintStream(result));
 
 		gradeSystem.showGrade("985002510");
 
 		String expect = "\tlab1\t\t91\n"+
 						"\tlab2\t\t97\n"+
 						"\tlab3\t\t82\n"+
 						"\tmid-term\t98\n"+
 						"\tfinal exam\t81\n"+
 						"\ttotal grade\t89\n";
 		
 		assertEquals(result.toString(), expect);
 	}
 	
 	@Test
 	public void testShowGrade3()
 	/*
 	 * Test Case 1GinputG null expected outputG
 	 * 䤣즹ID
 	 */
 	{
 		ByteArrayOutputStream result = new ByteArrayOutputStream();
 		GradeSystems gradeSystem = null;
 		try {
 			gradeSystem = new GradeSystems();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.setOut(new PrintStream(result));
 
 		gradeSystem.showGrade(null);
 
 		String expect = "䤣즹ID";
 		
 		assertEquals(result.toString(), expect);
 	}
 
 	//showRank
 	@Test
 	//Test Case 1GinputG 955002056 expected outputG9
 	public void testshowRank1()
 	{
 		String input = "955002056";
 		int expected = 9;
 		int result;
 		
 		GradeSystems gradeSystem = null;
 		try {
 			gradeSystem = new GradeSystems();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		result = gradeSystem.showRank(input);
 
 		assertEquals(expected, result);
 	}
 
 	@Test
 	//Test Case 2GinputG 986002026 expected outputG32
 	public void testshowRank2()
 	{
 		String input = "986002026";
 		int expected = 32;
 		int result;
 
 		GradeSystems gradeSystem = null;
 		try {
 			gradeSystem = new GradeSystems();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		result = gradeSystem.showRank(input);
 
 		assertEquals(expected, result);
 	}
 
 	@Test
 	//Test Case 3GinputG 975002070 expected outputG1
 	public void testshowRank3()
 	{
 		String input = "975002070";
 		int expected = 1;
 		int result;
 
 		GradeSystems gradeSystem = null;
 		try {
 			gradeSystem = new GradeSystems();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		result = gradeSystem.showRank(input);
 
 		assertEquals(expected, result);
 	}
 
 	//updateWeights
 	@Test
 	//Test Case GinputG null  expected outputGnull
 	public void testupdateWeights()
 	{
 		String inContent = "20\r\n20\r\n20\r\n20\r\n20\r\nY\r\n";
 		String expected = String.format("°t\n\tlab1\t\t10%%\n\tlab2\t\t10%%\n\tlab3\t\t10%%\n\tmid-term\t30%%\n\tfinal exam\t40%%\nJst\n\tlab1\t\t\tlab2\t\t\tlab3\t\t\tmid-term\t\tfinal exam\tнT{st\n\tlab1\t\t%d%%\n\tlab2\t\t%d%%\n\tlab3\t\t%d%%\n\tmid-term\t%d%%\n\tfinal exam\t%d%%\nHWT? Y (Yes)  N (No)\n", 20, 20, 20, 20, 20);
 		String result;
 		
 		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
 		System.setIn(new ByteArrayInputStream(inContent.getBytes()));
 		System.setOut(new PrintStream(outContent));
 		GradeSystems gradeSystem = null;
 		try {
 			gradeSystem = new GradeSystems();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		gradeSystem.updateWeights();
 		result = outContent.toString();
 		assertEquals(expected, result);
 	}
 }
