 package com.pjf.mat.sys;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.PriorityBlockingQueue;
 
 import org.apache.log4j.Logger;
 import com.pjf.mat.api.NotificationCallback;
 import com.pjf.mat.api.TimeOrdered;
 import com.pjf.mat.api.Timestamp;
 
 /**
  * Maintains a pipe of all received logs in time order. 
  * The delay through the pipe allows for late arriving events to be put in their
  * correct place.
  * 
  * @author pjf
  *
  */
 public class UnifiedEventLogger {	
	private final static int windowMs = 100;
 	private final static Logger logger = Logger.getLogger(UnifiedEventLogger.class);
	private static final long IDLE_TIME_FLUSH_MS = 1000;
 	private final PriorityBlockingQueue<TimeOrdered> store;
 	private final PumpWorker pumper;
 	private NotificationCallback notificationHandler;
 	private Timestamp lastAddedTs;	// timestamp of event last added
 	private boolean showLogs;
	private long lastRealtimeAddedMs = 0;
 
 	class PumpWorker extends Thread {
 		private boolean running;
 		
 		public PumpWorker() {
 			setName("UELogger");
 			running = true;
 		}
 		
 		@Override
 		public void run() {
 			while (running) {
 				try {
 					Thread.sleep(windowMs);
 					if (running) {
 						pump();
 					}
 				} catch (InterruptedException e) {
 					logger.debug("Thread interrupted: " + e.getMessage());
 				}
 			}
 		}
 
 		public void shutdown() {
 			running = false;
 		}
 	}
 	
 	public UnifiedEventLogger(NotificationCallback notificationHandler, boolean showLogs) {
 		this.notificationHandler = notificationHandler;
 		this.showLogs = showLogs;
 		this.lastAddedTs = null;
 		store = new PriorityBlockingQueue<TimeOrdered>();
 		pumper = new PumpWorker();
 		pumper.start();
 	}
 	
 	public void addLog(TimeOrdered event) {
 		if (showLogs) {
 			logger.info("Adding log: " + event);
 		}
 		store.put(event);
 		lastAddedTs = event.getTimestamp();
		lastRealtimeAddedMs = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Pump out all events that are windowMs earlier than the latest one written
 	 */
 	private void pump() {
		if (lastRealtimeAddedMs != 0) {
			if (!store.isEmpty()) {
				long msSinceLastAdd = System.currentTimeMillis() - lastRealtimeAddedMs;
				if (msSinceLastAdd > IDLE_TIME_FLUSH_MS) {
					flush();
				}
			}
		}
 		if (lastAddedTs != null) {
 			List<TimeOrdered> logs = new ArrayList<TimeOrdered>();
 			while (true) {
 				TimeOrdered first = store.peek();
 				if (first == null) {
 					break;
 				}
 				long diffNs = lastAddedTs.diffNs(first.getTimestamp());
 				if (diffNs < 100L * (long) windowMs) {
 					logger.debug("pump() diff is " + diffNs + " - breaking.");
 					break;
 				}
 				logger.debug("pump() diff is " + diffNs + " - logging.");
 				TimeOrdered log;
 				try {
 					log = store.take();
 					logs.add(log);
 				} catch (InterruptedException e) {
 					logger.warn("pump() - interrupted - " + e.getMessage());
 				}
 			}
 			if (!logs.isEmpty()) {
 				notificationHandler.notifyUnifiedEventLog(logs);				
 			}
 		}
 	}
 	
 	public void flush(){
 		List<TimeOrdered> logs = new ArrayList<TimeOrdered>();
 		store.drainTo(logs);
 		notificationHandler.notifyUnifiedEventLog(logs);		
 	}
 	
 	public void shutdown() {
 		pumper.shutdown();		
 	}
 	
 	
 	
 
 }
