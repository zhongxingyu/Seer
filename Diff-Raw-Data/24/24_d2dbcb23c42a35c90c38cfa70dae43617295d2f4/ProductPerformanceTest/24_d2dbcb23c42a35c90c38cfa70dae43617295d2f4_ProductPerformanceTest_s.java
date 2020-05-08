 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat, Inc. and/or its affiliates,
  * and individual contributors as indicated by the @author tags.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  *
  * (C) 2011,
  * @author JBoss, by Red Hat.
  */
 package com.arjuna.ats.tools.perftest.task;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.*;
 import java.lang.reflect.Constructor;
 import java.util.*;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class ProductPerformanceTest
 {
     private static String[] configKeys = {
             "iterations",
             "threads",
             "objectStoreDir",
             "objectStoreType",
             "jts",
             "products",
             "stats",
     };
     private static int BATCH_SIZE = 100;
 
     private String products[];
     private Map<WorkerTask, TaskResult> tasks;
     private int iterations = 200000;
     private int threads = 100; // 1000 gets an OOM error
     private String productOpt = "com.arjuna.ats.tools.perftest.task.RHWorkerTask";
     private Properties options = new Properties();
     private Calendar calendar = Calendar.getInstance();
 
     private void hackEAPVersion() {
         String profile = System.getProperty("profile");
 
         if (!profile.startsWith("EAP"))
             return;
 
         for (int i = 0; i < products.length; i++) {
             if (products[i].endsWith("RHWorkerTask")) {
                 if (profile.equals("EAP6"))
                     products[i] = "com.arjuna.ats.tools.perftest.task.NarayanaWorkerTask";
                 else if (profile.equals("EAP5"))
                     products[i] = "com.arjuna.ats.tools.perftest.task.JBossTSWorkerTask";
                 else
                     System.out.printf("No suitable TS class for profile %s%n", profile);
             }
         }
     }
 
     @Before
     public void setUp() throws Exception {
         String configResource = "test1.properties";
         InputStream bis = Thread.currentThread().getContextClassLoader().getResourceAsStream(configResource);
 
         options.load(bis);
 
         // allow optiosn set via system proeprties to override ones set in the config file
         for (String key : configKeys) {
             String val = System.getProperty(key);
 
             if (val != null)
                 options.setProperty(key, val);
         }
 
         products = options.getProperty("products", productOpt).split(",");
         iterations = Integer.valueOf(options.getProperty("iterations", String .valueOf(iterations)));
         threads = Integer.valueOf(options.getProperty("threads", String .valueOf(threads)));
         tasks = new HashMap<WorkerTask, TaskResult>();
 
         hackEAPVersion();
         System.setProperty("iterations", String.valueOf(iterations));
 
         System.out.printf("iterations=%d threads=%d%n", iterations, threads);

        if (iterations < 100 || threads < 1) {
            throw new RuntimeException("Error minimum number of iterations and threads is 100 and 1 respectively");
        }
     }
 
     @Test
     public void testProduct() throws Exception {
         TaskResult results[] = new TaskResult[products.length];
 
         System.out.printf("%d transactions and %d threads%n", iterations, threads);
 
         for (int i = 0; i < products.length; i++)
             results[i] = testLoop(products[i], iterations, threads);
 
         System.out.printf("%nComparative Throughput:%n");
         for (int i = 0; i < products.length; i++)
             System.out.println(results[i].toString());
 
         printResults();
     }
 
     private void printResults() {
         try {
 //            String resDir = options.getProperty("resultsDir", "target");
 //            File file = File.createTempFile("perf", "txt", new File(resDir));
             File file = new File(options.getProperty("resultsFile", "target/results.txt"));
             boolean exists = file.exists();
             FileWriter writer = new FileWriter(file, true);
             PrintWriter output = new PrintWriter(writer);
             boolean jts = (options.getProperty("jts", "false")).equals("true");
             String store = options.getProperty("objectStoreType", "default");
             int i = store.lastIndexOf('.');
 
             if (i != -1)
                 store = store.substring(i + 1);
 
             if (!exists)
                 output.printf("%12s %15s %12s %12s %12s %8s %20s%n",
                     "Time of Day", "Product", "Throughput", "Iterations", "Threads", "JTS", "Store");
 
             for (Map.Entry<WorkerTask, TaskResult> entry: tasks.entrySet()) {
                 WorkerTask task = entry.getKey();
                 TaskResult res = entry.getValue();
 
                 output.printf("%12tT %15s %12d %12s %12s %8s %20s%n",
                         calendar, task.getName(), (int) res.getThroughput(), res.iterations, res.threads, jts, store);
                 task.reportErrors(output);
             }
 
             output.close();
         } catch (IOException e) {
             System.out.printf("%s%n", e.getMessage());
         }
     }
 
     private WorkerTask newWorker(
             String className, CyclicBarrier cyclicBarrier, AtomicInteger count, int batch_size) throws Exception {
 
         Class classDef = Class.forName(className);
         Constructor constructor = classDef.getDeclaredConstructor(
                 new Class[]{CyclicBarrier.class, AtomicInteger.class, int.class});
         Object[] args = {cyclicBarrier, count, batch_size};
 
         return (WorkerTask) constructor.newInstance(args);
     }
 
     private TaskResult testLoop(String workerClassName, int iterations, int threads) throws Exception {
         AtomicInteger count = new AtomicInteger(iterations/BATCH_SIZE);
         final int nThreads = threads;
         CyclicBarrier cyclicBarrier = new CyclicBarrier(nThreads +1); // workers + self
         ExecutorService executorService = Executors.newCachedThreadPool();
         WorkerTask task = newWorker(workerClassName, cyclicBarrier, count, BATCH_SIZE);
 
         task.init(options);
 
         for(int i = 0; i < nThreads; i++)
             executorService.execute(task);
 
         System.out.print(new Date() + " " + workerClassName);
         long start = System.nanoTime();
         cyclicBarrier.await();
         cyclicBarrier.await();
 
         long end = System.nanoTime();
         long duration_ms = (end - start) / 1000000L;
 
         executorService.shutdown();
         task.fini();
 
         tasks.put(task, new TaskResult(workerClassName, iterations, threads, duration_ms));
 
         return tasks.get(task);
     }
 
     static class TaskResult {
         String product;
         long duration_ms;
         int iterations;
         int threads;
 
         TaskResult(String product, int iterations, int threads, long duration_ms) {
             this.product = product;
             this.iterations = iterations;
             this.threads = threads;
             this.duration_ms =  duration_ms;
         }
 
         public StringBuilder toString(StringBuilder sb) {
             sb.append(product);
             sb.append("\n  total time (ms): ").append(duration_ms);
             sb.append("\naverage time (ms): ").append((1.0*duration_ms)/iterations);
             sb.append("\ntx / second: ").append((1000.0/((1.0*duration_ms)/iterations)));
             sb.append('\n').append(threads).append(" threads").append('\n');
 
             return sb;
         }
 
         public double getThroughput() {
              return (1000.0/((1.0*duration_ms)/iterations));
         }
 
         public String toString() {
             StringBuilder sb = new StringBuilder();
 
             sb.append(product).append(": ")
                     .append((1000.0/((1.0*duration_ms)/iterations))).append(" tx / second");
 
             return sb.toString();
         }
     }
 }
