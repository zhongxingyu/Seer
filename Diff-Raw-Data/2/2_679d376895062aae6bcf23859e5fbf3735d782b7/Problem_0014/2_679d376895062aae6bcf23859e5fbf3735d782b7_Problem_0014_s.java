 package org.projecteuler;
 
 /* @See http://projecteuler.net/problem=14 
  * 
  * The following iterative sequence is defined for the set of positive integers:
  *
  * n → n/2 (n is even) n → 3n + 1 (n is odd)
  *
  * Using the rule above and starting with 13, we generate the following
  * sequence: 13 → 40 → 20 → 10 → 5 → 16 → 8 → 4 → 2 → 1
  *
  * It can be seen that this sequence (starting at 13 and finishing at 1)
  * contains 10 terms. Although it has not been proved yet (Collatz Problem), it
  * is thought that all starting numbers finish at 1.
  *
  * Which starting number, under one million, produces the longest chain?
  *
  * NOTE: Once the chain starts the terms are allowed to go above one million.
  *
  *
  */
 public class Problem_0014 {
 
    private static final int MAX_START = 1_000_000;
 
     private static long solve() {
         long answer = 0;
         long maxChainLength = 0;
 
         for (long i = MAX_START / 2; i < MAX_START; i++) {
             long n = i;
             long chainLength = 0;
 
             while (n != 1) {
                 chainLength++;
 
                 if (n % 2 == 0) {
                     n = n / 2;
                 } else {
                     n = (3 * n) + 1;
                 }
             }
 
             if (chainLength > maxChainLength) {
                 maxChainLength = chainLength;
                 answer = i;
             }
         }
 
         return answer;
     }
 
     public static void main(String... args) {
         long start = System.currentTimeMillis();
         System.out.println(solve());
         System.out.println(System.currentTimeMillis() - start);
     }
 }
