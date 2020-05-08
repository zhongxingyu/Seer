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
 package de.tuilmenau.ics.fog.transfer.forwardingNodes;
 
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.FoGEntity;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.events.NewConnectionEvent;
 import de.tuilmenau.ics.fog.facade.properties.CommunicationTypeProperty;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.PleaseOpenConnection;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.EventSourceBase;
 
 
 /**
  * Forwarding node representing a name registration from a higher layer.
  * The name was given by the higher layer. Incoming connection request
  * had to be accepted by the higher layer. The description lists the
  * requirements of the server for connections.
  */
 public class ServerFN extends Multiplexer
 {
 	public ServerFN(FoGEntity entity, Name name, NamingLevel level, Description description, Identity identity)
 	{
 		super(entity, name, level, false, identity, entity.getController());
 		
 		this.description = description;
 		
 		binding = new BindingImpl();
 	}
 	
 	/**
 	 * Initializes forwarding node
 	 */
 	public void open()
 	{
 		mEntity.getTransferPlane().registerNode(this, mName, mLevel, description);
 	}
 
 	/**
 	 * @return Description of the server requirements for all communications
 	 */
 	public Description getDescription()
 	{
 		return description;
 	}
 	
 	@Override
 	protected void handleDataPacket(Packet packet)
 	{
 		if(packet.isTraceRouting()){
			Logging.warn(this, "TRACEROUTE-Processing packet: " + packet);
 		}
 
 		// are we allowed to open connections implicitly?
 		CommunicationTypeProperty type = null;
 		if(description != null) {
 			type = (CommunicationTypeProperty) description.get(CommunicationTypeProperty.class);
 		}
 		if(type == null) {
 			type = CommunicationTypeProperty.getDefault();
 		}
 		
 		if(!type.requiresSignaling()) {
 			PleaseOpenConnection artSigMsg = new PleaseOpenConnection(description);
 			
 			artSigMsg.setSendersRouteUpToHisClient(new Route());
 			artSigMsg.execute(this, packet);
 		} else {
 			mLogger.err(this, "Binding is not allowed to open connection implicitly due to type " +type);
 		}
 	}
 	
 	public Binding getBinding()
 	{
 		return binding;
 	}
 	
 	public void addNewConnection(Connection conn)
 	{
 		binding.addNewConnection(conn);
 	}
 
 	class BindingImpl extends EventSourceBase implements Binding
 	{
 
 		@Override
 		public synchronized Connection getIncomingConnection()
 		{
 			if(newConnections != null) {
 				if(!newConnections.isEmpty()) {
 					return newConnections.removeFirst();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		public synchronized int getNumberWaitingConnections()
 		{
 			if(newConnections != null) {
 				return newConnections.size();
 			} else {
 				return 0;
 			}
 		}
 
 		@Override
 		public Name getName()
 		{
 			return ServerFN.this.getName();
 		}
 
 		@Override
 		public void close()
 		{
 			ServerFN.this.close();
 		}
 		
 		public synchronized void addNewConnection(Connection conn)
 		{
 			if(newConnections == null) {
 				newConnections = new LinkedList<Connection>();
 			}
 			
 			newConnections.addLast(conn);
 			
 			// notify observer about new connection
 			notifyObservers(new NewConnectionEvent(this));
 		}
 		
 		private LinkedList<Connection> newConnections = null;
 	}
 
 	private Description description;
 	private BindingImpl binding;
 }
