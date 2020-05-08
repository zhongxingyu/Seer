 // Copyright (c) 2006  Red Hat, Inc.
 // Written by Anthony Balkissoon <abalkiss@redhat.com>
 // Adapted from gnu.testlet.SimpleTestHarness written by Tom Tromey.
 // Copyright (c) 2005  Mark J. Wielaard  <mark@klomp.org>
 
 // This file is part of Mauve.
 
 // Mauve is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2, or (at your option)
 // any later version.
 
 // Mauve is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with Mauve; see the file COPYING.  If not, write to
 // the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 // Boston, MA 02110-1301 USA.
 
 // This file is used by Harness.java to run the tests in a separate process
 // so that the process can be killed by the Harness if it is hung.
 
 import gnu.testlet.ResourceNotFoundException;
 import gnu.testlet.TestHarness;
 import gnu.testlet.TestReport;
 import gnu.testlet.TestResult;
 import gnu.testlet.TestSecurityManager;
 import gnu.testlet.Testlet;
 import gnu.testlet.VisualTestlet;
 import gnu.testlet.config;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Vector;
 
 public class RunnerProcess
     extends TestHarness
 {
   // A description of files that are not tests
   private static final String NOT_A_TEST_DESCRIPTION = "not-a-test";
 
   // A description of files that fail to load
   private static final String FAIL_TO_LOAD_DESCRIPTION = "failed-to-load";
   
   // A description of a test that throws an uncaught exception
   private static final String UNCAUGHT_EXCEPTION_DESCRIPTION = "uncaught-exception";
   
   // Total number of harness.check calls since the last checkpoint
   private int count = 0;
   
   // The location of the emma.jar file
   private static String emmaJarLocation = null;
   
   // Whether or not to use EMMA
   private static boolean useEMMA = true;
 
   // Total number of harness.check fails plus harness.fail calls
   private int failures = 0;
 
   // The expected fails
   private static Vector expected_xfails = new Vector();
 
   // The number of expected failures that did fail
   private int xfailures = 0;
 
   // The number of expected failures that passed (unexpectedly)
   private int xpasses = 0;
 
   // The total number of harness.check calls plus harness.fail calls
   private int total = 0;
 
   // True if we should run in verbose (noisy) mode
   private static boolean verbose = false;  
 
   // True if failing calls to harness.check(Object, Object) should print the
   // toString methods of each Object
   private static boolean debug = false;
 
   // True if stack traces should be printed for uncaught exceptions
   private static boolean exceptions = true;
 
   // A description of the test
   private String description;
 
   // The name of the last checkpoint
   private String last_check;
 
   // The TestReport if a report is necessary
   private static TestReport report = null;
 
   // The xmlfile for the report
   private static String xmlfile = null;
   
   // The result of the current test
   private TestResult currentResult = null;
   
   // The EMMA forced data dump method
   private static Method emmaMethod = null;
   
   // The failure message for the last failing check()
   private String lastFailureMessage = null;
 
   /**
    * Should we run interactive or non-interactive tests ?
    */
   private static boolean interactive;
 
   protected RunnerProcess()
   {    
     try
       {
         BufferedReader xfile = new BufferedReader(new FileReader("xfails"));
         String str;
         while ((str = xfile.readLine()) != null)
           {
             expected_xfails.addElement(str);
           }
       }
     catch (FileNotFoundException ex)
       {
         // Nothing.
       }
     catch (IOException ex)
       {
         // Nothing.
       }
   }
 
   public static void main(String[] args)
   {    
     // The test that Harness wants us to run.
     String testname = null;
     
     // This reader is used to get testnames from Harness
     BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
     
     // Parse the arguments so we can create an appropriate RunnerProcess
     // to run the tests.
     for (int i = 0; i < args.length; i++)
       {        
         if (args[i].equals("-verbose"))
           // User wants to run in verbose mode.
           verbose = true;
         else if (args[i].equals("-debug"))
           // User wants extra debug info.
           debug = true;
         else if (args[i].equals("-noexceptions"))
           // User wants stack traces for uncaught exceptions.
           exceptions = false;
         else if (args[i].equals("-xmlout"))
           {
             // User wants a report.
             if (++i >= args.length)
               throw new RuntimeException("No file path after '-xmlout'.");
             xmlfile = args[i];
           }
         else if (args[i].equalsIgnoreCase("-emma"))
           {
             // User is specifying the location of the eclipse-ecj.jar file
             // to use for compilation.
             if (++i >= args.length)
               throw new RuntimeException("No file path " +
                     "after '-emma'.  Exit");
             emmaJarLocation = args[i];
           }
         else if (args[i].equals("-interactive"))
           interactive = true;
       }
     // If the user wants an xml report, create a new TestReport.
     if (xmlfile != null)
       {
         report = new TestReport(System.getProperties());
       }
     
     // Setup the data coverage dumping mechanism.  The default configuration
     // is to auto-detect EMMA, meaning if the emma classes are found on the 
     // classpath then we should force a dump of coverage data.  Also, the user
     // can configure with -with-emma=JARLOCATION or can specify -emma 
     // JARLOCATION on the command line to explicitly specify an emma.jar to use
     // to dump coverage data.
     if (emmaJarLocation == null)
       emmaJarLocation = config.emmaString;
     try
     {
       setupEMMA(!emmaJarLocation.equals("_auto_detect_emma_"));
     }
     catch (Exception emmaException)
     {
       useEMMA = false;
     }
     
     while (true)
       {
         // Ask Harness for a test to run, run it, report back to Harness, and
         // then repeat the cycle.
         try
         {
           testname = in.readLine();
           if (testname == null)
             System.exit(0);
           if (testname.equals("_dump_data_"))
             {              
               // Print the report if necessary.
               if (report != null)
                 {
                   File f = new File(xmlfile);
                   try
                     {
                       report.writeXml(f);
                     }
                   catch (IOException e)
                     {
                       throw new Error("Failed to write data to xml file: "
                                       + e.getMessage());
                     }
                 }
 
               if (useEMMA)
                 dumpCoverageData();
               else
                 System.out.println("_data_dump_okay_");
             }
           else if (testname.equals("_confirm_startup_"))
             System.out.println("_startup_okay_");
           else
             {
               RunnerProcess harness = new RunnerProcess();
               runAndReport(harness, testname);
             }
         }
         catch (IOException ioe)
         {          
           String shortName = stripPrefix(testname);
           if (verbose)
             System.out.println ("TEST: " + shortName + 
                                 "\n  FAIL: failed to load\n" +
                                 "TEST FAILED: failed to load "+ 
                                 shortName);
           else
             System.out.println("FAIL: " + stripPrefix(testname)
                                  + "\n  failed to load");
           System.out.println("RunnerProcess:fail-0");
         }
       }
   }
   
   /**
    * This method runs a single test.  If an exception is caught some
    * information is printed out so the test can be debugged.
    * @param name the name of the test to run
    */
   protected void runtest(String name)
   {
     // Try to ensure we start off with a reasonably clean slate.
     System.gc();
     System.runFinalization();
 
     currentResult = new TestResult(name.substring(12));
     description = name;
 
     checkPoint(null);
 
     Testlet t = null;
     try
       {
         Class k = Class.forName(name);
         int mods = k.getModifiers();
         if (Modifier.isAbstract(mods))
           {
             description = NOT_A_TEST_DESCRIPTION;
             return;
           }
         
         Object o = k.newInstance();
         if (! (o instanceof Testlet))
           {
             description = NOT_A_TEST_DESCRIPTION;
             return;
           }
         if (o instanceof VisualTestlet)
           {
             if (! interactive)
               {
                 description = NOT_A_TEST_DESCRIPTION;
                 return;
               }
           }
         else
           {
             if (interactive)
               {
                 description = NOT_A_TEST_DESCRIPTION;
                 return;
               }
           }
         t = (Testlet) o;
       }
     catch (Throwable ex)
       {
         description = FAIL_TO_LOAD_DESCRIPTION;
         // Maybe the file was marked not-a-test, check that before we report
         // it as an error
         try
         {
           File f = new File(name.replace('.', File.separatorChar) + ".java");
           BufferedReader r = new BufferedReader(new FileReader(f));
           String firstLine = r.readLine();
           // Since some people mistakenly put not-a-test not as the first line
           // we have to check through the file.
           while (firstLine != null)
             {
               if (firstLine.indexOf("not-a-test") != -1)
                   {
                     description = NOT_A_TEST_DESCRIPTION;
                     return;
                   }
               firstLine = r.readLine();
             }
         }
         catch(FileNotFoundException fnfe)
         {          
         }
         catch (IOException ioe)
         {          
         }
         
         String r = getStackTraceString(ex, "          ");
         currentResult.addException(ex, "failed loading class ", r);
         
         debug(ex);
         if (ex instanceof InstantiationException
             || ex instanceof IllegalAccessException)
           debug("Hint: is the code we just loaded a public non-abstract "
                 + "class with a public nullary constructor???");
 
         
         if (!verbose)
           System.out.println ("FAIL: " + stripPrefix(name)
 				+ "\n  exception when loading:");
         else
           {
             System.out.println ("TEST: "+stripPrefix(name));
             System.out.println("  FAIL: exception when loading");
           }
         if (exceptions)
           System.out.println(getStackTraceString(ex, "   "));
         
         if (verbose)
           System.out.println("TEST FAILED: exception when loading "
                              + stripPrefix(name));
         return;
       }
 
     // If the harness started okay, now we run the test.
     if (t != null)
       {
         try
           {
             if (verbose)
               System.out.println("TEST: " + stripPrefix(name));
             t.test(this);
             removeSecurityManager();
           }
         catch (Throwable ex)
           {
             
             String d = exceptionDetails(ex, name, exceptions);
             String r = getStackTraceString(ex, "          ");
 
             if (failures == 0 && !verbose)
                 System.out.println ("FAIL: " + stripPrefix(name));
             System.out.println(d);
             removeSecurityManager();
             currentResult.addException(ex, d, r);
             
             if (exceptions)
               System.out.println(getStackTraceString(ex, "   "));
             debug(ex);
             
             if (verbose)
               System.out.println("TEST FAILED: uncaught exception "
                                  + stripPrefix(description));
             description = UNCAUGHT_EXCEPTION_DESCRIPTION;
           }
       }
     if (report != null)
       report.addTestResult(currentResult);
   }
 
   /**
    * Returns the stack trace associated with the given Throwable as
    * a String.
    * @param ex the Throwable
    * @return a String representing the stack trace for the given Throwable.
    */
   private static String getStackTraceString(Throwable ex, String pad)  
   {
     StackTraceElement[] st = ex.getStackTrace();
     StringBuffer sb = new StringBuffer(pad + ex.toString() + "\n");
     for (int i = 0; i < st.length; i++)
       sb.append(pad + "at " + st[i].toString() + "\n");
     sb.setLength(sb.length() - 1);
     return sb.toString();
   }
   
   /**
    * This method runs a single test in a new Harness and increments the
    * total tests run and total failures, if the test fails.  Prints
    * PASS and adds to the report, if the appropriate options are enabled.
    * @param harness the TestHarness to use for this test
    * @param testName the name of the test
    */
   static void runAndReport(RunnerProcess harness, String testName)
   {
     // If this call to runtest hangs, Harness will terminate this process.
     harness.runtest(testName.replace(File.separatorChar, '.'));
 
     // If the test wasn't a real test, return and tell Harness so.
     if (harness.description.equals(NOT_A_TEST_DESCRIPTION))
       {
         System.out.println("RunnerProcess:not-a-test");
         return;
       }
     else if (harness.description.equals(FAIL_TO_LOAD_DESCRIPTION))
       {
         System.out.println("RunnerProcess:fail-0");
         return;
       }
     else if (harness.description.equals(UNCAUGHT_EXCEPTION_DESCRIPTION))
       {
         System.out.println("RunnerProcess:fail-0");
         return;
       }
     
     // Print out a summary.
     int temp = harness.done();
     
     // Report back to Harness that we've finished properly, whether the test
     // passed or failed.  Harness will wait for a message starting with 
     // "RunnerProcess" and if it doesn't receive it after a certain amount of 
     // time (specified in the timeout variable) it will consider the test hung
     // and will terminate and restart this Process.
     if (temp == 0)
       System.out.println ("RunnerProcess:pass");
     else
       System.out.println("RunnerProcess:fail-" + temp);
   }
   
   private final String getDescription(StackTraceElement[] st)
   {
     // Find the line number of the check() call that failed.
     int line = -1;
     String fileName = description.substring(description.lastIndexOf('.') + 1);
     fileName += ".java";
     for (int i = 0; i < st.length; i++)
       {
         if (st[i].getClassName().startsWith((description))
 					&& st[i].getFileName().equals(fileName))
           {
             line = st[i].getLineNumber();
             break;
           }
       }
     
     return ("  line " + line + ": " + ((last_check == null) ? "" : last_check) 
             + " [" + (count + 1) + "]");
   }
 
   protected int getFailures()
   {
     return failures;
   }
 
   /**
    * Removes the "gnu.testlet." from the start of a String.
    * @param val the String
    * @return the String with "gnu.testlet." removed
    */
   private static String stripPrefix(String val)
   {
     if (val.startsWith("gnu.testlet."))
       val = val.substring(12);
     return val;
   }
   
   /**
    * A convenience method that sets a checkpoint with the specified name
    * then prints a message about the forced fail.
    *
    * @param name  the checkpoint name.
    */
   public void fail(String name)
   {
     checkPoint(name);
     String desc = check2(false);    
     lastFailureMessage = "forced fail";
     currentResult.addFail(desc + " -- " +lastFailureMessage);
     System.out.println(lastFailureMessage);
   }
   
   /**
    * Checks the two objects for equality and prints a message if they are not
    * equal.
    *
    * @param result  the actual result.
    * @param expected  the expected result.
    */
   public void check(Object result, Object expected)
   {
     boolean ok = (result == null ? expected == null : result.equals(expected));
     String desc = check2(ok);
     // This debug message may be misleading, depending on whether
     // string conversion produces same results for unequal objects.
     if (! ok)
       {
         String gotString = result == null ? "null"
                                          : result.getClass().getName();
         String expString = expected == null ? "null"
                                            : expected.getClass().getName();
         
         // If the strings are equal but the objects aren't, we have to tell
         // the user so, otherwise we can just print the strings.
         if (gotString.equals(expString))
           {
             // Since the toString() methods can print long and ugly information
             // we only use them if the user really wants to see it, ie
             // if they used the -debug option.
             if (debug)
               {
                 gotString = result.toString();
                 expString = expected.toString();
                 lastFailureMessage = "\n           got " + gotString
                                    + "\n\n           but expected " + expString
                                    + "\n\n";
                System.out.println(lastFailureMessage);
                return;
               }
             else
               {
                lastFailureMessage = "Objects were not equal";
                System.out.println("objects were not equal.  " +
                        "Use -debug for more information.");
                return;
               }
           }
        lastFailureMessage = "got " + gotString + " but expected " + expString;
        currentResult.addFail(desc + " -- " +lastFailureMessage);
         System.out.println(lastFailureMessage);
       }
   }
 
   /**
    * Checks two booleans for equality and prints out a message if they are not
    * equal.
    * 
    * @param result the actual result.
    * @param expected the expected result.
    */
   public void check(boolean result, boolean expected)
   {
     boolean ok = (result == expected);
     String desc = check2(ok);
     if (! ok)
       {
         lastFailureMessage = "got " + result + " but expected " + expected;
         currentResult.addFail(desc + " -- " +lastFailureMessage);
         System.out.println(lastFailureMessage);
       }
   }
 
   /**
    * Checks two ints for equality and prints out a message if they are not
    * equal.
    * 
    * @param result the actual result.
    * @param expected the expected result.
    */
   public void check(int result, int expected)
   {
     boolean ok = (result == expected);
     String desc = check2(ok);
     if (! ok)
       {
         lastFailureMessage = "got " + result + " but expected " + expected;
         currentResult.addFail(desc + " -- " +lastFailureMessage);
         System.out.println(lastFailureMessage);
       }
   }
 
   /**
    * Checks two longs for equality and prints out a message if they are not
    * equal.
    * 
    * @param result the actual result.
    * @param expected the expected result.
    */
   public void check(long result, long expected)
   {
     boolean ok = (result == expected);
     String desc = check2(ok);
     if (! ok)
       {
         lastFailureMessage = "got " + result + " but expected " + expected;
         currentResult.addFail(desc + " -- " +lastFailureMessage);
         System.out.println(lastFailureMessage);
       }
   }
 
   /**
    * Checks two doubles for equality and prints out a message if they are not
    * equal.
    * 
    * @param result the actual result.
    * @param expected the expected result.
    */
   public void check(double result, double expected)
   {
     // This triple check overcomes the fact that == does not
     // compare NaNs, and cannot tell between 0.0 and -0.0;
     // and all without relying on java.lang.Double (which may
     // itself be buggy - else why would we be testing it? ;)
     // For 0, we switch to infinities, and for NaN, we rely
     // on the identity in JLS 15.21.1 that NaN != NaN is true.
     boolean ok = (result == expected ? (result != 0)
                                        || (1 / result == 1 / expected)
                                     : (result != result)
                                       && (expected != expected));
     String desc = check2(ok);
     if (! ok)
       {
         lastFailureMessage = "got " + result + " but expected " + expected;
         currentResult.addFail(desc + " -- " +lastFailureMessage);
         System.out.println(lastFailureMessage);
       }
   }
   
   /**
    * Checks if <code>result</code> is true.  If not, prints out 
    * a message.
    * @param result the boolean to check
    */
   public void check(boolean result)
   {
     String desc = check2(result);
     if (!result)
       {
         lastFailureMessage = "boolean passed to check was false";
         currentResult.addFail(desc + " -- " +lastFailureMessage);
         System.out.println(lastFailureMessage);
       }
   }
   
   /**
    * This method prints out failures and checks the XFAILS file.
    * @param result true if the test passed, false if it failed
    */
   private String check2(boolean result)
   {
     // Send a message to the Harness to let it know the current test
     // isn't hung, to restart the timer.
     System.out.println("RunnerProcess:restart-timer");
     
     // If the test failed we have to print out some explanation.
     StackTraceElement[] st = new Throwable().getStackTrace();
     String desc = getDescription(st);
 
     if (! result)
       {        
         if (! expected_xfails.contains(desc))
           {
             // If the failure wasn't expected, we need to print it to the
             // screen.
             if (failures == 0 && !verbose)
               System.out.println ("FAIL: " + stripPrefix(description));
             if (verbose)
               System.out.print("  FAIL:");
             System.out.print(desc + " -- ");
             ++failures;
           }
         else if (verbose)
           {
             // If it was expected but verbose is true, we also print it.
             System.out.println("X" + desc + " -- ");
             ++xfailures;
           }
       }
     else
       {
         // The test passed.  Only print info if verbose is true        
         currentResult.addPass(desc);
         if (verbose)
           {
             if (expected_xfails.contains(desc))
               {
                 System.out.println("XPASS: " + desc);
                 ++xpasses;
               }
             else
               System.out.println("  pass:" + desc);
           }
       }
     ++count;
     ++total;
     return desc;
   }
 
   public Reader getResourceReader(String name) throws ResourceNotFoundException
   {
     return new BufferedReader(new InputStreamReader(getResourceStream(name)));
   }
 
   public InputStream getResourceStream(String name)
       throws ResourceNotFoundException
   {
     // The following code assumes File.separator is a single character.
     if (File.separator.length() > 1)
       throw new Error("File.separator length is greater than 1");
     String realName = name.replace('#', File.separator.charAt(0));
     try
       {
         return new FileInputStream(getSourceDirectory() + File.separator
                                    + realName);
       }
     catch (FileNotFoundException ex)
       {
         throw new ResourceNotFoundException(ex.getLocalizedMessage() + ": "
                                             + getSourceDirectory()
                                             + File.separator + realName);
       }
   }
 
   public File getResourceFile(String name) throws ResourceNotFoundException
   {
     // The following code assumes File.separator is a single character.
     if (File.separator.length() > 1)
       throw new Error("File.separator length is greater than 1");
     String realName = name.replace('#', File.separator.charAt(0));
     File f = new File(getSourceDirectory() + File.separator + realName);
     if (! f.exists())
       {
         throw new ResourceNotFoundException("cannot find mauve resource file"
                                             + ": " + getSourceDirectory()
                                             + File.separator + realName);
       }
     return f;
   }
 
   public void checkPoint(String name)
   {
     last_check = name;
     count = 0;
   }
 
   public void verbose(String message)
   {
     if (verbose)
       System.out.println(message);
   }
 
   public void debug(String message)
   {
     debug(message, true);
   }
 
   public void debug(String message, boolean newline)
   {
     if (debug)
       {
         if (newline)
           System.out.println(message);
         else
           System.out.print(message);
       }
   }
 
   public void debug(Throwable ex)
   {
     if (debug)
       ex.printStackTrace(System.out);
   }
 
   public void debug(Object[] o, String desc)
   {
     debug("Dumping Object Array: " + desc);
     if (o == null)
       {
         debug("null");
         return;
       }
 
     for (int i = 0; i < o.length; i++)
       {
         if (o[i] instanceof Object[])
           debug((Object[]) o[i], desc + " element " + i);
         else
           debug("  Element " + i + ": " + o[i]);
       }
   }
 
   private void removeSecurityManager()
   {
     while (true)
       {
 	SecurityManager sm = System.getSecurityManager();
 	if (!(sm instanceof TestSecurityManager))
 	  break;
 	debug("warning: TestSecurityManager was not uninstalled");
 	((TestSecurityManager) sm).uninstall();
       }
   }
 
   /**
    * This method returns some information about uncaught exceptions.
    * Nothing is printed if the test was run with the -exceptions flag since in
    * that case a full stack trace will be printed.
    * @param ex the exception
    * @param name the name of the test
    * @param exceptions true if a full stack trace will be printed
    * @return a String containing some information about the uncaught exception
    */
   private String exceptionDetails(Throwable ex, String name,
                                          boolean exceptions)
   {
     // If we can't get a stack trace, we return no details.
     StackTraceElement[] st = ex.getStackTrace();
     if (st == null || st.length == 0)
       return "  uncaught exception:";
 
     // lineOrigin will store the line number in the test method that caused
     // the exception.
     int lineOrigin = -1;
     
     // fileName is the name of the Mauve test file
     String fileName = name.substring(name.lastIndexOf('.') + 1);
     fileName += ".java";
     
     // This for loop looks for the line within the test method that caused the
     // exception.
     for (int i = 0; i < st.length; i++)
       {
         if (st[i].getClassName().startsWith(name)
 					&& st[i].getFileName().equals(fileName))
           {
             lineOrigin = st[i].getLineNumber();
             break;
           }
       }
     
     // sb holds all the information we wish to return.
     StringBuffer sb = 
       new StringBuffer("  " + (verbose ? "FAIL: " : "")+ "line " + lineOrigin 
 		       + ": " + (last_check == null ? "" : last_check) +
 		       " [" + (count + 1) + "] -- uncaught exception:");
     
     // If a full stack trace will be printed, this method returns no details.
     if (exceptions)
       return sb.toString();
     
     // Otherwise, add some details onto the buffer before returning.
     sb.append("\n  " + ex.getClass().getName() + " in ");
     sb.append(stripPrefix(st[0].getClassName()) + "." + st[0].getMethodName()
               + " (line " + st[0].getLineNumber() + ")");
     sb.append("\n  Run tests with -exceptions to print exception " +
               "stack traces.");
     return sb.toString();
   }
   
   /**
    * This method is called from Harness to tidy up.  It prints out appropriate
    * information and returns 0 if the test passed or 1 if it failed.
    * @return 0 if the test passed, 1 if it failed
    */
   protected int done()
   {
     if (failures > 0 && verbose)
       {
         System.out.print("TEST FAILED: ");
         System.out.println(failures + " of " + total + " checks failed "
                            + stripPrefix(description));
       }
     else if (verbose)
       System.out.println("TEST PASSED (" + total + " checks) "
                          + stripPrefix(description));
     if (xpasses > 0)
       System.out.println(xpasses + " of " + total
                          + " tests unexpectedly passed");
     if (xfailures > 0)
       System.out.println(xfailures + " of " + total
                          + " tests expectedly failed");
     return failures;
   }
   
   /**
    * Sets up the compiler by reflection, sets up the compiler options,
    * and the PrintWriters to get error messages from the compiler.
    * 
    * @throws Exception if the emma jar can't be found and the sources
    * aren't in the proper place.
    */
   private static void setupEMMA(boolean useJar) throws Exception
   {
     ClassNotFoundException cnfe = null;
     Class klass = null;
     String classname = "com.vladium.emma.rt.RT";
     if (!useJar)
       {                
         try
         {
           klass = Class.forName(classname);
         }
         catch (ClassNotFoundException e)
         {
           cnfe = e;
           useJar = true;
         }
       }
     
     if (useJar)
       {
         File jar = new File(emmaJarLocation);
         if (! jar.exists() || ! jar.canRead())
           throw cnfe;
         
         ClassLoader loader = new URLClassLoader(new URL[] { jar.toURL() });
         try
         {
           klass = loader.loadClass(classname);
         }
         catch (ClassNotFoundException f)
         {
           throw cnfe;
         }
       }
     
     emmaMethod = 
       klass.getMethod
       ("dumpCoverageData", new Class[] 
           { File.class, boolean.class, boolean.class });
   }  
   
   /**
    * This method forces EMMA to dump its coverage data.  We do this
    * when all tests have been completed and only if the user either
    * configured with --with-emma-jar or specified -emma-jar on the
    * command line.
    */
   private static void dumpCoverageData()
   {
     try
     {
       emmaMethod.invoke(null, new Object[] {
                                             new File("coverage.ec"),
                                             Boolean.TRUE,
                                             Boolean.TRUE });
     }
     catch (Exception e)
     {
       // This shouldn't happen.
     }
     System.out.println("_data_dump_okay_");
   }
 }
