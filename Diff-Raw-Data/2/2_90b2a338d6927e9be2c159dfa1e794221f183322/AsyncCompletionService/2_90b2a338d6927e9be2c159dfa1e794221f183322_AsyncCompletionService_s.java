 /*
  * Copyright 2010-2011 Roger Kapsi
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package org.ardverk.concurrent;
 
 import java.util.Arrays;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * 
  */
 public class AsyncCompletionService {
 
     private AsyncCompletionService() {}
     
     /**
      * Creates and returns an {@link AsyncFuture} that will fire an event when 
      * the given {@link AsyncFuture}s are complete.
      */
     public static <V, T extends AsyncFuture<? extends V>> AsyncFuture<T[]> create(T... futures) {
         return create(Arrays.asList(futures), futures);
     }
     
     /**
      * Creates and returns an {@link AsyncFuture} that will fire an event when 
      * the given {@link AsyncFuture}s are complete.
      */
    public static <V, T extends Iterable<AsyncFuture<? extends V>>> AsyncFuture<T> create(T futures) {
         return create(futures, futures);
     }
     
     /**
      * Creates and returns an {@link AsyncFuture} that will fire an event when 
      * the given {@link AsyncFuture}s are complete.
      */
     @SuppressWarnings("unchecked")
     public static <T> AsyncFuture<T> create(
             Iterable<? extends AsyncFuture<?>> futures, final T value) {
         final Object lock = new Object();
         synchronized (lock) {
             final AsyncFuture<T> dst = new AsyncValueFuture<T>();
             
             final AtomicInteger counter = new AtomicInteger();
             AsyncFutureListener<Object> listener 
                     = new AsyncFutureListener<Object>() {
                 @Override
                 public void operationComplete(AsyncFuture<Object> future) {
                     synchronized (lock) {
                         if (counter.decrementAndGet() == 0) {
                             try {
                                 dst.setValue(value);
                             } catch (Throwable err) {
                                 dst.setException(err);
                             }
                         }
                     }
                 }
             };
             
             for (AsyncFuture<?> future : futures) {
                 ((AsyncFuture<Object>)future).addAsyncFutureListener(listener);
                 counter.incrementAndGet();
             }
             
             if (counter.get() == 0) {
                 try {
                     dst.setValue(value);
                 } catch (Throwable err) {
                     dst.setException(err);
                 }
             }
             
             return dst;
         }
     }
 }
