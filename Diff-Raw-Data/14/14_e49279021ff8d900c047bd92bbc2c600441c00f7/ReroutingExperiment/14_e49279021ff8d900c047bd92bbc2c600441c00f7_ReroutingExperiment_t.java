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
 package de.tuilmenau.ics.fog.packets.statistics;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 import java.util.Random;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.application.ReroutingExecutor.ReroutingSession;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.scripts.RerouteScript;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.IAutonomousSystem;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.topology.SimulationEventHandler;
 import de.tuilmenau.ics.fog.transfer.Gate;
 import de.tuilmenau.ics.fog.transfer.manager.NodeUp;
 import de.tuilmenau.ics.fog.transfer.manager.Controller.RerouteMethod;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.middleware.JiniHelper;
 
 
 public class ReroutingExperiment implements IRerouteMaster, IPacketStatistics, SimulationEventHandler
 {
 	/**
 	 * Event occuring in the experiment
 	 * 
 	 */
 	public class ExperimentEvent implements Serializable
 	{
 		private static final long serialVersionUID = 1910255559352678071L;
 		
 		public final int type; // the type of this event instance
 
 		private final String[] names = { "FINISHED", "IMPOSSIBLE" };
 		public static final int FINISHED = 0;
 		public static final int IMPOSSIBLE = 1; // event sent if the experiment cannot be executed because a start or end node would have to be broken
 		
 		public ExperimentEvent(int t) 
 		{
 			type = t;
 		}
 		
 		@Override
 		public String toString()
 		{
 			return "ExperimentEvent("+(names[type]+")");
 		}
 	}
 		
 	public final static int BROKEN_TYPE_NOTHING = 0;
 	public final static int BROKEN_TYPE_NODE = 1;
 	public final static int BROKEN_TYPE_BUS = 2;
 	public final static int BROKEN_TYPE_AT_WILL = 3;
 	
 	private int mBrokenType = BROKEN_TYPE_NOTHING;
 	private int mConcurrentBrokenType;
 	private String mBrokenName = null;
 	private float mPosition = 2;
 	private int mAssertPosition;
 	private int mUnregisterLinkCounter = 0;
 	private int mUnregisterNodeCounter = 0;
 	private boolean mProcessingFromBroken = false;
 	
 	private RerouteMethod mRerouteMethod;
 	
 	private int mStep = 0;
 	private RerouteScript mScript;
 	private String mSource;
 	private String mTarget;
 	private int mCount = 50;
 	private NameMappingService mNMS;
 	private Random mRandomGenerator = new Random();
 	private Packet mCurrentlyReceived;
 	private ReroutingSession mConcurrentReroutingSession;
 	private Logger mLogger = null;
 	private LinkedList<Name> mFailedNeighbours;
 	
 	public ReroutingExperiment(RerouteScript pScript, String pSource, String pTarget)
 	{
 		mNMS = HierarchicalNameMappingService.getGlobalNameMappingService();
 		mScript = pScript;
 		mSource = pSource;
 		mTarget = pTarget;
 		mRerouteMethod = RerouteMethod.LOCAL;
 		mFailedNeighbours = new LinkedList<Name>();
 		
 		mLogger = pScript.getLogger();
 		
 		pScript.getAS().getSimulation().subscribe(this);
 	}
 	
 	private Logger getLogger()
 	{
 		return mLogger;
 	}
 	
 	public void setBroken(int pType)
 	{	
 		getLogger().debug(this, "RerouteScript set element broken");
 	    mBrokenType = pType;
 	    if(mBrokenType != BROKEN_TYPE_AT_WILL) {
 	    	mConcurrentBrokenType = pType; 
 	    } else {
 		    mConcurrentBrokenType = (mRandomGenerator.nextBoolean()) ? BROKEN_TYPE_BUS : BROKEN_TYPE_NODE;
 		}
 	}
 	
 	public int getCurrentBrokenType()
 	{
 		return mBrokenType;
 	}
 	
 	public void setCount(int pCount)
 	{	
 		mCount = pCount;
 		getLogger().info(this, "This is Rerouting Experiment Number " + this.mCount);
 	}
 	
 	/**
 	 * @return Rerouting method, type of broken element, name of broken element
 	 */
 	@Override
 	public LinkedList<String> getStats()
 	{
 		getLogger().debug(this, "getting statistics (" +mConcurrentBrokenType +", " +mBrokenName +")");
 		
 		LinkedList<String> tStats = new LinkedList<String>();
 		tStats.add(Integer.toString(mRerouteMethod.ordinal()));
 		
 		int tBrokenType;
 		if (mStep==1) {
 			tBrokenType = BROKEN_TYPE_NOTHING;
 		} else {
 			tBrokenType = mConcurrentBrokenType;
 		}
 		tStats.add(Integer.toString(tBrokenType));
 		tStats.add(mBrokenName);
 		return tStats;
 	}
 	
 	public synchronized void reconstruct()
 	{
 		getLogger().log(this, "Completed execution of necessary reconstruction steps");
 		sendPacket(true,false);
 		return;
 	}
 	
 	public void executeNextStep()
 	{
 		ReroutingSession tSession = mConcurrentReroutingSession;
 		if(tSession == null) {
 			mLogger.log(this, "--------------------------ESTABLISHING CONNECTION------------------------");
 			try {
 				mConcurrentReroutingSession = mScript.getSourceAS().establishConnection(mSource, mTarget);
 			}
 			catch(RemoteException exc) {
 				mLogger.warn(this, "Can not establish connection because AS not reachable.", exc);
 				mConcurrentReroutingSession = null;
 			}
 			
 			// connection created?
 			if(mConcurrentReroutingSession == null) {
				terminateFailedExperiment("Connection can not be established. Maybe rerouting application not running?");
 			} else {
 				// connection failed?
 				if(mConcurrentReroutingSession.isStopped()) {
					terminateFailedExperiment("Session stopped.");
 				}
 			}
 			
 			mLogger.log(this, "Rerouting session is now " + mConcurrentReroutingSession);
 			return;
 		}
 		getLogger().debug(this, "Determining next step for " +mStep);
 		try {
 			switch (++mStep) {
 			case 1:
 				mLogger.log(this, "step "  + mStep + ":-------------------------REFERENCE--------------------");
 				if(JiniHelper.isEnabled()) {
 					JiniHelper.registerService(IRerouteMaster.class, this, "RerouteMaster_" + mCount);
 				}
 				/*
 				 * OK, we have been able to determine a valid element for break down, therefore we send a reference packet: push, send, ack
 				 */
 				getLogger().log(this, "Sending packet from " + mSource + " to " + mTarget + " while broken element is " + mBrokenName + " and reroute method is global ");
 				sendPacket(false,false);
 				break;
 			case 2:
 				if(!determineElementToBreak()) {
 					// stop this experiment run as it is impossible
					terminateFailedExperiment("No breakable element.");
 				} else {
 					mFailedNeighbours.clear();
 					breakElement();
 					mRerouteMethod = RerouteMethod.LOCAL;
 					mLogger.log(this, "Now executing experiment for local rerouting from " + mSource +" to " + mTarget + " while broken element is " + mBrokenName);
 					/*
 					 * push, send, ack
 					 */
 					mLogger.log(this, "step "  + mStep + ":-------------------------LOCAL--------------------");
 
 					sendPacket(false,true);
 				}
 				break;
 			case 3:
 				if(queryValidExperiment()) {
 					mLogger.log(this, "step "  + mStep + ":-------------------------REPAIRING--------------------");
 					repairElement();
 				}
 				break;
 			case 4:
 				if(queryValidExperiment()) {
 					mLogger.log(this, "step "  + mStep + ":-------------------------RECONSTRUCTING--------------------");
 					reconstruct();
 					/*
 					 * push, send, ack
 					 */
 				}
 				break;
 			case 5:
 				if(queryValidExperiment()) {
 					mFailedNeighbours.clear();
 					breakElement();
 					mUnregisterLinkCounter = 0;
 					mUnregisterNodeCounter = 0;
 					mRerouteMethod = RerouteMethod.FROM_BROKEN;
 					mProcessingFromBroken = true;
 					mLogger.log(this, "Now executing experiment for rerouting from broken node from " + mSource +" to " + mTarget + " while broken element is " + mBrokenName+ " and reroute method is local ");
 					/*
 					 * push, send, ack
 					 */
 					mLogger.log(this, "step "  + mStep + ":-------------------------FROM_BROKEN--------------------");
 					sendPacket(false,true);
 				}
 				break;
 			case 6:
 				if(queryValidExperiment()) {
 					mUnregisterLinkCounter = 0;
 					mUnregisterNodeCounter = 0;
 					mProcessingFromBroken = false;
 					mLogger.log(this, "step "  + mStep + ":-------------------------REPAIRING--------------------");
 					repairElement();
 				}
 				break;
 			case 7:
 				if(queryValidExperiment()) {
 					mLogger.log(this, "step "  + mStep + ":-------------------------RECONSTRUCTING--------------------");
 					reconstruct();
 					/*
 					 * push, send, ack
 					 */
 				}
 				break;
 			case 8:
 				if(queryValidExperiment()) {
 					mFailedNeighbours.clear();
 					breakElement();
 					mRerouteMethod = RerouteMethod.GLOBAL;
 					mLogger.log(this, "Now executing experiment for global rerouting from " + mSource +" to " + mTarget + " while broken element is " + mBrokenName+ " and reroute method is from detector ");
 					/*
 					 * push, send, ack
 					 */
 					mLogger.log(this, "step "  + mStep + ":-------------------------GLOBAL--------------------");
 
 					sendPacket(false,true);
 				}
 				break;
 			case 9:
 				if(queryValidExperiment()) {
 					mLogger.log(this, "step "  + mStep + ":-------------------------REPAIRING--------------------");
 					repairElement();
 				}
 				break;
 			case 10:
 				if(queryValidExperiment()) {
 					mLogger.log(this, "step "  + mStep + ":-------------------------RECONSTRUCTING--------------------");
 					reconstruct();
 					/*
 					 * push, send, ack
 					 */
 					sendPacket(true,false);
 				} //valid experiment ?
 				break;
 			case 11:
 				getLogger().debug(this, "Finishing experiment");
 				mScript.getAS().getSimulation().unsubscribe(this); // we're done and no longer interested in the events
 				// decouple this instance's FINISHED event and the current method via the central event loop
 				mScript.getAS().getSimulation().getTimeBase().scheduleIn(0.1, new IEvent() {
 					@Override
 					public void fire() {
 						mScript.getAS().getSimulation().publish(new ExperimentEvent(ExperimentEvent.FINISHED));
 					}
 				});
 				break;
 			default:
 				getLogger().err(this, "ReroutingExperiment#executeNextStep() got called too often! Ignoring.");
 				break;
 			}
 		} catch (RemoteException tExc) {
 			getLogger().err(this, "Unable to destroy or repair given object", tExc);
 		}
 		
 	}
 	
 	@Override
 	public String getTarget()
 	{
 		return mTarget;
 	}
 
 	@Override
 	public String getSource()
 	{
 		return mSource;
 	}
 	
 	public void initiate()
 	{
 		if(mCount > 0) {
 			executeNextStep();
 		}
 	}
 	
 	/**
 	 * Send packet with the following four possible options
 	 * 
 	 * false false to send a packet that is neither meant for reestablishment nor contains rerouting information
 	 * false true to send a packet that is not meant for reestablishment but contains rerouting information
 	 * true false to send a packet that is only meant for reestablishment of routes but does not contain rerouting firomation
 	 * true true to send a packet that is meant for reestablishment of routes and contains rerouting information
 	 * 
 	 * Of all mentioned types only the first possibilities make sense.
 	 * 
 	 * @param signal
 	 * @param pRerouteInformation
 	 */
 	private void sendPacket(boolean signal, boolean pRerouteInformation)
 	{
 		ReroutingTestAgent packet = null;
 		packet = new ReroutingTestAgent();
 		if(!signal) {
 			packet.setBrokenName(mBrokenName);
 			packet.setRerouteMethod(mRerouteMethod);
 			if(pRerouteInformation) {
 				packet.setBrokenType(mConcurrentBrokenType);
 			} else {
 				/*
 				 * this distinction is needed for parse_stats.py script
 				 */
 				packet.setBrokenType(BROKEN_TYPE_NOTHING);
 			}
 		}
 		getLogger().debug(this, "sending packet " +( signal ? "to recreate original routes " : "to test rerouting with broken name " + mBrokenName + " and source to target pair " + mSource + "->" + mTarget));
 
 		packet.setStep(mStep);
 		packet.setCount(mCount);
 		packet.setDestNode(mTarget);
 		packet.setSourceNode(mSource);
 		
 		try {
 			if(mCurrentlyReceived != null) {
 				getLogger().log(this, "Ordering AS " + mScript.getSourceAS().toString() + " to send reroute experiment from " + mSource + " to " + mTarget + " (last route:" + mCurrentlyReceived.getAuthentications().toString() + ")");
 			}
 			
 			if(signal) {
 				packet.dontLog();
 			}
 			
 			mConcurrentReroutingSession.sendData(packet);
 		} catch(RemoteException rExc) {
 			getLogger().err(this, "Warning! Unable to send packet", rExc);
 		} catch(NetworkException rExc) {
 			getLogger().err(this, "Warning! Unable to send packet", rExc);
 		}
 	}
 
 	/**
 	 * Calculate mBrokenName.
 	 * @param pPacket packet from first step where nothing was broken
 	 */
 	private boolean determineElementToBreak()
 	{
 		mAssertPosition = -1;
 		mBrokenName = null;
 		
 		/**
 		 * --- BROKEN NODE ---
 		 */
 		if(mConcurrentBrokenType == BROKEN_TYPE_NODE) {
 			LinkedList<Signature> tNodes = new LinkedList<Signature>();
 			if(mCurrentlyReceived != null) {
 				for (Signature signature: mCurrentlyReceived.getAuthentications()) {
 					tNodes.add(signature);
 				}
 			} else {
 				return false;
 			}
 			for(Signature signature: tNodes) {
 				mLogger.log(this, "may choose from node element " + signature.toString());
 			}
 			if(mPosition != 0) {
 				if( (int)(tNodes.size() / mPosition) == 0 || (int)(tNodes.size() / mPosition) == tNodes.size() - 1) {
 					getLogger().log(this, "Breaking node in the middle of the route.");
 					mAssertPosition = (int) Math.floor((double)tNodes.size() /2);
 				} else {
 					mAssertPosition = (int) Math.floor(((double)tNodes.size()-1)/mPosition);
 				}
 				mBrokenName = tNodes.get(mAssertPosition).getIdentity().getName();
 			} else {
 				getLogger().log(this, "Breaking random node");
 				mBrokenName = tNodes.get(mRandomGenerator.nextInt(tNodes.size()-2)+1).getIdentity().getName();
 			}
 			if(mBrokenName.equals(mSource) || mBrokenName.equals(mTarget)) {
 				mBrokenName = null;
 				getLogger().err(this, "The position you specified would break source or destination, skipping");
 				return false;
 			}
 		}
 		/**
 		 * --- BROKEN BUS ---
 		 */
 		else if(mConcurrentBrokenType == BROKEN_TYPE_BUS) {
 			LinkedList<String> tBusses = null;
 			if(mCurrentlyReceived != null) {
 				tBusses = mCurrentlyReceived.getBus();
 			} else {
 				return false;
 			}
 			
 			if(mPosition != 0) {
 				if( (int)(tBusses.size() / mPosition) == 0 || (int)(tBusses.size() / mPosition) == tBusses.size() - 1) {
 					getLogger().log(this, "Breaking bus in the middle of the route");
 					mAssertPosition = tBusses.size()/2;
 				} else {
 					mAssertPosition = (int) ((tBusses.size()-1)/mPosition);
 				}
 			} else {
 				// every bus may break down because source or destination could have more than one link
 				getLogger().log(this, "Breaking random bus");
 				mAssertPosition = mRandomGenerator.nextInt(tBusses.size());
 			}
 			
 			try {
 				mBrokenName = tBusses.get(mAssertPosition);
 			} catch (IndexOutOfBoundsException tExc) {
 				mLogger.warn(this, "Unable to determine element to break");
 			}
 		}
 		
 		getLogger().debug(this, "Determined element to break: " + mBrokenName);
 		return true;
 	}
 	
 	private synchronized boolean breakElement() throws RemoteException
 	{
 		return changeElement(true);
 	}
 	
 	private synchronized boolean repairElement() throws RemoteException
 	{
 		return changeElement(false);
 	}
 	
 	private synchronized boolean changeElement(boolean breakIt) throws RemoteException
 	{
 		boolean execution = false;
 		
 		if (mBrokenName == null) return false;
 		
 		if (mConcurrentBrokenType == BROKEN_TYPE_NODE) {
 			if(JiniHelper.isEnabled()) {
 				IAutonomousSystem as = (IAutonomousSystem) JiniHelper.getService(IAutonomousSystem.class, mNMS.getASNameByNode(mBrokenName)); 
 				execution = as.setNodeBroken(mBrokenName, breakIt, Config.Routing.ERROR_TYPE_VISIBLE);
 			} else {
 				for(IAutonomousSystem tAS : mScript.getSimulation().getAS()) {
 					String tASName = mNMS.getASNameByNode(mBrokenName);
 					if(tAS.getName().equals(tASName)) {
 						execution = tAS.setNodeBroken(mBrokenName, breakIt, Config.Routing.ERROR_TYPE_VISIBLE);
 					}
 				}
 			}
 		} else if (mConcurrentBrokenType == BROKEN_TYPE_BUS) {
 			if(JiniHelper.isEnabled()) {
 				ILowerLayer ll = (ILowerLayer) JiniHelper.getService(ILowerLayer.class, mBrokenName);
 				IAutonomousSystem as = (IAutonomousSystem) JiniHelper.getService(IAutonomousSystem.class, ll.getASName()); 
 				execution = as.setBusBroken(mBrokenName, breakIt, Config.Routing.ERROR_TYPE_VISIBLE);
 			} else {
 				LinkedList<IAutonomousSystem> tASs = mScript.getSimulation().getAS();
 				for(IAutonomousSystem tAS : tASs) {
 					/*
 					 * as it is a local simulation we may cast the interface to autonomous system 
 					 */
 					ILowerLayer ll = ((AutonomousSystem)tAS).getBusByName(mBrokenName);
 					if(ll != null) {
 						execution = tAS.setBusBroken(mBrokenName, breakIt, Config.Routing.ERROR_TYPE_VISIBLE);
 					}
 				}
 			}
 		}
 
 		if(breakIt) {
 			getLogger().debug(this, (execution ? "successfully" : "unsuccessfully") + " broke element " + mBrokenName + " in turn " + mCount + " at step " + mStep);
 			return true;
 		} else {
 			getLogger().debug(this, (execution ? "successfully" : "unsuccessfully") + " repaired element " + mBrokenName + " in turn " + mCount + " at step " + mStep);
 			return true;
 		}
 	}
 	
	private void terminateFailedExperiment(String errorMsg)
 	{
		mLogger.warn(this, errorMsg +" Terminate current experiment.");
 		
 		mScript.getAS().getSimulation().unsubscribe(this);
 		mScript.getAS().getSimulation().getTimeBase().scheduleIn(0.1, new IEvent() {
 			@Override
 			public void fire() {
 				mScript.getAS().getSimulation().publish(new ExperimentEvent(ExperimentEvent.IMPOSSIBLE));
 			}
 		});
 	}
 
 	public synchronized boolean queryValidExperiment()
 	{
 		return (mBrokenName != null) ? true : false;
 	}
 	
 	public RerouteMethod getRerouteMethod()
 	{
 		return mRerouteMethod;
 	}
 	
 	public void setPositionToBreak(float position)
 	{
 		this.mPosition = position;
 	}
 	
 	protected void finalize()
 	{
         if(JiniHelper.isEnabled()) {
         	JiniHelper.unregisterService(IRerouteMaster.class, this);
         }
     }
 	
 	public class ExperimentNotifier implements IEvent
 	{
 		public ExperimentNotifier(int pStep)
 		{
 			getLogger().debug(this, "Registering next rerouting action at step " + pStep);
 			if(pStep == 0) {
 				getLogger().log(this, "Will now establish a connection");
 			}
 		}
 		
 		public void fire()
 		{
 			executeNextStep();
 		}
 		
 		@Override
 		public String toString()
 		{
 			return this.getClass().getSimpleName();
 		}
 	}
 	
 	public synchronized boolean tell(Packet pPacket) throws RemoteException
 	{
 		getLogger().log(this, "Scheduling execution of next step while current step is " + mStep);
 		mCurrentlyReceived = pPacket;
 		mScript.getSimulation().getTimeBase().scheduleIn(0, new ExperimentNotifier(mStep));
 		return true;
 	}
 
 	@Override
 	public void simulationEvent(Serializable event) 
 	{
 		mLogger.debug(this, "Received simulation event: "+event.toString());
 		if (event instanceof NodeUp) {
 			mScript.getSimulation().getTimeBase().scheduleIn(0, new ExperimentNotifier(mStep));			
 		}
 		if (event instanceof Gate.GateNotification) {
 			Gate.GateNotification ev = (Gate.GateNotification)event;
 			switch (ev.type) {
 			case Gate.GateNotification.LOST_BE_GATE:
 				mFailedNeighbours.add(ev.destination);
 				break;
 			case Gate.GateNotification.GOT_BE_GATE:
 				if (mFailedNeighbours.remove(ev.destination)) {
 					if (mFailedNeighbours.isEmpty()) {
 						mLogger.debug(this, "Bus reconstruction complete. Continuing experiment.");
 						mScript.getSimulation().getTimeBase().scheduleIn(0, new ExperimentNotifier(mStep));					
 					}
 				}
 				break;
 			}
 		}
 	}
 }
