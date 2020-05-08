 package com.soebes.multithreading.cp;
 
import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.log4j.Logger;
 import org.testng.annotations.Test;
 
 /**
  * @author Karl Heinz Marbaise
  */
 public class MemorizerTest {
     private static final Logger LOGGER = Logger.getLogger(MemorizerTest.class);
 
     public VersionRange createRange(int start, int end) {
         List<Version> versionRange = new ArrayList<Version>();
         for (int i = start; i < end; i++) {
             versionRange.add(new Version(Integer.toString(i)));
         }
         VersionRange v = new VersionRange(versionRange);
         return v;
     }
 
     public int calculateNumberOfThreads(double utilization, double waittime, double computetime) {
         int numberOfCpus = Runtime.getRuntime().availableProcessors();
 
         double u_cpu = utilization; // (0..1) target CPU utilization
         double W = waittime; // wait time.
         double C = computetime; // compute time
         int threads = (int) (numberOfCpus * u_cpu * (1.0 + (W / C)));
         LOGGER.info("Number of CPU's: " + numberOfCpus
                 + " utilization:" + utilization
                 + " waittime:" + waittime
                 + " computetime:" + computetime +
                 " threads:" + threads);
         return threads;
     }
 
     @Test
     public void firstStart() throws InterruptedException, ExecutionException {
 
         int numberOfThreads = calculateNumberOfThreads(0.9, 1, 1);
 
         ExecutorService exec = Executors.newFixedThreadPool(numberOfThreads);
 
         ExecutorCompletionService<Index> execCompletion = new ExecutorCompletionService<Index>(exec);
 
         Integer numberOfStartedTasks = new Integer(0);
         Integer numberOfStoppedTasks = new Integer(0);
 
         for (int i = 0; i < 100; i++) {
             int start = i * 100 + 1;
             int ende = i * 100 + 100;
             LOGGER.info("start:" + start + " ende:" + ende);
 
             DoSomethingTimeConsuming task = new DoSomethingTimeConsuming(new Long(i), createRange(start, ende));
 
             LOGGER.info("exec.submit(" + i + ", task)");
 
             execCompletion.submit(task);
 
             numberOfStartedTasks++;
 
         }
 
         LOGGER.info("Submitted all tasks.");
 
         List<Index> indexerTasks = new ArrayList<Index>();
 
         // Check if all started tasks have been finished.
         while (numberOfStoppedTasks < numberOfStartedTasks) {
             // No not yet.
             Future<Index> result = execCompletion.poll();
             if (result == null) {
                 // LOGGER.info("No task has stopped.");
                 // Nothing stopped yet.
                 continue;
             }
 
             indexerTasks.add(result.get());
             numberOfStoppedTasks++;
             LOGGER.info("Task has stopped.");
 
             //This must be defined as a constant or may be controlled by the size of a pool ? (ThreadPool ?)
             if (indexerTasks.size() > 8) {
                 //
                 LOGGER.info("We got at least 8 producer tasks. Run mergeIndex()");
                 // mergeIndex (producerTasks);
                 String s = "";
                 for (Index index : indexerTasks) {
                     s += "#" + index.getName();
                 }
 //                Index dest = new Index("Name", new File("test"));
 //                dest.setName("D-" + s);
 //                IndexMerger indexMerge = new IndexMerger(dest, indexerTasks);
 //                execCompletion.submit(indexMerge);
                 indexerTasks = new ArrayList<Index>();
             }
         }
 
         // Are there some stopped task left over?
         if (indexerTasks.size() > 0) {
             LOGGER.info("We got at least 1 producer tasks. Run mergeIndex()");
             //
             // mergeIndex (producerTasks);
         }
 
     }
 }
