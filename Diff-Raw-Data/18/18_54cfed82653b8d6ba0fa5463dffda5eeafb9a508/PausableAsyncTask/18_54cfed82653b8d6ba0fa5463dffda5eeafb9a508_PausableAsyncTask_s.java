 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.jobsystem;
 
 import com.esotericsoftware.reflectasm.MethodAccess;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import javafx.concurrent.Task;
 
 public class PausableAsyncTask<V> extends Task<V> implements Pausable {
 
     private static final BiMap<Thread, PausableAsyncTask<?>> threadTaskPairs = HashBiMap.create();
 
     static <V> PausableAsyncTask<V> getTaskFromThread() {
         return (PausableAsyncTask<V>) threadTaskPairs.get(Thread.currentThread());
     }
 
     private static void putThreadTaskPair(Thread t, PausableAsyncTask<?> s) {
         threadTaskPairs.put(t, s);
     }
 
     private static void removeThreadTaskPair(Thread t) {
         threadTaskPairs.remove(t);
     }
 
     private Thread getThreadFromTask() {
         return threadTaskPairs.inverse().get(this);
     }
 
     private final JobNode node;
     private final String jobName;
     private final Object[] params;
 
     private boolean paused = false;
     private final Object pauseLock = new Object();
 
     public PausableAsyncTask(JobNode node, String jobName, Object... params) {
         if (!node.getJobs().contains(jobName))
            throw new JobNotExistsException(jobName + ": no such job for class " + node.getClass());
         this.node = node;
         this.jobName = jobName;
         this.params = params;
     }
 
     @Override
     public void pause() {
         // TODO should set paused when the task gets actually paused?
         if (!isRunning())
             throw new IllegalStateException("Cannot pause a task if it is not in RUNNING or SCHEDULED state");
         if (!paused && isRunning()) {
             paused = true;
             getThreadFromTask().interrupt();
         }
     }
 
     @Override
     public boolean isPaused() {
         return paused;
     }
 
     @Override
     public void waitWhilePaused() throws InterruptedException {
         while (paused) {
             synchronized (pauseLock) {
                 pauseLock.wait();
             }
         }
     }
 
     @Override
     public void resume() {
         if (paused) {
             paused = false;
             synchronized (pauseLock) {
                 pauseLock.notifyAll();
             }
         } else
             throw new IllegalStateException("Cannot resume a task if it is not paused");
     }
 
     @Override
     protected V call() throws Exception {
         // TODO make observer optional and with custom sleep time
         Thread t = new Thread(new Runnable() {
             final Thread main = Thread.currentThread();
 
             @Override
             public void run() {
                 try {
                     while (true) {
                         try {
                             int progress = JobNode.getProgress(main);
                             PausableAsyncTask.this.updateProgress(progress, 100);
                         } catch (TryLaterException ex) {
                         }
                         Thread.sleep(5);
                     }
                 } catch (InterruptedException ex) { /* stop observer */ }
             }
         });
         try {
             updateProgress(0, 100);
             t.start();
             PausableAsyncTask.putThreadTaskPair(Thread.currentThread(), PausableAsyncTask.this);
             MethodAccess access = MethodAccess.get(node.getClass());
             V ret = (V) access.invoke(node, jobName, params);
             updateProgress(100, 100);
             return ret;
         } finally {
             PausableAsyncTask.removeThreadTaskPair(Thread.currentThread());
             t.interrupt();
         }
     }
 
     @Override
     protected void cancelled() {
         super.cancelled();
         paused = false;
     }
 
     @Override
     protected void failed() {
         super.failed();
         if (paused) {
             paused = false;
             synchronized (pauseLock) {
                 pauseLock.notifyAll();
             }
         }
     }
 
     @Override
     protected void succeeded() {
         super.succeeded();
         paused = false;
     }
 
 }
