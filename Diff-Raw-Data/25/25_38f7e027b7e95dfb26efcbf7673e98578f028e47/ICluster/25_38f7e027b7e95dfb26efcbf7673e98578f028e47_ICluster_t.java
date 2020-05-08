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
 import de.tuilmenau.ics.fog.packets.hierarchical.NeighborClusterAnnounce;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyData;
 import de.tuilmenau.ics.fog.packets.hierarchical.election.BullyAnnounce;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPChannel;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.CoordinatorCEPMultiplexer;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 
 public interface ICluster extends Serializable, IRoutableClusterGraphTargetName
 {
 	/**
 	 * Sets the priority of the currently acting coordinator of this cluster.
 	 * 
 	 * @param pCoordinatorPriority is the priority of the entity
 	 */
 	public void setCoordinatorPriority(BullyPriority pCoordinatorPriority);
 	
 	/**
 	 * Get the priority of the currently acting coordinator
 	 * 
 	 * @return priority of the acting coordinator
 	 */
 	public BullyPriority getCoordinatorPriority();
 	
 	/**
 	 * Set the priority the reference node has in this cluster
 	 * 
 	 * @param pPriority
 	 */
 	public void setPriority(BullyPriority pPriority);
 	
 	/**
 	 * 
 	 * @return The priority of the node that is associated to this cluster is return here
 	 */
 	public BullyPriority getBullyPriority();
 
 	/**
 	 *  
 	 * @return Return the HRM controller of a node that is used for cluster handling etc.
 	 */
 	public HRMController getHRMController();
 	
 	/**
 	 * 
 	 * @return Provide list of connection end points that are connected to this cluster - the other connection end point
 	 * governs the cluster it is associated to
 	 */
	public LinkedList<CoordinatorCEPChannel> getClusterMembers();
 	
 	/**
 	 * 
 	 * @param pParticipatingCEP This is one connection end point that is used to communicate with the remote
 	 * connection end points that govern the clusters they are associated to.
 	 */
 	public void addParticipatingCEP(CoordinatorCEPChannel pParticipatingCEP);
 	
 	/**
 	 * 
 	 * @return The ID of the cluster is returned here.
 	 */
 	public Long getClusterID();
 	
 	/**
 	 * 
 	 * @return The hierarchical level the cluster acts on is returned here.
 	 */
 	public HierarchyLevel getHierarchyLevel();
 	
 	/**
 	 * 
 	 * @return The name of the coordinator that is responsible for this cluster is return here.
 	 */
 	public Name getCoordinatorName();
 	
 	/**
 	 * 
 	 * @return If you suffer from problems with the computation of the shortest route use this method instead of
 	 * the toString method
 	 */
 	public String getClusterDescription();
 
 	/**
 	 * 
 	 * @param pCoordName This is the name of the coordinator that is responsible for this cluster
 	 */
 	public void setCoordinatorName(Name pCoordName);
 	
 	/**
 	 * 
 	 * @return Get the address of the coordinator - is corresponds to a OSI-Layer 2 address. However implementation allows
 	 * the use of hierarchical addresses as well.
 	 */
 	public HRMName getCoordinatorsAddress();
 	
 	/**
 	 * 
 	 * @param pToken Set the token that is sent by the coordinator here.
 	 */
 	public void setToken(int pToken);
 	
 	/**
 	 * 
 	 * @return The token that is used for additional identification of the cluster is provided here.
 	 */
 	public int getToken();
 	
 	/**
 	 * 
 	 * @return The neighbor clusters of this entity are provided here.
 	 */
 	public LinkedList<ICluster> getNeighbors();
 	
 	/**
 	 * 
 	 * @return Find out which signature the coordinator has.
 	 */
 	public HRMSignature getCoordinatorSignature();
 	
 	/**
 	 * 
 	 * @param pHighestPriority This is the highest priority that was found for the set of participating node.
 	 */
 	public void setHighestPriority(BullyPriority pHighestPriority);
 
 	/**
 	 * 
 	 * @return The concurrently known highest priority of all priorities provided by the connection end points
 	 * is provided here.
 	 */
 	public BullyPriority getHighestPriority();
 	
 	/**
 	 * Once a connection end point receives an announcement it calls this method.
 	 * 
 	 * @param pAnnounce This is the announcement from which relevant information has to be extracted.
 	 * @param pCEP This is the connection from which necessary information will be extracted as well.
 	 */
 	public void handleBullyAnnounce(BullyAnnounce pAnnounce, CoordinatorCEPChannel pCEP);
 	
 	/**
 	 * This method has to be called by an connection end point once it receives the announcement about the existence
 	 * of a neighbor zone. 
 	 * 
 	 * @param pAnnounce This is the message that contains important information about the neighbor zone.
 	 * @param pCEP This is the connection end point that is used to transfer data to the object that CEP governs
 	 */
 	public void handleNeighborAnnouncement(NeighborClusterAnnounce pAnnounce, CoordinatorCEPChannel pCEP);
 	
 	/**
 	 * 
 	 * @return Return null in case the node is coordinator itself, otherwise the connection end point that leads to the coordinator is returned. 
 	 */
 	public CoordinatorCEPChannel getCoordinatorCEP();
 	
 	/**
 	 * Once an announcement was received, the coordinator is set via this method. However this function does not have to be necessarily called
 	 * if a BullyAnnounce message was received.  
 	 * 
 	 * @param pCoord Provide the connection end point that leads to the coordinator here.
 	 * @param pCoordSignature Provide the signature you received from the coordinator here.
 	 * @param pCoordName Set the name of the coordinator here.
 	 * @param pAddress The address of the coordinator can be set here. It can be either an OSI Layer 2 address or a HRMID. However using the last type as address
 	 * was not implemented. 
 	 */
	public void setCoordinatorCEP(CoordinatorCEPChannel pCoord, HRMSignature pCoordSignature, Name pCoordName, int pCoordToken, HRMName pAddress);
 	
 	/**
 	 * 
 	 * @param pNeighbor This is the neighbor that has to be added to the cluster. 
 	 */
 	public void addNeighborCluster(ICluster pNeighbor);
 	
 	/**
 	 * 
 	 * @param pHRMID This is the HRMID that identifies this cluster. Depending on the hierarchical level of this cluster the last
 	 * parts of that address might be zero.
 	 */
 	public void setHRMID(Object pCaller, HRMID pHRMID);
 	
 	/**
 	 * 
 	 * 
 	 * @param pData The data that should be sent is provided by this serializable object.
 	 * @param pAlreadyInformed Please provide a list that saves entities that were already informed - this is used to have a workaround
 	 * for the ConcurrentModificationException.
 	 */
 	public void sendClusterBroadcast(Serializable pData, LinkedList<CoordinatorCEPChannel> pAlreadyInformed);
 	
 	/**
 	 * 
 	 * @param pObj Provide the object for comparison
 	 * @return Return true in case the objects are equal to each other.
 	 */
 	public boolean equals(Object pObj);
 	
 	/**
 	 * In the first implementation of HRM BGP can be used at a distinct hierarchical level. For that reason one has to provide the
 	 * information whether a cluster lies  between two autonomous systems. Maybe that functionality will be reimplemented later on.
 	 * 
 	 * @return true in case this cluster is between two autonomous systems.
 	 */
 	public boolean isInterASCluster();
 	
 	/**
 	 * 
 	 * Explicitly say that this cluster is between to autonomous systems.
 	 */
 	public void setInterASCluster();
 	
 	/**
 	 * HRM was implemented as distributed system. If a cluster is notified about the existence of a nother cluster it remembers which
 	 * connection end point announced that cluster. The implementation expects that in order to route to that cluster it has
 	 * to reach the announcer first. However the announcer can be updated in case a shorter route to the target cluster is found. 
 	 * 
 	 * @return Return the connection end point that announced this cluster via NeighborZoneAnnounce to another connection end point. 
 	 */
 //	public CoordinatorCEPChannel getNegotiatorCEP();
 	
 	/**
 	 * HRM was implemented as distributed system. If a cluster is notified about the existence of a nother cluster it remembers which
 	 * connection end point announced that cluster. The implementation expects that in order to route to that cluster it has
 	 * to reach the announcer first. However the announcer can be updated in case a shorter route to the target cluster is found. 
 	 * 
 	 * @param pCEP Set the connection end point that announced this cluster via NeighborZoneAnnounce to another connection end point. 
 	 */
 //	public void setNegotiatorCEP(CoordinatorCEPChannel pCEP);
 	
 	/**
 	 * TODO
 	 *  
 	 * @return
 	 */
 	public TopologyData getTopologyData();
 
 	/**
 	 * 
 	 * @param pData This object has to include the address that should be associated to the cluster along with several entries that
 	 * describe as to how that entity should be reached.
 	 */
 	public void handleTopologyData(TopologyData pData);
 	
 	/**
 	 * It is possible, to address more than one destination within one packet - for that purpose a multiplexer is used.
 	 * 
 	 * @return The multiplexer that is associated to this cluster is returned here.
 	 */
 	public CoordinatorCEPMultiplexer getMultiplexer();
 }
