 package com.comphenix.xp;
 
 import static org.junit.Assert.*;
 
 import java.util.Random;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class RangeTest {
 
	private static final int REPEAT_COUNT = 10000;
 	
 	private static Random rnd;
 	
 	@BeforeClass
     public static void oneTimeSetUp() {
         // Initialize random source
 		rnd = new Random();
     }
 	
 	@Test
 	public void testRandom() {
 	
 		// Generate a random range
 		int start = rnd.nextInt(10);
 		int stop = start + 1 + rnd.nextInt(20);
 		Range range = new Range(start, stop);
 		
 		int[] results = SampleValues(range, REPEAT_COUNT);
 		
 		double average = getAverage(results);
 		double std = getStandardDeviation(average, results) / average;
 		
 		if (std > 0.1) {
 			fail(String.format("Standard deviation (%f) is greater than 10 procent.", std));
 		}
 	}
 	
 	@Test
 	public void testSpecific() {
 	
 		Range range = new Range(0.1, 0.1);
 		int[] results = SampleValues(range, REPEAT_COUNT);
 		
 		// "0" should occur approximately 90%.
 		double rate = results[0] / (double) REPEAT_COUNT;
 
 		if (Math.abs(rate - 0.9) > 0.1)
 			fail("The number 0 didn't occur 90% in the range 0 - 0.1");
 	}
 	
 	@Test
 	public void testUnbiased() {
 
 		Range range = new Range(0.1, 0.5);
		int[] results = SampleValues(range, REPEAT_COUNT);
 		
 		// The expected value (average)
 		double expected = (range.getStart() + range.getEnd()) / 2.0;
		double average = getAverage(results) / (double)REPEAT_COUNT;
 
 		if (Math.abs(expected - average) > 0.1) {
 			fail("Expected value differs too much.");
 		}
 	}
 	
 	private int[] SampleValues(Range range, int count) {
 		
 		int start = range.getMinimum();
 		int stop = range.getMaximum();
 		
 		// Storage of results (stop is inclusive)
 		int[] results = new int[stop - start + 1];
 
 		for (int i = 0; i < count; i++) {
 			int value = range.sampleInt(rnd);
 			
 			if (value >= start && value <= stop)
 				results[value - start]++;
 			else
 				fail(String.format("Outside of range [%d, %d] with value %d", start, stop, value));
 		}
 		
 		return results;
 	}
 	
 	private double getAverage(int[] numbers) {
 		
 		double sum = 0;
 		
 		// Find total sum
 		for (int i = 0; i < numbers.length; i++) {
 			sum += numbers[i];
 		}
 		
 		return sum / (double)numbers.length;
 	}
 	
 	private double getStandardDeviation(double average, int[] numbers) {
 		
 		double sum = 0;
 		
 		// Sum of squares
 		for (int i = 0; i < numbers.length; i++) {
 			sum += (numbers[i] - average) * (numbers[i] - average);
 		}
 		
 		return Math.sqrt(sum / numbers.length);
 	}
 }
