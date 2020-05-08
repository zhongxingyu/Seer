 package com.Eric;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
    Produces a list of primes all the way up to desired limit (inclusive). Also, provides useful functions that use a sieve.
  * @author Eric Ponce
  */
 public class SieveOfEratosthenes {
     
     /**
      * Produces a list of prime numbers up to and possibly including the limit.
      * @param limit highest possible number in list
      * @return Integer Array containing Sieve List.
      */
     public static int [] sieveList(int limit) {
         int [] list = new int[limit + 1];
         list[0] = 2;
         int count = 1;
         boolean [] primes = new boolean[limit + 1];
         Arrays.fill(primes, true);
         
         int current = 3;
         int t = 3;
         
         while (current <= limit) {
             if (primes[current]) {
                 list[count] = current;
                 count++;
                 t += current;
                 while (t <= limit) {
                     primes[t] = false;
                     t += current;        
                 }
             }
             current += 2;
             t = current;  
         }
        return trim(newArr, count); 
     }
     
     /**
      * Trims array to desired length
      * @param arr Array to be trim
      * @param length length of trimmed array
      * @return trimmed array
      * 
      */
     private static int [] trim(int [] arr, int length) {
        int [] newArr = new int[length];
        System.arraycopy(arr, 0, newArr, 0, length);
        return newArr;
     }
     
     /**
      * Produces the sum of all the primes up to and including the limit
      * @param limit highest possible number added to sum
      * @return sum of primes up to limit
      */
     public static long sumOfPrimes(int limit) {
         ArrayList<Integer> primes = sieveList(limit);
         long sum = 0;
         for (int e : primes) {
             sum += e;
         }
         return sum;
     }
     
     /**
      * Counts the number of prime and non-prime divisors in a number using a modified form of trial and division.
      * Generates a list of primes, gets the prime factors, and multiplies the exponents, which are added one.
      * So, for example, for the number 8, the prime factors are 2^3. From there, we add one to each exponent, so (3 + 1).
      * Then multiply by each other: (3 + 1) * 1 = 4.
      * @param n
      * @return 
      */
     public static long numberOfFactors(long n) {
         ArrayList<Integer> primes = sieveList((int)Math.sqrt(n) * 2);
         long factorCount = 1, a = 0;
         int currentPrime = 0;
         long exp = 0;
         while (n != 1) {
             a = (long) primes.get(currentPrime);
             if (n % a == 0) {
                 n /= a;
                 exp++;
             } else {
                 factorCount *= exp + 1;
                 currentPrime++;
                 exp = 0;
             }
         }
 
         factorCount *= exp + 1;
         return factorCount;
     }
 }
