 /*
  * The MIT Licence
  *
  * Copyright 2010 Joel Hockey (joel.hockey@gmail.com).  All rights reserved.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.joelhockey.jairusunit;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.regex.Pattern;
 
 import junit.framework.Assert;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 
 import org.mozilla.javascript.ClassCache;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.JavaScriptException;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.tools.debugger.Main;
 
 /**
  * Main class to execute JairusUnit.  Takes names of js files as args.
  * @author Joel Hockey
  */
 public class JairusUnit {
     public static final int SUCCESS_EXIT = 0;
     public static final int FAILURE_EXIT = 1;
     public static final int EXCEPTION_EXIT = 2;
 
     public static final Pattern STACK_TRACE_FILTER = Pattern.compile(
             "^\tat (" +
                 "junit|java.lang.reflect" +
                 "|org.mozilla.javascript.(Context|JavaAdapter|MemberBox|NativeJava|[Oo]ptimizer|ScriptRuntime|.*jairusunit.js)" +
                 "|sun.reflect" +
                 "|org.apache.tools.ant" +
                 "|com.joelhockey.jairusunit" +
             ")"
     );
 
     private JairusUnitScope scope = new JairusUnitScope();
 
     /**
      * Return JUnit {@link TestSuite} containing all
      * tests from given js file.
      * @param file javascript file containing tests
      * @return test suite
      */
     public TestSuite jairusunitTestSuite(String file) {
         TestSuite result = new TestSuite("jairusunit");
         Context cx = Context.enter();
         try {
             Function jairusunitTestSuite;
             ClassCache.get(scope).setCachingEnabled(false); // need this
             try {
                 scope.load("jairusunit.js");
                 jairusunitTestSuite = (Function) scope.get("jairusunitTestSuite", scope);
                 NativeJavaObject obj = (NativeJavaObject) jairusunitTestSuite.call(
                         cx, scope, jairusunitTestSuite, new Object[] {file});
                 TestSuite suite = (TestSuite) obj.unwrap();
                 result.addTest(suite);
             } catch (Exception e) {
                 String msg = JairusUnit.dumpError("Error loading jairusunit.js", e);
                 result.addTest(JairusUnit.warning(msg));
             }
         } finally {
             Context.exit();
         }
         return result;
     }
 
     /** Returns a test which will fail and log a warning message. */
     public static Test warning(final String msg) {
         return new TestCase("warning") {
             protected void runTest() {
                 Assert.fail(msg);
             }
         };
     }
 
     /**
      * Dump error.
      * @param notRhinoExceptionMsg msg if throwable is not {@link RhinoException}
      * @param file javascript filename with error
      * @param throwable should be {@link Throwable}, with special handling for
      * {@link RhinoException}
      * @return formatted message with stack dump
      */
     public static String dumpError(String notRhinoExceptionMsg, Object throwable) {
         StringWriter sw = new StringWriter();
         if (throwable instanceof RhinoException) {
             RhinoException re = (RhinoException) throwable;
             String filename = "";
             if (re.sourceName() != null) {
                 filename = "\"" + re.sourceName() + "\", ";
             }
             sw.write("\n" + filename + "line " + re.lineNumber() + ": " + re.details());
             String ls = re.lineSource();
             if (ls != null) {
                 sw.write("\n" + ls + "\n");
                 for (int i = 0; i < re.columnNumber() - 1; i++) {
                     sw.write(".");
                 }
                 sw.write("^\n");
             }
             if (throwable instanceof JavaScriptException) {
                 sw.write("\n" + ((JavaScriptException) throwable).getValue().toString());
             }
         } else if (notRhinoExceptionMsg != null) {
             sw.write(notRhinoExceptionMsg);
         }
         if (throwable != null && throwable instanceof Throwable) {
             sw.write('\n');
             ((Throwable) throwable).printStackTrace(new PrintWriter(sw));
         }
         return sw.toString();
     }
 
     /**
      * Dump stacktrace to string.
      * @param t throwable to dump
      * @return dump of stacktrace
      */
     public static String dumpStackTrace(Throwable t) {
         if (t == null) { return null; }
         StringWriter sw = new StringWriter();
         t.printStackTrace(new PrintWriter(sw));
         return sw.toString();
     }
 
     /**
      * Return stacktrace string with uninteresting lines removed.
      * @param t throwable
      * @return stacktrace string with uninteresting lines removed
      */
     public static String filterStackTrace(Throwable t) {
         return filterStackTrace(dumpStackTrace(t));
     }
     /**
      * Return stacktrace string with uninteresting lines removed.
      * @param stack stacktrace dump
      * @return stacktrace string with uninteresting lines removed
      */
     public static String filterStackTrace(String stack) {
         if (stack == null) { return null; }
         StringBuilder result = new StringBuilder();
         for (String line : stack.split("\n")) {
             if (!STACK_TRACE_FILTER.matcher(line).find()) {
                 result.append(line).append("\n");
             }
         }
         return result.toString();
     }
 
     /**
      * Main.  Args include filenames with optional '-todir &;t'dir>' or '-basedir &lt;dir> included
      * at any position within files.  todir option gives directory to write
      * junit-style 'plain' and 'xml' reports, default todir is 'target/surefire-reports'.
      * basedir gives base directory for given filenames, default basedir is '/'.
      * @param args js files
      */
     public static void main(String[] args) {
         JairusUnit ju = new JairusUnit();
 
         // start debugger if -Ddebugjs
         if (System.getProperty("debugjs") != null) {
            Main main = new Main("JairusUnit Debug " + Thread.currentThread().getName());
             main.setScope(ju.scope);
             main.attachTo(ContextFactory.getGlobal());
             main.pack();
             main.setSize(960, 720);
             main.setVisible(true);
         }
 
 
         boolean failure = false;
         try {
             String todir = "target/surefire-reports";
             String basedir = "";
             int i = 0;
             while (i < args.length) {
                 String arg = args[i++];
                 if ("-todir".equals(arg)) {
                     todir = args[i++];
                     File f = new File(todir);
                     if (!f.exists()) {
                         f.mkdir();
                     }
                     continue;
                 } else if ("-basedir".equals(arg)) {
                     basedir = args[i++];
                     continue;
                 }
                 // get suite using full filepath
                 TestSuite suite = ju.jairusunitTestSuite(basedir + "/" + arg);
 
                 // we need to mimic java-style pkgname.classname style to make reports look nice
                 // strip '.js' suffix, exclude basedir, prefix with 'jairusunit.' and change slashes to dots
                 String testName = "jairusunit." + arg.replaceAll("\\.js$", "").replaceAll("/|\\\\", ".");
 
                 // always write plain and xml reports - don't bother making people choose
                 PrintStream plain = new PrintStream(new FileOutputStream(todir + "/TEST-" + testName + ".txt"), true, "UTF-8");
                 PrintStream xml = new PrintStream(new FileOutputStream(todir + "/TEST-" + testName + ".xml"), true, "UTF-8");;
                 JairusUnitResultWriter printer = new JairusUnitResultWriter(System.out, plain, xml);
                 TestResult result = new TestResult();
                 result.addListener(printer);
                 printer.startTestSuite(testName);
                 suite.run(result);
                 printer.endTestSuite(testName);
                 plain.close();
                 xml.close();
                 if (!result.wasSuccessful()) {
                     failure = true;
                 }
             }
             System.exit(failure ? FAILURE_EXIT : SUCCESS_EXIT);
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(EXCEPTION_EXIT);
         }
     }
 }
