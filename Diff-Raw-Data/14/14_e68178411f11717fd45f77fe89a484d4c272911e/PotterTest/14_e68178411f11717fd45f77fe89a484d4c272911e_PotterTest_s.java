 package com.github.kpacha.jkata.potter.test;
 
 import junit.framework.TestCase;
 
 import com.github.kpacha.jkata.potter.Potter;
 
 public class PotterTest extends TestCase {
 
     public void testEmptyOrder() {
	assertEquals(0, Potter.priceFor(0));
     }
 
     public void testSingleBookOrder() {
	assertEquals(8, Potter.priceFor(1));
     }
 
     public void testTwoBookOrder() {
 	assertEquals(8 * 2 * 0.95, Potter.priceFor(2));
     }
 }
