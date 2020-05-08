 package com.kisel.algo;
 
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author brainless
 */
 public class Coder {
 
     private final String from;
     private final String to;
 
     public Coder(String from, String to) {
         this.from = from;
         this.to = to;
     }
 
     public String code(String number) {
         return makeCode(makeDigits(number));
     }
 
     private long makeDigits(String number) {
         int codePosition = from.length();
         int numberLength = number.length();
         long inDigits = 0;
         for (int i = 0; i < numberLength; i++) {
             inDigits += from.indexOf(number.charAt(i)) * pow(codePosition, numberLength - i - 1);
         }
         return inDigits;
     }
 
     private String makeCode(long inDigits) {
         int codePosition = to.length();
         StringBuilder resultCode = new StringBuilder();
         int r = 0;
         while (inDigits > 0) {
             resultCode.append(to.charAt((int) inDigits % codePosition));
             inDigits = inDigits / codePosition;
         }
         return resultCode.reverse().toString();
 
     }
 
     private int pow(int n, int p) {
         int result = 1;
         for (; p > 0; p--) {
             result *= n;
         }
         return result;
     }
 }
