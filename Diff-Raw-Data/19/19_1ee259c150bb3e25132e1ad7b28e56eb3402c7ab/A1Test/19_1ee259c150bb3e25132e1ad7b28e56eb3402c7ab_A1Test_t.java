 package ca.uwaterloo.joos.test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.PrintStream;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ca.uwaterloo.joos.Main;
 
 @SuppressWarnings("serial")
 public class A1Test {
 
 	private static Main instance;
 
 	class A1Exception extends Exception {
 	}
 
 	class ScannerException extends A1Exception {
 	}
 
 	class ParserException extends A1Exception {
 	}
 
 	class NoException extends Exception {
 	}
 
 	// private enum ExceptionType {General, ScannerException, ParserException,
 	// NoException};
 	@BeforeClass
 	public static void setUp() {
 		instance = new Main();
 	}
 
 	@Test
 	public void Test() {
 		try {
			File outFile = new File("tmp/a1test.out");
 			if (outFile.exists()) {
 				outFile.delete();
				outFile = new File("tmp/a1test.out");
 			}
 			System.setOut(new PrintStream(outFile));
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 
 		File directory = new File("resources/testcases/a1");
 		File[] testFileList = directory.listFiles();
 		for (File testFile : testFileList) {
 
 			try {
 				instance.execute(testFile);
 				Exception fileExpetion = extractFileError(testFile);
 				if (!(fileExpetion instanceof NoException)) {
 					System.out.println("ERROR: " + testFile.getName()
 							+ "\nrealExpetion: NoException" + " fileExpetion: "
 							+ fileExpetion.getClass().getSimpleName() + "\n");
 					// e.printStackTrace();
 				}
 			} catch (Exception e) {
 				Exception realExpetion = extractError(e.getClass().getName());
 				Exception fileExpetion = extractFileError(testFile);
 				if (!realExpetion.getClass().equals(fileExpetion.getClass())) {
 					if (!realExpetion.getClass().getSuperclass()
 							.equals(fileExpetion.getClass())) {
 						System.out.println("ERROR: " + testFile.getName()
 								+ "\nrealExpetion: "
 								+ realExpetion.getClass().getSimpleName()
 								+ " fileExpetion: "
 								+ fileExpetion.getClass().getSimpleName()
 								+ "\n");
 					}
 				}
 			}
 		}
 	}
 
 	private Exception extractFileError(File testFile) {
 		Exception fileExcpetion = new NoException();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(testFile));
 			String errorMessage = br.readLine();
 
 			if (errorMessage.contains("JOOS1:")) {
 				fileExcpetion = extractError(errorMessage);
 			} else {
 				errorMessage = br.readLine();
 				fileExcpetion = extractError(errorMessage);
 				if (fileExcpetion instanceof NoException) {
 					if (testFile.getName().contains("Je")) {
 						br.close();
 						return new A1Exception();
 					}
 				}
 			}
 			br.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return fileExcpetion;
 	}
 
 	public Exception extractError(String errorMessage) {
 		if (errorMessage.contains("LEXER_EXCEPTION")
 				| errorMessage.contains("ScanException")) {
 			return new ScannerException();
 		} else if (errorMessage.contains("PARSER_EXCEPTION")
 				| errorMessage.contains("ParseException")) {
 			return new ParserException();
 		}
 
 		return new NoException();
 	}
 }
