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
 
 import de.tuilmenau.ics.fog.packets.hierarchical.MultiplexHeader;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.ClusterName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
 * This abstract interface allows the receiver to detect the sender of a message.
  */
public class SignalingMessageHrmTopologyUpdate extends SignalingMessageHrm implements IEthernetPayload
 {
 	/**
 	 * Stores the ClusterName of the sender.
 	 */
 	protected ClusterName mSenderEntityName = new ClusterName(null, null, null, 0);
 
 	/**
 	 * Stores the L2 address of the node where the coordinator of the announced cluster is located
 	 */
 	private L2Address mSendingEntityNodeL2Address = new L2Address(0);
 
 	private static final long serialVersionUID = 6551744707863660735L;
 
 	/**
 	 * Constructor for getDefaultSize()
 	 */
 	protected SignalingMessageHrmTopologyUpdate()
 	{
 		super();
 	}
 	
 	/**
 	 * Constructor 
 	 * 
 	 * @param pSenderName the name of the sender
 	 * @param pReceiverName the name of the receiver
 	 */
 	public SignalingMessageHrmTopologyUpdate(HRMName pSenderName, HRMName pReceiverName)
 	{
 		super(pSenderName, pReceiverName);
 	}
 
 	/**
 	 * Returns the ClusterName of the sender
 	 * 
 	 * @return the ClusterName of the sender
 	 */
 	public ClusterName getSenderEntityName()
 	{
 		return mSenderEntityName;
 	}
 
 	/**
 	 * Sets the name of the sender of the message
 	 * 
 	 *  @param pSenderEntityName the name of the sender of the message
 	 */
 	public void setSenderEntityName(ClusterName pSenderEntityName)
 	{
 		mSenderEntityName = pSenderEntityName;
 	}
 	
 	/**
 	 * Sets the node L2Address of the sender of the message
 	 * 
 	 * @param pNodeL2Address the L2Address of the sender node 
 	 */
 	public void setSenderEntityNodeL2Address(L2Address pNodeL2Address)
 	{
 		mSendingEntityNodeL2Address = pNodeL2Address;
 	}
 	
 	/**
 	 * Returns the L2 address of the node where the coordinator of the announced cluster is located
 	 * 
 	 * @return the L2 address
 	 */
 	public L2Address getSenderEntityNodeL2Address()
 	{
 		return mSendingEntityNodeL2Address;
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
 		 * 		SendingEntityNodeL2Address 	= 16
 		 * 		SenderEntityName		 	= 9
 		 * 
 		 *************************************************************/
 
 		//HINT: the packet type of this election message is derived based on the packet type field from SignalingMessageHRM
 		
 		return getDefaultSize();
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
 		 * 		SendingEntityNodeL2Address 	= 16
 		 * 		SenderEntityName		 	= 9
 		 * 
 		 *************************************************************/
 
		//HINT: the packet type of this election message is derived based on the packet type field from SignalingMessageHRM
 
 		int tResult = 0;
 		
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("Size of SignalingMessageHrmTopologyUpdate");
 		}
 		tResult += SignalingMessageHrm.getDefaultSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += L2Address.getDefaultSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		tResult += ClusterName.getDefaultSize();
 		if(HRMConfig.DebugOutput.GUI_SHOW_PACKET_SIZE_CALCULATIONS){
 			Logging.log("   ..resulting size: " + tResult);
 		}
 		
 		// correct the calculation because SignalingMessageHrm::getDefaultSize() adds too much overhead
 		tResult -= MultiplexHeader.getDefaultSize();
 		
 		return tResult;
 	}
 
 }
