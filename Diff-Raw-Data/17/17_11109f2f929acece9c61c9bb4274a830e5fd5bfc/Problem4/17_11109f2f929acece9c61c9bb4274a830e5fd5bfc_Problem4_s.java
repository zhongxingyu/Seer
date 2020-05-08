 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.unknownentity.eulermaven;
 
 /**
  *
  * @author alde
  */
 class Problem4 {
 
         /*
          * 
          * A palindromic number reads the same both ways. The largest palindrome
          * made from the product of two 2-digit numbers is 9009 = 91  99.
          *
          * Find the largest palindrome made from the product of two 3-digit numbers.
          * 
          */
         void showResult() {
 
                 int max = 0;
                 outerloop:
                 for (int i = 999; i >= 100; i--) {
                         for (int n = 999; n >= 100; n--) {
                                 Integer test = n * i;
                                 if (test % 10 != 0) {
                                         Integer reverse = invertNumber(test);
                                         if (reverse.equals(test)) {
                                                 if (test > max) {
                                                         max = test;
                                                 }
                                         }
                                 }
 
                         }
                 }
                 System.out.println("Largest Palindrome is " + max);
 
         }
 
         private Integer invertNumber(Integer number) {
                 Integer newNumber = 0;
                //You can here do something to make sure 'number' is not negative
                 while (number > 0) {
                         newNumber = newNumber * 10 + number % 10;
                         number = number / 10;
                 }
                 return newNumber;
         }
 }
