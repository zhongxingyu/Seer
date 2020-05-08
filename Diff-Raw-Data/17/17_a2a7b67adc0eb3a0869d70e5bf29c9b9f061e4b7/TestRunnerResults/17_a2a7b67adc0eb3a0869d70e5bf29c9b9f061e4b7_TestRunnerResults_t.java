 /*
 * $Id: TestRunnerResults.java,v 1.4 2003-04-10 19:56:48 o_rossmueller Exp $
  */
 package org.junitee.runner;
 
 
 import java.util.*;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.Test;
 
 
 /**
  * @author <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
 * @version $Revision: 1.4 $
  * @since 1.5
  */
 public class TestRunnerResults implements TestRunnerListener {
 
   private long timestamp;
   private ArrayList suiteInfo = new ArrayList();
   private HashMap suites = new HashMap();
   private TestInfo currentInfo;
   private boolean failure = false;
   private boolean singleTest = false;
   private ArrayList errorMessages = new ArrayList();
   private boolean filterTrace = true;
   private boolean finished = false;
   private boolean stopped = false;
 
 
   public void start(boolean singleTest) {
     setSingleTest(singleTest);
   }
 
 
   public synchronized void setStopped() {
     stopped = true;
   }
 
 
   public synchronized boolean isStopped() {
     return stopped;
   }
 
 
   public synchronized void finish() {
     finished = true;
   };
 
 
   public synchronized boolean isFinished() {
     return finished;
   }
 
 
   public synchronized void addError(Test test, Throwable t) {
    boolean preRunError = false;

    if (getCurrentInfo() == null) {
      setTimestamp(System.currentTimeMillis());
      setCurrentInfo(new TestInfo(test));
      preRunError = true;
    }
     getCurrentInfo().setError(t);
     setFailure(true);
    if (preRunError) {
      endTest(test);
    }
   }
 
 
   public synchronized void addFailure(Test test, Throwable t) {
     getCurrentInfo().setFailure(t);
     setFailure(true);
   }
 
 
   public synchronized void addFailure(Test test, AssertionFailedError t) {
     getCurrentInfo().setFailure(t);
     setFailure(true);
   }
 
 
   public synchronized void endTest(Test test) {
     long elapsedTime = System.currentTimeMillis() - getTimestamp();
 
     getCurrentInfo().setElapsedTime(elapsedTime);
     addToSuite(getCurrentInfo());
     setCurrentInfo(null);
   }
 
 
   public synchronized void startTest(Test test) {
     setCurrentInfo(new TestInfo(test));
     setTimestamp(System.currentTimeMillis());
   }
 
 
   public synchronized void runFailed(String message) {
     errorMessages.add(message);
   }
 
 
   public synchronized long getTimestamp() {
     return timestamp;
   }
 
 
   public synchronized void setTimestamp(long timestamp) {
     this.timestamp = timestamp;
   }
 
 
   public synchronized List getSuiteInfo() {
     return (List)suiteInfo.clone();
   }
 
 
   public synchronized void setSuiteInfo(List suiteInfo) {
     this.suiteInfo = new ArrayList(suiteInfo);
   }
 
 
   public synchronized TestInfo getCurrentInfo() {
     return currentInfo;
   }
 
 
   public synchronized void setCurrentInfo(TestInfo currentInfo) {
     this.currentInfo = currentInfo;
   }
 
 
   public synchronized boolean isFailure() {
     return failure;
   }
 
 
   public synchronized void setFailure(boolean failure) {
     this.failure = failure;
   }
 
 
   public synchronized boolean isFilterTrace() {
     return filterTrace;
   }
 
 
   public synchronized void setFilterTrace(boolean filterTrace) {
     this.filterTrace = filterTrace;
   }
 
 
   public synchronized boolean isSingleTest() {
     return singleTest;
   }
 
 
   public synchronized void setSingleTest(boolean singleTest) {
     this.singleTest = singleTest;
   }
 
 
   public synchronized List getErrorMessages() {
     return errorMessages;
   }
 
 
   protected synchronized void addToSuite(TestInfo info) {
     String className = info.getTest().getClass().getName();
     TestSuiteInfo suite = (TestSuiteInfo)suites.get(className);
 
     if (suite == null) {
       suite = new TestSuiteInfo(className);
       suites.put(className, suite);
       suiteInfo.add(suite);
     }
     suite.add(info);
   }
 }
