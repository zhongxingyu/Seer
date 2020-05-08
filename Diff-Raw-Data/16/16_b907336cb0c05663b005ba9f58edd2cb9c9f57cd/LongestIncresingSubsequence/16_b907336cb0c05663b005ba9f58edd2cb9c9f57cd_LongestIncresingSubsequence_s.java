 package other;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created by sobercheg on 12/3/13.
  */
 public class LongestIncresingSubsequence {
     static class Subsequence {
         int numOfElements;
         List<Integer> subsequence;
 
         @Override
         public String toString() {
             return "Subsequence{" +
                     "numOfElements=" + numOfElements +
                     ", subsequence=" + subsequence +
                     '}';
         }
     }
 
     public Subsequence getLongestIncreasingSubsequence(int[] array) {
         // lengths of longest increasing subsequences ending at s[i]
         int[] s = new int[array.length];
         for (int i = 0; i < s.length; i++) {
             s[i] = 1;
         }
         // indexes of previous elements for longest subsequences ending at s[i]
        int[] prev = new int[array.length];
        int currentLongest = 0;
         int longestEndingIndex = 0;
 
         // 1 6 2 9 3 1 4
         for (int i = 1; i < array.length; i++) {
             for (int j = i - 1; j >= 0; j--) {

                 if (array[j] < array[i]) {
                     if (s[j] >= s[i]) {
                         s[i] = s[j] + 1;
                         prev[i] = j;
                         if (s[i] > currentLongest) {
                             currentLongest = s[i];
                             longestEndingIndex = i;
                         }
                     }
                 }
             }
         }
         int index = longestEndingIndex;
         Subsequence subsequence = new Subsequence();
         subsequence.subsequence = new ArrayList<Integer>();
         subsequence.subsequence.add(array[longestEndingIndex]);
         int numOfElements = 1;
        while (index != prev[index]) {
             subsequence.subsequence.add(array[prev[index]]);
             numOfElements++;
             index = prev[index];
         }
         Collections.reverse(subsequence.subsequence);
         subsequence.numOfElements = numOfElements;
         return subsequence;
     }
 
     public static void main(String[] args) {
         LongestIncresingSubsequence longestIncresingSubsequence = new LongestIncresingSubsequence();
        Subsequence subsequence = longestIncresingSubsequence.getLongestIncreasingSubsequence(new int[]{1, 6, 2, 9, 3, 1, 4, 8, 5, 5, 5});
         System.out.println(subsequence);
     }
 }
