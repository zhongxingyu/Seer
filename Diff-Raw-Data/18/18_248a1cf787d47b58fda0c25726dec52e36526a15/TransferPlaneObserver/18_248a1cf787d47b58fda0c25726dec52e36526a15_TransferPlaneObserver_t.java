 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.transfer;
 
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 
 
 /**
  * Interface for notification about transfer plane changes of a local host.
  */
 public interface TransferPlaneObserver
 {
 	/**
 	 * Two level of naming:
 	 * - NAMES: FN provides direct access to the named service
 	 * - ANNOUNCES: FN is entry point for going to the named service
 	 * - NONE: FN is not related to any named service
 	 */
 	public enum NamingLevel { NAMES, ANNOUNCES, NONE };
 	
 	/**
 	 * Registration method for registering a forwarding node. If
 	 * a name is given, the forwarding node will be available under
 	 * this name.
 	 * If the forwarding node is already known, only the name will
 	 * be registered.
 	 * 
 	 * @param pElement Forwarding node to register at routing service
 	 * @param pName Name for the forwarding node (null, if no name available)
 	 * @param pLevel Level of abstraction for the naming 
 	 * @param pDescription Requirements description for a connection to this node
 	 */
 	public void registerNode(ForwardingNode pElement, Name pName, NamingLevel pLevel, Description pDescription);
 	
 	/**
 	 * Update method for updating the capabilities of a forwarding node. 
 	 * 
 	 * @param pElement Forwarding node to register at routing service
 	 * @param pCapabilities Capabilities of this forwarding node
 	 */
 	public void updateNode(ForwardingNode pElement, Description pCapabilities);
 	
 	/**
 	 * Removes a node and all its names and all links to it from topology.
 	 *  
 	 * @param pElement node to be removed
 	 * @return true==successful; false==node not known by routing service
 	 */
 	public boolean unregisterNode(ForwardingNode pElement);
 	
 	/**
 	 * Announces a link between two forwarding nodes.
 	 * If no remote name is given, it announces a local route between two
 	 * forwarding notes on a single node. If a remote name is specified,
 	 * the name identifies a remote forwarding node on another host. The
 	 * routing service will use the name in order to link both.
 	 * 
 	 * @param pFrom Starting forwarding node of the link
 	 * @param pGate Gate with valid gate number. Target node information must be valid, if no remote name is given
 	 * @throws NetworkException If remote node name is not known.
 	 */
 	public void registerLink(ForwardingElement pFrom, AbstractGate pGate) throws NetworkException;
 	
 	/**
 	 * Unregister a link from a forwarding node.
 	 * 
	 * @param pFrom Forwarding node, from which the gate is starting.
 	 * @param pGate Gate to delete
 	 * @return true==success; false==link was not known
 	 */
	public boolean unregisterLink(ForwardingElement pFrom, AbstractGate pGate); 
 }
 
