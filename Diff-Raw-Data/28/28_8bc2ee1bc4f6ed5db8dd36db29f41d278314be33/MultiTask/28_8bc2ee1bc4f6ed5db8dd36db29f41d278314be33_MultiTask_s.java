 /*******************************************************************************
 * 2012 Ivan Shubin http://mindengine.net
 * 
 * This file is part of MindEngine.net Oculus Grid.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Oculus Grid.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
 package net.mindengine.oculus.grid.domain.task;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Composition of simple tasks used for tasks allocation between agents
  * 
  * @author Ivan Shubin
  * 
  */
 public class MultiTask extends Task {
     private static final long serialVersionUID = 2659186513531107457L;
     private List<Task> tasks = new ArrayList<Task>();
 
     public void setTasks(List<Task> tasks) {
         this.tasks = tasks;
     }
 
     public List<Task> getTasks() {
         return tasks;
     }
 
     @Override
     public void initTask() {
 
         if (tasks != null) {
             for (Task task : tasks) {
                 task.initTask();
             }
         }
     }
 
     /**
      * Checks for statuses of the children tasks and updates its own status. If
      * there is at least one task running and all other tasks are in waiting
      * state the complete status of the MultiTask will be "ACTIVE". Only if all
      * the children tasks were completed the complete status will be COMPLETED
      * as well. Also in this method will be calculated the average amount of
      * task completion in percents
      */
     @Override
     public TaskStatus updateTaskStatus() {
         boolean hasWaiting = false;
         boolean hasActive = false;
         boolean hasCompleted = false;
 
         float percent = 0.0f;
 
         for (Task task : tasks) {
             TaskStatus taskStatus = task.updateTaskStatus();
             percent += taskStatus.getPercent();

            if (TaskStatus.WAITING.equals(task.getTaskStatus().getStatus())) {
                 hasWaiting = true;
            } else if (TaskStatus.ACTIVE.equals(task.getTaskStatus().getStatus())) {
                 hasActive = true;
            } else if (TaskStatus.COMPLETED.equals(task.getTaskStatus().getStatus())) {
                 hasCompleted = true;
             }
         }
         if (tasks.size() > 0) {
             percent = percent / tasks.size();
         }
         getTaskStatus().setPercent(percent);
 
         if (hasCompleted && !hasActive && !hasWaiting) {
             getTaskStatus().setStatus(TaskStatus.COMPLETED);
         } else if (hasActive) {
             getTaskStatus().setStatus(TaskStatus.ACTIVE);
         } else
             getTaskStatus().setStatus(TaskStatus.WAITING);
 
         return getTaskStatus();
     }
 
     @Override
     public String type() {
         return Task.TYPE_MULTITASK;
     }
 
 }
