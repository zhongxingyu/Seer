 /**
  *  This file is part of DORS API.
  * 
  *  DORS API - Java is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  DORS API - Java is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with  DORS API - Java.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.dors.task;
 
 import com.dors.ThreadMaster;
 import com.dors.util.ConcurrentJobList;
 import com.dors.util.JobList;
 import com.dors.util.JobQueue;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Queue;
 import java.util.zip.DataFormatException;
 
 /**
  *
  * @author Faris
  */
 public class TaskController {
     
     // Lists  containing the tasks for all purposes
     private static List<Task> incomingTasks = new ConcurrentJobList<>();
     private static List<Task> idleTasks = new JobList<>();
     private static Queue<Task> executoryCycle = new JobQueue<>();
     
     /**
      * Submits a task into the incoming task queue
      * @param task
      * @throws Exception 
      */
     public static void addTask(Task task) throws Exception{
         if(task.getDate().before(new Date(System.currentTimeMillis()))){
             throw new DataFormatException();
         }
         incomingTasks.add(task);
     }
     
     /**
      * Loops through the incoming tasks and puts them into the 
      * executory cycle or the idle task list
      */
     public static void checkNewTasks(){
         if(incomingTasks.isEmpty()) {
             return;
         }
         Iterator<Task> it = incomingTasks.iterator();
         while(it.hasNext()){
             Task task = it.next();
             if(task.getDate().after(new Date(System.currentTimeMillis()))){
                 executoryCycle.add(task);
             } else {
                 idleTasks.add(task);
             }
             it.remove();
         }
     }
     
     /**
      * Loops through the idle task list and checks if they are
      * due to be inputted into the executoryCycle or delayed
      */
     public static void sortTasks() {
         Iterator<Task> it = idleTasks.iterator();
         while(it.hasNext()){
             Task task = it.next();
             if(task.getDate().after(new Date(System.currentTimeMillis()))){
                 executoryCycle.add(task);
                 it.remove();
             }
         }
         executeTasks();
     }
     
     /**
      * Executes all of the scheduled tasks to be executed in
      * the executoryCycle list
      */
     public static void executeTasks(){
         Iterator<Task> it = executoryCycle.iterator();
         while(it.hasNext()){
             Task task = it.next();
             ThreadMaster.getInstance().assignTask(task);
             it.remove();
         }
     }
 
 }
