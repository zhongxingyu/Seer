 /*
  * Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.rjeschke.neetutils.concurrent;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import com.github.rjeschke.neetutils.SysUtils;
 
 class RequeueWatcher<A, B> implements Runnable
 {
     private final RequeueWatcherCallback<A, B> callback;
     private final ConcurrentLinkedQueue<A> jobs;
     private final ConcurrentLinkedQueue<B> workers;
     private Thread thread;
     private int delayMs = 100;
     private volatile boolean running = true;
     
    private RequeueWatcher(RequeueWatcherCallback<A, B> callback, ConcurrentLinkedQueue<A> jobs, ConcurrentLinkedQueue<B> workers)
     {
         this.callback = callback;
         this.jobs = jobs;
         this.workers = workers;
     }
     
     public static <A, B> RequeueWatcher<A, B> start(RequeueWatcherCallback<A, B> callback, ConcurrentLinkedQueue<A> jobs, ConcurrentLinkedQueue<B> workers)
     {
         final RequeueWatcher<A, B> watcher = new RequeueWatcher<A, B>(callback, jobs, workers);
         watcher.thread = new Thread(watcher);
         watcher.thread.setDaemon(true);
         watcher.thread.start();
         
         return watcher;
     }
     
     public void stop()
     {
         this.running = false;
         try
         {
             this.thread.join();
         }
         catch (InterruptedException e)
         {
             // ignore
         }
     }
     
     @Override
     public void run()
     {
         while(this.running)
         {
             final long t0 = System.nanoTime();
             if(!this.jobs.isEmpty() && !this.workers.isEmpty())
             {
                 final A job = this.jobs.poll();
                 final B worker = this.workers.poll();
                 
                 if(job != null && worker != null)
                 {
                     this.callback.requeue(worker, job);
                 }
                 else
                 {
                     if(job != null)
                         this.jobs.offer(job);
                     if(worker != null)
                         this.workers.offer(worker);
                 }
             }
             final long t1 = System.nanoTime();
             final int ms = this.delayMs - (int)((t1 - t0) / 1000000);
             SysUtils.fineSleep(Math.max(1, ms));
         }
     }
 }
