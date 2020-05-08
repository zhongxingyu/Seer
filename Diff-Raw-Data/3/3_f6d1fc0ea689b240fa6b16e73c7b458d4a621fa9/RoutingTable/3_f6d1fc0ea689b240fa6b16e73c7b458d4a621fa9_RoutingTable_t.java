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
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class represents an HRM routing table
  */
 public class RoutingTable extends LinkedList<RoutingEntry>
 {
 
 	private static final long serialVersionUID = -9166625971164847894L;
 
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
 		if(pRoutingTableEntry.getSource().isZero()){
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
 				 * Check if known route was defined by a higher authority
 				 */
 				boolean tKnownRouteIsFromHigherAuthotiry = false;
 				if(pRoutingTableEntry.getOwner() != null){
 					if(tFoundDuplicate.getOwner() != null){
 						if(tFoundDuplicate.getOwner().getHierarchyLevel() > pRoutingTableEntry.getOwner().getHierarchyLevel()){
 							// indeed, we already know this route from a higher authority
 							tKnownRouteIsFromHigherAuthotiry = true;
 						}
 					}
 				}
 				
 				/**
 				 * Apply the given route
 				 */
 				if(!tKnownRouteIsFromHigherAuthotiry){
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
 		
 		for(RoutingEntry tEntry : pRoutingTable){
 
 			tResult |= delEntry(tEntry);
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
 	 * 
 	 * @return the found best entry
 	 */
 	public synchronized RoutingEntry getBestEntry(HRMID pDestination, int pDesiredMaxDelay, int pDesiredMinDataRate)
 	{
 		RoutingEntry tResult = null;
 		RoutingEntry tBestResultHopCount = null;
 		RoutingEntry tBestResultQoS = null;
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
 					 * BE metrics, optimize for:
 					 * 		1.) hop count
 					 * 		2.) data rate
 					 * 		3.) delay
 					 */
 					if(tBestResultHopCount != null){
 						if( 
 						  // better hop count?
 						  (tBestResultHopCount.getHopCount() > tEntry.getHopCount()) || 
 						  (
 						      // hop count is the same and and another criterion is better?
 							  (tBestResultHopCount.getHopCount() == tEntry.getHopCount()) && 
 							  ( 
 							      // better data rate along the route?		  
 						          (tBestResultHopCount.getMaxAvailableDataRate() < tEntry.getMaxAvailableDataRate()) ||
 								  ( 
 									  // date rate is also the same, but the delay is better along the route?	  
 								      (tBestResultHopCount.getMaxAvailableDataRate() == tEntry.getMaxAvailableDataRate()) && (tBestResultHopCount.getMinDelay() > tEntry.getMinDelay()) 
 								  )
 							  ) 
 						  ) 
 						  ){
 							
 							if (DEBUG){
 								Logging.log(this, "      ..found better (BE) entry: " + tEntry);
 							}
 
 							tBestResultHopCount = tEntry.clone();
 						}else{
 							if (DEBUG){
 								Logging.log(this, "      ..found uninteresting (BE) entry: " + tEntry);
 							}
 						}
 					}else{
 						if (DEBUG){
 							Logging.log(this, "      ..found first matching (BE) entry: " + tEntry);
 						}
 
 						tBestResultHopCount = tEntry.clone();
 					}
 					
 					/**
 					 * QoS metrics, optimize for:
 					 * 		1.) data rate (if desired)
 					 * 		2.) delay (if desired)
 					 * 		3.) hop count 		
 					 */
					if ((pDesiredMaxDelay > 0) || (pDesiredMinDataRate > 0)){
 						if(tBestResultQoS != null){						
 							
 							if(
 							  ( (pDesiredMinDataRate > 0) &&
      						  // better remaining available data rate?
 							  ( (tBestResultQoS.getMaxAvailableDataRate() < tEntry.getMaxAvailableDataRate()) ||
 						      // same date rate, delay isn't important and a better hop count?
 							  ( (tBestResultQoS.getMaxAvailableDataRate() == tEntry.getMaxAvailableDataRate()) && (pDesiredMaxDelay <= 0) && (tBestResultQoS.getHopCount() > tEntry.getHopCount()) ) ) ) ||
 							  
 							  ( (pDesiredMaxDelay > 0) && ( (pDesiredMinDataRate <= 0) || (tBestResultQoS.getMaxAvailableDataRate() == tEntry.getMaxAvailableDataRate()) ) &&  	  
      						  // better delay towards destination?
 							  ( (tBestResultQoS.getMinDelay() > tEntry.getMinDelay()) ||
 						      // same delay towards destination and a better hop count?
 							  ( (tBestResultQoS.getMinDelay() == tEntry.getMinDelay()) && (tBestResultQoS.getHopCount() > tEntry.getHopCount()) ) ) )
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
 					if (DEBUG){
 						Logging.log(this, "      ..ignoring entry: " + tEntry);
 					}
 				}
 			}
 
 			/**
 			 * BE result
 			 */
 			tResult = tBestResultHopCount;
 			
 			/**
 			 * QoS result
 			 */
 			if((pDesiredMinDataRate > 0) || (pDesiredMaxDelay > 0)) {
 				if(tBestResultQoS != null){
 					tResult = tBestResultQoS;
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
 	
 	public String toString()
 	{
 		return "[" + getClass().getSimpleName() + " with " + size() + "entries]"; 
 	}
 }
