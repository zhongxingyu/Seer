 package org.inigma.shared.job;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.annotation.PreDestroy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 import org.springframework.util.ClassUtils;
 
 public class Asynchronous {
     private static Logger logger = LoggerFactory.getLogger(Asynchronous.class);
     private static Timer timer = new Timer(true);
 
     private BlockingDeque<AsynchronousFutureTask<?>> workQueue;
     private ThreadPoolTaskExecutor executor;
     private long monitorInterval = 10000;
     private String label;
     AtomicInteger completed;
 
     public Asynchronous() {
         this(3);
     }
 
     public Asynchronous(int workers) {
         this.workQueue = new LinkedBlockingDeque<AsynchronousFutureTask<?>>();
         this.label = "WorkPool";
         this.completed = new AtomicInteger();
         executor = new ThreadPoolTaskExecutor();
         executor.setCorePoolSize(workers);
         executor.setDaemon(true);
         initialize();
     }
 
     @PreDestroy
     public void close() {
         executor.shutdown();
     }
 
     public String getLabel() {
         return label;
     }
 
     public TimerTask initialize() {
         this.completed.set(0); // reset the counter too
         executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
             @Override
             public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                 logger.warn("{}'s runnable {} was rejected", label, r);
             }
         });
         executor.initialize();
         for (int i = 0; i < executor.getCorePoolSize(); i++) {
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     AsynchronousFutureTask<?> task = null;
                     while (true) {
                         try {
                             task = workQueue.take();
                             task.run();
                             completed.incrementAndGet();
                             task.get(); // this is to force exception captured to be thrown. maybe better way???
                         } catch (InterruptedException e) {
                             logger.warn("{} was interrupted with queue size at {}", label, workQueue.size());
                         } catch (ExecutionException e) {
                             logger.error("Unhandled Exception in {} with {} using {}",
                                    new Object[] { label, task.getMethod(), task.getArguments() }, e);
                         }
                     }
                 }
             });
         }
         AsynchronousMonitor monitor = new AsynchronousMonitor(this);
         scheduleRepeatTask(monitor, monitorInterval);
         return monitor;
     }
 
     public <T> Future<T> invoke(final Object instance, final Method method, final Object... args) {
         AsynchronousFutureTask<T> task = new AsynchronousFutureTask<T>(instance, method, args);
         try {
             workQueue.put(task);
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
         return task;
     }
 
     public <T> Future<T> invoke(final Object instance, final String method, final Object... args) {
         return invoke(instance, findMethod(instance, method, args), args);
     }
 
     public boolean isEmpty() {
         return this.workQueue.isEmpty();
     }
 
     public void scheduleRepeatTask(TimerTask task, long interval) {
         timer.schedule(task, interval, interval);
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public void setMonitorInterval(long monitorInterval) {
         this.monitorInterval = monitorInterval;
     }
 
     public void setWorkers(int workers) {
         executor.setCorePoolSize(workers + 1);
     }
 
     public int size() {
         return workQueue.size();
     }
 
     @Override
     protected void finalize() throws Throwable {
         close();
         super.finalize();
     }
 
     protected Method findMethod(Object instance, String method, Object... args) {
         Set<Method> methods = new HashSet<Method>(Arrays.asList(instance.getClass().getMethods()));
         methods.addAll(Arrays.asList(instance.getClass().getDeclaredMethods()));
         Method actualMethod = null;
         for (Method m : methods) {
             if (method.equals(m.getName())) {
                 Class<?>[] types = m.getParameterTypes();
                 if (args.length == types.length) {
                     boolean valid = true;
                     for (int i = 0; i < args.length; i++) {
                         Class<?> paramType = ClassUtils.resolvePrimitiveIfNecessary(types[i]);
                         if (args[i] != null && !(paramType.isAssignableFrom(args[i].getClass()))) {
                             valid = false;
                         }
                     }
                     if (valid) {
                         actualMethod = m;
                         break;
                     }
                 }
             }
         }
         if (actualMethod == null) {
             throw new IllegalStateException("Method " + method + " not found with given arguments");
         }
 
         return actualMethod;
     }
 }
