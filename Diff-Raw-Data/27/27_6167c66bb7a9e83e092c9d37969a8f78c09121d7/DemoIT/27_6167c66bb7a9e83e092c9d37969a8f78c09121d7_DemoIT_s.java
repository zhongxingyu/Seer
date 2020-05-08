 package org.jaccept.examples;
 
 import org.jaccept.TestEventManager;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.annotations.Test;
 
 import static org.testng.AssertJUnit.assertEquals;
 import static org.testng.AssertJUnit.fail;
 
 public class DemoIT extends ExtendedTestCase {
     ExampleComponentWrapper c2t = new ExampleComponentWrapper(new ExampleComponent());
 
    @Test
     public void testFirstMethod() {
         addDescription("Test level description test 1");
 
         addStep("Create a component2Test instance", "");
         c2t = new ExampleComponentWrapper(new ExampleComponent());
 
         addStep("Call the setListener method with a testlistener",
                 "Testlistener should be set on component2Test");
         c2t.setListener(new ComponentLister() {
             public void firstMethodCalled(String param) {
                 TestEventManager.getInstance().addResult("TestListener called with param " + param);
             }
         });
         addStep("Call the c2t with the string 'alpha'",
                 "The testlistener should be notified of the call and the string 'returnValue' should be returned");
         assertEquals("Wrong return value", "returnValue", c2t.firstMethod("alpha"));
 
         addStep("Call the c2t with the string 'beta'",
                 "The testlistener should be notified of the call again and the string 'returnValue' should be still be returned");
         assertEquals("Wrong return value", "returnValue", c2t.firstMethod("beta"));
     }
 
     @Test
     public void testFirstMethodNull() {
         addDescription("Verify that the component2Test handles null parameters correctly");
         addReference("-------------");
 
         addStep("Create a component2Test instance", "");
         c2t = new ExampleComponentWrapper(new ExampleComponent());
 
         addStep("Call the setListener method with a testlistener",
                 "Testlistener should be set on component2Test");
         c2t.setListener(new ComponentLister() {
             public void firstMethodCalled(String param) {
                 TestEventManager.getInstance().addResult("TestListener called with param " + param);
             }
         });
 
         addStep("Call the c2t with the null",
                 "The testlistener should be notified of the call and the string 'returnValue' should be returned");
         assertEquals("Wrong return value", "returnValue", c2t.firstMethod(null));
 
         addStep("Here is an example of a test failure", "The active test event logger should output an informative " +
                 "representation of the failure.");
         //fail("This is an example of a failure message.");
     }
 }
