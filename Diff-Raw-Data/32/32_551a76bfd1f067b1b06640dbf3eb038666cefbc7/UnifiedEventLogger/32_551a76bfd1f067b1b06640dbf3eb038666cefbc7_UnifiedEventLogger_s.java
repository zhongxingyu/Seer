 package com.pjf.mat.sys;
 
 import java.util.concurrent.PriorityBlockingQueue;
 
 import org.apache.log4j.Logger;
 
 import com.pjf.mat.api.TimeOrdered;
 import com.pjf.mat.api.Timestamp;
 import com.pjf.mat.sys.MatSystem.NotificationHandler;
 
 /**
  * Maintains a pipe of all received logs in time order. 
  * The delay through the pipe allows for late arriving events to be put in their
  * correct place.
  * 
  * @author pjf
  *
  */
 public class UnifiedEventLogger {
 	
 	private final static int windowMs = 50;
 	
 	private final static Logger logger = Logger.getLogger(UnifiedEventLogger.class);
 	private final PriorityBlockingQueue<TimeOrdered> store;
 	private final PumpWorker pumper;
 	private NotificationHandler notificationHandler;
 	private Timestamp lastAddedTs;	// timestamp of event last added
 
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
 	
 	public UnifiedEventLogger(NotificationHandler notificationHandler) {
 		this.notificationHandler = notificationHandler;
 		this.lastAddedTs = null;
 		store = new PriorityBlockingQueue<TimeOrdered>();
 		pumper = new PumpWorker();
 		pumper.start();
 	}
 	
 	public void addLog(TimeOrdered event) {
 		store.put(event);
 		lastAddedTs = event.getTimestamp();
 	}
 	
 	/**
 	 * Pump out all events that are windowMs earlier than the latest one written
 	 */
 	private void pump() {
 		if (lastAddedTs != null) {
 			while (true) {
 				TimeOrdered first = store.peek();
 				long diffNs = lastAddedTs.diffNs(first.getTimestamp());
 				if (diffNs < 1000000L * (long) windowMs) {
 					break;
 				}
 				TimeOrdered log;
 				try {
 					log = store.take();
 					notificationHandler.notifyUnifiedEventLog(log);
 				} catch (InterruptedException e) {
 					logger.warn("pump() - interrupted - " + e.getMessage());
 				}
 			}
 		}
 	}
 	
 	public void flush(){
 		while (!store.isEmpty()) {
 			try {
 				TimeOrdered log = store.take();
 				notificationHandler.notifyUnifiedEventLog(log);
 			} catch (InterruptedException e) {
 				logger.warn("logAndClear() - interrupted - " + e.getMessage());
 			}
 		}
 	}
 
 	public void shutdown() {
 		pumper.shutdown();		
 	}
 	
 	
 	
 
 }
