 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.unknownentity.eulermaven;
 
 /**
  *
  * @author alde
  */
 public class Problem1 {
         /*
          * If we list all the natural numbers below 10 that are multiples of 3 or 5,
          * we get 3, 5, 6 and 9. The sum of these multiples is 23.
          *
          * Find the sum of all the multiples of 3 or 5 below 1000.
          *
          */
 
        public Problem1() {
        }

         public void showResult() {
                 int sum = 0;
                 for (int n = 0; n < 1000; n++) {
                         if (n % 3 == 0 || n % 5 == 0) {
                                 sum += n;
                         }
                 }
                 System.out.println("Sum of all multiples of 3 or 5 below 1000: " + sum);
         }
 }
