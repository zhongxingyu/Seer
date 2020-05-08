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
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.packets.hierarchical.ClusterDiscovery;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.MultiplexedPackage;
 import de.tuilmenau.ics.fog.packets.hierarchical.ClusterDiscovery.NestedDiscovery;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterDummy;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.NeighborCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.RoutableClusterGraphLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.ClusterParticipationProperty;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.ClusterParticipationProperty.NestedParticipation;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.Tuple;
 
 public class CoordinatorCEPMultiplexer
 {
 	private HashMap<CoordinatorCEPDemultiplexed, CoordinatorCEP> mMultiplexer;
 	private HashMap<CoordinatorCEP, LinkedList<CoordinatorCEPDemultiplexed>> mDemux;
 	private HashMap<Tuple<Long, Long>, CoordinatorCEPDemultiplexed> mClusterToCEPMapping;
 	private HRMController mHRMController = null;
 	private LinkedList<Name> mConnectedEntities = new LinkedList<Name>();
 	private ICluster mCluster;
 	
 	public CoordinatorCEPMultiplexer(HRMController pHRMController)
 	{
 		mHRMController = pHRMController;
 		mMultiplexer = new HashMap<CoordinatorCEPDemultiplexed, CoordinatorCEP>();
 		mDemux = new HashMap<CoordinatorCEP, LinkedList<CoordinatorCEPDemultiplexed>>();
 		mClusterToCEPMapping = new HashMap<Tuple<Long, Long>, CoordinatorCEPDemultiplexed>();
 	}
 	
 	public CoordinatorCEPDemultiplexed addConnection(ICluster pTargetCluster, ICluster pSourceCluster)
 	{
 		Name tName = pTargetCluster.getCoordinatorName();
 		CoordinatorCEPDemultiplexed tCEPDemultiplexed = null;
 //		long tAddress=0;
 
 		if(!mConnectedEntities.contains(pTargetCluster.getCoordinatorName())) {
 			mConnectedEntities.add(pTargetCluster.getCoordinatorName());
 			ClusterParticipationProperty tParticipationProperty = new ClusterParticipationProperty(pTargetCluster.getCoordinatorsAddress().getAddress().longValue(), pTargetCluster.getHierarchyLevel() + 1, pTargetCluster.getToken());
 			CoordinatorCEP tCEP = new CoordinatorCEP(getLogger(), mHRMController, false, pSourceCluster.getHierarchyLevel() + 1, mHRMController.getMultiplexerOnLevel(pSourceCluster.getHierarchyLevel() + 1));
 			ClusterDiscovery tBigDiscovery = new ClusterDiscovery(mHRMController.getPhysicalNode().getCentralFN().getName());
 			
 			for(Coordinator tManager : mHRMController.getClusterManagers(pSourceCluster.getHierarchyLevel()+1)) {
 				tCEPDemultiplexed = new CoordinatorCEPDemultiplexed(getLogger(), mHRMController, tManager);
 				tCEPDemultiplexed.setPeerPriority(pTargetCluster.getPriority());
 				tCEP.getMultiplexer().addMultiplexedConnection(tCEPDemultiplexed, tCEP);
 				tCEP.getMultiplexer().addDemultiplex(tCEP, tCEPDemultiplexed);
 				synchronized(mClusterToCEPMapping) {
 					getLogger().log(this, "Registering multiplex" + tManager.getClusterID() + " to " + pTargetCluster.getClusterID() + " with connection endpoint " + tCEPDemultiplexed);
 					mClusterToCEPMapping.put(new Tuple<Long, Long>(tManager.getClusterID(), pTargetCluster.getClusterID()), tCEPDemultiplexed);
 				}
 				tCEPDemultiplexed.setRemoteCluster(pTargetCluster);
 			}
 			
 			for(Coordinator tManager : mHRMController.getClusterManagers(pSourceCluster.getHierarchyLevel()+1)) {
 				if(pTargetCluster.getCoordinatorsAddress() == null) {
 					getLogger().err(this, "Error on trying to contact other clusters, as name is set please check its address");
 				} else {
 					NestedParticipation tParticipate = tParticipationProperty.new NestedParticipation(pTargetCluster.getClusterID(), pTargetCluster.getToken());
 					tParticipationProperty.addNestedparticipation(tParticipate);
 					tParticipate.setSenderPriority(tManager.getManagedCluster().getPriority());
 //					tAddress = pTargetCluster.getCoordinatorsAddress().getAddress().longValue();
 					
 					tParticipate.setSourceClusterID(tManager.getManagedCluster().getClusterID());
 					tParticipate.setSourceToken(tManager.getManagedCluster().getToken());
 					tParticipate.setSourceName(mHRMController.getPhysicalNode().getCentralFN().getName());
 					tParticipate.setSourceRoutingServiceAddress(tCEP.getSourceRoutingServiceAddress());
 					
 					List<RoutableClusterGraphLink> tClusterListToRemote = mHRMController.getRoutableClusterGraph().getRoute(tManager.getManagedCluster(), pTargetCluster);
 					if(!tClusterListToRemote.isEmpty()) {
 						/*
 						 * we need the last hop in direct to the neighbor
 						 */
 						ICluster tPredecessorToRemote = (ICluster) mHRMController.getRoutableClusterGraph().getDest(pTargetCluster, tClusterListToRemote.get(tClusterListToRemote.size()-1));
 						tParticipate.setPredecessor(ClusterDummy.compare(tPredecessorToRemote.getClusterID(), tPredecessorToRemote.getToken(), tPredecessorToRemote.getHierarchyLevel()));
 						getLogger().log(this, "Successfully set predecessor for " + pTargetCluster + ":" + tPredecessorToRemote);
 					} else {
 						getLogger().log(this, "Unable to set predecessor for " + pTargetCluster + ":");
 					}
 					
 	
 					try {
 						for(Name tIntermediateAddress : mHRMController.getHRS().getIntermediateNodes(mHRMController.getPhysicalNode().getRoutingService().getNameFor(mHRMController.getPhysicalNode().getCentralFN()), pTargetCluster.getCoordinatorsAddress())) {
 							tParticipationProperty.addAddressToTarget(tIntermediateAddress);
 						}
 					} catch (RoutingException tExc) {
 						getLogger().err(this, "Unable to find names", tExc);
 						return null;
 					}
 					
 					
 					for(ICluster tNeighbor: tManager.getManagedCluster().getNeighbors()) {
 						boolean tBreak = false;
 						for(CoordinatorCEPDemultiplexed tCheckForEdgeCluster : tNeighbor.getParticipatingCEPs()) {
 							if(tCheckForEdgeCluster != null && tCheckForEdgeCluster.isEdgeCEP()) tBreak = true;
 						}
 						if(tBreak) {
 							continue;
 						}
 						DiscoveryEntry tEntry = new DiscoveryEntry(tNeighbor.getToken(), tNeighbor.getCoordinatorName(), tNeighbor.getClusterID(), tNeighbor.getCoordinatorsAddress(), tNeighbor.getHierarchyLevel());
 						tEntry.setPriority(tNeighbor.getPriority());
 						if(tNeighbor.isInterASCluster()) {
 							tEntry.setInterASCluster();
 						}
 						List<RoutableClusterGraphLink> tClusterList = mHRMController.getRoutableClusterGraph().getRoute(tManager.getManagedCluster(), tNeighbor);
 						/*
 						 * the predecessor has to be the next hop
 						 */
 						if(!tClusterList.isEmpty()) {
 							ICluster tPredecessor = (ICluster) mHRMController.getRoutableClusterGraph().getDest(tNeighbor, tClusterList.get(tClusterList.size()-1));
 							tEntry.setPredecessor(ClusterDummy.compare(tPredecessor.getClusterID(), tPredecessor.getToken(), tPredecessor.getHierarchyLevel()));
 							getLogger().log(this, "Successfully set predecessor for " + tNeighbor + ":" + tPredecessor);
 						} else {
 							getLogger().log(this, "Unable to set predecessor for " + tNeighbor);
 						}
 						if(!tManager.getManagedCluster().isInterASCluster() && ! tNeighbor.isInterASCluster() && tManager.getPathToCoordinator(tManager.getManagedCluster(), tNeighbor) != null) {
 							for(RoutingServiceLinkVector tVector : tManager.getPathToCoordinator(tManager.getManagedCluster(), tNeighbor)) {
 								tEntry.addRoutingVectors(tVector);
 							}
 						}
 						if(tNeighbor.isInterASCluster()) {
 							tEntry.setInterASCluster();
 						}
 						tParticipate.addDiscoveryEntry(tEntry);
 					}
 				}
 			}
 			
 			Identity tIdentity = mHRMController.getPhysicalNode().getIdentity();
 			Description tConnectDescription = mHRMController.getConnectDescription(tParticipationProperty);
 			getLogger().log(this, "Connecting to " + pTargetCluster);
 			Connection tConn = null;;
 			try {
 				tConn = pSourceCluster.getHRMController().getHost().connectBlock(tName, tConnectDescription, tIdentity);
 				tCEP.start(tConn);
 				tCEP.write(tCEP.getSourceRoutingServiceAddress());
 			} catch (NetworkException tExc) {
 				getLogger().err(this, "Unable to connect to " + tName, tExc);
 			}
 
 			for(Coordinator tManager : mHRMController.getClusterManagers(pSourceCluster.getHierarchyLevel() + 1)) {
 				LinkedList<Integer> tTokens = new LinkedList<Integer>();
 				for(ICluster tClusterForToken : tManager.getManagedCluster().getNeighbors()) {
 					if(tClusterForToken.getHierarchyLevel() == tManager.getHierarchyLevel() - 1) {
 						tTokens.add((tClusterForToken.getToken()));
 					}
 				}
 				tTokens.add(tManager.getManagedCluster().getToken());
 				pTargetCluster.setNegotiatorCEP(tCEPDemultiplexed);
 				tManager.getParticipatingCEPs().add(tCEPDemultiplexed);
 				if(!pTargetCluster.getCoordinatorName().equals(mHRMController.getPhysicalNode().getCentralFN().getName())) {
 					NestedDiscovery tDiscovery = tBigDiscovery.new NestedDiscovery(
 							tTokens,
 							pTargetCluster.getClusterID(),
 							pTargetCluster.getToken(),
 							pTargetCluster.getHierarchyLevel(),
 							(pTargetCluster instanceof NeighborCluster ? ((NeighborCluster)pTargetCluster).getClustersToTarget() : 0));
 					if(pTargetCluster instanceof NeighborCluster && ((NeighborCluster)pTargetCluster).getClustersToTarget() == 0) {
 						getLogger().warn(this, "Set 0 as hop count to target " + pTargetCluster);
 					}
 					getLogger().log(this, "Created " + tDiscovery + " for " + pTargetCluster);
 					tDiscovery.setOrigin(tManager.getClusterID());
 					tDiscovery.setTargetClusterID(pTargetCluster.getCoordinatorsAddress().getAddress().longValue());
 					tBigDiscovery.addNestedDiscovery(tDiscovery);
 				}
 			}
 			boolean tAbleToWrite = tCEP.write(tBigDiscovery);
 			if(tAbleToWrite) {
 				try {
 					synchronized(tBigDiscovery) {
 						getLogger().log(this, "Waiting for come back of " + tBigDiscovery);
 						if(!tBigDiscovery.isDealtWith()) {
 							Logging.log(this, "ACTIVE WAITING");
 							tBigDiscovery.wait(10000);
 						}
 						getLogger().log(this, "come back of: " + tBigDiscovery);
 					}
 					for(NestedDiscovery tDiscovery : tBigDiscovery.getDiscoveries()) {
 						String tClusters = new String();
 						for(Cluster tCluster : mHRMController.getRoutingTargetClusters()) {
							tClusters += tCluster + "\n";
 						}
 						String tDiscoveries = new String();
 						for(DiscoveryEntry tEntry : tDiscovery.getDiscoveryEntries()) {
							tDiscoveries += "\n" + tEntry;
 						}
 						if(tDiscovery.getNeighborRelations() != null) {
 							for(Tuple<ClusterDummy, ClusterDummy> tTuple : tDiscovery.getNeighborRelations()) {
 								if(!mHRMController.getRoutableClusterGraph().isLinked(tTuple.getFirst(), tTuple.getSecond())) {
 									ICluster tFirstCluster = mHRMController.getCluster(tTuple.getFirst());
 									ICluster tSecondCluster = mHRMController.getCluster(tTuple.getSecond());
 									if(tFirstCluster != null && tSecondCluster != null ) {
 										tFirstCluster.addNeighborCluster(tSecondCluster);
 										getLogger().log(this, "Connecting " + tFirstCluster + " with " + tSecondCluster);
 									} else {
										getLogger().warn(this, "Unable to find cluster " + tTuple.getFirst() + ":" + tFirstCluster + " or " + tTuple.getSecond() + ":" + tSecondCluster + " out of " + tClusters + " while cluster discovery contained " + tDiscoveries + " and CEP is " + tCEP);
 									}
 								}
 							}
 						} else {
 							getLogger().warn(this, tDiscovery + "does not contain any neighbor relations");
 						}
 					}
 				} catch (InterruptedException tExc ) {
 					getLogger().err(this, "Error when waiting for come back of " + tBigDiscovery, tExc);
 				}
 			} else {
 				if(!tAbleToWrite) {
 					getLogger().err(this, "Unable to discover " + tName + " because " + (tAbleToWrite ? " CEP is not connected " : ""));
 				}
 				if(tBigDiscovery.getDiscoveries().isEmpty()) {
 					getLogger().err(this, "Unable to discover " + tName + " because " + (tBigDiscovery.getDiscoveries().isEmpty() ? " no discovery entries were inserted." : ""));
 				}
 			}
 			synchronized(mClusterToCEPMapping) {
 				mClusterToCEPMapping.notifyAll();
 			}
 			
 		}
 		
 		if(tCEPDemultiplexed == null) {
 			tCEPDemultiplexed = mClusterToCEPMapping.get(new Tuple<Long, Long>(pSourceCluster.getClusterID(), pTargetCluster.getClusterID()));
 			if(tCEPDemultiplexed == null) {
 				synchronized(mClusterToCEPMapping) {
 					try {
 						getLogger().log(this, pSourceCluster + " is waiting because establishment of connection to " + pTargetCluster + " did not yet take place");
 						mClusterToCEPMapping.wait(10000);
 						tCEPDemultiplexed = mClusterToCEPMapping.get(new Tuple<Long, Long>(pSourceCluster.getClusterID(), pTargetCluster.getClusterID()));
 						//mClusterToCEPMapping.remove(pSourceCluster.getClusterID());
 					} catch (IllegalMonitorStateException tExc) {
 						getLogger().err(this, "Error when establishing connection", tExc);
 					} catch (InterruptedException tExc) {
 						getLogger().err(this, "Error when establishing connection", tExc);
 
 					}
 				}
 			}
 		}
 		
 		getLogger().log(this, "Returning " + tCEPDemultiplexed + " for " + pSourceCluster + " and target " + pTargetCluster);
 		
 		return tCEPDemultiplexed;
 	}
 	
 	public boolean write(Serializable pData, CoordinatorCEPDemultiplexed pDemux, ICluster pTargetCluster)
 	{	
 		getLogger().log(this, "Writing " + pData + " from " + pDemux.getCluster() + " to the demultiplexed target cluster " + pTargetCluster);
 
 		LinkedList<ClusterDummy> tList = new LinkedList<ClusterDummy>();
 		ClusterDummy tSource = ClusterDummy.compare(pDemux.getCluster().getClusterID(), pDemux.getCluster().getToken(), pDemux.getCluster().getHierarchyLevel());
 		ClusterDummy tTarget = ClusterDummy.compare(pTargetCluster.getClusterID(), pTargetCluster.getToken(), pTargetCluster.getHierarchyLevel());
 		
 		tList.add(tTarget);
 		
 		MultiplexedPackage tMuxPackage = new MultiplexedPackage(tSource, tList, pData);
 		CoordinatorCEP tCEP = mMultiplexer.get(pDemux);
 		getLogger().log(this, "Sending " + tMuxPackage);
 		
 		// send packet
 		return tCEP.write(tMuxPackage);
 	}
 	
 	public HRMName getSourceRoutingServiceAddress(CoordinatorCEPDemultiplexed pCEP)
 	{
 		if(mMultiplexer.containsKey(pCEP)) {
 			return mMultiplexer.get(pCEP).getSourceRoutingServiceAddress();
 		}
 		return null;
 	}
 	
 	public HRMName getPeerRoutingServiceAddress(CoordinatorCEPDemultiplexed pCEP)
 	{
 		if(mMultiplexer.containsKey(pCEP)) {
 			return mMultiplexer.get(pCEP).getPeerRoutingServiceAddress();
 		}
 		return null;
 	}
 	
 	public Route getRouteToPeer(CoordinatorCEPDemultiplexed pCEP)
 	{
 		if(mMultiplexer.containsKey(pCEP)) {
 			return mMultiplexer.get(pCEP).getRouteToPeer();
 		}
 		return null;
 	}
 	
 	public synchronized void addMultiplexedConnection(CoordinatorCEPDemultiplexed pMultiplexedConnection, CoordinatorCEP pConnection)
 	{
 		getLogger().log(this, "Registering multiplexed connection from " + pMultiplexedConnection + " to " + pConnection);
 		mMultiplexer.put(pMultiplexedConnection, pConnection);
 		for(CoordinatorCEPDemultiplexed tCEP : mMultiplexer.keySet()) {
 			getLogger().log(this, tCEP + "->" + mMultiplexer.get(tCEP));
 		}
 		addDemultiplex(pConnection, pMultiplexedConnection);
 	}
 	
 	private void addDemultiplex(CoordinatorCEP pCEP, CoordinatorCEPDemultiplexed pDemux)
 	{
 		getLogger().log(this, "Registering demultiplexing from " + pCEP + " to " + pDemux);
 		if(mDemux.get(pCEP) == null) {
 			mDemux.put(pCEP, new LinkedList<CoordinatorCEPDemultiplexed>());
 		}
 		mDemux.get(pCEP).add(pDemux);
 	}
 	
 	public LinkedList<CoordinatorCEPDemultiplexed> getDemuxCEPs(CoordinatorCEP pCEP)
 	{
 		return mDemux.get(pCEP);
 	}
 	
 	public CoordinatorCEPDemultiplexed getDemuxedCEP(CoordinatorCEP pCEP, ClusterDummy pSource, ClusterDummy pCluster) throws NetworkException
 	{
 		if(mDemux.containsKey(pCEP)) {
 			for(CoordinatorCEPDemultiplexed tCEP : mDemux.get(pCEP)) {
 				if(tCEP.getCluster().getClusterID().equals(pCluster.getClusterID())) {
 					Tuple<Long, Long> tTuple = new Tuple<Long, Long>(pSource.getClusterID(), pCluster.getClusterID());
 					boolean tSourceIsContained = isClusterMultiplexed(tTuple);
					getLogger().log(this, "Comparing " + tCEP + "\n" + (tSourceIsContained ? getDemultiplex(tTuple) : "") + "\n" + tCEP.getRemoteCluster() + "\n" + (tSourceIsContained ? getDemultiplex(tTuple).getRemoteCluster() : "" ));
 					if(tSourceIsContained && getDemultiplex(tTuple) == tCEP) {
 						getLogger().log(this, "Returning " + tCEP + " for request on cluster " + pCluster);
 						return tCEP;
 					} else {
						getLogger().log("\nSource is " + pSource + "\nwhile target is\n" + pCluster+ "\ndemultiplex of source is\n" + getDemultiplex(tTuple) + "\nand currently evaluated CEP is\n" + tCEP);
 					}
 					if(!isClusterMultiplexed(tTuple) && tCEP.getCluster().getClusterID().equals(pCluster.getClusterID())) {
 						getLogger().log(this, "Returning " + tCEP + " for request on cluster " + pCluster);
 						return tCEP;
 					}
 				}
 			}
 		}
 		
 		getLogger().log(this, "Unable to find demultiplexed coonection endpoint for " + pCEP + " and target cluster " + pCluster.getClusterID());
 		for(CoordinatorCEP tCEP : mDemux.keySet()) {
 			getLogger().log(tCEP + " to " + mDemux.get(tCEP));
 		}
 
 		throw new NetworkException("No demultiplexed CEP found for " + pCEP + " and target cluster " + pCluster);
 	}
 	
 	public String toString()
 	{
 		return "CEPMultiplexer" + "@" + mHRMController.getPhysicalNode().getName() + ( mCluster != null ? "(" + mCluster + ")" : "");
 	}
 	
 	public void setCluster(ICluster pCluster)
 	{
 		mCluster = pCluster;
 	}
 	
 	public void registerDemultiplex(Long pSourceClusterID, Long pTargetClusterID, CoordinatorCEPDemultiplexed pCEP)
 	{
 		getLogger().log(this, "Registering demultiplex for Cluster ID" + pSourceClusterID + " to " + pTargetClusterID + " via " + pCEP);
 		mClusterToCEPMapping.put(new Tuple<Long, Long>(pSourceClusterID, pTargetClusterID), pCEP);
 	}
 	
 	private CoordinatorCEPDemultiplexed getDemultiplex(Tuple<Long, Long> pPair)
 	{
 		return mClusterToCEPMapping.get(pPair);
 	}
 	
 	public Logger getLogger()
 	{
 		return mHRMController.getLogger();
 	}
 	
 	private boolean isClusterMultiplexed(Tuple<Long, Long> pPair)
 	{
 		if(pPair == null) return false;
 		for(Tuple<Long, Long> tTuple : mClusterToCEPMapping.keySet()) {
 			if(tTuple.equals(pPair)) return true;
 		}
 		return false;
 	}
 	
 	
 }
