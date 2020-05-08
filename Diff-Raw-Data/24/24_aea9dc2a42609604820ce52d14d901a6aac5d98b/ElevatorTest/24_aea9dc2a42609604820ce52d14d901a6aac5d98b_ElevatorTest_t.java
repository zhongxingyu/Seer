 package com.se350.app;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for Elevator class.
  */
 public class ElevatorTest {
 
     /**
      * tests the constructor.
      *
      */
     @Test
     public void testConstructor() {
         Elevator elevatorOne = new Elevator( 1 );
         Elevator elevatorTwo = new Elevator( 2 );
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorNegativeNumber() {
         Elevator elevator = new Elevator( -1 );
         Assert.fail();
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorZeroNumber() {
         Elevator elevator = new Elevator( 0 );
         Assert.fail();
     }
 
 //   @Test
 //   public void testSomething() {
 //       Assert.assertTrue( false );
 //   }
 }
