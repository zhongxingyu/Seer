 package AP2DX.coordinator.test;
 
import java.util.ArrayList;

 import AP2DX.*;
import AP2DX.specializedMessages.ActionMotorMessage;
import AP2DX.specializedMessages.ActionMotorMessage.ActionType;
import AP2DX.specializedMessages.MotorMessage;
 import AP2DX.test.*;
 import AP2DX.coordinator.*;
 
 import org.junit.*;

 import static org.junit.Assert.*;
 
 public class CoordinatorTestCase extends AbstractTestCase
 {
     @Before
         public void before() throws Exception
         {
             test = new Program();
         }
 
 
     /**
 	 * default test method for the Program() class in the package.
 	 */
     @Override
 	@Test public void program() {
 		Program myTest = new Program();
         System.out.println("After new program");
 		assertNotNull(myTest);
         System.out.println("after assertNotNull()");
     }
    
    /**
     * default test method for the componentLogic method
     * @author Maarten de Waard
     */
    @Test public void componentLogic()
    {
    	ArrayList<AP2DXMessage> list = 
    		test.componentLogic(new MotorMessage(Module.REFLEX, Module.MOTOR, 20, 20));
        assertNotNull(list);
    }
 
    
     
     /**
      * default test method for the classMain()
      */ 
     @Override
     @Test public void classMain()
     {   
         Program myTest = new Program();
         myTest.main(new String[0]);
     }
 }
