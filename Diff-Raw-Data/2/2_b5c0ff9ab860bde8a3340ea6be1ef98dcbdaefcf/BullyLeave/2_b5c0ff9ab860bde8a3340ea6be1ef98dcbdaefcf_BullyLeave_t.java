 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical.election;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 
 /**
  * PACKET: This packet is used to inform the superior coordinator that a cluster member will leave the group.
  * 		   Alternatively, the coordinator could wait until the BullyAlive is missing. However, with this packet the convergence time is shortened.
  */
 public class BullyLeave extends SignalingMessageBully
 {
 
 	private static final long serialVersionUID = 7774205916502000178L;
 
 	/**
	 * Constructor
	 * 
 	 * @param pSenderName
 	 * @param pReceiverName
 	 */
 	public BullyLeave(Name pSenderName, BullyPriority pSenderPriority)
 	{
 		super(pSenderName, HRMID.createBroadcast(), pSenderPriority);
 	}
 
 }
