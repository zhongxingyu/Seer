 package algorithms.sorting;
 
 /**
  * Created by Yohan on 1/26/14.
  */
 public class SelectionSort<T> implements SortingAlgorithm<T>, IterationProcessingSortingAlgorithm {
     @Override
     public void sort(Comparable<T>[] a) {
         sort(a, new SortingIterationHandler() {
             @Override
             public void handle(Comparable[] a) {
                 //do nothing
             }
         });
     }
 
     @Override
     public void sort(Comparable[] a, SortingIterationHandler iterationHandler) {
         int N = a.length;
         for (int i = 0; i < N; i++) {
             int min = i;
             for (int j = i+1; j < N; j++)
                 if (Sorts.less(a[j], a[min])) min = j;
             Sorts.exch(a, i, min);
            iterationHandler.handle(a);
         }
     }
 }
