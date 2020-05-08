 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009  Blinz <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.util.concurrency;
 
 import java.util.Vector;
 
 /**
  * A Task that contains other Tasks for execution.
  * @author Blinz
  */
 public final class TaskList extends Task {
 
     private boolean tasksManaged = false;
     private final Vector<Task> tasksToAdd = new Vector<Task>();
     private final Vector<Task> tasksToRemove = new Vector<Task>();
     private final Vector<Task> tasks = new Vector<Task>();
 
     /**
      * Adds the given Task to this TaskExecuter to be executed in the future.
      * @param task
      */
     public final void add(Task task) {
         tasksToAdd.add(task);
     }
 
     /**
      * Removes the given Task and it will no longer be executed in the future.
      * @param task
      */
     public final void remove(Task task) {
         tasksToRemove.add(task);
     }
 
     @Override
     protected void run() {
         tasksManaged = false;
         for (int i = 0; i < tasks.size(); i++) {
             if (!tasks.get(i).moveOn()) {
                tasks.get(i).moveOn();
             }
         }
     }
 
     @Override
     void prepare() {
         if (tasksManaged()) {
             super.prepare();
             manageTasks();
         }
 
         for (int i = 0; i < tasks.size(); i++) {
             if (!tasks.get(i).prepared()) {
                 tasks.get(i).prepare();
             }
         }
     }
 
     private synchronized boolean tasksManaged() {
         if (tasksManaged) {
             return true;
         } else {
             tasksManaged = true;
             return false;
         }
     }
 
     private synchronized void manageTasks() {
         for (int i = tasksToRemove.size() - 1; i > -1; i--) {
             tasksToRemove.get(i).drop();
             tasks.remove(tasksToRemove.remove(i));
         }
 
         for (int i = tasksToAdd.size() - 1; i > -1; i--) {
             tasksToAdd.get(i).init(taskProcessor);
             tasks.add(tasksToAdd.remove(i));
         }
     }
 }
