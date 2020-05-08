 package kkckkc.jsourcepad.util;
 
 import com.google.common.base.Predicate;
 
 public class QueryUtils {
 
     public static Predicate<String> makePredicate(final String query) {
         return new Predicate<String>() {
                 public boolean apply(String s) {
                     int i = 0;
                     for (char c : query.toCharArray()) {
                         boolean found = false;
                         for (; i < s.length(); i++) {
                            found = s.charAt(i) == c;
                             if (found) break;
                         }
                         if (! found) return false;
                     }
 
                     return true;
                 }
             };
     }
 
     public static int getScorePenalty(String s, String query) {
         int score = 0;
         int i = 0;
         for (char c : query.toCharArray()) {
             for (; i < s.length(); i++) {
                if (s.charAt(i) == c) {
                    score -= i;
                     break;
                 }
             }
         }
         return score;
     }
 
 }
