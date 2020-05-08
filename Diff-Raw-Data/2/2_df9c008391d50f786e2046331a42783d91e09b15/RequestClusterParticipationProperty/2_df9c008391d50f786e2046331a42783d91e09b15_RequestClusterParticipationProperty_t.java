 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.properties;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.properties.AbstractProperty;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used for describing a cluster, which both the connection source and the destination should 
  * have in common (the connection destination should join the described cluster). 
  * 
  * Additionally, the sender describes local cluster members, which have already joined this new cluster.
  * 
  */
 public class RequestClusterParticipationProperty extends AbstractProperty
 {
 	/**
 	 * Stores the hierarchy level of the cluster
 	 */
 	private HierarchyLevel mHierarchyLevel = new HierarchyLevel(this, -1);
 	
 	/**
 	 * Stores the hierarchy level of the sender
 	 */
 	private HierarchyLevel mSenderHierarchyLevel = new HierarchyLevel(this,  -1);
 	
 	/**
 	 * Stores the unique clusterID
 	 */
 	private Long mClusterID = null;
 			
 	/**
 	 * Stores the FoG name of the node where the sender is located 
 	 */
 	private Name mSenderNodeName = null;
 	
 	/**
 	 * Stores the L2Address of the node where the sender is located
 	 */
 	private L2Address mSenderL2Address = null;
 
 	/**
 	 * Stores all registered cluster member descriptions
 	 */
 	private LinkedList<ClusterMemberDescription> mSenderClusterMembers = new LinkedList<ClusterMemberDescription>();
 
 	private static final long serialVersionUID = 7561293731302599090L;
 	
 	/**
 	 * Factory function
 	 * 
 	 * @param pHRMController the HRMController of the current node
 	 * @param pSenderHierarchyLevel the hierarchy level of the sender
 	 * @param pClusterID the already created unique ID for the cluster the sender and the receiver should be part of
 	 * @param pHierarchyLevel the hierarchy level of the cluster
 	 */
 	public static RequestClusterParticipationProperty create(HRMController pHRMController, HierarchyLevel pSenderHierarchyLevel, Long pClusterID, HierarchyLevel pHierarchyLevel)
 	{
 		// get the recursive FoG layer
 		FoGEntity tFoGLayer = (FoGEntity) pHRMController.getNode().getLayer(FoGEntity.class);
 
 		// get the central FN of this node
 		L2Address tThisHostL2Address = pHRMController.getHRS().getL2AddressFor(tFoGLayer.getCentralFN());
 	
 		RequestClusterParticipationProperty tResult = new RequestClusterParticipationProperty(pHRMController.getNodeName(), tThisHostL2Address, pSenderHierarchyLevel, pClusterID, pHierarchyLevel);
 		
 		return tResult;
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderNodeName the FoG name of the node where the sender is located
 	 * @param pSenderL2Address the L2Adress of the node where the sender is located
 	 * @param pSenderHierarchyLevel the hierarchy level of the sender
 	 * @param pClusterID the already created unique ID for the cluster the sender and the receiver should be part of
 	 * @param pHierarchyLevel the hierarchy level of the new cluster
 	 */
 	private RequestClusterParticipationProperty(Name pSenderNodeName, L2Address pSenderL2Address, HierarchyLevel pSenderHierarchyLevel, Long pClusterID, HierarchyLevel pHierarchyLevel)
 	{
 		Logging.log(this, "Setting sender node name: " + pSenderNodeName.toString());
 		Logging.log(this, "Setting sender L2Address: " + pSenderL2Address);
 		Logging.log(this, "Setting sender hierarchy level: " + pSenderHierarchyLevel.getValue());
 		Logging.log(this, "Setting target cluster ID: " + pClusterID);
 		Logging.log(this, "Setting cluster hierarchy level: " + pHierarchyLevel.getValue());
 		mSenderNodeName = pSenderNodeName;
 		mSenderL2Address = pSenderL2Address;
 		mSenderHierarchyLevel = pSenderHierarchyLevel;
 		mClusterID = pClusterID;
 		mHierarchyLevel = pHierarchyLevel;
 	}
 	
 	/**
 	 * Returns the unique cluster ID
 	 * 
 	 * @return the unique cluster ID 
 	 */
 	public Long getClusterID()
 	{
 		return mClusterID;
 	}
 	
 	/**
 	 * Returns the hierarchy level
 	 * 
 	 * @return the hierarchy level
 	 */
 	public HierarchyLevel getHierarchyLevel()
 	{
 		return mHierarchyLevel;
 	}
 	
 	/**
 	 * Returns the hierarchy level of the sender
 	 * 
 	 * @return the hierarchy level of the sender
 	 */
 	public HierarchyLevel getSenderHierarchyLevel()
 	{
 		return mSenderHierarchyLevel;
 	}
 
 	/**
 	 * Returns the FoG name of the node where the sender is located
 	 *  
 	 * @return the FoG name of the node where the sender is located
 	 */
 	public Name getSenderNodeName()
 	{
 		return mSenderNodeName;
 	}
 	
 	/**
 	 * 
 	 * Returns the L2Address of the node where the sender is located
 	 * 
 	 * @return the L2Address of the node where the sender is located 
 	 */
	public L2Address getSenderL2Address()
 	{
 		return mSenderL2Address;
 	}
 
 	/**
 	 * Adds a description of a member (local coordinator) to the future common cluster to the internal database.
 	 * For the base hierarchy level this is a cluster at sender side.
 	 * For a higher hierarchy level this is a coordinator which should be part of the future common cluster.
 	 * 
 	 * @param pSenderClusterMember the cluster member at sender side
 	 */
 	public ClusterMemberDescription addSenderClusterMember(Cluster pSenderClusterMember)
 	{
 		BullyPriority tClusterMemberPriority = null;
 		if(pSenderClusterMember.getHierarchyLevel().isBaseLevel()){
 			tClusterMemberPriority = pSenderClusterMember.getPriority();
 		}else{ // higher hierarchy level
 			if(pSenderClusterMember.getCoordinator() != null){
 				pSenderClusterMember.getCoordinator().getPriority();
 			}else{
 				Logging.err(this, "Coordinator of sender's cluster member should be defined here, cluster member: " + pSenderClusterMember);
 			}			
 		}
 		
 		// create the new member
 		ClusterMemberDescription tResult = new ClusterMemberDescription(pSenderClusterMember.getClusterID(), pSenderClusterMember.getCoordinatorID(), tClusterMemberPriority);
 
 		// add the cluster member to the database
 		Logging.log(this, "Adding sender's cluster member: " + tResult);
 
 		synchronized (mSenderClusterMembers) {
 			mSenderClusterMembers.add(tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of descriptions about known cluster members at sender side.
 	 * For the base hierarchy level, the sender describes its local level 0 cluster.
 	 * For higher hierarchy levels, the sender describes all its local coordinators for this new cluster.
 	 *  
 	 * @return the list of cluster members at sender side
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ClusterMemberDescription> getSenderClusterMembers()
 	{
 		LinkedList<ClusterMemberDescription> tResult = null;
 		
 		synchronized (mSenderClusterMembers) {
 			tResult = (LinkedList<ClusterMemberDescription>) mSenderClusterMembers.clone();
 		}
 		
 		return tResult;		
 	}
 	
 	/**
 	 * Generates a descriptive string about the object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		String tResult = getClass().getSimpleName() + "(ClusterID=" + mClusterID + ", HierLvl.=" + getHierarchyLevel().getValue() + ", ";
 		
 		synchronized (mSenderClusterMembers) {
 			tResult += mSenderClusterMembers.size() + " member(s))";
 			
 //			int i = 0;
 //			for (ClusterMemberDescription tEntry : mClusterMemberDescriptions){
 //				tResult += "\n      ..[" + i + "]: " + tEntry.toString();
 //				i++;
 //			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * This class is used to describe a cluster member of the cluster, which is described by the parent ClusterDescriptionProperty.
 	 */
 	public class ClusterMemberDescription implements Serializable
 	{
 		private static final long serialVersionUID = -6712697028015706544L;
 
 		/**
 		 * Stores the unique ID of the cluster at sender side
 		 */
 		private Long mClusterID;
 		
 		/**
 		 * Stores the unique ID of the coordinator 
 		 */
 		private int mCoordinatorID;
 
 		/**
 		 * Stores the Bully priority of this cluster member
 		 */
 		private BullyPriority mPriority = null;
 
 		private LinkedList<DiscoveryEntry> mDiscoveries;
 		
 		/**
 		 * Constructor
 		 *  
 		 * @param pClusterID the unique ID of the cluster
 		 * @param pCoordinatorID the unique ID of the coordinator
 		 */
 		private ClusterMemberDescription(Long pClusterID, int pCoordinatorID, BullyPriority pPriority)
 		{
 			mClusterID = pClusterID;
 			mCoordinatorID = pCoordinatorID;
 			mPriority = pPriority;
 		}
 		
 		/**
 		 * 
 		 * @return This is the priority of the cluster member. It is already here transmitted to
 		 * decrease communication complexity.
 		 */
 		public BullyPriority getPriority()
 		{
 			return mPriority;
 		}
 		
 		/**
 		 * 
 		 * @return The token of the cluster the coordinator is responsible for is returned here.
 		 */
 		public int getCoordinatorID()
 		{
 			return mCoordinatorID;
 		}
 		
 		/**
 		 * As the target cluster/coordinator has to be informed about the topology and especially has to receive
 		 * the knowledge as to how the source node of the participation request can be reached.
 		 * 
 		 * @param pEntry
 		 */
 		public void addDiscoveryEntry(DiscoveryEntry pEntry)
 		{
 			if(mDiscoveries == null) {
 				mDiscoveries = new LinkedList<DiscoveryEntry>();
 				mDiscoveries.add(pEntry);
 			} else {
 				mDiscoveries.add(pEntry);
 			}
 		}
 		
 		/**
 		 * 
 		 * @return The neighbors of the source node are returned by this method.
 		 */
 		public LinkedList<DiscoveryEntry> getNeighbors()
 		{
 			return mDiscoveries;
 		}
 		
 		/**
 		 * 
 		 * @return The cluster identity the coordinator represents is returned.
 		 */
 		public Long getClusterID()
 		{
 			return mClusterID;
 		}
 		
 		/**
 		 * Returns a descriptive string about this object
 		 * 
 		 * @return the descriptive string
 		 */
 		public String toString()
 		{
 			return getClass().getSimpleName() + "(ClusterID=" + getClusterID() + ", CoordID=" + getCoordinatorID() + (getPriority() != null ? ", PeerPrio=" + getPriority().getValue() : "") + ")";
 		}
 	}
 }
