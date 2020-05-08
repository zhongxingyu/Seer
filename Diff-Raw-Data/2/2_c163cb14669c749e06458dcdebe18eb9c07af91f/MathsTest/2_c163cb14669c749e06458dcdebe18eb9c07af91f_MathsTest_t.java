 package com.wonderbiz.lib.maths;
 
 import junit.framework.TestCase;
 
 public class MathsTest extends TestCase {
 
 	public final void testAdd() {
 		
 		Maths myMaths = new Maths(101, 2);
 		
 		assertTrue(103 == myMaths.add());
 	}
 
 	public final void testSubtract() {
 		Maths myMaths = new Maths(101, 2);
 		
 		assertTrue(99 == myMaths.subtract());
 	}
 
 	public final void testMultiply() {
 		Maths myMaths = new Maths(101, 2);
 		
 		assertTrue(202 == myMaths.multiply());
 	}
 
 	public final void testDivide() {
 		Maths myMaths = new Maths(101, 2);
 		
		assertTrue(50 == myMaths.divide());
 	}
 }
