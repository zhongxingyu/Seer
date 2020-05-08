 /**
  * Copyright (c) 2004, Regents of the University of California
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package avrora.test;
 
 import avrora.util.StringUtil;
 import avrora.util.Terminal;
 import avrora.util.Verbose;
 import avrora.util.ClassMap;
 import avrora.AVRTestHarness;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * The <code>AutomatedTester</code> is a class that is designed to be an
  * in-program test facility. It is capable of reading in test cases from
  * files, extracting properties specified in those test cases, and then
  * creating a test case of the right form for the right system.
  * <p/>
  * This is done through the use of another class, the <code>TestHarness</code>,
  * which is capable of creating instances of the <code>TestCase</code> class
  * that are collected by this framework.
  *
  * @see TestHarness
  * @see TestCase
  * @see TestResult
  *
  * @author Ben L. Titzer
  */
 public class AutomatedTester {
 
     private final ClassMap harnessMap;
 
     private final Verbose.Printer printer = Verbose.getVerbosePrinter("test");
 
     /**
      * The constructor for the <code>AutomatedTester</code> class accepts
      * an instance of <code>TestHarness</code> that is used to create the
      * specific test cases for each file name.
      */
     public AutomatedTester() {
         harnessMap = new ClassMap("Test Harness", TestHarness.class);
         harnessMap.addClass("simulator", AVRTestHarness.class);
     }
 
     private class TestPair {
         final TestCase testcase;
         final TestResult result;
 
         TestPair(TestCase tc, TestResult tr) {
             testcase = tc;
             result = tr;
         }
     }
 
     /**
      * The <code>runTests()</code> method runs the testing framework on each
      * of the specified filenames. The name of the target for the testcase is
      * specified in the text of each file. The testing framework extracts that
      * name and passes that to the test harness which can create an instance of
      * <code>TestCase</code>. Each test case is then run and the results are
      * tabulated.
      * @param fnames an array of the filenames of tests to run
      * @throws java.io.IOException if there is a problem loading the test cases
      */
     public void runTests(String[] fnames) throws java.io.IOException {
         List slist = new LinkedList();
         List flist = new LinkedList();
         List ilist = new LinkedList();
         List ulist = new LinkedList();
         List mlist = new LinkedList();
 
         for (int cntr = 0; cntr < fnames.length; cntr++) {
             printer.println("Running test " + StringUtil.quote(fnames[cntr]) + "...");
 
             String fname = fnames[cntr];
             TestPair pair = runTest(fname);
             TestResult result = pair.result;
 
             if (result.isSuccess())
                 slist.add(pair);
             else if (result.isInternalError())
                 ilist.add(pair);
             else if (result.isUnexpectedException())
                 ulist.add(pair);
             else if (result.isMalformed())
                 mlist.add(pair);
             else
                 flist.add(pair);
         }
 
         Terminal.printBrightGreen("Test successes");
         Terminal.println(": " + slist.size() + " of " + fnames.length);
 
         report(ilist, "Internal errors", Terminal.COLOR_YELLOW, fnames.length);
         report(ulist, "Unexpected exceptions", Terminal.COLOR_YELLOW, fnames.length);
         report(flist, "Failures", Terminal.COLOR_RED, fnames.length);
         report(mlist, "Malformed test cases", Terminal.COLOR_CYAN, fnames.length);
 
         // return 0 if all tests were successful, 1 otherwise
         if ( slist.size() == fnames.length )
             System.exit(0);
         else
             System.exit(1);
     }
 
     private static void report(List l, String c, int color, int total) {
         if (l.isEmpty()) return;
 
         Terminal.print(color, c);
         Terminal.println(": " + l.size() + " of " + total);
         Iterator i = l.iterator();
         while (i.hasNext()) {
             TestPair p = (TestPair) i.next();
             report(p.testcase.getFileName(), p.result);
         }
     }
 
     /**
      * The <code>report()</code> method generates a textual report of
      * the results of running the test case.
      * @param fname the name of the file
      * @param result the result of the test
      */
     private static void report(String fname, TestResult result) {
         Terminal.print("  ");
         Terminal.printRed(fname);
         Terminal.print(": ");
         result.shortReport();
         Terminal.print("\n");
     }
 
     private TestPair runTest(String fname) throws java.io.IOException {
         TestCase tc = readTestCase(fname);
         Throwable exception = null;
 
         try {
             tc.run();
         } catch (Throwable t) {
             exception = t;
         }
 
         return new TestPair(tc, tc.match(exception));
     }
 
     private TestCase readTestCase(String fname) throws java.io.IOException {
         BufferedReader r = new BufferedReader(new FileReader(fname));
         Properties vars = new Properties();
 
         while (true) {
             // TODO: could use some cleanup with StringUtil
 
             String buffer = r.readLine();
             if (buffer == null) break;
 
             int index = buffer.indexOf("@");
             if (index < 0) break;
 
             int index2 = buffer.indexOf(":");
             if (index2 < 0) break;
 
             String var = buffer.substring(index + 1, index2).trim();
             String val = buffer.substring(index2 + 1).trim();
 
             vars.put(var, val);
         }
 
         r.close();
 
         String expect = vars.getProperty("Result");
         String hname = vars.getProperty("Harness");
 
         if (expect == null)
             return new TestCase.Malformed(fname, "no result specified");
         if (hname == null )
             return new TestCase.Malformed(fname, "no test harness specified");
 
         try {
             TestHarness harness = (TestHarness)harnessMap.getObjectOfClass(hname);
             return harness.newTestCase(fname, vars);
         } catch (Throwable t) {
             t.printStackTrace();
             // TODO: better error messages for failure to find test harness
             return new TestCase.Malformed(fname, "exception in test case initialization");
         }
     }
 
 }
