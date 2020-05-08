 package com.mycompany.app;
 
 import org.junit.Test;
 
 /**
  * Unit test for simple App.
  */
 public class FactorialTest 
 {
    /**
     * Rigourous Test :-)
     */
 	@Test(expected=IllegalArgumentException.class)
     public void noNegativeFactorial()
     {
         Factorial.factorial(-1);
     }
 }
