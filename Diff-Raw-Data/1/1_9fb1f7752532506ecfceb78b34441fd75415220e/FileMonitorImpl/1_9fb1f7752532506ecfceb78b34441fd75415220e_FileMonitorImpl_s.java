 /**
  * 
  */
 package org.raptorframework.raptor;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /** 
  * @author Roger Schildmeijer
  *
  */
 public class FileMonitorImpl implements FileMonitor {
 
 	private final Map<File, FileObserver> observers = new HashMap<File, FileObserver>();
 	private final Map<File, Long> fileStatuses = new HashMap<File, Long>();
 	private final long checkInterval = 1000 * 1; /* ms */
 
 	private ScheduledExecutorService scheduler;
 
 	public void registerFileObserver(File fileToObserve, FileObserver listener) {
 		fileStatuses.put(fileToObserve, fileToObserve.lastModified());
 
 		synchronized (observers) {
 			observers.put(fileToObserve, listener);
 			if (observers.size() == 1) {
 				scheduler = Executors.newSingleThreadScheduledExecutor();
 				scheduler.scheduleAtFixedRate(
 						filesPoller, 0 /*initial delay*/, 
 						checkInterval, 
 						TimeUnit.MILLISECONDS
 				);
 			}
 		}
 
 	}
 
 	public void unregisterFileObserver(File observedFile) {
 		synchronized (observers) {
 			fileStatuses.remove(observedFile);
 			observers.remove(observedFile);
 			if (observers.isEmpty()) {
 				scheduler.shutdown();
 			}
 		}
 	}
 
 	private Runnable filesPoller = new Runnable() {
 
 		public void run() {
 			synchronized (observers) {
 				for (File file: observers.keySet()) {
 					if (file.lastModified() != fileStatuses.get(file)) {
 						// file has changed. 
 						fileStatuses.put(file, file.lastModified());
 						FileObserver observer = observers.get(file);
 						observer.fileChanged(file);
 					}
 				}
 			}
 
 		}
 
 	};
 
 }
