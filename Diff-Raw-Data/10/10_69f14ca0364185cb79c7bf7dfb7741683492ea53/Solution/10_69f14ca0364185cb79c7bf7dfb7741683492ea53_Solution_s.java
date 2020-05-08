 public class Solution {
     public int sqrt(int x) {
         int find = -1;
         if(x<0)
             return find;
         int start = 0;
         int end = x;
         while(start <= end){
             int mid = start + ((end-start)>>1); 
            if(start * start < x){
                 find = mid;
                 start = mid+1;
            }else if(start * start > x){
                 end = mid -1;
             }else{
                 return mid;
             }
         }
         return find;
     }
 }
