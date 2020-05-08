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
 
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
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
 	 * Stores the network interface for base hierarchy level
 	 */
 	private NetworkInterface mBaseHierarchyLevelNetworkInterface = null;
 	
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
 	 * Stores if the cluster role is still valid
 	 */
 	private boolean mClusterRoleValid = true;
 
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
 	 * Forwards a coordinator announcement by sending it to all cluster members
 	 * @param pSourceL2Address 
 	 * 
 	 * @param pAnnounceCoordinator the coordinator announcement
 	 */
 	public void forwardCoordinatorAnnouncement(L2Address pSourceL2Address, AnnounceCoordinator pAnnounceCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "Forwarding coordinator announcement: " + pAnnounceCoordinator);
 		}
 	
 		// do we have a loop?
 		if(!pAnnounceCoordinator.knowsCluster(getClusterID())){
 			// check the TTL once more
 			if(pAnnounceCoordinator.isTTLOkay()){
 				// forward this announcement to all cluster members
 				sendClusterBroadcast(pAnnounceCoordinator, true, pSourceL2Address /* do NOT forward to the source */);
 			}else{
 				Logging.err(this, "forwardCoordinatorAnnouncement() found invalid TTL for: " + pAnnounceCoordinator);
 			}
 		}else{
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.log(this, "forwardCoordinatorAnnouncement() found a forwarding loop for: " + pAnnounceCoordinator + ", dropping this packet");
 			}
 		}
 	}
 	
 	/**
 	 * Forwards a coordinator announcement by sending it to all cluster members
 	 * @param pSourceL2Address 
 	 * 
 	 * @param pAnnounceCoordinator the coordinator announcement
 	 */
 	public void forwardCoordinatorInvalidation(L2Address pSourceL2Address, InvalidCoordinator pInvalidCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "Forwarding coordinator invalidation: " + pInvalidCoordinator);
 		}
 	
 		// do we have a loop?
 		if(!pInvalidCoordinator.knowsCluster(getClusterID())){
 			// check the TTL once more
 			if(pInvalidCoordinator.isTTLOkay()){
 				// forward this invalidation to all cluster members
 				sendClusterBroadcast(pInvalidCoordinator, true, pSourceL2Address /* do NOT forward to the source */);
 			}else{
 				Logging.err(this, "forwardCoordinatorInvalidation() found invalid TTL for: " + pInvalidCoordinator);
 			}
 		}else{
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 				Logging.log(this, "forwardCoordinatorInvalidation() found a forwarding loop for: " + pInvalidCoordinator + ", dropping this packet");
 			}
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
 			return BullyPriority.create(this, mHRMController.getHierarchyNodePriority());
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
 		Logging.log(this, "EVENT: coordinator was lost");
 		
 		/**
 		 * Revoke HRMID of physical node if we are on base hierarchy level
 		 */ 
 		if(getHierarchyLevel().isBaseLevel()){
 			Logging.log(this, "Revoking physical node HRMID: " + getHRMID());
 			
 			eventRevokedHRMID(this, getHRMID());
 		}
 	}
 	
 	/**
 	 * EVENT: "lost cluster member", triggered by Elector in case a member left the election 
 
 	 * @param pComChannel the comm. channel of the lost cluster member
 	 */
 	public synchronized void eventClusterMemberLost(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: lost cluster member, comm. channel: " + pComChannel);
 		
 		// unregister the comm. channel
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
 	public synchronized void eventClusterRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: cluster role invalid");
 		
 		/**
 		 * TRIGGER: event coordinator role invalid
 		 */
 		if (getCoordinator() != null){
 			Logging.log(this, "     ..eventClusterRoleInvalid() invalidates now the local coordinator: " + getCoordinator());
 			getCoordinator().eventCoordinatorRoleInvalid();
 		}else{
			Logging.warn(this, "eventClusterInvalid() can't find the coordinator");
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
 		
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCluster(this);
 	}
 
 	/**
 	 * EVENT: detected additional cluster member, the event is triggered by the comm. channel
 	 * 
 	 * @param pComChannel the comm. channel of the new cluster member
 	 */
 	public void eventClusterMemberJoined(ComChannel pComChannel)
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
 		if (getCoordinator() != null){
 			getCoordinator().eventClusterMemberNeedsHRMID(pComChannel);
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
 		ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.OUT, this, pComSession, pPeer); //TODO: statt sofort den kanal aufzubauen lieber auf das ACK warten und dann aufbauen, andernfalls koennen karteileichen entstehen wenn der remote bereits den zielkoordinator nicht mehr kennt und nie ein ACK schickt
 		tComChannel.setRemoteClusterName(pLocalEndpointName);
 		tComChannel.setPeerPriority(pPeer.getPriority());
 		
 		/**
 		 * Send "RequestClusterMembership" along the comm. session
 		 * HINT: we cannot use the created channel because the remote side doesn't know anything about the new comm. channel yet)
 		 */
 		RequestClusterMembership tRequestClusterMembership = new RequestClusterMembership(mHRMController.getNodeName(), mHRMController.getNodeName(), createClusterName(), pRemoteEndPointName);
 		Logging.log(this, "       ..sending membership request: " + tRequestClusterMembership);
 		if (pComSession.write(tRequestClusterMembership)){
 			Logging.log(this, "      ..requested sucessfully for membership of: " + pPeer);
 		}else{
 			Logging.err(this, "      ..failed to request for membership of: " + pPeer);
 		}
 	}
 	
 	/**
 	 * Distributes cluster membership requests
 	 * HINT: This function has to be called in a separate thread!
 	 * 
 	 */
 	private int mCountDistributeMembershipRequests = 0;
 	public synchronized void distributeMembershipRequests()
 	{
 		mCountDistributeMembershipRequests ++;
 		
 		/*************************************
 		 * Request for local coordinators
 		 ************************************/
 		if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 			Logging.log(this, "\n\n\n################ REQUESTING MEMBERSHIP FOR LOCAL COORDINATORS STARTED (call nr: " + mCountDistributeMembershipRequests + ")");
 		}
 
 		if(mClusterRoleValid){
 			LinkedList<Coordinator> tCoordinators = mHRMController.getAllCoordinators(getHierarchyLevel().getValue() - 1);
 			
 			/**
 			 * Iterate over all found local coordinators
 			 */
 			synchronized (mInferiorLocalCoordinators) {
 				if(mCountDistributeMembershipRequests > 1){
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "      ..having connections to these inferior local coordinators: " + mInferiorLocalCoordinators.toString());
 					}
 				}
 				for (Coordinator tCoordinator : tCoordinators){
 					if (!mInferiorLocalCoordinators.contains(tCoordinator)){
 						if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 							Logging.log(this, "      ..found inferior local coordinator [NEW]: " + tCoordinator);
 						}
 						
 						// add this local coordinator to the list of connected coordinators
 						mInferiorLocalCoordinators.add(tCoordinator);
 	
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
 				if(mInferiorLocalCoordinators.size() > 0){
 					if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 						Logging.log(this, "\n\n\n################ REQUESTING MEMBERSHIP FOR REMOTE COORDINATORS STARTED");
 					}
 					LinkedList<CoordinatorProxy> tCoordinatorProxies = mHRMController.getAllCoordinatorProxies(getHierarchyLevel().getValue() - 1);
 					
 					if(tCoordinatorProxies.size() > 0){
 						/**
 						 * Iterate over all found remote coordinators
 						 */
 						synchronized (mInferiorRemoteCoordinators) {
 							if(mCountDistributeMembershipRequests > 1){
 								if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 									Logging.log(this, "      ..having connections to these inferior remote coordinators: " + mInferiorRemoteCoordinators.toString());
 								}
 							}
 							for (CoordinatorProxy tCoordinatorProxy : tCoordinatorProxies){
 								if (!mInferiorRemoteCoordinators.contains(tCoordinatorProxy)){
 									if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 										Logging.log(this, "      ..found remote inferior coordinator[NEW]: " + tCoordinatorProxy);
 									}
 									
 									// add this remote coordinator to the list of connected coordinators
 									mInferiorRemoteCoordinators.add(tCoordinatorProxy);
 									
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
 		Logging.log(this, "EVENT: detected local isolation");
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
