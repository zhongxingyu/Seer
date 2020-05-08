 package org.finomnis.mcdaemon.automation;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.finomnis.mcdaemon.tools.Log;
 
 public class TaskScheduler implements Runnable{
 
 	private List<Task> taskList = new LinkedList<Task>();
 	private Thread schedulerThread = null;
 	private volatile boolean shutdownRequested = false;
 	
 	public TaskScheduler(){
 		
 	}
 	
 	public void addTask(Task task){
 		taskList.add(task);
 	}
 	
 	public void requestShutdown(){
 		shutdownRequested = true;
		schedulerThread.interrupt();
 	}
 	
 	@Override
 	public void run() {
 		schedulerThread = Thread.currentThread();
 		
 		while(true){
 			if(shutdownRequested)
 				return;
 			
 			long sleepingTime = Long.MAX_VALUE;
 			long now = new Date().getTime();
 			Task nextTask = null;
 			for(Task task : taskList)
 			{
 				
 				long timeUntilTask = task.getScheduleTime().getTime() - now;
 				if(timeUntilTask < sleepingTime){
 					sleepingTime = timeUntilTask;
 					nextTask = task;
 				}
 			}
 			if(nextTask == null)
 				sleepingTime = 5000;
 				
 			try {
 				if(sleepingTime > 0)
 					Thread.sleep(sleepingTime);
 				nextTask.run();
 				nextTask.generateNewScheduleTime();
 			} catch (InterruptedException e) {
 				Log.debug("Scheduler Thread got interrupted.");
 			}
 		}
 	}
 
 }
