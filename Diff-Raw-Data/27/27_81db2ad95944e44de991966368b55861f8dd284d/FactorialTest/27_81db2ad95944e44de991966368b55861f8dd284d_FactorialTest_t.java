 package com.mycompany.app;
 
 import org.junit.Test;
import static org.junit.Assert.assertEquals;
 
 /**
  * Unit test for simple App.
  */
 public class FactorialTest 
 {
 	@Test(expected=IllegalArgumentException.class)
     public void noNegativeFactorial()
     {
         Factorial.factorial(-1);
     }
	
	@Test
	public void computeFactorials()
	{
		assertEquals(1, Factorial.factorial(0));
		assertEquals(1, Factorial.factorial(1));
		assertEquals(120, Factorial.factorial(6));
	}
 }
