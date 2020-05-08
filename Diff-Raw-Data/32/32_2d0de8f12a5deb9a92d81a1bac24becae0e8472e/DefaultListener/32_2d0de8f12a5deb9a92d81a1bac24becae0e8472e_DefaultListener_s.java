 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.output;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 import com.google.jstestdriver.FileResult;
 import com.google.jstestdriver.TestResult;
 import com.google.jstestdriver.TestRunResult;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.PrintStream;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 @Singleton
 public class DefaultListener implements TestResultListener {
 
   static final String NEW_LINE = System.getProperty("line.separator");
 
   private static final Logger logger = LoggerFactory.getLogger(DefaultListener.class);
 
   private final PrintStream out;
 
   private final AtomicBoolean finished = new AtomicBoolean(false);
   private final boolean verbose;
   private final Map<String, TestRunResult> browsersRunData = new ConcurrentHashMap<String, TestRunResult>();
   private final AtomicInteger lineColumn = new AtomicInteger(0);
   private final AtomicInteger totalPasses = new AtomicInteger(0);
   private final AtomicInteger totalFails = new AtomicInteger(0);
   private final AtomicInteger totalErrors = new AtomicInteger(0);
 
   private int lineLength = 70;
 
   @Inject
   public DefaultListener(@Named("outputStream") PrintStream out,
                          @Named("verbose") boolean verbose) {
     this.out = out;
     this.verbose = verbose;
   }
 
   public void setLineLength(int lineLength) {
     this.lineLength = lineLength;
   }
 
   public void finish() {
     // TODO(corysmith): Remove when refactoring the action to produce results objects
     // that can be composited into a full result.
     // right now it can be called multiple times. :P
     if (!finished.getAndSet(true)) {
       printSummary(out);
       for (Map.Entry<String, TestRunResult> entry : browsersRunData.entrySet()) {
         TestRunResult data = entry.getValue();
   
         printBrowserSummary(out, entry.getKey(), data);
   
         for (Problem problem : data.getProblems()) {
           problem.print(out, verbose);
         }
       }
     }
   }
 
   private void printSummary(PrintStream out) {
     if (!verbose) {
       out.println();
     }
     out.println(String.format("Total %d tests (Passed: %d; Fails: %d; Errors: %d) (%.2f ms)",
         (totalPasses.get() + totalFails.get() + totalErrors.get()), totalPasses.get(),
         totalFails.get(), totalErrors.get(), findMaxTime()));
   }
 
   private void printBrowserSummary(PrintStream out, String browser, TestRunResult data) {
     out.println(String.format("  %s: Run %d tests (Passed: %d; Fails: %d; Errors %d) (%.2f ms)",
         browser, (data.getPassed() + data.getFailed() + data.getErrors()),
         data.getPassed(), data.getFailed(), data.getErrors(), data.getTotalTime()));
   }
 
 
   private float findMaxTime() {
     float max = 0f;
 
     for (TestRunResult data : browsersRunData.values()) {
       max = Math.max(data.getTotalTime(), max);
     }
     return max;
   }
 
   public void onTestComplete(TestResult testResult) {
     String browser = testResult.getBrowserInfo().toString();
     TestRunResult runData = currentRunData(browser);
     TestResult.Result result = testResult.getResult();
     String log = testResult.getLog();
 
     runData.addTime(testResult.getTime());
     if (result == TestResult.Result.passed) {
       if (!verbose) {
         out.print('.');
         if (log.length() > 0) {
           runData.addProblem(new TestResultProblem(testResult));
         }
       } else {
         printInProgress(browser, "[PASSED] ", testResult, log);
       }
       runData.addPass();
       totalPasses.incrementAndGet();
     } else if (result == TestResult.Result.failed) {
       if (!verbose) {
         out.print('F');
       } else {
         printInProgress(browser, "[FAILED] ", testResult, log);
       }
       runData.addFail();
       runData.addProblem(new TestResultProblem(testResult));
       totalFails.incrementAndGet();
     } else if (result == TestResult.Result.error) {
       if (!verbose) {
         out.print('E');
       } else {
         printInProgress(browser, "[ERROR] ", testResult, log);
       }
       runData.addError();
       runData.addProblem(new TestResultProblem(testResult));
       totalErrors.incrementAndGet();
     } else {
       out.print("<" + result + ">");
       if (verbose) {
         out.println(" " + testResult.getTestCaseName() + "." + testResult.getTestName());
         if (log.length() > 0) {
           String[] logLines = log.split("\n");
 
           for (String line : logLines) {
             out.println("  " + line);
           }
         }
       }
       runData.addProblem(new TestResultProblem(testResult));
     }
     if (lineColumn.incrementAndGet() == lineLength) {
       out.println();
       lineColumn.set(0);
     }
   }
 
   private void printInProgress(String browser, String type, TestResult testResult, String log) {
     out.println(browser + " " + type + testResult.getTestCaseName() + "." + testResult.getTestName());
     if (log.length() > 0) {
       String[] logLines = log.split("\n");
 
       for (String line : logLines) {
         out.println("  " + line);
       }
     }
   }
 
   private synchronized TestRunResult currentRunData(String browser) {
     TestRunResult runData = browsersRunData.get(browser);
 
     if (runData == null) {
       runData = new TestRunResult();
       browsersRunData.put(browser, runData);
     }
     return runData;
   }
 
   public void onFileLoad(String browser, FileResult fileResult) {
    logger.debug("loaded {}", fileResult.toString());
     if (fileResult.isSuccess()) {
       if (verbose) {
         out.println(browser + " loaded " + fileResult.getFileSource().getFileSrc());
       }
       return;
     }
     TestRunResult runData = currentRunData(browser);
     runData.addError();
     runData.addProblem(new FileLoadProblem(fileResult));
     if (verbose) {
       out.println("[ERROR] " + fileResult.getMessage());
     }
   }
 }
