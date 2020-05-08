 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.bus.Bus;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.packets.hierarchical.routing.RouteReport;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingTable;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a cluster head at a defined hierarchy level.
  * At base hierarchy level, multiple Cluster instances may exist. However, at higher hierarchy levels, exactly one Cluster instance may exist.
  * Each Cluster instance may manage an unlimited amount of cluster members (-> ClusterMember).
  */
 public class Cluster extends ClusterMember
 {
 	/**
 	 * For using this class within (de-)serialization.
 	 */
 	private static final long serialVersionUID = -7486131336574637721L;
 
 	/**
 	 * This is the cluster counter, which allows for globally (related to a physical simulation machine) unique cluster IDs.
 	 */
 	public static long sNextFreeClusterID = 1;
 	
 	/**
 	 * Stores a reference to the local coordinator instance if the local router is also the coordinator for this cluster
 	 */
 	private Coordinator mCoordinator = null;
 	
 	/**
 	 * Stores the connect inferior local coordinators.
 	 */
 	private LinkedList<Coordinator> mInferiorLocalCoordinators = new LinkedList<Coordinator>(); //TODO: register and auto-remove if coordinator is invalidated
 	
 	/**
 	 * Stores the connect inferior local coordinators.
 	 */
 	private LinkedList<CoordinatorProxy> mInferiorRemoteCoordinators = new LinkedList<CoordinatorProxy>(); //TODO: register and auto-remove if coordinatorProxy is invalidated
 
 	/**
 	 * Stores a description of former GUICoordinatorIDs
 	 */
 	private String mDescriptionFormerGUICoordinatorIDs = "";
 	
 	/**
 	 * Stores a list of the already used addresses
 	 */
 	private LinkedList<Integer> mUsedAddresses = new LinkedList<Integer>();
 	
 	/**
 	 * Stores a list of the already reserved addresses
 	 */
 	private HashMap<Integer, ComChannel> mAddressReservations = new HashMap<Integer, ComChannel>();
 	
 	/**
 	 * Stores how many address broadcasts were already sent
 	 */
 	private int mSentAddressBroadcast = 0;
 
 	/**
 	 * Stores the local HRMID of the last address distribution event
 	 */
 	private HRMID mHRMIDLastDistribution = null;	
 	
 	/**
 	 * Stores a description about the HRMID allocations
 	 */
 	private String mDescriptionHRMIDAllocation = new String();
 	
 	/**
 	 * Stores the timeout for the next address distribution
 	 */
 	private double mAddressDistributionTimeout = 0;
 
 	/**
 	 * Stores how many clusters were created per hierarchy level
 	 */
 	public static int mCreatedClusters[] = new int[HRMConfig.Hierarchy.HEIGHT];
 
 	/**
 	 * This is the constructor of a cluster object. At first such a cluster is identified by its cluster
 	 * ID and the hierarchical level. Later on - once a coordinator is found, it is additionally identified
 	 * by a token the coordinator sends to all participants. In contrast to the cluster token the identity is used
 	 * to filter potential participants that may be used for the election of a coordinator.
 	 * 
 	 * Constructor
 	 * 
 	 * @param pHRMController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster, a value of "-1" triggers the creation of a new ID
 	 */
 	private Cluster(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID)
 	{
 		super(pHRMController, pHierarchyLevel, null, -1, null);
 		
 		Logging.log(this, "CONSTRUCTOR got ClusterID: " + pClusterID);
 		
 		// set the ClusterID
 		if ((pClusterID == null) || (pClusterID < 0)){
 			// create an ID for the cluster
 			setClusterID(createClusterID());
 
 			Logging.log(this, "ClusterID - created unique clusterID " + getClusterID() + "(" + getGUIClusterID() + ")");
 		}else{
 			// use the ClusterID from outside
 			setClusterID(pClusterID);
 
 			Logging.log(this, "ClusterID - using pre-defined clusterID " + getClusterID() + "(" + getGUIClusterID() + ")");
 		}
 		
 		synchronized (mCreatedClusters) {
 			mCreatedClusters[getHierarchyLevel().getValue()]++;
 		}
 	}
 	
 	/**
 	 * Factory function: create a cluster
 	 * 
 	 * @param pHRMController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster, a value of "-1" triggers the creation of a new ID
 	 * 
 	 * @return the new Cluster object
 	 */
 	static public Cluster create(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID)
 	{
 		Cluster tResult = new Cluster(pHRMController, pHierarchyLevel, pClusterID);
 		
 		Logging.log(tResult, "\n\n\n################ CREATED CLUSTER at hierarchy level: " + (tResult.getHierarchyLevel().getValue()));
 
 		// register at HRMController's internal database
 		pHRMController.registerCluster(tResult);
 
 		// creates new elector object, which is responsible for election processes
 		tResult.mElector = new Elector(pHRMController, tResult);
 
 		return tResult;
 	}
 
 	/**
 	 * Factory function: create a base hierarchy level cluster
 	 * 
 	 * @param pHrmController the local HRMController instance
 	 * 
 	 * @return the new Cluster object
 	 */
 	static public Cluster createBaseCluster(HRMController pHrmController)
 	{
 		return create(pHrmController, HierarchyLevel.createBaseLevel(), null);
 	}
 
 	/**
 	 * Generates a new ClusterID
 	 * 
 	 * @return the ClusterID
 	 */
 	static public synchronized long createClusterID()
 	{
 		// get the current unique ID counter
 		long tResult = sNextFreeClusterID * idMachineMultiplier();
 
 		// make sure the next ID isn't equal
 		sNextFreeClusterID++;
 	
 		return tResult;
 	}
 	
 	/**
 	 * Counts all registered clusters
 	 * 
 	 * @return the number of already created clusters
 	 */
 	public static long countCreatedClusters()
 	{
 		return (sNextFreeClusterID - 1);
 	}
 
 	/**
 	 * Creates a ClusterName object which describes this cluster
 	 * 
 	 * @return the new ClusterName object
 	 */
 	public ClusterName createClusterName()
 	{
 		ClusterName tResult = null;
 		
 		tResult = new ClusterName(mHRMController, getHierarchyLevel(), getClusterID(), getCoordinatorID());
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines the coordinator of this cluster. It is "null" if the election was lost or hasn't finished yet. 
 	 * 
 	 * @return the cluster's coordinator
 	 */
 	@Override
 	public Coordinator getCoordinator()
 	{
 		return mCoordinator;
 	}
 	
 	/**
 	 * Determines if a coordinator is known.
 	 * 
 	 * @return true if the coordinator is elected and known, otherwise false
 	 */
 	public boolean hasLocalCoordinator()
 	{
 		return (mCoordinator != null);
 	}
 	
 	/**
 	 * Returns how many address broadcasts were already sent
 	 * 
 	 * @return the number of broadcasts
 	 */
 	public int countAddressBroadcasts()
 	{
 		return mSentAddressBroadcast;
 	}
 
 	/**
 	 * EVENT: a cluster member requested explicitly a formerly assigned HRMID
 	 * 
 	 * @param pComChannel the comm. channel to the cluster member
 	 * @param pHRMID the requested HRMID
 	 */
 	public void eventReceivedRequestedHRMID(ComChannel pComChannel,	HRMID pHRMID)
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_DISTRIBUTION;
 		
 		if (DEBUG){
 			Logging.warn(this, "Handling RequestHRMID with requested HRMID " + pHRMID.toString() + " for: " + pComChannel);
 		}
 		HRMID tOldAssignedHRMID = pComChannel.getPeerHRMID();
 		
 		if((getHRMID() != null) && ((!getHRMID().isZero()) || (getHierarchyLevel().isHighest()))){
 			int tRequestedAddress = pHRMID.getLevelAddress(getHierarchyLevel());
 			if (DEBUG){
 				Logging.warn(this, "Peer requested address: " + tRequestedAddress);
 			}
 			synchronized (mUsedAddresses) {
 				synchronized (mAddressReservations) {
 					if((!mAddressReservations.keySet().contains(tRequestedAddress)) || (mAddressReservations.get(tRequestedAddress).equals(pComChannel))){
 						HRMID tNewOldHRMID = getHRMID().clone();
 						tNewOldHRMID.setLevelAddress(getHierarchyLevel().getValue(), tRequestedAddress);
 						if (DEBUG){
 							Logging.warn(this, "  ..assignment of requested address " + tRequestedAddress + " is POSSIBLE");
 						}
 						
 						ComChannel tFormerOwner = null;
 						
 						/**
 						 * add as "reserved" address
 						 */
 						mAddressReservations.put(tRequestedAddress, pComChannel);
 						
 						/**
 						 * revoke the address from the old cluster member
 						 */
 						if(mUsedAddresses.contains(tRequestedAddress)){
 							LinkedList<ComChannel> tChannels = getComChannels();
 							for(ComChannel tChannel : tChannels){
 								if(!tChannel.equals(pComChannel)){
 									HRMID tAssignedHRMID = tChannel.getPeerHRMID();
 									if((tAssignedHRMID != null) && (tAssignedHRMID.equals(tNewOldHRMID))){
 										tFormerOwner = tChannel;
 										if (DEBUG){
 											Logging.warn(this, "  ..revoking requested address " + tNewOldHRMID + " from: " + tChannel);
 										}
 										tChannel.signalRevokeAssignedHRMIDs();
 										tChannel.setPeerHRMID(null);
 									}
 								}
 							}							
 						}
 														
 						/**
 						 * assign the address to the requesting cluster member	
 						 */
 						if (DEBUG){
 							Logging.warn(this, "  ..assigning requested address " + tNewOldHRMID + " to: " + pComChannel);
 						}
 						pComChannel.setPeerHRMID(tNewOldHRMID);
 						pComChannel.distributeAssignHRMID(tNewOldHRMID, true);
 	
 						/**
 						 * assign a new HRMID to the former owner channel
 						 */
 						if(tFormerOwner != null){
 							tFormerOwner.setPeerHRMID(null);
 							eventClusterMemberNeedsHRMID(tFormerOwner, this + "eventRequestedHRMID() for: " + pComChannel);
 						}
 					}else{
 						if (DEBUG){
 							Logging.warn(this, "  ..assignment of requested address " + tRequestedAddress + " is IMPOSSIBLE, enforcing the assignment of " + tOldAssignedHRMID);
 							Logging.warn(this, "  ..current reservations are: " + mAddressReservations);
 						}
 						pComChannel.distributeAssignHRMID(tOldAssignedHRMID, true);
 					}
 				}
 			}
 		}else{
 			if (DEBUG){
 				Logging.warn(this, "eventRequestedHRMID() skipped because the own HRMID is invalid: " + getHRMID());
 			}
 		}
 			
 	}
 
 	/**
 	 * EVENT: cluster needs HRMIDs
 	 * This function sets a timer for a new address distribution
 	 */
 	public synchronized void eventClusterNeedsHRMIDs()
 	{
 		mAddressDistributionTimeout = mHRMController.getSimulationTime() + HRMConfig.Addressing.DELAY_ADDRESS_DISTRIBUTION; 
 	}
 	
 	/**
 	 * Checks if it is time to distribute address
 	 * 
 	 * @return true or false
 	 */
 	public synchronized boolean isTimeToDistributeAddresses()
 	{
 		boolean tResult = false;
 		
 		if (mAddressDistributionTimeout > 0){
 			if(mAddressDistributionTimeout < mHRMController.getSimulationTime()){
 				tResult = true;
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * DISTRIBUTE: distribute addresses among cluster members if:
 	 *           + an HRMID was received from a superior coordinator, used to distribute HRMIDs downwards the hierarchy,
 	 *           + we were announced as coordinator
 	 * This function is called for distributing HRMIDs among the cluster members.
 	 * Moreover, it implements the recursive address distribution.
 	 * 
 	 */
 	public void distributeAddresses()
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_DISTRIBUTION;
 		
 		// reset timer for address distribution
 		mAddressDistributionTimeout = 0;
 		
 		HRMID tOwnHRMID = getHRMID();
 
 		boolean tClusterIsTopOfHierarchy = getHierarchyLevel().isHighest(); 
 		boolean tNewL0HRMID = false;
 		
 		if((tOwnHRMID != null) || (tClusterIsTopOfHierarchy)){
 			// lock in order to avoid parallel execution of eventReceivedRequestHRMID
 			synchronized (mUsedAddresses) {
 				// do we have a new HRMID since the last call?
 	//			if ((mHRMIDLastDistribution == null) || (!mHRMIDLastDistribution.equals(tOwnHRMID))){
 					// update the stored HRMID
 					mHRMIDLastDistribution = tOwnHRMID;
 					
 					/**
 					 * reset list of already used addresses
 					 */
 					mUsedAddresses.clear();
 					
 					/**
 					 * Distribute addresses
 					 */
 					if ((tClusterIsTopOfHierarchy) || ((tOwnHRMID != null) && ((HRMConfig.Addressing.DISTRIBUTE_RELATIVE_ADDRESSES) || (!tOwnHRMID.isRelativeAddress()) /* we already have been assigned a valid HRMID? */))){
 						mSentAddressBroadcast++;
 						
 						if(DEBUG){
 							Logging.warn(this, "#### DISTRIBUTING ADDRESSES [" + mSentAddressBroadcast + "] to entities on level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 						}
 						
 						/**
 						 * Assign ourself an HRMID address
 						 */
 						// are we at the base level?
 						if(getHierarchyLevel().isBaseLevel()) {
 	
 							// get the old L0 HRMID
 							HRMID tOldL0HRMID = getL0HRMID();
 							/**
 							 * Check we should actually assign a new L0 HRMID 
 							 */
 							if((tOldL0HRMID == null) || (tOldL0HRMID.isZero()) || (tOldL0HRMID.isRelativeAddress()) || (!tOldL0HRMID.isCluster(getHRMID()))){
 								// create new HRMID for ourself
 								HRMID tThisNodesAddress = allocateClusterMemberAddress();
 								if((tThisNodesAddress != null) && (!tThisNodesAddress.equals(getL0HRMID()))){
 									// inform HRM controller about the address change
 									if(getL0HRMID() != null){
 										
 										// free only if the assigned new address has a different used cluster address
 										if(tThisNodesAddress.getLevelAddress(getHierarchyLevel()) != getL0HRMID().getLevelAddress(getHierarchyLevel())){
 											
 											mDescriptionHRMIDAllocation += "\n     ..revoked " + getL0HRMID().toString() + " for " + this + ", cause=distributeAddresses() [" + mSentAddressBroadcast + "]";
 	
 											freeClusterMemberAddress(getL0HRMID().getLevelAddress(getHierarchyLevel()));
 										}
 					
 									}
 			
 									mDescriptionHRMIDAllocation += "\n     .." + tThisNodesAddress.toString() + " for " + this + ", cause=distributeAddresses() [" + mSentAddressBroadcast + "]";
 									
 									if(DEBUG){
 										Logging.warn(this, "    ..setting local HRMID " + tThisNodesAddress.toString());
 									}
 						
 									// store the new HRMID for this node
 									if(DEBUG){
 										Logging.warn(this, "distributeAddresses() [" + mSentAddressBroadcast + "] sets new L0HRMID: " + tThisNodesAddress);
 									}
 									setL0HRMID(tThisNodesAddress);
 									
 									tNewL0HRMID = true;
 								}else{
 									if(tThisNodesAddress == null){
 										throw new RuntimeException(this + "::distributeAddresses() got a zero HRMID from allocateClusterMemberAddress()");
 									}
 									
 									// free the allocated address again
 									freeClusterMemberAddress(tThisNodesAddress.getLevelAddress(getHierarchyLevel()));
 								}
 							}else{
 								// do a refresh here -> otherwise, the local L0 might not been updated in case the clustering switched to an alternative/parallel cluster
 								if(DEBUG){
 									Logging.warn(this, "  ..refreshing L0 HRMID: " + tOldL0HRMID);
 								}
 								setL0HRMID(tOldL0HRMID);
 							}
 						}
 						if(tClusterIsTopOfHierarchy){
 							setHRMID(this, new HRMID(0));
 						}
 						
 						/**
 						 * Distribute AssignHRMID packets among the cluster members 
 						 */
 						LinkedList<ComChannel> tComChannels = getComChannels();
 						
 						if(DEBUG){
 							Logging.warn(this, "    ..distributing HRMIDs among cluster members: " + tComChannels);
 						}
 						int i = 0;
 						for(ComChannel tComChannel : tComChannels) {
 							/**
 							 * Trigger: cluster member needs HRMID
 							 */
 							eventClusterMemberNeedsHRMID(tComChannel, "distributeAddresses() [" + mSentAddressBroadcast + "]");
 							if(DEBUG){
 								Logging.warn(this, "   ..[" + i + "]: assigned " + tComChannel.getPeerHRMID() + "  to: " + tComChannel);
 							}
 							i++;
 						}
 						
 						/**
 						 * Announce the local node HRMIDs if we are at base hierarchy level
 						 */
 						if(tNewL0HRMID){
 							LinkedList<ClusterMember> tL0ClusterMembers = mHRMController.getAllL0ClusterMembers();
 							for(ClusterMember tL0ClusterMember : tL0ClusterMembers){
 								if(DEBUG){
 									Logging.warn(this, "distributeAddresses() [" + mSentAddressBroadcast + "] triggers an AnnounceHRMID for: " + tL0ClusterMember + " with HRMIDs: " + mHRMController.getHRMIDs());
 								}
 								tL0ClusterMember.distributeAnnounceHRMIDs();
 							}
 						}
 					}
 	//			}else{
 	//				Logging.log(this, "distributeAddresses() skipped because the own HRMID is still the same: " + getHRMID());
 	//			}
 			}
 		}else{
 			mDescriptionHRMIDAllocation += "\n     ..aborted distributeAddresses() because of own HRMID: " + tOwnHRMID;
 
 			if(DEBUG){
 				Logging.warn(this, "distributeAddresses() skipped because the own HRMID is still invalid: " + getHRMID());
 			}
 		}
 	}
 
 	/**
 	 * EVENT: new HRMID assigned
      * The function is called when an address update was received.
 	 * 
 	 * @param pSourceComChannel the source comm. channel
 	 * @param pHRMID the new HRMID
 	 * @param pIsFirmAddress is this address firm? 
 	 * 
 	 * @return true if the signaled address was accepted, other (a former address is requested from the peer) false
 	 */
 	@Override
 	public boolean eventAssignedHRMID(ComChannel pSourceComChannel, HRMID pHRMID, boolean pIsFirmAddress)
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_DISTRIBUTION; 
 		boolean tResult = false;
 		
 		if (DEBUG){
 			Logging.log(this, "Handling AssignHRMID with assigned HRMID " + pHRMID.toString());
 		}
 
 		if((pHRMID != null) && (!pHRMID.isZero())){
 			// setHRMID()
 			tResult = super.eventAssignedHRMID(pSourceComChannel, pHRMID, pIsFirmAddress);
 		
 			if(tResult){
 				/**
 				 * Automatic address distribution via this cluster
 				 */
 				if (hasLocalCoordinator()){
 					// we should automatically continue the address distribution?
 					if (HRMController.GUI_USER_CTRL_ADDRESS_DISTRUTION){
 						if (DEBUG){
 							Logging.log(this, "     ..continuing the address distribution process via this cluster");
 						}
 						distributeAddresses();
 						applyAddressToAlternativeClusters(pHRMID);
 					}			
 				}else{
 					if (DEBUG){
 						Logging.log(this, "     ..stopping address propagation here");
 					}
 				}
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Assign new HRMID for being addressable.
 	 *  
 	 * @param pCaller the caller who assigns the new HRMID
 	 * @param pHRMID the new HRMID
 	 */
 	@Override
 	public void setHRMID(Object pCaller, HRMID pHRMID)
 	{
 		Logging.log(this, "Got new HRMID: " + pHRMID + ", caller=" + pCaller);
 		
 		/**
 		 * Do we have a new HRMID?
 		 */
 		freeAllClusterMemberAddresses("setHRMID() for " + pHRMID);
 			
 		/**
 		 * Set the new HRMID
 		 */
 		super.setHRMID(pCaller, pHRMID);
 			
 //			/**
 //			 * Update the local HRG: find other active Cluster instances and store a local loopback link to them
 //			 */
 //			LinkedList<Cluster> tSiblings = mHRMController.getAllClusters(getHierarchyLevel());
 //			// iterate over all siblings
 //			for(Cluster tSibling : tSiblings){
 //				if(tSibling.isActiveCluster()){
 //					Logging.log(this, "  ..found active sibling: " + tSibling);
 //					HRMID tSiblingAddress = tSibling.getHRMID();
 //					HRMID tSiblingL0Address = tSibling.getL0HRMID();
 //					// has the sibling a valid address?
 //					if((tSiblingAddress != null) && (!tSiblingAddress.isZero())){
 //						// avoid recursion
 //						if(!tSibling.equals(this)){
 //							// create the new reported routing table entry
 //							RoutingEntry tRoutingEntryToSibling = RoutingEntry.create(getL0HRMID() /* this cluster */, tSiblingAddress /* the sibling */, tSiblingL0Address, 0 /* loopback route */, RoutingEntry.NO_UTILIZATION, RoutingEntry.NO_DELAY, RoutingEntry.INFINITE_DATARATE);
 //							// register the new HRG entry
 //							mHRMController.registerCluster2ClusterLinkHRG(getHRMID(), tSiblingAddress, tRoutingEntryToSibling);
 //						}
 //					}
 //				}
 //			}
 	}
 
 	/**
 	 * Resets the list of used cluster addresses
 	 * 
 	 * @param pCause the cause for this reset
 	 */
 	private void freeAllClusterMemberAddresses(String pCause)
 	{
 		/**
 		 * Reset the list of used addresses because we got a new HRMID
 		 */
 		synchronized (mUsedAddresses) {
 			Logging.log(this, "Resetting list of used addresses: " + mUsedAddresses);
 			mUsedAddresses.clear();
 			synchronized (mAddressReservations) {
 				mAddressReservations.clear();
 			}
 			
 			mDescriptionHRMIDAllocation += "\n     ..reset list of used cluster addresses" + ", cause=" + pCause;
 		}		
 	}
 
 	/**
 	 * Allocates a new HRMID for a cluster member.
 	 * 
 	 * @return the allocated HRMID for the cluster member
 	 */
 	private HRMID allocateClusterMemberAddress()
 	{
 		HRMID tResult = (getHRMID() != null ? getHRMID().clone() : null);
 		if(getHierarchyLevel().isHighest()){
 			tResult = new HRMID(0);
 		}
 		
 		if(tResult != null){
 			/**
 			 * Search for the lowest free address
 			 */
 			int tUsedAddress = 0;
 			synchronized (mUsedAddresses) {
 				// iterate over all possible addresses
 				for(tUsedAddress = 1; tUsedAddress < 1024 /* TODO: 2 ^ digits from HRMConfig */; tUsedAddress++){
 					// have we found a free address?
 					if((!mUsedAddresses.contains(tUsedAddress)) && (!mAddressReservations.keySet().contains(tUsedAddress))){
 						/**
 						 * NEW HRMID: update address usage
 						 */
 						mUsedAddresses.add(tUsedAddress);
 	
 						Logging.log(this, "Allocated ClusterMember address: " + tUsedAddress);
 						
 						break;
 					}
 				}
 			}
 	
 			// transform the member number to a BigInteger
 			BigInteger tAddress = BigInteger.valueOf(tUsedAddress);
 	
 			// set the member number for the given hierarchy level
 			tResult.setLevelAddress(getHierarchyLevel(), tAddress);
 	
 			// some debug outputs
 			if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 				Logging.log(this, "Set " + tAddress + " on hierarchy level " + getHierarchyLevel().getValue() + " for HRMID " + tResult.toString());
 				Logging.log(this, "Created for a cluster member the NEW HRMID=" + tResult.toString());
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Frees an allocated cluster member address.
 	 *  
 	 * @param pAddress the address which should be freed
 	 */
 	private void freeClusterMemberAddress(int pAddress)
 	{
 		synchronized (mUsedAddresses) {
 			if(mUsedAddresses.contains(pAddress)){
 				mUsedAddresses.remove(new Integer(pAddress));
 				synchronized (mAddressReservations) {
 					mAddressReservations.remove(new Integer(pAddress));
 				}
 				Logging.log(this, "Freed ClusterMember address: " + pAddress);
 			}
 		}
 	}
 	
 	/**
 	 * Returns the list of used addresses
 	 * 
 	 * @return the list
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<Integer> getUsedAddresses()
 	{
 		LinkedList<Integer> tResult = null;
 		
 		Logging.log(this, "Having allocated these HRMIDs: " + mDescriptionHRMIDAllocation);
 		
 		synchronized (mUsedAddresses) {
 			tResult = (LinkedList<Integer>) mUsedAddresses.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the list of used addresses
 	 * 
 	 * @return the list
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<Integer, ComChannel> getReservedAddresses()
 	{
 		HashMap<Integer, ComChannel> tResult = null;
 		
 		Logging.log(this, "Having reserved these HRMIDs: " + mDescriptionHRMIDAllocation);
 		
 		synchronized (mAddressReservations) {
 			tResult = (HashMap<Integer, ComChannel>) mAddressReservations.clone();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * EVENT: cluster member needs HRMID
 	 * 
 	 * @param pComChannel the comm. channel towards the cluster member, which needs a new HRMID
 	 * @param pCause the cause for this event
 	 */
 	private void eventClusterMemberNeedsHRMID(ComChannel pComChannel, String pCause)
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_DISTRIBUTION;
 		if(DEBUG){
 			Logging.warn(this, "EVENT: Cluster_Member_Needs_HRMID for: " + pComChannel + ", cause=" + pCause);
 			Logging.warn(this, "    ..used address: " + mUsedAddresses);
 		}
 		
 		/**
 		 * AUTO ADDRESS DISTRIBUTION
 		 */
 		if (HRMController.GUI_USER_CTRL_ADDRESS_DISTRUTION){
 			/**
 			 * get the old HRMID
 			 */
 			HRMID tOldHRMIDForPeer = pComChannel.getPeerHRMID();
 			
 			/**
 			 * look for old reservations
 			 */
 			if(mAddressReservations.containsValue(pComChannel)){
 				for(Integer tUsedAddress : mAddressReservations.keySet()){
 					if(mAddressReservations.get(tUsedAddress).equals(pComChannel)){
 						HRMID tHRMID = getHRMID().clone();
 						tHRMID.setLevelAddress(getHierarchyLevel().getValue(), tUsedAddress);
 						tOldHRMIDForPeer = tHRMID;
 						break;
 					}
 				}
 			}
 			
 			int tOldUsedAddress = (tOldHRMIDForPeer != null ? tOldHRMIDForPeer.getLevelAddress(getHierarchyLevel()) : -1);
 			HRMID tHRMIDForPeer = null;
 			/**
 			 * Check old assignment
 			 */
 			if(DEBUG){
 				Logging.warn(this, "    ..old peer HRMID: " + tOldHRMIDForPeer + "(lvl.: " + (tOldHRMIDForPeer != null ? tOldHRMIDForPeer.getHierarchyLevel() : "-1") + ") for " + pComChannel);
 			}
 			if(!HRMConfig.Addressing.REUSE_ADDRESSES){
 				if(DEBUG){
 					Logging.warn(this, "     ..reseting the old HRMID to null because address reusage is disabled");
 				}
 				tOldHRMIDForPeer = null;
 			}
 			
 			/**
 			 * OLD HRMID: update address usage
 			 */
 			boolean tOldAddrAlreadyUsed = mUsedAddresses.contains(tOldUsedAddress);
 					
 			/**
 			 * Check if we should actually assign a NEW HRMID 
 			 */
 			boolean tOldAddrReservered = ((mAddressReservations.keySet().contains(tOldUsedAddress)) && (!mAddressReservations.get(tOldUsedAddress).equals(pComChannel)));
 			boolean tOldAddressHasInvalidLvl = ((tOldHRMIDForPeer != null ? tOldHRMIDForPeer.getHierarchyLevel() : -1) != getHierarchyLevel().getValue() - 1);
 			if(DEBUG){
 				Logging.warn(this, "    .." + tOldHRMIDForPeer + " => already used: " + tOldAddrAlreadyUsed + ", reserved for other: " + tOldAddrReservered + (tOldAddrReservered ? "(" + mAddressReservations.get(tOldUsedAddress).getPeer() + ")": "") + ", invalid hier. lvl.: " + tOldAddressHasInvalidLvl);
 			}
 			if((tOldHRMIDForPeer == null) || (tOldHRMIDForPeer.isZero()) || (tOldHRMIDForPeer.isRelativeAddress()) || (tOldAddrReservered) || (tOldAddressHasInvalidLvl) || (tOldAddrAlreadyUsed) || ((!tOldHRMIDForPeer.isCluster(getHRMID()) && (!getHierarchyLevel().isHighest())))){
 				HRMID tNewHRMIDForPeer = allocateClusterMemberAddress();
 				if(tNewHRMIDForPeer != null){
 					mDescriptionHRMIDAllocation += "\n     .." + tNewHRMIDForPeer.toString() + " for " + pComChannel + ", cause=" + pCause;
 	
 					/**
 					 * Abort if we shouldn't distribute relative addresses
 					 */
 					if((!HRMConfig.Addressing.DISTRIBUTE_RELATIVE_ADDRESSES) && (tNewHRMIDForPeer.isRelativeAddress())){
 						//Logging.warn(this, "eventClusterMemberNeedsHRMID() aborted because the relative address shouldn't be distributed: " + tNewHRMIDForPeer);
 						
 						mDescriptionHRMIDAllocation += "\n     ..revoked " + tNewHRMIDForPeer.toString() + " for " + pComChannel + ", cause=" + pCause;
 
 						freeClusterMemberAddress(tNewHRMIDForPeer.getLevelAddress(getHierarchyLevel()));
 						
 						return;
 					}
 					
 					// store the HRMID under which the peer will be addressable from now 
 					pComChannel.setPeerHRMID(tNewHRMIDForPeer);
 	
 					// register this new HRMID in the local HRS and create a mapping to the right L2Address
 					if(!tNewHRMIDForPeer.isClusterAddress()){
 						if(DEBUG){
 							Logging.warn(this, "    ..creating MAPPING " + tNewHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address() + ", L2 route=" + mHRMController.getHRS().getL2RouteViaNetworkInterface(pComChannel.getPeerL2Address(), getBaseHierarchyLevelNetworkInterface()));
 						}
 						mHRMController.getHRS().mapHRMID(tNewHRMIDForPeer, pComChannel.getPeerL2Address(), mHRMController.getHRS().getL2RouteViaNetworkInterface(pComChannel.getPeerL2Address(), getBaseHierarchyLevelNetworkInterface()));
 					}
 					
 					/**
 					 * USE NEW HRMID
 					 */
 					tHRMIDForPeer = tNewHRMIDForPeer;
 				}else{
 					Logging.err(this, "::eventClusterMemberNeedsHRMID() got the invalid new cluster member address [" + getHRMID() + "] for: " + pComChannel);
 				}
 			}else{
 				mDescriptionHRMIDAllocation += "\n     ..reassigned " + tOldHRMIDForPeer.toString() + " for " + pComChannel + ", cause=" + pCause;
 
 				if(DEBUG){
 					Logging.warn(this, "    ..reassigning " + tOldHRMIDForPeer.toString() + " for " + pComChannel);
 				}
 
 				/**
 				 * USE OLD HRMID
 				 */
 				tHRMIDForPeer = tOldHRMIDForPeer;
 
 				if(!mUsedAddresses.contains(tOldUsedAddress)){
 					if(DEBUG){
 						Logging.warn(this, "    ..mark the address as used in this cluster");
 					}
 					// add the peer address to the used addresses
 					mUsedAddresses.add(tOldUsedAddress);
 				}
 			}
 	
 			// send the packet in every case
 			if(tHRMIDForPeer != null){
 				if(DEBUG){
 					if ((pComChannel.getPeerHRMID() != null) && (!pComChannel.getPeerHRMID().equals(tHRMIDForPeer))){
 						Logging.warn(this, "    ..replacing HRMID " + pComChannel.getPeerHRMID().toString() + " and assign new HRMID " + tHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address());
 					}else
 						Logging.warn(this, "    ..assigning new HRMID " + tHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address());
 				}
 	
 				pComChannel.distributeAssignHRMID(tHRMIDForPeer, false);
 			}else{
 				mDescriptionHRMIDAllocation += "\n     ..invalid HRMID for " + pComChannel + ", cause=" + pCause;
 
 				Logging.warn(this, "eventClusterMemberNeedsHRMID() detected invalid cluster HRMID and cannot signal new HRMID to: " + pComChannel);
 			}
 		}else{
 			if(DEBUG){
 				Logging.warn(this, "Address distribution is deactivated, no new assigned HRMID for: " + pComChannel);
 			}
 		}
 	}
 
 	/**
 	 * EVENT: all cluster addresses are invalid
 	 */
 	public void eventAllClusterAddressesInvalid()
 	{
 		Logging.log(this, "EVENT: all cluster addresses got declared invalid");
 
 		setHRMID(this, null);
 
 		/**
 		 * Revoke all HRMIDs from the cluster members
 		 */
 		LinkedList<ComChannel> tComChannels = getComChannels();
 		int i = 0;
 		for (ComChannel tComChannel : tComChannels){
 			/**
 			 * Unregister all HRMID-2-L2Address mappings
 			 */
 			for(HRMID tHRMIDForPeer:tComChannel.getAssignedPeerHRMIDs()){
 				// register this new HRMID in the local HRS and create a mapping to the right L2Address
 				Logging.log(this, "    ..removing MAPPING " + tHRMIDForPeer.toString() + " to " + tComChannel.getPeerL2Address());
 				mHRMController.getHRS().unmapHRMID(tHRMIDForPeer);
 			}
 
 			/**
 			 * Revoke all assigned HRMIDs from peers
 			 */
 			Logging.log(this, "   ..[" + i + "]: revoking HRMIDs via: " + tComChannel);
 			tComChannel.signalRevokeAssignedHRMIDs();
 			
 			i++;
 		}
 		
 		/**
 		 * Reset the list of used addresses
 		 */
 		freeAllClusterMemberAddresses("eventAllClusterAddressesInvalid()");
 	}
 
 	/**
 	 * EVENT: new local coordinator, triggered by the Coordinator
 	 * 
 	 * @param pCoordinator the new coordinator, which is located on this node
 	 */
 	public void eventNewLocalCoordinator(Coordinator pCoordinator)
 	{
 		Logging.log(this, "EVENT: new local coordinator: " + pCoordinator + ", old one is: " + mCoordinator);
 		
 		// set the coordinator
 		mCoordinator = pCoordinator;
 		
 		/**
 		 * Update cluster activation
 		 */
 		if(pCoordinator != null){
 			// mark this cluster as active
 			setClusterWithValidCoordinator(true);
 		}else{
 			// mark this cluster as inactive
 			setClusterWithValidCoordinator(false);
 		}
 		
 		// update the stored unique ID for the coordinator
 		if (pCoordinator != null){
 			setSuperiorCoordinatorID(pCoordinator.getCoordinatorID());
 			setCoordinatorID(pCoordinator.getCoordinatorID());			
 
 			// update the descriptive string about the coordinator
 			setSuperiorCoordinatorDescription(mCoordinator.toLocation());
 		}else{
 			setSuperiorCoordinatorID(0);
 			setCoordinatorID(0);
 			setSuperiorCoordinatorDescription("");
 		}
 	}
 	
 	/**
 	 * Detects the local neighborhood.
 	 * 		  IMPORTANT: This is the main function for determining capacities and link usage
 	 */
 	@Override
 	public void detectNeighborhood()
 	{
 		if(hasClusterValidCoordinator()){
 			if(getHierarchyLevel().isBaseLevel()){
 				super.detectNeighborhood();
 				if(countConnectedRemoteClusterMembers() > 1){
 					if(getBaseHierarchyLevelNetworkInterface() != null){
 						// get the physical BUS
 						Bus tPhysicalBus = (Bus)getBaseHierarchyLevelNetworkInterface().getBus();
 	
 						// iterate over all comm. channels
 						for(ComChannel tOuterChannel : getComChannels()){
 							HRMID tOuterHRMID = tOuterChannel.getPeerHRMID();
 							for(ComChannel tInnerChannel : getComChannels()){
 								HRMID tInnerHRMID = tInnerChannel.getPeerHRMID();
 								if((tOuterHRMID != null) && (tInnerHRMID != null)){
 									if(!tOuterHRMID.equals(tInnerHRMID)){
 										//Logging.log(this, "  .." + tOuterHRMID + " is BUS neighbor of: " + tInnerHRMID);
 										RoutingEntry tEntryForward = RoutingEntry.createRouteToDirectNeighbor(tOuterHRMID, tInnerHRMID, tInnerHRMID, tPhysicalBus.getUtilization(), tPhysicalBus.getDelayMSec(), tPhysicalBus.getAvailableDataRate(), this + "::detectNeighborhood()");
 										tEntryForward.setNextHopL2Address(tInnerChannel.getPeerL2Address());
 										mHRMController.registerAutoHRG(tEntryForward);
 
 										RoutingEntry tEntryBackward = RoutingEntry.createRouteToDirectNeighbor(tInnerHRMID, tOuterHRMID, tOuterHRMID, tPhysicalBus.getUtilization(), tPhysicalBus.getDelayMSec(), tPhysicalBus.getAvailableDataRate(), this + "::detectNeighborhood()");
 										tEntryBackward.setNextHopL2Address(tOuterChannel.getPeerL2Address());
 										mHRMController.registerAutoHRG(tEntryBackward);
 									}
 								}
 							}
 						}
 					}
 				}
 			}else{
 				Logging.err(this, "detectNeighborhood() expects base hierarchy level");
 			}
 		}
 	}
 	
 	/**
 	 * Returns the machine-local ClusterID (excluding the machine specific multiplier)
 	 * 
 	 * @return the machine-local ClusterID
 	 */
 	public long getGUIClusterID()
 	{
 		if (getClusterID() != null)
 			return getClusterID() / idMachineMultiplier();
 		else
 			return -1;
 	}
 	
 	/**
 	 * EVENT: coordinator lost
 	 */
 	public void eventCoordinatorLost()
 	{
 		Logging.log(this, "EVENT: coordinator was lost");
 		
 		// reset timer for address distribution
 		mAddressDistributionTimeout = 0;
 		
 		/**
 		 * Revoke HRMID of physical node if we are on base hierarchy level
 		 */ 
 		if(getHierarchyLevel().isBaseLevel()){
 			if(getL0HRMID() != null){
 				Logging.log(this, "Unregistering physical node HRMID: " + getL0HRMID());
 
 				mDescriptionHRMIDAllocation += "\n     ..revoked " + getL0HRMID().toString() + " for " + this + ", cause=eventCoordinatorLost()";
 
 				freeClusterMemberAddress(getL0HRMID().getLevelAddress(getHierarchyLevel()));
 
 				mHRMController.unregisterHRMID(this, getL0HRMID(), this + "::eventCoordinatorLost()");					
 			}
 		}
 		
 		// store the former coordinator ID
 		mDescriptionFormerGUICoordinatorIDs += " " + Long.toString(getGUICoordinatorID());
 		
 		// unregister coordinator
 		eventNewLocalCoordinator(null);
 	}
 	
 	/**
 	 * Returns a description of the former GUICoordinatorIDs
 	 * 
 	 * @return the description
 	 */
 	public String getDescriptionFormerGUICoordinatorIDs()
 	{
 		return mDescriptionFormerGUICoordinatorIDs;
 	}
 	
 	/**
 	 * Returns the comm. channel to a given peer entity
 	 * 
 	 * @param pPeer the peer entitiy
 	 * 
 	 * @return the found comm. channel
 	 */
 	public ComChannel getComChannelToMember(ControlEntity pPeer)
 	{
 		ComChannel tResult = null;
 		
 		LinkedList<ComChannel> tChannels = getComChannels();
 		for(ComChannel tComChannel : tChannels){
 			if(pPeer.equals(tComChannel.getPeer())){
 				tResult = tComChannel;
 				break;
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * EVENT: RouteReport from an inferior entity received, triggered by the comm. channel
 	 * 
 	 * @param pComChannel the source comm. channel 
 	 * @param pRouteReportPacket the packet (use packet for better debugging here)
 	 * @param pDeprecatedRoutingTable the routing table with deprecated entries
 	 */
 	public void eventReceivedRouteReport(ComChannel pSourceComChannel, RouteReport pRouteReportPacket, RoutingTable pDeprecatedRoutingTable)
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.SHOW_REPORT_PHASE;
 		
 		if(DEBUG){
 			Logging.log(this, "EVENT: ReceivedRouteReport: " + pRouteReportPacket + " from " + pSourceComChannel.getPeerL2Address());
 		}
 		
 		/**
 		 * mark deprecated entries for a near deletion
 		 */
 		if((pDeprecatedRoutingTable != null) && (pDeprecatedRoutingTable.size() > 0)){
 			Logging.warn(this, "Found deprecated reported routing table: " + pDeprecatedRoutingTable);
 			for(RoutingEntry tDeprecatedEntry : pDeprecatedRoutingTable){
 				Logging.warn(this, "   ..found deprecated reported routing entry: " + tDeprecatedEntry);
 				
 				/**
 				 * Set the timeout for deprecated reported routes to a short time period
 				 */
 				tDeprecatedEntry.setTimeout(mHRMController.getSimulationTime() + HRMConfig.Routing.ROUTE_TIMEOUT  + HRMConfig.Hierarchy.MAX_E2E_DELAY);
 				
 				/**
 				 * Mark as reported entry
 				 */
 				tDeprecatedEntry.setReportedLink(pSourceComChannel.getPeerHRMID());
 				
 				/**
 				 * Update the HRG in such a way that the deprecated will be removed automatically very soon if no other inferior coordinator reports the same route with a high timeout (the route is still known as active there)
 				 */
 				int tHopCount = tDeprecatedEntry.getHopCount();
 				switch(tHopCount)
 				{
 					case 0:
 						// it's an inter-cluster link because local loopbacks aren't sent as report
 						tDeprecatedEntry.extendCause(this + "::eventReceivedRouteReport()(0 hops) from " + pSourceComChannel.getPeerHRMID());
 						mHRMController.registerAutoHRG(tDeprecatedEntry);
 
 						break;
 					case 1:
 					default: // 2+
 						// do we have an intra-cluster link?
 						if((!tDeprecatedEntry.getDest().isClusterAddress()) && (tDeprecatedEntry.getDest().equals(tDeprecatedEntry.getLastNextHop()))){
 							tDeprecatedEntry.extendCause(this + "::eventReceivedRouteReport()(1 hop) from " + pSourceComChannel.getPeerHRMID());
 							mHRMController.registerLinkHRG(tDeprecatedEntry.getSource(), tDeprecatedEntry.getLastNextHop(), tDeprecatedEntry);
 						}else{
 							// strange, an inter-cluster link with ONE hop?!
 						}
 						break;
 				}
 			}
 			DEBUG = true;
 		}
 			
 		/**
 		 * Iterate over all reported routes and derive new data for the HRG
 		 */
 		RoutingTable tNewReportedRoutingTable = pRouteReportPacket.getRoutes();
 		for(RoutingEntry tEntry : tNewReportedRoutingTable){
 			if(DEBUG){
 				Logging.log(this, "   ..received route: " + tEntry);
 			}
 				
 			double tBefore = HRMController.getRealTime();
 			
 			/**
 			 * make sure a relative timeout is set in the reported routing table entry
 			 */
 			if(tEntry.getTimeout() <= 0){
 				tEntry.setTimeout(HRMConfig.Routing.ROUTE_TIMEOUT  + HRMConfig.Hierarchy.MAX_E2E_DELAY);
 			}
 
 			/**
 			 * Set the timeout for reported routes: use the previously stored relative timeout value from the reporter and form an absolute timeout
 			 */
 			tEntry.setTimeout(mHRMController.getSimulationTime() + tEntry.getTimeout());
 
 			/**
 			 * Mark as reported entry
 			 */
 			tEntry.setReportedLink(pSourceComChannel.getPeerHRMID());
 			
 			/**
 			 * Update the HRG
 			 */
 			int tHopCount = tEntry.getHopCount();
 			switch(tHopCount)
 			{
 				case 0:
 					// it's an inter-cluster link because local loopbacks aren't sent as report
 					tEntry.extendCause(this + "::eventReceivedRouteReport()(0 hops) from " + pSourceComChannel.getPeerHRMID());
 					/**
 					 * Check if the reported link is already reported by a local inferior coordinator.
 					 * 
 					 * For example, the local coordinator 1.0.0 exists and the foreign coordinator 2.0.0 reports a link from (1.0.0 <==> 2.0.0). 
 					 * In this case, the local coordinator 1.0.0 has already registered the corresponding HRG links.
 					 */
 //					HRMID tFromCluster = tEntry.getNextHop().getForeignCluster(tEntry.getSource());
 //					HRMID tToCluster = tEntry.getSource().getForeignCluster(tEntry.getNextHop());
 //					boolean tLinkAlreadyKnown = false;
 //					LinkedList<Coordinator> tLocalInferiorCoordinators = mHRMController.getAllCoordinators(getHierarchyLevel().getValue() - 1);
 //					for(Coordinator tCoordinator : tLocalInferiorCoordinators){
 //						if((tFromCluster.equals(tCoordinator.getHRMID())) || (tToCluster.equals(tCoordinator.getHRMID()))){
 //							if(DEBUG){
 //								Logging.log(this, "  ..route already known between " + tFromCluster + " and " + tToCluster + ", route=" + tEntry);
 //							}
 //							tLinkAlreadyKnown = true;
 //						}
 //					}
 //					if(!tLinkAlreadyKnown){
 						mHRMController.registerAutoHRG(tEntry);
 //					}else{
 //						if(DEBUG){
 //							Logging.log(this, "  ..dropping uninteresting reported route: " + tEntry);
 //						}
 //					}
 
 					break;
 				case 1:
 				default: // 2+
 					// do we have an intra-cluster link?
 					if((!tEntry.getDest().isClusterAddress()) && (tEntry.getDest().equals(tEntry.getLastNextHop()))){
 						tEntry.extendCause(this + "::eventReceivedRouteReport()(1 hop) from " + pSourceComChannel.getPeerHRMID());
 						mHRMController.registerLinkHRG(tEntry.getSource(), tEntry.getLastNextHop(), tEntry);
 					}else{
 						// strange, an inter-cluster link with ONE hop?!
 					}
 					break;
 			}
 			double tSpentTime = HRMController.getRealTime() - tBefore;
 			if(tSpentTime > 30){
 				Logging.log(this, "      ..eventReceivedRouteReport() for entry with " + tHopCount + " hops took " + tSpentTime + " ms for processing " + tEntry + " of route report: " + pRouteReportPacket);
 			}
 
 //			/**
 //			 * Does the next hop lead to a foreign cluster?
 //			 */				
 //			if(tDestHRMID.isClusterAddress()){
 //				// is it a route from a physical node to the next one, which belongs to the destination cluster? 
 //				if(pRoutingEntry.isRouteToDirectNeighbor()){
 //					// register automatically new links in the HRG based on pRoutingEntry 
 //					registerAutoHRG(pRoutingEntry);
 //				}
 //			}else{
 //				pRoutingEntry.extendCause(this + "::addHRMRoute()_2");
 //				if(HRMConfig.DebugOutput.GUI_SHOW_HRG_UPDATES){
 //					Logging.log(this, "  ..registering (" + mCallsAddHRMRoute + ") nodeHRMID-2-nodeHRMID HRG link for: " + pRoutingEntry);
 //				}
 //				registerLinkHRG(pRoutingEntry.getSource(), pRoutingEntry.getNextHop(), pRoutingEntry);
 //			}
 			
 //			HRMID tDestHRMID = tEntry.getDest();
 //			HRMID tOwnClusterAddress = getHRMID();
 //			if(tDestHRMID != null){
 //				// search for cluster destinations
 //				if(tDestHRMID.isClusterAddress()){
 //					// avoid recursion
 //					if(!tDestHRMID.equals(tOwnClusterAddress)){
 //						if(HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 //							Logging.log(this, "     ..route between clusters found");						
 //						}
 //						
 //						/**
 //						 * Store/update link in the HRG
 //						 */ 
 ////						mHRMController.registerCluster2ClusterLinkHRG(tEntry.getSource(), tDestHRMID, tEntry);
 //					}
 //				}				
 //			}else{
 //				Logging.err(this,  "Invalid route received: " + tEntry);
 //			}
 		}
 	}
 
 	/**
 	 * Returns all known inferior remote coordinators
 	 * 
 	 * @return the list of known inferior coordinators
 	 */
 	public LinkedList<CoordinatorProxy> getAllInferiorRemoteCoordinators()
 	{
 		LinkedList<CoordinatorProxy> tResult = null;
 		
 		synchronized (mInferiorRemoteCoordinators) {
 			tResult = (LinkedList<CoordinatorProxy>) mInferiorRemoteCoordinators.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * EVENT: "lost cluster member", triggered by Elector in case a member left the election 
 
 	 * @param pComChannel the comm. channel of the lost cluster member
 	 * @param pCause the cause for the call
 	 */
 	public synchronized void eventClusterMemberLost(ComChannel pComChannel, String pCause)
 	{
 		Logging.log(this, "EVENT: lost cluster member behind: " + pComChannel + "\n    cause=" + pCause);
 		
 		/**
 		 * Unregister all HRMID-2-L2Address mappings
 		 */
 		for(HRMID tHRMIDForPeer : pComChannel.getAssignedPeerHRMIDs()){
 			// unregister the old HRMID fom the local HRS and remove the mapping to the corresponding L2Address
 			Logging.log(this, "    ..removing MAPPING " + tHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address());
 			mHRMController.getHRS().unmapHRMID(tHRMIDForPeer);
 		}
 
 		/**
 		 * Free the previously allocated cluster address
 		 */
 		HRMID tPeerHRMID = pComChannel.getPeerHRMID();
 		if((tPeerHRMID != null) && (!tPeerHRMID.isZero())){
 			mDescriptionHRMIDAllocation += "\n     ..revoked " + tPeerHRMID.toString() + " for " + pComChannel + ", cause=eventClusterMemberLost()";
 
 			freeClusterMemberAddress(tPeerHRMID.getLevelAddress(getHierarchyLevel()));
 		}
 
 		/**
 		 * Unregister the comm. channel
 		 */ 
 		unregisterComChannel(pComChannel);
 
 		ControlEntity tChannelPeer = pComChannel.getPeer(); 
 		if (tChannelPeer != null){
 			/**
 			 * Update ARG
 			 */
 			mHRMController.unregisterLinkARG(this, tChannelPeer);
 
 			/**
 			 * Update locally stored database about inferior entities
 			 */
 			// does this comm. channel end at a local coordinator?
 			if(tChannelPeer instanceof Coordinator){
 				synchronized (mInferiorLocalCoordinators) {
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "      ..removing local cluster member: " + tChannelPeer);
 					}
 					if(mInferiorLocalCoordinators.contains(tChannelPeer)){
 						mInferiorLocalCoordinators.remove(tChannelPeer);
 					}else{
 						Logging.log(this, "Local inferior coordinator was already removed: " + tChannelPeer);
 					}
 				}
 			}else
 			// does this comm. channel end at a remote coordinator (a coordinator proxy)?
 			if(tChannelPeer instanceof CoordinatorProxy){
 				synchronized (mInferiorRemoteCoordinators) {
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "      ..removing remote cluster member: " + tChannelPeer);
 					}
 					if(mInferiorRemoteCoordinators.contains(tChannelPeer)){
 						mInferiorRemoteCoordinators.remove(tChannelPeer);
 					}else{
 						Logging.log(this, "Remote inferior coordinator was already removed: " + tChannelPeer);
 					}
 				}
 			}else{
 				Logging.err(this, "Comm. channel peer has unsupported type: " + tChannelPeer);
 			}
 		}
 
 		if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 			Logging.log(this, "      ..remaining comm. channels: " + getComChannels());
 			Logging.log(this, "      ..remaining connected local coordinators: " + mInferiorLocalCoordinators);
 			Logging.log(this, "      ..remaining connected remote coordinators: " + mInferiorRemoteCoordinators);
 		}
 
 		// check necessity
 		boolean tClusterStillNeeded = isClusterNeeded();
 		
 		if(tClusterStillNeeded){
 			/**
 			 * Trigger: "lost candidate" for election
 			 */		
 			if(isThisEntityValid()){
 				mElector.eventLostCandidate(pComChannel);
 			}
 
 			/**
 			 * Trigger: number of members changed
 			 */
 			eventNumberOfMembersChanged();
 		}
 	}
 
 	/**
 	 * EVENT: number of members have changed
 	 */
 	private void eventNumberOfMembersChanged()
 	{
 		if(getHierarchyLevel().isBaseLevel()){
 			if(HRMConfig.Hierarchy.AUTO_DETECT_AND_SEPRATE_GATEWAYS){
 				if(countConnectedRemoteClusterMembers() > 1){
 					if(!enforcesASSplit()){
 						Logging.warn(this, "This node is gateway - separate this cluster also as own L1 cluster");
 						setASSplit(true, true);
 					}
 				}else{
 					if(enforcesASSplit()){
 						Logging.warn(this, "This node is not anymore a gateway - merge the superior L1 cluster with the surrounding");
 						setASSplit(false, true);
 					}
 				}
 			}
 		}
 		
 		// it's time to update the GUI
 		mHRMController.notifyGUI(this);
 	}
 
 	/**
 	 * Checks the necessity of the cluster
 	 */
 	private boolean isClusterNeeded()
 	{
 		boolean tResult = true;
 		
 		Logging.log(this, "Checking necessity of this cluster");
 		// no further external candidates available/known (all candidates are gone) or has the last local inferior coordinator left the area?
 		if ((countConnectedClusterMembers() < 1 /* do we still have cluster members? */) || (mInferiorLocalCoordinators.size() == 0 /* has the last local coordinator left this cluster? */)){
 			Logging.log(this, "checkClusterNecessity() detected an unneeded cluster");
 			
 			tResult = false;
 			/**
 			 * TRIGGER: cluster invalid
 			 */
 			eventClusterRoleInvalid();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * EVENT: cluster role invalid
 	 */
 	public synchronized void eventClusterRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: cluster role invalid");
 		
 		if(isThisEntityValid()){
 			/**
 			 * Trigger: role invalid
 			 */
 			eventInvalidation();
 			
 			/**
 			 * TRIGGER: event coordinator role invalid
 			 */
 			if (getCoordinator() != null){
 				Logging.log(this, "     ..eventClusterRoleInvalid() invalidates now the local coordinator: " + getCoordinator());
 				getCoordinator().eventCoordinatorRoleInvalid();
 			}else{
 				Logging.log(this, "eventClusterInvalid() can't deactivate the coordinator because there is none");
 			}
 			
 			Logging.log(this, "============ EVENT: canceling all memberships");
 			Logging.log(this, "     ..knowing these comm. channels: " + getComChannels());
 			LinkedList<ComChannel> tcomChannels = getComChannels();
 			for(ComChannel tComChannel: tcomChannels){
 				Logging.log(this, "     ..cancelling: " + tComChannel);
 				/**
 				 * Check if we have already received an ACK for the ClusterMembershipRequest packet
 				 */
 				if(!tComChannel.isOpen()){
 					// enforce "open" state for this comm. channel
 					Logging.log(this, "   ..enforcing OPEN state for this comm. channel: " + tComChannel);
 					tComChannel.eventEstablished();
 				}
 				
 				/**
 				 * Destroy the channel
 				 */
 				unregisterComChannel(tComChannel);
 			}
 			
 			/**
 			 * Unregister from local databases
 			 */
 			Logging.log(this, "============ Destroying this cluster now...");
 			
 			// unregister the HRMID for this node from the HRM controller
 			if(getL0HRMID() != null){
 				
 				mDescriptionHRMIDAllocation += "\n     ..revoked " + getL0HRMID().toString() + " for " + this + ", cause=eventClusterRoleInvalid()";
 	
 				freeClusterMemberAddress(getL0HRMID().getLevelAddress(getHierarchyLevel()));
 	
 				mHRMController.unregisterHRMID(this, getL0HRMID(), this + "::eventClusterRoleInvalid()");					
 			}
 	
 			// unregister from HRMController's internal database
 			mHRMController.unregisterCluster(this);
 		}else{
 			Logging.warn(this, "This Cluster is already invalid");
 		}
 	}
 
 	/**
 	 * EVENT: detected additional cluster member, the event is triggered by the comm. channel
 	 * 
 	 * @param pComChannel the comm. channel of the new cluster member
 	 */
 	public synchronized void eventClusterMemberJoined(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: joined cluster member, comm. channel: " + pComChannel);
 		
 		/**
 		 * Update ARG
 		 */
 		ControlEntity tChannelPeer = pComChannel.getPeer(); 
 		if (tChannelPeer != null){
 			if (tChannelPeer instanceof Coordinator){
 				mHRMController.registerLinkARG(this, tChannelPeer, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.LOCAL_CONNECTION));
 			}else if(tChannelPeer instanceof CoordinatorProxy){
 				mHRMController.registerLinkARG(this, tChannelPeer, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.REMOTE_CONNECTION));	
 			}else{
 				Logging.err(this, "Peer (" + pComChannel.getPeer() + " is unsuported for channel: " + pComChannel);
 			}
 		}else{
 			if(!getHierarchyLevel().isBaseLevel()){
 				Logging.err(this, "Cannot link to invalid peer for channel: " + pComChannel);
 			}else{
 				// we are at base hierarchy level: the peer object is a ClusterMember object at a foreign node, there doesn't exist a representation for this entity on this node 
 			}
 		}
 
 		/**
 		 * Trigger: comm. channel established 
 		 */
 		eventComChannelEstablished(pComChannel);
 		
 		/**
 		 * Trigger: assign new HRMID
 		 */
 		if (hasLocalCoordinator()){
 			if(pComChannel.getPeerHRMID() == null){
 				eventClusterMemberNeedsHRMID(pComChannel, "eventClusterMemberJoined()");
 			}else{
 				Logging.log(this, "eventClusterMemberJoined() found an already existing peer HRMID for joined cluster member behind: " + pComChannel);
 			}
 		}else{
 			Logging.log(this, "Coordinator missing, we cannot assign a new HRMID to the joined cluster member behind comm. channel: " + pComChannel);
 		}
 		
 		eventNumberOfMembersChanged();
 	}
 
 	/**
 	 * Establishes a communication channel
 	 * 
 	 * @param pComSession the parent comm. session for the new channel
 	 * @param pRemoteEndPointName the remote EP describing ClusterName
 	 * @param pLocalEndpointName the local EP describing ClusterName
 	 * @param pPeer the control entity which represents the peer
 	 */
 	private void establishComChannel(ComSession pComSession, ClusterName pRemoteEndPointName, ClusterName pLocalEndpointName, ControlEntity pPeer)
 	{
 		Logging.log(this, "Establishing comm. channel to peer=" + pPeer + "(remoteEP=" + pRemoteEndPointName + ", localEP=" + pLocalEndpointName +")");
 		
 	    /**
 	     * Create communication channel
 	     */
 		Logging.log(this, "       ..creating new communication channel");
 		ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.OUT, this, pComSession, pPeer);
 		tComChannel.setRemoteClusterName(pLocalEndpointName);
 		tComChannel.setPeerPriority(pPeer.getPriority());
 		
 		/**
 		 * Send "RequestClusterMembership" along the comm. session
 		 * HINT: we cannot use the created channel because the remote side doesn't know anything about the new comm. channel yet)
 		 */
 		RequestClusterMembership tRequestClusterMembership = new RequestClusterMembership(mHRMController.getNodeL2Address(), pComSession.getPeerL2Address(), createClusterName(), pRemoteEndPointName);
 		Logging.log(this, "       ..sending membership request: " + tRequestClusterMembership);
 		if (pComSession.write(tRequestClusterMembership)){
 			Logging.log(this, "       ..requested successfully for membership of: " + pPeer);
 		}else{
 			Logging.err(this, "       ..failed to request for membership of: " + pPeer);
 		}
 	}
 	
 	/**
 	 * Distributes cluster membership requests
 	 * 
 	 * HINT: This function has to be called in a separate thread because it starts new connections and calls blocking functions
 	 * 
 	 */
 	private int mCountDistributeMembershipRequests = 0;
 	public void updateClusterMembers()
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS;
 		boolean tChanges = false;
 		
 		mCountDistributeMembershipRequests ++;
 		
 		/*************************************
 		 * Update local coordinators
 		 ************************************/
 		if(DEBUG){
 			Logging.log(this, "\n\n\n################ REQUESTING MEMBERSHIP FOR LOCAL COORDINATORS STARTED (call nr: " + mCountDistributeMembershipRequests + ")");
 		}
 
 		if(isThisEntityValid()){
 			LinkedList<Coordinator> tCoordinators = mHRMController.getAllCoordinators(getHierarchyLevel().getValue() - 1);
 			if(DEBUG){
 				Logging.log(this, "      ..inferior local coordinators: " + tCoordinators.size());
 			}
 			
 			/**
 			 * Copy list of inferior local coordinators
 			 */
 			LinkedList<Coordinator> tOldInferiorLocalCoordinators = null;
 			synchronized (mInferiorLocalCoordinators) {
 				tOldInferiorLocalCoordinators = (LinkedList<Coordinator>) mInferiorLocalCoordinators.clone();
 			}
 
 			/**
 			 * Iterate over all found inferior local coordinators
 			 */
 			synchronized (tOldInferiorLocalCoordinators) {
 				if(mCountDistributeMembershipRequests > 1){
 					if(DEBUG){
 						Logging.log(this, "      ..having connections to these inferior local coordinators: " + tOldInferiorLocalCoordinators.toString());
 					}
 				}
 				for (Coordinator tCoordinator : tCoordinators){
 					if (!tOldInferiorLocalCoordinators.contains(tCoordinator)){
 						if(DEBUG){
 							Logging.log(this, "      ..found inferior local coordinator [NEW]: " + tCoordinator);
 						}
 						
 						// add this local coordinator to the list of connected coordinators
 						synchronized (mInferiorLocalCoordinators) {
 							if(!mInferiorLocalCoordinators.contains(tCoordinator)){
 								mInferiorLocalCoordinators.add(tCoordinator);
 							}else{
 								Logging.err(this, "Cannot add a duplicate of the local inferior coordinator: " + tCoordinator);
 							}
 						}
 	
 						if(DEBUG){
 							Logging.log(this, "      ..get/create communication session");
 						}
 						ComSession tComSession = mHRMController.getCreateComSession(mHRMController.getNodeL2Address());		
 						if (tComSession != null){
 							/**
 							 * Create coordinator name for this coordinator
 							 */
 							ClusterName tRemoteEndPointName = tCoordinator.createCoordinatorName();
 							ClusterName tLocalEndPointName = new ClusterName(mHRMController, tRemoteEndPointName.getHierarchyLevel().inc() /* at the remote side, a CoordinatorAsClusterMember is always located at one hierarchy level above the original coordinator object */, tRemoteEndPointName.getClusterID(), tRemoteEndPointName.getCoordinatorID());
 	
 							/**
 							 * Establish the comm. channel
 							 */
 							establishComChannel(tComSession, tRemoteEndPointName, tLocalEndPointName, tCoordinator);
 							
 							tChanges = true;
 						}else{
 							Logging.err(this, "distributeMembershipRequests() couldn't determine the comm. session to: " + mHRMController.getNodeName() + " for local coordinator: " + tCoordinator);
 						}
 					}else{
 						if(DEBUG){
 							Logging.log(this, "      ..found inferior local coordinator [already connected]: " + tCoordinator);
 						}
 					}
 				}
 				if(DEBUG){
 					Logging.log(this, "    ..finished clustering of local inferior coordinators");
 				}
 	
 				/************************************
 				 * Update remote coordinators
 				 ************************************/
 				if(mInferiorLocalCoordinators.size() > 0){
 					if(DEBUG){
 						Logging.log(this, "\n\n\n################ REQUESTING MEMBERSHIP FOR REMOTE COORDINATORS STARTED");
 					}
 					LinkedList<CoordinatorProxy> tCoordinatorProxies = mHRMController.getAllCoordinatorProxies(getHierarchyLevel().getValue() - 1);
 					if(DEBUG){
 						Logging.log(this, "      ..inferior remote coordinators: " + tCoordinatorProxies.size());
 					}
 
 					/**
 					 * Copy list of known inferior remote coordinators
 					 */
 					LinkedList<CoordinatorProxy> tOldInferiorRemoteCoordinators = null;
 					synchronized (mInferiorRemoteCoordinators) {
 						tOldInferiorRemoteCoordinators = (LinkedList<CoordinatorProxy>) mInferiorRemoteCoordinators.clone();
 					}
 
 					/************************************
 					 * Drop old (invalid) remote coordinators
 					 ************************************/
 					synchronized (tOldInferiorRemoteCoordinators) {
 						for(CoordinatorProxy tCoordintorProxy : tOldInferiorRemoteCoordinators){
 							if(DEBUG){
 								Logging.log(this, "Found inferior remote coordinator behind proxy: " + tCoordintorProxy);
 							}
 							
 							if(!tCoordintorProxy.isThisEntityValid()){
 								if(DEBUG){
									Logging.log(this, "Found already invalided coordinator proxy: " + tCoordintorProxy);
 								}
 								ComChannel tComChannelToRemoteCoordinator = getComChannelToMember(tCoordintorProxy);
 								if(tComChannelToRemoteCoordinator != null){
 									if(DEBUG){
 										Logging.log(this, "   ..comm. channel is: " + tComChannelToRemoteCoordinator);
 										Logging.log(this, "   ..deactivating membership of: " + tCoordintorProxy);
 									}
 									eventClusterMemberLost(tComChannelToRemoteCoordinator, this + "::updateClusterMembers() detected invalid remote coordinator behind: " + tCoordintorProxy);
 									
 									tChanges = true;
 								}
 							}
 						}
 					}
 					
 					/************************************
 					 * Drop deprecated entry in list of connected remote coordinators
 					 ************************************/
 					synchronized (mInferiorRemoteCoordinators) {
 						LinkedList<CoordinatorProxy> tAllCoordinatorProxies = mHRMController.getAllCoordinatorProxies(getHierarchyLevel().dec().getValue());
 						for(CoordinatorProxy tCoordintorProxy : tAllCoordinatorProxies){
 							if(mInferiorRemoteCoordinators.contains(tCoordintorProxy)){
 								if(tCoordintorProxy.isThisEntityValid()){
 									ComChannel tComChannelToRemoteCoordinator = getComChannelToMember(tCoordintorProxy);
 									if(tComChannelToRemoteCoordinator == null){
 										if(DEBUG){
 											Logging.warn(this, "Found temporary invalided and returned coordinator proxy: " + tCoordintorProxy);
 										}
 										mInferiorRemoteCoordinators.remove(tCoordintorProxy);
 										tOldInferiorRemoteCoordinators = (LinkedList<CoordinatorProxy>) mInferiorRemoteCoordinators.clone();
 									}								
 								}
 							}
 						}
 					}
 
 					/*************************************
 					 * Detect new remote coordinators
 					 ************************************/
 					if(tCoordinatorProxies.size() > 0){
 						synchronized (tOldInferiorRemoteCoordinators) {
 							if(mCountDistributeMembershipRequests > 1){
 								if(DEBUG){
 									Logging.log(this, "      ..having connections to these inferior remote coordinators: " + tOldInferiorRemoteCoordinators.toString());
 								}
 							}
 							for (CoordinatorProxy tCoordinatorProxy : tCoordinatorProxies){
 								if (!tOldInferiorRemoteCoordinators.contains(tCoordinatorProxy)){
 									if(DEBUG){
 										Logging.log(this, "      ..found remote inferior coordinator[NEW]: " + tCoordinatorProxy);
 									}
 									
 									// add this remote coordinator to the list of connected coordinators
 									synchronized (mInferiorRemoteCoordinators) {
 										if(!mInferiorRemoteCoordinators.contains(tCoordinatorProxy)){
 											mInferiorRemoteCoordinators.add(tCoordinatorProxy);
 										}else{
 											Logging.err(this, "Cannot add a duplicate of the remote inferior coordinator: " + tCoordinatorProxy);
 										}
 									}
 									
 									ComSession tComSession = mHRMController.getCreateComSession(tCoordinatorProxy.getCoordinatorNodeL2Address());		
 									if (tComSession != null){
 										/**
 										 * Create coordinator name for this coordinator proxy
 										 */
 										ClusterName tRemoteEndPointName = tCoordinatorProxy.createCoordinatorName();
 										ClusterName tLocalEndPointName = new ClusterName(mHRMController, tRemoteEndPointName.getHierarchyLevel().inc() /* at the remote side, a CoordinatorAsClusterMember is always located at one hierarchy level above the original coordinator object */, tRemoteEndPointName.getClusterID(), tRemoteEndPointName.getCoordinatorID());
 										
 										/**
 										 * Establish the comm. channel
 										 */
 										establishComChannel(tComSession, tRemoteEndPointName, tLocalEndPointName, tCoordinatorProxy);
 										
 										tChanges = true;
 									}else{
 										Logging.err(this, "distributeMembershipRequests() couldn't determine the comm. session to: " + tCoordinatorProxy.getCoordinatorNodeL2Address() + " for remote coordinator: " + tCoordinatorProxy);
 									}
 								}else{
 									if(DEBUG){
 										Logging.log(this, "      ..found inferior remote coordinator [already connected]: " + tCoordinatorProxy);
 									}
 								}
 							}
 						}
 					}else{
 						/**
 						 * Trigger: detected local isolation
 						 */
 						eventDetectedIsolation();
 					}
 				}else{
 					if(DEBUG){
 						Logging.log(this, "  ..no local inferior coordinators existing");
 					}
 				}
 			}
 		}else{
 			Logging.warn(this, "updateClusterMembers() skipped because cluster role is already invalidated");
 		}
 		
 		// finally, check the necessity of this cluster again
 		if(!isClusterNeeded()){
 			tChanges = false;
 		}
 		
 		/**
 		 * trigger election update if changes happened
 		 */
 		if(tChanges){
 			Logging.log(this, "updateClusterMembers[" + mCountDistributeMembershipRequests + "] triggers a re-election due to topology changes");
 			mElector.startElection(this + "::updateClusterMembers()[" + mCountDistributeMembershipRequests + "]");
 		}else{
 			Logging.log(this, "updateClusterMembers[" + mCountDistributeMembershipRequests + "] detected no topology changes");
 		}
 	}
 
 	/**
 	 * EVENT: detected isolation
 	 */
 	private void eventDetectedIsolation()
 	{
 		if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 			Logging.log(this, "EVENT: detected local isolation");
 		}
 	}
 
 	/**
 	 * Defines the decoration text for the ARG viewer
 	 * 
 	 * @return text for the control entity or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return null;
 	}
 
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return toLocation() + "(" + idToString() + ")";
 	}
 
 	/**
 	 * Returns a location description about this instance
 	 * 
 	 * @return the location description
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + (getGUIClusterID() != -1 ? Long.toString(getGUIClusterID()) : "??") + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns a string including the ClusterID, the coordinator ID, and the node priority
 	 * 
 	 * @return the complex string
 	 */
 	private String idToString()
 	{
 		if ((getHRMID() == null) || (getHRMID().isRelativeAddress())){
 			return "Coordinator" + getGUICoordinatorID();
 		}else{
 			return "Coordinator" + getGUICoordinatorID() + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
