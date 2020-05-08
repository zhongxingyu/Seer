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
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.AnnounceCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.InvalidCoordinator;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RouteReport;
 import de.tuilmenau.ics.fog.packets.hierarchical.topology.RouteShare;
 import de.tuilmenau.ics.fog.routing.hierarchical.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.ElectionPriority;
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
 	private long mSentAnnounces = 0;
 	
 	/**
 	 * Stores how many invalidations were already sent
 	 */
 	private long mSentInvalidations = 0;
 	
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
 	private void setSuperiorCoordinatorComChannel(ComChannel pComChannel)
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
 	private void sendSuperiorCoordinator(SignalingMessageHrm pPacket)
 	{
 		if(HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 			Logging.log(this, "Sending to superior coordinator: " + pPacket);
 		}
 		
 		if(isSuperiorCoordinatorValid()){
 			// plausibility check if we actually use a link from a CoordinatorAsClusterMember
 			if(superiorCoordinatorComChannel().getParent() instanceof CoordinatorAsClusterMember){				
 				CoordinatorAsClusterMember tCoordinatorAsClusterMember = (CoordinatorAsClusterMember)superiorCoordinatorComChannel().getParent();
 				if(tCoordinatorAsClusterMember.isThisEntityValid()){
 					if(tCoordinatorAsClusterMember.getComChannelToClusterHead() != null){
 						// plausibility check if we actually use an active link
 						if(tCoordinatorAsClusterMember.getComChannelToClusterHead().isLinkActive()){
 							superiorCoordinatorComChannel().sendPacket(pPacket);
 						}else{
 							Logging.err(this, "sendSuperiorCoordinator() expected an active link, link is: " + superiorCoordinatorComChannel() + ", dropping: " + pPacket);
 						}
 					}else{
 						Logging.warn(this, "sendSuperiorCoordinator() aborted because the comm. channel to the cluster head is invalid for: " + tCoordinatorAsClusterMember + ", dropping: " + pPacket);
 					}
 				}else{
 					Logging.warn(this, "sendSuperiorCoordinator() aborted because of an invalidated CoordinatorAsClusterMember: " + tCoordinatorAsClusterMember + ", channel: "  + superiorCoordinatorComChannel() + ", dropping: " + pPacket);
 				}
 			}else{
 				Logging.err(this, "sendSuperiorCoordinator() expected a CoordinatorAsClusterMember as parent of: " + superiorCoordinatorComChannel() + ", dropping: " + pPacket);
 			}
 		}else{
 			Logging.err(this, "sendSuperiorCoordinator() aborted because the comm. channel to the superior coordinator is invalid" + ", dropping: " + pPacket);
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
 		boolean DEBUG_SHARE_PHASE_DETAILS = false;
 //		if(getHierarchyLevel().getValue() == 1){
 //			DEBUG_SHARE_PHASE_DETAILS = true;
 //		}
 		
 		// should we start the "share phase"?
 		if (sharePhaseHasTimeout()){
 			if (DEBUG_SHARE_PHASE_DETAILS){
 				Logging.log(this, "SHARE PHASE with cluster members on level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 			}
 
 			// store the time of this "share phase"
 			mTimeOfLastSharePhase = mHRMController.getSimulationTime();
 
 			if ((!HRMConfig.Routing.PERIODIC_SHARE_PHASES) && (!hasNewSharePhaseData())){ //TODO
 				if (DEBUG_SHARE_PHASE_DETAILS){
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
 						
 //						DEBUG_SHARE_PHASE_DETAILS = false;
 //						if(getHierarchyLevel().getValue() == 1){
 //							if(tPeerHRMID.getLevelAddress(1) == 5){
 //								DEBUG_SHARE_PHASE_DETAILS = true;
 //							}
 //						}
 						
 						/**
 						 * copy the received shared routing table from the superior coordinator
 						 */
 						RoutingTable tReceivedSharedRoutingTable = null;
 						synchronized (mReceivedSharedRoutingTable) {
 							tReceivedSharedRoutingTable = (RoutingTable) mReceivedSharedRoutingTable.clone();	
 						}
 
 						if(getHierarchyLevel().isBaseLevel()){
 							if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 								Logging.log(this, "  ..sharing routes with node: " + tPeerHRMID);
 							}
 
 							/*********************************************************************
 							 * SHARE 1: received routes from superior coordinator
 							 *********************************************************************/
 							HRMID tThisNodeHRMID = getCluster().getL0HRMID();
 
 							int j = -1;
 							for(RoutingEntry tReceivedSharedRoutingEntry : tReceivedSharedRoutingTable){
 								j++;
 								
 								/**
 								 * does the received route start at the peer? 
 								 * 		=> share: [original route from sup. coordinator]
 								 */
 								if(tReceivedSharedRoutingEntry.getSource().isCluster(tPeerHRMID)){
 									RoutingEntry tNewEntry = tReceivedSharedRoutingEntry.clone();
 									// reset L2Address for next hop
 									tNewEntry.setNextHopL2Address(null);
 									tNewEntry.extendCause(this + "::sharePhase()_ReceivedRouteShare_1(" + mCallsSharePhase + ")(" + j + ") as " + tNewEntry);
 									// share the received entry with the peer
 									tSharedRoutingTable.addEntry(tNewEntry);
 									
 									continue;
 								}
 								
 								/**
 								 * does the received route start at this node and the next node isn't the peer?
 								 * 		=> share: [route from peer to this node] ==> [original route from sup. coordinator]
 								 */
 								if((tReceivedSharedRoutingEntry.getSource().equals(tThisNodeHRMID)) && (!tReceivedSharedRoutingEntry.getNextHop().equals(tPeerHRMID))){
 									RoutingEntry tRoutingEntryWithPeer = mHRMController.getNeighborRoutingEntryHRG(tPeerHRMID, tThisNodeHRMID);
 									if(tRoutingEntryWithPeer != null){
 										RoutingEntry tNewEntry = tRoutingEntryWithPeer.clone(); 
 										// reset L2Address for next hop
 										tNewEntry.setNextHopL2Address(null);
 										tNewEntry.append(tReceivedSharedRoutingEntry, this + "::sharePhase()_append1_route_from_peer_via_me_to_destination(" + mCallsSharePhase + ")");
 										tNewEntry.extendCause(this + "::sharePhase()_ReceivedRouteShare_2(" + mCallsSharePhase + ")(" + j + ") as combination of " + tRoutingEntryWithPeer + " and " + tReceivedSharedRoutingEntry + " as " + tNewEntry);
 										// share the received entry with the peer
 										tSharedRoutingTable.addEntry(tNewEntry);
 									}
 									
 									continue;
 								}
 							}
 							
 							/*********************************************************************
 							 * SHARE 2: routes to known siblings of the peer at hierarchy level 0
 							 *********************************************************************/
 							// find all siblings of the peer
 							//HINT: the peer is one hierarchy level below this coordinator
 							if(mParentCluster.countConnectedRemoteClusterMembers() > 1){
 								LinkedList<HRMID> tKnownPeerSiblings = mHRMController.getSiblingsHRG(tPeerHRMID);
 								for(HRMID tPossibleDestination : tKnownPeerSiblings){
 									if (DEBUG_SHARE_PHASE_DETAILS){
 										Logging.log(this, "    ..possible sibling destination for " + tPeerHRMID + " is: " + tPossibleDestination);
 										Logging.log(this, "      ..determining path from " + tPeerHRMID + " to " + tPossibleDestination);
 									}
 	
 									/**
 									 * Get the route from the local HRG from the peer to its sibling	
 									 */
 									RoutingEntry tRoutingEntryToPossibleDestination = mHRMController.getRoutingEntryHRG(tPeerHRMID, tPossibleDestination, this + "::sharePhase()(" + mCallsSharePhase + ") for a route from " + tPeerHRMID + " to " + tPossibleDestination + " ==> ");
 									
 									/**
 									 * Add the found routing entry to the shared routing table
 									 */
 									if(tRoutingEntryToPossibleDestination != null){
 										tRoutingEntryToPossibleDestination.extendCause(this + "::sharePhase()_HRG_based(" + mCallsSharePhase + ") as " + tRoutingEntryToPossibleDestination);
 										// set the L2Address of the next hop again
 										tRoutingEntryToPossibleDestination.setNextHopL2Address(mHRMController.getHRS().getL2AddressFor(tRoutingEntryToPossibleDestination.getNextHop()));
 										tSharedRoutingTable.addEntry(tRoutingEntryToPossibleDestination);
 									}
 								}
 							}
 
 						}else{
 							if (DEBUG_SHARE_PHASE_DETAILS){
 								Logging.log(this, "  ..sharing routes with coordinator: " + tPeerHRMID);
 							}
 								
 							/*********************************************************************
 							 * SHARE 1: received routes from superior coordinator
 							 *********************************************************************/
 
 							int j = -1;
 							for(RoutingEntry tReceivedSharedRoutingEntry : tReceivedSharedRoutingTable){
 								if (DEBUG_SHARE_PHASE_DETAILS){
 									Logging.log(this, "    ..found entry from super coordinator: " + tReceivedSharedRoutingEntry);
 								}
 								j++;
 
 								/**
 								 * does the received route start at the peer (inferior cluster)? 
 								 * 		=> share: [original route from sup. coordinator]
 								 */
 								if(tReceivedSharedRoutingEntry.getSource().isCluster(tPeerHRMID)){
 									if (DEBUG_SHARE_PHASE_DETAILS){
 										Logging.log(this, "      .." + tReceivedSharedRoutingEntry.getSource() + " belongs to cluster " + tPeerHRMID);
 									}
 									RoutingEntry tNewEntry = tReceivedSharedRoutingEntry.clone();
 									// reset L2Address for next hop
 									tNewEntry.setNextHopL2Address(null);
 									tNewEntry.extendCause(this + "::sharePhase()_ReceivedRouteShare_2_1(" + mCallsSharePhase + ")(" + j + ") as " + tNewEntry);
 									// share the received entry with the peer
 									tSharedRoutingTable.addEntry(tNewEntry);
 									
 									continue;
 								}else{
 									if (DEBUG_SHARE_PHASE_DETAILS){
 										Logging.log(this, "      .." + tReceivedSharedRoutingEntry.getSource() + " DOES NOT BELONG to cluster " + tPeerHRMID);
 									}
 								}
 								
 								/**
 								 * does the received route start in this cluster?
 								 */
 								if(tReceivedSharedRoutingEntry.getSource().isCluster(getHRMID())){
 									if (DEBUG_SHARE_PHASE_DETAILS){
 										Logging.log(this, "      ..source belongs to this topology sharing cluster: " + tReceivedSharedRoutingEntry);
 									}
 											
 									/**
 									 * Determine the destination gateway for the intra-cluster routing
 									 */
 									HRMID tDestinationGatewayForIntraClusterRoute = tReceivedSharedRoutingEntry.getSource(); //NextHop().getClusterAddress(getHierarchyLevel().getValue() - 1);
 									if (DEBUG_SHARE_PHASE_DETAILS){
 										Logging.log(this, "      ..destination gateway for intra-cluster route: " + tDestinationGatewayForIntraClusterRoute);
 									}
 									
 									/**
 									 * Get the route from the local HRG from the peer to its sibling	
 									 */
 									RoutingEntry tIntraClusterRoutingEntry = mHRMController.getRoutingEntryHRG(tPeerHRMID, tDestinationGatewayForIntraClusterRoute, this + "::sharePhase()(" + mCallsSharePhase + ") for a route from " + tPeerHRMID + " to " + tDestinationGatewayForIntraClusterRoute + " ==> ");
 									if (DEBUG_SHARE_PHASE_DETAILS){
 										Logging.log(this, "      ..determined intra-cluster route to gateway: " + tIntraClusterRoutingEntry);
 									}
 									
 									if(tIntraClusterRoutingEntry != null){
 										RoutingEntry tNewEntry = tIntraClusterRoutingEntry.clone();
 										
 										/**
 										 * Add the more abstract route which was received in a "share" message from the superior coordinator
 										 */
 										tNewEntry.append(tReceivedSharedRoutingEntry, this + "::sharePhase()_ReceivedRouteShare_2_2(" + mCallsSharePhase + "), appending: " + tReceivedSharedRoutingEntry);
 										
 										/**
 										 * Add the found routing entry to the shared routing table
 										 */
 										// reset L2Address for next hop
 										tNewEntry.extendCause(this + "::sharePhase()_HRG_based(" + mCallsSharePhase + ") as " + tNewEntry);
 										tSharedRoutingTable.addEntry(tNewEntry);
 									}else{
 										Logging.err(this, "sharePhase() for " + tPeerHRMID + " couldn't find an intra-cluster route from " + tPeerHRMID + " to " + tDestinationGatewayForIntraClusterRoute + " for using the received share route: " + tReceivedSharedRoutingEntry);
 									}
 								}else{
 									Logging.err(this, "sharePhase() for " + tPeerHRMID + " detected a shared route which does not start in this cluster: " + tReceivedSharedRoutingEntry);
 								}
 							}
 							
 							/*********************************************************************
 							 * SHARE 2: routes to known siblings of the peer at the same hierarchy level
 							 *********************************************************************/
 							// find all siblings of the peer
 							//HINT: the peer is one hierarchy level below this coordinator
 							LinkedList<HRMID> tKnownPeerSiblings = mHRMController.getSiblingsHRG(tPeerHRMID);
 							for(HRMID tPossibleDestination : tKnownPeerSiblings){
 								if (DEBUG_SHARE_PHASE_DETAILS){
 									Logging.log(this, "    ..possible sibling destination for " + tPeerHRMID + " is: " + tPossibleDestination);
 									Logging.log(this, "      ..determining path from " + tPeerHRMID + " to " + tPossibleDestination);
 								}
 
 								/**
 								 * Get the route from the local HRG from the peer to its sibling	
 								 */
 								if(HRMConfig.Routing.MULTIPATH_ROUTING){
 									RoutingTable tAllRoutingEntriesToPossibleDestination = mHRMController.getAllRoutingEntriesHRG(tPeerHRMID, tPossibleDestination, this + "::sharePhase()(" + mCallsSharePhase + ") for a route from " + tPeerHRMID + " to " + tPossibleDestination + " ==> ");
 									if (DEBUG_SHARE_PHASE_DETAILS){
 										Logging.log(this, "   ..found " + tAllRoutingEntriesToPossibleDestination.size() + " routes from " + tPeerHRMID + " to " + tPossibleDestination);
 									}
 									for(RoutingEntry tRoutingEntryToPossibleDestination : tAllRoutingEntriesToPossibleDestination){
 										if (DEBUG_SHARE_PHASE_DETAILS){
 											Logging.log(this, "     ..entry: " + tRoutingEntryToPossibleDestination);
 										}
 	
 										/**
 										 * Add the found routing entry to the shared routing table
 										 */
 										// reset L2Address for next hop
 										tRoutingEntryToPossibleDestination.extendCause(this + "::sharePhase()_HRG_based(" + mCallsSharePhase + ") as " + tRoutingEntryToPossibleDestination);
 										tSharedRoutingTable.addEntry(tRoutingEntryToPossibleDestination);
 									}
 								}else{
 									RoutingEntry tRoutingEntryToPossibleDestination = mHRMController.getRoutingEntryHRG(tPeerHRMID, tPossibleDestination, this + "::sharePhase()(" + mCallsSharePhase + ") for a route from " + tPeerHRMID + " to " + tPossibleDestination + " ==> ");
 									/**
 									 * Add the found routing entry to the shared routing table
 									 */
 									if(tRoutingEntryToPossibleDestination != null){
 										tRoutingEntryToPossibleDestination.extendCause(this + "::sharePhase()_HRG_based(" + mCallsSharePhase + ") as " + tRoutingEntryToPossibleDestination);
 										tSharedRoutingTable.addEntry(tRoutingEntryToPossibleDestination);
 									}
 								}
 							} // for(HRMID tPossibleDestination : tKnownPeerSiblings)
 						}
 						
 						if(tSharedRoutingTable.size() > 0){
 							if (DEBUG_SHARE_PHASE_DETAILS){
 								Logging.log(this, "     SHARING with: " + tPeerHRMID);
 								for(RoutingEntry tEntry : tSharedRoutingTable){	
 									Logging.log(this, "      ..==> routing entry: " + tEntry);
 								}
 							}
 							
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
 	 * Checks if the channel to the superior coordinator is valid
 	 * 
 	 *  @return true or false
 	 */
 	public boolean isSuperiorCoordinatorValid()
 	{
 		boolean tResult = false;
 		
 		if (!getHierarchyLevel().isHighest()){
 			if(superiorCoordinatorComChannel() != null){
 				tResult = true;
 			}
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * This function implements the "report phase".
 	 * It sends locally stored sharable routing data to the superior coordinator
 	 */
 	public void reportPhase()
 	{
 		/**
 		 * Auto. delete deprecated routes
 		 */
 		if(getHierarchyLevel().isBaseLevel()){
 			mHRMController.autoRemoveObsoleteHRGLinks();
 		}
 		
 		/**
 		 * Create the report based on current topology data
 		 */
 		// the highest coordinator does not have any superior coordinator
 		if (!getHierarchyLevel().isHighest()){
 			// do we have a valid channel to the superior coordinator?
 			if(isSuperiorCoordinatorValid()){
 				// HINT: we do not report topology data which is already locally known
 				if(!mHRMController.getNodeL2Address().equals(superiorCoordinatorComChannel().getPeerL2Address())){
 					RoutingTable tReportRoutingTable = new RoutingTable();
 	
 					/****************************************************************************************************************************
 					 * Report 1: inter-cluster links to all neighbor clusters based on the local HRG
 					 * 			 If we are "1.2.0", we report forward/backward route with "1.3.0" and with "1.1.0" (if both clusters are direct neighbors)
 					 ***************************************************************************************************************************/
 					RoutingTable tRoutesToNeighbors = mHRMController.getReportRoutesToNeighborsHRG(getHRMID());
 					if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 						Logging.log(this, "   ..got inter-cluster routing report: " + tRoutesToNeighbors);
 					}
 					// add the found routes to the report routing table
 					tReportRoutingTable.addEntries(tRoutesToNeighbors);
 
 					/****************************************************************************************************************************
 					 * Report 2: intra-cluster routes between all possible combinations of gateway pairings
 					 ***************************************************************************************************************************/							
 					if(getHierarchyLevel().isBaseLevel()){
 						/***************************************************************
 						 * (L0): routes to remote ClusterMember (physical neighbor nodes) based on node-to-node messages
 						 **************************************************************/
 						if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 							Logging.log(this, "REPORT PHASE at hierarchy level " + getHierarchyLevel().getValue() + "/" + (HRMConfig.Hierarchy.HEIGHT - 1));
 						}
 
 						// get all comm. channels
 						LinkedList<ComChannel> tComChannels = mParentCluster.getComChannels();
 						// iterate over all comm. channels and fetch the recorded route reports
 						for(ComChannel tComChannel : tComChannels){
 							RoutingTable tComChannelTable = tComChannel.getReportedRoutingTable();
 							if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 								Logging.log(this, "   ..got L0 intra-cluster routing report: " + tComChannelTable);
 							}
 							// add the found routes to the overall route report, which is later sent to the superior coordinator
 							tReportRoutingTable.addEntries(tComChannelTable);
 						}
 					}else{
 						/**************************************************************
 						 * (L1+): routes between gateways
 						 **************************************************************/						
 						/**
 						 * step 1: find gateways
 						 */
 						ArrayList<HRMID> tGateways = new ArrayList<HRMID>();
 						LinkedList<HRMID> tNeighbors = mHRMController.getNeighborsHRG(getHRMID());
 						if(!tNeighbors.isEmpty()){
 							for(HRMID tNeighbor : tNeighbors){
 								if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 									Logging.log(this, "    ..found neighbor: " + tNeighbor);
 								}
 								// get the link to the neighbor
 								RoutingEntry tRoutingEntryToNeighbor = mHRMController.getNeighborRoutingEntryHRG(getHRMID(), tNeighbor);
 								if(tRoutingEntryToNeighbor != null){
 									HRMID tGateway = tRoutingEntryToNeighbor.getSource();
 									if(!tGateways.contains(tGateway)){
 										// get the cluster-internal source node for the inter-cluster link
 										if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 											Logging.log(this, "      ..found gateway: " + tGateway);
 										}
 										tGateways.add(tGateway);
 									}
 								}
 							}
 						}
 						/**
 						 * step 2: combine all gateways to pairs and determine a route per combination
 						 */ 
 						if(!tGateways.isEmpty()){
 							// do we have at least one possible combination?
 							if(tGateways.size() > 1){
 								/**
 								 * combine all gateways
 								 */ 
 								for(int tOuter = 0; tOuter < tGateways.size(); tOuter++){
 									for (int tInner = tOuter + 1; tInner < tGateways.size(); tInner++){
 										HRMID tSourceGateway = tGateways.get(tOuter);
 										HRMID tDestinationGateway = tGateways.get(tInner);
 
 										if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 											Logging.log(this, "      ..need a route from " + tSourceGateway + " to " + tDestinationGateway);
 										}
 										
 										/**********************************************************************
 										 ** step 2.1: forward route from "outer gateway" to "inner gateway"
 										 ** step 2.2: backward route from "inner gateway" to "outer gateway"
 										 **********************************************************************/
 										int tLoop = 1;
 										while(tLoop < 3){ // -> steps 2.1 and 2.2 need 2 loops
 											List<AbstractRoutingGraphLink> tPath = mHRMController.getRouteHRG(tSourceGateway, tDestinationGateway);
 											if(tPath != null){
 												if(!tPath.isEmpty()){
 													// the searched routing entry between the current two gateways 
 													RoutingEntry tFinalRoutingEntryBetweenGateways = null;
 													
 													/**
 													 * Determine a gateway-2-gateway route
 													 */
 													int tStep = 0;
 													for(AbstractRoutingGraphLink tLink : tPath){
 														RoutingEntry tStepRoutingEntry = (RoutingEntry)tLink.getRoute().getFirst();
 														
 														// chain the routing entries
 														if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 															Logging.log(this, "        ..step[ " + tStep + "]: " + tStepRoutingEntry);
 														}
 														tStep++;
 														
 														if(tFinalRoutingEntryBetweenGateways == null){
 															tFinalRoutingEntryBetweenGateways = tStepRoutingEntry;
 														}else{
 															tFinalRoutingEntryBetweenGateways.append(tStepRoutingEntry, this + "::reportPhase()_ReceivedRouteShare_append1");
 														}													
 													}
 													
 													/**
 													 * derive and add an entry to the routing report
 													 */ 
 													if(tFinalRoutingEntryBetweenGateways != null){
 														// enforce the destination gateway as next hop
 //														tFinalRoutingEntryBetweenGateways.setNextHop(tDestinationGateway);
 														tFinalRoutingEntryBetweenGateways.setRouteForClusterTraversal();
 	
 														if (HRMConfig.DebugOutput.SHOW_REPORT_PHASE){
 															Logging.log(this, "   ..got L1+ intra-cluster routing report entry: " + tFinalRoutingEntryBetweenGateways);
 														}
 	
 														// add the found gate-2-gateway route to the overall route report, which is later sent to the superior coordinator
 														tFinalRoutingEntryBetweenGateways.extendCause(this + "::reportPhase()_inter-gateway-route from " + tSourceGateway + " to " + tDestinationGateway);
 														// reset next hop L2Address
 														tFinalRoutingEntryBetweenGateways.setNextHopL2Address(null);
 														tReportRoutingTable.addEntry(tFinalRoutingEntryBetweenGateways);
 													}
 												}
 											}
 											
 											/**********************************************************************
 											 ** prepare step 2.2: backward route
 											 **********************************************************************/
 											tDestinationGateway = tGateways.get(tOuter);
 											tSourceGateway = tGateways.get(tInner);
 											
 											tLoop++;
 										}
 									}
 								}
 							}
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
 				Logging.warn(this, "reportPhase() aborted because channel to superior coordinator [" + superiorCoordinatorID() + "] is invalid for: " + superiorCoordinatorDescription());
 			}
 		}else{
 			// nothing to be done here: we are the highest hierarchy level, no one to send topology reports to
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
 			mHRMController.addHRMRouteShare(mReceivedSharedRoutingTable, getHierarchyLevel(), pSourceComChannel.getPeerHRMID(), this + "::eventReceivedRouteShare()");			
 		}		
 	}
 
 	/**
 	 * EVENT: "eventCoordinatorRoleInvalid", triggered by the Elector, the reaction is:
 	 * 	 	1.) create signaling packet "ElectionLeave"
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
 		 * Inform all superior clusters about our invalidation and invalidate the cluster membership (we leave all elections because we are no longer a possible election winner)
 		 */
 		if (!getHierarchyLevel().isHighest()){
 			Logging.log(this, "     ..invalidating these cluster memberships: " + mClusterMemberships);
 			while(mClusterMemberships.size() > 0) {
 				CoordinatorAsClusterMember tCoordinatorAsClusterMember = mClusterMemberships.getLast();
 				tCoordinatorAsClusterMember.eventCoordinatorAsClusterMemberRoleInvalid();
 			}
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
 	 * SEND: distribute AnnounceCoordinator messages among the neighbors which are within the given max. radius (see HRMConfig)        
 	 */
 	private synchronized void distributeCoordinatorAnnouncement()
 	{
 		if(isThisEntityValid()){
 			// trigger periodic Cluster announcements
 			if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 				if (HRMController.GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
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
 		if((HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS) && (HRMController.GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS)){
 			InvalidCoordinator tInvalidCoordinatorPacket = new InvalidCoordinator(mHRMController, mHRMController.getNodeName(), getCluster().createClusterName(), mHRMController.getNodeL2Address());
 
 			/**
 			 * Count the sent announces
 			 */
 			mSentInvalidations++;
 
 			/**
 			 * Send broadcasts in all locally known clusters at this hierarchy level
 			 */
 			LinkedList<Cluster> tClusters = mHRMController.getAllClusters(0); //HINT: we have to broadcast via level 0, otherwise, an inferior might already be destroyed and the invalidation message might get dropped
 //			if(HRMConfig.DebugOutput.SHOW_DEBUG_COORDINATOR_INVALIDATION_PACKETS){
 				Logging.log(this, "########## Distributing Coordinator invalidation [" + mSentInvalidations + "](to the bottom): " + tInvalidCoordinatorPacket);
 				Logging.log(this, "     ..distributing in clusters: " + tClusters);
 //			}
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
 	public long countAnnounces()
 	{
 		return mSentAnnounces;
 	}
 	
 	/**
 	 * Returns how many invalidations were already sent
 	 * 
 	 * @return the number of invalidations
 	 */
 	public long countInvalidations()
 	{
 		return mSentInvalidations;
 	}
 
 	/**
 	 * Implementation for IEvent::fire()
 	 */
 	@Override
 	public void fire()
 	{
 		if(isThisEntityValid()){
 			if(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS){
 				if(HRMController.GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
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
 				
 				if(mSentAnnounces < HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INITIAL_THRESHOLD){
 					// register next trigger for 
 					mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL, this);
 				}else{
 					// register next trigger for 
 					mHRMController.getAS().getTimeBase().scheduleIn(HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL * HRMConfig.Hierarchy.COORDINATOR_ANNOUNCEMENTS_INTERVAL_LONG_TERM_FACTOR, this);
 				}
 			}
 		}else{
 			if(HRMController.GUI_USER_CTRL_COORDINATOR_ANNOUNCEMENTS){
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
 		if (HRMController.GUI_USER_CTRL_ADDRESS_DISTRUTION){
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
 					mHRMController.cluster(this, getHierarchyLevel().inc());
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
 			if (HRMController.GUI_USER_CTRL_ADDRESS_DISTRUTION){
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
 			 * SEND: acknowledgment -> will be answered by a ElectionPriorityUpdate
 			 */
 			tComChannel.signalRequestClusterMembershipAck(createCoordinatorName());
 
 			/**
 			 * Trigger: comm. channel established 
 			 */
 			tClusterMembership.eventComChannelEstablished(tComChannel);
 	
 			/**
 			 * Trigger: joined a remote cluster (sends an ElectionPriorityUpdate)
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
 		
 		if(pMembership != null){
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
 		}else{
 			/**
 			 * reset all data about superior coordinator
 			 */
 			setSuperiorCoordinatorComChannel(null);
 			eventClusterCoordinatorAvailable(null, 0, null, "");
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
 	 * Returns the hierarchy Election priority of the node
 	 * 
 	 * @return the Election priority
 	 */
 	@Override
 	public ElectionPriority getPriority() 
 	{
 		return ElectionPriority.create(this, mHRMController.getHierarchyNodePriority(getHierarchyLevel()));
 	}
 	
 	/**
 	 * Sets a new Election priority
 	 * 
 	 * @param pPriority the new Election priority
 	 */
 	@Override
 	public void setPriority(ElectionPriority pPriority) 
 	{
 		if (!getPriority().equals(pPriority)){
 			Logging.err(this, "Updating Election priority from " + getPriority() + " to " + pPriority);
 		}else{
 			Logging.log(this, "Trying to set same Election priority " + getPriority());
 		}
 		
 		// update the Election priority of the parent cluster, which is managed by this coordinator
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
 
 		// update the Election priority of the parent cluster, which is managed by this coordinator
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
