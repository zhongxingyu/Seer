 package com.github.kpacha.jkata.romanNumerals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class RomanNumerals {
 
     private static List<RomanArabicPair> eqivalencies = new ArrayList<RomanArabicPair>() {
 	{
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
 }
