 package com.sirma.itt.javacourse.intro;
 
 
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 /**
  * Testing the functionallity of the arrays utility class.
  */
 public class ArraysUtilTest {
 
 	/**
 	 * Test if the method throws the proper excpetion when we input a null
 	 * array.
 	 */
 	@Test(expected = NullPointerException.class)
	public final void testEmptyMinElement() {
 		ArraysUtil.getMinElement(null);
	}
	
	/**
	 * Test if the method throws the proper excpetion when we input a null
	 * array.
	 */
	@Test(expected = NullPointerException.class)
	public final void testEmptySum() {
 		ArraysUtil.getSum(null);
 	}
 
 	/**
 	 * Testing the smallest elemet method.
 	 */
 	@Test
 	public final void testSmallestElement() {
 		int[] arr = { 1, 2, 3, 4, 5, 6 };
 		assertTrue(ArraysUtil.getMinElement(arr) == 1);
 	}
 
 	/**
 	 * Testing the sum method.
 	 */
 	@Test
 	public final void testArraySum() {
 		int[] arr = { 2, 2, 2 };
 		assertTrue(ArraysUtil.getSum(arr) == 6);
 	}
 
 	/**
 	 * Testing the print method.
 	 */
 	@Test
 	public final void testPrinting() {
 		int[] arr = { 0, 2, 3, 4, 5, 6, 7, 8 };
 		System.out.print("Testing ArraysUtil.printArray()... ");
 		ArraysUtil.print(arr);
 	}
 
 }
