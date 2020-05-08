 package com.appaholics.primes;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * Checks to see if the given number is a twisted prime. A twisted prime is a
  * prime number whose reverse is also a prime. Examples: 13-31, 167-761
  * 
  * @author Raghav Sood
  * @version 1
  */
 public class TwistedPrime {
 
 	/**
 	 * The main method.
 	 * 
 	 * @param args
 	 *            the arguments
 	 */
 	public static void main(String args[]) {
 		TwistedPrime twistedPrime = new TwistedPrime();
 		twistedPrime.run();
 	}
 
 	/**
 	 * Run.
 	 */
 	public void run() {
 		BufferedReader object = new BufferedReader(new InputStreamReader(System.in));
 
 		int num = 0;
 		int digit = 0;
 		int reverse = 0;
 
 		System.out.println("Enter a prime number to check if it's a Twisted Prime or not.");
 
 		try {
 			num = Integer.parseInt(object.readLine());
 		} catch (NumberFormatException e) {
 			System.out.println("NaN");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("IOException");
 			e.printStackTrace();
 		}
 
 		if (num < 10 && num > -10) {
 			System.out.println("You have entered a single digit number. It doesn't have a reverse.");
 		}
 
 		boolean isFirstNumPrime = isPrime(num);
 
 		int numCopy = num;
 
 		while (numCopy > 0) {
 			digit = numCopy % 10;
 			reverse = reverse * 10 + digit;
 			numCopy = numCopy / 10;
 
 		}
 
 		boolean isSecondNumPrime = isPrime(reverse);
 
 		if (isFirstNumPrime && isSecondNumPrime) {
 			System.out.println(num + " and " + reverse + " are Twisted Primes");
 		}
 	}
 
 	/**
 	 * Checks if a number is prime
 	 * 
 	 * @param n
 	 *            The number to be checked
 	 * @return true, if n is prime
 	 */
 	public boolean isPrime(int n) {
 		/**
 		 * Why this isPrime method is better than the one you're probably using:
 		 * 
 		 * It doesn't check all integers between 1 and n, because that's a
 		 * stupid way to do things. Instead, it checks against only 2 and add
 		 * integers up to the square root of n, because that's the smart way to
 		 * do things.
 		 * 
 		 * Reasons for the above:
 		 * 
 		 * 1) We learned numbers are prime if the only divisors they have are 1
 		 * and itself. Trivially, we can check every integer from 1 to itself
 		 * (exclusive) and test whether it divides evenly. This might tempt us
 		 * to check every number between 1 and n.
 		 * 
 		 * 2) This doesnt seem bad at first, but we can make it faster  much
 		 * faster. Consider that if 2 divides some integer n, then (n/2) divides
 		 * n as well. This tells us we dont have to try out all integers from 2
 		 * to n. Due to this, we only check against the number 2. We don't need
 		 * to check against the multiples of 2, and have immediately cut down
 		 * the numbers we must check against to half./n
 		 * 
 		 * 3) Finally, we know that you really only have to go up to the square
 		 * root of n, because if you list out all of the factors of a number,
 		 * the square root will always be in the middle (if it happens to not be
 		 * an integer, were still okay, we just might over-approximate, but our
 		 * code will still work).
 		 */
 		if (n % 2 == 0)
 			return false; // Can't be prime if it is a multiple of 2
 		for (int i = 3; i * i <= n; i += 2) { // We only need to check against
												// odd numbers upto n
 			if (n % i == 0)
 				return false;
 		}
 		return true;
 	}
 }
