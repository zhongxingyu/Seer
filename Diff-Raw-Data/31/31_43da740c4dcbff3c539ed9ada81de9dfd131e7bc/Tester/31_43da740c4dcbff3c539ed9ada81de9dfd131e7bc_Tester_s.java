 package primes13;
 
 import java.io.IOException;
 import java.util.*;
 
 public class Tester {
     private static LazySkipList cSkipList = null;
     private static Timer endTimer = new Timer(false);
     
     public static class RunTester implements Runnable {
         int id;
         long element;
         long[] iters;
         RandomSource rs;
 
         public RunTester(int id, long[] iters) {
             this.id = id;
             this.element = id;
             this.iters = iters;
         }
         
         public void run() {
             LazySkipList.Node[] elems = new LazySkipList.Node[2 * (LazySkipList.MAX_LEVEL + 1)];
             while(!shouldRun) ;
             long it = 0;
             for(; shouldRun; it++) {
                 element = (1103515245L * element + 12345L) & (RandomSource.m - 1);
                 cSkipList.add(element, elems);
                 cSkipList.remove(element, elems);
             }
             iters[id] = it;
         }
     }
     
     static volatile boolean shouldRun = false;
     static long startTime = 0, endTime;
     
     private static class KillTasks extends TimerTask {
         public void run() {
             shouldRun = false;
             endTime = System.nanoTime() / 1000000L;
         }
     }
     
    public static double test(int elements, int size, int numThreads, double runTimeApprox) {
         LazySkipList.Node[] elems = new LazySkipList.Node[2 * (LazySkipList.MAX_LEVEL + 1)];
 
         RandomSource rs = new RandomSource();
        if(size < elements) {
            for(int i = 0; i < elements - size; i++) {
                cSkipList.add(rs.next(), elems);
            }
        } else {
            for(int i = 0; i < size - elements; i++) {
                cSkipList.remove(rs.next(), elems);
            }
         }
 
         // Initialize threads
         Thread[] workers = new Thread[numThreads];
         long[] iter = new long[numThreads];
         for (int i = 0; i < numThreads; i++) {
             workers[i] = new Thread(new RunTester(i, iter));
             workers[i].start();
         }
 
         // Schedule stoping time
         endTimer.schedule(new KillTasks(), (long)(runTimeApprox * 60 * 1000));
         // Start iteration
         shouldRun = true;
         // Mark start
         startTime = System.nanoTime() / 1000000L;
         // Wait until all threads are done
         for (int i = 0; i < numThreads; i++) {
             try {
                 workers[i].join();
             } catch (InterruptedException e) {}
         }
         
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {}
 
         // Get total number of iterations finished
         long totalIters = 0;
         for (int i = 0; i < numThreads; i++) {
             totalIters += iter[i];
         }
         // Calculate rate (iterations / msec)
         return 1.0 * totalIters / (endTime - startTime);
     }
 
     public static void main(String[] args) {
         double runTimeApprox = Double.parseDouble(args[0]);
         int maxThreads = Integer.parseInt(args[1]);
         int threadStep = Integer.parseInt(args[2]);
         String className = "";
         cSkipList = new LazySkipList();
         
         LazySkipList.Node[] elems = new LazySkipList.Node[2 * (LazySkipList.MAX_LEVEL + 1)];
         
         // Seed skiplist with starting elements
         RandomSource rs = new RandomSource(-5);
         
        int finalElem = 10;
         int warmupElem = 1000000;
         for(int i = 0; i < warmupElem; i++) {
             long next = rs.next();
             cSkipList.add(next, elems);
         }
        rs = new RandomSource(-5);
        for(int i = 0; i < warmupElem - finalElem; i++) {
            cSkipList.remove(rs.next(), elems);
        }
         
         int[] threadN = new int[maxThreads / threadStep + 1];
         for(int i = 0; i < maxThreads / threadStep; i++) {
             threadN[threadN.length - i - 1] = maxThreads - threadStep * i;
         }
         threadN[0] = 1;
         System.out.println(Arrays.toString(threadN));
 
        int size = 1000;

         for(int numThreads : threadN) {
             try {
                 Runtime.getRuntime().exec("sudo set-cpus -n " + numThreads + " seq");
             } catch (IOException ex) {}
             int numElems = 10;
             Double[] rates = new Double[5];
            for(int i = 0; i < 5; i++) 
{                rates[i] = test(numElems, size, numThreads, runTimeApprox);
                size = numElems;
                 numElems *= 10;
             }
             // Log data points
             System.out.printf("%.4f,%.4f,%.4f,%.4f,%.4f\n", rates);
         }
 
         // Stop timer so program can exit
         endTimer.purge();
         endTimer.cancel();
     }
 }
