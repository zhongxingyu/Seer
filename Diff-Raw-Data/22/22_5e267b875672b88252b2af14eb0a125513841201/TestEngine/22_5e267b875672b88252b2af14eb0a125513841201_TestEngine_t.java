 /**
  * Copyright (c) 2004-2005, Regents of the University of California
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
 
 package cck.test;
 
 import cck.text.Status;
 import cck.text.Terminal;
 import cck.text.StringUtil;
 import cck.util.ClassMap;
 import cck.util.Util;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * The <code>AutomatedTester</code> is a class that is designed to be an in-program test facility. It is
  * capable of reading in test cases from files, extracting properties specified in those test cases, and then
  * creating a test case of the right form for the right system.
  * <p/>
  * This is done through the use of another class, the <code>TestHarness</code>, which is capable of creating
  * instances of the <code>TestCase</code> class that are collected by this framework.
  *
  * @author Ben L. Titzer
  * @see cck.test.TestCase
  * @see cck.test.TestResult
  */
 public class TestEngine {
 
     public static boolean LONG_REPORT;
     public static boolean PROGRESS_REPORT;
     public static boolean STATISTICS;
     public static int THREADS;
     public List[] results;
     public List successes;
 
     /**
      * The <code>TestHarness</code> interface encapsulates the notion of a testing harness that is capable of
      * creating the correct type of test cases given the file and a list of properties extracted from the file by
      * the automated testing framework.
      *
      * @author Ben L. Titzer
      */
     public interface Harness {
 
         /**
          * The <code>newTestCase()</code> method creates a new test case of the right type given the file name and
          * the properties already extracted from the file by the testing framework.
          *
          * @param fname the name of the file
          * @param props a list of properties extracted from the file
          * @return an instance of the <code>TestCase</code> class
          * @throws Exception if there is a problem creating the testcase or reading it
          */
         public TestCase newTestCase(String fname, Properties props) throws Exception;
     }
 
     private final ClassMap harnessMap;
     protected String[] testNames;
     protected int cursor;
 
     /**
      * The constructor for the <code>TestEngine</code> class creates a new test engine
      * with the specified class map. The class map is used to map short names in the
      * properties of test files to the class that implements the harness.
      * @param hm the class map that maps string names to harnesses
      */
     public TestEngine(ClassMap hm) {
         harnessMap = hm;
     }
 
     /**
      * The <code>runTests()</code> method runs the testing framework on each of the specified filenames. The
      * name of the target for the testcase is specified in the text of each file. The testing framework
      * extracts that name and passes that to the test harness which can create an instance of
      * <code>TestCase</code>. Each test case is then run and the results are tabulated.
      *
      * @param fnames an array of the filenames of tests to run
      * @throws java.io.IOException if there is a problem loading the test cases
      * @return true if all the tests pass
      */
     public boolean runTests(String[] fnames) throws java.io.IOException {
         // record start time
         long time = System.currentTimeMillis();
 
         // initialize the lists of tests and fields
         initTests(fnames);
 
         // run all the test cases
         runAllTests();
 
         // record end time
         time = System.currentTimeMillis() - time;
 
         // report failures
         reportFailures();
 
         // report successes
         reportSuccesses(time);
 
         // report statistics
         reportStatistics(results);
 
         // return true if all tests passed
         return successes.size() == testNames.length;
     }
 
     private void reportFailures() {
         report("Internal errors", results, TestResult.INTERNAL, testNames.length);
         report("Unexpected exceptions", results, TestResult.EXCEPTION, testNames.length);
         report("Failed", results, TestResult.FAILURE, testNames.length);
         report("Malformed test cases", results, TestResult.MALFORMED, testNames.length);
     }
 
     private void reportSuccesses(long time) {
         Terminal.printBrightGreen("Passed");
         Terminal.print(": " + successes.size());
         Terminal.print(" of " + testNames.length);
         Terminal.print(" in " + StringUtil.milliToSecs(time) +" seconds");
         Terminal.nextln();
     }
 
     private void initTests(String[] fnames) {
         Status.ENABLED = false;
         this.testNames = fnames;
         cursor = 0;
 
         results = new LinkedList[TestResult.MAX_CODE];
         for ( int cntr = 0; cntr < TestResult.MAX_CODE; cntr++ )
             results[cntr] = new LinkedList();
 
         successes = results[TestResult.SUCCESS];
     }
 
     private void runAllTests() {
         try {
             if ( THREADS > 1 ) {
                 // multi-threaded tests.
                 WorkThread[] threads = new WorkThread[THREADS];
                 for ( int cntr = 0; cntr < THREADS; cntr++ ) {
                     WorkThread thread = new WorkThread();
                     threads[cntr] = thread;
                     thread.start();
                 }
                 for ( int cntr = 0; cntr < THREADS; cntr++ ) {
                     threads[cntr].join();
                 }
             } else {
                 // single-threaded tests.
                 for ( int num = nextTest(); num >= 0; num = nextTest() ) {
                     runTest(num);
                 }
             }
         } catch (InterruptedException e) {
             throw Util.unexpected(e);
         }
     }
 
     private void runTest(int num) {
         try {
             TestCase tc = runTest(testNames[num]);
             synchronized(this) {
                 results[tc.result.code].add(tc);
             }
         } catch (IOException e) {
             throw Util.unexpected(e);
         }
     }
 
     protected int nextTest() {
         synchronized(this) {
             if ( cursor < testNames.length ) return cursor++;
             else return -1;
         }
     }
 
     private void reportStatistics(List[] tests) {
         if ( STATISTICS ) {
             for ( int cntr = 0; cntr < tests.length; cntr++ ) {
                 Iterator i = tests[cntr].iterator();
                 while (i.hasNext()) {
                     TestCase tc = (TestCase) i.next();
                     tc.reportStatistics();
                 }
             }
         }
     }
 
     private static void report(String c, List[] lists, int w, int total) {
         List list = lists[w];
         if (list.isEmpty()) return;
 
         Terminal.print(TestResult.getColor(w), c);
         Terminal.println(": " + list.size() + " of " + total);
         Iterator i = list.iterator();
         while (i.hasNext()) {
             TestCase tc = (TestCase) i.next();
             report(tc.getFileName(), tc.result);
         }
     }
 
     /**
      * The <code>report()</code> method generates a textual report of the results of running the test case.
      *
      * @param fname  the name of the file
      * @param result the result of the test
      */
     private static void report(String fname, TestResult result) {
         Terminal.print("  ");
         Terminal.printRed(fname);
         Terminal.print(": ");
         if (LONG_REPORT) result.longReport();
         else result.shortReport();
         Terminal.print("\n");
     }
 
     private TestCase runTest(String fname) throws java.io.IOException {
         TestCase tc = readTestCase(fname);
         Throwable exception = null;
 
         try {
            if ( PROGRESS_REPORT ) {
                Terminal.print("Running "+fname+"...");
                Terminal.flush();
            }
             tc.run();
         } catch (Throwable t) {
             exception = t;
         }
 
         try {
             tc.result = tc.match(exception);
         } catch (Throwable t) {
             tc.result = new TestResult.UnexpectedException("exception in match routine: ", t);
         }
        if ( PROGRESS_REPORT ) {
            if ( tc.result.isSuccess() ) Terminal.printGreen("passed");
            else Terminal.printRed("failed");
            Terminal.nextln();
        }

         return tc;
     }
 
     private TestCase readTestCase(String fname) throws java.io.IOException {
         BufferedReader r = new BufferedReader(new FileReader(fname));
         Properties vars = new Properties();
 
         while (true) {
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
 
         if (expect == null) return new TestCase.Malformed(fname, "no result specified");
         if (hname == null) return new TestCase.Malformed(fname, "no test harness specified");
 
         try {
             Harness harness = (Harness) harnessMap.getObjectOfClass(hname);
             return harness.newTestCase(fname, vars);
         } catch (Throwable t) {
             return new TestCase.InitFailure(fname, t);
         }
     }
 
     protected class WorkThread extends Thread {
         public void run() {
             for ( int num = nextTest(); num >= 0; num = nextTest() ) runTest(num);
         }
     }
 }
