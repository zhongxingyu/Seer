 /*
  * PaymentUtils.java
  *
  * Copyright (c) 2009 FooBrew, Inc.
  */
 package org.j2free.util;
 
 /**
  *
  * @author Arjun
  */
 public class PaymentUtils {
 
     public static String getCreditCardDigitsOnly(String s) {
         StringBuffer digitsOnly = new StringBuffer();
         char c;
         for (int i = 0; i < s.length(); i++) {
             c = s.charAt(i);
             if (Character.isDigit(c)) {
                 digitsOnly.append(c);
             }
         }
         return digitsOnly.toString();
     }
 
     //-------------------
     // Perform Luhn check
     //-------------------
     public static boolean isValidCreditCard(String cardNumber) {
         
         if (cardNumber == null || cardNumber.length() <= 0) {
             return false;
         }
         
         String digitsOnly = getCreditCardDigitsOnly(cardNumber);
         int sum = 0;
         int digit = 0;
         int addend = 0;
         boolean timesTwo = false;
 
         for (int i = digitsOnly.length() - 1; i >= 0; i--) {
             digit = Integer.parseInt(digitsOnly.substring(i, i + 1));
             if (timesTwo) {
                 addend = digit * 2;
                 if (addend > 9) {
                     addend -= 9;
                 }
             } else {
                 addend = digit;
             }
             sum += addend;
             timesTwo = !timesTwo;
         }
 
         int modulus = sum % 10;
         return modulus == 0;
     }
     
     public static String getCreditCardType(String cardNumber) {
        int firstDigit = Integer.valueOf(cardNumber.charAt(0));
         switch(firstDigit) {
             case 3: return "Amex";
             case 4: return "Visa";
             case 5: return "MasterCard";
             case 6: return "Discover";
         }
         return "invalid";
     }
             
 }
