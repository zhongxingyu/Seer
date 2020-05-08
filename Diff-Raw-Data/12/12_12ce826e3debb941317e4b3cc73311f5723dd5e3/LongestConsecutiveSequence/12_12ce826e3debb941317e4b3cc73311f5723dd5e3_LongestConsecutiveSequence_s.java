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
        int leftLenght = numberLength.get(left);
        int rightLenght = numberLength.get(right);
        int totalLenght = leftLenght + rightLenght;
        longest = Math.max(longest, totalLenght);
        numberLength.put(right + rightLenght - 1, totalLenght);
        numberLength.put(left - leftLenght + 1, totalLenght);
     }
 }
