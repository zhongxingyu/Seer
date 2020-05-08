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
 import java.util.List;
 import java.util.Scanner;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 
 
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
 
 	public void assertSOContainsLine(String message, String line) {
 		assertTrue(message, userOutput.contains(line));
 	}
 	
 	public void assertSOHasLine(String message, int index, String line) {
 		assertTrue(message, userOutput.get(index).equals(line));
 	}
 	
 	
 	public void assertSOContainsLines(String message, String[] lines) {
 		fail("unimplemented");
 	}
 	
 	public void assertSOContainsLines (String message, List<String> lines) {
 		fail("unimplemented");
 	}
 	
 	public void assertSOEquals (String message, String[] output) {
 		assertArrayEquals(message, userOutput.toArray(), output); 
 	}
 	
 	public void assertSOEquals (String message, List<String> output) {
 		assertArrayEquals(message, userOutput.toArray(), output.toArray());
 	}
 	
 	public void assertSOEquals (String message, File output) {
         assertSOEquals(message, file2StringArray(output));
 	}
 	
 	public void assertSONumLines(String message, int numLines) {
 		assertTrue(message, userOutput.size() == numLines);
 	}
 	
 	// pattern match stuff would be nice
 
 	
 	
 	/////////////
 
 	
 	// will hold the lines from the last set of user output
 	public List<String> userOutput = new ArrayList<String>();
 
 	
 	// override this
 	public void callUserCode() {
 		//throw new Exception("Method callUserCode should be overriden, and super should not be called.");
 		// urk, keeping this in makes ugly throw declarations necessary
 	}
 	
 	
	///////
	
 	
 	public void setUserInput(String[] input) {
 		StringBuffer tempSB = new StringBuffer();
 		String separator = System.getProperty("line.separator");
 		for (int i = 0; i < input.length; i++) {
 		   tempSB.append( input[i] );
 		   tempSB.append( separator );
 		}
 		InputStream is = new ByteArrayInputStream(tempSB.toString().getBytes());
 		System.setIn(is);
 	}
 	
 	public void setUserInput(List<String> input) {
 		String[] temp;
 		temp = new String[input.size()];
 		setUserInput(input.toArray(temp));
 	}
 	
	public void setUserInput(File input) {
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
 
 }
