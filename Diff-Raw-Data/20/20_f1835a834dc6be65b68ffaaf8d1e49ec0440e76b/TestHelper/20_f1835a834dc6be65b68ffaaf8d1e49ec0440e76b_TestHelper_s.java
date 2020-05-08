 package cs444.acceptance;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Stack;
 
 import cs444.Compiler;
 import cs444.types.PkgClassInfo;
 
 public class TestHelper {
 
     private static ITestCallbacks callbacks;
     private static boolean outputAsmFiles;
 
 	public static void assertReturnCodeForFiles(String path, int expectedReturnCode, boolean printErrors, boolean includeStdLib,
 	        boolean outputAsmFiles, List<String> ignoreList, ITestCallbacks testCallbacks) throws IOException, InterruptedException {
 	    TestHelper.callbacks = testCallbacks;
 	    TestHelper.outputAsmFiles = outputAsmFiles;
 
 		File folder = new File(path);
 
 		int totalTests = 0;
 		int filesSkipped = 0;
 		List<String> failFiles = new ArrayList<String>();
 		for (File file : folder.listFiles()) {
 			String fileName = file.getName();
 
 			// Use this line to test a single file
			// if (!fileName.equals("J1_StringCast")) continue;
 
 			if (ignoreList.contains(fileName)){
 			    System.out.print("*"); // skip file
                 filesSkipped++;
                 continue;
 			}
 
 			if (file.isFile() && fileName.toLowerCase().endsWith(".java")){
 			    runTestCase(path, expectedReturnCode, printErrors,
                         includeStdLib, failFiles, file, fileName);
 				totalTests++;
 			} else if (file.isDirectory() && !fileName.toLowerCase().endsWith(".skip")) {
 			    runTestCase(path, expectedReturnCode, printErrors,
                         includeStdLib, failFiles, file, fileName);
 				totalTests++;
 			} else {
 				System.out.print("*"); // skip file
 				filesSkipped++;
 			}
 		}
 
 		printSummary(totalTests, filesSkipped, failFiles);
 		int failures = failFiles.size();
 		assertEquals("Unexpected return code compiling or running " + failures + " files. Expected return code was: " + expectedReturnCode, 0, failures);
 	}
 
     private static void runTestCase(String path, int expectedReturnCode,
             boolean printErrors, boolean includeStdLib, List<String> failFiles,
             File file, String fileName) throws IOException,
             InterruptedException {
         List<String> sourceFiles = getAllFiles(file, includeStdLib);
 
         String[] array = new String[sourceFiles.size()];
         for (int i = 0; i < array.length; i++)
             array[i] = sourceFiles.get(i);
 
         if (callbacks.beforeCompile(file)
                 && compileAndTest(array, printErrors) == expectedReturnCode
                 && callbacks.afterCompile(file)) {
             System.out.print(".");
         }else{
             System.out.print("F");
             failFiles.add(path + fileName);
         }
     }
 
     public static void assertReturnCodeForFiles(String path, int expectedReturnCode, boolean printErrors, boolean includeStdLib,
             List<String> ignoreList) throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, includeStdLib, false, ignoreList, new EmptyCallbacks());
     }
 
     public static void assertReturnCodeForFiles(String path, int expectedReturnCode, boolean printErrors) throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, true);
     }
 
     public static void assertReturnCodeForFiles(String path, int expectedReturnCode, boolean printErrors, boolean includeStdLib)
             throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, includeStdLib, Collections.<String>emptyList());
     }
 
     public static void assertReturnCodeForFiles(String path,
             int expectedReturnCode, boolean printErrors, List<String> ignoreList) throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, true, ignoreList);
     }
 
 	private static ArrayList<String> getAllFiles(File root, boolean includeStdLib) {
 
 		ArrayList<String> result = new ArrayList<String>();
 		Stack<File> toVisit = new Stack<File>();
 
 		if(includeStdLib){
 		    File stdLib = new File("JoosPrograms/StdLib");
 		    toVisit.push(stdLib);
 		}
 
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
 		    System.out.println("Failed " + failFiles.size());
 			for (String fileName: failFiles) {
 				System.out.println("\t" + fileName);
 			}
 		}
 	}
 
 	private static int compileAndTest(String[] files, boolean printErrors) throws IOException, InterruptedException {
 	    PkgClassInfo.instance.clear();
 	    return Compiler.compile(files, printErrors, TestHelper.outputAsmFiles);
 	}
 }
