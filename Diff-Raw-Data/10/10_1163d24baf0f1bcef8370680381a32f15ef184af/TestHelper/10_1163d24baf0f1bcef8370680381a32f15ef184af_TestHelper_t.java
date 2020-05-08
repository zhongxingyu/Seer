 package cs444.acceptance;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import cs444.Compiler;
 import cs444.types.PkgClassInfo;
 
 public class TestHelper {
 
 	public static void assertReturnCodeForFiles(String path, int expectedReturnCode, boolean printErrors) throws IOException, InterruptedException {
 		File folder = new File(path);
 
 		int totalTests = 0;
 		int filesSkipped = 0;
 		List<String> failFiles = new ArrayList<String>();
 		for (File file : folder.listFiles()) {
 			String fileName = file.getName();
 
 			// Use this line to test a single file
			// if (!fileName.equals("Je_2_DuplicateType")) continue;
 
 			if (file.isFile() && fileName.toLowerCase().endsWith(".java")){
 			    List<String> sourceFiles = getAllFiles(file);
 
 				String[] array = new String[sourceFiles.size()];
 				for (int i = 0; i < array.length; i++)
 					array[i] = sourceFiles.get(i);
 
 				if (compileAndTest(array, printErrors) == expectedReturnCode) {
 					System.out.print(".");
 				}else{
 					System.out.print("F");
 					failFiles.add(path + fileName);
 				}
 				totalTests++;
 			} else if (file.isDirectory()) {
 			    List<String> sourceFiles = getAllFiles(file);
 
 				String[] array = new String[sourceFiles.size()];
 				for (int i = 0; i < array.length; i++)
 					array[i] = sourceFiles.get(i);
 
 				if (compileAndTest(array, printErrors) == expectedReturnCode) {
 					System.out.print(".");
 				} else {
 					System.out.print("F");
 					failFiles.add(path + fileName);
 				}
 				totalTests++;
 			} else {
 				System.out.print("*"); // skip file
 				filesSkipped++;
 			}
 		}
 
 		printSummary(totalTests, filesSkipped, failFiles);
 		int failures = failFiles.size();
 		assertEquals("Compiler return unexpected return code compiling " + failures + " files. Expected return code was: " + expectedReturnCode, 0, failures);
 	}
 
 	private static ArrayList<String> getAllFiles(File root) {
 
 		ArrayList<String> result = new ArrayList<String>();
 		Stack<File> toVisit = new Stack<File>();
 
 		File stdLib = new File("JoosPrograms/StdLib");
         toVisit.push(stdLib);
 
 		toVisit.push(root);
 
 		while (!toVisit.isEmpty()) {
 			File currentFile = toVisit.pop();
 			if (currentFile.isFile()) {
 				String fileName = currentFile.getAbsolutePath();
 				if (fileName.endsWith(".java"))
 					result.add(fileName);
 			} else if (currentFile.isDirectory()) {
 				for (File sourceFile : currentFile.listFiles())
 					toVisit.push(sourceFile);
 			}
 		}
 
 		return result;
 	}
 
 	private static void printSummary(int totalTests, int filesSkipped, List<String> failFiles) {
 		System.out.println("\nNumber of tests: " + totalTests);
 		if(filesSkipped > 0) System.out.println("Number of files skipped: " + filesSkipped);
 		if (failFiles.size() != 0){
 			System.out.println("Unexpected return code compiling these files: ");
 			for (String fileName: failFiles) {
 				System.out.println("\t" + fileName);
 			}
 		}
 	}
 
 	private static int compileAndTest(String[] files, boolean printErrors) throws IOException, InterruptedException {
 	    PkgClassInfo.instance.clear();
 	    return Compiler.compile(files, printErrors);
 	}
 }
