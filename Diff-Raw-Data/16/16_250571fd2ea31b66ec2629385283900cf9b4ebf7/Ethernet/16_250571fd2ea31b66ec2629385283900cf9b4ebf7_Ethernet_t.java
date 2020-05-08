 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - emulator interface
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.emulator;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.IEventRef;
import de.tuilmenau.ics.fog.application.util.LayerObserverCallback;
 import de.tuilmenau.ics.fog.emulator.Interface.ReceiveResult;
 import de.tuilmenau.ics.fog.emulator.ethernet.MACAddress;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.topology.ILowerLayerReceive;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.topology.NeighborList;
 import de.tuilmenau.ics.fog.topology.RemoteMedium;
 import de.tuilmenau.ics.fog.ui.Viewable;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 /**
  * Lower layer representing a link to a medium with non-local
  * neighbors. The layer is doing a neighbor discovery with a
  * simple Hello protocol and timeouts. For the transmission and
  * (de)serialization it uses an proxy object of the type
  * {@link Interface}.
  */
 public class Ethernet implements ILowerLayer, Runnable, IEvent
 {
 	private static final double TIMEOUT_SEC = 10;
 	private static final boolean EMULATE_BROKEN = false;
 	
 	
 	public Ethernet(AutonomousSystem pAS, String pName, String pInterfaceNameIn, String pInterfaceNameOut) throws NetworkException
 	{	
 		Logger tLogger = pAS.getLogger();
 		
 		mAS = pAS;
 		mName = pName;
 		mInterface = Interface.get(pInterfaceNameIn, pInterfaceNameOut, tLogger);
 		mLogger = new Logger(tLogger);
 		
 		new Thread(this).start();
 	}
 
 	@Override
 	public boolean isBroken()
 	{
 		return mBroken;
 	}
 	
 	@Override
 	public void setBroken(boolean pBroken, boolean pErrorTypeVisible)
 	{
 		mBroken = pBroken;
 	}
 	
 	@Override
 	public synchronized NeighborInformation attach(String name, ILowerLayerReceive receivingNode)
 	{
 		if(mHigherLayer != null) {
 			throw new RuntimeException(this +" - Can not attach " +receivingNode +" because there is already " +mHigherLayer +" registered.");
 		} else {
 			mHigherLayerName = name;
 			mHigherLayer = receivingNode;
 			
 			mMe = new NeighborInformation(mHigherLayerName, mInterface.getAddress());
 			mTimer = mAS.getTimeBase().scheduleIn(0, this);
 		}
 
 		return mMe;
 	}
 	
 	@Override
 	public synchronized SendResult sendPacketTo(NeighborInformation destination, Packet packet, NeighborInformation from)
 	{
 		if(!mBroken) {
 			if(mMe != null) {
 				if(mMe.equals(from)) {
 					Serializable llName = destination.getLLName();
 					
 					if(llName instanceof MACAddress) {
 						if(EMULATE_BROKEN) {
 							// Check, if neighbor is known. If not, it or the link might be broken 
 							if(searchNeighbor((MACAddress)llName) == null) {
 								return SendResult.LOWER_LAYER_BROKEN;
 							}
 						}
 						
 						try {
 							mLogger.trace(this, "Sending packet " +packet +" to " +destination);
 							
 							mInterface.send((MACAddress) llName, packet);
 							return SendResult.OK;
 						}
 						catch(IOException exc) {
 							mLogger.err(this, "Can not send packet " +packet, exc);
 						}
 					} else {
 						mLogger.err(this, "Invalid neighbor information " +llName);
 					}
 				} else {
 					mLogger.err(this, "From parameter " +from +" does not match my description " +mMe +".");
 				}
 			} else {
 				mLogger.warn(this, "Someone tries to send a packet, but nobody is attached.");
 			}
 		} else {
 			// we are broken; simulate error 
 			return SendResult.LOWER_LAYER_BROKEN;
 		}
 		
 		return SendResult.UNKNOWN_ERROR;
 	}
 	
 	@Override
 	public synchronized void detach(ILowerLayerReceive receivingNode)
 	{
 		if(mHigherLayer == receivingNode) {
 			mHigherLayerName = null;
 			mHigherLayer = null;
 			mMe = null;
 			
 			mAS.getTimeBase().cancelEvent(mTimer);
 			mTimer = null;
 		}
 	}
 
 	@Override
 	public NeighborList getNeighbors(NeighborInformation forMe)
 	{
 		return mNeighbors;
 	}
 
 	@Override
	public void registerObserverNeighborList(LayerObserverCallback observer)
 	{
 		if(!observerList.contains(observer)) observerList.add(observer);
 	}
 
 	@Override
	public boolean unregisterObserverNeighborList(LayerObserverCallback observer)
 	{
 		return observerList.remove(observer);	
 	}
 	
 	@Override
 	public String getName() 
 	{
 		return mName;
 	}
 
 	@Override
 	public Logger getLogger()
 	{
 		return mLogger;
 	}
 	
 	@Override
 	public Description getDescription()
 	{
 		return null; // TODO is lossy; what about bandwidth and delay?
 	}
 
 	@Override
 	public void modifyBandwidth(int bandwidthModification)
 	{
 		throw new RuntimeException(this +" - Resource reservations not supported.");
 	}
 
 	@Override
 	public String getASName() 
 	{
 		return mAS.getName();
 	}
 
 	@Override
 	public void close()
 	{
 		if(mInterface != null) {
 			mInterface.close();
 			mInterface = null;
 		}
 		
 		if(mTimer != null) {
 			
 		}
 	}
 	
 	@Override
 	public RemoteMedium getProxy()
 	{
 		// Ethernet object can not be shared with remote computers
 		return null;
 	}
 
 	@Override
 	public void fire()
 	{
 		if(mHigherLayer != null) {
 			mAS.getTimeBase().scheduleIn(TIMEOUT_SEC / 3.0d, this);
 			
 			try {
 				sendHello();
 			}
 			catch(Exception exc) {
 				mLogger.err(this, "Can not send hello.", exc);
 			}
 			
 			checkForTimeouts();
 		}
 	}
 	
 	@Override
 	public void run()
 	{
 		while(mInterface != null) {
 			receivedPacket();
 		}
 	}
 	
 	/**
 	 * Receiving a single packet from Ethernet.
 	 */
 	private void receivedPacket()
 	{
 		try {
 			ReceiveResult tRes = mInterface.receive();
 			
 			if(tRes != null) {
 				if(!mBroken) {
 					if(tRes.data instanceof NeighborInformation) {
 						helloReceived((NeighborInformation) tRes.data);
 					}
 					else if(tRes.data instanceof Packet) {
 						mLogger.trace(this, "Received " +tRes.data +" from " +tRes.source);
 						
 						NeighborInformation tSource = getNeighbor(tRes.source);
 						
 						if(mHigherLayer != null) {
 							try {
 								mHigherLayer.handlePacket((Packet) tRes.data, tSource);
 							}
 							catch (RemoteException tExc) {
 								mLogger.err(this, "Higher layer is not reachable." , tExc);
 							}
 						}
 						// else: ignore packet
 					}
 					else {
 						mLogger.err(this, "Received data of unknown type " +tRes.data +" from " +tRes.source);
 					}
 				} else {
 					mLogger.trace(this, "Received " +tRes.data +" from " +tRes.source +" in broken state. Ignoring packet.");
 				}
 			}
 		}
 		catch(Exception tExc) {
 			mLogger.err(this, "Can not receive data. Ignoring one packet.", tExc);
 		}
 	}
 	
 	@Override
 	public String toString()
 	{
 		return getName() +"(" +mMe +")";
 	}
 	
 	private void sendHello() throws IOException
 	{
 		if(!mBroken) {
 			mInterface.send(null, mMe);
 		}
 	}
 	
 	private synchronized NeighborInformation searchNeighbor(MACAddress pAddress)
 	{
 		for(NeighborInformation tNeighbor : mNeighbors) {
 			if(tNeighbor.getLLName().equals(pAddress)) {
 				// ok, we know the neighbor already -> store receive time
 				return tNeighbor;
 			}
 		}
 		
 		return null;
 	}
 	
 	private synchronized NeighborInformation getNeighbor(MACAddress pAddress)
 	{
 		NeighborInformation tNeighbor = searchNeighbor(pAddress);
 		
 		if(tNeighbor == null) {
 			// hm, neighbor not known
 			// => we missed a hello message
 			tNeighbor = new NeighborInformation(null, pAddress);
 			helloReceived(tNeighbor);
 		}
 		
 		return tNeighbor;
 	}
 	
 	private synchronized void helloReceived(NeighborInformation pNeighbor)
 	{
 		if(pNeighbor != null) {
 			mLogger.trace(this, "Hello packet received from neighbor, HL is " + pNeighbor.getHLName() + ", LL is " + pNeighbor.getLLName());
 			for(NeighborInformation tNeighbor : mNeighbors) {
 				if(tNeighbor.equals(pNeighbor)) {
 					// ok, we know the neighbor already -> store receive time
 					mNeighborTimes.put(tNeighbor, mAS.getTimeBase().now());
 					return;
 				}
 			}
 			
 			// we reach here, if the neighbor is not known
 			// -> add a new neighbor
 			mNeighbors.add(pNeighbor);
 			mNeighborTimes.put(pNeighbor, mAS.getTimeBase().now());
 			
 			// inform observer about new neighbor
			for(LayerObserverCallback obs : observerList) {
 				try {
 					obs.neighborDiscovered(pNeighbor);
 				}
 				catch(Exception tExc) {
 					// ignore exceptions; just report them
 					mLogger.err(this, "Ignoring exception from observer " +obs +" while hello", tExc);
 				}
 			}
 		}
 	}
 	
 	private synchronized void checkForTimeouts()
 	{
 		double tMinTime = mAS.getTimeBase().now() -TIMEOUT_SEC;
 		
 		for(NeighborInformation tNeighbor : mNeighbors) {
 			double tLastHelloTime = mNeighborTimes.get(tNeighbor);
 			
 			if(tLastHelloTime < tMinTime) {
 				// neighbor timed out and is maybe not reachable any more
 				// -> remove it from list
 				mNeighbors.remove(tNeighbor);
 				mNeighborTimes.remove(tNeighbor);
 				
 				if(!EMULATE_BROKEN) {
 					// inform observer about deleted neighbor
					for(LayerObserverCallback obs : observerList) {
 						try {
 							obs.neighborDisappeared(tNeighbor);
 						}
 						catch(Exception tExc) {
 							// ignore exceptions; just report them
 							mLogger.err(this, "Ignoring exception from observer " +obs +" while timeout: " +tExc);
 						}
 					}
 				}
 				// else: feedback is given in sendPacketTo
 				
 				// iterator is now invalid; wait for next
 				// check for checking the rest of the list
 				return;
 			}
 		}
 	}
 	
 	private AutonomousSystem mAS;
 	private Logger mLogger;
 	private String mName;
 	@Viewable(value="Broken")
 	private boolean mBroken = false;
	private LinkedList<LayerObserverCallback> observerList = new LinkedList<LayerObserverCallback>();
 	
 	private String mHigherLayerName;
 	private ILowerLayerReceive mHigherLayer;
 	
 	@Viewable(value="Interface")
 	private Interface mInterface;
 	@Viewable(value="Me")
 	private NeighborInformation mMe;
 	private NeighborList mNeighbors = new NeighborList(null);
 	private HashMap<NeighborInformation, Double> mNeighborTimes = new HashMap<NeighborInformation, Double>();
 	private IEventRef mTimer;
 
 }
