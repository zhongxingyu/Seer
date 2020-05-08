 package com.github.kpacha.jkata.potter.test;
 
 import junit.framework.TestCase;
 
 public class PotterTest extends TestCase {
 
     public void testEmptyOrder() {
 	assertEquals(0, Potter.priceFor(0));
     }
 }
