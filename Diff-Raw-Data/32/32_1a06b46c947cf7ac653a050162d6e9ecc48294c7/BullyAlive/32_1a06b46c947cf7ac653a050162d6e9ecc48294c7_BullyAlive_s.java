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
	public BullyAlive(Name pSenderName, Name pCoordinatorName)
 	{
 		super(pSenderName, -1 /* some value to signal "invalid priority */);
		mCoordinator = pCoordinatorName;
	}
	
	/**
	 * Determine the name of the source coordinator
	 * 
	 * @return name of the coordinator that
	 */
	//TODO: not used until now, however, the function is maybe used in the future when BullyAlive will be parsed by the main packet processing CoordinatorCEPDemultiplexed
	public Name getCoordinatorName()
	{
		return mCoordinator;
 	}
 	
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "(Sender=" + getSenderName() + ", Coordinator=" + mCoordinator + ")";
 	}
	
	// ########################################################################################################
	/**
	 * coordinator name
	 */
	private Name mCoordinator = null;
 }
