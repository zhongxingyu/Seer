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
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.TopologyReport;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
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
 	private static long sNextFreeClusterID = 1;
 	
 	/**
 	 * Stores a reference to the local coordinator instance if the local router is also the coordinator for this cluster
 	 */
 	private Coordinator mCoordinator = null;
 	
 	/**
 	 * Stores the connect inferior local coordinators.
 	 */
 	private LinkedList<Coordinator> mInferiorLocalCoordinators = new LinkedList<Coordinator>();
 	
 	/**
 	 * Stores the connect inferior local coordinators.
 	 */
 	private LinkedList<CoordinatorProxy> mInferiorRemoteCoordinators = new LinkedList<CoordinatorProxy>();
 
 	/**
 	 * Stores a description of former GUICoordinatorIDs
 	 */
 	private String mDescriptionFormerGUICoordinatorIDs = "";
 	
 	/**
 	 * Stores a list of the already used addresses
 	 */
 	private LinkedList<Integer> mUsedAddresses = new LinkedList<Integer>();
 	
 	/**
 	 * Stores how many address broadcasts were already sent
 	 */
 	private int mSentAddressBroadcast = 0;
 
 	/**
 	 * Stores the 
 	 */
 	private HRMID mHRMIDLastDistribution = null;	
 	
 	/**
 	 * Stores a description about the HRMID allocations
 	 */
 	private String mDescriptionHRMIDAllocation = new String();
 	
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
 
 		// creates new elector object, which is responsible for Bully based election processes
 		tResult.mElector = new Elector(pHRMController, tResult);
 
 		return tResult;
 	}
 
 	/**
 	 * Factory function: create a cluster
 	 * 
 	 * @param pHRMController the local HRMController instance
 	 * @param pClusterName the ClusterName for the new cluster object
 	 * 
 	 * @return the new Cluster object
 	 */
 	static public Cluster create(HRMController pHRMController, ClusterName pClusterName)
 	{
 		return create(pHRMController, pClusterName.getHierarchyLevel(), pClusterName.getClusterID());
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
 	 * Returns the correct connectivity/hierarchy Bully priority of the node
 	 * 
 	 * @return the Bully priority
 	 */
 	@Override
 	public BullyPriority getPriority() 
 	{
 		if(getHierarchyLevel().isBaseLevel()){
 			return BullyPriority.create(this, mHRMController.getConnectivityNodePriority());
 		}else{
 			return BullyPriority.create(this, mHRMController.getHierarchyNodePriority(getHierarchyLevel()));
 		}
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
 	 * DISTRIBUTE: distribute addresses among cluster members if:
 	 *           + an HRMID was received from a superior coordinator, used to distribute HRMIDs downwards the hierarchy,
 	 *           + we were announced as coordinator
 	 * This function is called for distributing HRMIDs among the cluster members.
 	 */
 	public void distributeAddresses()
 	{
 		HRMID tOwnHRMID = getHRMID();
 
 		// do we have a new HRMID since the last call?
 		if ((mHRMIDLastDistribution == null) || (!mHRMIDLastDistribution.equals(tOwnHRMID))){
 			// update the stored HRMID
 			mHRMIDLastDistribution = tOwnHRMID;
 			
 			/**
 			 * Distribute addresses
 			 */
 			if ((getHierarchyLevel().isHighest() /* top of hierarchy? */) || ((tOwnHRMID != null) && ((HRMConfig.Addressing.DISTRIBUTE_RELATIVE_ADDRESSES) || (!tOwnHRMID.isRelativeAddress()) /* we already have been assigned a valid HRMID? */))){
 				mSentAddressBroadcast++;
 				
 				Logging.log(this, "DISTRIBUTING ADDRESSES [" + mSentAddressBroadcast + "] to entities at level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 				
 				/**
 				 * Assign ourself an HRMID address
 				 */
 				// are we at the base level?
 				if(getHierarchyLevel().isBaseLevel()) {
 					// create new HRMID for ourself
 					HRMID tThisNodesAddress = allocateClusterMemberAddress();
 					if((tThisNodesAddress != null) && (!tThisNodesAddress.equals(getL0HRMID()))){
 						// inform HRM controller about the address change
 						if(getL0HRMID() != null){
 							
 							// free only if the assigned new address has a different used cluster address
 							if(tThisNodesAddress.getLevelAddress(getHierarchyLevel()) != getL0HRMID().getLevelAddress(getHierarchyLevel())){
 								freeClusterMemberAddress(getL0HRMID().getLevelAddress(getHierarchyLevel()));
 							}
 		
 						}
 
 						mDescriptionHRMIDAllocation += "\n     .." + tThisNodesAddress.toString() + " for " + this + ", cause=distributeAddresses() [" + mSentAddressBroadcast + "]";
 						
 						Logging.log(this, "    ..setting local HRMID " + tThisNodesAddress.toString());
 			
 						// store the new HRMID for this node
 						setL0HRMID(tThisNodesAddress);
 					}else{
 						if(tThisNodesAddress == null){
 							throw new RuntimeException(this + "::distributeAddresses() got a zero HRMID from allocateClusterMemberAddress()");
 						}
 						
 						// free the allocated address again
 						freeClusterMemberAddress(tThisNodesAddress.getLevelAddress(getHierarchyLevel()));
 					}
 				}
 		
 				/**
 				 * Distribute AssignHRMID packets among the cluster members 
 				 */
 				LinkedList<ComChannel> tComChannels = getComChannels();
 				
 				Logging.log(this, "    ..distributing HRMIDs among cluster members: " + tComChannels);
 				int i = 0;
 				for(ComChannel tComChannel : tComChannels) {
 					/**
 					 * Trigger: cluster member needs HRMID
 					 */
 					Logging.log(this, "   ..[" + i + "]: assigning HRMID to: " + tComChannel);
 					eventClusterMemberNeedsHRMID(tComChannel, "distributeAddresses() [" + mSentAddressBroadcast + "]");
 					i++;
 				}
 				
 				/**
 				 * Announce the local node HRMIDs if we are at base hierarchy level
 				 */
 				if(getHierarchyLevel().isBaseLevel()){
 					LinkedList<Cluster> tL0Clusters = mHRMController.getAllClusters(0);
 					for(Cluster tL0Cluster : tL0Clusters){
 						tL0Cluster.distributeAnnounceHRMIDs();
 					}
 				}
 			}
 		}else{
 			Logging.log(this, "distributeAddresses() skipped because the own HRMID is still the same: " + getHRMID());
 		}
 	}
 
 	/**
 	 * EVENT: new HRMID assigned
      * The function is called when an address update was received.
 	 * 
 	 * @param pHRMID the new HRMID
 	 */
 	@Override
 	public void eventAssignedHRMID(HRMID pHRMID)
 	{
 		if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 			Logging.log(this, "Handling AssignHRMID with assigned HRMID " + pHRMID.toString());
 		}
 
 		if((pHRMID != null) && (!pHRMID.isZero())){
 			// setHRMID()
 			super.eventAssignedHRMID(pHRMID);
 		
 			/**
 			 * Automatic address distribution via this cluster
 			 */
 			if (hasLocalCoordinator()){
 				// we should automatically continue the address distribution?
 				if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 					if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 						Logging.log(this, "     ..continuing the address distribution process via this cluster");
 					}
 					distributeAddresses();				
 				}			
 			}else{
 				if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.log(this, "     ..stopping address propagation here");
 				}
 			}
 		}
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
 		/**
 		 * Do we have a new HRMID?
 		 */
 		if((pHRMID == null) || (!pHRMID.equals(getHRMID()))){
 			/**
 			 * Reset the list of used addresses because we got a new HRMID
 			 */
 			synchronized (mUsedAddresses) {
 				Logging.log(this, "Resetting list of used addresses: " + mUsedAddresses);
 				mUsedAddresses.clear();
 			}
 			
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
 	}
 
 	/**
 	 * Allocates a new HRMID for a cluster member.
 	 * 
 	 * @return the allocated HRMID for the cluster member
 	 */
 	private HRMID allocateClusterMemberAddress()
 	{
 		HRMID tResult = (getHRMID() != null ? getHRMID().clone() : null);
 
 		if(tResult != null){
 			/**
 			 * Search for the lowest free address
 			 */
 			int tUsedAddress = 0;
 			synchronized (mUsedAddresses) {
 				// iterate over all possible addresses
 				for(tUsedAddress = 1; tUsedAddress < 1024 /* TODO: 2 ^ digits from HRMConfig */; tUsedAddress++){
 					// have we found a free address?
 					if(!mUsedAddresses.contains(tUsedAddress)){
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
 		}else{
 			tResult = null;
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
 	 * EVENT: cluster member needs HRMID
 	 * 
 	 * @param pComChannel the comm. channel towards the cluster member, which needs a new HRMID
 	 * @param pCause the cause for this event
 	 */
 	public void eventClusterMemberNeedsHRMID(ComChannel pComChannel, String pCause)
 	{
 		Logging.log(this, "EVENT: Cluster_Member_Needs_HRMID for: " + pComChannel + ", cause=" + pCause);
 		
 		/**
 		 * AUTO ADDRESS DISTRIBUTION
 		 */
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			HRMID tOldHRMIDForPeer = pComChannel.getPeerHRMID(); 
 			HRMID tHRMIDForPeer = null;
 			/**
 			 * Check old assignment
 			 */
 			Logging.log(this, "   ..old peer HRMID: " + tOldHRMIDForPeer);
 			if(!HRMConfig.Addressing.REUSE_ADDRESSES){
 				Logging.log(this, "     ..reseting the old HRMID to null because address reusage is disabled");
 				tOldHRMIDForPeer = null;
 			}
 			if((tOldHRMIDForPeer != null) && (!tOldHRMIDForPeer.isZero()) && ((tOldHRMIDForPeer.isCluster(getHRMID())) || (getHierarchyLevel().isHighest()))){
 				int tUsedAddress = tOldHRMIDForPeer.getLevelAddress(getHierarchyLevel());
 				synchronized (mUsedAddresses) {
 					if(!mUsedAddresses.contains(tUsedAddress)){
 						Logging.log(this, "     ..mark the address as used in this cluster");
 						// add the peer address to the used addresses
 						mUsedAddresses.add(tUsedAddress);
 					}else{
 						Logging.log(this, "     ..this address is already used, allocating a new one");
 						// the formerly used address isn't available anymore
 						tOldHRMIDForPeer = null;
 					}
 				}
 			}
 					
 			/**
 			 * Create a new HRMID for the peer
 			 */
 			if((tOldHRMIDForPeer == null) || (tOldHRMIDForPeer.isZero()) || (tOldHRMIDForPeer.isRelativeAddress()) || (!tOldHRMIDForPeer.isCluster(getHRMID()))){
 				HRMID tNewHRMIDForPeer = allocateClusterMemberAddress();
 				if(tNewHRMIDForPeer != null){
 					mDescriptionHRMIDAllocation += "\n     .." + tNewHRMIDForPeer.toString() + " for " + pComChannel + ", cause=" + pCause;
 	
 					/**
 					 * Abort if we shouldn't distribute relative addresses
 					 */
 					if((!HRMConfig.Addressing.DISTRIBUTE_RELATIVE_ADDRESSES) && (tNewHRMIDForPeer.isRelativeAddress())){
						//Logging.warn(this, "eventClusterMemberNeedsHRMID() aborted because the relative address shouldn't be distributed: " + tNewHRMIDForPeer);
 						
 						freeClusterMemberAddress(tNewHRMIDForPeer.getLevelAddress(getHierarchyLevel()));
 						
 						return;
 					}
 					
 					// store the HRMID under which the peer will be addressable from now 
 					pComChannel.setPeerHRMID(tNewHRMIDForPeer);
 	
 					// register this new HRMID in the local HRS and create a mapping to the right L2Address
 					Logging.log(this, "    ..creating MAPPING " + tNewHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address());
 					mHRMController.getHRS().mapHRMID(tNewHRMIDForPeer, pComChannel.getPeerL2Address());
 					
 					tHRMIDForPeer = tNewHRMIDForPeer;
 				}
 			}else{
 				Logging.log(this, "    ..reassigning " + tOldHRMIDForPeer.toString() + " for " + pComChannel);
 				tHRMIDForPeer = tOldHRMIDForPeer;
 			}
 	
 			// send the packet in every case
 			if(tHRMIDForPeer != null){
 				if ((pComChannel.getPeerHRMID() != null) && (!pComChannel.getPeerHRMID().equals(tHRMIDForPeer))){
 					Logging.log(this, "    ..replacing HRMID " + pComChannel.getPeerHRMID().toString() + " and assign new HRMID " + tHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address());
 				}else
 					Logging.log(this, "    ..assigning new HRMID " + tHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address());
 	
 				pComChannel.signalAssignHRMID(tHRMIDForPeer);
 			}else{
 				Logging.warn(this, "eventClusterMemberNeedsHRMID() detected invalid cluster HRMID and cannot signal new HRMID to: " + pComChannel);
 			}
 		}else{
 			Logging.log(this, "Address distribution is deactivated, no new assigned HRMID for: " + pComChannel);
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
 			Logging.log(this, "   ..[" + i + "]: recoking HRMIDs via: " + tComChannel);
 			tComChannel.signalRevokeAssignedHRMIDs();
 			
 			i++;
 		}
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
 			setClusterActivation(true);
 		}else{
 			// mark this cluster as inactive
 			setClusterActivation(false);
 		}
 		
 		// update the stored unique ID for the coordinator
 		if (pCoordinator != null){
 			setSuperiorCoordinatorID(pCoordinator.getCoordinatorID());
 			setCoordinatorID(pCoordinator.getCoordinatorID());			
 
 			// update the descriptive string about the coordinator
 			setSuperiorCoordinatorDescription(mCoordinator.toLocation());
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
 		
 		/**
 		 * Revoke HRMID of physical node if we are on base hierarchy level
 		 */ 
 		if(getHierarchyLevel().isBaseLevel()){
 			Logging.log(this, "Revoking physical node HRMID: " + getHRMID());
 			
 			eventRevokedHRMID(this, getHRMID());
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
 	 * EVENT: TopologyReport from an inferior entity received, triggered by the comm. channel
 	 * 
 	 * @param pTopologyReportPacket the packet
 	 */
 	public void eventTopologyReport(TopologyReport pTopologyReportPacket)
 	{
 		Logging.log(this, "EVENT: TopologyReport: " + pTopologyReportPacket);
 		
 		/**
 		 * Iterate over all reported routes and derive new data for the HRG
 		 */
 		for(RoutingEntry tEntry : pTopologyReportPacket.getRoutes()){
 			if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 				Logging.log(this, "   ..received route: " + tEntry);
 			}
 			HRMID tDestHRMID = tEntry.getDest();
 			HRMID tOwnClusterAddress = getHRMID();
 			if(tDestHRMID != null){
 				// search for cluster destinations
 				if(tDestHRMID.isClusterAddress()){
 					// avoid recursion
 					if(!tDestHRMID.equals(tOwnClusterAddress)){
 						if(HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 							Logging.log(this, "     ..route between clusters found");						
 						}
 						
 						/**
 						 * Store/update link in the HRG
 						 */ 
 //						mHRMController.registerCluster2ClusterLinkHRG(tEntry.getSource(), tDestHRMID, tEntry);
 					}
 				}				
 			}else{
 				Logging.err(this,  "Invalid route received: " + tEntry);
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "lost cluster member", triggered by Elector in case a member left the election 
 
 	 * @param pComChannel the comm. channel of the lost cluster member
 	 */
 	public void eventClusterMemberLost(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: lost cluster member, comm. channel: " + pComChannel);
 		
 		/**
 		 * Unregister all HRMID-2-L2Address mappings
 		 */
 		for(HRMID tHRMIDForPeer : pComChannel.getAssignedPeerHRMIDs()){
 			// unregister the old HRMID fom the local HRS and remove the mapping to the corresponding L2Address
 			Logging.log(this, "    ..removing MAPPING " + tHRMIDForPeer.toString() + " to " + pComChannel.getPeerL2Address());
 			mHRMController.getHRS().unmapHRMID(tHRMIDForPeer);
 		}
 
 		/**
 		 * Unregister the comm. channel
 		 */ 
 		unregisterComChannel(pComChannel);
 
 		/**
 		 * Update ARG
 		 */
 		ControlEntity tChannelPeer = pComChannel.getPeer(); 
 		if (tChannelPeer != null){
 			mHRMController.unregisterLinkARG(this, tChannelPeer);
 
 			// does this comm. channel end at a local coordinator?
 			if(tChannelPeer instanceof Coordinator){
 				synchronized (mInferiorLocalCoordinators) {
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "      ..removing local cluster member: " + tChannelPeer);
 					}
 					mInferiorLocalCoordinators.remove(tChannelPeer);					
 				}
 			}else
 			// does this comm. channel end at a remote coordinator (a coordinator proxy)?
 			if(tChannelPeer instanceof CoordinatorProxy){
 				synchronized (mInferiorRemoteCoordinators) {
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "      ..removing remote cluster member: " + tChannelPeer);
 					}
 					mInferiorRemoteCoordinators.remove(tChannelPeer);					
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
 		checkClusterNecessity();
 	}
 
 	/**
 	 * Checks the necessity of the cluster
 	 */
 	private void checkClusterNecessity()
 	{
 		// no further external candidates available/known (all candidates are gone) or has the last local inferior coordinator left the area?
 		if ((countConnectedClusterMembers() < 1 /* do we still have cluster members? */) || (mInferiorLocalCoordinators.size() == 0 /* has the last local coordinator left this cluster? */)){
 			/**
 			 * TRIGGER: cluster invalid
 			 */
 			eventClusterRoleInvalid();
 		}
 	}
 	
 	/**
 	 * EVENT: cluster role invalid
 	 */
 	public void eventClusterRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: cluster role invalid");
 		
 		/**
 		 * Trigger: Elector invalid
 		 */
 		getElector().eventInvalidation();
 
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
 			Logging.log(this, "     ..canceling: " + tComChannel);
 			destroyComChannel(tComChannel);
 		}
 		
 		/**
 		 * Unregister from local databases
 		 */
 		Logging.log(this, "============ Destroying this cluster now...");
 		
 		// unregister the HRMID for this node from the HRM controller
 		if(getL0HRMID() != null){
 			
 			freeClusterMemberAddress(getL0HRMID().getLevelAddress(getHierarchyLevel()));
 
 			mHRMController.unregisterHRMID(this, getL0HRMID(), this + "::eventClusterRoleInvalid()");					
 		}
 
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCluster(this);
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
 	}
 
 	/**
 	 * Destroys a communication channel
 	 * 
 	 * @param pComChannel the communication channel
 	 */
 	private void destroyComChannel(ComChannel pComChannel)
 	{
 		Logging.log(this, "Destroying comm. channel to peer=" + pComChannel.getPeer() + "(remoteEP=" + pComChannel.getRemoteClusterName() +")");
 		
 		// unregister the comm. channel
 		unregisterComChannel(pComChannel);
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
 		ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.OUT, this, pComSession, pPeer); //TODO: statt sofort den kanal aufzubauen lieber auf das ACK warten und dann aufbauen, andernfalls koennen karteileichen entstehen wenn der remote bereits den zielkoordinator nicht mehr kennt und nie ein ACK schickt -> UPDATE: der remote schickt jetzt autonmatisch eine verweigerung sobald der einen request bekommt, wo der koordinator bereits gekillt ist
 		tComChannel.setRemoteClusterName(pLocalEndpointName);
 		tComChannel.setPeerPriority(pPeer.getPriority());
 		
 		/**
 		 * Send "RequestClusterMembership" along the comm. session
 		 * HINT: we cannot use the created channel because the remote side doesn't know anything about the new comm. channel yet)
 		 */
 		RequestClusterMembership tRequestClusterMembership = new RequestClusterMembership(mHRMController.getNodeName(), pComSession.getPeerL2Address(), createClusterName(), pRemoteEndPointName);
 		Logging.log(this, "       ..sending membership request: " + tRequestClusterMembership);
 		if (pComSession.write(tRequestClusterMembership)){
 			Logging.log(this, "       ..requested sucessfully for membership of: " + pPeer);
 		}else{
 			Logging.err(this, "       ..failed to request for membership of: " + pPeer);
 		}
 	}
 	
 	/**
 	 * Distributes cluster membership requests
 	 * HINT: This function has to be called in a separate thread because it starts new connections and calls blocking functions
 	 * 
 	 */
 	private int mCountDistributeMembershipRequests = 0;
 	public void distributeMembershipRequests()
 	{
 		mCountDistributeMembershipRequests ++;
 		
 		/*************************************
 		 * Request for local coordinators
 		 ************************************/
 		if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 			Logging.log(this, "\n\n\n################ REQUESTING MEMBERSHIP FOR LOCAL COORDINATORS STARTED (call nr: " + mCountDistributeMembershipRequests + ")");
 		}
 
 		if(isThisEntityValid()){
 			LinkedList<Coordinator> tCoordinators = mHRMController.getAllCoordinators(getHierarchyLevel().getValue() - 1);
 			
 			/**
 			 * Copy list of inferior local coordinators
 			 */
 			LinkedList<Coordinator> tInferiorLocalCoordinators = null;
 			synchronized (mInferiorLocalCoordinators) {
 				tInferiorLocalCoordinators = (LinkedList<Coordinator>) mInferiorLocalCoordinators.clone();
 			}
 
 			/**
 			 * Iterate over all found inferior local coordinators
 			 */
 			synchronized (tInferiorLocalCoordinators) {
 				if(mCountDistributeMembershipRequests > 1){
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "      ..having connections to these inferior local coordinators: " + tInferiorLocalCoordinators.toString());
 					}
 				}
 				for (Coordinator tCoordinator : tCoordinators){
 					if (!tInferiorLocalCoordinators.contains(tCoordinator)){
 						if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 							Logging.log(this, "      ..found inferior local coordinator [NEW]: " + tCoordinator);
 						}
 						
 						// add this local coordinator to the list of connected coordinators
 						synchronized (mInferiorLocalCoordinators) {
 							mInferiorLocalCoordinators.add(tCoordinator);
 						}
 	
 						if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 							Logging.log(this, "      ..get/create communication session");
 						}
 						ComSession tComSession = mHRMController.getCreateComSession(mHRMController.getNodeL2Address());		
 						if (tComSession != null){
 							/**
 							 * Create coordinator name for this coordinator
 							 */
 							ClusterName tRemoteEndPointName = tCoordinator.createCoordinatorName();
 							ClusterName tLocalEndPointName = new ClusterName(mHRMController, new HierarchyLevel(this, tRemoteEndPointName.getHierarchyLevel().getValue() + 1 /* at the remote side, a CoordinatorAsClusterMember is always located at one hierarchy level above the original coordinator object */ ), tRemoteEndPointName.getClusterID(), tRemoteEndPointName.getCoordinatorID());
 	
 							/**
 							 * Establish the comm. channel
 							 */
 							establishComChannel(tComSession, tRemoteEndPointName, tLocalEndPointName, tCoordinator);
 						}else{
 							Logging.err(this, "distributeMembershipRequests() couldn't determine the comm. session to: " + mHRMController.getNodeName() + " for local coordinator: " + tCoordinator);
 						}
 					}else{
 						if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 							Logging.log(this, "      ..found inferior local coordinator [already connected]: " + tCoordinator);
 						}
 					}
 				}
 	
 				/************************************
 				 * Requests for remote coordinators
 				 ************************************/
 				if(tInferiorLocalCoordinators.size() > 0){
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "\n\n\n################ REQUESTING MEMBERSHIP FOR REMOTE COORDINATORS STARTED");
 					}
 					LinkedList<CoordinatorProxy> tCoordinatorProxies = mHRMController.getAllCoordinatorProxies(getHierarchyLevel().getValue() - 1);
 					
 					if(tCoordinatorProxies.size() > 0){
 						/**
 						 * Copy list of inferior local coordinators
 						 */
 						LinkedList<Coordinator> tInferiorRemoteCoordinators = null;
 						synchronized (mInferiorRemoteCoordinators) {
 							tInferiorRemoteCoordinators = (LinkedList<Coordinator>) mInferiorRemoteCoordinators.clone();
 						}
 
 						/**
 						 * Iterate over all found remote coordinators
 						 */
 						synchronized (tInferiorRemoteCoordinators) {
 							if(mCountDistributeMembershipRequests > 1){
 								if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 									Logging.log(this, "      ..having connections to these inferior remote coordinators: " + tInferiorRemoteCoordinators.toString());
 								}
 							}
 							for (CoordinatorProxy tCoordinatorProxy : tCoordinatorProxies){
 								if (!tInferiorRemoteCoordinators.contains(tCoordinatorProxy)){
 									if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 										Logging.log(this, "      ..found remote inferior coordinator[NEW]: " + tCoordinatorProxy);
 									}
 									
 									// add this remote coordinator to the list of connected coordinators
 									synchronized (mInferiorRemoteCoordinators) {
 										mInferiorRemoteCoordinators.add(tCoordinatorProxy);
 									}
 									
 									ComSession tComSession = mHRMController.getCreateComSession(tCoordinatorProxy.getCoordinatorNodeL2Address());		
 									if (tComSession != null){
 										/**
 										 * Create coordinator name for this coordinator proxy
 										 */
 										ClusterName tRemoteEndPointName = tCoordinatorProxy.createCoordinatorName();
 										ClusterName tLocalEndPointName = new ClusterName(mHRMController, new HierarchyLevel(this, tRemoteEndPointName.getHierarchyLevel().getValue() + 1 /* at the remote side, a CoordinatorAsClusterMember is always located at one hierarchy level above the original coordinator object */ ), tRemoteEndPointName.getClusterID(), tRemoteEndPointName.getCoordinatorID());
 										
 										/**
 										 * Establish the comm. channel
 										 */
 										establishComChannel(tComSession, tRemoteEndPointName, tLocalEndPointName, tCoordinatorProxy);
 									}else{
 										Logging.err(this, "distributeMembershipRequests() couldn't determine the comm. session to: " + mHRMController.getNodeName() + " for remote coordinator: " + tCoordinatorProxy);
 									}
 								}else{
 									if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
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
 					// no local inferior coordinators -> this cluster isn't needed
 				}
 			}
 		}else{
 			Logging.warn(this, "distributeMembershipRequests() skipped because cluster role is already invalidated");
 		}
 		
 		// finally, check the necessity of this cluster again
 		checkClusterNecessity();		
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
