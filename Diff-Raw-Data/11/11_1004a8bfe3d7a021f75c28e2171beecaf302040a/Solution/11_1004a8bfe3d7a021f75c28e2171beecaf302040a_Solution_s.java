 public class Solution {
     public ArrayList<ArrayList<Integer>> threeSum(int[] num) {
         // Start typing your Java solution below
         // DO NOT write main() function
         ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
         
         if(num == null || num.length < 3) return res;
         Arrays.sort(num);
         int[]sorted = num;
         
         Map<Integer,Integer> countMap = new HashMap<Integer,Integer>();
         for( int i = 0; i< num.length; i++) {
             if(!countMap.containsKey(sorted[i])) {
                 countMap.put(sorted[i], 1);
                 continue;
             }
             Integer val = countMap.get(sorted[i]);
             countMap.put(sorted[i], val+1);
         }
         for( int i = 0; i < sorted.length; i ++) {
            if(i > 0 && sorted[i] == sorted[i-1]){
                 continue;
             }
             Integer val = countMap.get(sorted[i]);
             if( val == 1) {
                 countMap.remove(sorted[i]);
             }else {
                 countMap.put(sorted[i],val-1);
             }
             int sum = -1 * sorted[i];
             for(int j = i+1; j < sorted.length; j++ ){
                if(sorted[j] == sorted[j-1]){
                     continue;
                 }
                 Integer thisVal = countMap.get(sorted[j]);
                 if( thisVal == 1) {
                     countMap.remove(sorted[j]);
                 }else { 
                     countMap.put(sorted[j],thisVal-1);
                 }
                 if(sorted[j] > sum - sorted[j] ||
                     !countMap.containsKey(sum - sorted[j]) ){
                     countMap.put(sorted[j], thisVal);
                     continue;
                 }
 
                 /*  if( res.size() != 0){
                     ArrayList<Integer> lastRes = res.get(res.size()-1);
                     if( (lastRes.get(0) == sorted[i]) &&
                         (lastRes.get(1) == sorted[j]) &&
                         (lastRes.get(2) == sum - sorted[j]) ) {
                         countMap.put(sorted[j], thisVal);
                         continue;
                     }
                 }*/
                 ArrayList<Integer> thisRes = new ArrayList<Integer>();
                 thisRes.add(sorted[i]);
                 thisRes.add(sorted[j]);
                 thisRes.add(sum-sorted[j]);
                 res.add(thisRes);
                 countMap.put(sorted[j], thisVal);
             }
             countMap.put(sorted[i],val);
         }
         return res;
     }
 
 }
 
