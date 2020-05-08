 package cs444.acceptance;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.Map.Entry;
 
 import cs444.Compiler;
 import cs444.CompilerSettings;
 import cs444.codegen.CodeGenVisitor;
 import cs444.codegen.Platform;
 import cs444.codegen.tiles.TileSet;
 import cs444.types.APkgClassResolver;
 import cs444.types.PkgClassInfo;
 import cs444.types.PkgClassResolver;
 
 public class TestHelper {
 
     private static ITestCallbacks callbacks;
     private static boolean outputAsmFiles;
 
     private static Set<Platform<?, ?>> platforms;
 
     public static final String TEST_LOCATION = Compiler.BASE_DIRECTORY + "JoosPrograms/";
 
 
     //Holds stdlib so that it can be reused
     private static boolean hasStdlib = false;
     private static Map<String, Map<String, PkgClassResolver>> nameSpaces;
     private static Map<String, APkgClassResolver> symbolMap;
     private static List<APkgClassResolver> pkgs;
 
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
            if (!fileName.equals("LongFields")) continue;
             //Use this line to stop when there are infinite loops
             //if (totalTests == 20) break;
 
             if (ignoreList.contains(fileName)) {
                 System.out.print("*"); // skip file
                 filesSkipped++;
                 continue;
             }
 
             if (file.isFile() && fileName.toLowerCase().endsWith(".java") ||
                     (file.isDirectory() && !fileName.toLowerCase().endsWith(".skip"))) {
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
         final List<String> sourceFiles = getAllFiles(file);
 
         if (!(callbacks.beforeCompile(file)
                 && compileAndTest(sourceFiles, printErrors, includeStdLib) == expectedReturnCode
                 && callbacks.afterCompile(file, platforms))) {
 
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
 
     private static List<String> getAllFiles(final File root) {
 
         final ArrayList<String> result = new ArrayList<>();
         final Stack<File> toVisit = new Stack<>();
 
         toVisit.push(root);
 
         while (!toVisit.isEmpty()) {
             final File currentFile = toVisit.pop();
             if (currentFile.isFile()) {
                 final String fileName = currentFile.getAbsolutePath();
                 if (fileName.endsWith(".java"))
                     if (fileName.endsWith("Main.java"))result.add(0, fileName);
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
         if (filesSkipped > 0) System.out.println("Number of files skipped: " + filesSkipped);
         if (failFiles.size() != 0) {
             System.out.println("Failed " + failFiles.size());
             for (final String fileName: failFiles) {
                 System.out.println("\t" + fileName);
             }
         }
     }
 
     public static void setupMaps() {
         if (!hasStdlib) {
             PkgClassInfo.instance.clear();
             final List<String> files = getAllFiles(new File(TEST_LOCATION + "StdLib"));
             platforms = new HashSet<>();
             final Set<String> opts = Collections.emptySet();
             for(final String platformStr : Compiler.defaultPlatforms) {
                 platforms.add(CompilerSettings.platformMap.get(platformStr).getPlatform(opts));
             }
             Compiler.compile(files, true, false, platforms);
             final PkgClassInfo info = PkgClassInfo.instance;
             nameSpaces = new HashMap<>();
             //because each entry is a map, we need to clone the maps or they will have entries put into them.
             for(final Entry<String, Map<String, PkgClassResolver>> entry : info.nameSpaces.entrySet()) {
                 final Map<String, PkgClassResolver> resolverClone = new HashMap<>(entry.getValue());
                 nameSpaces.put(entry.getKey(), resolverClone);
             }
 
             symbolMap = new HashMap<>(info.symbolMap);
             pkgs = new ArrayList<>(info.pkgs);
             hasStdlib = true;
         }
     }
 
     private static int compileAndTest(final List<String> files, final boolean printErrors, final boolean includeStdlib)
             throws IOException, InterruptedException {
 
         PkgClassResolver.reset();
         TileSet.reset();
         CodeGenVisitor.reset();
 
         if (includeStdlib) {
             setupMaps();
             PkgClassInfo.instance.clear(nameSpaces, symbolMap, pkgs);
         } else {
             PkgClassInfo.instance.clear();
         }
 
         final Set<String> opts = Collections.emptySet();
         platforms = new HashSet<>();
         for(final String platformStr : Compiler.defaultPlatforms) {
             platforms.add(CompilerSettings.platformMap.get(platformStr).getPlatform(opts));
         }
 
         return Compiler.compile(files, printErrors, TestHelper.outputAsmFiles, platforms);
     }
 }
