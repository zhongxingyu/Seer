 /**
  * 
  */
 package AP2DX.test;
 
import AP2DX.AP2DXBase;
 import junit.framework.TestCase;
 
 /**
  * @author Jasper
  *
  */
 public class AP2DXBaseTest extends TestCase {
 
     public void testNothing() {
     	assertEquals(1, 1);
     }
     
     public void testConcreteClass() {
    	AP2DXBase test = new ConcreteClass();
     	assertNotNull(test);
     	
     }
     
    private class ConcreteClass extends AP2DXBase {
     	//empty
    	
     }
     
 }
