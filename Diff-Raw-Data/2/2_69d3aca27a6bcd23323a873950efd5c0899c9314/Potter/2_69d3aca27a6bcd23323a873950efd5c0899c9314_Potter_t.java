 package com.github.kpacha.jkata.potter;
 
 public class Potter {
 
     public static double priceFor(int items) {
	if (items == 3)
	    return 8 * 3 * 0.90;
 	if (items == 2)
 	    return 8 * 2 * 0.95;
 	if (items == 1)
 	    return 8;
 	return 0;
     }
 }
