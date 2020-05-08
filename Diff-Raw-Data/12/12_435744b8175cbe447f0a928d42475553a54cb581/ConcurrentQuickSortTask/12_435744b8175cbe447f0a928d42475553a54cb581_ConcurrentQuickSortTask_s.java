 package rio.sorter;
 
 public class ConcurrentQuickSortTask implements Runnable {
 
     private ConcurrentQuickSort sorter;
     
     private long[] array;
     private int from;
     private int to;
 
     public ConcurrentQuickSortTask(ConcurrentQuickSort sorter, long[] array, int from, int to) {
         
         this.sorter = sorter;
         this.array = array;
         this.from = from;
         this.to = to;
     }
 
     @Override
     public void run() {
         
         // Insertion sort after threshold of 700 values
         if (to - from < 700) {
             insertionSort(from, to);
             sorter.taskDone();
             return;
         }
         
        if (from < to) {
            
             int pivotIndex = from + (to - from) / 2;
             pivotIndex = partition(from, to, pivotIndex);
             
             sorter.addTask(array, from, pivotIndex);
             sorter.addTask(array, pivotIndex + 1, to);
             sorter.taskDone();
         }
     }
     
     private int partition(int from, int to, int pivotIndex) {
         
         long pivotValue = array[pivotIndex];
         swapElements(pivotIndex, to);
         
         int newPivotIndex = from;
         for (int i = from; i < to; i++) {
             if (array[i] < pivotValue) {
                 swapElements(i, newPivotIndex);
                 newPivotIndex++;
             }
         }
         
         swapElements(newPivotIndex, to);
 
         return newPivotIndex;
     }
 
     private void swapElements(int index1, int index2) {
         
         long temp = array[index1];
         array[index1] = array[index2];
         array[index2] = temp;
     }
     
     private void insertionSort(int from, int to) {
             
         for (int i = from; i <= to; i++) {
            
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
