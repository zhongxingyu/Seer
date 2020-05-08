 package com.github.renszarv.algobase;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class FactorialTest {
 
 	@Test
 	public void test() {
 		Factorials factorials = Factorials.getInstance();
 		assertEquals(2, factorials.nth(2).intValue());
 		assertEquals(6, factorials.nth(3).intValue());
 		assertEquals(24, factorials.nth(4).intValue());
 		assertEquals(120, factorials.nth(5).intValue());
 		assertEquals(720, factorials.nth(6).intValue());
 		assertEquals(5040, factorials.nth(7).intValue());
 	}
 
	@Test
	public void testZeroAndOne() {
		Factorials factorials = Factorials.getInstance();
		assertEquals(1, factorials.nth(0).intValue());
		assertEquals(1, factorials.nth(1).intValue());
	}
 }
