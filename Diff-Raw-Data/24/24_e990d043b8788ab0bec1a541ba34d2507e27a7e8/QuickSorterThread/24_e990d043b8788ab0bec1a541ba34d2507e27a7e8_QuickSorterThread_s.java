 package rio.sorter;
 
 public class QuickSorterThread implements Runnable {
 
     private final long[] array;
    private final int treshold;
     private final WorkQueue workQueue;
     private int leftmostIndex;
     private int rightmostIndex;
 
    public QuickSorterThread(long[] array, int treshold, WorkQueue workQueue) {
         this.array = array;
        this.treshold = treshold;
         this.workQueue = workQueue;
     }
 
     @Override
     public void run() {
         while (true) {            
             Job job = null;
             while (job == null) {
                 job = workQueue.getNextJob();
                 if (job == null) {
                     try {
                         workQueue.workAvailable.wait();
                     } catch (InterruptedException ex) {
                         System.out.println(ex);
                         return;
                     }
                 }
             }
             
             leftmostIndex = job.getLeftmostIndex();
             rightmostIndex = job.getRightmostIndex();
             
            if (rightmostIndex - leftmostIndex < treshold) {
                 continue;
             }
             if (leftmostIndex < rightmostIndex) {
                 int pivotIndex = leftmostIndex + (rightmostIndex - leftmostIndex) / 2;
                 pivotIndex = partition(leftmostIndex, rightmostIndex, pivotIndex);
                 workQueue.addJob(new Job(leftmostIndex, pivotIndex));
                 workQueue.addJob(new Job(pivotIndex + 1, rightmostIndex));
             }
         }
     }
 
     private int partition(int leftmostIndex, int rightmostIndex, int pivotIndex) {
         long pivotValue = array[pivotIndex];
         swapElements(pivotIndex, rightmostIndex);
         int newPivotIndex = leftmostIndex;
         for (int i = leftmostIndex; i < rightmostIndex; i++) {
             if (array[i] < pivotValue) {
                 swapElements(i, newPivotIndex);
                 newPivotIndex++;
             }
         }
         swapElements(newPivotIndex, rightmostIndex);
 
         return newPivotIndex;
     }
 
     private void swapElements(int index1, int index2) {
         long temp = array[index1];
         array[index1] = array[index2];
         array[index2] = temp;
     }
 }
