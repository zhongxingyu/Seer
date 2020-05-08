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
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPMultiplexer;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.topology.IElementDecorator;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used when a layer 0 neighbor cluster is detected. 
  * It includes all needed data about the neighbor cluster. //TV
  */
 public class NeighborCluster implements ICluster, IElementDecorator
 {
 	private static final long serialVersionUID = -8746079632866375924L;
 	private int mToken;
 	private HierarchyLevel mHierarchyLevel = null;
 	private BullyPriority mPriority;
 	private BullyPriority mCoordinatorPriority;
 	private Name mCoordName;
 	private HRMName mCoordAddress;
 	private Long mClusterID;
 	private HRMController mHRMController;
 	private HRMSignature mCoordSignature;
 	private HRMID mHRMID;
 	private Name mAnnouncer;
 	private LinkedList<CoordinatorCEPChannel> mAnnouncedCEPs;
 	private Cluster mSourceIntermediateCluster = null;
 	private boolean mInterASCluster = false;
 	
 	/**
 	 * 
 	 * @param pClusterID identifier of the cluster
 	 * @param pCoordName name of the coordinator
 	 * @param pAddress routing service address
 	 * @param pToken as the token this cluster got
 	 * @param pLevel is the level this cluster is related to
 	 * @param pResponsibleCoordinator as the coordinator this cluster works on (local)
 	 */
 	public NeighborCluster(Long pClusterID, Name pCoordName, HRMName pAddress, int pToken, HierarchyLevel pLevel, HRMController pResponsibleCoordinator)
 	{	
 		mAnnouncer = pResponsibleCoordinator.getNode().getCentralFN().getName();
 		mCoordAddress = pAddress;
 		setCoordinatorName(pCoordName);
 		mClusterID = pClusterID;
 		mHRMController = pResponsibleCoordinator;
 		mCoordName = pCoordName;
 		mToken = pToken;
 		mHierarchyLevel = pLevel;
 	}
 	
 	public void addAnnouncedCEP(CoordinatorCEPChannel pCEP)
 	{
 		if(mAnnouncedCEPs == null) {
 			mAnnouncedCEPs = new LinkedList<CoordinatorCEPChannel>();
 		}
 		Logging.log(getClusterDescription(), "Adding announcer " + pCEP);
 		mAnnouncedCEPs.add(pCEP);
 	}
 	
 	public int getClusterDistanceToTarget()
 	{
 		return getHRMController().getClusterDistance(this);
 	}
 
 	public BullyPriority getHighestPriority()
 	{
 		return getBullyPriority();
 	}
 
 	public void handleNeighborAnnouncement(NeighborClusterAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		if(pAnnounce.getRoutingVectors() != null) {
 			for(RoutingServiceLinkVector tVector : pAnnounce.getRoutingVectors()) {
 				getHRMController().getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 			}
 		}
 		ICluster tCluster = getHRMController().getCluster(new ClusterName(pAnnounce.getToken(), pAnnounce.getClusterID(), pAnnounce.getLevel()));
 		if(tCluster == null)
 		{
 			tCluster = new NeighborCluster(pAnnounce.getClusterID(), pAnnounce.getCoordinatorName(), pAnnounce.getCoordAddress(), pAnnounce.getToken(), mHierarchyLevel,	getHRMController());
 			getHRMController().setSourceIntermediateCluster(tCluster, getHRMController().getSourceIntermediate(this));
 			((NeighborCluster)tCluster).addAnnouncedCEP(pCEP);
 			tCluster.setPriority(pAnnounce.getCoordinatorsPriority());
 			tCluster.setToken(pAnnounce.getToken());
 		} else {
 			Logging.log(this, "Cluster announced by " + pAnnounce + " is an intermediate neighbor ");
 		}
 		//((AttachedCluster)tCluster).setNegotiatingHost(pAnnounce.getAnnouncersAddress());
 
 		/*
 		 * function checks whether neighbor relation was established earlier
 		 */
 		addNeighborCluster(tCluster);
 
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
 		pCEP.addAnnouncedCluster(tCluster, this);
 	}
 
 	public CoordinatorCEPChannel getCoordinatorCEP()
 	{
 		return null;
 	}
 
	public synchronized void setCoordinatorCEP(CoordinatorCEPChannel pCoord, HRMSignature pCoordSignature, Name pCoordName, int pCoordToken, HRMName pAddress)
 	{
 		mCoordAddress = pAddress;
 		mCoordName = pCoordName;
 	}
 
 	public void addNeighborCluster(ICluster pNeighbor)
 	{
 		getHRMController().getRoutableClusterGraph().storeLink(this, pNeighbor, new RoutableClusterGraphLink(RoutableClusterGraphLink.LinkType.LOGICAL_LINK));
 	}
 
 	public void setHRMID(Object pCaller, HRMID pHRMID)
 	{
 		Logging.log(this, "Setting HRM ID: \"" + pHRMID + "\", triggered from " + pCaller);
 		mHRMID = pHRMID;
 	}
 
 	@Override
 	public void setCoordinatorPriority(BullyPriority pCoordinatorPriority)
 	{
 		/*
 		 * not needed, this is just a dummy for topology
 		 */
 	}
 
 	@Override
 	public BullyPriority getCoordinatorPriority()
 	{
 		return mCoordinatorPriority;
 	}
 
 	@Override
 	public void setPriority(BullyPriority pPriority)
 	{
 		mPriority = pPriority;
 	}
 
 	@Override
 	public HRMController getHRMController() {
 		return mHRMController;
 	}
 
 	@Override
	public LinkedList<CoordinatorCEPChannel> getClusterMembers()
 	{
 		LinkedList<CoordinatorCEPChannel> tCEPs = new LinkedList<CoordinatorCEPChannel>();
 		//tCEPs.add(mCEP);
 		return tCEPs;
 	}
 
 	@Override
 	public void addParticipatingCEP(CoordinatorCEPChannel pParticipatingCEP)
 	{
 		/*
 		 * not needed, this is just a dummy for topology
 		 */
 	}
 
 	@Override
 	public Long getClusterID()
 	{
 		return mClusterID;
 	}
 
 	@Override
 	public HierarchyLevel getHierarchyLevel() {
 		return mHierarchyLevel;
 	}
 
 	@Override
 	public Name getCoordinatorName()
 	{
 		return mCoordName;
 	}
 
 	@Override
 	public BullyPriority getBullyPriority()
 	{
 		return mPriority;
 	}
 
 	@Override
 	public String getClusterDescription()
 	{
 		return getClass().getSimpleName() + "(" + mAnnouncer + "->" + mCoordName + ")"+"PR" + "(" + mPriority + ")" + "ID(" + mClusterID + ")TK(" + mToken + ")@" + mHierarchyLevel;
 	}
 
 	@Override
 	public void setCoordinatorName(Name pCoordName)
 	{
 		mCoordName = pCoordName;
 	}
 
 	@Override
 	public HRMName getCoordinatorsAddress()
 	{
 		return mCoordAddress;
 	}
 
 	@Override
 	public void setToken(int pToken) {
 		mToken = pToken;
 	}
 
 	@Override
 	public int getToken() {
 		return mToken;
 	}
 
 	@Override
 	public LinkedList<ICluster> getNeighbors() {
 		LinkedList<ICluster> tCluster = new LinkedList<ICluster>();
 		for(IRoutableClusterGraphTargetName tNode : getHRMController().getRoutableClusterGraph().getNeighbors(this)) {
 			if(tNode instanceof ICluster) {
 				tCluster.add((ICluster) tNode);
 			}
 		}
 		return tCluster;
 	}
 
 	@Override
 	public HRMSignature getCoordinatorSignature() {
 		return mCoordSignature;
 	}
 
 	@Override
 	public HRMID getHrmID() {
 		return mHRMID;
 	}
 
 	@Override
 	public void setHighestPriority(BullyPriority pHighestPriority) {
 		/*
 		 * not needed, this is just a dummy for topology
 		 */
 	}
 
 	@Override
 	public void sendClusterBroadcast(Serializable pData, LinkedList<CoordinatorCEPChannel> pAlreadyInformed) {
 		/*
 		 * not needed, this is just a dummy for topology
 		 */
 	}
 	
 	@SuppressWarnings("unused")
 	public String toString()
 	{
 		if(mHRMID != null && HRMConfig.Debugging.PRINT_HRMIDS_AS_CLUSTER_IDS) {
 			return mHRMID.toString();
 		} else {
 			return getClusterDescription() + ":HOPS(" + getHRMController().getClusterDistance(this) + ")" + (mInterASCluster ? "InterAS" : "");
 		}
 	}
 
 	@Override
 	public Namespace getNamespace() {
 		return new Namespace("attachedcluster");
 	}
 
 	@Override
 	public int getSerialisedSize() {
 		return 0;
 	}
 
 	@Override
 	public boolean equals(Object pObj)
 	{
 		if(pObj instanceof ICluster) {
 			ICluster tCluster = (ICluster) pObj;
 			if(tCluster.getClusterID().equals(getClusterID()) &&
 					tCluster.getToken() == getToken() &&
 					tCluster.getHierarchyLevel() == getHierarchyLevel()) {
 				return true;
 			} else if(tCluster.getClusterID().equals(getClusterID()) && tCluster.getHierarchyLevel() == getHierarchyLevel()) {
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
 		return mInterASCluster;
 	}
 
 	@Override
 	public void setInterASCluster()
 	{
 		mInterASCluster = true;
 	}
 
 //	@Override
 //	public CoordinatorCEPChannel getNegotiatorCEP()
 //	{
 //		return mNegotiator;
 //	}
 //
 //	@Override
 //	public void setNegotiatorCEP(CoordinatorCEPChannel pCEP)
 //	{
 //		Logging.log(getClusterDescription(), "Setting " + pCEP + " as  negotiating CEP");
 //		if(!mNegotiators.contains(pCEP)) mNegotiators.add(pCEP);
 //		mNegotiator = pCEP;
 //	}
 
 	public CoordinatorCEPChannel getAnnouncedCEP(ICluster pCluster)
 	{
 		int tClosestCluster = Integer.MAX_VALUE;
 		CoordinatorCEPChannel tClosest = null;
 		for(CoordinatorCEPChannel tCEP : mAnnouncedCEPs) {
 			ClusterName tClusterName = tCEP.getRemoteClusterName();
 			ICluster tRemoteCluster = getHRMController().getCluster(tClusterName) != null ? getHRMController().getCluster(tClusterName) : tClusterName;
 			if(pCluster.getHierarchyLevel() == tRemoteCluster.getHierarchyLevel()) {
 				List<RoutableClusterGraphLink> tConnection = getHRMController().getRoutableClusterGraph().getRoute(pCluster, tRemoteCluster);
 				int tDistance = 0;
 				if(tConnection != null) {
 					tDistance = tConnection.size();
 				}
 				if(tDistance < tClosestCluster) {
 					tClosestCluster = tDistance;
 					tClosest = tCEP;
 				}
 			}
 		}
 		if(tClosest == null) {
 			Logging.err(this, "Would return no announced CEP");
 			tClosest = mAnnouncedCEPs.getFirst();
 		}
 		return tClosest;
 	}
 	
 	public LinkedList<CoordinatorCEPChannel> getAnnouncedCEPs()
 	{
 		return mAnnouncedCEPs;
 	}
 	
 	public void setAnnouncer(Name pAnnouncer)
 	{
 		Logging.log(this, "Announcer is " + pAnnouncer);
 		mAnnouncer = pAnnouncer;
 	}
 	
 	public void setSourceIntermediate(Cluster pIntermediate)
 	{
 		mSourceIntermediateCluster = pIntermediate;
 	}
 	
 	public Cluster getSourceIntermediateCluster()
 	{
 		return mSourceIntermediateCluster;
 	}
 
 	@Override
 	public void handleTopologyData(TopologyData pEnvelope)
 	{
 		
 	}
 
 	@Override
 	public void handleBullyAnnounce(BullyAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 		
 	}
 
 	@Override
 	public CoordinatorCEPMultiplexer getMultiplexer()
 	{
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
 		
 	}
 
 	@Override
 	public Object getDecorationValue()
 	{
 		return Float.valueOf(0.6f);
 	}
 
 	@Override
 	public void setDecorationValue(Object pValue)
 	{
 		
 	}
 
 	public int hashCode()
 	{
 		return mClusterID.intValue() * 1;
 	}
 
 	@Override
 	public TopologyData getTopologyData()
 	{
 		return null;
 	}	
 }
