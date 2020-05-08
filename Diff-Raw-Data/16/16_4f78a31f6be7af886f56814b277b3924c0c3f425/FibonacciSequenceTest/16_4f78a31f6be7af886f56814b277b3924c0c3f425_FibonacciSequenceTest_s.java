 package com.x1048576.kata.examples;
 
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
 		
 		// When
 		int actual = fibonacciSequence.next();
 	}
 }
