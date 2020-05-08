 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 
 /**
  * This class stores the meta data about a packet.
  * It is used for packet storage within comm. channels
  */
 public class ComChannelPacketMetaData
 {
 	private boolean mSent = false;
	private SignalingMessageHrm mPacket = null;
 	private double mTimestamp = 0;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pPacket the packet
 	 * @param pWasSent the I/O direction
 	 */
	ComChannelPacketMetaData(SignalingMessageHrm pPacket, boolean pWasSent, double pTimestamp)
 	{
 		mPacket = pPacket;
 		mSent = pWasSent;
 		mTimestamp = ((double)Math.round(pTimestamp * 100)) / 100;
 	}
 	
 	/**
 	 * Returns if the packet was sent
 	 * 
 	 * @return true or false
 	 */
 	public boolean wasSent()
 	{
 		return mSent;
 	}
 	
 	/**
 	 * Returns if the packet was received
 	 * 
 	 * @return true or false
 	 */
 	public boolean wasReceived()
 	{
 		return !mSent;
 	}
 	
 	/**
 	 * Returns the timestamp of the packet
 	 * 
 	 * @return the timestamp
 	 */
 	public double getTimetstamp()
 	{
 		return mTimestamp;
 	}
 	
 	/**
 	 * Returns the packet
 	 * 
 	 * @return the packet
 	 */
	public SignalingMessageHrm getPacket()
 	{
 		return mPacket;
 	}
 }
