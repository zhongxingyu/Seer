 package no.iterate.javazone;
 
 
 public class Tokenizer {
     public static String[] tokenize(String text) {
         if (text.trim().length() == 0) {
             return null;
         }
 
        text.replaceAll("[^\\p{L}\\p{Z}]", " "); // Replaces all special signs, except for dash and exclamation mark :p
        String[] tokenizedWords = text.split(" ");
 
         return tokenizedWords;
     }
 }
