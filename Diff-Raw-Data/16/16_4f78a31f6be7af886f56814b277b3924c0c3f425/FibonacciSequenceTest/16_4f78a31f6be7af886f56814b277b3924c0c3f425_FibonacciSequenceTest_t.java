 package com.x1048576.kata.examples;
 
import org.junit.Assert;
 import org.junit.Test;
 
 public class FibonacciSequenceTest {
 	
 	@Test
 	public void shouldCreateNewFibonacciSequence() {
 		
 		// When
 		new FibonacciSequence();
 	}
 	
 	@Test
 	public void shouldGetFirstNumberFromFibonacciSequence() {
 		
 		// Given
 		FibonacciSequence fibonacciSequence = new FibonacciSequence();
		int expected = 1;
 		
 		// When
 		int actual = fibonacciSequence.next();
		
		// Then
		Assert.assertEquals(expected, actual);
 	}
 }
