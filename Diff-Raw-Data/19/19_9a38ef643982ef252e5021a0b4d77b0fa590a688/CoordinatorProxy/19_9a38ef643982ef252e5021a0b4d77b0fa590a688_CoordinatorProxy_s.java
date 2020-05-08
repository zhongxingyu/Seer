 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents a cluster member (can also be a cluster head).
  */
 public class CoordinatorProxy extends ClusterMember
 {
 	private static final long serialVersionUID = 5456243840140110970L;
 
 	/**
 	 * Stores the hop distance to the coordinator node.
 	 */
 	private int mDistance = -1;
 	
 	/**
 	 * Stores the L2 address of the node where the coordinator is located
 	 */
 	private L2Address mCoordinatorNodeL2Address = null;
 	
 	/**
 	 * Constructor
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pCoordinatorID the unique coordinator ID for this cluster
 	 * @param pCoordinatorNodeL2Address the node L2 address where the coordinator is located
 	 * @param pHopCount the hop count to the coordinator node
 	 */
 	private CoordinatorProxy(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID, int pCoordinatorID, L2Address pCoordinatorNodeL2Address, int pHopCount)
 	{	
 		super(pHRMController, pHierarchyLevel, pClusterID, pCoordinatorID, pCoordinatorNodeL2Address);
 
 		mDistance = pHopCount;
 		
 		// store the L2 address of the node where the coordinator is located
 		mCoordinatorNodeL2Address = pCoordinatorNodeL2Address;
 
 		//Logging.log(this, "CREATED");
 	}
 
 	/**
 	 * Factory function
 	 *  
 	 * @param pHRMController the local HRMController instance
 	 * @param pClusterName a ClusterName which includes the hierarchy level, the unique ID of this cluster, and the unique coordinator ID
 	 * @param pClusterID the unique ID of this cluster
 	 * @param pCoordinatorNodeL2Address the node L2 address where the coordinator is located
 	 * @param pHopCount the hop count to the coordinator node
 	 */
 	public static CoordinatorProxy create(HRMController pHRMController, ClusterName pClusterName, L2Address pCoordinatorNodeL2Address, int pHopCount)
 	{	
 		CoordinatorProxy tResult = new CoordinatorProxy(pHRMController, pClusterName.getHierarchyLevel(), pClusterName.getClusterID(), pClusterName.getCoordinatorID(), pCoordinatorNodeL2Address, pHopCount);
 		
 		Logging.log(tResult, "\n\n\n################ CREATED COORDINATOR PROXY at hierarchy level: " + (tResult.getHierarchyLevel().getValue()));
 
 		// register at HRMController's internal database
 		pHRMController.registerCoordinatorProxy(tResult);
 		
 		return tResult;
 	}
 
 	/**
 	 * EVENT: remote coordinator role invalid, triggered by ControlEntity::unregisterAnnouncedCoordinatorARG() if a coordinator invalidation is received, the reaction is:
 	 * 	 	1.) update the local ARG
 	 */
 	public void eventRemoteCoordinatorRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: Coordinator_Role_Invalid");
 
 		// register at HRMController's internal database
 		mHRMController.unregisterCoordinatorProxy(this);
 	}
 	
 	/**
 	 * Creates a ClusterName object which describes this coordinator
 	 * 
 	 * @return the new ClusterName object
 	 */
 	public ClusterName createCoordinatorName()
 	{
 		ClusterName tResult = null;
 		
 		tResult = new ClusterName(mHRMController, getHierarchyLevel(), getClusterID(), getCoordinatorID());
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the L2 address of the node where the coordinator is located
 	 * 
 	 * @return the L2 address
 	 */
 	public L2Address getCoordinatorNodeL2Address()
 	{
 		return mCoordinatorNodeL2Address; 
 	}
 
 	/**
 	 * Sets a new distance (hop count to the coordinator node)
 	 * 
 	 * @param pDistance the new distance
 	 */
 	public void setDistance(int pDistance)
 	{
 		if (mDistance != pDistance){
 			/**
 			 * Update the base node priority
 			 */
 			// are we at base hierarchy level
 			if(getHierarchyLevel().isBaseLevel()){
 				// distance is the init. value?
 				if(mDistance != -1){
 					// decrease base node priority
 					mHRMController.decreaseHierarchyNodePriority_KnownBaseCoordinator(this);
 				}
 	
 				Logging.log(this, "Updating the distance (hop count) to the coordinator node to: " + pDistance);
 				mDistance = pDistance;
 
 				// increase base node priority
 				mHRMController.increaseHierarchyNodePriority_KnownBaseCoordinator(this);
 			}else{
 				Logging.log(this, "Updating the distance (hop count) to the coordinator node to: " + pDistance);
 				mDistance = pDistance;
 			}
 			
 		}else{
 			// old value == new value
 		}
 	}
 	
 	/**
 	 * Returns the hop distance to the coordinator
 	 * 
 	 * @return the hop distance
 	 */
 	public int getDistance()
 	{
 		return mDistance;
 	}
 
 	/**
 	 * Defines the decoration text for the ARG viewer
 	 * 
 	 * @return text for the control entity or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return "RemoteCoordinator" + getGUICoordinatorID() + "@" + getHierarchyLevel().getValue() +  "(hops=" + mDistance + ", " + idToString() + ")";
 	}
 
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return toLocation() + "(" + idToString() + ")";
 	}
 
 	/**
 	 * Returns a location description about this instance
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + (getHierarchyLevel().getValue());
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns a string including the ClusterID, the token, and the node priority
 	 * 
 	 * @return the complex string
 	 */
 	private String idToString()
 	{
 		if ((getHRMID() == null) || (getHRMID().isRelativeAddress())){
 			return "Cluster" + getGUIClusterID() + ", Node.=" + getCoordinatorNodeL2Address();
 		}else{
 			return "Cluster" + getGUIClusterID() + ", Node.=" + getCoordinatorNodeL2Address() + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
