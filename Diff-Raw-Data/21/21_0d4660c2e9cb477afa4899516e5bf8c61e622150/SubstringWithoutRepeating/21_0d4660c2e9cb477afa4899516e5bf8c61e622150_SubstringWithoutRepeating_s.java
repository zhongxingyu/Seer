 package leetcode;
 
 /**
 * Created by sobercheg on 12/21/13.
 * http://leetcode.com/2011/05/longest-substring-without-repeating-characters.html
  */
 public class SubstringWithoutRepeating {
     // limitation: ASCII chars only
     public static String getLongestSubstringWithoutRepeating(String input) {
         int maxSubstr = 0;
         int i = 0;
         int j = 0;
         int maxi = 0;
         int maxj = 0;
         boolean[] exists = new boolean[256];
 
         while (j < input.length()) {
             if (!exists[input.charAt(j)]) {
                 exists[input.charAt(j)] = true;
                 j++;
             } else {
                 if (maxSubstr < j - i) {
                     maxSubstr = j - i;
                     maxi = i;
                     maxj = j;
                 }
                while (i <= j) {
                     exists[input.charAt(i)] = false;
                     i++;
                 }
                 j++;
                 i++;
             }
         }
 
         if (maxSubstr < j - i) {
             maxSubstr = j - i;
             maxi = i;
             maxj = j;
         }
         return input.substring(maxi, maxj);
     }
 
     public static void main(String[] args) {
         System.out.println(SubstringWithoutRepeating.getLongestSubstringWithoutRepeating("abcabcbb"));
     }
 }
