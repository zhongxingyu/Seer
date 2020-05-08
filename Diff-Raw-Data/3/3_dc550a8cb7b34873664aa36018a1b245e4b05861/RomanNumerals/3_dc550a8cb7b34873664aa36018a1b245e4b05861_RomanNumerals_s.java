 package com.github.kpacha.jkata.romanNumerals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RomanNumerals {
 
     private static List<RomanArabicPair> eqivalencies = new ArrayList<RomanArabicPair>() {
 	{
 	    add(new RomanArabicPair("M", 1000));
 	    add(new RomanArabicPair("CM", 900));
 	    add(new RomanArabicPair("D", 500));
 	    add(new RomanArabicPair("CD", 400));
 	    add(new RomanArabicPair("C", 100));
 	    add(new RomanArabicPair("XC", 90));
 	    add(new RomanArabicPair("L", 50));
 	    add(new RomanArabicPair("XL", 40));
 	    add(new RomanArabicPair("X", 10));
 	    add(new RomanArabicPair("IX", 9));
 	    add(new RomanArabicPair("V", 5));
 	    add(new RomanArabicPair("IV", 4));
 	    add(new RomanArabicPair("I", 1));
 	}
     };
 
     public static String convert(int number) {
 	String roman = "";
 	for (RomanArabicPair equivalence : eqivalencies) {
 	    while (number >= equivalence.getArabic()) {
 		roman += equivalence.getRoman();
 		number -= equivalence.getArabic();
 	    }
 	}
 	return roman;
     }
 
     public static int convert(String roman) {
 	if (roman.startsWith("II")) {
 	    return 2;
 	}
 	return 1;
     }
 }
