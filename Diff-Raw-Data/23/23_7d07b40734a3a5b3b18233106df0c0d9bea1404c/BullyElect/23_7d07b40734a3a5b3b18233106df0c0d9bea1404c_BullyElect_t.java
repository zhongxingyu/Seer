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
  * PACKET: It is used when an election start is signaled.
  */
 public class BullyElect extends SignalingMessageBully //TV
 {
 	private static final long serialVersionUID = -335936730603961378L;
 
 	/**
 	 * 
 	 * @param pSenderName sender's identification
 	 * @param pSenderPriority senders priority for the election
 	 * @param pHierarchyLevel hierarchy level for this election
 	 */
 	public BullyElect(Name pSenderName, BullyPriority pSenderPriority, int pHierarchyLevel)
 	{
 		super(pSenderName, pSenderPriority);
 		mHierarchyLevel = pHierarchyLevel;
 	}
 	
 
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "(Sender=" + getSenderName() + ", SenderPrio=" + getSenderPriority().getValue() + ", Level=" + mHierarchyLevel + ")";
 	}
 
 	
 	/**
 	 * The hierarchy level for this election.
 	 */
 	private int mHierarchyLevel = 0;
 }
