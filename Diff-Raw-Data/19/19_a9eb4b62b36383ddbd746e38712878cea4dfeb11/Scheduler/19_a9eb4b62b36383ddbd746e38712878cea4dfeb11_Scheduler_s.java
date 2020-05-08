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
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 /**
  * A task scheduler for asynchronous tasks and background jobs. This
  * class handles all global background threads and task execution and
  * provides a simple time thread implementation to avoid multiple
  * background threads.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class Scheduler {
     // TODO: create threads when needed, terminate when no longer used
     // TODO: add support for one-off (potentially long-running) tasks
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(Scheduler.class.getName());
 
     /**
      * The singleton timer used for actual task scheduling.
      */
     private static Timer timer = new Timer(true);
 
     /**
      * The list of currently scheduled tasks.
      */
     private static ArrayList tasks = new ArrayList();
 
     /**
      * Schedules a recurring task. Only a single task with the same
      * task id is allowed, later tasks with the same id will be
      * ignored (if the first one is still scheduled).
      *
      * @param task           the task to run
      * @param delay          the first execution delay (in millis)
      * @param period         the waiting period (in millis)
      *
      * @return true if the task was scheduled, or
      *         false if an identical task was already in the queue
      */
     public static boolean schedule(Task task, long delay, long period) {
         if (task.nextExecutionTime < 0L) {
             LOG.fine("failed to schedule task '" + task.id +
                      "', already cancelled");
             return false;
         }
         synchronized (tasks) {
             for (int i = 0; i < tasks.size(); i++) {
                 Task other = (Task) tasks.get(i);
                 if (other.id.equals(task.id) && other.nextExecutionTime > 0L) {
                     LOG.fine("failed to schedule task '" + task.id +
                              "', already scheduled");
                     return false;
                 }
             }
             task.nextExecutionTime = System.currentTimeMillis() + delay;
             task.schedulerPeriod = period;
             tasks.add(task);
         }
         timer.schedule(task, delay, period);
         return true;
     }
 
     /**
      * Removes a task from the scheduler queue.
      *
      * @param task           the task to remove
      *
      * @return true if the task was unscheduled, or
      *         false if it had already been removed
      */
     public static boolean unschedule(Task task) {
         boolean cancelled = task.cancel();
         synchronized (tasks) {
             return tasks.remove(task) || cancelled;
         }
     }
}
