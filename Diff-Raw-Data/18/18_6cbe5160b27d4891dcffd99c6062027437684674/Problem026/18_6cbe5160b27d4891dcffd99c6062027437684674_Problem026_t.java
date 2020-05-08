 package com.github.johnnyo.euler;
 
 import java.math.BigInteger;
 
 import com.github.johnnyo.euler.util.SieveOfEratosthenes;
 
 /**
  * A unit fraction contains 1 in the numerator. The decimal representation of the unit fractions with denominators 2 to
  * 10 are given:
  * <ul>
  * <li>1/2 = 0.5
  * <li>1/3 = 0.(3)
  * <li>1/4 = 0.25
  * <li>1/5 = 0.2
  * <li>1/6 = 0.1(6)
  * <li>1/7 = 0.(142857)
  * <li>1/8 = 0.125
  * <li>1/9 = 0.(1)
  * <li>1/10 = 0.1
  * </ul>
  * Where 0.1(6) means 0.166666..., and has a 1-digit recurring cycle. It can be seen that 1/7 has a 6-digit recurring
  * cycle.
  * 
  * Find the value of d 1000 for which 1/d contains the longest recurring cycle in its decimal fraction part.
  * 
  * 
  * Solution:
  * 
  * This one is actually a little tricky, and requires knowing about a special class of numbers, known as Reptend Primes.
  * You can read more about ReptendPrimes at the link below. Once you know what a ReptendPrime is, you can reformulate
  * the question as:
  * 
  * What is the largest ReptendPrime under 1000?
  * 
  * @link http://mathworld.wolfram.com/FullReptendPrime.html
  * 
  * 
  * @author JohnnyO
  */
 public class Problem026 extends BaseTestCase {
 
    private static final BigInteger TEN = BigInteger.valueOf(10);

     @Override
    public final String solve() {
         // TODO Auto-generated method stub
         return "983";
     }
 
     @Override
    public final String getAnswer() {
         int max = 1;
         SieveOfEratosthenes sieve = new SieveOfEratosthenes(1000);
         for (int prime : sieve) {
            if (isFullReptend(prime)) {
                 max = prime;
            }
         }
         return Integer.toString(max);
     }
 
    private boolean isFullReptend(final int prime) {
 
         final BigInteger bigPrime = BigInteger.valueOf(prime);
 
         for (int i = 1; i < prime - 1; i++) {
             if (BigInteger.ONE.equals(TEN.modPow(BigInteger.valueOf(i), bigPrime))) {
                 return false;
             }
         }
 
         return BigInteger.ONE.equals(TEN.modPow(BigInteger.valueOf(prime - 1), bigPrime));
 
     }
 
 }
