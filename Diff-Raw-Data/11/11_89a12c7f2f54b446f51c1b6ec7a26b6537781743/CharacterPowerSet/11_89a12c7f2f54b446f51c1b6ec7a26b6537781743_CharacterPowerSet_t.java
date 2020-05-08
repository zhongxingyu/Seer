 package guibin.zhang.leetcode.permutationAndCombination;
 
 /**
  *
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class CharacterPowerSet {
     
    /**
     * Combination, select k items from arr.length items.
     * @param arr
     * @param branch
     * @param idx
     * @param startId
     * @param k 
     */
     public void combine(char[] arr, char[] branch, int idx, int startId, int k) {
         
         if (k == idx) {
             System.out.println(branch);
             return;
         }
         
         for (int i = startId; i < arr.length; i++) {
             branch[idx++] = arr[i];
             combine(arr, branch, idx, ++startId, k);
             idx --;
         }
     }
     
     public void powerSet(char[] arr) {
         
        for (int i = 1; i < arr.length; i++) {
             char[] branch = new char[i];
             combine(arr, branch, 0,  0, i);
         }
     }
     
     public static void main(String[] args) {
         
         CharacterPowerSet cps = new CharacterPowerSet();
         char[] arr = "ABCDE".toCharArray();
         cps.powerSet(arr);
     }
 }
