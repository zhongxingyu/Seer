 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.transfer.manager;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.ContinuationHandler;
 import de.tuilmenau.ics.fog.EventHandler;
 import de.tuilmenau.ics.fog.IContinuation;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.authentication.IdentityManagement;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.ui.Viewable;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.Timer;
 
 
 /**
  * Base class for all ongoing actions on a node. An instance indicates that
  * a work flow has been started and awaiting further input.
  * 
  * A process is independent from signaling messages and can be used without
  * them. If signaling messages are needed in order to finish a process, they
  * have to be created by the caller of a process.
  * 
  * A process is using a timer, which is able to terminate the process or
  * to trigger a check of the process status. The action of the timer
  * depends on the state of the process:
  *  - STARTING => terminate
  *  - OPERATING => check
  *  - CLOSING => none
  */
 public abstract class Process
 {
 	/**
 	 * Enables periodical checks of process in OPERATIONAL mode.
 	 */
 	private static final boolean CHECK_CONTINOUSLY_IN_OPERATING_MODE = Config.Transfer.PROCESS_CHECK_CONTINOUSLY_IN_OPERATING_MODE;
 	
 	
 	public Process(ForwardingNode base, Identity owner)
 	{
 		mProcessID = -1;
 		mBase = base;
 		mState = ProcessState.INIT;
 		mLogger = base.getEntity().getLogger();
 		
 		// Use owner if it is known to authentication service. Otherwise, use
 		// default identity of node or (TODO) create special one
 		IdentityManagement authService = mBase.getEntity().getAuthenticationService();
 		if(authService.canSignFor(owner)) {
 			mOwner = owner;
 		} else {
 			mOwner = mBase.getOwner();
 		}		
 		
		final Process tThis = this;
 		mTimer = new Timer(getTimeBase(), new IEvent() {
 			@Override
 			public void fire()
 			{
 				if(getState() == ProcessState.STARTING) {
 					mLogger.warn(Process.this, "Timeout during STARTING. Terminating...");
 					terminate(new NetworkException(this, "Timeout during STARTING."));
 				}
 				else if(getState() == ProcessState.OPERATING) {
 					try {
 						if(check() && !mBase.getEntity().getNode().isShuttingDown()) {
 							// everything ok; wait for next timeout
 							if(CHECK_CONTINOUSLY_IN_OPERATING_MODE) {
 								mTimer.restart();
 							}
 						} else {
 							mLogger.log(Process.this, "Check of process state was not successful. Terminating process...");
 							terminate(new NetworkException(this, "Status check of gates in process failed."));
 						}
 					}
 					catch(NetworkException tExc) {
 						mLogger.warn(Process.this, "Error during check. Terminating...", tExc);
 						terminate(tExc);
 					}
 				} 
 			}
			
			public String toString()
			{
				return tThis.toString();
			}
 		}, Config.PROCESS_STD_TIMEOUT_SEC);
 	}
 	
 	/**
 	 * Method for starting up a process.
 	 * Use it after the constructor.
 	 */
 	public void start() throws NetworkException
 	{
 		setState(ProcessState.STARTING);
 		mTimer.start();
 		
 		// make it available for signaling
 		register(getBase().getFreeGateNumber());
 	}
 	
 	/**
 	 * Checks regularly if the process is in a good state.
 	 * If check returns an error or throws an exception, the process will be terminated.
 	 * 
 	 * @return true=everything ok; false=terminate process
 	 * @throws NetworkException On problems. It will lead to the termination of the process
 	 */
 	protected abstract boolean check() throws NetworkException;
 
 	/**
 	 * Waits until the process had been terminated.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public void join() throws InterruptedException
 	{
 		synchronized(this) {
 			while(getState() != ProcessState.CLOSING) {
 				wait();
 			}
 		}
 	}
 	
 	protected void restartTimer()
 	{
 		mTimer.restart();
 	}
 	
 	/**
 	 * Terminates process and all related timer.
 	 * Method sets state to CLOSING.
 	 * All threads waiting for the process are waked up.
 	 * 
 	 * @param pCause if != null cause of termination is stated 
 	 */
 	public final void terminate(Exception pCause)
 	{
 		mLogger.log(this, "Terminating proccess now..");
 		if(!isFinished()) {
 			setState(ProcessState.CLOSING);
 			
 			mTerminationCause = pCause;
 
 			finished();
 			
 			// inform waiting threads
 			synchronized(this) {
 				notifyAll();
 			}
 		}
 	}
 	
 	/**
 	 * Signaling informs process about error on peer or during transfer
 	 */
 	public void errorNotification(Exception pCause)
 	{
 		mLogger.warn(this, "Error notification from outside. Terminating.", pCause);
 		terminate(pCause);
 	}
 	
 	public int getID()
 	{
 		if(mProcessID < 0) mLogger.warn(this, "No process ID set. Maybe call to start method is missing.");
 		
 		return mProcessID;
 	}
 	
 	public EventHandler getTimeBase()
 	{
 		return mBase.getEntity().getTimeBase();
 	}
 	
 	public Logger getLogger()
 	{
 		return mLogger;
 	}
 	
 	@Override
 	public String toString()
 	{
		return this.getClass().getSimpleName() +mProcessID +"@" +mBase.getEntity();
 	}
 	
 	public ForwardingNode getBase()
 	{
 		return mBase;
 	}
 	
 	public Identity getOwner()
 	{
 		return mOwner;
 	}
 	
 	public boolean isChangableBy(Identity changer)
 	{
 		if(mOwner != null) {
 			return mOwner.equals(changer);
 		} else {
 			// no owner; access not limited
 			return true;
 		}
 	}
 	
 	public Exception getTerminationCause()
 	{
 		return mTerminationCause;
 	}
 
 	protected void register(int processID)
 	{
 		mProcessID = processID;
 		
 		if(mProcessID >= 0) {
 			mBase.getEntity().getProcessRegister().registerProcess(mBase, this);
 		}
 	}
 	
 	public void observeNextStateChange(double maxWaitTimeSec, IContinuation<Process> continuation)
 	{
 		if(continuation != null) {
 			if(mContinuationsStateChange == null) {
 				mContinuationsStateChange = new ContinuationHandler<Process>(getTimeBase(), maxWaitTimeSec, this);
 			}
 			
 			mContinuationsStateChange.add(continuation);
 		}
 	}
 
 	/**
 	 * @return State of process (!=null)
 	 */
 	public ProcessState getState()
 	{
 		return mState;
 	}
 	
 	protected synchronized void setState(ProcessState newState)
 	{
 		if(newState != null) {
 			// set new state
 			mState = newState;
 			
 			// notify observer
 			if(mContinuationsStateChange != null) {
 				// invalidate handler before calling continuations in order to
 				// allow them to register again
 				ContinuationHandler<Process> handler = mContinuationsStateChange;
 				mContinuationsStateChange = null;
 				
 				handler.success(this);
 			}
 		} else {
 			throw new RuntimeException(this +" - can not set state due to null pointer argument");
 		}
 	}
 	
 	public boolean isOperational()
 	{
 		return mState == ProcessState.OPERATING;
 	}
 	
 	public boolean isFinished()
 	{
 		return mState == ProcessState.CLOSING;
 	}
 
 	/**
 	 * Is called only once when process is finishing.
 	 * Methods overriding is, must call {@code super.finished()}.
 	 */
 	protected void finished()
 	{
 		mTimer.cancel();
 		
 		synchronized(this) {
 			mBase.getEntity().getProcessRegister().unregisterProcess(mBase, this);
 		}
 		
 		// do not set mBase to null, because others might need
 		// it for the finishing process
 	}
 	
 	public enum ProcessState { INIT, STARTING, OPERATING, CLOSING }
 	
 	@Viewable("Process ID")
 	private int mProcessID;
 	
 	@Viewable("Responsable FN")
 	private ForwardingNode mBase;
 	
 	@Viewable("Owner")
 	private Identity mOwner;
 	
 	@Viewable("State")
 	private ProcessState mState;
 	
 	@Viewable("Timer")
 	private Timer mTimer;
 	
 	private Exception mTerminationCause = null;
 	private ContinuationHandler<Process> mContinuationsStateChange = null;
 	protected Logger mLogger;
 }
