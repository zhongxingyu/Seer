 /*
  * Mar 18, 2006
  */
 package com.thinkparity.browser.application.system;
 
 import com.thinkparity.model.parity.model.artifact.Artifact;
 import com.thinkparity.model.parity.model.artifact.ArtifactVersion;
 import com.thinkparity.model.parity.model.message.system.SystemMessage;
 
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 class SysAppImpl extends Thread {
 
 	/**
 	 * Flag indicating the current running state.
 	 * 
 	 * @see #run()
 	 */
 	Boolean running = Boolean.FALSE;
 
 	/**
 	 * Queue of pending new/updated artifacts\versions\system messages.
 	 * 
 	 */
 	private Integer queueItems = 0;
 
 	/**
 	 * The system application.
 	 * 
 	 */
 	private final SysApp sysApp;
 
 	/**
 	 * The system tray functionality.
 	 * 
 	 */
 	private SysTray sysTray;
 
 	SysAppImpl(final SysApp sysApp) {
 		super("[BROWSER2] [APP] [SYS] [THREAD]");
 		this.sysApp = sysApp;
 	}
 
 	/**
 	 * @see java.lang.Thread#run()
 	 * 
 	 */
 	public void run() {
 		while(running) {
 			sysApp.logger.info("[BROWSER2] [APP] [SYS] [IMPL] [RUNNING]");
 			try { synchronized(this) { wait(); } }
 			catch(final InterruptedException ix) {
 				sysApp.logger.info("[BROWSER2] [APP] [SYS] [IMPL] [INTERRUPTED]");
 			}
			try { processQueue(); }
			catch(final RuntimeException rx) {
				sysApp.logger.error("[BROWSER2] [APP] [SYS] [IMPL RUNNING]", rx);
				throw rx;
			}
 		}
 	}
 
 	/**
 	 * @see java.lang.Thread#start()
 	 * 
 	 */
 	public synchronized void start() {
 		running = Boolean.TRUE;
 		sysTray = new SysTray(sysApp);
 		super.start();
 	}
 
 	/**
 	 * End the application.
 	 *
 	 */
 	void end() {
 		running = Boolean.FALSE;
 		synchronized(this) { notifyAll(); }
 	}
 
 	String getString(final String localKey) {
 		return sysApp.getString(localKey);
 	}
 
 	String getString(final String localKey, final Object[] arguments) {
 		return sysApp.getString(localKey, arguments);
 	}
 
 	/**
 	 * Notification that an artifact has been received.
 	 * 
 	 * @param artifact
 	 *            The artifact.
 	 */
 	void notifyReceived(final Artifact artifact) {
 		synchronized(this) {
 			queueItems++;
 			notifyAll();
 		}
 	}
 
 	/**
 	 * Notification that an artifact version has been received.
 	 * 
 	 * @param artifactVersion
 	 *            The artifact.
 	 */
 	void notifyReceived(final ArtifactVersion artifactVersion) {
 		synchronized(this) {
 			queueItems++;
 			notifyAll();
 		}
 	}
 
 	/**
 	 * Notification that a system message has been received.
 	 * 
 	 * @param systemMessage
 	 *            The system message.
 	 */
 	void notifyReceived(final SystemMessage systemMessage) {
 		synchronized(this) {
 			queueItems++;
 			notifyAll();
 		}
 	}
 
 	/**
 	 * Reset the number of queue items.
 	 *
 	 */
 	void resetQueue() {
 		synchronized(this) {
 			queueItems = 0;
 			notifyAll();
 		}
 	}
 
 	/**
 	 * Obtain the total number of queued events.
 	 * 
 	 * @return The total number of queued artifacts and artifact versions.
 	 */
 	private Integer getQueueTotal() { return queueItems; }
 
 	/**
 	 * Process pending queue events.
 	 *
 	 */
 	private void processQueue() {
 		sysApp.logger.info("[BROWSER2] [APP] [SYS] [IMPL] [QUEUE:" + getQueueTotal()  + "]");
 		if(0 < getQueueTotal()) {
 			if(!sysTray.isInstalled()) { sysTray.install(); }
 		}
 		else {
 			if(sysTray.isInstalled()) { sysTray.unInstall(); }
 		}
 	}
 }
