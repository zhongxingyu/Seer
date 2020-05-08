 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import dapij.InstanceCreationTracker;
 import org.junit.Assert;
 import org.junit.Test;
 
 
 /**
  *
  * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
  */
 public class TestSingleThread {
 
     @Test
     public void testObjectCreationDetection() {
         HelloAzura ha = new HelloAzura(2);  /* create object to test agent */
         
         InstanceCreationTracker map = InstanceCreationTracker.INSTANCE;
        //Assert.assertEquals("Size: ", 0, map.getSize());
         
         /* Does not work if agent not supplied as a command line argument */
         Assert.assertEquals("Exists in map: ", true, map.hasKey(ha));
     }
 }
