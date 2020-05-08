 package pl.gajowy.kernelEstimator;
 
 import static java.lang.Math.*;
 
 public enum ErrorClass {
 
     //be careful when changing enum value indices, the code below relies on them!
     E04_OR_BIGGER,
     E05,
     E06,
     E07,
    E08_OR_SMALLER;
 
     public static ErrorClass forValue(float errorValue) {
         double errorOrder = -log10(abs(errorValue));
         int integerOrder = (int) Math.ceil(errorOrder);
        int truncatedOrder = max(4, min(integerOrder, 8));
        int classIndex = truncatedOrder - values().length + 1;
         return values()[classIndex];
     }
 }
