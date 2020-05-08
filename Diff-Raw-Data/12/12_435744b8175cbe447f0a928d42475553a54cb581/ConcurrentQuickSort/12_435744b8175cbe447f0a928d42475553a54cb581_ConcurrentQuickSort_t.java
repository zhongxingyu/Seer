 package rio.sorter;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ConcurrentQuickSort {
 
     private long[] array;
     
     private ExecutorService pool;
     private int taskCount;
     
     public ConcurrentQuickSort(long[] array) {
         
         this.array = array;
         
         int cores = Runtime.getRuntime().availableProcessors();
         taskCount = 0;
         pool = Executors.newFixedThreadPool(cores);
     }
     
     public void sort() {
         
         addTask(array, 0, array.length - 1);
         
         try {
             waitUntilDone();
         } catch (InterruptedException exception) {
             Logger.getLogger(ConcurrentQuickSort.class.getName()).log(Level.SEVERE, null, exception);
         }
     }
     
     public synchronized void addTask(long[] array, int from, int to) {
         
         taskCount++;
         
         Runnable task = new ConcurrentQuickSortTask(this, array, from, to);
         pool.execute(task);
     }
     
     public synchronized void taskDone() {
         
         taskCount--;
         
        if (taskCount == 0) {
             notify();
         }
     }
     
     public synchronized void waitUntilDone() throws InterruptedException {
         
         while (taskCount > 0) {
             wait();
         }
         
         pool.shutdown();
     }
 }
