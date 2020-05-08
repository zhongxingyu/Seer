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
 
 
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembershipAck;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a cluster head at a defined hierarchy level.
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
 	 * Stores the network interface for base hierarchy level
 	 */
 	private NetworkInterface mBaseHierarchyLevelNetworkInterface = null;
 	
 	/**
 	 * Stores a reference to the local coordinator instance if the local router is also the coordinator for this cluster
 	 */
 	private Coordinator mCoordinator = null;
 	
 	/**
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
 	static private Cluster create(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID)
 	{
 		Cluster tResult = new Cluster(pHRMController, pHierarchyLevel, pClusterID);
 		
 		// detect neighbor clusters (members), increase the Bully priority based on the local connectivity
 		tResult.initializeNeighborhood();
 
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
 	 * EVENT: cluster communication available, triggered by the comm. session 
 	 */
 	public void eventCommunicationAvailable()
 	{
 		Logging.log(this, "EVENT: communication available");
 		
 		boolean tStartBaseLevel =  ((getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.START_AUTOMATICALLY_BASE_LEVEL));
 		
 		// start coordinator election for the created HRM instance if desired
 		if(((!getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY)) || (tStartBaseLevel)){
 			Logging.log(this, "      ..starting ELECTION");
 			mElector.startElection();
 		}
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
 	 * Forwards a coordinator announcement by sending it to all cluster members
 	 * @param pSourceL2Address 
 	 * 
 	 * @param pAnnounceCoordinator the coordinator announcement
 	 */
 	public void forwardCoordinatorAnnouncement(L2Address pSourceL2Address, AnnounceCoordinator pAnnounceCoordinator)
 	{
 		Logging.log(this, "Forwarding coordinator announcement: " + pAnnounceCoordinator);
 	
 		// check the TTL once more
 		if(pAnnounceCoordinator.isTTLOkay()){
 			// forward this announcement to all cluster members
			sendClusterBroadcast(pAnnounceCoordinator, pSourceL2Address);
 		}else{
 			Logging.err(this, "forwardCoordinatorAnnouncement() found invalid TTL for: " + pAnnounceCoordinator);
 		}
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
 	 * EVENT: new local coordinator, triggered by the Coordinator
 	 * 
 	 * @param pCoordinator the new coordinator, which is located on this node
 	 */
 	public void eventNewLocalCoordinator(Coordinator pCoordinator)
 	{
 		Logging.log(this, "EVENT: new local coordinator: " + pCoordinator + ", old one is: " + mCoordinator);
 		
 		// set the coordinator
 		mCoordinator = pCoordinator;
 		
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
 		Logging.log(this, "EVENT: coordiantor was lost");
 		
 		/**
 		 * Revoke HRMID of physical node if we are on base hierarchy level
 		 */ 
 		if(getHierarchyLevel().isBaseLevel()){
 			Logging.log(this, "Revoking physical node HRMID: " + getHRMID());
 			
 			eventRevokedHRMID(this, getHRMID());
 		}
 	}
 	
 	/**
 	 * EVENT: notifies that a communication channel is became available
 	 * 
 	 * @param pComChannel the communication channel which became available
 	 */
 	public void eventComChannelEstablished(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: ComChannel established for " + pComChannel);
 		
 		boolean tCoordinatorIsFixed = false;
 		
 		// does the coordinator already know its superior coordinator?
 		if (getCoordinator() != null){
 			if (getCoordinator().superiorCoordinatorKnown()){
 				tCoordinatorIsFixed = true;
 			}
 		}
 		
 		/**
 		 * TRIGGER: election restart (do this only if election was already started)
 		 */
 		if (!tCoordinatorIsFixed){
 			if (mElector.wasStarted()){
 				mElector.startElection();
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "lost cluster member", triggered by Elector in case a member left the election 
 
 	 * @param pComChannel the comm. channel of the lost cluster member
 	 */
 	@Override
 	public void eventClusterMemberLost(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: lost cluster member, comm. channel: " + pComChannel);
 		
 		// unregister the comm. channel
 		unregisterComChannel(pComChannel);
 		
 		Logging.log(this, "      ..remaining comm. channels: " + getComChannels());
 
 		// no further external candidates available/known (all candidates are gone) ?
 		if (countConnectedClusterMembers() < 1){
 			/**
 			 * TRIGGER: all cluster members are gone, we destroy the coordinator
 			 */
 			if (getCoordinator() != null){
 				getCoordinator().eventCoordinatorRoleInvalid();
 			}else{
 				Logging.warn(this, "eventClusterMemberLost() can't find the coordinator");
 			}
 			/**
 			 * TRIGGER: all cluster members are gone, we destroy the cluster
 			 */
 			eventClusterLostAllMembers();
 		}			 
 	}
 
 	/**
 	 * EVENT: "lost all members", triggered by ourself in case the last member left the election 
 	 */
 	private void eventClusterLostAllMembers()
 	{
 		Logging.log(this, "============ EVENT: Cluster_Lost_All_Members");
 		Logging.log(this, "     ..knowing these comm. channels: " + getComChannels());
 
 		/**
 		 * Unregister from local databases
 		 */
 		Logging.log(this, "============ Destroying this cluster now...");
 		
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCluster(this);
 	}
 
 	/**
 	 * EVENT: got membership request, an inferior coordinator requests cluster membership, the event is triggered by the comm. session because of some comm. end point at remote side
 	 * 
 	 * @param pRemoteClusterName the description of the possible new cluster member
 	 * @param pSourceComSession the comm. session where the packet was received
 	 */
 	public void eventMembershipRequest(ClusterName pRemoteClusterName, ComSession pSourceComSession)
 	{
 		Logging.log(this, "EVENT: got a membership request from: " + pRemoteClusterName);
 		
 		/**
 		 * Create the communication channel for the described cluster member
 		 */
 		Logging.log(this, "     ..creating communication channel");
 		ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.IN, this, pSourceComSession);
 
 		/**
 		 * Set the remote ClusterName of the communication channel
 		 */
 		tComChannel.setRemoteClusterName(pRemoteClusterName);
 
 		/**
 		 * SEND: acknowledgment -> will be answered by a BullyPriorityUpdate
 		 */
 		RequestClusterMembershipAck tRequestClusterMembershipAckPacket = new RequestClusterMembershipAck(mHRMController.getNodeName(), getHRMID(), createClusterName());
 		tComChannel.sendPacket(tRequestClusterMembershipAckPacket);
 
 		/**
 		 * Trigger event "cluster member joined"
 		 */
 		eventClusterMemberJoined(tComChannel);
 	}
 	
 	/**
 	 * EVENT: detected additional cluster member, the event is triggered by ourself
 	 * 
 	 * @param pComChannel the comm. channel of the new cluster member
 	 */
 	private void eventClusterMemberJoined(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: lost cluster member, comm. channel: " + pComChannel);
 		
 		//TODO: should we do something additional here?
 
 		if (getCoordinator() != null){
 			/**
 			 * Trigger: assign new HRMID
 			 */
 			getCoordinator().eventClusterMemberNeedsHRMID(pComChannel);
 		}else{
 			Logging.warn(this, "Coordinator missing, we cannot assign a new HRMID to the joined cluster member behind comm. channel: " + pComChannel);
 		}
 	}
 
 	/**
 	 * Sets the network interface of this cluster (only for base hierarchy level)
 	 * 
 	 * @param pInterfaceToNeighbor the network interface
 	 */
 	public void setBaseHierarchyLevelNetworkInterface(NetworkInterface pInterfaceToNeighbor)
 	{
 		Logging.log(this, "Setting network interface (base hierarchy level) to: " + pInterfaceToNeighbor);
 		mBaseHierarchyLevelNetworkInterface = pInterfaceToNeighbor;		
 	}
 	
 	/**
 	 * Returns the network interface of this cluster (only for base hierarchy level)
 	 * 
 	 * @return the network interface
 	 */
 	public NetworkInterface getBaseHierarchyLevelNetworkInterface()
 	{
 		return mBaseHierarchyLevelNetworkInterface;
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
 	@SuppressWarnings("unused")
 	public String toString()
 	{
 		HRMID tHRMID = getHRMID();
 		
 		if(tHRMID != null && HRMConfig.Debugging.PRINT_HRMIDS_AS_CLUSTER_IDS) {
 			return tHRMID.toString();
 		} else {
 			return toLocation() + "(" + idToString() + ")";
 
 		}
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
 		if (getHRMID() == null){
 			return "ID=" + getClusterID() + ", CoordID=" + superiorCoordinatorID() +  ", Prio=" + getPriority().getValue();
 		}else{
 			return "HRMID=" + getHRMID().toString();
 		}
 	}
 }
