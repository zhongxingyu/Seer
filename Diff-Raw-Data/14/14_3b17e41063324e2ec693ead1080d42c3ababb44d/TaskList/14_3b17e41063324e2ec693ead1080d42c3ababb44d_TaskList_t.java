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
      * Gets the number of Tasks managed by this TaskList.
      * @return the number of Tasks managed by this TaskList
      */
     public final int size() {
         return tasks.size() + tasksToAdd.size();
     }
 
     /**
      * Adds the given Task to this TaskExecuter to be executed in the future.
      * @param task the Task to add to this TaskList
      */
     public final void add(final Task task) {
         tasksToAdd.add(task);
     }
 
     /**
      * Gets the Task at the given place in the order of execution.
      * @param index the location of the desired Task in the order of execution
      * @return the Task at the given place in the order of execution
      */
     public final Task get(final int index) {
         try {
             return tasks.get(index);
         } catch (final ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
 
     /**
      * Removes and gets the Task at the given place in the order of execution.
      * @param index the location of the desired Task in the order of execution
      * @return the Task at the given place in the order of execution
      */
     public final Task remove(final int index) {
         try {
             final Task t = tasks.get(index);
             tasksToRemove.add(t);
             return t;
         } catch (final ArrayIndexOutOfBoundsException e) {
             return null;
         }
     }
 
     /**
      * Removes the given Task and it will no longer be executed in the future.
      * @param task the Task to be removed
      */
     public final void remove(final Task task) {
         tasksToRemove.add(task);
     }
 
     /**
      * Clears this TaskLists of all Tasks.
      */
     public final void clear() {
         tasksToRemove.addAll(tasks);
         tasksToRemove.addAll(tasksToAdd);
     }
 
     @Override
     protected void run() {
         tasksManaged = false;
         for (int i = 0; i < tasks.size(); i++) {
             if (!tasks.get(i).moveOn()) {
                 tasks.get(i).enter();
             }
         }
     }
 
     @Override
     final void prepare() {
         super.prepare();
         if (!tasksManaged()) {
             manageTasks();
         }
 
         for (int i = 0; i < tasks.size(); i++) {
             if (!tasks.get(i).prepared()) {
                 tasks.get(i).prepare();
             }
         }
     }
 
     private final synchronized boolean tasksManaged() {
         if (tasksManaged) {
             return true;
         } else {
             tasksManaged = true;
             return false;
         }
     }
 
     private final void manageTasks() {
        for (int i = tasksToRemove.size() - 1; i > -1; i--) {
            final Task t = tasksToRemove.remove(i);
            t.drop();
             if (!tasks.remove(t)) {
                 tasksToAdd.remove(t);
             }
         }
 
        while (!tasksToAdd.isEmpty()) {
            final Task t = tasksToAdd.remove(0);
            t.init(taskProcessor);
            tasks.add(t);
         }
     }
 }
