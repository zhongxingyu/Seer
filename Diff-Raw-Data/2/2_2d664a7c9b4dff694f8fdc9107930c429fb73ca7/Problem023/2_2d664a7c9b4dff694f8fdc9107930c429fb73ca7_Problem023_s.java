 package com.github.johnnyo.euler;
 
 import java.util.*;
 
 import com.github.johnnyo.euler.util.PrimeFactors;
 
 /**
  * Find the sum of all the positive integers which cannot be written as the sum of two abundant numbers
  * 
  * @author JohnnyO
  * 
  */
 public class Problem023 extends BaseTestCase {
 
 	private static final int UPPER_LIMIT = 28123;
 
 	public String getAnswer() {
		return "X";
 	}
 
 	@Override
 	public String solve() {
 		// Step One: Generate a list of all the abundant numbers below our upper limit
 		List<Integer> abundantNumbers = new ArrayList<Integer>();
 		for (int i = 1; i <= UPPER_LIMIT; i++) {
 			Set<Integer> divisors = new PrimeFactors(i).getProperDivisors();
 			int sum = 0;
 			for (Integer x : divisors)
 				sum += x;
 			if (sum > i)
 				abundantNumbers.add(i);
 		}
 
 		// Step Two: Generate a list of all integers for 1 to upper limit
 		Set<Integer> allNumbers = new HashSet<Integer>();
 		for (int i = 0; i <= UPPER_LIMIT; i++)
 			allNumbers.add(i);
 
 		// Step Three: Remove from that list all the combinations of abundant numbers (this could be more efficient)
 		for (int i=0; i < abundantNumbers.size(); i++) {
 			for (int j=i; j < abundantNumbers.size(); j++) {
 				allNumbers.remove(abundantNumbers.get(i) + abundantNumbers.get(j));
 			}
 		}
 		
 		//Step Four : Sum what's left
 		int sum = 0;
 		for (Integer x : allNumbers) {
 			sum +=x;
 		}
 		return Integer.toString(sum);
 
 	}
 
 }
