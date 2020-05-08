 package ru.ssau.karanashev.complex;
 
 import static java.lang.Math.cos;
 import static java.lang.Math.sin;
 
 /**
  * User: Mukhamed Karanashev
  * Date: Sep 21, 2010
  * Time: 10:22:37 PM
  */
 public class ComplexOperations {
 
     /**
      * Multiplies two complex values: a + b*i, c + d*i. Result of multiplication will be stored
      * in the result array. If result array's length is less than 2 - new array will be createn.
      *
      * @param a
      * @param b
      * @param c
      * @param d
      * @param result
      * @return reference to result array
      */
     public static double[] mult(double a, double b, double c, double d, double[] result) {
        if (result == null || result.length < 2) {
             result = new double[2];
         }
 
         result[0] = a * c - b * d;
         result[1] = b * c + a * d;
 
         return result;
     }
 
     /**
      * Calculates complex exponent exp(i*w). Exponent will be
      *
      * @param w
      * @return
      */
     public static double[] exp(double w, double[] result) {
 
         if (result == null || result.length < 2) {
             result = new double[2];
         }
 
         result[0] = cos(w);
         result[1] = sin(w);
 
         return result;
     }
 
 }
