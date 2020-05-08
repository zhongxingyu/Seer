 package de.unisb.cs.depend.ccs_sem.evaluators;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
 import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;
 import de.unisb.cs.depend.ccs_sem.utils.ConcurrentHashSet;
 import de.unisb.cs.depend.ccs_sem.utils.UniqueQueue;
 
 
 public class ParallelEvaluator implements Evaluator {
 
     private final Integer numThreads;
 
     protected ExecutorService executor = null;
 
     protected ConcurrentMap<Expression, EvaluatorJob> currentlyEvaluating = null;
 
     protected Set<Expression> evaluatedSuccessors = null;
 
     protected final Object readyLock = new Object();
 
     protected EvaluationMonitor monitor;
 
    public boolean errorOccured = false;
 
     public ParallelEvaluator(int numThreads) {
         this.numThreads = numThreads;
     }
 
     public ParallelEvaluator() {
         this.numThreads = null;
     }
 
     public boolean evaluate(Expression expr)
             throws InterruptedException {
         return evaluate(expr, null);
     }
 
     public boolean evaluate(Expression expr, EvaluationMonitor monitor)
             throws InterruptedException {
         return evaluate0(expr, false, monitor);
     }
 
     public boolean evaluateAll(Expression expr, EvaluationMonitor monitor)
             throws InterruptedException {
         return evaluate0(expr, true, monitor);
     }
 
     // synchronized s.t. it can only be called once at a time
     private synchronized boolean evaluate0(Expression expr,
             boolean evaluateSuccessors, EvaluationMonitor monitor)
             throws InterruptedException {
         try {
             synchronized (readyLock) {
                 initialize(evaluateSuccessors, monitor);
 
                 if (evaluateSuccessors)
                     evaluatedSuccessors.add(expr);
                 // the EvaluatorJob automatically executes itself
                 new EvaluatorJob(expr, evaluateSuccessors);
 
                 try {
                     // use this variable to detect a stuck situation.
                     // if such a situation is detected, we check if we have
                     // circular dependencies, that are typically caused by
                     // unguarded expressions.
                     int lastCount = -1;
                     // the wait time is continously increased
                     int waitTime = 200;
                     while (lastCount != 0) {
                         readyLock.wait(waitTime);
                         waitTime = waitTime * 3 / 2;
                        int newCount = currentlyEvaluating.size();
                         if (lastCount == newCount) {
                             // check for cycle dependencies
                             if (hasCyclicDependencies()) {
                                 if (monitor != null)
                                     monitor.error("There are cyclic dependencies in the expressions. " +
                                         "This typically occures on unguarded expressions.");
                                 errorOccured = true;
                                 break;
                             }
                         }
                         lastCount = newCount;
                     }
                 } catch (final InterruptedException e) {
                     if (monitor != null)
                         monitor.error("interrupted");
                     errorOccured = true;
                     throw e;
                 }
             }
         } finally {
             shutdown();
         }
 
         return !errorOccured;
     }
 
     private boolean hasCyclicDependencies() {
        Set<EvaluatorJob> checked = new HashSet<EvaluatorJob>();
        for (EvaluatorJob job: currentlyEvaluating.values()) {
             if (checked.contains(job))
                 continue;
            UniqueQueue<EvaluatorJob> queue = new UniqueQueue<EvaluatorJob>();
             queue.add(job);
             EvaluatorJob e;
             while ((e = queue.poll()) != null) {
                 synchronized (e.expr) {
                    for (Barrier barrier: e.waiters) {
                         if (!queue.add(barrier.job))
                             return true;
                     }
                 }
             }
             checked.addAll(queue.getSeen());
         }
 
         return false;
     }
 
     protected void initialize(boolean evaluateSuccessors, EvaluationMonitor monitor2) {
         assert executor == null;
         assert currentlyEvaluating == null;
         assert monitor == null;
 
         final int threadsToInstantiate = numThreads == null
             ? Runtime.getRuntime().availableProcessors() + 1
             : numThreads;
 
         final UncaughtExceptionHandler eh = new Thread.UncaughtExceptionHandler() {
             public void uncaughtException(Thread t, Throwable e) {
                 errorOccured = true;
                 synchronized (readyLock) {
                     System.err.print("Exception in thread \""
                        + t.getName() + "\" ");
                     e.printStackTrace(System.err);
                     if (monitor != null)
                        monitor.error(e.getClass().getName() + ": " + e.getMessage());
                     readyLock.notifyAll();
                 }
             }
         };
         final ThreadFactory myThreadFactory = new MyThreadFactory(eh, getWorkerThreadPriority());
         executor = getExecutor(threadsToInstantiate, myThreadFactory);
 
         currentlyEvaluating = new ConcurrentHashMap<Expression, EvaluatorJob>();
 
         if (evaluateSuccessors)
             evaluatedSuccessors = new ConcurrentHashSet<Expression>(threadsToInstantiate*2);
 
         monitor = monitor2;
 
         errorOccured = false;
     }
 
     protected ExecutorService getExecutor(final int threadsToInstantiate,
             final ThreadFactory threadFactory) {
         return Executors.newFixedThreadPool(threadsToInstantiate, threadFactory);
     }
 
     /**
      * Shuts down this evaluator.
      * Delegates to the {@link ExecutorService} and awaits it's termination.
      */
     private void shutdown() {
         if (executor != null) {
             if (errorOccured)
                 executor.shutdownNow();
             else
                 executor.shutdown();
             try {
                 executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
             } catch (final InterruptedException e) {
                 // reset this flag
                 Thread.currentThread().interrupt();
             } catch (final UnsupportedOperationException e) {
                 // ignore
             }
         }
         executor = null;
 
         assert errorOccured || currentlyEvaluating == null || currentlyEvaluating.isEmpty();
         currentlyEvaluating = null;
 
         evaluatedSuccessors = null;
         monitor = null;
     }
 
     protected int getWorkerThreadPriority() {
         return Thread.NORM_PRIORITY;
     }
 
     private class EvaluatorJob implements Runnable {
 
         protected List<Barrier> waiters = null;
         protected final Expression expr;
         private final boolean evaluateSuccessors;
         private volatile boolean childrenEvaluated = false;
 
         public EvaluatorJob(Expression expr, boolean evaluateSuccessors) {
             this.expr = expr;
             this.evaluateSuccessors = evaluateSuccessors;
             currentlyEvaluating.putIfAbsent(expr, this);
             executor.execute(this);
         }
 
         // is externally synchronized (over the expression)
         private void addWaiter(Barrier waiter) {
             assert Thread.holdsLock(expr);
             if (waiters == null)
                 waiters = new ArrayList<Barrier>(2);
             waiters.add(waiter);
         }
 
         public void run() {
             if (!childrenEvaluated && !expr.isEvaluated()) {
 
                 Barrier barrier = null;
                 for (final Expression child: expr.getChildren()) {
                     synchronized (child) {
                         if (!child.isEvaluated()) {
                             if (barrier == null)
                                 barrier = new Barrier(this, executor, 2);
                             else
                                 barrier.inc();
 
                             EvaluatorJob childEvaluator =
                                     currentlyEvaluating.get(child);
                             if (childEvaluator == null)
                                 childEvaluator = new EvaluatorJob(child, false);
                             childEvaluator.addWaiter(barrier);
                         }
                     }
                 }
 
                 childrenEvaluated = true;
 
                 // if barrier == null, then all children have been evaluated
                 // and we use this thread to continue evaluation
                 if (barrier != null) {
 
                     // inform barrier that there are no more new children coming...
                     barrier.inform();
 
                     // the barrier will call us again when all children are evaluated
                     return;
                 }
             }
 
             expr.evaluate();
 
             synchronized (expr) {
                 if (waiters != null) {
                     for (final Barrier waiter: waiters)
                         waiter.inform();
                 }
             }
 
             if (evaluateSuccessors) {
                 if (monitor != null) {
                     monitor.newState(expr.getTransitions().size());
                 }
 
                 for (final Transition trans: expr.getTransitions()) {
                     final Expression succ = trans.getTarget();
                     if (evaluatedSuccessors.add(succ))
                         new EvaluatorJob(succ, true);
                 }
             }
 
             // now the work is ready
             final Object removed = currentlyEvaluating.remove(expr);
             assert removed != null;
 
             // if everything is evaluated, inform the waiting thread(s)
             // currentlyEvaluating can be null if another worker just informed
             // the main thread that we are ready
             if (currentlyEvaluating != null && currentlyEvaluating.isEmpty()) {
                 synchronized (readyLock) {
                     if (monitor != null)
                         monitor.ready();
                     readyLock.notifyAll();
                 }
             }
         }
     }
 
     // static to improve performance, even if we have to copy the
     // ExecutorService reference
     private static class Barrier {
 
         protected final EvaluatorJob job;
         private final Executor jobExecutor;
         private final AtomicInteger waitNo;
 
         public Barrier(EvaluatorJob jobToRun, Executor executor, int startNo) {
             this.job = jobToRun;
             this.jobExecutor = executor;
             this.waitNo = new AtomicInteger(startNo);
         }
 
         public void inc() {
             waitNo.incrementAndGet();
         }
 
         public void inform() {
             assert waitNo.get() > 0;
 
             final int remaining = waitNo.decrementAndGet();
             if (remaining == 0)
                 jobExecutor.execute(job);
         }
 
     }
 
     private static class MyThreadFactory implements ThreadFactory {
         static final AtomicInteger poolNumber = new AtomicInteger(1);
         final ThreadGroup group;
         final AtomicInteger threadNumber = new AtomicInteger(1);
         final String namePrefix;
         private final UncaughtExceptionHandler eh;
         private final int workerThreadPriority;
 
         public MyThreadFactory(UncaughtExceptionHandler eh, int workerThreadPriority) {
             this.eh = eh;
             this.workerThreadPriority = workerThreadPriority;
             group = Thread.currentThread().getThreadGroup();
             namePrefix = "parallelEvaluator-";
         }
 
         public Thread newThread(Runnable r) {
             final Thread t = new Thread(group, r,
                                   namePrefix + threadNumber.getAndIncrement(),
                                   0);
             if (t.isDaemon())
                 t.setDaemon(false);
             if (t.getPriority() != workerThreadPriority)
                 t.setPriority(workerThreadPriority);
             t.setUncaughtExceptionHandler(eh);
             return t;
         }
 
     }
 
 }
