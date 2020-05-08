 import java.util.Arrays;
 
 /**
  * Generate prime numbers from 2 to N using Sieve of Eratosthenes
  * 
  * @author chandershivdasani
  *
  */
 public class Prime {
 	
 	public static boolean[] generatePrime(int n) {
 		boolean[] primes = new boolean[n+1];
 		Arrays.fill(primes, true);
 		
 		//0 and 1 are not prime
 		primes[0] = false;
 		primes[1] = false;
 		
 		/*
		 * If any number < sqrt(n) divides n, the resulting
 		 * number will be between 1 - n and will also divide 
 		 * n.
 		 */
 		int m = (int)Math.sqrt(n);
 		
 		for(int i=0; i<=m; i++) {
 			if(primes[i]) {
 				for(int j=i; i <=n; j+=i) {
 					primes[j] = false;
 				}
 			}
 		}
 		
 		return primes;
 		
 	}
 	
 }
