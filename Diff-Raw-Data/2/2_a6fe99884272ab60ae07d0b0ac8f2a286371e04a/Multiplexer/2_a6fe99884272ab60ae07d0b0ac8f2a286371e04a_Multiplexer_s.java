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
 package de.tuilmenau.ics.fog.transfer.forwardingNodes;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.authentication.IdentityManagement;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.Invisible;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.Signalling;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.TransparentGate;
 import de.tuilmenau.ics.fog.transfer.manager.Controller;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.PacketLogger;
 import de.tuilmenau.ics.fog.ui.Viewable;
 
 
 /**
  * Forwards a message to the next gate based on the gate
  * list of the packet.
  * It implements the "forwarding node" functionality by
  * multiplexing between several registered gates.
  */
 public class Multiplexer extends GateContainer
 {
 	/**
 	 * Forwarding node in a FoG network.
 	 * 
 	 * @param node Node the FN is located on.
 	 * @param errorHandling Controller doing error handling for this FN (if null, the controller of the node is used)
 	 */
 	public Multiplexer(FoGEntity node, Controller errorHandling)
 	{
 		super(node, null, NamingLevel.NONE);
 		
 		mErrorGate = errorHandling;
 		if(mErrorGate == null) {
 			mErrorGate = node.getController();
 		}
 		
 		// TODO where to close it?
 		mPacketLog = PacketLogger.createLogger(mEntity.getTimeBase(), this, node);
 	}
 	
 	/**
 	 * Forwarding node in a FoG network.
 	 * 
 	 * @param node Node the FN is located on.
 	 * @param name Name of the FN (just for GUI and debugging reasons)
 	 * @param level Level of abstraction of name
 	 * @param owner Identity of the FN (optional; null if not available)
 	 * @param errorHandling Controller doing error handling for this FN (if null, the controller of the node is used)
 	 */
 	public Multiplexer(FoGEntity entity, Name name, NamingLevel level, boolean privateForTransfer, Identity owner, Controller errorHandling)
 	{
 		super(entity, name, level);
 		
 		mIsPrivate = privateForTransfer;
 		
 		mErrorGate = errorHandling;
 		if(mErrorGate == null) {
 			mErrorGate = entity.getController();
 		}
 		
 		mOwner = owner;
 		
 		// TODO where to close it?
 		mPacketLog = PacketLogger.createLogger(mEntity.getTimeBase(), this, entity);
 	}
 	
 	/**
 	 * Creates bi-directional connection between two multiplexer with
 	 * help of TransparentGates
 	 * 
 	 * @param pMux other multiplexer to connect to
 	 * @return true if connection had been established; false on error 
 	 */
 	public boolean connectMultiplexer(Multiplexer pMux)
 	{
 		TransparentGate toMux   = new TransparentGate(getEntity(), pMux);
 		TransparentGate fromMux = new TransparentGate(getEntity(), this);
 		
 		GateID toMuxGateNr   = registerGate(toMux);
 		GateID fromMuxGateNr = pMux.registerGate(fromMux);
 		
 		toMux.initialise();
 		fromMux.initialise();
 		
 		if((toMuxGateNr == null) || (fromMuxGateNr == null)) {
 			mLogger.err(this, "connecting to " +pMux +" failed because of missing gate numbers.");
 			
 			unregisterGate(toMux);
 			pMux.unregisterGate(fromMux);
 			return false;
 		} else {
 			toMux.setReverseGateID(fromMuxGateNr);
 			fromMux.setReverseGateID(toMuxGateNr);
 			return true;
 		}
 	}
 	
 	final public void handlePacket(Packet packet, ForwardingElement lastHop)
 	{
 		if(Config.Connection.LOG_PACKET_STATIONS){
 			Logging.log(this, "Forwarding: " + packet);
 		}
 
 		if(packet.isTraceRouting()){
 			Logging.log(this, "TRACEROUTE-Processing packet: " + packet);
 		}
 
 		// log packet for statistic
 		mPacketLog.add(packet);
 
 		packet.forwarded(this);
 		
 		// trace backward route
 		if((lastHop != null) && packet.traceBackwardRoute()) {
 			GateID tReturn = null;
 			
 			if(lastHop instanceof AbstractGate) {
 				tReturn = ((AbstractGate) lastHop).getReverseGateID();
 			}
 			
 			if(tReturn == null) {
 				tReturn = searchForGate(lastHop);
 			}
 			
 			if(tReturn != null) {
 				// e.g. for UpGates attached to a multiplexer
 				packet.addReturnRoute(tReturn);
 			} else {
 				/*
 				 * this is required in case a packet was emitted by a node
 				 */
 				if(!packet.getReturnRoute().isEmpty()) {
 					mLogger.warn(this, "Return route for " +packet +" broken at " +lastHop);
 					packet.returnRouteBroken();
 				}
 			}
 		}
 		
 		// add signature
 		// if packet already authenticated and multiplexer has the possibility to add a signature
 		if(packet.pleaseAuthenticate() && (mOwner != null)) {
 			IdentityManagement authService = mEntity.getAuthenticationService();
 			
 			// check, if the existing signatures are acceptable
 			if(authService.check(packet)) {
 				// create and add own signature
 				authService.sign(packet, mOwner);
 			} else {
 				mLogger.warn(this, "Signature not matching packet: " +packet);
 			}
 		}
 		
 		// handle invisible packets
 		boolean tInvisible = packet.isInvisible();
 		if(tInvisible) {
 			((Invisible) packet.getData()).execute(this, packet);
 		}
 		
 		// find next gate and delegate forwarding process to it
 		GateID tID = packet.fetchNextGateID();
 		
 		if(tID == null) {
 			if(packet.isTraceRouting()){
 				Logging.log(this, "TRACEROUTE-Forwarding to this FN, route=" + packet.getRoute() + ", the packet: " + packet);
 			}
 
 			mLogger.log(this, "Route of packet is : " + packet.getRoute());
 			if(packet.getRoute().isEmpty()) {
 				// end of gate list reached
 				// => packet is for this node
 				mLogger.log(this, "Packet is directed to this FN");
 				
 				handlePacket(packet);
 			} else {
 				// route not empty but no gate ID
 				// => check for incomplete route
 				//    even do so for invisible packets,
 				//    in order to deliver them to the
 				//    final goal
 				mErrorGate.incompleteRoute(packet, this);
 			}
 		} else {
 			// determine next gate
 			// => search gate and forward packet
 			AbstractGate tNext = getGate(tID);
 			
 			if(packet.isTraceRouting()){
				Logging.log(this, "TRACEROUTE-Forwarding to next gate: " + tNext + ",readyToReceive=" + tNext.isReadyToReceive() + ", the packet: " + packet);
 			}
 
 			// was ID valid?
 			if(tNext != null) {
 				// Call the handle method from the FN; so we do not have to
 				// do that in each gate implementation. Furthermore, we do
 				// not care about the state of the gate in order to mark the
 				// last (error) gate on the path.
 				if(tInvisible) {
 					((Invisible) packet.getData()).execute(tNext, packet);
 				}
 				
 				// is gate in correct state?
 				if(tNext.isReadyToReceive()) {
 					if (getEntity().getCentralFN() == this) {
 						packet.addToDownRoute(tNext.getGateID());
 					}
 					
 					packet.forwarded(tNext);
 					
 					if(packet.isTraceRouting()){
 						Logging.log(this, "TRACEROUTE-Forwarding to next FN the packet: " + packet);
 					}
 					tNext.handlePacket(packet, this);
 				} else {
 					if(packet.isTraceRouting()){
 						Logging.log(this, "TRACEROUTE-Invalid state for next gate: " + tNext + ", readyToReceive=" + tNext.isReadyToReceive() + ", the packet: " + packet);
 					}
 
 					if(!tInvisible) {
 						mErrorGate.invalidGateState(tNext, packet, this);
 					}
 					// else: suppress error handling
 				}
 			} else {
 				if(!tInvisible) {
 					mErrorGate.invalidGate(tID, packet, this);
 				}
 				// else: suppress error handling
 			}
 		}
 	}
 
 	/**
 	 * Method for handling packets, which are for the forwarding node itself.
 	 */
 	protected void handlePacket(Packet packet)
 	{
 		if(packet.isTraceRouting()){
 			Logging.log(this, "TRACEROUTE-Forwarding upwards the packet: " + packet);
 		}
 		
 		packet.logStats(getEntity().getNode().getAS().getSimulation());
 		
 		if(packet.getData() instanceof Signalling) {
 			Signalling tSig = (Signalling) packet.getData();
 			
 			mLogger.debug(this, "Executing signalling packet " +packet);
 			
 			boolean tRes = tSig.execute(this, packet);
 			
 			mLogger.trace(this, "Signalling packet " +packet +" execution result = " +tRes);
 		}
 		else if(packet.getData() instanceof Invisible) {
 			// ignore it; had been handled before
 			mLogger.trace(this, "Received invisible " + packet +" and ignoring it.");
 		}
 		else {
 			handleDataPacket(packet);
 		}
 	}
 	
 	/**
 	 * Called if a FN received data packets, which have to be forwarded to higher layer entities.
 	 * The implementation of this class just outputs an error.
 	 */
 	protected void handleDataPacket(Packet packet)
 	{
 		mLogger.warn(this, "Skip packet " +packet +" because socket towards app. " + getName().toString() + " is not valid (no signaling packet)");
 	}
 	
 	/**
 	 * @return Description of the server requirements for all communications
 	 */
 	public Description getDescription()
 	{
 		return getEntity().getNode().getCapabilities();
 	}
 
 	@Override
 	public String toString()
 	{
 		if (mName != null){
 			return getClass().getSimpleName() + "(" + mName + ")@" + mEntity;
 		}else{
 			return getClass().getSimpleName() + "@" + mEntity;
 		}
 	}
 	
 	@Override
 	public boolean isPrivateToTransfer()
 	{
 		return mIsPrivate;
 	}
 
 	@Override
 	public Identity getOwner()
 	{
 		if(mOwner != null) {
 			return mOwner;
 		} else {
 			return mEntity.getIdentity();
 		}
 	}
 	
 	private Controller mErrorGate = null;
 	private PacketLogger mPacketLog = null;
 	private boolean mIsPrivate = true;
 	
 	@Viewable("Owner")
 	private Identity mOwner = null;
 }
