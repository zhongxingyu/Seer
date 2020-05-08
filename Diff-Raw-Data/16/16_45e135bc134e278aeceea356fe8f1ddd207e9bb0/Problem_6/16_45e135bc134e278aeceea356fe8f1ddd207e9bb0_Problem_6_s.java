 package org.amm.euler.p100;
 
 /**
  * 
  * The sum of the squares of the first ten natural numbers is,
  * 
 *         12 + 22 + ... + 102 = 385 The square of the sum of the first ten
 *         natural numbers is,
  * 
 *         (1 + 2 + ... + 10)2 = 552 = 3025 Hence the difference between the sum
 *         of the squares of the first ten natural numbers and the square of the
 *         sum is 3025 385 = 2640.
  * 
 *         Find the difference between the sum of the squares of the first one
 *         hundred natural numbers and the square of the sum.
  */
 public class Problem_6 {
 	
 	public static long solution_0() {
 		int sum = 0;
 		int sum2 = 0;
 		for (int i = 1; i <= 100; ++i) {
 			sum += i;
 			sum2 += i * i;
 		}
 		return sum * sum - sum2;
 	}
 
 	public static long solution_1() {
 		int limit = 100;
 		long sum = limit * (limit + 1) / 2;
 		long sum2 = (2 * limit + 1) * (limit + 1) * limit / 6;
 		return sum * sum - sum2;
 	}
 	
 }
