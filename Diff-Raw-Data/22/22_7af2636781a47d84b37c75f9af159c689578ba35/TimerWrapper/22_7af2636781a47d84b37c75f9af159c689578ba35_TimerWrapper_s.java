 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.concurrent;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.yammer.metrics.core.Meter;
 import com.yammer.metrics.core.MetricsRegistry;
 import com.yammer.metrics.core.Timer;
 
 class TimerWrapper extends CallableWrapper
 {
     private final String threadPoolName;
     private Metrics metrics;
 
     public TimerWrapper(String threadPoolName)
     {
         this.threadPoolName = threadPoolName;
     }
 
     TimerWrapper setMetricsRegistry(MetricsRegistry registry)
     {
         this.metrics = new Metrics(threadPoolName, registry);
         return this;
     }
 
     @Override
     public <T> Callable<T> wrap(final Callable<T> callable)
     {
         final long enqueueNanos = System.nanoTime();
         final Metrics myMetrics = metrics;
         if (myMetrics == null) {
             return callable;
         }
 
         myMetrics.enqueueMeter.mark();
 
         return new Callable<T>() {
             @Override
             public T call() throws Exception
             {
                 myMetrics.queueTimer.update(System.nanoTime() - enqueueNanos, TimeUnit.NANOSECONDS);
                 myMetrics.dequeueMeter.mark();
                 try {
                     return callable.call();
                 } catch (Throwable t) {
                     myMetrics.exceptionMeter.mark();
                     throw t;
                 } finally {
                     myMetrics.totalTimer.update(System.nanoTime() - enqueueNanos, TimeUnit.NANOSECONDS);
                 }
             }
         };
     }
 
     static class Metrics
     {
         Meter exceptionMeter;
         Meter enqueueMeter;
         Meter dequeueMeter;
         Timer queueTimer;
         Timer totalTimer;
 
         public Metrics(String threadPoolName, MetricsRegistry registry)
         {
             String baseName = threadPoolName + ".";
             exceptionMeter = registry.newMeter(TimedExecutors.class, baseName + "exception", "exception", TimeUnit.MINUTES);
             enqueueMeter = registry.newMeter(TimedExecutors.class, baseName + "enqueue", "enqueue", TimeUnit.MINUTES);
             dequeueMeter = registry.newMeter(TimedExecutors.class, baseName + "dequeue", "dequeue", TimeUnit.MINUTES);
             queueTimer = registry.newTimer(TimedExecutors.class, baseName + "queued-duration");
             totalTimer = registry.newTimer(TimedExecutors.class, baseName + "total-duration");
         }
     }
 }
 
 class TimerWrapperProvider implements Provider<TimerWrapper>
 {
     private MetricsRegistry registry;
     private final String threadPoolName;
 
     public TimerWrapperProvider(String threadPoolName)
     {
         this.threadPoolName = threadPoolName;
     }
 
     @Inject(optional=true)
     void setMetricsRegistry(MetricsRegistry registry)
     {
         this.registry = registry;
     }
 
     @Override
     public TimerWrapper get()
     {
         TimerWrapper wrapper = new TimerWrapper(threadPoolName);
         if (registry != null) {
             wrapper = wrapper.setMetricsRegistry(registry);
         }
         return wrapper;
     }
 }
