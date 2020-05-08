 package com.psddev.dari.util;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * {@link ScheduledExecutorService} optimized for use with {@link Task}.
  */
 public final class TaskExecutor implements ScheduledExecutorService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);
 
     private final String name;
     private final ExecutorService executor;
     private final ScheduledExecutorService scheduledExecutor;
     private final List<WeakReference<Object>> tasks = new ArrayList<WeakReference<Object>>();
 
     /**
      * Creates an instance with the given {@code name}. This should only
      * be called within {@link Static#getInstance} which makes sure that
      * the name is unique.
      *
      * @throws IllegalArgumentException If the given {@code name} is
      *         {@code null}.
      */
    private TaskExecutor(String name) {
         if (name == null) {
             throw new IllegalArgumentException("Name can't be null!");
         }
 
         LOGGER.info("Creating [{}]", name);
 
         TaskThreadFactory threadFactory = new TaskThreadFactory(name);
         this.name = name;
         this.executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new SynchronousQueue<Runnable>(), threadFactory);
         this.scheduledExecutor = new ScheduledThreadPoolExecutor(5, threadFactory);
     }
 
     /** Returns the unique name of this task executor. */
     public String getName() {
         return name;
     }
 
     /**
      * Returns all callable and runnable tasks that are either running
      * or scheduled run in this executor.
      */
     public List<Object> getTasks() {
         List<Object> tasks = new ArrayList<Object>();
         for (Iterator<WeakReference<Object>> i = this.tasks.iterator(); i.hasNext(); ) {
             Object task = i.next().get();
             if (task != null) {
                 tasks.add(task);
             } else {
                 i.remove();
             }
         }
         return tasks;
     }
 
     /** Tries to pause all tasks currently running in this executor. */
     public void pauseTasks() {
         for (Object task : getTasks()) {
             if (task instanceof Task) {
                 ((Task) task).pause();
             }
         }
     }
 
     /** Resumes all paused tasks in this executor. */
     public void resumeTasks() {
         for (Object task : getTasks()) {
             if (task instanceof Task) {
                 ((Task) task).resume();
             }
         }
     }
 
     // --- ScheduledExecutorService support ---
 
     private synchronized void addTask(Object task) {
         for (WeakReference<Object> reference : tasks) {
             if (task.equals(reference.get())) {
                 return;
             }
         }
         tasks.add(new WeakReference<Object>(task));
     }
 
     @Override
     public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
         addTask(callable);
         return scheduledExecutor.schedule(callable, delay, unit);
     }
 
     @Override
     public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
         addTask(runnable);
         return scheduledExecutor.schedule(runnable, delay, unit);
     }
 
     @Override
     public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
         addTask(runnable);
         return scheduledExecutor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
     }
 
     @Override
     public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
         addTask(runnable);
         return scheduledExecutor.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
     }
 
     @Override
     public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
         timeout = TimeUnit.NANOSECONDS.convert(timeout, unit);
         long startTime = System.nanoTime();
         if (executor.awaitTermination(timeout, TimeUnit.NANOSECONDS)) {
             return scheduledExecutor.awaitTermination(timeout - (System.nanoTime() - startTime), TimeUnit.NANOSECONDS);
         } else {
             return false;
         }
     }
 
     @Override
     public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables) throws InterruptedException {
         for (Callable<?> callable : callables) {
             addTask(callable);
         }
         return executor.invokeAll(callables);
     }
 
     @Override
     public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> callables, long timeout, TimeUnit unit) throws InterruptedException {
         for (Callable<?> callable : callables) {
             addTask(callable);
         }
         return executor.invokeAll(callables, timeout, unit);
     }
 
     @Override
     public <T> T invokeAny(Collection<? extends Callable<T>> callables) throws ExecutionException, InterruptedException {
         for (Callable<?> callable : callables) {
             addTask(callable);
         }
         return executor.invokeAny(callables);
     }
 
     @Override
     public <T> T invokeAny(Collection<? extends Callable<T>> callables, long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
         for (Callable<?> callable : callables) {
             addTask(callable);
         }
         return executor.invokeAny(callables, timeout, unit);
     }
 
     @Override
     public boolean isShutdown() {
         return executor.isShutdown() && scheduledExecutor.isShutdown();
     }
 
     @Override
     public boolean isTerminated() {
         return executor.isTerminated() && scheduledExecutor.isTerminated();
     }
 
     /** Tries to stop all tasks currently running in this executor. */
     private void stopTasks() {
         for (Object task : getTasks()) {
             if (task instanceof Task) {
                 ((Task) task).stop();
             }
         }
     }
 
     @Override
     public void shutdown() {
         String name = getName();
         LOGGER.info("Gracefully shutting down [{}]", name);
         Static.INSTANCES.remove(name);
 
         stopTasks();
         executor.shutdown();
         scheduledExecutor.shutdown();
     }
 
     @Override
     public List<Runnable> shutdownNow() {
         String name = getName();
         LOGGER.info("Shutting down [{}] now!", name);
         Static.INSTANCES.remove(name);
 
         stopTasks();
         List<Runnable> remaining = new ArrayList<Runnable>();
         remaining.addAll(executor.shutdownNow());
         remaining.addAll(scheduledExecutor.shutdownNow());
         return remaining;
     }
 
     @Override
     public <T> Future<T> submit(Callable<T> callable) {
         addTask(callable);
         return executor.submit(callable);
     }
 
     @Override
     public Future<?> submit(Runnable runnable) {
         addTask(runnable);
         return executor.submit(runnable);
     }
 
     @Override
     public <T> Future<T> submit(Runnable runnable, T result) {
         addTask(runnable);
         return executor.submit(runnable, result);
     }
 
     @Override
     public void execute(Runnable runnable) {
         addTask(runnable);
         executor.execute(runnable);
     }
 
     /** {@link TaskExecutor} utility methods. */
     public static final class Static {
 
         private Static() {
         }
 
         private static final String DEFAULT_INSTANCE_NAME = "Miscellaneous Tasks";
         private static final Map<String, WeakReference<TaskExecutor>> INSTANCES = new ConcurrentHashMap<String, WeakReference<TaskExecutor>>();
 
         /** Returns a list of all active task executors. */
         public static synchronized List<TaskExecutor> getAll() {
             List<TaskExecutor> all = new ArrayList<TaskExecutor>();
             for (Iterator<WeakReference<TaskExecutor>> i = INSTANCES.values().iterator(); i.hasNext(); ) {
                 TaskExecutor executor = i.next().get();
                 if (executor != null) {
                     all.add(executor);
                 } else {
                     i.remove();
                 }
             }
             return all;
         }
 
         /**
          * Returns the task executor with the given {@code name}.
          *
          * @param name If blank, returns the default executor.
          */
         public static synchronized TaskExecutor getInstance(String name) {
             if (ObjectUtils.isBlank(name)) {
                 name = DEFAULT_INSTANCE_NAME;
             }
 
             WeakReference<TaskExecutor> reference = INSTANCES.get(name);
             TaskExecutor executor = reference != null ? reference.get() : null;
             if (executor == null) {
                 executor = new TaskExecutor(name);
                 INSTANCES.put(name, new WeakReference<TaskExecutor>(executor));
             }
 
             return executor;
         }
 
         /** Returns the default task executor. */
         public static TaskExecutor getDefault() {
             return getInstance(null);
         }
     }
 
     /** Thread factory optimized for use with {@link Task}. */
     private static class TaskThreadFactory implements ThreadFactory {
 
         private static final AtomicLong THREAD_INDEX = new AtomicLong();
 
         private final String name;
 
         public TaskThreadFactory(String name) {
             this.name = name;
         }
 
         // --- ThreadFactory support ---
 
         @Override
         public Thread newThread(Runnable runnable) {
             String threadName;
             if (Static.DEFAULT_INSTANCE_NAME.equals(name)) {
                 threadName = "";
             } else {
                 threadName = name + ": ";
             }
 
             if (runnable instanceof Task) {
                 threadName += ((Task) runnable).getName();
             } else {
                 threadName += "Thread #" + THREAD_INDEX.incrementAndGet();
             }
 
             Thread thread = new Thread(null, runnable, threadName);
             thread.setDaemon(true);
             thread.setPriority(Thread.NORM_PRIORITY);
             return thread;
         }
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link Static#getInstances} instead. */
     @Deprecated
     public static List<TaskExecutor> getInstances() {
         return Static.getAll();
     }
 
     /** @deprecated Use {@link Static#getInstance} instead. */
     @Deprecated
     public static TaskExecutor getInstance(String name) {
         return Static.getInstance(name);
     }
 }
