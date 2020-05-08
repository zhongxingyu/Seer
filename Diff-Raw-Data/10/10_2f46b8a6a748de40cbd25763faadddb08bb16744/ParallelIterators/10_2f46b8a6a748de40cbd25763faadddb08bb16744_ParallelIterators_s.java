 /*
  * Copyright (C) 2010 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.parallel;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterators;
 import com.google.common.util.concurrent.ValueFuture;
 
 import java.util.Iterator;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.trancecode.collection.TcIterators;
 import org.trancecode.function.TcPredicates;
 
 /**
  * Parallel equivalent of {@link Iterators}.
  * 
  * @author Herve Quiroz
  */
 public final class ParallelIterators
 {
     private static final int MAXIMUM_NUMBER_OF_ELEMENTS_IN_ADVANCE = 1024;
 
     private static Future<?> LAST = ValueFuture.create();
 
     private static <T> Future<T> last()
     {
         @SuppressWarnings("unchecked")
         final Future<T> last = (Future<T>) LAST;
         return last;
     }
 
     /**
      * @see Iterators#transform(Iterator, Function)
      */
     public static <F, T> Iterator<T> transform(final Iterator<F> fromIterable,
             final Function<? super F, ? extends T> function, final ExecutorService executor)
     {
         final Function<F, Callable<T>> applyFunction = ParallelFunctions.apply(function);
         final Iterator<Callable<T>> callables = Iterators.transform(fromIterable, applyFunction);
 
         final AtomicReference<Future<?>> submitTask = new AtomicReference<Future<?>>();
 
         final BlockingQueue<Future<T>> futures = new ArrayBlockingQueue<Future<T>>(
                 MAXIMUM_NUMBER_OF_ELEMENTS_IN_ADVANCE);
         final Function<Callable<T>, Future<T>> submitFunction = new Function<Callable<T>, Future<T>>()
         {
             @Override
             public Future<T> apply(final Callable<T> task)
             {
                 return executor.submit(new Callable<T>()
                 {
                     @Override
                     public T call() throws Exception
                     {
                         try
                         {
                             return task.call();
                         }
                         catch (final Exception e)
                         {
                             final Future<?> taskToCancel = submitTask.get();
                             if (taskToCancel != null)
                             {
                                 taskToCancel.cancel(true);
                                 final Future<T> last = last();
                                 futures.add(last);
                             }
                             throw e;
                         }
                     }
                 });
             }
         };
 
         submitTask.set(executor.submit(new Runnable()
         {
             @Override
             public void run()
             {
                 Iterators.addAll(futures, Iterators.transform(callables, submitFunction));
                 final Future<T> last = last();
                 futures.add(last);
             }
         }));
 
        return getUntilLast(TcIterators.removeAll(futures));
     }
 
     private static <T> Iterator<T> getUntilLast(final Iterator<Future<T>> futures)
     {
         final Future<T> last = last();
         final Iterator<Future<T>> futuresUntilLast = TcIterators.until(futures, TcPredicates.identicalTo(last));
         final Function<Future<T>, T> function = ParallelFunctions.get();
         return Iterators.transform(futuresUntilLast, function);
     }
 
     private ParallelIterators()
     {
         // No instantiation
     }
 }
