 package com.sirma.itt.javacourse.intro;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 /**
  * Testing the functionallity of findNodNok.
  */
 public class NokNodCalculatorTest {
 
 	/**
 	 * We except Illegal argument exception when we input zeros.
 	 */
 	@Test (expected = IllegalArgumentException.class)
	public void testNodZeros() {
 		System.out.println(NokNodCalculator.findNod(0, 0));
	}
	
	/**
	 * We except Illegal argument exception when we input zeros.
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testNokZeros() {
 		System.out.println(NokNodCalculator.findNok(0, 0));
 	}
 	
 	/**
 	 * Testing the Nod with different input values.
 	 */
 	@Test
 	public void testNod() {
 		// the biggest number that a and b can be divided by without a remainder
 		assertEquals(1, NokNodCalculator.findNod(3, 5));
 		assertEquals(4, NokNodCalculator.findNod(8, 12));
 	}
 	
 	/**
 	 * Testing the Nok with different input values.
 	 */
 	@Test
 	public void testNok() {
 		// the smallest number that can be divided by a and b simultaneously
 		assertTrue(NokNodCalculator.findNok(4, 6) == 12);
 		assertTrue(NokNodCalculator.findNok(3, 2) == 6);
 	}
 }
