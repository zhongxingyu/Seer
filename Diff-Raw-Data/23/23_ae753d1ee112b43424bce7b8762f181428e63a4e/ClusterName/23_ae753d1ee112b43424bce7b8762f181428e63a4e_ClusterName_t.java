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
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 //import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPMultiplexer;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 
 public class ClusterName implements Serializable, ICluster
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3027076881853652810L;
 	private int mToken;
 	private Long mClusterID;
 	private HierarchyLevel mHierarchyLevel = null;
 	
 	/**
 	 * 
 	 * @param pToken token of the cluster
 	 * @param pClusterID ID of the cluster
 	 * @param pLevel level of the cluster
 	 */
 	public ClusterName(int pToken, Long pClusterID, HierarchyLevel pLevel)
 	{
 		mClusterID = pClusterID;
 		mToken = pToken;
 		mHierarchyLevel = pLevel;
 	}
 	
 	@Override
 	public String toString()
 	{
		return "\"(ID=" + mClusterID + ", Tok=" + mToken + (mHierarchyLevel != null ? ", HierLvl.=" + mHierarchyLevel.getValue() : "") + ")\""; 
 	}
 
 	@Override
 	public HRMID getHrmID() {
 		return null;
 	}
 
 	@Override
 	public Namespace getNamespace() {
 		return null;
 	}
 
 	@Override
 	public int getSerialisedSize() {
 		return 0;
 	}
 
 	@Override
 	public void setCoordinatorPriority(BullyPriority pCoordinatorPriority) {		
 	}
 
 	@Override
 	public BullyPriority getNodePriority() {
 		return null;
 	}
 
 	@Override
 	public void setPriority(BullyPriority pPriority) {
 		
 	}
 
 	@Override
 	public HRMController getHRMController() {
 		return null;
 	}
 
 	@Override
 	public LinkedList<CoordinatorCEPChannel> getParticipatingCEPs() {
 		return null;
 	}
 
 	@Override
 	public void addParticipatingCEP(CoordinatorCEPChannel pParticipatingCEP) {
 		
 	}
 
 	@Override
 	public Long getClusterID() {
 		return mClusterID;
 	}
 
 	@Override
 	public HierarchyLevel getHierarchyLevel() {
 		return mHierarchyLevel;
 	}
 
 	@Override
 	public Name getCoordinatorName() {
 		return null;
 	}
 
 	@Override
 	public BullyPriority getBullyPriority() {
 		return null;
 	}
 
 	@Override
 	public String getClusterDescription() {
 		return null;
 	}
 
 	@Override
 	public void setCoordinatorName(Name pCoordName) {
 		
 	}
 
 	@Override
 	public HRMID getCoordinatorsAddress() {
 		return null;
 	}
 
 	@Override
 	public void setToken(int pToken) {
 		
 	}
 
 	@Override
 	public int getToken() {
 		return mToken;
 	}
 
 	@Override
 	public LinkedList<ICluster> getNeighbors() {
 		return null;
 	}
 
 	@Override
 	public HRMSignature getCoordinatorSignature() {
 		return null;
 	}
 
 	@Override
 	public BullyPriority getHighestPriority()
 	{
 		return null;
 	}
 
 	@Override
 	public void handleAnnouncement(NeighborClusterAnnounce pAnnounce, CoordinatorCEPChannel pCEP)
 	{
 	}
 
 	@Override
 	public CoordinatorCEPChannel getCoordinatorCEP()
 	{
 		return null;
 	}
 
 	@Override
 	public void setCoordinatorCEP(CoordinatorCEPChannel pCoord, HRMSignature pCoordSignature, Name pCoordName, HRMName pAddress)
 	{
 		
 	}
 
 	@Override
 	public void addNeighborCluster(ICluster pNeighbor)
 	{
 		
 	}
 
 	@Override
 	public void setHRMID(HRMID pHRMID)
 	{
 		
 	}
 
 	@Override
 	public void setHighestPriority(BullyPriority pHighestPriority) {
 		
 	}
 
 	@Override
 	public void sendClusterBroadcast(Serializable pData, LinkedList<CoordinatorCEPChannel> pAlreadyInformed)
 	{
 		
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
 	public LinkedList<CoordinatorCEPChannel> getLaggards()
 	{
 		return null;
 	}
 
 	@Override
 	public void addLaggard(CoordinatorCEPChannel pCEP)
 	{
 		
 	}
 
 	@Override
 	public CoordinatorCEPChannel getNegotiatorCEP()
 	{
 		return null;
 	}
 
 	@Override
 	public void setNegotiatorCEP(CoordinatorCEPChannel pCEP)
 	{
 		
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
 	public TopologyData getTopologyData()
 	{
 		return null;
 	}
 }
