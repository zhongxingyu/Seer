 package br.com.poke.euler;
 
 /**
  * Created with IntelliJ IDEA.
  * User: eldender
  * Date: 9/17/13
  * Time: 6:43 PM
  * Version: 1
  * Optimized after RudyPenteado explanation
  */
 
 /*
 If we list all the natural numbers below 10 that are multiples of 3 or 5, we get 3, 5, 6 and 9. The sum of these multiples is 23.
 
 Find the sum of all the multiples of 3 or 5 below 1000.
  */
 public class Problem1 {
 
     private static final int LIMIT = 1000;
     private static int result = 0;
 
     public static int work() {
 
         int divBy3 = 990 / 3; //amount of 3s from 3 to 990
         int divBy5 = 990 / 5; //amount of 5s from 5 to 990
 
         //this is the trick part, doing this we find the value of the first multiple number above 990 for both 3 and 5, half of this value is the sum of all other numbers
         int sum3s = divBy3 * (990 + 3) / 2; //sum of all multiple numbers of 3 below 990
         int sum5s = divBy5 * (990 + 5) / 2; //sum of all multiple numbers of 5 below 990
 
         //we have to sum the remaining
         int sumRemaining = 993 + 995 + 996 + 999;
 
         result = sum3s + sum5s + sumRemaining;
 
        //there is a problem, as we know some numbers might be multiple of both, 3 and 5, so we find the smallest common multiple which is 15, now we do the same
         int divBy15 = 990 / 15; //amount of 15s from 15 to 990
 
         int sum15s = divBy15 * (990 + 15) / 2; //sum of all multiple numbers of 15 below 990
 
         result -= sum15s; //here we are reducing the duplicate value
 
         return result;
     }
 }
