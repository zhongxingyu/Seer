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
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborZoneAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.PriorityUpdate;
 import de.tuilmenau.ics.fog.packets.hierarchical.RequestCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.RequestZoneMembership;
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
 import de.tuilmenau.ics.fog.routing.hierarchical.ElectionProcess.ElectionManager;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterDummy;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.IVirtualNode;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NeighborCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NodeConnection;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMIPMapper;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 
 public class CoordinatorCEPDemultiplexed implements IVirtualNode
 {
 	private static final long serialVersionUID = -8290946480171751216L;
 	private ICluster mRemoteCluster;
 	private ICluster mCluster;
 	private float mPeerPriority = -1;
 	private boolean mReceivedBorderNodeAnnouncement = false;
 	private boolean mPeerIsCoordinatorForNeighborZone = false;
 	private boolean mIsEdgeRouter = false;
 	private boolean mKnowsCoordinator = false;
 	private HashMap<ICluster, ICluster> mAnnouncerMapping;
 	private boolean mRequestedCoordinator = false;
 	private boolean mPartOfCluster = false;
 	private HRMController mHRMController = null;
 	private Logger mLogger = Logging.getInstance();
 	private BFSDistanceLabeler<IVirtualNode, NodeConnection> mBreadthFirstSearch;
 	private boolean mCrossLevelCEP = false;
 	
 	/**
 	 * 
 	 * @param pLogger Logger that should be used
 	 * @param pHRMController is the coordinator of a node
 	 * @param pCluster is the cluster this connection end point serves
 	 */
 	public CoordinatorCEPDemultiplexed(Logger pLogger, HRMController pHRMController, ICluster pCluster)
 	{
 		mHRMController = pHRMController;
 		mCluster = pCluster;
 		mLogger = pLogger;
 		mLogger.log(this, "Created");
 		getHRMController().getLogger().log(this, "Created for " + mCluster);
 	}
 	
 	/**
 	 * 
 	 * @param pData is the data that should be sent to the receiver side of this connection end point
 	 * @return true if the packet left the central multiplexer and the forwarding node that is attached to a direct down gate
 	 * @throws NetworkException
 	 */
 	public boolean receive(Serializable pData) throws NetworkException
 	{
 		Node tPhysicalNode = getHRMController().getPhysicalNode();
 		HierarchicalRoutingService tHRS = getHRMController().getHRS();
 		
 		/*
 		 * Invalid data
 		 */
 		if(pData == null) {
 			throw new NetworkException("Received invalid null pointer as data");
 		}
 
 		/*
 		 * main packet processing
 		 */
 		try {
 			
 			/**
 			 * RequestZoneMembership
 			 */
 			if(pData instanceof RequestZoneMembership) {
 				if(getCluster().getCoordinatorCEP() != null) {
 					Name tMyName = tPhysicalNode.getRoutingService().getNameFor(tPhysicalNode.getCentralFN());
 					if(getCluster().getCoordinatorName().equals(tMyName)) {
 						
 					}
 				}
 				
 			}
 			
 			/**
 			 * BullyElect
 			 */
 			if(pData instanceof BullyElect)	{
 				if(getCluster().getCoordinatorCEP() != null && ((BullyElect)pData).getSenderPriority() < getCluster().getHighestPriority()) {
 					mPeerPriority = ((BullyElect)pData).getSenderPriority();
 					if(getCluster().getHRMController().equals(tPhysicalNode.getCentralFN().getName())) {
 						BullyAnnounce tAnnounce = new BullyAnnounce(tPhysicalNode.getCentralFN().getName(), getCluster().getPriority(), getHRMController().getIdentity().createSignature(tPhysicalNode.toString(), null, getCluster().getLevel()), getCluster().getToken());
 						mLogger.log(this, " Sending bullyannounce because I have a coordinator: " + tAnnounce);
 						for(CoordinatorCEPDemultiplexed tCEP : getCluster().getParticipatingCEPs()) {
 							tAnnounce.addCoveredNode(tCEP.getPeerName());
 						}
 						if(tAnnounce.getCoveredNodes() == null || (tAnnounce.getCoveredNodes() != null && tAnnounce.getCoveredNodes().isEmpty())) {
 							getHRMController().getLogger().log(this, "Sending announce that does not cover anyhting");
 						}
 						write(tAnnounce);
 					} else {
						write(new BullyAlive(tPhysicalNode.getCentralFN().getName(), getCluster().getCoordinatorName()));
 						//TODO: packet is sent but never parsed or a timeout timer reset!!
 					}
 				} else {
 					mPeerPriority = ((BullyElect)pData).getSenderPriority();
 					BullyReply tAnswer = new BullyReply(tPhysicalNode.getCentralFN().getName(), getCluster().getPriority());
 					write(tAnswer);
 				}
 			}
 			
 			/**
 			 * BullyReply
 			 */
 			if(pData instanceof BullyReply) {
 				mPeerPriority = ((BullyReply)pData).getSenderPriority();
 			}
 			
 			/**
 			 * BullyAnnounce
 			 */
 			if(pData instanceof BullyAnnounce)  {
 				/*
 				 * Only an intermediate cluster on level 0 is able to store an announcement an forward it once a coordinator is set
 				 */
 				getCluster().interpretAnnouncement((BullyAnnounce)pData, this);
 			}
 			
 			/**
 			 * NeighborZoneAnnounce
 			 */
 			if(pData instanceof NeighborZoneAnnounce) {
 				NeighborZoneAnnounce tAnnounce = (NeighborZoneAnnounce)pData;
 
 				getHRMController().getLogger().log(this, "\n\n\nReceived " + tAnnounce + "\n\n\n");
 				
 				if(tAnnounce.isInterASAnnouncement()) {
 					Logging.log(tPhysicalNode.getAS().getName() + " received an announcement from " + tAnnounce.getASIdentification());
 					if(tPhysicalNode.getAS().getName().equals(tAnnounce.getASIdentification())) {
 						if(!getSourceName().equals(getPeerName())) {
 							for(Route tPath : tHRS.getCoordinatorRoutingMap().getRoute((HRMName)getSourceName(), (HRMName)getPeerName())) {
 								tAnnounce.addRoutingVector(new RoutingServiceLinkVector(tPath, tHRS.getCoordinatorRoutingMap().getSource(tPath), tHRS.getCoordinatorRoutingMap().getDest(tPath)));
 							}
 						}
 						for(CoordinatorCEPDemultiplexed tCEP : getCluster().getParticipatingCEPs()) {
 							boolean tWroteAnnouncement = false;
 							if(tCEP.isEdgeCEP()) {
 								tCEP.write(tAnnounce);
 								tWroteAnnouncement = true;
 							}
 							Logging.log(this, "Testing " + tCEP + " whether it is an inter as link:" + tWroteAnnouncement);
 						}
 					} else {
 						if(getCluster() instanceof Cluster) {
 							if(!getSourceName().equals(getPeerName())) {
 								RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(getRouteToPeer(), (HRMName)getSourceName(), (HRMName)getPeerName());
 								tAnnounce.addRoutingVector(tVector);
 							}
 							for(CoordinatorCEPDemultiplexed tCEP : getCluster().getParticipatingCEPs()) {
 								boolean tWroteAnnouncement = false;
 								if(tCEP.getRemoteCluster().getLevel() -1 == tAnnounce.getLevel()) {
 									tCEP.write(tAnnounce);
 									tWroteAnnouncement = true;
 								}
 								Logging.log(this, "Testing " + tCEP + " whether it leads to the clusters coordinator: " + tWroteAnnouncement);
 							}
 						} else if(getCluster() instanceof Coordinator) {
 							Logging.log(this, "Inter AS announcement " + tAnnounce + " is handled by " + getCluster() + " whether it leads to the clusters coordinator");
 							((Coordinator)getCluster()).getManagedCluster().handleAnnouncement(tAnnounce, this);
 						}
 					}
 				} else {
 					getCluster().handleAnnouncement(tAnnounce, this);
 				}
 				Logging.log(this, "Received " + tAnnounce + " from remote cluster " + mRemoteCluster);
 			}
 			/**
 			 * PriorityUpdate
 			 */
 			if(pData instanceof PriorityUpdate) {
 				mPeerPriority = ((PriorityUpdate)pData).getPriority();
 			}
 			/**
 			 * TopologyData
 			 */
 			if(pData instanceof TopologyData) {
 				getCluster().handleTopologyEnvelope((TopologyData)pData);
 			}/* else if (pData instanceof NestedDiscovery) {
 				NestedDiscovery tDiscovery = (NestedDiscovery) pData;
 				handleClusterDiscovery(tDiscovery);
 			}*/
 			
 			/**
 			 * RouteRequest
 			 */
 			if(pData instanceof RouteRequest) {
 				RouteRequest tRequest = (RouteRequest) pData;
 				if(tRequest.getTarget() instanceof HRMID) {
 					HRMName tRequestAddress = tRequest.getSource();
 					HRMName tDestinationAddress = getSourceName();
 					if(!tRequest.isAnswer() && tHRS.getFIBEntry( (HRMID) tRequest.getTarget()) != null && tRequestAddress != null && tRequestAddress.equals(tDestinationAddress)) {
 						/*
 						 * Find out if route request can be solved by this entity without querying a higher coordinator
 						 */
 						for(IVirtualNode tCluster : getHRMController().getClusters(0)) {
 							FIBEntry tEntry = tHRS.getFIBEntry( (HRMID) tRequest.getTarget());
 							if(tCluster instanceof Cluster && tEntry != null && (tEntry.getFarthestClusterInDirection() == null || tEntry.getFarthestClusterInDirection().equals(tCluster))) {
 								Route tRoute = tHRS.getRoutePath( getSourceName(), tRequest.getTarget(), new Description(), tPhysicalNode.getIdentity());
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
 								tRequest.addRoutingVector(new RoutingServiceLinkVector(tRoute, tSource, tTarget));
 								tRequest.setAnswer();
 								tRequest.setResult(ResultType.SUCCESS);
 								write(tRequest);
 								return true;
 							}
 						}
 					}
 					
 					if(!tRequest.isAnswer() && tRequest.isRouteAccumulation()) {
 						if(getRemoteCluster().getLevel() != getCluster().getLevel() && getCluster().isInterASCluster()) {
 							HRMID tAddress =  (HRMID) tRequest.getTarget();
 							LinkedList<Name> tIPAddresses = HRMIPMapper.getHRMIPMapper().getIPFromHRMID(tAddress);
 							Route tRoute = null;
 							if(tIPAddresses != null) {
 								for(Name tTargetAddress : tIPAddresses) {
 									try {
 										tRoute = ((RoutingServiceMultiplexer)tPhysicalNode.getRoutingService()).getRoute(tPhysicalNode.getCentralFN(), tTargetAddress, ((RouteRequest)pData).getDescription(), null);
 									} catch (NetworkException tExc) {
 										Logging.info(this, "BGP routing service did not find a route to " + tTargetAddress);
 									}
 									Logging.log(this, "Interop: Route to "+ tAddress + " with IP address " + tTargetAddress + " is " + tRoute);
 								}
 							} else {
 								getHRMController().getLogger().err(this, "Unable to distribute addresses because no IP address is available");
 							}
 							if(tRoute != null) {
 								tRequest.setAnswer();
 								tRequest.setRoute(tRoute);
 								write(tRequest);
 							}
 						} 
 						return true;
 					} else if(tRequest.isAnswer()) {
 						/*
 						 * In this case normally someone is waiting for this packet to arrive, therefore is not handled by any cluster and you only get a notification.
 						 */
 						synchronized(tRequest) {
 							tRequest.notifyAll();
 						}
 						return true;
 					}
 					if(getCluster() instanceof Cluster) {
 						Coordinator tManager = ((Cluster)getCluster()).getClusterManager();
 						tManager.handleRouteRequest((RouteRequest) pData, getRemoteCluster());
 						tManager.registerRouteRequest(tRequest.getSession(), this);
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
 				if(tRequest.getTarget() instanceof HRMID && !tRequest.isAnswer()) {
 					List<Route> tFinalPath = tHRS.getCoordinatorRoutingMap().getRoute(tRequest.getSource(), tRequest.getTarget());
 					if(tRequest.getRequiredClusters() != null) {
 
 						for(ICluster tDummy : tRequest.getRequiredClusters()) {
 							tFinalPath = null;
 							List<Route> tPath = tHRS.getCoordinatorRoutingMap().getRoute(tRequest.getSource(), tRequest.getTarget());
 							
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
 									tPath = tHRS.getCoordinatorRoutingMap().getRoute(tCandidate, tRequest.getTarget());
 									
 									if(tPath.size() < tOldPath.size()) {
 										List<Route> tFirstPart = tHRS.getCoordinatorRoutingMap().getRoute(tRequest.getSource(), tCandidate); 
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
 							tRequest.addRoutingVector(new RoutingServiceLinkVector(tSegment, tHRS.getCoordinatorRoutingMap().getSource(tSegment), tHRS.getCoordinatorRoutingMap().getDest(tSegment)));
 						}
 					}
 					tRequest.setAnswer();
 					write(tRequest);
 				} else if(tRequest.getTarget() instanceof HRMID && tRequest.isAnswer()) {
 					synchronized (tRequest) {
 						tRequest.notifyAll();
 					}
 				}
 			}
 			
 			/**
 			 * RequestCoordinator
 			 */
 			if (pData instanceof RequestCoordinator) {
 				getHRMController().getLogger().log(this, "Received " + pData);
 				RequestCoordinator tRequest = (RequestCoordinator) pData;
 				if(!tRequest.isAnswer()) {
 					if(getCluster().getCoordinatorCEP() != null) {
 						ICluster tCluster = getCluster().getHRMController().getClusterWithCoordinatorOnLevel(getCluster().getLevel());
 						Logging.log(this, "Name of coordinator is " + tCluster.getCoordinatorName());
 						
 						int tToken = tCluster.getToken();
 						Name tCoordinatorName = tCluster.getCoordinatorName();
 						long tCoordinatorAddress = tCluster.getCoordinatorsAddress().getAddress().longValue();
 						HRMName tL2Address = tCluster.getCoordinatorsAddress();
 						int tLevel = tCluster.getLevel();
 						
 						DiscoveryEntry tEntry = new DiscoveryEntry(tToken, tCoordinatorName, tCoordinatorAddress, tL2Address, tLevel);
 						tEntry.setPriority(getCluster().getNodePriority());
 						tEntry.setRoutingVectors(getPath(tCluster.getCoordinatorsAddress()));
 						tRequest.addDiscoveryEntry(tEntry);
 						tRequest.setCoordinatorKnown(true);
 						tRequest.setAnswer();
 					} else {
 						tRequest.setCoordinatorKnown(false);
 						tRequest.setAnswer();
 					}
 					write(tRequest);
 				} else {
 					if(tRequest.isCoordinatorKnown()) {
 						mKnowsCoordinator = true;
 					} else {
 						mKnowsCoordinator = false;
 					}
 					if(tRequest.getDiscoveryEntries() != null) {
 						for(DiscoveryEntry tEntry : tRequest.getDiscoveryEntries()) {
 							ClusterDummy tDummy = handleDiscoveryEntry(tEntry);
 							getCluster().getHRMController().getCluster(ClusterDummy.compare((((HRMName)getSourceName()).getAddress().longValue()), getCluster().getToken(), getCluster().getLevel())).addNeighborCluster(getCluster().getHRMController().getCluster(tDummy));
 							addAnnouncedCluster(getHRMController().getCluster(tDummy), getRemoteCluster());
 						}
 					}
 					synchronized(tRequest) {
 						Logging.log(this, "Received answer to " + tRequest + ", notifying");
 						tRequest.mWasNotified = true;
 						tRequest.notifyAll();
 					}
 				}
 			}
 		} catch (PropertyException tExc) {
 			Logging.err(this, "Unable to fulfill requirements", tExc);
 		}
 		return true;
 	}
 	
 	/**
 	 * @deprecated
 	 * If this CEP is used for communication between different hierarchical levels: for use of HRM+BGP
 	 */
 	public void setCrossLayerCEP()
 	{
 		mCrossLevelCEP = true;
 	}
 	
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
 	public void getPathTo(NestedDiscovery pDiscovery, ICluster pCluster) throws NetworkException, PropertyException
 	{
 		if(pCluster.getCoordinatorName() != null) {
 			DiscoveryEntry tEntry = new DiscoveryEntry(pCluster.getToken(), pCluster.getCoordinatorName(), pCluster.getClusterID(), pCluster.getCoordinatorsAddress(), pCluster.getLevel());
 			tEntry.setClusterHops(getCluster().getHRMController().getClusterDistance(pCluster));
 			tEntry.setPriority(pCluster.getPriority());
 			tEntry.setRoutingVectors(getPath(pCluster.getCoordinatorsAddress()));
 			if(pCluster.isInterASCluster()) {
 				tEntry.setInterASCluster();
 			}
 			
 			List<NodeConnection> tClusterList = getHRMController().getClusterMap().getRoute(getCluster(), pCluster);
 			if(!tClusterList.isEmpty()) {
 				ICluster tPredecessor = (ICluster) getHRMController().getClusterMap().getDest(pCluster, tClusterList.get(tClusterList.size()-1));
 				tEntry.setPredecessor(ClusterDummy.compare(tPredecessor.getClusterID(), tPredecessor.getToken(), tPredecessor.getLevel()));
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
 		return mCluster;
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
 		if(getCluster().getLevel() == 0) {
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
 	
 	public void setPeerPriority(float pPeerPriority)
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
 			if(pNegotiate.getLevel() < mAnnouncerMapping.get(pAnnounced).getLevel()) {
 				mAnnouncerMapping.remove(pAnnounced);
 				mAnnouncerMapping.put(pAnnounced, pNegotiate);
 			} else if (pNegotiate instanceof NeighborCluster && mAnnouncerMapping.get(pAnnounced) instanceof NeighborCluster && ((NeighborCluster)pNegotiate).getClustersToTarget() < ((NeighborCluster)mAnnouncerMapping.get(pAnnounced)).getClustersToTarget()) {
 				getCluster().getHRMController().getLogger().log(this, "replacing negotiating cluster of " + pAnnounced + ": " + mAnnouncerMapping.get(pAnnounced) + " with " + pNegotiate);
 				mAnnouncerMapping.remove(pAnnounced);
 				mAnnouncerMapping.put(pAnnounced, pNegotiate);
 			}
 		}
 	}
 	
 	public float getPeerPriority()
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
 	
 	public boolean write(Serializable pData)
 	{
 		if(pData instanceof RequestCoordinator) {
 			mRequestedCoordinator = true;
 			getHRMController().getLogger().log(this, "Sending " + pData);
 		}
 		if(getCluster() instanceof Coordinator && !mCrossLevelCEP) {
 			getCEPMultiplexer().write(pData, this, ClusterDummy.compare(((L2Address)getPeerName()).getAddress().longValue(), getCluster().getToken(), getCluster().getLevel()));
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
 		ElectionManager.getElectionManager().removeElection(getCluster().getLevel(), getCluster().getClusterID());
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
 				mBreadthFirstSearch = new BFSDistanceLabeler<IVirtualNode, NodeConnection>();
 			}
 			mBreadthFirstSearch.labelDistances(getHRMController().getClusterMap().getGraphForGUI(), tSourceCluster);
 			List<IVirtualNode> tDiscoveryCandidates = mBreadthFirstSearch.getVerticesInOrderVisited();
 			if(tSourceCluster != null) {
 				for(IVirtualNode tVirtualNode : tDiscoveryCandidates) {
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
 								pDiscovery.addNeighborRelation(ClusterDummy.compare(tCluster.getClusterID(), tCluster.getToken(), tCluster.getLevel()), ClusterDummy.compare(tNeighbor.getClusterID(), tNeighbor.getToken(), tNeighbor.getLevel()));
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
 			for(ICluster tCluster : getHRMController().getClusters()) {
 				if(tCluster.equals(ClusterDummy.compare(pEntry.getClusterID(), pEntry.getToken(), getCluster().getLevel() - 1))) {
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
 			((NeighborCluster)tNewCluster).setClusterHopsOnOpposite(pEntry.getClusterHops(), this);
 		}
 		if(pEntry.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tLink : pEntry.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tLink.getSource(), tLink.getDestination(), tLink.getPath());
 			}
 		}
 		return ClusterDummy.compare(tNewCluster.getClusterID(), tNewCluster.getToken(), tNewCluster.getLevel());
 	}
 	
 	public String toString()
 	{
 		return getClass().getSimpleName()/* + "(" + mIdentification + ")"*/ + "@" + getCluster().getClusterDescription() +  (getPeerName() != null ? "->" + ((HRMName)getPeerName()).getDescr() + ":PR(" + mPeerPriority + ")" : "") + (mIsEdgeRouter ? "|INTER" : "|INTRA");
 	}
 	
 	public boolean hasRequestedCoordinator()
 	{
 		return mRequestedCoordinator;
 	}
 	
 	public Route getRouteToPeer()
 	{
 		return getMultiplexer().getRouteToPeer(this);
 	}
 	
 	@Override
 	public int getSerialisedSize()
 	{
 		return 0;
 	}
 	
 	public HRMID retrieveAddress()
 	{
 		return null;
 	}
 	
 	public Name retrieveName()
 	{
 		return null;
 	}
 
 	@Override
 	public Namespace getNamespace()
 	{
 		return null;
 	}
 	
 	protected CoordinatorCEPMultiplexer getMultiplexer()
 	{
 		return getCluster().getMultiplexer();
 	}
 }
