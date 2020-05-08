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
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 
 /**
  * PACKET: It is used when an answer to BullyElect is signaled.
  */
 public class BullyReply  extends SignalingMessageBully
 {
 	private static final long serialVersionUID = -4666721123778977947L;
 	
 	/**
 	 * 
 	 * @param pSenderName sender's identification
 	 * @param pSenderPriority senders priority for the election
 	 */
 	public BullyReply(Name pSenderName, BullyPriority pSenderPriority)
 	{
 		super(pSenderName, pSenderPriority);
 	}
 	
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "(Sender=" + getSenderName() + ", SenderPrio=" + getSenderPriority() + ")";
 	}
 }
