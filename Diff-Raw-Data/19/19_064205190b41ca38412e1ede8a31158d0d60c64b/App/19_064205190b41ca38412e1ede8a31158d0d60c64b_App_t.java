 package rio.sorter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 
 public class App {
 
     public static void main(String[] args) throws FileNotFoundException {
 
        testSpeedWithDifferentThresholds();
     }
 
    private static void testSpeedWithDifferentThresholds() {
         long[] randomArray = genRandomLongArray();
 
 
        for (int threshold = 0; threshold < 100; threshold += 10) {
            testThreshold(randomArray, threshold);
         }
 
     }
 
     private static long[] genRandomLongArray() {
         int size = 50000000;
         long[] array = new long[size];
         for (int i = 0; i < size; i++) {
             array[i] = (long) (Math.random() * Long.MAX_VALUE);
         }
         return array;
     }
 
    private static void testThreshold(long[] randomArray, int threshold) {
         long[] times = new long[5];
         for (int i = 0; i < 5; i++) {
             long[] sortArray = Arrays.copyOf(randomArray, randomArray.length);
            ConcurrentQuickSort sorter = new ConcurrentQuickSort(sortArray, threshold);
             long startTime = System.nanoTime();
             sorter.sort();
             long elapsedTimeInMS = (System.nanoTime() - startTime) / 1000000;
             times[i] = elapsedTimeInMS;
         }
         Arrays.sort(times);
        System.out.println("Threshold: " + threshold + " Median time: " + times[2]);
     }
 }
