 package cs444.acceptance;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Stack;
 
 import cs444.Compiler;
 import cs444.codegen.x86_32.linux.X86_32LinuxPlatform;
 import cs444.types.PkgClassInfo;
 
 public class TestHelper {
 
     private static ITestCallbacks callbacks;
     private static boolean outputAsmFiles;
 
     public static final String TEST_LOCATION = Compiler.BASE_DIRECTORY + "JoosPrograms/";
 
     public static void assertReturnCodeForFiles(final String path, final int expectedReturnCode, final boolean printErrors, final boolean includeStdLib,
             final boolean outputAsmFiles, final List<String> ignoreList, final ITestCallbacks testCallbacks) throws IOException, InterruptedException {
         TestHelper.callbacks = testCallbacks;
         TestHelper.outputAsmFiles = outputAsmFiles;
 
         final File folder = new File(path);
 
         int totalTests = 0;
         int filesSkipped = 0;
         final List<String> failFiles = new ArrayList<String>();
         for (final File file : folder.listFiles()) {
             final String fileName = file.getName();
 
             // Use this line to test a single file
            //if (!fileName.equals("StaticFieldsReadWrite")) continue;
 
             if (ignoreList.contains(fileName)){
                 System.out.print("*"); // skip file
                 filesSkipped++;
                 continue;
             }
 
             if (file.isFile() && fileName.toLowerCase().endsWith(".java") ||
                     (file.isDirectory() && !fileName.toLowerCase().endsWith(".skip"))){
                 runTestCase(path, expectedReturnCode, printErrors, includeStdLib, failFiles, file, fileName);
                 totalTests++;
             } else {
                 System.out.print("*"); // skip file
                 filesSkipped++;
             }
         }
 
         printSummary(totalTests, filesSkipped, failFiles);
         final int failures = failFiles.size();
         assertEquals("Unexpected return code compiling or running " + failures + " files. Expected return code was: " + expectedReturnCode, 0, failures);
     }
 
     private static void runTestCase(final String path, final int expectedReturnCode,
             final boolean printErrors, final boolean includeStdLib, final List<String> failFiles,
             final File file, final String fileName) throws IOException, InterruptedException {
         final List<String> sourceFiles = getAllFiles(file, includeStdLib);
 
         final String[] array = new String[sourceFiles.size()];
         sourceFiles.toArray(array);
 
         if (callbacks.beforeCompile(file)
                 && compileAndTest(array, printErrors) == expectedReturnCode
                 && callbacks.afterCompile(file)) {
             System.out.print(".");
         }else{
             System.out.print("F");
             failFiles.add(path + fileName);
         }
     }
 
     public static void assertReturnCodeForFiles(final String path, final int expectedReturnCode, final boolean printErrors, final boolean includeStdLib,
             final List<String> ignoreList) throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, includeStdLib, false, ignoreList, new EmptyCallbacks());
     }
 
     public static void assertReturnCodeForFiles(final String path, final int expectedReturnCode, final boolean printErrors) throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, true);
     }
 
     public static void assertReturnCodeForFiles(final String path, final int expectedReturnCode, final boolean printErrors, final boolean includeStdLib)
             throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, includeStdLib, Collections.<String>emptyList());
     }
 
     public static void assertReturnCodeForFiles(final String path,
             final int expectedReturnCode, final boolean printErrors, final List<String> ignoreList) throws IOException, InterruptedException {
         assertReturnCodeForFiles(path, expectedReturnCode, printErrors, true, ignoreList);
     }
 
     private static ArrayList<String> getAllFiles(final File root, final boolean includeStdLib) {
 
         final ArrayList<String> result = new ArrayList<String>();
         final Stack<File> toVisit = new Stack<File>();
 
         if(includeStdLib){
             final File stdLib = new File(TEST_LOCATION + "StdLib");
             toVisit.push(stdLib);
         }
 
         toVisit.push(root);
 
         while (!toVisit.isEmpty()) {
             final File currentFile = toVisit.pop();
             if (currentFile.isFile()) {
                 final String fileName = currentFile.getAbsolutePath();
                 if (fileName.endsWith(".java"))
                     if(fileName.endsWith("Main.java"))result.add(0, fileName);
                     else result.add(fileName);
             } else if (currentFile.isDirectory()) {
                 for (final File sourceFile : currentFile.listFiles())
                     toVisit.push(sourceFile);
             }
         }
 
         return result;
     }
 
     private static void printSummary(final int totalTests, final int filesSkipped, final List<String> failFiles) {
         System.out.println("\nNumber of tests: " + totalTests);
         if(filesSkipped > 0) System.out.println("Number of files skipped: " + filesSkipped);
         if (failFiles.size() != 0){
             System.out.println("Failed " + failFiles.size());
             for (final String fileName: failFiles) {
                 System.out.println("\t" + fileName);
             }
         }
     }
 
     private static int compileAndTest(final String[] files, final boolean printErrors) throws IOException, InterruptedException {
         PkgClassInfo.instance.clear();
         //Reset the platforms
         X86_32LinuxPlatform.reset();
         return Compiler.compile(files, printErrors, TestHelper.outputAsmFiles);
     }
 }
