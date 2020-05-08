 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * physical plan
  * 
  * @author xeraph
  * 
  */
 public abstract class QueryTask implements Runnable {
 	private final Logger logger = LoggerFactory.getLogger(QueryTask.class);
 
 	public enum TaskStatus {
 		INIT, RUNNING, FINALIZING, COMPLETED, CANCELED
 	}
 
 	private TaskStatus status = TaskStatus.INIT;
 	private Throwable failure;
 	private QueryTask parentTask;
 	private CopyOnWriteArraySet<QueryTask> subTasks = new CopyOnWriteArraySet<QueryTask>();
 	private CopyOnWriteArraySet<QueryTask> dependencies = new CopyOnWriteArraySet<QueryTask>();
 	private CopyOnWriteArraySet<QueryTaskListener> listeners = new CopyOnWriteArraySet<QueryTaskListener>();
 

 	public TaskStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(TaskStatus status) {
 		this.status = status;
 	}
 
 	public Throwable getFailure() {
 		return failure;
 	}
 
 	public void setFailure(Throwable failure) {
 		this.failure = failure;
 	}
 
 	public Collection<QueryTask> getSubTasks() {
 		return new ArrayList<QueryTask>(subTasks);
 	}
 
 	public QueryTask getParentTask() {
 		return parentTask;
 	}
 
 	public void setParentTask(QueryTask parentTask) {
 		this.parentTask = parentTask;
 	}
 
 	public void addSubTask(QueryTask task) {
 		if (task == null)
 			throw new IllegalArgumentException("null task is not allowed");
 		task.setParentTask(this);
 		subTasks.add(task);
 	}
 
 	public void removeSubTask(QueryTask task) {
 		if (task == null)
 			throw new IllegalArgumentException("null task is not allowed");
 		subTasks.remove(task);
 	}
 
 	public boolean isRunnable() {
 		for (QueryTask t : dependencies)
			if (t.getStatus() != TaskStatus.COMPLETED)
 				return false;
 
 		return status == TaskStatus.INIT;
 	}
 
 	// invoked when task is started
 	public void onStart() {
 	}
 
 	// like finally block, regardless of normal complete or cancel
 	public void onCleanUp() {
 	}
 
 	public Collection<QueryTask> getDependencies() {
 		return dependencies;
 	}
 
 	public void addDependency(QueryTask task) {
 		checkNotNull(task, "task");
 
 		if (logger.isDebugEnabled())
 			logger.debug("araqne logdb: [{}] depends on [{}] task", this, task);
 
 		dependencies.add(task);
 	}
 
 	public void removeDependency(QueryTask task) {
 		checkNotNull(task, "task");
 		dependencies.remove(task);
 	}
 
 	public Collection<QueryTaskListener> getListeners() {
 		return listeners;
 	}
 
 	public void addListener(QueryTaskListener listener) {
 		checkNotNull(listener, "listener");
 		listeners.add(listener);
 	}
 
 	public void removeListener(QueryTaskListener listener) {
 		checkNotNull(listener, "listener");
 		listeners.remove(listener);
 	}
 
 	private void checkNotNull(Object o, String name) {
 		if (o == null)
 			throw new IllegalArgumentException("null " + name + " is not allowed");
 	}
 }
