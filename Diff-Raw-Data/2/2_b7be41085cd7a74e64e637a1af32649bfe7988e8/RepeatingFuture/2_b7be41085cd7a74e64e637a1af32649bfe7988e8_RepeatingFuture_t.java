 /* Copyright © 2013 Matthew Champion
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
  * Neither the name of mattunderscore.com nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL MATTHEW CHAMPION BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 
 package com.mattunderscore.rated.executor;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 /**
  * A base implementation for a {@link Future} of a task that is executed repeatedly but a limited
  * number of times.
  * <P>
  * The {@link #get()} and {@link #get(long, TimeUnit)} methods return the result of the most recent
  * execution and only block if the task has not yet run. This implementation does not support
  * changing the number of times to repeat the task.
  * 
  * @author Matt Champion
  * @param <V>
  *            The type of object returned by {@link #get()}
  * @since 0.1.0
  */
 /* package */ final class RepeatingFuture<V> extends BaseFuture<V> implements IRepeatingFuture<V>
 {
     private final List<TaskExecutionResult<V>> results = new ArrayList<TaskExecutionResult<V>>();
     private final int repetitions;
     private final CountDownLatch[] latches;
     private final TaskCanceller canceller;
     private TaskWrapper task;
     private int cancellationPoint = -1;
 
     /**
      * Constructor for the future. This should be called by the subclass
      * 
      * @param repetitions
      *            The number of times it is to repeat
      */
     public RepeatingFuture(final TaskCanceller canceller, final int repetitions)
     {
         this.canceller = canceller;
         this.repetitions = repetitions;
         latches = new CountDownLatch[repetitions];
         for (int i = 0; i < repetitions; i++)
         {
             latches[i] = new CountDownLatch(1);
         }
     }
 
     @Override
     public V getResult(int index) throws InterruptedException, ExecutionException,
             CancellationException, IndexOutOfBoundsException
     {
         latches[index].await();
         checkCancellationException();
         TaskExecutionResult<V> result = getStoredResult(index);
         checkExecutionException(result);
         return result.result;
     }
 
     @Override
     public V getResult(int index, long timeout, TimeUnit unit) throws InterruptedException,
             ExecutionException, CancellationException, TimeoutException, IndexOutOfBoundsException
     {
         if (!latches[index].await(timeout, unit))
         {
             throw new TimeoutException();
         }
         checkCancellationException();
         TaskExecutionResult<V> result = getStoredResult(index);
         checkExecutionException(result);
         return result.result;
     }
 
     @Override
     public int getExpectedExecutions()
     {
         return repetitions;
     }
 
     @Override
     public int getCompletedExecutions()
     {
         return results.size();
     }
 
     protected void checkCancellationException(int index) throws CancellationException
     {
         if (index > cancellationPoint)
         {
             throw new CancellationException();
         }
     }
 
     @Override
     protected void processResult(TaskExecutionResult<V> result)
     {
         int index = results.size();
         results.add(result);
         latches[index].countDown();
     }
 
     @Override
     protected boolean processCancellation(boolean mayInterruptIfRunning)
     {
         cancellationPoint = results.size();
         for (int i = cancellationPoint; i < latches.length; i++)
         {
             latches[i].countDown();
         }
         final boolean cancelled = canceller.cancelTask(task, mayInterruptIfRunning);
         return cancelled;
     }
 
     @Override
     protected boolean taskDone()
     {
         return repetitions == results.size();
     }
 
     @Override
     protected void await() throws InterruptedException
     {
         latches[0].await();
     }
 
     @Override
     protected boolean await(long timeout, TimeUnit unit) throws InterruptedException
     {
         return latches[0].await(timeout, unit);
     }
 
     @Override
     protected TaskExecutionResult<V> getResult()
     {
        return getStoredResult(results.size()-1);
     }
 
     /**
      * Gets the ith execution result.
      * 
      * @param index
      *            The result to get
      * @return The result
      */
     protected TaskExecutionResult<V> getStoredResult(final int index)
     {
         return results.get(index);
     }
 
     @Override
     public void setTask(TaskWrapper wrapper)
     {
         this.task = wrapper;
     }
 }
