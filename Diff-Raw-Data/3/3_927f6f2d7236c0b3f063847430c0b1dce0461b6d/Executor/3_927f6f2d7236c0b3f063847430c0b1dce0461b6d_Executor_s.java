 /*
 
     Executor library for Processing.
     Copyright (c) 2012-2013 held jointly by the individual authors.
 
     This file is part of Executor library for Processing.
 
     Executor library for Processing is free software: you can redistribute it and/or
     modify it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Executor library for Processing is distributed in the hope that it will be
     useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Executor library for Processing.  If not, see
     <http://www.gnu.org/licenses/>.
 
 */
 package org.dishevelled.processing.executor;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.SwingUtilities;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 
 import processing.core.PApplet;
 
 /**
  * Executor.
  *
  * @author  Michael Heuer
  */
 public final class Executor
 {
     private final PApplet applet;
     private final LoadingCache<String, Runnable> calls;
     private final ScheduledExecutorService scheduledExecutorService;
 
    public Executor(final PApplet applet, final int threadPoolSize) {
         checkNotNull(applet, "applet must not be null");
         this.applet = applet;
         calls = CacheBuilder.newBuilder().build(new ReflectiveMethodCall());
         scheduledExecutorService = Executors.newScheduledThreadPool(threadPoolSize);
     }
 
 
     // call method names on applet via reflection
 
     Runnable call(final String methodName)
     {
         checkNotNull(methodName, "methodName must not be null");
         return calls.getUnchecked(methodName);
     }
 
     // schedule method name calls
 
     public ScheduledFuture<?> later(final String methodName)
     {
         return later(call(methodName));
     }
 
     public ScheduledFuture<?> later(final String methodName, final long ms)
     {
         return later(call(methodName), ms);
     }
 
     public ScheduledFuture<?> later(final String methodName, final long delay, final TimeUnit unit)
     {
         return later(call(methodName), delay, unit);
     }
 
     public ScheduledFuture<?> after(final ScheduledFuture<?> future, final String methodName)
     {
         return after(future, call(methodName));
     }
 
     public ScheduledFuture<?> after(final ScheduledFuture<?> future, final String methodName, final long ms)
     {
         return after(future, call(methodName), ms);
     }
 
     public ScheduledFuture<?> after(final ScheduledFuture<?> future, final String methodName, final long delay, final TimeUnit unit)
     {
         return after(future, call(methodName), delay, unit);
     }
 
     public ScheduledFuture<?> repeat(final String methodName, final long ms)
     {
         return repeat(call(methodName), ms);
     }
 
     public ScheduledFuture<?> repeat(final String methodName, final long delay, final TimeUnit unit)
     {
         return repeat(call(methodName), delay, unit);
     }
 
     public ScheduledFuture<?> repeat(final String methodName, final long initialDelay, final long delay, final TimeUnit unit)
     {
         return repeat(call(methodName), initialDelay, delay, unit);
     }
 
     // schedule runnable tasks
 
     ScheduledFuture<?> later(final Runnable task)
     {
         return scheduledExecutorService.schedule(task, 0L, TimeUnit.MILLISECONDS);
     }
 
     ScheduledFuture<?> later(final Runnable task, final long ms)
     {
         return scheduledExecutorService.schedule(task, ms, TimeUnit.MILLISECONDS);
     }
 
     ScheduledFuture<?> later(final Runnable task, final long delay, final TimeUnit unit)
     {
         return scheduledExecutorService.schedule(task, delay, unit);
     }
 
     ScheduledFuture<?> after(final ScheduledFuture<?> future, final Runnable task)
     {
         checkNotNull(future, "future must not be null");
         return scheduledExecutorService.schedule(task, future.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
     }
 
     ScheduledFuture<?> after(final ScheduledFuture<?> future, final Runnable task, final long ms)
     {
         checkNotNull(future, "future must not be null");
         return scheduledExecutorService.schedule(task, future.getDelay(TimeUnit.MILLISECONDS) + ms, TimeUnit.MILLISECONDS);
     }
 
     ScheduledFuture<?> after(final ScheduledFuture<?> future, final Runnable task, final long delay, final TimeUnit unit)
     {
         checkNotNull(future, "future must not be null");
         return scheduledExecutorService.schedule(task, future.getDelay(unit) + delay, unit);
     }
 
     ScheduledFuture<?> repeat(final Runnable task, final long ms)
     {
         return scheduledExecutorService.scheduleWithFixedDelay(task, 0L, ms, TimeUnit.MILLISECONDS);
     }
 
     ScheduledFuture<?> repeat(final Runnable task, final long delay, final TimeUnit unit)
     {
         return scheduledExecutorService.scheduleWithFixedDelay(task, 0L, delay, unit);
     }
 
     ScheduledFuture<?> repeat(final Runnable task, final long initialDelay, final long delay, final TimeUnit unit)
     {
         return scheduledExecutorService.scheduleWithFixedDelay(task, initialDelay, delay, unit);
     }
 
     /**
      * Cache loader for building reflective method call tasks.
      */
     private final class ReflectiveMethodCall extends CacheLoader<String, Runnable>
     {
         @Override
         public Runnable load(final String methodName)
         {
             return new Runnable()
                 {
                     @Override
                     public void run()
                     {
                         Runnable reflectiveMethodCall = new Runnable()
                             {
                                 @Override
                                 public void run()
                                 {
                                     try
                                     {
                                         Method method = applet.getClass().getMethod(methodName, (Class<?>[]) null);
                                         method.invoke(applet, (Object[]) null);
                                     }
                                     catch (IllegalAccessException e)
                                     {
                                         // ignore
                                     }
                                     catch (IllegalArgumentException e)
                                     {
                                         // ignore
                                     }
                                     catch (InvocationTargetException e)
                                     {
                                         // ignore
                                     }
                                     catch (NoSuchMethodException e)
                                     {
                                         // ignore
                                     }
                                     catch (SecurityException e)
                                     {
                                         // ignore
                                     }
                                 }
                             };
 
                         // be sure to invoke on the event dispatch thread
                         if (SwingUtilities.isEventDispatchThread())
                         {
                             reflectiveMethodCall.run();
                         }
                         else
                         {
                             SwingUtilities.invokeLater(reflectiveMethodCall);
                         }
                     }
             };
         }
     }
 }
