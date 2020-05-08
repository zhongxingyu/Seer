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
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.FIBEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.addressing.AssignHRMID;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyPriorityUpdate;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterName;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.HierarchyLevel;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.HRMGraphNodeName;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NeighborCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.RoutableClusterGraphLink;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.ui.Logging;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 
 /**
  * This class is used for a coordinator instance and can be used on all hierarchy levels.
  * A cluster's elector instance is responsible for creating instances of this class.
  */
 public class Coordinator implements ICluster, HRMEntity
 {
 	/**
 	 * This is the GUI specific cluster counter, which allows for globally unique cluster IDs.
 	 * It's only used within the GUI. 	
 	 */
 	private static int sGUICoordinatorID = 0;
 	
 	/**
 	 * The hierarchy level on which this coordinator is located.
 	 */
 	private HierarchyLevel mHierarchyLevel; //TODO: remove and use the level from the cluster instance
 
 	/**
 	 * List for identification of entities this cluster manager is connected to
 	 */
 	private LinkedList<Name> mConnectedEntities = new LinkedList<Name>();
 	
 	private HRMSignature mSignature = null;
 	
 	private HRMID mHRMID = null;
 	private Cluster mManagedCluster;
 	private HashMap<CoordinatorCEPChannel, TopologyData> mAddressMapping = null;
 	private LinkedList<CoordinatorCEPChannel> mCEPs = null;
 	private CoordinatorCEPChannel mCoordinatorCEP = null;
 	private HRMSignature mCoordinatorSignature = null;
 	private Name mCoordinatorName = null;
 	private HRMName mCoordinatorAddress = null;
 	private int mToken;
 	private BullyPriority mHighestPriority = null;
 	private List<HRMGraphNodeName> mClustersToNotify;
 	private LinkedList<Long> mBouncedAnnounces = new LinkedList<Long>();
 	private int mReceivedAnnounces = 0;
 //	private LinkedList<Name> mIgnoreOnAddressDistribution = null;
 	private Long mClusterID;
 	private LinkedList<HRMID> mHigherHRMIDs = null;
 	private TopologyData mTopologyData = null;
 	private HashMap<HRMID, FIBEntry> mIDToFIBMapping = new HashMap<HRMID, FIBEntry>();
 	private LinkedList<NeighborClusterAnnounce> mReceivedAnnouncements;
 //	private HashMap<Long, CoordinatorCEPChannel> mRouteRequestDispatcher;
 	private HashMap<HRMID, LinkedList<RoutingServiceLinkVector>> mAddressToPathMapping;
 	
 	/**
 	 * This is the GUI specific coordinator ID. It is used to allow for an easier debugging.
 	 */
 	private int mGUICoordinatorID = sGUICoordinatorID++;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6824959379284820010L;
 	
 	/**
 	 * The constructor for a cluster object. Usually, it is called by a cluster's elector instance
 	 * 
 	 * @param pCluster the parent cluster instance
 	 */
 	@SuppressWarnings("unused")
 	public Coordinator(Cluster pCluster)
 	{
 		mManagedCluster = pCluster;
 		
 		// clone the HRMID of the managed cluster because it can already contain the needed HRMID prefix address
 		mHRMID =  mManagedCluster.getHRMID().clone();
 		
 		mHierarchyLevel = mManagedCluster.getHierarchyLevel();
 		mClusterID = pCluster.getClusterID();
 		mAddressMapping = new HashMap<CoordinatorCEPChannel, TopologyData>();
 		mCEPs = new LinkedList<CoordinatorCEPChannel>();
 		
 		// creates the coordinator signature
 		mSignature = getHRMController().createCoordinatorSignature(this);
 
 		// register itself as coordinator for the managed cluster
 		mManagedCluster.setCoordinator(this);
 
 		// register at HRMController's internal database
 		getHRMController().registerCoordinator(this);
 
 		Logging.log(this, "CREATED");
 
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			Logging.log(this, "   ..starting address distribution");
 			signalAddressDistribution();
 		}
 	}
 	
 	/**
 	 * Returns the coordinator HRMSignature
 	 * 
 	 * @return the signature
 	 */
 	public HRMSignature getSignature()
 	{
 		return mSignature;
 	}
 
 	public void storeAnnouncement(NeighborClusterAnnounce pAnnounce)
 	{
 		Logging.log(this, "Storing " + pAnnounce);
 		if(mReceivedAnnouncements == null) {
 			mReceivedAnnouncements = new LinkedList<NeighborClusterAnnounce>();
 		}
 		pAnnounce.setNegotiatorIdentification(new ClusterName(mManagedCluster.getToken(), mManagedCluster.getClusterID(), mManagedCluster.getHierarchyLevel()));
 		mReceivedAnnouncements.add(pAnnounce);
 	}
 	
 	public LinkedList<Long> getBounces()
 	{
 		return mBouncedAnnounces;
 	}
 	
 	public void clusterCoordinators()
 	{
 		Logging.log(this, "CLUSTERING STARTED on hierarchy level " + getHierarchyLevel().getValue() + ", will connect to " + mManagedCluster.getNeighbors());
 		
 		// are we already at the highest hierarchy level?
 		if (getHierarchyLevel().isHighest()){
 			Logging.warn(this,  "CLUSTERING SKIPPED, no clustering on highest hierarchy level " + getHierarchyLevel().getValue() + " needed");
 			return;
 		}
 			
 		int tRadius = HRMConfig.Routing.EXPANSION_RADIUS;
 
 		Logging.log(this, "Radius is " + tRadius);
 
 		BFSDistanceLabeler<HRMGraphNodeName, RoutableClusterGraphLink> tBreadthFirstSearch = new BFSDistanceLabeler<HRMGraphNodeName, RoutableClusterGraphLink>();
 
 		for(int i = 1; i <= tRadius; i++) {
 			
 			String tString = new String(">>> Expanding to radius (" + i + "/" + tRadius + ", possible clusters:");
 			for(Cluster tCluster : getHRMController().getRoutingTargetClusters()) {
 				if(tCluster.getHierarchyLevel().getValue() == getHierarchyLevel().getValue() - 1) {
 					tString += "\n" + tCluster.toString();
 				}
 			}
 			Logging.log(this, tString);
 			
 			// compute the distances of all the node from the managed cluster
 			tBreadthFirstSearch.labelDistances(getHRMController().getRoutableClusterGraph().getGraphForGUI(), mManagedCluster);
 			
 			mClustersToNotify = tBreadthFirstSearch.getVerticesInOrderVisited();
 			List<HRMGraphNodeName> tClustersToNotify = new LinkedList<HRMGraphNodeName>(); 
 			Logging.log(this, "Clusters remembered for notification: " + mClustersToNotify);
 			for(HRMGraphNodeName tNode : mClustersToNotify) {
 				if(!((ICluster)tNode).isInterASCluster()) {
 					if(tNode instanceof Cluster && i == 1) {
 						tClustersToNotify.add(tNode);
 					} else if (tNode instanceof NeighborCluster && ((NeighborCluster)tNode).getClusterDistanceToTarget() <= i && ((NeighborCluster)tNode).getClusterDistanceToTarget() != 0 && !mConnectedEntities.contains(((NeighborCluster)tNode).getCoordinatorName())) {
 						tClustersToNotify.add(tNode);					
 					}
 				}
 			}
 			mClustersToNotify = tClustersToNotify;
 			Logging.log(this, "clusters that are remaining for this round: " + mClustersToNotify);
 			connectToNeighbors(i);
 		}
 		/*
 		for(CoordinatorCEP tCEP : mCEPs) {
 			tCEP.write(new BullyElect(mManagedCluster.getPriority(), pLevel, getCoordinator().getReferenceNode().getCentralFN().getName(), null));
 		}
 		*/
 		Logging.log(this, "has a total of the following connections to higher candidates" + mCEPs);
 	}
 	
 	public LinkedList<RoutingServiceLinkVector> getPathToCoordinator(ICluster pSourceCluster, ICluster pDestinationCluster)
 	{
 		if(pDestinationCluster instanceof Cluster && ((Cluster)pDestinationCluster).isInterASCluster()) {
 			Logging.info(this, "Omitting " + pDestinationCluster + " because it is an inter AS cluster");
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
 	
 	private HRMGraphNodeName getFarthestVirtualNodeInDirection(HRMGraphNodeName pSource, HRMGraphNodeName pTarget)
 	{
 		List<RoutableClusterGraphLink> tList = getHRMController().getRoutableClusterGraph().getRoute(pSource, pTarget);
 
 		//ICluster tFarthestCluster = null;
 		HRMGraphNodeName tResult = pSource;
 		try {
 			int tDistance = 0;
 			if(tList.size() > HRMConfig.Routing.EXPANSION_RADIUS) {
 				while(tDistance != HRMConfig.Routing.EXPANSION_RADIUS) {
 					tResult = getHRMController().getRoutableClusterGraph().getDest(tResult, tList.get(0));
 					tList.remove(0);
 					tDistance++;
 				}
 				return tResult;
 			} else {
 				return pTarget;
 			}
 		} catch (Exception tExc) {
 			Logging.err(this, "Unable to determine cluster that is farthest in direction from " + pSource + " to target " + pTarget);
 			return null;
 		}
 	}
 	
 	/**
 	 * Creates a new HRMID for a cluster member depending on the given member number.
 	 * 
 	 * @param pMemberNumber the member number
 	 * @return the new HRMID for the cluster member
 	 */
 	private HRMID createClusterMemberAddress(int pMemberNumber)
 	{
 		HRMID tHRMID = mHRMID.clone();
 		
 		// transform the member number to a BigInteger
 		BigInteger tAddress = BigInteger.valueOf(pMemberNumber);
 
 		// set the member number for the given hierarchy level
 		tHRMID.setLevelAddress(mHierarchyLevel, tAddress);
 
 		// some debug outputs
 		if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 			Logging.log(this, "Set " + tAddress + " on hierarchy level " + mHierarchyLevel.getValue() + " for HRMID " + tHRMID.toString());
 			Logging.log(this, "Created for a cluster member the NEW HRMID=" + tHRMID.toString());
 		}
 		
 		return tHRMID;
 	}
 
 	/**
 	 * This function is called for distributing HRMIDs among the cluster members.
 	 */
 	public void signalAddressDistribution()
 	{
 		/**
 		 * The following value is used to assign monotonously growing addresses to all cluster members.
 		 * The addressing has to start with "1".
 		 */
 		int tNextClusterMemberAddress = 1;
 
 		Logging.log(this, "DISTRIBUTING ADDRESSES to entities on level " + (getHierarchyLevel().getValue() - 1) + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 		
 		/**
 		 * Assign ourself an HRMID address
 		 */
 		// are we at the base level?
 		if(mHierarchyLevel.isBaseLevel()) {
 			
 			// create new HRMID for ourself
 			HRMID tOwnAddress = createClusterMemberAddress(tNextClusterMemberAddress++);
 
 			Logging.log(this, "    ..setting local HRMID " + tOwnAddress.toString());
 
 			//HINT: don't update the HRMID of the coordinator here!
 			
 			// update the HRMID of the managed cluster by direct call and avoid additional communication overhead
 			mManagedCluster.setHRMID(this, tOwnAddress);
 		}
 
 		/**
 		 * Distribute AssignHRMID packets among the cluster members 
 		 */
 		Logging.log(this, "    ..distributing HRMIDs among cluster members: " + mManagedCluster.getClusterMembers());
 		for(CoordinatorCEPChannel tClusterMember : mManagedCluster.getClusterMembers()) {
 
 			//TODO: don't send this update in a loop to ourself!
 			//TODO: check if cluster members already have an address and distribute only free addresses here
 			
 			// create new HRMID for cluster member
 			HRMID tHRMID = createClusterMemberAddress(tNextClusterMemberAddress++);
 
 			// store the HRMID under which the peer will be addressable from now 
 			tClusterMember.setPeerClusterMemberHRMID(tHRMID);
 			
 			if ((tClusterMember.getPeerClusterMemberHRMID() != null) && (!tClusterMember.getPeerClusterMemberHRMID().equals(tHRMID))){
 				Logging.log(this, "    ..replacing HRMID " + tClusterMember.getPeerClusterMemberHRMID().toString() + " and assign new HRMID " + tHRMID.toString() + " to " + tClusterMember.getPeerName());
 			}else
 				Logging.log(this, "    ..assigning new HRMID " + tHRMID.toString() + " to " + tClusterMember.getPeerName());
 
 			// create new AssignHRMID packet for the cluster member
 			AssignHRMID tAssignHRMID = new AssignHRMID(getHRMController().getNode().getCentralFN().getName(), tHRMID);
 			
 			// send the packet
 			tClusterMember.sendPacket(tAssignHRMID);
 		}
 	}
 	
 	private boolean connectToNeighbors(int radius)
 	{
 		for(HRMGraphNodeName tNode : mClustersToNotify) {
 			if(tNode instanceof ICluster && !((ICluster) tNode).isInterASCluster()) {
 				ICluster tCluster = (ICluster)tNode;
 				Name tName = tCluster.getCoordinatorName();
 				synchronized(tCluster) {
 					if(tName == null) {
 						try {
 							tCluster.wait();
 						} catch (InterruptedException tExc) {
 							Logging.err(this, tCluster + " is skipped on cluster discovery", tExc);
 						}
 					}
 				}
 				
 				if(mConnectedEntities.contains(tName)){
 					Logging.log(this, " L" + mHierarchyLevel + "-skipping connection to " + tName + " for cluster " + tNode + " because connection already exists");
 					continue;
 				} else {
 					/*
 					 * was it really this cluster? -> reevaluate
 					 */
 					Logging.log(this, " L" + mHierarchyLevel + "-adding connection to " + tName + " for cluster " + tNode);
 					getMultiplexer().addConnection(tCluster, mManagedCluster);
 					//new CoordinatorCEP(mManagedCluster.getCoordinator().getLogger(), mManagedCluster.getCoordinator(), this, false);
 					mConnectedEntities.add(tName);
 				}
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public void setCoordinatorPriority(BullyPriority pCoordinatorPriority) {
 		if(mCoordinatorCEP != null) {
 			if (!mCoordinatorCEP.getPeerPriority().equals(pCoordinatorPriority)){
 				Logging.info(this, "Tried to set a priority that does not correspond with the priority of the concurrent coordinator, wrong connection endpoint?");
 			}
 			mCoordinatorCEP.setPeerPriority(pCoordinatorPriority);
 		}
 	}
 
 	@Override
 	public BullyPriority getCoordinatorPriority() {
 		if(mCoordinatorCEP != null) {
 			return mCoordinatorCEP.getPeerPriority();
 		}
 		return null;
 	}
 
 	@Override
 	public void setPriority(BullyPriority pPriority) 
 	{
 		if (!getBullyPriority().equals(pPriority)){
 			Logging.err(this, "############# Trying to update Bully priority from " + getBullyPriority() + " to " + pPriority);
 		}else{
 			Logging.log(this, "############# Trying to set same Bully priority " + getBullyPriority());
 		}
 
 		//TODO: remove this function
 	}
 
 	@Override
 	public HRMController getHRMController() {
 		return mManagedCluster.getHRMController();
 	}
 
 	@Override
 	public LinkedList<CoordinatorCEPChannel> getClusterMembers() {
 		return mCEPs;
 	}
 
 	public LinkedList<HRMID> getHigherHRMIDs()
 	{
 		return mHigherHRMIDs;
 	}
 	
 	public HashMap<CoordinatorCEPChannel, TopologyData> getAddressMapping()
 	{
 		return mAddressMapping;
 	}
 	
 	public LinkedList<CoordinatorCEPChannel> getLowerCEPs()
 	{
 		return mManagedCluster.getClusterMembers();
 	}
 	
 	@Override
 	public void addParticipatingCEP(CoordinatorCEPChannel pParticipatingCEP) {
 		mCEPs.add(pParticipatingCEP);
 	}
 
 	@Override
 	public Long getClusterID() {
 		return mClusterID;
 	}
 
 	@Override
 	public HierarchyLevel getHierarchyLevel() {
 		return new HierarchyLevel(this, mHierarchyLevel.getValue() + 1);
 	}
 
 	@Override
 	public Name getCoordinatorName() {
 		return mCoordinatorName;
 	}
 
 	@Override
 	public BullyPriority getBullyPriority() 
 	{
 		// return the Bully priority of the managed cluster object
 		return mManagedCluster.getBullyPriority();
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
 					Logging.log(this, "ACTIVE WAITING");
 					wait(1000);
 				} catch (InterruptedException tExc) {
 					Logging.err(this, "Error while waiting for address", tExc);
 				}
 			}
 			return mCoordinatorAddress;
 		}
 	}
 
 	@Override
 	public void setToken(int pToken) {
 		if(mToken != 0) {
 			Logging.log(this, "######################### Updating token to " + pToken);
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
 	}
 	
 	/**
 	 * Handles packet type "AssignHRMID"
 	 * 
 	 * @param pAssignHRMIDPacket the received packet
 	 */
 	public void handleAssignHRMID(AssignHRMID pAssignHRMIDPacket)
 	{
 		// extract the HRMID from the packet 
 		HRMID tHRMID = pAssignHRMIDPacket.getHRMID();
 		
 		Logging.log(this, "Handling AssignHRMID with assigned HRMID " + tHRMID.toString());
 		
 		// update the local HRMID
 		setHRMID(this, tHRMID);
 		
 		// we should automatically continue the address distribution?
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			Logging.log(this, "     ..continuing the address distribution process for this coordiantor");
 			signalAddressDistribution();				
 		}else{
 			Logging.log(this, "     ..stopping address propagation here because address distribution is done in step-by-step mode");
 		}
 	}
 
 	
 	public void handleSharedTopologyData(TopologyData pTopologyData)
 	{
 		/*
 		 * this cluster manager only computes the FIB derived from Radius algorithm
 		 */
 //		Node tNode = getHRMController().getNode();
 		
 //TODO: still needed here?
 //		if(pTopologyData.getPushThrougs() != null && !pTopologyData.getPushThrougs().isEmpty()) {
 //			for(FIBEntry tEntry : pTopologyData.getPushThrougs()) {
 //				if((tEntry.getDestination() != null && !tEntry.getDestination().equals(new HRMID(0)) ) && tEntry.getNextHop() != null && !tEntry.getNextHop().equals(tNode.getRoutingService().getNameFor(tNode.getCentralFN()))) {
 //					getHRMController().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 //				}
 //			}
 //		}
 		
 		Logging.log(this, "Received topology data: " + pTopologyData);
 		mTopologyData = pTopologyData;
 		
 		if(pTopologyData.getEntries() != null && !pTopologyData.getEntries().isEmpty()) {
 			if(mHigherHRMIDs == null) mHigherHRMIDs = new LinkedList<HRMID>();
 			for(FIBEntry tEntry : pTopologyData.getEntries()) {
 				mHigherHRMIDs.add(tEntry.getDestination());
 				registerFIBEntry(tEntry);
 			}
 			Logging.log(this, "Have to provide FEs for " + mHigherHRMIDs);
 		}
 	}
 
 	@Override
 	public BullyPriority getHighestPriority() {
 		return mHighestPriority;
 	}
 	
 	public void handleBullyAnnounce(BullyAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		/**
 		 * Stores the local cluster, which corresponds to the correct hierarchy level
 		 */
 		ICluster tLocalCluster = getHRMController().getClusterWithCoordinatorOnLevel(getHierarchyLevel().getValue());		
 		if ((tLocalCluster != null) && (mManagedCluster != tLocalCluster)){
 			Logging.err(this,  "################## WE SHOULD NEVER REACH HERE, clusters differ from each other: " + mManagedCluster + " != " + tLocalCluster);
 		}
 
 		/**
 		 * the name of the cluster, which is managed by this coordinator
 		 */
 		ClusterName tLocalManagedClusterName = new ClusterName(mManagedCluster.getToken(), mManagedCluster.getClusterID(), mManagedCluster.getHierarchyLevel());
 
 		/*
 		 * check whether old priority was lower than new priority
 		 */
 		if(pAnnounce.getSenderPriority().isHigher(this, getCoordinatorPriority())) {
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
 					List<RoutableClusterGraphLink> tClusterList = getHRMController().getRoutableClusterGraph().getRoute(mManagedCluster, pCEP.getRemoteClusterName());
 					if(tClusterList.size() > 0) {
 						if(getHRMController().getRoutableClusterGraph().getDest(pCEP.getRemoteClusterName(), tClusterList.get(tClusterList.size() - 1)) instanceof Cluster) {
 							Logging.warn(this, "Not sending neighbor zone announce because another intermediate cluster has a shorter route to target");
 							if(tClusterList != null) {
 								String tClusterRoute = new String();
 								HRMGraphNodeName tTransitiveElement = mManagedCluster;
 								for(RoutableClusterGraphLink tConnection : tClusterList) {
 									tClusterRoute += tTransitiveElement + "\n";
 									tTransitiveElement = getHRMController().getRoutableClusterGraph().getDest(tTransitiveElement, tConnection);
 								}
 								Logging.log(this, "cluster route to other entity:\n" + tClusterRoute);
 							}
 						} else {
 							
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
 							tOldCovered.setNegotiatorIdentification(tLocalManagedClusterName);
 							
 							DiscoveryEntry tOldCoveredEntry = new DiscoveryEntry(tLocalCluster.getToken(), tLocalCluster.getCoordinatorName(), tLocalCluster.getCoordinatorsAddress().getAddress().longValue(), tLocalCluster.getCoordinatorsAddress(), tLocalCluster.getHierarchyLevel());
 							/*
 							 * the forwarding cluster to the newly discovered cluster has to be one level lower so it is forwarded on the correct cluster
 							 * 
 							 * calculation of the predecessor is because the cluster identification on the remote site is multiplexed
 							 */
 							tClusterList = getHRMController().getRoutableClusterGraph().getRoute(mManagedCluster, getCoordinatorCEP().getRemoteClusterName());
 							if(!tClusterList.isEmpty()) {
 								ICluster tPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(mManagedCluster, tClusterList.get(0));
 								tOldCoveredEntry.setPredecessor(new ClusterName(tPredecessor.getToken(), tPredecessor.getClusterID(), tPredecessor.getHierarchyLevel()));
 							}
 							tOldCoveredEntry.setPriority(getCoordinatorPriority());
 							tOldCoveredEntry.setRoutingVectors(pCEP.getPath(tLocalCluster.getCoordinatorsAddress()));
 							tOldCovered.setCoveringClusterEntry(tOldCoveredEntry);
 //							List<Route> tPathToCoordinator = getCoordinator().getHRS().getCoordinatorRoutingMap().getRoute((HRMName)pCEP.getSourceName(), getCoordinatorsAddress());
 							//tOldCovered.setAnnouncer(getCoordinator().getHRS().getCoordinatorRoutingMap().getDest(tPathToCoordinator.get(0)));
 							pCEP.sendPacket(tOldCovered);
 							
 							/*
 							 * now the old cluster is notified about the new cluster
 							 */
 							
 							NeighborClusterAnnounce tNewCovered = new NeighborClusterAnnounce(pAnnounce.getSenderName(), getHierarchyLevel(), pAnnounce.getCoordSignature(), pCEP.getPeerName(), pAnnounce.getToken(), pCEP.getPeerName().getAddress().longValue());
 							tNewCovered.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 							tNewCovered.setNegotiatorIdentification(tLocalManagedClusterName);
 							DiscoveryEntry tCoveredEntry = new DiscoveryEntry(pAnnounce.getToken(),	pAnnounce.getSenderName(), (pCEP.getPeerName()).getAddress().longValue(), pCEP.getPeerName(),	getHierarchyLevel());
 							tCoveredEntry.setRoutingVectors(pCEP.getPath(pCEP.getPeerName()));
 							tNewCovered.setCoveringClusterEntry(tCoveredEntry);
 							tCoveredEntry.setPriority(pAnnounce.getSenderPriority());
 							
 							List<RoutableClusterGraphLink> tClusters = getHRMController().getRoutableClusterGraph().getRoute(mManagedCluster, pCEP.getRemoteClusterName());
 							if(!tClusters.isEmpty()) {
 								ICluster tNewPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(mManagedCluster, tClusters.get(0));
 								tCoveredEntry.setPredecessor(new ClusterName(tNewPredecessor.getToken(), tNewPredecessor.getClusterID(), tNewPredecessor.getHierarchyLevel()));
 							}
 							Logging.warn(this, "Rejecting " + (getCoordinatorCEP().getPeerName()).getDescr() + " in favor of " + pAnnounce.getSenderName());
 							tNewCovered.setRejection();
 							getCoordinatorCEP().sendPacket(tNewCovered);
 							for(CoordinatorCEPChannel tCEP : getClusterMembers()) {
 								if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerName())) {
 									tCEP.setAsParticipantOfMyCluster(true);
 								} else {
 									tCEP.setAsParticipantOfMyCluster(false);
 									
 								}
 							}
 							setToken(pAnnounce.getToken());
 							setCoordinatorCEP(pCEP, pAnnounce.getCoordSignature(), pAnnounce.getSenderName(),pAnnounce.getToken(),  pCEP.getPeerName());
 							getHRMController().setClusterWithCoordinator(getHierarchyLevel(), this);
 							getCoordinatorCEP().sendPacket(tNewCovered);
 						}
 					}
 				}
 				
 			} else {
 				if (pAnnounce.getCoveredNodes() != null){
 					for(CoordinatorCEPChannel tCEP : getClusterMembers()) {
 						if(pAnnounce.getCoveredNodes().contains(tCEP.getPeerName())) {
 							tCEP.setAsParticipantOfMyCluster(true);
 						} else {
 							tCEP.setAsParticipantOfMyCluster(false);
 						}
 					}
 				}
 				setToken(pAnnounce.getToken());
 				getHRMController().setClusterWithCoordinator(getHierarchyLevel(), this);
 				setCoordinatorCEP(pCEP, pAnnounce.getCoordSignature(), pAnnounce.getSenderName(), pAnnounce.getToken(), pCEP.getPeerName());
 			}
 		} else {
 			/*
 			 * this part is for the coordinator that intended to announce itself -> send rejection and send acting coordinator along with
 			 * the announcement that is just gained a neighbor zone
 			 */
 			
 			NeighborClusterAnnounce tUncoveredAnnounce = new NeighborClusterAnnounce(getCoordinatorName(), getHierarchyLevel(), getCoordinatorSignature(), getCoordinatorsAddress(), getToken(), getCoordinatorsAddress().getAddress().longValue());
 			tUncoveredAnnounce.setCoordinatorsPriority(getCoordinatorCEP().getPeerPriority());
 			/*
 			 * the routing service address of the announcer is set once the neighbor zone announce arrives at the rejected coordinator because this
 			 * entity is already covered
 			 */
 			
 			tUncoveredAnnounce.setNegotiatorIdentification(tLocalManagedClusterName);
 			
 			DiscoveryEntry tUncoveredEntry = new DiscoveryEntry(tLocalCluster.getToken(), tLocalCluster.getCoordinatorName(), tLocalCluster.getCoordinatorsAddress().getAddress().longValue(), tLocalCluster.getCoordinatorsAddress(), tLocalCluster.getHierarchyLevel());
 			List<RoutableClusterGraphLink> tClusterList = getHRMController().getRoutableClusterGraph().getRoute(mManagedCluster, pCEP.getRemoteClusterName());
 			if(!tClusterList.isEmpty()) {
 				ICluster tPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(mManagedCluster, tClusterList.get(0));
 				tUncoveredEntry.setPredecessor(new ClusterName(tPredecessor.getToken(), tPredecessor.getClusterID(), tPredecessor.getHierarchyLevel()));
 			}
 			tUncoveredEntry.setPriority(getCoordinatorPriority());
 			tUncoveredEntry.setRoutingVectors(pCEP.getPath(tLocalCluster.getCoordinatorsAddress()));
 			tUncoveredAnnounce.setCoveringClusterEntry(tUncoveredEntry);
 			Logging.warn(this, "Rejecting " + (getCoordinatorCEP().getPeerName()).getDescr() + " in favour of " + pAnnounce.getSenderName());
 			tUncoveredAnnounce.setRejection();
 			pCEP.sendPacket(tUncoveredAnnounce);
 			
 			/*
 			 * this part is for the acting coordinator, so NeighborZoneAnnounce is sent in order to announce the cluster that was just rejected
 			 */
 			
 			NeighborClusterAnnounce tCoveredAnnounce = new NeighborClusterAnnounce(pAnnounce.getSenderName(), getHierarchyLevel(), pAnnounce.getCoordSignature(), pCEP.getPeerName(), pAnnounce.getToken(), (pCEP.getPeerName()).getAddress().longValue());
 			tCoveredAnnounce.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 			
 //			List<Route> tPathToCoordinator = getCoordinator().getHRS().getCoordinatorRoutingMap().getRoute(pCEP.getSourceName(), pCEP.getPeerName());
 			
 			//tCoveredAnnounce.setAnnouncer(getCoordinator().getHRS().getCoordinatorRoutingMap().getDest(tPathToCoordinator.get(0)));
 			tCoveredAnnounce.setNegotiatorIdentification(tLocalManagedClusterName);
 			DiscoveryEntry tCoveredEntry = new DiscoveryEntry(pAnnounce.getToken(), pAnnounce.getSenderName(), (pCEP.getPeerName()).getAddress().longValue(), pCEP.getPeerName(), getHierarchyLevel());
 			tCoveredEntry.setRoutingVectors(pCEP.getPath(pCEP.getPeerName()));
 			tCoveredAnnounce.setCoveringClusterEntry(tCoveredEntry);
 			tCoveredEntry.setPriority(pAnnounce.getSenderPriority());
 			tCoveredAnnounce.setCoordinatorsPriority(pAnnounce.getSenderPriority());
 			
 			List<RoutableClusterGraphLink> tClusters = getHRMController().getRoutableClusterGraph().getRoute(mManagedCluster, getCoordinatorCEP().getRemoteClusterName());
 			if(!tClusters.isEmpty()) {
 				ICluster tNewPredecessor = (ICluster) getHRMController().getRoutableClusterGraph().getDest(mManagedCluster, tClusters.get(0));
 				tUncoveredEntry.setPredecessor(new ClusterName(tNewPredecessor.getToken(), tNewPredecessor.getClusterID(), tNewPredecessor.getHierarchyLevel()));
 			}
 			Logging.log(this, "Coordinator CEP is " + getCoordinatorCEP());
 			getCoordinatorCEP().sendPacket(tCoveredAnnounce);
 		}
 	}
 	
 	private ICluster addAnnouncedCluster(NeighborClusterAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		NeighborCluster tCluster = null;
 		if(pAnnounce.isAnnouncementFromForeign())
 		{
 			tCluster = new NeighborCluster(pAnnounce.getCoordAddress().getAddress().longValue(), pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken(), new HierarchyLevel(this, mHierarchyLevel.getValue() + 2),	mManagedCluster.getHRMController());
 			getHRMController().setSourceIntermediateCluster(tCluster, getHRMController().getSourceIntermediate(this));
 			((NeighborCluster)tCluster).addAnnouncedCEP(pCEP);
 			tCluster.setToken(pAnnounce.getToken());
 			tCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 			//mManagedCluster.addNeighborCluster(tCluster);
 		} else {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 		}
 		if(pAnnounce.getCoordinatorName() != null) {
 			RoutingService tRS = (RoutingService)getHRMController().getNode().getRoutingService();
 			if(! tRS.isKnown(pAnnounce.getCoordinatorName())) {
 				try {
 					getHRMController().getHRS().registerNode(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 				} catch (RemoteException tExc) {
 					Logging.err(this, "Unable to register " + pAnnounce.getCoordinatorName() + " at higher entity", tExc);
 				}
 				
 			}
 		}
 		return tCluster;
 	}
 	
 	@Override
 	public void handleNeighborAnnouncement(NeighborClusterAnnounce	pAnnounce, CoordinatorCEPChannel pCEP)
 	{		
 		if(pAnnounce.getCoveringClusterEntry() != null) {
 //			Cluster tForwardingCluster = null;
 			
 			if(pAnnounce.isRejected()) {
 //				Cluster tMultiplex = mManagedCluster;
 //				tForwardingCluster = (Cluster) ((Cluster) getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster()) == null ? pCEP.getRemoteCluster() : getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster())) ;
 				//pAnnounce.setAnnouncer( (tForwardingCluster.getCoordinatorsAddress() != null ? tForwardingCluster.getCoordinatorsAddress() : null ));
 				Logging.log(this, "Removing " + this + " as participating CEP from " + this);
 				getClusterMembers().remove(this);
 			}
 			if(pAnnounce.getCoordinatorName() != null) {
 				RoutingService tRS = (RoutingService)getHRMController().getNode().getRoutingService();
 				if(! tRS.isKnown(pAnnounce.getCoordinatorName())) {
 					
 					try {
 						getHRMController().getHRS().registerNode(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 					} catch (RemoteException tExc) {
 						Logging.err(this, "Unable to register " + pAnnounce.getCoordinatorName() + " at name mapping service", tExc);
 					}
 				}
 			}
 			try {
 				pCEP.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry());
 			} catch (PropertyException tExc) {
 				Logging.log(this, "Unable to fulfill requirements");
 			}
 			Logging.log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 		} else {
 			Logging.log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 		}
 	}
 
 	@Override
 	public CoordinatorCEPChannel getCoordinatorCEP() {
 		return mCoordinatorCEP;
 	}
 
 	@Override
 	public void setCoordinatorCEP(CoordinatorCEPChannel pCoord, HRMSignature pCoordSignature, Name pCoordName, int pCoordToken, HRMName pAddress) {
 		/**
 		 * the name of the cluster, which is managed by this coordinator
 		 */
 		ClusterName tLocalManagedClusterName = new ClusterName(mManagedCluster.getToken(), mManagedCluster.getClusterID(), mManagedCluster.getHierarchyLevel());
 		setToken(pCoordToken);
 		
 		Logging.log(this, "announcement number " + (++mReceivedAnnounces) + ": Setting Coordinator " + pCoord + " with name " + pCoordName + " with routing address " + pAddress);
 		Logging.log(this, "previous coordinator was " + mCoordinatorCEP + " with name " + mCoordinatorName);
 		mCoordinatorCEP = pCoord;
 		mCoordinatorSignature = pCoordSignature;
 		mCoordinatorName = pCoordName;
 		synchronized(this) {
 			mCoordinatorAddress = pAddress;
 			notifyAll();
 		}
 
 		//		LinkedList<CoordinatorCEP> tEntitiesToNotify = new LinkedList<CoordinatorCEP> ();
 		if(pCoordSignature != null) {
 			for(HRMGraphNodeName tNode: getHRMController().getRoutableClusterGraph().getNeighbors(mManagedCluster)) {
 				if(tNode instanceof ICluster && !((ICluster) tNode).isInterASCluster()) {
 					for(CoordinatorCEPChannel tCEP : mCEPs) {
 						if(((ICluster)tNode).getCoordinatorsAddress().equals(tCEP.getPeerName()) && !tCEP.isPartOfMyCluster()) {
 							Logging.info(this, "Informing " + tCEP + " about existence of neighbor zone ");
 							NeighborClusterAnnounce tAnnounce = new NeighborClusterAnnounce(pCoordName, getHierarchyLevel(), pCoordSignature, pAddress, getToken(), pAddress.getAddress().longValue());
 							tAnnounce.setCoordinatorsPriority(getCoordinatorCEP().getPeerPriority());
 							LinkedList<RoutingServiceLinkVector> tVectorList = tCEP.getPath(pAddress);
 							tAnnounce.setRoutingVectors(tVectorList);
 							tAnnounce.setNegotiatorIdentification(tLocalManagedClusterName);
 							tCEP.sendPacket(tAnnounce);
 						}
 						Logging.log(this, "Informed " + tCEP + " about new neighbor zone");
 					}
 				}
 			}
 		}
 		if(mReceivedAnnouncements != null) {
 			for(NeighborClusterAnnounce tAnnounce : mReceivedAnnouncements) {
 				pCoord.sendPacket(tAnnounce);
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
 
 	
 	/**
 	 * Assign new HRMID for being addressable as cluster member.
 	 *  
 	 * @param pCaller the caller who assigns the new HRMID
 	 * @param pHRMID the new HRMID
 	 */
 	public void setHRMID(Object pCaller, HRMID pHRMID)
 	{
 		Logging.log(this, "ASSINGED HRMID=" + pHRMID + " (caller=" + pCaller + ")");
 
 		// update the HRMID
 		if (pHRMID != null){
 			mHRMID = pHRMID.clone();
 		}else{
 			mHRMID = null;
 		}
 		
 		// inform HRM controller about the change
 		getHRMController().updateCoordinatorAddress(this);
 	}
 
 	public HRMID getHRMID() {
 		return mHRMID;
 	}
 	
 	@Override
 	public void setHighestPriority(BullyPriority pHighestPriority) {
 		mHighestPriority = pHighestPriority;
 	}
 
 	public void sendClusterBroadcast(Serializable pData, LinkedList<CoordinatorCEPChannel> pAlreadyInformed)
 	{
 		if(pData instanceof BullyPriorityUpdate)
 		{
 			Logging.log(this, "Will send priority update to" + mCEPs);
 		}
 		LinkedList<CoordinatorCEPChannel> tInformedCEPs = null;
 		if(pAlreadyInformed != null) {
 			tInformedCEPs= pAlreadyInformed;
 		} else {
 			tInformedCEPs = new LinkedList<CoordinatorCEPChannel>(); 
 		}
 		try {
 			for(CoordinatorCEPChannel tCEP : mCEPs)
 			{
 				if(!tInformedCEPs.contains(tCEP))
 				{
 					tCEP.sendPacket(pData);
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
 
 	public Cluster getManagedCluster()
 	{
 		return mManagedCluster;
 	}
 	
 	@Override
 	public TopologyData getTopologyData()
 	{
 		return mTopologyData;
 	}
 	
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
 	
 //	public void handleRouteRequest(RouteRequest pRequest, IRoutableClusterGraphTargetName pSourceCluster)
 //	{
 //		/**
 //		 * Stored the routing DB of the local HRM controller
 //		 */
 //		final RoutableGraph<HRMName, Route> tLocalRoutingDB = getHRMController().getHRS().getCoordinatorRoutingMap();
 //
 //		final RouteRequest tParameterRouteRequest = pRequest;
 //		final IRoutableClusterGraphTargetName tSourceCluster = pSourceCluster;
 //		final Coordinator tManager = this;
 //		
 //		if(pRequest.getResult() != null && pRequest.getResult().equals(ResultType.UNFEASIBLE)) {
 //			CoordinatorCEPChannel tCEP = mRouteRequestDispatcher.get(tParameterRouteRequest.getSession());
 //			tParameterRouteRequest.setAnswer();
 //			tCEP.sendPacket(tParameterRouteRequest);
 //			return;
 //		}
 //		
 //		new Thread () {
 //			public void run() {
 //				int tDescendingDifference = 0;
 //				if(tParameterRouteRequest.getTarget() instanceof HRMID) {
 //					tDescendingDifference = (((HRMID)tParameterRouteRequest.getTarget())).getDescendingDifference(tSourceCluster.getHrmID());
 //				}
 //				
 //				/*
 //				 * Beginning of the recursion
 //				 */
 //				if(tDescendingDifference > mHierarchyLevel.getValue()) {
 //					RouteRequest tRequest = tParameterRouteRequest.clone();
 //					getCoordinatorCEP().sendPacket(tRequest);
 //					synchronized(tRequest) {
 //						try {
 //							tRequest.wait();
 //						} catch (InterruptedException tExc) {
 //							Logging.err(this, "Error when waiting for " + tRequest, tExc);
 //						}
 //					}
 //					Logging.log(tManager, "Come back of " + tRequest);
 //					try {
 //						if(tRequest.getRoutingVectors() != null) {
 //							for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 //								tParameterRouteRequest.addRoutingVector(tVector);
 //							}
 //						}
 //					} catch (ConcurrentModificationException tExc) {
 //						if(tRequest.getRoutingVectors() != null) {
 //							for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 //								if(tParameterRouteRequest.getRoutingVectors().contains(tVector)) {
 //									tParameterRouteRequest.addRoutingVector(tVector);
 //								}
 //							}
 //						}
 //					}
 //					
 //					tParameterRouteRequest.setAnswer();
 //					tParameterRouteRequest.setResult(tRequest.getResult());
 //					CoordinatorCEPChannel tCEP = mRouteRequestDispatcher.get(tParameterRouteRequest.getSession());
 //					tCEP.sendPacket(tParameterRouteRequest);
 //				} else {
 //					/*
 //					 * end of the recursion
 //					 */
 //					Logging.log(tManager, "Reached highest cluster");
 //					final HRMID tLocalTarget = ((HRMID) (tParameterRouteRequest.getTarget())).clone();
 //					for(int i = 0; i < mHierarchyLevel.getValue(); i++) {
 //						tLocalTarget.setLevelAddress(new HierarchyLevel(this, i), BigInteger.valueOf(0));
 //					}
 //					LinkedList<IRoutableClusterGraphTargetName> tNodesToIgnore = new LinkedList<IRoutableClusterGraphTargetName>();
 //					
 //					List<RoutableClusterGraphLink> tClusterConnection = null;; 
 //					try {
 //						Logging.log(tManager, "Invalidating nodes " + tNodesToIgnore);
 //						tClusterConnection = getHRMController().getRoutableClusterGraph().getRouteWithInvalidatedNodes(tSourceCluster, getVirtualNodeFromHRMID(tLocalTarget), tNodesToIgnore);
 //						LinkedList<ICluster> tClusters = new LinkedList<ICluster>();
 //						ICluster tLastCluster = (ICluster) tSourceCluster;
 //						if(tClusterConnection != null && !tClusterConnection.isEmpty()) {
 //							for(RoutableClusterGraphLink tConnection : tClusterConnection) {
 //								tClusters.add(tLastCluster);
 //								tLastCluster = (ICluster) getHRMController().getRoutableClusterGraph().getDest(tLastCluster, tConnection);
 //							}
 //						} else {
 //							tParameterRouteRequest.setResult(ResultType.UNFEASIBLE);
 //							CoordinatorCEPChannel tCEP = mRouteRequestDispatcher.get(tParameterRouteRequest.getSession());
 //							tCEP.sendPacket(tParameterRouteRequest);
 //							return;
 //						}
 //						
 //						tClusters.add(tLastCluster);
 //						
 //						/*
 //						 * find descending difference from left to right
 //						 */
 //						Logging.log(tManager, "Got cluster connection " + tClusterConnection);
 //						Logging.log(tManager, "Got cluster connection over " + tClusters);
 //						
 //						Route tRoute = new Route();
 //						tLastCluster = (ICluster) tSourceCluster;
 //						for(ICluster tCluster : tClusters) {
 //							List<Route> tPath = tLocalRoutingDB.getRoute(tLastCluster.getCoordinatorsAddress(), tCluster.getCoordinatorsAddress());
 //							if(!tPath.isEmpty() && tPath.size() == 1 && !tLastCluster.getCoordinatorsAddress().equals(tCluster.getCoordinatorsAddress())) {
 //								for(Route tRoutePart : tPath) {
 //									tRoute.addAll(tRoutePart);
 //									RoutingServiceLinkVector tVector = new RoutingServiceLinkVector(tRoutePart, tLocalRoutingDB.getSource(tRoutePart), tLocalRoutingDB.getDest(tRoutePart));
 //									tParameterRouteRequest.addRoutingVector(tVector);
 //								}
 //							} else if(tPath.isEmpty() && !tLastCluster.getCoordinatorsAddress().equals(tCluster.getCoordinatorsAddress()) || !tPath.isEmpty() && tPath.size() > 1 && !tLastCluster.getCoordinatorsAddress().equals(tCluster.getCoordinatorsAddress())) {
 //								Logging.err(tManager, "Unable to calculate a route segment path from " + tLastCluster.getCoordinatorsAddress() + " to " + tCluster.getCoordinatorsAddress());
 //								for(CoordinatorCEPChannel tCEP : mManagedCluster.getParticipatingCEPs()) {
 //									if(tCEP.getRemoteClusterName().equals(tLastCluster)) {
 //										Logging.log(tManager, "About to ask route from " + tLastCluster + " to " + tCluster);
 //										RouteRequest tRequest = new RouteRequest(tLastCluster.getCoordinatorsAddress(), tCluster.getCoordinatorsAddress(), null, 0);
 //										tRequest.addRequiredCluster(new ClusterName(tLastCluster.getToken(), tLastCluster.getClusterID(), tLastCluster.getHierarchyLevel()));
 //
 //										tCEP.sendPacket(tRequest);
 //										synchronized(tRequest) {
 //											if(!tRequest.isAnswer()) {
 //												try {
 //													tRequest.wait();
 //												} catch (InterruptedException tExc) {
 //													Logging.err(this, "Error when waiting for come back of route request " + tRequest, tExc);
 //												}
 //											}
 //										}
 //										if(tRequest.getRoutingVectors() != null) {
 //											for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 //												tParameterRouteRequest.addRoutingVector(tVector);
 //											}
 //										}	
 //									}
 //								}
 //							}
 //							Logging.log(tManager, "Calculated route from " + tLastCluster.getCoordinatorsAddress() + " to " + tCluster.getCoordinatorsAddress() + ":" + tPath);
 //							tLastCluster = tCluster;
 //						}
 //						Logging.log(tManager, "Concurrent route request is " + tParameterRouteRequest);
 //						if(((HRMID)tParameterRouteRequest.getTarget()).getLevelAddress(mHierarchyLevel.getValue()) != BigInteger.valueOf(0)) {
 //							CoordinatorCEPChannel tCEP = mManagedCluster.getCEPOfCluster((ICluster) getVirtualNodeFromHRMID(tLocalTarget));
 //							RouteRequest tRequest = new RouteRequest(tCEP.getPeerName(), tParameterRouteRequest.getTarget(), tParameterRouteRequest.getDescription(), tParameterRouteRequest.getSession());
 //							tCEP.sendPacket(tRequest);
 //							synchronized(tRequest) {
 //								try {
 //									tRequest.wait();
 //								} catch (InterruptedException tExc) {
 //									Logging.err(tManager, "Error while waiting for", tExc);
 //								}
 //							}
 //							Logging.log(tManager, "Come back of " + tRequest);
 //							if(tRequest.getRoutingVectors() != null) {
 //								for(RoutingServiceLinkVector tVector : tRequest.getRoutingVectors()) {
 //									tParameterRouteRequest.addRoutingVector(tVector);
 //								}
 //							}
 //							tParameterRouteRequest.setResult(tRequest.getResult());
 //							Logging.log(tManager, "Route request is now " + tParameterRouteRequest);
 //						}
 //						long tRequestSession = tParameterRouteRequest.getSession();
 //						Logging.log(tManager, "registered requests for " + mRouteRequestDispatcher + ": ");
 //						for(Long tLong : mRouteRequestDispatcher.keySet()) {
 //							Logging.log(this, tLong + " is pointing on " + mRouteRequestDispatcher.get(tLong));
 //						}
 //						CoordinatorCEPChannel tCEP = mRouteRequestDispatcher.get(tRequestSession);
 //						tParameterRouteRequest.setAnswer();
 //						tCEP.sendPacket(tParameterRouteRequest);
 //					} catch (NullPointerException tExc) {
 //						Logging.err(tManager, "Error when trying to calculate route with invalidated node", tExc);
 //					}
 //				}
 //			}
 //		}.start();
 //	}
 
 	@Override
 	public int getSerialisedSize()
 	{
 		return 0;
 	}
 
 	@Override
 	public CoordinatorCEPMultiplexer getMultiplexer() {
 		return getHRMController().getMultiplexerOnLevel(mHierarchyLevel.getValue() + 1);
 	}
 
 //	public void registerRouteRequest(Long pSession, CoordinatorCEPChannel pCEP)
 //	{
 //		if( mRouteRequestDispatcher == null ) {
 //			mRouteRequestDispatcher = new HashMap<Long, CoordinatorCEPChannel>();
 //		}
 //		Logging.log(this, "registered " + pSession + " with " + pCEP);
 //		mRouteRequestDispatcher.put(pSession, pCEP);
 //	}
 	
 	public String toString()
 	{
 		//return getClass().getSimpleName() + (mManagedCluster != null ? "(" + mManagedCluster.toString() + ")" : "" ) + "TK(" +mToken + ")COORD(" + mCoordinatorSignature + ")@" + mLevel;
		return toLocation() + " (" + (mManagedCluster != null ? "Cluster" + mManagedCluster.getGUIClusterID() + ", " : "") + idToString() + ")";
 	}
 
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + mGUICoordinatorID + "@" + getHRMController().getNodeGUIName() + "@" + (getHierarchyLevel().getValue() - 1);
 		
 		return tResult;
 	}
 	private String idToString()
 	{
 		if (getHRMID() == null){
 			return "ID=" + getClusterID() + ", Tok=" + mToken +  ", NodePrio=" + getBullyPriority().getValue() +  (getCoordinatorSignature() != null ? ", Coord.=" + getCoordinatorSignature() : "");
 		}else{
 			return "HRMID=" + getHRMID().toString();
 		}
 	}
 }
