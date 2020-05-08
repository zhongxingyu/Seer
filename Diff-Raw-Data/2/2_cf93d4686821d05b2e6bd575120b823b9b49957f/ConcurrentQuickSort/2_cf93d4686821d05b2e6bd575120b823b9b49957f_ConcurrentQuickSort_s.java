 package rio.sorter;
 
 public class ConcurrentQuickSort {
 
     private final long[] array;
     private final int threshold;
     private final WorkQueue workQueue;
 
     public ConcurrentQuickSort(long[] array, int threshold) {
         this.array = array;
         this.threshold = threshold;
         workQueue = new WorkQueue();
     }
 
     public ConcurrentQuickSort(long[] array) {
        this(array, 50);  // 50 seems best when tested with testSpeedWithDifferentTresholds()
     }
 
     public void sort() {
         workQueue.addJob(new Job(0, array.length - 1));
         QuickSorterThread[] threads = new QuickSorterThread[4];
         for (int i = 0; i < threads.length; i++) {
             threads[i] = new QuickSorterThread(array, 0, workQueue);
             threads[i].run();
         }
     } 
 
     private void insertionSort() {
         for (int i = 0; i < array.length; i++) {
             long value = array[i];
             int j = i - 1;
             while (j >= 0 && array[j] > value) {
                 array[j + 1] = array[j];
                 j--;
             }
             array[j + 1] = value;
         }
     }
 }
