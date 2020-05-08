 package com.jadekler.app;
 
 /**
 * Hello world!
  *
  */
 public class CubicCounter {
     int count;
 
     public static void main(String[] args) {
         CubicCounter cc = new CubicCounter();
 
         for (int i = 1; i <= 10; i++) {
             System.out.println(i+"x"+i+" "+cc.countPermutations(i,i));
             System.out.println(i+"x"+i+" "+cc.twoNChooseN(i,i));
         }
     }
 
     /**
      * Using combinatorial identity C(2n, n) derived by hand on plane (woot!)
      * Note: This assumes cubic property and therefore ignores k
      * Note: We are simplifying C(2n, n) = (2n)!/(n!(2n-n)!) = (2n)!/(n!n!) = (2n)(2n-1)..(n+1)/n!
      *       We could (easily) simplify further but this is a good start
      */
     public long twoNChooseN(int n, int k) {
         return this.factorial(2*n,n+1)/(this.factorial(n,1));
     }
 
     public int countPermutations(int n, int k) {
         this.count = 0;
         countPermutations(0, 0, n, k);
         return this.count;
     }
 
     public void countPermutations(int n, int k, int max_n, int max_k) {
         if (n == max_n && k == max_k) {
             this.count += 1;
         }
 
         if (n < max_n) {
             countPermutations(n+1, k, max_n, max_k);
         }
 
         if (k < max_k) {
             countPermutations(n, k+1, max_n, max_k);
         }
     }
 
     /**
      * Raises 2 to the power of n. I know there is a math function for this, but I'm on a plane to SF and forgot where the 
      * math library is (and, frankly, the function name. expr()? exp()? i'll just make my own.. :) )
      * @param  n Power to raise 2 by
      * @return   2^n
      */
     public int exp2(int n) {
         if (n == 0) {
             return 1;
         }
 
         return 2<<(n-1);
     }
 
     /**
      * As with exp2, I don't know where the math lib is so I'm just writing my own
      * @param  n      Number to begin factorial at
      * @param  n_stop Number to end factorial at
      * @return        Returns n*n-1*n-2*...*n-n_stop+1. Set n_stop = 1 for n!
      */
     public long factorial(int n, int n_stop) {
         long product = 1;
 
         while (n >= n_stop) {
             product *= n;
             n--;
         }
 
         return product;
     }
 }
