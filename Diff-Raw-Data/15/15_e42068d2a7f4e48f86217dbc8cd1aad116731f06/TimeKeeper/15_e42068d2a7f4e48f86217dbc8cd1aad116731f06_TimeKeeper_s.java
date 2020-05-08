 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.Timer;
 import model.TaskServer;
 import model.Logging;
 
 public class TimeKeeper{
 	public int timeCounterInSeconds = 0;
 	public int taskExecutionTimeInSeconds;
 	public int totalTeamTimeInMinutes;
 	public double configurationTimeInMinutes = 2.0;
 	public double runTimeInMinutes = 5.0;
 	public double maximumTimeInMinutes = configurationTimeInMinutes + runTimeInMinutes;
 	public double elapsedTimeInMinutes;
 	public double remainingTimeInMinutes;
 	private TaskServer tServer;
 	private Logging logg = Logging.getInstance("TaskLog.log");
 	private String timerOp= "Timer";
 	
 	private static TimeKeeper instance = null;
 	   protected TimeKeeper() {
 	      // Exists only to defeat instantiation.
 	   }
 	   public static TimeKeeper getInstance() {
 	      if(instance == null) {
 	         instance = new TimeKeeper();
 	      }
 	      return instance;
 	   }
 
 	public Timer MasterTimer = new Timer(1000, new ActionListener() {
 		 
         public void actionPerformed(ActionEvent e)
         {
         	timeCounterInSeconds++;
        	System.out.println(timeCounterInSeconds);
        	if(timeCounterInSeconds > (maximumTimeInMinutes * 60)){
         		timeCounterInSeconds = 0;
         		tServer.listenForConnection();
         	}
         }
        });
 	
 	public void startTimer(){
 		System.out.println(timeCounterInSeconds);
 		MasterTimer.start();
 		logg.LoggingFile(timerOp,"Started");
 	}
 	
 	public void stopTimer(){
 		System.out.println(timeCounterInSeconds);
 		MasterTimer.stop();
 		logg.LoggingFile(timerOp,"Stopped in " + timeCounterInSeconds + "sec");
 	}
 	
 	public int getTimer(){
 		System.out.println(timeCounterInSeconds);
 		return timeCounterInSeconds;
 	}
 }
