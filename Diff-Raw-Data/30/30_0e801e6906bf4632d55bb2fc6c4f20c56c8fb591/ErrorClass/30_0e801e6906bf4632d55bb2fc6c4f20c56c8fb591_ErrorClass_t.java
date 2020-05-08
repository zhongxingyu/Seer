 package pl.gajowy.kernelEstimator;
 
 import static java.lang.Math.*;
 
 public enum ErrorClass {
 
     //be careful when changing enum value indices, the code below relies on them!
     E04_OR_BIGGER,
     E05,
     E06,
     E07,
    E08,
    E09_OR_SMALLER;

    public static final int MIN_ORDER = 4;
    public static final int MAX_ORDER = 9;
 
     public static ErrorClass forValue(float errorValue) {
         double errorOrder = -log10(abs(errorValue));
         int integerOrder = (int) Math.ceil(errorOrder);
        int truncatedOrder = max(MIN_ORDER, min(integerOrder, MAX_ORDER));
        int classIndex = truncatedOrder - MIN_ORDER;
         return values()[classIndex];
     }
 }
