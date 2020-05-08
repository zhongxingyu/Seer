 package dynprog;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * User: absharma
  * Date: 4/9/12
  */
 public class CoinChange {
 
     public int[] greedy(int amt, int[] denominations) {
         int[] out = new int[denominations.length];
         _greedy(amt, denominations, 0, out);
         return out;
     }
 
     private void _greedy(int amt, int[] denominations, int index, int[] out) {
         if (amt == 0 || index >= denominations.length) {
             if (amt != 0) {
                 throw new RuntimeException("Error in solving coin change - remaining amount " + amt);
             }
             return;
         }
 
         int quotient = amt / denominations[index];
         int remainder = amt % denominations[index];
         out[index] = quotient;
         _greedy(remainder, denominations, ++index, out);
     }
 
     //todo less recursive
 
     public int dynamicProgramming(int amt, int[] denominations) {
         int[] out = new int[amt + 1];
         int num = _dynamicProgramming(amt, denominations, out);
         Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
        for (int j = 0; j < denominations.length; j++) {
            map.put(denominations[j], 0); // initialize the map
        }
         getDenominationsArray(out, amt, map);
         int[] arr = new int[denominations.length];
         int i = denominations.length - 1;
         for(Integer key : map.keySet()) {
             arr[i] = map.get(key);
             i--;
         }
         for (int j = 0; j < arr.length; j++) {
             System.out.println(denominations[j] + " * " +  arr[j]) ;
         }
         return num;
     }
 
    /**
     * Get the list of denominations definitely included in an amount recursively using the out array
     */
     private void getDenominationsArray(int[] out, int amt, Map<Integer, Integer> map) {
         if(amt > 0) {
            map.put(out[amt], map.get(out[amt]) + 1);
             getDenominationsArray(out, amt - out[amt], map);
         }
     }
 
 /*
 for a denomination of [2, 1]
 
 c[0] = 0
 c[1] = 1 + min {c[1 - 2], c[1 - 1]} = 1 + c[0] = 1
 c[2] = 1 + min {c[2 - 2], c[2 - 1]} = 1 + min {c[0], c[1]} = 1 + 0 = 1
 c[3] = 1 + min {c[3 - 2], c[3 - 1]} = 1 + min {c[1], c[2]} = 1 + 1 = 2
 c[4] = 1 + min {c[4 - 2], c[4 - 1]} = 1 + min {c[2], c[3]} = 1 + 1 = 2
 c[5] = 1 + min {c[5 - 2], c[5 - 1]} = 1 + min {c[3], c[4]} = 1 + 2 = 3
 
 also store the coin denomination definitely used in a solution - contained in out array
  */
 
     public int _dynamicProgramming(int amt, int[] denominations, int[] out) {
         int[] counts = new int[amt + 1];
         counts[0] = 0;
         for (int j = 1; j <= amt; j++) {
             int min = Integer.MAX_VALUE;
             int used = -1;
             for (int i = 0; i < denominations.length; i++) {
                 int index = j - denominations[i];
                 if (index >= 0) {
                     if (min > counts[index]) {
                         min = counts[index];
                         used = denominations[i];
                     }
                 }
             }
             counts[j] = 1 + min;
             out[j] = used;
 //            System.out.println(j + " = " + counts[j]);
         }
         return counts[amt];
     }
 }
 
