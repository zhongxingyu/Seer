 /**
  * 
  */
 package acitiveobjects;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import main.Init;
 
 import passiveobjects.Helpers;
 import passiveobjects.Resource;
 import passiveobjects.Task;
 import passiveobjects.Warehouse;
 import passiveobjects.WorkerSpecialty;
 import passiveobjects.WorkingBoard;
 
 /**
  * @author lxmonk
  * 
  */
 public class Worker implements Runnable {
 
 	final String name;
 	final int workHours;
 	final List<WorkerSpecialty> specialtyList;
 	Task currentTask;
 	Logger logger;
 	WorkingBoard workingBoard;
 	Warehouse warehouse;
 	boolean gotResources;
 
 	/**
 	 * The worker's constructor
 	 * 
 	 * @param aName
 	 *            worker's name
 	 * @param theWorkHours
 	 *            worker's work hours
 	 * @param list
 	 *            A list of {@link WorkerSpecialty}
 	 * @param theWorkingBoard
 	 *            the {@link WorkingBoard}
 	 * @param theWarehouse
 	 *            the {@link Warehouse}
 	 */
 	public Worker(String aName, int theWorkHours, List<WorkerSpecialty> list,
 			WorkingBoard theWorkingBoard, Warehouse theWarehouse) {
 		this.name = aName;
 		this.workHours = theWorkHours;
 		this.specialtyList = list;
 		this.workingBoard = theWorkingBoard;
 		this.warehouse = theWarehouse;
 		this.currentTask = null;
 		this.gotResources = false;
 	}
 
 	/**
 	 * sets the {@link Logger}
 	 * 
 	 * @param aLogger
 	 *            the Logger
 	 */
 	public void setLogger(Logger aLogger) {
 		this.logger = aLogger;
 	}
 
 	/**
 	 * returns the Worker's name
 	 * 
 	 * @return Worker's name
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * returns the worker's workHours
 	 * 
 	 * @return the worker's workHours
 	 */
 	public int getWorkHours() {
 		return this.workHours;
 	}
 
 	/**
 	 * returns the worker's currents {@link Task}
 	 * 
 	 * @return the worker's currents {@link Task}
 	 */
 	public Task getCurrentTask() {
 		return this.currentTask;
 	}
 
 	/**
 	 * returns a String list of all the resources the worker got
 	 * 
 	 * @return a list of resources names
 	 */
 	public List<String> getWorkerResources() {
 		if ((!this.gotResources) || (this.currentTask == null))
 			return null;
 		else {
 			List<String> resourceList = new ArrayList<String>();
 			for (Resource resource : this.currentTask.getNeededResources())
 				resourceList.add(resource.getName());
 			return resourceList;
 		}
 	}
 
 	/**
 	 * makes the worker wait the extra time, if the task is complete before is
 	 * workHours are complete
 	 * 
 	 * @param time
 	 *            the number of hours he needs to wait
 	 */
	public synchronized void workForNothing(int time)
	throws InterruptedException {
 		this.wait(time * Init.SECOND);
 	}
 
 	@Override
 	public void run() {
 		try {
 			while (!Thread.interrupted()) {
 				this.currentTask = null;
 				this.gotResources = false;
 				Task temp = null;
 				for (WorkerSpecialty workerSpecialty : this.specialtyList) {
 					// searching for a task
 					temp = this.workingBoard.getTaskBySpecialty(workerSpecialty);
 					if ((temp != null) && (temp.getHoursStillNeeded() > 0)
 							&& (!temp.isComplete()) && (!temp.isAborted())) {
 						this.currentTask = temp;
 						break;
 						// we found a task , and it still needs hours, and it's not
 						// completed or aborted
 					}
 					if (Thread.interrupted()) throw new InterruptedException(); 
 				}
 				if (this.currentTask != null) { // found a qualifies task to work on
 					this.gotResources = this.warehouse.getResources(
 							this.currentTask, this.name);
 					// if resources are not available, the worker waits
 					if (Thread.interrupted()) throw new InterruptedException();
 					while ((this.gotResources) 
 							&& (this.currentTask.getHoursStillNeeded() > 0)
 							&& (!this.currentTask.isComplete())
 							&& (!this.currentTask.isAborted())) {
 						int shortShift = this.currentTask.signInWorker(this);
 						if (Thread.interrupted()) throw new InterruptedException();
 						if (shortShift == 0) {
 							this.logger.info(this.name + " took task "
 									+ this.currentTask.getName() + " of project "
 									+ this.currentTask.getProjectName() + " for "
 									+ this.workHours + " hours at "
 									+ Helpers.staticTimeNow());
 							this.currentTask.work(this.workHours, this.name);
 						} else {
 							this.logger.info(this.name + " took task "
 									+ this.currentTask.getName() + " of project "
 									+ this.currentTask.getProjectName() + " for "
 									+ shortShift + " hours at "
 									+ Helpers.staticTimeNow());
 							this.currentTask.work(shortShift, this.name);
 							if (Thread.interrupted()) throw new InterruptedException();
 							this.workForNothing(this.workHours - shortShift);
 						}
 					}
 					if (this.currentTask.isAborted()) {
 						this.logger.info(this.name + " stops working on task "
 								+ this.currentTask.getName() + " of project "
 								+ this.currentTask.getProjectName() + " at "
 								+ Helpers.staticTimeNow());
 					}
 					if (Thread.interrupted()) throw new InterruptedException();
 					if (this.gotResources) {
 						this.warehouse.returnResources(this.currentTask
 								.getNeededResources(), this.name);
 						this.gotResources = false;
 					}
 				} else {
 					this.workingBoard.waitTillPostTask();
 					this.currentTask = null;
 				}
 			}
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			this.logger.fine("Worker " + this.name
 					+ " was interrupted (in catch block) at "
 					+ Helpers.staticTimeNow());
 		}	
 	}
 }
 
 
