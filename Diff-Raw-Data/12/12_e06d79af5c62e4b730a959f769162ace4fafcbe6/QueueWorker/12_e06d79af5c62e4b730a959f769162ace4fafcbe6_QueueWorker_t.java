 package de.sectud.ctf07.scoringsystem;
 
 public class QueueWorker extends Thread {
 	private QueueJob job = null;
 
 	private boolean quit = false;;
 
 	public QueueWorker() {
 		setDaemon(true);
 		start();
 	}
 
 	public synchronized void quit() {
 		this.quit = true;
 		notify();
 	}
 
 	public synchronized boolean setJob(QueueJob job) {
 		if (this.job != null) {
 			return false;
 		}
 		this.job = job;
 		notify();
 		return true;
 	}
 
 	public synchronized boolean hasJob() {
 		return this.job != null;
 	}
 
 	@Override
 	public void run() {
 		QueueJob qj = null;
 		while (!this.quit) {
 			synchronized (this) {
 				try {
 					if (this.job == null) {
 						wait();
 					}
 					if (this.quit) {
 						break;
 					}
 					qj = this.job;
 				} catch (InterruptedException e) {
 				}
 			}
 			if (qj != null) {
 				try {
 					qj.runonce();
					synchronized (this) {
						this.job = null;
					}
 				} catch (Throwable t) {
 				}
 			}
 		}
 	}
 }
