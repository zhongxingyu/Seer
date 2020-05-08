 package algorithm;
 
 public class TwoWayMerge {
 
     public static void main(String[] args) {
        int[] a = {1, 4};
        int[] b = {2, 3};
 
         int[] ints = two_way_merge(a, b);
 
         System.out.println(ints);
 
     }
 
     public static int[] two_way_merge(int[] a, int[] b) {
 
         int[] re = new int[a.length + b.length];
 
         int i = 0;
         int j = 0;
         int k = 0;
 
         while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) {
                 re[k++] = a[i++];
             } else {
                 re[k++] = b[j++];
             }
         }
 
        while (i < a.length) {
             re[k++] = a[i++];
         }
         while (j < b.length) {
             re[k++] = b[j++];
         }
 
         return re;
     }
 }
