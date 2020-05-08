 package grisu.model.status;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.StatusException;
 import grisu.model.dto.DtoActionStatus;
 
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.concurrent.CountDownLatch;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class StatusObject {
 
 	public interface Listener {
 		public void statusMessage(ActionStatusEvent event);
 	}
 
 	static final Logger myLogger = LoggerFactory.getLogger(StatusObject.class
 			.getName());
 
 	public static StatusObject waitForActionToFinish(ServiceInterface si, String handle)
 			throws StatusException {
 		return waitForActionToFinish(si, handle, 10, false);
 
 	}
 
 	public static StatusObject waitForActionToFinish(ServiceInterface si,
 			String handle, int recheckIntervalInSeconds, boolean exitIfFailed)
 					throws StatusException {
 		return waitForActionToFinish(si, handle, recheckIntervalInSeconds,
 				exitIfFailed, -1);
 	}
 
 	public static StatusObject waitForActionToFinish(ServiceInterface si,
 			String handle, int recheckIntervalInSeconds, boolean exitIfFailed,
 			int treshold_in_secs) throws StatusException {
 
 		final StatusObject temp = new StatusObject(si, handle);
 		temp.waitForActionToFinish(recheckIntervalInSeconds,
 				exitIfFailed, treshold_in_secs);
 
 		return temp;
 	}
 
 	private final ServiceInterface si;
 	private final String handle;
 
 	private volatile Thread t = null;
 
 	private Vector<Listener> listeners;
 
 	private DtoActionStatus lastStatus;
 	private double lastPercentage = 0;
 	private boolean taskFinished = false;
 
 	private long startMonitoringTime = -1;
 	private long finishedMonitoringTime = -1;
 
 	private volatile boolean waitWasInterrupted = false;
 
 	private final CountDownLatch finished = new CountDownLatch(1);
 
 	public StatusObject(ServiceInterface si, String handle) {
 		this(si, handle, (Vector) null);
 	}
 
 	public StatusObject(ServiceInterface si, String handle, Listener l) {
 		this(si, handle, (Vector) null);
 		addListener(l);
 	}
 
 	public StatusObject(ServiceInterface si, String handle,
 			Vector<Listener> listeners) {
 		this.si = si;
 		this.handle = handle;
 		this.listeners = listeners;
 
 		if (listeners != null) {
 			for (final Listener l : listeners) {
 				addListener(l);
 			}
 		}
 	}
 
 	synchronized public void addListener(Listener l) {
 		if (listeners == null) {
 			listeners = new Vector();
 		}
 		listeners.addElement(l);
 	}
 
 	private synchronized void createWaitThread(final int waitTime,
 			final int thresholdInSeconds) {
 
 		if (t == null) {
 			t = new Thread() {
 				@Override
 				public void run() {
 					while (!getStatus().isFinished()) {
 						try {
							if (((new Date().getTime() - startMonitoringTime) * 1000) < thresholdInSeconds) {
 								throw new InterruptedException(
 										"Threshold for task monitoring exceeded. Not waiting any longer...");
 							}
 							myLogger.debug("Waiting for task {} to finish...",
 									handle);
 							Thread.sleep(waitTime * 1000);
 						} catch (final InterruptedException e) {
 							myLogger.error(e.getLocalizedMessage(), e);
 							waitWasInterrupted = true;
 							break;
 						}
 					}
 					finishedMonitoringTime = new Date().getTime();
 					finished.countDown();
 				}
 			};
 			t.setName("Wait thread for status: " + handle);
 			startMonitoringTime = new Date().getTime();
 			t.start();
 		}
 	}
 
 	private void fireEvent(ActionStatusEvent message) {
 		if ((listeners != null) && !listeners.isEmpty()) {
 
 			// make a copy of the listener list in case
 			// anyone adds/removes mountPointsListeners
 			Vector targets;
 			synchronized (this) {
 				targets = (Vector) listeners.clone();
 			}
 
 			// walk through the listener list and
 			// call the gridproxychanged method in each
 			final Enumeration e = targets.elements();
 			while (e.hasMoreElements()) {
 				final Listener l = (Listener) e.nextElement();
 				l.statusMessage(message);
 			}
 		}
 	}
 
 	public String getHandle() {
 		return this.handle;
 	}
 
 	public synchronized DtoActionStatus getStatus() {
 
 		if (taskFinished) {
 			return lastStatus;
 		}
 		myLogger.debug("Checking status for: " + handle);
 		lastStatus = si.getActionStatus(handle);
 		if ((taskFinished != lastStatus.isFinished())
 				|| (lastPercentage != lastStatus.percentFinished())) {
 			lastPercentage = lastStatus.percentFinished();
 			taskFinished = lastStatus.isFinished();
 			fireEvent(new ActionStatusEvent(lastStatus));
 		}
 		myLogger.debug("Status for " + handle + ": "
 				+ lastStatus.percentFinished() + " %" + " / finished: "
 				+ lastStatus.isFinished());
 
 		return lastStatus;
 	}
 
 	synchronized public void removeListener(Listener l) {
 		if (listeners == null) {
 			listeners = new Vector<Listener>();
 		}
 		listeners.removeElement(l);
 	}
 
 	public void waitForActionToFinish(int recheckIntervalInSeconds,
 			boolean exitIfFailed)
 					throws StatusException {
 		waitForActionToFinish(recheckIntervalInSeconds, exitIfFailed,
 				-1);
 	}
 
 	/**
 	 * Waits for the remote task to be finshed.
 	 * 
 	 * @param recheckIntervalInSeconds
 	 *            how long to wait inbetween status checks
 	 * @param exitIfFailed
 	 *            whether to return from wait if task not finished yet but
 	 *            failed already (maybe because at least one sub-task failed).
 	 * @param threshholdInSeconds
 	 *            after how long to stop monitoring (in seconds) or -1 for never
 	 *            stop monitoring until task finished
 	 * @return true if the task is finished, false if monitoring was interrupted
 	 *         either by thread interrupt or threshold
 	 * @throws StatusException
 	 *             if the handle can't be found
 	 */
 	public void waitForActionToFinish(int recheckIntervalInSeconds,
 			boolean exitIfFailed, int threshholdInSeconds)
 					throws StatusException {
 
 		lastStatus = si.getActionStatus(handle);
 
 		if (lastStatus == null) {
 
 			throw new StatusException("Can't find status with handle "
 					+ this.handle);
 		}
 
 		if (lastStatus.isFinished()) {
 			return;
 		}
 
 		createWaitThread(recheckIntervalInSeconds, threshholdInSeconds);
 
 		try {
 			finished.await();
 		} catch (InterruptedException e) {
 			myLogger.error("Waiting for status " + handle
 					+ " to finish interrupted.", e);
 			waitWasInterrupted = true;
 			throw new StatusException(e.getLocalizedMessage(), e);
 		}
 
 		return;
 
 	}
 
 }
