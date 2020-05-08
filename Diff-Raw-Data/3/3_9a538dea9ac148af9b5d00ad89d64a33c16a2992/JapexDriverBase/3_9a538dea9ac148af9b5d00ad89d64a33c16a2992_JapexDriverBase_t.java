 /*
  * Japex ver. 0.1 software ("Software")
  * 
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
  * 
  * This Software is distributed under the following terms:
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, is permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistribution in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * 
  * Neither the name of Sun Microsystems, Inc., 'Java', 'Java'-based names,
  * nor the names of contributors may be used to endorse or promote products
  * derived from this Software without specific prior written permission.
  * 
  * The Software is provided "AS IS," without a warranty of any kind. ALL
  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
  * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
  * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
  * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
  * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
  * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
  * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
  * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGES.
  * 
  * You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any
  * nuclear facility.
  */
     
 package com.sun.japex;
 
 import java.io.File;
 import java.lang.management.*;
 
 public class JapexDriverBase implements JapexDriver, Params {
     
     private Driver _driver;    
     private TestSuiteImpl _testSuite;    
     private TestCaseImpl _testCase;
     
     private boolean _needWarmup = true;
     
     private MemoryMXBean _memoryBean;
     private long _heapBytes;
     
     public JapexDriverBase() {
         _memoryBean = ManagementFactory.getMemoryMXBean();
         _heapBytes = 0L;
     }
     
     public void setDriver(Driver driver) {
         _driver = driver;
     }
     
     public void setTestSuite(TestSuiteImpl testSuite) {
         _testSuite = testSuite;
     }
     
     public void setTestCase(TestCaseImpl testCase) {
         _testCase = testCase;
        _needWarmup = true;
     }
     
     protected TestSuiteImpl getTestSuite() {
         return _testSuite;
     }
     
     // -- Internal interface ---------------------------------------------
     
     /**
      * Execute prepare phase. Even in multi-threaded test, this
      * method will only be executed in single-threaded mode, so there's no
      * need for additional synchronization.
      */
     public void prepare() {
         long millis, nanos;
         TestCaseImpl tc = _testCase;
         
         nanos = Util.currentTimeNanos();
         prepare(tc);
         nanos = Util.currentTimeNanos() - nanos;
         tc.setDoubleParam(Constants.ACTUAL_PREPARE_TIME, 
                           Util.nanosToMillis(nanos));
     }
     
     /**
      * Execute the warmup phase. This method can be executed concurrently
      * by multiple threads. Care should be taken to ensure proper
      * synchronization. Note that parameter getters and setters are
      * already synchronized.
      */
     public void warmup() {
         long millis;
         TestCaseImpl tc = _testCase;
         
         // Get number of threads to adjust iterations
         int nOfThreads = tc.getIntParam(Constants.NUMBER_OF_THREADS);
         
         int warmupIterations = 0;
         String warmupTime = tc.getParam(Constants.WARMUP_TIME);
         if (warmupTime != null) {
             // Calculate end time
             long startTime = millis = Util.currentTimeMillis();
             long endTime = startTime + Util.parseDuration(warmupTime);
 
             while (endTime > millis) {
                 warmup(tc);      // Call warmup
                 warmupIterations++;
                 millis = Util.currentTimeMillis();
             } 
         }
         else {
             // Adjust warmup iterations based on number of threads
             warmupIterations = tc.getIntParam(Constants.WARMUP_ITERATIONS) / nOfThreads;
             
             for (int i = 0; i < warmupIterations; i++) {
                 warmup(tc);      // Call warmup
             }
         }
         
         // Accumulate actual number of iterations
         synchronized (tc) {
             int actualWarmupIterations =  
                 tc.hasParam(Constants.ACTUAL_WARMUP_ITERATIONS) ? 
                     tc.getIntParam(Constants.ACTUAL_WARMUP_ITERATIONS) : 0;
             tc.setIntParam(Constants.ACTUAL_WARMUP_ITERATIONS, 
                            actualWarmupIterations + warmupIterations);
         }
     }
     
     /**
      * Execute the run phase. This method can be executed concurrently
      * by multiple threads. Care should be taken to ensure proper
      * synchronization. Note that parameter getters and setters are
      * already synchronized.
      */
     public void run() {
         long millis;
         TestCaseImpl tc = _testCase;
         
         // Force GC and record current memory usage
         _memoryBean.gc();
         _heapBytes = _memoryBean.getHeapMemoryUsage().getUsed();
         
         // Get number of threads to adjust iterations
         int nOfThreads = tc.getIntParam(Constants.NUMBER_OF_THREADS);
         
         int runIterations = 0;
         String runTime = tc.getParam(Constants.RUN_TIME);
         if (runTime != null) {
             // Calculate end time
             long startTime = Util.currentTimeMillis();
             long endTime = startTime + Util.parseDuration(runTime);
 
             // Run phase
             do {
                 run(tc);      // Call run
                 runIterations++;
                 millis = Util.currentTimeMillis();
             } while (endTime >= millis);
         }
         else {
             // Adjust runIterations based on number of threads
             runIterations = tc.getIntParam(Constants.RUN_ITERATIONS) / nOfThreads;
 
             // Run phase
             for (int i = 0; i < runIterations; i++) {
                 run(tc);      // Call run
             }
         }
         
         // Accumulate actual number of iterations
         synchronized (tc) {
             int actualRunIterations =  
                 tc.hasParam(Constants.ACTUAL_RUN_ITERATIONS) ? 
                     tc.getIntParam(Constants.ACTUAL_RUN_ITERATIONS) : 0;
             tc.setIntParam(Constants.ACTUAL_RUN_ITERATIONS, 
                            actualRunIterations + runIterations);
         }
     }
     
     /**
      * Called exactly once after calling run. Computes japex.resultValue
      * based on global param japex.resultUnit. Only three possible values
      * are recognized: "tps" (default), "ms" (latency in millis) and 
      * "mbps" (which requires setting japex.inputFile). If no errors are
      * found calls finish(testCase) on the driver.
      */
     public void finish() {        
         // Call finish(testCase) in user's driver
         finish(_testCase);
 
         // If result value has been computed then we're done
         if (_testCase.hasParam(Constants.RESULT_VALUE)) {
             return;
         }
         
         String resultUnit = getTestSuite().getParam(Constants.RESULT_UNIT);
         
         // Check to see if a different result unit was set
         if (resultUnit == null || resultUnit.equalsIgnoreCase("tps")) {
             // Default - computed elsewhere
         }
         else if (resultUnit.equalsIgnoreCase("ms")) {
             _testCase.setParam(Constants.RESULT_UNIT, "ms");
 
             _testCase.setDoubleParam(Constants.RESULT_VALUE, 
                 _testCase.getDoubleParam(Constants.ACTUAL_RUN_TIME) /
                 _testCase.getDoubleParam(Constants.ACTUAL_RUN_ITERATIONS));                            
         }
         else if (resultUnit.equalsIgnoreCase("mbps")) {
             // Check if japex.inputFile was defined
             String inputFile = _testCase.getParam(Constants.INPUT_FILE);            
             if (inputFile == null) {
                 throw new RuntimeException("Unable to compute japex.resultValue " + 
                     " because japex.inputFile is not defined or refers to an illegal path.");
             }
 
             // Length of input file
             long fileSize = new File(inputFile).length();
             
             // Calculate Mbps
             _testCase.setParam(Constants.RESULT_UNIT, "Mbps");
             _testCase.setDoubleParam(Constants.RESULT_VALUE,
                 (fileSize * 0.000008d 
                     * _testCase.getLongParam(Constants.ACTUAL_RUN_ITERATIONS)) /    // Mbits
                 (_testCase.getLongParam(Constants.ACTUAL_RUN_TIME) / 1000.0));      // Seconds
         }
         else if (resultUnit.equalsIgnoreCase("mbs")) {
             _testCase.setParam(Constants.RESULT_UNIT, "MBs");
             _testCase.setDoubleParam(Constants.RESULT_VALUE, 
                 (_memoryBean.getHeapMemoryUsage().getUsed() - _heapBytes) / 
                 (1024.0 * 1024.0));     // Megabytes used
         }
         else {
             throw new RuntimeException("Unknown value '" + 
                 resultUnit + "' for global param japex.resultUnit.");
         }
         
     }
     
     // -- Callable interface ------------------------------------------
 
     /**
      * Concurrently execute the warmup phase the first time it is 
      * called, and the run phase the second time it is called.
      * Care should be taken to ensure proper synchronization. Note 
      * that parameter getters and setters are already synchronized.
      */
     public Object call() {
         if (_needWarmup) {
             warmup(); 
             _needWarmup = false;
         }
         else {
             run();
         }
         return null;
     }    
     
     // -- Params interface -----------------------------------------------
     
     public boolean hasParam(String name) {
         return _driver.hasParam(name);
     }
     
     public void setParam(String name, String value) {
         _driver.setParam(name, value);
     }
     
     public String getParam(String name) {
         return _driver.getParam(name);
     }
        
     public void setBooleanParam(String name, boolean value) {
         _driver.setBooleanParam(name, value);
     }
     
     public boolean getBooleanParam(String name) {
         return _driver.getBooleanParam(name);
     }
     
     public void setIntParam(String name, int value) {
         _driver.setIntParam(name, value);
     }    
     
     public int getIntParam(String name) {
         return _driver.getIntParam(name);
     }
     
     public void setLongParam(String name, long value) {
         _driver.setLongParam(name, value);
     }    
     
     public long getLongParam(String name) {
         return _driver.getLongParam(name);
     }
     
     public void setDoubleParam(String name, double value) {
         _driver.setDoubleParam(name, value);
     }
     
     public double getDoubleParam(String name) {
         return _driver.getDoubleParam(name);
     }
     
     // -- JapexDriver interface ------------------------------------------
     
     /**
      * Called once when the class is loaded.
      */
     public void initializeDriver() {
     }
     
     /**
      * Called exactly once for every test, before calling warmup.
      */
     public void prepare(TestCase testCase) {
     }
     
     /**
      * Called once or more for every test, before calling run. Default 
      * implementation is to call run().
      */
     public void warmup(TestCase testCase) {   
         run(testCase);
     }
     
     /**
      * Called once or more for every test to obtain perf data.
      */
     public void run(TestCase testCase) {
     }
     
     /**
      * Called exactly once after calling run. 
      */
     public void finish(TestCase testCase) {
     }
     
     /**
      * Called after all tests are completed.
      */
     public void terminateDriver() {
     }
 
 }
