 /*******************************************************************************
  * Copyright (c) 2004 - 2005 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Dec 22, 2004
   */
 package org.eclipse.mylar.tasks.internal;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.mylar.tasks.AbstractCategory;
 import org.eclipse.mylar.tasks.ITask;
 
  
 /**
  * @author Mik Kersten
  */
 public class TaskList implements Serializable {
 
     private static final long serialVersionUID = 3618984485791021105L;
 
     private List<ITask> rootTasks = new ArrayList<ITask>();
     private List<AbstractCategory> categories = new ArrayList<AbstractCategory>();
     private transient List<ITask> activeTasks = new ArrayList<ITask>();
     
     public void addRootTask(ITask task) {
     		rootTasks.add(task);
     }
     
     public void addCategory(AbstractCategory cat) {
     	categories.add(cat);
     }
     
     public void setActive(ITask task, boolean active) {
         task.setActive(active);
         if (active) {
             activeTasks.add(task);
         } else {
             activeTasks.remove(task);
         }
     }
     
     public void deleteTask(ITask task) {
     	boolean deleted = deleteTaskHelper(rootTasks, task);
     	if (!deleted) {
 			for (TaskCategory cat : getTaskCategories()) {
 				deleted = deleteTaskHelper(cat.getChildren(), task);
 				if (deleted) {
 					return;
 				}
 			}
     	}
 	}
     
     private boolean deleteTaskHelper(List<ITask> tasks, ITask t) {
     	for (ITask task : tasks) {
     		if (task.getHandle().equals(t.getHandle())) {
     			tasks.remove(task);
     			return true;
     		} else {
    				if (deleteTaskHelper(task.getChildren(), t))
    					return true;
     		}
 		}
     	return false;
 	}
     
     public void deleteCategory(AbstractCategory category) {
     	categories.remove(category);
     }
     /**
      * TODO: make data structure handle this traversal
      */
     public ITask getTaskForId(String id) {
     	ITask t = null;
     	for (TaskCategory cat : getTaskCategories()) {
 			if ((t = findTaskHelper(cat.getChildren(), id)) != null) {
 				return t;
 			}
 		}
         return findTaskHelper(rootTasks, id);
     } 
     
     private ITask findTaskHelper(List<ITask> tasks, String id) {
         for (ITask task : tasks) {
             if (task.getHandle() == id) {
                 return task;
             }
             ITask t = findTaskHelper(task.getChildren(), id);
             if (t != null) {
             	return t;
             }
         }
         return null;
     }
     
     public List<ITask> getActiveTasks() {
         return activeTasks;
     }
     
     public List<ITask> getRootTasks() {
         return rootTasks;
     }
     
     public List<AbstractCategory> getCategories() {
     	return categories;
     }
        
     public int findLargestTaskHandle() {
     	int max = 0;
     	max = Math.max(largestTaskHandleHelper(rootTasks), max);
     	for (TaskCategory cat : getTaskCategories()) {
         	max = Math.max(largestTaskHandleHelper(cat.getChildren()), max);
     	}
     	return max;
     }
     
     private int largestTaskHandleHelper(List<ITask> tasks) {
     	int ihandle = 0;
     	int max = 0;
     	for (ITask t : tasks) {
    		if(t.participatesInTaskHandles()) {
    			String string = t.getHandle().substring(t.getHandle().indexOf('-')+1, t.getHandle().length());
    			if (string != "") {
    				ihandle = Integer.parseInt(string);
    			}
     		}
     		max = Math.max(ihandle, max);
     		ihandle = largestTaskHandleHelper(t.getChildren());
     		max = Math.max(ihandle, max);
     	}
     	return max;
     }
     
     public List<Object> getRoots() {
     	List<Object> roots = new ArrayList<Object>();
     	for (ITask t : rootTasks) {
     		roots.add(t);
     	}
     	for (AbstractCategory cat : categories) {
     		roots.add(cat);
     	}
     	return roots;
     }
     
     public void createCategory(String description) {
     	TaskCategory c = new TaskCategory(description);
     	categories.add(c);
     }
     
     public List<TaskCategory> getTaskCategories() {
     	List<TaskCategory> cats = new ArrayList<TaskCategory>();
     	for (AbstractCategory cat : categories) {
     		if (cat instanceof TaskCategory) {
     			cats.add((TaskCategory)cat);
     		}
     	}
     	return cats;
     }
 
 	public void clear() {
 		activeTasks.clear();
 		categories.clear();
 		rootTasks.clear();
 	}
 }
