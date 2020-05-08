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
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 
 /**
  * PACKET: It is used to signal that a peer is still alive.
  * 		   The packet has to be send as broadcast.
  */
 public class BullyAlive extends SignalingMessageBully
 {
 	private static final long serialVersionUID = 4870662765189881992L;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 */
	public BullyAlive(Name pSenderName) //TODO: ev. sollte hier auch die Prio. bermittelt werden, so dass man nicht immer erst auf das BullyElect warten muss
 	{
 		super(pSenderName, HRMID.createBroadcast());
 	}
 
 	/**
 	 * Returns a describing string
 	 * 
 	 * @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "(Sender=" + getSenderName() + ", Receiver=" + getReceiverName() + ")";
 	}
 }
