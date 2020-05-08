 /* 
  * This code isn't copyrighted. Do what you want with it. :) 
  */
 package panoramakit.engine.task;
 
 import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMemoryErrorScreen;
 
 /**
  * The task manager keeps track of all the tasks currently running.
  * 
  * @author dayanto
  */
 public class TaskManager
 {
	private static final Minecraft mc = Minecraft.getMinecraft();
	
 	public static final TaskManager instance = new TaskManager();
 	
 	private Task activeTask = null;
 	private ArrayList<Task> taskQueue = new ArrayList<>();
 	
 	/**
 	 * Add a new task to the end of the task queue.
 	 */
 	public void addTask(Task task)
 	{
 		taskQueue.add(task);
 	}
 	
 	/**
 	 * Returns whether or not the task manager has any tasks.
 	 */
 	public boolean hasTasks()
 	{
 		return !taskQueue.isEmpty() || activeTask != null;
 	}
 	
 	/**
 	 * Returns the number of tasks that are in the queue, waiting to run.
 	 */
 	public int tasksInQueue()
 	{
 		return taskQueue.size();
 	}
 	
 	/**
 	 * Get the currently active task.
 	 */
 	public Task getCurrentTask()
 	{
 		return activeTask;
 	}
 	
 	/**
 	 * Terminates all tasks by first clearing all the scheduled tasks and then asking the currently active task to stop. The mod will then
 	 * continuously ask the remaining task whether it has finished and remove it once it has.
 	 */
 	public void halt()
 	{
 		if (!taskQueue.isEmpty()) {
 			taskQueue.clear();
 			activeTask.stop();
 		}
 	}
 	
 	/**
 	 * Executes the current task and continues with the next task in the queue if it has finished.
 	 */
 	public void runTick()
 	{
 		if (!hasTasks()) return;
		if (mc.currentScreen instanceof GuiMemoryErrorScreen) halt();
 		
 		do {
 			if(activeTask == null) runNextTask();
 			
 			activeTask.perform();
 			clearTaskIfCompleted();
 			clearTaskIfStopped();
 		} 
 		while(activeTask == null && taskQueue.size() > 0); // if one has been completed continue doing the next
 	}
 	
 	/**
 	 * Takes the first task in the queue and runs it.
 	 */
 	private void runNextTask()
 	{
 		if (taskQueue.size() > 0) {
 			activeTask = taskQueue.get(0);
 			taskQueue.remove(0);
 			activeTask.init();
 			if (activeTask instanceof ThreadedTask) {
 				((ThreadedTask)activeTask).start();
 			}
 		}
 	}
 	
 	/**
 	 * If the current task has been completed, finish it off and get rid of it.
 	 */
 	private void clearTaskIfCompleted()
 	{
 		if (activeTask != null) {
 			if (activeTask.hasCompleted()) {
 				activeTask.finish();
 				activeTask = null;
 			}
 		}
 	}
 	
 	/**
 	 * Removes the current task if it has been stopped and handled all the cleanup.
 	 */
 	private void clearTaskIfStopped()
 	{
 		if (activeTask != null && activeTask.hasStopped()) {
 			activeTask = null;
 		}
 	}
 }
