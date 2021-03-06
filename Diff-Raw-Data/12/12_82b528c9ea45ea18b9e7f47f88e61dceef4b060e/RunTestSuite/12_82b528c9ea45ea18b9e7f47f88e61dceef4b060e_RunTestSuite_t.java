 package x10.util;
 
 import polyglot.frontend.Compiler;
 import polyglot.types.Types;
 import polyglot.util.ErrorQueue;
 import polyglot.util.SilentErrorQueue;
 import polyglot.util.Position;
 import polyglot.util.ErrorInfo;
 import polyglot.main.Report;
 import polyglot.main.Main;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.lang.*;
 import java.lang.StringBuilder;
 
 import x10.parser.AutoGenSentences;
 import x10.Configuration;
 import x10.X10CompilerOptions;
 
 /**
  * This program is intended to be used to determine if on a given test
  * suite there is a difference between errors produced by one version
  * of the X10 compiler and another. This
  * program is intended to help in situations in which the first
  * version of the compiler does not completely pass the tests,
  * producing some errors or failing instead. The compiler writer can
  * add various <em>markers</em> into the source code for the tests
  * specifying whether an error is expected on this line or not,
  * whether the compiler should not parse a given line, whether the
  * compiler crashes on a given file etc. This program then checks the
  * errors it gets against these markers and emits an error only if the
  * error it receives is not accounted for by the markers.
 
  * <p> Here is how this program is typically used. A version of the
  * compiler is considered stable if <code>RunTestSuite</code> runs on
  * a given test suite without producing errors. Note: This does not
  * mean that the compiler successfully handles the given tests -- just
  * that the errors/crashes it produces have been accounted for by appropriate
  * markers in the source code for the tests.
  * Now make some changes to the compiler and run
  * <code>RunTestSuite</code> again. If it produces errors, then you
  * know that your code changes have caused new errors to arise. Fix
  * your code so that <code>RunTestSuite</code> reports no errors. Now
  * you know that your code is producing exactly the same errors as the
  * stable version of the compiler.
 
  *<p> Again, this does not mean that your new compiler has no errors,
  *just that it has no <em>new</em> errors. At some point you need to
  *go and fix the existing errors, and remove the corresponding markers
  *from the <code>*.x10</code> files.
 
  * <p> In more detail, this program reads a bunch of x10 files and
  * runs the front end on them, taps the <code>ErrorQueue</code>, 
  * extracts line number information from error messages, and
  * compares these errors with error markers in the file and verify
  * that all errors are expected.
  *
  * <p> <code>RunTestSuite</code> accepts all the flags that the X10
  * compiler accepts (and uses them in the same way) except that
  * instead of accepting a list of <code>*.x10</code> files, it
  * requires a directory (or comma-separated list of directories) to be
  * provided as the first argument.  From these directories it collects
  * all <code>*.x10</code> files.
  *
  * <p>The environment flag <code>SEPARATE_COMPILER</code> can be set
  * to <code>false<code> to force <code>RunTestSuite</code> to use the same
  * compiler object for multiple tests. In general this runs the tests
  * much faster, but is still a bit brittle (may yield errors when
  * compiling each test with a separate compiler object would not).
  *
  * <p> The environment flag <code>QUIET</code> can be set to
  * <code>true</code> to run in quiet mode. In this mode various
  * warnings and helpful messages are not printed out.
  *
  * <p> The environment flag <code>SHOW_EXPECTED_ERRORS</code> prints all
  * the errors (even the expected errors).
  * This is useful if we want to diff the output of the compiler to make
  * sure the error messages are exactly the same.
  *
 
  * <p> Five kinds of error markers can be inserted in <code>*.x10</code> files:
  * <ul>
  *  <li><code>ERR</code>  - marks an error or warning
  * <li><code>ShouldNotBeERR<code> - the compiler reports an error, but
  * it shouldn't
  * <li><code>ShouldBeErr</code> - the compiler doesn't report an
  * error, but it should
  * <li><code>COMPILER_CRASHES</code> - the compiler currently crashes
  * on this file
  * <li><code>SHOULD_NOT_PARSE</code> - the compiler should report
  * parsing and lexing errors on this file
  * </ul> 
  * <p> Multiple markers (even of the same kind) may appear on the
  * same line. Thus if a line is marked with <code>ShouldNotBeErr
  * ShouldNotBeErr</code>, the test runner complains if the compiler
  * does not produce two errors on this line.
 
  * <p>The first 3 markers (<code>ERR, ShouldNotBeERR, ShouldBeErr</code>)
  * can come in the form of annotations (<code>@ERR</code>) or in the
  * form of comments (<code>// ERR</code>) The last two
  * (<code>COMPILER_CRASHES,SHOULD_NOT_PARSE</code>) must be a comment.
  *
  * <p> Annotations are replaced with errors by a compiler phase called
  * <code>ErrChecker</code> that happens immediately after parsing.
 
  * <p> The problem with annotations currently
  * are: 
  * <ol>
    <li>You can't put annotations on statement expressions, i.e.,
  * <code> @ERR i=3;</code>
  * doesn't parse
  * However, you can write:
  * <code>@ERR {i=3;}</code>
  *
  *
  * By default we run the compiler with VERBOSE_CALLS.
  * If the file contains the line:
 //OPTIONS: -STATIC_CALLS
  * then we run it with STATIC_CALLS.
 
  * Some directories are permenantly excluded from the test suite
  * (see EXCLUDE_DIRS and EXCLUDE_FILES fields)
  * For example,  "AutoGen" direcotry contains really big files that takes a long time to compile,
  * or LangSpec contains some problematic files I can't fix because they are auto-generated.
 </ul>
  */
 public class RunTestSuite {
    public static boolean getEnvVariable(String name) {
        final String val = System.getenv(name);
        return val!=null && (val.equalsIgnoreCase("t") || val.equalsIgnoreCase("true"));
    }
    public static boolean SEPARATE_COMPILER = getEnvVariable("SEPARATE_COMPILER");
    public static boolean SHOW_EXPECTED_ERRORS = getEnvVariable("SHOW_EXPECTED_ERRORS");
    public static boolean SHOW_RUNTIMES = getEnvVariable("SHOW_RUNTIMES");
    public static boolean QUIET = !SHOW_EXPECTED_ERRORS && getEnvVariable("QUIET");
 
     private static void println(String s) {
         if (!QUIET) System.out.println(s);
     }
     private static int EXIT_CODE = 0;
     private static java.lang.StringBuilder ALL_ERRORS = new StringBuilder();
     private static void err(String s) {
         EXIT_CODE = 1;
         System.err.println(s);
         ALL_ERRORS.append(s).append("n");
     }
 
     //_MustFailCompile means the compilation should fail.
     //_MustFailTimeout means that when running the file it will have an infinite loop
     private static final String[] EXCLUDE_FILES_WITH_SUFFIX = {
             //"_MustFailCompile.x10",
     };
     private static final String[] EXCLUDE_DIRS = {
             "AutoGen", // it takes too long to compile these files
             "NOT_WORKING", // to exclude some benchmarks: https://x10.svn.sourceforge.net/svnroot/x10/benchmarks/trunk
     };
     private static final String[] EXCLUDE_FILES = {
             // LangSpec is auto-generated, so I can't fix those files to make a clean test suite
             "Classes250.x10","Classes160.x10","Classes170.x10",
             "InnerClasses5p9v.x10","Packages5t5g.x10","Stimulus.x10","Statements51.x10", "ClassCtor30_MustFailCompile.x10", "ThisEscapingViaAt_MustFailCompile.x10",
     };
     private static final String[] EXCLUDE_FILES_WITH = {
     };
     private static final String[] INCLUDE_ONLY_FILES_WITH = {
             //"_MustFailCompile.x10",
     };
     public static final int MAX_ERR_QUEUE = 10000;
 
     static {
         Arrays.sort(EXCLUDE_FILES);
         Arrays.sort(EXCLUDE_DIRS);
     }
     private static boolean shouldIgnoreDir(String name) {
         if (Arrays.binarySearch(EXCLUDE_DIRS,name)>=0) return true;
         return false;
     }
     private static boolean shouldIgnoreFile(String name) {
         if (Arrays.binarySearch(EXCLUDE_FILES,name)>=0) return true;
         for (String suffix : EXCLUDE_FILES_WITH_SUFFIX)
             if (name.endsWith(suffix))
                 return true;
         for (String mid : EXCLUDE_FILES_WITH)
             if (name.contains(mid))
                 return true;
         if (INCLUDE_ONLY_FILES_WITH.length>0) {
             for (String mid : INCLUDE_ONLY_FILES_WITH)
                 if (name.contains(mid))
                     return false;
             return true;
         }
         return false;
     }
     private static final int MAX_FILES_NUM = Integer.MAX_VALUE; // Change it if you want to process only a small number of files
 
     /**
      * Finds all *.x10 files in all sub-directories, and compiles them.
      * @param args
      *  The first argument is the directory to search all the *.x10 files.
      *  E.g.,
      *  C:\cygwin\home\Yoav\intellij\sourceforge\x10.runtime\src-x10
      *  C:\cygwin\home\Yoav\intellij\sourceforge\x10.tests
      * @throws Throwable Can be a failed assertion or missing file.
      */
     public static void main(String[] args) throws Throwable {
         assert args.length>0 : "The first command line argument must be an x10 filename or a comma separated list of the directories.\n"+
                     "E.g.,\n"+
                     "C:\\cygwin\\home\\Yoav\\intellij\\sourceforge\\x10.tests,C:\\cygwin\\home\\Yoav\\intellij\\sourceforge\\x10.dist\\samples,C:\\cygwin\\home\\Yoav\\intellij\\sourceforge\\x10.runtime\\src-x10";
 
         List<String> remainingArgs = new ArrayList<String>(Arrays.asList(args));
         remainingArgs.remove(0);
 
         for (String s : args) {
             if (s.contains("STATIC_CALLS") || s.contains("VERBOSE_CALLS"))
                 throw new RuntimeException("You should run the test suite without -VERBOSE_CALLS or -STATIC_CALLS");
         }
         if (SEPARATE_COMPILER)
             println("Running each file with a separate (new) compiler object, so it's less efficient but more stable.");
 
 
         final String dirName = args[0];
         ArrayList<File> files = new ArrayList<File>(10);
         if (dirName.endsWith(".x10")) {
             final File dir = new File(dirName);
             assert dir.isFile() : "File doesn't not exists: "+dirName;
             files.add(getCanonicalFile(dir));
         } else {
             for (String dirStr : dirName.split(",")) {
                 File dir = new File(dirStr);
                 assert dir.isDirectory() : "The first command line argument must be the directory of x10.tests, and you passed: "+dir;
                 int before = files.size();
                 recurse(dir,files);
                 if (before==files.size()) println("Warning: Didn't find any .x10 files to compile in any subdirectory of "+dir);
             }
         }
         ArrayList<FileSummary> summaries = new ArrayList<FileSummary>();
         HashMap<String,File> fileName2File = new HashMap<String, File>();
         for (File f : files) {
             FileSummary fileSummary = analyzeFile(f);
             summaries.add(fileSummary);
 
             String name = f.getName();
             if (fileName2File.containsKey(name))
                 println("Warning: Found two files with the same name in different directories. This maybe confusing and might cause problems in the classpath.\n\tfile1="+fileName2File.get(name)+"\n\tfile2="+f);
             else
                 fileName2File.put(name,f);
         }
 
 
         // adding the directories of the files to -sourcepath (in case they refer to other files that are not compiled, e.g., if we decide to compile the files one by one)
         // I'm also adding parent folders to support packages (see T3.x10)
         LinkedHashSet<String> directories = new LinkedHashSet<String>();
         for (FileSummary f : summaries) {
             int index = -1;
             while ((index = f.fileName.indexOf('/',index+1))!=-1) {
                 directories.add(f.fileName.substring(0, index));
             }
         }
         String dirs = "";
         for (String dir : directories)
             dirs += ";"+dir;
         int argsNum = remainingArgs.size();
         boolean foundSourcePath = false;
         for (int i=1; i<argsNum; i++) {
             final String arg = remainingArgs.get(i);
             if (arg.contains("/x10.runtime/src-x10")) {
                 final String sourcepath = arg + dirs;
                 remainingArgs.set(i, sourcepath);
                 println("sourcepath is: "+sourcepath);
                 foundSourcePath = true;
                 break;
             }
         }
         assert foundSourcePath : "You must use an argument -sourcepath that includes '/x10.runtime/src-x10'";
 
         long start = System.currentTimeMillis();
         for (FileSummary f : summaries) {
             compileFile(f,remainingArgs);
         }
         if (SHOW_RUNTIMES) println("Total running time to compile all files="+(System.currentTimeMillis()-start));
         if (EXIT_CODE!=0) println("Summary of all errors:\n\n"+ALL_ERRORS);
         println("\n\n\n\n\n"+ (EXIT_CODE==0 ? "SUCCESS" : "FAILED") + "\n\n\n");
         System.exit(EXIT_CODE);
     }
     private static int count(String s, String sub) {
         final int len = sub.length();
         int index=-len, res=0;
         while ((index=s.indexOf(sub,index+len))>=0) res++;
         return res;
     }
     public static ArrayList<ErrorInfo> runCompiler(String[] newArgs) {
         return runCompiler(newArgs,false,false);
     }
 
     private static SilentErrorQueue errQueue = new SilentErrorQueue(MAX_ERR_QUEUE,"TestSuiteErrQueue");
     private static Main MAIN = new Main();
     private static Compiler COMPILER;
     public static ArrayList<ErrorInfo> runCompiler(String[] newArgs, boolean COMPILER_CRASHES, boolean STATIC_CALLS) {
         errQueue.getErrors().clear();
         LinkedHashSet<String> sources = new LinkedHashSet<String>();
         final Compiler comp = MAIN.getCompiler(newArgs, null, errQueue, sources);
         if (SEPARATE_COMPILER || COMPILER==null)
             COMPILER = comp;
         X10CompilerOptions opts = (X10CompilerOptions) COMPILER.sourceExtension().getOptions();
         opts.x10_config.STATIC_CALLS = STATIC_CALLS;
         opts.x10_config.VERBOSE_CALLS = !STATIC_CALLS;
         long start = System.currentTimeMillis();
         Throwable err = null;
         try {
             COMPILER.compileFiles(sources);
         } catch (Throwable e) {
             err = e;
         }
         if (COMPILER_CRASHES) {
             if (err==null) err("We expected the compiler to crash, but it didn't :) Remove the 'COMPILER_CRASHES' marker from file "+newArgs[0]);
         } else {
             if (err!=null) {
                 err("Compiler crashed for args="+Arrays.toString(newArgs)+" with exception:");
                 err.printStackTrace();
             }
         }
 
         if (SHOW_RUNTIMES) println("Compiler running time="+(System.currentTimeMillis()-start));
         final ArrayList<ErrorInfo> res = (ArrayList<ErrorInfo>) errQueue.getErrors();
         assert res.size()<MAX_ERR_QUEUE : "We passed the maximum number of errors!";
         return res;
     }
 
     static class LineSummary {
         int lineNo;
         int errCount;
         // todo: add todo, ShouldNotBeERR, ShouldBeErr statistics.
     }
     static class FileSummary {
         final File file;
         final String fileName;
         FileSummary(File f) {
             file = f;
             fileName = f.getAbsolutePath().replace('\\','/');
         }
         boolean STATIC_CALLS = false;
         boolean COMPILER_CRASHES;
         boolean SHOULD_NOT_PARSE;
         ArrayList<String> options = new ArrayList<String>();
         ArrayList<LineSummary> lines = new ArrayList<LineSummary>();
     }
     private static FileSummary analyzeFile(File file) throws IOException {
         FileSummary res = new FileSummary(file);
         final ArrayList<String> lines = AutoGenSentences.readFile(file);
         int lineNum = 0;
         for (String line : lines) {
             lineNum++;
             if (line.contains("COMPILER_CRASHES")) res.COMPILER_CRASHES = true;
             if (line.contains("SHOULD_NOT_PARSE")) res.SHOULD_NOT_PARSE = true;
             int optionsIndex = line.indexOf("OPTIONS:");
             if (optionsIndex>=0) {
                 final String option = line.substring(optionsIndex + "OPTIONS:".length()).trim();
                 res.options.add(option);
                 if (option.equals("-STATIC_CALLS"))
                     res.STATIC_CALLS = true;
             }
             line = line.trim();
             int commentIndex = line.indexOf("//");
             if (commentIndex>0) { // if the line contains just a comment, then we ignore it.
                 int errIndex = line.indexOf("ERR");
                 boolean isERR = errIndex!=-1;
                 if (isERR && commentIndex<errIndex) {
                     LineSummary lineSummary = new LineSummary();
                     lineSummary.lineNo = lineNum;
                     lineSummary.errCount = count(line,"ERR");
                     res.lines.add(lineSummary);
                 }
             }
         }
         return res;
     }
     private static void compileFile(FileSummary summary, List<String> args) throws IOException {
         File file = summary.file;
 
         boolean STATIC_CALLS = summary.STATIC_CALLS; // all the files without ERR markers are done in my batch, using STATIC_CALLS (cause they shouldn't have any errors)
 
         // Now running polyglot
         List<String> allArgs = new ArrayList<String>();
         allArgs.add(summary.fileName);
         allArgs.addAll(args);
         String[] newArgs = allArgs.toArray(new String[allArgs.size()+2]);
         newArgs[newArgs.length-2] = STATIC_CALLS ? "-STATIC_CALLS" : "-VERBOSE_CALLS";
         newArgs[newArgs.length-1] = STATIC_CALLS ? "-VERBOSE_CALLS=false" : "-STATIC_CALLS=false";
         println("Running: "+ summary.fileName);
         ArrayList<ErrorInfo> errors = runCompiler(newArgs, summary.COMPILER_CRASHES, STATIC_CALLS);
         // remove SHOULD_BE_ERR_MARKER and
         // parsing errors (if SHOULD_NOT_PARSE)
         // treating @ERR and @ShouldNotBeERR as if it were a comment (adding a LineSummary)
         for (Iterator<ErrorInfo> it = errors.iterator(); it.hasNext(); ) {
             ErrorInfo info = it.next();
             final int kind = info.getErrorKind();
             if ((kind==ErrorInfo.SHOULD_BE_ERR_MARKER) ||
                 (summary.SHOULD_NOT_PARSE && (kind==ErrorInfo.LEXICAL_ERROR || kind==ErrorInfo.SYNTAX_ERROR)))
                 it.remove();
 
             final Position position = info.getPosition();
             if (kind==ErrorInfo.ERR_MARKER || kind==ErrorInfo.SHOULD_NOT_BE_ERR_MARKER) {
                 it.remove();
                 int lineNo = position.line();
                 LineSummary foundLine = null;
                 for (LineSummary lineSummary : summary.lines)
                     if (lineNo==lineSummary.lineNo) {
                         foundLine = lineSummary;
                         break;
                     }
                 if (foundLine==null) {
                     foundLine = new LineSummary();
                     foundLine.lineNo = lineNo;
                     summary.lines.add(foundLine);
                 }
                 foundLine.errCount++;
             }
         }
 
         // Now checking the errors reported are correct and match ERR markers
         // 1. find all ERR markers that don't have a corresponding error
             for (LineSummary lineSummary : summary.lines) {
                 // try to find the matching error
                 int expectedErrCount = lineSummary.errCount;
                 int lineNum = lineSummary.lineNo;
                 int foundErrCount = 0;
                 ArrayList<ErrorInfo> errorsFound = new ArrayList<ErrorInfo>(expectedErrCount);
                 for (Iterator<ErrorInfo> it=errors.iterator(); it.hasNext(); ) {
                     ErrorInfo err = it.next();
                     final Position position = err.getPosition();
                     if (position!=null && new File(position.file()).equals(file) && position.line()==lineNum) {
                         // found it!
                         errorsFound.add(err);
                         it.remove();
                         if (SHOW_EXPECTED_ERRORS)
                             println("Found error in position="+position+" err: "+ err);
                         foundErrCount++;
                     }
                 }
                 if (expectedErrCount!=foundErrCount &&
                         // we try to have at most 1 or 2 errors in a line.
                         (expectedErrCount<3 || foundErrCount<3)) { // if the compiler reports more than 3 errors, and we marked more than 3, then it's too many errors on one line and it marks the fact the compiler went crazy and issues too many wrong errors.
                     err("File "+file+" has "+expectedErrCount+" ERR markers on line "+lineNum+", but the compiler reported "+ foundErrCount+" errors on that line! errorsFound=\n"+errorsFound);
                 }
             }
 
         // 2. report all the remaining errors that didn't have a matching ERR marker
         // first report warnings
         int warningCount = 0;
         for (ErrorInfo err : errors)
             if (err.getErrorKind()==ErrorInfo.WARNING) {
                 if (!err.getMessage().startsWith(Types.MORE_SPECIFIC_WARNING)) { // ignore those warning messages
                     err("Got a warning in position: "+err.getPosition()+"\nMessage: "+err+"\n");
                 }
                 warningCount++;
             }
         if (errors.size()>warningCount) {
             err("\nThe following errors did not have a matching ERR marker:\n\n");
             for (ErrorInfo err : errors)
                 if (err.getErrorKind()!=ErrorInfo.WARNING)
                     err("Position:\n"+err.getPosition()+"\nMessage: "+err+"\n");
         }
         // todo: check that for each file (without errors) we generated a *.class file, and load them and run their main method (except for the ones with _MustFailTimeout)
     }
     private static void recurse(File dir, ArrayList<File> files) {
         if (files.size()>=MAX_FILES_NUM) return;
         // sort the result, so the output is identical for diff purposes (see SHOW_EXPECTED_ERRORS)
         final File[] filesInDir = dir.listFiles();
         Arrays.sort(filesInDir);
         for (File f : filesInDir) {
             String name = f.getName();
             final boolean isDir = f.isDirectory();
             if (!isDir && shouldIgnoreFile(name)) continue;
             if (isDir && shouldIgnoreDir(name)) continue;
             if (files.size()>=MAX_FILES_NUM) return;
             if (isDir)
                 recurse(f, files);
             else {
                 if (name.endsWith(".x10")) {
                     files.add(getCanonicalFile(f));
                 }
             }
         }
     }
     private static File getCanonicalFile(File f) {
         try {
             return f.getCanonicalFile();
         } catch (java.io.IOException e) {
             return f;
         }
     }
 }
