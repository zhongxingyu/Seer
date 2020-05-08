 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical;
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents an HRM routing table
  */
 public class RoutingTable extends LinkedList<RoutingEntry>
 {
 
 	private static final long serialVersionUID = -9166625971164847894L;
 
 	/**
 	 * REPORT/SHARE:
 	 * Stores if the transmitted routing table describes only a diff to the last update
 	 */
 	private boolean mIsARoutingTableDiff = false;
 
 	/**
 	 * Marks the table as diff to the last update
 	 */
 	public void markAsDiff()
 	{
 		mIsARoutingTableDiff = true;
 	}
 	
 	/**
 	 * Returns true if the table describes only a diff to the last update
 	 * 
 	 * @return true or false
 	 */
	public boolean isOnlyDiff()
 	{
 		return mIsARoutingTableDiff;
 	}
 	
 	/**
 	 * Sets the timeout for all routing table entries
 	 * 
 	 * @param pHRMController the local HRMController instance
 	 */
 	public void setTimeout(HRMController pHRMController)
 	{
 		/**
 		 * set timeout for each routing table entry
 		 */
 		double tLifetime = calcRoutingTableLifetime(pHRMController);
 		for (RoutingEntry tEntry: this){
 			tEntry.setTimeout(tLifetime);
 		}
 	}
 
 	/**
 	 * Calculates the timeout for all routing table entries
 	 * 
 	 * @param pHRMController the local HRMController instance
 	 * 
 	 * @return the timeout
 	 */
 	private double calcRoutingTableLifetime(HRMController pHRMController)
 	{
 		double tResult = HRMConfig.Routing.ROUTE_TIMEOUT  + HRMConfig.Hierarchy.MAX_E2E_DELAY; 
 	
 		if((pHRMController != null) && (pHRMController.hasLongTermStableHierarchy())){
 			//Logging.err(this, "Using higher lifetime here");
 			tResult = HRMConfig.Routing.ROUTE_TIMEOUT_STABLE_HIERARCHY + HRMConfig.Hierarchy.MAX_E2E_DELAY;
 		}
 	
 		return tResult;
 	}
 
 	/**
 	 * Inserts/updates a routing table entry
 	 * 
 	 * @param pRoutingTableEntry the new entry
 	 * 
 	 * @return true if the entry was inserted/updated in the routing table
 	 */
 	public synchronized boolean addEntry(RoutingEntry pRoutingTableEntry)
 	{
 		boolean tResult = false;
 		boolean tRouteIsTooLong = false;
 		RoutingEntry tOldTooLongRoute = null;
 		
 		if(pRoutingTableEntry.getDest() == null){
 			Logging.err(this, "addEntry() got an entry with an invalid destination");
 			return false;
 		}
 		
 		if((pRoutingTableEntry.getDest() != null) && (pRoutingTableEntry.getDest().isZero())){
 			throw new RuntimeException(this + "::addEntry() got an entry with a wildcard destination");
 		}
 		if((pRoutingTableEntry.getSource() != null) && (pRoutingTableEntry.getSource().isZero())){
 			throw new RuntimeException(this + "::addEntry() got an entry with a wildcard source");
 		}
 	
 		/**
 		 * Check for duplicates
 		 */
 		RoutingEntry tFoundDuplicate = null;
 		if (HRMConfig.Routing.AVOID_DUPLICATES_IN_ROUTING_TABLES){
 			for (RoutingEntry tEntry: this){
 				/**
 				 * Search for a SHORTER or LONGER ROUTE DESCRIPTION
 				 */
 				if(tEntry.equalsOutgoingRoute(pRoutingTableEntry)){
 					if(tEntry.getHopCount() < pRoutingTableEntry.getHopCount()){
 						// drop the given routing entry because we already know that the actual route is shorter
 						tRouteIsTooLong = true;
 						
 						//Logging.log(this, "Route is longer than known ones: " + pRoutingTableEntry);
 						break;
 					}
 					if(tEntry.getHopCount() > pRoutingTableEntry.getHopCount()){					
 						tOldTooLongRoute = tEntry;
 					}
 				}
 				
 				/**
 				 * Search for DUPLICATE
 				 */
 				if(tEntry.equals(pRoutingTableEntry)){
 					//Logging.log(this, "REMOVING DUPLICATE: " + tEntry);
 					
 					tFoundDuplicate = tEntry;
 					
 					break;						
 				}
 			}
 		}
 		
 		if(!tRouteIsTooLong){
 			/**
 			 * Add the entry to the local routing table
 			 */
 			if (tFoundDuplicate == null){
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "ADDING ROUTE      : " + pRoutingTableEntry);
 				}
 	
 				// add the route to the routing table
 				if(pRoutingTableEntry.isLocalLoop()){
 					//Logging.log(null, "Adding as first: " + pRoutingTableEntry + ", cause=" + pRoutingTableEntry.getCause());
 					addFirst(pRoutingTableEntry.clone());
 				}else{
 					//Logging.log(null, "Adding as last: " + pRoutingTableEntry + ", cause=" + pRoutingTableEntry.getCause());
 					add(pRoutingTableEntry.clone());
 				}
 				
 				tResult = true;
 			}else{
 				/**
 				 * Update L2Address
 				 */
 				tFoundDuplicate.setNextHopL2Address(pRoutingTableEntry.getNextHopL2Address());
 
 				/**
 				 * Check if known route was defined by a higher authority
 				 */
 				boolean tKnownRouteIsFromHigherAuthority = false;
 				if(pRoutingTableEntry.getOwner() != null){
 					if(tFoundDuplicate.getOwner() != null){
 						if(tFoundDuplicate.getOwner().getHierarchyLevel() > pRoutingTableEntry.getOwner().getHierarchyLevel()){
 							// indeed, we already know this route from a higher authority
 							tKnownRouteIsFromHigherAuthority = true;
 						}
 					}
 				}
 				
 				/**
 				 * Apply the given route
 				 */
 				if(!tKnownRouteIsFromHigherAuthority){
 					/**
 					 * Update TIMEOUT
 					 */
 					if(pRoutingTableEntry.getTimeout() > tFoundDuplicate.getTimeout()){
 						if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 							Logging.log(this, "Updating timeout for: " + tFoundDuplicate + " to: " + pRoutingTableEntry.getTimeout());
 						}
 						tFoundDuplicate.setTimeout(pRoutingTableEntry.getTimeout());
 						tFoundDuplicate.setCause(pRoutingTableEntry.getCause());
 						
 						tResult = true;
 					}else{
 						if (tFoundDuplicate.getTimeout() > 0){
 							//Logging.err(this, "Expected monotonous growing timeout values for: " + pRoutingTableEntry);
 						}
 					}
 					
 					/**
 					 * Update DELAY
 					 */
 					tFoundDuplicate.setMinDelay(pRoutingTableEntry.getMinDelay());
 	
 					/**
 					 * Update BANDWIDTH
 					 */
 					tFoundDuplicate.setMaxAvailableDataRate(pRoutingTableEntry.getMaxAvailableDataRate());
 					tFoundDuplicate.setNextHopMaxAvailableDataRate(pRoutingTableEntry.getNextHopMaxAvailableDataRate());
 	
 					/**
 					 * Update UTILIZATION
 					 */
 					tFoundDuplicate.setUtilization(pRoutingTableEntry.getUtilization());
 					
 					/**
 					 * Update ORIGIN
 					 */
 					tFoundDuplicate.setOrigin(pRoutingTableEntry.getOrigin());
 	
 					/**
 					 * Update OWNER
 					 */
 					tFoundDuplicate.addOwner(pRoutingTableEntry.getOwner());
 	
 					/**
 					 * Update SHARER
 					 */
 					if(pRoutingTableEntry.isSharedLink()){
 						tFoundDuplicate.setSharedLink(pRoutingTableEntry.getShareSender());
 					}
 				}else{
 					// known route is from higher authority
 				}
 			}
 			
 			/**
 			 * Delete an old routing entry which describes the route as too long
 			 */
 			if(tOldTooLongRoute != null){
 				delEntry(tOldTooLongRoute);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Deletes a route from the HRM routing table.
 	 * 
 	 * @param pRoutingTableEntry the routing table entry
 	 *  
 	 * @return true if the entry was found and removed, otherwise false
 	 */
 	public synchronized boolean delEntry(RoutingEntry pRoutingTableEntry)
 	{
 		boolean tResult = false;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REMOVING ROUTE: " + pRoutingTableEntry);
 		}
 
 		LinkedList<RoutingEntry> tRemoveThese = new LinkedList<RoutingEntry>();
 		
 		/**
 		 * Go over the RIB database and search for matching entries, mark them for deletion
 		 */
 		for(RoutingEntry tEntry: this){
 			if(tEntry.equals(pRoutingTableEntry)){
 				tRemoveThese.add(tEntry);
 			}
 		}
 		
 		/**
 		 * Remove all marked RIB entries
 		 */
 		if (tRemoveThese.size() > 0){
 			for(RoutingEntry tEntry: tRemoveThese){
 				//Logging.log(null, "Removing: " + tEntry + ", cause=" + tEntry.getCause());
 				remove(tEntry);
 				
 				tResult = true;
 			}
 		}else{
 			//Logging.warn(this, "Couldn't remove RIB entry: " + pRoutingTableEntry.toString());
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Adds a table to this routing table
 	 * 
 	 * @param pRoutingTable the routing table with new entries
 	 * 
 	 * @return true if the table had new routing data
 	 */
 	public synchronized boolean addEntries(RoutingTable pRoutingTable)
 	{
 		boolean tResult = false;
 		
 		for(RoutingEntry tEntry : pRoutingTable){
 			RoutingEntry tNewEntry = tEntry.clone();
 			tNewEntry.extendCause("RT::addEntries() as " + tNewEntry);
 			
 			tResult |= addEntry(tNewEntry);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Deletes a table from the HRM routing table
 	 * 
 	 * @param pRoutingTable the routing table with old entries
 	 * 
 	 * @return true if the table had existing routing data
 	 */
 	public synchronized boolean delEntries(RoutingTable pRoutingTable)
 	{
 		boolean tResult = false;
 		
		if(pRoutingTable.size() > 0){
			for(RoutingEntry tEntry : pRoutingTable){
	
				tResult |= delEntry(tEntry);
			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Searches for a routing entry which leads to a direct neighbor
 	 * 
 	 * @param pNeighborHRMID the HRMID of the destination neighbor
 	 * 
 	 * @return the found routing entry, null if nothing was found
 	 */
 	public synchronized RoutingEntry getDirectNeighborEntry(HRMID pNeighborHRMID)
 	{
 		RoutingEntry tResult = null;
 		
 		for (RoutingEntry tEntry: this){
 			if(tEntry.isRouteToDirectNeighbor()){
 				if(tEntry.getDest().equals(pNeighborHRMID)){
 					tResult = tEntry.clone();
 					
 					break;						
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines the best entry for the given destination and QoS values
 	 * 
 	 * @param pDestination the desired destination
 	 * @param pDesiredMaxDelay the desired max. E2E delay
 	 * @param pDesiredMinDataRate the desired min. data rate
 	 * @param pForbiddenNextHopHRMID the HRMID of a forbidden next hop
 	 * @param pForbiddenNextHopL2Address the L2Address of a forbidden next hop
 	 * 
 	 * @return the found best entry
 	 */
 	public synchronized RoutingEntry getBestEntry(HRMID pDestination, long pDesiredMaxDelay, long pDesiredMinDataRate, HRMID pForbiddenNextHopHRMID, L2Address pForbiddenNextHopL2Address)
 	{
 		RoutingEntry tResult = null;
 		RoutingEntry tBestResultBERouting = null;
 		RoutingEntry tBestResultQoS = null;
 		RoutingEntry tBestResultMatchingQoS = null;
 		
 		boolean DEBUG = HRMConfig.DebugOutput.GUI_SHOW_ROUTING;
 		
 		if (DEBUG){
 			Logging.log(this, "### Searching for best routing table entry towards: " + pDestination +", desired max. delay=" + pDesiredMaxDelay + ", desired min. data rate=" + pDesiredMinDataRate);
 		}
 		
 		/**
 		 * DATA RATE has the highest priority:
 		 * 		if bandwidth is defined => we want to have a good throughput and little packet loss for multimedia data
 		 * 		if delay is defined => we want to have fast information, mostly short messages are used, e.g., sensor data, packet loss is okay but delayed transmission is bad 
 		 * 		if both is defined => we want to have the golden transmission for some high priority data, in this case we optimize primarily for bandwidth and secondarily for delay
 		 * 		the hop count is always the last optimization criterion
 		 */
 			
 		/**
 		 * Iterate over all routing entries and search for the best entry
 		 */
 		if(size() > 0){
 			for(RoutingEntry tEntry : this){
 				/**
 				 * Check for a matching (route directs packets towards destination) entry
 				 */ 
 				if(pDestination.isCluster(tEntry.getDest())){
 					/**
 					 * Check if the next hop has a forbidden HRMID or L2Address
 					 */
 					if((!tEntry.getNextHop().equals(pForbiddenNextHopHRMID)) && ((tEntry.getNextHopL2Address() == null) || (!tEntry.getNextHopL2Address().equals(pForbiddenNextHopL2Address)))){
 						/**
 						 * BE metrics, optimize for:
 						 * 		1.) hop count
 						 * 		2.) data rate
 						 * 		3.) delay
 						 */
 						if(tBestResultBERouting != null){
 							if( 
 								// better hop count?
 								(tEntry.getHopCount() < tBestResultBERouting.getHopCount()) || 
 								(
 									// should we enforce BE routing?
 									(!HRMController.ENFORCE_BE_ROUTING) && 
 									// hop count is the same and and another criterion is better?
 									(tEntry.getHopCount() == tBestResultBERouting.getHopCount()) && 
 									( 
 										// better data rate along the route?		  
 										(tEntry.getMaxAvailableDataRate() > tBestResultBERouting.getMaxAvailableDataRate()) ||
 										( 
 											// date rate is also the same, but the delay is better along the route?	  
 											(tEntry.getMaxAvailableDataRate() == tBestResultBERouting.getMaxAvailableDataRate()) && (tEntry.getMinDelay() < tBestResultBERouting.getMinDelay()) 
 										)
 									) 
 								) 
 							){
 								if (DEBUG){
 									Logging.log(this, "      ..found better (BE) entry: " + tEntry);
 								}
 
 								tBestResultBERouting = tEntry.clone();
 							}else{
 								if (DEBUG){
 									Logging.log(this, "      ..found uninteresting (BE) entry: " + tEntry);
 								}
 							}
 						}else{
 							if (DEBUG){
 								Logging.log(this, "      ..found first matching (BE) entry: " + tEntry);
 							}
 	
 							tBestResultBERouting = tEntry.clone();
 						}
 						
 						/**
 						 * QoS metrics, optimize for:
 						 * 		1.) data rate (if desired)
 						 * 		2.) delay (if desired)
 						 * 		3.) hop count 		
 						 */
 						if ((pDesiredMaxDelay != 0) || (pDesiredMinDataRate != 0)){
 							/**
 							 * Determine best matching QoS related entry
 							 */
 							if( 
 							  ( (pDesiredMinDataRate <= 0) || (pDesiredMinDataRate <= tEntry.getMaxAvailableDataRate()) ) /* matching data rate */ && 
 							  ( (pDesiredMaxDelay <= 0) || (pDesiredMaxDelay >= tEntry.getMinDelay()) ) /* matching delay */
 							  ){
 								if(tBestResultMatchingQoS != null){
 									if(
 											
 									  (
 											  
 									 /******************************************************************
 									  * condition matrix for a given desired data rate:
 									  * 
 									  *       +---------+----------+-----------+----------+------------+    
 									  *       |  cond.  |    DR    | nextHopDR |   Delay  | HopCount   |
 									  *       +---------+----------+-----------+----------+------------+    
 									  *       |    1    |    >     |           |          |            |
 									  *       |    2    |    =     |     >     |          |            |
 									  *       |    3    |    =     |     =     |     <    |            |
 									  *       |    4    |    =     |     =     |     =    |     <      |
 									  *       +---------+----------+-----------+----------+------------+    
 									  * 
 									  ******************************************************************/
 									  ( pDesiredMinDataRate >= 0) &&
 									  (		
 									  ( (tEntry.getMaxAvailableDataRate()  > tBestResultMatchingQoS.getMaxAvailableDataRate()) ) ||
 								      ( (tEntry.getMaxAvailableDataRate() == tBestResultMatchingQoS.getMaxAvailableDataRate()) && (tEntry.getNextHopMaxAvailableDataRate()  > tBestResultMatchingQoS.getNextHopMaxAvailableDataRate()) ) || 
 									  ( (tEntry.getMaxAvailableDataRate() == tBestResultMatchingQoS.getMaxAvailableDataRate()) && (tEntry.getNextHopMaxAvailableDataRate() == tBestResultMatchingQoS.getNextHopMaxAvailableDataRate()) && (tEntry.getMinDelay()  < tBestResultMatchingQoS.getMinDelay()) ) ||  
 									  ( (tEntry.getMaxAvailableDataRate() == tBestResultMatchingQoS.getMaxAvailableDataRate()) && (tEntry.getNextHopMaxAvailableDataRate() == tBestResultMatchingQoS.getNextHopMaxAvailableDataRate()) && (tEntry.getMinDelay() == tBestResultMatchingQoS.getMinDelay()) && (tEntry.getHopCount() < tBestResultMatchingQoS.getHopCount()) )
 									  )
 									  
 									  ) || (
 											  
 									  ( pDesiredMinDataRate < 0) &&
 									  (
 									  ( (tEntry.getMinDelay()  < tBestResultMatchingQoS.getMinDelay()) ) ||  
 									  ( (tEntry.getMinDelay() == tBestResultMatchingQoS.getMinDelay()) && (tEntry.getMaxAvailableDataRate() > tBestResultMatchingQoS.getMaxAvailableDataRate()) )
 									  )
 									  
 									  )
 									  ){
 										if (DEBUG){
 											Logging.log(this, "      ..found better (QoS match) entry: " + tEntry);
 										}
 			
 										tBestResultMatchingQoS = tEntry.clone();
 									}else{
 										if (DEBUG){
 											Logging.log(this, "      ..found uninteresting (QoS match) entry: " + tEntry);
 										}
 									}
 								}else{
 									if (DEBUG){
 										Logging.log(this, "      ..found first matching (QoS match) entry: " + tEntry);
 									}
 			
 									tBestResultMatchingQoS = tEntry.clone();
 								}
 							}							
 								
 							/**
 							 * Determine best available QoS related entry
 							 */
 							if(tBestResultQoS != null){						
 								/******************************************************************
 								 * condition matrix:
 								 * 
 								 *       +---------+----------+-----------+----------+------------+    
 								 *       |  cond.  |    DR    | nextHopDR |   Delay  | HopCount   |
 								 *       +---------+----------+-----------+----------+------------+    
 								 *       |    1    |    >     |           |          |            |
 								 *       |    2    |    =     |     >     |          |            |
 								 *       |    3    |    =     |     =     |     <    |            |
 								 *       |    4    |    =     |     =     |     =    |     <      |
 								 *       +---------+----------+-----------+----------+------------+    
 								 * 
 								 ******************************************************************/
 								if(
 								  ( (tEntry.getMaxAvailableDataRate()  > tBestResultQoS.getMaxAvailableDataRate()) ) ||
 							      ( (tEntry.getMaxAvailableDataRate() == tBestResultQoS.getMaxAvailableDataRate()) && (tEntry.getNextHopMaxAvailableDataRate()  > tBestResultQoS.getNextHopMaxAvailableDataRate()) ) || 
 								  ( (tEntry.getMaxAvailableDataRate() == tBestResultQoS.getMaxAvailableDataRate()) && (tEntry.getNextHopMaxAvailableDataRate() == tBestResultQoS.getNextHopMaxAvailableDataRate()) && (tEntry.getMinDelay()  < tBestResultQoS.getMinDelay()) ) ||  
 								  ( (tEntry.getMaxAvailableDataRate() == tBestResultQoS.getMaxAvailableDataRate()) && (tEntry.getNextHopMaxAvailableDataRate() == tBestResultQoS.getNextHopMaxAvailableDataRate()) && (tEntry.getMinDelay() == tBestResultQoS.getMinDelay()) && (tEntry.getHopCount() < tBestResultQoS.getHopCount()) )
 								  ){
 									if (DEBUG){
 										Logging.log(this, "      ..found better (QoS) entry: " + tEntry);
 									}
 		
 									tBestResultQoS = tEntry.clone();
 								}else{
 									if (DEBUG){
 										Logging.log(this, "      ..found uninteresting (QoS) entry: " + tEntry);
 									}
 								}
 							}else{
 								if (DEBUG){
 									Logging.log(this, "      ..found first matching (QoS) entry: " + tEntry);
 								}
 		
 								tBestResultQoS = tEntry.clone();
 							}
 						}
 					}else{
 						//if (DEBUG){
 							Logging.log(this, "### Searched for a best routing table entry towards: " + pDestination +", desired max. delay=" + pDesiredMaxDelay + ", desired min. data rate=" + pDesiredMinDataRate);
 							Logging.log(this, "      ..found forbidden (next hop forbidden) entry: " + tEntry);
 						//}
 					}
 				}else{
 					if (DEBUG){
 						Logging.log(this, "      ..ignoring entry: " + tEntry);
 					}
 				}
 			}
 
 			/**
 			 * BE result
 			 */
 			tResult = tBestResultBERouting;
 			boolean tBERouteMatchesQoS = true;
 			if(tBestResultBERouting != null){
 				if((pDesiredMaxDelay > 0) && (tBestResultBERouting.getMinDelay() > pDesiredMaxDelay)){
 					if (DEBUG){
 						Logging.log(this, "      ..BE route doesn't match QoS because of the determined delay along the BE route");
 					}
 					tBERouteMatchesQoS = false;
 				}
 				if((pDesiredMinDataRate > 0) && ((tBestResultBERouting.getMaxAvailableDataRate() < pDesiredMinDataRate) || (tBestResultBERouting.getNextHopMaxAvailableDataRate() < pDesiredMinDataRate))){
 					if (DEBUG){
 						Logging.log(this, "      ..BE route doesn't match QoS because of the determined data rate along the BE route");
 					}
 					tBERouteMatchesQoS = false;
 				}
 			}else{
 				// no valid BE route found
 				tBERouteMatchesQoS = false;
 			}
 			
 			/**
 			 * QoS result
 			 */
 			if(!HRMController.ENFORCE_BE_ROUTING){
 				if((pDesiredMinDataRate != 0) || (pDesiredMaxDelay != 0)) {
 					if((pDesiredMaxDelay < 0) || (!tBERouteMatchesQoS) || (tBestResultBERouting.getUtilization() >= HRMConfig.Routing.MAX_DESIRED_LINK_UTILIZATION) || (tBestResultBERouting.getMaxAvailableDataRate() - pDesiredMinDataRate <= HRMConfig.Routing.MIN_REMAINING_BE_DATA_RATE)){
 						if(tBestResultMatchingQoS != null){
 							if (DEBUG){
 								Logging.log(this, "      ..setting best matching QoS route: " + tBestResultMatchingQoS);
 							}
 	
 							// use route with best matching QoS values
 							tResult = tBestResultMatchingQoS;
 						}else if (tBestResultQoS != null){
 							if (DEBUG){
 								Logging.log(this, "      ..setting best QoS route: " + tBestResultMatchingQoS);
 							}
 	
 							// fall-back to best QoS values
 							tResult = tBestResultQoS;
 						}
 					}
 				}
 			}			
 		}else{
 			if (DEBUG){
 				Logging.log(this, "      ..found empty routing table");
 			}
 		}
 		
 		if (DEBUG){
 			Logging.log(this, "### BEST ROUTE is: " + tResult);
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Returns the size of a serialized representation of this packet
 	 * 
 	 *  @return the size of a serialized representation
 	 */
 	public int getSerializedSize()
 	{
 		/*************************************************************
 		 * Size of serialized elements in [bytes]:
 		 * 
 		 * 		Flags					 = 1 (is a diff?)
 		 * 		Routing table length	 = 2
 		 * 		Routing table			 = dynamic
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		tResult += 1;
 		tResult += 2; // size of the following list
 		for(RoutingEntry tEntry: this){
 			tResult += tEntry.getSerialisedSize();
 			//TODO
 			if(tEntry.getNextHopL2Address() != null){
 				//Logging.warn(this, "Size calculation should contain next L2 address for entry: " + tEntry);
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return "[" + getClass().getSimpleName() + " with " + size() + " entries]"; 
 	}
 }
