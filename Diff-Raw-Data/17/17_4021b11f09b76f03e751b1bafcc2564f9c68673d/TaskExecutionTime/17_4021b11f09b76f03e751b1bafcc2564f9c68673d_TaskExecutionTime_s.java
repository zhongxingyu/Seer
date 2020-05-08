 package model;
 
 /**
  * This class holds a pair of a task and how much time
  * that task still needs to run on the CPU. This is useful
  * in scheduling.
  * 
  * @author Thom Castermans
  */
 public class TaskExecutionTime implements Comparable<TaskExecutionTime> {
 
 	/** Task of which the execution time is being kept track of. */
 	private Task t;
 	/** Execution time left for given task. */
 	private double e;
 	
 	/**
 	 * Construct a new pair of Task and execution time left,
 	 * where the execution time left is initialized to the
 	 * execution time of the task.
 	 * 
 	 * @param task Task to be paired with execution time.
 	 */
 	public TaskExecutionTime(Task task) {
 		this.t = task;
 		this.e = task.getExecutionTime();
 	}
 	
 	/**
 	 * Return how much time the task in this object still
 	 * needs to execute.
 	 * 
 	 * @return How much time the task still needs to execute.
 	 */
 	public double getExecutionTimeLeft() {
 		return e;
 	}
 	
 	/**
 	 * Return the task in this object, of which the execution
 	 * time is being kept track of.
 	 * 
 	 * @return Task in this object.
 	 */
 	public Task getTask() {
 		return t;
 	}
 	
 	/**
 	 * Simulate a run of the task in this object for given
 	 * amount of time. The execution time this task still
 	 * needs is adjusted, with a minimum of zero.
 	 * 
 	 * @param executionTime Time the task runs.
 	 * @return The time the task actually has executed. This
 	 *         may be less than asked, when the task is done.
 	 */
 	public double execute(double executionTime) {
 		this.e -= executionTime;
 		double executed = executionTime;
 		if (this.e < 0) {
 			executed = executed + this.e;
 			this.e = 0;
 		}
 		return executed;
 	}
 
 	@Override
 	public int compareTo(TaskExecutionTime that) {
 		return this.t.compareTo(that.t);
 	}
 }
