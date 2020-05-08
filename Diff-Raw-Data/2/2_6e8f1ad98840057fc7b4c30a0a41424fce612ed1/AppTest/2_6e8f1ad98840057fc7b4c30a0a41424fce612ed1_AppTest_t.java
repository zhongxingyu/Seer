 package frans.traficLight;
 
 import enviroment.Color;
 import enviroment.Crossing;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest 
     extends TestCase
 {
     /**
      * Create the test case
      *
      * @param testName name of the test case
      */
     public AppTest( String testName )
     {
         super( testName );
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite()
     {
         return new TestSuite( AppTest.class );
     }
 
     /**
      * Rigourous Test :-)
      */
     public void testApp()
     {
     	Crossing crossing = new Crossing();
     	TraficLight traficLight = new TraficLight(crossing);
     	//TODO add test
  
     	crossing.getEastSensor().setCarDetected(false);
     	crossing.getWestSensor().setCarDetected(false);
     	crossing.getNorthSensor().setCarDetected(false);
     	crossing.getSouthSensor().setCarDetected(false);
     	
         traficLight.Execute();
         
     	assertTrue(crossing.getEastLight().isRed());
     	assertTrue(crossing.getNorthLight().isRed());
     	assertTrue(crossing.getSouthLight().isRed());
     	assertTrue(crossing.getWestLight().isRed());
     	
     	crossing.getEastSensor().setCarDetected(true);
     	traficLight.Execute();    	
     	
     	assertTrue(crossing.getEastLight().getColor() == Color.GREEN);
     	assertTrue(crossing.getNorthLight().getColor() == Color.RED);
     	assertTrue(crossing.getSouthLight().getColor() == Color.RED);
     	
    	crossing.getEastSensor().setValid(false);
     }
 }
