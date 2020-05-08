 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 public class ThreeSum {
     
     public ArrayList<ArrayList<Integer>> threeSum(int[] num) {
 
         ArrayList<ArrayList<Integer>> out = new ArrayList<ArrayList<Integer>>();
         
         Arrays.sort(num);
         int length = num.length;
         HashSet<String> filter = new HashSet<String>();
         
        
         for (int i = 0; i < length; i++) {
            for (int j = i+1; j < length - 1; j++) {
                 int rest = 0 - num[i] - num[j];
                 int k = -1;
                 int lo = j + 1; 
                 int high = length -1;
                 while (lo <= high) {
                     int mid = lo + (high - lo)/2;
                     if (num[mid] < rest)
                         lo = mid + 1;
                     else if (num[mid] > rest)
                         high = mid -1;
                     else {
                         k = mid;
                         break;
                     }
                         
                 }
                 
                 if (k != -1) {
                     
                     String key = new String(num[i]+" "+num[j]+" "+num[k]);
                     if (!filter.contains(key)) {
                         ArrayList<Integer> item = new ArrayList<Integer>();
                         item.add(num[i]);
                         item.add(num[j]);
                         item.add(num[k]);
                         out.add(item);
                         filter.add(key);
                     }
                   
                     
                 }
                 
             }
             
             
         }
         
         return out;
     }
     
     public static void printArray(ArrayList<ArrayList<Integer>> input) {
         if (input == null)
             return;
         int size = input.size();
         System.out.print("("+size+") ");
         
         for (int i = 0; i < size; i++) {
             ArrayList<Integer> item = input.get(i);
             System.out.print("[");
             int idx = item.size()-1;
             for (int j = 0; j < idx; j++)
                 System.out.print(item.get(j)+",");
             System.out.print(item.get(idx)+"] ");
         }
         System.out.println();   
     }
     public static void main(String[] args) {
         ThreeSum sum = new ThreeSum();
         int[] a24 = {-2, 0, 1, 1, 2};
         int[] a26 = {-1, 0, 1, 2, -1, -4};
         int[] a28 = {-4, -2, -2, -2, 0, 1, 2, 2,
                 2, 3, 3, 4, 4, 6, 6};
         printArray(sum.threeSum(a24));
         printArray(sum.threeSum(a26));
         printArray(sum.threeSum(a28));
     }
 
 }
