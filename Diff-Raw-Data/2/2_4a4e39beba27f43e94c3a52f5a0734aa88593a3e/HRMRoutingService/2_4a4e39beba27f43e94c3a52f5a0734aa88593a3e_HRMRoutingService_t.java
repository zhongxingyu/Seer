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
 
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentAddress;
 import de.tuilmenau.ics.fog.routing.RouteSegmentDescription;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.*;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.DirectDownGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.SimpleName;
 import de.tuilmenau.ics.graph.RoutableGraph;
 
 /**
  * Routing service instance local to a host.
  * 
  * The local information are stored locally. Furthermore, they are duplicated
  * and reported to the next higher level routing service instance.
  */
 public class HRMRoutingService implements RoutingService, Localization
 {
 	/**
 	 * The physical node on which this routing service instance is running.
 	 */
 	private Node mNode = null;
 
 	/**
 	 * Stores the reference to the AutonomousSystem. 
 	 */
 	private AutonomousSystem mAS = null;
 	
 	/**
 	 * Stores the name mapping instance for mapping FoG names to L2 addresses
 	 */
 	private HierarchicalNameMappingService<L2Address> mFoGNamesToL2AddressesMapping = null;
 
 	/**
 	 * Stores the mapping from FoG FNs to L2 addresses
 	 */
 	private HashMap<ForwardingNode, L2Address> mFNToL2AddressMapping = new HashMap<ForwardingNode, L2Address>();
 	
 	/**
 	 * Stores a reference to the local HRMController application
 	 */
 	private HRMController mHRMController = null;
 
 	/**
 	 * Stores the L2 address of the central FN
 	 */
 	private L2Address mCentralFNL2Address = null;
 
 	/**
 	 * Stores the HRMIDs of direct neighbor nodes.
 	 */
 	private LinkedList<HRMID> mDirectNeighborAddresses = new LinkedList<HRMID>();
 
 	/**
 	 * Stores the mapping from HRMIDs of local neighbors to their L2 addresses
 	 */
 	private HashMap<HRMID, L2Address> mHRMIDToL2AddressMapping = new HashMap<HRMID, L2Address>();
 
 	/**
 	 * Stores the HRM based routing table which is used for hop-by-hop routing.
 	 */
 	private RoutingTable mRoutingTable = new RoutingTable();
 	
 	/**
 	 * Stores the local L2 addresses based routing graph (consisting of FNs and Gates from local node and links to direct physical neighbors)
 	 */
 	private final RoutableGraph<L2Address, RoutingServiceLink> mL2RoutingGraph = new RoutableGraph<L2Address, RoutingServiceLink>();
 
 	/**
 	 * Stores if the start of the HRMController application instance is still pending
 	 */
 	private boolean mWaitOnControllerstart = true;
 
 	/**
 	 * Creates a local HRS instance for a node.
 	 * @param pAS the autonomous system at which the HRS is instantiated 
 	 * @param pNode the node on which this routing service instance is created on
 	 */
 	public HRMRoutingService(AutonomousSystem pAS, Node pNode)
 	{
 		Logging.log(this, "CREATED ON " + pNode);
 		
 		mNode = pNode;
 		mAS = pAS;
 		
 		// create name mapping instance to map FoG names to L2 addresses
 		mFoGNamesToL2AddressesMapping = new HierarchicalNameMappingService<L2Address>(HierarchicalNameMappingService.getGlobalNameMappingService(mAS.getSimulation()), null);
 	}
 
 	/**
 	 * This function creates the local HRM controller application. It uses a FoG server FN for offering its CEP.
 	 * For this purpose, the HRS has to be already registered because the server FN registers a node and links at the local (existing!) routing service.
 	 */
 	@Override
 	public void registered()
 	{
 		Logging.log(this, "Got event \"ROUTING SERVICE REGISTERED\"");
 		
 		// create HRM controller instance 
 		mHRMController = new HRMController(mAS, mNode, this);
 		
 		mWaitOnControllerstart = false;
 
 		// end an active waiting by getHRMController()
 		synchronized(this){
 			notify();
 		}
 	}
 
 	/**
 	 * Returns a reference to the HRMController application.
 	 * However, this function waits in case the application wasn't started yet.
 	 * 
 	 * @return the HRMController application
 	 */
 	public HRMController getHRMController()
 	{
 		int tLoop = 0;
 		while(mWaitOnControllerstart){
 			try {
 				synchronized (this) {
 					wait(500 /* ms */);
 				}
 				if (tLoop > 0){
 					Logging.log(this, "WAITING FOR HRMController application start - loop " + tLoop);
 				}
 				tLoop++;
 			} catch (InterruptedException e) {
 				Logging.log(this, "CONTINUING PROCESSING");
 			}		
 		}
 		
 		if (mHRMController == null){
 			throw new RuntimeException(this + ": HRMController reference is still invalid");
 		}
 		
 		return mHRMController;
 	}
 	
 	/**
 	 * Adds a route to the local HRM routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pRoutingTableEntry the routing table entry
 	 * @return true if the entry is new and was added, otherwise false
 	 */
 	public boolean addHRMRoute(RoutingEntry pRoutingTableEntry)
 	{
 		/**
 		 * Store the routing entry in the routing table
 		 */
 		boolean tResult = mRoutingTable.addEntry(pRoutingTableEntry);
 		
 		/**
 		 * Store the destination HRMID in the HRMID-2-L2Address mapping and as direct neighbor
 		 */
 		// is this routing entry new to us?
 		if(tResult){
 			// get the HRMID of the destination
 			HRMID tDestHRMID = pRoutingTableEntry.getDest().clone();
 
 			// save HRMID of the given route if it belongs to a direct neighbor node
 			if (pRoutingTableEntry.isRouteToDirectNeighbor())
 			{
 				synchronized(mDirectNeighborAddresses){
 					// get the L2 address of the next (might be null)
 					L2Address tL2Address = pRoutingTableEntry.getNextHopL2Address();
 					
 					// add address for a direct neighbor
 					if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 						Logging.log(this, "     ..adding " + tDestHRMID + " as address of a direct neighbor");
 					}
 					mDirectNeighborAddresses.add(tDestHRMID);
 
 					if (tL2Address != null){
 						// add L2 address for this direct neighbor
 						if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 							Logging.log(this, "     ..add mapping from " + tDestHRMID + " to " + tL2Address);
 						}
 						/**
 						 * Update mapping HRMID-2-L2Address
 						 */
 						mapHRMID(tDestHRMID, tL2Address);
 					}
 				}
 			}
 			
 			if(pRoutingTableEntry.isLocalLoop()){
 				/**
 				 * Update mapping HRMID-2-L2Address
 				 */
 				mapHRMID(tDestHRMID, mHRMController.getNodeL2Address());
 			}
 		}
 		
 		return tResult;
 	}	
 
 	/**
 	 * Adds routes to the local HRM routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pRoutingTable the routing table with new entries
 	 * 
 	 * @return true if the entry is new and was added, otherwise false
 	 */
 	public boolean addHRMRoutes(RoutingTable pRoutingTable)
 	{
 		boolean tResult = false;
 		
 		for(RoutingEntry tEntry : pRoutingTable){
 			tResult |= addHRMRoute(tEntry);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Deletes a route from the local HRM routing table.
 	 * This function is usually used when a timeout occurred and the corresponding route became too old. 
 	 * 
 	 * @param pRoutingTableEntry the routing table entry
 	 *  
 	 * @return true if the entry was found and removed, otherwise false
 	 */
 	public boolean delHRMRoute(RoutingEntry pRoutingTableEntry)
 	{
 		/**
 		 * Remove the routing entry from the routing table
 		 */
 		boolean tResult = mRoutingTable.delEntry(pRoutingTableEntry);
 		
 		/**
 		 * Remove the destination HRMID from the HRMID-2-L2Address mapping and as direct neighbor
 		 */
 		if(tResult){
 			// get the HRMID of the direct neighbor
 			HRMID tDestHRMID = pRoutingTableEntry.getDest().clone();
 
 			if (pRoutingTableEntry.isRouteToDirectNeighbor()){
 				// add address for a direct neighbor
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..removing " + tDestHRMID + " as address of a direct neighbor");
 				}
 				mDirectNeighborAddresses.remove(tDestHRMID);
 
 				// add L2 address for this direct neighbor
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..remove HRMID-2-L2Address mapping for " + tDestHRMID);
 				}
 				/**
 				 * Update mapping HRMID-2-L2Address
 				 */
 				unmapHRMID(tDestHRMID);
 			}
 			
 			if(pRoutingTableEntry.isLocalLoop()){
 				/**
 				 * Update mapping HRMID-2-L2Address
 				 */
 				unmapHRMID(tDestHRMID);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Deletes routes from the local HRM routing table.
 	 * 
 	 * @param pRoutingTable the routing table with old entries
 	 * 
 	 * @return true if the table had existing routing data
 	 */
 	public boolean delHRMRoutes(RoutingTable pRoutingTable)
 	{
 		boolean tResult = false;
 		
 		for(RoutingEntry tEntry : pRoutingTable){
 			tResult |= delHRMRoute(tEntry);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Registers a route at the local L2 routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pToL2Address the L2Address of the destination
 	 * @param pRoute the route to the direct neighbor
 	 * 
 	 * @return returns true if the route was stored and an GUI update is needed
 	 */
 	public boolean registerLinkL2(L2Address pToL2Address, Route pRoute)
 	{
 		boolean tResult = true;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK: dest.=" + pToL2Address + ", route=\"" + pRoute + "\"");
 		}
 
 		if (pToL2Address != null){
 			
 			/**
 			 * Determine the old logical L2 link towards the neighbor
 			 */
 			Route tOldRoute = null;
 			L2LogicalLink tOldL2Link = null;
 			List<RoutingServiceLink> tOldLinkList = getRouteFromGraph(mL2RoutingGraph, getCentralFNL2Address(), pToL2Address);
 			if((tOldLinkList != null) && (tOldLinkList.size() == 1)){
 				// get the first and only route entry
 				RoutingServiceLink tLink = tOldLinkList.get(0);
 				if(tLink instanceof L2LogicalLink){
 					// get the logical L2 link
 					tOldL2Link = (L2LogicalLink) tLink;
 					
 					// get the old route from the logical L2 link description
 					tOldRoute = tOldL2Link.getRoute();					
 
 					if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 						Logging.log(this, "      ..found old route: " + tOldRoute + " to direct neighbor: " + pToL2Address);
 					}
 				}
 			}
 
 			/**
 			 * Clone the route
 			 */
 			Route tNewRoute = pRoute.clone();
 			
 			/**
 			 * Check if the new route is shorter than the old known one.
 			 * In the latter case, update the old logical link.
 			 */
 			boolean tNewLogicalLink = true;
 			if (tOldRoute != null){
 				// mark as an update instead of a new link
 				tNewLogicalLink = false;
 
 				if (tNewRoute.isShorter(tOldRoute)){
 					if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 						Logging.log(this, "      ..updating to better ROUTE \"" + tNewRoute + "\" to direct neighbor: " + pToL2Address);
 					}
 										
 					// update the old logical link
 					tOldL2Link.setRoute(tNewRoute);
 				}else{
 					if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 						Logging.log(this, "      ..dropping new ROUTE \"" + tNewRoute + "\" to direct neighbor: " + pToL2Address);
 					}
 				}
 			}
 			
 			/**
 			 * Create a new logical link
 			 */
 			if(tNewLogicalLink){
 				Logging.log(this, "      ..storing new ROUTE \"" + tNewRoute + "\" to direct neighbor: " + pToL2Address);
 
 				// store the new route
 				storeL2Link(getCentralFNL2Address(), pToL2Address, new L2LogicalLink(tNewRoute));
 			}
 
 		}else{
 			Logging.err(this, "addRouteToDirectNeighbor() got an invalid neighbor L2Address");
 
 			// route was dropped
 			tResult = false;
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the local HRM routing table
 	 * 
 	 * @return the local HRM routing table
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<RoutingEntry> routingTable()
 	{
 		LinkedList<RoutingEntry> tResult = null;
 		
 		synchronized (mRoutingTable) {
 			tResult = (LinkedList<RoutingEntry>) mRoutingTable.clone();
 		}
 		
 		return tResult;		
 	}
 	
 	/**
 	 * Returns the list of known neighbor HRMIDs
 	 * 
 	 * @return the desired list of HRMIDs
 	 */
 	public HashMap<HRMID, L2Address> getHRMIDToL2AddressMapping()
 	{
 		HashMap<HRMID, L2Address> tResult = new HashMap<HRMID, L2Address>();
 		
 		synchronized (mHRMIDToL2AddressMapping) {
 			for (HRMID tAddr : mHRMIDToL2AddressMapping.keySet()){
 				L2Address tL2Address = mHRMIDToL2AddressMapping.get(tAddr);
 				if (tL2Address != null){
 					tResult.put(tAddr.clone(), tL2Address.clone());
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines a route within a RoutableGraph.
 	 *  
 	 * @param pGraph the RoutableGraph instance
 	 * @param pSource the source where the searched route should start
 	 * @param pDestination the destination where the searched route should start
 	 * @return the found route
 	 */
 	@SuppressWarnings("unchecked")
 	private <LinkType> List<RoutingServiceLink> getRouteFromGraph(RoutableGraph pGraph, HRMName pSource, HRMName pDestination)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET ROUTE in graph " + pGraph.getClass().getSimpleName().toString() + " from " + pSource + " to " + pDestination);
 		}
 
 		List<RoutingServiceLink> tResult = null;
 
 		List<LinkType> tFoundRoute = null;
 		
 		// HINT: keep the locked (synchronized) area small!
 		synchronized (pGraph) {
 			// check if source/destination are known by the graph
 			if(pGraph.contains(pSource) && pGraph.contains(pDestination)) {
 				try {
 					// determine the route in the graph instance
 					tFoundRoute = (List<LinkType>)pGraph.getRoute(pSource, pDestination);
 				} catch (ClassCastException tExc) {
 					Logging.err(this, "Unable to cast the getRouteFromGraph result, returning null", tExc);
 					
 					// reset the result
 					tResult = null;
 				}
 			}
 		}
 
 		// have we found a route in the graph instance?
 		if((tFoundRoute != null) && (!tFoundRoute.isEmpty())) {
 			// create result object
 			tResult = new LinkedList<RoutingServiceLink>();
 			
 			/**
 			 * transform the route from the graph instance to a list of RoutingServiceLink(GateIDs) objects
 			 */
 			if(tFoundRoute.get(0) instanceof RoutingServiceLink) {
 				// iterate over all links(GateIDs), add them to the result list
 				for(RoutingServiceLink tLinkInFoundRoute : (List<RoutingServiceLink>)tFoundRoute) {
 					tResult.add(tLinkInFoundRoute);
 				}
 			} else if(tFoundRoute.get(0) instanceof RouteSegmentPath) {
 				// iterate over all routing segments and their stored links(GateIDs), add them to the result list
 				for(RouteSegmentPath tRouteSegment : (List<RouteSegmentPath>)tFoundRoute) {
 					for(GateID tID : tRouteSegment) {
 						tResult.add(new RoutingServiceLink(tID, null));
 					}
 				}
 			}
 		}
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..RESULT(getRouteFromGraph): " + tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Stores a link in the local L2 specific routing graph
 	 * 
 	 * @param pFromL2Address the starting point of the link
 	 * @param pToL2Address the ending point of the link
 	 * @param pRoutingServiceLink the link description
 	 */
 	private void storeL2Link(L2Address pFromL2Address, L2Address pToL2Address, RoutingServiceLink pRoutingServiceLink)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK IN L2 GRAPH:  source=" + pFromL2Address + " ## dest.=" + pToL2Address + " ## link=" + pRoutingServiceLink);
 		}
 		
 		synchronized (mL2RoutingGraph) {
 			mL2RoutingGraph.link(pFromL2Address, pToL2Address, pRoutingServiceLink);
 		}
 	}
 
 	/**
 	 * Returns the destination node of a link from the local FoG based routing graph
 	 * This function is used within HRMController::getL2AddressOfFirstFNTowardsNeighbor().
 	 * 
 	 * @param pLink the link for which the destination has to be determined
 	 * 
 	 * @return the search destination node
 	 */
 	public HRMName getL2LinkDestination(RoutingServiceLink pLink)
 	{
 		HRMName tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "Search for the L2 destination node of " + pLink);
 		}
 		
 		synchronized (mL2RoutingGraph) {
 			tResult = mL2RoutingGraph.getDest(pLink);
 		}
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Creates a mapping from a FoG name to an L2Address
 	 *   
 	 * @param pName the FoG name
 	 * @param pL2Address the L2 address
 	 * @throws RemoteException
 	 */
 	public void mapFoGNameToL2Address(Name pName, Name pL2Address)
 	{
 		Logging.log(this, "REGISTERING NAME-to-L2ADDRESS MAPPING: " + pName + " to " + pL2Address);
 
 		if (pL2Address != null){
 			if (pL2Address instanceof L2Address){
 				if(!isKnown(pName)) {
 					synchronized (mFoGNamesToL2AddressesMapping) {
 						mFoGNamesToL2AddressesMapping.registerName(pName, (L2Address)pL2Address, NamingLevel.NAMES);
 					}
 				}else{
 					Logging.warn(this, "       ..skipped registration of NAME-to-L2Address because a mapping already exists to the L2Address: " + getL2AddressFor(pName).toString());
 				}
 			}else{
 				Logging.err(this, "Given L2Address has invalid type: " + pL2Address);
 			}
 		}else{
 			Logging.warn(this, "       ..skipped registration of NAME-to-L2Address because the L2Address is invalid");
 		}
 	}
 
 	/**
 	 * Creates a mapping from an HRMID to an L2Address
 	 * 
 	 * @param pHRMID the HRMID 
 	 * @param pL2Address the L2Address
 	 */
 	public void mapHRMID(HRMID pHRMID, L2Address pL2Address)
 	{
 		boolean tDuplicateFound = false;
 		
 		synchronized (mHRMIDToL2AddressMapping) {
 			for (HRMID tHRMID: mHRMIDToL2AddressMapping.keySet()){
 				if (tHRMID.equals(pHRMID)){
 					tDuplicateFound = true;
 					break;
 				}
 			}
 			if (!tDuplicateFound){
 				mHRMIDToL2AddressMapping.put(pHRMID, pL2Address);
 			}else{
 				// HRMID is already known, mapping already exists
 			}
 		}
 	}
 
 	/**
 	 * Removes a mapping for a given HRMID
 	 * 
 	 * @param pHRMID the HRMID for which the mapping should be removed
 	 */
 	public void unmapHRMID(HRMID pHRMID)
 	{
 		synchronized (mHRMIDToL2AddressMapping) {
 			for (HRMID tHRMID: mHRMIDToL2AddressMapping.keySet()){
 				if (tHRMID.equals(pHRMID)){
 					mHRMIDToL2AddressMapping.remove(pHRMID);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the L2 address of this physical node's central FN
 	 * 
 	 * @return the L2 address of this physical node's central FN
 	 */
 	public L2Address getCentralFNL2Address()
 	{
 		return mCentralFNL2Address;
 	}
 	
 	/**
 	 * Determines the L2Address for the given FN
 	 * 
 	 * @param pNode the FN for which the HRM name should be determined
 	 * @return the determined L2Address if it exists, otherwise null is returned 
 	 */
 	public L2Address getL2AddressFor(ForwardingNode pNode)
 	{
 		L2Address tResult = null;
 		
 		synchronized (mFNToL2AddressMapping) {
 			tResult = mFNToL2AddressMapping.get(pNode);			
 		}
 	
 //		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 //			Logging.log(this, "Determined name for " + pNode.toString() + " as " + tResult);
 //		}
 		
 		return tResult;
 	}	
 	@Override
 	public L2Address getNameFor(ForwardingNode pNode)
 	{
 		return getL2AddressFor(pNode);
 	}
 
 	/**
 	 * This function is part of the RoutingService interface. It executes a reverse lookup for a given L2Address 
 	 * and determines the corresponding FN. The function is used within the transfer plane in order to check if 
 	 * a given destination is local or not.
 	 * 
 	 *  @return the determined FN, null if no FN was found 
 	 */
 	@Override
 	public ForwardingNode getLocalElement(Name pSearchedL2Address)
 	{
 		ForwardingNode tResult = null;
 		
 // HINT: The following lines would provide the correct functionality but this would lead to routing problems within TransferPlane::getRoute when a DestinationApplicationProperty requirement is used.
 //       By deactivating this function, we enforce calls to the HRS instance for each "incompleteRoute" event.
 //
 //		
 //		// is the search L2Address valid?
 //		if(pSearchedL2Address != null) {
 //			
 //			// iterate over all known local FEs(FNs)
 //			for(ForwardingElement tFE : mFNToL2AddressMapping.keySet()) {
 //				// get the L2Address of the current FE
 //				L2Address tFoundL2Address = mFNToL2AddressMapping.get(tFE);
 //				
 //				// have we found the searched mapping?
 //				if(pSearchedL2Address.equals(tFoundL2Address)) {
 //					// check if the FE is also an FN
 //					if(tFE instanceof ForwardingNode) {
 //						// we have a valid result
 //						tResult = (ForwardingNode) tFE;
 //						
 //						// return immediately
 //						break;
 //					}else{
 //						Logging.warn(this, "     ..found FE entry isn't derived from an FN, entry=" + tFE);
 //					}
 //				}
 //			}
 //		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines the L2Address for a given FoG name.
 	 * 
 	 * @param pName the FoG name
 	 * @return the L2Addresses
 	 */
 	private NameMappingEntry<L2Address>[] getL2AddressFor(Name pName)
 	{
 		NameMappingEntry<L2Address>[] tResult = null;
 		
 		synchronized (mFoGNamesToL2AddressesMapping) {
 			tResult = mFoGNamesToL2AddressesMapping.getAddresses(pName);			
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines the L2Addresses for a given HRMID
 	 * 
 	 * @param pHRMID the HRMID for which the L2Address has to be determined
 	 * @return the resulting L2Address, returns "null" if no mapping was found
 	 */
 	private L2Address getL2AddressFor(HRMID pHRMID)
 	{
 		L2Address tResult = null;
 		
 		synchronized (mHRMIDToL2AddressMapping) {
 			// iterate over all mappings
 			for (HRMID tHRMID : mHRMIDToL2AddressMapping.keySet()){
 				// compare the values
 				if (tHRMID.equals(pHRMID)){
 					// get the L2 address
 					tResult = mHRMIDToL2AddressMapping.get(tHRMID);
 					// leave the for-loop
 					break;
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Registers a node at the database of this HRS instance
 	 * 
 	 * @param pElement the FN to register at routing service
 	 * @param pName the name for the FN (null, if no name available)
      * @param pLevel the level of abstraction for the naming  
      * @param pDescription the requirements description for a connection to this node
 	 */
 	@Override
 	public void registerNode(ForwardingNode pElement, Name pName, NamingLevel pLevel, Description pDescription)
 	{	
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING NODE: " + pElement + " with name " + pName + (pLevel != NamingLevel.NONE ? " on naming level " + pLevel : "") + " with description " + pDescription);
 		}
 
 		/**
 		 * Determine addresses for "pName"
 		 */
 		NameMappingEntry<L2Address> [] tAddresses = getL2AddressFor(pName);
 
 		// have we found any already existing addresses?
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			if ((tAddresses != null) && (tAddresses.length > 0)){
 				Logging.log(this, "Found " + tAddresses.length + " already registered address for " + pElement);
 				for (int i = 0; i < tAddresses.length; i++){
 					Logging.log(this, "     ..[" + i + "](" + tAddresses[i].getAddress().getClass().getSimpleName() + "): " + tAddresses[i].getAddress().toString());
 				}
 			}
 		}		
 		
 		/**
 		 * Register name mappings 
 		 */
 		synchronized (mFNToL2AddressMapping) {
 			// is there already an L2Address registered for the node?
 			if(!mFNToL2AddressMapping.containsKey(pElement)) {
 				/**
 				 * Generate L2 address for the node
 				 */
 				L2Address tNodeL2Address = L2Address.createL2Address();
 				tNodeL2Address.setDescr(pElement.toString());
 				
 
 				if (pElement.equals(getCentralFN())){
 					Logging.log(this, "     ..registering L2 address for central FN: " + tNodeL2Address);
 					mCentralFNL2Address = tNodeL2Address;
 				}
 				
 				/** 
 				 * Register mapping from FN to L2address
 				 */
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..registering NAME MAPPING for FN \"" + pElement + "\": L2address=\"" + tNodeL2Address + "\", level=" + pLevel);
 				}
 				mFNToL2AddressMapping.put(pElement, tNodeL2Address);
 				/** 
 				 * Register mapping from FoG name to L2address
 				 */
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..registering NAME MAPPING for FoG name \"" + pName + "\": L2address=\"" + tNodeL2Address + "\", level=" + pLevel);
 				}
 				synchronized (mFoGNamesToL2AddressesMapping) {
 					mFoGNamesToL2AddressesMapping.registerName(pName, tNodeL2Address, pLevel);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Unregisters an FN.
 	 * 
 	 * @param pElement the FN which should be unregistered.
 	 * @return true if the FN was removed, otherwise false
 	 */
 	@Override
 	public boolean unregisterNode(ForwardingNode pElement)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "UNREGISTERING NODE: " + pElement);
 		}
 
 		// get the L2Address of this FN
 		L2Address tNodeL2Address = getL2AddressFor(pElement);
 
 		// have we found an L2 address?
 		if (tNodeL2Address != null){
 			/**
 			 * remove the FN from the L2 specific routing graph
 			 */
 			synchronized (mL2RoutingGraph) {
 				if(mL2RoutingGraph.contains(tNodeL2Address)) {
 					mL2RoutingGraph.remove(tNodeL2Address);
 				}
 			}
 		}
 			
 		return true;
 	}
 	
 	/**
 	 * Registers a link in the local L2 specific routing graph
 	 * HINT: This function must not block. Otherwise, the entire FoGSiEm GUI will hang until this functions returns.
 	 * 
 	 *  @param pFrom the FN where the link begins
 	 *  @param pGate the Gate (link) which is to be registered
 	 *  @throws NetworkException
 	 */
 	@Override
 	public void registerLink(ForwardingElement pFrom, AbstractGate pGate) throws NetworkException
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK: source=" + pFrom + " ## dest.=" + pGate.getNextNode() + " ## attr.=" + pGate.getDescription() + " ## gate=" + pGate + " ## peer=" + pGate.getRemoteDestinationName());
 		}
 
 		/**
 		 * Make sure that the starting point of the link is already known to the FN-to-L2address mapping.
 		 * Determine the L2Address of the starting point.
 		 */
 		L2Address tFromL2Address = null;
 		// check the class type of pFrom
 		if (pFrom instanceof ForwardingNode){
 			// get the FN where the route should start
 			ForwardingNode tFromFN = (ForwardingNode)pFrom;
 			
 			// determine the L2address of the starting point
 			tFromL2Address = getL2AddressFor(tFromFN);
 			
 			// is the FN still unknown for the FN-to-L2address mapping?
 			if(tFromL2Address == null) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Source node " + pFrom +" of link " + pGate + " isn't known yet. It will be registered implicitly.");
 				}
 				
 				registerNode(tFromFN, null, NamingLevel.NONE, null);
 				
 				// determine the L2address of the starting point to check if the registration was successful
 				tFromL2Address = getL2AddressFor(tFromFN);
 				if(tFromL2Address == null) {
 					throw new RuntimeException(this + " - FN " + pFrom + " is still unknown to the FN-to-L2address mapping, although it was registered before.");
 				}
 			}
 		}else{
 			// pFrom isn't derived from ForwardingNode
 			throw new RuntimeException(this + " - Source FE " + pFrom + " has the wrong class hierarchy(ForwardingNode is missing).");
 		}
 
 		/**
 		 * Check if the link is one to another physical neighbor node or not.
 		 * Determine the L2Address of the ending point of the link
 		 */
 		L2Address tToL2Address = null;
 		boolean tIsLinkToPhysicalNeigborNode = false;
 		NetworkInterface tInterfaceToNeighbor = null;
 		if( pGate instanceof DirectDownGate){
 			// get the direct down gate
 			DirectDownGate tDirectDownGate = (DirectDownGate)pGate;
 			
 			// get the network interface to the neighbor
 			tInterfaceToNeighbor = tDirectDownGate.getNetworkInterface();
 			
 			// mark as link to another node
 			tIsLinkToPhysicalNeigborNode = true;
 			
 			// determine the L2Address of the destination FN for this gate
 			// HINT: For DirectDownGate gates, this address is defined in "DirectDownGate" by a call to "RoutingService.getL2AddressFor(ILowerLayer.getMultiplexerGate())".
 			//       However, there will occur two calls to registerLink():
 			//				* 1.) the DirectDownGate is created
 			//				* 2.) the peer has answered by a packet of "OpenGateResponse" and the peer name is now known
 			//       Therefore, we ignore the first registerLink() request and wait for the (hopefully) appearing second request.
 			tToL2Address = (L2Address) pGate.getRemoteDestinationName();
 			if (tToL2Address == null){
 				Logging.warn(this, "Peer name wasn't avilable via AbstractGate.getRemoteDestinationName(), will skip this registerLink() request and wait until the peer is known");
 			}
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "      ..external link, which ends at the physical node: " + tToL2Address);
 			}
 		}else{
 			// mark as node-internal link
 			tIsLinkToPhysicalNeigborNode = false;
 
 			// we have any kind of a gate, we determine its ending point
 			ForwardingNode tToFN = (ForwardingNode)pGate.getNextNode();
 			
 			/**
 			 * Make sure that the starting point of the link is already known to the FN-to-L2address mapping.
 		 	*/
 			tToL2Address = getL2AddressFor(tToFN);
 			// is the FN still unknown for the FN-to-L2address mapping?
 			if(tToL2Address == null) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "Destination node " + tToFN +" of link " + pGate + " isn't known yet. It will be registered implicitly.");
 				}
 				
 				registerNode(tToFN, null, NamingLevel.NONE, null);
 				
 				// determine the L2address of the starting point to check if the registration was successful
 				tToL2Address = getL2AddressFor(tToFN);
 				if(tToL2Address == null) {
 					throw new RuntimeException(this + " - Destination FN " + pFrom + " is still unknown to the FN-to-L2address mapping, although it was registered before.");
 				}
 			}
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "      ..internal link, which ends at the local node " + tToL2Address);
 			}
 		}
 
 		if(tToL2Address == null) {
 			// return immediately because the peer name is sill unknown
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "Peer name is still unknown, waiting for the second request (source=" + tFromL2Address + ", gate=" + pGate + ")");
 			}
 			return;
 		}
 		
 		/**
 		 * Add link to L2 specific routing graph
 		 */
 		storeL2Link(tFromL2Address, tToL2Address, new RoutingServiceLink(pGate.getGateID(), null));
 		
 		/**
 		 * DIRECT NEIGHBOR FOUND: create a HRM connection to it
 		 */
 		if(tIsLinkToPhysicalNeigborNode) {
 			L2Address tThisHostL2Address = getL2AddressFor(getCentralFN());
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "      ..NODE " + tThisHostL2Address + " FOUND POSSIBLE DIRECT NEIGHBOR: " + tToL2Address + "?");
 			}
 
 			if((!pFrom.equals(tThisHostL2Address)) && (!tToL2Address.equals(tThisHostL2Address))) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "    ..actually found an interesting link from " + tThisHostL2Address + " to " + tToL2Address + " via FN " + pFrom);
 				}
 				getHRMController().eventDetectedPhysicalNeighborNode(tInterfaceToNeighbor, tToL2Address);
 			}else{
 				Logging.warn(this, "registerLink() ignores the new link to a possible neighbor, from=" + tFromL2Address + "(" + pFrom + ")" + " to " + tToL2Address + " because it is linked to the central FN " + tThisHostL2Address);
 			}
 		}
 	}
 	
 	/**
 	 * Unregisters a link from the local L2 specific routing graph
 	 * HINT: This function must not block. Otherwise, the entire FoGSiEm GUI will hang until this functions returns.
 	 * 
 	 *  @param pFrom the FN where the link begins
 	 *  @param pGate the Gate (link) which is to be unregistered
 	 */  
 	@Override
 	public boolean unregisterLink(ForwardingElement pFrom, AbstractGate pGate)
 	{
 		//if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "UNREGISTERING LINK from " + pFrom + " to " + pGate.getNextNode() + ", gate " + pGate);
 		//}
 
 		/**
 		 * Check if the link is one to another physical neighbor node or not.
 		 * Determine the L2Address of the ending point of the link
 		 */
 		L2Address tToL2Address = null;
 		boolean tIsLinkToPhysicalNeigborNode = false;
 		NetworkInterface tInterfaceToNeighbor = null;
 		if( pGate instanceof DirectDownGate){
 			// get the direct down gate
 			DirectDownGate tDirectDownGate = (DirectDownGate)pGate;
 			
 			// get the network interface to the neighbor
 			tInterfaceToNeighbor = tDirectDownGate.getNetworkInterface();
 
 			// mark as link to another node
 			tIsLinkToPhysicalNeigborNode = true;
 			
 			// determine the L2Address of the destination FN for this gate
 			// HINT: For DirectDownGate gates, this address is defined in "DirectDownGate" by a call to "RoutingService.getL2AddressFor(ILowerLayer.getMultiplexerGate())".
 			//       However, there will occur two calls to registerLink():
 			//				* 1.) the DirectDownGate is created
 			//				* 2.) the peer has answered by a packet of "OpenGateResponse" and the peer name is now known
 			//       Therefore, we ignore the first registerLink() request and wait for the (hopefully) appearing second request.
 			tToL2Address = (L2Address) pGate.getRemoteDestinationName();
 			if (tToL2Address == null){
 				Logging.warn(this, "Peer name wasn't avilable via AbstractGate.getRemoteDestinationName(), will skip this unregisterLink() request and wait until the peer is known");
 			}
 		}else{
 			// mark as node-internal link
 			tIsLinkToPhysicalNeigborNode = false;
 		}
 			
 		/**
 		 * Determine the L2Address of the starting point of the link
 		 */
 		L2Address tFromL2Address = mFNToL2AddressMapping.get(pFrom);
 
 		/**
 		 * Remove the link from the L2 specific routing graph
 		 */
 		synchronized (mL2RoutingGraph) {
 			// determine which links are outgoing from the FN "pFrom"
 			Collection<RoutingServiceLink> tOutgoingLinksFromFN = mL2RoutingGraph.getOutEdges(tFromL2Address);
 			// have we found at least one outgoing link?
 			if(tOutgoingLinksFromFN != null) {
 				for(RoutingServiceLink tOutgoingLink : tOutgoingLinksFromFN) {
 					// have we found the right outgoing link? (we check the GateIDs)
 					if(tOutgoingLink.equals(pGate)) {
 						// remove the link from the L2 specific routing graph
 						mL2RoutingGraph.unlink(tOutgoingLink);
 						break;
 					}
 				}
 			}
 		}
 		
 		/**
 		 * DIRECT NEIGHBOR FOUND: create a HRM connection to it
 		 */
 		if(tIsLinkToPhysicalNeigborNode) {
 			L2Address tThisHostL2Address = getL2AddressFor(getCentralFN());
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "      ..NODE " + tThisHostL2Address + " FOUND POSSIBLE DIRECT NEIGHBOR: " + tToL2Address + "?");
 			}
 
 			if((!pFrom.equals(tThisHostL2Address)) && (!tToL2Address.equals(tThisHostL2Address))) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "    ..actually found an interesting lost link from " + tThisHostL2Address + " to " + tToL2Address + " via FN " + pFrom);
 				}
 				getHRMController().eventLostPhysicalNeighborNode(tInterfaceToNeighbor, tToL2Address);
 			}else{
 				Logging.warn(this, "unregisterLink() ignores the lost link to the neighbor, from=" + tFromL2Address + "(" + pFrom + ")" + " to " + tToL2Address + " because it is linked to the central FN " + tThisHostL2Address);
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Updates the capabilities of existing forwarding nodes.
 	 * (However, this function is only used to update the capabilities of the central FN instead of all FNs. 
 	 * Because either the central FN is able to provide a special function or no FN on this physical node is able to do this.)
 	 * 
 	 * @param pElement the FN for which the capabilities have to be updated
 	 * @param pCapabilities the new capabilities for the FN
 	 */
 	@Override
 	public void updateNode(ForwardingNode pElement, Description pCapabilities)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "UPDATING NODE " + pElement + ": old caps.=" + mNode.getCapabilities() + ", new caps.=" + pCapabilities);
 		}
 
 		// TODO: what about functional requirements and function placing?
 	}
 	
 	/**
 	 * Returns a reference to the used name mapping service
 	 * 
 	 * @return the reference to the name mapping service
 	 */
 	@Override
 	public NameMappingService<L2Address> getNameMappingService()
 	{
 		return mFoGNamesToL2AddressesMapping;
 	}
 
 	/**
 	 * Checks if a given FoG FN name is known to this HRS instance
 	 * 
 	 * @return true if the FoG name is known, otherwise false
 	 */
 	@Override
 	public boolean isKnown(Name pName)
 	{
 		boolean tResult = false;
 		
 		// check if the FoG name is stored in the FoG-to-L2Addresses mapping
 		synchronized (mFoGNamesToL2AddressesMapping) {
 			tResult = (mFoGNamesToL2AddressesMapping.getAddresses(pName) != null);			
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Unregisters a FoG name for a given FN
 	 * 
 	 * @return true if the operation was successful, otherwise false is returned
 	 */
 	@Override
 	public boolean unregisterName(ForwardingNode pFN, Name pName)
 	{
 		boolean tResult = false;
 		
 		// determine the L2Address 
 		L2Address tFNL2Address = getL2AddressFor(pFN);
 		
 		// unregister mapping from FoG name to the determined L2address
 		synchronized (mFoGNamesToL2AddressesMapping) {
 			tResult = mFoGNamesToL2AddressesMapping.unregisterName(pName, tFNL2Address);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines a list of Gate IDs from an L2 address to another L2 address based on the L2 specific routing graph.
 	 * 
 	 * @param pFromL2Address the starting point of the desired route
 	 * @param pToL2Address the ending point of the desired route
 	 * @return a list of Gate IDs to the neighbor node, returns "null" if no route was found
 	 */
 	private List<RoutingServiceLink> getL2GateIDsForRoute(L2Address pFromL2Address, L2Address pToL2Address)
 	{
 		List<RoutingServiceLink> tResult = null;
 
 		synchronized (mL2RoutingGraph) {
 			// query route in the L2 specific routing graph
 			tResult = getRouteFromGraph(mL2RoutingGraph, pFromL2Address, pToL2Address);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines a route from an L2 address to another one, based on the L2 specific routing graph.
 	 * 
 	 * @param pFromL2Address the L2 address of the starting point
 	 * @param pToL2Address the L2 address of the ending point
 	 * @return the found route, returns null if no route was available
 	 */
 	private Route getL2Route(L2Address pFromL2Address, L2Address pToL2Address)
 	{
 		Route tResultRoute = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET ROUTE from \"" + pFromL2Address + "\" to \"" + pToL2Address +"\"");
 		}
 		
 		// is the source equal to the destination? 
 		if (!pFromL2Address.equals(pToL2Address)){
 			// get a list of Gate IDs to the destination
 			List<RoutingServiceLink> tGateIDsToDestination = getL2GateIDsForRoute(pFromL2Address, pToL2Address);
 	
 			// gate ID list is empty?
 			if((tGateIDsToDestination != null) && (!tGateIDsToDestination.isEmpty())) {
 				// create a route segment which can store a list of Gate IDs
 				RouteSegmentPath tRouteSegmentPath = new RouteSegmentPath();
 				// iterate over all gate IDs in the list
 				for(RoutingServiceLink tLink : tGateIDsToDestination) {
 					if (tLink instanceof L2LogicalLink){
 						L2LogicalLink tLogicalLink = (L2LogicalLink)tLink;
 						
 						// store the route as immediate result
 						return tLogicalLink.getRoute().clone();
 					}else{
 						// store the Gate ID in the route segment
 						tRouteSegmentPath.add(tLink.getID());
 					}
 				}
 				
 				// create new route
 				tResultRoute = new Route();
 				
 				// add the list of Gate IDs to the resulting route
 				tResultRoute.add(tRouteSegmentPath);
 			}
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "getL2Route() delivers an empty route because source and destination are equal: " + pFromL2Address);
 			}
 			
 			// create a new empty route because we have already reached the destination
 			tResultRoute = new Route();
 		}
 
 		return tResultRoute;
 	}
 	
 	/**
 	 * Determines a general route from this node's central FN to a destination with special requirements
 	 * 
 	 * @param pDestination
 	 * @param pRequirements
 	 * @param pRequester
 	 * @return the determined route
 	 * @throws RoutingException
 	 * @throws RequirementsException
 	 */
 	public Route getRoute(Name pDestination, Description pRequirements, Identity pRequester) throws RoutingException, RequirementsException
 	{
 		return getRoute(getCentralFN(), pDestination, pRequirements, pRequester);
 	}
 	
 	/**
 	 * Determines a general route from a source to a destination with special requirements
 	 * 
 	 * @param pSource the FN where the route should start
 	 * @param pDestination the FoG name of the ending point of the route
 	 * @param pRequirements the route requirements 
 	 * @param pRequester the getRoute() caller
 	 * @return the determined route
 	 */
 	@SuppressWarnings("unused")
 	@Override
 	public Route getRoute(ForwardingNode pSource, Name pDestination, Description pRequirements, Identity pRequester) throws RoutingException, RequirementsException
 	{		
 		Route tResultRoute = null;
 		L2Address tDestinationL2Address = null;
 		L2Address tSourceL2Address = null;
 
 		/**
 		 * Check parameters
 		 */
 		// check source parameter
 		if(pSource == null){
 			throw new RoutingException("Invalid source parameter.");
 		}
 		// check destination parameter
 		if(pDestination == null){
 			throw new RoutingException("Invalid destination parameter.");
 		}
 		// avoid additional checks
 		if(pRequirements == null) {
 			pRequirements = new Description();
 		}
 
 		/**
 		 * Debug output about process start
 		 */
 		// debug output
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			if(pRequirements == null) {
 				Logging.log(this, "GET ROUTE from \"" + pSource + "\" to \"" + pDestination +"\"");
 			} else {
 				Logging.log(this, "GET ROUTE from \"" + pSource + "\" to \"" + pDestination + "\" with requirements \"" + pRequirements.toString() + "\"");
 			}
 		}
 
 		/**
 		 * Count the route request
 		 */
 		// count call
 		//TODO: mCounterGetRoute.write(+1.0, mTimeBase.nowStream());
 
 		/************************************************************************
 		 * HRM based routing to an HRMID
 		 ************************************************************************/
 		if (pDestination instanceof HRMID){
 			HRMID tDestHRMID = (HRMID)pDestination;
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "      ..HRM based routing to " + tDestHRMID);
 			}
 			
 			/*************************************
 			 * CHECK NEIGHBORHOD
 			 * Check if the destination is a direct neighbor
 			 ************************************/
 			boolean tDestinationIsDirectNeighbor = false;
 			synchronized (mDirectNeighborAddresses) {
 				for (HRMID tHRMID : mDirectNeighborAddresses){
 					if (tHRMID.equals(tDestHRMID)){
 						if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 							Logging.log(this, "      .." + tDestHRMID + " is an address of a direct neighbor node");
 						}
 						tDestinationIsDirectNeighbor = true;
 						break;
 					}
 				}
 			}
 
 			/*************************************
 			 * FIND NEXT HRMID
 			 * Determine the next hop of the desired route
 			 *************************************/
 			HRMID tNextHopHRMID = null;
 			HRMID tThisHop = null;
 			boolean tIsLocalHRMID = false;
 			synchronized (mRoutingTable) { //TODO: separate class for RoutingTable object
 				/**
 				 * Iterate over all routing entries and search for a correct destination cluster
 				 */
 				int tBestDiffLevel = 99;//TODO: use fixed constant here
 				if(mRoutingTable.size() > 0){
 					RoutingEntry tLoopEntry = null;
 					for(RoutingEntry tEntry : mRoutingTable){
 						/**
 						 * Check if the destination belongs to the cluster of this entry
 						 */ 
 						if(tDestHRMID.isCluster(tEntry.getDest())){
 							if(!tEntry.isLocalLoop()){
 								int tCurDiffLevel = tDestHRMID.getPrefixDifference(tEntry.getDest());
 		
 								if(tCurDiffLevel < tBestDiffLevel){
 									// use the next hop from this entry
 									tNextHopHRMID = tEntry.getNextHop();
 									
 									// use the source from this entry
 									tThisHop = tEntry.getSource();
 									
 									if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 										Logging.log(this, "      ..found better matching entry: " + tEntry);
 									}
 								}else{
 									if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 										Logging.log(this, "      ..found uninteresting entry: " + tEntry);
 									}
 								}
 							}else{
 								// we found a local loop for this HRMID -> this HRMID is a local one
 								tLoopEntry = tEntry;
 							}
 						}else{
 							if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 								Logging.log(this, "      ..ignoring entry: " + tEntry);
 							}
 						}
 					}
 					if(tNextHopHRMID == null){
 						if(tLoopEntry != null){
 							if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 								Logging.log(this, "      ..found matching loop entry: " + tLoopEntry);
 							}
 							
 							//HINT: we don't use the next hop from the loop entry because we have already reached the destination node -> we try to contact the destination application later
 							
 							// use the source from this entry
 							tThisHop = tLoopEntry.getSource();
 
 							/**
 							 * Record the HRM based route
 							 */
 							recordHRMRoute(pRequirements, tDestHRMID, tDestHRMID);	
 
 							// mark HRMID as local
 							tIsLocalHRMID = true; 
 						}
 					}
 				}else{
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "      ..found empty routing table");
 					}
 				}
 			}
 			
 			if(tNextHopHRMID != null){
 				/**
 				 * Record the HRM based route
 				 */
 				recordHRMRoute(pRequirements, tThisHop, tNextHopHRMID);	
 				
 				/**
 				 * Show debug output
 				 */
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "      ..NEXT HOP: " + tNextHopHRMID);
 					if(tThisHop != null){
 						Logging.log(this, "      ..VIA: " + tThisHop);
 					}
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.trace(this, "      ..haven't found next hop (HRMID) for destination: " + tDestHRMID);
 				}
 			}
 
 			
 			/*************************************
 			 * FIND NEXT L2 ADDRESS
 			 * Determine the L2 specific gates towards the neighbor node
 			 *************************************/
 			L2Address tNextHopL2Address = null;
 			if (tNextHopHRMID != null){
 				tNextHopL2Address = getL2AddressFor(tNextHopHRMID);
 			}else{
 				if(!tIsLocalHRMID){
 					Logging.log(this, "getRoute() wasn't able to determine the HRMID of the next hop in order to route towards " + tDestHRMID.toString());
 				}
 			}
 
 			/*************************************
 			 * GET ROUTE to next L2 address
 			 *************************************/
 			if (tNextHopL2Address != null){
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "      ..NEXT HOP(L2ADDRESS): " + tNextHopL2Address);
 				}
 				if (tNextHopL2Address != null){
 					// get a route to the neighbor
 					tResultRoute = getL2Route(getCentralFNL2Address(), tNextHopL2Address);
 				}
 				
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "      ..ROUTE TO NEXT HOP: " + tResultRoute);
 				}
 
 				if (tResultRoute != null){
 					/**
 					 * RE-ENCODE the destination HRMID
 					 */
 					// do we route to a direct neighbor?
 					if(!tDestinationIsDirectNeighbor){
 						// do we already reached the destination?
 						if(!tResultRoute.isEmpty()){
 							tResultRoute.addLast(new RouteSegmentAddress(tDestHRMID));
 						}
 					}
 
 					/**
 					 * ENCODE the original requirements
 					 */
 					// do we already reached the destination?
 					if(!tResultRoute.isEmpty()){
 						tResultRoute.addLast(new RouteSegmentDescription(pRequirements));
 					}
 				}
 			}else{
 				/**
 				 * ERROR MESSAGE
 				 */
 				if((tNextHopHRMID != null) && (!tIsLocalHRMID)){
 					Logging.err(this, "getRoute() wasn't able to determine the L2 address of the next hop " + tNextHopHRMID + " in the following mapping: ");
 
 					synchronized (mHRMIDToL2AddressMapping) {
 						if(mHRMIDToL2AddressMapping.size() > 0){
 							for (HRMID tHRMID : mHRMIDToL2AddressMapping.keySet()){
 								Logging.log(this, "       ..mapping " + tHRMID + " to " + mHRMIDToL2AddressMapping.get(tHRMID));
 							}
 						}else{
 							Logging.log(this, "   ..no mapping entries found");
 						}
 					}
 				}
 			}
 
 			/***************************************************************************
 			 * IF ENCODE the destination application as destination if possible,
 			 *            use the following L2 based routing
 			 ***************************************************************************/
 			if(tNextHopHRMID == null){
 				if(tIsLocalHRMID){
 					/**
 					 * Check if a destination application is encoded in the requirements and use it for routing
 					 */
 					pDestination = getDestinationApp(pRequirements);
 					if(pDestination == null){
 						Logging.err(this, "getRoute() hasn't found the destination application for a route to " + pDestination + " with requirements: " + pRequirements);
 					}
 				}else{
 					Logging.err(this, "getRoute() wasn't able to determine an HRM based route to " + pDestination + " with requirements: " + pRequirements);
 				}
 			}else{
 				/**
 				 * show result
 				 */
 				if(tResultRoute != null){
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "      ..RESULT(getRoute() to HRMID): " + tResultRoute);
 					}
 				}
 			}
 		}
 
 		/***********************************************************************
 		 * L2 based routing to a FoG name
 		 ***********************************************************************/
 		if(tResultRoute == null){
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "      ..L2 based routing to " + pDestination);
 			}
 			
 			/**
 			 * Determine the L2 address of the destination
 			 */
 			//L2Address tDestinationL2Address = null;
 			if (pDestination instanceof L2Address){
 				// cast directly
 				tDestinationL2Address = (L2Address)pDestination;
 			}else{
 				// search in the FoGName-to-L2Address mapping
 				NameMappingEntry<L2Address>[] tDestinationL2Addresses = getL2AddressFor(pDestination);
 				// have we found valid L2 address(es)?
 				if ((tDestinationL2Addresses != null) && (tDestinationL2Addresses.length > 0)){
 					// use the last, and therefore most up-to-date, found L2 address
 					tDestinationL2Address = tDestinationL2Addresses[tDestinationL2Addresses.length - 1].getAddress();
 				}
 
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "      ..found destination L2Address: " + tDestinationL2Address);
 				}
 			}
 
 			// have we found the L2 address of the destination?
 			if (tDestinationL2Address != null){
 				/**
 				 * Determine the L2 address of the source
 				 */
 				//L2Address tSourceL2Address = null;
 				if (pSource instanceof L2Address){
 					// cast directly
 					tSourceL2Address = (L2Address)pSource;
 				}else{
 					// search in the FN-to-L2Address mapping
 					tSourceL2Address = getL2AddressFor(pSource);
 				}
 				// check if the L2 address of the source is valid
 				if (tSourceL2Address != null){
 					/**
 					 * Get a route from the source to the destination based on their L2 addresses
 					 */
 					tResultRoute = getL2Route(tSourceL2Address, tDestinationL2Address);
 		
 					if (tResultRoute != null){
 						encodeDestinationApplication(tResultRoute, pRequirements);
 					}else{
 						// no route found
 						Logging.log(this, "Couldn't determine a route from " + tSourceL2Address + " to " + tDestinationL2Address + ", knowing the following routing graph nodes");
 						// list known nodes
 						synchronized (mL2RoutingGraph) {
 							Collection<L2Address> tGraphNodes = mL2RoutingGraph.getVertices();
 							int i = 0;
 							for (L2Address tL2Address : tGraphNodes){
 								Logging.log(this, "     ..[" + i + "]: " + tL2Address);
 								i++;
 							}
 						}
 					}
 					
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "      ..RESULT(getRoute() to " + pDestination + "): " + tResultRoute);
 					}
 				}else{
 					Logging.err(this, "getRoute() wasn't able to determine the L2 address of the source " + pSource);
 				}
 			}else{
 				Logging.err(this, "getRoute() wasn't able to determine the L2 address of the destination " + pDestination);
 			}
 		}
 
 		/**
 		 * CATCH ROUTING ERROR
 		 */
 		if(tResultRoute == null){
 			// no route found
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.err(this, "getRoute() couldn't determine a route from " + pSource + " to " + pDestination + ", knowing the following routing graph");
 				
 				// list known topology
 				synchronized (mL2RoutingGraph) {
 					Collection<L2Address> tGraphNodes = mL2RoutingGraph.getVertices();
 					int i = 0;
 					for (L2Address tL2Address : tGraphNodes){
 						Logging.err(this, "     ..node[" + i + "]: " + tL2Address);
 						i++;
 					}
 					Collection<RoutingServiceLink> tGraphLinks = mL2RoutingGraph.getEdges();
 					i = 0;
 					for (RoutingServiceLink tLink : tGraphLinks){
 						Logging.err(this, "     ..gate[" + i + "]: " + tLink.getID());
 						i++;
 					}
 				}
				//throw new RuntimeException(this + "::getRoute() failed");
 			}
 		}
 		
 		// return immediately
 		return tResultRoute;
 	}
 	
 	/**
 	 * Records the current HOP within an possibly existing ProbeRoutingProperty property.
 	 * 
 	 * @param pRequirements the requirements of the getRoute() request
 	 * @param pHopHRMID the HRMID of the hop, which should be recorded
 	 */
 	private void recordHRMRoute(Description pRequirements, HRMID pThisHop, HRMID pNextHopHRMID)
 	{
 		if (HRMConfig.Routing.RECORD_ROUTE_FOR_PROBES){
 			// check if we have valid requirements
 			if (pRequirements != null){
 				// search for the ProbeRoutingProperty property
 				ProbeRoutingProperty tPropProbeRouting = (ProbeRoutingProperty) pRequirements.get(ProbeRoutingProperty.class);
 				if(tPropProbeRouting != null) {
 					HRMID tLastHopHRMID = tPropProbeRouting.getLastHop();
 					
 					/**
 					 * Check if last and current hop have the same HRMID
 					 */
 					boolean tDuplicate = false;
 					if ((tLastHopHRMID != null) && (tLastHopHRMID.equals(pNextHopHRMID))){
 						tDuplicate = true;
 					}
 					
 					/**
 					 * Store the HRMID of this node
 					 */
 					if(pThisHop != null){
 						tPropProbeRouting.addHop(pThisHop);
 					}
 					
 					/**
 					 * Store the HRMID of the next hop
 					 */
 					if (!tDuplicate){
 						// don't store the same entry two times
 						if(!pNextHopHRMID.equals(pThisHop)){
 							// store the HRMID
 							tPropProbeRouting.addHop(pNextHopHRMID);
 						}
 					}else{
 						// we have the same hop like last time
 					}
 				}else{
 					Logging.warn(this, "Cannot record HRM Route (next=" + pNextHopHRMID + " via " + pThisHop + ") because the needed property wasn't found within the requirements");
 				}
 			}else{
 				Logging.warn(this, "Cannot record HRM Route (next=" + pNextHopHRMID + " via " + pThisHop + ") because no requirements were found");
 			}
 		}
 	}
 
 	/**
 	 * Determines the destination application from a requirements description
 	 * 
 	 * @param pRequirements the requirements
 	 * 
 	 * @return the app. name
 	 */
 	private Name getDestinationApp(Description pRequirements)
 	{
 		Name tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "getDestinationApp() is deriving the destination app from: " + pRequirements);
 		}
 		
 		/**
 		 * Check if a destination application is encoded in the requirements
 		 */
 		DestinationApplicationProperty tPropDestApp = (DestinationApplicationProperty) pRequirements.get(DestinationApplicationProperty.class);
 		if(tPropDestApp != null) {
 			Logging.log(this, "    ..found destination application property: " + tPropDestApp);
 			
 			// remove the found property from the requirements
 			pRequirements.remove(tPropDestApp);
 		
 			/**
 			 * Add the destination application as FoG name to the route
 			 */
 			if(tPropDestApp.getAppName() != null) {
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "    ..encoding as getRoute() destination the destination  appl. name: " + tPropDestApp.getAppName());
 				}
 				tResult = tPropDestApp.getAppName();
 			} else {
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "    ..encoding as getRoute() destination the destination  appl. name space: " + tPropDestApp.getAppNamespace());
 				}
 				tResult = new SimpleName(tPropDestApp.getAppNamespace());
 			}
 			Logging.log(this, "    ..found destination app.: " + tResult);
 		}else{
 			if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 				Logging.log(this, "getDestinationApp() wasn't able to derive the destination app. from: " + pRequirements);
 			}
 		}
 		
 		return tResult;
 	}
 
 
 	/**
 	 * Encodes the destination application in the given route. The destination application is 
 	 * derived from the data of the given requirements.
 	 * This function should be used if the destination node was reached.
 	 * 
 	 * @param pRoute the current route
 	 * @param pRequirements the requirements, which can include a description for the destination application
 	 */
 	private void encodeDestinationApplication(Route pRoute, Description pRequirements)
 	{
 		// do we have requirements?
 		if(pRequirements != null) {
 			/**
 			 * Check if a destination application is encoded in the requirements
 			 */
 			DestinationApplicationProperty tPropDestApp = (DestinationApplicationProperty) pRequirements.get(DestinationApplicationProperty.class);
 			if(tPropDestApp != null) {
 				Logging.log(this, "    ..found destination application property: " + tPropDestApp);
 				
 				// remove the found property from the requirements
 				pRequirements.remove(tPropDestApp);
 			
 				/**
 				 * Add the destination application to the route
 				 */
 				if(tPropDestApp.getAppName() != null) {
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "    ..encoding in route the destination  appl. name: " + tPropDestApp.getAppName());
 					}
 					pRoute.add(new RouteSegmentAddress(tPropDestApp.getAppName()));
 				} else {
 					if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 						Logging.log(this, "    ..encoding in route the destination  appl. name space: " + tPropDestApp.getAppNamespace());
 					}
 					pRoute.add(new RouteSegmentAddress(new SimpleName(tPropDestApp.getAppNamespace())));
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 					Logging.log(this, "    ..no destination application property found");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determines all links which are outgoing from a defined FN
 		
 	 * @param pFNName the name of the FN from which the outgoing links should be enumerated
 	 * @return the list of outgoing links
 	 */
 	public Collection<RoutingServiceLink> getOutgoingLinks(L2Address pFNName)
 	{
 		Collection<RoutingServiceLink> tResult = null;
 
 		synchronized (mL2RoutingGraph) {
 			tResult = mL2RoutingGraph.getOutEdges(pFNName);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * This method is derived from RoutingService
 	 * 
 	 * @param pElement the element for which an error is reported
 	 */
 	@Override
 	public void reportError(Name pElement)
 	{
 		Logging.warn(this, "############ Transfer plane reported an error for " + pElement + " #############");
 		//TODO: remove the element from the routing graph
 	}
 
 	/**
 	 * Returns the local L2Address based routing graph (for GUI only)
 	 * 
 	 * @return the routing graph
 	 */
 	public RoutableGraph<L2Address, RoutingServiceLink> getL2RGForGraphViewer()
 	{
 		return mL2RoutingGraph;
 	}
 
 	/**
 	 * Returns the central FN of this node
 	 *  
 	 * @return the central FN
 	 */
 	public Multiplexer getCentralFN()
 	{
 		Multiplexer tResult = null;
 		
 		// get the recursive FoG layer
 		FoGEntity tFoGLayer = (FoGEntity) mNode.getLayer(FoGEntity.class);
 		
 		// get the central FN of this node
 		tResult = tFoGLayer.getCentralFN();
 		
 		return tResult;
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	@Override
 	public int getNumberVertices()
 	{
 		return 0;
 	}
 
 	@Override
 	public int getNumberEdges()
 	{
 		return 0;
 	}
 
 	@Override
 	public int getSize()
 	{
 		return 0;
 	}
 
 	@Override
 	public LinkedList<Name> getIntermediateFNs(ForwardingNode pSource,	Route pRoute, boolean pOnlyDestination)
 	{
 		return null;
 	}
 
 	
 
 	/**
 	 * Returns the namespace which is handled by our HRM routing service.
 	 * This function is inherited from RoutingService. 
 	 */
 	@Override
 	public Namespace getNamespace()
 	{
 		return HRMName.NAMESPACE_HRM;
 	}
 
 	/**
 	 * Returns a descriptive string for this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return toLocation();
 	}
 
 	/**
 	 * Returns a string describing the location of this instance
 	 * 
 	 * @return the descriptive string
 	 */
 	@Override
 	public String toLocation()
 	{
 		String tResult = getClass().getSimpleName() + (mNode != null ? "@" + mNode.toString() : "");
 		
 		return tResult;
 	}
 }
