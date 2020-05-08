 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.dbgp.internal;
 
 public abstract class DbgpWorkingThread extends DbgpTermination {
 	private Thread thread;
 	private final String name;
 
 	public DbgpWorkingThread(String name) {
 		this.name = name;
 	}
 
 	public void start() {
 		if (thread == null || !thread.isAlive()) {
 			thread = new Thread(new Runnable() {
 				public void run() {
 					try {
 						workingCycle();
 					} catch (Exception e) {
 						fireObjectTerminated(e);
 						return;
 					}
 
 					fireObjectTerminated(null);
 				}
 			}, name);
 
 			thread.start();
 		} else {
 			throw new IllegalStateException("Thread already started");
 		}
 	}
 
 	public void requestTermination() {
 		if (thread != null && thread.isAlive()) {
 			thread.interrupt();
 		}
 	}
 
 	public void waitTerminated() throws InterruptedException {
		thread.join();
 	}
 
 	// Working cycle
 	protected abstract void workingCycle() throws Exception;
 }
