 /**
  *    This module represents an engine for the load testing framework
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
 package com.smartitengineering.loadtest.engine;
 
 import com.smartitengineering.loadtest.engine.events.LoadTestEngineStateChangeListener;
 import com.smartitengineering.loadtest.engine.events.TestCaseTransitionListener;
 import com.smartitengineering.loadtest.engine.management.TestCaseBatchCreator;
 import com.smartitengineering.loadtest.engine.management.TestCaseThreadManager;
 import com.smartitengineering.loadtest.engine.result.TestResult;
 import java.util.Date;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * This is the central engine that will control the flow of the executing the
  * tests and finally forming and returning the test result. After initialization
  * and before inoking other operations. The test creator class should be
  * provided to the engine so that it can delegate test instance creation to it.
  * Thread manager should be provided so that it can manage all threads created
  * by the engine. Once all information are available start operations should be
  * used to start the testing. In order to reuse the engine please use the
 * reinstantiateCreatedState operation. Test result is available only once the
  * test engine finishes its testing operation. Use the appropriate listeners to
  * get updates on test engine's state and its test cases states.
  * @author imyousuf
  */
 public interface LoadTestEngine {
 
     /**
      * Initializes the test engine with the test instances. Once initialized its
      * ready to be started. Please note that to invoke start, init must be
      * invoked earlier.
      * 
      * @param testName Name of the corresponding test result
      * @param testInstances A collection of instances that this engine will
      *                      execute
      * @param initProperties Properties to be used initialization
      * @throws IllegalArgumentException If the testName is null or no unit
      *                                  instances specified and/or depends on
      *                                  the specification of the implementor
      * @throws IllegalStateException If engine is not in created state
      *                                  or implementations specific
      */
     public void init(String testName,
                      Set<UnitTestInstance> testInstances,
                      Properties initProperties)
         throws IllegalArgumentException,
                IllegalStateException;
 
     /**
      * Returns the current state of the engine.
      * 
      * @return Current state of the engine
      */
     public LoadTestEngine.State getState();
 
     /**
      * This method will set the status ot CREATED and ensure that the engine can
      * be reused for another test. It will ensure that the same engine can be
      * used to either repeast the test or perform new set of tests.
      */
    public void reinstantiateCreatedState();
 
     /**
      * Starts the engine; that is it starts executing the test instances as they
      * are supposed to be configured. Once started the engine should be in
      * STARTED state and it can not be stopped. Depending on the thread policy
      * supplied to the engine, one can decide to terminate threads and suspend
      * the test instance.
      * 
      * @throws java.lang.IllegalStateException If not in INITIALIZED state
      */
     public void start()
         throws IllegalStateException;
 
     /**
      * Datetime stamp of commencing the tests.
      * 
      * @return Start time
      * @throws java.lang.IllegalStateException If the engine state is not (or
      *                                           after) STARTED
      */
     public Date getStartTime()
         throws IllegalStateException;
 
     /**
      * Datetime stamp of the ending of the tests.
      * 
      * @return End time
      * @throws java.lang.IllegalStateException If the engine state is not
      *                                          FINISHED
      */
     public Date getEndTime()
         throws IllegalStateException;
 
     /**
      * Returns the duration of the test once it is finished.
      * 
      * @return Duration of the testing
      * @throws java.lang.IllegalStateException If the engine state is not
      *                                          FINISHED
      */
     public long getDuration()
         throws IllegalStateException;
 
     /**
      * Returns the result of the test executed by the test engine
      * 
      * @return The result representing the test executed by the engine
      * @throws java.lang.IllegalStateException If the engine state is not
      *                                          FINISHED
      */
     public TestResult getTestResult()
         throws IllegalStateException;
 
     /**
      * Adds state change listener if listener is not null
      * 
      * @param listener Listener to add
      */
     public void addLoadTestEngineStateChangeListener(
         LoadTestEngineStateChangeListener listener);
 
     /**
      * Remmoves the state change listener iff it exists
      * 
      * @param listener Listener to remove
      */
     public void removeLoadTestEngineStateChangeListener(
         LoadTestEngineStateChangeListener listener);
 
     /**
      * Adds test cases' state transition listener, which will invoke on
      * particular states of the test cases'.
      * 
      * @param listener Listener to add
      */
     public void addTestCaseTransitionListener(
         TestCaseTransitionListener listener);
 
     /**
      * Removes the state transition listener iff it exists
      * 
      * @param listener Listener to remove
      */
     public void removeTestCaseTransitionListener(
         TestCaseTransitionListener listener);
 
     /**
      * Sets the batch creator class that is responsible for creating batches of
      * test case and thread tor execute. Please note that the batch creator
      * must have no-args constructor.
      *
      * @param batchCreator
      */
     public void setTestCaseBatchCreator(
         Class<? extends TestCaseBatchCreator> batchCreator);
 
     /**
      * Sets the fully qualified class for the batch creator responsible for
      * craeting test case batches. 
      * 
      * @param batchCreator The fully qualified batch creator class name
      * @throws java.lang.IllegalArgumentException If string is not a valid batch
      *                                            creator
      */
     public void setTestCaseBatchCreator(String batchCreator)
         throws IllegalArgumentException;
 
     /**
      * Retreive the batch creator class for this test case engine. It is to be
      * noted that if creator is not set before initialize is called then it will
      * be simply ignored and default batch creator might be returned.
      * @return Batch creator class
      */
     public Class<? extends TestCaseBatchCreator> getTestCaseBatchCreator();
 
     /**
      * Sets the thread manager for this test engine.
      * @param threadManager Manager for test case threads
      */
     public void setTestCaseThreadManager(TestCaseThreadManager threadManager);
 
     /**
      * Returns the manager for this test engine. It is to be noted that if
      * manager is not set before initialize is called then it will be simply
      * ignored and default manager might be returned.
      * @return Manager for managing threads
      */
     public TestCaseThreadManager getTestCaseThreadManager();
 
     /**
      * Enum for representing the state of this load test engine
      */
     public enum State {
 
         /**
          * Representing the created state
          */
         CREATED(1),
         /**
          * Representing the initialized state after the init method is invoked
          */
         INITIALIZED(2),
         /**
          * Representing the started state once start is invoked
          */
         STARTED(3),
         /**
          * Representing the finished state once the engine is done
          */
         FINISHED(4);
         /**
          * At what step of the life-cycle is the current state located
          */
         private int stateStep;
 
         State(int stateStep) {
             this.stateStep = stateStep;
         }
 
         /**
          * Get the state's step level
          * @return The step level
          */
         public int getStateStep() {
             return stateStep;
         }
     }
 }
