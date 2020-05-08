 package com.MyTestProjects.Concurrent.forkjoin.thresholdexperement;
 
 import java.util.concurrent.RecursiveAction;
 
 /**
  * Created in 8/18/13 11:15 PM
  * Author gary
  */
 public class ArrayDelimiter extends RecursiveAction {
 
     /**
      * The processing array
      */
     private double[] processingArr;
 
     /**
      * A num of a start to the array processing
      */
     private int start;
 
     /**
      * The threshold of the beginning of division
      */
     private long divThreshold;
 
     /**
      * A num of a start to the array processing
      */
     private int end;
 
     /**
      * Initialize the array delimiter
      *
      * @param processingArr
      * @param start
      * @param divThreshold
      * @param end
      */
     public ArrayDelimiter(double[] processingArr, int start, long divThreshold, int end) {
         this.processingArr = processingArr;
         this.start = start;
         this.divThreshold = divThreshold;
         this.end = end;
     }
 
     /**
      * The main computation performed by this task.
      */
     @Override
     protected void compute() {
         /* Check if the processing array is less then the limit of division*/
         if ((end - start) < divThreshold) {
             for (int index = 0; index < processingArr.length; index++) {
                 if (index % 2 == 0) {
                     processingArr[index] = Math.sqrt(processingArr[index]);
                 } else {
                     processingArr[index] = Math.cbrt(processingArr[index]);
                 }
             }
         } else {
            int mid = (start + end) / 2;
             ArrayDelimiter firstPart = new ArrayDelimiter(processingArr, start, divThreshold, mid);
             ArrayDelimiter secondPart = new ArrayDelimiter(processingArr, mid, divThreshold, end);
             invokeAll(firstPart, secondPart);
         }
     }
 }
