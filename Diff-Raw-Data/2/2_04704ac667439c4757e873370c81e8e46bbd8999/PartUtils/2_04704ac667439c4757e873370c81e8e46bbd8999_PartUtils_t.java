 package com.mishmash.alpha.vehicleparts;
 
 public class PartUtils {
     public final static double PERCENTAGE_CONVERTER = 100.0;
     
     public static double convertFromPercentageToModifier(double percentage) {
         return 1.0 + (percentage / PERCENTAGE_CONVERTER);
     }
     
     public static double convertFromMultiplicativeModifierToAdditive(double modifier) {
        return (modifier >= 0) ? modifier - 1.0 : 1.0 - modifier;
     }
     
     public static boolean doubleEquals(double first, double second) {
         return Math.abs(first - second) < IVehiclePart.DELTA;
     }
 
 }
