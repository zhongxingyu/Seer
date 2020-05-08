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
 
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.RevokeHRMIDs;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembershipAck;
 import de.tuilmenau.ics.fog.packets.hierarchical.MultiplexHeader;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.*;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RoutingInformation;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * A communication channel can exist between level (0) cluster instances, or a level (n) coordinator and a level (n + 1) cluster instance.
  * Each ComChannel instance has only one possible parental ComSession associated. However, a ComSession object can be responsible
  * for multiple ComChannel instances.
  *
  * 
  * ****************************************************************************************************************************
  * ***************************** Explanation for two hierarchy levels (0 and 1) and three nodes *******************************
  * ****************************************************************************************************************************
  * From an abstract view, the communication is as follows: (note: node 2 is depicted two times here!)
  *
  *                                                        /===========================\
  *                                                        |         +---------+       |
  *                                                        |         |Coord.3@1|       |
  *                                                        |         +---------+       |
  *                                                        \== Cluster4@2 =|===========/
  *                                                                       /|\
  *                                                                        |
  *                                                                        |internal comm.
  *                                                                        |
  *                                                                       \|/
  *                                /=======================================|======\
  *                                |  +---------+                    +---------+  |
  *                                |  |Coord.1@0| <-- int. comm. --> |Coord.2@0|  |
  *                                |  +---------+                    +---------+  |
  *                                \======|======= Cluster3@1 =============|======/
  *                                      /|\                              /|\
  *                                       |                                |
  *                                       |internal comm.                  |internal comm.
  *                                       |                                |
  *                                      \|/                              \|/
  *   /===================================|=====\                     /====|====================================\
  *   |   +------+                   +------+   |                     |   +------+                   +------+   |			
  *   |   |Node 1| <-- ext. comm.--> |Node 2|   |                     |   |Node 2| <-- ext. comm.--> |Node 3|   |
  *   |   +------+                   +------+   |                     |   +------+                   +------+   |
  *   \============= Cluster1@0 ================/                     \============= Cluster2@0 ================/
  *   
  *   
  *   
  * ****************************************************************************************************************************
  * ****************************************************************************************************************************
  *
  * From a detailed implementation view, the used communication channels between the objects are as follows (arrows indicate the connection direction):
  *
  *                                                                       /====================\
  *                                                                       |                    |
  *                                                                       | instance on node 2 |
  *                                                                       |                    |
  *                                                                       \== Cluster4@2 ======/
  *                                                                                /|\
  *   A CONNECTION is needed as parent --->                                         |CHANNEL
  *   for each channel                                                              |
  *                                                                             +-------+        
  *                                                                             |Coord.3|        
  *                                                                             +-------+        
  *                                                                                 |
  *                                                                                 |LOCAL OBJ. REF.
  *                                                                                 |
  *                                                                                 |
  *                                                                       /====================\
  *                                  /--                                  |                    |
  *                                  |                  +---- CHANNEL --->| instance on node 2 |
  *                                  |                  |                 |                    |
  *   both channels are summarized --+                  |                 \== Cluster3@1 ======/
  *   in ONE CONNECTION in order to  |                  |                          /|\                
  *   reduce connection complexity   |                  |                           |   
  *                                  |                  |                           |CHANNEL   
  *                                  \--                |                           |   
  *                                                 +-------+                   +-------+  
  *                                                 |Coord.1|                   |Coord.2|  
  *                                                 +-------+                   +-------+  
  *                                                     |                           |
  *                                                     |LOCAL OBJ. REF.            |LOCAL OBJ. REF.
  *                                                     |                           |
  *                                                     |                           |
  *   /====================\                 /====================\      /====================\                 /====================\
  *   |                    |                 |                    |      |                    |                 |                    |			
  *   | instance on node 1 | <-- CHANNEL --> | instance on node 2 |      | instance on node 2 | <-- CHANNEL --> | instance on node 3 | 
 *   |      (MEMBER)      |                 |                    |      |                    |                 |       (MEMBER)     | 
  *   \===== Cluster1@0 ===/                 \===== Cluster1@0 ===/      \===== Cluster2@0 ===/                 \===== Cluster2@0 ===/
  *   
  *   
  *   HINT (distributed cluster representation): a cluster can be represented by multiple instances (see "Cluster1@0") 
  *         because it can span over multiple physical nodes
  *   
  *   HINT (base hierarchy level): on level 0, the connection direction depends on the detection order, 
  *         either node 1 detects first node 2 or the other way round
  *   
  *   HINT (cluster broadcasts): if a coordinator wants to send a packet to all cluster members, it 
  *         calls a function of the cluster object ("sendClusterBroadcast()")
  *         
  *   HINT (addressing cluster member): if a coordinator wants to send data (e.g., during "share phase") to a selected
  *        cluster member, it uses the communication channels, which are stored within the cluster instance
  *        
  *  GENERAL: Because of the selected distribution of communication channels, a coordinator can be destroyed without 
  *           losing all communication channels for an existing network cluster.
  *           Under normal circumstances*, each coordinator should have only ONE communication channel, which leads to 
  *           its superior cluster. But each cluster can have MANY communication channels, each leading to one cluster member.
  *
  *   HINT: A comm. session can summarize multiple local comm. channels. However, these channels can belong to different local coordinators.
  *                    
  *   HINT (distributed clustering): The clustering code is based on redundant clusters. Hence, a broadcast domain with 3 nodes leads to 3
  *   	  L0 cluster associations (Cluster/ClusterMember instances) per node. Additionally, at higher hierarchy levels, the clustering code
  *        sees every node as possible cluster head and instantiates a Cluster object at every node. Each of those Cluster object can have
  *        communication channels to multiple ClusterMember objects. 
  *                        
  *           *otherwise, bugs exist within the clustering
  *                    
  * ****************************************************************************************************************************
  * ****************************************************************************************************************************
  * ****************************************************************************************************************************/
 
 public class ComChannel
 {
 	public enum Direction{IN, OUT};
 
 	private ClusterName mRemoteClusterName = null;
 
 	/**
 	 * Stores the parent control entity (cluster or coordinator) to which this communication channel belongs to
 	 */
 	private ControlEntity mParent;
 	
 	/**
 	 * Stores the parent communication session
 	 */
 	private ComSession mParentComSession = null;
 	
 	/**
 	 * Stores the Bully priority of the peer
 	 */
 	private BullyPriority mPeerPriority = null;
 	
 	/**
 	 * Stores the freshness of the Bully priority of the peer
 	 */
 	private double mPeerPriorityTimestampLastUpdate = 0; 
 	
 	/**
 	 * Stores the direction of the communication channel (either "out" or "in")
 	 */
 	private Direction mDirection;
 	
 	/**
 	 * Stores a list of assigned HRMIDs
 	 */
 	private LinkedList<HRMID> mAssignedHRMIDs = new LinkedList<HRMID>();
 	
 	private boolean mPartOfCluster = false;
 	private HRMController mHRMController = null;
 	
 	/**
 	 * For COORDINATORS: Stores the HRMID under which the corresponding peer cluster member is addressable.
 	 */
 	private HRMID mPeerHRMID = null;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pHRMController is the HRMController instance of this node
 	 * @param pDirection the direction of the communication channel (either upward or downward)
 	 * @param pParentComSession is the parental comm. session
 	 */
 	public ComChannel(HRMController pHRMController, Direction pDirection, ControlEntity pParent, ComSession pParentComSession)
 	{
 		// store the HRMController application reference
 		mHRMController = pHRMController;
 		
 		// store the direction
 		mDirection = pDirection;
 		
 		// the peer priority gets initialized by a default value ("undefined")
 		mPeerPriority = BullyPriority.create(this);
 
 		// store the parent (owner) of this communication channel
 		mParent = pParent;
 		if (mParent == null){
 			Logging.err(this, "Parent invalid");
 		}
 		
 		mParentComSession = pParentComSession;
 		if (mParentComSession == null){
 			Logging.err(this, "Parent communication session is invalid");
 		}
 		
 		// register at the parental communication session
 		mParentComSession.registerComChannel(this);
 		
 		// register at the parent (owner)
 		mParent.registerComChannel(this);
 		
 		Logging.log(this, "CREATED");
 	}
 	
 	/**
 	 * Defines the HRMID of the peer which is a cluster member.
 	 * 
 	 * @param pHRMID the new HRMID under which the peer is addressable
 	 */
 	public void setPeerHRMID(HRMID pHRMID)
 	{
 		mPeerHRMID = pHRMID.clone();		
 	}
 	
 	/**
 	 * Determines the address of the peer (e.g., a cluster member).
 	 * 
 	 * @return the HRMID of the peer or "null"
 	 */
 	public HRMID getPeerHRMID()
 	{
 		return mPeerHRMID;
 	}
 	
 	/**
 	 * Returns the direction of the communication  channel
 	 * 
 	 * @return the direction
 	 */
 	public Direction getDirection()
 	{
 		return mDirection;
 	}
 	
 	/**
 	 * Handles a SignalingMessageHrm packet.
 	 * 
 	 * @param pSignalingMessageHrmPacket the packet
 	 */
 	private void getPeerHRMIDFromHRMSignalingMessage(SignalingMessageHrm pSignalingMessageHrmPacket)
 	{
 		// can we learn the peer's HRMID from the packet?
 		if (pSignalingMessageHrmPacket.getSenderName() instanceof HRMID){
 			// get the HRMID of the peer
 			HRMID tPeerHRMID = (HRMID)pSignalingMessageHrmPacket.getSenderName();
 			
 			// update peer's HRMID
 			setPeerHRMID(tPeerHRMID);
 		}		
 	}
 
 	/**
 	 * Handles a RoutingInformation packet.
 	 * 
 	 * @param pRoutingInformationPacket the packet
 	 */
 	private void handleSignalingMessageSharePhase(RoutingInformation pRoutingInformationPacket)
 	{
 		if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 			Logging.log(this, "SHARE PHASE DATA received from \"" + getPeerHRMID() + "\", DATA: " + pRoutingInformationPacket);
 		}
 		
 		//TODO: event in coord./cluster aufrufen
 		
 		for (RoutingEntry tEntry : pRoutingInformationPacket.getRoutes()){
 			if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE)
 				Logging.log(this, "      ..found route: " + tEntry);
 			
 			mHRMController.addHRMRoute(tEntry);
 		}
 	}
 
 	/**
 	 * Updates the Bully priority of the peer.
 	 * 
 	 * @param pPeerPriority the Bully priority
 	 */
 	public void setPeerPriority(BullyPriority pPeerPriority)
 	{
 		if (pPeerPriority == null){
 			Logging.warn(this, "Trying to set a NULL POINTER as peer priority, ignoring this request, current priority: " + getPeerPriority());
 			return;
 		}
 
 		// get the current simulation time
 		double tNow = mHRMController.getSimulationTime();
 		
 		Logging.log(this, "Updating peer priority from " + mPeerPriority.getValue() + " to " + pPeerPriority.getValue() + ", last update was " + (tNow - mPeerPriorityTimestampLastUpdate) + " seconds before");
 		
 		// update the freshness of the peer priority
 		mPeerPriorityTimestampLastUpdate = tNow;
 
 		// update the peer Bully priority itself
 		mPeerPriority = pPeerPriority;
 	}
 
 	/**
 	 * Returns the Bully priority of the communication peer
 	 * 
 	 * @return the Bully priority
 	 */
 	public BullyPriority getPeerPriority()
 	{
 		if (mPeerPriority == null){
 			mPeerPriority = BullyPriority.create(this);
 		}
 			
 		//TODO: getPeerPriorityFreshness() integrieren und einen Timeout bauen, so das danach nur null geliefert wird
 		
 		return mPeerPriority;
 	}
 
 	/**
 	 * Returns the L2Address of the peer
 	 * 
 	 * @return the L2Address
 	 */
 	public L2Address getPeerL2Address()
 	{
 		return mParentComSession.getPeerL2Address();
 	}
 	/**
 	 * Returns the parental control entity
 	 * 
 	 * @return the parental control entity
 	 */
 	public ControlEntity getParent()
 	{
 		return mParent;
 	}
 	
 	/**
 	 * Returns the parental communication session
 	 * 
 	 * @return the parental communication session
 	 */
 	public ComSession getParentComSession()
 	{
 		return mParentComSession;
 	}
 	
 	/**
 	 * Returns the route to the peer
 	 * 
 	 * @return the route to the peer
 	 */
 	public Route getRouteToPeer()
 	{
 		return mParentComSession.getRouteToPeer();
 	}
 
 	/**
 	 * Sends a packet to the peer
 	 * 
 	 * @param pPacket the packet
 	 * 
 	 * @return true if successful, otherwise false
 	 */
 	public boolean sendPacket(SignalingMessageHrm pPacket)
 	{
 		// create destination description
 		ClusterName tDestinationClusterName = getRemoteClusterName();
 		
 		if (tDestinationClusterName != null){
 			Logging.log(this, "SENDING DATA " + pPacket + " to destination " + tDestinationClusterName);
 	
 			// create the source description
 			ClusterName tSourceClusterName = new ClusterName(mHRMController, getParent().getHierarchyLevel(), getParent().getClusterID(), getParent().superiorCoordinatorID());
 			
 			// add source route entry
 			pPacket.addSourceRoute("[S]: " + this.toString());
 			
 			// create the Multiplex-Header
 			MultiplexHeader tMultiplexHeader = new MultiplexHeader(tSourceClusterName, tDestinationClusterName, pPacket);
 				
 			// send the final packet (including multiplex-header)
 			return getParentComSession().write(tMultiplexHeader);
 		}else{
 			Logging.log(this, "Destination is still undefined, skipping packet payload " + pPacket);
 			return false;
 		}
 	}
 
 	/**
 	 * Sets a new name for the remote cluster
 	 * 
 	 * @param pClusterName the new name for the remote cluster
 	 */
 	public void setRemoteClusterName(ClusterName pClusterName)
 	{
 		Logging.log(this, "Setting remote/peer cluster name: " + pClusterName);
 		
 		mRemoteClusterName = pClusterName;
 	}
 
 	/**
 	 * Returns the name of the remote cluster
 	 *  
 	 * @return the name of the remote cluster
 	 */
 	public ClusterName getRemoteClusterName()
 	{
 		return mRemoteClusterName;
 	}
 	
 	/**
 	 * 
 	 */
 	public void eventParentComSessionEstablished()
 	{
 		/**
 		 * TRIGGER: inform the parental ControlEntity about the established communication session and its inferior channels
 		 */
 		getParent().eventComChannelEstablished(this);
 	}
 
 	/**
 	 * Revokes all formerly assigned HRMIDs
 	 * 
 	 * @param pHRMID 
 	 */	
 	public void signalRevokeHRMIDs()
 	{
 		// debug output
 		synchronized (mAssignedHRMIDs) {
 			if (mAssignedHRMIDs.size() > 0){
 				for(HRMID tHRMID : mAssignedHRMIDs){
 					Logging.log(this, "Revoking assigned HRMID: " + tHRMID);
 				}
 	
 				/**
 				 * Revoke the HRMIDs from the peer
 				 */
 				// create the packet
 				RevokeHRMIDs tRevokeHRMIDsPacket = new RevokeHRMIDs(mHRMController.getNodeName(), getPeerHRMID(), mAssignedHRMIDs);
 				// send the packet
 				sendPacket(tRevokeHRMIDsPacket);
 				
 				/**
 				 * Clear the list of stored assigned HRMID
 				 */
 				mAssignedHRMIDs.clear();
 			}
 		}
 	}
 
 	/**
 	 * Stores an assigned HRMID
 	 * 
 	 * @param pHRMID the assigned HRMID
 	 */
 	public void storeAssignedHRMID(HRMID pHRMID)
 	{
 		Logging.log(this, "Storing assigned HRMID: " + pHRMID);
 		
 		synchronized(mAssignedHRMIDs){
 			mAssignedHRMIDs.add(pHRMID);
 		}
 	}
 	
 	/**
 	 * Closes the comm. channel
 	 */
 	public void closeChannel()
 	{
 		//TODO: should we inform the peer about our death?
 
 		// unregister from the parent comm. session
 		mParentComSession.unregisterComChannel(this);
 	}
 	
 	/**
 	 * 
 	 * @param pData is the data that should be sent to the receiver side of this connection end point
 	 * @return true if the packet left the central multiplexer and the forwarding node that is attached to a direct down gate
 	 * @throws NetworkException
 	 */
 	@SuppressWarnings("unused")
 	public boolean receiveData(Serializable pData) throws NetworkException
 	{
 		if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS){
 			Logging.log(this, "RECEIVED DATA (" + pData.getClass().getSimpleName() + ") from \"" + getPeerL2Address() + "/" + getPeerHRMID() + "\": " + pData);
 		}
 			
 		/*
 		 * Invalid data
 		 */
 		if(pData == null) {
 			throw new NetworkException("Received invalid null pointer as data");
 		}
 
 		/**
 		 * HRM signaling message
 		 */
 		if (pData instanceof SignalingMessageHrm){
 			// cast to a SignalingMessageHrm signaling message
 			SignalingMessageHrm tSignalingMessageHrmPacket = (SignalingMessageHrm)pData;
 		
 			// process SignalingMessageHrm message
 			getPeerHRMIDFromHRMSignalingMessage(tSignalingMessageHrmPacket);
 			
 			// add source route entry
 			tSignalingMessageHrmPacket.addSourceRoute("[R]: " + this.toString());
 
 			//HINT: don't return here because we are still interested in the more detailed packet data from derived packet types!
 		}
 		
 		/**
 		 * Bully signaling message
 		 */
 		if (pData instanceof SignalingMessageBully) {
 			// cast to a Bully signaling message
 			SignalingMessageBully tBullyMessage = (SignalingMessageBully)pData;
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_BULLY)
 				Logging.log(this, "RECEIVED BULLY MESSAGE " + tBullyMessage.getClass().getSimpleName());
 
 			// the packet is received by a cluster
 			//HINT: this is only possible at base hierarchy level
 			if (mParent instanceof ClusterMember){
 				ClusterMember tParentClusterProxy = (ClusterMember)mParent;
 				
 				if (tParentClusterProxy.getElector() != null){
 					tParentClusterProxy.getElector().handleSignalingMessageBully(tBullyMessage, this);
 				}else{
 					Logging.warn(this, "Elector is still invalid");
 				}
 				return true;
 			}		
 
 			// the packet is received by a coordinator
 			if (mParent instanceof Coordinator){
 				Coordinator tCoordinator = (Coordinator)mParent;
 				
 				tCoordinator.getCluster().getElector().handleSignalingMessageBully(tBullyMessage, this);
 				
 				return true;
 			}
 
 			Logging.warn(this, "IGNORING THIS MESSAGE: " + tBullyMessage);
 
 			return true;
 		}
 
 		/**
 		 * RoutingInformation
 		 */
 		if (pData instanceof RoutingInformation){
 			// cast to a RoutingInformation signaling message
 			RoutingInformation tRoutingInformationPacket = (RoutingInformation)pData;
 
 			// process Bully message
 			handleSignalingMessageSharePhase(tRoutingInformationPacket);
 			
 			return true;
 		}
 		
 		/**
 		 * AssignHRMID
 		 */
 		if(pData instanceof AssignHRMID) {
 			AssignHRMID tAssignHRMIDPacket = (AssignHRMID)pData;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "ASSIGN_HRMID-received from \"" + getPeerHRMID() + "\" assigned HRMID: " + tAssignHRMIDPacket.getHRMID().toString());
 
 			// let the coordinator process the HRMID assignment
 			getParent().eventNewHRMIDAssigned(tAssignHRMIDPacket.getHRMID());
 			
 			return true;
 		}
 
 		/**
 		 * RevokeHRMIDs
 		 */
 		if(pData instanceof RevokeHRMIDs){
 			RevokeHRMIDs tRevokeHRMIDsPacket = (RevokeHRMIDs)pData;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "REVOKE_HRMIDS-received from \"" + getPeerHRMID() + "\" revoked HRMIDs: " + tRevokeHRMIDsPacket.getHRMIDs().toString());
 
 			// revoke the HRMIDs step-by-step
 			for(HRMID tHRMID: tRevokeHRMIDsPacket.getHRMIDs()){
 				getParent().eventRevokedHRMID(this, tHRMID);
 			}
 			
 			return true;
 		}
 		
 		/**
 		 * RequestClusterMembershipAck
 		 */
 		if(pData instanceof RequestClusterMembershipAck) {
 			RequestClusterMembershipAck tRequestClusterMembershipAckPacket = (RequestClusterMembershipAck)pData;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "REQUEST_CLUSTER_MEMBERSHIP_ACK-received from \"" + getPeerHRMID() + "\"");
 
 			// is the parent a coordinator or a cluster?
 			if (getParent() instanceof Coordinator){
 				Coordinator tCoordinator = (Coordinator)getParent();
 				
 				// trigger event "joined superior cluster"
 				tCoordinator.eventJoinedSuperiorCluster(this);		
 			}else{
 				Logging.err(this, "Expected a Coordinator object as parent for processing RequestClusterMembershipAck data but parent is " + getParent());
 			}
 			
 			return true;
 		}
 		
 		/**
 		 * AnnounceCluster
 		 */
 		if(pData instanceof AnnounceCoordinator) {
 			AnnounceCoordinator tAnnounceClusterPacket = (AnnounceCoordinator)pData;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "ANNOUNCE_CLUSTER-received from \"" + getPeerHRMID() + "\", announcement is: " + tAnnounceClusterPacket);
 		
 			getParent().eventCoordinatorAnnouncement(this, tAnnounceClusterPacket);
 			
 			return true;
 		}
 		
 		Logging.warn(this, ">>>>>>>>>>>>> Found unsupported packet: " + pData);
 		return true;
 	}
 	
 	
 	//TODO:
 	public void setAsParticipantOfMyCluster(boolean pPartOfMyCluster)
 	{
 		mPartOfCluster = pPartOfMyCluster;
 	}
 	
 	public boolean isPartOfMyCluster()
 	{
 		return mPartOfCluster;
 	}
 	
 	
 
 	
 	
 	
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mParent.toString() + "(Peer="+ (getPeerL2Address() != null ? getPeerL2Address() + " <#> " + getPeerHRMID() : "") + ")";
 	}
 }
