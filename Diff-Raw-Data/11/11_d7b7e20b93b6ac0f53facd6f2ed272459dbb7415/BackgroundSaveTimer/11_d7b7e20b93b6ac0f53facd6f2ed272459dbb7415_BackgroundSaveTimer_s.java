 package org.eclipse.mylar.tasklist.internal;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.mylar.core.util.ErrorLogger;
 import org.eclipse.mylar.core.util.ITimerThreadListener;
 import org.eclipse.mylar.core.util.TimerThread;
 
 /**
  * Timer that periodically runs saveRequested() on its client as a job
  * 
  * @author Wesley Coelho
  */
 public class BackgroundSaveTimer implements ITimerThreadListener {
 
	private final static int DEFAULT_SAVE_INTERVAL = 5 * 60 * 1000;
 	
 	private int saveInterval = DEFAULT_SAVE_INTERVAL;
 
 	private IBackgroundSaveListener listener = null;
 
 	private TimerThread timer = null;
 
 	private boolean forceSyncExec = false;
 	
 	public BackgroundSaveTimer(IBackgroundSaveListener listener) {
 		this.listener = listener;
 		timer = new TimerThread(saveInterval / 1000); // This constructor wants seconds
 		timer.addListener(this);
 	}
 
 	public void start() {
 		timer.start();
 	}
 	
 
 	public void stop() {
 		timer.killTimer();
 	}
 	
 	public void setSaveIntervalMillis(int saveIntervalMillis) {
 		this.saveInterval = saveIntervalMillis;
 		timer.setTimeoutMillis(saveIntervalMillis);
 	}
 
 	public int getSaveIntervalMillis() {
 		return saveInterval;
 	}
 
 	/**
 	 * For testing
 	 */
 	public void setForceSyncExec(boolean forceSyncExec) {
 		this.forceSyncExec = forceSyncExec;
 	}
 
 	/**
 	 * Called by the ActivityTimerThread Calls save in a new job
 	 */
 	public void fireTimedOut() {
 		try {
 			if (!forceSyncExec) {
 				final SaveJob job = new SaveJob("Saving Task Data", listener);
 				job.schedule();
 			} else {
 				listener.saveRequested();
 			}
 		} catch (RuntimeException e) {
 			ErrorLogger.log("Could not schedule save job", this);
 		}
 	}
 
 	/** Job that makes the save call */
 	private class SaveJob extends Job {
 		private IBackgroundSaveListener listener = null;
 
 		public SaveJob(String name, IBackgroundSaveListener listener) {
 			super(name);
 			this.listener = listener;
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			listener.saveRequested();
 			return Status.OK_STATUS;
 		}
 	}
 
 }
