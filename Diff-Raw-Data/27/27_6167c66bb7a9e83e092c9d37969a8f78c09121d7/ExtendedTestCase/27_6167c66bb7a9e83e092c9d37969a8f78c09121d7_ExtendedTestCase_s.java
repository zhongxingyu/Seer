 package org.jaccept.structure;
 
 import org.jaccept.TestEventManager;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Listeners;
 
 public abstract class ExtendedTestCase {
     private static final TestEventManager testEventManager = TestEventManager.getInstance();
 
    @BeforeClass
     public void registerTest() {
         testEventManager.classStart(this.getClass().getSimpleName());
     }
 
     /**
      * Used this method to add a description to a test
      *
      * @param description A general description of the purpose of this test
      */
     public void addDescription(String description) {
         testEventManager.addDescription(description);
     }
 
     /**
      * Used for adding a reference entry to an test
      */
     public void addReference(String reference) {
         testEventManager.addReference(reference);
     }
 
     /**
      * Used for adding a test step event during a test run. A step typically corresponds to 1 stimuli.
      *
      * @param stimuli        The stimuli which is added to the system.
      * @param expectedResult The measurable expected changes to the system as a consequence of the stimuli. May be empty
      *                       if this is part of preparing the state of the system for the real test.
      */
     protected void addStep(String stimuli, String expectedResult) {
         testEventManager.stepStart(stimuli, expectedResult);
     }
 
     protected void addFixture(String setupDescription) {
         testEventManager.addFixture(setupDescription);
     }
 }
