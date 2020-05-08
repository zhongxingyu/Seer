 /*
  * Copyright (c) 2012 Tao Ma. All rights reserved.
  */
 
 package groundwork.algorithms;
 
 /**
 * A few of Sort algorithms implementation.
  * User: Tao Ma
  * Date: 4/15/12
  * Time: 10:46 PM
  */
 public class Sort {
     public void insertionSort(int[] array) {
         if (null == array) return;
         if (array.length < 2) return;
 
         for (int i = 1; i < array.length; i++) {
             int key = array[i];
             int j = i - 1;
             while (j >= 0 && array[j] > key) {
                 array[j + 1] = array[j];
                 j--;
             }
             array[j + 1] = key;
         }
     }
 
     public void merge(int[] array) {
         if (null == array) return;
         if (array.length < 2) return;
         doMergeSort(array, 0, array.length / 2, array.length - 1);
     }
 
     private void doMergeSort(int[] array, int p, int q, int r) {
         if (p < r) {
             doMergeSort(array, p, (p + q) / 2, q);
             doMergeSort(array, q + 1, (q + r) / 2, r);
             doMerge(array, p, q, r);
         }
     }
 
     private void doMerge(int[] array, int p, int q, int r) {
         int[] lArray = new int[q - p + 1];
         int[] rArray = new int[r - q];
         System.arraycopy(array, p, lArray, 0, lArray.length);
         System.arraycopy(array, q + 1, rArray, 0, rArray.length);
 
         int lIndex = 0;
         int rIndex = 0;
         boolean lEnd = false;
         boolean rEnd = false;
         for (int i = p; i <= r; i++) {
             if (!lEnd && lArray[lIndex] <= rArray[rIndex] || rEnd) {
                 array[i] = lArray[lIndex];
                 if (lIndex + 1 < lArray.length) lIndex++;
                 else lEnd = true;
             } else {
                 array[i] = rArray[rIndex];
                 if (rIndex + 1 < rArray.length) rIndex++;
                 else rEnd = true;
             }
         }
     }
 
     public void quick(int[] array, int p, int r) {
         if (p < r) {
             int q = partition(array, p, r);
             quick(array, p, q - 1);
             quick(array, q + 1, r);
         }
     }
 
     private int partition(int[] array, int p, int r) {
         int x = array[r];
         int i = p - 1;
         for (int j = p; j < r; j++) {
             if (array[j] <= x) {
                 i++;
                 exchange(array, i, j);
             }
         }
         i++;
         exchange(array, i, r);
         return i;
     }
 
     private void exchange(int[] array, int p, int r) {
         int tmp = array[r];
         array[r] = array[p];
         array[p] = tmp;
     }
 }
