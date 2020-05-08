 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical.topology;
 
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingTable;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * PACKET: This packet is used within the HRM "share" phase. 
  * 		   A coordinator uses this packet in order to share route with cluster members.
  */
 public class RouteShare extends SignalingMessageHrm
 {
 	private static final long serialVersionUID = 2105684166786450748L;
 	
 	/**
 	 * Stores the database with routing entries.
 	 */
 	private RoutingTable mRoutingTable = new RoutingTable();
 	
 	/**
 	 * Stores the counter of created packets from this type
 	 * This value is only used for debugging. It is not part of the HRM concept. 
 	 */
 	public static Long sCreatedPackets = new Long(0);
 
 	/**
 	 * Constructor for getDefaultSize()
 	 */
 	private RouteShare()
 	{
 		super();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 * @param pReceiverName the name of the message receiver
 	 * @param pRoutingTable the routing table which is shared
 	 */
 	public RouteShare(HRMName pSenderName, HRMName pReceiverName, RoutingTable pRoutingTable)
 	{
 		super(pSenderName, pReceiverName);
 		if(pRoutingTable != null){
 			mRoutingTable = pRoutingTable;
 		}
 		sCreatedPackets++;
 	}
 
 	/**
 	 * Returns the database of routing entries.
 	 * 
 	 * @return the database
 	 */
 	public RoutingTable getRoutes()
 	{
 		return (RoutingTable) mRoutingTable.clone();
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
 		 * 		SignalingMessageHRM	     = 1
		 * 		RoutingTable.length		 = 1
 		 * 		RoutingTable			 = dynamic
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		tResult += getDefaultSize();
		tResult += 1; // size of the following list
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += (mRoutingTable.size() * RoutingEntry.getDefaultSize());
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		
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
 		 * 		SignalingMessageHRM	     = 1
 		 * 
 		 *************************************************************/
 
 		int tResult = 0;
 		
 		RouteShare tTest = new RouteShare();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("Size of " + tTest.getClass().getSimpleName());
 		}
 		tResult += SignalingMessageHrm.getDefaultSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns if this packet type has a dynamic size
 	 * 
 	 * @return true or false
 	 */
 	public static boolean hasDynamicSize()
 	{
 		return true;
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
 		return getClass().getSimpleName() + "[" + getMessageNumber() + "](Sender=" + getSenderName() + ", Receiver=" + getReceiverName() + ", "+ mRoutingTable.size() + " shared routes)";
 	}
 }
