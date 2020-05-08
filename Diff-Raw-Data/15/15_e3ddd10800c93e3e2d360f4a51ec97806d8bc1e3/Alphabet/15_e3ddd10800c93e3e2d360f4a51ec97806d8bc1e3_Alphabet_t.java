 package jpower.core;
 
 public class Alphabet {
 
     private static final char[] ALPHABET = {
             'a',
             'b',
             'c',
             'd',
             'e',
             'f',
             'g',
             'h',
             'i',
             'j',
             'k',
             'l',
             'm',
             'n',
             'o',
             'p',
             'q',
             'r',
             's',
             't',
             'u',
             'v',
             'w',
             'x',
             'y',
             'z'
     };
 
     public static char[] get() {
         return ALPHABET;
     }
 
     public static char[] uppercase() {
         char[] uppercase = new char[ALPHABET.length];
         for (int i = 0; i < ALPHABET.length; i++) {
            uppercase[i] = Character.toUpperCase(ALPHABET[i]);
         }
         return uppercase;
     }
 
     public static char[] lowercase() {
         return get();
     }
 }
