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
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.bus.Bus;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AnnounceHRMIDs;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.RequestHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.RevokeHRMIDs;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.InformClusterLeft;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.InformClusterMembershipCanceled;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembershipAck;
 import de.tuilmenau.ics.fog.packets.hierarchical.PingPeer;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.*;
 import de.tuilmenau.ics.fog.packets.hierarchical.routing.RouteShare;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.ISignalingMessageHrmTopologyASSeparator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.routing.RouteReport;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingTable;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.Simulation;
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
  *                                                                                 |
  *   A CONNECTION/Session is needed as parent --->                                 |CHANNEL
  *   for each channel                                                             \|/
  *                                                                             +-------+        
  *                                                                             |Coord.3|        
  *                                                                             +-------+        
  *                                                                                 |
  *                                                                                 |LOCAL OBJ. REF.
  *                                                                                 |
  *                                                                                 | 
  *                                                                       /====================\
  *                                  /--                                  |                    |
  *                                  |                  +---- CHANNEL --- | instance on node 2 |
  *                                  |                  |                 |                    |
  *   both channels are summarized --+                  |                 \== Cluster3@1 ======/
  *   in ONE CONNECTION/Session to   |                  |                           |                 
  *   reduce connection complexity   |                  |                           |   
  *                                  |                  |                           |CHANNEL   
  *                                  \--               \|/                         \|/   
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
 	 * Stores the Election priority of the peer
 	 */
 	private ElectionPriority mPeerPriority = null;
 	
 	/**
 	 * Stores the freshness of the Election priority of the peer
 	 */
 	private double mPeerPriorityTimestampLastUpdate = 0; 
 	
 	/**
 	 * Stores the direction of the communication channel (either "out" or "in")
 	 */
 	private Direction mDirection;
 	
 	/**
 	 * Stores a list of known peer HRMIDs
 	 */
 	private LinkedList<HRMID> mPeerHRMIDs = new LinkedList<HRMID>();
 	
 	/**
 	 * Stores a list of assigned peer HRMIDs
 	 */
 	private LinkedList<HRMID> mAssignedPeerHRMIDs = new LinkedList<HRMID>();
 	
 	/**
 	 * Stores the comm. channel state
 	 */
 	public enum ChannelState{CLOSED, HALF_OPEN, OPEN};
 	private ChannelState mChannelState = ChannelState.HALF_OPEN;
 	
 	/**
 	 * Stores the peer entity
 	 */
 	private ControlEntity mPeer = null;
 	
 	/**
 	 * Stores the counter for received packets
 	 */
 	private int mReceivedPackets = 0;
 	
 	/**
 	 * Stores the counter for sent packets
 	 */
 	private int mSentPackets = 0;
 	
 	/**
 	 * Stores if this comm. channel is end-point of an active HRM link between the parent and the peer, active for election and topology distribution
 	 */
 	private boolean mLinkActiveForElection = true;
 	
 	/**
 	 * Stores a description about all link activation changes
 	 */
 	private String mDesccriptionLinkActivation = "";
 	
 	/**
 	 * Stores the HRMController reference
 	 */
 	private HRMController mHRMController = null;
 	
 	/**
 	 * Stores the HRMID of the peer
 	 */
 	private HRMID mPeerHRMID = null;
 	
 	/**
 	 * Stores the send/received packets
 	 */
 	private LinkedList<ComChannelPacketMetaData> mPackets = new LinkedList<ComChannelPacketMetaData>();
 	
 	/**
 	 * Stores the timeout of this channel
 	 */
 	private double mTimeout = 0;
 
 	/**
 	 * Stores the time of the last PingPeer packet used to check the peer life state
 	 */
 	private double mTimeLastPingPeer = 0;
 	
 	/**
 	 * Stores the cause for the timeout
 	 */
 	private String mTimeoutCause = "";	
 
 	/**
 	 * Stores the time of the last refresh
 	 */
 	private double mTimeoutStart = 0;
 	
 	/**
 	 * Stores the packet queue
 	 */
 	private LinkedList<SignalingMessageHrm> mPacketQueue = new LinkedList<SignalingMessageHrm>();
 	
 	/**
 	 * Stores the routing table, which is reported based on peer HRMIDs data
 	 */
 	private RoutingTable mReportedRoutingTablePeerHRMIDs = new RoutingTable();
 
 	/**
 	 * Stores the routing table, which is stored locally based on peer HRMIDs data
 	 */
 	private RoutingTable mLocalRoutingTablePeerHRMIDs = new RoutingTable();
 
 	/**
 	 * Stores the routing table, which is reported to the coordinator
 	 */
 	private RoutingTable mReportedRoutingTable = new RoutingTable();
 
 	/**
 	 * Stores the last reported routing table, which was received from an inferior coordinator
 	 */
 	private RoutingTable mLastReceivedReportedRoutingTable = new RoutingTable();
 
 	/**
 	 * Stores the last shared routing table, which was shared by the superior coordinator/cluster
 	 */
 	private RoutingTable mLastReceivedSharedRoutingTable = new RoutingTable();
 
 	/**
 	 * Stores the last sent shared routing table, which was shared with inferior coordinator/cluster member
 	 */
 	private RoutingTable mLastSentSharedRoutingTable = new RoutingTable();
 	
 	/**
 	 * Stores the time when the last full routing table was shared with the inferior control entity
 	 */
 	private double mTimeLastCompleteSharedRoutingTable = 0;
 
 	/**
 	 * Stores if the last shared routing table was sent during an unstable hierarchy
 	 */
 	private boolean mLastSharedRoutingTableWasDuringUnstableHierarchy = true;
 
 	/**
 	 * Stores the last Election priority which was signaled to the peer
 	 */
 	private ElectionPriority mSignaledPriority = null;
 
 	/**
 	 * Stores if the local entity is already signaled as winner
 	 */
 	private boolean mSignaledAsWinner = false;
 	
 	/**
 	 * Stores the cause for the closing of this channel
 	 */
 	private String mCloseCause = "";
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pHRMController is the HRMController instance of this node
 	 * @param pDirection the direction of the communication channel (either upward or downward)
 	 * @param pParent the parent control entity
 	 * @param pParentComSession is the parental comm. session
 	 */
 	public ComChannel(HRMController pHRMController, Direction pDirection, ControlEntity pParent, ComSession pParentComSession)
 	{
 		// store the HRMController application reference
 		mHRMController = pHRMController;
 		
 		// store the direction
 		mDirection = pDirection;
 		
 		// store the peer entity
 		mPeer = null;
 
 		// the peer priority gets initialized by a default value ("undefined")
 		mPeerPriority = ElectionPriority.create(this);
 
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
 	 * Constructor
 	 * 
 	 * @param pHRMController is the HRMController instance of this node
 	 * @param pDirection the direction of the communication channel (either upward or downward)
 	 * @param pParent the parent control entity
 	 * @param pParentComSession is the parental comm. session
 	 */
 	public ComChannel(HRMController pHRMController, Direction pDirection, ControlEntity pParent, ComSession pParentComSession, ControlEntity pPeer)
 	{
 		this(pHRMController, pDirection, pParent, pParentComSession);
 		
 		// store the peer entity
 		setPeer(pPeer);
 	}
 	
 	/**
 	 * Defines the HRMID of the peer which is a cluster member.
 	 * 
 	 * @param pHRMID the new HRMID under which the peer is addressable
 	 */
 	public void setPeerHRMID(HRMID pHRMID)
 	{
 		if(pHRMID != null){
 			if(!pHRMID.equals(mPeerHRMID)){
 				if(!pHRMID.isZero()){
 					Logging.log(this, "Setting new peer HRMID: " + pHRMID);
 	
 					mPeerHRMID = pHRMID.clone();
 					
 					boolean tPeerHRMIDIsNew = false;
 					
 					synchronized(mAssignedPeerHRMIDs){
 						if(!mAssignedPeerHRMIDs.contains(pHRMID)){
 							if(!(mParent instanceof Cluster)){
 								mAssignedPeerHRMIDs.clear();
 							}
 							mAssignedPeerHRMIDs.add(pHRMID);
 						}else{
 							//Logging.warn(this, "storePeerHRMID() skips storing the already known HRMID: " + pHRMID); 
 						}
 					}
 		
 					/**
 					 * Add peerHRMID to peerHRMIDs
 					 */
 					synchronized (mPeerHRMIDs) {
 						if(!mPeerHRMIDs.contains(pHRMID)){
 							if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_DISTRIBUTION){
 								Logging.err(this, "    ..adding to stored peerHRMIDs the peerHRMID: " + getPeerHRMID());
 							}
 							mPeerHRMIDs.add(pHRMID);
 							tPeerHRMIDIsNew = true;
 						}
 					}
 					
 					/**
 					 * Inform the parent ClusterMember about the new peer HRMIDs
 					 */
 					if(tPeerHRMIDIsNew){
 						detectNeighborhood();
 					}
 				}else{
 					throw new RuntimeException(this + "::setPeerHRMID() got a zero HRMID as peer HRMID");
 				}
 			}else{
 				//Logging.warn(this, "Ignoring set-request of peer HRMID: " + pHRMID);
 			}
 		}else{
 			// reset
 			synchronized (mAssignedPeerHRMIDs) {
 				mAssignedPeerHRMIDs.remove(mPeerHRMID);
 			}
 			synchronized (mPeerHRMIDs) {
 				mPeerHRMIDs.remove(mPeerHRMID);
 			}
 			mPeerHRMID = null;
 		}
 	}
 			
 	/**
 	 * Detects the local neighborhood.
 	 * 		  IMPORTANT: This is the main function for determining capacities and link usage
 	 */
 	private int mCallsDetectNeighborhood = 0;
 	public void detectNeighborhood()
 	{
 		if (mParent.getHierarchyLevel().isBaseLevel()){
 			if(mParent instanceof ClusterMember){
 				mCallsDetectNeighborhood++;
 	
 				// get the list of neighbor HRMIDs
 				LinkedList<HRMID> tNeighborHRMIDs = getPeerHRMIDs();
 	
 				if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE_COM_CHANNELS){
 					Logging.log(this, "EVENT: new neighbor HRMIDs (" + mCallsDetectNeighborhood + ") for: " + this);
 					Logging.log(this, "    ..neighbor HRMIDs (" + mCallsDetectNeighborhood + "): " + tNeighborHRMIDs);
 				}
 				
 				/**
 				 * Continue only for base hierarchy level
 				 */
 				ClusterMember tParentClusterMember = (ClusterMember)mParent;
 				if(tParentClusterMember.getBaseHierarchyLevelNetworkInterface() != null){
 					Bus tPhysicalBus = (Bus)tParentClusterMember.getBaseHierarchyLevelNetworkInterface().getBus();
 					
 					if(tParentClusterMember.getHierarchyLevel().isBaseLevel()){
 						// determine the HRMID of this node for this L0 cluster
 						HRMID tThisNodeClusterMemberHRMID = tParentClusterMember.getL0HRMID();
 		
 						/********************************************************************
 						 * Determine the HRMID which is used as source for reported routes 
 						 ********************************************************************/
 						HRMID tSourceForReportedRoutes = tParentClusterMember.getHRMID(); // the HRMID of this cluster
 						if((tParentClusterMember.getHierarchyLevel().isBaseLevel()) && (tThisNodeClusterMemberHRMID != null)){
 							// use the L0 cluster member address instead of the cluster address 
 							tSourceForReportedRoutes = tThisNodeClusterMemberHRMID;
 						}
 		
 						/********************************************************************
 						 * Update routing table
 						 ********************************************************************/
 						if((tSourceForReportedRoutes != null) && (!tSourceForReportedRoutes.isZero())){
 							// backup old reported routing table
 							RoutingTable tDeprecatedReportedRoutingTable = null;
 							synchronized (mReportedRoutingTablePeerHRMIDs) {
 								tDeprecatedReportedRoutingTable = (RoutingTable) mReportedRoutingTablePeerHRMIDs.clone();
 							}
 							
 							// backup old locally stored routing table
 							RoutingTable tDeprecatedLocalRoutingTable = null;
 							synchronized (mLocalRoutingTablePeerHRMIDs) {
 								tDeprecatedLocalRoutingTable = (RoutingTable) mLocalRoutingTablePeerHRMIDs.clone();
 							}
 		
 							// iterate over all neighbor HRMIDs
 							RoutingTable tNewReportedRoutingTable = new RoutingTable();
 							RoutingTable tNewLocalRoutingTable = new RoutingTable();
 							if(tNeighborHRMIDs.size() > 0){
 								for(HRMID tNeighborHRMID : tNeighborHRMIDs){
 									if((tNeighborHRMID != null) && (!tNeighborHRMID.isZero())){
 										if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE_COM_CHANNELS){
 											Logging.err(this, "   ..found (" + mCallsDetectNeighborhood + ") neighbor HRMID: " + tNeighborHRMID);
 										}
 										RoutingEntry tLocalRoutingEntry = null;
 										RoutingEntry tReportedRoutingEntryForward = null;
 										RoutingEntry tReportedRoutingEntryBackward = null;
 										
 										/**
 										 * Determine the correctly aggregated peer description
 										 */
 										HRMID tGeneralizedNeighborHRMID = null;
 										if(!tNeighborHRMID.equals(getPeerHRMID())){
 											/**
 											 * Generalize neighbor HRMID to its cluster address, depending on the locally assigned HRMID.
 											 * If the local node owns 1.1.1 and 1.2.1, and the neighbor HRMID is 1.3.1, the result will be 1.3.0 because this cluster is a foreign one for the local node 
 											 */ 
 											if(getPeerHRMID() != null)
 												tGeneralizedNeighborHRMID = getPeerHRMID().getForeignCluster(tNeighborHRMID); //mHRMController.aggregateForeignHRMID(tNeighborHRMID);
 											else
 												tGeneralizedNeighborHRMID = tNeighborHRMID; //mHRMController.aggregateForeignHRMID(tNeighborHRMID);
 										}else{
 											// use the peer HRMID directly
 											tGeneralizedNeighborHRMID = getPeerHRMID();
 										}
 										
 										/**
 										 * Learn the routes
 										 */
 										if((tGeneralizedNeighborHRMID != null) && (!tGeneralizedNeighborHRMID.isZero())){
 											double tTimeoffset = HRMConfig.Routing.ROUTE_TIMEOUT  + HRMConfig.Hierarchy.MAX_E2E_DELAY;//2 * mHRMController.getPeriodReportPhase(mParent.getHierarchyLevel());
 
 											//Logging.log(this, "DELAY: " + tPhysicalBus.getDelayMSec());
 											if(tGeneralizedNeighborHRMID.isClusterAddress()){
 												/**
 												 * Neighbor cluster address detected
 												 */
 												if(!tNeighborHRMID.equals(getPeerHRMID())){
 													/**
 													 * HRM routing table entry
 													 */
 													if(!mHRMController.isLocalCluster(tGeneralizedNeighborHRMID)){
 														// create the new routing table entry
 														tLocalRoutingEntry = RoutingEntry.createRouteToDirectNeighbor(tSourceForReportedRoutes, tGeneralizedNeighborHRMID, getPeerHRMID(), tPhysicalBus.getUtilization(), tPhysicalBus.getDelayMSec(), tPhysicalBus.getAvailableDataRate(), null);
 														tLocalRoutingEntry.addOwner(mParent.getHRMID());
 														tLocalRoutingEntry.setOrigin(mParent.getHRMID());
 														tLocalRoutingEntry.extendCause(this + "::detectNeighborhood()_1(" + mCallsDetectNeighborhood + ") for peerHRMID " + tNeighborHRMID + " as " + tLocalRoutingEntry);
 														// define the L2 address of the next hop in order to let "addHRMRoute" trigger the HRS instance the creation of new HRMID-to-L2ADDRESS mapping entry
 														tLocalRoutingEntry.setNextHopL2Address(getPeerL2Address());
 														// set the timeout for the found route to neighborhood
 														tLocalRoutingEntry.setTimeout(mHRMController.getSimulationTime() + tTimeoffset);
 													}
 													
 													/**
 													 * HRG links: forward and backward link between the HRMIDs of the direct neighbor
 													 */
 													// create the forward routing table entry
 													tReportedRoutingEntryForward = RoutingEntry.create(getPeerHRMID(), tGeneralizedNeighborHRMID, tNeighborHRMID, 0, RoutingEntry.NO_UTILIZATION, RoutingEntry.NO_DELAY, RoutingEntry.INFINITE_DATARATE, (String)null);
 													tReportedRoutingEntryForward.addOwner(mParent.getHRMID());
 													tReportedRoutingEntryForward.setOrigin(mParent.getHRMID());
 													tReportedRoutingEntryForward.extendCause( this + "::detectNeighborhood()_2(" + mCallsDetectNeighborhood + ") for peerHRMID " + tNeighborHRMID + " as " + tReportedRoutingEntryForward);
 													// define the L2 address of the next hop in order to let "addHRMRoute" trigger the HRS instance the creation of new HRMID-to-L2ADDRESS mapping entry
 													tReportedRoutingEntryForward.setNextHopL2Address(getPeerL2Address());
 													// set the timeout for the found route to neighborhood
 													tReportedRoutingEntryForward.setTimeout(mHRMController.getSimulationTime() + tTimeoffset);
 													// create the backward routing table entry
 													tReportedRoutingEntryBackward = RoutingEntry.create(tNeighborHRMID, tGeneralizedNeighborHRMID.getForeignCluster(getPeerHRMID()), getPeerHRMID(), 0, RoutingEntry.NO_UTILIZATION, RoutingEntry.NO_DELAY, RoutingEntry.INFINITE_DATARATE, (String)null);
 													tReportedRoutingEntryBackward.addOwner(mParent.getHRMID());
 													tReportedRoutingEntryBackward.setOrigin(mParent.getHRMID());
 													tReportedRoutingEntryBackward.extendCause(this + "::detectNeighborhood()_3(" + mCallsDetectNeighborhood + ") for peerHRMID " + tNeighborHRMID + " as " + tReportedRoutingEntryBackward);
 													// define the L2 address of the next hop in order to let "addHRMRoute" trigger the HRS instance the creation of new HRMID-to-L2ADDRESS mapping entry
 													tReportedRoutingEntryBackward.setNextHopL2Address(mHRMController.getNodeL2Address());
 													tReportedRoutingEntryBackward.setTimeout(mHRMController.getSimulationTime() + tTimeoffset);
 												}
 											}else{
 												/**
 												 * L0 physical node neighbor address detected
 												 */
 
 												/**
 												 * HRM routing table entry
 												 */
 												// create the new routing table entry
 												tLocalRoutingEntry = RoutingEntry.createRouteToDirectNeighbor(tSourceForReportedRoutes, tGeneralizedNeighborHRMID, tNeighborHRMID, tPhysicalBus.getUtilization(), tPhysicalBus.getDelayMSec(), tPhysicalBus.getAvailableDataRate(), null);
 												tLocalRoutingEntry.addOwner(mParent.getHRMID());
 												tLocalRoutingEntry.setOrigin(mParent.getHRMID());
 												tLocalRoutingEntry.extendCause(this + "::detectNeighborhood()_4(" + mCallsDetectNeighborhood + ") for peerHRMID " + tNeighborHRMID + " as " + tLocalRoutingEntry);
 												// define the L2 address of the next hop in order to let "addHRMRoute" trigger the HRS instance the creation of new HRMID-to-L2ADDRESS mapping entry
 												tLocalRoutingEntry.setNextHopL2Address(getPeerL2Address());
 												// set the timeout for the found route to neighborhood
 												tLocalRoutingEntry.setTimeout(mHRMController.getSimulationTime() + tTimeoffset);
 					
 												/**
 												 * HRG links: forward and backward link to the direct neighbor
 												 */
 												// create the forward routing table entry
 												tReportedRoutingEntryForward = tLocalRoutingEntry.clone();
 												tReportedRoutingEntryForward.addOwner(mParent.getHRMID());
 												tReportedRoutingEntryForward.setOrigin(mParent.getHRMID());
 												// create the backward routing table entry
 												tReportedRoutingEntryBackward = RoutingEntry.createRouteToDirectNeighbor(tNeighborHRMID, tSourceForReportedRoutes, tSourceForReportedRoutes, tPhysicalBus.getUtilization(), tPhysicalBus.getDelayMSec(), tPhysicalBus.getAvailableDataRate(), null);
 												tReportedRoutingEntryBackward.addOwner(mParent.getHRMID());
 												tReportedRoutingEntryBackward.setOrigin(mParent.getHRMID());
 												tReportedRoutingEntryBackward.extendCause(this + "::detectNeighborhood()_5(" + mCallsDetectNeighborhood + ") for peerHRMID " + tNeighborHRMID + " as " + tReportedRoutingEntryBackward);
 												// define the L2 address of the next hop in order to let "addHRMRoute" trigger the HRS instance the creation of new HRMID-to-L2ADDRESS mapping entry
 												tReportedRoutingEntryBackward.setNextHopL2Address(mHRMController.getNodeL2Address());
 												// set the timeout for the found route to neighborhood
 												tReportedRoutingEntryBackward.setTimeout(mHRMController.getSimulationTime() + tTimeoffset);
 											}
 										}
 						
 										/**
 										 * Store the learned routes
 										 */
 										if(tReportedRoutingEntryForward != null){
 											// add the entry to the reported routing table
 											if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE_COM_CHANNELS){
 												Logging.err(this, "   ..adding (" + mCallsDetectNeighborhood + ") reported forward route: " + tReportedRoutingEntryForward);
 											}
 											tNewReportedRoutingTable.addEntry(tReportedRoutingEntryForward);
 										}
 										if(tReportedRoutingEntryBackward != null){
 											// add the entry to the reported routing table
 											if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE_COM_CHANNELS){
 												Logging.err(this, "   ..adding (" + mCallsDetectNeighborhood + ") reported backward route: " + tReportedRoutingEntryBackward);
 											}
 											tNewReportedRoutingTable.addEntry(tReportedRoutingEntryBackward);
 										}
 				
 										if(tLocalRoutingEntry != null){
 											// add the entry to the reported routing table
 											if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE_COM_CHANNELS){
 												Logging.log(this, "   ..adding (" + mCallsDetectNeighborhood + ") local route: " + tLocalRoutingEntry + ", next network interface=" + tParentClusterMember.getBaseHierarchyLevelNetworkInterface());
 											}
 											if(tParentClusterMember.getBaseHierarchyLevelNetworkInterface() != null){
 												tLocalRoutingEntry.setNextHopL2NetworkInterace(tParentClusterMember.getBaseHierarchyLevelNetworkInterface());
 											}
 											tNewLocalRoutingTable.addEntry(tLocalRoutingEntry);
 										}
 									}else{
 										Logging.err(this, "Received zero neighbor address");
 									}
 								}
 							}
 							
 							/**
 							 * Step 1: apply new routes:
 							 * 			- set the new value for reported routes based on peer HRMIDs
 							 * 			- inform the HRMController
 							 * 			- update mReportedRoutingTable
 							 */ 
 							mReportedRoutingTablePeerHRMIDs = tNewReportedRoutingTable;					
 							mLocalRoutingTablePeerHRMIDs = tNewLocalRoutingTable;
 							// HRG links
 							for(RoutingEntry tEntry : tNewReportedRoutingTable){
 								mHRMController.registerAutoHRG(tEntry);
 							}
 							// HRM routes
 //							synchronized (mLocalRoutingTablePeerHRMIDs) {
 //								for (RoutingEntry tEntry: mLocalRoutingTablePeerHRMIDs){
 //									Logging.log(this, "storing entry: " + tEntry + "\n   ..with network if: " + tEntry.getNextHopL2NetworkInterface());
 //								}
 //							}
 							mHRMController.addHRMRoutes(mLocalRoutingTablePeerHRMIDs);
 							synchronized (mReportedRoutingTable) {
 								mReportedRoutingTable.addEntries(mReportedRoutingTablePeerHRMIDs);
 								if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE_COM_CHANNELS){
 									Logging.err(this, "Added (" + mCallsDetectNeighborhood + ") to local routing table: " + mLocalRoutingTablePeerHRMIDs);
 								}
 							}
 							
 							/**
 							 * Step 2: forget deprecated routes:
 							 * 			- derive the deprecated routes
 							 * 			- HRMController about the deprecated routing table entries
 							 * 			- update mReportedRoutingTable
 							 */ 
 							tDeprecatedReportedRoutingTable.delEntries(tNewReportedRoutingTable);
 							tDeprecatedLocalRoutingTable.delEntries(tNewLocalRoutingTable);
 							// HRG links
 							for(RoutingEntry tEntry : tDeprecatedReportedRoutingTable){
 								mHRMController.unregisterAutoHRG(tEntry);
 							}
 							// HRM routes
 							mHRMController.delHRMRoutes(tDeprecatedLocalRoutingTable);
 							synchronized (mReportedRoutingTable) {
 								mReportedRoutingTable.delEntries(tDeprecatedReportedRoutingTable);
 								if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE_COM_CHANNELS){
 									Logging.err(this, "Removed (" + mCallsDetectNeighborhood + ") from local routing table: " + tDeprecatedLocalRoutingTable);
 								}
 							}
 						}else{
 							//Logging.warn(this, "eventNeighborHRMIDs()(" + mCallsdetectNeighborhood + ") skipped because own source HRMID is zero, ignoring neighbor HRMIDs: " + tNeighborHRMIDs);
 						}
 					}else{
 						// we are at higher hierarchy level
 					}
 				}else{
 					// no network interface known
 				}
 			}else{
 				Logging.err(this, "eventNewPeerHRMID()(" + mCallsDetectNeighborhood + ") expected a ClusterMember as parent, parent is: " + mParent);
 			}
 		}else{
 			// higher hierarchy level -> this function is only needed for node-2-node neighborhood and not cluster-2-cluster
 		}
 	}
 
 	/**
 	 * Returns the reported routing table
 	 * 
 	 * @return the reported routing table
 	 */
 	public RoutingTable getReportedRoutingTable()
 	{
 		RoutingTable tResult = new RoutingTable();
 		
 		synchronized (mReportedRoutingTable) {
 			if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 				//Logging.err(this, "Reporting routing table: " + mReportedRoutingTable);
 			}
 			for(RoutingEntry tEntry : mReportedRoutingTable){
 				tResult.add(tEntry.clone());
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the shared routing table
 	 * 
 	 * @return the shared routing table
 	 */
 	public RoutingTable getSharedRoutingTable()
 	{
 		RoutingTable tResult = new RoutingTable();
 		
 		synchronized (mLastReceivedSharedRoutingTable) {
 			if(HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 				//Logging.err(this, "Reporting routing table: " + mReportedRoutingTable);
 			}
 			for(RoutingEntry tEntry : mLastReceivedSharedRoutingTable){
 				tResult.add(tEntry.clone());
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines the address of the peer (e.g., a cluster member).
 	 * 
 	 * @return the HRMID of the peer or "null"
 	 */
 	public HRMID getPeerHRMID()
 	{
 		if(mPeerHRMID != null){
 			return mPeerHRMID.clone();
 		}else{
 			return null;
 		}
 	}
 	
 	/**
 	 * Determines the AsID of the peer.
 	 * 
 	 * @return the AsID of the peer or "null"
 	 */
 	public Long getPeerAsID()
 	{
 		if(getParentComSession().getPeerAsID() != null){
 			return getParentComSession().getPeerAsID();
 		}else{
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the machine-local AsID (excluding the machine specific multiplier)
 	 * 
 	 * @return the machine-local AsID
 	 */
 	public Long getGUIPeerAsID()
 	{
 		//TODO: if JINI is used, the function uniqueIDsSimulationMachineMultiplier() could return the wrong value here
 		if (getPeerAsID() != null)
 			return getPeerAsID() / Simulation.uniqueIDsSimulationMachineMultiplier();
 		else
 			return new Long(-1);
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
 	 * Sets a new peer entity
 	 */
 	public void setPeer(ControlEntity pPeer)
 	{
 		Logging.log(this, "Setting new peer: " + pPeer);
 		mPeer = pPeer;
 	}
 
 	/**
 	 * Returns the peer entity
 	 * 
 	 * @return peer entity
 	 */
 	public ControlEntity getPeer()
 	{
 		return mPeer;
 	}
 	
 	/**
 	 * EVENT: established
 	 */
 	public synchronized void eventEstablished()
 	{
 		Logging.log(this, "EVENT: established");
 		
 		setState(ChannelState.OPEN, "::eventEstablished()");
 	}
 	
 	/**
 	 * Returns if the comm. channel is open
 	 * 
 	 * @return true or false
 	 */
 	public boolean isOpen()
 	{
 		return (mChannelState == ChannelState.OPEN);
 	}
 	
 	/**
 	 * Returns if the comm. channel is half-open
 	 * 
 	 * @return true or false
 	 */
 	public boolean isHalfOpen()
 	{
 		return (mChannelState == ChannelState.HALF_OPEN);
 	}
 
 	/**
 	 * Returns if the comm. channel is closed
 	 * 
 	 * @return true or false
 	 */
 	public boolean isClosed()
 	{
 		return (mChannelState == ChannelState.CLOSED);
 	}
 
 	/**
 	 * Returns the state of the channel
 	 * 
 	 * @return the channel state
 	 */
 	public ChannelState getState()
 	{
 		return mChannelState;
 	}
 	
 	/**
 	 * Sets a new state for the channel
 	 * 
 	 * @param pState new channel state
 	 * @param pCause the cause for this state change
 	 */
 	public void setState(ChannelState pState, String pCause)
 	{
 		Logging.log(this, "Setting channel state to: " + pState.toString() + ", cause=" + pCause);
 		mChannelState = pState;
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
 			
 			if((tPeerHRMID != null) && (!tPeerHRMID.isZero())){
 				//Logging.log(this, "Extracted from received signaling message " + pSignalingMessageHrmPacket + " a new peer HRMID: " + tPeerHRMID);
 				
 				// update peer's HRMID
 				setPeerHRMID(tPeerHRMID);
 			}
 		}		
 	}
 
 	/**
 	 * @param pRouteReportPacket
 	 */
 	private void eventReceivedRouteReport(RouteReport pRouteReportPacket)
 	{
 		if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 			Logging.log(this, "REPORT PHASE DATA received from \"" + getPeerHRMID() + "\", DATA: " + pRouteReportPacket);
 		}
 	
 		RoutingTable tDeprecatedReportedRoutingTable = null;
 		RoutingTable tNewReceivedReportedRoutingTable = null;
 		synchronized (mLastReceivedReportedRoutingTable) {
 			/**
 			 * have we received a full update? -> check for deprecated entries
 			 */
 			tNewReceivedReportedRoutingTable = pRouteReportPacket.getRoutes();
 			if(!tNewReceivedReportedRoutingTable.isOnlyDiff()){
 				tDeprecatedReportedRoutingTable = (RoutingTable) mLastReceivedReportedRoutingTable.clone();
 				tDeprecatedReportedRoutingTable.delEntries(tNewReceivedReportedRoutingTable);
 			}
 				
 			/**
 			 * Store the received reported routing info
 			 */
 			if((tDeprecatedReportedRoutingTable != null) && (tDeprecatedReportedRoutingTable.size() > 0)){
 				mLastReceivedReportedRoutingTable.delEntries(tDeprecatedReportedRoutingTable);
 			}
 			mLastReceivedReportedRoutingTable.addEntries(tNewReceivedReportedRoutingTable); 
 
 			/**
 			 * Record the routing report
 			 */
 			if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 				Logging.err(this, "   ..got routing report: " + tNewReceivedReportedRoutingTable);
 			}
 		}
 
 		if(mParent instanceof Cluster){
 			Cluster tParentCluster = (Cluster)mParent;
 			
 			/**
 			 * Trigger: inform the cluster about the new routing report
 			 */
 			double tBefore = HRMController.getRealTime();
 			tParentCluster.eventReceivedRouteReport(this, pRouteReportPacket, tDeprecatedReportedRoutingTable);
 			double tSpentTime = HRMController.getRealTime() - tBefore;
 			if(tSpentTime > 50){
 				Logging.log(this, "      ..eventReceivedRouteReport() took " + tSpentTime + " ms for route report: " + pRouteReportPacket);
 			}
 		}else{
 			Logging.err(this, "eventReceivedRouteReport() expected a Cluster as parent, parent is: " + mParent);
 		}
 	}
 
 	/**
 	 * Handles a RouteShare packet.
 	 * 
 	 * @param pRouteSharePacket the packet
 	 */
 	private void eventReceivedRouteShare(RouteShare pRouteSharePacket)
 	{
 		boolean DEBUG = HRMConfig.DebugOutput.SHOW_SHARE_PHASE; 
 	
 		if (DEBUG){
 			Logging.log(this, "SHARE PHASE DATA received from \"" + getPeerHRMID() + "\", DATA: " + pRouteSharePacket);
 		}
 		
 		RoutingTable tDeprecatedSharedRoutingTable = null; 
 		synchronized (mLastReceivedSharedRoutingTable) {
 			/**
 			 * have we received a full update? -> check for deprecated entries
 			 */
 			RoutingTable tNewReceivedSharedRoutingTable = pRouteSharePacket.getRoutes();
 			if(!tNewReceivedSharedRoutingTable.isOnlyDiff()){
 				tDeprecatedSharedRoutingTable = (RoutingTable) mLastReceivedSharedRoutingTable.clone();
 				tDeprecatedSharedRoutingTable.delEntries(tNewReceivedSharedRoutingTable);
 			}
 			
 			/**
 			 * set the absolute timeout values per received share routing entry
 			 */
 			for(RoutingEntry tEntry : tNewReceivedSharedRoutingTable){
 //				if(tEntry.isRouteAcrossNetwork()){
 //					Logging.warn(this, "  ..received shared LOOP ROUTE (TO: " + tEntry.getTimeout() + "): " + tEntry);
 //				}
 
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
 
 				if(DEBUG){
 					Logging.log(this, "  ..got shared routing entry (TO: " + tEntry.getTimeout() + "): " + tEntry);					
 				}
 			}
 			
 			/**
 			 * Store the received shared routing info
 			 */
 			if((tDeprecatedSharedRoutingTable != null) && (tDeprecatedSharedRoutingTable.size() > 0)){
 				mLastReceivedSharedRoutingTable.delEntries(tDeprecatedSharedRoutingTable);
 			}
 			mLastReceivedSharedRoutingTable.addEntries(tNewReceivedSharedRoutingTable); 
 
 			if((tDeprecatedSharedRoutingTable != null) && (tDeprecatedSharedRoutingTable.size() > 0)){
 				Logging.warn(this, "Lost shared routing data (last message included it): " + tDeprecatedSharedRoutingTable);
 				if(HRMConfig.DebugOutput.GUI_SHOW_ROUTE_DEPRECATIONS){
 					for(RoutingEntry tEntry : tDeprecatedSharedRoutingTable){
 						Logging.warn(this, "   ..lost: " + tEntry);	
 					}
 //					Logging.err(this, "New shared routing data: " + pRouteSharePacket.getRoutes());
 //					for(RoutingEntry tEntry : pRouteSharePacket.getRoutes()){
 //						Logging.err(this, "   lost: " + tEntry);	
 //					}
 				}
 			}
 		}
 
 
 		if(mParent instanceof CoordinatorAsClusterMember){
 			CoordinatorAsClusterMember tParentCoordinatorAsClusterMember = (CoordinatorAsClusterMember)mParent;
 			
 			/**
 			 * Trigger: inform the CoordinatorAsClusterMember about the new routing report
 			 */
 			tParentCoordinatorAsClusterMember.getCoordinator().eventReceivedRouteShare(this, mLastReceivedSharedRoutingTable, tDeprecatedSharedRoutingTable);
 			
 			return;
 		}
 		
 		if((mParent instanceof ClusterMember) && (!(mParent instanceof Cluster))){
 			ClusterMember tParentClusterMember = (ClusterMember)mParent;
 			
 			/**
 			 * Trigger: inform the ClusterMember about the new routing report
 			 */
 			tParentClusterMember.eventReceivedRouteShare(this, mLastReceivedSharedRoutingTable, tDeprecatedSharedRoutingTable);
 			
 			return;
 		}
 
 		Logging.err(this, "eventReceivedRouteShare() expected a CoordinatorAsClusterMember/ClusterMember as parent, parent is: " + mParent);
 	}
 
 	/**
 	 * Handles a AnnounceHRMIDs packet.
 	 * 
 	 * @param pAnnounceHRMIDsPacket the packet
 	 */
 	@SuppressWarnings("unchecked")
 	private void eventReceivedAnnounceHRMIDs(AnnounceHRMIDs pAnnounceHRMIDsPacket)
 	{
 		if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_DISTRIBUTION){
 			Logging.log(this, "Received announced peer HRMIDs: " + pAnnounceHRMIDsPacket.getHRMIDs());
 		}
 				
 		/**
 		 * Reset peerHRMIDs
 		 */
 		synchronized (mPeerHRMIDs) {
 			mPeerHRMIDs = (LinkedList<HRMID>) pAnnounceHRMIDsPacket.getHRMIDs().clone();
 		}
 		
 		/**
 		 * Add peerHRMID to peerHRMIDs
 		 */
 		if((getPeerHRMID() != null) && (!getPeerHRMID().isZero())){
 			synchronized (mPeerHRMIDs) {
 				if(!mPeerHRMIDs.contains(getPeerHRMID())){
 					if(HRMConfig.DebugOutput.GUI_SHOW_ADDRESS_DISTRIBUTION){
 						Logging.err(this, "    ..adding to stored peerHRMIDs the peerHRMID: " + getPeerHRMID());
 					}
 					mPeerHRMIDs.add(getPeerHRMID());
 				}
 			}
 		}
 		
 		/**
 		 * Inform the parent ClusterMember about the new peer HRMIDs
 		 */
 		if(mParent instanceof ClusterMember){
 			detectNeighborhood();
 		}else{
 			Logging.err(this, "eventReceivedAnnounceHRMIDs() expected a ClusterMember as parent, parent is: " + mParent);
 		}
 	}
 
 	/**
 	 * Sets the last signaled own priority
 	 * 
 	 * @param pNewPriority the new signaled priority
 	 */
 	private boolean setSignaledPriority(ElectionPriority pNewPriority)
 	{
 		boolean tResult = false;
 		
 		if (pNewPriority == null){
 			Logging.warn(this, "Trying to set a NULL POINTER as signaled priority, ignoring this request, current priority: " + mSignaledPriority);
 			return false;
 		}
 
 		if(pNewPriority.getValue() < 0){
 			throw new RuntimeException("Invalid priority update from " + getSignaledPriority().getValue() + " to " + pNewPriority.getValue());
 		}
 		
 		// is the new value equal to the old one?
 		if(!pNewPriority.equals(getSignaledPriority())){
 			Logging.log(this, "Updating peer priority from " + mPeerPriority.getValue() + " to " + pNewPriority.getValue());
 	
 			// update the peer Election priority itself
 			mSignaledPriority = pNewPriority;
 			
 			// we have a new priority
 			tResult = true;
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the Election priority which was last signaled to the communication peer
 	 * 
 	 * @return the Election priority
 	 */
 	public ElectionPriority getSignaledPriority()
 	{
 		if (mSignaledPriority == null){
 			mSignaledPriority = ElectionPriority.create(this);
 		}
 			
 		return mSignaledPriority;
 	}
 	
 	/**
 	 * Returns if the local entity is currently signaled as winner towards the peer
 	 * 
 	 * @return true or false
 	 */
 	public boolean isSignaledAsWinner()
 	{
 		return mSignaledAsWinner;
 	}
 	
 	/**
 	 * Updates the Election priority of the peer.
 	 * 
 	 * @param pPeerPriority the Election priority
 	 */
 	public boolean setPeerPriority(ElectionPriority pPeerPriority)
 	{
 		boolean tResult = false;
 		
 		if (pPeerPriority == null){
 			Logging.warn(this, "Trying to set a NULL POINTER as peer priority, ignoring this request, current priority: " + getPeerPriority());
 			return false;
 		}
 
 		if(pPeerPriority.getValue() < 0){
 			throw new RuntimeException("Invalid priority update from " + mPeerPriority.getValue() + " to " + pPeerPriority.getValue());
 		}
 		
 		// is the new value equal to the old one?
 		if(!pPeerPriority.equals(mPeerPriority)){
 			// get the current simulation time
 			double tNow = mHRMController.getSimulationTime();
 			
 			Logging.log(this, "Updating peer priority from " + mPeerPriority.getValue() + " to " + pPeerPriority.getValue() + ", last update was " + (tNow - mPeerPriorityTimestampLastUpdate) + " seconds before");
 			
 			// update the freshness of the peer priority
 			mPeerPriorityTimestampLastUpdate = tNow;
 	
 			// update the peer Election priority itself
 			mPeerPriority = pPeerPriority;
 			
 			// we have a new priority
 			tResult = true;
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the Election priority of the communication peer
 	 * 
 	 * @return the Election priority
 	 */
 	public ElectionPriority getPeerPriority()
 	{
 		if (mPeerPriority == null){
 			mPeerPriority = ElectionPriority.create(this);
 		}
 			
 		return mPeerPriority;
 	}
 
 	/**
 	 * Determines the connectivity of the peer node
 	 * If the peer has a modified node weight, this leads to a higher connectivity value and delivers a correct value for comparing it with the local connectivity.
 	 * 
 	 * @return the connectivity of the node 
 	 */
 	public long getPeerConnectivity()
 	{
 		long tResult = 0;
 		
 		tResult = (getPeerPriority().getValue() / ElectionPriority.OFFSET_FOR_CONNECTIVITY);
 		
 		return tResult;
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
 	 * Returns true if this comm. channel leads to a remote node
 	 *  
 	 * @return true or false
 	 */
 	public boolean toRemoteNode()
 	{
 		return (!mHRMController.getNodeL2Address().equals(getPeerL2Address()));
 	}
 
 	/**
 	 * Returns true if this comm. channel leads to the local node
 	 *  
 	 * @return true or false
 	 */
 	public boolean toLocalNode()
 	{
 		return (mHRMController.getNodeL2Address().equals(getPeerL2Address()));
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
 	 * Count the amount of sent packets
 	 * 
 	 * @return the counter
 	 */
 	public int countSentPackets()
 	{
 		return mSentPackets;
 	}
 	
 	/**
 	 * Count the amount of received packets
 	 * 
 	 * @return the counter
 	 */
 	public int countReceivedPackets()
 	{
 		return mReceivedPackets;
 	}
 
 	/**
 	 * Returns a storage with all sent/received packets
 	 * 
 	 * @return the packet I/O storage
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ComChannelPacketMetaData> getSeenPackets()
 	{
 		LinkedList<ComChannelPacketMetaData> tResult = null;
 		
 		synchronized (mPackets) {
 			tResult = (LinkedList<ComChannelPacketMetaData>) mPackets.clone();
 		}
 
 		return tResult;
 	}
 	
 	/**
 	 * Stores a packet for delayed debugging
 	 * 
 	 * @param pPacket the packet
 	 */
 	public void storePacket(SignalingMessageHrm pPacket, boolean pWasSent)
 	{
 		if(!isClosed()){
 			synchronized (mPackets) {
 				if (pWasSent){
 					/**
 					 * count the packets
 					 */
 					mSentPackets++;
 				}else{
 					/**
 					 * count the packets
 					 */
 					mReceivedPackets++;
 				}
 				// limit the storage size
 				while(mPackets.size() > HRMConfig.DebugOutput.COM_CHANNELS_MAX_PACKET_STORAGE_SIZE){
 					mPackets.removeFirst();
 				}
 
 				/**
 				 * TRACK PACKETS
 				 */
 				if(HRMConfig.DebugOutput.ALLOW_MEMORY_CONSUMING_TRACK_COMM_CHANNEL_PACKETS){
 					// add the packet to the storage: filter AnnounceCoordinator, RouteReport, RouteShare
 					if(!((pPacket instanceof AnnounceCoordinator) || (pPacket instanceof RouteReport) || (pPacket instanceof RouteShare) || (pPacket instanceof InvalidCoordinator))){
 						mPackets.add(new ComChannelPacketMetaData(pPacket, pWasSent, mHRMController.getSimulationTime()));
 					}
 				}
 			}
 		}else{
 			// channel already closed, we are not interested in old packets anymore
 		}
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
 		if(mChannelState != ChannelState.CLOSED /* at least, "half_open" is needed */){
 			// create destination description
 			ClusterName tDestinationClusterName = getRemoteClusterName();
 			
 			if (tDestinationClusterName != null){
 				if (HRMConfig.DebugOutput.SHOW_SENT_CHANNEL_PACKETS){
 					Logging.log(this, "SENDING DATA " + pPacket + " to destination " + tDestinationClusterName);
 				}
 		
 				// create the source description
 				ClusterName tSourceClusterName = new ClusterName(mHRMController, mParent.getHierarchyLevel(), mParent.getClusterID(), mParent.getCoordinatorID());
 				
 				// add source route entry
 				pPacket.addSourceRoute("[S]: " + this.toString());
 				
 				// create the Multiplex-Header
 				pPacket.setMultiplexHeader(tSourceClusterName, tDestinationClusterName);
 					
 				/**
 				 * Store the packet 
 				 */
 				storePacket(pPacket, true);
 
 				/**
 				 * Store the election priority which the peer was told last
 				 */
 				if(pPacket instanceof SignalingMessageElection){
 					/**
 					 * priority
 					 */
 					SignalingMessageElection tSignalingMessageElection = (SignalingMessageElection)pPacket;
 					
 					ElectionPriority tNewPriority = tSignalingMessageElection.getSenderPriority();
 					
 					if(!tNewPriority.isUndefined()){
 						setSignaledPriority(tNewPriority);
 					}
 					
 					/**
 					 * winner
 					 */
 					if(pPacket instanceof ElectionAnnounceWinner){
 						mSignaledAsWinner = true;
 					}
 					if(pPacket instanceof ElectionResignWinner){
 						mSignaledAsWinner = false;
 					}					
 				}
 				
 				// send the final packet (including multiplex-header)
 				return getParentComSession().write(pPacket);
 			}else{
 				Logging.warn(this, "Destination is still undefined, skipping packet payload " + pPacket);
 				return false;
 			}
 		}else{
 			Logging.err(this, "sendPacket() found closed channel, dropping packet: " + pPacket);
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
 	 * Sends "RequestHRMID"
 	 * 
 	 * @param pHRMID the HRMID which is requested to be assigned
 	 */
 	public void requestHRMIDAssignment(HRMID pHRMID)
 	{
 		HRMID tSenderHRMID = mParent.getHRMID();
 		if(mParent instanceof ClusterMember){
 			ClusterMember tParentClusterMember = (ClusterMember)mParent;
 			if(tParentClusterMember.getL0HRMID() != null){
 				tSenderHRMID = tParentClusterMember.getL0HRMID();
 			}
 		}
 		
 		/**
 		 * Send RequestHRMID
 		 */
 		// create new AssignHRMID packet for the cluster member
 		RequestHRMID tRequestHRMIDPacket = new RequestHRMID(tSenderHRMID, getPeerHRMID(), pHRMID);
 		// send the packet
 		sendPacket(tRequestHRMIDPacket);
 	}
 
 	/**
 	 * Sends "AssignHRMID"
 	 * 
 	 * @param pHRMID the HRMID which is to be assigned
 	 */
 	public void distributeAssignHRMID(HRMID pHRMID, boolean pDistributeFirmAddress)
 	{
 		Logging.log(this, "Distributing assigned HRMID: " + pHRMID + ", firm=" + pDistributeFirmAddress);
 		
 		HRMID tSenderHRMID = mParent.getHRMID();
 		if(mParent instanceof ClusterMember){
 			ClusterMember tParentClusterMember = (ClusterMember)mParent;
 			if(tParentClusterMember.getL0HRMID() != null){
 				tSenderHRMID = tParentClusterMember.getL0HRMID();
 			}
 		}
 		
 		/**
 		 * Send AssignHRMID
 		 */
 		// create new AssignHRMID packet for the cluster member
 		AssignHRMID tAssignHRMIDPacket = new AssignHRMID(tSenderHRMID, getPeerHRMID(), pHRMID);
 		if(pDistributeFirmAddress){
 			tAssignHRMIDPacket.setFirmAddress();
 		}
 		// send the packet
 		sendPacket(tAssignHRMIDPacket);
 		
 		/**
 		 * Store the new HRMID for the peer
 		 */
 		setPeerHRMID(pHRMID);
 		
 		/**
 		 * Trigger: new peer HRMIDs because we got a new HRMID assigned
 		 */
 		if(mParent.getHierarchyLevel().isBaseLevel()){
 			detectNeighborhood();
 		}
 	}
 
 	/**
 	 * Revokes all formerly assigned HRMIDs
 	 */	
 	public void signalRevokeAssignedHRMIDs()
 	{
 		// debug output
 		LinkedList<HRMID >tPeerHRMIDs = getPeerHRMIDs();
 		if (tPeerHRMIDs.size() > 0){
 			Logging.log(this, "Revoking assigned HRMIDs...");
 			int i = 0;
 			for(HRMID tHRMID : tPeerHRMIDs){				
 				synchronized (mAssignedPeerHRMIDs) {
 					mAssignedPeerHRMIDs.remove(tHRMID);
 				}
 				synchronized (mPeerHRMIDs) {
 					mPeerHRMIDs.remove(tHRMID);
 				}
 
 				Logging.log(this, "    ..[" + i + "]: " + tHRMID);
 				i++;
 			}
 			mPeerHRMID = null;
 			
 			/**
 			 * Revoke the HRMIDs from the peer
 			 */
 			// create the packet
 			RevokeHRMIDs tRevokeHRMIDsPacket = new RevokeHRMIDs(mHRMController.getNodeL2Address(), getPeerHRMID(), tPeerHRMIDs);
 			// send the packet
 			sendPacket(tRevokeHRMIDsPacket);
 		}
 	}
 
 	/**
 	 * Returns the list of known assigned peer HRMIDs
 	 * 
 	 * @return the list
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<HRMID> getAssignedPeerHRMIDs()
 	{
 		LinkedList<HRMID> tResult = null;
 		
 		synchronized (mAssignedPeerHRMIDs) {
 			tResult = (LinkedList<HRMID>) mAssignedPeerHRMIDs.clone();
 		}
 		
 		return tResult; 
 	}
 
 	/**
 	 * Returns the list of known peer HRMIDs
 	 * 
 	 * @return the list
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<HRMID> getPeerHRMIDs()
 	{
 		LinkedList<HRMID> tResult = null;
 		
 		synchronized (mPeerHRMIDs) {
 			tResult = (LinkedList<HRMID>) mPeerHRMIDs.clone();
 		}
 		
 		return tResult; 
 	}
 	
 	/**
 	 * Return the number of domains the peer node node belongs to
 	 * 
 	 * @return
 	 */
 	public int getPeerDomains()
 	{
 		int tResult = 0;
 		
 		synchronized (mPeerHRMIDs) {
 			tResult = mPeerHRMIDs.size();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Acknowledges a RequestClusterMembership packet
 	 * 
 	 * @param pSource the source of the acknowledgment (e.g., a coordinator description)
 	 * @param pRequestClusterMembershipPacket the causing packet
 	 */
 	public void signalRequestClusterMembershipAck(ClusterName pSource, RequestClusterMembership pRequestClusterMembershipPacket)
 	{
 		// create the packet
 		RequestClusterMembershipAck tRequestClusterMembershipAckPacket = new RequestClusterMembershipAck(mHRMController.getNodeL2Address(), getPeerHRMID(), pSource);
 		// send the packet
 		Logging.log(this, "Acknowledging cluster membership request " + pRequestClusterMembershipPacket + " by: " + tRequestClusterMembershipAckPacket + " for " + getRemoteClusterName());
 		sendPacket(tRequestClusterMembershipAckPacket);
 	}
 
 	/**
 	 * Closes the comm. channel
 	 */
 	public synchronized void closeChannel(String pCause)
 	{
 		Logging.log(this, "Closing this channel");
 		if(isOpen()){
 			/**
 			 * Inform the peer
 			 */
 			if(mParent instanceof Cluster){
 				Cluster tParentCluster = (Cluster)mParent;
 				
 				/**
 				 * Send "InformClusterMembershipCanceled" along the comm. channel
 				 */
 				InformClusterMembershipCanceled tInformClusterMembershipCanceled = new InformClusterMembershipCanceled(mHRMController.getNodeL2Address(), mHRMController.getNodeL2Address(), tParentCluster.createClusterName(), getRemoteClusterName());
 			    Logging.log(this, "       ..sending membership canceled: " + tInformClusterMembershipCanceled);
 			    sendPacket(tInformClusterMembershipCanceled);
 			}else if(mParent instanceof ClusterMember){
 				/**
 				 * Send: "Leave" to all superior clusters
 				 */
 				InformClusterLeft tInformClusterLeft = new InformClusterLeft(mHRMController.getNodeL2Address(), getPeerHRMID(), null, null);
 			    Logging.log(this, "       ..sending cluster left: " + tInformClusterLeft);
 				sendPacket(tInformClusterLeft);
 			}
 
 			/**
 			 * Change the channel state
 			 */
 			setState(ChannelState.CLOSED, "::closeChannel()");
 		}else{
 		    Logging.log(this, "       ..channel wasn't established, parent is: " + mParent);
 		}
 		
 		mCloseCause = pCause;
 		
 		// unregister from the parent comm. session
 		mParentComSession.unregisterComChannel(this, this + "::closeChannel()\n   ^^^^" + pCause);
 		
 		// some early memory freeing
 		//HINT: closed channel (-> marked as "deleted") are stored in a list but their former packets are not interesting anymore in this case
 		mPackets.clear();
 
 		//HINT: closed channel (-> marked as "deleted") are stored in a list and their remaining buffered packets are processed anyway -> DO NOT clear mPacketQueue
 	}
 	
 	/**
 	 * Returns the cause for the closing of this channel
 	 * 
 	 * @return the cause
 	 */
 	public String getCloseCause()
 	{
 		return mCloseCause;
 	}
 	
 	/**
 	 * Resets the timeout of this channel
 	 * 
 	 * @param pCause the cause for this call
 	 */
 	public void resetTimeout(String pCause)
 	{
 		// reset the timeout to 0
 //		if(mTimeout != 0){
 //			Logging.warn(this, "Resetting timeout now due: " + pCause);
 //		}
 		mTimeout = 0;
 		mTimeLastPingPeer = 0;
 	}
 
 	/**
 	 * Returns the timeout of this channel
 	 * 
 	 * @return the timeout
 	 */
 	public double getTimeout()
 	{
 		return mTimeout;
 	}
 	
 	/**
 	 * Sets the timeout of this channel 
 	 * 
 	 * @param pTimerOffset the offset of the timer
 	 */
 	public void setTimeout(String pCause)
 	{
 		if(mTimeout == 0){
 			/**
 			 * need MAX_E2E_DELAY for 2 transmissions: 1.) PING, 2.) ALIVE
 			 * add additional MAX_E2E_DELAY to allow the peer to show its life state by "normal signaling traffic"
 			 */
 			double tOffset = HRMConfig.Hierarchy.TIME_BEFORE_CHANNEL_IS_PINGED + 2 * HRMConfig.Hierarchy.MAX_E2E_DELAY;
 			mTimeout = mHRMController.getSimulationTime() + tOffset;
 			mTimeoutStart = mHRMController.getSimulationTime();
 			mTimeoutCause = pCause;
 		}else{
 			// timeout already set
 		}
 		
 //		Logging.warn(this, "Got a defined timeout of: " + tOffset + ", will end at: " + mTimeout + ", cause=" + pCause);		
 	}
 
 	/**
 	 * Returns the time of the last refresh
 	 * 
 	 * @return the searched time
 	 */
 	public double timeoutStart()
 	{
 		return mTimeoutStart;
 	}
 
 	/**
 	 * Returns the cause for the timeout
 	 * 
 	 * @return the cause
 	 */
 	public String getTimeoutCause()
 	{
 		return mTimeoutCause;
 	}
 	
 	/**
 	 * Returns if this channel is obsolete due refresh timeout (peer isn't there anymore)
 	 * 
 	 * @return true or false
 	 */
 	public boolean isObsolete()
 	{
 		boolean tResult = false;
 		
 		/**
 		 * timeout set?
 		 */
 		if(mTimeout > 0){
 			/**
 			 * should we actively ping the peer in order to get its life state?
 			 */
 			if((mTimeoutStart + HRMConfig.Hierarchy.TIME_BEFORE_CHANNEL_IS_PINGED < mHRMController.getSimulationTime()) && (mTimeLastPingPeer == 0) && (mTimeout > mHRMController.getSimulationTime())){
 				mTimeLastPingPeer = mHRMController.getSimulationTime();
 				
 				// try to ping the peer entity -> if the peer answers this packet within 2*MAX_E2E_DELAY seconds, the peer (e.g., cluster head) is still alive.
 				if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 					Logging.warn(this, "CHECKING COM. TO PEER: " + getPeerL2Address());
 				}
 
 				signalPingPeerPacket(false);
 			}else{
 				/**
 				 * final timeout occurred?
 				 */
 				if(mTimeout < mHRMController.getSimulationTime()){
 					mTimeLastPingPeer = 0;
 					tResult = true;
 				}
 			}
 		}
 		
 		if(tResult){
 			Logging.log(this, "This channel got deprecated due timeout");
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Main packet receive function. It is used by the parent ComSession.
 	 *  
 	 * @param pPacket the packet
 	 * 
 	 * @return true
 	 */
 	public boolean receivePacket(SignalingMessageHrm pPacket)
 	{
 		/**
 		 * Store the packet in queue
 		 */
 		synchronized (mPacketQueue) {
 			mPacketQueue.add(pPacket);
 			
 			mHRMController.notifyPacketProcessor(this);
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Returns the packet queue of this channel
 	 *  
 	 * @return the packet queue
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<SignalingMessageHrm> getPacketQueue()
 	{
 		LinkedList<SignalingMessageHrm> tResult = null;
 		
 		synchronized (mPacketQueue) {
 			tResult = (LinkedList<SignalingMessageHrm>) mPacketQueue.clone();			
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Processes one packet, triggered by packet processor (HRMController)
 	 */
 	public void processOnePacket()
 	{
 		SignalingMessageHrm tNextPacket = null;
 		
 		double tBefore = HRMController.getRealTime();
 		synchronized (mPacketQueue) {
 			if(mPacketQueue.size() > 0){
 				tNextPacket = mPacketQueue.removeFirst();
 			}
 		}
 		double tSpentTime = HRMController.getRealTime() - tBefore;
 		if(tSpentTime > 10){
 			Logging.log(this, "    ..processOnePacket() took " + tSpentTime + " ms for getting next packet: " + tNextPacket);
 		}
 
 		if(tNextPacket != null){
 			if((mParent.isThisEntityValid()) || (tNextPacket instanceof RequestClusterMembership) /* never block this kind of packets */){
 				tBefore = HRMController.getRealTime();
 				handlePacket(tNextPacket);
 				tSpentTime = HRMController.getRealTime() - tBefore;
 				if(tSpentTime > 50){
 					Logging.log(this, "    ..processOnePacket() took " + tSpentTime + " ms for handling packet: " + tNextPacket);
 				}
 			}else{
 				if(HRMConfig.Measurement.VALIDATE_RESULTS_EXTENSIVE){
 					Logging.warn(this, "Parent control entity is already invalidated, dropping received packet: " + tNextPacket);
 				}
 			}
 		}else{
 			Logging.err(this, "Cannot process an invalid packet");
 		}
 	}
 	
 	/**
 	 * Processes one packet 
 	 * 
 	 * @param pPacket the packet
 	 * 
 	 * @return true if everything worked fine
 	 */
 	@SuppressWarnings("unused")
	private boolean handlePacket(SignalingMessageHrm pPacket)
 	{
 		/**
 		 * Refresh the timeout
 		 */
 		resetTimeout(pPacket.toString());
 		
 		/**
 		 * Store the packet 
 		 */
 		storePacket(pPacket, false);
 		
 		if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS){
 			Logging.log(this, "RECEIVED DATA (" + pPacket + ") from \"" + getPeerL2Address() + "/" + getPeerHRMID() + "\": " + pPacket);
 		}
 			
 		/*
 		 * Invalid data
 		 */
 		if(pPacket == null) {
 			Logging.err(this, "Received invalid null pointer as data");
 			return false;
 		}
 
 		/**
 		 * Drop packet due to AS-Split
 		 */
 		if(mParent instanceof ClusterMember){
 			ClusterMember tParentClusterMember = (ClusterMember)mParent;
 			if(pPacket instanceof ISignalingMessageHrmTopologyASSeparator){
 				ISignalingMessageHrmTopologyASSeparator tSignalingMessageASSeparator = (ISignalingMessageHrmTopologyASSeparator)pPacket;
 				if(!tSignalingMessageASSeparator.isAllowedToEnterAs(mHRMController, getPeerAsID())){
 					//Logging.warn(this, "Dropping packet due to AS border, packet=" + pPacket);
 					return true;
 				}
 
 				if((tParentClusterMember.enforcesASSplit()) && (!tSignalingMessageASSeparator.isAllowedToEnterAs(mHRMController, new Long(-1 /* some invalid value which differs from the local one */)))){
 					//Logging.warn(this, "Dropping packet due to AS-split, packet=" + pPacket);
 					return true;
 				}
 			}
 		}
 
 		/**
 		 * HRM signaling message
 		 */
 		if (pPacket instanceof SignalingMessageHrm){
 			// cast to a SignalingMessageHrm signaling message
 			SignalingMessageHrm tSignalingMessageHrmPacket = (SignalingMessageHrm)pPacket;
 		
 			// process SignalingMessageHrm message
 			getPeerHRMIDFromHRMSignalingMessage(tSignalingMessageHrmPacket);
 			
 			// add source route entry
 			tSignalingMessageHrmPacket.addSourceRoute("[R]: " + this.toString());
 
 			//HINT: don't return here because we are still interested in the more detailed packet data from derived packet types!
 		}
 		
 		/**
 		 * Election signaling message:
 		 * 			Cluster ==> ClusterMember
 		 * 			ClusterMember ==> Cluster
 		 */
 		if (pPacket instanceof SignalingMessageElection) {
 			// cast to a Election signaling message
 			SignalingMessageElection tElectionPacket = (SignalingMessageElection)pPacket;
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING_ELECTIONS)
 				Logging.log(this, "RECEIVED BULLY MESSAGE " + tElectionPacket.getClass().getSimpleName());
 
 			// the packet is received by a cluster
 			//HINT: this is only possible at base hierarchy level
 			if (mParent instanceof ClusterMember){
 				ClusterMember tParentClusterProxy = (ClusterMember)mParent;
 				
 				if (tParentClusterProxy.getElector() != null){
 					tParentClusterProxy.getElector().handleElectionMessage(tElectionPacket, this);
 				}else{
 					Logging.warn(this, "Elector is still invalid");
 				}
 				return true;
 			}		
 
 			// the packet is received by a coordinator
 			if (mParent instanceof Coordinator){
 				Coordinator tCoordinator = (Coordinator)mParent;
 				
 				tCoordinator.getCluster().getElector().handleElectionMessage(tElectionPacket, this);
 				
 				return true;
 			}
 
 			Logging.warn(this, "IGNORING THIS MESSAGE: " + tElectionPacket);
 
 			return true;
 		}
 
 		/**
 		 * RouteReport
 		 */
 		if (pPacket instanceof RouteReport){
 			// cast to a RouteReport signaling message
 			RouteReport tRouteReportPacket = (RouteReport)pPacket;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "TOPOLOGY_REPORT-received from \"" + getPeerHRMID() + "\": " + tRouteReportPacket);
 
 			// process RouteReport message
 			eventReceivedRouteReport(tRouteReportPacket);
 			
 			return true;
 		}
 
 		/**
 		 * RouteShare:
 		 */
 		if (pPacket instanceof RouteShare){
 			// cast to a RouteShare signaling message
 			RouteShare tRouteSharePacket = (RouteShare)pPacket;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "ROUTING_INFORMATION-received from \"" + getPeerHRMID() + "\": " + tRouteSharePacket);
 
 			// process RouteShare message
 			eventReceivedRouteShare(tRouteSharePacket);
 			
 			return true;
 		}
 		
 		/**
 		 * PingPeer:
 		 * 			CoordinatorAsClusterMember ==> Cluster (head)
 		 */
 		if (pPacket instanceof PingPeer){
 			PingPeer tPingPeerPacket = (PingPeer)pPacket;
 			
 			if(tPingPeerPacket.isPacketTracking()){
 				Logging.warn(this, "RECEIVED PING_PACKET: " + tPingPeerPacket);
 			}
 			
 			if(mParent instanceof ClusterMember){
 				ClusterMember tParentClusterMember = (ClusterMember)mParent;
 				
 				tParentClusterMember.eventReceivedPing(this, tPingPeerPacket);
 			}
 			return true;
 		}
 		
 		/**
 		 * AssignHRMID:
 		 * 			Coordinator (via Cluster) ==> all inferior local/remote ClusterMember
 		 */
 		if(pPacket instanceof AssignHRMID) {
 			AssignHRMID tAssignHRMIDPacket = (AssignHRMID)pPacket;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "ASSIGN_HRMID-received from \"" + getPeerHRMID() + "\" assigned HRMID: " + tAssignHRMIDPacket.getHRMID().toString());
 
 			HRMID tAssignedHRMID = tAssignHRMIDPacket.getHRMID();
 
 			// let the coordinator process the HRMID assignment
 			mParent.eventAssignedHRMID(this, tAssignedHRMID, tAssignHRMIDPacket.isFirmAddress());
 			
 			/**
 			 * Trigger: new peer HRMIDs because we got a new HRMID assigned
 			 */
 			if(mParent.getHierarchyLevel().isBaseLevel()){
 				detectNeighborhood();
 			}
 			
 			return true;
 		}
 
 		/**
 		 * RequestHRMID:
 		 * 			an inferior local/remote ClusterMember ==> Coordinator (via Cluster)
 		 */
 		if(pPacket instanceof RequestHRMID) {
 			RequestHRMID tRequestHRMIDPacket = (RequestHRMID)pPacket;
 			
 			if(mParent instanceof Cluster){	
 				Cluster tParentCluster = (Cluster)mParent;
 				
 				if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 					Logging.log(this, "REQUEST_HRMID-received from \"" + getPeerHRMID() + "\" requested HRMID: " + tRequestHRMIDPacket.getHRMID().toString());
 	
 				HRMID tRequestedHRMID = tRequestHRMIDPacket.getHRMID();
 	
 				// let the parent cluster process the HRMID request
 				tParentCluster.eventReceivedRequestedHRMID(this, tRequestedHRMID);
 			}else{
 				Logging.err(this, "handlePacket() expected a Cluster as parent, parent is: " + mParent + ", packet is: " + tRequestHRMIDPacket);
 			}
 			
 			return true;
 		}
 
 		/**
 		 * AnnounceHRMIDs
 		 */
 		if (pPacket instanceof AnnounceHRMIDs){
 			// cast to a AnnounceHRMIDs signaling message
 			AnnounceHRMIDs tAnnounceHRMIDsPacket = (AnnounceHRMIDs)pPacket;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "ANNOUNCE_HRMIDS-received from \"" + getPeerHRMID() + "\" revoked HRMIDs: " + tAnnounceHRMIDsPacket.getHRMIDs().toString());
 
 			// process AnnounceHRMIDs message
 			eventReceivedAnnounceHRMIDs(tAnnounceHRMIDsPacket);
 			
 			return true;
 		}
 		
 		/**
 		 * RevokeHRMIDs:
 		 * 			Coordinator (via Cluster) ==> all inferior local/remote ClusterMember
 		 */
 		if(pPacket instanceof RevokeHRMIDs){
 			RevokeHRMIDs tRevokeHRMIDsPacket = (RevokeHRMIDs)pPacket;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "REVOKE_HRMIDS-received from \"" + getPeerHRMID() + "\" revoked HRMIDs: " + tRevokeHRMIDsPacket.getHRMIDs().toString());
 
 			// revoke the HRMIDs step-by-step
 			for(HRMID tHRMID: tRevokeHRMIDsPacket.getHRMIDs()){
 				mParent.eventRevokedHRMID(this, tHRMID);
 			}
 			
 			return true;
 		}
 		
 		/**
 		 * RequestClusterMembership:
 		 * 			Cluster ==> Coordinator(CoordinatorAsClusterMember)
 		 *  
 		 */
 		if(pPacket instanceof RequestClusterMembership) {
 			RequestClusterMembership tRequestClusterMembershipPacket = (RequestClusterMembership)pPacket;
 
 //			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)}
 				Logging.log(this, "REQUEST_CLUSTER_MEMBERSHIP-received from \"" + tRequestClusterMembershipPacket.getSenderName() + "\": " + tRequestClusterMembershipPacket);
 			//}
 
 			if(mParent instanceof CoordinatorAsClusterMember){
 				CoordinatorAsClusterMember tParentCoordinatorAsClusterMember = (CoordinatorAsClusterMember)mParent;
 				
 				if(tParentCoordinatorAsClusterMember.isThisEntityValid()){
 					/**
 					 * SEND: acknowledgment -> will be answered by a ElectionPriorityUpdate
 					 */
 					signalRequestClusterMembershipAck(tParentCoordinatorAsClusterMember.createCoordinatorName(), tRequestClusterMembershipPacket);
 		
 					/**
 					 * Trigger: comm. channel established 
 					 */
 					tParentCoordinatorAsClusterMember.eventComChannelEstablished(this);
 				}else{
 					Coordinator tParentCoordinator = tParentCoordinatorAsClusterMember.getCoordinator();
 					Logging.err(this, "WARNING - " + tRequestClusterMembershipPacket + " was received by an already invalidated CoordinatorAsClusterMember instance, creating a new one..");
 							
 					if((getParentComSession().isAvailable()) && (tParentCoordinator.isThisEntityValid())){
 						ComChannel tNewComChannel = tParentCoordinator.eventClusterMembershipRequest(tRequestClusterMembershipPacket.getRequestingCluster(), getParentComSession());
 						Logging.log(this, "  ..created for " + tRequestClusterMembershipPacket + " a new CoordinatorAsClusterMember and the new comm. channel: " + tNewComChannel);
 							
 						if(tNewComChannel != null){
 							Logging.log(this, "  ..delivering packet: " + tRequestClusterMembershipPacket + " directly to: " + tNewComChannel);
 							tNewComChannel.handlePacket(tRequestClusterMembershipPacket);
 						}
 					}else{
 						Logging.log(this, "  ..PARENT Coordinator is ALREADY INVALID, denying request by \"" + tRequestClusterMembershipPacket + "\", parent coordinator is: " + tParentCoordinator);
 						mParentComSession.denyClusterMembershipRequest(tRequestClusterMembershipPacket.getRequestingCluster(), tRequestClusterMembershipPacket.getDestination());
 					}
 				}
 			}else{
 				Logging.err(this, "Expected a CoordinatorAsClusterMember object as parent for processing RequestClusterMembership data but parent is " + mParent);
 			}
 
 			return true;
 		}
 
 		/**
 		 * RequestClusterMembershipAck:
 		 * 			Coordinator(CoordinatorAsClusterMember) ==> Cluster
 		 *  
 		 */
 		if(pPacket instanceof RequestClusterMembershipAck) {
 			RequestClusterMembershipAck tRequestClusterMembershipAckPacket = (RequestClusterMembershipAck)pPacket;
 
 //			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "REQUEST_CLUSTER_MEMBERSHIP_ACK-received from \"" + getPeerHRMID() + "\": " + tRequestClusterMembershipAckPacket);
 
 			// is the parent a coordinator or a cluster?
 			if (mParent instanceof Cluster){
 				Cluster tCluster = (Cluster)mParent;
 				
 				// trigger event "cluster member joined"
 				tCluster.eventClusterMemberJoined(this);		
 			}else{
 				Logging.err(this, "Expected a Cluster object as parent for processing RequestClusterMembershipAck data but parent is " + mParent);
 			}
 			
 			return true;
 		}
 		
 		/**
 		 * InformClusterLeft:
 		 * 			ClusterMember ==> Cluster
 		 */
 		if(pPacket instanceof InformClusterLeft) {
 			InformClusterLeft tInformClusterLeftPacket = (InformClusterLeft)pPacket;
 
 			Logging.log(this, "INFORM_CLUSTER_LEFT-received from \"" + getPeerHRMID() + "\": " + tInformClusterLeftPacket);
 
 			if(!isOpen()){
 				if(HRMConfig.Measurement.VALIDATE_RESULTS_EXTENSIVE){
 					Logging.warn(this, "Received InformClusterLeft in state " + mChannelState.toString() + ": " + tInformClusterLeftPacket);
 				}
 			}
 			
 			// no further transmissions
 			mChannelState = ChannelState.CLOSED;
 
 			// is the parent a coordinator or a cluster?
 			if (mParent instanceof Cluster){
 				Cluster tCluster = (Cluster)mParent;
 				
 				// trigger event "cluster member joined"
 				tCluster.eventClusterMemberLost(this, this + "::handlePacket() for " + tInformClusterLeftPacket);		
 			}else{
 				Logging.err(this, "Expected a Cluster object as parent for processing LeaveCluster data but parent is " + mParent);
 			}
 
 			return true;
 		}
 		
 		/**
 		 * InformClusterMembershipCanceled:
 		 * 			Cluster ==> ClusterMember
 		 */
 		if(pPacket instanceof InformClusterMembershipCanceled) {
 			InformClusterMembershipCanceled tInformClusterMembershipCanceledPacket = (InformClusterMembershipCanceled)pPacket;
 
 			Logging.log(this, "INFORM_CLUSTER_MEMBERSHIP_CANCELED-received from \"" + getPeerHRMID() + "\": " + tInformClusterMembershipCanceledPacket + ", remote cluster name=" + getRemoteClusterName());
 
 			if(!tInformClusterMembershipCanceledPacket.getSendingCluster().equals(getRemoteClusterName())){
 				Logging.err(this, "##########################");
 				Logging.err(this, "### handlePacket() got INFORM_CLUSTER_MEMBERSHIP_CANCELED packet with wrong requesting cluster: " + tInformClusterMembershipCanceledPacket.getSendingCluster() + ", local remote cluster=" + getRemoteClusterName());
 				Logging.err(this, "##########################");
 			}
 			// no further transmissions
 			mChannelState = ChannelState.CLOSED;
 			
 			// is the parent a coordinator or a cluster?
 			if (mParent instanceof ClusterMember){
 				ClusterMember tClusterMember = (ClusterMember)mParent;
 				
 				// trigger event "cluster member joined"
 				Logging.log(this, "   ..invalidating the ClusterMember role of: " + tClusterMember);
 				tClusterMember.eventClusterMemberRoleInvalid(this);		
 			}else{
 				Logging.err(this, "Expected a ClusterMember object as parent for processing LeaveCluster data but parent is " + mParent);
 			}
 			
 			return true;
 		}
 		
 		/**
 		 * AnnounceCluster
 		 * 			Coordinator (via Cluster) ==> all inferior local/remote ClusterMember
 		 */
 		if(pPacket instanceof AnnounceCoordinator) {
 			AnnounceCoordinator tAnnounceClusterPacket = (AnnounceCoordinator)pPacket;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "ANNOUNCE_COORDINATOR-received from \"" + getPeerHRMID() + "\", announcement is: " + tAnnounceClusterPacket);
 		
 			mParent.eventCoordinatorAnnouncement(this, tAnnounceClusterPacket);
 			
 			return true;
 		}
 		
 		/**
 		 * InvalidCoordinator
 		 * 			Coordinator (via Cluster) ==> all inferior local/remote ClusterMember
 		 */
 		if(pPacket instanceof InvalidCoordinator) {
 			InvalidCoordinator tInvalidCoordinatorPacket = (InvalidCoordinator)pPacket;
 
 			if (HRMConfig.DebugOutput.SHOW_RECEIVED_CHANNEL_PACKETS)
 				Logging.log(this, "INVALID_COORDINATOR-received from \"" + getPeerHRMID() + "\", invalidation is: " + tInvalidCoordinatorPacket);
 		
 			mParent.eventCoordinatorInvalidation(this, tInvalidCoordinatorPacket);
 			
 			return true;
 		}
 
 		Logging.warn(this, ">>>>>>>>>>>>> Found unsupported packet: " + pPacket);
 		return true;
 	}
 	
 	/**
 	 * SEND: RouteShare to a cluster member
 	 * 
 	 * @param pRoutingTable the routing table which should be shared
 	 */
 	public void distributeRouteShare(RoutingTable pRoutingTable)
 	{
 		boolean DEBUG = false;
 		
 		/**
 		 * Set the timeout for each entry depending on the state of the known HRM hierarchy
 		 */
 		boolean tReportOnlyADiff = false;
 		
 		if(mHRMController.hasLongTermStableHierarchy()){
 			/**
 			 * should we report only a diff.? 
 			 */
 			if((HRMConfig.Routing.SHARE_ROUTE_RATE_REDUCTION_FOR_STABLE_HIERARCHY) && (mTimeLastCompleteSharedRoutingTable > 0) && (mHRMController.getSimulationTime() < mTimeLastCompleteSharedRoutingTable + HRMConfig.Routing.ROUTE_TIMEOUT_STABLE_HIERARCHY) && (!mLastSharedRoutingTableWasDuringUnstableHierarchy)){
 				/**
 				 * we actually provide only a diff to the last diff/complete reported routing table
 				 */
 				tReportOnlyADiff = true;
 				
 				RoutingTable tDiffReportRoutingTable = new RoutingTable();
 				for(RoutingEntry tNewEntry : pRoutingTable){
 					boolean tEntryHasChanges = true;
 					for(RoutingEntry tOldEntry : mLastSentSharedRoutingTable){
 						/**
 						 * is the new entry rather an old one?
 						 */
 						if ((tOldEntry.equals(tNewEntry)) && (tOldEntry.equalsQoS(tNewEntry))){
 							tEntryHasChanges = false;
 							break;
 						}
 					}
 					
 					/**
 					 * add the entry with changes to the "diff" table
 					 */
 					if(tEntryHasChanges){
 						tDiffReportRoutingTable.add(tNewEntry);
 					}
 				}
 				
 				// store the complete routing table as last report but send only the diff
 				mLastSentSharedRoutingTable = (RoutingTable) pRoutingTable.clone();
 				// the "diff" table
 				pRoutingTable = tDiffReportRoutingTable;
 				pRoutingTable.markAsDiff();
 						
 				if (DEBUG){
 					Logging.log(this, "   ..sharing the DIFF TABLE with " + getPeerL2Address() + ":");
 					int j = 0;
 					for(RoutingEntry tEntry : pRoutingTable){
 						Logging.log(this, "     ..[" + j +"] (TO: " + tEntry.getTimeout() + "): " + tEntry);
 						j++;
 					}
 				}
 			}
 			
 			mLastSharedRoutingTableWasDuringUnstableHierarchy = false;
 		}else{
 			mLastSharedRoutingTableWasDuringUnstableHierarchy = true;
 		}
 		
 		/**
 		 * Remember the time of the last reported complete routing table -> report every x seconds a complete table
 		 */
 		if(!tReportOnlyADiff){
 			// report a complete routing table
 			mLastSentSharedRoutingTable = (RoutingTable) pRoutingTable.clone();
 			// store the time
 			mTimeLastCompleteSharedRoutingTable = mHRMController.getSimulationTime();
 			
 			if (DEBUG){
 				Logging.log(this, "   ..sharing the COMPLETE TABLE with " + getPeerL2Address() + ":");
 				int j = 0;
 				for(RoutingEntry tEntry : pRoutingTable){
 					Logging.log(this, "     ..[" + j +"] (TO: " + tEntry.getTimeout() + "): " + tEntry);
 					j++;
 				}
 			}
 		}
 
 		
 		if(pRoutingTable.size() > 0){
 			// create new RouteShare packet for the cluster member
 			RouteShare tRouteSharePacket = new RouteShare(mHRMController.getNodeL2Address(), getPeerHRMID(), mHRMController, pRoutingTable);
 			
 			// send the packet
 			sendPacket(tRouteSharePacket);
 		}
 	}
 
 	/**
 	 * SEND: PingPeer
 	 * 
 	 * @param pTrackingActive is tracking active?
 	 */
 	public void signalPingPeerPacket(boolean pTrackingActive)
 	{
 		// create new ProbePacket packet
 		PingPeer tPingPeerPacket = new PingPeer(mHRMController.getNodeL2Address(),  getPeerHRMID());
 		if(pTrackingActive){
 			tPingPeerPacket.activateTracking();
 
 			Logging.warn(this, "SENDING PING_PEER_PACKET: " + tPingPeerPacket);
 		}
 		
 		// send the packet
 		sendPacket(tPingPeerPacket);
 	}
 
 	/**
 	 * (De-)activates the HRM link for election and later topology distribution
 	 * 
 	 * @param pState the new state
 	 * @param pCause describes the cause for this change
 	 */
 	public void setLinkActivationForElection(boolean pState, String pCause)
 	{
 		Logging.log(this, "Updating link activation from: " + mLinkActiveForElection + " to: " + pState);
 
 		mLinkActiveForElection = pState;
 		
 		if(HRMConfig.DebugOutput.ALLOW_MEMORY_CONSUMING_TRACK_LINK_ACTIVATIONS){
 			mDesccriptionLinkActivation += "\n ..[" +pState +"] <== " + pCause;
 		}
 	}
 	
 	/**
 	 * Returns a description about all link activation changes
 	 * 
 	 * @return the description about all changes
 	 */
 	public String getDescriptionLinkActivation()
 	{
 		return mDesccriptionLinkActivation;
 	}
 	
 	/**
 	 * Returns true if the parent and the peer use actively this link for election and topology distribution (e.g., a cluster member is link to a cluster)
 	 * 
 	 * @return true or false
 	 */
 	public boolean isLinkActiveForElection()
 	{
 		return mLinkActiveForElection;
 	}	
 	
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mParent.toString() + "(Peer="+ (getPeerL2Address() != null ? (getPeer() != null ? getPeer() : getPeerL2Address()) + ", PeerHRMID=" + getPeerHRMID() : "") + ", " + mChannelState.toString() + ", " + (mLinkActiveForElection ? "ActiveLINK" : "InactiveLINK") + ")";
 	}
 }
