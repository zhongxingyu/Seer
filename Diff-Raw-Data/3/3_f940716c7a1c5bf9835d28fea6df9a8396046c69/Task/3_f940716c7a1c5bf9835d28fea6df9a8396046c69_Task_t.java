 /**
  * 
  */
 package passiveobjects;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import acitiveobjects.Manager;
 import acitiveobjects.Worker;
 
 /**
  * @author lxmonk
  * 
  */
 public interface Task {
 
 	/**
 	 * return the specialization needed by the manager to complete this task.
 	 * 
 	 * @return the relevant ManagerSpecializtion
 	 */
 	public ManagerSpecialization getManagerSpecializtion();
 
 	/**
 	 * return the specialty needed by the worker to complete this task.
 	 * 
 	 * @return relevant WorkerSpecialty
 	 */
 	public WorkerSpecialty getWorkerSpecialty();
 
 	/**
 	 * return the name of the manager who published this task
 	 * 
 	 * @return the manager's name
 	 */
 	public String getManagerName();
 
 	/**
 	 * return the manager who published this task
 	 * 
 	 * @return the manager
 	 */
 	public Manager getManager();
 
 	/**
 	 * returns the size(number of work hours) of this task
 	 * 
 	 * @return the number of work hours of this task
 	 */
 	public int getSize();
 
 	/**
 	 * returns the number of work hours still needed in order to complete this
 	 * task
 	 * 
 	 * @return the number of work hours still needed
 	 */
 	public int getHoursStillNeeded();
 
 	/**
 	 * returns the number of work hours already done on this task
 	 * 
 	 * @return the number of completed work hours
 	 */
 	public int getHoursDone();
 
 	/**
 	 * returns a list of the needed Resources for the task
 	 * 
 	 * @return a list of needed Resources
 	 */
 	public List<Resource> getNeededResources();
 
 	/**
 	 * returns a list of workers who worked on the task
 	 * 
 	 * @return a list of workers
 	 */
 	public List<Worker> getWorkers();
 
 	/**
 	 * returns a list of all workers names who worked on the task
 	 * 
 	 * @return a list of workers names
 	 */
 	public List<String> getAllWorkerNames();
 
 	/**
 	 * Checks if the task is complete or not
 	 * 
 	 * @return a boolean if the task is complete or not
 	 */
 	public boolean isComplete();
 
 	/**
 	 * notify the manager that Task has been completed.
 	 * 
 	 * @param workerName
 	 *            the worker's name (for the Logger)
 	 */
 	public void taskIsDone(String workerName);
 
 	/**
 	 * adds a worker to the list of workers who worked on the task changes the
 	 * hours still needed on the task according to the worker's workHours
 	 * 
 	 * @param worker
 	 *            the new worker working on the task
 	 * @return if the worker will work a full shift return 0. else return the
 	 *         length of his short shift;
 	 */
 	public int signInWorker(Worker worker);
 
 	/**
 	 * update the number of hours done on this task.
 	 * 
 	 * @param hours
 	 *            the number of hours to the hours done on this task
 	 */
 	public void incrementHoursDone(int hours);
 
 	/**
 	 * Decreases the number of hours the task still needs to be completed.
 	 * 
 	 * @param hours
 	 *            the number of hours to be decreased.
 	 */
 	public void decreaseHoursStillNeeded(int hours);
 
 	/**
 	 * Sets the manager who published this task to the working board.
 	 * 
 	 * @param manager
 	 *            the {@link Manager}
 	 */
 	public void setManager(Manager manager);
 
 	/**
 	 * returns the name of this {@link Task}.
 	 * 
 	 * @return the name of this {@link Task}
 	 */
 	public String getName();
 
 	/**
 	 * simulates the work done on this task by a worker
 	 * 
 	 * @param workHours
 	 *            the number of hours the worker will work
 	 * @param workerName
 	 *            the worker's name (for the Logger)
 	 * @throws InterruptedException
 	 *             according to specs.
 	 */
 	public void work(int workHours, String workerName)
 			throws InterruptedException;
 
 	/**
 	 * abort working on this task
 	 */
 	public void abortTask();
 
 	/**
 	 * return if this task is aborted
 	 * 
 	 * @return if this task is aborted
 	 */
 	public boolean isAborted();
 
 	/**
 	 * make the manager wait() until the task is done.
 	 * 
	 * 
 	 */
 	public void monitorCompletion() /*throws InterruptedException*/;
 
 	/**
 	 * returns the name of the project that the Task belong to
 	 * 
 	 * @return the name of the project that the Task belong to
 	 */
 	public String getProjectName();
 
 	/**
 	 * sets the Logger
 	 * 
 	 * @param logger
 	 *            a Logger
 	 */
 	public void setLogger(Logger logger);
 }
