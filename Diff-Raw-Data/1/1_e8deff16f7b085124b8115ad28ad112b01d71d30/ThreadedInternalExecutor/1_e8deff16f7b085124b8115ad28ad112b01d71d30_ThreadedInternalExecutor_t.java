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
 
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.LockSupport;
 
 import com.mattunderscore.executors.ITaskWrapper;
 
 /*package*/class ThreadedInternalExecutor implements IInternalExecutor
 {
     private final long rate;
     private final TimeUnit unit;
     private final TaskQueue taskQueue;
     private final LoopingTask thisTask;
     private final ThreadFactory factory;
     private Thread thread;
     private volatile boolean running = false;
     private boolean stopping = false;
     private volatile boolean interruptable;
 
     /* package */ThreadedInternalExecutor(final TaskQueue taskQueue, final long rate,
             final TimeUnit unit, final ThreadFactory factory)
     {
         this.thisTask = new LoopingTask();
         this.factory = factory;
         this.taskQueue = taskQueue;
         this.rate = rate;
         this.unit = unit;
     }
 
     @Override
     public void submit(ITaskWrapper wrapper)
     {
         taskQueue.add(wrapper);
         stopping = false;
         if (!running)
         {
             start();
         }
     }
 
     /**
      * Start the execution of the queued tasks.
      */
     private void start()
     {
         if (running)
         {
             return;
         }
         else
         {
             running = true;
             thread = factory.newThread(thisTask);
             thread.start();
         }
     }
 
     @Override
     public void requestStop()
     {
         if (taskQueue.isEmpty())
         {
             stopping = true;
         }
     }
 
     @Override
     public void stop()
     {
         if (running)
         {
             running = false;
         }
     }
 
     @Override
     public boolean interrupt()
     {
         if (interruptable)
         {
             thread.interrupt();
             return true;
         }
         else
         {
             return false;
         }
     }
 
     private final class LoopingTask implements Runnable
     {
         public void run()
         {
             final long rateInNanos = TimeUnit.NANOSECONDS.convert(rate, unit);
             long targetTime = System.nanoTime() + rateInNanos;
             while (running)
             {
 
                 // Execute next task
                 final ITaskWrapper task = taskQueue.poll();
                 if (task != null)
                 {
                     interruptable = true;
                     task.execute();
                    taskQueue.clearCurrentTask();
                     interruptable = false;
                 }
                 // Sleep until the next execution
                 while (true)
                 {
                     // Time until target
                     final long sleepFor = targetTime - System.nanoTime();
                     if (sleepFor > 0)
                     {
                         LockSupport.parkNanos(sleepFor);
                     }
                     else
                     {
                         break;
                     }
                 }
                 // Stop if needed
                 if (stopping)
                 {
                     stop();
                 }
                 // Calculate the next time to run off the time it was supposed to run last. This
                 // provides more accurate scheduling than calculating the next time to run off
                 // the time it actually ran
                 targetTime = targetTime + rateInNanos;
             }
         }
     }
 }
