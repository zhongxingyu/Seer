 // The sum of the primes below 10 is 2 + 3 + 5 + 7 = 17.
 // Find the sum of all the primes below two million.
 
 import static java.lang.System.out;
 
 public class Euler10bpw {
 
 	public static void main(String[] args) {
 
		long answer = 0L;
 
 		for(long i=2L; i<2000000; i++) {
 			if (is_prime(i) == true) {
 				answer += i;
 			}
 		}
 
 		out.println(answer);
 
 	}
 
 	static boolean is_prime(long num) {
         for(long i=2L; i<=Math.sqrt(num); i++) {
             if(num % i == 0) {
                 return false;
             }
         }
         return true;
     }
 
 }
