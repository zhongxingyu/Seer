 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.coordination;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.election.*;
 import de.tuilmenau.ics.fog.packets.hierarchical.ClusterDiscovery.NestedDiscovery;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.RequestCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.RouteRequest;
 import de.tuilmenau.ics.fog.packets.hierarchical.RouteRequest.ResultType;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData.FIBEntry;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceMultiplexer;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HierarchicalRoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterDummy;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.IRoutableClusterGraphNode;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NeighborCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.RoutableClusterGraphLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionProcess.ElectionManager;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMIPMapper;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 
 public class CoordinatorCEPDemultiplexed implements IRoutableClusterGraphNode
 {
 	private static final long serialVersionUID = -8290946480171751216L;
 	private ICluster mRemoteCluster;
 	private ICluster mPeerCluster;
 	private long mPeerPriority = -1;
 	private boolean mReceivedBorderNodeAnnouncement = false;
 	private boolean mPeerIsCoordinatorForNeighborZone = false;
 	private boolean mIsEdgeRouter = false;
 	private boolean mKnowsCoordinator = false;
 	private HashMap<ICluster, ICluster> mAnnouncerMapping;
 //	private boolean mRequestedCoordinator = false;
 	private boolean mPartOfCluster = false;
 	private HRMController mHRMController = null;
 	private Logger mLogger = Logging.getInstance();
 	private BFSDistanceLabeler<IRoutableClusterGraphNode, RoutableClusterGraphLink> mBreadthFirstSearch;
 	private boolean mCrossLevelCEP = false;
 	
 	/**
 	 * 
 	 * @param pLogger Logger that should be used
 	 * @param pHRMController is the coordinator of a node
 	 * @param pPeerCluster is the cluster this connection end point serves
 	 */
 	public CoordinatorCEPDemultiplexed(Logger pLogger, HRMController pHRMController, ICluster pPeerCluster)
 	{
 		mHRMController = pHRMController;
 		mPeerCluster = pPeerCluster;
 		mLogger = pLogger;
 		mLogger.log(this, "Created");
 		getHRMController().getLogger().log(this, "Created for " + mPeerCluster);
 	}
 	
 	/**
 	 * Handles a Bully signaling packet.
 	 * 
 	 * @param pBullyPacket the Bully signaling packet
 	 */
 	private void handleSignalingMessageBully(SignalingMessageBully pPacketBully) throws NetworkException
 	{
 		Node tNode = getHRMController().getPhysicalNode();
 		boolean BULLY_SIGNALING_DEBUGGING = true;
 
 		/**
 		 * BullyElect
 		 */
 		if(pPacketBully instanceof BullyElect)	{
 			
 			// cast to Bully elect packet
 			BullyElect tPacketBullyElect = (BullyElect)pPacketBully;
 			
 			if (BULLY_SIGNALING_DEBUGGING)
 				mLogger.log("Node " + tNode + ": BULLY-received from \"" + mPeerCluster + "\" an ELECT: " + tPacketBullyElect);
 
 			if ((getCluster().getCoordinatorCEP() != null) && (tPacketBullyElect.getSenderPriority().getValue() < getCluster().getHighestPriority())) {
 				
 				mPeerPriority = tPacketBullyElect.getSenderPriority().getValue();
 				
 				if (getCluster().getHRMController().equals(tNode.getCentralFN().getName())) {
 					// create ANNOUNCE packet
 					BullyAnnounce tAnnouncePacket = new BullyAnnounce(tNode.getCentralFN().getName(), new BullyPriority(getCluster().getBullyPriority()), getHRMController().getIdentity().createSignature(tNode.toString(), null, getCluster().getHierarchyLevel()), getCluster().getToken());
 					
 					for(CoordinatorCEPDemultiplexed tCEP : getCluster().getParticipatingCEPs()) {
 						tAnnouncePacket.addCoveredNode(tCEP.getPeerName());
 					}
 					if(tAnnouncePacket.getCoveredNodes() == null || (tAnnouncePacket.getCoveredNodes() != null && tAnnouncePacket.getCoveredNodes().isEmpty())) {
 						mLogger.log(this, "Sending announce that does not cover anyhting");
 					}
 
 					// send packet
 					if (BULLY_SIGNALING_DEBUGGING)
 						mLogger.log("Node " + tNode + ": BULLY-sending to \"" + mPeerCluster + "\" an ANNOUNCE: " + tAnnouncePacket);
 					sendPacket(tAnnouncePacket);
 					
 				} else {
 					// create ALIVE packet
 					BullyAlive tAlivePacket = new BullyAlive(tNode.getCentralFN().getName());
 					
 					// send packet
 					if (BULLY_SIGNALING_DEBUGGING)
 						mLogger.log("Node " + tNode + ": BULLY-sending to \"" + mPeerCluster + "\" an ALIVE: " + tAlivePacket);
 					sendPacket(tAlivePacket);
 					//TODO: packet is sent but never parsed or a timeout timer reset!!
 				}
 			} else {
 				// store peer's Bully priority
 				//TODO: peer prio direkt mal abspeichern und auf grte checken!
 				mPeerPriority = tPacketBullyElect.getSenderPriority().getValue();
 				
 				// create REPLY packet
 				BullyReply tReplyPacket = new BullyReply(tNode.getCentralFN().getName(), new BullyPriority(getCluster().getBullyPriority()));
 				
 				// send the answer packet
 				if (BULLY_SIGNALING_DEBUGGING)
 					mLogger.log("Node " + tNode + ": BULLY-sending to \"" + mPeerCluster + "\" a REPLY: " + tReplyPacket);
 				sendPacket(tReplyPacket);
 			}
 		}
 		
 		/**
 		 * BullyReply
 		 */
 		if(pPacketBully instanceof BullyReply) {
 			
 			// cast to Bully replay packet
 			BullyReply tReplyPacket = (BullyReply)pPacketBully;
 
 			if (BULLY_SIGNALING_DEBUGGING)
 				mLogger.log("Node " + tNode + ": BULLY-received from \"" + mPeerCluster + "\" a REPLY: " + tReplyPacket);
 
 			// store peer's Bully priority
 			//TODO: peer prio direkt mal abspeichern und auf grte checken!
 			mPeerPriority = tReplyPacket.getSenderPriority().getValue();
 		}
 		
 		/**
 		 * BullyAnnounce
 		 */
 		if(pPacketBully instanceof BullyAnnounce)  {
 			// cast to Bully replay packet
 			BullyAnnounce tAnnouncePacket = (BullyAnnounce)pPacketBully;
 
 			if (BULLY_SIGNALING_DEBUGGING)
 				mLogger.log("Node " + tNode + ": BULLY-received from \"" + mPeerCluster + "\" an ANNOUNCE: " + tAnnouncePacket);
 
 			//TODO: only an intermediate cluster on level 0 is able to store an announcement and forward it once a coordinator is set
 			getCluster().handleBullyAnnounce(tAnnouncePacket, this);
 		}
 
 		/**
 		 * BullyPriorityUpdate
 		 */
 		if(pPacketBully instanceof BullyPriorityUpdate) {
 			// cast to Bully replay packet
 			BullyPriorityUpdate tPacketBullyPriorityUpdate = (BullyPriorityUpdate)pPacketBully;
 
 			if (BULLY_SIGNALING_DEBUGGING)
 				mLogger.log("Node " + tNode + ": BULLY-received from \"" + mPeerCluster + "\" a PRIORITY UPDATE: " + tPacketBullyPriorityUpdate);
 
 			// store peer's Bully priority
 			mPeerPriority = tPacketBullyPriorityUpdate.getSenderPriority().getValue();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pData is the data that should be sent to the receiver side of this connection end point
 	 * @return true if the packet left the central multiplexer and the forwarding node that is attached to a direct down gate
 	 * @throws NetworkException
 	 */
 	public boolean receive(Serializable pData) throws NetworkException
 	{
 		boolean CHANNEL_SIGNALING_DEBUGGING = true;
 
 		Node tNode = getHRMController().getPhysicalNode();
 		HierarchicalRoutingService tHRS = getHRMController().getHRS();
 		
 		/*
 		 * Invalid data
 		 */
 		if(pData == null) {
 			if (CHANNEL_SIGNALING_DEBUGGING)
 				mLogger.log("Node " + tNode + ": CHANNEL-received from \"" + mPeerCluster + "\" invalid data");
 
 			throw new NetworkException("Received invalid null pointer as data");
 		}
 
 		/*
 		 * main packet processing
 		 */
 		try {
 			
 			/**
 			 * Bully signaling message
 			 */
 			if (pData instanceof SignalingMessageBully) {
 				// cast to a Bully signaling message
 				SignalingMessageBully tBullyMessage = (SignalingMessageBully)pData;
 			
 				// process Bully message
 				handleSignalingMessageBully(tBullyMessage);
 				
 				return true;
 			}
 			
 			/**
 			 * NeighborClusterAnnounce
 			 */
 			if(pData instanceof NeighborClusterAnnounce) {
 				NeighborClusterAnnounce tAnnouncePacket = (NeighborClusterAnnounce)pData;
 
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					mLogger.log("Node " + tNode + ": CHANNEL-received from \"" + mPeerCluster + "\" a NEIGHBOR CLUSTER ANNOUNCE: " + tAnnouncePacket);
 
 				if(tAnnouncePacket.isInterASAnnouncement()) {
 					Logging.log(tNode.getAS().getName() + " received an announcement from " + tAnnouncePacket.getASIdentification());
 					if(tNode.getAS().getName().equals(tAnnouncePacket.getASIdentification())) {
 						if(!getSourceName().equals(getPeerName())) {
 							for(Route tPath : tHRS.getCoordinatorRoutingMap().getRoute((HRMName)getSourceName(), (HRMName)getPeerName())) {
 								tAnnouncePacket.addRoutingVector(new RoutingServiceLinkVector(tPath, tHRS.getCoordinatorRoutingMap().getSource(tPath), tHRS.getCoordinatorRoutingMap().getDest(tPath)));
 							}
 						}
 						for(CoordinatorCEPDemultiplexed tCEP : getCluster().getParticipatingCEPs()) {
 							boolean tWroteAnnouncement = false;
 							if(tCEP.isEdgeCEP()) {
 								
 								// send packet
 								tCEP.sendPacket(tAnnouncePacket);
 
 								tWroteAnnouncement = true;
 							}
 							Logging.log(this, "Testing " + tCEP + " whether it is an inter as link:" + tWroteAnnouncement);
 						}
 					} else {
 						if(getCluster() instanceof Cluster) {
 							if(!getSourceName().equals(getPeerName())) {
 								RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(getRouteToPeer(), (HRMName)getSourceName(), (HRMName)getPeerName());
 								tAnnouncePacket.addRoutingVector(tVector);
 							}
 							for(CoordinatorCEPDemultiplexed tCEP : getCluster().getParticipatingCEPs()) {
 								boolean tWroteAnnouncement = false;
 								if(tCEP.getRemoteCluster().getHierarchyLevel() -1 == tAnnouncePacket.getLevel()) {
 									
 									// send packet
 									tCEP.sendPacket(tAnnouncePacket);
 									
 									tWroteAnnouncement = true;
 								}
 								Logging.log(this, "Testing " + tCEP + " whether it leads to the clusters coordinator: " + tWroteAnnouncement);
 							}
 						} else if(getCluster() instanceof Coordinator) {
 							Logging.log(this, "Inter AS announcement " + tAnnouncePacket + " is handled by " + getCluster() + " whether it leads to the clusters coordinator");
 							((Coordinator)getCluster()).getManagedCluster().handleAnnouncement(tAnnouncePacket, this);
 						}
 					}
 				} else {
 					getCluster().handleAnnouncement(tAnnouncePacket, this);
 				}
 				Logging.log(this, "Received " + tAnnouncePacket + " from remote cluster " + mRemoteCluster);
 			}
 			
 			
 			/**
 			 * TopologyData
 			 */
 			if(pData instanceof TopologyData) {
 				TopologyData tTopologyPacket = (TopologyData)pData;
 				
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					mLogger.log("Node " + tNode + ":  CHANNEL-received from \"" + mPeerCluster + "\" TOPOLOGY DATA: " + tTopologyPacket);
 
 				getCluster().handleTopologyData(tTopologyPacket);
 			}/* else if (pData instanceof NestedDiscovery) {
 				NestedDiscovery tDiscovery = (NestedDiscovery) pData;
 				handleClusterDiscovery(tDiscovery);
 			}*/
 			
 			
 			/**
 			 * RouteRequest
 			 */
 			if(pData instanceof RouteRequest) {
 				RouteRequest tRouteRequestPacket = (RouteRequest) pData;
 				
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					mLogger.log("Node " + tNode + ":  CHANNEL-received from \"" + mPeerCluster + "\" ROUTE REQUEST: " + tRouteRequestPacket);
 
 				if(tRouteRequestPacket.getTarget() instanceof HRMID) {
 					HRMName tRequestAddress = tRouteRequestPacket.getSource();
 					HRMName tDestinationAddress = getSourceName();
 					if(!tRouteRequestPacket.isAnswer() && tHRS.getFIBEntry( (HRMID) tRouteRequestPacket.getTarget()) != null && tRequestAddress != null && tRequestAddress.equals(tDestinationAddress)) {
 						/*
 						 * Find out if route request can be solved by this entity without querying a higher coordinator
 						 */
 						for(IRoutableClusterGraphNode tCluster : getHRMController().getClusters(0)) {
 							FIBEntry tEntry = tHRS.getFIBEntry( (HRMID) tRouteRequestPacket.getTarget());
 							if(tCluster instanceof Cluster && tEntry != null && (tEntry.getFarthestClusterInDirection() == null || tEntry.getFarthestClusterInDirection().equals(tCluster))) {
 								Route tRoute = tHRS.getRoutePath( getSourceName(), tRouteRequestPacket.getTarget(), new Description(), tNode.getIdentity());
 								RouteSegmentPath tPath = (RouteSegmentPath) tRoute.getFirst();
 								HRMName tSource = null;
 								HRMName tTarget = null;
 								for(Route tCandidatePath : tHRS.getCoordinatorRoutingMap().getEdges()) {
 									if(tCandidatePath.equals(tPath)) {
 										 tSource = tHRS.getCoordinatorRoutingMap().getSource(tCandidatePath);
 										 tTarget = tHRS.getCoordinatorRoutingMap().getDest(tCandidatePath);
 										 break;
 									}
 								}
 								tRouteRequestPacket.addRoutingVector(new RoutingServiceLinkVector(tRoute, tSource, tTarget));
 								tRouteRequestPacket.setAnswer();
 								tRouteRequestPacket.setResult(ResultType.SUCCESS);
 
 								// send packet
 								sendPacket(tRouteRequestPacket);
 								
 								return true;
 							}
 						}
 					}
 					
 					if(!tRouteRequestPacket.isAnswer() && tRouteRequestPacket.isRouteAccumulation()) {
 						if(getRemoteCluster().getHierarchyLevel() != getCluster().getHierarchyLevel() && getCluster().isInterASCluster()) {
 							HRMID tAddress =  (HRMID) tRouteRequestPacket.getTarget();
 							LinkedList<Name> tIPAddresses = HRMIPMapper.getHRMIPMapper().getIPFromHRMID(tAddress);
 							Route tRoute = null;
 							if(tIPAddresses != null) {
 								for(Name tTargetAddress : tIPAddresses) {
 									try {
 										tRoute = ((RoutingServiceMultiplexer)tNode.getRoutingService()).getRoute(tNode.getCentralFN(), tTargetAddress, ((RouteRequest)pData).getDescription(), null);
 									} catch (NetworkException tExc) {
 										Logging.info(this, "BGP routing service did not find a route to " + tTargetAddress);
 									}
 									Logging.log(this, "Interop: Route to "+ tAddress + " with IP address " + tTargetAddress + " is " + tRoute);
 								}
 							} else {
 								getHRMController().getLogger().err(this, "Unable to distribute addresses because no IP address is available");
 							}
 							if(tRoute != null) {
 								tRouteRequestPacket.setAnswer();
 								tRouteRequestPacket.setRoute(tRoute);
 								
 								// send packet
 								sendPacket(tRouteRequestPacket);
 								
 							}
 						} 
 						return true;
 					} else if(tRouteRequestPacket.isAnswer()) {
 						/*
 						 * In this case normally someone is waiting for this packet to arrive, therefore is not handled by any cluster and you only get a notification.
 						 */
 						synchronized(tRouteRequestPacket) {
 							tRouteRequestPacket.notifyAll();
 						}
 						return true;
 					}
 					if(getCluster() instanceof Cluster) {
 						Coordinator tManager = ((Cluster)getCluster()).getClusterManager();
 						tManager.handleRouteRequest((RouteRequest) pData, getRemoteCluster());
 						tManager.registerRouteRequest(tRouteRequestPacket.getSession(), this);
 					} else if (getCluster() instanceof Coordinator) {
 						/*
 						 * Normally that case should not appear ...
 						 */
 						((Coordinator)getCluster()).handleRouteRequest((RouteRequest) pData, this);
 					}
 					/*
 					 * This comment relates to the following else if statement: use routing service address as last instance because it is the default and all
 					 * other addresses are derived from the HRMID
 					 */
 				}
 				
 				/**
 				 * HRMID
 				 */
 				if(tRouteRequestPacket.getTarget() instanceof HRMID && !tRouteRequestPacket.isAnswer()) {
 					List<Route> tFinalPath = tHRS.getCoordinatorRoutingMap().getRoute(tRouteRequestPacket.getSource(), tRouteRequestPacket.getTarget());
 					if(tRouteRequestPacket.getRequiredClusters() != null) {
 
 						for(ICluster tDummy : tRouteRequestPacket.getRequiredClusters()) {
 							tFinalPath = null;
 							List<Route> tPath = tHRS.getCoordinatorRoutingMap().getRoute(tRouteRequestPacket.getSource(), tRouteRequestPacket.getTarget());
 							
 							ICluster tCluster = getHRMController().getCluster(tDummy);
 							LinkedList<HRMName> tAddressesOfCluster = new LinkedList<HRMName>();
 							
 							if( tCluster instanceof Cluster ) {
 								for(CoordinatorCEPDemultiplexed tCEP : ((Cluster)tCluster).getParticipatingCEPs()) {
 									tAddressesOfCluster.add(tCEP.getPeerName());
 								}
 							}
 							if( tAddressesOfCluster.contains(tHRS.getCoordinatorRoutingMap().getDest(tPath.get(0))) ) {
 								tFinalPath = tPath;
 							} else {
 								for(HRMName tCandidate : tAddressesOfCluster) {
 									List<Route> tOldPath = tPath;
 									tPath = tHRS.getCoordinatorRoutingMap().getRoute(tCandidate, tRouteRequestPacket.getTarget());
 									
 									if(tPath.size() < tOldPath.size()) {
 										List<Route> tFirstPart = tHRS.getCoordinatorRoutingMap().getRoute(tRouteRequestPacket.getSource(), tCandidate); 
 										Route tSegment = (tFirstPart.size() > 0 ? tFirstPart.get(0) : null);
 										if(tSegment != null) {
 											tPath.add(0, tSegment);
 										}
 										tFinalPath = tPath;
 									}
 								}
 							}
 						}
 					}
 					if(tFinalPath != null && !tFinalPath.isEmpty()) {
 						for(Route tSegment : tFinalPath) {
 							tRouteRequestPacket.addRoutingVector(new RoutingServiceLinkVector(tSegment, tHRS.getCoordinatorRoutingMap().getSource(tSegment), tHRS.getCoordinatorRoutingMap().getDest(tSegment)));
 						}
 					}
 					tRouteRequestPacket.setAnswer();
 					
 					// send packet
 					sendPacket(tRouteRequestPacket);
 					
 				} else if(tRouteRequestPacket.getTarget() instanceof HRMID && tRouteRequestPacket.isAnswer()) {
 					synchronized (tRouteRequestPacket) {
 						tRouteRequestPacket.notifyAll();
 					}
 				}
 			}
 			
 			/**
 			 * RequestCoordinator
 			 */
 			if (pData instanceof RequestCoordinator) {
 				RequestCoordinator tRequestCoordinatorPacket = (RequestCoordinator) pData;
 				
 				if (CHANNEL_SIGNALING_DEBUGGING)
 					mLogger.log("Node " + tNode + ":  CHANNEL-received from \"" + mPeerCluster + "\" COORDINATOR REQUEST: " + tRequestCoordinatorPacket);
 
 				if(!tRequestCoordinatorPacket.isAnswer()) {
 					if(getCluster().getCoordinatorCEP() != null) {
 						ICluster tCluster = getCluster().getHRMController().getClusterWithCoordinatorOnLevel(getCluster().getHierarchyLevel());
 						Logging.log(this, "Name of coordinator is " + tCluster.getCoordinatorName());
 						
 						int tToken = tCluster.getToken();
 						Name tCoordinatorName = tCluster.getCoordinatorName();
 						long tCoordinatorAddress = tCluster.getCoordinatorsAddress().getAddress().longValue();
 						HRMName tL2Address = tCluster.getCoordinatorsAddress();
 						int tLevel = tCluster.getHierarchyLevel();
 						
 						DiscoveryEntry tEntry = new DiscoveryEntry(tToken, tCoordinatorName, tCoordinatorAddress, tL2Address, tLevel);
 						tEntry.setPriority(getCluster().getNodePriority());
 						tEntry.setRoutingVectors(getPath(tCluster.getCoordinatorsAddress()));
 						tRequestCoordinatorPacket.addDiscoveryEntry(tEntry);
 						tRequestCoordinatorPacket.setCoordinatorKnown(true);
 						tRequestCoordinatorPacket.setAnswer();
 					} else {
 						tRequestCoordinatorPacket.setCoordinatorKnown(false);
 						tRequestCoordinatorPacket.setAnswer();
 					}
 					
 					// send packet
 					sendPacket(tRequestCoordinatorPacket);
 					
 				} else {
 					if(tRequestCoordinatorPacket.isCoordinatorKnown()) {
 						mKnowsCoordinator = true;
 					} else {
 						mKnowsCoordinator = false;
 					}
 					if(tRequestCoordinatorPacket.getDiscoveryEntries() != null) {
 						for(DiscoveryEntry tEntry : tRequestCoordinatorPacket.getDiscoveryEntries()) {
 							ClusterDummy tDummy = handleDiscoveryEntry(tEntry);
 							getCluster().getHRMController().getCluster(ClusterDummy.compare((((HRMName)getSourceName()).getAddress().longValue()), getCluster().getToken(), getCluster().getHierarchyLevel())).addNeighborCluster(getCluster().getHRMController().getCluster(tDummy));
 							addAnnouncedCluster(getHRMController().getCluster(tDummy), getRemoteCluster());
 						}
 					}
 					synchronized(tRequestCoordinatorPacket) {
 						Logging.log(this, "Received answer to " + tRequestCoordinatorPacket + ", notifying");
 						tRequestCoordinatorPacket.mWasNotified = true;
 						tRequestCoordinatorPacket.notifyAll();
 					}
 				}
 			}
 		} catch (PropertyException tExc) {
 			Logging.err(this, "Unable to fulfill requirements", tExc);
 		}
 		return true;
 	}
 	
 //	/**
 //	 * @deprecated
 //	 * If this CEP is used for communication between different hierarchical levels: for use of HRM+BGP
 //	 */
 //	public void setCrossLayerCEP()
 //	{
 //		mCrossLevelCEP = true;
 //	}
 	
 	/**
 	 * 
 	 * @param pTarget is the target to which routing service link vectors should be generated
 	 * @return vectors that provide a patch between forwarding nodes along with the gate numbers
 	 */
 	public LinkedList<RoutingServiceLinkVector> getPath(HRMName pTarget)
 	{
 		LinkedList<RoutingServiceLinkVector> tVectors = new LinkedList<RoutingServiceLinkVector>();
 		RoutableGraph<HRMName, Route> tRoutingDatabase = getHRMController().getHRS().getCoordinatorRoutingMap();
 		List<Route> tRoute = tRoutingDatabase.getRoute(getMultiplexer().getSourceRoutingServiceAddress(this), pTarget);
 		HRMName tSource = getMultiplexer().getSourceRoutingServiceAddress(this);
 		HRMName tDestination;
 		if(tRoute == null) {
 			return null;
 		} else {
 			for(int i = 0 ; i < tRoute.size() ; i++) {
 				if(tRoute.get(i) instanceof Route) {
 					tDestination = tRoutingDatabase.getDest(tRoute.get(i));
 					RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(tRoute.get(i), tSource, tDestination);
 					tVectors.add(tVector);
 					tSource = tDestination;
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
 	 * @param pCluster as cluster (along with the coordinator) to which a path has to be filled into the discovery entry
 	 * @throws NetworkException 
 	 * @throws PropertyException in case the requirements to the target coordinator can not be fulfilled
 	 */
 	private void getPathTo(NestedDiscovery pDiscovery, ICluster pCluster) throws NetworkException, PropertyException
 	{
 		if(pCluster.getCoordinatorName() != null) {
 			DiscoveryEntry tEntry = new DiscoveryEntry(pCluster.getToken(), pCluster.getCoordinatorName(), pCluster.getClusterID(), pCluster.getCoordinatorsAddress(), pCluster.getHierarchyLevel());
 			tEntry.setClusterHops(getCluster().getHRMController().getClusterDistance(pCluster));
 			tEntry.setPriority(pCluster.getBullyPriority());
 			tEntry.setRoutingVectors(getPath(pCluster.getCoordinatorsAddress()));
 			if(pCluster.isInterASCluster()) {
 				tEntry.setInterASCluster();
 			}
 			
 			List<RoutableClusterGraphLink> tClusterList = getHRMController().getRoutableClusterGraph().getRoute(getCluster(), pCluster);
 			if(!tClusterList.isEmpty()) {
 				ICluster tPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(pCluster, tClusterList.get(tClusterList.size()-1));
 				tEntry.setPredecessor(ClusterDummy.compare(tPredecessor.getClusterID(), tPredecessor.getToken(), tPredecessor.getHierarchyLevel()));
 			}
 			
 			pDiscovery.addDiscoveryEntry(tEntry);
 		}
 	}
 	
 	/**
 	 * Connection end points control the clusters they are associated to.
 	 * 
 	 * @return As one node may be associated to more than one cluster you can use this method to find out
 	 * which cluster is controlled by this connection end point.
 	 */
 	public ICluster getCluster()
 	{
 		return mPeerCluster;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ICluster getRemoteCluster()
 	{
 		ICluster tCluster = null;
 		if(mRemoteCluster instanceof ClusterDummy) {
 			tCluster = getHRMController().getCluster(mRemoteCluster);
 		}
 		if(getCluster().getHierarchyLevel() == 0) {
 			return getCluster();
 		}
 		return (tCluster == null ? mRemoteCluster : tCluster);
 	}
 
 	public HRMController getHRMController()
 	{
 		return mHRMController;
 	}
 	
 	public void setAsParticipantOfMyCluster(boolean pPartOfMyCluster)
 	{
 		mPartOfCluster = pPartOfMyCluster;
 	}
 	
 	public boolean receivedBorderNodeAnnouncement()
 	{
 		return mReceivedBorderNodeAnnouncement;
 	}
 	
 	public ICluster getNegotiator(ICluster pCluster)
 	{
 		if(mAnnouncerMapping == null) {
 			mAnnouncerMapping = new HashMap<ICluster, ICluster>();
 		}
 		ICluster tCluster = mAnnouncerMapping.get(pCluster);
 		return tCluster;
 	}
 	
 	public boolean knowsCoordinator()
 	{
 		return mKnowsCoordinator;
 	}
 	
 	public void setPeerPriority(long pPeerPriority)
 	{
 		mPeerPriority = pPeerPriority;
 	}
 	
 	public void addAnnouncedCluster(ICluster pAnnounced, ICluster pNegotiate)
 	{
 		mLogger.log(this, "Cluster " + pAnnounced + " as announced by " + pNegotiate);
 		if(pNegotiate == null) {
 			return;
 		}
 		if(mAnnouncerMapping == null) {
 			mAnnouncerMapping = new HashMap<ICluster, ICluster>();
 		}
 		if(!mAnnouncerMapping.containsKey(pAnnounced)) {
 			mAnnouncerMapping.put(pAnnounced, pNegotiate);
 		} else {
 			getHRMController().getLogger().log(this, "comparing " + pNegotiate + " to " + mAnnouncerMapping.get(pAnnounced));
 			if(pNegotiate.getHierarchyLevel() < mAnnouncerMapping.get(pAnnounced).getHierarchyLevel()) {
 				mAnnouncerMapping.remove(pAnnounced);
 				mAnnouncerMapping.put(pAnnounced, pNegotiate);
 			} else if (pNegotiate instanceof NeighborCluster && mAnnouncerMapping.get(pAnnounced) instanceof NeighborCluster && ((NeighborCluster)pNegotiate).getClustersToTarget() < ((NeighborCluster)mAnnouncerMapping.get(pAnnounced)).getClustersToTarget()) {
 				getCluster().getHRMController().getLogger().log(this, "replacing negotiating cluster of " + pAnnounced + ": " + mAnnouncerMapping.get(pAnnounced) + " with " + pNegotiate);
 				mAnnouncerMapping.remove(pAnnounced);
 				mAnnouncerMapping.put(pAnnounced, pNegotiate);
 			}
 		}
 	}
 	
 	public long getPeerPriority()
 	{
 		return mPeerPriority;
 	}
 	
 	public void setRemoteCluster(ICluster pNegotiator)
 	{
 		if(pNegotiator == null) {
 			mLogger.warn(this, "Setting " + pNegotiator + " as negotiating cluster");
 		}
 		mRemoteCluster = pNegotiator;
 	}
 
 	public CoordinatorCEPMultiplexer getCEPMultiplexer()
 	{
 		return getMultiplexer();
 	}
 	
 	public boolean sendPacket(Serializable pData)
 	{
 		if(pData instanceof RequestCoordinator) {
 //			mRequestedCoordinator = true;
 			getHRMController().getLogger().log(this, "Sending " + pData);
 		}
 		if(getCluster() instanceof Coordinator && !mCrossLevelCEP) {
 			getCEPMultiplexer().write(pData, this, ClusterDummy.compare(((L2Address)getPeerName()).getAddress().longValue(), getCluster().getToken(), getCluster().getHierarchyLevel()));
 		} else {
 			getCEPMultiplexer().write(pData, this, getRemoteCluster());
 		}
 		return true;
 	}
 	
 	public HRMName getPeerName()
 	{
 		return getMultiplexer().getPeerRoutingServiceAddress(this);
 	}
 	
 	public HRMName getSourceName()
 	{
 		return getMultiplexer().getSourceRoutingServiceAddress(this);
 	}
 	
 	public boolean isPeerCoordinatorForNeighborZone()
 	{
 		return mPeerIsCoordinatorForNeighborZone;
 	}
 	
 	public boolean isPartOfMyCluster()
 	{
 		return mPartOfCluster;
 	}
 	
 	public boolean isEdgeCEP()
 	{
 		return mIsEdgeRouter;
 	}
 	
 	public void setEdgeCEP()
 	{
 		ElectionManager.getElectionManager().removeElection(getCluster().getHierarchyLevel(), getCluster().getClusterID());
 		mIsEdgeRouter = true;
 	}
 	
 	public void handleClusterDiscovery(NestedDiscovery pDiscovery, boolean pRequest) throws PropertyException, NetworkException
 	{
 		if(pRequest){
 			ICluster tSourceCluster=null;
 			tSourceCluster = getHRMController().getCluster(ClusterDummy.compare(pDiscovery.getSourceClusterID(), pDiscovery.getToken(), pDiscovery.getLevel()));
 			if(tSourceCluster == null) {
 				Logging.err(this, "Unable to find appropriate cluster for" + pDiscovery.getSourceClusterID() + " and token" + pDiscovery.getToken() + " on level " + pDiscovery.getLevel() + " remote cluster is " + getRemoteCluster());
 			}
 			if(mBreadthFirstSearch == null ) {
 				mBreadthFirstSearch = new BFSDistanceLabeler<IRoutableClusterGraphNode, RoutableClusterGraphLink>();
 			}
 			mBreadthFirstSearch.labelDistances(getHRMController().getRoutableClusterGraph().getGraphForGUI(), tSourceCluster);
 			List<IRoutableClusterGraphNode> tDiscoveryCandidates = mBreadthFirstSearch.getVerticesInOrderVisited();
 			if(tSourceCluster != null) {
 				for(IRoutableClusterGraphNode tVirtualNode : tDiscoveryCandidates) {
 					if(tVirtualNode instanceof ICluster) {
 						ICluster tCluster = (ICluster) tVirtualNode;
 						
 						int tRadius = HRMConfig.Routing.EXPANSION_RADIUS;
 						Logging.log(this, "Radius is " + tRadius);
 						
 						if(tCluster instanceof NeighborCluster && ((NeighborCluster)tCluster).getClustersToTarget() + pDiscovery.getDistance() > tRadius) continue;
 						boolean tBreak=false;
 						for(CoordinatorCEPDemultiplexed tCEP : tCluster.getParticipatingCEPs()) {
 							if(tCEP.isEdgeCEP()) tBreak = true;
 						}
 						if(tBreak) {
 							continue;
 						}
 						int tToken = tCluster.getToken();
 						if(!pDiscovery.getTokens().contains(Integer.valueOf(tToken))) {
 							if(tCluster instanceof NeighborCluster) {
 								getHRMController().getLogger().log(this, "Reporting " + tCluster + " to " + ((HRMName)getPeerName()).getDescr() + " because " + pDiscovery.getDistance() + " + " + ((NeighborCluster)tCluster).getClustersToTarget() + "=" + (pDiscovery.getDistance() + ((NeighborCluster)tCluster).getClustersToTarget()));
 								Logging.log(this, "token list was " + pDiscovery.getTokens());
 							}
 							getPathTo(pDiscovery, tCluster);
 							for(ICluster tNeighbor : tCluster.getNeighbors()) {
 								pDiscovery.addNeighborRelation(ClusterDummy.compare(tCluster.getClusterID(), tCluster.getToken(), tCluster.getHierarchyLevel()), ClusterDummy.compare(tNeighbor.getClusterID(), tNeighbor.getToken(), tNeighbor.getHierarchyLevel()));
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
 				HashMap<ClusterDummy, ClusterDummy> tToSetNegotiator = new HashMap<ClusterDummy, ClusterDummy>();
 				for(DiscoveryEntry tEntry : pDiscovery.getDiscoveryEntries()) {
 					tToSetNegotiator.put(handleDiscoveryEntry(tEntry), tEntry.getPredecessor());
 				}
 				for(ClusterDummy tDummy : tToSetNegotiator.keySet()) {
 					addAnnouncedCluster(getHRMController().getCluster(tDummy), getHRMController().getCluster(tToSetNegotiator.get(tDummy)));
 				}
 			}
 		}
 	}
 	
 	public ClusterDummy handleDiscoveryEntry(DiscoveryEntry pEntry) throws PropertyException
 	{
 		getHRMController().getLogger().trace(this, "Handling " + pEntry);
 		ICluster tNewCluster = getHRMController().getCluster(ClusterDummy.compare(pEntry.getClusterID(), pEntry.getToken(), pEntry.getLevel()));
 		if(tNewCluster == null) {
			for(Cluster tCluster : getHRMController().getRoutingTargetClusters()) {
 				if(tCluster.equals(ClusterDummy.compare(pEntry.getClusterID(), pEntry.getToken(), getCluster().getHierarchyLevel() - 1))) {
 					tNewCluster = tCluster;
 					if(tNewCluster instanceof NeighborCluster && tNewCluster.getCoordinatorsAddress() == null && tNewCluster.getCoordinatorName() == null) {
 						getHRMController().getLogger().log(this, "Filling required information into " + tNewCluster);
 						tNewCluster.setCoordinatorCEP(null, null, pEntry.getCoordinatorName(), pEntry.getCoordinatorRoutingAddress());
 						if(pEntry.isInterASCluster()) tNewCluster.setInterASCluster();
 					}
 				}
 			}
 			if(tNewCluster == null) {
 				/*
 				 * Be aware of the fact that the new attached cluster has lower level
 				 */
 				tNewCluster = new NeighborCluster(pEntry.getClusterID(), pEntry.getCoordinatorName(), pEntry.getCoordinatorRoutingAddress(), pEntry.getToken(), pEntry.getLevel(), getHRMController());
 				
 				getCluster().getHRMController().setSourceIntermediateCluster(tNewCluster, getCluster().getHRMController().getSourceIntermediate(getCluster()));
 				((NeighborCluster)tNewCluster).addAnnouncedCEP(this);
 				tNewCluster.setToken(pEntry.getToken());
 				tNewCluster.setPriority(pEntry.getPriority());
 				getHRMController().addCluster(tNewCluster);
 				if(pEntry.isInterASCluster()) {
 					tNewCluster.setInterASCluster();
 				}
 				try {
 					getHRMController().getHRS().registerNode(tNewCluster.getCoordinatorName(), tNewCluster.getCoordinatorsAddress());
 				} catch (RemoteException tExc) {
 					getHRMController().getLogger().err(this, "Unable to register " + tNewCluster.getCoordinatorName(), tExc);
 				}
 				getHRMController().getLogger().log(this, "Created " + tNewCluster);
 			}
 			
 			((NeighborCluster)tNewCluster).addAnnouncedCEP(this);
 //			((NeighborCluster)tNewCluster).setClusterHopsOnOpposite(pEntry.getClusterHops(), this);
 		}
 		if(pEntry.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tLink : pEntry.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tLink.getSource(), tLink.getDestination(), tLink.getPath());
 			}
 		}
 		return ClusterDummy.compare(tNewCluster.getClusterID(), tNewCluster.getToken(), tNewCluster.getHierarchyLevel());
 	}
 	
 	public String toString()
 	{
 		return getClass().getSimpleName()/* + "(" + mIdentification + ")"*/ + "@" + getCluster().getClusterDescription() +  (getPeerName() != null ? "->" + ((HRMName)getPeerName()).getDescr() + ":PR(" + mPeerPriority + ")" : "") + (mIsEdgeRouter ? "|INTER" : "|INTRA");
 	}
 //	
 //	public boolean hasRequestedCoordinator()
 //	{
 //		return mRequestedCoordinator;
 //	}
 //	
 	public Route getRouteToPeer()
 	{
 		return getMultiplexer().getRouteToPeer(this);
 	}
 	
 	@Override
 	public int getSerialisedSize()
 	{
 		return 0;
 	}
 	
 	public HRMID getHrmID()
 	{
 		return null;
 	}
 //	
 //	public Name retrieveName()
 //	{
 //		return null;
 //	}
 
 	@Override
 	public Namespace getNamespace()
 	{
 		return null;
 	}
 	
 	private CoordinatorCEPMultiplexer getMultiplexer()
 	{
 		return getCluster().getMultiplexer();
 	}
 }
