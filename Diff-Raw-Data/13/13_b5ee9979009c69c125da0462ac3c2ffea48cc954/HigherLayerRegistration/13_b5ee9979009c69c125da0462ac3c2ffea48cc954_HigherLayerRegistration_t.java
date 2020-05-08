 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Bus
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
 package de.tuilmenau.ics.fog.bus;
 
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.EventHandler;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.Config.Simulator.SimulatorMode;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.topology.Breakable.Status;
 import de.tuilmenau.ics.fog.topology.ILowerLayerReceive;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.RateLimitedAction;
 import de.tuilmenau.ics.fog.util.RateMeasurement;
 
 
 /**
  * Represents a registration of a higher layer at a lower layer.
  * It stores packets, which should be processed by this higher
  * layer. The method deliverPackets must be called periodically
  * in order to deliver this packets.
  */
 public class HigherLayerRegistration extends RateLimitedAction<Packet>
 {
 	private static final int MAX_NO_DELIVERED_PACKETS_PER_STEP = 1000;
 	private static final long MAX_PACKET_DELIVER_DURATION_MSEC = 100;
 	private static final double MAX_PACKET_DURATION_WARN_RATE_MSG_PER_SEC = 0.5d;
 	
 	
 	public HigherLayerRegistration(EventHandler timeBase, RateMeasurement measurement, Logger logger, String hlName, int llName, ILowerLayerReceive hl)
 	{
 		super(timeBase, MAX_PACKET_DURATION_WARN_RATE_MSG_PER_SEC);
 		
 		mNeighbor = new NeighborInformation(hlName, llName);
 		mHL = hl;
 		mLogger = logger;
 		mDatarateMeasurement = measurement;
 	}
 	
 	private class PacketDeliveryEvent implements IEvent
 	{
 		public PacketDeliveryEvent(Envelope packet)
 		{
 			this.packet = packet;
 		}
 
 		@Override
 		public void fire()
 		{
 			if(Config.Connection.LOG_PACKET_STATIONS){
 				Logging.log(this, "Got FIRE delivery event for: " + packet.mPacket);
 			}
 
 			deliverPacket(packet, getEventHandler().now());
 		}
 		
 		public String toString()
 		{
 			return getClass().getSimpleName() + " for packet: " + packet.mPacket;
 		}
 		
 		private Envelope packet;
 	}
 	
 	/**
 	 * Stores an incoming packet in its queue. The content is not copied.
 	 * 
 	 * @param packet packet, which should be stored for delivery
 	 * @return Result of sending operation
 	 */
 	public Status storePacket(Envelope packet)
 	{
 		if(packet != null) {
 			try {
 				Status tStatus = mHL.isBroken();
 				
 				if(tStatus == Status.OK) {
 					if(Config.Connection.LOG_PACKET_STATIONS){
 						Logging.log(this, "Sheduling delivery of: " + packet.mPacket + ", delay: "+ packet.mDeliverDuration);
 					}
 
 					//
 					// handling via event queue
 					// Note: It is important to sort packets with same time
 					//       in order of arrival. Otherwise higher layer
 					//       protocols gets problems with packets not in
 					//       order. Therefore, use ">" and not ">="!
 					//
 					getEventHandler().scheduleIn(packet.mDeliverDuration, new PacketDeliveryEvent(packet));
 				}else{
 					if(Config.Connection.LOG_PACKET_STATIONS){
 						Logging.log(this, "Sheduling delivery of: " + packet + " CANCELD, bus state: " + tStatus);
 					}
 				}
 
 				return tStatus;
 			} catch (RemoteException tExc) {
 				// remote call failed
 				// -> we will assume that the node is really broken
 				return Status.UNKNOWN_ERROR;
 			}
 		} else {
 			return Status.OK;
 		}
 	}
 	
 	/**
 	 * Delivers packets from queue to higher layer. This method copies
 	 * packets.
 	 * 
 	 * @return number of delivered packets
 	 */
 	public int deliverPackets()
 	{
 		int counter = 0;
 		
 		if(packetlist != null) {
 			while(!packetlist.isEmpty() && (counter < MAX_NO_DELIVERED_PACKETS_PER_STEP)) {
 				boolean deliver = true;
 				double now = getEventHandler().now();
 				
 				synchronized (packetlist) {
 					Envelope packet = packetlist.getFirst();
 					deliver = (packet.mTimeToDeliver <= now);
 				}
 
 				if(deliver) {
 					Envelope packet = null;
 					synchronized (packetlist) {
 						packet = packetlist.removeFirst();
 					}
 					
 					if(deliverPacket(packet, now)) {
 						counter++;
 					}
 				} else {
 					// first packet needs some more time for delay
 					// skip this queue and try something else
 					break;
 				}
 			}
 		}
 		
 		return counter;
 	}
 	
 	private boolean deliverPacket(Envelope packet, double now)
 	{
 		// calculate difference between scheduled time and actual delivery time
 		double delayMSec = (packet.mTimeToDeliver -now) *1000.0d;
 		
 		if(Config.Connection.LOG_PACKET_STATIONS){
 			Logging.log(this, "Delivering: " + packet + ", delay: "+ packet.mDeliverDuration + ", now: " + now);
 		}
 
 		if(Config.Transfer.DEBUG_PACKETS) {
 			mLogger.debug(this, "deliver " +packet.mPacket +" from " +packet.mFrom +" to " +mHL +" (delay [msec] = " +Math.round(delayMSec) +")");
 		}
 		
 		try {
 			if(mDatarateMeasurement != null) {
 				mDatarateMeasurement.write(packet.mPacket.getSerialisedSize());
 			}
 			
 			long time = System.currentTimeMillis();
 			mHL.handlePacket(packet.mPacket.clone(), packet.mFrom);
 			lastPacketDurationMSec = System.currentTimeMillis() -time;
 			
 			if(Config.Transfer.DEBUG_PACKETS) {
 				mLogger.debug(this, "Handling packet via " + mHL +" took " +lastPacketDurationMSec +" msec");
 			}
 			
 			// emit waring if a packet takes to much time to deliver
 			if(lastPacketDurationMSec > MAX_PACKET_DELIVER_DURATION_MSEC) {
 				trigger(packet.mPacket);
 			}
 		}
 		catch(RemoteException tExc) {
 			// ignore exceptions; just report them
 			mLogger.err(this, "Ignoring remote exception from higher layer " +mHL +" for packet '" +packet.mPacket, tExc);
 			return false;
 		}
 		catch (Exception Exc) {
 			mLogger.err(this, "Ignoring exception caused by higher layer", Exc);
 			return false;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * @return Information about attached entity (!= null)
 	 */
 	public NeighborInformation getNeighbor()
 	{
 		return mNeighbor;
 	}
 	
 	public ILowerLayerReceive getLowerLayerReceive()
 	{
 		return mHL;
 	}
 	
 	public boolean is(ILowerLayerReceive higherLayer)
 	{
 		// do check with equals in order to be prepared for RMI objects
 		return mHL.equals(higherLayer);
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "HigherLayerRegistration(" +mNeighbor +")";
 	}
 	
 	@Override
 	protected void doAction(Packet packet)
 	{
 		// ignore this warning in batch mode, since the duration of the handle
 		// packet method is not important in this non-real-time environment
 		if(Config.Simulator.MODE != SimulatorMode.FAST_SIM) {
			if(Config.Transfer.DEBUG_PACKET_TIMINGS){
				mLogger.warn(this, "Packet " +packet +" took " +lastPacketDurationMSec +" msec at higher layer " +mHL);
			}
 		}
 	}
 
 	
 	private NeighborInformation mNeighbor;
 	private Logger mLogger;
 	private LinkedList<Envelope> packetlist;	
 	private ILowerLayerReceive mHL;
 	private long lastPacketDurationMSec = -1;
 	private RateMeasurement mDatarateMeasurement;
 }
