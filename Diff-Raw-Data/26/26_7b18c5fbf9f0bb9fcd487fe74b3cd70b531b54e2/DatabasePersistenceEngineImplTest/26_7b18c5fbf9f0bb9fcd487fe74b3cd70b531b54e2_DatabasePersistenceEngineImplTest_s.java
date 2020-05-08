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
 package com.smartitengineering.loadtest.engine.impl.persistence.db;
 
 import com.smartitengineering.loadtest.engine.LoadTestEngine;
 import com.smartitengineering.loadtest.engine.management.TestCaseBatchCreator;
 import com.smartitengineering.loadtest.engine.management.TestCaseThreadManager;
 import com.smartitengineering.loadtest.engine.management.TestCaseThreadPolicy;
 import com.smartitengineering.loadtest.engine.UnitTestInstance;
 import com.smartitengineering.loadtest.engine.events.LoadTestEngineStateChangeListener;
 import com.smartitengineering.loadtest.engine.events.TestCaseTransitionListener;
 import com.smartitengineering.loadtest.engine.persistence.PersistenceEngine;
 import com.smartitengineering.loadtest.engine.persistence.PersistentTestResultEngine;
 import com.smartitengineering.loadtest.engine.result.KeyedInformation;
 import com.smartitengineering.loadtest.engine.result.TestCaseInstanceResult;
 import com.smartitengineering.loadtest.engine.result.TestCaseResult;
 import com.smartitengineering.loadtest.engine.result.TestProperty;
 import com.smartitengineering.loadtest.engine.result.TestResult;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import junit.framework.TestCase;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  *
  * @author imyousuf
  */
 public class DatabasePersistenceEngineImplTest
     extends TestCase {
 
     private static LoadTestEngine loadTestEngine;
     private static PersistenceEngine dbImpl;
 
     public DatabasePersistenceEngineImplTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp()
         throws Exception {
         super.setUp();
         if (loadTestEngine == null) {
             loadTestEngine = new LoadTestEngine() {
 
                 public void init(String testName,
                                  Set<UnitTestInstance> testInstances,
                                  Properties initProperties)
                     throws IllegalArgumentException {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public State getState() {
                     return LoadTestEngine.State.FINISHED;
                 }
 
                public void reinstanteCreatedState() {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void start()
                     throws IllegalStateException {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public Date getStartTime() {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public Date getEndTime() {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public long getDuration()
                     throws IllegalStateException {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public TestResult getTestResult()
                     throws IllegalStateException {
                     TestResult testResult = new TestResult();
                     testResult.setTestName("Test Name");
                     testResult.setStartDateTime(new Date());
                     testResult.setEndDateTime(new Date(
                         System.currentTimeMillis() + 10000));
                     HashSet<KeyedInformation> infos =
                         new HashSet<KeyedInformation>();
                     KeyedInformation info = new KeyedInformation();
                     info.setKey("Key 1");
                     info.setValue("value 1");
                     infos.add(info);
                     info = new KeyedInformation();
                     info.setKey("Key 2");
                     info.setValue("value 2");
                     infos.add(info);
                     info = new KeyedInformation();
                     info.setKey("Key 3");
                     info.setValue("value 3");
                     infos.add(info);
                     testResult.setOtherInfomations(infos);
                     HashSet<TestCaseResult> results =
                         new HashSet<TestCaseResult>();
                     TestCaseResult result = new TestCaseResult();
                     result.setInstanceFactoryClassName(
                         "instanceFactoryClassName");
                     result.setName("Test Case Name");
                     result.setOtherInfomations(infos);
                     HashSet<TestProperty> properties =
                         new HashSet<TestProperty>();
                     TestProperty property = new TestProperty();
                     property.setKey("test prop key 1");
                     property.setValue("test prop val 1");
                     properties.add(property);
                     property = new TestProperty();
                     property.setKey("test prop key 2");
                     property.setValue("test prop val 2");
                     properties.add(property);
                     property = new TestProperty();
                     property.setKey("test prop key 3");
                     property.setValue("test prop val 3");
                     properties.add(property);
                     result.setTestProperties(properties);
                     HashSet<TestCaseInstanceResult> instanceResults =
                         new HashSet<TestCaseInstanceResult>();
                     TestCaseInstanceResult instanceResult =
                         new TestCaseInstanceResult();
                     instanceResult.setEndTestCaseState(
                         com.smartitengineering.loadtest.engine.TestCase.State.FINISHED);
                     instanceResult.setEndTime(new Date(
                         System.currentTimeMillis() + 10000));
                     instanceResult.setInstanceNumber(1);
                     instanceResult.setOtherInfomations(infos);
                     instanceResult.setStartTime(new Date());
                     instanceResults.add(instanceResult);
                     instanceResult = (TestCaseInstanceResult) instanceResult.
                         clone();
                     instanceResult.setInstanceNumber(2);
                     instanceResults.add(instanceResult);
                     instanceResult = (TestCaseInstanceResult) instanceResult.
                         clone();
                     instanceResult.setInstanceNumber(3);
                     instanceResults.add(instanceResult);
                     result.setTestCaseInstanceResults(instanceResults);
                     results.add(result);
                     result = (TestCaseResult) result.clone();
                     result.setName(result.getName() + " 1");
                     results.add(result);
                     result = (TestCaseResult) result.clone();
                     result.setName(result.getName() + " 2");
                     results.add(result);
                     testResult.setTestCaseRunResults(results);
                     return testResult;
                 }
 
                 public void setTestCaseThreadPolicy(TestCaseThreadPolicy policy) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public TestCaseThreadPolicy getTestCaseThreadPolicy() {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void unsetTestCaseThreadPolicy() {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void addLoadTestEngineStateChangeListener(
                     LoadTestEngineStateChangeListener listener) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void removeLoadTestEngineStateChangeListener(
                     LoadTestEngineStateChangeListener listener) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void addTestCaseTransitionListener(
                     TestCaseTransitionListener listener) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void removeTestCaseTransitionListener(
                     TestCaseTransitionListener listener) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void setTestCaseBatchCreator(Class<? extends TestCaseBatchCreator> batchCreator) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void setTestCaseBatchCreator(String batchCreator)
                     throws IllegalArgumentException {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public Class<? extends TestCaseBatchCreator> getTestCaseBatchCreator() {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public void setTestCaseThreadManager(TestCaseThreadManager threadManager) {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
 
                 public TestCaseThreadManager getTestCaseThreadManager() {
                     throw new UnsupportedOperationException("Not supported yet.");
                 }
             };
         }
         if (dbImpl == null) {
             System.setProperty(
                 "com.smartitengineering.loadtest.engine.persistence.db.props_file",
                 "classpath:db-test-config.properties");
             ApplicationContext context =
                 new ClassPathXmlApplicationContext(
                 "com/smartitengineering/loadtest/engine/impl/persistence/db/db-beanfactory.xml");
             dbImpl = (PersistenceEngine) context.getBean("persistenceEngine");
         }
     }
 
     /**
      * Test of doPersist method, of class DatabasePersistenceEngineImpl.
      */
     public void testDoPersist() {
         System.out.println("----------- START ------------");
         dbImpl.init(loadTestEngine, new Properties());
         assertEquals(true, dbImpl.persistTestResult());
         System.out.println("------------ END -------------");
 
     }
 
     public void testGetAll() {
         System.out.println("----------- START ------------");
         PersistentTestResultEngine engine =
             dbImpl.getPersistentTestResultEngine();
         List<TestResult> allResults = engine.getAllResults();
         System.out.println("------------ END -------------");
         if(allResults.isEmpty()) {
             fail("All can not be null!");
         }
     }
 
     public void testGetTestResultByName() {
         System.out.println("----------- START ------------");
         PersistentTestResultEngine engine =
             dbImpl.getPersistentTestResultEngine();
         List<TestResult> allResults = engine.getAllForTestName("Test Name");
         System.out.println("------------ END -------------");
         if(allResults.isEmpty()) {
             fail("Get by name can not be null!");
         }
     }
     
     public void testGetTestResultById() {
         System.out.println("----------- START ------------");
         PersistentTestResultEngine engine =
             dbImpl.getPersistentTestResultEngine();
         TestResult result = engine.getTestResultById(1);
         System.out.println("------------ END -------------");
         if(result == null) {
             fail("Get by id can not be null!");
         }
     }
     
     public void testGetTestResultsByDateRange() {
         System.out.println("----------- START ------------");
         PersistentTestResultEngine engine =
             dbImpl.getPersistentTestResultEngine();
         List<TestResult> results = engine.getAllResultWithinDateRange(null, null);
         System.out.println("------------ END -------------");
         if(results.isEmpty()) {
             fail("Get by name can not be null!");
         }
     }
     
     public void testGetTestResultByDateRangeAndName() {
         System.out.println("----------- START ------------");
         PersistentTestResultEngine engine =
             dbImpl.getPersistentTestResultEngine();
         List<TestResult> results = engine.getAllResultWithinDateRange("Test ", null, null);
         System.out.println("------------ END -------------");
         if(results.isEmpty()) {
             fail("Get by name can not be null!");
         }
     }
 
     public void testDeleteTestResult() {
         System.out.println("----------- START ------------");
         PersistentTestResultEngine engine =
             dbImpl.getPersistentTestResultEngine();
         List<TestResult> allResults = engine.getAllResults();
         for (TestResult testResult : allResults) {
             assertEquals(true, dbImpl.getPersistentTestResultEngine().deleteTestResult(testResult));
         }
         System.out.println("------------ END -------------");
     }
 }
