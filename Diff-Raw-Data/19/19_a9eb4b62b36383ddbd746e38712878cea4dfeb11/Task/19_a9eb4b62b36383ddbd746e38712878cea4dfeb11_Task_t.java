 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2012 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.task;
 
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A scheduled background task. This class handles execution of a
  * single task and provides some simple accessors for information
  * about it.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public abstract class Task extends TimerTask {
 
     /**
      * The class logger.
      */
     private static final Logger LOG = Logger.getLogger(Task.class.getName());
 
     /**
      * The unique task identifier. This is used to prevent scheduling
      * multiple instances of the same task.
      */
     public String id = null;
 
     /**
      * The last execution time (in milliseconds since Epoch) of the
      * task. This value is set each time the task execution begins.
      * Before the first run, this value will be zero (0).
      */
     public long lastExecutionTime = 0L;
 
     /**
      * The next scheduled execution time (in milliseconds since
      * Epoch) of the task. This is an approximate value, adjusted
      * both when task execution begins and when it terminates. The
      * value will be set to zero (0) when a single-run task begins
      * execution or when a recurring task is canceled.
      */
     public long nextExecutionTime = 0L;
 
     /**
      * The recurring scheduler period. This is non-zero if this task
      * is scheduled for recurring execution.
      */
     public long schedulerPeriod = 0L;
 
     /**
      * Creates a new scheduler task with the provided unique id. The
      * identifier is used to prevent scheduling multiple versions of
      * the same task.
      *
      * @param id             the unique task identifier
      */
     public Task(String id) {
         this.id = id;
     }
 
     /**
      * Executes this task and updates the scheduling timers. This
      * method also ensures proper logging of exceptions, etc.
      */
     public final void run() {
         lastExecutionTime = System.currentTimeMillis();
         if (schedulerPeriod <= 0L) {
             nextExecutionTime = 0L;
         } else {
             nextExecutionTime = lastExecutionTime + schedulerPeriod;
         }
         try {
             LOG.fine("starting scheduled task '" + id + "'");
             execute();
             LOG.fine("completed scheduled task '" + id + "'");
         } catch (Throwable t) {
             LOG.log(Level.WARNING, "exception in scheduled task '" + id + "'", t);
         }
         if (schedulerPeriod > 0L) {
             nextExecutionTime = System.currentTimeMillis() + schedulerPeriod;
         } else {
             Scheduler.unschedule(this);
         }
     }
 
     /**
      * Executes this task.
      *
      * @throws Exception if an error occurred during the execution
      */
     public abstract void execute() throws Exception;
 
     /**
      * Cancels this scheduled task. This method is safe to call
      * multiple times.
      *
      * @return true if the task was canceled, or
      *         false if it were no longer scheduled (already canceled)
      */
     public boolean cancel() {
         if (nextExecutionTime >= 0L) {
             nextExecutionTime = -1L;
             return Scheduler.unschedule(this);
         } else {
             return super.cancel();
         }
     }
}
