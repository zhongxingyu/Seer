 package com.mycompany.testproject;
 
 import com.mycompany.testproject.Example;
 import com.mycompany.testproject.ObjectExample;
 import com.mycompany.testproject.FactorialExample;
 import com.mycompany.testproject.FunctionExample;
 import fi.lolcatz.profiler.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest {
 
     @BeforeClass
     public static void classSetup() {
         Example.main(null);
         // Used to initialize the method, creating objects for the first time causes problems with profiler
         ObjectExample.createPersons(1);
         Util.loadAgent();
         ProfileData.initialize();
     }
 
     @Before
     public void testSetup() {
         ProfileData.resetCounters();
     }
 
     @Test
     public void testExample() {
         assertEquals(3, Example.sum(1, 2));
     }
 
     @Test
     public void testRecursionCostSix() {
         FunctionExample.recursiveFunction(6);
         long totalCost = ProfileData.getTotalCost();
        assertTrue("The total cost was: " + totalCost, totalCost == 110);
     }
     
     @Test
     public void testRecursionCostIsDeterministic() {
         FunctionExample.recursiveFunction(6);
         long totalCost = ProfileData.getTotalCost();
        assertTrue("The total cost was: " + totalCost, totalCost == 110);
     }
 
     @Test
     public void testRecursionCostThree() {
         FunctionExample.recursiveFunction(3);
         long totalCost = ProfileData.getTotalCost();
         assertTrue("The total cost was: " + totalCost, 170 > totalCost && totalCost > 85);
     }
 
     @Test
     public void testIterativeCostFive() {
         FunctionExample.iterativeFunction(5);
         long totalCost = ProfileData.getTotalCost();
        assertTrue("The total cost was: " + totalCost, totalCost == 71);
     }
     
     @Test
     public void testIterativeCostIsDeterministic() {
         FunctionExample.iterativeFunction(5);
         long totalCost = ProfileData.getTotalCost();
        assertTrue("The total cost was: " + totalCost, totalCost == 71);
     }
     
     @Test
     public void testIterativeHundred() {
         FunctionExample.iterativeFunction(100);
         long totalCost = ProfileData.getTotalCost();
         assertTrue("The total cost was: " + totalCost, 400 > totalCost && totalCost > 300);
     }
 
     @Test
     public void testIterativeThreeThousandOne() {
         FunctionExample.iterativeFunction(3001);
         long totalCost = ProfileData.getTotalCost();
         assertTrue("The total cost was: " + totalCost, 600 > totalCost && totalCost > 300);
     }
 
     @Test
     public void testRecursionCostThreeThousandTwo() {
         FunctionExample.recursiveFunction(3002);
         long totalCost = ProfileData.getTotalCost();
         assertTrue("The total cost was: " + totalCost, 700 > totalCost && totalCost > 500);
     }
 
     @Test
     public void testSumOnePlusTwo() {
         Example.sum(1, 2);
         long totalCost = ProfileData.getTotalCost();
        assertTrue("The total cost was: " + totalCost, 20 > totalCost && totalCost > 9);
     }
 
     @Test
     public void testFactorialTwoThousand() {
         FactorialExample.factorialInt(2000);
         long totalCost = ProfileData.getTotalCost();
         assertTrue("The total cost was: " + totalCost, 16100 > totalCost && totalCost > 15000);
     }
 
     @Test
     public void testObjectsTen() {
         ObjectExample.createPersons(10);
         long totalCost = ProfileData.getTotalCost();
         ProfileData.printBasicBlocksCost(false);
        assertTrue("The total cost was: " + totalCost, totalCost == 210);
     }
 
     @Test
     public void testObjectsBehaveDeterministic() {
         ObjectExample.createPersons(10);
         long totalCost = ProfileData.getTotalCost();
         ProfileData.printBasicBlocksCost(false);
        assertTrue("The total cost was: " + totalCost + ", expected 211", totalCost == 210);
     }
     
     
 }
