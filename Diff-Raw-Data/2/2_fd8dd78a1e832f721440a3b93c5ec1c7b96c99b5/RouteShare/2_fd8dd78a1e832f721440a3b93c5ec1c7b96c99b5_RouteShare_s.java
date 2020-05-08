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
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingEntry;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingTable;
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
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 * @param pReceiverName the name of the message receiver
 	 * @param pRoutingTable the routing table which is shared
 	 */
 	public RouteShare(Name pSenderName, Name pReceiverName, RoutingTable pRoutingTable)
 	{
 		super(pSenderName, pReceiverName);
 		if(pRoutingTable != null){
 			mRoutingTable = pRoutingTable;
 		}
 	}
 
 	/**
 	 * Adds a route to the database of routing entries.
 	 * 
 	 * @param pRoutingEntry the new route
 	 */
 	public void addRoute(RoutingEntry pRoutingEntry)
 	{
 		if (HRMConfig.DebugOutput.SHOW_SHARE_PHASE){
 			Logging.log(this, "Adding routing entry: " + pRoutingEntry);
 		}
 		
 		if (mRoutingTable.contains(pRoutingEntry)){
 			Logging.err(this, "Duplicated entries detected, skipping this \"addRoute\" request");
 			return;
 		}
 		
 		mRoutingTable.add(pRoutingEntry);
 	}
 	
 	/**
 	 * Returns the database of routing entries.
 	 * 
 	 * @return the database
 	 */
 	public RoutingTable getRoutes()
 	{
		return mRoutingTable;
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
