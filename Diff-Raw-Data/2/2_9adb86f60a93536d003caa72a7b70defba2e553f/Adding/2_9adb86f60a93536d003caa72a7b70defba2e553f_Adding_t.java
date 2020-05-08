 package com.github.fhd.javaunitevolution.samples.adding;
 
 /**
  * This sample aims to evolve a method that will add to numbers to demonstrate
  * the basics of Java Unit Evolution.
  * <p>
 * The algorithm will simply have to use the function for addition, which is
  * among those it can use:
  * <ul>
  * <li>Addition
  * <li>Subtraction
  * <li>Multiplication
  * </ul>
  */
 public abstract class Adding {
     /**
      * Adds the two numbers together.
      * @param a One of the numbers to add.
      * @param b One of the numbers to add.
      * @return The result of adding the two numbers together.
      */
     public abstract int add(int a, int b);
 
     public static int operationAdd(int a, int b) {
         return a + b;
     }
 
     public static int operationSubtract(int a, int b) {
         return a - b;
     }
 
     public static int operationMultiply(int a, int b) {
         return a * b;
     }
 }
