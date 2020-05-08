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
 		
 		if(pRoutingTableEntry.getDest() == null){
 			Logging.err(this, "addEntry() got an entry with an invalid destination");
 			return false;
 		}
 		
 		if(pRoutingTableEntry.getDest().isZero()){
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
 				if(tEntry.getDest() != null){
 					// have we found a route to the same destination which uses the same next hop?
 					//TODO: what about multiple links to the same next hop?
 					if ((tEntry.getDest().equals(pRoutingTableEntry.getDest())) /* same destination? */ &&
 						(tEntry.getNextHop().equals(pRoutingTableEntry.getNextHop())) /* same next hop? */){
 
 						//Logging.log(this, "REMOVING DUPLICATE: " + tEntry);
 						tFoundDuplicate = tEntry;
 						
 						break;						
 					}							
 				}
 			}
 		}
 		
 		/**
 		 * Add the entry to the local routing table
 		 */
 		if (tFoundDuplicate == null){
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "ADDING ROUTE      : " + pRoutingTableEntry);
 			}
 
 			// add the route to the routing table
			if(pRoutingTableEntry.isLocalLoop()){
				addFirst(pRoutingTableEntry.clone());
			}else{
				add(pRoutingTableEntry.clone());
			}
 			
 			tResult = true;
 		}else{
 			//TODO: support for updates tFoundDuplicate
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
 			if((tEntry.getDest() != null) && (tEntry.getNextHop() != null)){
 				// do the destinations and next hops match?
 				if ((tEntry.getDest().equals(pRoutingTableEntry.getDest())) && 
 					(tEntry.getNextHop().equals(pRoutingTableEntry.getNextHop()))){
 					tRemoveThese.add(tEntry);
 				}
 			}
 		}
 		
 		/**
 		 * Remove all marked RIB entries
 		 */
 		if (tRemoveThese.size() > 0){
 			for(RoutingEntry tEntry: tRemoveThese){
 				remove(tEntry);
 				
 				tResult = true;
 			}
 		}else{
 			Logging.warn(this, "Couldn't remove RIB entry: " + pRoutingTableEntry.toString());
 		}
 		
 		return tResult;
 	}
 
 }
