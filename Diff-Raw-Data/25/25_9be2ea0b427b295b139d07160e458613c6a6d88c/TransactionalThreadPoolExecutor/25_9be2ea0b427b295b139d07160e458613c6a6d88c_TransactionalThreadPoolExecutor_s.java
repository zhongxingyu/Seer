 package org.multiverse.transactional.executors;
 
 import org.multiverse.annotations.TransactionalMethod;
 import org.multiverse.annotations.TransactionalObject;
 import org.multiverse.api.Transaction;
 import org.multiverse.api.lifecycle.TransactionLifecycleEvent;
 import org.multiverse.api.lifecycle.TransactionLifecycleListener;
 import org.multiverse.transactional.collections.TransactionalLinkedList;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.AtomicLong;
 
 import static org.multiverse.api.StmUtils.retry;
 import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;
 
 /**
 * A transactional {@link java.util.concurrent.Executor} implementation that looks a lot like the
 * {@link java.util.concurrent.ThreadPoolExecutor}.
  * <p/>
 * Known concurrent limitations
  * <ol>
  * <li>no concurrent takes and puts</li>
  * <li>no concurrent takes (not really a big
  * issue since the takes only have a very short transactions)</li>
  * <li>no concurrent puts</li>
  * </ol>
  * This will eventually be solved when a better queue implementation is found.
  * <p/>
  * todo:
  * <ol>
 * <li> Shutdown now not implemented</li>
  * </ol>
  * <p/>
  * <dt>Exception handling</dt>
  * If an Exception is thrown when a command is executed, it will not be logged and discarded instead. In the
  * ThreadPoolExecutor it is possible to override the
  * {@link java.util.concurrent.ThreadPoolExecutor#afterExecute(Runnable, Throwable)} but this functionality isn't
  * provided yet. So the best thing that you can do is wrap the command in a logging Runnable that catches and
  * logs the exception.
  * <p/>
 * A task executes by a TransactionalThreadPoolExecutor will not automatically receives its own transaction.
  *
  * @author Peter Veentjer.
  */
 @TransactionalObject
 public class TransactionalThreadPoolExecutor extends AbstractExecutorService {
 
     private final BlockingQueue<Runnable> workQueue;
     private final TransactionalLinkedList<Thread> threads = new TransactionalLinkedList<Thread>();
 
     private State state;
     private int corePoolSize;
     private ThreadFactory threadFactory;
 
 
     /**
      * Creates a new TransactionalThreadPoolExecutor with a unbound workqueue and 1 as corePoolSize and maxPoolSize.
      * <p/>
      * If no limit is placed on the workQueue is could lead to a system not degrading gracefull because it runs out of
      * memory instead of rejecting tasks.
      */
     public TransactionalThreadPoolExecutor() {
         this(new TransactionalLinkedList<Runnable>(), 1);
     }
 
     /**
      * Creates a new TransactionalThreadPool with an unbound workqueue and the provided poolSize.
      *
      * @param poolSize the maximum and core poolsize.
      * @throws IllegalArgumentException if poolSize smaller than 0.
      */
     public TransactionalThreadPoolExecutor(int poolSize) {
         this(new TransactionalLinkedList<Runnable>(), poolSize);
     }
 
     /**
      * Creates a new TransactionalThreadPoolExecutor with the given workQueue and 1 as corePoolSize and maxPoolSize.
      *
      * @param workQueue the BlockingQueue to store unprocessed work.
      * @throws NullPointerException if workQueue is null.
      */
     public TransactionalThreadPoolExecutor(BlockingQueue<Runnable> workQueue) {
         this(workQueue, 1);
     }
 
     /**
      * Creates a new TransactionalThreadPoolExecutor.
      *
      * @param workQueue    the BlockingQueue to store unprocessed work.
      * @param corePoolSize the minimum number of threads in this TransactionalThreadPoolExecutor.
      * @throws NullPointerException     if workQueue is null.
      * @throws IllegalArgumentException if corePoolSize is smaller than zero or if maxPoolSize smaller than
      *                                  corePoolSize.
      */
     public TransactionalThreadPoolExecutor(BlockingQueue<Runnable> workQueue, int corePoolSize) {
         if (workQueue == null) {
             throw new NullPointerException();
         }
         if (corePoolSize < 0) {
             throw new IllegalArgumentException();
         }
 
         this.corePoolSize = corePoolSize;
         this.workQueue = workQueue;
         this.state = State.unstarted;
         this.threadFactory = new ThreadFactory() {
             final AtomicLong idGenerator = new AtomicLong();
 
             @Override
             public Thread newThread(Runnable r) {
                 return new Thread(r, "worker-" + idGenerator.incrementAndGet());
             }
         };
     }
 
     /**
      * Returns the minimum number of threads in this TransactionalThreadPoolExecutor. The returned value will always be
      * equal or larger than zero.
      *
      * @return the minimum number of threads in this TransactionalThreadPoolExecutor.
      */
     @TransactionalMethod(readonly = true)
     public int getCorePoolSize() {
         return corePoolSize;
     }
 
     /**
      * Sets the corePoolSize of this TransactionalThreadPoolExecutor. If the TransactionalThreadPoolExecutor is
      * unstarted, it isn't started. If the newCorePoolSize is larger than the maximumPoolSize, it automatically sets the
      * maxPoolSize to newCorePoolSize.
      *
      * @param newCorePoolSize the new core poolsize.
      * @throws IllegalArgumentException if newCorePoolSize is smaller than 0.
      * @throws IllegalStateException    if this TransactionalThreadPoolExecutor is shutdown or terminated.
      */
     @TransactionalMethod(automaticReadTracking = false)
     public void setCorePoolSize(int newCorePoolSize) {
         if (newCorePoolSize < 0) {
             throw new IllegalArgumentException();
         }
 
         switch (state) {
             case unstarted:
                 corePoolSize = newCorePoolSize;
                 break;
             case started:
                 int extra = newCorePoolSize - corePoolSize;
                 if (extra > 0) {
                     createAndRegisterWorkers(extra);
                 }
 
                 corePoolSize = newCorePoolSize;
                 break;
             case shutdown:
                 throw new IllegalStateException();
             case terminated:
                 throw new IllegalStateException();
         }
     }
 
     /**
      * Returns the current number of threads in this TransactionalThreadPoolExecutor.
      *
      * @return the current number of threads.
      */
     @TransactionalMethod(readonly = true)
     public int getCurrentPoolSize() {
         return threads.size();
     }
 
     /**
      * Returns the BlockingQueue this TransactionalThreadPoolExecutor uses to store unprocessed work.
      *
      * @return the BlockingQueue used to store unprocessed work.
      */
     @TransactionalMethod(readonly = true)
     public BlockingQueue<Runnable> getWorkQueue() {
         return workQueue;
     }
 
     /**
      * Returns the State this TransactionalThreadPoolExecutor has.
      *
      * @return the State this TransactionalThreadPoolExecutor has.
      */
     @TransactionalMethod(readonly = true)
     public State getState() {
         return state;
     }
 
     /**
      * Sets the thread factory used to createReference new threads.
      *
      * @param threadFactory the new thread factory
      * @throws NullPointerException if threadFactory is null
      * @see #getThreadFactory
      */
     @TransactionalMethod(automaticReadTracking = false)
     public void setThreadFactory(ThreadFactory threadFactory) {
         if (threadFactory == null) {
             throw new NullPointerException();
         }
         this.threadFactory = threadFactory;
     }
 
     /**
      * Returns the thread factory used to createReference new threads.
      *
      * @return the current thread factory
      * @see #setThreadFactory
      */
     @TransactionalMethod(readonly = true)
     public ThreadFactory getThreadFactory() {
         return threadFactory;
     }
 
     @Override
     public void shutdown() {
         switch (state) {
             case unstarted:
                 state = State.terminated;
                 break;
             case started:
                 state = State.shutdown;
                 break;
             case shutdown:
                 //the shutdown already is in progress, so ignore
                 break;
             case terminated:
                 //ignore it.
                 break;
             default:
                 throw new IllegalStateException();
         }
     }
 
     @Override
     @TransactionalMethod(automaticReadTracking = false)
     public List<Runnable> shutdownNow() {
         switch (state) {
             case unstarted:
                 state = State.terminated;
                 return Collections.EMPTY_LIST;
             case started:
                 //fall through                
             case shutdown:
                 LinkedList<Runnable> sink = new LinkedList<Runnable>();
                 workQueue.drainTo(sink);
 
                 if (threads.isEmpty()) {
                     state = State.terminated;
                 } else {
                     state = State.shutdown;
                     getThreadLocalTransaction().registerLifecycleListener(new InterruptWorkersListener());
                 }
 
                 return sink;
             case terminated:
                 //ignore it.
                 return Collections.EMPTY_LIST;
             default:
                 throw new IllegalStateException();
         }
     }
 
 
     @Override
     @TransactionalMethod(readonly = true)
     public boolean isShutdown() {
         return state == State.shutdown || state == State.terminated;
     }
 
     @Override
     @TransactionalMethod(readonly = true)
     public boolean isTerminated() {
         return state == State.terminated;
     }
 
     @TransactionalMethod(readonly = true)
     public boolean isStarted() {
         return state == State.started;
     }
 
     @Override
     @TransactionalMethod(readonly = true, automaticReadTracking = true)
     public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
         switch (state) {
             case unstarted:
                 throw new UnsupportedOperationException();
             case started:
                 throw new UnsupportedOperationException();
             case shutdown:
                 throw new UnsupportedOperationException();
             case terminated:
                 return true;
             default:
                 throw new IllegalStateException();
         }
     }
 
     /**
      * Awaits for this TransactionalThreadPoolExecutor to complete. This call can be interrupted.
      *
      * @throws InterruptedException if the Thread was interrupted.
      */
     @TransactionalMethod(readonly = true, automaticReadTracking = true, interruptible = true)
     public void awaitTermination() throws InterruptedException {
         if (state != State.terminated) {
             retry();
         }
     }
 
     /**
      * Awaits for this TransactionalThreadPoolExecutor to complete without the possibility of being interrupted.
      */
     @TransactionalMethod(readonly = true, automaticReadTracking = true)
     public void awaitTerminationUninterruptibly() {
         if (state != State.terminated) {
             retry();
         }
     }
 
     @Override
     public void execute(Runnable command) {
         if (command == null) {
             throw new NullPointerException();
         }
 
         switch (state) {
             case unstarted:
                 start();
                 //fall through
             case started:
                 boolean workOffered = workQueue.offer(command);
                 if (!workOffered) {
                     throw new RejectedExecutionException();
                 }
                 break;
             case shutdown:
                 throw new RejectedExecutionException();
             case terminated:
                 throw new RejectedExecutionException();
             default:
                 throw new IllegalStateException();
         }
     }
 
     /**
      * Starts this TransactionalThreadPoolExecutor. If the TransactionalThreadPoolExecutor already is started, the call
      * is ignored.
      *
      * @throws IllegalStateException if the this TransactionalThreadPoolExecutor already is shutdown or terminated.
      */
     public void start() {
         switch (state) {
             case unstarted:
                 state = State.started;
                 createAndRegisterWorkers(corePoolSize);
                 break;
             case started:
                 //ignore call
                 break;
             case shutdown:
                 throw new IllegalStateException();
             case terminated:
                 throw new IllegalStateException();
             default:
                 throw new IllegalStateException();
         }
     }
 
     private void createAndRegisterWorkers(int size) {
         Thread[] newThreads = new Thread[size];
         for (int k = 0; k < corePoolSize; k++) {
             Worker worker = new Worker();
             Thread thread = threadFactory.newThread(worker);
             worker.thread = thread;
 
             threads.add(thread);
             newThreads[k] = thread;
         }
 
         getThreadLocalTransaction().registerLifecycleListener(new StartWorkersListener(newThreads));
     }
 
     private void signalTerminated() {
         state = State.terminated;
     }
 
     enum State {
         unstarted, started, shutdown, terminated
     }
 
     private class Worker implements Runnable {
 
         private Thread thread;
 
         @Override
         public void run() {
             work();
             drainWorkQueue();
             die();
         }
 
         private void work() {
             boolean again = true;
             do {
                 try {
                     Runnable command = takeWork();
                     if (command == null) {
                         again = false;
                     } else {
                         execute(command);
                     }
                 } catch (InterruptedException ex) {
                     //ignore, continue the loop.. will terminate if executor was shutdown.
                 }
             } while (again);
         }
 
         @TransactionalMethod
         private Runnable takeWork() throws InterruptedException {
             if (threads.size() > corePoolSize || isShutdown()) {
                 threads.remove(thread);
                 return null;
             } else {
                 return workQueue.take();
             }
         }
 
         private void drainWorkQueue() {
             boolean again = true;
             do {
                 Runnable command = workQueue.poll();
                 if (command == null) {
                     again = false;
                 } else {
                     execute(command);
                 }
             } while (again);
         }
 
         @TransactionalMethod
         private void die() {
             if (isShutdown()) {
                 //it could be that this method is called more than once, but that doesn't cause problems
                 signalTerminated();
             }
         }
 
         private void execute(Runnable command) {
             try {
                 command.run();
             } catch (RuntimeException ignore) {
             }
         }
     }
 
     private static class StartWorkersListener implements TransactionLifecycleListener {
 
         private final Thread[] threads;
 
         private StartWorkersListener(Thread... threads) {
             this.threads = threads;
         }
 
         @Override
         public void notify(Transaction t, TransactionLifecycleEvent event) {
             if (event == TransactionLifecycleEvent.postCommit) {
                 for (int k = 0; k < threads.length; k++) {
                     threads[k].start();
                 }
             }
         }
     }
 
     private class InterruptWorkersListener implements TransactionLifecycleListener {
 
         @Override
         @TransactionalMethod
         public void notify(Transaction tx, TransactionLifecycleEvent event) {
             if (event == TransactionLifecycleEvent.postCommit) {
                 for (Thread thread : threads) {
                     thread.interrupt();
                 }
             }
         }
     }
 }
