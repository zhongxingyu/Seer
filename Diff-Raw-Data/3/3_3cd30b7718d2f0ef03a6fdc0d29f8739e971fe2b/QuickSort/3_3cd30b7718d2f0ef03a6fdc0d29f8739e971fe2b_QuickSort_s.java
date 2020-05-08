 package com.id.math.sort;
 
 
 import com.id.math.util.ArrayUtils;
 
 /**
  * @author idanilov
  * @complexity in worse case : O(n*n), in worse case : O(n*ln(n)).
  * @stable false
  */
 public class QuickSort extends AbstractSort {
 
     public int[] sort(int[] d) {
        //data = ArrayUtils.copy(d);
        data = new int[] {3,2,1};
         quickSort(0, data.length - 1);
         return data;
     }
 
     private void quickSort(int start, int end) {
         if (start >= end) {
             return;
         }
         int i = start, j = end;
         int mid = (i + j) / 2;
         while (i < j) {
             while ((i < mid) && (data[i] <= data[mid])) {
                 i++;
             }
             while ((j > mid) && (data[j] >= data[mid])) {
                 j--;
             }
             if (i < j) {
                 ArrayUtils.swap(data, i, j);
                 if (i == mid) {
                     mid = j;
                 } else if (j == mid) {
                     mid = i;
                 }
             }
         }
         quickSort(start, mid);
         quickSort(mid + 1, end);
     }
 
     public static void main(String[] args) {
         System.err.println("-=QUICK SORT=-");
         int[] source = TestData.RANDOM;
         System.err.println("Source:");
         ArrayUtils.printArray(source);
 
         int[] result = new QuickSort().sort(source);
         System.err.println("Result:");
         ArrayUtils.printArray(result);
     }
 
 }
