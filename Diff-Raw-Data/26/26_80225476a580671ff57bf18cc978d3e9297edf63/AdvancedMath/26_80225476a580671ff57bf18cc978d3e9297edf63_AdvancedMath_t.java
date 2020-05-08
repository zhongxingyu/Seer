 package advancedmath;
 
 /**
  * @author Fahd Ananta
  */
 public class AdvancedMath {
 
     private static final double LOG_2 = Math.log(2.0);

     /**
      * ComplexNumberException
      */
     private static class ComplexNumberException extends Exception {
 
         public ComplexNumberException() {
             super("Exception: Complex number generated");
         }
     }
 
     /**
      * Returns a random integer in the range lower - upper
      * Ex. randInt(1, 10) = 9
      * @param lower lower bound of range
      * @param upper upper bound of range
      * @return random integer in range
      */
     public static int randInt(int lower, int upper) {
         int low = (upper < lower) ? upper : lower;
         int hi = (lower > upper) ? lower : upper;
 
         return (int) Math.rint(Math.abs(hi - low) * Math.random()) + low;
     }
 
     /**
      * Hyperbolic cosine; mathematical function cosh(x)
      * @param x a value
      * @return cosh(x)
      */
     public static double cosh(double x) {
         return (0.5 * (Math.exp(-1 * x) + Math.exp(x)));
     }
 
     /**
      * Hyperbolic sine; mathematical function sinh(x)
      * @param x a value
      * @return sinh(x)
      */
     public static double sinh(double x) {
         return (0.5 * ((-1) * Math.exp(-1 * x) + Math.exp(x)));
     }
 
     /**
      * Hyperbolic tangent; mathematical function tanh(x)
      * @param x a value
      * @return tanh(x)
      */
     public static double tanh(double x) {
         return (sinh(2 * x)) / (cosh(2 * x) + 1);
     }
 
     /**
      * Inverse hyperbolic cosine; mathematical function arccosh(x)
      * @param x a value
      * @return arccosh(x)
      * @throws ComplexNumberException when complex number involved
      */
     public static double acosh(double x) throws ComplexNumberException {
         if (x < 1) {
             throw new ComplexNumberException();
         }
         return Math.log(x + (Math.sqrt(-1 + x) * Math.sqrt(1 + x)));
     }
 
     /**
      * Inverse hyperbolic sine; mathematical function arcsinh(x)
      * @param x a value
      * @return arcsinh(x)
      * @throws ComplexNumberException when complex number involved
      */
     public static double asinh(double x) throws ComplexNumberException {
         double innerResult = x + Math.sqrt(1 + Math.pow(x, 2.0));
         if (innerResult <= 0) {
             throw new ComplexNumberException();
         }
         return Math.log(innerResult);
     }
 
     /**
      * Inverse hyperbolic tangent; mathematical function arctanh(x)
      * @param x a value
      * @return arctanh(x)
      * @throws ComplexNumberException when complex number involved
      */
     public static double atanh(double x) throws ComplexNumberException {
        if (Math.abs(x) >= 1.0) {
             throw new ComplexNumberException();
         }
         return (0.5 * (Math.log(1 + x) - Math.log(1 - x)));
     }
 
     /**
      * Returns base 2 logarithm of a double value
      * @param x a value
      * @return the base 2 logarithm of x
      */
     public static double log2(double x) {
         return (Math.log(x) / LOG_2);
     }
 
     /**
      * Truncates x to 0 decimal places
      * Ex. trunc(22.64) = 22
      * @param x number to truncate
      * @return truncated value
      */
     public static int trunc(double x) {
         return (int) trunc(x, 0);
     }
 
     /**
      * Truncates x to number of decimal places specified by digits
      * Ex. trunc(22.64, 1) = 22.6
      * Ex. trunc(12345.678, -2) = 12300.0
      * @param x number to truncate
      * @param i decimal places to keep after truncation
      * @return truncated value
      */
     public static double trunc(double x, int i) {
        return ((int) (x * Math.pow(10, i))) / Math.pow(10, i);
     }
 }
