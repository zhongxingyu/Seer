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
 
 import javafx.concurrent.Service;
 import javafx.concurrent.Task;
 
 public class PausableAsyncService<V> extends Service<V> implements Pausable {
 
     private final JobNode node;
     private final String jobName;
     private final Object[] params;
 
     private PausableAsyncTask<V> currentTask;
 
     public PausableAsyncService(JobNode node, String jobName, Object... params) {
         if (!node.getJobs().contains(jobName))
            throw new JobNotExistsException(node, jobName);
         this.node = node;
         this.jobName = jobName;
         this.params = params;
     }
 
     @Override
     public void pause() {
         if (currentTask == null)
             throw new IllegalStateException("Cannot pause a service if it is not in RUNNING or SCHEDULED state");
         currentTask.pause();
     }
 
     @Override
     public boolean isPaused() {
         return currentTask == null ? false : currentTask.isPaused();
     }
 
     @Override
     public void waitWhilePaused() throws InterruptedException {
         if (currentTask != null)
             currentTask.waitWhilePaused();
     }
 
     @Override
     public void resume() {
         if (currentTask != null)
             currentTask.resume();
         else
             throw new IllegalStateException("Cannot resume a service if it is not paused");
     }
 
     @Override
     protected Task<V> createTask() {
         currentTask = new PausableAsyncTask<V>(node, jobName, params);
         return currentTask;
     }
 
     @Override
     protected void cancelled() {
         super.cancelled();
         currentTask = null;
     }
 
     @Override
     protected void failed() {
         super.failed();
         currentTask = null;
     }
 
     @Override
     protected void succeeded() {
         super.succeeded();
         currentTask = null;
     }
 
 }
