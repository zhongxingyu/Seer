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
 package net.mindengine.oculus.grid.server;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.locks.ReentrantLock;
 
 import net.mindengine.oculus.grid.domain.task.DefaultTask;
 import net.mindengine.oculus.grid.domain.task.MultiTask;
 import net.mindengine.oculus.grid.domain.task.Task;
 import net.mindengine.oculus.grid.domain.task.TaskStatus;
 
 /**
  * Used for working with all types of tasks which are in different states
  * (awaiting, active, completed)
  * 
  * @author Ivan Shubin
  * 
  */
 public class TaskContainer {
 
 	/**
 	 * Collection of tasks which were not picked up by the TRMClient.<br>
 	 * As soon as TRMClient picks up this task it will be deleted from list
 	 */
 	private Map<Long, TaskWrapper> completedTasks = new HashMap<Long, TaskWrapper>();
 
 	/**
 	 * Collection of all tasks which are in awaiting state (not assigned to any
 	 * agent)<br>
 	 * Tasks will be fetched by the priority as soon as any of agents will
 	 * become available
 	 */
 	private Queue<TaskWrapper> queuedTasks = new LinkedBlockingQueue<TaskWrapper>();
 
 	/**
 	 * Tasks that were assigned to agent
 	 */
 	private Map<Long, TaskWrapper> assignedTasks = new HashMap<Long, TaskWrapper>();
 
 	/**
 	 * All tasks for all kind of states
 	 */
 	private Map<Long, TaskWrapper> tasks = new HashMap<Long, TaskWrapper>();
 
 	private ReentrantLock taskLock = new ReentrantLock();
 
 	public TaskWrapper getTask(Long taskId) {
 		return tasks.get(taskId);
 	}
 
 	/**
 	 * Moves task to completed queue. In case if the task is a child of some
 	 * {@link DefaultTask} and is the last active task in it its parent will be
 	 * also moved to completed list
 	 * 
 	 * @param taskWrapper
 	 */
 	public void moveTaskToCompleted(TaskWrapper taskWrapper) {
 		taskLock.lock();
 		try {
 			moveTaskToCompletedWithoutLock(taskWrapper);
 		}
 		catch (Throwable e) {
 			throw new RuntimeException(e);
 		}
 		finally {
 			taskLock.unlock();
 		}
 	}
 
 	/**
 	 * This method is used only from {@link #moveTaskToCompleted(TaskWrapper)}
 	 * method. This trick was implemented as there could occur recursion, when
 	 * updating parent tasks status
 	 * 
 	 * @param taskWrapper
 	 */
 	private void moveTaskToCompletedWithoutLock(TaskWrapper taskWrapper) {
 	    if(queuedTasks.contains(taskWrapper)) {
 	        queuedTasks.remove(taskWrapper);
 	    }
 		removeTaskFromTemp(taskWrapper);
 		completedTasks.put(taskWrapper.getId(), taskWrapper);
 		taskWrapper.setState(TaskWrapper.COMPLETED);
 		Task task = taskWrapper.getTask();
 		task.getTaskStatus().setStatus(TaskStatus.COMPLETED);
 		taskWrapper.setAssignedAgent(null);
 		taskWrapper.getTask().setCompletedDate(new Date());
 		
 		if (taskWrapper.getParent() != null) {
 			updateMultiTaskStatus(taskWrapper.getParent());
 		}
 	}
 	
 	public void moveTaskToErrorTask(TaskWrapper taskWrapper, String message) {
         removeTaskFromTemp(taskWrapper);
         completedTasks.put(taskWrapper.getId(), taskWrapper);
         taskWrapper.setState(TaskWrapper.COMPLETED);
         Task task = taskWrapper.getTask();
         task.getTaskStatus().setStatus(TaskStatus.ERROR);
         task.getTaskStatus().setMessage(message);
         taskWrapper.setAssignedAgent(null);
         taskWrapper.getTask().setCompletedDate(new Date());
         
         if (taskWrapper.getParent() != null) {
             updateMultiTaskStatus(taskWrapper.getParent());
         }
     }
 
 	/**
 	 * Updates the multi tasks status and moves it to completed tasks list if
 	 * all child tasks are completed
 	 * 
 	 * @param multiTask
 	 */
 	private void updateMultiTaskStatus(TaskWrapper multiTaskWrapper) {
 	    MultiTask multiTask = (MultiTask) multiTaskWrapper.getTask();
 	    
 	    boolean bAllCompleted = true;
 		for (Task task : multiTask.getTasks()) {
 			if (!(task.getTaskStatus().getStatus().equals(TaskStatus.COMPLETED) || task.getTaskStatus().getStatus().equals(TaskStatus.ERROR))) {
 				bAllCompleted = false;
 			}
 		}
 		if (bAllCompleted) {
 			moveTaskToCompletedWithoutLock(multiTaskWrapper);
 		}
 	}
 
 	public boolean hasQueuedTasks() {
 		return !queuedTasks.isEmpty();
 	}
 
 	public TaskWrapper pickQueuedTask() {
 		return queuedTasks.peek();
 	}
 
 	/**
 	 * Registers the task in container and puts it to the tasks queue if it is
 	 * not a {@link DefaultTask}. In case if is {@link DefaultTask} - all the child
 	 * tasks will be also registered and put to tasks queue
 	 * 
 	 * @param taskWrapper
 	 *            Task wrapper with task from client in it
 	 * @return ID number of the task
 	 */
 	public Long registerNewTask(TaskWrapper taskWrapper) {
 		taskLock.lock();
 		Long taskId;
 		try {
 			taskId = generateNewTaskId();
 			taskWrapper.getTask().setId(taskId);
 			taskWrapper.setState(TaskWrapper.QUEUED);
 			taskWrapper.getTask().getTaskStatus().setStatus(TaskStatus.WAITING);
 			tasks.put(taskId, taskWrapper);
 			
 		}
 		catch (Throwable e) {
 			taskLock.unlock();
 			throw new RuntimeException(e);
 		}
 
 		if (taskWrapper.isMultiTask()) {
 			taskLock.unlock();
 			taskWrapper.setChildren(new ArrayList<TaskWrapper>());
 
 			MultiTask multiTask = (MultiTask) taskWrapper.getTask();
 			for (Task childTask : multiTask.getTasks()) {
 				TaskWrapper childTaskWrapper = new TaskWrapper();
 				childTaskWrapper.setParent(taskWrapper);
 				childTaskWrapper.setTask(childTask);
 				registerNewTask(childTaskWrapper);
 				taskWrapper.getChildren().add(childTaskWrapper);
 			}
 			return taskId;
 		}
 		else {
 			/*
 			 * As this task is not a MultiTask putting it to the tasks queue
 			 */
 			queuedTasks.add(taskWrapper);
 			taskLock.unlock();
 		}
 		return taskId;
 	}
 
 	/**
 	 * Removes task from its temporary containers (assignedTasks, queuedTasks,
 	 * completedTasks)
 	 * 
 	 * @param task
 	 */
 	private void removeTaskFromTemp(TaskWrapper task) {
 		if (task.getState().equals(TaskWrapper.ASSIGNED)) {
 			assignedTasks.remove(task.getId());
 		}
 		else if (task.getState().equals(TaskWrapper.COMPLETED)) {
 			completedTasks.remove(task.getId());
 		}
 	}
 
 	private Long uniqueTaskId = 0L;
 
 	/**
 	 * Generating the task ID number and verifying if such id already exists in
 	 * tasks list
 	 * 
 	 * @return The unique task ID number
 	 */
 	private Long generateNewTaskId() {
 		uniqueTaskId++;
 		if (tasks.containsKey(uniqueTaskId)) {
 			return generateNewTaskId();
 		}
 		return uniqueTaskId;
 	}
 
 	public void removeCompletedTask(Long id) {
 		taskLock.lock();
 		removeCompletedTaskWithoutLock(id);
 		taskLock.unlock();
 	}
 
 	public void removeCompletedTaskWithoutLock(Long id) {
 		if (completedTasks.containsKey(id)) {
 			TaskWrapper taskWrapper = tasks.get(id);
 			if (taskWrapper.getTask() instanceof MultiTask) {
 			    MultiTask multiTask = (MultiTask) taskWrapper.getTask();
 				for (Task task : multiTask.getTasks()) {
 					removeCompletedTaskWithoutLock(task.getId());
 				}
 			}
 
 			tasks.remove(id);
 			completedTasks.remove(id);
 		}
 	}
 
 	/**
 	 * Prints to console all tasks. Used for debugging in console only
 	 */
 	protected void printTasks() {
 		taskLock.lock();
 		System.out.println("=========================");
 		System.out.println(tasks.size());
 		System.out.println("=========================");
 		for (Map.Entry<Long, TaskWrapper> task : tasks.entrySet()) {
 			System.out.println(task.getValue());
 		}
 		taskLock.unlock();
 	}
 
 	protected ReentrantLock getTaskLock() {
 		return taskLock;
 	}
 
 	protected Map<Long, TaskWrapper> getCompletedTasks() {
 		return completedTasks;
 	}
 
 	protected void setCompletedTasks(Map<Long, TaskWrapper> completedTasks) {
 		this.completedTasks = completedTasks;
 	}
 
 	protected Queue<TaskWrapper> getQueuedTasks() {
 		return queuedTasks;
 	}
 
 	protected void setQueuedTasks(Queue<TaskWrapper> queuedTasks) {
 		this.queuedTasks = queuedTasks;
 	}
 
 	protected Map<Long, TaskWrapper> getAssignedTasks() {
 		return assignedTasks;
 	}
 
 	protected void setAssignedTasks(Map<Long, TaskWrapper> assignedTasks) {
 		this.assignedTasks = assignedTasks;
 	}
 
 	protected Map<Long, TaskWrapper> getTasks() {
 		return tasks;
 	}
 }
