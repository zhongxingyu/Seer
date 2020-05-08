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
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMSignature;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 
 /**
  * PACKET: It is used when a new coordinator is signaled to all cluster members
  */
 public class BullyAnnounce extends SignalingMessageBully
 {
 	private static final long serialVersionUID = 794175467972815277L;
 	/**
 	 * @param pCoord Name (IName) of the coordinator that is announcing itself
 	 * @param pSenderPriority is the priority the coordinator was elected with
 	 * @param pCoordinatorSignature is the signature of the coordinator - can be replaced by cryptographic identity
 	 * @param pToken is the active token that is used for the identification of the domain the coordinator is active in case no Cluster IDs can be provided a priori
 	 */
 	public BullyAnnounce(Name pSenderName, BullyPriority pSenderPriority, HRMSignature pCoordinatorSignature, int pToken)
 	{
 		super(pSenderName, pSenderPriority);
 		mCoordSignature = pCoordinatorSignature;
 		mToken = pToken;
 	}
 	
 	/**
 	 * 
 	 * @return the active token that is used for the identification of the domain the coordinator is active in case no Cluster IDs can be provided a priori
 	 */
 	public int getToken()
 	{
 		return mToken;
 	}
 
 	@Override
 	public String toString()
 	{
		return getClass().getSimpleName() + "(Sender=" + getSenderName() + ", SenderPrio=" + getSenderPriority().getValue() + ")";
 	}
 	
 	/**
 	 * 
 	 * @return the signature of the coordinator - can be replaced by cryptographic identity
 	 */
 	public HRMSignature getCoordSignature()
 	{
 		return mCoordSignature;
 	}
 	
 	/**
 	 * 
 	 * @param pName is one further node that is covered by the coordinator that created this message
 	 */
 	public void addCoveredNode(Name pName)
 	{
 		if(mCoveredNodes == null) {
 			mCoveredNodes = new LinkedList<Name>();
 		}
 		mCoveredNodes.add(pName);
 	}
 	
 	/**
 	 * 
 	 * @return the nodes that are covered by the coordinator that sent this message
 	 */
 	public LinkedList<Name> getCoveredNodes()
 	{
 		return mCoveredNodes;
 	}
 	
 	private HRMSignature mCoordSignature;
 	private int mToken;
 	private LinkedList<Name> mCoveredNodes = null;
 }
