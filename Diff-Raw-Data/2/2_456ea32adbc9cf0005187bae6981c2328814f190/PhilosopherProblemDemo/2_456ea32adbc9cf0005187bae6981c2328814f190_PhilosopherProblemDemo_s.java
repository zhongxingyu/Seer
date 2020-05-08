 /**
  *  Home work 1.
  *  Philosopher problem.
  *
  *  Features:
  *      - Add standart deviation of waiting time calculation.
  *      - Philosopher class has verbose flag.
  *      - RunnablePhilosopher class has polite mode to minimize
          waiting time standart deviation.
  *
  *  Compilation:
  *  $ javac PhilosopherProblemDemo.java
  *
  *  =================================================================
  *
  *  Run example 1 (with polite mode).
  *  5 philosophers, 60 seconds for dinner, seed = 360, polite mode on:
  *  $ java PhilosopherProblemDemo
  *  Philosopher 0 ate = 376 times and waited = 21565 ms.
  *  Philosopher 1 ate = 385 times and waited = 21429 ms.
  *  Philosopher 2 ate = 389 times and waited = 21307 ms.
  *  Philosopher 3 ate = 386 times and waited = 21353 ms.
  *  Philosopher 4 ate = 385 times and waited = 21433 ms.
  *  -------------- Summary ---------------
  *  Mean: 21415.0 ms.
  *  Stddev: 98.14275317108238.
  *
  *
  *  Run example 2 (Without polite mode).
  *  5 philosophers, 60 seconds for dinner, seed = 360, polite mode off:
  *  $ java PhilosopherProblemDemo
  *  Philosopher 0 ate = 355 times and waited = 24181 ms.
  *  Philosopher 1 ate = 400 times and waited = 19521 ms.
  *  Philosopher 2 ate = 427 times and waited = 17530 ms.
  *  Philosopher 3 ate = 462 times and waited = 13751 ms.
  *  Philosopher 4 ate = 369 times and waited = 23036 ms.
  *  -------------- Summary ---------------
  *  Mean: 19603.0 ms.
  *  Stddev: 4221.774804510539.
  *
  *
  *  @author Aman Orazaev
  *
  */
 
 import java.util.Random;
 
 
 
 
 class Fork implements Comparable<Fork> {
     private static int nextId = 0;
     private int id;
 
     public Fork() {
         this.id = nextId++;
     }
 
     public int compareTo(Fork rhs) {
         return new Integer(this.id).compareTo(new Integer(rhs.id));
     }
 }
 
 
 
 
 
 class Philosopher {
     private int eatCount;
     private volatile long waitTime;
     private long startWait;
 
     protected Random random;
     protected int position;
     protected Fork left;
     protected Fork right;
 
     protected boolean verbose;
 
     protected final int MAX_TIME_TO_WAIT = 100;
 
     public Philosopher(int position, Fork left, Fork right, int seed) {
         if (left == right) {
             throw new java.lang.IllegalArgumentException(
                     "Philosopher must have 2 different forks.");
         }
 
         this.eatCount = 0;
         this.waitTime = 0;
         this.startWait = System.currentTimeMillis();
         this.random = new Random(seed);
 
         this.position = position;
         this.left = left;
         this.right = right;
 
         this.verbose = true;
     }
 
     public Philosopher(int position, Fork left, Fork right) {
         this(position, left, right, 0);
         this.random = new Random();
     }
 
     public void startEating() {
         waitTime += System.currentTimeMillis() - startWait;
 
         if (verbose)
             System.out.println("[Philosopher " + position + "] is eating.");
         tryToSleep(random.nextInt(MAX_TIME_TO_WAIT));
 
         ++eatCount;
         if (verbose)
             System.out.println("[Philosopher " + position + "] finished eating.");
     }
 
     public void startThinking() {
         if (verbose)
             System.out.println("[Philosopher " + position + "] is thinking.");
         tryToSleep(random.nextInt(MAX_TIME_TO_WAIT));
 
         if (verbose)
             System.out.println("[Philosopher " + position + "] is hungry.");
         startWait = System.currentTimeMillis();
     }
 
     public int getPosition() {
         return position;
     }
 
     public long getWaitTime() {
         return waitTime;
     }
 
     public int getEatCount() {
         return eatCount;
     }
 
     protected void tryToSleep(int mseconds) {
         try {
             Thread.sleep(mseconds);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 }
 
 
 
 
 
 class RunnablePhilosopher extends Philosopher implements Runnable {
     private volatile boolean stopFlag;
     private boolean politeMode;
     private RunnablePhilosopher leftNeighbour;
     private RunnablePhilosopher rightNeighbour;
 
     RunnablePhilosopher(int position, Fork left, Fork right, int seed) {
         super(position, left, right, seed);
         this.politeMode = false;
         this.stopFlag = false;
         this.leftNeighbour = null;
         this.rightNeighbour = null;
     }
 
     RunnablePhilosopher(int position, Fork left, Fork right) {
         this(position, left, right, 0);
     }
 
     public void setLeftNeighbour(RunnablePhilosopher leftNeighbour) {
         this.leftNeighbour = leftNeighbour;
     }
 
     public void setRightNeighbour(RunnablePhilosopher rightNeighbour) {
         this.rightNeighbour = rightNeighbour;
     }
 
     public RunnablePhilosopher turnOnPoliteMode() {
         this.politeMode = true;
         return this;
     }
 
     public RunnablePhilosopher verboseOff() {
         this.verbose = false;
         return this;
     }
 
     private boolean bePolite(RunnablePhilosopher other) {
         if (other != null
             && other.getWaitTime() - getWaitTime() > MAX_TIME_TO_WAIT * 2) {
             tryToSleep(random.nextInt(MAX_TIME_TO_WAIT));
             return true;
         }
 
         return false;
     }
 
     public void run() {
         while (!stopFlag) {
             startThinking();
             Fork first = (left.compareTo(right) < 0) ? left : right;
             Fork second = (left.compareTo(right) < 0) ? right : left;
 
             if (politeMode && !bePolite(leftNeighbour)) {
                 bePolite(rightNeighbour);
             }
 
             synchronized (first) {
                     if (verbose)
                         System.out.println("[Philosopher " + position
                                 + "] took first fork.");
                 synchronized (second) {
                     if (verbose)
                         System.out.println("[Philosopher " + position
                                 + "] took second fork.");
                     startEating();
                 }
             }
         }
 
         if (verbose)
             System.out.println("[Philosopher " + position + "] stopped.");
     }
 
     public void stop() {
         stopFlag = true;
     }
 
 }
 
 
 
 
 
 public class PhilosopherProblemDemo {
     private int count;
     private int dinnerTimeInSeconds;
     private int seed;
 
     public PhilosopherProblemDemo(int count, int dinnerTimeInSeconds, int seed) {
         this.count = count;
         this.dinnerTimeInSeconds = dinnerTimeInSeconds;
         this.seed = seed;
     }
 
     public void demo() {
         RunnablePhilosopher[] phils = createPhilosophers();
 
         try {
             dinner(phils);
         } catch (java.lang.InterruptedException e) {
             e.printStackTrace();
         }
 
         printSummary(phils);
     }
 
     private RunnablePhilosopher[] createPhilosophers() {
         RunnablePhilosopher[] phils = new RunnablePhilosopher[count];
 
         Fork last = new Fork();
         Fork left = last;
         for (int i = 0; i < count; ++i) {
             Fork right = (i == count - 1) ? last : new Fork();
             phils[i] = new RunnablePhilosopher(i, left, right, seed + i)
                     .turnOnPoliteMode()
                     .verboseOff();
             left = right;
         }
 
         for (int i = 0; i < count; ++i) {
             phils[i].setRightNeighbour(
                     (i != count - 1) ? phils[i + 1] : phils[0]);
             phils[i].setLeftNeighbour(
                     (i != 0) ? phils[i - 1] : phils[count - 1]);
         }
 
         return phils;
     }
 
     private void dinner(RunnablePhilosopher[] phils)
     throws java.lang.InterruptedException {
         Thread[] threads = new Thread[count];
 
         for (int i = 0; i < count; ++i) {
             threads[i] = new Thread(phils[i]);
             threads[i].start();
         }
 
         Thread.sleep(dinnerTimeInSeconds * 1000);
 
         for (RunnablePhilosopher phil : phils) {
             phil.stop();
         }
 
         for (Thread thread : threads) {
             thread.join();
         }
     }
 
     private void printSummary(RunnablePhilosopher[] phils) {
         for (RunnablePhilosopher phil : phils) {
             System.out.println("Philosopher " + phil.getPosition()
                     + " ate = " + phil.getEatCount()
                     + " times and waited = " + phil.getWaitTime() + " ms.");
         }
 
         System.out.println("-------------- Summary ---------------");
         System.out.println("Mean: " + calculateMean(phils) + " ms.");
         System.out.println("Stddev: " + calculateStddev(phils) + ".");
     }
 
     private double calculateMean(Philosopher[] phils) {
         double mean = 0;
         for (Philosopher phil : phils) {
             mean += phil.getWaitTime() / phils.length;
         }
 
         return mean;
     }
 
     private double calculateStddev(Philosopher[] phils) {
         if (phils.length == 1) {
             return Double.NaN;
         }
 
         double mean = calculateMean(phils);
         double stddev = 0;
         for (Philosopher phil : phils) {
             stddev += (phil.getWaitTime() - mean) * (phil.getWaitTime() - mean)
                     / (phils.length - 1);
         }
 
         return Math.sqrt(stddev);
     }
 
     public static void main(String[] args) {
         new PhilosopherProblemDemo(5, 60, 360).demo();
     }
 }
