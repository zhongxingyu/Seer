 package kinsey.jim.euler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Problem3 {
 
 	// The prime factors of 13195 are 5, 7, 13 and 29.
 	//
 	// What is the largest prime factor of the number 600851475143 ?
 	
 	public static void main(String[] args) {
 		System.out.println(largestPrimeFactor(600851475143l));
 	}
 
 	public static long largestPrimeFactor(long number) {
 		if (number <= 1) {
 			return 0;
 		}
 		return lastObject(primeFactors(number));
 	}
 	
 	public static List<Long> primeFactors(long number) {
 		List<Long> factors = new ArrayList<Long>();
 		 
 		for (long i = 2; i <= number; i++) {
 			if (isPrime(i) && isMultipleOf(number, i)) {
 				factors.add(i);
 				long factor = number / i;
 				if (isPrime(factor)) {
 					factors.add(factor);
 				}
 				else {
 					factors.addAll(primeFactors(factor));
 				}
 				break;
 			}
 		}
 		
 		return factors;
 	}
 	
 	private static boolean isMultipleOf(long number, long factor) {
 		return (number % factor == 0);
 	}
 	
 	private static boolean isPrime(long number) {
 		if (number < 2)
 			return false;
 		
		for (int i = 2; i <= Math.sqrt(number); i++) {
 			if (number % i == 0) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private static <E> E lastObject(List<E> objects) {
 		if (objects.size() == 0) {
 			return null;
 		}
 		return objects.get(objects.size() - 1);
 	}
 		
 }
