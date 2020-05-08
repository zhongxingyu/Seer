 public class Solution {
     public int sqrt(int x) {
         int find = -1;
         if(x<0)
             return find;
         int start = 0;
         int end = x;
        if(end > Math.sqrt(Integer.MAX_VALUE))
            end = (int)Math.sqrt(Integer.MAX_VALUE);
         while(start <= end){
             int mid = start + ((end-start)>>1); 
            if(mid * mid < x){
                 find = mid;
                 start = mid+1;
            }else if(mid * mid > x){
                 end = mid -1;
             }else{
                 return mid;
             }
         }
         return find;
     }
 }

