 package systemout;
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 
 /*
  * 
  * @Version 0.8.2
  * 
  */
 public class SystemOutTest {
 
 	
 	// a- Extend this class in your test class
 	// b- override callUserCode() with whatever you need to do to have 
 	//    the user generate the System.out.println calls
 	//
 	// For each test/set-of-tests for a particular run of user code
 	//  1- call collectUserOutput() or collectUserOuput(String[] userInput).
 	//     This will invoke callUserCode() for you.
 	//  2- call the relevant assertSO... methods
  
 	// if the users code will require inputs, you should make a String[]
 	// or List<String>, with one String for each input.  You can set the input
 	// to use for a particular run of the users code with either a 
 	// call to collectUserOuput(String[]) (or with List<String>),
 	// or by invoking setUserInput(String[]) (or with List<String>) followed
 	// by collectUserOutput()
 	
 	
 	/////////
 	
 
 	
 	
 	////// SysOut asserts
 	//TODO utility method to 'ease' strings to make them compare more forgivingly
 	//      e.g., trim, lowercase, etc.  Make is so asserts can ease strings or not
 
 	// user output contains a line somewhere in it
 	public void assertSOContainsLine(String message, String line) {
 		assertTrue(message, userOutput.contains(line));
 	}
 	
 	//user output has a line at a specific index
 	public void assertSOHasLine(String message, int index, String line) {
 		assertTrue(message, userOutput.get(index).equals(line));
 	}
 	
 	// user output contains a set of lines somewhere in it
 	public void assertSOContainsLines(String message, String[] lines) {
 		fail("assertSOContainsLines unimplemented");
 	}
 	public void assertSOContainsLines (String message, List<String> lines) {
		String[] tmp = {""};
 		assertSOContainsLines(message, lines.toArray(tmp));
 	}
 	
 	// user output contains a set of lines starting at a specific index
 	public void assertSOHasLines(String message, int start, String[] lines) {
 		assertTrue(message, userOutput.size() >= lines.length + start);
		String[] tmp = {""};
 		String[] user_output_subset = Arrays.copyOfRange(userOutput.toArray(tmp), start, start + lines.length);
 		debug("------");
 		debug(user_output_subset);
 		debug(lines);
 		assertArrayEquals(message, user_output_subset, lines);
 	}
 	public void assertSOHasLines(String message, int start, List<String> lines) {
		String[] tmp = {""};
 		assertSOHasLines(message, start, lines.toArray(tmp));
 	}
 		
 	
 	// checks whether the user output is equal to the argument
 	public void assertSOEquals (String message, String[] output) {
		String[] temp = new String[output.length];
 		assertArrayEquals(message, userOutput.toArray(temp), output); 
 	}
 	
 	public void assertSOEquals (String message, List<String> output) {
		String[] temp = new String[output.size()];
		assertArrayEquals(message, userOutput.toArray(temp), output.toArray(temp));
 	}
 	
 	public void assertSOEquals (String message, File output) {
         assertSOEquals(message, file2StringArray(output));
 	}
 	
 	// checks whether the user output has a certain number of lines
 	public void assertSONumLines(String message, int numLines) {
 		assertTrue(message, userOutput.size() == numLines);
 	}
 	
 	// pattern match stuff would be nice
 
 	
 	
 	/////////////
 
 	
 	// will hold the lines from the last set of user output
 	public List<String> userOutput = new ArrayList<String>();
 
 	
 	// override this
 	public void callUserCode() {
 		//throw new Exception("Method callUserCode should be overridden, and super should not be called.");
 		// urk, keeping this in makes ugly throw declarations necessary
 	}
 	
 	
 	///////
 	
 	
 	// NOTE: this will, of course, not do anything to Scanner objects 
 	// that have already been created with a previous System.in.   
 	public void setUserInput(String[] input) {
 		System.setIn(originalIn);
 		StringBuffer tempSB = new StringBuffer();
 		String separator = System.getProperty("line.separator");
 		for (int i = 0; i < input.length; i++) {
 		   tempSB.append( input[i] );
 		   tempSB.append( separator );
 		}
 		InputStream is = new ByteArrayInputStream(tempSB.toString().getBytes());
 		try {
 			System.setIn(is);
 		} catch (SecurityException e) {
 			//eh.
 			e.printStackTrace();
 		}
 	}
 	
 	public void setUserInput(List<String> input) {
 		String[] temp;
 		temp = new String[input.size()];
 		setUserInput(input.toArray(temp));
 	}
 	
 	public void setUserInput(File input) {
 		// how about a FileInputStream, yo!
 		setUserInput(file2StringArray(input));
 	}
 	
 	
 	////
 	
 	public void collectUserOutput(String[] input) {
 		setUserInput(input);
 		collectUserOutput();
 	}
 	
 	public void collectUserOutput(List<String> input) {
 		setUserInput(input);
 		collectUserOutput();
 	}
 	
 	public void collectUserOutput(File input) {
 		setUserInput(input);
 		collectUserOutput();
 	}
 
 	public static String filenamePrefix = "tmpSysoutTest";
 	Scanner userOutputScanner;
 	
 	// call this to run the users code
 	public void collectUserOutput() {
 		File userOutputTempFile;
 		try {
 			userOutputTempFile = File.createTempFile(filenamePrefix, null);
 			PrintStream userOutputTempStream = new PrintStream (userOutputTempFile);
 			System.setOut (userOutputTempStream);
 			userOutputScanner = new Scanner(userOutputTempFile);
 			
 			callUserCode();
 			
 			userOutput.clear();
 			while (userOutputScanner.hasNextLine()) {
 				userOutput.add(userOutputScanner.nextLine());
 			}
 			
 			userOutputTempStream.close();
 			userOutputTempFile.delete();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		
 	}
 	
 	
 	
 	private static PrintStream originalOut;
 	private static InputStream originalIn;
 	
 	@BeforeClass
 	public static void setup() {
 		originalOut = System.out;
 		originalIn = System.in;
 	}
 
 	@AfterClass
 	public static void teardown() {
 		System.setOut(originalOut);
 		System.setIn(originalIn);
 		
 	}
 	
 	/// helpers
 	private List<String> file2StringArray(File f) {
         List<String> lines = new ArrayList<String>();
 		try {
 			FileReader fr = new FileReader(f);
 			BufferedReader br = new BufferedReader(fr);
 	        String line = null;
 	        while ((line = br.readLine()) != null) {
 	            lines.add(line);
 	        }
 	        br.close();
 		} catch (FileNotFoundException e) {
 			// from the new FileReader()
 			// could throw a fail() here, but better to blow up probably
 			e.printStackTrace();
 		} catch (IOException e) {
 			// from the readLine()s -- lets blow up here as well
 			e.printStackTrace();
 		}
         return lines;
 	}
 
 	
 	// prints to the original System.out
 	public void debug(String msg) {
 		originalOut.println(msg);
 	}
 	
 	public void debug(String[] msgs) {
 		for (int i=0; i < msgs.length; i++) {
 			originalOut.print("["+i+"] ");
 			debug(msgs[i]);
 		}
 	}
 	
 	public void debug(List<String> msgs) {
 		String[] tmp = {""};
 		debug(msgs.toArray(tmp));
 	}
 }
