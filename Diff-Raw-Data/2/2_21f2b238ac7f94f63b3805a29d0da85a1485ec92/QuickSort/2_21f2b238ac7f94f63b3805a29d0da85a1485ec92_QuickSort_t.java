 package algorithm;
 
 public class QuickSort {
     public static void qsort(int[] a, int start, int end) {
         if (end <= start) return;
         int left = start;
         int right = end;
         int pivot = a[left + (right - left) / 2];
         while (left <= right) {
             while (a[left] < pivot) left++;
             while (a[right] > pivot) right--;
             if (left <= right) {
                 int tmp = a[left];
                 a[left] = a[right];
                 a[right] = tmp;
                 left++;
                 right--;
             }
         }
         if (left < end) qsort(a, left, end);
        if (start < right) qsort(a, start, right);
     }
 
     public static void main(String[] args) {
         int[] a = {4, 5, 3, 2, 1};
 
         for (int i : a) {
             System.out.print(i + ",");
         }
         System.out.println("start--->\r\n");
         qsort(a, 0, a.length - 1);
 
         System.out.println("");
         System.out.println("<-----end\r\n");
         for (int i : a) {
             System.out.print(i + ",");
         }
 
     }
 }
