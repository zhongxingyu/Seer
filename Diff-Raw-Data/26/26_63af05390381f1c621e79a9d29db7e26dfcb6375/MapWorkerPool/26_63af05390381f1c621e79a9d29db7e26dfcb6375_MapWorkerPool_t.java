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
 
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Semaphore;
 
 import com.github.rjeschke.neetutils.SysUtils;
 import com.github.rjeschke.neetutils.collections.Colls;
 
 /**
  * 
  * @author René Jeschke (rene_jeschke@yahoo.de)
  * 
  * @param <A>
  * @param <B>
  */
 public class MapWorkerPool<A, B> implements Runnable, RequeueWatcherCallback<MapWorkerPool.Job<A, B>, MapWorkerPool.ThreadWorker<A, B>>
 {
     private final int numThreads;
     private final int queueLimit;
     private final boolean serialCallbacks;
     private final MapWorkerCallback<A, B> callback;
     private final ConcurrentLinkedQueue<ThreadWorker<A, B>> workers = new ConcurrentLinkedQueue<ThreadWorker<A, B>>();
     private final ConcurrentLinkedQueue<Job<A, B>> jobs = new ConcurrentLinkedQueue<Job<A, B>>();
     private final ConcurrentLinkedQueue<WorkerResult<A, B>> results = new ConcurrentLinkedQueue<WorkerResult<A, B>>();
     private final Semaphore resultSync = new Semaphore(1);
     private final Thread[] threads;
     private Thread callbackThread = null;
     private RequeueWatcher<Job<A, B>, ThreadWorker<A, B>> watcher;
 
     private MapWorkerPool(MapWorkerCallback<A, B> callback, int threads, int queueLimit, boolean serialCallbacks)
     {
         this.callback = callback;
         this.numThreads = threads;
         this.serialCallbacks = serialCallbacks;
         this.queueLimit = Math.max(0, queueLimit);
         this.threads = new Thread[threads];
     }
 
     public static <A, B> MapWorkerPool<A, B> start(MapWorkerCallback<A, B> callback, int threads, int queueLimit,
             boolean serialCallbacks)
     {
         final MapWorkerPool<A, B> jobber = new MapWorkerPool<A, B>(callback, ThreadPool.defaultThreadcount(threads),
                 queueLimit, serialCallbacks);
 
         for(int i = 0; i < jobber.threads.length; i++)
         {
             final ThreadWorker<A, B> w = new ThreadWorker<A, B>(jobber);
             final Thread t = new Thread(w);
             t.setDaemon(true);
             jobber.workers.offer(w);
             t.start();
             jobber.threads[i] = t;
         }
 
         if(serialCallbacks)
         {
             jobber.resultSync.acquireUninterruptibly();
             final Thread t = new Thread(jobber);
             t.setDaemon(true);
             t.start();
             jobber.callbackThread = t;
         }
 
         jobber.watcher = RequeueWatcher.start(jobber, jobber.jobs, jobber.workers);
         
         return jobber;
     }
 
    public static <A, B> List<B> processCollection(MapWorker<A, B> worker, int threads, Iterable<A> input)
     {
         final int usedThreads = ThreadPool.defaultThreadcount(threads);
         final ProcessCollectionMapWorkerCallback<A, B> callback = new ProcessCollectionMapWorkerCallback<A, B>();
         final MapWorkerPool<A, B> pool = MapWorkerPool.start(callback, usedThreads, usedThreads * 4, true);
 
         for(final A a : input)
             pool.enqueue(worker, a);
 
         pool.stop();
 
         return callback.outputList;
     }
 
     public final static int availableProcessors()
     {
         return Runtime.getRuntime().availableProcessors();
     }
 
     public int threadCount()
     {
         return this.numThreads;
     }
 
     public void enqueue(MapWorker<A, B> worker, A object)
     {
         if(worker == null)
             throw new NullPointerException("A null Worker is not permitted");
 
         final ThreadWorker<A, B> w = this.workers.poll();
         final Job<A, B> job = new Job<A, B>(worker, object);
         if(w != null)
         {
             w.setWorkLoad(job);
         }
         else
         {
             if(this.queueLimit != 0 && this.jobs.size() >= this.queueLimit)
             {
                 final int ql = Math.max(this.queueLimit >> 1, 1);
                 while(this.jobs.size() > ql)
                     SysUtils.fineSleep(5);
             }
 
             this.jobs.offer(job);
         }
     }
 
     private void reuseOrEnqueue(ThreadWorker<A, B> w)
     {
         final Job<A, B> job = this.jobs.poll();
         if(job != null)
             w.setWorkLoad(job);
         else
             this.workers.offer(w);
     }
 
     void doCallback(ThreadWorker<A, B> threadWorker, MapWorker<A, B> worker, WorkerStatus status, A input, B output)
     {
         if(this.serialCallbacks)
         {
             this.results.offer(new WorkerResult<A, B>(worker, status, input, output));
             this.resultSync.release();
         }
         else
         {
             try
             {
                 this.callback.workerCallback(this, worker, status, input, output);
             }
             catch (Throwable t)
             {
                 //
             }
         }
         this.reuseOrEnqueue(threadWorker);
     }
 
     public boolean hasWork()
     {
         return !this.jobs.isEmpty();
     }
 
     public void join()
     {
         while(!this.jobs.isEmpty() || (this.serialCallbacks && !this.results.isEmpty()))
             SysUtils.sleep(10);
     }
 
     public void stop()
     {
         final StopWorker<A, B> stop = new StopWorker<A, B>();
 
         this.join();
 
         for(int i = 0; i < this.numThreads; i++)
             this.enqueue(stop, null);
 
         for(int i = 0; i < this.numThreads; i++)
             SysUtils.threadJoin(this.threads[i]);
 
         if(this.callbackThread != null)
         {
             SysUtils.sleep(50);
             this.results.offer(new WorkerResult<A, B>(null, null, null, null));
             this.resultSync.release();
             SysUtils.threadJoin(this.callbackThread);
         }
         
         this.watcher.stop();
     }
 
     @Override
     public void run()
     {
         for(;;)
         {
             try
             {
                 this.resultSync.acquireUninterruptibly();
                 final WorkerResult<A, B> r = this.results.poll();
                 if(r.status == null)
                     break;
                 this.callback.workerCallback(this, r.worker, r.status, r.input, r.output);
             }
             catch (Throwable t)
             {
                 //
             }
         }
     }
 
     static class WorkerResult<A, B>
     {
         final MapWorker<A, B> worker;
         final WorkerStatus status;
         final A input;
         final B output;
 
         public WorkerResult(MapWorker<A, B> worker, WorkerStatus status, A input, B output)
         {
             this.worker = worker;
             this.status = status;
             this.input = input;
             this.output = output;
         }
     }
 
     static class ThreadWorker<A, B> implements Runnable
     {
         private final Semaphore sync = new Semaphore(1);
         private final MapWorkerPool<A, B> pool;
         private volatile Job<A, B> workload = null;
 
         public ThreadWorker(MapWorkerPool<A, B> pool)
         {
             this.sync.acquireUninterruptibly();
             this.pool = pool;
         }
 
         protected void setWorkLoad(Job<A, B> job)
         {
             this.workload = job;
             this.sync.release();
         }
 
         @Override
         public void run()
         {
             for(;;)
             {
                 boolean ok = true;
                 Throwable ta = null;
                 B output = null;
                 try
                 {
                     this.sync.acquireUninterruptibly();
                     if(this.workload.worker instanceof StopWorker)
                         break;
                     output = this.workload.worker.run(this.workload.input);
                 }
                 catch (Throwable t)
                 {
                     ta = t;
                     ok = false;
                 }
 
                 this.pool.doCallback(this, this.workload.worker, ok ? WorkerStatus.OK : new WorkerStatus(ta),
                         this.workload.input, output);
             }
         }
     }
 
     static class Job<A, B>
     {
         public final MapWorker<A, B> worker;
         public final A input;
 
         public Job(MapWorker<A, B> worker, A input)
         {
             this.worker = worker;
             this.input = input;
         }
     }
 
     static class StopWorker<A, B> implements MapWorker<A, B>
     {
         public StopWorker()
         {
             //
         }
 
         @Override
         public B run(A object)
         {
             return null;
         }
     }
 
     static class ProcessCollectionMapWorkerCallback<A, B> implements MapWorkerCallback<A, B>
     {
         final List<B> outputList = Colls.list();
 
         public ProcessCollectionMapWorkerCallback()
         {
             // empty
         }
 
         @Override
         public void workerCallback(MapWorkerPool<A, B> pool, MapWorker<A, B> worker, WorkerStatus status, A input,
                 B output)
         {
             this.outputList.add(output);
         }
     }
 
     @Override
     public void requeue(ThreadWorker<A, B> worker, Job<A, B> job)
     {
         worker.setWorkLoad(job);
     }
 }
