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
 
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RouteReport;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RouteShare;
 import de.tuilmenau.ics.fog.routing.hierarchical.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used for a coordinator instance and can be used on all hierarchy levels.
  * A cluster's elector instance is responsible for creating instances of this class.
  */
 public class Coordinator extends ControlEntity implements Localization, IEvent
 {
 	/**
 	 * Stores the simulation timestamp of the last "share phase"
 	 */
 	private double mTimeOfLastSharePhase = 0; 
 	
 	/**
 	 * Stores the routes which should be shared with cluster members.
 	 */
 	private LinkedList<RoutingEntry> mSharedRoutes = new LinkedList<RoutingEntry>();
 	
 	/**
 	 * Stores whether the data of the "shared phase" has changed or not.
 	 */
 	private boolean mSharedRoutesHaveChanged = false;
 	
 	/**
 	 * Stores the parent cluster, which is managed by this coordinator instance.
 	 */
 	private Cluster mParentCluster = null;
 
 	/**
 	 * This is the coordinator counter, which allows for globally (related to a physical simulation machine) unique coordinator IDs.
 	 */
 	private static int sNextFreeCoordinatorID = 1;
 
 	/**
 	 * Stores the cluster memberships of this coordinator
 	 */
 	private LinkedList<CoordinatorAsClusterMember> mClusterMemberships = new LinkedList<CoordinatorAsClusterMember>();
 	
 	/**
 	 * Stores the communication channel to the superior coordinator.
 	 * For a base hierarchy level cluster, this is a level 0 coordinator.
 	 * For a level n coordinator, this is a level n+1 coordinator. 
 	 */
 	private ComChannel mSuperiorCoordinatorComChannel = null;
 
 	/**
 	 * Stores how many announces were already sent
 	 */
 	private int mSentAnnounces = 0;
 	
 	/**
 	 * Stores the last received routing table from the superior coordinator
 	 */
 	private RoutingTable mReceivedSharedRoutingTable = new RoutingTable();
 	
 	private static final long serialVersionUID = 6824959379284820010L;
 	
 	/**
 	 * The constructor for a cluster object. Usually, it is called by a cluster's elector instance
 	 * 
 	 * @param pCluster the parent cluster instance
 	 */
 	public Coordinator(Cluster pCluster)
 	{
 		// use the HRMController reference and the hierarchy level from the cluster
 		super(pCluster.mHRMController, pCluster.getHierarchyLevel());
 		
 		mParentCluster = pCluster;
 		
 		// create an ID for the cluster
 		setCoordinatorID(createCoordinatorID());
 		
 		// update the cluster ID
 		setClusterID(mParentCluster.getClusterID());
 		
 		// register itself as coordinator for the managed cluster
 		mParentCluster.eventNewLocalCoordinator(this);
 
 		// register at HRMController's internal database
 		mHRMController.registerCoordinator(this);
 
 		Logging.log(this, "\n\n\n################ CREATED COORDINATOR at hierarchy level: " + getHierarchyLevel().getValue());
 		
 		// trigger periodic Cluster announcements
 		if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 			if(HRMConfig.Hierarchy.PERIODIC_COORDINATOR_ANNOUNCEMENTS){
 				Logging.log(this, "Activating periodic coordinator announcements");
 				
 				// register next trigger for 
 				mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL * 2, this);
 			}
 		}
 	}
 	
 	/**
 	 * Generates a new ClusterID
 	 * 
 	 * @return the ClusterID
 	 */
 	static private synchronized long createCoordinatorID()
 	{
 		// get the current unique ID counter
 		long tResult = sNextFreeCoordinatorID * idMachineMultiplier();
 
 		// make sure the next ID isn't equal
 		sNextFreeCoordinatorID++;
 	
 		if(tResult < 1){
 			throw new RuntimeException("Created an invalid coordinator ID " + tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Sets the communication channel to the superior coordinator.
 	 * For a base hierarchy level cluster, this is a level 0 coordinator.
 	 * For a level n coordinator, this is a level n+1 coordinator.
 	 *  
 	 * @param pComChannel the new communication channel
 	 */
 	protected void setSuperiorCoordinatorComChannel(ComChannel pComChannel)
 	{
 		Logging.log(this, "Setting superior comm. channel: " + pComChannel);
 		mSuperiorCoordinatorComChannel = pComChannel;
 	}
 	
 	/**
 	 * Returns a reference to the communication channel towards the superior coordinator.
 	 * 
 	 * @return the communication channel
 	 */
 	public ComChannel superiorCoordinatorComChannel()
 	{
 		return mSuperiorCoordinatorComChannel;
 	}
 
 	/**
 	 * Sends a packet to the superior coordinator
 	 * 
 	 * @param pPacket the packet
 	 */
 	public void sendSuperiorCoordinator(SignalingMessageHrm pPacket)
 	{
 		if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 			Logging.log(this, "Sending to superior coordinator: " + pPacket);
 		}
 		
 		if(superiorCoordinatorComChannel() != null){
 			// plausibility check if we actually use a link from a CoordinatorAsClusterMember
 			if(superiorCoordinatorComChannel().getParent() instanceof CoordinatorAsClusterMember){				
 				CoordinatorAsClusterMember tCoordinatorAsClusterMember = (CoordinatorAsClusterMember)superiorCoordinatorComChannel().getParent();
				if(tCoordinatorAsClusterMember.isThisEntityValid()){
					if(tCoordinatorAsClusterMember.getComChannelToClusterHead() != null){
						// plausibility check if we actually use an active link
						if(tCoordinatorAsClusterMember.getComChannelToClusterHead().isLinkActive()){
							superiorCoordinatorComChannel().sendPacket(pPacket);
						}else{
							Logging.err(this, "sendSuperiorCoordinator() expected an active link, link is: " + superiorCoordinatorComChannel());
						}
					}else{
						Logging.warn(this, "sendSuperiorCoordinator() aborted because the comm. channel to the cluster head is invalid for: " + tCoordinatorAsClusterMember);
					}
 				}else{
					Logging.warn(this, "sendSuperiorCoordinator() aborted because of an invalidated CoordinatorAsClusterMember: " + tCoordinatorAsClusterMember);
 				}
 			}else{
 				Logging.err(this, "sendSuperiorCoordinator() expected a CoordinatorAsClusterMember as parent of: " + superiorCoordinatorComChannel());
 			}
 		}else{
 			Logging.err(this, "sendSuperiorCoordinator() aborted because the comm. channel to the superior coordinator is invalid");
 			int i = 0;
 			for(CoordinatorAsClusterMember tMembership : mClusterMemberships){
 				Logging.err(this, "  ..possible comm. channel [" + i + "] " + (tMembership.isActiveCluster() ? "(A)" : "") + ":" + tMembership.getComChannelToClusterHead());
 				i++;
 			}
 		}
 	}
 
 	/**
 	 * Checks if the "share phase" should be started or not
 	 * 
 	 * @return true if the "share phase" should be started, otherwise false
 	 */
 	private boolean sharePhaseHasTimeout()
 	{
 		// determine the time between two "share phases"
 		double tDesiredTimePeriod = mHRMController.getPeriodSharePhase(getHierarchyLevel().getValue());
 		
 		// determine the time when a "share phase" has to be started 
 		double tTimeNextSharePhase = mTimeOfLastSharePhase + tDesiredTimePeriod;
 	
 		// determine the current simulation time from the HRMCotnroller instance
 		double tCurrentSimulationTime = mHRMController.getSimulationTime();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TIMING_ROUTE_DISTRIBUTION){
 			Logging.log(this, "Checking for timeout of \"share phase\": desired time period is " + tDesiredTimePeriod + ", " + tCurrentSimulationTime + " > " + tTimeNextSharePhase + "? -> " + (tCurrentSimulationTime >= tTimeNextSharePhase));
 		}
 		
 		return (tCurrentSimulationTime >= tTimeNextSharePhase);
 	}
 	
 	/**
 	 * Determines if new "share phase" data is available
 	 * 
 	 * @return true if new data is available, otherwise false
 	 */
 	private boolean hasNewSharePhaseData()
 	{
 		boolean tResult = false;
 		
 		synchronized (mSharedRoutes){
 			tResult = mSharedRoutesHaveChanged;
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * This function implements the "share phase".
 	 * It distributes locally stored sharable routing data among the known cluster members
 	 */
 	private int mCallsSharePhase = 0;
 	@SuppressWarnings("unused")
 	public void sharePhase()
 	{
 		// should we start the "share phase"?
 		if (sharePhaseHasTimeout()){
 			if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 				Logging.log(this, "SHARE PHASE with cluster members on level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 			}
 
 			// store the time of this "share phase"
 			mTimeOfLastSharePhase = mHRMController.getSimulationTime();
 
 			if ((!HRMConfig.Routing.PERIODIC_SHARE_PHASES) && (!hasNewSharePhaseData())){ //TODO
 				if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 					Logging.log(this, "SHARE PHASE aborted because routing data hasn't changed since last signaling round");
 				}
 				return;
 			}
 			
 			mCallsSharePhase++;
 			
 			// get all comm. channels to inferior cluster members
 			LinkedList<ComChannel> tComChannels = mParentCluster.getComChannels();
 
 			/*******************************************************************
 			 * Iterate over all comm. channels and share routing data
 			 *******************************************************************/ 
 			for (ComChannel tComChannel : tComChannels){
 				/**
 				 * Only proceed if the link is actually active
 				 */
 				if(tComChannel.isLinkActive()){
 					RoutingTable tSharedRoutingTable = new RoutingTable();
 					HRMID tPeerHRMID = tComChannel.getPeerHRMID();
 					if((tPeerHRMID != null) && (!tPeerHRMID.isZero())){
 						if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 							Logging.log(this, "  ..sharing routes with: " + tPeerHRMID);
 						}
 						
 						if(getHierarchyLevel().isBaseLevel()){
 							HRMID tThisNodeHRMID = getCluster().getL0HRMID();
 
 							/*********************************************************************
 							 * SHARE: received routes from superior coordinator
 							 *********************************************************************/
 							// copy the received routing table
 							RoutingTable tReceivedSharedRoutingTable = null;
 							synchronized (mReceivedSharedRoutingTable) {
 								tReceivedSharedRoutingTable = (RoutingTable) mReceivedSharedRoutingTable.clone();	
 							}
 
 							int j = 0;
 							for(RoutingEntry tEntry : tReceivedSharedRoutingTable){
 								/**
 								 * does the received route start at the peer?
 								 */
 								if(tEntry.getSource().equals(tPeerHRMID)){
 									RoutingEntry tNewEntry = tEntry.clone();
 									tNewEntry.extendCause(this + "::sharePhase()_ReceivedRouteShare_1(" + mCallsSharePhase + ")(" + j + ")");
 									// share the received entry with the peer
 									tSharedRoutingTable.addEntry(tNewEntry);
 									
 									continue;
 								}
 								
 								/**
 								 * does the received route start at this node and the next node isn't the peer?
 								 */
 								if((tEntry.getSource().equals(tThisNodeHRMID)) && (!tEntry.getNextHop().equals(tPeerHRMID))){
 									RoutingEntry tRoutingEntryWithPeer = mHRMController.getRoutingEntryHRG(tPeerHRMID, tThisNodeHRMID);
 									if(tRoutingEntryWithPeer != null){
 										tRoutingEntryWithPeer.chain(tEntry);
 										tRoutingEntryWithPeer.extendCause(this + "::sharePhase()_ReceivedRouteShare_2(" + mCallsSharePhase + ")(" + j + ")");
 										// share the received entry with the peer
 										tSharedRoutingTable.addEntry(tRoutingEntryWithPeer);
 									}									
 								}
 								
 								j++;
 							}
 							
 						}else{
 //							/*********************************************************************
 //							 * SHARE: routes to neighbors of ourself
 //							 *********************************************************************/
 //							// find all siblings of ourself
 //							//HINT: the found siblings have the same hierarchy level like this coordinator
 //							LinkedList<HRMID> tKnownSibblings = mHRMController.getDestinationsHRG(getHRMID());
 //							for(HRMID tPossibleDestination : tKnownSibblings){
 //								if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 //									Logging.log(this, "    ..possible higher destination: " + tPossibleDestination);
 //								}
 //								
 //								// get the inter-cluster path to the possible destination
 //								List<AbstractRoutingGraphLink> tPath = mHRMController.getRouteHRG(getHRMID(), tPossibleDestination);
 //								if(tPath != null){
 //									
 //								}								
 //							}
 	
 							/*********************************************************************
 							 * SHARE: routes to neighbors of the peer
 							 *********************************************************************/
 							// find all siblings of the peer
 							//HINT: the peer is one hierarchy level below this coordinator
 							LinkedList<HRMID> tKnownPeerSiblings = mHRMController.getDestinationsHRG(tPeerHRMID);
 							for(HRMID tPossibleDestination : tKnownPeerSiblings){
 								if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 									Logging.log(this, "    ..possible peer destination: " + tPossibleDestination);
 								}
 	
 								// get the inter-cluster path to the possible destination
 								List<AbstractRoutingGraphLink> tPath = mHRMController.getRouteHRG(tPeerHRMID, tPossibleDestination);
 								if(tPath != null){
 									// the searched routing entry to the possible destination cluster 
 									RoutingEntry tFinalRoutingEntryToDestination = null;
 									
 									// the last cluster gateway
 									HRMID tLastClusterGateway = null;
 									HRMID tFirstForeignGateway = null;
 									
 									if(!tPath.isEmpty()){
 										int i = 0;
 										for(AbstractRoutingGraphLink tLink : tPath){
 											RoutingEntry tInterClusterRoutingEntry = (RoutingEntry)tLink.getRoute().getFirst();
 											
 											if(tFinalRoutingEntryToDestination != null){
 												if(tLastClusterGateway == null){
 													throw new RuntimeException(this + "::sharePhase() should never reach this point");
 												}
 												
 												/**
 												 * Add the intra-cluster path from the last gateway to the next one
 												 */
 												// the next cluster gateway
 												HRMID tNextClusterGateway = tInterClusterRoutingEntry.getSource();
 												// the intra-cluster path
 												List<AbstractRoutingGraphLink> tIntraClusterPath = mHRMController.getRouteHRG(tLastClusterGateway, tNextClusterGateway);
 												if(tIntraClusterPath != null){
 //													if(!tIntraClusterPath.isEmpty()){
 													if(tIntraClusterPath.size() == 1){
 														AbstractRoutingGraphLink tIntraClusterLogLink = tIntraClusterPath.get(0);
 														// get the routing entry from the last gateway to the next one
 														RoutingEntry tIntraClusterRoutingEntry = (RoutingEntry) tIntraClusterLogLink.getRoute().getFirst();
 														
 														// chain the routing entries
 														tFinalRoutingEntryToDestination.chain(tIntraClusterRoutingEntry);
 				
 														/**
 														 * Determine the next hop for the resulting path
 														 */
 														if(tFirstForeignGateway == null){
 															if(tIntraClusterRoutingEntry.getHopCount() > 0){
 																tFirstForeignGateway = tIntraClusterRoutingEntry.getNextHop();
 															}
 														}
 				
 														if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 															Logging.log(this, "      ..intra-cluster step[" + i + "]: " + tIntraClusterRoutingEntry);
 															//Logging.err(this, "        ..==>: " + tFinalRoutingEntryToDestination);
 														}
 														i++;	
 													}else{
 														Logging.warn(this, "sharePhase() found a complex intra-cluster route: " + tIntraClusterPath + " from " + tLastClusterGateway + " to " + tNextClusterGateway + " as: " + tIntraClusterPath + " for a routing from " + tPeerHRMID + " to " + tPossibleDestination);
 														
 														// reset
 														tFinalRoutingEntryToDestination = null;
 		
 														// abort
 														break;
 														
 														//HINT: do not throw a RuntimeException here because such a situation could have a temporary cause
 													}
 												}else{
 													Logging.warn(this, "sharePhase() couldn't find a route from " + tLastClusterGateway + " to " + tNextClusterGateway + " for a routing from " + tPeerHRMID + " to " + tPossibleDestination);
 													
 													// reset
 													tFinalRoutingEntryToDestination = null;
 	
 													// abort
 													break;
 
 													//HINT: do not throw a RuntimeException here because such a situation could have a temporary cause
 												}
 												
 												/**
 												 * Add the inter-cluster path
 												 */
 												// chain the routing entries
 												tFinalRoutingEntryToDestination.chain(tInterClusterRoutingEntry);
 											}else{
 												/**
 												 * First step of the resulting path
 												 */
 												tFinalRoutingEntryToDestination = tInterClusterRoutingEntry;
 											}
 											
 											/**
 											 * Determine the next hop for the resulting path
 											 */
 											if(tFirstForeignGateway == null){
 												if(tInterClusterRoutingEntry.getHopCount() > 0){
 													tFirstForeignGateway = tInterClusterRoutingEntry.getNextHop();
 												}
 											}
 			
 											
 											if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 												Logging.log(this, "      ..inter-cluster step[" + i + "]: " + tInterClusterRoutingEntry);
 												//Logging.err(this, "        ..==>: " + tFinalRoutingEntryToDestination);
 											}
 				
 											//update last cluster gateway
 											tLastClusterGateway = tInterClusterRoutingEntry.getNextHop();
 													
 											i++;	
 										}
 									}else{
 										Logging.err(this, "sharePhase() found an empty path from " + tPeerHRMID + " to " + tPossibleDestination);
 									}
 									
 									if(tFinalRoutingEntryToDestination != null){
 										/**
 										 * Set the DESTINATION for the resulting routing entry
 										 */
 										tFinalRoutingEntryToDestination.setDest(tPeerHRMID.getForeignCluster(tPossibleDestination) /* aggregate the destination here */);
 			
 										/**
 										 * Set the NEXT HOP for the resulting routing entry 
 										 */
 										if(tFirstForeignGateway != null){
 											tFinalRoutingEntryToDestination.setNextHop(tFirstForeignGateway);
 										}
 										
 										/**
 										 * Add the found routing entry to the shared routing table
 										 */
 										if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 											Logging.log(this, "      ..==> routing entry: " + tFinalRoutingEntryToDestination);
 										}
 										tFinalRoutingEntryToDestination.extendCause(this + "::sharePhase()_HRG_based(" + mCallsSharePhase + ")");
 										tSharedRoutingTable.addEntry(tFinalRoutingEntryToDestination);
 									}
 								}else{
 									Logging.err(this, "Couldn't determine a route from " + tPeerHRMID + " to " + tPossibleDestination);
 								}
 							}
 						}
 						
 						if(tSharedRoutingTable.size() > 0){
 							tComChannel.distributeRouteShare(tSharedRoutingTable);
 						}
 					}	
 				}
 			}
 		}else{
 			// share phase shouldn't be started, we have to wait until next trigger
 		}
 	}
 
 	/**
 	 * This function implements the "report phase".
 	 * It sends locally stored sharable routing data to the superior coordinator
 	 */
 	public void reportPhase()
 	{
 		if(HRMConfig.Routing.REPORT_TOPOLOGY_AUTOMATICALLY){
 			/**
 			 * Auto. delete deprecated routes
 			 */
 			if(getHierarchyLevel().isBaseLevel()){
 				mHRMController.unregisterAutoHRG();
 			}
 			
 			/**
 			 * Create the report based on current topology data
 			 */
 			// the highest coordinator does not have any superior coordinator
 			if (!getHierarchyLevel().isHighest()){
 				// do we have a valid channel to the superior coordinator?
 				if(superiorCoordinatorComChannel() != null){
 					// do not report topology data which is already locally known
 					if(!mHRMController.getNodeL2Address().equals(superiorCoordinatorComChannel().getPeerL2Address())){
 						RoutingTable tReportRoutingTable = new RoutingTable();
 		
 						/**
 						 * Report 1: routes to all neighbors
 						 */
 						RoutingTable tRoutesToNeighbors = mHRMController.getRoutesWithNeighborsHRG(getHRMID());
 						// add the found routes to the report routing table
 						tReportRoutingTable.addEntries(tRoutesToNeighbors);
 
 						/**
 						 * Report 2 (L0): routes to direct neighbors
 						 */							
 						if(getHierarchyLevel().isBaseLevel()){ //TODO: remove this limitation
 							if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 								Logging.log(this, "REPORT PHASE at hierarchy level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 							}
 
 							// get all comm. channels
 							LinkedList<ComChannel> tComChannels = mParentCluster.getComChannels();
 							// iterate over all comm. channels and fetch the recorded route reports
 							for(ComChannel tComChannel : tComChannels){
 								RoutingTable tComChannelTable = tComChannel.getReportedRoutingTable();
 								if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 									Logging.log(this, "   ..got L0 node report: " + tComChannelTable);
 								}
 								// add the found routes to the overall route report, which is later sent to the superior coordinator
 								tReportRoutingTable.addEntries(tComChannelTable);
 							}
 						}
 						
 						/**
 						 * Send the created report routing table to the superior coordinator
 						 */
 						if(tReportRoutingTable.size() > 0){
 							if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 								Logging.log(this, "   ..reporting via " + superiorCoordinatorComChannel() + " the routing table:");
 								int i = 0;
 								for(RoutingEntry tEntry : tReportRoutingTable){
 									Logging.log(this, "     ..[" + i +"]: " + tEntry);
 									i++;
 								}
 							}
 							
 							// create new RouteReport packet for the superior coordinator
 							RouteReport tRouteReportPacket = new RouteReport(getHRMID(), superiorCoordinatorComChannel().getPeerHRMID(), tReportRoutingTable);
 							// send the packet to the superior coordinator
 							sendSuperiorCoordinator(tRouteReportPacket);
 						}else{
 							if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 								Logging.log(this, "reportPhase() aborted because no report for " + superiorCoordinatorComChannel() + " available");
 							}
 						}
 					}else{
 						if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 							Logging.log(this, "reportPhase() aborted because no report in a loopback is allowed");
 						}
 					}
 				}else{
 					Logging.warn(this, "reportPhase() aborted because channel to superior coordinator is invalid");
 				}
 			}else{
 				// we are the highest hierarchy level, no one to send topology reports to
 			}
 		}
 	}
 	
 	/**
 	 * EVENT: RouteShare
 	 * 
 	 * @param pSourceComChannel the source comm. channel
 	 * @param pRouteSharePacket the packet
 	 */
 	public void eventReceivedRouteShare(ComChannel pSourceComChannel, RouteShare pRouteSharePacket)
 	{
 		if(HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 			Logging.err(this, "EVENT: ReceivedRouteShare via: " + pSourceComChannel);
 		}
 		
 		synchronized (mReceivedSharedRoutingTable) {
 			mReceivedSharedRoutingTable = pRouteSharePacket.getRoutes();
 			mHRMController.addHRMRouteShare(mReceivedSharedRoutingTable, this + "::eventReceivedRouteShare()");			
 		}		
 	}
 
 	/**
 	 * EVENT: "eventCoordinatorRoleInvalid", triggered by the Elector, the reaction is:
 	 * 	 	1.) create signaling packet "BullyLeave"
 	 * 		2.) send the packet to the superior coordinator 
 	 */
 	public synchronized void eventCoordinatorRoleInvalid()
 	{
 		Logging.log(this, "============ EVENT: Coordinator_Role_Invalid");
 
 		/**
 		 * Trigger: role invalid
 		 */
 		eventInvalidation();
 		
 		/**
 		 * Trigger: invalid coordinator
 		 */
 		distributeCoordinatorInvalidation();
 		
 		/**
 		 * Inform all superior clusters about the event and trigger the invalidation of this coordinator instance -> we leave all Bully elections because we are no longer a possible election winner
 		 */
 		if (!getHierarchyLevel().isHighest()){
 			eventAllClusterMembershipsInvalid();
 		}else{
 			Logging.log(this, "eventCoordinatorRoleInvalid() skips further signaling because hierarchy end is already reached at: " + getHierarchyLevel().getValue());
 		}
 
 		/**
 		 * Revoke own HRMID
 		 */ 
 		if((getHRMID() != null) && (!getHRMID().isZero())){
 			eventRevokedHRMID(this, getHRMID());
 		}
 		
 		/**
 		 * Trigger: revoke all assigned HRMIDs from all cluster members
 		 */
 		mParentCluster.eventAllClusterAddressesInvalid();
 		
 		/**
 		 * Unregister from local databases
 		 */
 		Logging.log(this, "============ Destroying this coordinator now...");
 		
 		// unregister from HRMController's internal database
 		mHRMController.unregisterCoordinator(this);
 		
 		/**
 		 * Inform the inferior cluster about our destruction
 		 */
 		mParentCluster.eventCoordinatorLost();
 	}
 	
 	/**
 	 * EVENT: all cluster membership invalid
 	 */
 	private void eventAllClusterMembershipsInvalid()
 	{
 		Logging.log(this, "EVENT: all cluster memberships invalid");
 		
 		Logging.log(this, "     ..invalidating these cluster memberships: " + mClusterMemberships);
 		while(mClusterMemberships.size() > 0) {
 			CoordinatorAsClusterMember tCoordinatorAsClusterMember = mClusterMemberships.getLast();
 			tCoordinatorAsClusterMember.eventClusterMembershipInvalid();
 		}
 	}
 	
 	/**
 	 * SEND: distribute AnnounceCoordinator messages among the neighbors which are within the given max. radius (see HRMConfig)        
 	 */
 	public static boolean USER_CTRL_COORDINATOR_ANNOUNCEMENTS = true;
 	private synchronized void distributeCoordinatorAnnouncement()
 	{
 		if(isThisEntityValid()){
 			// trigger periodic Cluster announcements
 			if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 				if (USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 					LinkedList<Cluster> tL0Clusters = mHRMController.getAllClusters(0);
 					AnnounceCoordinator tAnnounceCoordinatorPacket = new AnnounceCoordinator(mHRMController, mHRMController.getNodeName(), getCluster().createClusterName(), mHRMController.getNodeL2Address());
 					
 					/**
 					 * Count the sent announces
 					 */
 					mSentAnnounces++;
 					
 					if(getHierarchyLevel().isBaseLevel()){
 						/**
 						 * Send cluster broadcasts in all other active L0 clusters if we are at level 0 
 						 */
 						for(Cluster tCluster : tL0Clusters){
 							tCluster.sendClusterBroadcast(tAnnounceCoordinatorPacket, true);
 						}
 					}else{
 						/**
 						 * Send cluster broadcast (to the bottom) in all active inferior clusters - either direct or indirect via the forwarding function of a higher cluster
 						 */
 						LinkedList<Cluster> tClusters = mHRMController.getAllClusters(getHierarchyLevel().getValue());
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "########## Distributing Coordinator announcement (to the bottom): " + tAnnounceCoordinatorPacket);
 							Logging.log(this, "     ..distributing in clusters: " + tClusters);
 						}
 						for(Cluster tCluster : tClusters){
 							tCluster.sendClusterBroadcast(tAnnounceCoordinatorPacket, true);
 						}
 						
 						/**
 						 * Send cluster broadcasts in all known inactive L0 clusters
 						 */
 						LinkedList<Cluster> tInactiveL0Clusters = new LinkedList<Cluster>();
 						for(Cluster tCluster : tL0Clusters){
 							if(!tCluster.isActiveCluster()){
 								tInactiveL0Clusters.add(tCluster);
 							}
 						}					
 						if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 							Logging.log(this, "########## Distributing Coordinator announcement (to the side): " + tAnnounceCoordinatorPacket);
 							Logging.log(this, "     ..distributing in inactive clusters: " + tClusters);
 						}
 						for(Cluster tCluster : tInactiveL0Clusters){
 							tCluster.sendClusterBroadcast(tAnnounceCoordinatorPacket, true);
 						}
 					}
 				}else{
 					Logging.warn(this, "USER_CTRL_COORDINATOR_ANNOUNCEMENTS is set to false, this prevents the HRM system from creating a correct hierarchy");
 				}
 			}else{
 				Logging.warn(this, "HRMConfig->COORDINATOR_ANNOUNCEMENTS is set to false, this prevents the HRM system from creating a correct hierarchy");
 			}
 		}else{
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 				Logging.warn(this, "distributeCoordinatorAnnouncement() aborted because coordinator role is already invalidated");
 			}
 		}
 	}
 
 	/**
 	 * SEND: distribute InvalidCoordinator messages among the neighbors which are within the given max. radius (see HRMConfig)        
 	 */
 	private synchronized void distributeCoordinatorInvalidation()
 	{
 		// trigger periodic Cluster announcements
 		if((HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS) && (USER_CTRL_COORDINATOR_ANNOUNCEMENTS)){
 			InvalidCoordinator tInvalidCoordinatorPacket = new InvalidCoordinator(mHRMController, mHRMController.getNodeName(), getCluster().createClusterName(), mHRMController.getNodeL2Address());
 			/**
 			 * Send broadcasts in all locally known clusters at this hierarchy level
 			 */
 			LinkedList<Cluster> tClusters = mHRMController.getAllClusters(0); //HINT: we have to broadcast via level 0, otherwise, an inferior coul)d already be destroyed and the invalidation message might get dropped
 			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 				Logging.log(this, "########## Distributing Coordinator invalidation (to the bottom): " + tInvalidCoordinatorPacket);
 				Logging.log(this, "     ..distributing in clusters: " + tClusters);
 			}
 			for(Cluster tCluster : tClusters){
 				tCluster.sendClusterBroadcast(tInvalidCoordinatorPacket, true);
 			}
 		}
 	}
 
 	/**
 	 * Returns how many announces were already sent
 	 * 
 	 * @return the number of announces
 	 */
 	public int countAnnounces()
 	{
 		return mSentAnnounces;
 	}
 	
 	/**
 	 * Implementation for IEvent::fire()
 	 */
 	@Override
 	public void fire()
 	{
 		if(isThisEntityValid()){
 			if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 				if(USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 					if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 						Logging.log(this, "###########################");
 						Logging.log(this, "###### FIRE FIRE FIRE #####");
 						Logging.log(this, "###########################");
 					}
 					
 					/**
 					 * Trigger: ClusterAnnounce distribution
 					 */
 					distributeCoordinatorAnnouncement();
 				}
 				
 				// register next trigger for 
 				mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL, this);
 			}
 		}else{
 			if(USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
 				//Logging.warn(this, "fire() aborted because coordinator role is already invalidated");
 			}
 		}
 	}
 
 	/**
 	 * EVENT: "announced", triggered by Elector if the election was won and this coordinator was announced to all cluster members 	 
 	 */
 	public synchronized void eventAnnouncedAsCoordinator()
 	{
 		Logging.log(this, "EVENT: announced as coordinator");
 
 		/**
 		 * Trigger: explicit cluster announcement to neighbors
 		 */ 
 		distributeCoordinatorAnnouncement();
 
 		/**
 		 * AUTO ADDRESS DISTRIBUTION
 		 */
 		if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 			//Logging.log(this, "EVENT ANNOUNCED - triggering address assignment for " + mParentCluster.getComChannels().size() + " cluster members");
 
 			getCluster().distributeAddresses();
 		}
 		
 
 		/**
 		 * AUTO CLUSTERING
 		 */
 		if(!getHierarchyLevel().isHighest()) {
 			if (HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY){ 
 				if(getHierarchyLevel().getValue() < HRMConfig.Hierarchy.CONTINUE_AUTOMATICALLY_HIERARCHY_LIMIT){
 					//Logging.log(this, "EVENT ANNOUNCED - triggering clustering of this cluster's coordinator and its neighbors");
 
 					// start the clustering at the hierarchy level
 					mHRMController.cluster(this, new HierarchyLevel(this, getHierarchyLevel().getValue() + 1));
 				}else{
 					//Logging.log(this, "EVENT ANNOUNCED - stopping clustering because height limitation is reached at level: " + getHierarchyLevel().getValue());
 				}
 			}else{
 				Logging.warn(this, "EVENT ANNOUNCED - stopping clustering because automatic continuation is deactivated");
 			}
 		}
 	}
 
 	/**
 	 * EVENT: coordinator announcement, we react on this by:
 	 *       1.) store the topology information locally
 	 *       2.) forward the announcement downward the hierarchy to all locally known clusters (where this node is the head) ("to the bottom")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pAnnounceCoordinator the received announcement
 	 */
 	@Override
 	public void eventCoordinatorAnnouncement(ComChannel pComChannel, AnnounceCoordinator pAnnounceCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "EVENT: coordinator announcement (from above): " + pAnnounceCoordinator);
 		}
 		
 		/**
 		 * Storing that the announced coordinator is a superior one of this node
 		 */
 		// is the packet still on its way from the top to the bottom AND does it not belong to an L0 coordinator?
 		if((!pAnnounceCoordinator.enteredSidewardForwarding()) && (!pAnnounceCoordinator.getSenderClusterName().getHierarchyLevel().isBaseLevel())){
 			mHRMController.registerSuperiorCoordinator(pAnnounceCoordinator.getSenderClusterName());
 		}
 
 		//HINT: we don't store the announced remote coordinator in the ARG here because we are waiting for the side-ward forwarding of the announcement
 		//      otherwise, we would store [] routes between this local coordinator and the announced remote one
 
 		/**
 		 * Record the passed clusters
 		 */
 		pAnnounceCoordinator.addGUIPassedCluster(new Long(getGUIClusterID()));
 
 		/**
 		 * Forward the coordinator announcement to all locally known clusters at this hierarchy level
 		 */
 		LinkedList<Cluster> tClusters = mHRMController.getAllClusters(getHierarchyLevel());
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_ANNOUNCEMENT_PACKETS){
 			Logging.log(this, "\n\n########## Forwarding Coordinator announcement: " + pAnnounceCoordinator);
 			Logging.log(this, "     ..distributing in clusters: " + tClusters);
 		}
 		for(Cluster tCluster : tClusters){
 			tCluster.sendClusterBroadcast(pAnnounceCoordinator, true);
 		}
 	}
 	
 	/**
 	 * EVENT: coordinator invalidation, we react on this by:
 	 *       1.) remove the topology information locally
 	 *       2.) forward the invalidation downward the hierarchy to all locally known clusters (where this node is the head) ("to the bottom")
 	 * 
 	 * @param pComChannel the source comm. channel
 	 * @param pInvalidCoordinator the received invalidation
 	 */
 	@Override
 	public synchronized void eventCoordinatorInvalidation(ComChannel pComChannel, InvalidCoordinator pInvalidCoordinator)
 	{
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "EVENT: coordinator invalidation (from above): " + pInvalidCoordinator);
 		}
 		
 		/**
 		 * Store the announced remote coordinator in the ARG 
 		 */
 		if(!pInvalidCoordinator.getSenderClusterName().equals(this)){
 			unregisterAnnouncedCoordinatorARG(this, pInvalidCoordinator);
 		}else{
 			Logging.err(this, "eventCoordinatorInvalidation() was triggered for an invalidation of ourself, announcement: " + pInvalidCoordinator);
 		}
 
 		/**
 		 * Forward the coordinator invalidation to all locally known clusters at this hierarchy level
 		 */
 		LinkedList<Cluster> tClusters = mHRMController.getAllClusters(getHierarchyLevel());
 		if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 			Logging.log(this, "\n\n########## Forwarding Coordinator invalidation: " + pInvalidCoordinator);
 			Logging.log(this, "     ..distributing in clusters: " + tClusters);
 		}
 		for(Cluster tCluster : tClusters){
 			tCluster.sendClusterBroadcast(pInvalidCoordinator, true);
 		}
 	}
 
 	/**
 	 * Creates a ClusterName object which describes this coordinator
 	 * 
 	 * @return the new ClusterName object
 	 */
 	public ClusterName createCoordinatorName()
 	{
 		ClusterName tResult = null;
 		
 		tResult = new ClusterName(mHRMController, getHierarchyLevel(), getCluster().getClusterID(), getCoordinatorID());
 		
 		return tResult;
 	}
 
 	/**
 	 * EVENT: new HRMID assigned
      * The function is called when an address update was received.
      * 
 	 * @param pSourceComChannel the source comm. channel
 	 * @param pHRMID the new HRMID
 	 */
 	@Override
 	public void eventAssignedHRMID(ComChannel pSourceComChannel, HRMID pHRMID)
 	{
 		if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 			Logging.log(this, "Handling AssignHRMID with assigned HRMID " + pHRMID.toString());
 		}
 
 		if((pHRMID != null) && (!pHRMID.isZero())){
 			// setHRMID()
 			super.eventAssignedHRMID(pSourceComChannel, pHRMID);
 		
 			/**
 			 * Automatic address distribution via the cluster
 			 */
 			// we should automatically continue the address distribution?
 			if (HRMConfig.Addressing.ASSIGN_AUTOMATICALLY){
 				if (HRMConfig.DebugOutput.SHOW_DEBUG_ADDRESS_DISTRIBUTION){
 					Logging.log(this, "     ..continuing the address distribution process via this cluster");
 				}
 				getCluster().distributeAddresses();				
 			}			
 		}
 	}
 
 	/**
 	 * EVENT: cluster membership request, a cluster requests of a coordinator to acknowledge cluster membership, triggered by the comm. session
 	 * 
 	 * @param pRemoteClusterName the description of the possible new cluster member
 	 * @param pSourceComSession the comm. session where the packet was received
 	 */
 	private int mClusterMembershipRequestNr = 0;
 	public void eventClusterMembershipRequest(ClusterName pRemoteClusterName, ComSession pSourceComSession)
 	{
 		mClusterMembershipRequestNr++;
 		
 		Logging.log(this, "EVENT: got cluster membership request (" + mClusterMembershipRequestNr + ") from: " + pRemoteClusterName);
 		
 		if(isThisEntityValid()){
 			/**
 			 * Create new cluster (member) object
 			 */
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "    ..creating new local cluster membership for: " + pRemoteClusterName + ", remote node: " + pSourceComSession.getPeerL2Address());
 			}
 			CoordinatorAsClusterMember tClusterMembership = CoordinatorAsClusterMember.create(mHRMController, this, pRemoteClusterName, pSourceComSession.getPeerL2Address());
 			
 			/**
 			 * Create the communication channel for the described cluster member
 			 */
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "     ..creating communication channel");
 			}
 			ComChannel tComChannel = new ComChannel(mHRMController, ComChannel.Direction.IN, tClusterMembership, pSourceComSession);
 	
 			/**
 			 * Set the remote ClusterName of the communication channel
 			 */
 			tComChannel.setRemoteClusterName(pRemoteClusterName);
 	
 			/**
 			 * SEND: acknowledgment -> will be answered by a BullyPriorityUpdate
 			 */
 			tComChannel.signalRequestClusterMembershipAck(createCoordinatorName());
 
 			/**
 			 * Trigger: comm. channel established 
 			 */
 			tClusterMembership.eventComChannelEstablished(tComChannel);
 	
 			/**
 			 * Trigger: joined a remote cluster (sends a Bully priority update)
 			 */
 			tClusterMembership.eventJoinedRemoteCluster(tComChannel);
 		}else{
 			Logging.warn(this, "eventClusterMembershipRequest() aborted because coordinator role is already invalidated");
 
 			/**
 			 * Inform the peer by the help of a InformClusterLeft packet
 			 */
 			pSourceComSession.denyClusterMembershipRequest(pRemoteClusterName, createCoordinatorName());			
 		}
 	}
 
 	public void eventClusterMembershipToSuperiorCoordinator(CoordinatorAsClusterMember pMembership)
 	{
 		Logging.log(this, "EVENT: cluster membership to superior coordinator updated to: " + pMembership);
 
 		/**
 		 * Deactivate the old membership
 		 */
 		if((superiorCoordinatorComChannel() != null) && (superiorCoordinatorComChannel().getParent() != null)){
 			if(superiorCoordinatorComChannel().getParent() instanceof CoordinatorAsClusterMember){
 				CoordinatorAsClusterMember tOldMembership = (CoordinatorAsClusterMember)superiorCoordinatorComChannel().getParent();
 				
 				tOldMembership.setMembershipActivation(false);
 			}else{
 				Logging.err(this, "Expected a CoordinatorAsClusterMember as parent of: " + superiorCoordinatorComChannel());
 			}
 		}
 		
 		/**
 		 * Activate the new membership
 		 */
 		pMembership.setMembershipActivation(true);
 		
 		/**
 		 * Set the comm. channel to the superior coordinator
 		 */
 		if (superiorCoordinatorComChannel() != pMembership.getComChannelToClusterHead()){
 			Logging.log(this, "eventClusterMembershipToSuperiorCoordinator() updates comm. channel to superior coordinator: " + pMembership.getComChannelToClusterHead());
 			setSuperiorCoordinatorComChannel(pMembership.getComChannelToClusterHead());
 		}
 
 		/**
 		 * Update info. about superior coordinator
 		 */
 		eventClusterCoordinatorAvailable(pMembership.superiorCoordinatorNodeName(), pMembership.getCoordinatorID(), pMembership.superiorCoordinatorHostL2Address(), pMembership.superiorCoordinatorDescription());
 
 		/**
 		 * Set the HRMID of the CoordinatorAsClusterMember instance
 		 */
 		if((getHRMID() == null) || (getHRMID().isZero()) || (!getHRMID().equals(pMembership.getHRMID()))){
 			Logging.log(this, "eventClusterMembershipToSuperiorCoordinator() updates HRMID to: " + pMembership.getHRMID());
 			eventAssignedHRMID(pMembership.getComChannelToClusterHead(), pMembership.getHRMID());
 		}
 	}
 	
 	/**
 	 * EVENT: cluster membership activated
 	 * 
 	 * @param pMembership the membership
 	 */
 	public void eventClusterMembershipActivated(CoordinatorAsClusterMember pMembership)
 	{
 		Logging.log(this, "EVENT: cluster membership activated: " + pMembership);
 	}
 
 	/**
 	 * EVENT: cluster membership deactivated
 	 * 
 	 * @param pMembership the membership
 	 */
 	public void eventClusterMembershipDeactivated(CoordinatorAsClusterMember pMembership)
 	{
 		Logging.log(this, "EVENT: cluster membership deactivated: " + pMembership);
 
 		/**
 		 * If we have lost the membership to the superior coordinator, we search for the next possible superior coordinator/cluster
 		 */
 		if (superiorCoordinatorComChannel() == pMembership.getComChannelToClusterHead()){
 			Logging.warn(this, "Lost the channel to the superior coordinator, remaining channels to superior clusters: " + getClusterMembershipComChannels());
 		}
 	}
 
 	/**
 	 * Registers a new cluster membership for this coordinator
 	 * 
 	 * @param pMembership the new cluster membership
 	 */
 	public void registerClusterMembership(CoordinatorAsClusterMember pMembership)
 	{
 		synchronized (mClusterMemberships) {
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "Registering cluster membership for: " + pMembership);
 			
 				//HINT: a check for already existing cluster memberships has to be done based on equals AND a check of the peer ClusterName
 			}
 			
 			// add this cluster membership
 			mClusterMemberships.add(pMembership);
 		}
 	}
 	
 	/**
 	 * Unregisters a new cluster membership for this coordinator
 	 * 
 	 * @param pMembership the cluster membership which should be removed
 	 */
 	public void unregisterClusterMembership(CoordinatorAsClusterMember pMembership)
 	{
 		synchronized (mClusterMemberships) {
 			if(HRMConfig.DebugOutput.SHOW_CLUSTERING_STEPS){
 				Logging.log(this, "Unregistering cluster membership for: " + pMembership);
 			
 				//HINT: a check for already existing cluster memberships has to be done based on equals AND a check of the peer ClusterName
 			}
 			
 			// remove this cluster membership
 			mClusterMemberships.remove(pMembership);
 		}
 	}
 
 	/**
 	 * Returns all register communication channels
 	 * 
 	 * @return the communication channels
 	 */
 	public LinkedList<ComChannel> getClusterMembershipComChannels()
 	{
 		LinkedList<ComChannel> tResult = new LinkedList<ComChannel>();
 			
 		synchronized (mClusterMemberships) {
 			for (ClusterMember tClusterMembership : mClusterMemberships){
 				tResult.addAll(tClusterMembership.getComChannels());
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Checks if a membership to a given cluster does already exist 
 	 * 
 	 * @param pCluster the ClusterName of a cluster for which the membership is searched
 	 */
 	private boolean hasMembership(ClusterName pCluster)
 	{
 		boolean tResult = false;
 		
 		//Logging.log(this, "Checking cluster membership for: " + pCluster);
 		synchronized (mClusterMemberships) {
 			for(CoordinatorAsClusterMember tClusterMembership : mClusterMemberships){
 				//Logging.log(this, "       ..cluster membership: " + tClusterMembership);
 				//Logging.log(this, "         ..comm. channels: " + tClusterMembership.getComChannels());
 				if(tClusterMembership.hasComChannel(pCluster)){
 					tResult = true;
 					break;
 				}
 			}
 		}
 		return tResult;
 	}
 	
 	/**
 	 * Returns if the initial clustering has already finished
 	 * 
 	 * @return true or false
 	 */
 	public boolean isClustered()
 	{
 		// search for an existing cluster at this hierarchy level
 		Cluster tSuperiorCluster = mHRMController.getCluster(getHierarchyLevel().getValue() + 1);
 		
 		return ((getHierarchyLevel().isHighest()) || ((tSuperiorCluster != null) && (hasMembership(tSuperiorCluster))));
 	}
 
 	/**
 	 * Returns the hierarchy Bully priority of the node
 	 * 
 	 * @return the Bully priority
 	 */
 	@Override
 	public BullyPriority getPriority() 
 	{
 		return BullyPriority.create(this, mHRMController.getHierarchyNodePriority(getHierarchyLevel()));
 	}
 	
 	/**
 	 * Sets a new Bully priority
 	 * 
 	 * @param pPriority the new Bully priority
 	 */
 	@Override
 	public void setPriority(BullyPriority pPriority) 
 	{
 		if (!getPriority().equals(pPriority)){
 			Logging.err(this, "Updating Bully priority from " + getPriority() + " to " + pPriority);
 		}else{
 			Logging.log(this, "Trying to set same Bully priority " + getPriority());
 		}
 		
 		// update the Bully priority of the parent cluster, which is managed by this coordinator
 		mParentCluster.setPriority(pPriority);
 	}
 
 	/**
 	 * Assign new HRMID for being addressable.
 	 * HINT: We use the HRMID of the managed cluster.
 	 *  
 	 * @param pCaller the caller who assigns the new HRMID
 	 * @param pHRMID the new HRMID
 	 */
 	@Override
 	public void setHRMID(Object pCaller, HRMID pHRMID)
 	{
 		Logging.log(this, "Got new HRMID: " + pHRMID + ", caller=" + pCaller);
 
 		// update the Bully priority of the parent cluster, which is managed by this coordinator
 		mParentCluster.setHRMID(pCaller, pHRMID);
 	}
 
 	/**
 	 * Returns the HRMID under which this node is addressable for this cluster
 	 * HINT: We use the HRMID of the managed cluster.
 	 * 
 	 * @return the HRMID
 	 */
 	@Override
 	public HRMID getHRMID() {
 		return mParentCluster.getHRMID();
 	}
 
 	/**
 	 * Returns a reference to the cluster, which this coordinator manages.
 	 * 
 	 * @return the managed cluster
 	 */
 	public Cluster getCluster()
 	{
 		return mParentCluster;
 	}
 	
 	/**
 	 * Returns the unique ID of the parental cluster
 	 * 
 	 * @return the unique cluster ID
 	 */
 	@Override
 	public Long getClusterID() {
 		return mParentCluster.getClusterID();
 	}
 	
 	/**
 	 * Generates a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return toLocation() + "(" + idToString() + ")";
 	}
 
 	/**
 	 * Returns a location description about this instance
 	 * 
 	 * @return the location description
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + getGUICoordinatorID() + "@" + mHRMController.getNodeGUIName() + "@" + getHierarchyLevel().getValue();
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a string including the ClusterID, the coordinator ID, and the node priority
 	 * 
 	 * @return the complex string
 	 */
 	private String idToString()
 	{
 		if ((getHRMID() == null) || (getHRMID().isRelativeAddress())){
 			return "Cluster" + getGUIClusterID();
 		}else{
 			return "Cluster" + getGUIClusterID() + ", HRMID=" + getHRMID().toString();
 		}
 	}
 }
