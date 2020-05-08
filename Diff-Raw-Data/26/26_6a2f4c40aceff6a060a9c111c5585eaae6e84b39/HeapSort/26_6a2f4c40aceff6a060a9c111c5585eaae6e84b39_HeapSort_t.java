 package com.raunak.sort;

 public class HeapSort {
 
     public static void main(String[] args) {
 
        Integer[] input = { 4,1,3,2,16,9,10,14,8,7 };
 
         Integer[] x = heapSort(input);
 
         for (Integer i : x) {
             System.out.println(i);
         }
     }
 
     private static Integer[] heapify(Integer[] input, int index, int heapSize) {
 
         int leftChild = 2 * index + 1;
         int rightChild = (2 * index) + 2;
 
         int largest = index;
         if (leftChild < heapSize && input[largest] < input[leftChild]) {
             largest = leftChild;
         }
 
         if (rightChild < heapSize && input[largest] < input[rightChild]) {
             largest = rightChild;
         }
 
         if (index != largest) {
             int largestVal = input[largest];
             input[largest] = input[index];
             input[index] = largestVal;
 
             return heapify(input, largest, heapSize);
         }
         return input;
     }
 
     private static Integer[] buildMaxHeap(Integer[] input) {
 
        for (int i = input.length / 2; i >= 0; i--) {
             input = heapify(input, i, input.length);
         }
 
         return input;
     }
 
     private static Integer[] heapSort(Integer[] input) {

         input = buildMaxHeap(input);

        int count = input.length - 1;

        for (int j = input.length - 1; j >= 1; j--) {

             int maxValue = input[0];
             input[0] = input[j];
             input[j] = maxValue;

             input = heapify(input, 0, count--);
         }

         return input;
     }
 }
