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
 import java.math.BigInteger;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import de.tuilmenau.ics.fog.exceptions.AuthenticationException;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.packets.election.BullyPriorityUpdate;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.RouteRequest;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.RouteRequest.ResultType;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData.FIBEntry;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.*;
import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterDummy;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.IRoutableClusterGraphNode;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NeighborCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.RoutableClusterGraphLink;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMIPMapper;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 
 /**
  * This class is used for a coordinator instance and can be used on all hierarchy levels.
  */
 public class Coordinator implements ICluster, Observer
 {
 	/**
 	 * This is the GUI specific cluster counter, which allows for globally unique cluster IDs.
 	 * It's only used within the GUI. 	
 	 */
 	private static int sGUICoordinatorID = 0;
 	
 	/*
 	 * List for identification of clusters
 	 */
 	private LinkedList<Integer> mTokens = new LinkedList<Integer>();
 	/*
 	 * List for identification of entities this cluster manager is connected to
 	 */
 	private LinkedList<Name> mConnectedEntities = new LinkedList<Name>();
 	private HRMID mHRMID = null;
 	private int mLevel;
 	private int mLastUsedAddress;
 	private Cluster mManagedCluster;
 	private HashMap<CoordinatorCEPDemultiplexed, TopologyData> mAddressMapping = null;
 	private LinkedList<CoordinatorCEPDemultiplexed> mCEPs = null;
 	private CoordinatorCEPDemultiplexed mCoordinatorCEP = null;
 	private HRMSignature mCoordinatorSignature = null;
 	private Name mCoordinatorName = null;
 	private HRMName mCoordinatorAddress = null;
 	private BullyPriority mBullyPriority;
 	private int mToken;
 	private long mHighestPriority;
 	private BFSDistanceLabeler<IRoutableClusterGraphNode, RoutableClusterGraphLink> mBreadthFirstSearch = new BFSDistanceLabeler<IRoutableClusterGraphNode, RoutableClusterGraphLink>();
 	private List<IRoutableClusterGraphNode> mClustersToNotify;
 	private LinkedList<Long> mBouncedAnnounces = new LinkedList<Long>();
 	private int mReceivedAnnounces=0;
 	private LinkedList<Name> mIgnoreOnAddressDistribution=null;
 	private Long mClusterID;
 	private LinkedList<HRMID> mHigherHRMIDs = null;
 	private TopologyData mEnvelope = null;
 	private HashMap<HRMID, IRoutableClusterGraphNode> mAddressToClusterMapping = new HashMap<HRMID, IRoutableClusterGraphNode>();
 	private HashMap<HRMID, FIBEntry> mIDToFIBMapping = new HashMap<HRMID, FIBEntry>();
 	private LinkedList<NeighborClusterAnnounce> mReceivedAnnouncements;
 	private LinkedList<HRMSignature> mSignatures = new LinkedList<HRMSignature>();
 //	private HashMap<CoordinatorCEPDemultiplexed, Integer> mCEPsToBGPRouters;
 	private HashMap<Long, CoordinatorCEPDemultiplexed> mRouteRequestDispatcher;
 	private HashMap<HRMID, LinkedList<RoutingServiceLinkVector>> mAddressToPathMapping;
 	
 	/**
 	 * This is the GUI specific coordinator ID. It is used to allow for an easier debugging.
 	 */
 	private int mGUICoordinatorID = sGUICoordinatorID++;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6824959379284820010L;
 	public Coordinator(Cluster pCluster, int pLevel, HRMID pInitialAddress)
 	{
 		mHRMID =  pInitialAddress;
 		mLevel = pLevel;
 		mClusterID = pCluster.getClusterID();
 		mLastUsedAddress = 0;
 		mAddressMapping = new HashMap<CoordinatorCEPDemultiplexed, TopologyData>();
 		mManagedCluster = pCluster;
 		mCEPs = new LinkedList<CoordinatorCEPDemultiplexed>();
 		mManagedCluster.getHRMController().getRoutableClusterGraph().addObserver(this);
 		mBullyPriority = new BullyPriority(getHRMController().getPhysicalNode(), mLevel);
 		getHRMController().registerClusterManager(this, mLevel);
 		
 		Logging.log(this, "Creating coordinator instance on hierarchy level " + mLevel);
 	}
 	
 //	public void addIgnoreEntry(Name pEntry)
 //	{
 //		if(mIgnoreOnAddressDistribution == null) {
 //			mIgnoreOnAddressDistribution = new LinkedList<Name>();
 //			mIgnoreOnAddressDistribution.add(pEntry);
 //		} else {
 //			mIgnoreOnAddressDistribution.add(pEntry);
 //		}
 //	}
 //	
 //	public void addIgnoreEntries(Collection<Name> pEntries)
 //	{
 //		if(mIgnoreOnAddressDistribution == null ) {
 //			mIgnoreOnAddressDistribution = new LinkedList<Name>();
 //			if(pEntries != null) {
 //				mIgnoreOnAddressDistribution.addAll(pEntries);
 //			}
 //		} else {
 //			if(pEntries != null) {
 //				mIgnoreOnAddressDistribution.addAll(pEntries);
 //			}
 //		}
 //	}
 //	
 //	public static BigInteger generateAdress(int pLevel, BigInteger pValue)
 //	{
 //		return pValue.shiftLeft(HRMConfig.Hierarchy.USED_BITS_PER_LEVEL * pLevel);
 //	}
 	
 	public void storeAnnouncement(NeighborClusterAnnounce pAnnounce)
 	{
 		getLogger().log(this, "Storing " + pAnnounce);
 		if(mReceivedAnnouncements == null) {
 			mReceivedAnnouncements = new LinkedList<NeighborClusterAnnounce>();
 		}
 		pAnnounce.setNegotiatorIdentification(ClusterDummy.compare(mManagedCluster.getClusterID(), mManagedCluster.getToken(), mManagedCluster.getHierarchyLevel()));
 		mReceivedAnnouncements.add(pAnnounce);
 	}
 	
 	public LinkedList<Long> getBounces()
 	{
 		return mBouncedAnnounces;
 	}
 	
 //	public void setHierarchicalLevelID(int pLevel, int pID)
 //	{
 //		mHRMID.setLevelAddress(pLevel, BigInteger.valueOf(pID));
 //	}
 	
 	private HRMID generateNextAddress()
 	{
 		HRMID tID = mHRMID.clone();
 		BigInteger tAddress = BigInteger.valueOf(++mLastUsedAddress);
 		tID.setLevelAddress(mLevel - 1, tAddress);
 		if(mLevel != 1) {
 			HRMIPMapper.registerHRMID(tID);
 		}
 		return tID;
 	}
 	
 	public boolean prepareAboveCluster(int pLevel)
 	{
 		getLogger().log(this, "Preparing cluster on level "  +pLevel + ":I will connect to " + mManagedCluster.getNeighbors());
 		int tRadius = HRMConfig.Routing.EXPANSION_RADIUS;
 
 		Logging.log(this, "Radius is " + tRadius);
 		for(int i = 1; i <= tRadius; i++) {
 			
 			String tString = new String(">>> Expanding to radius (" + i + "/" + tRadius + ", possible clusters:");
 			for(Cluster tCluster : getHRMController().getRoutingTargetClusters()) {
 				if(tCluster.getHierarchyLevel() == getHierarchyLevel() - 1) {
 					tString += "\n" + tCluster.toString();
 				}
 			}
 			getLogger().log(this, tString);
 			
 			mBreadthFirstSearch.labelDistances(getHRMController().getRoutableClusterGraph().getGraphForGUI(), mManagedCluster);
 			mClustersToNotify = mBreadthFirstSearch.getVerticesInOrderVisited();
 			List<IRoutableClusterGraphNode> tClustersToNotify = new LinkedList<IRoutableClusterGraphNode>(); 
 			getLogger().log(this, "Clusters remembered for notification: " + mClustersToNotify);
 			for(IRoutableClusterGraphNode tNode : mClustersToNotify) {
 				if(!((ICluster)tNode).isInterASCluster()) {
 					if(tNode instanceof Cluster && i == 1) {
 						tClustersToNotify.add(tNode);
 					} else if (tNode instanceof NeighborCluster && ((NeighborCluster)tNode).getClustersToTarget() <= i && ((NeighborCluster)tNode).getClustersToTarget() != 0 && !mConnectedEntities.contains(((NeighborCluster)tNode).getCoordinatorName())) {
 						tClustersToNotify.add(tNode);					
 					}
 				}
 			}
 			mClustersToNotify = tClustersToNotify;
 			getLogger().log(this, "clusters that are remaining for this round: " + mClustersToNotify);
 			connectToNeighbors(i);
 		}
 		/*
 		for(CoordinatorCEP tCEP : mCEPs) {
 			tCEP.write(new BullyElect(mManagedCluster.getPriority(), pLevel, getCoordinator().getReferenceNode().getCentralFN().getName(), null));
 		}
 		*/
 		getLogger().log(this, "has a total of the following connections to higher candidates" + mCEPs);
 		return true;
 	}
 	
 	public LinkedList<RoutingServiceLinkVector> getPathToCoordinator(ICluster pSourceCluster, ICluster pDestinationCluster)
 	{
 		if(pDestinationCluster instanceof Cluster && ((Cluster)pDestinationCluster).isInterASCluster()) {
 			getLogger().info(this, "Omitting " + pDestinationCluster + " because it is an inter AS cluster");
 		} else {
 			List<Route> tCoordinatorPath = getHRMController().getHRS().getCoordinatorRoutingMap().getRoute(pSourceCluster.getCoordinatorsAddress(), pDestinationCluster.getCoordinatorsAddress());
 			LinkedList<RoutingServiceLinkVector> tVectorList = new LinkedList<RoutingServiceLinkVector>();
 			if(tCoordinatorPath != null) {
 				for(Route tPath : tCoordinatorPath) {
 					tVectorList.add(new RoutingServiceLinkVector(tPath, getHRMController().getHRS().getCoordinatorRoutingMap().getSource(tPath), getHRMController().getHRS().getCoordinatorRoutingMap().getDest(tPath)));
 				}
 			}
 			return tVectorList;
 		}
 		return null;
 	}
 	
 	private void addApprovedSignature(HRMSignature pSignature)
 	{
 		if(!mSignatures.contains(pSignature)) {
 			mSignatures.add(pSignature);
 		}
 	}
 	
 	private IRoutableClusterGraphNode getFarthestVirtualNodeInDirection(IRoutableClusterGraphNode pSource, IRoutableClusterGraphNode pTarget)
 	{
 		List<RoutableClusterGraphLink> tList = getHRMController().getRoutableClusterGraph().getRoute(pSource, pTarget);
 
 		//ICluster tFarthestCluster = null;
 		IRoutableClusterGraphNode tTransitiveElement = pSource;
 		try {
 			int tDistance = 0;
 			if(tList.size() > HRMConfig.Routing.EXPANSION_RADIUS) {
 				while(tDistance != HRMConfig.Routing.EXPANSION_RADIUS) {
 					tTransitiveElement = getHRMController().getRoutableClusterGraph().getDest(tTransitiveElement, tList.get(0));
 					tList.remove(0);
 					tDistance++;
 				}
 				return tTransitiveElement;
 			} else {
 				return pTarget;
 			}
 		} catch (IndexOutOfBoundsException tExc) {
 			Logging.err(this, "Unable to determine cluster that is farthest in direction to target");
 			return null;
 		}
 	}
 	
 	/**
 	 * This is the function the highest coordinator calls in order to distribute all addresses to its clients
 	 * @throws RemoteException 
 	 * @throws RequirementsException 
 	 * @throws RoutingException 
 	 */
 	public void distributeAddresses() throws RoutingException, RequirementsException, RemoteException
 	{
 		TopologyData tManagedClusterEnvelope = new TopologyData();
 		Logging.log(this, "Will now distribute addresses to entities on level 0");
 		if(mLevel == 1) {
 			HRMID tSelf = generateNextAddress();
 			tManagedClusterEnvelope.setHRMID(tSelf);
 			mManagedCluster.setHRMID(tSelf);
 		}
 		/*
 		 * in this part the addresses are mapped either to CEPs or clusters
 		 * 
 		 * level one : map addresses to connection end points, later retrieveAddress() is used in order to distribute the next hop entry
 		 */
 		getLogger().log(this, "available clients for address distribution: " + mManagedCluster.getParticipatingCEPs());
 		for(CoordinatorCEPDemultiplexed tReceivingCEP : mManagedCluster.getParticipatingCEPs()) {
 			HRMID tID = null;
 			TopologyData tEnvelope = new TopologyData();
 			try {
 				if(!tReceivingCEP.isPeerCoordinatorForNeighborZone() || (mIgnoreOnAddressDistribution != null && mIgnoreOnAddressDistribution.contains(tReceivingCEP.getPeerName()))) {
 					/*
 					 * generate next address and map it to a CEP in case we are on level one, or to a cluster in case we are in a level higher than 1
 					 */
 					tID = generateNextAddress();
 					
 					if(mLevel == 1 ) {
 						map(tID, tReceivingCEP);
 					} else {
 						map(tID, tReceivingCEP.getRemoteCluster());
 					}
 					tEnvelope.setHRMID(tID);
 				} else {
 					getLogger().log(this, "Skipping " + tReceivingCEP + " in address distribution as it is a coordinator for another cluster on level " + mLevel);
 				}
 				/*
 				 * Collect all forwarding entries for connection end point tReceivingCEP, afterwards routes to supernodes are calculated
 				 */
 				mAddressMapping.put(tReceivingCEP, tEnvelope);
 				
 				/*
 				 * for identification, the cluster gets its generated HRMID
 				 */
 				
 				if(tReceivingCEP.getRemoteCluster() != null && mLevel != 1) {
 					tReceivingCEP.getRemoteCluster().setHRMID(tID);
 				} else {
 					getLogger().log(this, "unable to find remote cluster for " + tReceivingCEP);
 				}
 			} catch (NullPointerException tExc) {
 				getLogger().err(this, "Connection endpoint " + tReceivingCEP + " is gone ");
 			}
 		}
 		/*
 		 * Now the calculation of the next hops begins
 		 * That takes \mathcal{O}(n^2*m*n \log n)
 		 */
 		
 		/*
 		 * outer loop: for every source the target is calculated
 		 */
 		
 		try {
 		
 			for(CoordinatorCEPDemultiplexed tSourceCEP : mAddressMapping.keySet()) {
 				
 				/*
 				 * inner loop: these are the target we mapped an address to, earlier
 				 */
 				for(CoordinatorCEPDemultiplexed tDestinationCEP: mAddressMapping.keySet()) {
 					if(tSourceCEP == tDestinationCEP) {
 						continue;
 					}
 					
 					/*
 					 * if cluster managers level is above one, HRMIDs are mapped to clusters
 					 */
 					
 					// are we on a higher hierarchy level?
 					if(mLevel > 1) {
 						/*
 						 * calculate entire cluster route from source to target
 						 * 
 						 * then: tell the cluster the next neighbor cluster that brings the packet to its target
 						 */
 						List<RoutableClusterGraphLink> tList = getHRMController().getRoutableClusterGraph().getRoute(tSourceCEP.getRemoteCluster(), tDestinationCEP.getRemoteCluster());
 						HRMID tFrom = tSourceCEP.getRemoteCluster().getHrmID();
 						HRMID tTo   = tDestinationCEP.getRemoteCluster().getHrmID();
 						
 						if(tFrom.equals(tTo)) {
 							continue;
 						}
 						
 						/*
 						 * Probably that cluster only knows the route to its neighbors, so the route to the next cluster has to be provided
 						 * 
 						 * As  two clusters can have the same coordinator, we provide the cluster that differs in the address of the coordinator
 						 */
 						
 						ICluster tNextCluster = null;
 						try {
 							tNextCluster = (ICluster)getHRMController().getRoutableClusterGraph().getDest(tSourceCEP.getRemoteCluster(), tList.get(0));
 						} catch (IndexOutOfBoundsException tExc) {
 							getLogger().err(this, "Unable to calculate nex hop for " + tFrom + " to " + tTo);
 							
 							/*
 							 * If no cluster neighbor was calculated, the source cluster is provided
 							 */
 							
 							tNextCluster = tSourceCEP.getRemoteCluster();
 						}
 						/*
 						 * if the coordinator address of the forwarding cluster equals the address of the source coordinator address, take the next cluster
 						 */
 						if(tNextCluster.getCoordinatorsAddress().equals(tSourceCEP.getRemoteCluster().getCoordinatorsAddress()) && tList.size() > 1) {
 							tNextCluster = (ICluster)getHRMController().getRoutableClusterGraph().getDest(tNextCluster, tList.get(1));
 						}
 						
 						/*
 						 * The address of the next hop has to be found out.
 						 * Cluster managers above cluster one do not forward by Logical Link Layer addresses, but by HRMIDs that are later on mapped to the next physical router
 						 */
 						HRMName tNextHop  = null;
 						if(!tSourceCEP.getRemoteCluster().getCoordinatorsAddress().equals(tDestinationCEP.getRemoteCluster().getCoordinatorsAddress())) {
 							tNextHop = getHRMController().getRoutableClusterGraph().getDest(tSourceCEP.getRemoteCluster(), tList.get(0)).getHrmID();
 						}
 						
 						FIBEntry tEntry = mAddressMapping.get(tSourceCEP).new FIBEntry(tTo, tNextHop, ClusterDummy.compare(
 								tNextCluster.getClusterID(),
 								tNextCluster.getToken(),
 								tNextCluster.getHierarchyLevel()),
 								getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel-1)
 								);
 						IRoutableClusterGraphNode tTargetNode = getFarthestVirtualNodeInDirection(tSourceCEP.getRemoteCluster(), tDestinationCEP.getRemoteCluster());
 						ClusterDummy tDummy = null;
 						if(tTargetNode instanceof ICluster) {
 							tDummy = ClusterDummy.compare(((ICluster)tTargetNode).getClusterID(), ((ICluster)tTargetNode).getToken(), ((ICluster)tTargetNode).getHierarchyLevel());
 						}
 						tEntry.setFarthestClusterInDirection(tDummy);
 						/*
 						 * In this case the routes are calculated directly and not mapped to the responding nodes
 						 */
 						LinkedList<RoutingServiceLinkVector> tPolygon = null;
 						tPolygon = getPathToCoordinator(tSourceCEP.getRemoteCluster(), tDestinationCEP.getRemoteCluster()); 
 						if(tPolygon != null && !tPolygon.isEmpty()) {
 							tEntry.setRoutingVectors(tPolygon);
 						}
 						mAddressMapping.get(tSourceCEP).addForwardingentry(tEntry);
 					} else {
 						/*
 						 * At level one:
 						 * In this case the HRMID is mapped to a connection endpoint, not to a cluster like before
 						 * In that case the address is a direct neighbor. So we use the peer routing service address what is supposed to be the logical link layer address
 						 * 
 						 */
 						
 						FIBEntry tEntry = mAddressMapping.get(tSourceCEP).new FIBEntry(
 								mAddressMapping.get(tDestinationCEP).getHRMID(),
 								(HRMName)tDestinationCEP.getPeerName(),
 								ClusterDummy.compare(mManagedCluster.getClusterID(), mManagedCluster.getToken(), mManagedCluster.getHierarchyLevel()),
 								getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel-1));
 						mAddressMapping.get(tSourceCEP).addForwardingentry(tEntry);
 						LinkedList<RoutingServiceLinkVector> tPolygon = getPathToCoordinator(tSourceCEP.getRemoteCluster(), tDestinationCEP.getRemoteCluster()); 
 						if(!tPolygon.isEmpty()) {
 							/*
 							 * Here was a strange error, twist while and setting of the routing vector if fowarding is broken
 							 */
 							while(!tPolygon.getFirst().equals(tSourceCEP.getPeerName())) {
 								tPolygon.removeFirst();
 								if(tPolygon.isEmpty()) {
 									break;
 								}
 							}
 							tEntry.setRoutingVectors(tPolygon);
 						}
 						IRoutableClusterGraphNode tTargetNode = getFarthestVirtualNodeInDirection(tSourceCEP.getRemoteCluster(), tDestinationCEP.getRemoteCluster());
 						ClusterDummy tDummy = null;
 						if(tTargetNode instanceof ICluster) {
 							tDummy = ClusterDummy.compare(((ICluster)tTargetNode).getClusterID(), ((ICluster)tTargetNode).getToken(), ((ICluster)tTargetNode).getHierarchyLevel());
 						}
 						tEntry.setFarthestClusterInDirection(tDummy);
 					}
 				}
 				
 				// are we on basic level?
 				if(mLevel == 1 ) {
 					/*
 					 * The host itself has to tell its client how to reach it: get the address providers address: retrieveAddress() and then give the clients the address of the address provider
 					 */
 					FIBEntry tEntry = mAddressMapping.get(tSourceCEP).new FIBEntry(
 							mManagedCluster.getHrmID(),
 							tSourceCEP.getSourceName(),
 							ClusterDummy.compare(mManagedCluster.getClusterID(), mManagedCluster.getToken(), mManagedCluster.getHierarchyLevel()),
 							getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel-1));
 					mAddressMapping.get(tSourceCEP).addForwardingentry(tEntry);
 					IRoutableClusterGraphNode tTargetNode = getFarthestVirtualNodeInDirection(tSourceCEP.getRemoteCluster(), mManagedCluster);
 					ClusterDummy tDummy = null;
 					if(tTargetNode instanceof ICluster) {
 						tDummy = ClusterDummy.compare(((ICluster)tTargetNode).getClusterID(), ((ICluster)tTargetNode).getToken(), ((ICluster)tTargetNode).getHierarchyLevel());
 					}
 					tEntry.setFarthestClusterInDirection(tDummy);
 					/*
 					 * Now the managed cluster needs the information on how to reach the next hop
 					 */
 					FIBEntry tManagedEntry = tManagedClusterEnvelope.new FIBEntry(
 							mAddressMapping.get(tSourceCEP).getHRMID(),
 							tSourceCEP.getPeerName(),
 							ClusterDummy.compare(mManagedCluster.getClusterID(), mManagedCluster.getToken(), mManagedCluster.getHierarchyLevel()),
 							getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel));
 					tManagedClusterEnvelope.addForwardingentry(tManagedEntry);
 					IRoutableClusterGraphNode tPeerNode = getFarthestVirtualNodeInDirection(mManagedCluster, tSourceCEP.getRemoteCluster());
 					ClusterDummy tPeerDummy = null;
 					if(tTargetNode instanceof ICluster) {
 						tPeerDummy = ClusterDummy.compare(((ICluster)tPeerNode).getClusterID(), ((ICluster)tPeerNode).getToken(), ((ICluster)tPeerNode).getHierarchyLevel());
 					}
 					tEntry.setFarthestClusterInDirection(tPeerDummy);
 				}
 				
 				/*
 				 * Here starts distribution of the HRMIDs provided by higher coordinators
 				 */
 				
 				if(mHigherHRMIDs != null) {
 					// are we on a higher hierarchy level?
 					if(mLevel > 1) {
 						for(HRMID tHRMID : mHigherHRMIDs) {
 							/*
 							 * tNegotiator is the source cluster
 							 * tRelevant is the super node that provides the possibility to route the packet to its destination
 							 * tHRMIDMapping gets the cluster of the connection end point that announced that cluster.
 							 * 
 							 * special treatment is used if the negotiator equals the HRMID mapping: we have to tell that cluster how to reach the destination via a routing service link
 							 */
 							ICluster tNegotiator = tSourceCEP.getRemoteCluster();
 							ICluster tRelevant = ((ICluster)getVirtualNodeFromHRMID(tHRMID));
 							ICluster tHRMIDMapping = null;
 							if(tRelevant instanceof NeighborCluster) {
 								NeighborCluster tNewRelevant = (NeighborCluster)tRelevant;
 								CoordinatorCEPDemultiplexed tNewRelevantCEP = tNewRelevant.getAnnouncedCEP(tSourceCEP.getRemoteCluster());
 								tHRMIDMapping = tNewRelevantCEP.getNegotiator(tRelevant);
 								
 							} else {
 								//tHRMIDMapping = ((IntermediateCluster)tRelevant).getAnnouncedCEP().getNegotiator(tRelevant);
 								tHRMIDMapping = tRelevant;
 							}
 							
 							if(tHRMIDMapping == null) {
 								throw new RoutingException("Unable to find forwarding cluster for HRMID " + tHRMID + " while forwarding cluster was " + tRelevant);
 							}
 							
 							LinkedList<RoutingServiceLinkVector> tListToTarget = null;
 							
 							if(getPathFromHRMID(tHRMID) != null && !getPathFromHRMID(tHRMID).isEmpty() && getPathFromHRMID(tHRMID).contains(tNegotiator.getCoordinatorsAddress())) {
 								/*
 								 * If a path was provided, we get it and trim it until the entry for the chosen node is found:
 								 * For this to work, HRMID and RoutingServiceLinkVector can be compared and are true if the source of the
 								 * routing service link equals the routing service address
 								 */
 								LinkedList<RoutingServiceLinkVector> tPath = getPathFromHRMID(tHRMID);
 								if(!tPath.isEmpty()) {
 									LinkedList<RoutingServiceLinkVector> tSavedPath = (LinkedList<RoutingServiceLinkVector>) tPath.clone();
 									while(!tPath.isEmpty() && !tPath.getFirst().equals(tNegotiator.getCoordinatorsAddress())) {
 										tPath.removeFirst();
 									}
 									getLogger().log(this, "Started with initial path " + tSavedPath + " and path is now " + tPath);
 								}
 								/*
 								 * that list is given to the chose node so he can continue in doing what this node currently does: push the route forward through the network
 								 */
 								tListToTarget = tPath;
 							} else {
 								/*
 								 * If no list is found, we calculate a new route again
 								 * getPathToCoordinator calculates the path from a source clusters coordinator to a target coordinator
 								 */
 								LinkedList<RoutingServiceLinkVector> tRouteToCoordinator = getPathToCoordinator(tNegotiator, tRelevant);
 								/*
 								 * If we can give that node a route to the target from this point we do so.
 								 */
 								if(!tRouteToCoordinator.isEmpty()) {
 									tListToTarget = tRouteToCoordinator;
 								} else {
 									tListToTarget = null;
 								}
 							}
 							
 							if(tNegotiator.getHierarchyLevel() != tHRMIDMapping.getHierarchyLevel()) {
 								getLogger().err(this, "Searching for a route between clusters withing different levels");
 							}
 							
 							/*
 							 * Calculate forwarding supernodes
 							 * 
 							 * Get the cluster connection list from the source to the cluster that is able to forward the specified HRMID
 							 */
 							
 							List<RoutableClusterGraphLink> tList = getHRMController().getRoutableClusterGraph().getRoute(tNegotiator, tHRMIDMapping);
 							getLogger().log(this, "Cluster route from " + tNegotiator + " to " + tHRMIDMapping + " is " + tList);
 							
 							/*
 							 * forwarding is done based on HRMIDs, for a target HRMID the next hop (an HRMID) has to be calculated
 							 */
 							
 							//HRMID tFrom =  tSourceCEP.getRemoteCluster().retrieveAddress();
 							HRMID tTo   = tHRMID;
 							HRMName tNextHop = null;
 							FIBEntry tEntry = null;
 							if(tNegotiator.equals(tHRMIDMapping)) {
 								/*
 								 * Well, in this case, we are just giving the "forwarding cluster" the information, that is has to forward all
 								 * entries for the given higher HRMID
 								 * 
 								 * Furthermore it is not necessary to set the farthest cluster for that entry
 								 */
 								tNextHop = tHRMID;
 								tEntry = mAddressMapping.get(tSourceCEP).new FIBEntry(tTo, tNextHop, ClusterDummy.compare(
 										tNegotiator.getClusterID(),
 										tNegotiator.getToken(),
 										tNegotiator.getHierarchyLevel()),
 										getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel - 1)
 								);
 								/*
 								 * As the cluster probably does not know to which node it has to forward that packet,
 								 * the route to the coordinator of the target cluster is chosen.
 								 */
 								if(tListToTarget != null) {
 									tEntry.setRoutingVectors(tListToTarget);
 									if(getSignatureOfPath(tHRMID) != null) {
 										tEntry.setSignature(getSignatureOfPath(tHRMID));
 									}
 								}
 								IRoutableClusterGraphNode tTargetNode = getFarthestVirtualNodeInDirection(tSourceCEP.getRemoteCluster(), tSourceCEP.getRemoteCluster());
 								ClusterDummy tDummy = null;
 								if(tTargetNode instanceof ICluster) {
 									tDummy = ClusterDummy.compare(((ICluster)tTargetNode).getClusterID(), ((ICluster)tTargetNode).getToken(), ((ICluster)tTargetNode).getHierarchyLevel());
 								}
 								tEntry.setFarthestClusterInDirection(tDummy);
 							} else {
 								/*
 								 * In that case the cluster has to be told to which cluster is has to forward that packet
 								 * 
 								 * First check whether there exists a cluster connection from the source cluster to the forwarding cluster
 								 */
 								if(!tList.isEmpty()) {
 									tNextHop = getHRMController().getRoutableClusterGraph().getDest(tSourceCEP.getRemoteCluster(), tList.get(0)).getHrmID();
 									tEntry = mAddressMapping.get(tSourceCEP).new FIBEntry(tTo, tNextHop, ClusterDummy.compare(
 											((ICluster)getHRMController().getRoutableClusterGraph().getDest(tSourceCEP.getRemoteCluster(), tList.get(0))).getClusterID(),
 											((ICluster)getHRMController().getRoutableClusterGraph().getDest(tSourceCEP.getRemoteCluster(), tList.get(0))).getToken(),
 											((ICluster)getHRMController().getRoutableClusterGraph().getDest(tSourceCEP.getRemoteCluster(), tList.get(0))).getHierarchyLevel()),
 											getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel - 1)
 										);
 									if(tListToTarget != null) {
 										LinkedList<RoutingServiceLinkVector> tVectors = (LinkedList<RoutingServiceLinkVector>) tListToTarget.clone();
 										while(!tVectors.isEmpty() && !tVectors.getFirst().equals(tSourceCEP.getPeerName())) {
 											tVectors.removeFirst();
 										}
 										getLogger().log(this, "Started with initial path " + tListToTarget + " while list is now " + tVectors);
 										// was tListToTarget before, change if something is wrong now
 										tEntry.setRoutingVectors(tVectors);
 										if(getSignatureOfPath(tHRMID) != null) {
 											tEntry.setSignature(getSignatureOfPath(tHRMID));
 										}
 									}
 									IRoutableClusterGraphNode tTargetNode = getFarthestVirtualNodeInDirection(tSourceCEP.getRemoteCluster(), tNegotiator);
 									ClusterDummy tDummy = null;
 									if(tTargetNode instanceof ICluster) {
 										tDummy = ClusterDummy.compare(((ICluster)tTargetNode).getClusterID(), ((ICluster)tTargetNode).getToken(), ((ICluster)tTargetNode).getHierarchyLevel());
 									}
 									tEntry.setFarthestClusterInDirection(tDummy);
 								} else {
 									/*
 									 * That should only happen accidentally. the list to the target cluster does not exist,
 									 * so the source cluster stays where it is.
 									 * 
 									 * If however, a route between the coordinators was calculated, we provide the next hop to that cluster
 									 */
 									LinkedList<RoutingServiceLinkVector> tVectors = null;
 									if(tListToTarget != null && !tListToTarget.isEmpty()) {
 										tVectors = (LinkedList<RoutingServiceLinkVector>) tListToTarget.clone();
 										while(!tVectors.getFirst().equals(tSourceCEP.getPeerName())) {
 											tVectors.removeFirst();
 										}
 									}
 									ClusterDummy tDummy = ClusterDummy.compare(tSourceCEP.getRemoteCluster().getClusterID(),
 											tSourceCEP.getRemoteCluster().getToken(),
 											tSourceCEP.getRemoteCluster().getHierarchyLevel()
 											);
 									tEntry = mAddressMapping.get(tSourceCEP).new FIBEntry(tTo, null, tDummy,
 											getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel - 1)
 										);
 									if(tVectors != null) {
 										tEntry.setRoutingVectors(tVectors);
 										if(getSignatureOfPath(tHRMID) != null) {
 											tEntry.setSignature(getSignatureOfPath(tHRMID));
 										}
 									}
 									tEntry.setFarthestClusterInDirection(tDummy);
 								}
 							}
 							/*
 							 * Now that forwarding entry is saved inside the clients forwarding table
 							 */
 							mAddressMapping.get(tSourceCEP).addForwardingentry(tEntry);
 						}
 					} else {
 						/*
 						 * We are at level one, so now the real forwarding table is calculated
 						 */
 						getLogger().log(this, "Would now distribute addresses for higher clusters");
 						for(HRMID tHRMID : mHigherHRMIDs) {
 							/*
 							 * Find out to which cluster the HRMID is mapped to
 							 */
 							getLogger().log(this, tHRMID + " is mapped to " + (ICluster)getVirtualNodeFromHRMID(tHRMID));
 							/*
 							 * Get that cluster for route calculation
 							 */
 							ICluster tCluster = (ICluster)getVirtualNodeFromHRMID(tHRMID);
 							/*
 							 * As we are on level one, we have to find out, to which node tSourceCEP has to forward the packets for the 
 							 * given higher HRMID
 							 * 
 							 * if one of the clients has the given address, the packet has to be forwarded to that client
 							 */
 							List<Route> tPath=null;
 							if(tCluster!= null) {
 								 tPath = getHRMController().getHRS().getCoordinatorRoutingMap().getRoute(tSourceCEP.getSourceName(), tCluster.getCoordinatorsAddress());
 							}
 							
 							HRMName tDestination = null;
 							/*
 							 * Find out to which routing service address the packet has to be forwarded to
 							 */
 							HRMName tAddress = null;
 							
 							/*
 							 * First try: find out if a path was provided
 							 */
 							
 							LinkedList<RoutingServiceLinkVector> tPolygon = null;
 							if(getPathFromHRMID(tHRMID) != null && !getPathFromHRMID(tHRMID).isEmpty()) {
 								/*
 								 * Nice.
 								 * Get the path the higher entity provided
 								 * 
 								 * Cut it until the path from the node we calculate the route for is reached
 								 */
 								tPolygon = (LinkedList<RoutingServiceLinkVector>) getPathFromHRMID(tHRMID).clone();
 								while(!tPolygon.getFirst().equals(tSourceCEP.getPeerName())) {
 									tPolygon.removeFirst();
 									if(tPolygon.isEmpty()) {
 										break;
 									}
 								}
 								if(!tPolygon.isEmpty()) {
 									tDestination = tPolygon.getFirst().getDestination();
 								}
 							}
 							
 							/*
 							 * Second try: calculate the path ourselves
 							 */
 							
 							if(tDestination == null) {
 								tAddress = (tPath != null && tPath.size() > 0 ? getHRMController().getHRS().getCoordinatorRoutingMap().getDest(tPath.get(0)) :  tSourceCEP.getSourceName());
 							}
 							
 							if(tAddress != null) {
 								if(tSourceCEP.getPeerName().equals(tAddress) && tPath.size() > 1) {
 									tDestination = getHRMController().getHRS().getCoordinatorRoutingMap().getDest(tPath.get(1));
 								} else if(tPath != null && !tPath.isEmpty()) {
 									for(CoordinatorCEPDemultiplexed tCEP : mManagedCluster.getParticipatingCEPs()) {
 										if(tCEP.getPeerName().equals(tAddress)) {
 											tDestination = tCEP.getPeerName();
 										}
 									}
 								}
 								if(tDestination == null) {
 									tDestination = tSourceCEP.getSourceName();
 								}
 							}
 							
 							FIBEntry tEntry = mAddressMapping.get(tSourceCEP).new FIBEntry(
 									tHRMID,
 									tDestination,
 									(tCluster != null ? ClusterDummy.compare(mManagedCluster.getClusterID(), mManagedCluster.getToken(), mManagedCluster.getHierarchyLevel()) : null),
 									getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel - 1));
 							mAddressMapping.get(tSourceCEP).addForwardingentry(tEntry);
 							if(tPolygon != null && !tPolygon.isEmpty()) {
 								tEntry.setRoutingVectors(tPolygon);
 								if(getSignatureOfPath(tHRMID) != null) {
 									tEntry.setSignature(getSignatureOfPath(tHRMID));
 								}
 							}
 							IRoutableClusterGraphNode tTargetNode = getFarthestVirtualNodeInDirection(tSourceCEP.getRemoteCluster(), tSourceCEP.getRemoteCluster());
 							ClusterDummy tDummy = null;
 							if(tTargetNode instanceof ICluster) {
 								tDummy = ClusterDummy.compare(((ICluster)tTargetNode).getClusterID(), ((ICluster)tTargetNode).getToken(), ((ICluster)tTargetNode).getHierarchyLevel());
 							}
 							tEntry.setFarthestClusterInDirection(tDummy);
 						}
 					}
 				}
 				if(mSignatures!= null && !mSignatures.isEmpty()) {
 					for(HRMSignature tSignature : mSignatures) {
 						mAddressMapping.get(tSourceCEP).addApprovedSignature(tSignature);
 					}
 				}
 				mAddressMapping.get(tSourceCEP).addApprovedSignature(getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel - 1));
 				tSourceCEP.write(mAddressMapping.get(tSourceCEP));
 			}
 			if(mHigherHRMIDs != null) {
 				for(HRMID tHRMID : mHigherHRMIDs) {
 	
 					/*
 					 * Now the forwarding entry for the cluster itself is generated:
 					 * Tell the cluster how to reach the given destination
 					 */
 					
 					ICluster tCluster = (ICluster)getVirtualNodeFromHRMID(tHRMID);
 					
 					List<Route> tRoute = getHRMController().getHRS().getCoordinatorRoutingMap().getRoute((HRMName) getHRMController().getPhysicalNode().getRoutingService().getNameFor(getHRMController().getPhysicalNode().getCentralFN()), tCluster.getCoordinatorsAddress());
 					HRMName tNextHop=null;
 					LinkedList<RoutingServiceLinkVector> tPathToTarget = null;
 					if(getPathFromHRMID(tHRMID) != null && !getPathFromHRMID(tHRMID).isEmpty() && getPathFromHRMID(tHRMID).contains(getHRMController().getPhysicalNode().getRoutingService().getNameFor(getHRMController().getPhysicalNode().getCentralFN()))) {
 						
 						LinkedList<RoutingServiceLinkVector> tPolygon = (LinkedList<RoutingServiceLinkVector>) getPathFromHRMID(tHRMID).clone();
 						while(!tPolygon.getFirst().equals(getHRMController().getPhysicalNode().getRoutingService().getNameFor(getHRMController().getPhysicalNode().getCentralFN()))) {
 							tPolygon.removeFirst();
 							if(tPolygon.isEmpty()) {
 								break;
 							}
 						}
 						if(tPolygon.isEmpty()) {
 							getLogger().err(this, "Tried to calculate next hop directly, but no entry found");
 							if(!tRoute.isEmpty()) {
 								tNextHop = getHRMController().getHRS().getCoordinatorRoutingMap().getDest(tRoute.get(0));
 								for(Route tPolygonPath : tRoute) {
 									tPolygon.add(new RoutingServiceLinkVector(tPolygonPath, getHRMController().getHRS().getCoordinatorRoutingMap().getSource(tPolygonPath), getHRMController().getHRS().getCoordinatorRoutingMap().getDest(tPolygonPath)));
 								}
 							}
 						}
 						for(CoordinatorCEPDemultiplexed tCEP : mManagedCluster.getParticipatingCEPs()) {
 							if(tCEP.getPeerName().equals(tPolygon.getFirst().getDestination())) {
 								tNextHop = tPolygon.getFirst().getDestination();
 								tPathToTarget = tPolygon;
 							}
 						}
 						if(tNextHop == null && tPolygon != null && !tPolygon.isEmpty()) {
 							tNextHop = tPolygon.getFirst().getDestination();
 						}
 						tPathToTarget = tPolygon;
 					} else if(!tRoute.isEmpty()) {
 						tNextHop = getHRMController().getHRS().getCoordinatorRoutingMap().getDest(tRoute.get(0));
 					}
 					
 					FIBEntry tEntry = tManagedClusterEnvelope.new FIBEntry(
 							tHRMID,
 							tNextHop,
 							ClusterDummy.compare(mManagedCluster.getClusterID(), mManagedCluster.getToken(), mManagedCluster.getHierarchyLevel()),
 							getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel));
 					if(tPathToTarget != null && !tPathToTarget.isEmpty()) {
 						tEntry.setRoutingVectors(tPathToTarget);
 						if(getSignatureOfPath(tHRMID) != null) {
 							tEntry.setSignature(getSignatureOfPath(tHRMID));
 						}
 					}
 					IRoutableClusterGraphNode tTargetNode = getFarthestVirtualNodeInDirection(getManagedCluster(), tCluster);
 					ClusterDummy tDummy = null;
 					if(tTargetNode instanceof ICluster) {
 						tDummy = ClusterDummy.compare(((ICluster)tTargetNode).getClusterID(), ((ICluster)tTargetNode).getToken(), ((ICluster)tTargetNode).getHierarchyLevel());
 					}
 					tEntry.setFarthestClusterInDirection(tDummy);
 					tManagedClusterEnvelope.addForwardingentry(tEntry);
 				}
 			}
 			
 			// are we at base level?
 			if(mLevel == 1) {
 				
 				for(HRMSignature tSignature : mSignatures) {
 					tManagedClusterEnvelope.addApprovedSignature(tSignature);
 				}
 				tManagedClusterEnvelope.addApprovedSignature(getHRMController().getIdentity().createSignature(getHRMController().getPhysicalNode().toString(), null, mLevel));
 				mManagedCluster.handleTopologyData(tManagedClusterEnvelope);
 			}
 		
 		} catch (AuthenticationException tExc) {
 			getLogger().err(this, "Unable to create signatures, maybe this entity is not allowed to?", tExc);
 		}
 	}
 	
 	private boolean connectToNeighbors(int radius)
 	{
 		for(IRoutableClusterGraphNode tNode : mClustersToNotify) {
 			if(tNode instanceof ICluster && !((ICluster) tNode).isInterASCluster()) {
 				ICluster tCluster = (ICluster)tNode;
 				Name tName = tCluster.getCoordinatorName();
 				synchronized(tCluster) {
 					if(tName == null) {
 						try {
 							tCluster.wait();
 						} catch (InterruptedException tExc) {
 							getLogger().err(this, tCluster + " is skipped on cluster discovery", tExc);
 						}
 					}
 				}
 				
 				if(mConnectedEntities.contains(tName)){
 					getLogger().log(this, " L" + mLevel + "-skipping connection to " + tName + " for cluster " + tNode + " because connection already exists");
 					continue;
 				} else {
 					/*
 					 * was it really this cluster? -> reevaluate
 					 */
 					getLogger().log(this, " L" + mLevel + "-adding connection to " + tName + " for cluster " + tNode);
 					CoordinatorCEPDemultiplexed tCEP = getMultiplexer().addConnection(tCluster, mManagedCluster);
 					//new CoordinatorCEP(mManagedCluster.getCoordinator().getLogger(), mManagedCluster.getCoordinator(), this, false);
 					mConnectedEntities.add(tName);
 				}
 			}
 		}
 		return true;
 	}
 	
 	public String toString()
 	{
 		//return getClass().getSimpleName() + (mManagedCluster != null ? "(" + mManagedCluster.toString() + ")" : "" ) + "TK(" +mToken + ")COORD(" + mCoordinatorSignature + ")@" + mLevel;
 		return "Coordinator " + mGUICoordinatorID + "@L" + (mLevel - 1) + " " + (mManagedCluster != null ? "(ManagedCluster=" + mManagedCluster.getGUIClusterID() + ", ": "(" ) + "Tok=" +mToken + ", CoordSign=" + mCoordinatorSignature + ")";
 	}
 	
 	@Override
 	public void setCoordinatorPriority(long pCoordinatorPriority) {
 		if(mCoordinatorCEP != null && mCoordinatorCEP.getPeerPriority() != pCoordinatorPriority) {
 			getLogger().info(this, "Tried to set a priority that does not correspond with the priority of the concurrent coordinator, wrong connection endpoint?");
 		}
 	}
 
 	@Override
 	public long getNodePriority() {
 		if(mCoordinatorCEP != null) {
 			return mCoordinatorCEP.getPeerPriority();
 		}
 		return 0;
 	}
 
 	@Override
 	public void setPriority(long pPriority) {
 		mBullyPriority = new BullyPriority(pPriority);
 	}
 
 	@Override
 	public HRMController getHRMController() {
 		return mManagedCluster.getHRMController();
 	}
 
 	@Override
 	public LinkedList<CoordinatorCEPDemultiplexed> getParticipatingCEPs() {
 		return mCEPs;
 	}
 
 	public LinkedList<HRMID> getHigherHRMIDs()
 	{
 		return mHigherHRMIDs;
 	}
 	
 	public HashMap<CoordinatorCEPDemultiplexed, TopologyData> getAddressMapping()
 	{
 		return mAddressMapping;
 	}
 	
 	public LinkedList<CoordinatorCEPDemultiplexed> getLowerCEPs()
 	{
 		return mManagedCluster.getParticipatingCEPs();
 	}
 	
 	@Override
 	public void addParticipatingCEP(CoordinatorCEPDemultiplexed pParticipatingCEP) {
 		mCEPs.add(pParticipatingCEP);
 	}
 
 	@Override
 	public Long getClusterID() {
 		return mClusterID;
 	}
 
 	@Override
 	public int getHierarchyLevel() {
 		return mLevel;
 	}
 
 	@Override
 	public Name getCoordinatorName() {
 		return mCoordinatorName;
 	}
 
 	@Override
 	public long getPriority() {
 		return mBullyPriority.getPriority();
 	}
 
 	@Override
 	public String getClusterDescription() {
 		return getClass().getSimpleName() + "(" + mManagedCluster + ")";
 	}
 
 	@Override
 	public void setCoordinatorName(Name pCoordName) {
 		mCoordinatorName = pCoordName;
 	}
 
 	@Override
 	public HRMName getCoordinatorsAddress() {
 		synchronized(this) {
 			while(mCoordinatorAddress == null) {
 				try {
 					wait(1000);
 				} catch (InterruptedException tExc) {
 					getLogger().err(this, "Error while waiting for address", tExc);
 				}
 			}
 			return mCoordinatorAddress;
 		}
 	}
 
 	@Override
 	public void setToken(int pToken) {
 		if(mToken != 0) {
 			Logging.log(this, "Updating token");
 		}
 		mToken = pToken;
 	}
 
 	@Override
 	public int getToken() {
 		return mToken;
 	}
 
 	@Override
 	public LinkedList<ICluster> getNeighbors() {
 		return new LinkedList<ICluster>();
 	}
 
 	@Override
 	public HRMSignature getCoordinatorSignature() {
 		return mCoordinatorSignature;
 	}
 	
 	private void registerFIBEntry(FIBEntry pEntry)
 	{
 		mIDToFIBMapping.put(pEntry.getDestination(), pEntry);
 		ICluster tTargetCluster = null;
 		tTargetCluster = getHRMController().getCluster(pEntry.getNextCluster());
 		map(pEntry.getDestination(), tTargetCluster);
 	}
 	
 	@Override
 	public void handleTopologyData(TopologyData pTopologyData)
 	{
 		/*
 		 * this cluster manager only computes the FIB derived from Radius algorithm
 		 */
 		Node tNode = getHRMController().getPhysicalNode();
 		
 		if(pTopologyData.getPushThrougs() != null && !pTopologyData.getPushThrougs().isEmpty()) {
 			for(FIBEntry tEntry : pTopologyData.getPushThrougs()) {
 				if((tEntry.getDestination() != null && !tEntry.getDestination().equals(new HRMID(0)) ) && tEntry.getNextHop() != null && !tEntry.getNextHop().equals(tNode.getRoutingService().getNameFor(tNode.getCentralFN()))) {
 					getHRMController().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 				}
 			}
 		}
 		
 		if(pTopologyData.getApprovedSignatures() != null) {
 			for(HRMSignature tSignature : pTopologyData.getApprovedSignatures()) {
 				addApprovedSignature(tSignature);
 			}
 		}
 		
 		getLogger().log(this, "Received topology data: " + pTopologyData);
 		mEnvelope = pTopologyData;
 		
 		// update the node's label within GUI
 		tNode.setDecorationValue(tNode.getDecorationValue() + "," + pTopologyData.getHRMID());
 		
 		getHRMController().addIdentification(pTopologyData.getHRMID());
 		if(pTopologyData.getEntries() != null && !pTopologyData.getEntries().isEmpty()) {
 			if(mHigherHRMIDs == null) mHigherHRMIDs = new LinkedList<HRMID>();
 			for(FIBEntry tEntry : pTopologyData.getEntries()) {
 				mHigherHRMIDs.add(tEntry.getDestination());
 				registerFIBEntry(tEntry);
 			}
 			getLogger().log(this, "Have to provide FEs for " + mHigherHRMIDs);
 		}
 		setHRMID(pTopologyData.getHRMID());
 		mManagedCluster.setHRMID(pTopologyData.getHRMID());
 		try {
 			distributeAddresses();
 		} catch (RoutingException tExc) {
 			Logging.err(this, "Error-got routing exception when trying to distribute addresses", tExc);
 		} catch (RequirementsException tExc) {
 			Logging.err(this, "Error-got requirements exception when trying to distribute addresses", tExc);
 		} catch (RemoteException tExc) {
 			Logging.err(this, "Error-got remote exception when trying to distribute addresses", tExc);
 		}
 	}
 
 	@Override
 	public long getHighestPriority() {
 		return mHighestPriority;
 	}
 	
 	public synchronized void interruptElection()
 	{/*
 		if(mElection != null)
 		{
 			Logging.log(this, "interrupting election " + mElection);
 			mElection.interrupt();
 			mElection=null;
 		}
 	*/}
 	
 	public void initiateElection()
 	{/*
 		Logging.log(this, "Initiated election.");
 		if(!electionInProgress()) {
 			mElection = new ElectionProcess(new LinkedList<ICluster>());
 			mElection.start();
 		} else {
 			Logging.log(this, "There is currently an election in progress, please interrupt before starting a new one");
 		}
 	*/}
 
 	public void interpretAnnouncement(BullyAnnounce pAnnounce, CoordinatorCEPDemultiplexed pCEP)
 	{
 		/*
 		 * check whether old priority was lower than new priority
 		 */
 		if(getNodePriority() <= pAnnounce.getSenderPriority()) {
 			/*
 			 * check whether a coordinator is already set
 			 */
 			if(getCoordinatorCEP() != null) {
 				if(getCoordinatorName() != null && !pAnnounce.getSenderName().equals(getCoordinatorName())) {
 					/*
 					 * a coordinator was set earlier -> therefore inter-cluster communicatino is necessary
 					 * 
 					 * find the route from the managed cluster to the cluster this entity got to know the higher cluster
 					 */
 					List<RoutableClusterGraphLink> tClusterList = getHRMController().getRoutableClusterGraph().getRoute(getManagedCluster(), pCEP.getRemoteCluster());
 					if(tClusterList.size() > 0) {
 						if(getHRMController().getRoutableClusterGraph().getDest(pCEP.getRemoteCluster(), tClusterList.get(tClusterList.size() - 1)) instanceof Cluster) {
 							getLogger().warn(this, "Not sending neighbor zone announce because another intermediate cluster has a shorter route to target");
 							if(tClusterList != null) {
 								String tClusterRoute = new String();
 								IRoutableClusterGraphNode tTransitiveElement = getManagedCluster();
 								for(RoutableClusterGraphLink tConnection : tClusterList) {
 									tClusterRoute += tTransitiveElement + "\n";
 									tTransitiveElement = getHRMController().getRoutableClusterGraph().getDest(tTransitiveElement, tConnection);
 								}
 								getLogger().log(this, "cluster route to other entity:\n" + tClusterRoute);
 							}
 						} else {
 							
 							ClusterDummy tDummy = ClusterDummy.compare(getManagedCluster().getClusterID(), getManagedCluster().getToken(), getManagedCluster().getHierarchyLevel());
 							
 							/*
 							 * This is for the new coordinator - he is being notified about the fact that this cluster belongs to another coordinator
 							 * 
 							 * If there are intermediate clusters between the managed cluster of this cluster manager we do not send the announcement because in that case
 							 * the forwarding would get inconsistent
 							 * 
 							 * If this is a rejection the forwarding cluster as to be calculated by the receiver of this neighbor zone announcement
 							 */
 							
 							NeighborClusterAnnounce tOldCovered = new NeighborClusterAnnounce(getCoordinatorName(), getHierarchyLevel(), getCoordinatorSignature(), getCoordinatorsAddress(),getToken(), getCoordinatorsAddress().getAddress().longValue());
 							tOldCovered.setCoordinatorsPriority(getCoordinatorCEP().getPeerPriority());
 							tOldCovered.setNegotiatorIdentification(tDummy);
 							
 							DiscoveryEntry tOldCoveredEntry = new DiscoveryEntry(
 									getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getToken(),
 									getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorName(),
 									getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorsAddress().getAddress().longValue(),
 									getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorsAddress(),
 									getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getHierarchyLevel()
 									);
 							/*
 							 * the forwarding cluster to the newly discovered cluster has to be one level lower so it is forwarded on the correct cluster
 							 * 
 							 * calculation of the predecessor is because the cluster identification on the remote site is multiplexed
 							 */
 							tClusterList = getHRMController().getRoutableClusterGraph().getRoute(getManagedCluster(), getCoordinatorCEP().getRemoteCluster());
 							if(!tClusterList.isEmpty()) {
 								ICluster tPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(getManagedCluster(), tClusterList.get(0));
 								tOldCoveredEntry.setPredecessor(ClusterDummy.compare(tPredecessor.getClusterID(), tPredecessor.getToken(), tPredecessor.getHierarchyLevel()));
 							}
 							tOldCoveredEntry.setPriority(getNodePriority());
 							tOldCoveredEntry.setRoutingVectors(pCEP.getPath(getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorsAddress()));
 							tOldCovered.setCoveringClusterEntry(tOldCoveredEntry);
 //							List<Route> tPathToCoordinator = getCoordinator().getHRS().getCoordinatorRoutingMap().getRoute((HRMName)pCEP.getSourceName(), getCoordinatorsAddress());
 							//tOldCovered.setAnnouncer(getCoordinator().getHRS().getCoordinatorRoutingMap().getDest(tPathToCoordinator.get(0)));
 							pCEP.write(tOldCovered);
 							
 							/*
 							 * now the old cluster is notified about the new cluster
 							 */
 							
 							NeighborClusterAnnounce tNewCovered = new NeighborClusterAnnounce(pAnnounce.getSenderName(), getHierarchyLevel(), pAnnounce.getCoordSignature(), (HRMName)pCEP.getPeerName(), pAnnounce.getToken(), (((HRMName)pCEP.getPeerName()).getAddress().longValue()));
 							tNewCovered.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 							tNewCovered.setNegotiatorIdentification(tDummy);
 							DiscoveryEntry tCoveredEntry = new DiscoveryEntry(
 									pAnnounce.getToken(),
 									pAnnounce.getSenderName(),
 									((HRMName)pCEP.getPeerName()).getAddress().longValue(),
 									(HRMName)pCEP.getPeerName(),
 									getHierarchyLevel()
 									);
 							tCoveredEntry.setRoutingVectors(pCEP.getPath(pCEP.getPeerName()));
 							tNewCovered.setCoveringClusterEntry(tCoveredEntry);
 							tCoveredEntry.setPriority( pAnnounce.getSenderPriority());
 							
 							List<RoutableClusterGraphLink> tClusters = getHRMController().getRoutableClusterGraph().getRoute(getManagedCluster(), pCEP.getRemoteCluster());
 							if(!tClusters.isEmpty()) {
 								ICluster tNewPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(getManagedCluster(), tClusters.get(0));
 								tCoveredEntry.setPredecessor(ClusterDummy.compare(tNewPredecessor.getClusterID(), tNewPredecessor.getToken(), tNewPredecessor.getHierarchyLevel()));
 							}
 							getLogger().warn(this, "Rejecting " + (getCoordinatorCEP().getPeerName()).getDescr() + " in favor of " + pAnnounce.getSenderName());
 							tNewCovered.setRejection();
 							getCoordinatorCEP().write(tNewCovered);
 							for(CoordinatorCEPDemultiplexed tCEP : getParticipatingCEPs()) {
 								if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerName())) {
 									tCEP.setAsParticipantOfMyCluster(true);
 								} else {
 									tCEP.setAsParticipantOfMyCluster(false);
 									
 								}
 							}
 							setToken(pAnnounce.getToken());
 							setCoordinatorCEP(pCEP, pAnnounce.getCoordSignature(), pAnnounce.getSenderName(), pCEP.getPeerName());
 							getHRMController().setClusterWithCoordinator(getHierarchyLevel(), this);
 							getHRMController().addApprovedSignature(pAnnounce.getCoordSignature());
 							getCoordinatorCEP().write(tNewCovered);
 						}
 					}
 				}
 				
 			} else {
 				for(CoordinatorCEPDemultiplexed tCEP : getParticipatingCEPs()) {
 					if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerName())) {
 						tCEP.setAsParticipantOfMyCluster(true);
 					} else {
 						tCEP.setAsParticipantOfMyCluster(false);
 					}
 				}
 				setToken(pAnnounce.getToken());
 				getHRMController().setClusterWithCoordinator(getHierarchyLevel(), this);
 				setCoordinatorCEP(pCEP, pAnnounce.getCoordSignature(), pAnnounce.getSenderName(), (HRMName)pCEP.getPeerName());
 			}
 		} else {
 			/*
 			 * this part is for the coordinator that intended to announce itself -> send rejection and send acting coordinator along with
 			 * the announcement that is just gained a neighbor zone
 			 */
 			
 			ClusterDummy tDummy = ClusterDummy.compare(getManagedCluster().getClusterID(), getManagedCluster().getToken(), getManagedCluster().getHierarchyLevel());
 			NeighborClusterAnnounce tUncoveredAnnounce = new NeighborClusterAnnounce(getCoordinatorName(), getHierarchyLevel(), getCoordinatorSignature(), getCoordinatorsAddress(), getToken(), getCoordinatorsAddress().getAddress().longValue());
 			tUncoveredAnnounce.setCoordinatorsPriority(getCoordinatorCEP().getPeerPriority());
 			/*
 			 * the routing service address of the announcer is set once the neighbor zone announce arrives at the rejected coordinator because this
 			 * entity is already covered
 			 */
 			
 			tUncoveredAnnounce.setNegotiatorIdentification(tDummy);
 			
 			DiscoveryEntry tUncoveredEntry = new DiscoveryEntry(
 					getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getToken(),
 					getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorName(),
 					getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorsAddress().getAddress().longValue(),
 					getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorsAddress(),
 					getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getHierarchyLevel()
 					);
 			List<RoutableClusterGraphLink> tClusterList = getHRMController().getRoutableClusterGraph().getRoute(getManagedCluster(), pCEP.getRemoteCluster());
 			if(!tClusterList.isEmpty()) {
 				ICluster tPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(getManagedCluster(), tClusterList.get(0));
 				tUncoveredEntry.setPredecessor(ClusterDummy.compare(tPredecessor.getClusterID(), tPredecessor.getToken(), tPredecessor.getHierarchyLevel()));
 			}
 			tUncoveredEntry.setPriority(getNodePriority());
 			tUncoveredEntry.setRoutingVectors(pCEP.getPath(getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel()).getCoordinatorsAddress()));
 			tUncoveredAnnounce.setCoveringClusterEntry(tUncoveredEntry);
 			getLogger().warn(this, "Rejecting " + (getCoordinatorCEP().getPeerName()).getDescr() + " in favour of " + pAnnounce.getSenderName());
 			tUncoveredAnnounce.setRejection();
 			pCEP.write(tUncoveredAnnounce);
 			
 			/*
 			 * this part is for the acting coordinator, so NeighborZoneAnnounce is sent in order to announce the cluster that was just rejected
 			 */
 			
 			NeighborClusterAnnounce tCoveredAnnounce = new NeighborClusterAnnounce(pAnnounce.getSenderName(), getHierarchyLevel(), pAnnounce.getCoordSignature(), pCEP.getPeerName(), pAnnounce.getToken(), (pCEP.getPeerName()).getAddress().longValue());
 			tCoveredAnnounce.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 			
 //			List<Route> tPathToCoordinator = getCoordinator().getHRS().getCoordinatorRoutingMap().getRoute(pCEP.getSourceName(), pCEP.getPeerName());
 			
 			//tCoveredAnnounce.setAnnouncer(getCoordinator().getHRS().getCoordinatorRoutingMap().getDest(tPathToCoordinator.get(0)));
 			tCoveredAnnounce.setNegotiatorIdentification(tDummy);
 			DiscoveryEntry tCoveredEntry = new DiscoveryEntry(
 					pAnnounce.getToken(),
 					pAnnounce.getSenderName(),
 					(pCEP.getPeerName()).getAddress().longValue(),
 					pCEP.getPeerName(),
 					getHierarchyLevel()
 					);
 			tCoveredEntry.setRoutingVectors(pCEP.getPath(pCEP.getPeerName()));
 			tCoveredAnnounce.setCoveringClusterEntry(tCoveredEntry);
 			tCoveredEntry.setPriority(pAnnounce.getSenderPriority());
 			tCoveredAnnounce.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 			
 			List<RoutableClusterGraphLink> tClusters = getHRMController().getRoutableClusterGraph().getRoute(getManagedCluster(), getCoordinatorCEP().getRemoteCluster());
 			if(!tClusters.isEmpty()) {
 				ICluster tNewPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(getManagedCluster(), tClusters.get(0));
 				tUncoveredEntry.setPredecessor(ClusterDummy.compare(tNewPredecessor.getClusterID(), tNewPredecessor.getToken(), tNewPredecessor.getHierarchyLevel()));
 			}
 			getLogger().log(this, "Coordinator CEP is " + getCoordinatorCEP());
 			getCoordinatorCEP().write(tCoveredAnnounce);
 		}
 	}
 	
 	private ICluster addAnnouncedCluster(NeighborClusterAnnounce pAnnounce, CoordinatorCEPDemultiplexed pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		NeighborCluster tCluster = null;
 		if(pAnnounce.isAnnouncementFromForeign())
 		{
 			tCluster = new NeighborCluster(
 					pAnnounce.getCoordAddress().getAddress().longValue(),
 					pAnnounce.getCoordinatorName(),
 					pAnnounce.getCoordAddress(),
 					pAnnounce.getToken(),
 					mLevel + 1,
 					mManagedCluster.getHRMController());
 			getHRMController().setSourceIntermediateCluster(tCluster, getHRMController().getSourceIntermediate(this));
 			((NeighborCluster)tCluster).addAnnouncedCEP(pCEP);
 			tCluster.setToken(pAnnounce.getToken());
 			tCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 			//mManagedCluster.addNeighborCluster(tCluster);
 		} else {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 		}
 		if(pAnnounce.getCoordinatorName() != null) {
 			RoutingService tRS = (RoutingService)getHRMController().getPhysicalNode().getRoutingService();
 			if(! tRS.isKnown(pAnnounce.getCoordinatorName())) {
 				try {
 					getHRMController().getHRS().registerNode(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 				} catch (RemoteException tExc) {
 					getLogger().err(this, "Unable to register " + pAnnounce.getCoordinatorName() + " at higher entity", tExc);
 				}
 				
 			}
 		}
 		return tCluster;
 	}
 	
 	@Override
 	public void handleAnnouncement(NeighborClusterAnnounce	pAnnounce, CoordinatorCEPDemultiplexed pCEP)
 	{		
 		if(pAnnounce.getCoveringClusterEntry() != null) {
 //			Cluster tForwardingCluster = null;
 			
 			if(pAnnounce.isRejected()) {
 //				Cluster tMultiplex = getManagedCluster();
 //				tForwardingCluster = (Cluster) ((Cluster) getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster()) == null ? pCEP.getRemoteCluster() : getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster())) ;
 				//pAnnounce.setAnnouncer( (tForwardingCluster.getCoordinatorsAddress() != null ? tForwardingCluster.getCoordinatorsAddress() : null ));
 				getLogger().log(this, "Removing " + this + " as participating CEP from " + this);
 				getParticipatingCEPs().remove(this);
 			}
 			if(pAnnounce.getCoordinatorName() != null) {
 				RoutingService tRS = (RoutingService)getHRMController().getPhysicalNode().getRoutingService();
 				if(! tRS.isKnown(pAnnounce.getCoordinatorName())) {
 					
 					try {
 						getHRMController().getHRS().registerNode(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 					} catch (RemoteException tExc) {
 						getHRMController().getLogger().err(this, "Unable to register " + pAnnounce.getCoordinatorName() + " at name mapping service", tExc);
 					}
 				}
 			}
 			try {
 				pCEP.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry());
 			} catch (PropertyException tExc) {
 				getLogger().log(this, "Unable to fulfill requirements");
 			}
 			getLogger().log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 		} else {
 			getLogger().log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 		}
 	}
 
 	@Override
 	public CoordinatorCEPDemultiplexed getCoordinatorCEP() {
 		return mCoordinatorCEP;
 	}
 
 	@Override
 	public void setCoordinatorCEP(CoordinatorCEPDemultiplexed pCoord, HRMSignature pCoordSignature, Name pCoordName, HRMName pAddress) {
 		Logging.log(this, "announcement number " + (++mReceivedAnnounces) + ": Setting Coordinator " + pCoord + " with name " + pCoordName + " with routing address " + pAddress);
 		Logging.log(this, "previous coordinator was " + mCoordinatorCEP + " with name " + mCoordinatorName);
 		mCoordinatorCEP = pCoord;
 		mCoordinatorSignature = pCoordSignature;
 		mCoordinatorName = pCoordName;
 		synchronized(this) {
 			mCoordinatorAddress = pAddress;
 			notifyAll();
 		}
 		getHRMController().getPhysicalNode().setDecorationValue("(" + pCoordSignature + ")");
 //		LinkedList<CoordinatorCEP> tEntitiesToNotify = new LinkedList<CoordinatorCEP> ();
 		if(pCoordSignature != null) {
 			for(IRoutableClusterGraphNode tNode: getHRMController().getRoutableClusterGraph().getNeighbors(getManagedCluster())) {
 				if(tNode instanceof ICluster && !((ICluster) tNode).isInterASCluster()) {
 					for(CoordinatorCEPDemultiplexed tCEP : mCEPs) {
 						if(((ICluster)tNode).getCoordinatorsAddress().equals(tCEP.getPeerName()) && !tCEP.isPartOfMyCluster()) {
 							getLogger().info(this, "Informing " + tCEP + " about existence of neighbor zone ");
 							NeighborClusterAnnounce tAnnounce = new NeighborClusterAnnounce(pCoordName, getHierarchyLevel(), pCoordSignature, pAddress, getToken(), pAddress.getAddress().longValue());
 							tAnnounce.setCoordinatorsPriority(getCoordinatorCEP().getPeerPriority());
 							LinkedList<RoutingServiceLinkVector> tVectorList = tCEP.getPath(pAddress);
 							tAnnounce.setRoutingVectors(tVectorList);
 							tAnnounce.setNegotiatorIdentification(ClusterDummy.compare(getManagedCluster().getClusterID(), getManagedCluster().getToken(), getManagedCluster().getHierarchyLevel()));
 							tCEP.write(tAnnounce);
 						}
 						getLogger().log(this, "Informed " + tCEP + " about new neighbor zone");
 					}
 				}
 			}
 		}
 		getHRMController().addApprovedSignature(pCoordSignature);
 		if(mReceivedAnnouncements != null) {
 			for(NeighborClusterAnnounce tAnnounce : mReceivedAnnouncements) {
 				pCoord.write(tAnnounce);
 			}
 		}
 		
 	}
 
 	@Override
 	public void addNeighborCluster(ICluster pNeighbor) {
 		/*
 		 * cluster manager does not need neighbors
 		 */
 		//TODO: remove this
 	}
 
 	@Override
 	public void setHRMID(HRMID pHRMID) {
 		mHRMID = pHRMID;
 	}
 
 	@Override
 	public HRMID getHrmID() {
 		return mHRMID;
 	}
 	
 //	public void includeCluster(NeighborCluster pAttached)
 //	{
 //		if(!mTokens.contains(pAttached.getToken())) {
 //			mTokens.add(pAttached.getToken());
 //		}
 //	}
 
 	@Override
 	public void update(Observable pO, Object pArg) {
 	}
 
 	@Override
 	public void setHighestPriority(long pHighestPriority) {
 		mHighestPriority = pHighestPriority;
 	}
 
 	public void sendClusterBroadcast(Serializable pData, LinkedList<CoordinatorCEPDemultiplexed> pAlreadyInformed)
 	{
 		if(pData instanceof BullyPriorityUpdate)
 		{
 			Logging.log(this, "Will send priority update to" + mCEPs);
 		}
 		LinkedList<CoordinatorCEPDemultiplexed> tInformedCEPs = null;
 		if(pAlreadyInformed != null) {
 			tInformedCEPs= pAlreadyInformed;
 		} else {
 			tInformedCEPs = new LinkedList<CoordinatorCEPDemultiplexed>(); 
 		}
 		try {
 			for(CoordinatorCEPDemultiplexed tCEP : mCEPs)
 			{
 				if(!tInformedCEPs.contains(tCEP))
 				{
 					tCEP.write(pData);
 					tInformedCEPs.add(tCEP);
 				}
 			}
 		} catch (ConcurrentModificationException tExc) {
 			Logging.warn(this, "change in cluster CEP number occured, sending message to new peers", tExc);
 			sendClusterBroadcast(pData, tInformedCEPs);
 		}
 	}
 
 	@Override
 	public Namespace getNamespace() {
 		return new Namespace("clustermanager");
 	}
 	
 	@Override
 	public boolean equals(Object pObj)
 	{
 		if(pObj instanceof Cluster) {
 			return false;
 		}
 		if(pObj instanceof ICluster) {
 			ICluster tCluster = (ICluster) pObj;
 			if(tCluster.getClusterID().equals(getClusterID()) &&
 					tCluster.getToken() == getToken() &&
 					tCluster.getHierarchyLevel() == getHierarchyLevel()) {
 				return true;
 			} else if(tCluster.getClusterID().equals(getClusterID()) && tCluster.getHierarchyLevel() == getHierarchyLevel()) {
 				Logging.log(this, "compared to " + pObj + "is false");
 				return false;
 			} else if (tCluster.getClusterID().equals(getClusterID())) {
 				return false;
 			}
 		}
 		return false;
 	}	
 	
 	@Override
 	public boolean isInterASCluster()
 	{
 		return false;
 	}
 
 	@Override
 	public void setInterASCluster()
 	{
 		
 	}
 
 	@Override
 	public LinkedList<CoordinatorCEPDemultiplexed> getLaggards()
 	{
 		return null;
 	}
 
 	@Override
 	public void addLaggard(CoordinatorCEPDemultiplexed pCEP) 
 	{
 		
 	}
 
 	@Override
 	public CoordinatorCEPDemultiplexed getNegotiatorCEP()
 	{
 		return null;
 	}
 
 	@Override
 	public void setNegotiatorCEP(CoordinatorCEPDemultiplexed pCEP)
 	{
 		
 	}
 	
 	public Cluster getManagedCluster()
 	{
 		return mManagedCluster;
 	}
 	
 	@Override
 	public TopologyData getTopologyData()
 	{
 		return mEnvelope;
 	}
 	
 //	public int getBorderToken(HRMID pHRMID)
 //	{
 //		if(mIDToFIBMapping.containsKey(pHRMID)) {
 //			return mIDToFIBMapping.get(pHRMID).getBorderIdentification();
 //		}
 //		return 0;
 //	}
 //	
 	private HRMSignature getSignatureOfPath(HRMID tHRMID)
 	{
 		if(mIDToFIBMapping.containsKey(tHRMID) && mIDToFIBMapping.get(tHRMID).getSignature() != null) {
 			return mIDToFIBMapping.get(tHRMID).getSignature();
 		} else {
 			return null;
 		}
 		
 	}
 	
 	public LinkedList<RoutingServiceLinkVector> getPathFromHRMID(HRMID pID)
 	{
 		if(mIDToFIBMapping.containsKey(pID) && mIDToFIBMapping.get(pID).getRouteToTarget() != null) {
 			return mIDToFIBMapping.get(pID).getRouteToTarget();
 		} else if(mAddressToPathMapping != null && mAddressToPathMapping.containsKey(pID)) {
 				return (LinkedList<RoutingServiceLinkVector>) mAddressToPathMapping.get(pID).clone();
 			} else {
 				return null;
 			}
 	}
 	
 	private void map(HRMID pHRMID, IRoutableClusterGraphNode pToVirtualNode)
 	{
 		getLogger().log(this, "Mapping HRMID " + pHRMID + " to " + pToVirtualNode);
 		// Check if this is safe
 		if(mAddressToClusterMapping.containsKey(pHRMID)) {
 			mAddressToClusterMapping.remove(pHRMID);
 		}
 		if(pToVirtualNode instanceof ICluster) {
 			mAddressToClusterMapping.put(pHRMID, getHRMController().getCluster((ICluster)pToVirtualNode));
 		} else {
 			mAddressToClusterMapping.put(pHRMID, pToVirtualNode);
 		}
 	}
 	
 	public HashMap<HRMID, IRoutableClusterGraphNode> getMappings()
 	{
 		return mAddressToClusterMapping;
 	}
 	
 	public IRoutableClusterGraphNode getVirtualNodeFromHRMID(HRMID pHRMID)
 	{
 		IRoutableClusterGraphNode tNode = mAddressToClusterMapping.get(pHRMID);
 		if(tNode != null) {
 			/*
 			 * OK: node was found
 			 */
 		} else {
 			tNode = mIDToFIBMapping.get(pHRMID).getNextCluster();
 		}
 		return tNode;
 	}
 	
 	public void handleRouteRequest(RouteRequest pRequest, IRoutableClusterGraphNode pSourceCluster)
 	{
 		final RouteRequest tParameterRouteRequest = pRequest;
 		final IRoutableClusterGraphNode tSourceCluster = pSourceCluster;
 		final Coordinator tManager = this;
 		
 		if(pRequest.getResult() != null && pRequest.getResult().equals(ResultType.UNFEASIBLE)) {
 			CoordinatorCEPDemultiplexed tCEP = mRouteRequestDispatcher.get(tParameterRouteRequest.getSession());
 			tParameterRouteRequest.setAnswer();
 			tCEP.write(tParameterRouteRequest);
 			return;
 		}
 		
 		new Thread () {
 			public void run() {
 				int tDescendingDifference = 0;
 				if(tParameterRouteRequest.getTarget() instanceof HRMID) {
 					tDescendingDifference = (((HRMID)tParameterRouteRequest.getTarget())).getDescendingDifference(tSourceCluster.getHrmID());
 				}
 				
 				/*
 				 * Beginning of the recursion
 				 */
 				if(tDescendingDifference >= mLevel) {
 					RouteRequest tRequest = tParameterRouteRequest.clone();
 					getCoordinatorCEP().write(tRequest);
 					synchronized(tRequest) {
 						try {
 							tRequest.wait();
 						} catch (InterruptedException tExc) {
 							getLogger().err(this, "Error when waiting for " + tRequest, tExc);
 						}
 					}
 					Logging.log(tManager, "Come back of " + tRequest);
 					try {
 						if(tRequest.getRoutingVectors() != null) {
 							for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 								tParameterRouteRequest.addRoutingVector(tVector);
 							}
 						}
 					} catch (ConcurrentModificationException tExc) {
 						if(tRequest.getRoutingVectors() != null) {
 							for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 								if(tParameterRouteRequest.getRoutingVectors().contains(tVector)) {
 									tParameterRouteRequest.addRoutingVector(tVector);
 								}
 							}
 						}
 					}
 					
 					tParameterRouteRequest.setAnswer();
 					tParameterRouteRequest.setResult(tRequest.getResult());
 					CoordinatorCEPDemultiplexed tCEP = mRouteRequestDispatcher.get(tParameterRouteRequest.getSession());
 					tCEP.write(tParameterRouteRequest);
 				} else {
 					/*
 					 * end of the recursion
 					 */
 					getLogger().log(tManager, "Reached highest cluster");
 					final HRMID tLocalTarget = ((HRMID) (tParameterRouteRequest.getTarget())).clone();
 					for(int i = 0; i < mLevel-1; i++) {
 						tLocalTarget.setLevelAddress(i, BigInteger.valueOf(0));
 					}
 					LinkedList<IRoutableClusterGraphNode> tNodesToIgnore = new LinkedList<IRoutableClusterGraphNode>();
 					
 					List<RoutableClusterGraphLink> tClusterConnection = null;; 
 					try {
 						Logging.log(tManager, "Invalidating nodes " + tNodesToIgnore);
 						tClusterConnection = getHRMController().getRoutableClusterGraph().getRouteWithInvalidatedNodes(tSourceCluster, getVirtualNodeFromHRMID(tLocalTarget), tNodesToIgnore);
 						LinkedList<ICluster> tClusters = new LinkedList<ICluster>();
 						ICluster tLastCluster = (ICluster) tSourceCluster;
 						if(tClusterConnection != null && !tClusterConnection.isEmpty()) {
 							for(RoutableClusterGraphLink tConnection : tClusterConnection) {
 								tClusters.add(tLastCluster);
 								tLastCluster = (ICluster) getHRMController().getRoutableClusterGraph().getDest(tLastCluster, tConnection);
 							}
 						} else {
 							tParameterRouteRequest.setResult(ResultType.UNFEASIBLE);
 							CoordinatorCEPDemultiplexed tCEP = mRouteRequestDispatcher.get(tParameterRouteRequest.getSession());
 							tCEP.write(tParameterRouteRequest);
 							return;
 						}
 						
 						tClusters.add(tLastCluster);
 						
 						/*
 						 * find descending difference from left to right
 						 */
 						Logging.log(tManager, "Got cluster connection " + tClusterConnection);
 						Logging.log(tManager, "Got cluster connection over " + tClusters);
 						
 						Route tRoute = new Route();
 						tLastCluster = (ICluster) tSourceCluster;
 						for(ICluster tCluster : tClusters) {
 							List<Route> tPath = getHRMController().getHRS().getCoordinatorRoutingMap().getRoute(tLastCluster.getCoordinatorsAddress(), tCluster.getCoordinatorsAddress());
 							if(!tPath.isEmpty() && tPath.size() == 1 && !tLastCluster.getCoordinatorsAddress().equals(tCluster.getCoordinatorsAddress())) {
 								for(Route tRoutePart : tPath) {
 									tRoute.addAll(tRoutePart);
 									RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(tRoutePart, getHRMController().getHRS().getCoordinatorRoutingMap().getSource(tRoutePart), getHRMController().getHRS().getCoordinatorRoutingMap().getDest(tRoutePart));
 									tParameterRouteRequest.addRoutingVector(tVector);
 								}
 							} else if(tPath.isEmpty() && !tLastCluster.getCoordinatorsAddress().equals(tCluster.getCoordinatorsAddress()) || !tPath.isEmpty() && tPath.size() > 1 && !tLastCluster.getCoordinatorsAddress().equals(tCluster.getCoordinatorsAddress())) {
 								Logging.err(tManager, "Unable to calculate a route segment path from " + tLastCluster.getCoordinatorsAddress() + " to " + tCluster.getCoordinatorsAddress());
 								for(CoordinatorCEPDemultiplexed tCEP : getManagedCluster().getParticipatingCEPs()) {
 									if(tCEP.getRemoteCluster().equals(tLastCluster)) {
 										getLogger().log(tManager, "About to ask route from " + tLastCluster + " to " + tCluster);
 										RouteRequest tRequest = new RouteRequest(tLastCluster.getCoordinatorsAddress(), tCluster.getCoordinatorsAddress(), null, 0);
 										tRequest.addRequiredCluster(new ClusterDummy(tLastCluster.getToken(), tLastCluster.getClusterID(), tLastCluster.getHierarchyLevel()));
 
 										tCEP.write(tRequest);
 										synchronized(tRequest) {
 											if(!tRequest.isAnswer()) {
 												try {
 													tRequest.wait();
 												} catch (InterruptedException tExc) {
 													getLogger().err(this, "Error when waiting for come back of route request " + tRequest, tExc);
 												}
 											}
 										}
 										if(tRequest.getRoutingVectors() != null) {
 											for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 												tParameterRouteRequest.addRoutingVector(tVector);
 											}
 										}	
 									}
 								}
 							}
 							Logging.log(tManager, "Calculated route from " + tLastCluster.getCoordinatorsAddress() + " to " + tCluster.getCoordinatorsAddress() + ":" + tPath);
 							tLastCluster = tCluster;
 						}
 						Logging.log(tManager, "Concurrent route request is " + tParameterRouteRequest);
 						if(((HRMID)tParameterRouteRequest.getTarget()).getLevelAddress(mLevel -1 ) != BigInteger.valueOf(0)) {
 							CoordinatorCEPDemultiplexed tCEP = mManagedCluster.getCEPOfCluster((ICluster) getVirtualNodeFromHRMID(tLocalTarget));
 							RouteRequest tRequest = new RouteRequest(tCEP.getPeerName(), tParameterRouteRequest.getTarget(), tParameterRouteRequest.getDescription(), tParameterRouteRequest.getSession());
 							tCEP.write(tRequest);
 							synchronized(tRequest) {
 								try {
 									tRequest.wait();
 								} catch (InterruptedException tExc) {
 									Logging.err(tManager, "Error while waiting for", tExc);
 								}
 							}
 							Logging.log(tManager, "Come back of " + tRequest);
 							if(tRequest.getRoutingVectors() != null) {
 								for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 									tParameterRouteRequest.addRoutingVector(tVector);
 								}
 							}
 							tParameterRouteRequest.setResult(tRequest.getResult());
 							Logging.log(tManager, "Route request is now " + tParameterRouteRequest);
 						}
 						long tRequestSession = tParameterRouteRequest.getSession();
 						Logging.log(tManager, "registered requests for " + mRouteRequestDispatcher + ": ");
 						for(Long tLong : mRouteRequestDispatcher.keySet()) {
 							Logging.log(tLong + " is pointing at " + mRouteRequestDispatcher.get(tLong));
 						}
 						CoordinatorCEPDemultiplexed tCEP = mRouteRequestDispatcher.get(tRequestSession);
 						tParameterRouteRequest.setAnswer();
 						tCEP.write(tParameterRouteRequest);
 					} catch (NullPointerException tExc) {
 						Logging.err(tManager, "Error when trying to calculate route with invalidated node", tExc);
 					}
 				}
 			}
 		}.start();
 	}
 
 	@Override
 	public int getSerialisedSize()
 	{
 		return 0;
 	}
 
 	@Override
 	public CoordinatorCEPMultiplexer getMultiplexer() {
 		return getHRMController().getMultiplexerOnLevel(mLevel);
 	}
 
 	public void registerRouteRequest(Long pSession, CoordinatorCEPDemultiplexed pCEP)
 	{
 		if( mRouteRequestDispatcher == null ) {
 			mRouteRequestDispatcher = new HashMap<Long, CoordinatorCEPDemultiplexed>();
 		}
 		Logging.log(this, "registered " + pSession + " with " + pCEP);
 		mRouteRequestDispatcher.put(pSession, pCEP);
 	}
 	
 	public Logger getLogger()
 	{
 		return getHRMController().getLogger();
 	}
 }
