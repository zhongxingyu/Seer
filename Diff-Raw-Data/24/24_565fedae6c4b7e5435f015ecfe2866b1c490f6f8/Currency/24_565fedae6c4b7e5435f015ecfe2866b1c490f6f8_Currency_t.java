 package com.lake.tahoe.utils;
 
 import java.text.NumberFormat;
 import java.text.ParseException;
 
 /**
  * Created by rhu on 11/3/13.
  */
 public class Currency {
 
	public static String getDisplayDollars(int cents) {
 		NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
		return numberFormat.format(cents / 100.0);
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
