 package no.ntnu;
 
 public class Factorial {
 	public static int factorial(int value) {
 		int result = 1;
		for(int i = value; i > 2; i--) {
 			result = Multiplication.multiply(result, i);
 		}
 		return result;
 	}
 }
