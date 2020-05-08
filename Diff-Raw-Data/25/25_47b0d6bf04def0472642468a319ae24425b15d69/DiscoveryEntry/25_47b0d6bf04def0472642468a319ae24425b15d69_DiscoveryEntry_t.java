 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterName;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 
 /**
  * 
  * object that is used for information exchange on newly discovered clusters
  */
 public class DiscoveryEntry implements Serializable
 {
 	private static final long serialVersionUID = 3728552024939381816L;
 	private Name mCoordinatorName;
 	private LinkedList<RoutingServiceLinkVector> mRoutingVectors;
 	private int mToken;
 	private Long mClusterID;
	private L2Address mCoordinatorL2Address;
 	private BullyPriority mPriority = null;
 	private HierarchyLevel mLevel = null;
 	private int mClusterHops;
 	private ClusterName mPredecessor;
 	
 	/**
 	 * 
 	 * @param pToken is the token of the cluster that will be reported
 	 * @param pCoordinatorName is the name of the coordinator that will be reported
 	 * @param pClusterID is the cluster ID of the cluster that will be reported
 	 * @param pCoordinatorAddress is the address of the coordinator
 	 */
	public DiscoveryEntry(int pToken, Name pCoordinatorName, Long pClusterID, L2Address pCoordinatorAddress, HierarchyLevel pLevel)
 	{
 		setToken(pToken);
 		setCoordinatorName(pCoordinatorName);
 		setClusterID(pClusterID);
		mCoordinatorL2Address = pCoordinatorAddress;
 		mLevel = pLevel;
 		mPriority = new BullyPriority(this);
 	}
 	
 	/**
 	 * 
 	 * @param pDummy contains cluster identification of the first cluster along the path to the entity that sent the discovery request
 	 */
 	public void setPredecessor(ClusterName pDummy)
 	{
 		mPredecessor = pDummy;
 	}
 	
 	/**
 	 * 
 	 * @return cluster identification of the first cluster along the path to the entity that sent the discovery request
 	 */
 	public ClusterName getPredecessor()
 	{
 		return mPredecessor;
 	}
 	
 	/**
 	 * 
 	 * @return number of cluster hops along the shortest known path from the source cluster to the target
 	 */
 	public int getClusterHops()
 	{
 		return mClusterHops;
 	}
 	
 	/**
 	 * 
 	 * @param pHops number of cluster hops along the shortest known path from the source cluster to the target
 	 */
 	public void setClusterHops(int pHops)
 	{
 		mClusterHops = pHops;
 	}
 	
 	/**
 	 * 
 	 * @return level at which discovered cluster is found
 	 */
 	public HierarchyLevel getLevel()
 	{
 		return mLevel;
 	}
 
 	/**
 	 * 
 	 * @param pPriority is the priority of the coordinator that represents the target cluster
 	 */
 	public void setPriority(BullyPriority pPriority)
 	{
 		mPriority = pPriority;
 	}
 	
 	/**
 	 * 
 	 * @return priority of the coordinator that represents the target cluster
 	 */
 	public BullyPriority getPriority()
 	{
 		return mPriority;
 	}
 	
 	/**
 	 * 
 	 * @return physical name of host identification of the node that is coordinator
 	 */
 	public Name getCoordinatorName()
 	{
 		return mCoordinatorName;
 	}
 
 	/**
 	 * 
 	 * @param pCoordinatorName set physical name of host identification of the node that is coordinator
 	 */
 	public void setCoordinatorName(Name pCoordinatorName)
 	{
 		mCoordinatorName = pCoordinatorName;
 	}
 
 	/**
 	 * 
 	 * @return routing vectors that are necessary to reach the target coordinator from the host that reports the new discovered cluster
 	 */
 	public LinkedList<RoutingServiceLinkVector> getRoutingVectors()
 	{
 		return mRoutingVectors;
 	}
 
 	/**
 	 * 
 	 * @param pRoutingVector add routing vectors that are necessary to reach the target coordinator from the host that reports the new discovered cluster
 	 */
 	public void addRoutingVectors(RoutingServiceLinkVector pRoutingVector)
 	{
 		if(mRoutingVectors == null) {
 			mRoutingVectors = new LinkedList<RoutingServiceLinkVector>();
 		}
 		mRoutingVectors.add(pRoutingVector);
 	}
 	
 	/**
 	 * 
 	 * @param pVectors set routing vectors that are necessary to reach the target coordinator from the host that reports the new discovered cluster
 	 */
 	public void setRoutingVectors(LinkedList<RoutingServiceLinkVector> pVectors)
 	{
 		mRoutingVectors = pVectors;
 	}
 
 	/**
 	 * 
 	 * @return token of the cluster that is is reported for discovery
 	 */
 	public int getToken()
 	{
 		return mToken;
 	}
 
 	/**
 	 * 
 	 * @param pToken to set token of the cluster that is reported for discovery
 	 */
 	public void setToken(int pToken)
 	{
 		mToken = pToken;
 	}
 
 	/**
 	 * 
 	 * @return cluster ID of cluster that is reported for discovery
 	 */
 	public Long getClusterID()
 	{
 		return mClusterID;
 	}
 
 	/**
 	 * 
 	 * @param pClusterID cluster ID of cluster that is reported for discovery
 	 */
 	public void setClusterID(Long pClusterID)
 	{
 		mClusterID = pClusterID;
 	}
 
 	/**
 	 * 
 	 * @return physical name of the coordinator
 	 */
	public L2Address getCoordinatorRoutingAddress()
 	{
		return mCoordinatorL2Address;
 	}
 	
 	public String toString()
 	{
 		return getClass().getSimpleName() + "ID(" + mClusterID + ")COORD(" + mCoordinatorName + ")+VECTORS(" + mRoutingVectors + ")HOPS(" + mClusterHops + ")";
 	}
 }
