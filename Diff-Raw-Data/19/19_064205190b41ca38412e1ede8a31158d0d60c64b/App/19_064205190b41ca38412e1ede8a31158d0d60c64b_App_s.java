 package rio.sorter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 
 public class App {
 
     public static void main(String[] args) throws FileNotFoundException {
 
        testSpeedWithDifferentTresholds();
     }
 
    private static void testSpeedWithDifferentTresholds() {
         long[] randomArray = genRandomLongArray();
 
 
        for (int treshold = 0; treshold < 100; treshold += 10) {
            testTreshold(randomArray, treshold);
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
 
    private static void testTreshold(long[] randomArray, int treshold) {
         long[] times = new long[5];
         for (int i = 0; i < 5; i++) {
             long[] sortArray = Arrays.copyOf(randomArray, randomArray.length);
            ConcurrentQuickSort sorter = new ConcurrentQuickSort(sortArray, treshold);
             long startTime = System.nanoTime();
             sorter.sort();
             long elapsedTimeInMS = (System.nanoTime() - startTime) / 1000000;
             times[i] = elapsedTimeInMS;
         }
         Arrays.sort(times);
        System.out.println("Treshold: " + treshold + " Median time: " + times[2]);
     }
 }
