 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.election;
 
 import de.tuilmenau.ics.fog.facade.Name;
 
 /**
  * PACKET: It is used to signal that a peer is still alive.
  */
 public class BullyAlive extends SignalingBully
 {
 	private static final long serialVersionUID = 4870662765189881992L;
 	
 	/**
 	 * @param pSenderName the sender's name 
 	 * @param pCoordinatorName the coordinator's name
 	 */
	public BullyAlive(Name pSenderName)
 	{
 		super(pSenderName, -1 /* some value to signal "invalid priority */);
 	}
 	
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "(Sender=" + getSenderName() + ")";
 	}
 }
