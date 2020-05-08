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
 package de.tuilmenau.ics.fog.transfer;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.List;
 
 import de.tuilmenau.ics.CommonSim.datastream.numeric.IDoubleWriter;
 import de.tuilmenau.ics.CommonSim.datastream.numeric.SumNode;
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.EventHandler;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.GateContainer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.CSVWriter;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.graph.RoutableGraph;
 
 
 /**
  * Transfer plane instance local to a host. It stores all gates and forwarding
  * node of a host. If allowed, this information is passed to a routing service
  * instance.
  * 
  * This class is esp. useful for displaying the whole transfer plane of a node
  * in the GUI.
  */
 public class TransferPlane implements TransferPlaneObserver
 {
 	/**
 	 * Creates a local routing service entity.
 	 * 
 	 * @param pRS Reference to next higher layer routing service entity
 	 * @param pNameMapping Reference to name resolution
 	 */
 	public TransferPlane(EventHandler pTimeBase, Logger pLogger)
 	{
 		mMap = new RoutableGraph<ForwardingElement, ForwardingElement>();
 		mDevNull = null;
 		mTimeBase = pTimeBase;
 		mLogger = pLogger;
 		
 		mCounterGetRouteFound = SumNode.openAsWriter(getClass().getName() +".route.number");
 	}
 	
 	public void setRoutingService(RoutingService pRoutingService)
 	{
 		mRS = pRoutingService;
 	}
 
 	public Route getRoute(ForwardingNode pSource, Name pDestination, Description pRequirements, Identity pRequester) throws RoutingException, RequirementsException
 	{
 		Route resRoute = null;
 		boolean internalRequest = false;
 		
		if(pRequirements == null) {
			Logging.log(this, "GET ROUTE from \"" + pSource + "\" to \"" + pDestination +"\"");
		} else {
			Logging.log(this, "GET ROUTE from \"" + pSource + "\" to \"" + pDestination + "\" with requirements \"" + pRequirements.toString() + "\"");
		}

 		try {
 			// check if the route is just a local one
 			// within the graph of this instance
 			ForwardingNode tLocalDestination = mRS.getLocalElement(pDestination);
 			if(tLocalDestination != null) {
 				// routing service thinks that destination is a local element
 				// -> local route request
 				internalRequest = true;
 				resRoute = getRoute(pSource, tLocalDestination);
 			} else {
 				resRoute = mRS.getRoute(pSource, pDestination, pRequirements, pRequester);
 			}
 			
 			logRSRequest(pSource.getEntity(), resRoute, internalRequest);
 			return resRoute;
 		}
 		catch(RoutingException exc) {
 			logRSRequest(pSource.getEntity(), null, internalRequest);
 			throw exc;
 		}
 		catch(RequirementsException exc) {
 			logRSRequest(pSource.getEntity(), null, internalRequest);
 			throw exc;
 		}
 	}
 	
 	/**
 	 * Logs a routing service request and its result.
 	 * The file format is as follows:
 	 * 1. Node name
 	 * 2. RS name (tranfer service name if internal)
 	 * 3. RS vertices
 	 * 4. RS edges
 	 * 5. RS size (zero if internal)
 	 * 6. route length in gate numbers
 	 * 7. route length in segments
 	 * 
 	 * 6 and 7 are zero if an error occured.
 	 */
 	private synchronized void logRSRequest(FoGEntity entity, Route result, boolean internal)
 	{
 		if(Config.Logging.LOG_ROUTE_REQUEST_RESULTS) {
 			Simulation sim = entity.getNode().getAS().getSimulation();
 			
 			try {
 				if((sRoutingLog == null) && !sTriedToOpen) {
 					// try to open log (only once)
 					sTriedToOpen = true;
 					
 					String filename = sim.getBaseDirectory() +"RS_results.csv";
 					sRoutingLog = new CSVWriter(filename);
 					
 					// write header line
 					sRoutingLog.write("node");
 					sRoutingLog.write("RS name");
 					sRoutingLog.write("RS vertices");
 					sRoutingLog.write("RS edges");
 					sRoutingLog.write("RS size");
 					sRoutingLog.write("Route length in gate numbers");
 					sRoutingLog.write("Route length in segments");
 					sRoutingLog.finishEntry();
 					
 					sim.getLogger().info(this, "Opened routing service log '" +filename +"'");
 					
 					sim.registerClosable(new Closeable() {
 						@Override
 						public void close() throws IOException {
 							synchronized(TransferPlane.this) {
 								if(sRoutingLog != null) {
 									sRoutingLog.close();
 									sRoutingLog = null;
 									sTriedToOpen = false;
 								}
 							}
 						}
 					});
 				}
 				
 				// log statistical data about routing service itself and the result
 				if(sRoutingLog != null) {
 					sRoutingLog.write(entity.toString());
 					if(internal) {
 						sRoutingLog.write(this.toString());
 						sRoutingLog.write(mMap.getNumberVertices());
 						sRoutingLog.write(mMap.getNumberEdges());
 						sRoutingLog.write(0);
 					} else {
 						sRoutingLog.write(mRS.toString());
 						sRoutingLog.write(mRS.getNumberVertices());
 						sRoutingLog.write(mRS.getNumberEdges());
 						sRoutingLog.write(mRS.getSize());
 					}
 					
 					if(result != null) {
 						sRoutingLog.write(result.sizeNumberGates());
 						sRoutingLog.write(result.size());
 					} else {
 						sRoutingLog.write(0);
 						sRoutingLog.write(0);
 					}
 					
 					sRoutingLog.finishEntry();
 				}
 			}
 			catch(IOException exc) {
 				if(sRoutingLog != null) {
 					sim.getLogger().err(this, "Error while opening/writing routing service log.", exc);
 					try {
 						sRoutingLog.close();
 					}
 					catch(IOException exc2) {
 						// ignore
 					}
 					sRoutingLog = null;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Route calculation in local map without contacting the routing service.
 	 */
 	private Route getRoute(ForwardingElement pSource, ForwardingElement pTarget) throws RoutingException
 	{
 		Route tRes = null;
 		
 		mLogger.log(this, "Searching for internal route from " +pSource +" to " +pTarget);
 		
 		// Does map contain start and end points?
 		if(!mMap.contains(pSource)) {
 			throw new RoutingException("Map does not contain source '" +pSource +"'. Invalid try to calculate internal route in " +this +".");
 		}
 		if(!mMap.contains(pTarget)) {
 			throw new RoutingException("Map does not contain target '" +pTarget +"'. Invalid try to calculate internal route in " +this +".");
 		}
 
 		// Are source and destination different?
 		if(!pSource.equals(pTarget)) {
 			// Do path calculation itself
 			List<ForwardingElement> tPath = mMap.getRoute(pSource, pTarget);
 			
 			// is there a route from source to target?
 			if(!tPath.isEmpty()) {
 				tRes = new Route();
 				
 				// convert path to list of gateIDs
 				tRes = new Route();
 				for(ForwardingElement tLink : tPath) {
 					if(tLink instanceof Gate) {
 						tRes.addLast(((Gate)tLink).getGateID());
 					} else {
 						throw new RoutingException(this +" - Invalid link " +tLink +" in internal route.");
 					}
 				}
 			}
 			// else: tRes remains null
 		} else {
 			tRes = new Route();
 		}
 
 		// no route?
 		if(tRes == null) {
 			throw new RoutingException("No internal route from '" +pSource +"' to '" +pTarget +"' available.");
 		} else {
 			// count call
 			mCounterGetRouteFound.write(+1.0, mTimeBase.nowStream());
 			
 			mLogger.log(this, "Internal route from " +pSource +" to " +pTarget +" = " +tRes);
 		}
 		
 		return tRes;
 	}
 	
 	@Override
 	public void registerNode(ForwardingNode pElement, Name pName, NamingLevel pLevel, Description pDescription)
 	{
 		if(!mMap.contains(pElement)) {
 			// add it to graph
 			mMap.add(pElement);
 		}
 		
 		// report it to higher layer, if it is not private or if it is important for naming
 		boolean reportIt = !pElement.isPrivateToTransfer() || (pLevel != NamingLevel.NONE);
 		if(reportIt) {
 			if (mRS != null) {
 				mRS.registerNode(pElement, pName, pLevel, pDescription);
 			}else{
 				mLogger.err(this, "Invalid routing service found, cannot register node " + pName + "[" + pElement + "]");
 			}
 		}
 	}
 
 	@Override
 	public void updateNode(ForwardingNode pElement, Description pCapabilities) 
 	{
 		if(!mMap.contains(pElement)) {
 			// add it to graph implicitly
 			registerNode(pElement, null, NamingLevel.NONE, pCapabilities);
 		} else {
 			mRS.updateNode(pElement, pCapabilities);
 		}
 	}
 
 	/**
 	 * Checks only if the element is known locally.
 	 * The next higher layer might not know about the element.
 	 * We can not check if the next higher level might not know
 	 * about it on purpose or accidently.
 	 */
 	public boolean isRegistered(ForwardingNode pElement)
 	{
 		return mMap.contains(pElement);
 	}
 
 	@Override
 	public boolean unregisterNode(ForwardingNode pElement)
 	{
 		if(pElement != null) {
 			if(mMap.remove(pElement)) {
 				mRS.unregisterNode(pElement);
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public void registerLink(ForwardingElement pFrom, AbstractGate pGate) throws NetworkException
 	{
 		ForwardingElement tTo = pGate.getNextNode();
 		
 		if(tTo == null) {
 			if(mDevNull == null) {
 				mDevNull = new DummyForwardingElement("dev/null");
 			}
 			
 			tTo = mDevNull;
 		}
 		
 		mMap.link(pFrom, tTo, pGate);
 		
 		// should gate be reported to routing?
 		boolean isPrivate = pGate.isPrivateToTransfer();
 		if(!isPrivate) {
 			// source or destination private to transfer service?
 			if(pFrom instanceof GateContainer) isPrivate |= ((GateContainer) pFrom).isPrivateToTransfer();
 			if(tTo instanceof GateContainer) isPrivate |= ((GateContainer) tTo).isPrivateToTransfer();
 		}
 		
 		if(!isPrivate) {
 			// ignore other start and end points since they are
 			// just private elements of the node or helper elements of the GUI
 			if(pFrom instanceof ForwardingNode) {
 				if (mRS != null) {
 					mRS.registerLink(pFrom, pGate);
 				}else{
 					mLogger.err(this, "Invalid routing service found, cannot register link from " + pFrom + " to " + tTo);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public boolean unregisterLink(ForwardingElement pNode, AbstractGate pGate) 
 	{
 		if(pGate != null) {
 			// unlink it for GUI in any case (pNode might be a Bus!)
 			if(mMap.unlink(pGate)) {
 				// ignore other elements, since they are only in for the GUI
 				if(pNode instanceof ForwardingNode) {
 					return mRS.unregisterLink(pNode, pGate);
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @return internal graph for displaying it with a GUI
 	 */
 	public RoutableGraph<ForwardingElement, ForwardingElement> getGraph()
 	{
 		return mMap;
 	}
 	
 	private RoutingService mRS = null;
 	private final RoutableGraph<ForwardingElement, ForwardingElement> mMap;
 	private ForwardingElement mDevNull;
 	private Logger mLogger;
 	
 	private EventHandler mTimeBase;
 	
 	/**
 	 * Counter for calls to getRoute with a positive result.
 	 */
 	private IDoubleWriter mCounterGetRouteFound;
 
 	/**
 	 * Static routing log
 	 */
 	private static CSVWriter sRoutingLog = null;
 	private static boolean sTriedToOpen = false;
 }
