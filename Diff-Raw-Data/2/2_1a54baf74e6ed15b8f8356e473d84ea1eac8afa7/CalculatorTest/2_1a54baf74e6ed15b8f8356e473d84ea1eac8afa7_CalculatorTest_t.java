 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author marcelo
  */
 public class CalculatorTest {
     
     public CalculatorTest() {
         
         
         /**
          * 
          */
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
 
     /**
      * Test of soma method, of class Calculator.
      */
     @Test
     public void testSoma() {
         System.out.println("soma");
         int a = 10;
         int b = 5;
         Calculator instance = new Calculator();
        int expResult = 15;
         int result = instance.soma(a, b);
         assertEquals(expResult, result);
     }
 }
