 package leetcode;
 
 /**
 * Created by sobercheg on 12/21/13. http://leetcode.com/2011/05/longest-substring-without-repeating-characters.html
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
                // somewhat tricky line
                /*
                The next question is to ask yourself what happens when you found a repeated character? For example,
                if the string is “abcdcedf”, what happens when you reach the second appearance of ‘c’?

When you have found a repeated character (let’s say at index j), it means that the current substring (excluding the
repeated character of course) is a potential maximum, so update the maximum if necessary. It also means that the
repeated character must have appeared before at an index i, where i is less than j.

Since you know that all substrings that start before or at index i would be less than your current maximum,
you can safely start to look for the next substring with head which starts exactly at index i+1.
                 */

                while (input.charAt(i) != input.charAt(j)) {
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
        System.out.println(SubstringWithoutRepeating.getLongestSubstringWithoutRepeating("cabcd"));
     }
 }
