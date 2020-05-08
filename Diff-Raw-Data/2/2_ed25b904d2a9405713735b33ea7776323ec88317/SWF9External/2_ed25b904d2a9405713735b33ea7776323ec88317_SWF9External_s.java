 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * SWF9 calls to external compiler
  *
  * @author dda@ddanderson.com
  * @author ptw@openlaszlo.org
  * @description: JavaScript -> ActionScript3 translator, calling AS3 compiler -> SW9 
  *
  */
 
 package org.openlaszlo.sc;
 import java.io.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.openlaszlo.sc.parser.*;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.server.LPS;
 
 import org.apache.commons.collections.LRUMap;
 
 /**
  * The SWF9External manages communication with the
  * external compiler - generation of source files,
  * calling the compiler, packaging up the result and
  * interpreting error messages.
  *
  * It is expected that a new SWF9External object
  * be created for each chunk of compilation.  Each
  * new SWF9External gets a new temporary 'work' directory
  * for compilation, and verifies classname uniqueness
  * within that space.
  */
 public class SWF9External {
 
   /** Number of errors shown before truncating */
   static public final int MAX_ERRORS_SHOWN = 50;
 
   /**
    * A directory we create under the Java runtime's temp dir,
    * that contains our compilation work directories, one for each compilation.
    */
   public static final String WORK_DIR_PARENT = "lzswf9";
 
   /**
    * The prefix for naming the work directories, which appear
    * under the WORK_DIR_PARENT in the Java runtime's temp dir.
    * For example, /tmp/lzswf9/lzgen...., although /tmp
    * may be replaced by something else when running within
    * tomcat or another application server.
    */
   public static final String WORK_DIR_PREFIX = "lzgen";
 
   private File workdir;
   private Compiler.OptionMap options;
   private ScriptCompilerInfo mInfo;
 
   static public String optionsFilename = "LZC_COMPILER_OPTIONS";
 
 
   /*
    * Used by getFileNameForClassName to prevent filename conflicts.
    * The key is the 'tolower' name, the value is an ArrayList of
    * UniqueGlobalName objects.
    */
   private HashMap uniqueFileNames = new HashMap();
 
   public class UniqueGlobalName {
     String globalName;
     boolean isClass;
     int subdirnum;    // subdirector number, 0 means top level
   }
 
   private int maxSubdirnum = 0;
 
   // For incremental mode, check if last compile had same options as current compile.
   boolean compilerOptionsChanged() {
     try {
       File optsfile = new File(workDirectoryName(optionsFilename));
       String newOpts = optionsToString();
       if (optsfile.exists()) {
         String prevOpts = FileUtils.readFileString(optsfile);
        System.err.println("prevOpts: "+prevOpts);
        System.err.println("newOpts: "+newOpts);
         return !newOpts.equals(prevOpts);
       } else {
         return true;
       }
     }
     catch (IOException ioe) {
       throw new CompilerError("compilerOptionsChanged: error while reading incremental compiler options file: " + ioe);
     }
   }
 
   // For incremental mode, write out the compiler options in a persistent file in the working dir
   void writeOptionsFile() {
     try {
       File optsfile = new File(workDirectoryName(optionsFilename));
       String newOpts = optionsToString();
       StringReader from = new StringReader(newOpts);
       // Rewrite the compiler options file with current options
       FileWriter outw = new FileWriter(optsfile);
       FileUtils.send(from, outw);
       outw.close();
     }
     catch (IOException ioe) {
       throw new CompilerError("writeOptionsFile: cannot write incremental compiler options file: " + ioe);
     }
   }
 
 
 
 
     private String optionsToString() {
       StringBuffer result = new StringBuffer();
       result.append("{");
       TreeMap sorted = new TreeMap(options);
       for (Iterator i = sorted.keySet().iterator(); i.hasNext(); ) {
         Object key = i.next();
         result.append(key);
         result.append(": ");
         result.append(sorted.get(key));
         if (i.hasNext()) {
           result.append(", ");
         }
       }
       result.append("}");
       return result.toString();
     }
 
     public SWF9External(Compiler.OptionMap options, boolean buildSharedLibrary) {
       this.options = options;
       mInfo = (ScriptCompilerInfo) options.get(Compiler.COMPILER_INFO);
       if (mInfo == null) {
         mInfo = new ScriptCompilerInfo();
       }
       if (options.getBoolean(Compiler.REUSE_WORK_DIRECTORY)) {
         // Re-use the previous working directory from the ScriptCompilerInfo
         workdir = mInfo.workDir;
       } else {
         workdir = createCompilationWorkDir(options, buildSharedLibrary);
         // Copy pointer to working directory to the ScriptCompilerInfo,
         // so any subsequent <import> library compilations can use it.
         mInfo.workDir = workdir;
       }
 
       // If this is not an incremental compile, erase all files in the working directory
       if (!options.getBoolean(Compiler.DEBUG_EVAL)) {
         if (!options.getBoolean(Compiler.REUSE_WORK_DIRECTORY)) {
           if (options.getBoolean(Compiler.INCREMENTAL_COMPILE)) {
             // If the compiler options changed from the last compile, then clean the directory
             if (compilerOptionsChanged()) {
               System.err.println("swf9 compiler options changes, cleaning working dir");
               deleteDirectoryFiles(workdir);
             }
           } else {
             System.err.println("cleaning working dir");
             deleteDirectoryFiles(workdir);
           }
         }
       }
       writeOptionsFile();
     }
 
     public static void deleteDirectoryFiles(File dir) {
       if (dir.isDirectory()) {
         File[] children = dir.listFiles();
         for (int i=0; i<children.length; i++) {
           File f = children[i];
           if (f.isFile()) {
             f.delete();
           }
         }
       }
     }
 
 
     /**
      * Return the bytes in a file
      */
     public static byte[] getBytes(String filename)
       throws IOException
     {
       File f = new File(filename);
       long len = f.length();
 
       // Passing around byte arrays has limitations.
       if (len > Integer.MAX_VALUE)
         throw new IOException(filename + ": output too large");
 
       byte[] result = new byte[(int)len];
       int pos = 0;
 
       FileInputStream fis = null;
       try {
         fis = new FileInputStream(filename);
         while (pos < len) {
           int nbytes = fis.read(result, pos, (int)len - pos);
           if (nbytes < 0) {
             // premature end of file.  File.length() lied or the
             // length of the file changed out from under us.
             // Either way, we cannot trust it.
             throw new IOException(filename + ": file size discrepency byte " +
                                   pos + "/" + len);
           }
           pos += nbytes;
         }
         // Sanity check, make sure file hasn't been appended to
         if (fis.read() != -1)
           throw new IOException(filename + ": file growing during read at byte " +
                                 pos);
       }
       finally {
         closeit(fis);
       }
       return result;
     }
 
     /**
      * Create a temporary work directory for compilation
      * and return a File for it.
      * @throw CompilerError when directory creation fails
      */
     private File createCompilationWorkDir(Compiler.OptionMap options, boolean buildSharedLibrary)
     {
       // TODO: [2007-11-20 dda] Need some provisions for file
       // cleanup on error, and on success too.
 
       File f = null;
       try {
         String tmpdirstr = System.getProperty("java.io.tmpdir");
         String swf9tmpdirstr = tmpdirstr + File.separator + WORK_DIR_PARENT;
         (new File(swf9tmpdirstr)).mkdirs();
         String appDirPrefix = mInfo.buildDirPathPrefix;
 
         // For Windows, we need to strip any "disk drive" prefix from
         // the path. e.g., "C:"
         if (appDirPrefix != null && appDirPrefix.matches("^[a-zA-Z]:.*")) {
           appDirPrefix = appDirPrefix.substring(3);
         }
 
         if (buildSharedLibrary) {
           // Compiling the LFC
           f = File.createTempFile(WORK_DIR_PREFIX, "", new File(swf9tmpdirstr));
           if (!f.delete()) {
             throw new CompilerError("getCompilationWorkDir: temp file does not exist");
           }
           if (!f.mkdir()) {
             throw new CompilerError("getCompilationWorkDir: cannot make workdir");
           }
         } else {
           f = new File(swf9tmpdirstr + File.separator + appDirPrefix);
           f.mkdirs();
         }
       }
       catch (IOException ioe) {
         throw new CompilerError("getCompilationWorkDir: cannot get temp directory: " + ioe);
       }
       // Copy the pointer to our work directory to the ScriptCompilerInfo object
       this.mInfo.workDir = f;
       return f;
     }
 
     /**
      * For a relative file name, return an absolute path name
      * as the file would appear in the work directory for the compiler.
      */
     public String workDirectoryName(String file)
     {
       if (new File(file).isAbsolute()) {
         throw new IllegalArgumentException("workDirectoryName: file name must be relative");
       }
       return workdir.getPath() + File.separator + file;
     }
 
 
     /**
      * Close an input stream unconditionally.
      */
     public static void closeit(InputStream is)
     {
       try {
         if (is != null)
           is.close();
       }
       catch (IOException ioe) {
         // don't rethrow, we can live with an error during cleanup
         // TODO: [2007-11-20 dda] log this
         System.err.println("Exception closing: " + ioe);
       }
     }
 
     /**
      * Close an output stream unconditionally.
      */
     public static void closeit(OutputStream os)
     {
       try {
         if (os != null)
           os.close();
       }
       catch (IOException ioe) {
         // don't rethrow, we can live with an error during cleanup
         // TODO: [2007-11-20 dda] log this
         System.err.println("Exception closing: " + ioe);
       }
     }
 
     /**
      * A collector for an output stream from an external process.
      */
     public static class OutputCollector extends Thread {
 
       private Exception exception = null;
       private InputStream is;
 
       // we don't expect this to be terribly big, can fit in memory
       StringBuffer sb = new StringBuffer();
 
       public OutputCollector(InputStream is) {
         this.is = is;
       }
     
       public void run() {
         try {
           BufferedReader reader = new BufferedReader(new InputStreamReader(is));
           String line;
           while ((line = reader.readLine()) != null) {
             sb.append(line + "\n");
             collect(line);
           }
           reader.close();
         }
         catch (Exception ex) {
           exception = ex;
         }
       }
 
       public String getOutput() {
         return sb.toString();
       }
       public Exception getException() {
         return exception;
       }
       public void collect(String str) {
         // this version does no more analysis with the output
       }
     }
 
     /**
      * A single error message from the external compiler.
      */
     public static class ExternalCompilerError {
       private int origlinenum = -1;
       private int linenum;
       private int colnum;
       private String error;
       private String code = "";
       private String cleanedCode = "";
       private String orig = "";
       private TranslationUnit tunit;
 
       ExternalCompilerError() {
         this(null, -1, -1, "", "");
       }
 
       ExternalCompilerError(TranslationUnit tunit, int linenum, int colnum, String error, String orig) {
         this.tunit = tunit;
         this.linenum = linenum;
         this.colnum = colnum;
         this.error = error;
         this.orig = orig;
       }
 
       public String toString() {
         String tunitstr = (tunit == null) ? "unknown" : tunit.getName();
         return "External error: " + tunitstr + ": " + linenum + ": " +
           colnum + ": " + error + ": for line:\n" + code;
       }
 
       public int getLineNumber() {
         return linenum;
       }
 
       // returns just the compiler error: e.g.
       //  Error:  variable 'x' undefined
       public String getErrorString() {
         return error;
       }
 
       // returns the original untouched compiler error message
       public String originalErrorString() {
         return orig;
       }
 
       // returns the complete the compiler error,
       // but without the positional 'caret', and
       // an indication of where the code starts,
       // other than just a newline.  This is
       // meant to be read in the browser.
       //   Error: variable 'x' undefined, in line: result = x + 4;
       public String cleanedErrorString() {
         String result = error.trim();
         while (result.endsWith("\n") || result.endsWith(".")) {
           result = result.substring(0, result.length() - 1);
         }
         result += ", in line: " + cleanedCode;
         return result;
       }
 
       public void addCodeLine(String str) {
         if (code.length() > 0) {
           code += "\n";
         }
         code += str;
 
         // In cleanedCode, don't keep lines with just spaces and caret (^)
         if (!str.matches("^[ ^]*$")) {
           if (cleanedCode.length() > 0) {
             cleanedCode += "\n";
           }
           cleanedCode += str;
         }
       }
 
       public String getCode() {
         return code;
       }
 
       public String getCleanedCode() {
         return cleanedCode;
       }
 
       public TranslationUnit getTranslationUnit() {
         return tunit;
       }
 
     }
 
     /**
      * Parse and return an integer (a line number).
      * @throw CompilerError when the input string is not a number
      */
     public static int safeInt(String s)
     {
       try {
         return Integer.parseInt(s);
       }
       catch (NumberFormatException nfe) {
         // should be 'impossible' as the pattern matcher should only
         // give us valid numbers.
         throw new CompilerError("Bad linenumber translation: " + s);
       }
     }
 
     /**
      * Collect the error stream, digesting them into individual
      * ExternalCompilerErrors.
      */
     public class ExternalCompilerErrorCollector extends OutputCollector {
 
       private String inputFilename;
       private Pattern errPattern;
       private List errors = new ArrayList();
       private ExternalCompilerError lastError = null;
       private TranslationUnit[] tunits;
       private List severe = new ArrayList();
 
       // we don't expect this to be terribly big, can fit in memory
       StringBuffer sb = new StringBuffer();
 
       public ExternalCompilerErrorCollector(InputStream is, List tunits) {
         super(is);
         this.inputFilename = inputFilename;
         this.tunits = (TranslationUnit[])tunits.toArray(new TranslationUnit[0]);
 
         // Expect errors to look like File.as(48): col: 1 Error: some message
 
         String pat = "([^\\\\/]+)\\.as\\(([0-9]+)\\): *col: *([0-9]*) *(.*)";
         errPattern = Pattern.compile(pat);
         //System.out.println("Using error pattern: " + pat);
       }
 
       public TranslationUnit locateTranslationUnit(String nm)
       {
         for (int i=0; i<tunits.length; i++) {
           if (nm.equals(tunits[i].getName()))
             return tunits[i];
         }
         return null;
       }
 
       // Do our best to identify severe errors that happen in practice.
       // We'll probably need to add to this list.
 
       public boolean matchSevere(String str) {
         return str.contains("OutOfMemoryError") || str.contains("Java heap space");
       }
 
       public void collect(String str) {
       
         // We expect errors from this compiler to start with the file name.
         // anything else is just showing us the contents of the line.
       
         Matcher matcher = errPattern.matcher(str);
         if (matcher.find()) {
           String classnm = matcher.group(1);
           String linenumstr = matcher.group(2);
           String colstr = matcher.group(3);
           TranslationUnit tunit = locateTranslationUnit(classnm);
           lastError = new ExternalCompilerError(tunit, safeInt(linenumstr),
                                                 safeInt(colstr),
                                                 matcher.group(4),
                                                 str);
           errors.add(lastError);
         }
         else if (matchSevere(str)) {
           severe.add(str);
         }
         else {
           if (lastError == null) {
             System.err.println("Stray error string from external compiler: " + str);
             // Capture it in an error message not tied to a particular line
             lastError = new ExternalCompilerError();
           }
           lastError.addCodeLine(str);
         }
       }
     
       public List getErrors() {
         return errors;
       }
 
       public List getSevereErrors() {
         return severe;
       }
     }
 
     /**
      * True if UNIX quoting rules are in effect.
      */
     public static boolean useUnixQuoting() {
       return !isWindows();
     }
 
     /**
      * Return a more nicely formatted command line.
      * On UNIX systems, we change '$' to \$' so the
      * output line can be cut and pasted into a shell.
      */
     public String prettyCommand(List cmd)
     {
       String cmdstr = "";
       for (Iterator iter = cmd.iterator(); iter.hasNext(); ) {
         if (cmdstr.length() > 0)
           cmdstr += " ";
 
         String arg = (String)iter.next();
         if (useUnixQuoting()) {
       
           // goodness, both $ and \ are special characters for regex.
           arg = arg.replaceAll("[$]", "\\\\\\$");
         }
         if (arg.indexOf(' ') >= 0) {
           arg = "\"" + arg + "\"";
         }
         cmdstr += arg;
       }
       return cmdstr;
     }
 
     /**
      * Copy an environment variable from the current system environment.
      */
     public static void copyEnvVar(List envvars, String varname) {
       String val = System.getenv(varname);
       if (val != null) {
         envvars.add(varname + "=" + val);
       }
     }
 
     // The string collected in BigErrorString
     // will be passed as an exception, so it can't be too large
     //
     public static class BigErrorString {
       int count = 0;
       String str = "";
 
       public void add(String errstr) {
         if (str.length() > 0) {
           str += "\n";
         }
         count++;
         if (count < MAX_ERRORS_SHOWN) {
           str += errstr;
         }
         else if (count == MAX_ERRORS_SHOWN) {
           str += ".... more than " + MAX_ERRORS_SHOWN +
             " errors, additional errors not shown.";
         }
       }
     }
 
     /**
      * Run the compiler using the command/arguments in cmd.
      * Collect and report any errors, and check for the existence
      * of the output file.
      * @throw CompilerError if there are errors messages from the external
      *        compiler, or if any part of the compilation process has problems
      */
     public void execCompileCommand(List cmd, String dir, List tunits,
                                    String outfileName)
       throws IOException          // TODO: [2007-11-20 dda] clean up, why catch only some exceptions?
     {
       String compilerClass = (String) cmd.remove(0);
       String[] cmdstr = (String[])cmd.toArray(new String[0]);
       String prettycmd = prettyCommand(cmd);
       System.err.println("Executing compiler: (cd " + dir + "; " + prettycmd + ")");
       BigErrorString bigErrorString = new BigErrorString();
 
       // Generate a small script (unix style) to document how
       // to build this batch of files.
       String buildsh = isWindows() ? "rem build script\n" : "#!/bin/sh\n";
       buildsh += "cd \"" + dir + "\"\n";
       buildsh += prettycmd + "\n";
       String buildfn = isWindows() ? "build.bat" : "build.sh";
       Compiler.emitFile(workDirectoryName(buildfn), buildsh);
 
 
       if (options.getBoolean(Compiler.EMIT_AS3_ONLY)) {
         return;
       }
 
       List newenv = new ArrayList();
       newenv.add("FLEX_HOME="+FLEX_HOME());
       copyEnvVar(newenv, "HOME");
       copyEnvVar(newenv, "PATH");
       copyEnvVar(newenv, "JAVA_HOME");
       Process proc = Runtime.getRuntime().exec(cmdstr, (String[])newenv.toArray(new String[0]), null);
       try {
         OutputStream os = proc.getOutputStream();
         OutputCollector outcollect = new OutputCollector(proc.getInputStream());
         ExternalCompilerErrorCollector errcollect = new ExternalCompilerErrorCollector(proc.getErrorStream(), tunits);
         os.close();
         outcollect.start();
         errcollect.start();
         int exitval = proc.waitFor();
         outcollect.join();
         errcollect.join();
 
         if (outcollect.getException() != null) {
           System.err.println("Error collecting compiler output: " + outcollect.getException());
           // TODO: [2007-11-20 dda] log this
         }
         String compilerOutput = outcollect.getOutput();
         if (compilerOutput.length() > 0) {
           System.err.println("compiler output:\n" + compilerOutput);
         }
 
         if (errcollect.getException() != null) {
           System.err.println("Error collecting compiler output: " + errcollect.getException());
           // TODO: [2007-11-20 dda] log this
         }
         List severe = errcollect.getSevereErrors();
         if (severe.size() > 0) {
           for (Iterator iter = severe.iterator(); iter.hasNext(); ) {
             String errstr = "SEVERE ERROR: " + (String)iter.next();
             bigErrorString.add(errstr);
             System.err.println(errstr);
           }
         }
         List errs = errcollect.getErrors();
         if (errs.size() > 0) {
           System.err.println("ERRORS: ");
           for (Iterator iter = errs.iterator(); iter.hasNext(); ) {
             ExternalCompilerError err = (ExternalCompilerError)iter.next();
             TranslationUnit tunit = err.getTranslationUnit();
             String srcLineStr;
             TranslationUnit.SourceFileLine srcFileLine;
 
             // actualSrcLine is the name/linenumber of the actual files
             // used in compilation, not the original sources.
             String actualSrcFile = null;
             if (tunit == null) {
               actualSrcFile = "(unknown)";
             }
             else  {
               actualSrcFile = tunit.getSourceFileName();
               if (actualSrcFile == null)
                 actualSrcFile = "(" + tunit.getName() + ")";
             }
 
             String actualSrcLine = "[" + actualSrcFile + ": " + err.getLineNumber() + "] ";
 
             if (tunit == null) {
               srcLineStr = "tunit/line unknown: ";
             }
             else if ((srcFileLine = tunit.originalLineNumber(err.getLineNumber())) == null) {
               srcLineStr = "line unknown: ";
             }
             else {
               srcLineStr = srcFileLine.sourcefile.name + ": " + srcFileLine.line + ": ";
             }
             System.err.println(actualSrcLine + srcLineStr + err.getErrorString());
 
             bigErrorString.add(srcLineStr + err.cleanedErrorString());
           }
         }
 
         if (exitval != 0) {
           System.err.println("FAIL: compiler returned " + exitval);
         }
       }
       catch (InterruptedException ie) {
         throw new CompilerError("Interrupted compiler");
       }
       System.err.println("Done executing compiler");
       if (!new File(outfileName).exists()) {
         System.err.println("Intermediate file " + outfileName + ": does not exist");
         if (bigErrorString.str.length() > 0) {
           throw new CompilerError(bigErrorString.str);
         }
         else {
           throw new CompilerError("Errors from compiler, output file not created");
         }
       }
     }
 
     /**
      * Run the compiler using the command/arguments in cmd. Invokes the Flex compiler classes
      * directly, does not exec a subprocess. 
      * Collect and report any errors, and check for the existence
      * of the output file.
      * @throw CompilerError if there are errors messages from the external
      *        compiler, or if any part of the compilation process has problems
      */
     public void callJavaCompileCommand(List acmd, String dir, List tunits,
                                        String outfileName)
       throws IOException          // TODO: [2007-11-20 dda] clean up, why catch only some exceptions?
     {
       final List cmd = acmd;
       final String compilerClass = (String) cmd.remove(0);
       String[] cmdstr = (String[])cmd.toArray(new String[0]);
       String prettycmd = prettyCommand(cmd);
       System.err.println("Executing compiler: (cd " + dir + "; " + prettycmd + ")");
       BigErrorString bigErrorString = new BigErrorString();
 
       // Generate a small script (unix style) to document how
       // to build this batch of files.
       String buildsh = isWindows() ? "rem build script\n" : "#!/bin/sh\n";
       buildsh += "cd \"" + dir + "\"\n";
       buildsh += prettycmd + "\n";
       String buildfn = isWindows() ? "build.bat" : "build.sh";
       Compiler.emitFile(workDirectoryName(buildfn), buildsh);
 
       // Remove the shell script executable path from beginning of cmd arg list
       cmd.remove(0);
 
       if (options.getBoolean(Compiler.EMIT_AS3_ONLY)) {
         // write out the command line as the output instead
         PrintWriter outf = new PrintWriter(new FileWriter(outfileName));
         for (Iterator iter = cmd.iterator(); iter.hasNext(); ) {
           String arg = (String)iter.next();
           outf.print(arg+" ");
         }
         outf.close();
         System.err.println("option EMIT_AS3_ONLY set, returning without invoking flex compiler, call 'lcompile #' to compile as3");
         return;
       }
       // Save original System.err, System.out
       PrintStream sout = System.out;
       PrintStream serr = System.err;
 
       ByteArrayOutputStream bout = new ByteArrayOutputStream();
       ByteArrayOutputStream berr = new ByteArrayOutputStream();
 
       PrintStream nout = new PrintStream(bout);
       PrintStream nerr = new PrintStream(berr);
 
       // Rebind to capture output
       System.setErr(nerr);
       System.setOut(nout);
 
       // flex2.tools.Mxmlc +flexlib="$FLEX_HOME/frameworks"
       // flex2.tools.Compc
       // 
 
       System.setProperty("FLEX_HOME", FLEX_HOME());
       // The Mxlmc and Compc util classes need to see this arg first in the args list
       cmd.add(0, "+flexlib="+FLEX_HOME()+"/frameworks");
 
 
 
       final Integer exitval[] = new Integer[1];
 
       Thread worker = new Thread() {
           public void run() {
             //Process proc = Runtime.getRuntime().exec(cmdstr, (String[])newenv.toArray(new String[0]), null);
 
             String args[] = (String[])cmd.toArray(new String[0]);
             if (compilerClass.equals("mxmlc")) {
               flex2.tools.Mxmlc.mxmlc(args);
               exitval[0] = new Integer(flex2.compiler.util.ThreadLocalToolkit.errorCount());
 
             } else if (compilerClass.equals("compc")) {
               flex2.tools.Compc.compc(args);
               exitval[0] = new Integer(flex2.compiler.util.ThreadLocalToolkit.errorCount());
             } 
 
           }
         };
 
       try {
         worker.start();
         worker.join();
       } catch (java.lang.InterruptedException e) {
         throw new CompilerError("Errors from compiler, output file not created" + e);      
       } finally {
         // Restore system output and err streams
         System.setErr(serr);
         System.setOut(sout);
       }
 
       try {
         nerr.flush();
         nout.flush();
 
         System.out.println("compiler output is "+bout.toString());
 
         OutputCollector outcollect = new OutputCollector(new ByteArrayInputStream(bout.toByteArray()));
         ExternalCompilerErrorCollector errcollect =
           new ExternalCompilerErrorCollector(new ByteArrayInputStream(berr.toByteArray()), tunits);
         outcollect.start();
         errcollect.start();
         outcollect.join();
         errcollect.join();
 
         if (outcollect.getException() != null) {
           System.err.println("Error collecting compiler output: " + outcollect.getException());
           // TODO: [2007-11-20 dda] log this
         }
         String compilerOutput = outcollect.getOutput();
         if (compilerOutput.length() > 0) {
           System.err.println("compiler output:\n" + compilerOutput);
         }
 
         if (errcollect.getException() != null) {
           System.err.println("Error collecting compiler output: " + errcollect.getException());
           // TODO: [2007-11-20 dda] log this
         }
         List severe = errcollect.getSevereErrors();
         if (severe.size() > 0) {
           for (Iterator iter = severe.iterator(); iter.hasNext(); ) {
             String errstr = "SEVERE ERROR: " + (String)iter.next();
             bigErrorString.add(errstr);
             System.err.println(errstr);
           }
         }
         List errs = errcollect.getErrors();
         if (errs.size() > 0) {
           System.err.println("ERRORS: ");
           for (Iterator iter = errs.iterator(); iter.hasNext(); ) {
             ExternalCompilerError err = (ExternalCompilerError)iter.next();
             TranslationUnit tunit = err.getTranslationUnit();
             String srcLineStr;
             TranslationUnit.SourceFileLine srcFileLine;
 
             // actualSrcLine is the name/linenumber of the actual files
             // used in compilation, not the original sources.
             String actualSrcFile = null;
             if (tunit == null) {
               actualSrcFile = "(unknown)";
             }
             else  {
               actualSrcFile = tunit.getSourceFileName();
               if (actualSrcFile == null)
                 actualSrcFile = "(" + tunit.getName() + ")";
             }
 
             String actualSrcLine = "[" + actualSrcFile + ": " + err.getLineNumber() + "] ";
 
             if (tunit == null) {
               srcLineStr = "tunit/line unknown: ";
             }
             else if ((srcFileLine = tunit.originalLineNumber(err.getLineNumber())) == null) {
               srcLineStr = "line unknown: ";
             }
             else {
               srcLineStr = srcFileLine.sourcefile.name + ": " + srcFileLine.line + ": ";
             }
             System.err.println(actualSrcLine + srcLineStr + err.getErrorString());
 
             bigErrorString.add(srcLineStr + err.cleanedErrorString());
           }
         }
 
         if (exitval[0].intValue() != 0) {
           System.err.println("FAIL: compiler returned " + exitval[0].intValue());
         }
       } catch (InterruptedException ie) {
         throw new CompilerError("Interrupted compiler");
       }
     
       System.err.println("Done executing compiler");
       if (!new File(outfileName).exists()) {
         System.err.println("Intermediate file " + outfileName + ": does not exist");
         if (bigErrorString.str.length() > 0) {
           throw new CompilerError(bigErrorString.str);
         }
         else {
           throw new CompilerError("Errors from compiler, output file not created");
         }
       }
 
       
     }
 
     public static String FLEX_HOME () {
       return LPS.HOME()+File.separator+"WEB-INF";
     }
 
     /**
      * Return a pathname given by a property in the LPS properties.
      * If the path not absolute, it is relative to the LFC directory.
      */
     public static String getFlexPathname(String subpath) {
       return FLEX_HOME() + File.separator + subpath;
     }
 
     /**
      * Tells whether to exec a subprocess to run the flex compiler.
      * Uses lps.properties entry for compiler.swf9.execflex
      */
     public static boolean execFlex() {
       return "true".equals(LPS.getProperty("compiler.swf9.execflex"));
     }
 
     /**
      * Return a boolean value given by a property in the LPS properties.
      */
     public static boolean getLPSBoolean(String propname, boolean defaultValue) {
       String valueString = LPS.getProperty(propname);
       if (valueString == null)
         return defaultValue;
 
       return Boolean.getBoolean(valueString);
     }
 
     /**
      * Get the file name of the LFC shared library for SWF9.
      */
     public static String getLFCLibrary(String runtime, boolean debug, boolean backtrace) {
       return LPS.getLFCDirectory() + File.separator + LPS.getLFCname(runtime, debug, false, backtrace, false);
     }
 
     /**
      * Get the relative URL of the LFC shared library for SWF9.
      */
     public static String getLFCLibraryRelativeURL(String runtime, boolean debug, boolean backtrace) {
       return LPS.getLFCname(runtime, debug, false, backtrace, false).replaceFirst("swc$", "swf");
     }
 
     public static boolean isWindows() {
       String osname = System.getProperty("os.name");
       assert osname != null;
       return osname.startsWith("Windows");
     }
 
     /**
      * Compile the given translation units, producing a binary output.
      */
     public byte[] compileTranslationUnits(List tunits, boolean buildSharedLibrary)
       throws IOException
     {
       List cmd = new ArrayList();
       String outfilebase;
       String exeSuffix = isWindows() ? ".exe" : "";
 
       boolean debug = options.getBoolean(Compiler.DEBUG_SWF9);
       boolean backtrace = options.getBoolean(Compiler.DEBUG_BACKTRACE);
       boolean nameFunctions = options.getBoolean(Compiler.NAME_FUNCTIONS);
     
       // NB: this code used to call execCompileCommand, and pass in the pathname of
       // a shell script to invoke the flex compiler. It now calls callJavaCompileCommand
       // to directly call into the flex jar file now.
       //
       // The first arg in the cmd list is the name 'compc' or 'mxmlc',
       // which will be mapped to the appropriate class in
       // callJavaCompileCommand
 
       if (buildSharedLibrary) {
         outfilebase = "app.swc";
         cmd.add("compc");
         cmd.add(getFlexPathname("bin" + File.separator + "compc" + exeSuffix));
       }
       else {
         outfilebase = "app.swf";
         cmd.add("mxmlc");
         cmd.add(getFlexPathname("bin" + File.separator + "mxmlc" + exeSuffix));
       }
 
       // Path to the flex compiler config file
       cmd.add("-load-config="+getFlexPathname("frameworks/flex-config.xml"));
 
       // -compiler.source-path [path-element] [...]   alias -sp 
       //list of path elements that form the roots of ActionScript class 
       //hierarchies (repeatable)
 
       //    file-specs [path-element] [...]   a list of source files to compile, the last file specified will be 
       //used as the target application (repeatable, default variable)
 
       String outfilename = workdir.getPath() + File.separator + outfilebase;
       boolean swf9Warnings = getLPSBoolean("compiler.swf9.warnings", true);
     
       if (!swf9Warnings) {
         cmd.add("-compiler.show-actionscript-warnings=false");
       }
 
       cmd.add("-compiler.source-path+=" + workdir.getPath());
       for (int i=1; i<=maxSubdirnum; i++) {
         cmd.add("-compiler.source-path+=" + workdir.getPath() + File.separator + i);
       }
       if (nameFunctions) {
         // Ensure function names and source location information is in
         // the binary for debugging
         cmd.add("-debug=true");
       }
       cmd.add("-compiler.headless-server=true");
       cmd.add("-compiler.fonts.advanced-anti-aliasing=true");
       cmd.add("-output");
       cmd.add(outfilename);
     
 
       String runtime = ((String)options.get(Compiler.RUNTIME));
 
       if (buildSharedLibrary) {
         // must be last before list of classes to follow.
         cmd.add("-include-classes");
         // For LFC library, we list all the classes.
         for (Iterator iter = tunits.iterator(); iter.hasNext(); ) {
           TranslationUnit tunit = (TranslationUnit)iter.next();
           cmd.add(tunit.getName());
         }
       } else {
         cmd.add("-default-size");
         cmd.add(options.get(Compiler.CANVAS_WIDTH, "800"));
         cmd.add(options.get(Compiler.CANVAS_HEIGHT, "600"));
         if (options.getBoolean(Compiler.SWF9_USE_RUNTIME_SHARED_LIB)) { // 
           // TODO [hqm 2008-11] This usage of the Flash
           // "runtime-shared-library" feature does not work yet. See LPP-7387
           cmd.add("-runtime-shared-library-path="+ getLFCLibrary(runtime, debug, backtrace) + "," + 
                   "lib" + File.separator +  getLFCLibraryRelativeURL(runtime, debug, backtrace) +
                   ",," // specifies explicitly empty policy file arg
                   ); 
         } else {
           cmd.add("-compiler.library-path+=" + getLFCLibrary(runtime, debug, backtrace));
         }
 
         if (options.getBoolean(Compiler.SWF9_LOADABLE_LIB) ||
             options.getBoolean(Compiler.DEBUG_EVAL)) {
           // Don't include the LFC in this app
           cmd.add("-external-library-path+="+getLFCLibrary(runtime, debug, backtrace));
         }
 
         if (options.getBoolean(Compiler.SWF9_LOADABLE_LIB)) {
           // If it's a loadable lib, check links against the main app,
           // but don't link those classes in. We do this by declaring the main app
           // source working directory as a external-library-path
           cmd.add("-compiler.source-path+="+workdir);
           cmd.add("-external-library-path+="+workdir);
 
         }
 
         // Add in WEB-INF/flexlib and APPDIR/flexlib to flex library search paths if they exist
         if ((new File(getFlexPathname("flexlib"))).isDirectory()) {
           cmd.add("-compiler.library-path+=" + getFlexPathname("flexlib"));
         } 
 
         if ((new File(workdir.getPath() + File.separator + "flexlib")).isDirectory()) {
           cmd.add("-compiler.library-path+=" + getFlexPathname("flexlib"));
         } 
 
         // TODO [hqm 2009-01] SEE LPP-7589 - when one loadable library
         // is loaded by another loadable library at runtime, for some
         // reason references to global "$as3" get a runtime unknown
         // variable error. This is the workaround, explicitly including
         // these globals definitions. For some reason, other globals,
         // like 'canvas', don't seem to have this issue.
         cmd.add("-includes");
         for (Iterator iter = org.openlaszlo.compiler.Compiler.GLOBAL_RUNTIME_VARS.iterator(); iter.hasNext(); ) {
           String varname = (String)iter.next();
           cmd.add(varname);
         }
       }
     
       if ("swf10".equals((String)options.get(Compiler.RUNTIME))) {
         cmd.add("-target-player=10.0.0");
       } else  if ("swf9".equals((String)options.get(Compiler.RUNTIME))) {
         cmd.add("-target-player=9.0.0");
       }
 
       if (options.getBoolean(Compiler.INCREMENTAL_COMPILE)) {
         cmd.add("-compiler.incremental=true");
       }
 
       if (!buildSharedLibrary) {
         String mainclassname = (String) options.get(Compiler.SWF9_WRAPPER_CLASSNAME);
 
         // Insert preloader frame, unless we're compiling a loadable library
         if (!(options.getBoolean(Compiler.SWF9_LOADABLE_LIB) ||
               options.getBoolean(Compiler.DEBUG_EVAL))) {
           // Put application on second frame...
           cmd.add("-frame");
           cmd.add("two");
           cmd.add(mainclassname);
           // List the preloader .as file - the application is on the second frame
           cmd.add("-file-specs=" + workdir.getPath() + File.separator + "LzPreloader.as");
         } else {
           // For the application, we just list one .as file
           cmd.add("-file-specs=" + workdir.getPath() + File.separator + mainclassname + ".as");
         }
       }
 
       // clear out any previously compiled object file
       new File(outfilename).delete();
 
       mFlexTime = System.currentTimeMillis();
 
       // Call the Flex compiler, either in its own exec'ed process or in a thread 
       if (execFlex()) {
         execCompileCommand(cmd, workdir.getPath(), tunits, outfilename);
       } else {
         callJavaCompileCommand(cmd, workdir.getPath(), tunits, outfilename);
       }
       //System.err.println("elapsed time in .as file writing: "+(mElapsed/1000)+" msec");
       //System.err.println("elapsed time in flex compilation: "+(System.currentTimeMillis() - mFlexTime)+" msec");
       
       return getBytes(outfilename);
 
     }
 
     /**
      * Get a unique file name for the class or global variable.  Flex
      * requires that classes and global variables must be defined
      * within matching file names.  For example 'var FooBar' must be in
      * FooBar.as, and 'class foobar' must be in foobar.as.  But some
      * file systems (like FAT32 and AFS+) do not allow file names in the
      * same directory that only differ by case.  In order to allow
      * names that differ by case, we use subdirectories on an as needed
      * basis.  For example, if we see names in the order:
      * "foo" "fOO" "BAR" "Foo" "bar"
      * the file/directory names will be used:
      * <pre>
      *  "./foo.as"
      *  "./1/fOO.as"
      *  "./bar.as"
      *  "./2/Foo.as"
      *  "./1/bar.as"
      * </pre>
      * This system guarantees usable file names on all systems,
      * normally keeping all files in the top level directory, and
      * minimizes the number of subdirectories.  Each subdirectory
      * requires an additional '-compiler.source-path+=...' argument
      * to flex, and we'd rather not test whether there is a limit.
      *
      * This method also checks for class/global var names that have been
      * used before and throws an error.
      *
      * A side effect of this method is to set the maxSubdirnum to the
      * maximum of the subdirectory numbers used.
      *
      * @throw CompilerError for class/var names used previously.
      */
     private String getFileNameForClassName(String name, boolean isClass) {
       String lower = name.toLowerCase();
       List list = (List)uniqueFileNames.get(lower);
 
       if (list == null) {
         list = new ArrayList();
         uniqueFileNames.put(lower, list);
       } else {
         for (Iterator iter = list.iterator(); iter.hasNext(); ) {
           UniqueGlobalName unique = (UniqueGlobalName)iter.next();
           if (name.equals(unique.globalName) && isClass == unique.isClass) {
             String what = isClass ? "class" : "global var";
             throw new CompilerError("cannot declare " + what +
                                     " name more than once: \"" + name + "\"");
           }
         }
       }
       int dirnum = list.size();
       UniqueGlobalName unique = new UniqueGlobalName();
       unique.globalName = name;
       unique.isClass = isClass;
       unique.subdirnum = dirnum;
       list.add(unique);
       String subdirname = workdir.getPath();
       if (dirnum > 0) {
         subdirname += File.separator + dirnum;
       }
       if (dirnum > maxSubdirnum) {
         (new File(subdirname)).mkdirs();
         maxSubdirnum = dirnum;
       }
       return subdirname + File.separator + name + ".as";
     }
 
     /**
      * Return the number of newlines in the string.
      */
     public static int countLines(String str) {
       int count = 0;
       int pos = -1;
       while ((pos = str.indexOf('\n', pos+1)) > 0) {
         count++;
       }
       return count;
     }
 
   // For performance metering, used for measuring compilation times
   long mElapsed = 0;
   long mFlexTime = 0;
 
     /**
      * Write a file given by the translation unit, and using the
      * given pre and post text.
      * @throw CompilerError for any write errors, or class name conflicts
      */
     public void writeFile(TranslationUnit tunit, String pre, String post) {
       String name = tunit.getName();
       String body = tunit.getContents();
       String infilename = getFileNameForClassName(name, tunit.isClass());
       tunit.setSourceFileName(infilename);
       tunit.setLineOffset(countLines(pre));
 
       
 
       if (options.getBoolean(Compiler.PROGRESS)) {
         System.err.println("Creating: " + infilename);
       }
 
       FileOutputStream fos = null;
 
       String content = pre + body + post;
       File diskfile = new File(infilename);
       long startTime = System.nanoTime();
 
       if (options.getBoolean(Compiler.INCREMENTAL_COMPILE)) {
         if (diskfile.exists()) {
           long lastWritten = diskfile.lastModified();
           long classModified = tunit.getLastModified();
 
           // If the lzx source file was modified more recently than the generated .as file, then
           // write out a new .as file.
           if (classModified >= lastWritten) {
             //System.err.print("!");
             //System.err.println(" MODIFIED: "+tunit.getLZXFilename()+" asfile: "+tunit.getSourceFileName() + ":: "+new Date(lastWritten)+", lzxfile: "+new Date(classModified)+"\n");
           } else {
             long elapsedTime = System.nanoTime() - startTime;
             mElapsed += (elapsedTime/1000);
             //            System.err.println("unmodified: "+tunit.getLZXFilename()+" asfile: "+tunit.getSourceFileName() + ":: "+new Date(lastWritten)+", lzxfile: "+new Date(classModified)+"\n");
             return;
           }
         } else {
           //System.err.println("diskfile for tunit "+tunit+" does not exist: "+diskfile);
         }
       }
 
       try {
         fos = new FileOutputStream(infilename);
         fos.write(pre.getBytes());
         fos.write(body.getBytes());
         fos.write(post.getBytes());
         fos.close();
       }
       catch (IOException ioe) {
         System.err.println("Exception in postprocessing, file=" + infilename + ": " + ioe);
         throw new CompilerError("Exception creating files for external compilation: " + ioe);
       }
       finally {
         closeit(fos);
       }
       long elapsedTime = System.nanoTime() - startTime;
       mElapsed += (elapsedTime/1000);
     }
   }
 
 /**
  * @copyright Copyright 2006-2010 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
 
