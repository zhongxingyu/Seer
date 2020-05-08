 package no.iterate.javazone;
 
 
 public class Tokenizer {
     public static String[] tokenize(String text) {
         if (text.trim().length() == 0) {
             return null;
         }
 
        String specialsStripped = text.replaceAll("[^\\p{L}\\p{Z}]", " ");
        String[] tokenizedWords = specialsStripped.split(" ");
 
         return tokenizedWords;
     }
 }
