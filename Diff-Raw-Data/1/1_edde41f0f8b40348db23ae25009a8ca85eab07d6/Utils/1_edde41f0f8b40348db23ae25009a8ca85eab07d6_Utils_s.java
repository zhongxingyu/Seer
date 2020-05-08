 package org.jetbrains.plugins.xml.searchandreplace.search.predicates;
 
 public class Utils {
   public static boolean wildcardMatches(String text, String pattern) {
     String[] cards = pattern.split("\\*");
     if (text.isEmpty() || text.charAt(0) != '*') {
       if(cards.length > 0 && text.indexOf(cards[0]) != 0) {
         return false;
       }
     }
     for (String card : cards) {
       int idx = text.indexOf(card);
       if(idx == -1) {
         return false;
       }
       text = text.substring(idx + card.length());
     }
     if (!text.isEmpty() && pattern.charAt(pattern.length()-1) != '*') {
       return false;
     }
     return true;
   }
 }
