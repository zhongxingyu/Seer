 import java.util.concurrent.*;
 
 public class MergeSort {
     private static ForkJoinPool threadPool;
     private static final int DIV_THRESHOLD = 16;
 
     public static void sort(Comparable[] a) {
         threadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
         Comparable[] sorted = new Comparable[a.length];
         threadPool.invoke(new SortTask(a, sorted, 0, a.length - 1));
     }
 
     static class SortTask extends RecursiveAction {
         private Comparable[] source, sorted;
         private int lowerIndex, upperIndex;
 
        public SortTask(Comparable[] source, Comparable[] sorted, 
                             int lowerIndex, int upperIndex) {
             this.source = source;
             this.sorted = sorted;
             this.lowerIndex = lowerIndex;
             this.upperIndex = upperIndex;
         }
 
         @Override
         protected void compute() {
             if(upperIndex - lowerIndex < DIV_THRESHOLD) {
                 insertionSort(source, lowerIndex, upperIndex);
                 return;
             }
 
             int midIndex = (lowerIndex + upperIndex) >>> 1;
            invokeAll( new SortTask(source, sorted, lowerIndex, midIndex), 
                        new SortTask(source, sorted, midIndex + 1, upperIndex) );
             merge(source, sorted, lowerIndex, midIndex, upperIndex);
         }
     }
 
     private static void merge(Comparable[] source, Comparable[] sorted,
                               int lowerIndex, int midIndex, int upperIndex) {
         if(source[midIndex].compareTo(source[midIndex+1]) <= 0)
             return;
 
        System.arraycopy(source, lowerIndex, sorted, 
                             lowerIndex, midIndex - lowerIndex + 1);
 
         int i = lowerIndex;
         int j = midIndex + 1;
         int k = lowerIndex;
 
         while(k < j && j <= upperIndex) {
             if(sorted[i].compareTo(source[j]) <= 0)
                 source[k++] = sorted[i++];
             else
                 source[k++] = source[j++];
         }
         System.arraycopy(sorted, i, source, k, j - k);
     }
 
    private static void insertionSort(Comparable[] a, 
                                         int li, int ui) {
         for(int i = li + 1; i <= ui; i++) {
             int j = i;
             Comparable t = a[j];
             while(j > li && t.compareTo(a[j-1]) < 0) {
                 a[j] = a[j-1];
                 --j;
             }
             a[j] = t;
         }
     }
 }
 
