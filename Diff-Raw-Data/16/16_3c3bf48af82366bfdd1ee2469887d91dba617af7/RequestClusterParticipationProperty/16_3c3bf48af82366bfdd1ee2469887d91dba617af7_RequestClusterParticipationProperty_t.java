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
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.properties.AbstractProperty;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterName;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.HierarchyLevel;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used for describing a cluster, which both the connection source and the destination should 
  * have in common (the connection destination should join the described cluster). 
  */
 public class RequestClusterParticipationProperty extends AbstractProperty
 {
 	/**
 	 * Stores the hierarchy level of the cluster
 	 */
 	private HierarchyLevel mHierarchyLevel = null;
 	
 	/**
 	 * Stores the unique clusterID
 	 */
 	private Long mClusterID = null;
 			
 	/**
 	 * Stores the unique coordinator ID
 	 */
 	private int mCoordinatorID = 0;
 	
 	/**
 	 * Stores all registered cluster member descriptions
 	 */
 	private LinkedList<ClusterMemberDescription> mClusterMemberDescriptions = new LinkedList<ClusterMemberDescription>();
 
 	private Name mSourceName;
 	private HRMName mSourceAddress;
 	private static final long serialVersionUID = 7561293731302599090L;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pClusterID the already created unique ID for the cluster the sender and the receiver should be part of
 	 * @param pHierarchyLevel the hierarchy level of the cluster
 	 * @param pCoordinatorID the unique ID of the coordinator (or 0 if none exists)
 	 */
 	public RequestClusterParticipationProperty(Long pClusterID, HierarchyLevel pHierarchyLevel, int pCoordinatorID)
 	{
 		Logging.log(this, "Setting target cluster ID " + pClusterID);
		Logging.log(this, "Setting target coordinator ID " + pCoordinatorID);
 		mClusterID = pClusterID;
 		mHierarchyLevel = pHierarchyLevel;
 		mCoordinatorID = pCoordinatorID;
 		
 		Logging.log(this, "Setting cluster hierarchy level: " + pHierarchyLevel.getValue());
 	}
 	
 	/**
 	 * Returns the unique coordinator ID
 	 * 
 	 * @return the coordinator ID
 	 */
 	public int getCoordinatorID()
 	{
 		return mCoordinatorID;
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
 	 * Adds a description of a cluster member to the internal database
 	 * 
 	 * @param pClusterID the unique cluster ID
 	 * @param pToken the unique coordinator ID
 	 * @param pPriority the Bully priority of this cluster member
 	 */
 	public ClusterMemberDescription addClusterMember(Long pClusterID, int pToken, BullyPriority pPriority)
 	{
 		// create the new member
 		ClusterMemberDescription tResult = new ClusterMemberDescription(pClusterID, pToken, pPriority);
 
 		// add the cluster member to the database
 		Logging.log(this, "Adding cluster member description: " + tResult);
 
 		synchronized (mClusterMemberDescriptions) {
 			mClusterMemberDescriptions.add(tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of descriptions about known cluster members
 	 *  
 	 * @return the list of registered cluster members
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ClusterMemberDescription> getClusterMemberDescriptions()
 	{
 		LinkedList<ClusterMemberDescription> tResult = null;
 		
 		synchronized (mClusterMemberDescriptions) {
 			tResult = (LinkedList<ClusterMemberDescription>) mClusterMemberDescriptions.clone();
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
 		String tResult = getClass().getSimpleName() + "(ClusterID=" + mClusterID + ", CoordID=" + mCoordinatorID + ", ";
 		
 		synchronized (mClusterMemberDescriptions) {
 			tResult += mClusterMemberDescriptions.size() + " member descriptions)";
 			
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
 		 * Stores the unique ID of the cluster
 		 */
 		private Long mClusterID;
 		
 		/**
 		 * Stores the unique ID of the coordinator 
 		 */
 		private int mCoordinatorID;
 
 		private ClusterName mPredecessor;
 		private LinkedList<DiscoveryEntry> mDiscoveries;
 		private HierarchyLevel mHierarchyLevel = null;
 		private BullyPriority mPriority = null;
 		
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
 		 * @param pLevel Set the level of the source cluster here. In general this is one level below the level of
 		 * the cluster that should be joined. So the level of this nested exception is below the level of the
 		 * ClusterParticipationProperty this nested participation is part of.
 		 */
 		public void setHierarchyLevel(HierarchyLevel pHierarchyLevel)
 		{
 			Logging.log(this, "Setting cluster member hierarchy level: " + pHierarchyLevel.getValue());
 			
 			mHierarchyLevel = pHierarchyLevel;
 		}
 		
 		/**
 		 * Get the level of the source cluster here. In general this is one level below the level of
 		 * the cluster that should be joined. So the level of this nested exception is below the level of the
 		 * ClusterParticipationProperty this nested participation is part of.
 		 * 
 		 * @return The level of the cluster that should participate is returned.
 		 */
 		public HierarchyLevel getHierarchyLevel()
 		{
 			return mHierarchyLevel;
 		}
 		
 		/**
 		 * 
 		 * @param pAddress This is the address of the node that is about to join the cluster.
 		 */
 		public void setSourceL2Address(HRMName pAddress)
 		{
 			mSourceAddress = pAddress;
 		}
 		
 //		/**
 //		 * 
 //		 * @param pPredecessor This has to be the second last cluster of the path to the target cluster. Once the target interprets
 //		 * that cluster it knows which "outgoing" cluster should be used. in order to reach the node that generated the participation
 //		 * property.
 //		 */
 //		public void setPredecessor(ClusterName pPredecessor)
 //		{
 //			mPredecessor = pPredecessor;
 //		}
 //		
 //		/**
 //		 * 
 //		 * @return This is be the second last cluster of the path to the target cluster. Once the target interprets
 //		 * this cluster it knows which "outgoing" cluster should be used. in order to reach the node that generated the participation
 //		 * property.
 //		 * 
 //		 */
 //		public ClusterName getPredecessor()
 //		{
 //			return mPredecessor;
 //		}
 		
 		/**
 		 * 
 		 * @param pSource This is the name of the host or node that is about to join the target cluster.
 		 */
 		public void setSourceName(Name pSource)
 		{
 			mSourceName = pSource;
 		}
 		
 		/**
 		 * 
 		 * @return This is the name of the host or node that is about to join the target cluster.
 		 */
 		public Name getSourceName()
 		{
 			return mSourceName;
 		}
 		
 		/**
 		 * 
 		 * @return The address of the entity that wishes to become member of the cluster is returned. 
 		 */
 		public HRMName getSourceL2Address()
 		{
 			return mSourceAddress;
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
