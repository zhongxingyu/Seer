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
 
 import java.io.Serializable;
 
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used to identify a cluster (independent from its physical location)
  */
 public class ClusterName extends ControlEntity implements Serializable, AbstractRoutingGraphNode
 {
 	private static final long serialVersionUID = 3027076881853652810L;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pHRMController the local HRMController instance (for accessing topology data)
 	 * @param pHierarchyLevel the hierarchy level
 	 * @param pClusterID the unique ID of the cluster
 	 * @param pCoordinatorID the unique ID of the coordinator
 	 */
 	public ClusterName(HRMController pHRMController, HierarchyLevel pHierarchyLevel, Long pClusterID, int pCoordinatorID)
 	{
 		super(pHRMController, pHierarchyLevel);
 		
 		Logging.log(this, "Creating ClusterName for cluster: " + pClusterID + " and coordinator: " + pCoordinatorID);
 
 		setClusterID(pClusterID);
 		setSuperiorCoordinatorID(pCoordinatorID);
 		setCoordinatorID(pCoordinatorID);
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
 		String tResult = "Cluster" + getGUIClusterID() + "@" + getHierarchyLevel().getValue();
 		
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
			return "Lvl=" + getHierarchyLevel().getValue() + ", ID=" + getClusterID() + ", CoordID=" + getCoordinatorID() +  ", Prio=" + getPriority().getValue();
 		}else{
 			return "HRMID=" + getHRMID().toString();
 		}
 	}
 }
