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
 package de.tuilmenau.ics.fog.topology;
 
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.application.util.LayerObserverCallback;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.topology.ILowerLayer.SendResult;
 import de.tuilmenau.ics.fog.transfer.DummyForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.GateContainer;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.Multiplexer;
 import de.tuilmenau.ics.fog.transfer.gates.DirectDownGate;
 import de.tuilmenau.ics.fog.transfer.gates.DownGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.GateIterator;
 import de.tuilmenau.ics.fog.transfer.gates.LowerLayerReceiveGate;
 import de.tuilmenau.ics.fog.transfer.manager.Controller;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 public class NetworkInterface implements LayerObserverCallback
 {
 	private static final double REATTACH_TIMER_SEC = 10.0d;
 	
 	
 	public NetworkInterface(ILowerLayer pLowerLayer, FoGEntity pEntity)
 	{
 		mEntity = pEntity;
 		mLowerLayer = pLowerLayer;
 		mAttached = false;
 		
 		if(mLowerLayer instanceof ForwardingElement) {
 			mProxyForLL = (ForwardingElement) mLowerLayer;
 		} else {
 			mProxyForLL = new DummyForwardingElement(mLowerLayer);
 		}
 	}
 	
 	/**
 	 * Attaches network interface to lower layer and
 	 * creates all needed gates.
 	 * 
 	 * @return if attach operation was successfully or not
 	 */
 	public boolean attach()
 	{
 		// use receive gate as indicator, whether or not we are connected 
 		if(!mAttached) {
 			mAttached = true;
 			
 			// create forwarding node if it does not exist from previous attach operations
 			// and perform a (re-)open.
 			if(mMultiplexer == null) {
 				Name tFNName = null;
 				if(!Config.Routing.REDUCE_NUMBER_FNS) {
 					tFNName = Controller.generateRoutingServiceName();
 				}
 				mMultiplexer = new Multiplexer(mEntity, tFNName, NamingLevel.NAMES, false, getEntity().getIdentity(), mEntity.getController());
 			}
 			mMultiplexer.open();
 			
 			// create gates
 			mReceiveGate = new LowerLayerReceiveGate(mEntity, this);
 					
 			try {
 				// synch is needed for blocking access to mLowerLayerID, which
 				// might be needed for signaling purposes in a parallel thread
 				synchronized(this) {
 					// Register for msg from bus
 					mLowerLayerID = mLowerLayer.attach(null, mReceiveGate);
 				}
 				
 				// now we might have an ID. Therefore the gate is inserted in the plane.
 				mReceiveGate.initialise();
 				
 				if(mLowerLayerID != null) {
 					// inform RS about uplink (optional)
 					mEntity.getTransferPlane().registerLink(mProxyForLL, mReceiveGate);
 					
 					// link central multiplexer with multiplexer of interface
 					mMultiplexer.connectMultiplexer(mEntity.getCentralFN());
 				
 					// Register for updates of neighbors in range of lower layer
 					mLowerLayer.registerObserverNeighborList(this);
 					
 					// Look for already known neighbors
 					updateNeighbors();
 				} else {
 					if(mDownGates.size() > 0){
 						mEntity.getLogger().err(this, "Did not get valid lower layer ID from " +mLowerLayer);
 					}
 					detach();
 					return false;
 				}
 			}
 			catch(Exception tExc) {
 				mEntity.getLogger().err(this, "Can not attach to lower layer " +mLowerLayer, tExc);
 				detach();
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Check if there are already known neighbors available.
 	 * It asks the lower layer for a list of known neighbors
 	 * and informs the controller about each.
 	 * 
 	 * @return If it was able to retrieve neighbors from LL
 	 */
 	public boolean updateNeighbors()
 	{
 		try {
 			NeighborList neighbors = mLowerLayer.getNeighbors(mLowerLayerID);
 			
 			// go through list and run discover
 			if(neighbors != null) {
 				for(NeighborInformation neighbor : neighbors) {
 					neighborDiscovered(neighbor);
 				}
 				
 				return true;
 			} else {
 				return false;
 			}
 		} catch (RemoteException exc) {
 			mEntity.getLogger().err(this, "Ignoring remote exception during updating of lower layer neighbors from '" +mLowerLayer +"'.", exc);
 			return false;
 		}
 	}
 	
 	@Override
 	public void neighborDiscovered(NeighborInformation newNeighbor)
 	{
 		if(mLowerLayerID != null) {
 			mEntity.getController().addNeighbor(this, newNeighbor);
 		} else {
 			// maybe we forgot to unregister the observer of the lower layer? 
 			mEntity.getLogger().err(this, "Neighbor discovered was called, but interface is not connected. Ignoring call.");
 		}
 	}
 	
 	@Override
 	public void neighborDisappeared(NeighborInformation oldNeighbor)
 	{
 		if(mLowerLayerID != null) {
 			mEntity.getController().delNeighbor(this, oldNeighbor);
 		} else {
 			// maybe we forgot to unregister the observer of the lower layer? 
 			mEntity.getLogger().err(this, "Neighbor disappeared was called, but interface is not connected. Ignoring call.");
 		}
 	}
 	
 	@Override
 	public void neighborCheck()
 	{
 		updateNeighbors();
 	}
 
 	/**
 	 * Detaches network interface from the lower layer and
 	 * removes all gates attached to it.
 	 */
 	public void detach()
 	{
 		Logging.log(this, "Detaching..");
 		
 		synchronized (mMultiplexer) {
 			if(mAttached) {
 				mAttached = false;
 				
 				// unregister from observer list
 				try {
 					mLowerLayer.unregisterObserverNeighborList(this);
 				} catch (RemoteException tExc) {
 					mEntity.getLogger().err(this, "Error while unregistering from observer list.", tExc);
 				}
 				
 				// close all down gates for this interface
 				synchronized (mDownGates) {
 					for(DirectDownGate tGate : mDownGates) {
 						mMultiplexer.unregisterGate(tGate);
 						tGate.shutdown();
 					}
 					mDownGates.clear();
 				}
 				
 				// Unregister for msg from bus
 				if(mEntity != null){
 					if (mEntity.getTransferPlane() != null){
 						mEntity.getTransferPlane().unregisterLink(mProxyForLL, mReceiveGate);
 					}
 					try {
 						mLowerLayer.detach(mReceiveGate);
 					}
 					catch(RemoteException tExc) {
 						mEntity.getLogger().warn(this, "Ignoring remote exception during detaching from lower layer.", tExc);
 					}
 					// do not inform receive gate about closing
 					// (this would result in an calling of detach
 					// again)
 					
 					// unlink central multiplexer with multiplexer of interface
 					mEntity.getCentralFN().unregisterGatesTo(mMultiplexer);
 					mMultiplexer.unregisterGatesTo(mEntity.getCentralFN());
 				}
 				mMultiplexer.close();
 				
 				// invalidating gates
 				if(mReceiveGate != null) {
 					mReceiveGate.shutdown();
 					mReceiveGate = null;
 				}
 		
 				mLowerLayerID = null;
 			}
 		}
 	}
 	
 	/**
 	 * Is used to re-install the network interface in the routing service
 	 * after a broken time of the node (NOT the link!). Furthermore, it
 	 * tries to re-establish the gates to its neighbors. Both might have
 	 * been deleted during the broken time of the node.
 	 */
 	public void repair()
 	{
 		if(mAttached) {
 			// previous attach operation failed? 
 			if(mLowerLayerID == null) {
 				attach();
 			} else {
 				// just reinstall routing service stuff
 				mMultiplexer.open();
 				
 				// re-link central multiplexer with multiplexer of interface
 				mEntity.getCentralFN().unregisterGatesTo(mMultiplexer);
 				mMultiplexer.unregisterGatesTo(mEntity.getCentralFN());
 
 				mMultiplexer.connectMultiplexer(mEntity.getCentralFN());
 				
 				// refresh neighbors
 				updateNeighbors();
 			}
 			// else: not attached, nothing to repair
 		}
 	}
 	
 	public void enableReattach()
 	{
 		mEntity.getTimeBase().scheduleIn(REATTACH_TIMER_SEC, new IEvent() {
 			@Override
 			public void fire()
 			{
 				if(mAttached) {
 					// we are attached => check neighbors
 					if(!updateNeighbors()) {
 						mEntity.getTimeBase().scheduleIn(REATTACH_TIMER_SEC, this);
 					}
 				} else {
 					// we are not attached => try to attach again
 					if(!attach()) {
 						//Logging.warn(this, "triggering re-attaching of this network interface");
 						mEntity.getTimeBase().scheduleIn(REATTACH_TIMER_SEC, this);
 					}
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Called by the receive gate in order to signal the removing
 	 * of the lower layer from the simulation. 
 	 */
 	public void remove()
 	{
 		if(mAttached) {
 			if(mEntity != null) {
 				mEntity.getNode().detach(mLowerLayer);
 			}
 			
 			// invalidate link to lower layer
 			mLowerLayer = null;
 		}
 		// otherwise: we are not attached or we are currently detaching
 	}
 	
	public NeighborInformation getLowerLayerID()
 	{
 		return mLowerLayerID;
 	}
 	
 	/**
 	 * @return multiplexer, where received packets will be forwarded to 
 	 */
 	public GateContainer getMultiplexerGate()
 	{
 		return mMultiplexer;
 	}
 	
 	public FoGEntity getEntity()
 	{
 		return mEntity;
 	}
 	
 	public ILowerLayer getBus()
 	{
 		return mLowerLayer;
 	}
 
 	/**
 	 * Method for GUI purposes! It returns a proxy object
 	 * for the lower layer, which can be used for GUI
 	 * purposes.
 	 */
 	public ForwardingElement getLowerLayerGUIRepresentation()
 	{
 		return mProxyForLL;
 	}
 
 	/**
 	 * This method should be used for sending packets to somebody on the bus
 	 * (1) It is used directly for signaling stuff.
 	 * (2) It is used by DownGate
 	 */
 	public SendResult sendPacketTo(NeighborInformation destination, Packet packet, ForwardingElement from)
 	{
 		SendResult res;
 		
 		// TODO check, where the clearDownRoute really has to be called.
 		//packet.clearDownRoute();
 		try {
 			res = mLowerLayer.sendPacketTo(destination, packet, mLowerLayerID);
 		}
 		catch(RemoteException tExc) {
 			mEntity.getLogger().warn(this, "Remote exception during sending operation => lower layer broken", tExc);
 			res = SendResult.LOWER_LAYER_BROKEN;
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Search for DownGate, which is sending packets to a peer defined by
 	 * lower layer id.
 	 * 
 	 * @return ID or null, if not available
 	 */
 	public GateID searchForGateTo(NeighborInformation lowerLayerID)
 	{
 		if(lowerLayerID != null) {
 			synchronized (mDownGates) {
 				for(DirectDownGate tGate : mDownGates) {
 					if(lowerLayerID.equals(tGate.getToLowerLayerID()))
 						return tGate.getGateID();
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * DownGates will automatically (in constructor) register at their
 	 * interface. Registration is needed for backward route trace.
 	 */
 	public void attachDownGate(DirectDownGate gate)
 	{
 		synchronized (mDownGates) {
 			mDownGates.add(gate);
 		}
 	}
 	
 	/**
 	 * Used to update description of best effort gates after a change of
 	 * the capacity of the lower layer.
 	 */
 	public void refreshGates()
 	{
 		synchronized (mMultiplexer) {
 			GateIterator iter = mMultiplexer.getIterator(DownGate.class);
 			while(iter.hasNext()) {
 				DownGate gate = (DownGate) iter.next(); // valid cast due to iterator filter
 				
 				if(gate.refreshDescription()) {
 					// something changed -> inform routing service
 					try {
 						gate.getEntity().getTransferPlane().registerLink(mMultiplexer, gate);
 					}
 					catch (NetworkException exc) {
 						mEntity.getLogger().warn(this, "Can not update description of gate " +gate +" in routing service.", exc);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public String toString()
 	{
 		return "NI:" +mLowerLayerID +"@" +mLowerLayer;
 	}
 
 
 	private FoGEntity mEntity;
 	private ILowerLayer mLowerLayer;
 	private NeighborInformation mLowerLayerID;
 	private ForwardingElement mProxyForLL;
 	private LowerLayerReceiveGate mReceiveGate;
 	private Multiplexer mMultiplexer;
 	private LinkedList<DirectDownGate> mDownGates = new LinkedList<DirectDownGate>();
 	
 	/**
 	 * Attach is not checked by checking references, because references
 	 * need to valid during detach operation. Explicit boolean prevents
 	 * cascading detach operations. 
 	 */
 	private boolean mAttached;
 
 }
 
