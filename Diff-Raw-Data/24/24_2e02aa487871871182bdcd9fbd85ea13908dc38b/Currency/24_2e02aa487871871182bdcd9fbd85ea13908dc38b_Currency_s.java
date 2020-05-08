 package com.lake.tahoe.utils;
 
 import java.text.NumberFormat;
 import java.text.ParseException;
 
 /**
  * Created by rhu on 11/3/13.
  */
 public class Currency {
 
	public static String getDisplayDollars(double cents) {
 		NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
		return numberFormat.format(cents / 100);
	}

	public static String getDisplayDollars(float amount) {
		NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
		return numberFormat.format(amount);
 	}
 
 	public static int getAmountInCents(String amtText) {
 		if (amtText == null || "".equals(amtText)) {
 			return 0;
 		}
 
 		NumberFormat nf = NumberFormat.getCurrencyInstance();
 		try {
 			return (int) (nf.parse(amtText).floatValue() * 100);
 		} catch (ParseException e) {
 			return 0;
 		}
 	}
 }
