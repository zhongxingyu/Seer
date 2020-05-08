 package fi.lolcatz.profiler;
 
 import org.apache.log4j.Logger;
 
 public class ComplexityAnalysis {
 
     private static Logger logger = Logger.getLogger(ComplexityAnalysis.class);
 
     private ComplexityAnalysis() {
     }
 
     /**
      * Throws AssertionError if the parameter is not linear
      * @throws AssertionError 
      */
     public static void assertLinear(Output<?> output) throws AssertionError {
         if (!isLinear(output)) {
             throw new AssertionError();
         }
     }
     
     /**
      * Throws AssertionError if the parameter is not linear
      * @throws AssertionError 
      */
     public static void assertLinear(String message, Output<?> output) throws AssertionError {
         if (!isLinear(output)) {
             throw new AssertionError(message);
         }
     }
     
     /**
      * Throws AssertionError if the parameter is linear
      * @throws AssertionError 
      */
     public static void assertNotLinear(Output<?> output) throws AssertionError {
         if (isLinear(output)) {
             throw new AssertionError();
         }
     }
     
     /**
      * Throws AssertionError if the parameter is linear
      * @throws AssertionError 
      */
     public static void assertNotLinear(String message, Output<?> output) throws AssertionError {
         if (isLinear(output)) {
             throw new AssertionError(message);
         }
     }
     
     /**
      * Throws AssertionError if the parameter is slower than linear
      * @throws AssertionError 
      */
     public static void assertLinearOrFaster(Output<?> output, double margin) throws AssertionError {
         if (!isLinearOrFaster(output, margin)) {
             throw new AssertionError();
         }
     }
     
     /**
      * Throws AssertionError if the parameter is slower than linear
      * @throws AssertionError 
      */
     public static void assertLinearOrFaster(String message, Output<?> output, double margin) throws AssertionError {
         if (!isLinearOrFaster(output, margin)) {
             throw new AssertionError(message);
         }
     }
 
     /**
      * Throws AssertionError if the parameter is not quadratic
      * @throws AssertionError 
      */
     public static void assertQuadratic(Output<?> output) throws AssertionError {
         if (!isQuadratic(output)) {
             throw new AssertionError();
         }
     }
     
     /**
      * Throws AssertionError if the parameter is not quadratic
      * @throws AssertionError 
      */
     public static void assertQuadratic(String message, Output<?> output) throws AssertionError {
         if (!isQuadratic(output)) {
             throw new AssertionError(message);
         }
     }
     
     /**
      * Throws AssertionError if the parameter is quadratic
      * @throws AssertionError 
      */
     public static void assertNotQuadratic(Output<?> output) throws AssertionError {
         if (isQuadratic(output)) {
             throw new AssertionError();
         }
     }
     
         /**
      * Throws AssertionError if the parameter is quadratic
      * @throws AssertionError 
      */
     public static void assertNotQuadratic(String message, Output<?> output) throws AssertionError {
         if (isQuadratic(output)) {
             throw new AssertionError(message);
         }
     }
     
     /**
      * Throws AssertionError if the parameter is slower than quadratic
      * @throws AssertionError 
      */
     public static void assertQuadraticOrFaster(Output<?> output, double margin) throws AssertionError {
         if (!isQuadraticOrFaster(output, margin)) {
             throw new AssertionError();
         }
     }
     
     /**
      * Throws AssertionError if the parameter is slower than quadratic
      * @throws AssertionError 
      */
     public static void assertQuadraticOrFaster(String message, Output<?> output, double margin) throws AssertionError {
         if (!isQuadraticOrFaster(output, margin)) {
             throw new AssertionError(message);
         }
     }
 
     /**
      * Throws AssertionError if the parameter is not NlogN
      * @throws AssertionError 
      */
     public static void assertNlogN(Output<?> output) throws AssertionError {
         if (!isNlogN(output)) {
             throw new AssertionError();
         }
     }
     
     /**
      * Throws AssertionError if the parameter is not NlogN
      * @throws AssertionError 
      */
     public static void assertNlogN(String message, Output<?> output) throws AssertionError {
         if (!isNlogN(output)) {
             throw new AssertionError(message);
         }
     }
     
     /**
      * Throws AssertionError if the parameter is NlogN
      * @throws AssertionError 
      */
     public static void assertNotNlogN(Output<?> output) throws AssertionError {
         if (isNlogN(output)) {
             throw new AssertionError();
         }
     }
     
      /**
      * Throws AssertionError if the parameter is NlogN
      * @throws AssertionError 
      */
     public static void assertNotNlogN(String message, Output<?> output) throws AssertionError {
         if (isNlogN(output)) {
             throw new AssertionError(message);
         }
     }
 
     /**
      * Calculates the linearity of the parameter output
      *
      * @return True if the output is linear, false if the output is not linear
      */
     private static boolean isLinear(Output<?> output) {
         if (output.getSize().size() < 2) {
             return false;
         }
         Integer x0 = output.getSize().get(0);
         Long y0 = output.getTime().get(0);
         Integer xn = output.getSize().get(output.getSize().size() - 1);
         Long yn = output.getTime().get(output.getTime().size() - 1);
         double a = Math.abs(y0 - yn) / Math.abs(x0 - xn);
         double b = y0 - a * x0;
         for (int i = 0; i < output.getTime().size(); i++) {
            long time = output.getTime().get(i);
             double function = a * output.getSize().get(i) + b;
             boolean linearity = time * 1.002 >= function && time - (time * 0.002) <= function;
             if (!linearity) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Calculates the runtime speed of the parameter output
      *
      * @return True if the output is linear or faster, false if the output is
      * slower
      */
     public static boolean isLinearOrFaster(Output<?> output, double margin) {
         if (output.getSize().size() < 2) {
             return false;
         }
         Integer x0 = output.getSize().get(0);
         Long y0 = output.getTime().get(0);
         Integer xn = output.getSize().get(output.getSize().size() - 1);
         Long yn = output.getTime().get(output.getTime().size() - 1);
         double a = Math.abs(y0 - yn) / Math.abs(x0 - xn);
         double b = y0 - a * x0;
         for (int i = 0; i < output.getTime().size(); i++) {
            long time = output.getTime().get(i);
             double function = a * output.getSize().get(i) + b;
             boolean linearOrFaster = time <= function * margin;
             if (!linearOrFaster) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Calculates whether the parameter output is O(n*n)
      * 
      * a, b and c are from the function an*n+bn+c
      * denom is the common divider for them.
      * 
      * @param out size must be >= 3
      * @return True if output is quadratic, false if not
      */
     private static boolean isQuadratic(Output<?> out) {
         if (out.getSize().size() < 2) {
             return false;
         }
         Integer x1 = out.getSize().get(0);
         Integer x2 = out.getSize().get(1);
         Integer x3 = out.getSize().get(2);
 
         long y1 = out.getTime().get(0);
         long y2 = out.getTime().get(1);
         long y3 = out.getTime().get(2);
 
         double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
         double a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
         double b = (x3 * x3 * (y1 - y2) + x2 * x2 * (y3 - y1) + x1 * x1 * (y2 - y3)) / denom;
         double c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;
 
         for (int i = 0; i < out.getTime().size(); i++) {
            long time = out.getTime().get(i);
             double function = (a * out.getSize().get(i) * out.getSize().get(i)) + (b * out.getSize().get(i)) + c;
            boolean exponential = time * 1.1 >= function && time * 0.9 <= function;
             if (!exponential) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Calculates the runtime speed of the parameter output
      *
      * a, b and c are from the function an*n+bn+c
      * denom is the common divider for them.
      * 
      * @param out size must be >= 3     
      * @return True if the output is quadratic or faster, false if the output is
      * slower
      */
     public static boolean isQuadraticOrFaster(Output<?> out, double margin) {
         if (out.getSize().size() < 2) {
             return false;
         }
         Integer x1 = out.getSize().get(0);
         Integer x2 = out.getSize().get(1);
         Integer x3 = out.getSize().get(2);
 
         long y1 = out.getTime().get(0);
         long y2 = out.getTime().get(1);
         long y3 = out.getTime().get(2);
 
         double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
         double a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
         double b = (x3 * x3 * (y1 - y2) + x2 * x2 * (y3 - y1) + x1 * x1 * (y2 - y3)) / denom;
         double c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;
 
         for (int i = 0; i < out.getTime().size(); i++) {
            long time = out.getTime().get(i);
             double function = (a * out.getSize().get(i) * out.getSize().get(i)) + (b * out.getSize().get(i)) + c;
             boolean exponentialOrFaster = time <= function * margin;
             if (!exponentialOrFaster) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Calculates whether the class variable output is O(NlogN)
      *
      * @return True if output is O(NlogN), false if not
      */
     private static boolean isNlogN(Output<?> out) {
         if (out.getSize().size() < 2) {
             return false;
         }
         Integer x1 = out.getSize().get(0);
         Integer x2 = out.getSize().get(1);
         Integer x3 = out.getSize().get(2);
 
         long y1 = out.getTime().get(0);
         long y2 = out.getTime().get(1);
         long y3 = out.getTime().get(2);
 
         double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
         double a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
         double b = (x3 * (Math.log(x3) / Math.log(2)) * (y1 - y2) + x2 * (Math.log(x2) / Math.log(2)) * (y3 - y1) + x1 * (Math.log(x1) / Math.log(2)) * (y2 - y3)) / denom;
         double c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;
 
         for (int i = 0; i < out.getTime().size(); i++) {
            long time = out.getTime().get(i);
             double function = (a * out.getSize().get(i) * (Math.log(out.getSize().get(i)) / Math.log(2))) + (b * out.getSize().get(i)) + c;
             //System.out.println("function: " + a + "*" + out.getSize().get(i) * (Math.log(out.getSize().get(i)) / Math.log(2)) + "+" + b + "*" + out.getSize().get(i) + "+" + c + " = " + function);
             boolean nlogn = time * 1.004 >= function && time - (time * 0.004) <= function;
             if (!nlogn) {
                 return false;
             }
         }
 
         return true;
     }
 }
