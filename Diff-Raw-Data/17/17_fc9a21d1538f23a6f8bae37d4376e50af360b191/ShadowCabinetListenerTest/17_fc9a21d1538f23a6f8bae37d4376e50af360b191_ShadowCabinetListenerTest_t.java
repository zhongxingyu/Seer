 package com.thoughtworks.shadow;
 
 import com.thoughtworks.shadow.tests.Success;
 import com.thoughtworks.turtlemock.Turtle;
 import com.thoughtworks.turtlemock.internal.ProxyTypeMock;
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 
 public class ShadowCabinetListenerTest extends TestCase {
     private ProxyTypeMock listener;
     private ShadowCabinet cabinet;
     private ComparableTestShadow testShadow;
 
     protected void setUp() throws Exception {
         listener = Turtle.mock(ShadowCabinetListener.class);
         cabinet = new ShadowCabinet();
         cabinet.addListener((ShadowCabinetListener) listener.mockTarget());
 
         testShadow = new ComparableTestShadow(new Success());
     }
 
    public void testShouldNotRemoveTestThenAddContainedTestShadow() throws Exception {
        cabinet.setMaxSize(1);
        cabinet.add(testShadow);
        cabinet.add(testShadow);
        listener.assertNotDid("afterRemoveTest");
    }

     public void testRemoveListener() throws Exception {
         cabinet.removeListener((ShadowCabinetListener) listener.mockTarget());
         cabinet.add(testShadow);
         cabinet.action(new TestResult());
         listener.assertNotDid("startAction");
     }
 
     public void testShouldFireStartAndThenEndActionEventWhenAction() throws Exception {
         cabinet.add(testShadow);
         cabinet.action(new TestResult());
         listener.assertDid("startAction").then("endAction");
     }
 
     public void testShouldFireAfterAddTestEventWhenAddTestIntoCabinet() throws Exception {
         cabinet.add(testShadow);
         listener.assertDid("afterAddTest").with(testShadow);
     }
 
     public void testShouldFireAfterRemoveTestEventWhenRemoveTestFromCabinet() throws Exception {
         cabinet.add(testShadow);
         cabinet.remove(testShadow);
         listener.assertDid("afterRemoveTest").with(testShadow);
     }
 
     public void testShouldNotFireAfterAddTestEventIfTestExists() throws Exception {
         cabinet.add(testShadow);
         listener.assertDid("afterAddTest").with(testShadow);
         cabinet.add(testShadow);
         listener.assertNotDid("afterAddTest");
     }
 
     public void testShouldNotFireAfterRemoveTestEventIfTestDoesNotExist() throws Exception {
         cabinet.remove(testShadow);
         listener.assertNotDid("afterRemoveTest");
     }
 
     public void testShouldFireRemoveEventAndAddEventIfTestAddedReplacesOtherOne() throws Exception {
         cabinet.setMaxSize(1);
         cabinet.add(testShadow);
         ComparableTestShadow newTest = new ComparableTestShadow(new Success());
         cabinet.add(newTest);
         listener.assertDid("afterRemoveTest").with(testShadow);
         listener.assertDid("afterAddTest").with(newTest);
     }
 }
