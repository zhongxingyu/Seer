 /**
  * 
  */
 package AP2DX.planner.test;
 
 //import junit.framework.Assert;
 //import junit.framework.TestCase;
 
 import mockit.Expectations;
 import mockit.Mock;
 import mockit.MockClass;
 import mockit.MockUp;
 import mockit.Mockit;
 import static mockit.Deencapsulation.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import AP2DX.*;
 import AP2DX.planner.*;
 
 /**
  * @author Jasper Timmer
  */
 public class PlannerTestCase {
 
     private Program test;
 	
     @BeforeClass 
         public static void beforeClass() throws Exception {
             System.out.println("Before setUpMocks");
             Mockit.setUpMocks(FakeBase.class);
             Mockit.setUpMocks(FakeAP2DXMessage.class);
             System.out.println("After SetUpMocks");
         }
 
     @Before
         public void before() throws Exception
         {
             test = new Program();
         }
 
 
     /**
 	 * Test method for {@link AP2DX.planner.Program#Program()}.
 	 */
 	@Test public void program() {
 		Program myTest = new Program();
         System.out.println("After new program");
 		assertNotNull(myTest);
         System.out.println("after assertNotNull()");
 
 	}
 
 
     /**
      * Test method for {@link AP2DX.planner.Program#componentLogic()}
      * @author Maarten de Waard
      */
     @Test public void componentLogic()
     {
         test.componentLogic(new AP2DXMessage("Hoi", Module.TEST));
     }
 
 
     @Test public void override()
     {   
         invoke(test, "doOverride");
         //test.doOverride();
     }
 
    @test public void plannerMain()
     {
        test.main();
     }
 
 	/**
 	 * This is the mocked AP2DXBase.
 	 * 
 	 * @author Jasper
 	 * 
 	 */
 	@MockClass(realClass = AP2DXBase.class)
 	public static final class FakeBase {		
 		/**
 		 * Override the constructor of AP2DXBase to do nothing.
 		 */
 		@Mock
 		public void $init(Module module) {
             System.out.printf("Module: %s\n", module);
 			//instead of connecting and configuring, do nothing
 		}
 	}
     
     /**
      * Mock the message class, so we won't need it
      * @author Maarten de Waard
      */
     @MockClass(realClass = AP2DXMessage.class)
     public static final class FakeAP2DXMessage 
     {
         @Mock
         public void $init(String in, Module origin)
         {
             System.out.printf("In: %s, Module: %s", in, origin);
         }
 
         @Mock
         public void parseMessage()
         {
             System.out.println("parsing message");
         }
 
     }
 
 }
 
 
 
 
