 /*10:36*/
 public class Solution {
     public int largestRectangleArea(int[] height) {
         if (height == null || height.length == 0)
             return 0;
 
         int len = height.length;
         int max = 0;
         int dp [][] = new int [2][len];
         for (int i = 0; i<2; ++i)
             dp [i] = new int[len];
 
         for (int k = 0; k<len; ++k){
             for(int i =0; i+k <len; ++i ){
                 if(k==0){
                     dp[k%2][i] = height[i];
                     if(height[i] > max)
                         max = height[i];
                     continue;
                 }
                 int j = i+k; 
                 if(height[i]<height[j]){
                     int min = (dp[(k-1)%2][i] < height[i])?(dp[(k-1)%2][i]):(height[i]);
                     dp [k%2][i] = min;
                     if(min*(k+1) > max)
                         max = min*(k+1);
                 }else{
                     int min = (dp[(k-1)%2][i+1] < height[j])?(dp[(k-1)%2][i+1]):(height[j]);
                     dp [k%2][i] = min;
                     if(min*(k+1) > max)
                         max = min*(k+1);
                 }
             }
         }
         return max;
     }
 
     public static void main(String[] args){
         int [] test = {2};
         System.out.println(new Solution().largestRectangleArea(test));
     }
 
 }
