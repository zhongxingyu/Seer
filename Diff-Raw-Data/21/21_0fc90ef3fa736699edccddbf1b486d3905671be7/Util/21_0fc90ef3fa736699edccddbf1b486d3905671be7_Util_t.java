 package com.example.pokerchance;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.NumberFormat;
 
 public class Util {
 	
  public static String formatChance(float num) {
	 double value = new BigDecimal(num).setScale(2, RoundingMode.UP).doubleValue();
 	 return String.format("%2.2f : 1", value); 
  }
 	
  public static String formatUnit(float num) {
 	 
 	return String.format("%2.0f", num); 
  }
 	
  public static String formatPercent(float num) {
	 double value = new BigDecimal(num).setScale(2, RoundingMode.UP).doubleValue();
 	 return String.format("%2.2f", value) + "%"; 
  }
 
 }
