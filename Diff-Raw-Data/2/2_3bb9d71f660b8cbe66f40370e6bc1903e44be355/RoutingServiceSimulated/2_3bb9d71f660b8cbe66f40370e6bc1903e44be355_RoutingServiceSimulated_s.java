 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.simulated;
 
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.facade.properties.IgnoreDestinationProperty;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentAddress;
 import de.tuilmenau.ics.fog.routing.RouteSegmentDescription;
 import de.tuilmenau.ics.fog.routing.RouteSegmentMissingPart;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.GateContainer;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 /**
  * Routing service instance local to a host.
  * 
  * The local information are stored locally. Furthermore, they are duplicated
  * and reported to the next higher level routing service instance.
  */
 public class RoutingServiceSimulated implements RoutingService
 {
 	/**
 	 * Creates a local routing service entity.
 	 * 
 	 * @param pRS Reference to next higher layer routing service entity
 	 * @param pNameMapping Reference to name resolution
 	 */
 	public RoutingServiceSimulated(RemoteRoutingService pRoutingService, String pName, Node pNode)
 	{
 		if(Config.Routing.ENABLE_NODE_RS_HIERARCHY_LEVEL) {
 			mRS = new PartialRoutingService(pNode.getTimeBase(), pNode.getLogger(), "RS@" +pName +"@" +pNode.getAS(), pRoutingService);
 		} else {
 			mRS = pRoutingService;
 		}
 		
 		mRoutingIDs = new HashMap<ForwardingNode, RoutingServiceAddress>();
 		mNameMapping = HierarchicalNameMappingService.getGlobalNameMappingService();
 		mLogger = pNode.getLogger();
 	}
 	
 	@Override
 	public Route getRoute(ForwardingNode pSource, Name pDestination, Description pRequirements, Identity pRequester) throws RoutingException, RequirementsException
 	{
 		if((pRequirements == null) || pRequirements.isBestEffort()) {
 			mLogger.log(this, "Searching for a route from \"" + pSource + "\" to \"" + pDestination +"\"");
 		} else {
 			mLogger.log(this, "Searching for a route from \"" + pSource + "\" to \"" + pDestination + "\" with requirements \"" + pRequirements + "\"");
 		}
 
 		//
 		// Get addresses for source and destination
 		//
 		RoutingServiceAddress tFrom = getNameFor(pSource);
 		if(tFrom == null) {
 			throw new RoutingException("Can not resolve address for forwarding node '" +pSource +"'.");
 		}
 		
 		RoutingServiceAddress tTo;
 		if(pDestination instanceof RoutingServiceAddress) {
 			tTo = (RoutingServiceAddress) pDestination;
 		} else {
 			tTo = getAddress(pDestination, pRequirements);
 		}
 		
 		if(tTo == null) {
 			throw new RoutingException("Can not resolve address for name '" +pDestination +"'.");
 		}
 		
 		Route tRes;
 		try {
 			tRes = mRS.getRoute(tFrom, tTo, pRequirements, pRequester);
 		} catch(RemoteException tExc) {
 			throw new RoutingException("Remote routing service not available.", tExc);
 		}
 		
 		// check if routing service would like to have a function
 		// if so, we have to create it first and are not allowed to
 		// create a gate for the non-functional stuff
 		boolean tRoutingServerRequestFunction = false;
 		if(tRes.size() > 0) {
 			if(tRes.getFirst() instanceof RouteSegmentMissingPart) {
 				tRoutingServerRequestFunction = true;
 			}
 		}
 		
 		// is there an non-empty route?
 		if(!tRes.isEmpty()) {
 			//
 			// look for non-functional requirements within request and request
 			// creation of missing gates satisfying the non-functional properties.
 			//
 			// HINT: for every connection dedicated QoS enabled gates are created
 			//
 			if ((pRequirements != null) && !tRoutingServerRequestFunction) {
 				Description tNonFunDesc = pRequirements.getNonFunctional();
 				
 				// found QoS requirements within route request?
 				// => then remove calculated BE based route and inform only about missing gates!
 				if(!tNonFunDesc.isBestEffort() && (pSource instanceof GateContainer)) {
 					GateContainer tContainer = (GateContainer)pSource;
 					AbstractGate tParallelGate = tContainer.getGate(tRes.getFirst(false));
 	
 					// Do we have a suitable next gate?
 					// If not, we have to iterate until we resolved all names and get a gate.
 					while(tParallelGate == null) {
 						if(!tRes.isEmpty() && tRes.getFirst() instanceof RouteSegmentAddress) {
 							RouteSegmentAddress nextSegment = (RouteSegmentAddress) tRes.removeFirst();
 							
 							// recursive call without QoS (which will avoid a second recursive call)
 							Route partRoute = getRoute(pSource, nextSegment.getAddress(), null, pRequester);
 							if(!partRoute.isEmpty()) {
 								tRes = partRoute;
 							}
 							// else: Intermediate goal reached -> use remaining part of previous tRes
 							
 							tParallelGate = tContainer.getGate(tRes.getFirst(false));
 						} else {
 							// no name anymore; no further route
 							break;
 						}
 					}
 						
 					if(tParallelGate != null) {
 						mLogger.log(this, "Route contains non-functional requirements and we have to create a parallel gate for " +tParallelGate);
 	
 						Description tCapabilities = tParallelGate.getDescription();
 						Description tGateRequ;
 						Description tRemainingRequ = tNonFunDesc;
 						
 						if(tCapabilities != null) {
 							try {
 								tGateRequ = tCapabilities.deriveRequirements(tNonFunDesc);
 								
 								tRemainingRequ = tNonFunDesc.removeCapabilities(tGateRequ);
 							}
 							catch(PropertyException tExc) {
 								throw new RoutingException(this, "Requirements " +tNonFunDesc +" can not be fullfilled.", tExc);
 							}
 						} else {
 							tGateRequ = null;
 						}
 						/*
 						 *  return route in form of:
 						 *  	1. new gate
 						 *  	2. destination address
 						 *  	3. requirements (all)
 						 */
 						RouteSegmentMissingPart newGate = new RouteSegmentMissingPart(tGateRequ, tParallelGate, pRequester);
 						tRes = new Route();
 						tRes.addFirst(newGate);
 						tRes.addLast(new RouteSegmentAddress(pDestination));
 						tRes.addLast(new RouteSegmentDescription(tRemainingRequ));
 					} else {
 						throw new RoutingException("Routing delivers path with wrong gate IDs. Can not define missing parts for gate " +tRes.getFirst(false) +" of " +tFrom +".");
 					}
 				}
 			}
 		}
 		
 		return tRes;
 	}
 	
 	@Override
 	public LinkedList<Name> getIntermediateFNs(ForwardingNode pSource, Route pRoute, boolean pOnlyDestination)
 	{
 		LinkedList<Name> tRes = null;
 		RoutingServiceAddress tFrom = getNameFor(pSource);
 
 		if(tFrom != null) {
 			if(pOnlyDestination) {
 				// Destination only
 				try {
 					Name tDest = mRS.getAddressFromRoute(tFrom, pRoute);
 					
 					if(tDest != null) {
 						tRes = new LinkedList<Name>();
 						tRes.add(tDest);
 					}
 				} catch (RemoteException tExc) {
 					mLogger.warn(this, "Can not determine destination of route " +pRoute +" starting at " +pSource, tExc);
 				}
 			} else {
 				// Complete route
 				try {
 					tRes = getHops(tFrom, pRoute);
 					LinkedList<Name> tRes2 = getHopsLocal(pSource, pRoute);
 					if(tRes2.size() > tRes.size()) tRes = tRes2;
 				} catch (Exception tExc) {
 					mLogger.warn(this, "Can not determine intermediate FNs of route " +pRoute +" starting at " +pSource, tExc);
 				}
 			}
 		} else {
 			mLogger.warn(this, "Startpoint " +pSource +" not known to routing service. Can not calculate intermediate FNs.");
 		}
 		
 		if(tRes == null) {
 			// return empty list on error
 			return new LinkedList<Name>();
 		} else {		
 			return tRes;
 		}
 	}
 	
 	private LinkedList<Name> getHops(RoutingServiceAddress pSource, Route pRoute) throws RemoteException, NetworkException
 	{
 		LinkedList<Name> tHops = new LinkedList<Name>();
 		Route tPartRoute = (Route) pRoute.clone();
 		RouteSegmentPath tRouteSegment;
 		boolean tIgnoreNull = true;
 		
 		try {
 			// Create a list of intermediate hops without holes and without null pointers.
 			// Process the route from the end to the start. Start with the destination
 			// and continue with the hops previous to it.
 			while (!tPartRoute.isEmpty()) {
 				tRouteSegment = (RouteSegmentPath)tPartRoute.getLast();
 				while(!tRouteSegment.isEmpty()) {
 					Name addr = mRS.getAddressFromRoute(pSource, tPartRoute);
 					
 					// ignore 
 					if(addr == null) {
 						/*
 						 *  After the first valid entry, we will not accepted null entries any more.
 						 *  Because it would be not logical that the routing service is able to calculate
 						 *  destination addresses for longer routes but not for shorter ones.
 						 */
 						if(!tIgnoreNull) {
 							throw new NetworkException(this, "Able to calculate destination for long route but not for shorter routes.");
 						}
 					} else {
 						tIgnoreNull = false;
 						tHops.addFirst((RoutingServiceAddress) addr); // TODO cast umgehen
 					}
 					
 					tRouteSegment = (RouteSegmentPath) tPartRoute.getLast();
 					tRouteSegment.removeLast();	
 				}
 				tPartRoute.removeLast();
 			}
 		}
 		catch(ClassCastException tExc) {
 			// there are segments in the route, which are not supported
 			mLogger.warn(this, "Can not determine all FN addresses of route " +pRoute +". There are unsupported segments in the route.", tExc);
 
 			// return hops determined so far
 		}
 		
 		return tHops;
 	}
 	
 	private LinkedList<Name> getHopsLocal(ForwardingNode pSource, Route pRoute) throws RemoteException, NetworkException
 	{
 		LinkedList<Name> tHops = new LinkedList<Name>();
 		Route tPartRoute = (Route) pRoute.clone();
 		RouteSegmentPath tRouteSegment;
 		ForwardingNode tCurrFN = pSource;
 		
 		try {
 			// Create a list of intermediate hops without holes and without null pointers.
 			// Start with the start of the route and travel through the gates.
 			while (!tPartRoute.isEmpty()) {
 				tRouteSegment = (RouteSegmentPath)tPartRoute.removeFirst();
 				
 				while(!tRouteSegment.isEmpty()) {
 					GateID tGateNumber = tRouteSegment.removeFirst();
 					AbstractGate tGate = ((Multiplexer)tCurrFN).getGate(tGateNumber);
 					Name tNextName = null;
 					
 					if(tGate != null) {
 						// does the gate sends packets to some remote destination?
 						tNextName = tGate.getRemoteDestinationName();
 						
 						if(tNextName != null) {
 							tHops.addLast(tNextName);
 							return tHops;
 						} else {
 							// no, gate delivers packet locally -> go on
 							tCurrFN = (ForwardingNode) tGate.getNextNode();
 							
 							tNextName = getNameFor(tCurrFN);
 							if(tNextName != null) {
 								tHops.addLast(tNextName);
 							} else {
 								return tHops;
 							}
 						}
 					} else {
 						// no more valid gates
 						return tHops;
 					}
 				}
 			}
 		}
 		catch(ClassCastException tExc) {
 			// there are segments in the route, which are not supported
 			mLogger.warn(this, "Can not determine all local FN addresses of route " +pRoute +". There are unsupported segments in the route.", tExc);
 
 			// return hops determined so far
 		}
 		
 		return tHops;
 	}
 	
 	@Override
 	public RoutingServiceAddress getNameFor(ForwardingNode pNode)
 	{
 		return mRoutingIDs.get(pNode);
 	}
 
 	@Override
 	public ForwardingNode getLocalElement(Name pNode)
 	{
 		if(pNode != null) {
 			synchronized (mRoutingIDs) {
 				for(ForwardingNode element : mRoutingIDs.keySet()) {
 					RoutingServiceAddress addr = mRoutingIDs.get(element);
 					
 					if(pNode.equals(addr)) {
 						return element;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * TODO is this method really needed?
 	 * 
 	 * @param pNode Element to search for
 	 * @return Name registered in the name mapping system (null if no name found)
 	 */
 	public Name getName(RoutingServiceAddress pNode)
 	{
 		try {
 			Name[] names = mNameMapping.getNames(pNode);
 			
 			if(names.length > 0) return names[0];
 		} catch (RemoteException tExc) {
 			// ignore it and return error
 		}
 		
 		return null;
 	}
 
 	@Override
 	public boolean isKnown(Name pName)
 	{
 		try {
 			return (mNameMapping.getAddresses(pName).length > 0);
 		} catch (RemoteException tExc) {
 			mLogger.err(this, "Name " +pName +" not known since remote mapping service does not response.", tExc);
 			
 			return false;
 		}
 	}
 	
 	@Override
 	public void registerNode(ForwardingNode pElement, Name pName, NamingLevel pLevel, Description pDescription)
 	{
 		// does it have an address?
 		// if not => create and store ID for it
 		RoutingServiceAddress tID = getNameFor(pElement);
 		if(tID == null) {
 			tID = generateAddress();
 			
 			// For GUI: add additionally the node the address is used for
 			tID.setDescr(pElement);
 			
 			// set first capabilities based on the node data
 			tID.setCaps(pElement.getEntity().getNode().getCapabilities());
 			
 			// register node with ID in list
 			synchronized (mRoutingIDs) {
 				mRoutingIDs.put(pElement, tID);
 			}
 		}
 		
 		try {
 			// register name
 			if(pName != null) {
 				mNameMapping.registerName(pName, tID, pLevel);
 			}
 			
 			// forward registration to higher entity
 			mRS.registerNode(tID, (pName != null));
 		} catch (RemoteException tExc) {
 			mLogger.err(this, "Can not inform routing service entity about new node.", tExc);
 		}
 	}
 
 	@Override
 	public void updateNode(ForwardingNode pElement, Description pCapabilities) 
 	{
 		RoutingServiceAddress tRSAdr = getNameFor(pElement);
 		if(tRSAdr != null) {
 			mLogger.debug(this, "Updating capabilities for " + pElement + " to " + pCapabilities);
 			tRSAdr.setCaps(pCapabilities);
 			
 			try {
 				mRS.registerNode(tRSAdr, false);
 			} catch (RemoteException tExc) {
 				mLogger.err(this, "Can not inform routing service about update of capabilities for " +pElement);
 			}
 		} else {
 			mLogger.err(this, "Can not update FN " +pElement +" because element not found (caps=" +pCapabilities +")");
 		}
 	}
 
 	/**
 	 * Method created a new address, which can than be used for a new forwarding
 	 * node in the transfer plane.
 	 * 
 	 * @return New address (!= null)
 	 */
 	protected RoutingServiceAddress generateAddress()
 	{
 		try {
 			return mRS.generateAddress();
 		} catch (RemoteException e) {
 			// if no connection to server, generate ID on its own
 			return RoutingServiceAddress.generateNewAddress();
 		}
 	}
 
 	@Override
 	public boolean unregisterName(ForwardingNode pElement, Name pName)
 	{
 		RoutingServiceAddress tAddr = mRoutingIDs.get(pElement);
 		
 		if(tAddr != null) {
 			try {
 				// deleting on all names or just a specific one?
 				if(pName == null) {
 					return mNameMapping.unregisterNames(tAddr);
 				} else {
 					return mNameMapping.unregisterName(pName, tAddr);
 				}
 			} catch (RemoteException tExc) {
 				mLogger.err(this, "Can not unregister node from name mapping.", tExc);
 			}
 		}
 		// else: node not known anyway
 		
 		return false;
 	}
 	
 	@Override
 	public boolean unregisterNode(ForwardingNode pElement)
 	{
 		if(pElement != null) {
 			// unregister all names for this element
 			unregisterName(pElement, null);
 			
 			try {
 				RoutingServiceAddress tAddr = getNameFor(pElement);
 				if(tAddr != null) {
 					mRS.unregisterNode(tAddr);
 				}
 				// else: not registered at all
 			} catch (RemoteException tExc) {
 				mLogger.err(this, "Failed to unregister node " +pElement, tExc);
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public void reportError(Name pElement)
 	{
 		ForwardingNode element = getLocalElement(pElement);
 		boolean res = unregisterNode(element);
 		
 		// If it was not successful, just the local entity
 		// was not able to resolve it. Therefore, we have
 		// to try it once again with the next higher entity.
 		if(!res && (pElement instanceof RoutingServiceAddress)) {
 			try {
 				mRS.unregisterNode((RoutingServiceAddress) pElement);
 
 			} catch (RemoteException tExc) {
 				mLogger.err(this, "Failed to unregister node " +pElement, tExc);
 			}
 		}
 	}
 
 	public void registerLink(ForwardingElement pFrom, AbstractGate pGate) throws NetworkException
 	{
 		RoutingServiceAddress tFrom = getNameFor((ForwardingNode) pFrom);
 		
		if(tFrom != null) {
 			mLogger.log(this, "Source node " +pFrom +" of link " +pGate +" not known. Register it implicitly.");
 			registerNode((ForwardingNode)pFrom, null, NamingLevel.NONE, null);
 			
 			tFrom = getNameFor((ForwardingNode) pFrom);
 			if(tFrom == null) {
 				throw new RuntimeException(this +" - FN " +pFrom +" not known even so it was registered before.");
 			}
 		}
 		
 		informRoutingService((ForwardingNode)pFrom, pGate.getNextNode(), pGate, pGate.getRemoteDestinationName());
 	}
 	
 	/**
 	 * Informs routing service about new connection provided by a gate.
 	 * Might be called recursively.
 	 */
 	private void informRoutingService(ForwardingNode pFrom, ForwardingElement pTo, AbstractGate pGate, Name pRemoteDestinationName) throws NetworkException
 	{
 		// is it a local connection between two FNs?
 		if(pRemoteDestinationName == null) {
 			// announce local connections between multiplexers
 			// -> signals routes through nodes to next higher routing service level
 			if(pTo instanceof GateContainer) {
 				// ignore gates without a correct ID
 				if(pGate.getGateID() != null) {
 					// recursive call
 					RoutingServiceAddress tAddress = getNameFor((ForwardingNode) pTo);
 					if(tAddress == null) {
 						mLogger.warn(this, "Destination node " +pTo +" in link " +pGate +" was not registered.");
 						registerNode((GateContainer)pTo, null, NamingLevel.NONE, null);
 					}
 					informRoutingService(pFrom, pTo, pGate, tAddress);
 				}
 			}
 		} else {
 			RoutingServiceAddress tFrom = getNameFor(pFrom);
 			RoutingServiceAddress tTo;
 			
 			if(pRemoteDestinationName instanceof RoutingServiceAddress) {
 				tTo = (RoutingServiceAddress) pRemoteDestinationName;
 			} else {
 				tTo = getAddress(pRemoteDestinationName, null);
 			}
 			
 			if((tFrom != null) && (tTo != null)) {
 				// TODO Here, QoS gates are not forwarded to the parent.
 				//      In the future, this filtering had to be done by
 				//      the transfer service
 				boolean tQoSGate = false;
 				if(pGate.getDescription() != null) {
 					tQoSGate = !pGate.getDescription().isBestEffort();
 				}
 				
 				if(!tQoSGate) {
 					try {
 						mRS.registerLink(tFrom, tTo, pGate.getGateID(), pGate.getDescription());
 					} catch (RemoteException exc) {
 						throw new NetworkException("Failed to register link " +pGate +": " +tFrom +"->" +tTo +" at higher entity.", exc);
 					}
 				}
 			} else {
 				throw new NetworkException("Failed to register link " +pGate +" due to wrong name: " +tFrom +"->" +tTo);
 			}
 		}
 	}
 	
 	@Override
 	public boolean unregisterLink(ForwardingElement pNode, AbstractGate pGate) 
 	{
 		if(pGate != null) {
 			RoutingServiceAddress tID = mRoutingIDs.get(pNode);
 			
 			if(tID != null) {	
 				try {
 					return mRS.unregisterLink(tID, pGate.getGateID());
 				}
 				catch (RemoteException exc) {
 					mLogger.err(this, "Failed to unregister link " +pGate +" from " +tID, exc);
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public int getNumberVertices()
 	{
 		try {
 			return mRS.getNumberVertices();
 		} catch (RemoteException exc) {
 			mLogger.err(this, "Can not determine number of vertices from remote RS.", exc);
 			return 0;
 		}
 	}
 
 	@Override
 	public int getNumberEdges()
 	{
 		try {
 			return mRS.getNumberEdges();
 		} catch (RemoteException exc) {
 			mLogger.err(this, "Can not determine number of edges from remote RS.", exc);
 			return 0;
 		}
 	}
 
 	@Override
 	public int getSize()
 	{
 		try {
 			return mRS.getSize();
 		} catch (RemoteException exc) {
 			mLogger.err(this, "Can not determine size of remote RS.", exc);
 			return 0;
 		}
 	}
 	
 	public RemoteRoutingService getRoutingService()
 	{
 		return mRS;
 	}
 	
 	public NameMappingService getNameMappingService()
 	{
 		return mNameMapping;
 	}
 	
 	private boolean checkIfNameIsOnIgnoreList(RoutingServiceAddress pName, Description pDescription)
 	{
 		if(pName != null) {
 			if(pDescription != null) {
 				for(Property prop : pDescription) {
 					if(prop instanceof IgnoreDestinationProperty) {
 						Name ignoreName = ((IgnoreDestinationProperty) prop).getDestinationName();
 						
 						if(ignoreName != null) {
 							if(ignoreName.equals(pName)) {
 								return true;
 							}
 						}
 					}
 					// else: other property -> ignore it
 				}
 			}
 			// else: no ignore list -> do nothing
 		} else {
 			// null name should always be ignored
 			return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @param pName Element to search for
 	 * @return Address registered in the name mapping system (null if no address found)
 	 */
 	private RoutingServiceAddress getAddress(Name pName, Description pDescription) throws RoutingException
 	{
 		try {
 			NameMappingEntry<RoutingServiceAddress>[] addrs = mNameMapping.getAddresses(pName);
 			
 			if(addrs.length == 0) {
 				return null;
 			} else {
 				// Check if some destinations are excluded from search.
 				// Return first address, which is not on the ignore list.
 				for(int i=0; i<addrs.length; i++) {
 					if(!checkIfNameIsOnIgnoreList(addrs[i].getAddress(), pDescription)) {
 						return addrs[i].getAddress();
 					}
 				}
 				
 				mLogger.warn(this, "Have to ignore all " +addrs.length +" addresses listed for name " +pName +".");
 				return null;
 			}
 		} catch(RemoteException tExc) {
 			throw new RoutingException("Can not resolve address for name " +pName +".", tExc);
 		}
 	}
 	
 	@Override
 	public Namespace getNamespace()
 	{
 		return null;
 	}
 	
 	@Override
 	public String toString()
 	{
 		if(mRS != null) {
 			return mRS.toString();
 		} else {
 			return super.toString();
 		}
 	}
 	
 	private RemoteRoutingService mRS = null;
 	private final HashMap<ForwardingNode, RoutingServiceAddress> mRoutingIDs;
 	private final NameMappingService<RoutingServiceAddress> mNameMapping;
 	private Logger mLogger;
 }
