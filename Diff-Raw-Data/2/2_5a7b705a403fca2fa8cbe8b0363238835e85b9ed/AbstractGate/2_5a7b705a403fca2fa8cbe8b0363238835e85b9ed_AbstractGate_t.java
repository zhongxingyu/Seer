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
 package de.tuilmenau.ics.fog.transfer.gates;
 
 import de.tuilmenau.ics.CommonSim.datastream.numeric.CounterNode;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.IDoubleWriter;
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.ContinuationHandler;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.IContinuation;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.Gate;
 import de.tuilmenau.ics.fog.ui.Viewable;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.Timer;
 
 
 public abstract class AbstractGate implements Gate, ForwardingElement
 {
 	protected static final double UNUSED_TIMEOUT_SEC = Config.Transfer.GATE_UNUSED_TIMEOUT_SEC;
 	
 	
 	public AbstractGate(FoGEntity pEntity, Description pDescription, Identity pOwner)
 	{
 		mEntity = pEntity;
 		mState = GateState.START;
 		mRefCounter = 1;
 		mLogger = pEntity.getLogger();
 		mOwner = pOwner;
 		
 		setDescription(pDescription);
 		
 		mLogger.log(this, "created");
 	}
 	
 	/**
 	 * @param pID ID for gate or {@code null} for deleting old ID.
 	 * 
 	 * @return Shows whether method action was executed, in other words
 	 * {@code true} if and only if {@link #getReferenceCounter()}
 	 * equals {@code 1}.
 	 */
 	public final boolean setID(GateID pID)
 	{
 		if(mRefCounter == 1) {
 			setGateID(pID);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @param pID ID for gate or {@code null} for deleting old ID.
 	 */
 	protected void setGateID(GateID pID)
 	{
 		mId = pID;
 	}
 	
 	public GateID getGateID()
 	{
 		return mId;
 	}
 	
 	public FoGEntity getEntity()
 	{
 		return mEntity;
 	}
 	
 	public ForwardingElement getNextNode()
 	{
 		return null;
 	}
 	
 	/**
 	 * @param pReverseGateID The ID for the local partner gate or {@code null}
 	 * for deleting old partner gates ID.
 	 * 
 	 * @return Shows whether method action was executed, in other words
 	 * {@code true} if and only if {@link #getReferenceCounter()}
 	 * equals {@code 1}.
 	 */
 	public final boolean setReverseGateID(GateID pReverseGateID)
 	{
 		if(mRefCounter == 1) {
 			setLocalPartnerGateID(pReverseGateID);
 			return true;
 		}
 		return false;
 	}
 	
 	public void setRemoteDestinationName(Name pRemoteDestinationName)
 	{
 		mRemoteDestinationName = pRemoteDestinationName;
 		// TODO quick&dirty notification of new BE-DirectDownGates for the Rerouting experiment infrastructure
 		if (this instanceof DirectDownGate && mState == GateState.OPERATE && (mDescription == null || mDescription.isBestEffort())) {
 			getEntity().getNode().getAS().getSimulation().publish(new Gate.GateNotification(Gate.GateNotification.GOT_BE_GATE, mRemoteDestinationName));
 		}
 	}
 	
 	public Name getRemoteDestinationName()
 	{
 		return mRemoteDestinationName;
 	}
 	
 	/**
 	 * @param pReverseGateID The ID for the local partner gate or {@code null}
 	 * for deleting old partner gates ID.
 	 */
 	protected void setLocalPartnerGateID(GateID pReverseGateID)
 	{
 		mReverseGateID = pReverseGateID;
 	}
 	
 	public boolean isReverseGateAvailable()
 	{
 		return (mReverseGateID != null);
 	}
 	
 	public GateID getReverseGateID()
 	{
 		return mReverseGateID;
 	}
 	
 	public GateState getState()
 	{
 		return mState;
 	}
 	
 	public boolean isReadyToReceive()
 	{
 		return (mState == GateState.OPERATE) || (mState == GateState.INIT);
 	}
 	
 	public boolean isOperational()
 	{
 		return (mState == GateState.OPERATE);
 	}
 	
 	public boolean isDeleted()
 	{
 		return (mState == GateState.DELETED) || (mState == GateState.SHUTDOWN);
 	}
 
 	
 	public Description getDescription()
 	{
 		return mDescription;
 	}
 	
 	public Identity getOwner()
 	{
 		if(mOwner != null) {
 			return mOwner;
 		}else{
 			return mEntity.getIdentity();
 		}
 	}
 	
 	protected void setDescription(Description pNewDescr)
 	{
 		if (pNewDescr != null){
 			mDescription = pNewDescr.clone();
 		}else{
 			mDescription = null;
 		}
 	}
 	
 	@Override
 	public void waitForStateChange(double maxWaitTimeSec, IContinuation<Gate> continuation)
 	{
 		if(continuation != null) {
 			if(mContinuationsStateChange == null) {
 				mContinuationsStateChange = new ContinuationHandler<Gate>(mEntity.getTimeBase(), maxWaitTimeSec, this);
 			}
 			
 			mContinuationsStateChange.add(continuation);
 		}
 	}
 	
 	public synchronized void setState(GateState newState)
 	{
 		// Check if state transition is allowed.
 		// Loops to the same state are not allowed at all.
 		if( ((mState == GateState.START) && (newState == GateState.INIT)) ||
 			((mState == GateState.INIT) && ((newState == GateState.OPERATE) || (newState == GateState.ERROR) || (newState == GateState.SHUTDOWN))) ||
 			((mState == GateState.OPERATE) && ((newState == GateState.SHUTDOWN) || (newState == GateState.ERROR))) ||
 			((mState == GateState.ERROR) && ((newState == GateState.SHUTDOWN) || (newState == GateState.OPERATE))) ||
 			((mState == GateState.SHUTDOWN) && (newState == GateState.DELETED))
 			)
 		{
 			mLogger.trace(this, "Gate state transition from " +mState +" to " +newState);
 			
 			// switch to new state
 			mState = newState;
 			
 			if(mContinuationsStateChange != null) {
 				// invalidate handler before calling continuations in order to
 				// allow them to register again
 				ContinuationHandler<Gate> tHandler = mContinuationsStateChange;
 				mContinuationsStateChange = null;
 				
 				tHandler.success(this);
 			}
 		} else {
 			throw new RuntimeException("Gate state transition from " +mState +" to " +newState +" is not allowed (" +this +").");
 		}
 	}
 	
 	/**
 	 * If the new state is different from the current one, the gate switches
 	 * to the new one.
 	 * 
 	 * @param newState New state for gate
 	 */
 	protected void switchToState(GateState newState)
 	{
 		if(mState != newState) {
 			setState(newState);
 		}
 	}
 	
 	@Override
	final synchronized public void initialise()
 	{
 		// run init only once
 		if(getState() == GateState.START) {
 			setState(GateState.INIT);
 			
 			IDoubleWriter counter = CounterNode.openAsWriter(getClass().getCanonicalName() +".number");
 			counter.write(+1.0, mEntity.getTimeBase().nowStream());
 			
 			IDoubleWriter sum = CounterNode.openAsWriter(getClass().getCanonicalName() +".sum");
 			sum.write(+1.0, mEntity.getTimeBase().nowStream());
 			
 			try {
 				init();
 			}
 			catch (Exception exc) {
 				mLogger.err(this, "Exception during initialisation.", exc);
 				setState(GateState.ERROR);
 			}
 			catch (Error err) {
 				mLogger.err(this, "Error during initialisation.", err);
 				setState(GateState.ERROR);
 			}
 		}
 	}
 	
 	@Override
 	final public void shutdown()
 	{
 		if(getState() != GateState.DELETED) {
 			if(mRefCounter > 1) {
 				mRefCounter--;
 			} else {
 				mRefCounter = 0;
 				if(getState() != GateState.SHUTDOWN) {
 					// gate is somewhere in INIT, OPERATE or ERROR
 					setState(GateState.SHUTDOWN);
 					
 					IDoubleWriter counter = CounterNode.openAsWriter(getClass().getCanonicalName() +".number");
 					counter.write(-1.0, mEntity.getTimeBase().nowStream());
 
 					try {
 						close();
 					} catch (NetworkException exc) {
 						mLogger.err(this, "Gate shutdown was not successful.", exc);
 					}
 				}
 				// else: already in shutdown mode
 				
 				// did the gate switched directly to DELETED or is some
 				// more time required?
 				if(getState() != GateState.DELETED) {
 					// start timer for deleting internals of gate by force
 					Timer timer = new Timer(mEntity.getTimeBase(), new IEvent() {
 						@Override
 						public void fire()
 						{
 							if(getState() != GateState.DELETED) {
 								mLogger.warn(AbstractGate.this, "Timeout while waiting for DELETED. Delete by force.");
 								delete();
 							}
 						}
 					}, Config.Transfer.GATE_STD_TIMEOUT_SEC);
 					timer.start();
 				}
 			}
 			// else: existing other references; must not be deleted
 		}
 		// else: already deleted; nothing to do
 	}
 	
 	/**
 	 * Command for the gate to initialize its internal state. This might
 	 * include sending messages to its peers.
 	 */
 	protected abstract void init() throws NetworkException;
 	
 	/**
 	 * The default implementation returns always true. However,
 	 * it depends on the private flag of forwarding nodes if a
 	 * gate is reported to the routing. 
 	 * 
 	 * @return Indicates if a gate should be known by the transfer service only. 
 	 */
 	public boolean isPrivateToTransfer()
 	{
 		return false;
 	}
 
 	@Override
 	public int getNumberMessages(boolean reset)
 	{
 		int res = mMsgCounter;
 		
 		if(reset) mMsgCounter = 0;
 		
 		return res; 
 	}
 	
 	protected void incMessageCounter()
 	{
 		mMsgCounter++;
 	}
 	
 	/**
 	 * Command for the gate to check and update its internal state.
 	 */
 	public void refresh()
 	{
 		// dummy
 	}
 	
 	/**
 	 * Command for the gate to shutdown and inform its peers.
 	 */
 	protected void close() throws NetworkException
 	{
 		delete();
 	}
 	
 	/**
 	 * Clean-up internal attributes
 	 */
 	protected void delete()
 	{
 		if(getState() != GateState.DELETED) {
 			setState(GateState.DELETED);
 		}
 	}
 	
 	/**
 	 * Is called by timer if gate enters idle state.
 	 */
 	protected void idleEntered()
 	{
 		setState(GateState.ERROR);
 	}
 	
 	private class UnusedGateTimeout implements IEvent
 	{
 		public UnusedGateTimeout()
 		{
 			mPacketCounter = getNumberMessages(false);
 			mLastCheck = false;
 		}
 		
 		@Override
 		public void fire()
 		{
 			if(getState() != GateState.DELETED) {
 				// do not check for "<" since somebody might have reseted the counter
 				if(getNumberMessages(false) == mPacketCounter) {
 					if(mLastCheck) {
 						mLogger.log(AbstractGate.this, "Timeout and packet counter unchanged for the second time: Gate is idle.");
 						
 						idleEntered();
 						
 						// do not schedule the timer again
 						return;
 					} else {
 						mLastCheck = true;
 					}
 				} else {
 					mPacketCounter = getNumberMessages(false);
 					mLastCheck = false;
 				}
 					
 				schedule();
 			}
 			// else: do not schedule again
 		}
 		
 		public void schedule()
 		{
 			mEntity.getTimeBase().scheduleIn(AbstractGate.UNUSED_TIMEOUT_SEC, this);
 		}
 		
 		private int mPacketCounter;
 		private boolean mLastCheck;
 	}
 
 	/**
 	 * Starts process for supervising gate if it is no longer in use.
 	 */
 	public void startCheckForIdle()
 	{
 		if(mTimeout == null) {
 			mTimeout = new UnusedGateTimeout();
 			mTimeout.schedule();
 		}
 	}
 	
 	/**
 	 * @return The number of processes using this gate in their paths.
 	 * <br/><br/>
 	 * It is
 	 * really the number of accessing processes and not the total occurrence
 	 * of this gate in arbitrary paths. Every process counts as {@code 1},
 	 * independent of possible multiple participations of this gate in their
 	 * paths.
 	 */
 	public int getReferenceCounter()
 	{
 		return mRefCounter;
 	}
 	
 	public String toString()
 	{
 		StringBuffer tOut = new StringBuffer(128);
 		
 		tOut.append(this.getClass().getSimpleName());
 		
 		if(mId != null) {
 			tOut.append("(");
 			tOut.append(mId);
 			
 			// append state only if it is important
 			if(!isReadyToReceive()) {
 				tOut.append(";");
 				tOut.append(mState);
 			}
 			
 			if(isReverseGateAvailable()) {
 				tOut.append(";");
 				tOut.append(mReverseGateID);
 			}
 			
 			tOut.append(")");
 		}
 		
 		tOut.append("@");
 		tOut.append(mEntity);
 		
 		return tOut.toString();
 	}
 	
 	@Viewable("Gate number")
 	private GateID mId;
 	
 	@Viewable("Reverse gate number")
 	private GateID mReverseGateID;
 	
 	@Viewable("Remote destination name")
 	private Name mRemoteDestinationName = null;
 	
 	@Viewable("State")
 	private GateState mState;
 	
 	// description will be shown in detail view explicitly
 	// in order to distinguish between empty list and null
 	private Description mDescription;
 	
 	@Viewable("Owner")
 	private Identity mOwner;
 	
 	@Viewable("Reference counter")
 	protected int mRefCounter = 0;
 	
 	@Viewable("Processed messages")
 	private int mMsgCounter = 0;
 	
 	@Viewable("FoG entity")
 	protected FoGEntity mEntity;
 	
 	protected Logger mLogger;
 	
 	private ContinuationHandler<Gate> mContinuationsStateChange = null;
 	private UnusedGateTimeout mTimeout = null;
 }
