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
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.packets.Invisible;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.Signalling;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.Gate.GateState;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.GateIterator;
 import de.tuilmenau.ics.fog.transfer.gates.HorizontalGate;
 import de.tuilmenau.ics.fog.transfer.manager.Process;
 import de.tuilmenau.ics.fog.transfer.manager.ProcessConnection;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Viewable;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 /**
  * Forwarding marking the end of the transfer plane and having a link to a
  * higher layer connection end point.
  */
 public class ClientFN implements ForwardingNode
 {
 	public ClientFN(FoGEntity pEntity, Name pName, Name pServerName, Description pDescription, Identity pOwner)
 	{
 		mEntity = pEntity;
 		mName = pName;
 		mDescription = pDescription;
 		mOwner = pOwner;
 		
 		mServerName = pServerName;
 		
 		mEntity.getTransferPlane().registerNode(this, null, NamingLevel.NONE, pDescription);
 	}
 	
 	/**
 	 * There is only a single outgoing gate for ClientFN, since
 	 * they are the last element in the chain to the higher layer.
 	 */
 	@Override
 	public GateID registerGate(AbstractGate newgate)
 	{
 		unregisterGate(mOutgoingGate);
 		
 		if(newgate != null) {
 			GateID number = new GateID(getFreeGateNumber());
 			mOutgoingGate = newgate;
 			mOutgoingGate.setID(number);
 			
 			try {
 				mEntity.getTransferPlane().registerLink(this, mOutgoingGate);
 			}
 			catch (NetworkException exc) {
 				// since this link is not really useful for routing service,
 				// ignore this exception
 			}
 			mEntity.getLogger().log(this, "Outgoing " + newgate + " added");
 			return number;
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public boolean replaceGate(AbstractGate oldGate, AbstractGate byNewGate)
 	{
 		if(oldGate == mOutgoingGate) {
 			return registerGate(byNewGate) != null;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public GateIterator getIterator(Class<?> requestedGateClass)
 	{
 		if(mOutgoingGate != null) {
 			LinkedList<AbstractGate> container = new LinkedList<AbstractGate>();
 			container.add(mOutgoingGate);
 			return new GateIterator(container, requestedGateClass);
 		}
 		
 		return new GateIterator();
 	} 
 
 	@Override
	public boolean unregisterGate(AbstractGate oldgate)
 	{
 		if((mOutgoingGate == oldgate) && (mOutgoingGate != null)) {
 			if(mEntity.getTransferPlane() != null){
 				mEntity.getTransferPlane().unregisterLink(this, mOutgoingGate);
 			}
 			
 			mOutgoingGate.setID(null);
 			if(mOutgoingGate.getState() == GateState.START){
 				mOutgoingGate.initialise();
 			}
 			mOutgoingGate.shutdown();
 			mOutgoingGate = null;
 			mEntity.getLogger().log(this, "Outgoing " + oldgate + " released");
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * packets received from peer
 	 */
 	@Override
 	public void handlePacket(Packet packet, ForwardingElement lastHop)
 	{
 		// special handling for experiment packets
 		packet.forwarded(this);
 
 		// check if route is really empty
 		if(packet.fetchNextGateID() == null) {
 			final Object data = packet.getData();
 			
 			// normal data or a signaling message?
 			if(data instanceof Signalling) {
 				((Signalling) data).execute(this, packet);
 			}
 			else if(data instanceof Invisible) {
 				((Invisible) data).execute(this, packet);
 			}
 			else {
 				// deliver data to application
 				if(mCEP != null) {
 					mCEP.receive(data);
 				} else {
 					getLogger().warn(this, "Can not forward data '" +data +"' due to missing link to connection end point.");
 				}
 			}
 			packet.finished(this);
 		} else {
 			mEntity.getLogger().err(this, "Gate list was not finished. Packet " +packet +" dropped.");
 			packet.dropped(this);
 		}
 	}
 		
 	/**
 	 * Packets from CEP that should be send to peer
 	 */
 	public void send(Packet packet) throws NetworkException
 	{
 		packet.forwarded(this);
 		if(mOutgoingGate != null) {
 			if(packet.isInvisible()) {
 				Invisible data = (Invisible) packet.getData();
 				data.execute(this, packet);
 				data.execute(mOutgoingGate, packet);
 			}
 			if(packet.isSignalling()) {
 				mEntity.getNode().getAuthenticationService().sign(packet, getOwner());
 			}
 
 			packet.forwarded(mOutgoingGate);
 			if(Config.Connection.LOG_PACKET_STATIONS){
 				Logging.log(this, "Sending: " + packet);
 			}
 			mOutgoingGate.handlePacket(packet, this);
 		} else {
 			throw new NetworkException(this, "No connection to tranfer plane. Can not send data.");
 		}
 	}
 	
 	public void setPeerRoutingName(Name pPeerRoutingName)
 	{
 		mPeerRoutingName = pPeerRoutingName;
 	}
 	
 	public Name getPeerRoutingName()
 	{
 		return mPeerRoutingName;
 	}
 	
 	public Route getRoute()
 	{
 		if((mOutgoingGate != null) && (mOutgoingGate instanceof HorizontalGate)) {
 			return ((HorizontalGate) mOutgoingGate).getRoute();
 		} else {
 			return null;
 		}
 	}
 	
 	public void setRelatedProcess(ProcessConnection process)
 	{
 		mRelatedProcess = process;
 	}
 	
 	public Process getRelatedProcess()
 	{
 		return mRelatedProcess;
 	}
 
 	/**
 	 * Might be called recursively from Process.terminate or IReceiveCallback.closed
 	 */
 	public void closed()
 	{
 		if(!mClosing) {
 			mClosing = true;
 			
 			// remove outgoing gate
 			registerGate(null);
 			
 			if(mEntity.getTransferPlane() != null){
 				mEntity.getTransferPlane().unregisterNode(this);
 			}
 			
 			if(mCEP != null) {
 				mCEP.closed();
 				mCEP = null;
 			}
 			
 			// delete everything else related to the socket
 			if(mRelatedProcess != null) {
 				mRelatedProcess.terminate(null);
 				mRelatedProcess = null;
 			}
 		}
 	}
 	
 	public boolean isConnected()
 	{
 		if(mOutgoingGate != null) {
 			return (mOutgoingGate.getState() == GateState.OPERATE);
 		}
 
 		return false;
 	}
 	
 	public void setConnectionEndPoint(ConnectionEndPoint cep)
 	{
 		mCEP = cep;
 	}
 	
 	public ConnectionEndPoint getConnectionEndPoint()
 	{
 		return mCEP;
 	}
 	
 	@Override
 	public FoGEntity getEntity()
 	{
 		return mEntity;
 	}
 
 	@Override
 	public Description getDescription()
 	{
 		return mDescription;
 	}
 	
 	@Override
 	public Identity getOwner()
 	{
 		if(mOwner != null) return mOwner;
 		else return mEntity.getIdentity();
 	}
 	
 	@Override
 	public boolean isPrivateToTransfer()
 	{
 		// end point is always private for transfer service
 		return true;
 	}
 	
 	@Override
 	public synchronized int getFreeGateNumber()
 	{
 		return 1;	// There should only be one outgoing gate.
 	}
 	
 	@Override
 	public String toString()
 	{
 		if(mName != null)
 			return "ClientFN(" +mName +", peer=" + mPeerRoutingName + ")";
 		else
 			return "ClientFN(peer=" + mPeerRoutingName + ")";
 	}
 	
 	public Logger getLogger()
 	{
 		return mEntity.getLogger();
 	}
 	
 	private FoGEntity mEntity;
 	private Name mName;
 	private Description mDescription;
 	private AbstractGate mOutgoingGate;
 	
 	@Viewable("Peer routing name")
 	private Name mPeerRoutingName;
 	
 	@Viewable("Related process")
 	private ProcessConnection mRelatedProcess;
 	
 	@Viewable("Owner")
 	private Identity mOwner;
 	
 	@Viewable("Server name")
 	private Name mServerName;
 	
 	private ConnectionEndPoint mCEP;
 	private boolean mClosing;
 }
