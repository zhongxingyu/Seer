 package model;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.event.EventListenerList;
 
 import controller.ConnectionListener;
 import controller.TimerListener;
 
 public class TaskTimer implements ConnectionListener {
 
 	private long configTime;
 	private long runTime;
 	private long usedTime;
 	private Timer timer;
 	private long startTime;
 	private long startRunTime;
 	private boolean configTimeRunning;
 	private boolean runTimeRunning;
 	private boolean timerStop;
 	private boolean inTime;
 	private Logging logg;
 	private String logId = "Timer";
 
 	private EventListenerList listOfTimerListeners = new EventListenerList();
 
 	class Task extends TimerTask {
 
 		@Override
 		public void run() {
 			if (timerStop) {
 				this.cancel();
 				return;
 			}
 			if (configTimeRunning) {
 				usedTime = System.currentTimeMillis() - startTime;
 				if (usedTime >= configTime) {
 					setConfigTimeStop();
 					inTime = false;
 				}
 			}
 			if (runTimeRunning) {
 				usedTime = System.currentTimeMillis() - startRunTime;
 				if (usedTime >= runTime) {
 					notifyTimerTick(millisecToString(usedTime), inTime);
 					notifyTimerOverrun();
 					cancel();
 					return;
 				}
 			}
 			notifyTimerTick(millisecToString(usedTime), inTime);
 		}
 	}
 
 	public TaskTimer() {
 		logg = Logging.getInstance();
 		timerStop = true;
 		usedTime = 0;
 	}
 
 	public void startNewTimer(long configTime, long runTime) {
 
 		if (timerStop) {
 			this.configTime = configTime * 1000;
 			this.runTime = runTime * 1000;
 			usedTime = 0;
 			timer = new Timer();
 			configTimeRunning = true;
 			runTimeRunning = false;
 			inTime = true;
 			timerStop = false;
 			startTime = System.currentTimeMillis();
 			timer.scheduleAtFixedRate(new Task(), 10, 10);
 			logg.globalLogging(logId, "New config time  "
 					+ millisecToString(this.configTime));
 			logg.globalLogging(logId, "New run time "
 					+ millisecToString(this.runTime));
			logg.globalLogging(logId, "Setup time startet at "
 					+ millisecToString(usedTime));
 			notifyTimerReset(millisecToString(usedTime));
 			notifyTimerSetMaximumTime(("setup time: ")
 					.concat(millisecToString(this.configTime)));
 		}
 	}
 
 	public void startRunTimer() {
 		runTimeRunning = true;
 	}
 
 	public void stopTimer() {
 		timerStop = true;
 		logg.globalLogging(logId, "stopped at " + millisecToString(usedTime));
 		logg.competitionLogging(logId, "stopped at "
 				+ millisecToString(usedTime));
 	}
 
 	public void resetTimer() {
 		this.configTime = 0;
 		this.runTime = 0;
 		usedTime = 0;
 		configTimeRunning = false;
 		runTimeRunning = false;
 		timerStop = true;
 		notifyTimerReset(millisecToString(usedTime));
 	}
 
 	public void setConfigTimeStop() {
 		configTimeRunning = false;
 		usedTime = 0;
 		startRunTime = System.currentTimeMillis();
 		runTimeRunning = true;
 		notifyTimerSetMaximumTime(("run time: ")
 				.concat(millisecToString(runTime)));
 		logg.globalLogging(logId, "Run time started at "
 				+ millisecToString(usedTime));
 		logg.competitionLogging(logId, "Run time started at "
 				+ millisecToString(usedTime));
 	}
 
 	private void notifyTimerTick(String currentTime, boolean inTime) {
 		Object[] listeners = listOfTimerListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TimerListener.class) {
 				((TimerListener) listeners[i + 1]).timerTick(currentTime,
 						inTime);
 			}
 		}
 	}
 
 	private void notifyTimerReset(String resetTime) {
 		Object[] listeners = listOfTimerListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TimerListener.class) {
 				((TimerListener) listeners[i + 1]).timerReset(resetTime);
 			}
 		}
 	}
 
 	private void notifyTimerSetMaximumTime(String maxTime) {
 		Object[] listeners = listOfTimerListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TimerListener.class) {
 				((TimerListener) listeners[i + 1]).timerSetMaximumTime(maxTime);
 			}
 		}
 	}
 
 	public void notifyTimerOverrun() {
 		Object[] listeners = listOfTimerListeners.getListenerList();
 		// Each listener occupies two elements - the first is the listener class
 		// and the second is the listener instance
 		for (int i = 0; i < listeners.length; i += 2) {
 			if (listeners[i] == TimerListener.class) {
 				((TimerListener) listeners[i + 1]).timerOverrun();
 			}
 		}
 	}
 
 	public void addTimerListener(TimerListener tL) {
 		listOfTimerListeners.add(TimerListener.class, tL);
 	}
 
 	public void removeTimerListener(TimerListener tL) {
 		listOfTimerListeners.remove(TimerListener.class, tL);
 	}
 
 	private String millisecToString(long millisec) {
 		Date date = new Date(millisec);
 		String formattedDate = new SimpleDateFormat("mm:ss").format(date);
 		return formattedDate;
 	}
 
 	@Override
 	public void teamConnected(String teamName) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void teamDisconnected() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void taskSpecSent() {
 		if (configTimeRunning)
 			setConfigTimeStop();
 		inTime = true;
 	}
 }
