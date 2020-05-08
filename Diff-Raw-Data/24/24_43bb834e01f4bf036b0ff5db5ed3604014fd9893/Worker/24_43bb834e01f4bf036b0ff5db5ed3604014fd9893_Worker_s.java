package edu.berkeley.cs.cs162.Server;

import edu.berkeley.cs.cs162.Synchronization.ThreadSafeQueue;
 
 public abstract class Worker implements Runnable {
	/**
	 * Message for worker threads. 
	 */
	ThreadSafeQueue<Runnable> messageQueue;
 }
