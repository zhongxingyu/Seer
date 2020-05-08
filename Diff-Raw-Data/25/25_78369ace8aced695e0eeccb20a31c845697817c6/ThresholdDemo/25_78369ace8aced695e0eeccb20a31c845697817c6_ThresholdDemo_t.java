 package com.MyTestProjects.Concurrent.forkjoin.thresholdexperement;
 
import java.util.Arrays;
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveAction;
 
 /**
  * Created in 8/18/13 11:31 PM
  * Author gary
  */
 public class ThresholdDemo {
     public static void main(String[] args) {
         int arrSize;
         int coresSize;
         int divThreshold;
 
         if (args.length < 3) {
             throw new IllegalArgumentException("Incorrect args." +
                     " Please enter the array size, num of cores and the threshold of division.");
         }
 
         try {
         arrSize = Integer.parseInt(args[0]);
         coresSize = Integer.parseInt(args[1]);
         divThreshold = Integer.parseInt(args[2]);
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Incorrect format of args." +
                     " Please enter the array size, num of cores and the threshold of division.");
         }
 
         ForkJoinPool pool = new ForkJoinPool(coresSize);
         double[] arr = new double[arrSize];
 
         for (int i = 0; i < arrSize; i++) {
            String value = i + "." + String.valueOf(i).charAt(0) + String.valueOf(i).charAt(String.valueOf(i).length() -1);
             arr[i] = Double.parseDouble(value);
         }
 
         System.out.printf("Start the array processing. Array size: %d Num of cores: %d Threshold of division: %d\n",
                 arrSize, coresSize, divThreshold);
 
         RecursiveAction task = new ArrayDelimiter(arr, 0, divThreshold, arrSize);
         long startTime = System.nanoTime();
         pool.invoke(task);
         long endTime = System.nanoTime();
 
         System.out.printf("End the array processing. Array size: %d Num of cores: %d Threshold of division: %d\n" +
                " After processing array is: %s\n Executing time is %d\n",
                arrSize, coresSize, divThreshold, Arrays.toString(arr), (endTime - startTime));
     }
 }
