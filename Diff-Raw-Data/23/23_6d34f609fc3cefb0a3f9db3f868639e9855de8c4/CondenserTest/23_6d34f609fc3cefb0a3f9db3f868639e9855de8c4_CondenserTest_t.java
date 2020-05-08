 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eel.seprphase2.Simulator;
 
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import eel.seprphase2.Utilities.Percentage;
 import eel.seprphase2.Utilities.Pressure;
 import eel.seprphase2.Utilities.Temperature;
 import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.hamcrest.Matchers.*;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import static eel.seprphase2.Utilities.Units.*;
 
 /**
  *
  * @author James
  */
 public class CondenserTest {
     
     public CondenserTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of waterLevel method, of class Condenser.
      */
     @Test
     public void shouldInitializeWithZeroWaterLevel() {
         
         Condenser instance = new Condenser();
         Percentage expResult = new Percentage(0);
         Percentage result = instance.waterLevel();
         assertEquals(expResult, result);
        
     }
     
     
 
     /**
      * Test of temperature method, of class Condenser.
      */
     @Test
     public void shouldInitializeAtRoomTemperature() {
 
         Condenser instance = new Condenser();
         Temperature expResult = new Temperature(350);
         Temperature result = instance.temperature();
         assertEquals(expResult, result);
 
     }
     
     @Test
     public void shouldKeepConstantTemperatureWithNoCoolantOrSteam(){
         Condenser instance = new Condenser();
         Temperature expResult = new Temperature(350);
         Temperature result = instance.temperature();
         assertEquals(expResult, result);
         for(int i=0; i<100; i++)
         {
             instance.step();
             assertEquals(expResult, result);
         }
     }
     
     @Test
     public void shouldIncreaseTemperatureWhenNoCoolantPresentAndHasSteam()
     {
         Condenser instance = new Condenser();
         instance.steamPort().pressure = new Pressure(201325);
         instance.steamPort().mass =  kilograms(5);
         instance.steamPort().temperature = kelvin(373.15);
         instance.step();  
         double previous = instance.temperature().inKelvin();
         for(int i=0; i<1000; i++)
         {
             instance.step();
             assertTrue(instance.temperature().inKelvin()>previous);
             previous = instance.temperature().inKelvin();
         }
     
     }
     
     
     
     @Test
     public void shouldDecreaseTemperatureWhenOnlyHasCoolant()
     {
         Condenser instance = new Condenser();
         instance.coolantInputPort().mass =  kilograms(5);
         instance.coolantInputPort().temperature = kelvin(285.15);
         instance.step();  
         double previous = instance.temperature().inKelvin();
         for(int i=0; i<100; i++)
         {
             instance.step();
             assertTrue(instance.temperature().inKelvin()<previous);
             previous = instance.temperature().inKelvin();
         }
     
     }
 
     /**
      * Test of pressure method, of class Condenser.
      */
     @Test
     public void shouldInitializeWith1AtmPressure() {
         Condenser instance = new Condenser();
         Pressure expResult = new Pressure(101325);
         Pressure result = instance.pressure();
         assertEquals(expResult, result);
        
     }
 
     /**
      * Test of step method, of class Condenser.
      */
     @Test
     public void shouldIncreaseWaterMassWithMoreSteam() {
         
         Condenser instance = new Condenser();
         instance.steamPort().pressure = new Pressure(201325);
         instance.steamPort().mass =  kilograms(5);
         instance.step();    
         Percentage p = instance.waterLevel();
         assertNotSame(instance.waterLevel(), new Percentage(0));
         
     }
 
     @Test
     public void shouldNotIncreaseWaterMassWithNoMoreSteam() {
         
         Condenser instance = new Condenser();
         instance.step();    
         Percentage p = instance.waterLevel();
         assertSame(instance.waterLevel().points(), new Percentage(0).points());
     }
     
     
     @Test
     public void shouldPutWaterToOutputPortWithSteam()
     {
         Condenser instance = new Condenser();
         instance.steamPort().pressure = new Pressure(201325);
         instance.steamPort().mass =  kilograms(5);
         instance.step();    
         
         assertTrue(instance.waterPort().mass.hashCode() > 0);
         
     }
     
     @Test
     public void shouldNotPutWaterToOutputPortWithNoSteam()
     {
         Condenser instance = new Condenser();
         instance.step();    
         
         assertEquals(instance.waterPort().mass, kilograms(0));
         
     }
    
    
    @Test
    public void shouldWear()
    {
        Condenser instance = new Condenser();
        
        double previous = instance.getWear().ratio();
        for(int i=0; i<100; i++)
        {
            instance.step();  
            assertTrue(instance.getWear().ratio()>previous);
            previous = instance.getWear().ratio();
        }
        
    }
     /**
      * Test of calculateWearDelta method, of class Condenser.
      
     @Test 
     public void testCalculateWearDelta() {
         
         Condenser instance = new Condenser();
         Percentage expResult = null;
         Percentage result = instance.calculateWearDelta();
         assertEquals(expResult, result);
 
     }
     * */
 }
