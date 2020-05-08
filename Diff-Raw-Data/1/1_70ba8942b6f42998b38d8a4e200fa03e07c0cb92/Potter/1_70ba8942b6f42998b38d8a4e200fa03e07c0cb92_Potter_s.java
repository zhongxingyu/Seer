 package com.github.kpacha.jkata.potter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Potter {
 
     private static Map<Integer, Double> discount = new HashMap<Integer, Double>() {
 	{
 	    put(3, 0.9);
 	    put(2, 0.95);
 	    put(1, 1.0);
 	    put(0, 1.0);
 	}
     };
 
     public static double priceFor(int items) {
 	return 8 * items * getDiscountFor(items);
     }
 
     private static double getDiscountFor(int items) {
 	return discount.get(items);
     }
 }
