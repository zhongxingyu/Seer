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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.ISignalingMessageHrmBroadcastable;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a cluster member (can also be a cluster head).
  */
 public class ClusterMember extends ClusterName
 {
 	private static final long serialVersionUID = -8746079632866375924L;
 
 	/**
 	 * Stores the L2 address of the node where the coordinator of the addressed cluster is located
 	 */
 	private L2Address mCoordinatorNodeL2Address = null;
 	
 	/**
 	 * Stores the elector which is responsible for coordinator elections for this cluster.
 	 */
 	protected Elector mElector = null;
 
 	/**
 	 * Returns the cluster activation
 	 */
 	private boolean mClusterActivation = false;
 	
 	/**
 	 * Constructor
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pCoordinatorID the unique coordinator ID for this cluster
 	 * @param pCoordinatorNodeL2Address the L2 address of the node where the coordinator of this cluster is located
 	 */
 	public ClusterMember(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID, int pCoordinatorID, L2Address pCoordinatorNodeL2Address)
 	{	
 		super(pHRMController, pHierarchyLevel, pClusterID, pCoordinatorID);
 
 		// store the L2 address of the node where the coordinator is located
 		mCoordinatorNodeL2Address = pCoordinatorNodeL2Address;
 	}
 
 	/**
 	 * Factory function
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pClusterName a ClusterName which includes the hierarchy level, the unique ID of this cluster, and the unique coordinator ID
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pClusterHeadNodeL2Address the L2 address of the node where the cluster head is located
 	 */
 	public static ClusterMember create(HRMController pHRMController, ClusterName pClusterName, L2Address pClusterHeadNodeL2Address)
 	{	
 		ClusterMember tResult = new ClusterMember(pHRMController, pClusterName.getHierarchyLevel(), pClusterName.getClusterID(), pClusterName.getCoordinatorID(), pClusterHeadNodeL2Address);
 		
 		Logging.log(tResult, "\n\n\n################ CREATED CLUSTER MEMBER at hierarchy level: " + (tResult.getHierarchyLevel().getValue()));
 
 		// register at HRMController's internal database
 		pHRMController.registerClusterMember(tResult);
 
 		// creates new elector object, which is responsible for Bully based election processes
 		tResult.mElector = new Elector(pHRMController, tResult);
 		
 		return tResult;
 	}
 
 	/**
 	 * EVENT: coordinator announcement, we react on this by:
 	 *       1.) store the topology information locally
 	 *       2.) forward the announcement within the same hierarchy level ("to the side")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pAnnounceCoordinator the received announcement
 	 */
 	@Override
 	public void eventCoordinatorAnnouncement(ComChannel pComChannel, AnnounceCoordinator pAnnounceCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "EVENT: coordinator announcement (from side): " + pAnnounceCoordinator);
 		}
 		
 		/**
 		 * Storing that the announced coordinator is a superior one of this node
 		 */
 		// is the packet still on its way from the top to the bottom AND does it not belong to an L0 coordinator?
 		if((!pAnnounceCoordinator.enteredSidewardForwarding()) && (!pAnnounceCoordinator.getSenderClusterName().getHierarchyLevel().isBaseLevel())){
 			mHRMController.registerSuperiorCoordinator(pAnnounceCoordinator.getSenderClusterName());
 		}
 
 		/**
 		 * Check if we should forward this announcement "to the side"
 		 */
 		// is this the 2+ passed ClusterMember OR (in case it is the first passed ClusterMember) the peer is the origin of the announce -> forward the announcement 
 		Route tRoute = pAnnounceCoordinator.getRoute();
 		if(((tRoute != null) && (!tRoute.isEmpty()) && (tRoute.getFirst() != null)) || (pAnnounceCoordinator.getSenderClusterCoordinatorNodeL2Address().equals(pComChannel.getPeerL2Address()))){
 			/**
 			 * Record the passed clusters
 			 */
 			pAnnounceCoordinator.addGUIPassedCluster(new Long(getGUIClusterID()));
 	
 			/**
 			 * Enlarge the stored route towards the announcer
 			 */
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.log(this, "      ..adding route: " + pComChannel.getRouteToPeer());
 			}
 			pAnnounceCoordinator.addRouteHop(pComChannel.getRouteToPeer());
 			
 			/**
 			 * Store the announced remote coordinator in the ARG 
 			 */
 			registerAnnouncedCoordinatorARG(this, pAnnounceCoordinator);
 			
 			/**
 			 * transition from one cluster to the next one => decrease TTL value
 			 */
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.log(this, "Deacreasing TTL of: " + pAnnounceCoordinator);
 			}
 			pAnnounceCoordinator.decreaseTTL(); //TODO: decreasen in abhaengigkeit der hier. ebene -> dafuer muss jeder L0 cluster wissen welche hoeheren cluster darueber liegen
 		
 			/**
 			 * forward the announcement if the TTL is still okay
 			 */
 			if(pAnnounceCoordinator.isTTLOkay()){
 				// do we have a loop?
 				if(!pAnnounceCoordinator.hasPassedNode(mHRMController.getNodeL2Address())){
 					/**
 					 * Record the passed nodes
 					 */
 					pAnnounceCoordinator.addPassedNode(mHRMController.getNodeL2Address());
 					
 					/**
 					 * Check if this announcement is already on its way sidewards
 					 */
 					if(!pAnnounceCoordinator.enteredSidewardForwarding()){
 						// are we a cluster member of a cluster, which is located on the same node from where this announcement comes from? -> forward the packet to the side
 						if (pComChannel.getPeerL2Address().equals(pAnnounceCoordinator.getSenderClusterCoordinatorNodeL2Address())){
 							/**
 							 * mark packet as "sideward forwarded"
 							 */
 							pAnnounceCoordinator.setSidewardForwarding();
 						}else{
 							// we are a cluster member of any cluster located at a node where this announcement was received from a superior coordinator
 							
 							/**
 							 * drop the packet and return immediately
 							 */ 
 							return;
 						}
 					}
 		
 					/**
 					 * Forward the announcement within the same hierarchy level ("to the side")
 					 */
 					// get locally known neighbors for this cluster and hierarchy level
 					LinkedList<Cluster> tLocalClusters = mHRMController.getAllClusters(getHierarchyLevel());
 					if(tLocalClusters.size() > 0){
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "     ..found " + tLocalClusters.size() + " neighbor clusters");
 						}
 			
 						for(Cluster tLocalCluster: tLocalClusters){
 							/**
 							 * Forward the announcement
 							 * HINT: we avoid loops by excluding the sender from the forwarding process
 							 */
 							if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 								Logging.log(this, "     ..fowarding this event to locally known neighbor cluster: " + tLocalCluster);
 							}
 							
 							// forward this announcement to all cluster members
 							tLocalCluster.sendClusterBroadcast(pAnnounceCoordinator, true, pComChannel.getPeerL2Address() /* exclude this from the forwarding process */);
 						}
 					}else{
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "No neighbors found, ending forwarding of: " + pAnnounceCoordinator);
 						}
 					}
 				}else{
 					//if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 						Logging.log(this, "eventCoordinatorAnnouncement() found a forwarding loop for: " + pAnnounceCoordinator + "\n   ..passed clusters: " + pAnnounceCoordinator.getGUIPassedClusters()+ "\n   ..passed nodes: " + pAnnounceCoordinator.getPassedNodes());
 					//}
 				}
 			}else{
 				if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 					Logging.log(this, "TTL exceeded for coordinator announcement: " + pAnnounceCoordinator);
 				}
 			}
 		}
 	}
 
 	/**
 	 * EVENT: coordinator announcement, we react on this by:
 	 *       1.) store the topology information locally
 	 *       2.) forward the announcement within the same hierarchy level ("to the side")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pAnnounceCoordinator the received announcement
 	 */
 	@Override
 	public void eventCoordinatorInvalidation(ComChannel pComChannel, InvalidCoordinator pInvalidCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "EVENT: coordinator invalidation (from side): " + pInvalidCoordinator);
 		}
 		
 		/**
 		 * Store the announced remote coordinator in the ARG 
 		 */
 		unregisterAnnouncedCoordinatorARG(this, pInvalidCoordinator);
 		
 		/**
 		 * transition from one cluster to the next one => decrease TTL value
 		 */
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "Deacreasing TTL of: " + pInvalidCoordinator);
 		}
 		pInvalidCoordinator.decreaseTTL(); //TODO: decreasen in abhaengigkeit der hier. ebene -> dafuer muss jeder L0 cluster wissen welche hoeheren cluster darueber liegen
 	
 		/**
 		 * forward the announcement if the TTL is still okay
 		 */
 		if(pInvalidCoordinator.isTTLOkay()){
 			// do we have a loop?
 			if(!pInvalidCoordinator.hasPassedNode(mHRMController.getNodeL2Address())){
 				/**
 				 * Record the passed nodes
 				 */
 				pInvalidCoordinator.addPassedNode(mHRMController.getNodeL2Address());
 
 				/**
 				 * Check if this announcement is already on its way sidewards
 				 */
 				if(!pInvalidCoordinator.enteredSidewardForwarding()){
 					// are we a cluster member of a cluster, which is located on the same node from where this announcement comes from? -> forward the packet to the side
 					if (pComChannel.getPeerL2Address().equals(pInvalidCoordinator.getSenderClusterCoordinatorNodeL2Address())){
 						/**
 						 * mark packet as "sideward forwarded"
 						 */
 						pInvalidCoordinator.setSidewardForwarding();
 					}else{
 						// we are a cluster member of any cluster located at a node where this announcement was received from a superior coordinator
 						
 						/**
 						 * drop the packet and return immediately
 						 */ 
 						return;
 					}
 				}
 	
 				/**
 				 * Forward the announcement within the same hierarchy level ("to the side")
 				 */
 				// get locally known neighbors for this cluster and hierarchy level
 				LinkedList<Cluster> tLocalClusters = mHRMController.getAllClusters(getHierarchyLevel());
 				if(tLocalClusters.size() > 0){
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 						Logging.log(this, "     ..found " + tLocalClusters.size() + " neighbor clusters");
 					}
 		
 					for(Cluster tLocalCluster: tLocalClusters){
 						/**
 						 * Forward the announcement
 						 * HINT: we avoid loops by excluding the sender from the forwarding process
 						 */
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 							Logging.log(this, "     ..fowarding this event to locally known neighbor cluster: " + tLocalCluster);
 						}
 						
 						// forward this announcement to all cluster members
 						tLocalCluster.sendClusterBroadcast(pInvalidCoordinator, true, pComChannel.getPeerL2Address() /* exclude this from the forwarding process */);
 					}
 				}else{
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 						Logging.log(this, "No neighbors found, ending forwarding of: " + pInvalidCoordinator);
 					}
 				}
 			}else{
 				//if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 					Logging.log(this, "eventCoordinatorAnnouncement() found a forwarding loop for: " + pInvalidCoordinator + "\n   ..passed nodes: " + pInvalidCoordinator.getPassedNodes());
 				//}
 			}
 		}else{
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 				Logging.log(this, "TTL exceeded for coordinator invalidation: " + pInvalidCoordinator);
 			}
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
 		
 		/**
 		 * Trigger: established for the comm. channel
 		 */
 		pComChannel.eventEstablished();
 
 		/**
 		 * Trigger: start coordinator election
 		 */
 		boolean tStartBaseLevel =  ((getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.START_AUTOMATICALLY_BASE_LEVEL));
 		// start coordinator election for the created HRM instance if the configuration allows this
 		if(((!getHierarchyLevel().isBaseLevel()) && (HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY)) || (tStartBaseLevel)){
 			Logging.log(this, "      ..starting ELECTION");
 			mElector.startElection();
 		}
 	}
 
 	/**
 	 * EVENT: cluster membership request, a cluster requests of a coordinator to acknowledge cluster membership, triggered by the comm. session
 	 * 
 	 * @param pRemoteClusterName the description of the possible new cluster member
 	 * @param pSourceComSession the comm. session where the packet was received
 	 */
 	public void eventClusterMembershipRequest(ClusterName pRemoteClusterName, ComSession pSourceComSession)
 	{
 		Logging.log(this, "EVENT: got cluster membership request from: " + pRemoteClusterName);
 		
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
 		 * Trigger: comm. channel established 
 		 */
 		eventComChannelEstablished(tComChannel);
 		
 		/**
 		 * SEND: acknowledgment -> will be answered by a BullyPriorityUpdate
 		 */
 		tComChannel.signalRequestClusterMembershipAck(null);
 
 		/**
 		 * Trigger: joined a remote cluster (sends a Bully priority update)
 		 */
 		eventJoinedRemoteCluster(tComChannel);
 	}
 
 	/**
 	 * EVENT: we have joined the superior cluster, triggered by ourself or the CoordinatorAsClusterMemeber if a request for cluster membership was ack'ed
 	 * 
 	 * @param pComChannelToRemoteCluster the comm. channel to the cluster
 	 */
 	protected void eventJoinedRemoteCluster(ComChannel pComChannelToRemoteCluster)
 	{
 		Logging.log(this, "HAVE JOINED remote cluster");
 		
 		/**
 		 * Trigger: joined remote cluster (in Elector)
 		 */
 		mElector.eventJoinedRemoteCluster(pComChannelToRemoteCluster);
 	}
 
 	/**
 	 * Sends a packet to the coordinator
 	 * 
 	 * @param pPacket the packet
 	 */
 	public void sendCoordinator(SignalingMessageHrm pPacket)
 	{
 		Logging.log(this, "Sending to superior coordinator: " + pPacket);
 		
 		if(superiorCoordinatorComChannel() != null){
 			superiorCoordinatorComChannel().sendPacket(pPacket);
 		}else{
 			Logging.warn(this, "Channel to superior coordinator is invalid");
 		}
 	}
 
 	/**
 	 * Sends a packet as broadcast to all cluster members
 	 * 
 	 * @param pPacket the packet which has to be broadcasted
 	 * @param pIncludeLoopback should loopback communication be included?
 	 * @param pExcludeL2Address describe a node which shouldn't receive this broadcast if we are at base hierarchy level
 	 */
 	protected void sendClusterBroadcast(ISignalingMessageHrmBroadcastable pPacket, boolean pIncludeLoopback, L2Address pExcludeL2Address)
 	{
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 
 		// get the L2Addres of the local host
 		L2Address tLocalL2Address = mHRMController.getHRS().getCentralFNL2Address();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 			Logging.log(this, "Sending BROADCASTS from " + tLocalL2Address + " the packet " + pPacket + " to " + tComChannels.size() + " communication channels, local base prio: " + mHRMController.getHierarchyNodePriority());
 		}
 		
 		for(ComChannel tComChannel : tComChannels) {
 			boolean tIsLoopback = tComChannel.toLocalNode();
 			
			if((pExcludeL2Address == null /* excluded peer address is null, we send everywhere */) || (!pExcludeL2Address.equals(tComChannel.getPeerL2Address()) /* should the peer be excluded? */) || (pIncludeLoopback)){
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 					if (!tIsLoopback){
 						Logging.log(this, "       ..to " + tComChannel + ", excluded: " + pExcludeL2Address);
 					}else{
 						Logging.log(this, "       ..to LOOPBACK " + tComChannel);
 					}
 				}
 	
 				if ((pIncludeLoopback) || (!tIsLoopback)){
 					if(tComChannel.isOpen()){
 						SignalingMessageHrm tNewPacket = pPacket.duplicate();
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 							Logging.log(this, "           ..sending duplicate packet: " + tNewPacket);
 						}
 						// send the packet to one of the possible cluster members
 						tComChannel.sendPacket(tNewPacket);
 					}else{
 						if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 							Logging.log(this, "             ..sending skipped because we are still waiting for establishment of channel: " + tComChannel);
 						}
 					}
 				}else{
 					if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 						Logging.log(this, "              ..skipping " + (tIsLoopback ? "LOOPBACK CHANNEL" : ""));
 					}
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
 					Logging.log(this, "              ..skipping EXCLUDED DESTINATION: " + pExcludeL2Address);
 				}
 			}
 		}
 	}
 	public void sendClusterBroadcast(ISignalingMessageHrmBroadcastable pPacket, boolean pIncludeLoopback)
 	{
 		sendClusterBroadcast(pPacket, pIncludeLoopback, null);
 	}
 
 	/**
 	 * Sets a new link state for all comm. channels
 	 * 
 	 * @param pState the new state
 	 * @param pCause the cause for this change
 	 */
 	public void setLAllinksActivation(boolean pState, String pCause)
 	{
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 		
 		Logging.log(this, "Setting new link state (" + pState + ") to all " + tComChannels.size() + " comm. channels");
 
 		for(ComChannel tComChannel : tComChannels) {
 			tComChannel.setLinkActivation(pState, pCause);
 		}
 	}
 
 	/**
 	 * Returns all active links
 	 * 
 	 * @return the active links
 	 */
 	public LinkedList<ComChannel> getActiveLinks()
 	{
 		LinkedList<ComChannel> tResult = new LinkedList<ComChannel>();
 		
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 		
 		for(ComChannel tComChannel : tComChannels) {
 			if(tComChannel.getLinkActivation()){
 				tResult.add(tComChannel);
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines the coordinator of this cluster. It is "null" if the election was lost or hasn't finished yet. 
 	 * 
 	 * @return the cluster's coordinator
 	 */
 	public Coordinator getCoordinator()
 	{
 		Logging.err(this, "!!!!! >>> ClusterMember::getCoordinator() should never be called, otherwise, an error in higher clustering code exists <<< !!!!!");
 		return null;
 	}
 
 	/**
 	 * Returns how many connected cluster members are known
 	 * 
 	 * @return the count
 	 */
 	public int countConnectedClusterMembers()
 	{
 		int tResult = 0;
 
 		// count all communication channels
 		tResult = getComChannels().size();
 
 		return tResult;
 	}
 
 	/**
 	 * Returns how many connected external cluster members are known
 	 * 
 	 * @return the count
 	 */
 	public int countConnectedRemoteClusterMembers()
 	{
 		int tResult = 0;
 
 		// get all communication channels
 		LinkedList<ComChannel> tComChannels = getComChannels();
 
 		for(ComChannel tComChannel : tComChannels) {
 			// filter loopback channels
 			if (tComChannel.toRemoteNode()){
 				tResult++;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns if this ClusterMember belongs to a remote cluster
 	 * 
 	 * @return true or false
 	 */
 	public boolean isRemoteCluster()
 	{
 		if(this instanceof Cluster){
 			return false;
 		}
 		
 		return (countConnectedRemoteClusterMembers() > 0);
 	}
 	
 	/**
 	 * EVENT: new connectivity node priority
 	 * 
 	 * @param pNewConnectivityNodePriority the new connectivity node priority
 	 */
 	public void eventConnectivityNodePriorityUpdate(long pNewConnectivityNodePriority)
 	{
 		Logging.log(this, "EVENT: base node priority update to:  " + pNewConnectivityNodePriority);
 		
 		if(getHierarchyLevel().isBaseLevel()){
 			/**
 			 * Set the new priority if it differs from the old one
 			 */
 			if((getPriority() != null) && (getPriority().getValue() != pNewConnectivityNodePriority)){
 				Logging.log(this, "Got new connectivity node priority, updating own priority from " + getPriority().getValue() + " to " + pNewConnectivityNodePriority);
 				setPriority(BullyPriority.create(this, pNewConnectivityNodePriority));
 			}
 		}else{
 			throw new RuntimeException("Got a call to ClusterMemeber::eventConnectivityNodePriorityUpdate at higher hierarchy level " + getHierarchyLevel().getValue());
 		}
 	}
 
 	/**
 	 * EVENT: cluster membership canceled (by cluster head)
 	 * 
 	 *  @param: pComChannel the comm. channel from where the cancellation was received
 	 */
 	public synchronized void eventClusterMembershipCanceled(ComChannel pComChannel)
 	{
 		Logging.log(this, "EVENT: cluster membership canceled: " + pComChannel);
 		
 		/**
 		 * Unregister from the HRMController's internal database
 		 */ 
 		mHRMController.unregisterClusterMember(this);
 	}
 
 	/**
 	 * Sets a new Bully priority
 	 * 
 	 * @param pPriority the new Bully priority
 	 */
 	@Override
 	public void setPriority(BullyPriority pPriority)
 	{
 		BullyPriority tOldPriority = getPriority();
 		
 		if((pPriority != null) && (!pPriority.isUndefined())){
 			/**
 			 * Set the new priority
 			 */
 			super.setPriority(pPriority);
 	
 			/**
 			 * Send priority update if necessary 
 			 */
 			if ((tOldPriority != null) && (!tOldPriority.isUndefined()) && (!tOldPriority.equals(pPriority))){
 				mElector.updatePriority();
 			}else{
 				Logging.log(this, "First priority was set: " + pPriority.getValue());
 			}
 		}else{
 			Logging.err(this, "REQUEST FOR SETTING UNDEFINED PRIORITY");
 		}
 	}
 
 	/**
 	 * Returns a hash code for this object.
 	 * This function is used within the ARG for identifying objects.
 	 * 
 	 * @return the hash code
 	 */
 	@Override
 	public int hashCode()
 	{
 		return getClusterID().intValue();
 	}
 
 	/**
 	 * Returns the elector of this cluster
 	 * 
 	 * @return the elector
 	 */
 	public Elector getElector()
 	{
 		return mElector;
 	}
 
 	/**
 	 * Returns the L2 address of the node where the coordinator of the described cluster is located
 	 * 
 	 * @return the L2 address
 	 */
 	public Name getCoordinatorNodeL2Address()
 	{
 		return mCoordinatorNodeL2Address;
 	}
 	
 	/**
 	 * Sets the cluster activation
 	 * 
 	 * @param pState the new state
 	 */
 	public void setClusterActivation(boolean pState)
 	{
 		if(mClusterActivation != pState){
 			Logging.log(this, "Setting cluster activation to: " + pState);
 			
 			mClusterActivation = pState;
 		}
 	}
 	
 	/**
 	 * Returns the cluster activation
 	 *  
 	 * @return true or false
 	 */
 	public boolean getClusterActivation()
 	{
 		return mClusterActivation;
 	}
 
 	/**
 	 * Defines the decoration text for the ARG viewer
 	 * 
 	 * @return text for the control entity or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return "RemoteCluster" + getGUIClusterID() + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue() + "(" + idToString() + ")";
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
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUIClusterID() + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns a string including the ClusterID, the token, and the node priority
 	 * 
 	 * @return the complex string
 	 */
 	private String idToString()
 	{
 		if ((getHRMID() == null) || (getHRMID().isRelativeAddress())){
 			return (getGUICoordinatorID() > 0 ? "Coordinator" + getGUICoordinatorID() : "") + (getCoordinatorNodeL2Address() != null ? ", CoordNode.=" + getCoordinatorNodeL2Address() : "");
 		}else{
 			return (getGUICoordinatorID() > 0 ? "Coordinator" + getGUICoordinatorID() : "") + (getCoordinatorNodeL2Address() != null ? ", CoordNode.=" + getCoordinatorNodeL2Address() : "") + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
