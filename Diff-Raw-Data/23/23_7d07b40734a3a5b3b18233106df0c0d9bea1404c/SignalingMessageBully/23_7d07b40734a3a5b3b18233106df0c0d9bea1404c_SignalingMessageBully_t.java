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
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 
 public class SignalingMessageBully extends SignalingMessageHrm
 {
 	private static final long serialVersionUID = -7721094891385820251L;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 * @param pSenderPriority the Bully priority of the message sender
 	 */
 	public SignalingMessageBully(Name pSenderName, BullyPriority pSenderPriority)
 	{
 		mSenderName = pSenderName;
 		mSenderPriority = pSenderPriority;
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pSenderName the name of the message sender
 	 */
 	public SignalingMessageBully(Name pSenderName)
 	{
 		mSenderPriority = new BullyPriority(-1 /* some value to signal "invalid priority */);
 		mSenderName = pSenderName;
 	}
 
 	/**
 	 * Determine the name of the message sender
 	 * 
 	 * @return name of the sender
 	 */
 	public Name getSenderName()
 	{
 		return mSenderName;
 	}
 
 	/**
 	 * Determine the sender's priority.
 	 * 
 	 * @return the priority of the message sender
 	 */
 	public BullyPriority getSenderPriority()
 	{
 		return mSenderPriority;
 	}
 
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "(Sender=" + getSenderName() + ", SenderPrio=" + getSenderPriority().getValue() + ")";
 	}
 
 	/**
 	 * This is the Bully priority of the message sender.
 	 */
 	private BullyPriority mSenderPriority = null;
 	
 	/**
 	 * The name of the sender of this message. This is always a name of a physical node.
 	 */
 	private Name mSenderName = null;
 }
