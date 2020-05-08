 package guibin.zhang.leecode.permutationAndCombination;
 
 import java.util.Arrays;
 
 /**
  *
  * 
  * 
  * @author Gubin Zhang <guibin.beijing@gmail.com>
  */
 public class ThreeSumCloset {
     public int threeSumClosest(int[] num, int target) {
         // Start typing your Java solution below
         // DO NOT write main() function
         int len = num.length;
         if(len < 3) return 0;
         
        int closet = num[0] + num[1] + num[2];
         Arrays.sort(num);
         
         for (int i = 0; i < len - 2; i++) {
             int j = i + 1;
             int m = len - 1;
             while (j < m) {
                int sum = num[i] + num[j] + num[m];
                int delta = sum - target;
                 if (delta == 0) {
                    closet = sum;
                     return closet;
                 } else {
                    if (Math.abs(delta) < Math.abs(closet - target)) {
                        closet = sum;
                     }
                     if (delta > 0) {
                         m --;
                     } else {
                         j ++;
                     }
                 }
             }
         }
         return closet;
     }
     
     public static void main(String[] args) {
         ThreeSumCloset ts = new ThreeSumCloset();
         int[] num = {0, 0, 0};
         System.out.println(ts.threeSumClosest(num, 1));
     }
 }
