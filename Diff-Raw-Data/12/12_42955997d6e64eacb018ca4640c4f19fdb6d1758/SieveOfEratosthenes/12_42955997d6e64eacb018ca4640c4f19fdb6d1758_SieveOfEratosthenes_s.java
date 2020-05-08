<<<<<<< HEAD
=======
 package com.Eric;
 
>>>>>>> added sums
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
import java.util.ListIterator;
 
 /**
    Produces a list of primes all the way up to desired limit (inclusive).
  * @author Eric
  */
 public class SieveOfEratosthenes {
     
     /**
      * Produces a list of prime number up to and possibly including the limit.
      * @param limit highest possible number in list
      * @return Integer ArrayList containing Sieve List.
      */
     public static ArrayList<Integer> sieveList(int limit) {
         ArrayList<Integer> list = new ArrayList<Integer>();
         list.add(2);
         boolean [] primes = new boolean[limit + 1];
         Arrays.fill(primes, true);
         
         int current = 3;
         int t = 3;
         
         while (current <= limit) {
             if (primes[current] == true) {
                 list.add(current);
                 t += current;
                 while (t <= limit) {
                     primes[t] = false;
                     t += current;        
                 }
             }
             current += 2;
             t = current;  
         }
         return list;        
     }
     
     public static int sumOfPrimes(int limit) {
         LinkedList<Integer> primes = new LinkedList<Integer>(sieveList(limit));
         Iterator<Integer> iterator = primes.iterator();
         int sum = 0;
         while (iterator.hasNext()) {
             sum += iterator.next();
         }
         return sum;
     }
 }
