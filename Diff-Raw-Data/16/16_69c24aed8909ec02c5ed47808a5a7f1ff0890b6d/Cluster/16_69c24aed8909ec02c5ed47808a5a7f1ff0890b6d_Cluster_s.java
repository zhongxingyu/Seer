 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.clustering;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData.FIBEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyPriorityUpdate;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.Coordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPMultiplexer;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.Elector;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionManager;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMEntity;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.IElementDecorator;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a clusters on a defined hierarchy level.
  * 
  */
 public class Cluster implements ICluster, IElementDecorator, HRMEntity
 {
 	/**
 	 * This is the GUI specific cluster counter, which allows for globally unique cluster IDs.
 	 * It's only used within the GUI. 	
 	 */
 	private static int sGUIClusterID = 0;
 
 	/**
 	 * Stores the Bully priority of this node for this cluster.
 	 * The value is also used inside the Elector of this cluster.
 	 */
 	private BullyPriority mBullyPriority = null;
 
 	/**
	 * Stored the hierarchy level of this cluster.
 	 */
 	private HierarchyLevel mHierarchyLevel;
 
 	private CoordinatorCEPChannel mChannelToCoordinator = null;
 	private Long mClusterID;
 	private BullyPriority mHighestPriority = null;
 	private HRMID mHRMID = null;
 	private BullyPriority mCoordinatorPriority;
 	private Name mCoordName;
 	private Name mCoordAddress;
 	private HRMController mHRMController;
 	private LinkedList<CoordinatorCEPChannel> mCEPs;
 	private LinkedList<NeighborClusterAnnounce> mReceivedAnnounces = null;
 	private HRMSignature mCoordSignature;
 	private boolean mInterASCluster = false;
 	private int mToken;
 	private int mAnnoucementCounter = 0;
 	private LinkedList<CoordinatorCEPChannel> mLaggards; // only used by the Elector
 	private TopologyData mTopologyData = null;
 	private CoordinatorCEPChannel mAnnouncer = null;
 	private LinkedList<CoordinatorCEPChannel> mOldParticipatingCEPs;
 	private Coordinator mCoordinator = null;
 	private CoordinatorCEPMultiplexer mMux = null;
 	
 	/**
 	 * This is the GUI specific cluster ID. It is used to allow for an easier debugging.
 	 */
 	private int mGUIClusterID = sGUIClusterID++;
 	
 	/**
 	 * This is the constructor of an intermediate cluster. At first such a cluster is identified by its cluster
 	 * ID and the hierarchical level. Later on - once a coordinator is found, it is additionally identified
 	 * by a token the coordinator sends to all participants. In contrast to the cluster token the identity is used
 	 * to filter potential participants that may be used for the election of a coordinator.
 	 * 
 	 * @param pClusterID
 	 * @param pLevel
 	 * @param ptHRMController
 	 */
 	public Cluster(Long pClusterID, HierarchyLevel pHierarchyLevelValue, HRMController ptHRMController)
 	{
 		mClusterID = pClusterID;
 		mHierarchyLevel = pHierarchyLevelValue;
 		mCEPs = new LinkedList<CoordinatorCEPChannel>();
 		mReceivedAnnounces = new LinkedList<NeighborClusterAnnounce>();
 		mHRMController = ptHRMController;
 		mBullyPriority = BullyPriority.createForCluster(this);
 		Logging.log(this, "CREATED CLUSTER " + mClusterID + " on level " + mHierarchyLevel + " with priority " + mBullyPriority.getValue());
 		for(ICluster tCluster : getHRMController().getRoutingTargets())
 		{
 			Logging.log(this, "Found already known neighbor: " + tCluster);
 			if ((tCluster.getHierarchyLevel().equals(pHierarchyLevelValue)) && (tCluster != this))
 			{
 				if (!(tCluster instanceof Cluster)){
 					Logging.err(this, "Routing target should be a cluster, but it is " + tCluster);
 				}
 				tCluster.addNeighborCluster(this);
 
 				// increase Bully priority because of changed connectivity (topology depending)
 				mBullyPriority.increaseConnectivity();
 			}
 		}
		Elector tProcess = ElectionManager.getElectionManager().addElection(mHierarchyLevel.getValue(), mClusterID, new Elector(this));
 		mMux = new CoordinatorCEPMultiplexer(mHRMController);
 		mMux.setCluster(this);
 	}
 	
 	public void setAnnouncedCEP(CoordinatorCEPChannel pCEP)
 	{
 		mAnnouncer = pCEP;
 	}
 	
 	public void handleBullyAnnounce(BullyAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		setToken(pAnnounce.getToken());
 		setCoordinatorCEP(pCEP, pAnnounce.getCoordSignature(), pAnnounce.getSenderName(), pCEP.getPeerName());
 		getHRMController().addApprovedSignature(pAnnounce.getCoordSignature());
 		getHRMController().setClusterWithCoordinator(getHierarchyLevel(), this);
 	}
 	
 	public void setCoordinatorCEP(CoordinatorCEPChannel pCoordinatorChannel, HRMSignature pCoordSignature, Name pCoordName, HRMName pAddress)
 	{
 		Logging.log(this, "announcement number " + (++mAnnoucementCounter) + ": Setting Coordinator " + pCoordinatorChannel + " with signature " + pCoordSignature + " with routing address " + pAddress + " and priority ");
 		Logging.log(this, "previous channel to coordinator was " + mChannelToCoordinator + " for coordinator " + mCoordName);
 		mChannelToCoordinator = pCoordinatorChannel;
 		mCoordSignature = pCoordSignature;
 		mCoordName = pCoordName;
 		if(mChannelToCoordinator == null) {
 			synchronized(this) {
 				mCoordAddress = getHRMController().getNode().getRoutingService().getNameFor(getHRMController().getNode().getCentralFN());
 				notifyAll();
 			}
 			setCoordinatorPriority(getBullyPriority());
 		} else {
 			synchronized(this) {
 				mCoordAddress = pAddress;
 				notifyAll();
 			}
 			setCoordinatorPriority(pCoordinatorChannel.getPeerPriority());
 			try {
 				getHRMController().getHRS().registerNode(pCoordName, pAddress);
 			} catch (RemoteException tExc) {
 				Logging.err(this, "Unable to register " + pCoordName, tExc);
 			}
 			
 			if(pCoordinatorChannel.getRouteToPeer() != null && !pCoordinatorChannel.getRouteToPeer().isEmpty()) {
 				if(pAddress instanceof L2Address) {
 					getHRMController().getHRS().registerNode((L2Address) pAddress, false);
 				}
 				
 				getHRMController().getHRS().registerRoute(pCoordinatorChannel.getSourceName(), pCoordinatorChannel.getPeerName(), pCoordinatorChannel.getRouteToPeer());
 			}
 		}
 		Logging.log(this, "This cluster has the following neighbors: " + getNeighbors());
 		for(ICluster tCluster : getNeighbors()) {
 			if(tCluster instanceof Cluster) {
 				Logging.log(this, "CLUSTER-CEP - found already known neighbor cluster: " + tCluster);
 
 				Logging.log(this, "Preparing neighbor zone announcement");
 				NeighborClusterAnnounce tAnnounce = new NeighborClusterAnnounce(pCoordName, mHierarchyLevel, pCoordSignature, pAddress, getToken(), mClusterID);
 				tAnnounce.setCoordinatorsPriority(mBullyPriority); //TODO : ???
 				if(pCoordinatorChannel != null) {
 					tAnnounce.addRoutingVector(new RoutingServiceLinkVector(pCoordinatorChannel.getRouteToPeer(), pCoordinatorChannel.getSourceName(), pCoordinatorChannel.getPeerName()));
 				}
 				((Cluster)tCluster).announceNeighborCoord(tAnnounce, pCoordinatorChannel);
 			}
 		}
 		if(mReceivedAnnounces.isEmpty()) {
 			Logging.log(this, "No announces came in while no coordinator was set");
 		} else {
 			Logging.log(this, "sending old announces");
 			while(!mReceivedAnnounces.isEmpty()) {
 				if(mChannelToCoordinator != null)
 				{
 					// OK, we have to notify the other node via socket communication, so this cluster has to be at least one hop away
 					mChannelToCoordinator.sendPacket(mReceivedAnnounces.removeFirst());
 				} else {
 					/*
 					 * in this case this announcement came from a neighbor intermediate cluster
 					 */
 					handleNeighborAnnouncement(mReceivedAnnounces.removeFirst(), pCoordinatorChannel);
 				}
 			}
 		}
 		if(pCoordinatorChannel == null) {
 //			boolean tIsEdgeRouter = false;
 			LinkedList<ClusterName> tInterASClusterIdentifications = new LinkedList<ClusterName>();
 
 			for(IRoutableClusterGraphNode tNode : getHRMController().getRoutableClusterGraph().getNeighbors(this)) {
 				if(tNode instanceof ICluster && ((ICluster) tNode).isInterASCluster()) {
 					ICluster tCluster = (ICluster)tNode;
 //					tIsEdgeRouter = true;
 					tInterASClusterIdentifications.add(new ClusterName(tCluster.getToken(), tCluster.getClusterID(), tCluster.getHierarchyLevel()));
 				}
 			}
 		}
 	}
 	
 	private ICluster addAnnouncedCluster(NeighborClusterAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		ICluster tCluster = getHRMController().getCluster(new ClusterName(pAnnounce.getToken(), pAnnounce.getClusterID(), pAnnounce.getLevel()));
 		if(tCluster == null) {
 			tCluster = new NeighborCluster(pAnnounce.getClusterID(), pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken(), mHierarchyLevel, getHRMController());
 			getHRMController().setSourceIntermediateCluster(tCluster, this);
 			((NeighborCluster)tCluster).addAnnouncedCEP(pCEP);
 			((NeighborCluster)tCluster).setSourceIntermediate(this);
 			tCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 			tCluster.setToken(pAnnounce.getToken());
 			
 			if(pAnnounce.isInterASCluster()) {
 				tCluster.setInterASCluster();
 			}
 			
 			try {
 				getHRMController().getHRS().registerNode(tCluster.getCoordinatorName(), tCluster.getCoordinatorsAddress());
 			} catch (RemoteException tExc) {
 				Logging.err(this, "Unable to fulfill requirements", tExc);
 			}
 			
 		} else {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 		}
 		/*if(tCluster instanceof AttachedCluster) {
 			((AttachedCluster)tCluster).setNegotiatingHost(pAnnounce.getAnnouncersAddress());
 		}*/
 		
 		/*
 		 * function checks whether neighbor relation was established earlier
 		 */
 		addNeighborCluster(tCluster);
 
 		if(pAnnounce.getCoordinatorName() != null) {
 //			Description tDescription = new Description();
 			try {
 				getHRMController().getHRS().registerNode(pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress());
 			} catch (RemoteException tExc) {
 				Logging.warn(this, "Unable to register " + pAnnounce.getCoordinatorName(), tExc);
 			}
 		}
 		return tCluster;
 	}
 	
 	public void handleNeighborAnnouncement(NeighborClusterAnnounce	pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		if(!pAnnounce.getCoordinatorName().equals(getHRMController().getNode().getCentralFN().getName())) {
 			Logging.log(this, "Received announcement of foreign cluster");
 		}
 		
 		if(getHierarchyLevel().isBaseLevel()) {
 			if(pCEP != null) {
 				if(!pCEP.getSourceName().equals(pCEP.getPeerName()) && pCEP.getRouteToPeer() != null) {
 					RoutingServiceLinkVector tLink = new RoutingServiceLinkVector(pCEP.getRouteToPeer().clone(),  pCEP.getSourceName(), pCEP.getPeerName());
 					pAnnounce.addRoutingVector(tLink);
 					Logging.log(this, "Added routing vector " + tLink);
 				}
 				pAnnounce.isForeignAnnouncement();
 			}
 			if(pCEP != null) {
 				pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			}
 		} else {
 			if(getHRMController().getClusterWithCoordinatorOnLevel(mHierarchyLevel.getValue()) == null) {
 				/*
 				 * no coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 				 */
 				for(Coordinator tManager : getHRMController().getCoordinator(new HierarchyLevel(this, mHierarchyLevel.getValue() + 1))) {
 					if(tManager.getNeighbors().contains(pAnnounce.getNegotiatorIdentification())) {
 						tManager.storeAnnouncement(pAnnounce);
 					}
 				}
 			} else {
 				/*
 				 * coordinator set -> find cluster that is neighbor of the predecessor, so routes are correct
 				 */
 				for(Coordinator tManager : getHRMController().getCoordinator(new HierarchyLevel(this, mHierarchyLevel.getValue() + 1))) {
 					if(tManager.getNeighbors().contains(pAnnounce.getNegotiatorIdentification())) {
 						if(tManager.getCoordinatorCEP() != null) {
 							tManager.getCoordinatorCEP().sendPacket(pAnnounce);
 						}
 					}
 				}
 			}
 			
 			
 			if(pAnnounce.getCoveringClusterEntry() != null) {
 //				Cluster tForwardingCluster = null;
 				
 				if(pAnnounce.isRejected()) {
 //					Cluster tMultiplex = this;
 //					tForwardingCluster = (Cluster) ((Cluster) getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster()) == null ? pCEP.getRemoteCluster() : getCoordinator().getLastUncovered(tMultiplex, pCEP.getRemoteCluster())) ;
 					//pAnnounce.setAnnouncer( (tForwardingCluster.getCoordinatorsAddress() != null ? tForwardingCluster.getCoordinatorsAddress() : null ));
 					Logging.log(this, "Removing " + this + " as participating CEP from " + this);
 					getParticipatingCEPs().remove(this);
 				}
 				try {
 					addNeighborCluster(getHRMController().getCluster(pCEP.handleDiscoveryEntry(pAnnounce.getCoveringClusterEntry())));
 				} catch (PropertyException tExc) {
 					Logging.log(this, "Unable to fulfill requirements");
 				}
 				Logging.log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 				pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			} else if(pCEP != null) {
 				Logging.log(this, "new negotiating cluster will be " + getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 				pCEP.addAnnouncedCluster(addAnnouncedCluster(pAnnounce, pCEP), getHRMController().getCluster(pAnnounce.getNegotiatorIdentification()));
 			}
 		}
 	}
 	
 	public CoordinatorCEPChannel getCoordinatorCEP()
 	{
 		return mChannelToCoordinator;
 	}
 	
 	public void addNeighborCluster(ICluster pNeighbor)
 	{
 		LinkedList<ICluster> tNeighbors = getNeighbors(); 
 		if(!tNeighbors.contains(pNeighbor))
 		{
 			if(pNeighbor instanceof Cluster) {
 				RoutableClusterGraphLink tLink = new RoutableClusterGraphLink(RoutableClusterGraphLink.LinkType.PHYSICAL_LINK);
 				getHRMController().getRoutableClusterGraph().storeLink(pNeighbor, this, tLink);
 			} else {
 				RoutableClusterGraphLink tLink = new RoutableClusterGraphLink(RoutableClusterGraphLink.LinkType.LOGICAL_LINK);
 				getHRMController().getRoutableClusterGraph().storeLink(pNeighbor, this, tLink);
 			}
 			if(pNeighbor instanceof Cluster && !pNeighbor.isInterASCluster()) {
 				
 				Logging.log(this, "CLUSTER - adding neighbor cluster: " + pNeighbor);
 
 				// increase Bully priority because of changed connectivity (topology depending) 
 				mBullyPriority.increaseConnectivity();
 				
 				if(!mInterASCluster) {
 					Logging.log(this, "Informing " + getParticipatingCEPs() + " about change in priority and initiating new election");
 					
 					sendClusterBroadcast(new BullyPriorityUpdate(getHRMController().getNode().getCentralFN().getName(), mBullyPriority), null);
 					
 					Logging.log(this, "Informed other clients about change of priority - it is now " + mBullyPriority.getValue());
 				}
 			}
 		}
 	}
 	
 	private void announceNeighborCoord(NeighborClusterAnnounce pAnnouncement, CoordinatorCEPChannel pCEP)
 	{
 		Logging.log(this, "Handling " + pAnnouncement);
 		if(mCoordName != null)
 		{
 			if(getHRMController().getNode().getCentralFN().getName().equals(mCoordName))
 			{
 				handleNeighborAnnouncement(pAnnouncement, pCEP);
 			} else {
 				mChannelToCoordinator.sendPacket(pAnnouncement);
 			}
 		} else {
 			mReceivedAnnounces.add(pAnnouncement);
 		}
 	}
 	
 	/**
 	 * @param pHRMID identification of this cluster
 	 */
 	public void setHRMID(Object pCaller, HRMID pHRMID)
 	{
 		Logging.log(this, "Setting HRM ID: \"" + pHRMID + "\", triggered from " + pCaller);
 		mHRMID = pHRMID;
 	}
 	
 	public BullyPriority getHighestPriority()
 	{
 		if (mHighestPriority == null){
 			mHighestPriority = new BullyPriority(this);
 		}
 		
 		return mHighestPriority;
 	}
 	
 	public LinkedList<CoordinatorCEPChannel> getOldParticipatingCEPs()
 	{
 		return mOldParticipatingCEPs;
 	}
 	
 	public void setParticipatingCEPs(LinkedList<CoordinatorCEPChannel> pCEPs)
 	{
 		mOldParticipatingCEPs = mCEPs;
 		Logging.log(this, "Setting participating CEPs to " + pCEPs);
 		mCEPs = pCEPs;
 	}
 	
 	public void addParticipatingCEP(CoordinatorCEPChannel pParticipatingCEP)
 	{
 		if(!mCEPs.contains(pParticipatingCEP)) {
 			mCEPs.add(pParticipatingCEP);
 			Logging.info(this, "Added " + pParticipatingCEP + " to participating CEPs");
 			if(mCEPs.size() > 1) {
 				Logging.info(this, "Adding second participating CEP " + pParticipatingCEP);
 //				StackTraceElement[] tStackTrace = Thread.currentThread().getStackTrace();
 //				for (StackTraceElement tElement : tStackTrace) {
 //					getCoordinator().getLogger().log(tElement.toString());
 //				}
 			}
 		}
 	}
 
 	public LinkedList<CoordinatorCEPChannel> getParticipatingCEPs()
 	{
 		return mCEPs;
 	}
 	
 	public HRMController getHRMController()
 	{
 		return mHRMController;
 	}
 	
 	public void setPriority(BullyPriority pPriority)
 	{
 		BullyPriority tBullyPriority = mBullyPriority;
 		mBullyPriority = pPriority;
 		Logging.info(this, "Setting Bully priority for cluster " + toString() + " from " + tBullyPriority.getValue() + " to " + mBullyPriority.getValue());
 	}
 	
 	public BullyPriority getCoordinatorPriority()
 	{
 		return mCoordinatorPriority;
 	}
 	
 	public void setCoordinatorPriority(BullyPriority pCoordinatorPriority)
 	{
 		mCoordinatorPriority = pCoordinatorPriority;
 	}
 	
 	public Long getClusterID()
 	{
 		return mClusterID;
 	}
 	
 	public HRMSignature getCoordinatorSignature()
 	{
 		return mCoordSignature;
 	}
 	
 	public LinkedList<ICluster> getNeighbors()
 	{
 		LinkedList<ICluster> tList = new LinkedList<ICluster>();
 		for(IRoutableClusterGraphNode tNode : getHRMController().getRoutableClusterGraph().getNeighbors(this)) {
 			if(tNode instanceof ICluster) {
 				tList.add((ICluster)tNode);
 			}
 		}
 		return tList;
 	}
 	
 	public int getToken()
 	{
 		return mToken;
 	}
 	
 	public L2Address getCoordinatorsAddress()
 	{
 		return (L2Address) mCoordAddress;
 	}
 	
 	public void setCoordinatorName(Name pCoordName)
 	{
 		mCoordName = pCoordName;
 	}
 	
 	public void sendClusterBroadcast(Serializable pData, LinkedList<CoordinatorCEPChannel> pAlreadyInformed)
 	{
 		Logging.log(this, "Sending CLUSTER BROADCAST " + pData);
 		
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
 					Logging.log(this, "   BROADCAST TO " + tCEP);
 					tCEP.sendPacket(pData);
 					tInformedCEPs.add(tCEP);
 				}
 			}
 		} catch (ConcurrentModificationException tExc) {
 			Logging.warn(this, "change in cluster CEP number occured, sending message to new peers", tExc);
 			sendClusterBroadcast(pData, tInformedCEPs);
 		}
 	}
 	
 	public String getClusterDescription()
 	{
 		return toLocation();
 		//getHRMController().getPhysicalNode() + ":" + mClusterID + "@" + mHierarchyLevel + "(" + mCoordSignature + ")";
 	}
 	
 	public BullyPriority getBullyPriority()
 	{
 		if (mBullyPriority == null){
 			mBullyPriority = new BullyPriority(this);
 		}
 			
 		return mBullyPriority;
 	}
 	
 	public Name getCoordinatorName()
 	{
 		return mCoordName;
 	}
 	
 	public HierarchyLevel getHierarchyLevel()
 	{
 		return mHierarchyLevel;
 	}
 	
 	public int getGUIClusterID()
 	{
 		return mGUIClusterID;
 	}
 	
 	@Override
 	public void setToken(int pToken) {
 		if(mToken != 0) {
 			Logging.log(this, "Updating token");
 		}
 		mToken = pToken;
 	}
 	
 	@Override
 	public HRMID getHrmID() {
 		return mHRMID;
 	}
 	
 	@Override
 	public void setHighestPriority(BullyPriority pHighestPriority) {
 		mHighestPriority = pHighestPriority;
 	}
 	
 	@Override
 	public Namespace getNamespace() {
 		return new Namespace("cluster");
 	}
 
 	@Override
 	public int getSerialisedSize() {
 		return 0;
 	}
 	
 
 	@Override
 	public boolean equals(Object pObj)
 	{
 		if(pObj instanceof Coordinator) {
 			return false;
 		}
 		if(pObj instanceof Cluster) {
 			ICluster tCluster = (ICluster) pObj;
 			if (tCluster.getClusterID().equals(getClusterID()) && (tCluster.getToken() == getToken()) && (tCluster.getHierarchyLevel() == getHierarchyLevel())) {
 				return true;
 			} else if(tCluster.getClusterID().equals(getClusterID()) && tCluster.getHierarchyLevel() == getHierarchyLevel()) {
 				return false;
 			} else if (tCluster.getClusterID().equals(getClusterID())) {
 				return false;
 			}
 		}
 		if(pObj instanceof ClusterName) {
 			ClusterName tClusterName = (ClusterName) pObj;
 			if (tClusterName.getClusterID().equals(getClusterID()) && (tClusterName.getToken() == getToken()) && (tClusterName.getHierarchyLevel() == getHierarchyLevel())) {
 				return true;
 			} else if(tClusterName.getClusterID().equals(getClusterID()) && tClusterName.getHierarchyLevel() == getHierarchyLevel()) {
 				return false;
 			} else if (tClusterName.getClusterID().equals(getClusterID())) {
 				return false;
 			}
 		}
 		return false;
 	}	
 
 	@Override
 	public boolean isInterASCluster()
 	{
 		return mInterASCluster;
 	}
 
 	@Override
 	public void setInterASCluster()
 	{
 		mInterASCluster = true;
 	}
 
 	/**
 	 * As the implemented version of HRM uses a fully distributed algorithm for signaling it is possible that some nodes are not
 	 * associated to a coordinator because they were not covered. In that case such a node sends RequestCoordinator messages to 
 	 * the neighbors. If a neighbor is not covered by a coordinator either, it is added as laggard.
 	 * 
 	 * @return Return the list of laggards that were not covered by a coordinator either. 
 	 */
 	public LinkedList<CoordinatorCEPChannel> getLaggards()
 	{
 		return mLaggards;
 	}
 
 	/**
 	 * As the implemented version of HRM uses a fully distributed algorithm for signaling it is possible that some nodes are not
 	 * associated to a coordinator because they were not covered. In that case such a node sends RequestCoordinator messages to
 	 * the neighbors. If a neighbor is not covered by a coordinator either, it is aded as laggard.
 	 * 
 	 * @param pCEP Add one connection end point as laggard here.
 	 */
 	public void addLaggard(CoordinatorCEPChannel pCEP)
 	{
 		if(mLaggards == null) {
 			mLaggards = new LinkedList<CoordinatorCEPChannel>();
 			mLaggards.add(pCEP);
 		} else {
 			mLaggards.add(pCEP);
 		}
 	}
 
 	public CoordinatorCEPChannel getAnnouncedCEP()
 	{
 		return mAnnouncer;
 	}
 
 	@Override
 	public void handleTopologyData(TopologyData pEnvelope)
 	{
 		if(pEnvelope.getApprovedSignatures() != null) {
 			for(HRMSignature tSignature : pEnvelope.getApprovedSignatures()) {
 				getHRMController().addApprovedSignature(tSignature);
 			}
 		}
 		mTopologyData = pEnvelope;
 		HierarchicalNameMappingService<HRMID> tNMS = null;
 		try {
 			tNMS = (HierarchicalNameMappingService) HierarchicalNameMappingService.getGlobalNameMappingService();
 		} catch (RuntimeException tExc) {
 			HierarchicalNameMappingService.createGlobalNameMappingService(getHRMController().getNode().getAS().getSimulation());
 		}
 		Name tLocalRouterName = getHRMController().getNode().getCentralFN().getName();
 		
 		tNMS.registerName(tLocalRouterName, pEnvelope.getHRMID(), NamingLevel.NAMES);
 		String tString = new String();
 		for(NameMappingEntry<HRMID> tEntry : tNMS.getAddresses(tLocalRouterName)) {
 			tString += tEntry + " ";
 		}
 		Logging.log(this, "Currently registered names: " + tString);
 
 		setHRMID(this, pEnvelope.getHRMID());
 		
 		getHRMController().getNode().setDecorationValue(getHRMController().getNode().getDecorationValue() + " " + pEnvelope.getHRMID().toString() + ",");
 		getHRMController().addIdentification(pEnvelope.getHRMID());
 		if(mTopologyData.getEntries() != null) {
 			for(FIBEntry tEntry : mTopologyData.getEntries()) {
 				if((tEntry.getDestination() != null && !tEntry.getDestination().equals(new HRMID(0)) ) && tEntry.getNextHop() != null) {
 					/*if(!getCoordinator().getHRS().getRoutingTable().containsKey(tEntry.getDestination())) {
 						getCoordinator().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 					} else {
 						if(getCoordinator().getHRS().getFIBEntry(tEntry.getDestination()).isWriteProtected()) {
 							getCoordinator().getLogger().log(this, "Not replacing " + getCoordinator().getHRS().getFIBEntry(tEntry.getDestination()) + " with " + tEntry);
 						} else {
 							getCoordinator().getHRS().getRoutingTable().remove(tEntry.getDestination());
 							getCoordinator().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 						}
 					}*/
 					getHRMController().getHRS().addRoutingEntry(tEntry.getDestination(), tEntry);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public TopologyData getTopologyData()
 	{
 		return mTopologyData;
 	}
 	
 	/**
 	 * 
 	 * @return Return the cluster manager that is associated to this intermediate cluster. However it is only initialized if this
 	 * node really had the highest priority.
 	 */
 	public Coordinator getCoordinator()
 	{
 		return mCoordinator;
 	}
 	
 	public void setCoordinator(Coordinator pCoordinator)
 	{
 		mCoordinator = pCoordinator;
 	}
 	
 	@Override
 	public synchronized CoordinatorCEPMultiplexer getMultiplexer()
 	{
 		return mMux;
 	}
 
 	/**
 	 * This method is specific for the handling of RouteRequests.
 	 * 
 	 * @param pCluster
 	 * @return
 	 */
 	public CoordinatorCEPChannel getCEPOfCluster(ICluster pCluster)
 	{
 		for(CoordinatorCEPChannel tCEP : getParticipatingCEPs()) {
 			if(tCEP.getRemoteClusterName().equals(pCluster)) {
 				return tCEP;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public Object getDecorationParameter()
 	{
 		return IElementDecorator.Color.GREEN;
 
 	}
 
 	@Override
 	public void setDecorationParameter(Object pDecoration)
 	{
 		// not used, but have to be implemented for implementing interface IElementDecorator
 	}
 
 	@Override
 	public Object getDecorationValue()
 	{
 		return Float.valueOf(0.8f);
 	}
 
 	@Override
 	public void setDecorationValue(Object tLabal)
 	{
 		// not used, but have to be implemented for implementing interface IElementDecorator
 	}
 
 	public int hashCode()
 	{
 		return mClusterID.intValue();
 	}
 	
 	@SuppressWarnings("unused")
 	public String toString()
 	{
 		if(mHRMID != null && HRMConfig.Debugging.PRINT_HRMIDS_AS_CLUSTER_IDS) {
 			return mHRMID.toString();
 		} else {
 			return toLocation() + " (ID=" + getClusterID() + ", Tok=" + mToken +  ", NodePrio=" + getBullyPriority().getValue() +  (getCoordinatorSignature() != null ? ", Coord.=" + getCoordinatorSignature() : "") + (mInterASCluster ? ", TRANSIT" : "") + ")";
 
 		}
 	}
 
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + mGUIClusterID + "@" + getHRMController().getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
 		return tResult;
 	}
 }
