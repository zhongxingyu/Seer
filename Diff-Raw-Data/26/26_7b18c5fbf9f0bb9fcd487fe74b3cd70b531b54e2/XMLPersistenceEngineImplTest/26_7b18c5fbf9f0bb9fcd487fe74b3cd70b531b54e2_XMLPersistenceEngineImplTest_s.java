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
 package com.smartitengineering.loadtest.engine.impl.persistence.filesystem.xml;
 
 import com.smartitengineering.loadtest.engine.LoadTestEngine;
 import com.smartitengineering.loadtest.engine.management.TestCaseBatchCreator;
 import com.smartitengineering.loadtest.engine.management.TestCaseThreadManager;
 import com.smartitengineering.loadtest.engine.management.TestCaseThreadPolicy;
 import com.smartitengineering.loadtest.engine.UnitTestInstance;
 import com.smartitengineering.loadtest.engine.events.LoadTestEngineStateChangeListener;
 import com.smartitengineering.loadtest.engine.events.TestCaseTransitionListener;
 import com.smartitengineering.loadtest.engine.impl.persistence.filesystem.AbstractFileSystemPersistenceEngine;
 import com.smartitengineering.loadtest.engine.result.KeyedInformation;
 import com.smartitengineering.loadtest.engine.result.TestCaseInstanceResult;
 import com.smartitengineering.loadtest.engine.result.TestCaseResult;
 import com.smartitengineering.loadtest.engine.result.TestProperty;
 import com.smartitengineering.loadtest.engine.result.TestResult;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import junit.framework.TestCase;
 
 /**
  *
  * @author imyousuf
  */
 public class XMLPersistenceEngineImplTest
     extends TestCase {
 
     private static LoadTestEngine loadTestEngine;
     private static Properties initProperties;
 
     public XMLPersistenceEngineImplTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp()
         throws Exception {
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
                     testResult.setId(100);
                     testResult.setStartDateTime(new Date());
                     testResult.setEndDateTime(new Date(
                         System.currentTimeMillis() + 10000));
                     testResult.setVersion(13);
                     HashSet<KeyedInformation> infos =
                         new HashSet<KeyedInformation>();
                     KeyedInformation info = new KeyedInformation();
                     info.setId(1);
                     info.setKey("Key 1");
                     info.setValue("value 1");
                     infos.add(info);
                     info = new KeyedInformation();
                     info.setId(2);
                     info.setKey("Key 2");
                     info.setValue("value 2");
                     infos.add(info);
                     info = new KeyedInformation();
                     info.setId(3);
                     info.setKey("Key 3");
                     info.setValue("value 3");
                     infos.add(info);
                     testResult.setOtherInfomations(infos);
                     HashSet<TestCaseResult> results =
                         new HashSet<TestCaseResult>();
                     TestCaseResult result = new TestCaseResult();
                     result.setId(1);
                     result.setInstanceFactoryClassName(
                         "instanceFactoryClassName");
                     result.setName("Test Case Name");
                     result.setOtherInfomations(infos);
                     HashSet<TestProperty> properties =
                         new HashSet<TestProperty>();
                     TestProperty property = new TestProperty();
                     property.setId(1);
                     property.setKey("test prop key 1");
                     property.setValue("test prop val 1");
                     properties.add(property);
                     property = new TestProperty();
                     property.setId(2);
                     property.setKey("test prop key 2");
                     property.setValue("test prop val 2");
                     properties.add(property);
                     property = new TestProperty();
                     property.setId(3);
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
                     instanceResult.setId(1);
                     instanceResult.setInstanceNumber(1);
                     instanceResult.setOtherInfomations(infos);
                     instanceResult.setStartTime(new Date());
                     instanceResults.add(instanceResult);
                     instanceResult = (TestCaseInstanceResult) instanceResult.
                         clone();
                     instanceResult.setId(2);
                     instanceResult.setInstanceNumber(2);
                     instanceResults.add(instanceResult);
                     instanceResult = (TestCaseInstanceResult) instanceResult.
                         clone();
                     instanceResult.setId(22);
                     instanceResult.setInstanceNumber(3);
                     instanceResults.add(instanceResult);
                     result.setTestCaseInstanceResults(instanceResults);
                     results.add(result);
                     result = (TestCaseResult) result.clone();
                     result.setId(2);
                     results.add(result);
                     result = (TestCaseResult) result.clone();
                     result.setId(3);
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
         if (initProperties == null) {
             try {
                 InputStream input = getClass().getClassLoader().
                     getResourceAsStream(
                     "test-config.properties");
                 Properties tempProperties = new Properties();
                 tempProperties.load(input);
                 if (tempProperties.size() > 0) {
                     initProperties = tempProperties;
                 }
             }
             catch (Exception ex) {
                 ex.printStackTrace();
             }
             finally {
                 if (initProperties == null) {
                     initProperties = new Properties();
                     initProperties.setProperty(
                         AbstractFileSystemPersistenceEngine.RESULTS_ROOT_DIRECTORY_PROP,
                         System.getProperty("user.home"));
                 }
             }
         }
     }
 
     /**
      * Test of persistTestResult method, of class XMLPersistenceEngineImpl.
      */
     public void testPersistTestResult() {
         System.out.println("persistTestResult");
         XMLPersistenceEngineImpl instance = new XMLPersistenceEngineImpl();
         instance.init(loadTestEngine, initProperties);
         boolean expResult = true;
         boolean result = instance.persistTestResult();
         assertEquals(expResult, result);
         final List<TestResult> allResults =
             instance.getPersistentTestResultEngine().getAllResults();
         System.out.println(allResults);
         assertEquals(expResult, instance.getPersistentTestResultEngine().
             deleteTestResult(allResults.get(
             0)));
     }
 }
