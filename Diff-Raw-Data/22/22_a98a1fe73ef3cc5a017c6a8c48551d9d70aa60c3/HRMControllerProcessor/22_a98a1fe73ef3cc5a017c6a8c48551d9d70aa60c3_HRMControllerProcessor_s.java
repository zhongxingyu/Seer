 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical;
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ComChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ControlEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is implemented as thread. It is responsible for clustering tasks and packet processing of an HRMController.
  */
 public class HRMControllerProcessor extends Thread
 {
 	/**
 	 * Stores the HRMController reference
 	 */
 	private HRMController mHRMController = null;
 	
 	/**
 	 * Stores how many pending request do exist per hierarchy level
 	 */
 	private int[] mPendingClusterRequests = new int[HRMConfig.Hierarchy.HEIGHT]; 
 
 	/**
 	 * Stores pending requests for packet processing
 	 */
 	private LinkedList<ComChannel> mPendingPacketRequests = new LinkedList<ComChannel>();
 	
 	/**
 	 * Stores pending requests for hierarchy priority update processing 
 	 */
 	private LinkedList<HierarchyLevel> mPendingHierarchyUpdates = new LinkedList<HierarchyLevel>();
 	
 	/**
 	 * Stores a log about "update" events
 	 */
 	private String mDescriptionClusterUpdates = new String();
 	
 	/**
 	 * Stores the number of update requests
 	 */
 	private int mNumberUpdateRequests = 0;
 	
 	/**
 	 * Allow to exit the processor thread
 	 */
 	private boolean mProcessorNeeded = true;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pHRMController the HRMController instance
 	 */
 	HRMControllerProcessor(HRMController pHRMController)
 	{
 		mHRMController = pHRMController;
 		Logging.log(this, "##### Created clusterer thread for: " + mHRMController);
 	}
 	
 	/**
 	 * Returns a log about "update cluster" events
 	 */
 	public String getGUIDescriptionClusterUpdates()
 	{
 		return mDescriptionClusterUpdates;
 	}
 	
 	/**
 	 * EVENT: "update cluster" for a given hierarchy level
 	 * 
 	 * @param pCause the causing control entity
 	 * @param pHierarchyLevel the hierarchy level where a clustering should be done
 	 */
 	private long mEventUpdateCluster = 0;
 	public synchronized void eventUpdateCluster(ControlEntity pCause, HierarchyLevel pHierarchyLevel)
 	{
 		mEventUpdateCluster++;
 		
 		if(mProcessorNeeded){
 			if(pHierarchyLevel.getValue() <= HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 				Logging.log(this, "\n\n################ CLUSTERING EVENT TRIGGERED at hierarchy level: " + pHierarchyLevel.getValue() + ", cause=" + pCause);
 				mPendingClusterRequests[pHierarchyLevel.getValue()]++;
 				
 				mDescriptionClusterUpdates += "\n [" + mNumberUpdateRequests + "]: (L" + pHierarchyLevel.getValue() + ") <== " + pCause; 
 				mNumberUpdateRequests++;
 				
 				// trigger wake-up
				Logging.log(this, "Notify - [" + mEventUpdateCluster + "] - eventUpdateCluster(" + pCause + ", " + pHierarchyLevel + ")");
 				notify();
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "received packet"
 	 * 
 	 * @param pComChannel the comm. channel which received a new packet
 	 */
 	private long mEventReceivedPacket = 0;
 	public synchronized void eventReceivedPacket(ComChannel pComChannel)
 	{
 		mEventReceivedPacket++;
 		
 		synchronized (mPendingPacketRequests) {
 			mPendingPacketRequests.add(pComChannel);
 		}
 
 		// trigger wake-up
		Logging.log(this, "Notify - [" + mEventReceivedPacket + "] - eventReceivedPacket(" + pComChannel + ")");
 		notify();
 	}
 
 	/**
 	 * EVENT: "new hierarchy priority"
 	 * 
 	 * @param pHierarchyLevel the hierarchy level
 	 */
 	private long mEventNewHierarchyPriority = 0;
 	public synchronized void eventNewHierarchyPriority(HierarchyLevel pHierarchyLevel)
 	{
 		mEventNewHierarchyPriority++;
 		
 		synchronized (mPendingHierarchyUpdates) {
 			mPendingHierarchyUpdates.add(pHierarchyLevel);
 		}
 
 		// trigger wake-up
		Logging.log(this, "Notify - [" + mEventNewHierarchyPriority + "] - eventNewHierarchyPriority(" + pHierarchyLevel + ")");
 		notify();
 	}
 
 	/**
 	 * Returns the next "cluster event" (uses passive waiting)
 	 * 
 	 * @return the next cluster event (a hierarchy level)
 	 */
 	private synchronized int getNextClusterEvent()
 	{
 		for(int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 			if(mPendingClusterRequests[i] > 0){
 				mPendingClusterRequests[i]--;
 				return i;
 			}
 		}
 		
 		return -1;
 	}
 
 	/**
 	 * Returns the next comm. channel which has pending packet data
 	 * 
 	 * @return the next comm. channel
 	 */
 	private synchronized ComChannel getNextComChannel()
 	{
 		ComChannel tResult = null;
 		
 		synchronized (mPendingPacketRequests) {
 			if(mPendingPacketRequests.size() > 0){
 				tResult = mPendingPacketRequests.removeFirst();
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the next hierarchy level which has pending priority updates
 	 * 
 	 * @return the next hierarchy level
 	 */
 	private synchronized HierarchyLevel getNextHierarchyLevelForPriorityUpdate()
 	{
 		HierarchyLevel tResult = null;
 		
 		synchronized (mPendingHierarchyUpdates) {
 			if(mPendingHierarchyUpdates.size() > 0){
 				tResult = mPendingHierarchyUpdates.removeFirst();
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Implements the actual clustering
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for clustering
 	 */
 	private void cluster(int pHierarchyLevel)
 	{
 		if(pHierarchyLevel <= HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 			if((pHierarchyLevel >= 0) && (pHierarchyLevel < HRMConfig.Hierarchy.HEIGHT)){
 				// search for an existing cluster at this hierarchy level
 				Cluster tTargetCluster = mHRMController.getCluster(pHierarchyLevel);
 				
 				/**
 				 * Create a new superior cluster if none does already exist
 				 */
 				if(tTargetCluster == null){
 					// does a local inferior coordinator exist?
 					if(!mHRMController.getAllCoordinators(pHierarchyLevel - 1).isEmpty()){
 						tTargetCluster = Cluster.create(mHRMController, new HierarchyLevel(this, pHierarchyLevel), Cluster.createClusterID());
 					}else{
 						Logging.log(this, "No local inferior coordinator found, skipping clustering request at hierarchy level: " + pHierarchyLevel);						
 					}
 				}
 				
 				/**
 				 * Distribute membership requests
 				 */
 				if(tTargetCluster != null){
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "\n\n################ CLUSTERING STARTED at hierarchy level: " + pHierarchyLevel);
 					}
 					tTargetCluster.distributeMembershipRequests();
 				}
 				
 			}else{
 				Logging.err(this, "cluster() cannot start for a hierarchy level  of: " + pHierarchyLevel);
 			}
 		}else{
 			Logging.warn(this, "cluster() canceled clustering because height limitation is reached at level: " + pHierarchyLevel);
 		}
 	}
 	
 	private synchronized void waitForNextEvent()
 	{
 		// suspend until next trigger
 		try {
 			wait();
 			//Logging.log(this, "WakeUp");
 		} catch (InterruptedException tExc) {
 			Logging.warn(this, "waitForNextEvent() got an interrupt", tExc);
 		}
 	}
 
 	/**
 	 * main function
 	 */
 	public void run()
 	{
 		Thread.currentThread().setName("Processor@" + mHRMController);
 
 		while(mProcessorNeeded){
 			boolean tFoundEvent = false;
 			
 			/************************
 			 * Packet processing
 			 ***********************/
 			ComChannel tNextCommChannel = getNextComChannel();
 			while(tNextCommChannel != null){
 				tFoundEvent = true;
 
 				double tBefore = HRMController.getRealTime();
 				
 				// process the next comm. channel data
 				tNextCommChannel.processOnePacket();
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
				Logging.log(this, "Processing a packet took " + tSpentTime + " ms for " + tNextCommChannel);
 				
 				// get the next waiting comm. channel
 				tNextCommChannel = getNextComChannel();
 			}	
 
 			/************************
 			 * Hierarchy priority processing
 			 ***********************/
 			HierarchyLevel tNextHierarchyLevel = getNextHierarchyLevelForPriorityUpdate();
 			while(tNextHierarchyLevel != null){
 				tFoundEvent = true;
 				
 				double tBefore = HRMController.getRealTime();
 
 				// process the next hierarchy priority update
 				mHRMController.distributeHierarchyNodePriorityUpdate(tNextHierarchyLevel);
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				Logging.log(this, "Processing an hierarchy priority update for hier. level " + tNextHierarchyLevel + " took " + tSpentTime + " ms");
 
 				// get the next hierarchy priority update
 				tNextHierarchyLevel = getNextHierarchyLevelForPriorityUpdate();
 			}	
 
 			/***********************
 			 * Clustering
 			 ***********************/
 			int tNextClusterEvent = getNextClusterEvent();
 			if(tNextClusterEvent >= 0){
 				tFoundEvent = true;
 
 				double tBefore = HRMController.getRealTime();
 
 				cluster(tNextClusterEvent);
 
 				double tSpentTime = HRMController.getRealTime() - tBefore;
 
 				Logging.log(this, "Processing a clustering request for hier. level " + tNextClusterEvent + " took " + tSpentTime + " ms");
 			}
 			
 			/**
 			 * Wait for next event
 			 */
 			if(!tFoundEvent){
 				waitForNextEvent();
 			}
 		}
 	}
 
 	public synchronized void exit()
 	{
 		mProcessorNeeded = false;
 		notify();
 	}
 	
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mHRMController;
 	}
 }
