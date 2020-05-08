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
 
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ControlEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is implemented as thread. It is responsible for clustering tasks of an HRMController.
  */
 public class ClustererThread extends Thread
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
 	 * Stores a log about "update" events
 	 */
 	private String mDescriptionClusterUpdates = new String();
 	
 	/**
 	 * Stores the number of update requests
 	 */
 	private int mNumberUpdateRequests = 0;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pHRMController the HRMController instance
 	 */
 	ClustererThread(HRMController pHRMController)
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
 	public synchronized void eventUpdateCluster(ControlEntity pCause, HierarchyLevel pHierarchyLevel)
 	{
 		if(pHierarchyLevel.getValue() <= HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 			Logging.log(this, "\n\n################ CLUSTERING TRIGGERED at hierarchy level: " + pHierarchyLevel.getValue());
 			mPendingClusterRequests[pHierarchyLevel.getValue()]++;
 			
 			mDescriptionClusterUpdates += "\n [" + mNumberUpdateRequests + "]: (L" + pHierarchyLevel.getValue() + ") <== " + pCause; 
 			mNumberUpdateRequests++;
 			
 			// trigger wake-up
 			notify();
 		}
 	}
 
 	/**
 	 * Returns the next "cluster event" (uses passive waiting)
 	 * 
 	 * @return the next cluster event
 	 */
 	private synchronized int getNextClusterEvent()
 	{
 		while(true){
 			for(int i = 0; i < HRMConfig.Hierarchy.HEIGHT; i++){
 				if(mPendingClusterRequests[i] > 0){
 					mPendingClusterRequests[i]--;
 					return i;
 				}
 			}
 			
 			// suspend until next trigger
 			try {
 				wait();
 			} catch (InterruptedException tExc) {
 				Logging.warn(this, "getNextClusterEvent() got an interrupt", tExc);
 			}
 		}
 	}
 	
 	/**
 	 * Implements the actual clustering
 	 * 
 	 * @param pHierarchyLevel the hierarchy level for clustering
 	 */
 	private void cluster(int pHierarchyLevel)
 	{
 		if(pHierarchyLevel <= HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 			Logging.log(this, "\n\n################ CLUSTERING STARTED at hierarchy level: " + pHierarchyLevel);
 
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
						return;
					}
 				}
 				
 				/**
 				 * Distribute membership requests
 				 */
 				tTargetCluster.distributeMembershipRequests();
 				
 			}else{
 				Logging.err(this, "cluster() cannot start for a hierarchy level  of: " + pHierarchyLevel);
 			}
 		}else{
 			Logging.warn(this, "cluster() canceled clustering because height limitation is reached at level: " + pHierarchyLevel);
 		}
 	}
 	
 	/**
 	 * main function
 	 */
 	public void run()
 	{
 		Thread.currentThread().setName("Clusterer@" + mHRMController);
 
 		while(true){
 			/**
 			 * Get the next cluster event
 			 */
 			int tNextClusterEvent = getNextClusterEvent();
 			
 			
 			/**
 			 * Process the next cluster event
 			 */
 			cluster(tNextClusterEvent);
 		}
 	}
 
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mHRMController;
 	}
 }
