 package com.github.ndtaylor.FibonacciFun;
 
 import java.util.Scanner;
 
 /**
  * A simple class for ad hoc testing of the fibonacci Calculator.
  * @author Nathan Taylor
  */
 public class Driver {
 
     /**
      * Continuously loops asking the user what fibonacci number they want to calculate until they 
     * enter something that is not an interger.
      * @param args Ignores all arguments.
      */
     public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);
         int n;
         
         System.out.printf("What fibonacci number do you want to calculate? ");
         while (sc.hasNextInt()) {
             n = sc.nextInt();
             System.out.printf("The %dth fibonacci number is %d\n", n, 
                 FibonacciCalculator.fibonacci(n));
             System.out.printf("\nWhat fibonacci number do you want to calculate? ");
         } 
         System.out.printf("Ending program. Been fun!\n");
     }
 }
