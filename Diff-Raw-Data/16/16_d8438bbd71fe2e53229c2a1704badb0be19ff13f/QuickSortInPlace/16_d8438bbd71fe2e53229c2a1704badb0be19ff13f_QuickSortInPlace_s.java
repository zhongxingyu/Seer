 package edu.msergey.jalg.sorting;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * In place quick sort with efficent memory use.
  * Pivot is 1st element.
  */
 public class QuickSortInPlace<E extends Comparable> extends SortWithProfiling<E> {
     public QuickSortInPlace(E[] data) {
         super(data);
     }
 
     @Override
     public E[] sort() {
         sort(data, 0, data.length - 1);
         return data;
     }
 
     private void sort(E[] a, int l, int r) {
        if (r - l <= 0) return;
 
         E pivot = a[l];
         int j = l + 1;
 
        for (int i = l + 1; i < r; i++) {
             if (a[i].compareTo(pivot) < 0) {
                 j++;
                E temp = a[i];
                a[i] = a[j];
                a[j] = temp;
             }
         }
        E temp = a[j-1];
        a[j-1] = a[l];
        a[l] = temp;
 
         sort(a, l, j-1);
         sort(a, j, r);
     }
 }
