 package guibin.zhang.leetcode.dp;
 
 /**
  * 
  * The longest Increasing Subsequence (LIS) problem is to 
  * find the length of the longest subsequence of a given sequence 
  * such that all elements of the subsequence are sorted in increasing order. 
  * 
  * For example, length of LIS for { 10, 22, 9, 33, 21, 50, 41, 60, 80 } is 6 
  * and LIS is {10, 22, 33, 50, 60, 80}.
  * 
  * Question link:
  * http://www.geeksforgeeks.org/dynamic-programming-set-3-longest-increasing-subsequence/
  * https://www.youtube.com/watch?v=SZByPn0deMY
  * 
  * @author Guibin Zhang <guibin.beijing@gmail.com>
  */
 public class LongestIncreasingSubsquence {
     
     /** 
      * To make use of recursive calls, this function must return two things.
      * 1) Length of LIS ending with element arr[n-1]. We use max_ending_here 
      * for this purpose
      * 2) Overall maximum as the LIS may end with an element before arr[n-1] 
      * max_ref is used this purpose.
      * The value of LIS of full array of size n is stored in int[] max_ref which is our final result
      * 
      * @param arr 
      * @param endingIdx endingIdx exclusive
      * @param maxRef Save the max number of list till now
      * @return The number of lis of arr[endingIdx - 1]
      */
     public int lisNaive(int[] arr, int endingIdx, int[] maxRef) {
         
         // Base case
         if(endingIdx == 1) return 1;
         int res, maxEndingHere = 1;// length of LIS ending with arr[n-1]
         
         /**
          * Recursively get all LIS ending with arr[0], arr[1] ... ar[n-2]. If 
          * arr[i-1] is smaller than arr[n-1], and max ending with arr[n-1] needs
          * to be updated, then update it 
         */
         for (int i = 1; i < endingIdx; i++) {
             res = lisNaive(arr, i, maxRef);
             if (arr[i - 1] < arr[endingIdx -1] && res + 1 > maxEndingHere) {
                 maxEndingHere = res + 1;
             }
         }
         // Compare max_ending_here with the overall max. And update the
         // overall max if needed
         if (maxEndingHere > maxRef[0]) {
             maxRef[0] = maxEndingHere;
         }
         
         return maxEndingHere;
     }
     
     public int lis(int[] arr, int n) {
         
         int[] maxRef = {1};
         lisNaive(arr, n , maxRef);
         return maxRef[0];
     }
     
     /**
      * 
      * @param arr
      * @return 
      */
     public int lisDP(int[] arr) {
         
         // counter[i] to save the number of lis till arr[i]
         int[] counter = new int[arr.length];
         String[] paths = new String[arr.length];
         // Initialize counter values for all index as 1.
         for (int i = 0; i < counter.length; i++) {
             counter[i] = 1;
            paths[i] = i + " ";
         }
         
         // Compute optimized lis value in bottom up manner
         int maxLength = 1;
         for (int i = 1; i < arr.length; i++) {
             for (int j = 0; j < i; j++) {//Check all the previous elements
                 if (arr[i] > arr[j] && counter[i] < counter[j] + 1) {
                     counter[i] = counter[j] + 1;
                     paths[i] = paths[j] + arr[i] + " ";
                     if (counter[i] > maxLength) {
                         maxLength = counter[i];
                     }
                 }
             }
         }
         
         // Pick up the maximum of all lis values
         for (int i = 0; i < arr.length; i++) {
             if (counter[i] == maxLength) {
                 System.out.println("Maxium length of lis: " + paths[i]);
             }
         }
         
         return maxLength;
     }
     
     public static void main(String[] args) {
         int arr[] = { 10, 22, 9, 33, 21, 50, 41, 60 };
         LongestIncreasingSubsquence lis = new LongestIncreasingSubsquence();
         System.out.println(lis.lis(arr, 8));
         
         System.out.println(lis.lisDP(arr));
     }
 }
