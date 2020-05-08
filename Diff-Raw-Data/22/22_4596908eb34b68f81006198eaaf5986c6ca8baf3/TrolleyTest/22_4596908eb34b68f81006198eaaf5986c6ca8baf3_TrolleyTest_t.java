 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package TrolleyRegistration;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author andreasmosti
  */
 public class TrolleyTest {
     Trolley instance; 
     
     public TrolleyTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {
         instance = new Trolley(1, 250); 
     }
     
     @After
     public void tearDown() {
         instance = null;
     }
 
     /**
      * Test of getTrolleyNr method, of class Trolley.
      */
     @Test
     public void testGetTrolleyNr() {
         int expResult = 1;
        int result = instance.getTrolleyId();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
       
     }
 
     /**
      * Test of getOwnWeight method, of class Trolley.
      */
     @Test
     public void testGetOwnWeight() {
         int expResult = 250;
         int result = instance.getOwnWeight();
         
         assertEquals(expResult, result);
         
        
     }
 
     /**
      * Test of getPayLoad method, of class Trolley.
      */
     @Test
     public void testSetAndGetPayLoad() {
         int totalWeight = 350;
         int addedPayload = 100; 
         
         instance.setTotalWeight(totalWeight);
         
         int result = instance.getPayLoad();
        
         assertEquals(addedPayload, result);
        
       
     }
 
     /**
      * Test of getTotalWeight method, of class Trolley.
      */
     @Test
     public void testGetTotalWeight() {
         int expected = 550; 
         
         instance.setTotalWeight(expected);
         int result = instance.getTotalWeight();
         
         assertEquals(expected, result);
     }
 
     /**
      * Test of toString method, of class Trolley.
      */
     @Test
     public void testToString() {
         String expResult = "Vogn 1 Egenvekt: 250 Lastvekt: 0 Totalvekt: 250";
         String result = instance.toString();
         assertEquals(expResult, result);
     }
 }
