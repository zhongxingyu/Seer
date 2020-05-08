 package model;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import controller.TimeKeeper;
 
 
 
 public class TaskScheduler extends TimerTask {
 	public Timer timer;
	private TimeKeeper timekeeper = TimeKeeper.getInstance();
 	private TaskScheduler taskscheduler;
 	private TaskServer taskServer;
 	
 	private static TaskScheduler instance = null;
 	   protected TaskScheduler() {
 	      // Exists only to defeat instantiation.
 	   }
 	   public static TaskScheduler getInstance() {
 	      if(instance == null) {
 	         instance = new TaskScheduler();
 	      }
 	      return instance;
 	   }
 	   
 	public void timeOut(){
 		if(timekeeper.getTimer() > (int)(timekeeper.getConfigurationTimeInMinutes()*60)){
 			timekeeper.setElapsedTimeInMinutes(timekeeper.getTimer()/60);
 			timekeeper.remainingTimeInMinutes = timekeeper.getMaximumTimeInMinutes() - timekeeper.getElapsedTimeInMinutes();
 			timer.schedule (taskscheduler,(int)(timekeeper.remainingTimeInMinutes * 60 * 1000));
 		}
 		else{
 			timer.schedule (taskscheduler,(int)(timekeeper.getRunTimeInMinutes() * 60 * 1000));
 		}
 	}
 
 	@Override
 	public void run() {
 		taskServer.taskComplete();
 	}
 
 }
