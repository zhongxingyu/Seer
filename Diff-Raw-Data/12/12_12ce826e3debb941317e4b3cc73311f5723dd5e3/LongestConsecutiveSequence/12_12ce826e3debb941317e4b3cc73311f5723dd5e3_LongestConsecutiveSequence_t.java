 package leetcode;
 
 import java.util.HashMap;
 
 /**
  * User: huangd
  * Date: 2/21/13
  * Time: 11:10 PM
  */
 public class LongestConsecutiveSequence {
 
     private HashMap<Integer, Integer> numberLength;
     private int longest;
 
     public int longestConsecutive(int[] num) {
         // Start typing your Java solution below
         // DO NOT write main() function
         if (num == null || num.length == 0) {
             return 0;
         }
 
         numberLength = new HashMap<Integer, Integer>();
         longest = 1;
         for (int i = 0; i < num.length; ++i) {
             if (numberLength.containsKey(num[i])) {
                 continue;
             } else {
                 numberLength.put(num[i], 1);
                 if (numberLength.containsKey(num[i] + 1)) {
                     updateEdge(num[i], num[i] + 1);
                 }
                 if (numberLength.containsKey(num[i] - 1)) {
                     updateEdge(num[i] - 1, num[i]);
                 }
             }
         }
         return longest;
     }
 
     private void updateEdge(int left, int right) {
        int leftLength = numberLength.get(left);
        int rightLength = numberLength.get(right);
        int totalLength = leftLength + rightLength;
        longest = Math.max(longest, totalLength);
        numberLength.put(right + rightLength - 1, totalLength);
        numberLength.put(left - leftLength + 1, totalLength);
     }
 }
