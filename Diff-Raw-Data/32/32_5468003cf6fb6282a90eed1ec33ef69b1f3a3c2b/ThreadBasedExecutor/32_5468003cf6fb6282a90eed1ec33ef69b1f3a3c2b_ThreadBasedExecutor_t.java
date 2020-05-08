 package de.unisb.cs.depend.ccs_sem.evaluators.executors;
 
 import java.util.ArrayList;
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.concurrent.AbstractExecutorService;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 
 
 /**
  * This executor tries to keep the assignment of jobs to working threads
  * more statically in such a way that a working thread that submits a new
  * job will most probably be the thread that later executes this job.
  * If a working thread has no more jobs, it takes one of the other's jobs, of
  * course.
  *
  * This implementation is not fair; the incoming jobs are organized in a stack,
  * so the last incoming job is executed first.
  *
  * @author Clemens Hammacher
  */
 public class ThreadBasedExecutor extends AbstractExecutorService {
 
     private final ThreadFactory threadFactory;
     protected volatile boolean isShutdown = false;
     protected Map<Thread, Stack<Runnable>> threadJobs = new HashMap<Thread, Stack<Runnable>>();
     protected volatile boolean forcedStop = false;
 
     // object for synchronization
     protected Object waitForNewJobs = new Object();
 
     public ThreadBasedExecutor(int poolSize,
             ThreadFactory myThreadFactory) {
         super();
         if (poolSize < 1)
             throw new IllegalArgumentException("Poolsize must be > 0");
         this.threadFactory = myThreadFactory;
         initialize(poolSize);
     }
 
     private void initialize(int poolSize) {
         for (int i = 0; i < poolSize; ++i) {
             final Thread newThread = threadFactory.newThread(new Worker());
             threadJobs.put(newThread, new Stack<Runnable>());
         }
        // the threads must not be started before the threadJobs Map is filled
        for (final Thread newThread: threadJobs.keySet())
            newThread.start();
     }
 
     public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
         final long waitUntil = System.currentTimeMillis() + unit.toMillis(timeout);
         for (final Thread t: threadJobs.keySet()) {
             if (t.isAlive()) {
                 final long waitMillis = waitUntil - System.currentTimeMillis();
                 if (waitMillis <= 0)
                     return false;
                 t.join(waitMillis);
             }
         }
         return true;
     }
 
     public boolean isShutdown() {
         return isShutdown;
     }
 
     public boolean isTerminated() {
         if (!isShutdown)
             return false;
 
         for (final Thread t: threadJobs.keySet())
             if (t.isAlive())
                 return false;
         return true;
     }
 
     public void shutdown() {
         synchronized (waitForNewJobs) {
             isShutdown = true;
             waitForNewJobs.notifyAll();
         }
     }
 
     public List<Runnable> shutdownNow() {
         synchronized (waitForNewJobs) {
             forcedStop = true;
             isShutdown = true;
             waitForNewJobs.notifyAll();
         }
 
         final List<Runnable> list = new ArrayList<Runnable>();
         for (final Stack<Runnable> q: threadJobs.values()) {
             list.addAll(q);
             q.clear();
         }
 
         return list;
     }
 
     public void execute(Runnable command) {
         final Thread thread = Thread.currentThread();
         Stack<Runnable> jobs = threadJobs.get(thread);
         if (jobs == null) {
             // only check for shutdown if the execute-request comes from
             // outside. our own thread may still submit tasks
             if (isShutdown())
                 throw new RejectedExecutionException("Already shutdown.");
             // get the first queue
             jobs = threadJobs.values().iterator().next();
         }
         jobs.push(command);
         synchronized (waitForNewJobs) {
             waitForNewJobs.notify();
         }
     }
 
     private class Worker implements Runnable {
 
         private Stack<Runnable> myJobs;
         private Thread myThread;
 
         public Worker() {
             // nothing
         }
 
         public void run() {
             myThread = Thread.currentThread();
             myJobs = threadJobs.get(myThread);
             if (myJobs == null) {
                 assert false;
                 return;
             }
             while (!forcedStop) {
                 Runnable nextJob = getNextJob();
                 if (nextJob == null) {
                     if (isShutdown && !forcedStop) {
                         // look a last time for a new job
                         nextJob = getNextJob();
                         if (nextJob == null)
                             break;
                     } else
                         continue;
                 }
                 nextJob.run();
             }
         }
 
         private Runnable getNextJob() {
             try {
                 return myJobs.pop();
             } catch (final EmptyStackException e) {
                 // then go on...
             }
 
             Stack<Runnable> preferredStack = null;
             int maxSize = 0;
             for (final Stack<Runnable> q: threadJobs.values()) {
                 if (q.size() > maxSize) {
                     maxSize = q.size();
                     preferredStack = q;
                 }
             }
 
             if (preferredStack != null) {
                 try {
                     return preferredStack.pop();
                 } catch (final EmptyStackException e) {
                     // then go on...
                 }
             }
 
             synchronized (waitForNewJobs) {
                 if (myJobs.isEmpty() && !isShutdown) {
                     try {
                         waitForNewJobs.wait();
                         // it doesn't matter if we continue without having been notified...
                     } catch (final InterruptedException e) {
                         // hm, then go on... (seems like we are forced to shut down)
                     }
                 }
             }
 
             return null;
         }
 
     }
 }
