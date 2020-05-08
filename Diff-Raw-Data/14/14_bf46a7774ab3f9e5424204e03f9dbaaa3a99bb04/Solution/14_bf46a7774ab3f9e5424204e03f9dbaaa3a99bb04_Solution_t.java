 public class Solution {
     public int numDistinct(String S, String T) {
         if(S == null || T == null || S.isEmpty() || T.isEmpty())
             return 0;
         int m = S.length();
         int n = T.length();
         int[] prev = new int[m+1];
         int[] next = new int[m+1];
        int[] sumPrev= new int[m+1];
         prev[0] = 1;
 
         for(int i = 1; i<= n; ++i){
            sumPrev[i-1] = prev[i-1];
             for(int k = i; k<=m; ++k){
                sumPrev[k] = sumPrev[k-1]+prev[k];
                 if( T.charAt(i-1) == S.charAt(k-1) ){
                    next [k] = sumPrev[k-1];    
                 }else{
                     next [k] = 0;    
                 }
             }
             for(int k = 0;k<=m; ++k){
                 prev [k] = next[k];
                 next [k] = 0;
                sumPrev [k] = 0;
             }
         }
         
         int sum = 0;
         for(int k = 1;k<=m; ++k){
             sum += prev[k];
         }
         return sum;
     }
 }
