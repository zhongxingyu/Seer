 package jumble.util;
 
 import java.util.Collection;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Tests the RTSI class. Extremely low jumble score so this needs more work.
  * 
  * @author Tin Pavlinic
  * @version $Revision$
  */
 public class RTSITest extends TestCase {
   public void testRTSI() throws Exception {
     Collection c = RTSI.find("jumble.util", "jumble.util.Command");
     assertEquals(2, c.size());
     assertTrue(c.contains("jumble.util.LightOff"));
     assertTrue(c.contains("jumble.util.DoorClose"));
   }
 
   public void testJarRTSI() throws Exception {
    // The bcel jar is pretty much guaranteed to be on the classpath
    Collection c = RTSI.find("org.apache.bcel.generic", "org.apache.bcel.generic.Instruction");
 
    assertTrue(c.contains("org.apache.bcel.generic.IADD"));
   }
 
   public void testGetAllVisiblePackages() {
     Collection c = RTSI.getAllVisiblePackages();
     assertTrue(c.contains("jumble"));
     assertTrue(c.contains("jumble.dependency"));
     assertTrue(c.contains("jumble.fast"));
     assertTrue(c.contains("jumble.util"));
     assertTrue(c.contains("org.apache.bcel"));
     assertTrue(c.contains("junit"));
     assertTrue(c.contains("org.apache"));
     assertTrue(c.contains("junit.framework"));
     assertTrue(c.contains("experiments"));
   }
   
   public static Test suite() {
     TestSuite suite = new TestSuite(RTSITest.class);
     return suite;
   }
   
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 }
