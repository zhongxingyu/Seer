 package com.bvb.spring.jms.listener;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.springframework.jms.listener.DefaultMessageListenerContainer;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
 
 import com.bvb.spring.jms.listener.config.PauseConfig;
 import com.bvb.spring.jms.listener.exception.PauseConsumptionException;
 import com.bvb.spring.jms.listener.keepalive.KeepAliveManager;
 import com.bvb.spring.jms.listener.keepalive.KeepAliveResponse;
 import com.bvb.spring.jms.listener.keepalive.KeepAliveService;
 import com.bvb.spring.jms.listener.throttler.FixedRateThrottlerCounter;
 import com.bvb.spring.jms.listener.utils.DateUtils;
 import com.google.common.base.Preconditions;
 
 /**
  * This class extends the {@link DefaultMessageListenerContainer} so that it is possible for users of it to provide a keep
  * alive timer that tests whether some aspect the listener requires is still valid and active.  The container can be set
  * so that it will start with no consumption occurring until the keep alive is valid.  The clients of the PDMLC can choose
  * to implement the keep alive service, which allows them to identify how long before the next keep alive should be called
  * and whether the container can now start or stop.
  * On top of these features the PDMLC can also receive a specific runtime exception thrown by the listener which allows
  * it to take action to throttle the consumption of messages into the gateway.  Due to the design of the DMLC it has not
  * been possible to add code that would throttle consumption at a fixed rate.
  *
  */
 public class BackoffDefaultMessageListeningContainer extends DefaultMessageListenerContainer
 {
     private static final int KEEP_ALIVE_THEAD_COUNT = 2;
     public static long MIN_THROTTLE_TIME_MS = TimeUnit.MINUTES.toMillis(2);
     public static long THROTTLE_LESSEN_PERIOD_MS = TimeUnit.SECONDS.toMillis(60);
     
     private boolean initiallyNotRunning = false;
     private Object throttlingLock = new Object();
     private Object taskSchedulerLock = new Object();
     private KeepAliveService keepAliveService;
     private ThreadPoolTaskScheduler scheduler;
     private KeepAliveManager keepAliveManager;
     private long keepAliveIntervalMs = TimeUnit.SECONDS.toMillis(15);
     private ScheduledFuture<?> taskThrottleRelease;
     private int actualFullconcurrentConsumers;
     private int actualFullmaxConcurrentConsumers;
     private volatile boolean stoppingFromExternalCall = false;
     private List<DmlcStartObserver> observers = new ArrayList<DmlcStartObserver>();
 
     /**
      * Returns whether the DMLC is set to initially not start consuming messages until the keep alive returns success.
      * @return {@code true} if initially we consume nothing on startup, otherwise {@code false}.
      */
     public boolean isInitiallyStopped()
     {
         return initiallyNotRunning;
     }
 
     /**
      * Set whether the DMLC is set to initially not start consuming messages until a keep alive returns success.  This
      * prevents messages being consumed until the connection is open.
      * @param initiallyStopped {@code true} to not start until the keep alive succeeds.  {@code false} to behave as a 
      * normal DMLC and start immediately.
      */
     public void setInitiallyStopped(boolean initiallyStopped)
     {
         this.initiallyNotRunning = initiallyStopped;
     }
     
     /**
      * Set the keep alive service.  This service will be called based on the keep alive interval to identify whether the
      * connection or upstream services are active.  If they are not then consumption of messages can be throttled or stopped.
      * @param keepAliveService the service to use.
      */
     public void setKeepAliveService(KeepAliveService keepAliveService)
     {
         this.keepAliveService = keepAliveService;
     }
     
     @Override
     public void start()
     {
         stoppingFromExternalCall = false;
         if (!initiallyNotRunning)
         {
             startDmlc();
         }
         // start the keep alive thread and build the task scheduler, if not already done.
         startKeepAliveAndBuildTaskScheduler();
     }
     
     @Override
     public void stop()
     {
         doTaskStop();
         super.stop();
     }
     
     @Override
     public void shutdown()
     {
         doTaskStop();
         stopTaskScheduler();
         super.shutdown();
     }
     
     private void doTaskStop()
     {
         stoppingFromExternalCall = true;
         if (keepAliveManager != null)
         {
             keepAliveManager.stop();
         }
         cancelThrottleTask();
         notifyObserversStop(null);
     }
     
     private void notifyObserversStop(PauseConfig config)
     {
         for (DmlcStartObserver o : observers)
         {
            if (config != null)
             {
                 o.stopped();
             }
             else
             {
                 o.stopped(config);
             }
         }
     }
     
     private void notifyObserversRunning()
     {
         for (DmlcStartObserver o : observers)
         {
             o.running();
         }
     }
     
     /**
      * Add an observer who will get notified when the BDMLC stops or starts.
      * @param observer the observer to add.
      * @throws NullPointerException if the observer is null.
      */
     public void setObserver(DmlcStartObserver observer)
     {
         registerObserver(observer);
     }
     
     /**
      * Add an observer who will get notified when the BDMLC stops or starts.
      * @param observer the observer to add.
      * @throws NullPointerException if the observer is null.
      */
     public void registerObserver(DmlcStartObserver observer)
     {
         Preconditions.checkNotNull(observer);
         observers.add(observer);
     }
     
     /**
      * Remove an observer.
      * @param observer the observer to remove.
      */
     public void unRegisterObserver(DmlcStartObserver observer)
     {
         observers.remove(observer);
     }
     
     @Override
     public void stop(Runnable callback)
     {
         doTaskStop();
         super.stop(callback);
     }
     
     @Override
     public void setConcurrentConsumers(int concurrentConsumers)
     {
         super.setConcurrentConsumers(concurrentConsumers);
         updateConcurrency();
     }
     
     @Override
     public void setConcurrency(String concurrency)
     {
         super.setConcurrency(concurrency);
         updateConcurrency();
     }
     
     @Override
     public void setMaxConcurrentConsumers(int count)
     {
         super.setMaxConcurrentConsumers(count);
         updateConcurrency();
     }
     
     @Override
     protected void handleListenerException(Throwable ex)
     {
         // if this is an exception indicating some back-off or keep alive is required
         if (ex.getCause() instanceof PauseConsumptionException)
         {
             handlePauseConsumptionException((PauseConsumptionException) ex.getCause());
         }
         super.handleListenerException(ex);
     }
     
     protected void handlePauseConsumptionException(PauseConsumptionException ex)
     {
         // Stop the DMLC and throttle the consumption if required
         stopDmlc(ex.getConfig());
         logger.warn(String.format("Gateway->Stopped.  Gateway message listener returned PauseConsumption with config: [%s]",
             ex.getConfig().toString()));
     }
     
     protected void setThrottledMaxConcurrentConsumers(int count)
     {
         super.setMaxConcurrentConsumers(count);
     }
     
     protected void setThrottledConcurrentConsumers(int count)
     {
         super.setConcurrentConsumers(count);
     }
     
     protected void updateConcurrency()
     {
         actualFullconcurrentConsumers = getConcurrentConsumers();
         actualFullmaxConcurrentConsumers = getMaxConcurrentConsumers();
     }
     
     protected void startDmlc()
     {
         if (!isRunning())
         {
             super.start();
             notifyObserversRunning();
         }
     }
     
     protected void stopDmlc(PauseConfig config)
     {
         if (isRunning())
         {
             super.stop();
             notifyObserversStop(config);
         }
         if (config != null)
         {
             // throttle if returned in the response
             throttleConsumers(config);
             // set the next keep alive, if asked to
             keepAliveManager.rescheduleAlways(config.getDelayConsumptionForMs());
         }
     }
     
     private void throttleConsumers(PauseConfig config)
     {
         if (config.isThrottled())
         {
             synchronized (throttlingLock)
             {
                 int currentMax = getMaxConcurrentConsumers();
                 PauseConfig configToUse = new PauseConfig(currentMax, config);
                 int maxConsumers = configToUse.getThrottleMaxConcurrent();
                 // start a timer to release the throttling after a period of time
                 startThrottlingReleaseTask(configToUse);
                 int currentConcurrent = getConcurrentConsumers();
                 if (currentConcurrent > maxConsumers)
                 {
                     setThrottledConcurrentConsumers(maxConsumers);
                 }
                 setThrottledMaxConcurrentConsumers(maxConsumers);
             }
         }
     }
     
     /*
      * Needs to be synchronized with the throttling lock object.
      */
     private void startThrottlingReleaseTask(PauseConfig config)
     {
         cancelThrottleTask();
         scheduleThrottlingRelaxTask(config);
     }
 
     private void scheduleThrottlingRelaxTask(PauseConfig config)
     {
         // When stopping do not reschedule any tasks
         if (!stoppingFromExternalCall)
         {
             // schedule another task
             int target = actualFullmaxConcurrentConsumers;
             FixedRateThrottlerCounter counter = new FixedRateThrottlerCounter(target, config);
             // schedule to run in the future when the first throttling relax interval occurs
             taskThrottleRelease = 
                     scheduler.scheduleWithFixedDelay(new ThrottlingRelaxerRunnable(counter), 
                         DateUtils.getNowPlusMs(config.getThrottleRelaxIntervalMs()), THROTTLE_LESSEN_PERIOD_MS);
         }
     }
 
     private void cancelThrottleTask()
     {
         synchronized(throttlingLock)
         {
             if (taskThrottleRelease != null)
             {
                 // cancel existing task
                 taskThrottleRelease.cancel(false);
                 taskThrottleRelease = null;
             }
         }
     }
     
     private void startKeepAliveAndBuildTaskScheduler()
     {
         synchronized (taskSchedulerLock)
         {
             if (scheduler == null)
             {
                 this.scheduler = buildScheduler(KEEP_ALIVE_THEAD_COUNT);
                 keepAliveManager = new KeepAliveManager(scheduler, new KeepAliveRunnable(), keepAliveIntervalMs);
             }
             keepAliveManager.start();
         }
     }
     
     private synchronized void stopTaskScheduler()
     {
         synchronized (taskSchedulerLock)
         {
             if (scheduler != null)
             {
                 scheduler.shutdown();
                 scheduler = null;
             }
         }
     }
     
     private ThreadPoolTaskScheduler buildScheduler(int poolSize)
     {
         ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
         scheduler.setThreadNamePrefix(this.getBeanName() + "-KeepAlive");
         scheduler.setPoolSize(poolSize);
         scheduler.initialize();
         return scheduler;
     }
     
     /**
      * Get the keep alive interval in milliseconds.  This is in the interval between keep alive runs, where the keep alive
      * service returns whether the gateway can continue with normal operation.
      * @return the keep alive interval.
      */
     public long getKeepAliveInterval()
     {
         if (keepAliveManager != null)
         {
             return keepAliveManager.getKeepAliveInterval();
         }
         else
         {
             return keepAliveIntervalMs;
         }
     }
 
     /**
      * Set the keep alive interval in milliseconds.
      * @param keepAliveIntervalMs the interval to set in milliseconds.
      */
     public void setKeepAliveInterval(long keepAliveIntervalMs)
     {
         this.keepAliveIntervalMs = keepAliveIntervalMs;
     }
 
     class KeepAliveRunnable implements Runnable
     {
 
         @Override
         public void run()
         {
             // default to always starting the DMLC if there is no keep alive service defined
             boolean start = true;
             if (keepAliveService != null)
             {
                 start = processByService();
             }
             // if we want to start the DMLC
             if (start)
             {
                 startDmlc();
             }
         }
         
         private boolean processByService()
         {
             KeepAliveResponse response = keepAliveService.keepAlive();
             if (response == null)
             {
                 logger.warn("Keep alive service returned null which is not permitted, ignoring result");
             }
             else if (response.isSuccess())
             {
                 // only restart the scheduling if the new value is different from the old
                 keepAliveManager.rescheduleIfDifferent(response.getPauseConfig().getDelayConsumptionForMs());
                 return true;
             }
             else
             {
                 logger.warn(String.format("Keep alive service return failure, stopping message consumption.  Using settings "
                         + "from KeepAliveResponse: [%s]", response.toString()));
                 stopDmlc(response.getPauseConfig());
             }
             return false;
         }
 
     }
     
     public class ThrottlingRelaxerRunnable implements Runnable
     {
         private final FixedRateThrottlerCounter counter;
         
         public ThrottlingRelaxerRunnable(FixedRateThrottlerCounter counter)
         {
             this.counter = counter;
         }
 
         @Override
         public void run()
         {
             // if we are not running then don't increase any concurrent consumers
             if (!isRunning())
             {
                 logger.trace("Throttling relaxer running, but DMLC is not running");
                 return;
             }
             int newMax = counter.incrementAndGet();
             int current = getMaxConcurrentConsumers();
             boolean done = false;
             if (newMax > current)
             {
                 logger.info(String.format("Relaxing throttling, growing max from: [%s] to [%s]", current, newMax));
                 setThrottledConcurrentConsumers(newMax);
             }
             else
             {
                 logger.info(String.format("Throttler relaxer running but max concurrent consumers: [%s], then new value: [%s]",
                     current, newMax));
                 done = true;
             }
             // check if we are done
             if (done || counter.isDone())
             {
                 int max = (newMax > current) ? newMax : current;
                 logger.info(String.format("Cancelling Throttler relaxer task as old max re-instated or passed: [%s]", max));
                 cancelThrottleTask();
             }
         }
 
     }
     
 }
