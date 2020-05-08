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
				 * Update the timeout value
 				 */
 				if(pRoutingTableEntry.getTimeout() > tFoundDuplicate.getTimeout()){
 					if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 						Logging.log(this, "Updating timeout for: " + tFoundDuplicate + " to: " + pRoutingTableEntry.getTimeout());
 					}
 					tFoundDuplicate.setTimeout(pRoutingTableEntry.getTimeout());
 					tFoundDuplicate.setCause(pRoutingTableEntry.getCause());
 					
 					tResult = true;
 				}else{
					//Logging.log(this, "Cannot update timeout value: " + tFoundDuplicate.getTimeout());
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
 	
 	public synchronized RoutingEntry getBestEntry(HRMID pDestination)
 	{
 		RoutingEntry tResult = null;
 		RoutingEntry tBestResultHopCount = null;
 		int tBestHopCount = HRMConfig.Hierarchy.MAX_HOPS_TO_A_REMOTE_COORDINATOR;
 		
 		/**
 		 * Iterate over all routing entries and search for the best entry
 		 */
 		if(size() > 0){
 			for(RoutingEntry tEntry : this){
 				/**
 				 * Check if the destination belongs to the cluster of this entry
 				 */ 
 				if(pDestination.isCluster(tEntry.getDest())){
 					/**
 					 * MATCH: we have found a matching entry
 					 */
 					
 					/**
 					 * best HOP COUNT match
 					 */
 					if(tBestResultHopCount != null){
 						if(tEntry.getHopCount() < tBestHopCount){
 							if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 								Logging.log(this, "      ..found better matching entry: " + tEntry);
 							}
 
 							tBestResultHopCount = tEntry;
 							tBestHopCount = tEntry.getHopCount();
 						}else{
 							if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 								Logging.log(this, "      ..found worse entry: " + tEntry);
 							}
 						}
 					}else{
 						if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 							Logging.log(this, "      ..found first matching entry: " + tEntry);
 						}
 
 						tBestResultHopCount = tEntry;
 						tBestHopCount = tEntry.getHopCount();
 					}
 					
 					/**
 					 * best BANDWIDTH match
 					 */
 					//TODO
 					
 					/**
 					 * best DELAY match
 					 */
 					//TODO
 				}else{
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "      ..ignoring entry: " + tEntry);
 					}
 				}
 			}
 
 			//TODO: QoS values here
 
 			tResult = (tBestResultHopCount != null ? tBestResultHopCount.clone() : null);
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "      ..found empty routing table");
 			}
 		}
 		
 		return tResult;
 	}
 }
