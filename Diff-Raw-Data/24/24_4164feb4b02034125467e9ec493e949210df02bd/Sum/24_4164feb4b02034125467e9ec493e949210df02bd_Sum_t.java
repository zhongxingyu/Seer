 package leetcode;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: zhuang
  * Date: 5/2/13
  * Time: 11:31 AM
  * To change this template use File | Settings | File Templates.
  *
  * Linear time algorithm to detect two sum
 
 
  Given an array of integers, find two numbers such that they add up to a specific target number.
 
  The function twoSum should return indices of the two numbers such that they add up to the target, where index1 must be less than index2. Please note that your returned answers (both index1 and index2) are not zero-based.
 
  You may assume that each input would have exactly one solution.
 
  Input: numbers={2, 7, 11, 15}, target=9
  Output: index1=1, index2=2
 
  */
 public class Sum {
 
     /**
      *
      * Need to be careful about the duplicate of two numbers,
      * This implementation is not space efficient, since we use HashMap to keep track of index which is not required.
      */
     public static int[] twoSum(int[] numbers, int target) {
         // Start typing your Java solution below
         // DO NOT write main() function
         int[] aux = new int[numbers.length];
         HashMap<Integer, Integer> valueToIndex = new HashMap<Integer, Integer>();
         for (int i = 0; i < numbers.length; i++) {
             valueToIndex.put(numbers[i], i);
             aux[i] = numbers[i];
         }
 
         int i = 0, j = numbers.length - 1;
         boolean found = false;
 
         Arrays.sort(aux);
 
         while (i != j) {
             if (aux[i] + aux[j] < target) i++;
             else if (aux[i] + aux[j] > target) j--;
             else {
                 found = true;
                 break;
             }
         }
 
         if (!found) return null;
 
         int[] result = new int[2];
         if (aux[i] == aux[j]) {  /* Handle duplicates */
             int first = -1, second = -1;
             for (int k = 0; k < numbers.length; k++) {
                 if (numbers[k] == aux[i]) {
                     if (first == -1) {
                         first = k;
                     } else if (second == -1) {
                         second = k;
                         break;
                     }
                 }
             }
             result[0] = first + 1;
             result[1] = second + 1;
             return result;
         }
 
 
         if (valueToIndex.get(aux[i]) < valueToIndex.get(aux[j])) {
             result[0] = valueToIndex.get(aux[i]) + 1;
             result[1] = valueToIndex.get(aux[j]) + 1;
         } else {
             result[0] = valueToIndex.get(aux[j]) + 1;
             result[1] = valueToIndex.get(aux[i]) + 1;
         }
         return result;
     }
 
     /**
      *
      * 1. Create a aux array and sort it in order
      * 2. Traverse the aux array using head and tail pointer to get the target result
      * 3. traverse again on the original array to generate result
      */
     public static int[] twoSumBetter(int[] numbers, int target) {
         int[] aux = new int[numbers.length];
         for (int i = 0; i < numbers.length; i++) {
             aux[i] = numbers[i];
         }
 
         int i = 0, j = numbers.length - 1;
         boolean found = false;
 
         Arrays.sort(aux);
 
         while (i != j) {
             if (aux[i] + aux[j] < target) i++;
             else if (aux[i] + aux[j] > target) j--;
             else {
                 found = true;
                 break;
             }
         }
 
         if (!found) return null;
         int firstTarget = aux[i], secondTarget = aux[j];
 
         int[] result = new int[2];
         boolean foundOne = false;
         int leftOver = -1;
         for (int k = 0; k < numbers.length; k++) {
 
             if (foundOne) {
                 if (numbers[k] == leftOver) {
                     result[1] = k + 1;
                     return result;
                 }
             }
 
             if (numbers[k] == firstTarget || numbers[k] == secondTarget) {
 
                 foundOne = true;
                 leftOver = numbers[k] == firstTarget ? secondTarget : firstTarget;
                 result[0] = k + 1;
             }
         }
 
         return result;
 
     }
 
 
     /**
      * Got this answer from Wiki 3-Sum, but there is bug for:
      * -4,-2,-2,-2,0,1,2,2,2,3,3,4,4,6,6
      *
     *
     * 1. Handle Array size less than 3
     * 2. Handle duplicate results, keep track of the first element
      *
      *
      * @param num
      * @return
      */
     public static ArrayList<ArrayList<Integer>> threeSum(int[] num) {
         // Start typing your Java solution below
         // DO NOT write main() function

         ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

        if (num.length < 3)  // handle edge case
            return result;

         int[] aux = Arrays.copyOf(num, num.length);
         Arrays.sort(aux);
 
         int j = 0, k =num.length - 1, oldA = -9999999, oldB = -9999999, oldC = -9999999;
         int oldStart = aux[0]; // keep track of start value
         for (int i = 0; i < aux.length - 2; i++) {
 
             if (i != 0 && aux[i] == oldStart) {
                 continue;
             }
 
             j = i + 1; // next to the start
             k = num.length - 1; // last index
 
             while (j != k) {
                 if (aux[i] + aux[j] + aux[k] == 0) {
                     if (aux[i] != oldA || aux[j] != oldB || aux[k] != oldC) {
                         // add it into array list
                         ArrayList<Integer> newArray = new ArrayList<Integer>();
                         newArray.add(aux[i]);newArray.add(aux[j]);newArray.add(aux[k]);
                         result.add(newArray);
                         oldA = aux[i]; oldB = aux[j]; oldC = aux[k];
 
                     }
                     k--;
                 } else if (aux[i] + aux[j] + aux[k] > 0) k--;
                 else if (aux[i] + aux[j] + aux[k] < 0) j++;
             }
 
            oldStart = aux[i]; // update old start
         }
 
         return result;
     }
 
     public static void main(String[] args) {
        int[] num = {-4,-2,-2,-2,0,1,2,2,2,3,3,4,4,6,6}; //
        //int[] num = {-1, -3, 1, -2, -1,5,6,2,1,0};
         System.out.println(Sum.threeSum(num));
     }
 }
