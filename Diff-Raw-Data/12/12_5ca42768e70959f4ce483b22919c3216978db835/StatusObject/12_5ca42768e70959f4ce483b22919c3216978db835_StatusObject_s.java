 package org.vpac.grisu.model.status;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bushe.swing.event.EventBus;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.model.dto.DtoActionStatus;
 
 public class StatusObject {
 
 	public enum Listener {
 		STDOUT
 	}
 
 	public static StatusObject waitForActionToFinish(ServiceInterface si,
 			String handle, int recheckIntervalInSeconds, boolean exitIfFailed,
 			boolean sendStatusEvent) throws InterruptedException {
 
 		StatusObject temp = new StatusObject(si, handle);
 		temp.waitForActionToFinish(recheckIntervalInSeconds, exitIfFailed,
 				sendStatusEvent);
 
 		return temp;
 	}
 
 	private final ServiceInterface si;
 	private final String handle;
 
 	private final Set<Listener> listeners;
 
 	private DtoActionStatus lastStatus;
 
 	public StatusObject(ServiceInterface si, String handle) {
 		this(si, handle, new HashSet<Listener>());
 	}
 
 	public StatusObject(ServiceInterface si, String handle, Listener l) {
 		this(si, handle, new HashSet<Listener>());
 		addListener(l);
 	}
 
 	public StatusObject(ServiceInterface si, String handle,
 			Set<Listener> listeners) {
 		this.si = si;
 		this.handle = handle;
 		this.listeners = listeners;
 
 		if (listeners != null) {
 			for (Listener l : listeners) {
 				addListener(l);
 			}
 		}
 	}
 
 	public void addListener(Listener l) {
 
 		if (l != null) {
 			switch (l) {
 			case STDOUT:
 				throw new UnsupportedOperationException("not yet supported");
 			}
 		}
 
 	}
 
 	public DtoActionStatus getStatus() {
 
 		lastStatus = si.getActionStatus(handle);
 		return lastStatus;
 	}
 
 	public void waitForActionToFinish(int recheckIntervalInSeconds,
 			boolean exitIfFailed, boolean sendStatusEvent)
 			throws InterruptedException {
 		waitForActionToFinish(recheckIntervalInSeconds, exitIfFailed,
 				sendStatusEvent, null);
 	}
 
 	public void waitForActionToFinish(int recheckIntervalInSeconds,
 			boolean exitIfFailed, boolean sendStatusEvent,
 			String statusMessagePrefix) throws InterruptedException {
 
 		while (!(lastStatus = si.getActionStatus(handle)).isFinished()) {
 			if (sendStatusEvent) {
 				EventBus.publish(handle, new ActionStatusEvent(lastStatus,
 						statusMessagePrefix));
 			}
 
 			if (exitIfFailed) {
 				if (lastStatus.isFailed()) {
 					return;
 				}
 			}
 
 			if (Thread.currentThread().isInterrupted()) {
 				throw new InterruptedException(
 						"Interrupted while waiting for action " + handle
 								+ " to finish on backend.");
 			}
 
 			try {
 				Thread.sleep(recheckIntervalInSeconds * 1000);
 			} catch (InterruptedException e) {
				// doesn't matter
 			}
 
 		}
 		return;
 
 	}
 
 }
