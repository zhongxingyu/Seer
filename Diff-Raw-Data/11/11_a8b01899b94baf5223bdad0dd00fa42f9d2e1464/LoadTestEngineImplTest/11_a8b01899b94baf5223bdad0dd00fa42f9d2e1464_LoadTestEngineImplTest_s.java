 /**
  *    This module represents an engine IMPL for the load testing framework
  *    Copyright (C) 2008  Imran M Yousuf (imran@smartitengineering.com)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.loadtest.engine.impl;
 
 import com.smartitengineering.loadtest.engine.LoadTestEngine;
 import com.smartitengineering.loadtest.engine.UnitTestInstance;
 import com.smartitengineering.loadtest.engine.events.BatchEvent;
 import com.smartitengineering.loadtest.engine.events.TestCaseBatchListener;
 import com.smartitengineering.loadtest.engine.result.TestCaseInstanceResult;
 import com.smartitengineering.loadtest.engine.result.TestCaseResult;
 import com.smartitengineering.loadtest.engine.result.TestResult;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import junit.framework.TestCase;
 
 /**
  *
  * @author imyousuf
  */
 public class LoadTestEngineImplTest
     extends TestCase {
 
     public LoadTestEngineImplTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp()
         throws Exception {
         super.setUp();
     }
 
     public void testInit() {
         LoadTestEngine engine;
         //Following is the success scenario
         engine = new LoadTestEngineImpl();
         Properties properties = new Properties();
         //Allow 4 simultaneous batch creators be in action at a time
         properties.setProperty(LoadTestEngineImpl.PROPS_PERMIT_KEY, "4");
         HashSet<UnitTestInstance> hashSet = new HashSet();
         createDataSet1(hashSet);
         final String testName = "test-1";
         engine.init(testName, hashSet, properties);
         assertEquals(LoadTestEngine.State.INITIALIZED, engine.getState());
         engine.reinstantiateCreatedState();
         engine.init(testName, hashSet, null);
         assertEquals(LoadTestEngine.State.INITIALIZED, engine.getState());
         engine.reinstantiateCreatedState();
         engine.init(testName, hashSet, new Properties());
         assertEquals(LoadTestEngine.State.INITIALIZED, engine.getState());
         engine.reinstantiateCreatedState();
         final Properties invalidProps = new Properties();
         invalidProps.setProperty(LoadTestEngineImpl.PROPS_PERMIT_KEY, "4asd");
         engine.init(testName, hashSet, invalidProps);
         assertEquals(LoadTestEngine.State.INITIALIZED, engine.getState());
 
         //Following are the failure scenarios
 
         //Scenario-1 test case name is empty
         engine.reinstantiateCreatedState();
         try {
             engine.init(null, hashSet, properties);
             fail("Initialization should not succeed! - Invalid name");
         }
         catch (IllegalArgumentException exception) {
         }
         catch (Exception exception) {
             fail(exception.getMessage());
         }
         try {
             engine.init(null, hashSet, properties);
             fail("Initialization should not succeed! - Invalid name");
         }
         catch (IllegalArgumentException exception) {
         }
         catch (Exception exception) {
             fail(exception.getMessage());
         }
 
         //Scenario-2 test case name is null/empty
         engine.reinstantiateCreatedState();
         try {
             engine.init(testName, null, properties);
             fail("Initialization should not succeed! - Invalid test instance");
         }
         catch (IllegalArgumentException exception) {
         }
         catch (Exception exception) {
             fail(exception.getMessage());
         }
         engine.reinstantiateCreatedState();
         try {
             engine.init(testName, Collections.<UnitTestInstance>emptySet(),
                 properties);
             fail("Initialization should not succeed! - Invalid test instance");
         }
         catch (IllegalArgumentException exception) {
         }
         catch (Exception exception) {
             fail(exception.getMessage());
         }
 
     }
 
     public void testStart() {
         LoadTestEngine engine;
         //Following is the success scenario
         engine = new LoadTestEngineImpl();
         Properties properties = new Properties();
         //Allow 4 simultaneous batch creators be in action at a time
         properties.setProperty(LoadTestEngineImpl.PROPS_PERMIT_KEY, "4");
         HashSet<UnitTestInstance> hashSet = new HashSet();
         createDataSet1(hashSet);
         final String testName = "test-1";
         engine.init(testName, hashSet, properties);
         final HashMap<String, Integer> instanceBatchCount =
             new HashMap<String, Integer>();
         final HashMap<String, List<Integer>> testCasesCount =
             new HashMap<String, List<Integer>>();
         final List<String> endedInstances = new ArrayList<String>();
         addBatchListener(engine, instanceBatchCount, testCasesCount,
             endedInstances);
         engine.start();
         assertEquals(LoadTestEngine.State.STARTED, engine.getState());
         long startMillis = System.currentTimeMillis();
         final int maxWaitDuration = 10000;
         waitForEngineToFinish(engine, startMillis, maxWaitDuration);
         assertEquals(LoadTestEngine.State.FINISHED, engine.getState());
         assertTrue(validateForDataSet(instanceBatchCount, testCasesCount,
             endedInstances, getDataSet1()));
         System.out.println("Status: " + engine.getState().name());
         System.out.println("Duration: " + (System.currentTimeMillis() -
             startMillis));
 
         //Failure scenarios
         engine.reinstantiateCreatedState();
         try {
             engine.start();
             fail("Engine should not have started!");
         }
         catch (IllegalStateException stateException) {
         }
         catch (Exception ex) {
             fail(ex.getMessage());
         }
     }
 
     public void testGetTestResult() {
         LoadTestEngine engine;
         //Following is the success scenario
         engine = new LoadTestEngineImpl();
         Properties properties = new Properties();
         //Allow 4 simultaneous batch creators be in action at a time
         properties.setProperty(LoadTestEngineImpl.PROPS_PERMIT_KEY, "4");
         HashSet<UnitTestInstance> hashSet = new HashSet();
         createDataSet1(hashSet);
         final String testName = "test-1";
         engine.init(testName, hashSet, properties);
         Date startDate = new Date();
         engine.start();
         long startMillis = System.currentTimeMillis();
         final int maxWaitDuration = 10000;
         waitForEngineToFinish(engine, startMillis, maxWaitDuration);
         Date endDate = new Date();
         TestResult result = engine.getTestResult();
         assertNotNull(result);
         assertTrue(validateResult(result, getDataSet1(), startDate, endDate));
 
         //Test failure scenario
         engine.reinstantiateCreatedState();
         try {
             engine.getTestResult();
             fail("TestResult should not be retrievable!");
         }
         catch (IllegalStateException stateException) {
         }
         catch (Exception exception) {
             fail(exception.getMessage());
         }
 
         engine.reinstantiateCreatedState();
         try {
             engine.init(testName, hashSet, properties);
             engine.getTestResult();
             fail("TestResult should not be retrievable!");
         }
         catch (IllegalStateException stateException) {
         }
         catch (Exception exception) {
             fail(exception.getMessage());
         }
     }
 
     public void testAddTestCaseBatchListener() {
     }
 
     public void testRemoveTestCaseBatchListener() {
     }
 
     private void addBatchListener(LoadTestEngine engine,
                                   final HashMap<String, Integer> instanceBatchCount,
                                   final HashMap<String, List<Integer>> testCasesCount,
                                   final List<String> endedInstances) {
         engine.addTestCaseBatchListener(new TestCaseBatchListener() {
 
             public synchronized void batchAvailable(BatchEvent event) {
                 String testName = event.getTestInstanceName();
                 if (instanceBatchCount.containsKey(testName)) {
                     Integer count = instanceBatchCount.get(testName);
                     int threadCount =
                         event.getBatch().getBatch().getValue().size();
                     count = count.intValue() + 1;
                     instanceBatchCount.put(testName, count);
                     List<Integer> threadCounts = testCasesCount.get(testName);
                     threadCounts.add(threadCount);
                     testCasesCount.put(testName, threadCounts);
                 }
                 else {
                     Integer count;
                     int threadCount =
                         event.getBatch().getBatch().getValue().size();
                     count = 1;
                     instanceBatchCount.put(testName, count);
                     List<Integer> threadCounts =
                         new ArrayList<Integer>();
                     threadCounts.add(threadCount);
                     testCasesCount.put(testName, threadCounts);
                 }
             }
 
             public void batchCreationEnded(BatchEvent event) {
                 endedInstances.add(event.getTestInstanceName());
             }
         });
     }
 
     private void createDataSet1(Set<UnitTestInstance> hashSet) {
         createDataSet(getDataSet1(), hashSet);
     }
 
     private void createDataSet(Set<TestData> dataSet,
                                Set<UnitTestInstance> testInstances) {
         for (TestData data : dataSet) {
             UnitTestInstance instance = createUnitTestInstance(data.instanceName,
                 data.sleepDuration, data.batchCount,
                 data.threadsPerBatch, data.batchInterval);
             testInstances.add(instance);
         }
     }
 
     private UnitTestInstance createUnitTestInstance(final String testName,
                                                     final int sleepDuration,
                                                     final int numOfBatches,
                                                     final int testCasesPerBatch,
                                                     final int batchInterval) {
         /*
         final int batchInterval = 1000;
         final int testCasesPerBatch = 3;
         final int numOfBatches = 5;
         final int sleepDuration = 100;
          */
         UnitTestInstance instance = new UnitTestInstance();
         Properties properties = new Properties();
 
         //Name the test case class
         properties.setProperty(DefaultTestCaseCreationFactory.CLASS_NAME_PROPS,
             DummyTestCase.class.getName());
 
         //Specify how long the test case would sleep
         properties.setProperty(DummyTestCase.SLEEP_TIME_PROP, Integer.toString(
             sleepDuration));
 
         //Create batches at 1s interval
         properties.setProperty(UniformDelayProvider.DELAY_PROPS,
             Integer.toString(batchInterval));
 
         //Create 5 batches
         properties.setProperty(UniformDelayProvider.STEP_COUNT_PROPS,
             Integer.toString(numOfBatches));
 
         //Create 3 test cases for each batch
         properties.setProperty(UniformStepSizeProvider.UNIT_STEP_SIZE_PROPS,
             Integer.toString(testCasesPerBatch));
 
         //Set the different class names
         instance.setInstanceFactoryClassName(DefaultTestCaseCreationFactory.class.
             getName());
         instance.setDelayTimeProviderClassName(UniformDelayProvider.class.
             getName());
         instance.setIncrementSizeProviderClassName(UniformStepSizeProvider.class.
             getName());
         instance.setProperties(properties);
         instance.setName(testName);
         return instance;
     }
 
     private Set<TestData> getDataSet1() {
         LinkedHashSet<TestData> dataSet = new LinkedHashSet<TestData>();
         String[] instanceNames = {"instance-1", "instance-2", "instance-3",
             "instance-4",
         };
         int[] batchCounts = {5, 6, 3, 3,};
         int[] threadsPerBatchCounts = {3, 4, 5, 5,};
         int[] sleepDurations = {10, 5, 20, 10};
         int[] batchIntervals = {500, 200, 500, 300};
         for (int i = 0; i < instanceNames.length; ++i) {
             TestData testData = new TestData();
             testData.instanceName = instanceNames[i];
             testData.batchCount = batchCounts[i];
             testData.threadsPerBatch = threadsPerBatchCounts[i];
             testData.sleepDuration = sleepDurations[i];
             testData.batchInterval = batchIntervals[i];
             dataSet.add(testData);
         }
         return dataSet;
     }
 
     private boolean validateForDataSet(
         final HashMap<String, Integer> instanceBatchCount,
         final HashMap<String, List<Integer>> testCasesCount,
         final List<String> endedInstances,
         final Set<TestData> dataSet) {
         boolean result = true;
         String instanceName;
         int expectedBatchCount;
         int expectedThreadsPerBatch;
         for (TestData data : dataSet) {
             instanceName = data.instanceName;
             expectedBatchCount = data.batchCount;
             expectedThreadsPerBatch = data.threadsPerBatch;
             result = result && instanceBatchCount.containsKey(instanceName) &&
                 instanceBatchCount.get(instanceName).intValue() ==
                 expectedBatchCount;
             result = result && testCasesCount.containsKey(instanceName);
             if (result) {
                 List<Integer> threadCounts = testCasesCount.get(instanceName);
                 for (Integer threadCount : threadCounts) {
                     result = result && threadCount.intValue() ==
                         expectedThreadsPerBatch;
                 }
             }
         }
         result = result && dataSet.size() == endedInstances.size();
         if (result) {
             for (TestData data : dataSet) {
                 result = result && endedInstances.contains(data.instanceName);
             }
         }
         return result;
     }
 
     private boolean validateResult(TestResult result,
                                    Set<TestData> dataSet,
                                    Date startDate,
                                    Date endDate) {
         boolean validationResult = true;
         validationResult = validationResult && startDate.before(result.
             getStartDateTime());
         validationResult = validationResult && endDate.after(result.
             getEndDateTime());
         if (validationResult) {
             for (TestCaseResult caseResult : result.getTestCaseRunResults()) {
                 //Test for existence of test instance
                 String instanceName = caseResult.getName();
                 boolean found = false;
                 TestData instanceData = null;
                 for (TestData data : dataSet) {
                     if (data.instanceName.equals(instanceName)) {
                         found = true;
                         instanceData = data;
                     }
                 }
                 validationResult = validationResult && found;
                 if (validationResult) {
                     //Check for all instances in result
                     validationResult = validationResult && caseResult.
                         getTestCaseInstanceResults().size() ==
                         instanceData.batchCount * instanceData.threadsPerBatch;
                     if (!validationResult) {
                         break;
                     }
                     //Check for unique instance number
                     Set<Integer> numbers = new HashSet<Integer>();
                     for (TestCaseInstanceResult instanceResult : caseResult.
                         getTestCaseInstanceResults()) {
                         validationResult = validationResult && !numbers.contains(
                             instanceResult.getInstanceNumber());
                         if (validationResult) {
                             numbers.add(instanceResult.getInstanceNumber());
                         }
                         else {
                             break;
                         }
                     }
                 }
                 else {
                     break;
                 }
             }
         }
         return validationResult;
     }
 
     private void waitForEngineToFinish(LoadTestEngine engine,
                                        long startMillis,
                                        final int maxWaitDuration) {
         while (!engine.getState().
             equals(LoadTestEngine.State.FINISHED) &&
             System.currentTimeMillis() - startMillis < maxWaitDuration) {
             try {
                 //Wait for the engine to finish
                 Thread.sleep(500);
             }
             catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     private class TestData {
 
         String instanceName;
         int batchCount;
         int threadsPerBatch;
         int sleepDuration;
         int batchInterval;
     }
 }
