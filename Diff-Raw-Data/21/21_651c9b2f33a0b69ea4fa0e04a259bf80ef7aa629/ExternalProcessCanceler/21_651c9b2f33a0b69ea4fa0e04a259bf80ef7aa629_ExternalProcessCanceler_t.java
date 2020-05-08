 package org.padacore.core.utils;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * Monitor an external process in order to stop it when required by the monitor.
  */
 public class ExternalProcessCanceler extends Thread {
 
 	private IExternalProcess process;
 	private IProgressMonitor monitor;
	
	private static long ONE_SECOND_IN_MS = 1000;
 
 	public ExternalProcessCanceler(IExternalProcess process, IProgressMonitor monitor) {
 		this.process = process;
 		this.monitor = monitor;
 	}
 
 	@Override
 	public void run() {
 		while (!this.process.isFinished()) {
 			if (this.monitor.isCanceled()) {
 				this.process.stop();
			} else {
				try {
					sleep(ONE_SECOND_IN_MS);
				} catch (InterruptedException e) {
				}
 			}
 		}
 	}
 }
