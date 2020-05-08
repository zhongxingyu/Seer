 package algorithms.sorting;
 
 /**
  * Created by Yohan on 2/8/14.
  */
 public class ShellSort<T> implements SortingAlgorithm<T>, IterationProcessingSortingAlgorithm {
     @Override
     public void sort(Comparable[] a) {
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
         int h = 1;
         while (h < N / 3) {
             h = 3 * h + 1;
         }
         while (h >= 1) {
             for (int i = h; i < N; i++) {
                iterationHandler.handle(a);
                 for (int j = i; j >= h && Sorts.less(a[j], a[j - h]); j -= h) {
                     Sorts.exch(a, j, j - h);
                 }
             }
             h = h / 3;
         }
     }
 }
