 package com.pjf.mat.sys;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.Semaphore;
 
 public class TimeoutSemaphore {
 
 	private static final long serialVersionUID = -3476547520719296799L;
 	private final Semaphore sem;
 	private final Timer timer;
 	private final TimeoutHandler timeout;
 	private boolean timedOutFlag;
 
 	class TimeoutHandler extends TimerTask {
 		@Override
 		public void run() {
 			sem.release();
 			timedOutFlag = true;
 		}		
 	}
 
 	public TimeoutSemaphore(int permits) {
 		sem = new Semaphore(permits);
 		timer = new Timer();
 		timeout = new TimeoutHandler();
 		timedOutFlag = false;
 	}
 
 	public void acquire(long timeoutMs) throws InterruptedException {
 		timer.schedule(timeout, timeoutMs);
 		timedOutFlag = false;		
 		sem.acquire();
 		timeout.cancel();
 	}
 	
 	public void release() {
 		sem.release();
		timer.cancel();
 	}
 	
 	public boolean timedOut() {
 		return timedOutFlag;
 	}
 }
