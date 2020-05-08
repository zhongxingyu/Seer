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
 package de.tuilmenau.ics.fog.transfer.manager;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.IContinuation;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.exceptions.TransferServiceException;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.facade.properties.FunctionalRequirementProperty;
 import de.tuilmenau.ics.fog.facade.properties.IDirectionPair;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.facade.properties.LossRateProperty;
 import de.tuilmenau.ics.fog.facade.properties.NonFunctionalRequirementsProperty;
 import de.tuilmenau.ics.fog.facade.properties.PriorityProperty;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.facade.properties.TransportProperty;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.Signalling;
 import de.tuilmenau.ics.fog.packets.TransferFailed;
 import de.tuilmenau.ics.fog.packets.statistics.ReroutingTestAgent;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.RouteSegmentAddress;
 import de.tuilmenau.ics.fog.routing.RouteSegmentDescription;
 import de.tuilmenau.ics.fog.routing.RouteSegmentMissingPart;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingService;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.Gate;
 import de.tuilmenau.ics.fog.transfer.Gate.GateState;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.GateContainer;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.DirectDownGate;
 import de.tuilmenau.ics.fog.transfer.gates.DownGate;
 import de.tuilmenau.ics.fog.transfer.gates.ErrorReflectorGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.GateIterator;
 import de.tuilmenau.ics.fog.transfer.gates.HorizontalGate;
 import de.tuilmenau.ics.fog.transfer.gates.ReroutingGate;
 import de.tuilmenau.ics.fog.transfer.gates.TransparentGate;
 import de.tuilmenau.ics.fog.transfer.gates.roles.GateClass;
 import de.tuilmenau.ics.fog.transfer.gates.roles.IFunctionDescriptor;
 import de.tuilmenau.ics.fog.transfer.manager.RequirementsToGatesMapper.Variable;
 import de.tuilmenau.ics.fog.transfer.manager.RequirementsToGatesMapper.Word;
 import de.tuilmenau.ics.fog.transfer.manager.path.SocketPathParam;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.SimpleName;
 
 
 public class Controller
 {
 	public Controller(Node node)
 	{
 		mNode = node;
 		mLogger = node.getLogger();
 	}
 	
 	/**
 	 * A packet contains an invalid gate number. The transfer plane was
 	 * not able to resolve the next gate for this number.
 	 * The packet has to be taken over by the manager. After this call, the
 	 * transfer plane will not consider the packet any further. 
 	 * 
 	 * @param gateNumber Invalid number the lastHop was not able to resolve.
 	 * @param packet Packet, which causes the exception
 	 * @param lastHop Element, which detected the wrong gate number
 	 */
 	public void invalidGate(GateID gateNumber, Packet packet, ForwardingNode lastHop)
 	{
 		mLogger.err(this, "Invalid gate number " +gateNumber +" in: " +packet +" at FN " +lastHop + ". Packet dropped.");
 		packet.dropped(lastHop);
 		// inform packet sender about invalid gate
 		//TODO: folgende Zeilen fhren zu dauernden Connection-Retries wenn der Socket auf anderer Seite geschlossen wurde
 		//Packet tErrSigPacket = new Packet(packet.getReturnRoute().clone(), new SignallingInvalidGate());
 		//lastHop.handlePacket(tErrSigPacket, null);
 	}
 	
 	/**
 	 * A packet is not able to pass through a gate, since the gate is not in
 	 * a suitable state.
 	 * 
 	 * @param gate Gate, which is not in OPERATE state
 	 * @param packet Packet, which can not be forwarded to the gate
 	 * @param lastHop Element, which detected the error
 	 */
 	public void invalidGateState(AbstractGate gate, Packet packet, ForwardingNode lastHop)
 	{
 		mLogger.err(this, "Invalid gate state of gate " +gate +" for: " +packet +" at FN " +lastHop + ". Packet dropped.");
 		packet.dropped(lastHop);
 	}
 	
 	private void reportTransferError(Packet packet, Multiplexer lastHop, Exception error)
 	{
 		mLogger.warn(this, "Transfer error for packet " +packet +" at FN " +lastHop, error);
 		
 		packet.dropped(lastHop);
 		// it is a normal packet, inform source about error
 		Route reverseRoute = packet.getReturnRoute();
 		if(reverseRoute != null) {
 			Object payload = packet.getData();
 			Signalling basedOn = null;
 			if(payload instanceof Signalling) {
 				basedOn = (Signalling) payload;
 			}
 			
 			Packet errMsg = new Packet(reverseRoute, new TransferFailed(error, basedOn));
 			mNode.getAuthenticationService().sign(errMsg, mNode.getIdentity());
 			lastHop.handlePacket(errMsg, null);
 		} else {
 			mLogger.warn(this, "No reverse route for packet. Can not send an error message to source.");
 		}
 	}
 	
 	/**
 	 * A packet contains an incomplete route. The manager is requested
 	 * to perform a routing service lookup in order to calculate the next
 	 * (partial) route for this packet.
 	 * The packet has to be taken over by the manager. After this call, the
 	 * transfer plane will not consider the packet any further. 
 	 * 
 	 * @param packet Packet with incomplete route.
 	 * @param lastHop Element, which detected the incomplete route.
 	 */
 	public void incompleteRoute(Packet packet, Multiplexer lastHop)
 	{
 		try {
 			if(!packet.change()) {
 				throw new TransferServiceException(this, "Too many changes for packet " +packet +" at FN " +lastHop);
 			}
 		
 			// are there some more information in the route, which can be used
 			// to find the destination?
 			if(!packet.getRoute().isEmpty()) {
 				RouteSegment segment = packet.getRoute().removeFirst();
 				
 				if(segment instanceof RouteSegmentAddress) {
 					//
 					// There is no explicit route (any more) and node is requested to
 					// calculate the next partial route towards the destination.
 					//
 					Name addr = ((RouteSegmentAddress) segment).getAddress();
 					Description descr = null;
 					
 					mLogger.log(this, "Incomplete route with target " +segment +" in: " +packet +" at FN " +lastHop);
 					
 					// is there a description in the route?
 					if(!packet.getRoute().isEmpty()) {
 						if(packet.getRoute().getFirst() instanceof RouteSegmentDescription) {
 							descr = ((RouteSegmentDescription) packet.getRoute().removeFirst()).getDescription();
 						}
 					}
 					
 					try {
 						Identity origin = null;
 						Signature firstSig = packet.getSenderAuthentication();
 						if(firstSig != null) {
 							origin = firstSig.getIdentity();
 						}
 						Route nextRoute = mNode.getTransferPlane().getRoute(lastHop, addr, descr, origin);
 					
						packet.getRoute().addFirst(nextRoute);
						mLogger.log(this, "Set new route for " + packet);
						lastHop.handlePacket(packet, null);
 					}
 					catch(NetworkException exc) {
 						throw new TransferServiceException(this, "Can not calculate next partial route to " +segment +". Packet dropped.", exc);
 					}
 				}
 				else if(segment instanceof RouteSegmentMissingPart) {
 					//
 					// A part of the route is missing and the routing service requested
 					// the creation of gates
 					//
 					mLogger.log(this, "Missing parts of route in: " +packet +" at FN " +lastHop);
 					
 					resolveMissingPart(lastHop, (RouteSegmentMissingPart) segment, packet);
 				}
 				else {
 					throw new TransferServiceException(this, "Wrong first route segment type " +segment.getClass() +". Packet dropped.");
 				}
 			} else {
 				throw new TransferServiceException(this, "No route in packet " +packet +". Packet dropped.");
 			}
 		}
 		catch(Exception exception) {
 			reportTransferError(packet, lastHop, exception);
 		}
 	}
 	
 	private class CreateGateContinuation implements IContinuation<Gate>
 	{
 		/**
 		 * Packet has to proceed only through new gate.
 		 */
 		public CreateGateContinuation(Multiplexer pFN, Packet pPacket)
 		{
 			this(pFN, pPacket, null, null);
 		}
 
 		/**
 		 * Packet has to proceed:
 		 * 1. through new gate (down)
 		 * 2. travel to the remote process
 		 * 3. pass the peer gate of the remote process (up)
 		 * 
 		 * @param pRouteBetweenConstrProcesses Is optional; if null, the address of the remote process will be added.
 		 */
 		public CreateGateContinuation(Multiplexer pFN, Packet pPacket, ProcessGateCollectionConstruction pConstrProcess, RouteSegment pRouteBetweenConstrProcesses)
 		{
 			mFN = pFN;
 			mPacket = pPacket;
 			mConstrProcess = pConstrProcess;
 			mSegment = pRouteBetweenConstrProcesses;
 		}
 
 		@Override
 		public void success(Gate pGate)
 		{
 			if(pGate.isOperational()) {
 				boolean startsWithMissingPart = false;
 				
 				if(mConstrProcess != null) {
 					Route toPeerGate = mConstrProcess.getPeerRouteUp();
 
 					// 1. Route in remote process up
 					if(toPeerGate != null) {
 						mPacket.addGateIDFront(toPeerGate);
 					} else {
 						mLogger.warn(this, "No peer route up to its gates for packet " +mPacket);
 					}
 					
 					// was intermediate route defined or do we have to insert an address?
 					if(mSegment == null) {
 						Name peerName = mConstrProcess.getPeerRoutingName();
 						if(peerName != null) {
 							mSegment = new RouteSegmentAddress(peerName);
 						} else {
 							mLogger.warn(this, "No peer routing name available for packet " +mPacket);
 						}
 					}
 					
 					// 2. Route to remote process
 					if(mSegment != null) {
 						mPacket.addGateIDFront(mSegment);
 					}
 				} else {
 					RouteSegment seg = mPacket.getRoute().getFirst();
 					
 					if(seg != null) {
 						startsWithMissingPart = (seg instanceof RouteSegmentMissingPart);
 					}
 				}
 				
 				// 3. Local route down
 				//    If there is a (second) missing part, we have to deal with it first
 				if(!startsWithMissingPart) {
 					mPacket.addGateIDFront(pGate.getGateID());
 				} else {
 					if(mConstrProcess == null) {
 						mLogger.trace(this, "Leading MissingPart for " +mPacket);
 					}
 				}
 				
 				mFN.handlePacket(mPacket, null);
 			} else {
 				if(pGate.getState() == GateState.INIT) {
 					pGate.waitForStateChange(Config.Transfer.GATE_STD_TIMEOUT_SEC, this);
 				} else {
 					failure(pGate, null);
 				}
 			}
 		}
 
 		@Override
 		public void failure(Gate pGate, Exception pException)
 		{
 			mLogger.err(this, "Can not use new gate " +pGate +". Dropping packet " +mPacket, pException);
 			mPacket.droppingDetected(this);
 		}
 		
 		public Process getProcess()
 		{
 			return mConstrProcess;
 		}
 		
 		private Multiplexer mFN;
 		private Packet mPacket;
 		private ProcessGateCollectionConstruction mConstrProcess;
 		private RouteSegment mSegment;
 	}
 	
 	/** Wrapper for using CreateGateContinuation with Process */
 	private class CreateGateProcessContinuation implements IContinuation<Process>
 	{
 		public CreateGateProcessContinuation(IContinuation<Gate> pCont, Gate pGate)
 		{
 			mCont = pCont;
 			mGate = pGate;
 		}
 
 		@Override
 		public void success(Process pProcess)
 		{
 			mCont.success(mGate);
 		}
 
 		@Override
 		public void failure(Process pProcess, Exception pException)
 		{
 			mCont.failure(mGate, pException);
 		}
 		
 		private IContinuation<Gate> mCont;
 		private Gate mGate;
 	}
 	
 	private class CreationResult
 	{
 		public CreationResult(Gate pGate, ProcessGateCollectionConstruction pProcess)
 		{
 			mGate = pGate;
 			mProcess = pProcess;
 		}
 		
 		public Gate mGate;
 		public ProcessGateCollectionConstruction mProcess;
 	}
 	
 	private void addContinuation(Gate gate, Process process, IContinuation<Gate> continuation)
 	{
 		// Wait until gate is ready.
 		// If gate is operational, we will wait for the process.
 		// If both are ready, we will not wait and continue right away.
 		synchronized(gate) {
 			if(gate.isOperational()) {
 				if((process == null) || process.isOperational()) {
 					continuation.success(gate);
 				} else {
 					IContinuation<Process> contProc = new CreateGateProcessContinuation(continuation, gate);
 					process.observeNextStateChange(Config.PROCESS_STD_TIMEOUT_SEC, contProc);
 				}
 			} else {
 				gate.waitForStateChange(Config.Transfer.GATE_STD_TIMEOUT_SEC, continuation);
 			}
 		}
 	}
 	
 	/**
 	 * Handles a {@link RouteSegmentMissingPart} request from the routing service.
 	 * 
 	 * @param fn Forwarding node at which the gate is required.
 	 * @param newGateDescription Description of the requested gate
 	 * @param packet Packet, which triggered the creation and which should be send through the gate
 	 * @throws NetworkException On error
 	 */
 	private void resolveMissingPart(Multiplexer fn, RouteSegmentMissingPart newGateDescription, Packet packet) throws NetworkException
 	{
 		AbstractGate parallelGate = newGateDescription.getParallelGate();
 		Gate newGate = null;
 		boolean reuse = false;
 		
 		if(parallelGate == null) {
 			//
 			// Create new function, which is not replacing another one
 			//
 			Description requ = newGateDescription.getDescr();
 			CreationResult res = createNewArbitraryGate(fn, requ, newGateDescription.getRequester(), fn, null);
 			
 			newGate = res.mGate;
 			if(newGate != null) {
 				IContinuation<Gate> cont = new CreateGateContinuation(fn, packet, res.mProcess, null);
 				
 				addContinuation(newGate, res.mProcess, cont);
 			}
 		}
 		else if(parallelGate instanceof TransparentGate) {
 			// we do not care about QoS for boring gates on the same host
 			// -> just reuse parallel one
 			reuse = true;
 		}
 		else if(parallelGate instanceof DownGate) {
 			// parallel gate leaves host
 			// -> create parallel one with other QoS
 			Description requ = newGateDescription.getDescr();
 			
 			if(!requ.isBestEffort()) {
 				mLogger.log(this, "Creating gate in parallel to " +parallelGate +" with " +requ +".");
 
 				// is max loss really required?
 				LossRateProperty lossRate = (LossRateProperty) requ.get(LossRateProperty.class);
 				if(lossRate != null) {
 					if(lossRate.isBE()) {
 						// just a min value; ignore it
 						lossRate = null;
 					}
 				}
 				
 				// continuation, which handles a packet after new gates have been set up 
 				CreateGateContinuation cont = null;
 				
 				if(lossRate != null) {
 					//
 					// 1. Handle loss rate requirements:
 					//    Calls method recursively to handle remaining
 					//    requirements without loss first (see (2)).
 					//
 					requ.remove(lossRate);
 					
 					// check if it is a problem with this lower layer
 					boolean llIsLossy = isLossy(((DownGate) parallelGate).getLowerLayer().getBus());
 					
 					// was loss rate the only property?
 					if(requ.isBestEffort() && llIsLossy) {
 						// 1.1 yes -> create Transport gates to deal with loss
 						Route routeToPeer = new Route();
 						routeToPeer.addFirst(parallelGate.getGateID());
 						
 						Description tRequ = new Description();
 						tRequ.set(new TransportProperty(false, true));
 						
 						CreationResult res = createNewArbitraryGate(fn, tRequ, mNode.getIdentity(), fn, routeToPeer);
 						newGate = res.mGate;
 						
 						// how to proceed with the packet itself?
 						cont = new CreateGateContinuation(fn, packet, res.mProcess, new RouteSegmentPath(parallelGate.getGateID()));
 					} else {
 						// 1.2 no -> create other QoS gates first
 						
 						// do we need the transport stuff later on?
 						if(llIsLossy) {
 							Description tLossDescr = new Description();
 							tLossDescr.set(lossRate);
 							
 							packet.addGateIDFront(new RouteSegmentMissingPart(tLossDescr, null, newGateDescription.getRequester()));
 						}
 						// else: ignore loss requ. because link is not lossy
 						
 						// recursive call without loss rate
 						resolveMissingPart(fn, newGateDescription, packet);
 						return;
 					}
 				} else {
 					//
 					// 2. Handle requirements without loss rate requirement
 					//
 					DownGate downGate = (DownGate) parallelGate;
 					boolean priorityOnly = isPriorityOnly(requ);
 					
 					if(priorityOnly) {
 						// 2.1 Try to reuse another priority gate
 						newGate = searchForParallelPriorityGate(fn, downGate);
 						reuse = (newGate != null);
 					}
 					
 					if(newGate == null) {
 						// 2.2 Reuse not possible: Create a new gate
 						newGate = createNewDownGate(fn, downGate.getLowerLayer(), downGate.getToLowerLayerID(), requ, newGateDescription.getRequester(), null, false);
 					}
 					
 					// Check if there is a RouteSegementMissingPart on the "stack", which
 					// had been stored by the first recursive call of this method (see 1.2).
 					// If so, set the new DownGate as parallel gate for this activity.
 					RouteSegment nextSegment = packet.getRoute().getFirst();
 					if((nextSegment != null) && (nextSegment instanceof RouteSegmentMissingPart)) {
 						RouteSegmentMissingPart nextSegmentMissing = (RouteSegmentMissingPart) nextSegment;
 						
 						if(nextSegmentMissing.getParallelGate() == null) {
 							if(newGate instanceof AbstractGate) {
 								nextSegmentMissing.setParallelGate((AbstractGate) newGate);
 							} else {
 								mLogger.err(this, "Can not update RouteSegmentMissingPart to new gate because type of " +newGate +" does not match.");
 							}
 						}
 					}
 					
 					// proceed after gate is ready without any process dependencies
 					cont = new CreateGateContinuation(fn, packet);
 				}
 				
 				if(newGate != null) {
 					// debug test
 					if(cont == null){
 						throw new RuntimeException(this +" - Internal error: " +newGate +" defined but no continuation.");
 					}
 					
 					// wait until we can send along the packet
 					addContinuation(newGate, cont.getProcess(), cont);
 				}
 			} else {
 				// no non-functional requ? -> reuse
 				reuse = true;
 			}
 		}
 		
 		if(reuse) {
 			mLogger.log(this, "Reusing " +parallelGate +"; do not create anything new.");
 			
 			// if not done before, we forward the packet
 			if(newGate == null) {	
 				packet.addGateIDFront(parallelGate.getGateID());
 				fn.handlePacket(packet, null);
 				newGate = parallelGate;
 			}
 		}
 		
 		if(newGate == null) {
 			// no way to setup something equivalent with other QoS
 			// -> error
 			throw new TransferServiceException(this, "Unable to create something equivalent to " +parallelGate +".");
 		}
 	}
 
 	/**
 	 * @return If a lower layer might loose packets.
 	 */
 	private static boolean isLossy(ILowerLayer bus)
 	{
 		try {
 			Description busDescr = bus.getDescription();
 			
 			if(busDescr != null) {
 				LossRateProperty bussLoss = (LossRateProperty) busDescr.get(LossRateProperty.class);
 				if(bussLoss != null) {
 					return bussLoss.isBE();
 				}
 			}
 		} catch (RemoteException exc) {
 			// ignore it
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Transfer plane detected a packet with a suboptimal route.
 	 * TODO use it in multiplexer
 	 * TODO check, if all needed parameters are present
 	 * TODO who copies the packet? 
 	 *  
 	 * @param packet Packet with suboptimal route.
 	 * @param lastHop Element, which detected the suboptimal route.
 	 */
 	public void suboptimalRoute(Packet packet, ForwardingElement lastHop)
 	{
 		mLogger.warn(this, "Suboptimal route: " +packet.getReturnRoute());
 
 		// ignore this waring and just go on
 		lastHop.handlePacket(packet, null);
 	}
 	
 	/**
 	 * Adds a new interface to a bus to a node. If node is already
 	 * connected to bus, nothing is done.
 	 * 
 	 * @param bus Bus, which the node should connect to
 	 * @return Interface for the connection to the bus OR null, if attach operation failed
 	 */
 	public NetworkInterface addLink(ILowerLayer bus)
 	{
 		NetworkInterface newNetworkInterface = getNetworkInterface(bus);
 		
 		// isn't node already connected to bus?
 		if(newNetworkInterface == null) {
 			newNetworkInterface = new NetworkInterface(bus, mNode);
 			
 			if(newNetworkInterface.attach()) {
 				mLogger.log(this, "new network interface " +newNetworkInterface);
 				
 				mNode.count(NetworkInterface.class.getName(), true);
 				
 				// register generic send gate
 				// => that is important for calculating the reverse routes, while setting up DownGates
 	//			mNode.GetCentralMultiplexer().registerGate(newNetworkInterface.getGenericSendGate());
 			
 				// TODO folgendes mit RoutingService "nachbauen" zum Anzeigen?
 		/*			SimulationElements.getInstance().link(newNetworkInterface, mNode, newNetworkInterface.getBus());
 					if(Config.EXTENDED_GRAPH) {
 						SimulationElements.getInstance().link(newNetworkInterface.getBus(), newNetworkInterface.getReceiveGate());
 						SimulationElements.getInstance().link(newNetworkInterface.getReceiveGate(), newNetworkInterface.getMultiplexerGate());
 					} else {
 						// without receive gate
 						SimulationElements.getInstance().link(newNetworkInterface.getBus(), newNetworkInterface.getMultiplexerGate());
 					}
 			*/		
 				buslist.add(newNetworkInterface);
 				
 				mNode.notifyObservers(newNetworkInterface);
 			} else {
 				mLogger.warn(this, "Can not attach to network interface to lower layer " +bus);
 			}
 		}
 		return newNetworkInterface;
 	}
 	
 	/**
 	 * Searches for a interface to a bus.
 	 * 
 	 * @param bus Searching for this bus (by reference)
 	 * @return interface or null if not connected to bus
 	 */
 	public NetworkInterface getNetworkInterface(ILowerLayer bus)
 	{
 		for(NetworkInterface tInterf : buslist) {
 			if(tInterf != null)
 				if(tInterf.getBus() == bus) return tInterf;
 		}
 		
 		return null;
 	}
 	
 
 	/**
 	 * Removes interface connected to a bus.
 	 * 
 	 * @param lowerLayer
 	 */
 	public NetworkInterface removeLink(ILowerLayer lowerLayer)
 	{
 		for(NetworkInterface tInter : buslist) {
 			if(tInter.getBus() == lowerLayer) {
 				buslist.remove(tInter);
 				removeNetworkInterface(tInter);
 				
 				if(tInter != null) {
 					mNode.count(NetworkInterface.class.getName(), false);
 				}
 				
 				return tInter;
 			}
 		}
 		
 		return null;
 	}
 	
 	private void removeNetworkInterface(NetworkInterface netInterface)
 	{
 		mLogger.log(this, "removing network interface " +netInterface);
 		
 		netInterface.detach();
 		
 		mNode.notifyObservers(netInterface);
 	}
 	
 	public void addNeighbor(NetworkInterface pInterface, NeighborInformation pNeighborLLID)
 	{
 		GateContainer tFN = pInterface.getMultiplexerGate(); 
 
 		synchronized(tFN) { synchronized(tFN.getNode()) {
 			ReroutingGate[] backup = new ReroutingGate[1];
 			Description requ = Description.createBE(false);
 			DownGate downGate = checkDownGateAvailable(tFN, pNeighborLLID, null, requ, backup);
 			
 			// check, if DownGate already available
 			if(downGate == null) {
 				// if there is a backup for the best-effort gate, there might be more gates,
 				// which can be repaired?
 				if(backup[0] != null) {
 					mLogger.trace(this, "BE DownGate to neighbor available as rerouting gate. Maybe other gates can be repaired, too? Schedule event.");
 					
 					mNode.getTimeBase().scheduleIn(1.0d, new RepairEvent(pInterface, pNeighborLLID));
 				}
 				
 				//
 				// Option (A):
 				// Request bidirectional connection without previous setup on node itself
 				//
 				// Assumes that the neighbor will try to setup a reverse gate for its gate.
 				
 				//
 				// Option (B):
 				// Setup one gate immediately and request reverse gate, only
 				//
 				try {
 					createNewDownGate(tFN, pInterface, pNeighborLLID, requ, mNode.getIdentity(), backup[0], false);
 				}
 				catch (NetworkException tExc) {
 					mLogger.warn(this, "Can not add down gate to neighbor " +pNeighborLLID +". Ignoring neighbor.", tExc);
 				}
 			} else {
 				mLogger.log(this, "DownGate to neighbor " +pNeighborLLID +" on interface " +pInterface +" already available.");
 
 				mLogger.log(this, "Refreshing DownGate " +downGate);
 				downGate.refresh();
 			}
 		} }
 	}
 	
 	/**
 	 * Creates new down gate to neighbor and requests the reverse gate from it. 
 	 * 
 	 * @param pFN FN to connect the gate to
 	 * @param pInterface Lower layer
 	 * @param pNeighborLLID Lower layer name of peer
 	 * @param pDescription Requirements for down gate
 	 * @return reference to new gate (!= null)
 	 * @throws NetworkException on error
 	 */
 	private Gate createNewDownGate(GateContainer pFN, NetworkInterface pInterface, NeighborInformation pNeighborLLID, Description pDescription, Identity pRequester, ReroutingGate pBackup, boolean pLazyCreation) throws NetworkException
 	{
 		mLogger.log(this, "Creating DownGate to neighbor " +pNeighborLLID +" on interface " +pInterface +" with " +pDescription);
 		
 		// create process handling the creation and deletion of down gate
 		ProcessDownGate tProcess = new ProcessDownGate(pFN, pInterface, pNeighborLLID, pDescription, pRequester, pBackup);
 		
 		try {
 			Gate tGate = null;
 			
 			tProcess.start();
 			
 			if(pLazyCreation && (pBackup != null)) {
 				// delay creation it until signaling is finished
 			} else {
 				tGate = tProcess.create();
 			}
 			
 			// Request reverse gate from peer
 			Name localRoutingName = mNode.getRoutingService().getNameFor(pFN);
 			
 			tProcess.signal(localRoutingName);
 			
 			return tGate;
 		}
 		catch (NetworkException tExc) {
 			tExc = new TransferServiceException(this, "Opening down gate to neighbor " +pNeighborLLID +" failed.", tExc);
 			
 			tProcess.terminate(tExc);
 			throw tExc;
 		}
 	}
 	
 	/**
 	 * Checks it an available process can be reused in order to satisfy the
 	 * given requirements.
 	 * 
 	 * TODO does the matching requires the destination and the intermediate route, too?
 	 * 
 	 * @param pFN FN at which the requirements should be fulfilled
 	 * @param pRequirements Requirements themselves
 	 * @return Matching process or null
 	 */
 	private ProcessConstruction isAlreadyAvailable(GateContainer pFN, Description pRequirements)
 	{
 		ProcessConstruction tRes = null;
 		ProcessList processList = pFN.getNode().getProcessRegister().getProcesses(pFN);
 		
 		if((processList != null) && (pRequirements != null)) {
 			for(Process tProcess : processList) {
 				if(tProcess.isOperational() && (tProcess instanceof ProcessConstruction)) {
 					Description tDesc = ((ProcessConstruction) tProcess).getDescription();
 					
 					// does the process contains the same requ than the request?
 					if(pRequirements.equals(tDesc)) {
 						tRes = (ProcessConstruction) tProcess;
 						break;
 					}
 				}
 			}
 		}
 		
 		return tRes;
 	}
 
 	/**
 	 * Creates or reuses gates in order to satisfy given requirements.
 	 */
 	private CreationResult createNewArbitraryGate(GateContainer pFN, Description pRequiredFunction, Identity pRequester, ForwardingNode pNextFN, Route pRouteToPeer)
 	{
 		ProcessConstruction tAlreadyAvailable = isAlreadyAvailable(pFN, pRequiredFunction);
 		if((tAlreadyAvailable == null) || !(tAlreadyAvailable  instanceof ProcessGateCollectionConstruction)) {
 			if(pRouteToPeer != null) {
 				mLogger.log(this, "Creating gates for " +pRequiredFunction +" at " +pFN +" to " +pNextFN +" with route " +pRouteToPeer);
 			} else {
 				mLogger.log(this, "Creating gates for " +pRequiredFunction +" at " +pFN +" to " +pNextFN +" without peer");
 			}
 			
 			ProcessGateCollectionConstruction tProcess = new ProcessGateCollectionConstruction(pFN, pNextFN, pRequiredFunction, pRequester);
 			
 			try {
 				tProcess.disableHorizontal();
 				tProcess.start();
 				tProcess.recreatePath(null, null);
 				if(Config.Connection.TERMINATE_WHEN_IDLE) {
 					tProcess.activateIdleTimeout();
 				}
 				
 				if(pRouteToPeer != null) {
 					// Request peer gate from peer host
 					tProcess.signal(false, pRouteToPeer);
 				} else {
 					// No peer; emulate response from one
 					tProcess.updateRoute(null, null, null, pRequester);
 				}
 				
 				AbstractGate gate = tProcess.getClientLeavingGate();
 				return new CreationResult(gate, tProcess);
 			}
 			catch (NetworkException tExc) {
 				mLogger.err(this, "Opening gates for " +pRequiredFunction +" at " +pFN +" failed.", tExc);
 				tProcess.terminate(tExc);
 			}
 		} else {
 			ProcessGateCollectionConstruction tProcess = (ProcessGateCollectionConstruction) tAlreadyAvailable;
 			
 			mLogger.log(this, "Reusing gates for " +pRequiredFunction +" at " +pFN +" with process " +tProcess);
 			
 			return new CreationResult(tProcess.getClientLeavingGate(), tProcess);
 		}
 		
 		return new CreationResult(null, null);
 	}
 	
 	private static DirectDownGate searchForParallelPriorityGate(Multiplexer fn, DownGate gate)
 	{
 		Name remoteFN = gate.getRemoteDestinationName();
 		
 		// do we know the name of the next FN?
 		if(remoteFN != null) {
 			GateIterator iter = fn.getIterator(DirectDownGate.class);
 			while(iter.hasNext()) {
 				DirectDownGate currGate = (DirectDownGate) iter.next();
 				Name currRemoteFN = currGate.getRemoteDestinationName();
 				
 				// does the gate goes to the same remote FN?
 				if(remoteFN.equals(currRemoteFN)) {
 					// is it a priority only gate?
 					if(isPriorityOnly(currGate.getDescription())) {
 						return currGate;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	public void delNeighbor(NetworkInterface pInterface, NeighborInformation pNeighborLLID)
 	{
 		DirectDownGate gate;
 		GateContainer container = pInterface.getMultiplexerGate();
 		
 		mLogger.log(this, "Deleting DirectDownGate to neighbor " +pNeighborLLID +" on interface " +pInterface);
 		
 		synchronized(container) {	
 			do {
 				gate = checkDownGateAvailable(container, pNeighborLLID, null, null, null);
 				
 				if(gate != null) {
 					mNode.getTransferPlane().unregisterLink(container, gate);
 					gate.shutdown();
 					
 					container.unregisterGate(gate);
 				}
 			}
 			while(gate != null);
 		}
 	}
 	
 	public void repair()
 	{
 		mRepairCountdown = buslist.size();
 		for(NetworkInterface ni : buslist) {
 			ni.repair();
 		}
 	}
 	
 	/**
 	 * @return If the only non-functional requirement is a PriorityProperty (there is at least one priority property and no other non-functional one) 
 	 */
 	private static boolean isPriorityOnly(Description pRequirements)
 	{
 		boolean hasPriority = false;
 		
 		if(pRequirements != null) {	
 			for(Property prop : pRequirements) {
 				if(prop instanceof NonFunctionalRequirementsProperty) {
 					if(prop instanceof PriorityProperty) {
 						hasPriority = true;
 					} else {
 						if(!((NonFunctionalRequirementsProperty) prop).isBE()) {
 							// there is at least one non-func requ., which is not a PriorityProperty
 							return false;
 						}
 					}
 				}
 			}
 		}
 		
 		return hasPriority;
 	}
 	
 	public static boolean checkGateDescr(AbstractGate pGate, Description pRequirements)
 	{
 		if(pRequirements != null) {
 			Description tMyRequ = pGate.getDescription();
 			
 			if(tMyRequ != null) {
 				return tMyRequ.equals(pRequirements);
 			} else {
 				return pRequirements.isBestEffort();
 			}
 		} else {
 			return true;
 		}
 	}
 	
 	/**
 	 * Checks if a DownGate starting at pFN is going to pNeighborLLID.
 	 * 
 	 * @param pFN Staring FN
 	 * @param pNeighborLLID ID of the lower layer the gate is connected to
 	 * @param pDescription QoS description (== null, if no filtering for description)
 	 * @return Gate fitting the parameters
 	 */
 	public DirectDownGate checkDownGateAvailable(ForwardingNode pFN, NeighborInformation pNeighborLLID, GateID pReverseGateNumber, Description pDescription, ReroutingGate[] pBackupGate)
 	{
 		if((pFN != null) && (pNeighborLLID != null)) {
 			//
 			// Search for gate directly
 			//
 			Iterator<AbstractGate> tIter = pFN.getIterator(DirectDownGate.class);
 			boolean tSpecialCase = false;
 			
 			// In case of best-effort, both communication partners might try to setup
 			// gates at the same time. Thus, gate numbers are not known, but gates
 			// already established.
 			if(pDescription != null) {
 				if(pDescription.isBestEffort()) {
 					tSpecialCase = true;
 				}
 			}
 			
 			while(tIter.hasNext()) {
 				// type cast is valid, due to filter for iterator
 				DirectDownGate tGate = (DirectDownGate) tIter.next();
 				
 				if(pNeighborLLID.equals(tGate.getToLowerLayerID())) {
 					// do we filter for reverse gate number?
 					if(pReverseGateNumber != null) {
 						if(tSpecialCase || pReverseGateNumber.equals(tGate.getReverseGateID())) {
 							if(checkGateDescr(tGate, pDescription)) {
 								return tGate;
 							} else {
 								mLogger.err(this, "Gate number for gate '" +tGate +"' matches but description " +pDescription +" not. ");
 							}
 						}
 					} else {
 						if(checkGateDescr(tGate, pDescription)) {
 							return tGate;
 						}
 					}
 				}
 			}
 			
 			//
 			// Search for old gate
 			// Maybe there was a down gate and the node has some
 			// gates from repairing the down gate.
 			//
 			if(pBackupGate != null) {
 				if(pBackupGate.length > 0) {
 					pBackupGate[0] = null;
 					
 					tIter = pFN.getIterator(ReroutingGate.class);
 					
 					while(tIter.hasNext()) {
 						// type cast is valid, due to filter for iterator
 						ReroutingGate tGate = (ReroutingGate) tIter.next();
 						
 						if(tGate.match(pNeighborLLID, pReverseGateNumber, pDescription)) {
 							pBackupGate[0] = tGate;
 							return null;
 						}
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public int getNumberLowerLayers()
 	{
 		return buslist.size();
 	}
 	
 	public static Name generateRoutingServiceName()
 	{
 		sName++;
 		return new SimpleName(Node.NAMESPACE_HOST, "__" +sName);
 	}
 	
 	public enum RerouteMethod { LOCAL, GLOBAL, FROM_BROKEN };
 
 	public enum BrokenType { NODE, BUS, UNKNOWN };
 
 	/**
 	 * Initiates rerouting when broken node or bus is detected.
 	 * @param pType is the broken element a node or a bus?
 	 * @param pNetworkInterface network interface connected to the bus, the broken node is also connected to
 	 * @param pPacket packet to be delivered when broken node was detected
 	 * @param pFrom last used gate
 	 */
 	public void handleBrokenElement(BrokenType pType, NetworkInterface pNetworkInterface, Packet pPacket, DirectDownGate pFrom)
 	{
 		Config tConfig = mNode.getConfig();
 		RerouteMethod tRerouteMethod = tConfig.routing.REROUTE;
 		
 		if (pPacket.getData() instanceof ReroutingTestAgent) {
 			mLogger.debug(this, "This packet gets special treatment- extracted from packet, getting Reroute method");
 			tRerouteMethod = ((ReroutingTestAgent) pPacket.getData()).getRerouteMethod();
 		}
 
 		mLogger.info(this, "Rerouting method " +tRerouteMethod +" for failure in " +pType +" at " +pFrom +" for route " +pPacket);
 		
 		Route tOldRoute = pPacket.getRoute();
 		
 		// TODO try to repair it
 		//pPacket.returnRouteBroken();
 
 		// Restore gates used while sending the packet downwards
 		if(pPacket.getDownRoute() != null) {
 			tOldRoute.addFirst(pPacket.getDownRoute());
 			pPacket.clearDownRoute();
 		}
 		RoutingService tRs = mNode.getRoutingService();
 
 		try {
 			Description tRouteRequirements = pFrom.getDescription();
 			ProcessRerouting tProcess = null;
 
 			//
 			// Determine destination if rerouting from broken is active
 			//
 			Name destinationFromBroken = null;
 			int removeGatesFromRoute = 0;
 			if(tRerouteMethod == RerouteMethod.FROM_BROKEN) {
 				LinkedList<Name> tDestination = tRs.getIntermediateFNs(mNode.getCentralFN(), tOldRoute, true);
 				if(tDestination.size() >= 1) {
 					destinationFromBroken = tDestination.get(0);
 					removeGatesFromRoute = -1;
 				} else {
 					mLogger.warn(this, "Rerouting method " +tRerouteMethod +", but destination is not known for route " +tOldRoute +". Fallback to last known FN of route.");
 					
 					LinkedList<Name> tHops = tRs.getIntermediateFNs(mNode.getCentralFN(), tOldRoute, false);
 					if(tHops.size() > 1) {
 						destinationFromBroken = tHops.getLast();
 						removeGatesFromRoute = tHops.size() -2;
 					}
 				}
 			}
 			
 			//
 			// Determine destination for local repair,
 			// remove broken elements from routing and transfer, and
 			// create rerouting gates
 			//
 			if (pType == BrokenType.NODE) {
 				LinkedList<Name> tHops = tRs.getIntermediateFNs(mNode.getCentralFN(), tOldRoute, false);
 				// remove first FN on peer node; since node is broken, FN should be not available, too
 				if((tHops.size() >= 2)) {
 					tRs.reportError(tHops.get(1));
 				}
 				
 				if(tRerouteMethod == RerouteMethod.LOCAL) {
 					// try to get first FN after peer node
 					if(tHops.size() > 4) {
 						// ignore 1 local gate and 3 from the next node from route
 						destinationFromBroken = tHops.get(4);
 						removeGatesFromRoute = 3;
 					} else {
 						// not enough hops known for repairing; clear list for later checks
 						mLogger.err(this, "Can not determine at least 5 next FN where the route " +tOldRoute +" travels to.");
 						tHops.clear();
 					}
 				}
 				// else: FN not required or already determined
 				
 				// modify transfer service
 				if((tConfig.routing.REROUTE_USE_HORIZONTAL_GATES) && (tRerouteMethod != RerouteMethod.GLOBAL)) {
 					// setup backup gates with alternative routes
 					tProcess = new ProcessRerouting(pNetworkInterface, pFrom, removeGatesFromRoute, destinationFromBroken);
 					tProcess.start();
 				} else {
 					// do we send an error msg back?
 					if(tRerouteMethod == RerouteMethod.GLOBAL) {
 						pNetworkInterface.getMultiplexerGate().replaceGate(pFrom, createErrorReflectorGate());
 					} else {
 						// no backup, just remove old gate
 						pNetworkInterface.getMultiplexerGate().unregisterGate(pFrom);
 					}
 				}
 				
 				pFrom.shutdown();
 			}
 			else if (pType == BrokenType.BUS || pType == BrokenType.UNKNOWN) {
 				// modify transfer service
 				if((tConfig.routing.REROUTE_USE_HORIZONTAL_GATES) && (tRerouteMethod != RerouteMethod.GLOBAL)) {
 					// setup backup gates with alternative routes
 					tProcess = new ProcessRerouting(pNetworkInterface, pFrom, removeGatesFromRoute, destinationFromBroken);
 					tProcess.start();
 					
 					pFrom.shutdown();
 					// check whether we need to notify anyone about the loss of a neighbour connection
 					if (pFrom.getDescription() == null || pFrom.getDescription().isBestEffort()) {
 						// lost a best effort gate
 						mNode.getAS().getSimulation().publish(
 								new Gate.GateNotification(Gate.GateNotification.LOST_BE_GATE, pFrom.getRemoteDestinationName()));
 					}
 				} else {
 					// do we send an error msg back?
 					if(tRerouteMethod == RerouteMethod.GLOBAL) {
 						pNetworkInterface.getMultiplexerGate().replaceGate(pFrom, createErrorReflectorGate());
 						// check whether we need to notify anyone about the loss of a neighbour connection
 						if (pFrom.getDescription() == null || pFrom.getDescription().isBestEffort()) {
 							// lost a best effort gate
 							mNode.getAS().getSimulation().publish(
 									new Gate.GateNotification(Gate.GateNotification.LOST_BE_GATE, pFrom.getRemoteDestinationName()));
 						}
 					} else {
 						// if not, detach and remove all gates and FNs related to this interface
 						pNetworkInterface.detach();
 					
 						// try again to re-attach later on
 						pNetworkInterface.enableReattach();
 					}
 				}
 			}
 			
 			// Is one gate with requirements broken? Maybe the best effort does not work either.
 			if(tRouteRequirements != null) {
 				if(!tRouteRequirements.isBestEffort()) {
 					DownGate tBEGate = checkDownGateAvailable(pNetworkInterface.getMultiplexerGate(), pFrom.getToLowerLayerID(), null, Description.createBE(false), null);
 					
 					if(tBEGate != null) {
 						mLogger.log(this, "Gate with requirements failed. Check best effort gate " +tBEGate);
 						tBEGate.handlePacket(new Packet(null), null);
 					}
 				}
 			}
 			
 			if(tProcess != null) {
 				tProcess.signal();
 			}
 
 			// Send new packet along
 			mNode.getCentralFN().handlePacket(pPacket, null);
 		}
 		catch(Exception exc) {
 			mLogger.err(this, "Handle broken element failed at " + this, exc);
 			pPacket.droppingDetected(this);
 		}
 	}
 	
 	private ErrorReflectorGate createErrorReflectorGate()
 	{
 		ErrorReflectorGate tGate = new ErrorReflectorGate(mNode, null);
 		
 		tGate.initialise();
 		
 		return tGate;
 	}
 	
 	private static ForwardingNode[] fillList(boolean up, ForwardingNode pTarget, Word solution, Description pDescription, LinkedList<SocketPathParam> pList, ForwardingNode[] pFnArray)
 	{
 		Iterator<Variable> iterator;
 		Variable gateType = null;
 		ForwardingNode tCurrentFN = null;
 		ForwardingNode tBaseFN = null;
 		
 		
 		if(up) {	
 			tCurrentFN = pFnArray[0];
 			tBaseFN = pFnArray[0];
 			
 			iterator = solution.descendingIterator();
 			LinkedList<Variable> tReversalList = new LinkedList<Variable>();
 			
 			while(iterator.hasNext()) {
 				Variable tVar = iterator.next();
 				if(tVar.equals("*")) {
 					break;
 				} else {
 					tReversalList.addFirst(tVar);
 				}	
 			}
 			iterator = tReversalList.iterator();
 		} else {
 			tBaseFN = pFnArray[0];
 			tCurrentFN = pFnArray[1]; 
 			
 			iterator = solution.iterator();
 		}
 		//If Up reuse is always possible because the BaseFn euqals the actual Fn.
 		//For the DOwn Direction reuse is only possible if the last baseFn possible for reuse equals the actual Fn
 		boolean checkReUse = up;
 		
 		while(iterator.hasNext()) {
 			gateType = iterator.next();		
 			Iterator<AbstractGate> tGateIterator = tBaseFN.getIterator(null);
 			AbstractGate tGate = null;
 			boolean contains = false;
 			
 			// process list until the transport flag is reached
 			if(gateType.equals("*")) {
 				break;
 			} else {
 				ForwardingNode targetFN = pTarget;
 				HashMap<String, Serializable> config = null;
 				
 				Property prop = pDescription.get(gateType.getPropertyClass());
 				if((prop != null) && (prop instanceof FunctionalRequirementProperty)) {
 					if(up) {
 						config = ((FunctionalRequirementProperty) prop).getUpValueMap();
 					} else {
 						config = ((FunctionalRequirementProperty) prop).getDownValueMap();
 					}
 				}
 				
 				if(!up) targetFN = null;
 				SocketPathParam pathParam = null;
 				
 				if(tBaseFN.getNode().equals(tCurrentFN.getNode())) {
 					checkReUse = true;
 				} else {
 					checkReUse = false;
 				}
 				
 				// This is for the purpose to find out if a existing Gate could be re-used.
 				// Therefore String Operations are used.
 				if(checkReUse) {
 					while(tGateIterator.hasNext() && contains==false) {
 						tGate = tGateIterator.next();
 						String GateToString = tGate.toString();
 						String gateTypeToString = gateType.toString();
 						gateTypeToString = gateTypeToString.substring(0, gateTypeToString.indexOf("("));
 						contains = GateToString.contains(gateTypeToString);
 					}
 				} else {
 					contains = false;
 				}
 				
 				if(!contains || !Config.Connection.RE_USE_ACTIVATED) {
 					//create SocketPathParam for new Gate
 					pathParam = new SocketPathParam(null, targetFN, new GateClass(gateType.getName()), config); 
 					checkReUse = false;
 					tCurrentFN = (ForwardingNode) tGate.getNextNode();
 				} else {
 					//create SocketPathParam to re use a existing Gate
 					pathParam = new SocketPathParam(tGate.getGateID(), targetFN, new GateClass(gateType.getName()), config);
 					tBaseFN =  (ForwardingNode) tGate.getNextNode();
 					tCurrentFN = (ForwardingNode) tGate.getNextNode();
 					contains=false;
 				}
 				pList.addLast(pathParam);
 			}
 		}
 		
 		if(!up) {
 			if((pList.size() > 0) && (pTarget != null)) {
 				pList.getLast().updateTarget(pTarget); // name only the last one
 			}
 		}
 		ForwardingNode[] tFnArray = new ForwardingNode[2];
 		tFnArray[0] = tBaseFN;
 		tFnArray[1] = tCurrentFN;
 		return tFnArray;
 	}
 	
 	private void getIntermediateFuncRequ(Word pSolution, Description pDescrForSolution, Description pIntermRequ) throws PropertyException
 	{
 		Iterator<Variable> tIter = pSolution.iterator();
 		int tStars = 0;
 		LinkedList<Integer> tIntermediatePart = new LinkedList<Integer>();
 		Variable tGateType = null;
 		int i = 0;
 		
 		while(tIter.hasNext()) {
 			tGateType = tIter.next();
 		
 			if(tGateType.equals("*")) {
 				tStars++;
 			}
 			// note all intermediate parts between the first and second
 			// star to list for later processing
 			// TODO what if there are multiple stars (e.g. 3) in the list?
 			else if(tStars == 1) {
 				tIntermediatePart.addLast(i);
 			}
 			
 			i++;
 		}
 		
 		// Note: tStars < 2 => only two stacks without intermediate parts
 		if(tStars == 2) {
 			mLogger.log(this, tIntermediatePart.size() +" intermediate functions in " +pSolution);
 			
 			for(Integer varNo : tIntermediatePart) {
 				Property tRequ = pSolution.getRequirementFor(varNo, pDescrForSolution);
 				
 				if(tRequ != null) {
 					pIntermRequ.add(tRequ);
 				} else {
 					throw new PropertyException(this, "Can not find property for " +pSolution.get(varNo) +" in " +pDescrForSolution +".");
 				}
 			}
 		}
 		
 		if(tStars > 2) {
 			// see to do from above
 			throw new PropertyException(this, "More than two stars in solution '" +pSolution +"'; can not be handled by the controller.");
 		}
 	}
 	
 	/**
 	 * Maps requirements to gates for local sockets. In addition, required
 	 * functions for the intermediate connection are calculated.
 	 * 
 	 * @param pTargetNameDown
 	 * @param pDescription Overall requirements for whole connection
 	 * @param pListUp Result for up-part of stack (!= null)
 	 * @param pListDown Result for down-part of stack (!= null)
 	 * @return Result for intermediate route with missing requirements, which can not be handled locally.
 	 */
 	public Description deriveGatesFromRequirements(ForwardingNode pTargetNameDown, Description pDescription, LinkedList<SocketPathParam> pListUp, LinkedList<SocketPathParam> pListDown) throws NetworkException
 	{
 		Description tIntermediateRequirements;
 		
 		if(Config.Connection.USE_REQU_MAPPING_LANGUAGE) {
 			//
 			// CODE FROM REQU TO GATE MAPPING LANGUAGE
 			//
 			
 			RequirementsToGatesMapper mapper = RequirementsToGatesMapper.getInstance(mNode);
 			
 			Word solution = mapper.getSolutionFor(pDescription);
 			
 			if(solution != null) {
 				mLogger.info(this, "solution for " +pDescription +" = " +solution);
 				
 				ForwardingNode[] tTargetNameDownArray = new ForwardingNode[1];
 				tTargetNameDownArray[0] = pTargetNameDown;
 				
 				ForwardingNode[] tBaseFN = fillList(true, null, solution, pDescription, pListUp, tTargetNameDownArray);
 				fillList(false, pTargetNameDown, solution, pDescription, pListDown, tBaseFN);
 				
 				Iterator<SocketPathParam> tListUpIterator = pListUp.iterator();
 				Iterator<SocketPathParam> tListDownIterator = pListDown.descendingIterator();
 				
 				// Set local partnerparam's.
 				// Therefore process the two filled Lists ListUp and ListDown.
 				while(tListUpIterator.hasNext() && tListDownIterator.hasNext()) {
 					SocketPathParam tSPP = tListUpIterator.next();
 					SocketPathParam tSPPP = tListDownIterator.next();
 					tSPP.setLocalPartnerParam(tSPPP);
 					tSPPP.setLocalPartnerParam(tSPP);
 				}
 				
 				// At least the non-functional requirements are needed for setting
 				// up a connection. Additional functional requirements depend on
 				// the used mapping method.
 				tIntermediateRequirements = pDescription.getNonFunctional();
 				getIntermediateFuncRequ(solution, pDescription, tIntermediateRequirements);
 			} else {
 				throw new NetworkException(this, "Can not find gate setup for requirements " +pDescription);
 			}
 		} else {
 			//
 			// CODE FROM DIPLOMA THESIS
 			// TODO: at the moment only the client side is checked for capabilities, this prohibits server side single side gates 
 			//
 			IFunctionDescriptor tFuncUp = null;
 			IFunctionDescriptor tFuncDown = null;
 			HashMap<String, Serializable> tConfigUp = null;
 			HashMap<String, Serializable> tConfigDown = null;
 			SocketPathParam pathParamUp = null;
 			SocketPathParam pathParamDown = null;
 			Description capabilities = mNode.getCapabilities();
 			mLogger.trace(this, "Comparing required properties " + pDescription + " with following capabilities \"" + capabilities.toString() + "\"");
 			
 			tIntermediateRequirements = new Description();
 			
 			if(pDescription != null && !pDescription.isEmpty()) {
 				for(Property prop : pDescription) {
 					if(prop != null) {
 						if(prop instanceof FunctionalRequirementProperty) {
 							FunctionalRequirementProperty req = (FunctionalRequirementProperty) prop;
 							
 							// does the node support this feature?
 							if(capabilities.get(req.getClass()) != null) {	
 								mLogger.trace(this, "Property matches local capabilities");
 								IDirectionPair dirPair = req.getDirectionPair();
 								if(dirPair != null) {
 									tFuncUp = dirPair.getUpBehavior();
 									tFuncDown = dirPair.getDownBehavior();
 									tConfigUp = req.getUpValueMap();
 									tConfigDown = req.getDownValueMap();
 									if(tConfigUp != null && tConfigUp.isEmpty()) {
 										tConfigUp = null;
 									}
 									if(tConfigDown != null && tConfigDown.isEmpty()) {
 										tConfigDown = null;
 									}
 									pathParamUp = new SocketPathParam(null, null, tFuncUp, tConfigUp);
 									pathParamDown = new SocketPathParam(null, pTargetNameDown, tFuncDown, tConfigDown);
 									pathParamUp.setLocalPartnerParam(pathParamDown);
 									pathParamDown.setLocalPartnerParam(pathParamUp);
 									pListUp.addLast(pathParamUp);
 									pListDown.addFirst(pathParamDown);
 								}
 								pTargetNameDown = null;
 							} else {
 								// node does not support this requirement
 								// -> add it to intermediate requirements
 								tIntermediateRequirements.add(req);
 							}
 						} else {
 							if(prop instanceof NonFunctionalRequirementsProperty) {
 								// non-functional requirements
 								tIntermediateRequirements.add(prop);
 							} else {
 								// requirement not classified
 								// -> ignore it
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return tIntermediateRequirements;
 	}
 	
 	/**
 	 * Informs controller about the deletion of the node.
 	 * Cleans up everything in order to allow garb. collector to clean up.
 	 */
 	public void closed()
 	{
 		//
 		// Detach from all links/buses
 		//
 		while(!buslist.isEmpty()) {
 			NetworkInterface tInterface = buslist.removeFirst();
 			
 			if(tInterface != null) {
 				removeNetworkInterface(tInterface);
 			}
 		}
 	}
 	
 	/**
 	 * Updates capabilities in existing forwarding nodes.
 	 * (However, this function only updates the cap. of the central FN
 	 * instead of all FNs. Because either the central FN is able to provide
 	 * a special function or not FN on this node is able to do this.
 	 * 
 	 * @param pCapabilities New capability description for all FNs
 	 */
 	public void updateFNsCapabilties(Description pCapabilities) 
 	{
 		// only update RS entry for the central FN of the current node
 		mNode.getTransferPlane().updateNode(mNode.getCentralFN(), pCapabilities);
 	}
 
 	public String toString()
 	{
 		return "Controller@" +mNode;
 	}
 	
 	private class RepairEvent implements IEvent
 	{
 		public RepairEvent(NetworkInterface pInterface, NeighborInformation pNeighborLLID)
 		{
 			mInterface = pInterface;
 			mNeighborLLID = pNeighborLLID;
 		}
 		
 		@Override
 		public void fire()
 		{
 			mLogger.log(this, "Repair gates for interface " +mInterface);
 			
 			synchronized(mInterface.getNode()) {
 				// Search for rerouting gates and try to repair them
 				GateIterator tIter = mInterface.getMultiplexerGate().getIterator(ReroutingGate.class);
 				
 				while(tIter.hasNext()) {
 					// type cast is valid, due to filter for iterator
 					ReroutingGate tGate = (ReroutingGate) tIter.next();
 					
 					if(tGate.match(mNeighborLLID, null, null)) {
 						try {
 							createNewDownGate(mInterface.getMultiplexerGate(), mInterface, mNeighborLLID, tGate.getDescription(), tGate.getOwner(), tGate, true);
 						}
 						catch (NetworkException tExc) {
 							mLogger.warn(this, "Failed to repair " +tGate +".", tExc);
 						}
 					}
 				}
 			}
 		}
 		
 		private NetworkInterface mInterface;
 		private NeighborInformation mNeighborLLID;
 	}
 	
 	public void receivedOpenGateResponse()
 	{
 		if (mRepairCountdown != -1) {
 			mRepairCountdown--;
 			if (mRepairCountdown == 0) {
 				mRepairCountdown = -1;
 				mLogger.info(this, "All gates repaired.");
 				mNode.getAS().getSimulation().publish(new NodeUp(mNode.getAS().getName()+":"+mNode.toString()));
 			}
 		}
 	}
 	
 	private static int sName = 0;
 	
 	private Node mNode;
 	private Logger mLogger;
 	private final LinkedList<NetworkInterface> buslist = new LinkedList<NetworkInterface>();
 	private int mRepairCountdown = -1;
 }
 
