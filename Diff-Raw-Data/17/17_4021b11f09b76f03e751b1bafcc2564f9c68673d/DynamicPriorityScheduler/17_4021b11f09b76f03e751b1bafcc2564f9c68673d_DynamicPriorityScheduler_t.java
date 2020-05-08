 package model.scheduleralgorithms;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import model.Schedule;
 import model.SchedulerAlgorithm;
 import model.Task;
 import model.TaskExecutionTime;
 import model.TaskInstance;
 import model.Utils;
 
 /**
  * An abstract class used to create Dynamic Priority schedulers.
  * 
  * @author Barnabbas
  * @author Thom Castermans
  */
 public abstract class DynamicPriorityScheduler implements SchedulerAlgorithm {
 	
 	/** If the priority of every task should be refreshed every tick, or
 	 *  only when a new job is added to the system.
 	 */
 	private boolean updatePriorityEveryTick = false;
 	/** The set of tasks that is scheduled. */
 	List<Task> tasksToBeScheduled;
 	
 	/**
 	 * Return if the priority of every task is refreshed every tick ({@code true}),
 	 * or only when a new instance is added to the system ({@code false}).
 	 * 
 	 * @return If priority of every task is updated every tick.
 	 */
 	public boolean isPriorityUpdatedEveryTick() {
 		return updatePriorityEveryTick;
 	}
 
 	/**
 	 * Change if the priority is updated every tick (or more, when a task runs
 	 * less than a tick).
 	 * 
 	 * @param updatePriority New value for {@link #isPriorityUpdatedEveryTick()}.
 	 */
 	public void setUpdatePriorityEveryTick(boolean updatePriority) {
 		this.updatePriorityEveryTick = updatePriority;
 	}
 
 	/**
 	 * Assigns a priority to a Task at a given time. A higher priority will
 	 * be scheduled first.
 	 * 
 	 * @param task The Task to assign a priority to.
 	 * @param time The current system time.
 	 * @return A priority for Task {@code task}.
 	 */
 	protected abstract int getPriority(Task task, double time);
 
 	/**
 	 * Create a schedule for the given set of tasks.
 	 * 
 	 * @param tasks The set of tasks to be scheduled.
 	 */
 	public Schedule createSchedule(Set<Task> tasks) {
 		// set tasks
 		this.tasksToBeScheduled = Arrays.asList(tasks.toArray(new Task[] {}));
 		// the cyclus of this task set
 		int lcm = Utils.lcm(tasks);
 
 		// the final schedule
 		List<TaskInstance> schedule = new ArrayList<TaskInstance>();
 
 		// queue, used to get the task with highest priority and schedule it
 		PriorityQueue<TaskExecutionTime> taskQueue = new PriorityQueue<TaskExecutionTime>();
 		for (Task t : tasks) {
 			t.setPriority(getPriority(t, 0));
 			taskQueue.add(new TaskExecutionTime(t));
 		}
 		
 		double sysTime = 0;
 		double newSysTime = 0;
 		TaskExecutionTime te;
 		while (sysTime < lcm) {
 			// Refresh the priority of all tasks in the queue by calling 'getPriority' again.
 			// Only do this when it is switched on, as it is bad for performance.
 			if (updatePriorityEveryTick) {
 				TaskExecutionTime[] tmpTaskQueueArr = taskQueue.toArray(new TaskExecutionTime[] {});
 				taskQueue.retainAll(new ArrayList<TaskExecutionTime>());
 				for (TaskExecutionTime tmpTE : tmpTaskQueueArr) {
 					tmpTE.getTask().setPriority(getPriority(tmpTE.getTask(), sysTime));
 					taskQueue.add(tmpTE);
 				}
 			}
 				
 			// If the queue is empty, skip to the time when a task becomes available
 			// and add that task to the queue
 			if (taskQueue.isEmpty()) {
 				double minStartTime = Double.MAX_VALUE;
 				Task minStartTask = null;
 				for (Task t : tasks) {
 					double nextExecution = sysTime - (sysTime % t.getPeriod()) + t.getPeriod();
 					if (nextExecution < minStartTime && nextExecution < lcm) {
 						minStartTime = nextExecution;
 						minStartTask = t;
 					}
 				}
 				
 				// No task available until the end? Quit then.
 				if (minStartTask == null)  break;
 				
 				// Add other tasks to the queue that start at that start time
 				// If we do not do this, tasks will not be added
 				for (Task t : tasks) {
 					if (t.equals(minStartTask))  continue; // skip task we are going to add later
 					double nextExecution = sysTime - (sysTime % t.getPeriod()) + t.getPeriod();
 					if (nextExecution == minStartTime) {
 						t.setPriority(getPriority(t, newSysTime));
 						taskQueue.add(new TaskExecutionTime(t));
 					}
 				}
 				
 				// Skip to task, jaj.
 				sysTime = minStartTime;
 				minStartTask.setPriority(getPriority(minStartTask, newSysTime));
 				taskQueue.add(new TaskExecutionTime(minStartTask));
 			}
 			
 			// Get a task from the queue, let it execute
 			te = taskQueue.peek();
 			newSysTime = sysTime + te.execute(getMaxExecutionTimeAt(te.getTask(), sysTime));
 			schedule.add(new TaskInstance(te.getTask(), sysTime, newSysTime));
 			// Remove the task from the queue if it is done with its execution
			if (te.getExecutionTimeLeft() < TaskExecutionTime.DELTA) {
 				taskQueue.poll();
 			}
 			// If the deadline is passed after execution of the task,
 			// we have a deadline miss and thus return the schedule so
 			// far, that is not feasible.
 			for (TaskExecutionTime tmpTE : taskQueue) {
 				if (tmpTE.getTask().getAbsoluteDeadline(sysTime) < newSysTime) {
 					return new Schedule(schedule, tmpTE.getTask());
 				}
 			}
 			// If a task becomes available while executing this task,
 			// add the new task(s) to the queue
 			for (Task t : tasks) {
 				/* We check if a new period lies within the interval (sysTime, newSysTime].
 				 * We do this by looking at the old and new systime modulo the task period.
 				 * 
 				 * Now, if the old system time is before a "start of a period" of the task
 				 * and the new system time is after that start of a  period, then after this
 				 * calculation the result of the old system time will suddenly be greater
 				 * than the result of the new system time.
 				 */
 				if (sysTime % t.getPeriod() > newSysTime % t.getPeriod() && newSysTime < lcm) {
 					// If the task is still in the queue, we have a deadline miss!
 					for(TaskExecutionTime tmpTE : taskQueue) {
 						if (tmpTE.getTask().equals(t)) {
 							return new Schedule(schedule, t);
 						}
 					}
 					
 					t.setPriority(getPriority(t, newSysTime));
 					taskQueue.add(new TaskExecutionTime(t));
 				}
 			}
 			sysTime = newSysTime;
 		}
 
 		// we are not scheduling anymore
 		this.tasksToBeScheduled = null;
 
 		// if there is still a task to be scheduled, we have a deadline miss per definition
 		if (!taskQueue.isEmpty()) {
 			return new Schedule(schedule, taskQueue.peek().getTask());
 		}
 		
 		return new Schedule(schedule);
 	}
 
 	/**
 	 * Given the time and tasks that are being scheduled now in
 	 * {@code tasksToBeScheduled}, give the time you can let a
 	 * task execute without missing a task release of other tasks.
 	 * 
 	 * @param runsCurrently Currently running task.
 	 * @param sysTime Current system time.
 	 * @return See description.
 	 */
 	private double getMaxExecutionTimeAt(Task runsCurrently, double sysTime) {
 		double pos1 = 1;
 		double pos2 = Double.MAX_VALUE;
 		for (Task task : tasksToBeScheduled) {
 			double nextReleaseTime = (Math.floor(sysTime / task.getPeriod()) + 1) * task.getPeriod();
 			if (nextReleaseTime - sysTime < pos2)  pos2 = nextReleaseTime - sysTime;
 		}
 		return Math.min(pos1, pos2);
 	}
 }
