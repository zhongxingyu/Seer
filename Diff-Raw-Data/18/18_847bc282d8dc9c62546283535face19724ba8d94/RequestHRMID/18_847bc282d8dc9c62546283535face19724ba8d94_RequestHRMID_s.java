 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical.addressing;
 
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * PACKET: This packet is used to request a formerly assigned address again. Such packets are sent from a a cluster members to the superior coordinator.
  */
 public class RequestHRMID  extends SignalingMessageHrm
 {
 	/**
 	 * For using the class within (de-)serialization processes.  
 	 */
 	private static final long serialVersionUID = 92265081176242872L;
 
 	/**
 	 * Stores the HRMID which was assign to the target of this packet.
 	 */
 	private HRMID mHRMID = new HRMID();
 
 	/**
 	 * Stores the counter of created packets from this type
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
	private static Long sCreatedPackets = new Long(0);
 
 	/**
 	 * Constructor for getDefaultSize()
 	 */
 	private RequestHRMID()
 	{
 		super();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 * @param pReceiverName the name of the message receiver
 	 * @param pAssignedHRMID the assigned new HRMID for the receiver
 	 */
 	public RequestHRMID(HRMName pSenderName, HRMName pReceiverName, HRMID pAssignedHRMID)
 	{
 		super(pSenderName, pReceiverName);
 		mHRMID = pAssignedHRMID;
 		synchronized (sCreatedPackets) {
 			sCreatedPackets++;
 		}
 	}
 	
 	/**
 	 * Returns the assigned new HRMID
 	 *  
 	 * @return the new assigned HRMID
 	 */
 	public HRMID getHRMID()
 	{
 		return mHRMID;
 	}
 	
 	/**
 	 * Returns the size of a serialized representation of this packet 
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.transfer.gates.headers.ProtocolHeader#getSerialisedSize()
 	 */
 	@Override
 	public int getSerialisedSize()
 	{
 		/*************************************************************
 		 * Size of serialized elements in [bytes]:
 		 * 
 		 * 		[MultiplexHeader]
 		 * 		[SignalingMessageHrm]
 		 * 		HRMID					= 4
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		tResult += getDefaultSize();
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the default size of this packet
 	 * 
 	 * @return the default size
 	 */
 	public static int getDefaultSize()
 	{
 		/*************************************************************
 		 * Size of serialized elements in [bytes]:
 		 * 
 		 * 		[MultiplexHeader]
 		 * 		[SignalingMessageHrm]
 		 * 		HRMID					= 4
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		RequestHRMID tTest = new RequestHRMID();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("Size of " + tTest.getClass().getSimpleName());
 		}
 		tResult += SignalingMessageHrm.getDefaultSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += tTest.mHRMID.getSerialisedSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the counter of created packets from this type
 	 *  
 	 * @return the packet counter
 	 */
 	public static long getCreatedPackets()
 	{
 		long tResult = 0;
 		
 		synchronized (sCreatedPackets) {
 			tResult = sCreatedPackets;
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "](Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ", requestedHRMID=" + getHRMID() + ")";
 	}
 }
