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
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.RevokeHRMIDs;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.ClusterDiscovery.NestedDiscovery;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembershipAck;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnounceRemoteCluster;
 import de.tuilmenau.ics.fog.packets.hierarchical.MultiplexHeader;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.*;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RoutingInformation;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.graph.RoutableGraph;
 
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
  *   |                    |                 |                    |      |                    |                 |                    | 
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
 		mPeerPriority = new BullyPriority(this);
 
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
 			Logging.warn(this, "Trying to set NULL POINTER as Bully priority, ignoring this request");
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
 			mPeerPriority = new BullyPriority(this);
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
 	 * @param pData the packet payload
 	 * 
 	 * @return true if successful, otherwise false
 	 */
 	public boolean sendPacket(Serializable pData)
 	{
 		// create destination description
 		ClusterName tDestinationClusterName = getRemoteClusterName();
 		
 		if (tDestinationClusterName != null){
 			Logging.log(this, "Sending " + pData + " to destination " + tDestinationClusterName);
 	
 			// create the source description
 			ClusterName tSourceClusterName = new ClusterName(mHRMController, getParent().getHierarchyLevel(), getParent().superiorCoordinatorID(), ((ICluster)getParent()).getClusterID());
 			
 			// create the Multiplex-Header
 			MultiplexHeader tMultiplexHeader = new MultiplexHeader(tSourceClusterName, tDestinationClusterName, pData);
 				
 			// send the final packet (including multiplex-header)
 			return getParentComSession().write(tMultiplexHeader);
 		}else{
 			Logging.log(this, "Destination is still undefined, skipping packet payload " + pData);
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
 	 * Handles a Bully signaling packet.
 	 * 
 	 * @param pPacketBully the packet
 	 */
 //	private void handleSignalingMessageBully(SignalingMessageBully pPacketBully) throws NetworkException
 //	{
 //		Node tNode = mHRMController.getNode();
 //		Name tLocalNodeName = mHRMController.getNodeName(); 
 //				
 //		boolean BULLY_SIGNALING_DEBUGGING = true;
 //
 //		if (getPeer() == null){
 //			Logging.warn(this, "Peer is still invalid!");
 //		}
 //		
 //		/**
 //		 * ELECT
 //		 */
 //		if(pPacketBully instanceof BullyElect)	{
 //			
 //			// cast to Bully elect packet
 //			BullyElect tPacketBullyElect = (BullyElect)pPacketBully;
 //			
 //			if (BULLY_SIGNALING_DEBUGGING)
 //				Logging.log(this, "BULLY-received from \"" + mParent + "\" an ELECT: " + tPacketBullyElect);
 //
 //			if ((getPeer().getSuperiorCoordinatorCEP() != null) && (getPeer().getHighestPriority().isHigher(this, tPacketBullyElect.getSenderPriority()))) {
 //				
 //				mPeerPriority = tPacketBullyElect.getSenderPriority();
 //				
 //				if (getPeer().mHRMController.equals(tLocalNodeName)) {
 //					BullyAnnounce tAnnouncePacket = new BullyAnnounce(tLocalNodeName, getPeer().getBullyPriority(), "CEP-to?", getPeer().getToken());
 //					
 //					for(CoordinatorCEPChannel tCEP : getPeer().getClusterMembers()) {
 //						tAnnouncePacket.addCoveredNode(tCEP.getPeerL2Address());
 //					}
 //					if(tAnnouncePacket.getCoveredNodes() == null || (tAnnouncePacket.getCoveredNodes() != null && tAnnouncePacket.getCoveredNodes().isEmpty())) {
 //						Logging.log(this, "Sending announce that does not cover anyhting");
 //					}
 //
 //					// send packet
 //					if (BULLY_SIGNALING_DEBUGGING)
 //						Logging.log(this, "BULLY-sending to \"" + mParent + "\" an ANNOUNCE: " + tAnnouncePacket);
 //					sendPacket(tAnnouncePacket);
 //					
 //				} else {
 //					// create ALIVE packet
 //					BullyAlive tAlivePacket = new BullyAlive(tLocalNodeName);
 //					
 //					// send packet
 //					if (BULLY_SIGNALING_DEBUGGING)
 //						Logging.log(this, "BULLY-sending to \"" + mParent + "\" an ALIVE: " + tAlivePacket);
 //					sendPacket(tAlivePacket);
 //					//TODO: packet is sent but never parsed or a timeout timer reset!!
 //				}
 //			} else {
 //				if (getPeer() instanceof Cluster){
 //					// store peer's Bully priority
 //					//TODO: peer prio direkt mal abspeichern und auf grte checken!
 //					mPeerPriority = tPacketBullyElect.getSenderPriority();
 //					
 //					// create REPLY packet
 //					BullyReply tReplyPacket = new BullyReply(tLocalNodeName, getPeerHRMID(), getPeer().getBullyPriority());
 //					
 //					// send the answer packet
 //					if (BULLY_SIGNALING_DEBUGGING)
 //						Logging.log(this, "BULLY-sending to \"" + mParent + "\" a REPLY: " + tReplyPacket);
 //					sendPacket(tReplyPacket);
 //				}else{
 //					Logging.err(this, "Peer is not a cluster, skipping BullyReply message, peer is " + getPeer());
 //				}
 //			}
 //		}
 //		
 //	}
 	
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
 			Logging.log(this, "RECEIVED DATA from \"" + getPeerL2Address() + "/" + getPeerHRMID() + "\": " + pData);
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
 			
 			//HINT: don't return here because we are still interested in the more detailed packet data from derived packet types!
 		}
 		
 		/**
 		 * Bully signaling message
 		 */
 		if (pData instanceof SignalingMessageBully) {
 			// the packet is received by a cluster
 			//HINT: this is only possible at base hierarchy level
 			if (mParent instanceof Cluster){
 				Cluster tParentCluster = (Cluster)mParent;
 				
 				// cast to a Bully signaling message
 				SignalingMessageBully tBullyMessage = (SignalingMessageBully)pData;
 
 				Logging.log(this, "BULLY MESSAGE FROM " + getPeerL2Address() + "/" + tBullyMessage.getSenderName());
 				
 				// process Bully message
 				tParentCluster.handlePacket(tBullyMessage, this);
 			}
 			
 			// the packet is received by a coordinator
 			if (mParent instanceof Coordinator){
 				Coordinator tCoordinator = (Coordinator)mParent;
 				
 				Logging.log(this, "BULLY MESSAGE FROM " + getPeerL2Address());
 				Logging.log(this, "POSSIBLE LOOP DETECTED HERE");
 				
 				// cast to a Bully signaling message
 				SignalingMessageBully tBullyMessage = (SignalingMessageBully)pData;
 			
 				// process Bully message
 				tCoordinator.getCluster().handlePacket(tBullyMessage, this);
 			}
 
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
 
 			// is the parent a coordinator or a cluster?
 			if (getParent() instanceof Coordinator){
 				Coordinator tCoordinator = (Coordinator)getParent();
 				
 				// let the coordinator process the HRMID assignment
 				tCoordinator.handleAssignHRMID(tAssignHRMIDPacket);
 			} else if (getParent() instanceof Cluster){
 				Cluster tCluster = (Cluster)getParent();
 
 				// let the cluster process the HRMID assignment
 				tCluster.handleAssignHRMID(tAssignHRMIDPacket);
 			}
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
 				Logging.log(this, "REQUEST_CLUSTER_MEMBERSHIP_ACK-received from \"" + getPeerHRMID());
 
 			// is the parent a coordinator or a cluster?
 			if (getParent() instanceof Coordinator){
 				Coordinator tCoordinator = (Coordinator)getParent();
 				
 				// trigger event "joined superior cluster"
 				tCoordinator.eventJoinedSuperiorCluster();		
 			}else{
 				Logging.err(this, "Expected a Coordinator object as parent for processing RequestClusterMembershipAck data but parent is " + getParent());
 			}
 			return true;
 		}
 		
 		
 		
 		
 		/**
 		 * NeighborClusterAnnounce
 		 */
 		if(pData instanceof AnnounceRemoteCluster) {
 			AnnounceRemoteCluster tAnnouncePacket = (AnnounceRemoteCluster)pData;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "NEIGHBOR received from \"" + mParent + "\" a NEIGHBOR CLUSTER ANNOUNCE: " + tAnnouncePacket);
 
 			if (!(getParent() instanceof Cluster)){
 				Logging.err(this, "Peer should be a cluster here");
 			}
 			getParent().handleNeighborAnnouncement(tAnnouncePacket, this);
 
 			Logging.log(this, "Received " + tAnnouncePacket + " from remote cluster " + mRemoteClusterName);
 		}
 		
 		
 //		/**
 //		 * RequestCoordinator
 //		 */
 //		if (pData instanceof RequestCoordinator) {
 //			RequestCoordinator tRequestCoordinatorPacket = (RequestCoordinator) pData;
 //			
 //			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 //				Logging.log(this, "CHANNEL-received from \"" + mParent + "\" COORDINATOR REQUEST: " + tRequestCoordinatorPacket);
 //
 //			if(!tRequestCoordinatorPacket.isAnswer()) {
 //				if(getParent().superiorCoordinatorComChannel() != null) {
 //					ICluster tCluster = mHRMController.getClusterWithCoordinatorOnLevel(getParent().getHierarchyLevel().getValue());
 //					Logging.log(this, "Name of coordinator is " + tCluster.getCoordinatorName());
 //					
 //					int tToken = tCluster.superiorCoordinatorID();
 //					Name tCoordinatorName = tCluster.getCoordinatorName();
 //					L2Address tCoordL2Addr = ((ControlEntity)tCluster).superiorCoordinatorL2Address();
 //					
 //					long tCoordinatorAddress = tCoordL2Addr.getComplexAddress().longValue();
 //					DiscoveryEntry tEntry = new DiscoveryEntry(tToken, tCoordinatorName, tCoordinatorAddress, tCoordL2Addr, tCluster.getHierarchyLevel());
 //					tEntry.setPriority(getParent().getPriority());
 //					tEntry.setRoutingVectors(getPath(tCoordL2Addr));
 //					tRequestCoordinatorPacket.addDiscoveryEntry(tEntry);
 //					tRequestCoordinatorPacket.setCoordinatorKnown(true);
 //					tRequestCoordinatorPacket.setAnswer();
 //				} else {
 //					tRequestCoordinatorPacket.setCoordinatorKnown(false);
 //					tRequestCoordinatorPacket.setAnswer();
 //				}
 //				
 //				// send packet
 //				sendPacket(tRequestCoordinatorPacket);
 //				
 //			} else {
 //				if(tRequestCoordinatorPacket.isCoordinatorKnown()) {
 //					mKnowsCoordinator = true;
 //				} else {
 //					mKnowsCoordinator = false;
 //				}
 //				if(tRequestCoordinatorPacket.getDiscoveryEntries() != null) {
 //					for(DiscoveryEntry tEntry : tRequestCoordinatorPacket.getDiscoveryEntries()) {
 //						ClusterName tDummy = handleDiscoveryEntry(tEntry);
 //						Cluster tCluster = mHRMController.getClusterByID(new ClusterName(mHRMController, getParent().getHierarchyLevel(), getParent().superiorCoordinatorID(), getParent().getClusterID()));
 //						tCluster.registerNeighborARG(mHRMController.getClusterByID(tDummy));
 //					}
 //				}
 //				synchronized(tRequestCoordinatorPacket) {
 //					Logging.log(this, "Received answer to " + tRequestCoordinatorPacket + ", notifying");
 //					tRequestCoordinatorPacket.notifyAll();
 //				}
 //			}
 //		}
 
 		Logging.warn(this, ">>>>>>>>>>>>> Found unsupported packet: " + pData);
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @param pTarget is the target to which routing service link vectors should be generated
 	 * @return vectors that provide a patch between forwarding nodes along with the gate numbers
 	 */
 	public LinkedList<RoutingServiceLinkVector> getPath(L2Address pTarget)
 	{
 		LinkedList<RoutingServiceLinkVector> tVectors = new LinkedList<RoutingServiceLinkVector>();
 		
 		L2Address tLocalCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 
 		RoutableGraph<L2Address, Route> tRoutingDatabase = mHRMController.getHRS().getCoordinatorRoutingMap();
 		
 		List<Route> tRoute = tRoutingDatabase.getRoute(tLocalCentralFNL2Address, pTarget);
 		
 		L2Address tDestination = null;
 		if(tRoute == null) {
 			return null;
 		} else {
 			for(int i = 0 ; i < tRoute.size() ; i++) {
 				if(tRoute.get(i) instanceof Route) {
 					tDestination = (L2Address) tRoutingDatabase.getDest(tRoute.get(i));
 					RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(tRoute.get(i), tLocalCentralFNL2Address, tDestination);
 					tVectors.add(tVector);
 					tLocalCentralFNL2Address = tDestination;
 				}
 			}
 		}
 		return tVectors;
 	}
 
 	/**
 	 * The nested discovery entry you provide in the first argument is the message that has to be filled with
 	 * information on how to get to the cluster(s coordinator) provided as second argument
 	 * 
 	 * @param pDiscovery entry of the entity that has to be informed about the cluster provided as second argument
 	 * @param pDestination as cluster (along with the coordinator) to which a path has to be filled into the discovery entry
 	 * @throws NetworkException 
 	 * @throws PropertyException in case the requirements to the target coordinator can not be fulfilled
 	 */
 	private void getPathTo(NestedDiscovery pDiscovery, ControlEntity pDestination) throws NetworkException, PropertyException
 	{
 		L2Address tCoordL2Addr = ((ControlEntity)pDestination).superiorCoordinatorHostL2Address();
 
 		DiscoveryEntry tEntry = new DiscoveryEntry(pDestination.getCoordinatorID(), ((ICluster)pDestination).getCoordinatorHostName(), pDestination.getClusterID(), tCoordL2Addr, pDestination.getHierarchyLevel());
 		tEntry.setClusterHops(mHRMController.getClusterDistance((ControlEntity)pDestination));
 		tEntry.setPriority(pDestination.getPriority());
 		tEntry.setRoutingVectors(getPath(tCoordL2Addr));
 		
 //		List<AbstractRoutingGraphLink> tClusterList = mHRMController.getRouteARG(getParent(), (ControlEntity)pDestination);
 //		if(!tClusterList.isEmpty()) {
 //			ICluster tPredecessorCluster = (ICluster) mHRMController.getOtherEndOfLinkARG((ControlEntity)pDestination, tClusterList.get(tClusterList.size() - 1));
 //			ClusterName tPredecessorClusterName = new ClusterName(mHRMController, tPredecessorCluster.getHierarchyLevel(), tPredecessorCluster.getCoordinatorID(), tPredecessorCluster.getClusterID());
 //			tEntry.setPredecessor(tPredecessorClusterName);
 //		}
 		
 		pDiscovery.addDiscoveryEntry(tEntry);
 	}
 	
 	public void setAsParticipantOfMyCluster(boolean pPartOfMyCluster)
 	{
 		mPartOfCluster = pPartOfMyCluster;
 	}
 	
 	public boolean isPartOfMyCluster()
 	{
 		return mPartOfCluster;
 	}
 	
 	public void handleClusterDiscovery(NestedDiscovery pDiscovery, boolean pRequest) throws PropertyException, NetworkException
 	{
 		if(pRequest){
 			Cluster tSourceCluster = mHRMController.getClusterByID(new ClusterName(mHRMController, pDiscovery.getLevel(), pDiscovery.getToken(), pDiscovery.getSourceClusterID()));
 			if(tSourceCluster == null) {
 				Logging.err(this, "Unable to find appropriate cluster for" + pDiscovery.getSourceClusterID() + " and token" + pDiscovery.getToken() + " on level " + pDiscovery.getLevel() + " remote cluster is " + getRemoteClusterName());
 			}
 
 			List<AbstractRoutingGraphNode> tARGNodesOrderDistance = mHRMController.getNeighborClustersOrderedByRadiusInARG(tSourceCluster);
 			
 			if(tSourceCluster != null) {
 				for(AbstractRoutingGraphNode tARGNode : tARGNodesOrderDistance) {
 					if(tARGNode instanceof ControlEntity) {
 						ControlEntity tControlEntity = (ControlEntity) tARGNode;
 						ICluster tICluster = (ICluster)tControlEntity; //TODO: entfernen, wenn ICluster vollstaendig entfernt ist
 						
 						int tRadius = HRMConfig.Routing.EXPANSION_RADIUS;
 						Logging.log(this, "Radius is " + tRadius);
 						
 						int tDistance = 0;
 						if(tControlEntity instanceof ClusterProxy){
 							ClusterProxy tClusterProxy = (ClusterProxy) tControlEntity;
 							
 							tDistance = mHRMController.getClusterDistance(tClusterProxy) + pDiscovery.getDistance();
 							if (tDistance > tRadius){
 								continue;
 							}
 						}
 						int tToken = ((ICluster)tControlEntity).getCoordinatorID();
 						if(!pDiscovery.getTokens().contains(Integer.valueOf(tToken))) {
 							getPathTo(pDiscovery, tControlEntity);
 							for(ControlEntity tNeighbor : tControlEntity.getNeighborsARG()) {
 								ICluster tNeighborICluster = (ICluster)tNeighbor; //TODO: entfernen, wenn ICluster vollstaendig entfernt ist
 
 								ClusterName tFirstClusterName = new ClusterName(mHRMController, tICluster.getHierarchyLevel(), tICluster.getCoordinatorID(), tICluster.getClusterID()); 
 								ClusterName tSecondClusterName = new ClusterName(mHRMController, tNeighbor.getHierarchyLevel(), tNeighborICluster.getCoordinatorID(), tNeighborICluster.getClusterID()); 
 								pDiscovery.addNeighborRelation(tFirstClusterName, tSecondClusterName);
 							}
 						} else {
 							/*
 							 * Maybe print out additional stuff if required
 							 */
 						}
 					}
 				}
 			}
 		} else {
 			if(pDiscovery.getDiscoveryEntries() != null) {
 				for(DiscoveryEntry tEntry : pDiscovery.getDiscoveryEntries()) {
 					handleDiscoveryEntry(tEntry);
 				}
 			}
 		}
 	}
 	
 	public ClusterName handleDiscoveryEntry(DiscoveryEntry pEntry)
 	{
 		ClusterName tResult = null;
 		
 		Logging.trace(this, "Handling " + pEntry);
 		
 		if(pEntry.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tLink : pEntry.getRoutingVectors()) {
 				mHRMController.getHRS().registerRoute(tLink.getSource(), tLink.getDestination(), tLink.getPath());
 			}
 		}
 
 		Cluster tNewCluster = mHRMController.getClusterByID(new ClusterName(mHRMController, pEntry.getLevel(), pEntry.getToken(), pEntry.getClusterID()));
 		if(tNewCluster != null) {
 			tResult = new ClusterName(mHRMController, tNewCluster.getHierarchyLevel(), tNewCluster.getCoordinatorID(), tNewCluster.getClusterID());
 		}else{
 			/*
 			 * Be aware of the fact that the new attached cluster has lower level
 			 */
 			Logging.log(this, "     ..creating cluster proxy");
 			ClusterProxy tClusterProxy = new ClusterProxy(mHRMController, pEntry.getClusterID(), pEntry.getLevel(), pEntry.getCoordinatorName(), pEntry.getCoordinatorL2Address(), pEntry.getToken());
 			
 			mHRMController.setSourceIntermediateCluster(tClusterProxy, mHRMController.getSourceIntermediateCluster(getParent()));
 			tClusterProxy.setSuperiorCoordinatorID(pEntry.getToken());
 			tClusterProxy.setPriority(pEntry.getPriority());
 			mHRMController.getHRS().mapFoGNameToL2Address(tClusterProxy.getCoordinatorHostName(), pEntry.getCoordinatorL2Address());
 			Logging.log(this, "Created " + tClusterProxy);
 			tResult = new ClusterName(mHRMController, tClusterProxy.getHierarchyLevel(), tClusterProxy.getCoordinatorID(), tClusterProxy.getClusterID());
 		}
 		
 		return tResult;
 	}
 	
 
 	
 	
 	
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mParent.toString() + "(ClusterID=" + mParent.getClusterID() + ", PeerPrio=" + mPeerPriority.getValue() + (getPeerL2Address() != null ? ", PeerL2Addres=" + getPeerL2Address() + ", Peer=" + getPeerHRMID() : "") + ")";
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
 }
