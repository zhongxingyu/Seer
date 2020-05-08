 package com.github.kpacha.jkata.fizzBuzz.test;
 
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 
 public class FizzBuzzTest extends TestCase {
 
     public void testOne() {
 	assertEquals(Arrays.asList(1), FizzBuzz.generate(1));
     }
 }
