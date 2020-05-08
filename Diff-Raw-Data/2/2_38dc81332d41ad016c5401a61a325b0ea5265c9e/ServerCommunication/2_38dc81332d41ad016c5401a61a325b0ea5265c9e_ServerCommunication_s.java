 /*******************************************************************************
  * Copyright (C) 2012 Henrik Voß und Sven Nobis
  * 
  * This file is part of DSI8AndroidCommunicationLibrary
  * (https://github.com/SvenTo/DSI8AndroidCommunicationLibrary)
  * 
  * DSI8AndroidCommunicationLibrary is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  ******************************************************************************/
 package de.dsi8.dsi8acl.communication.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.dsi8.dsi8acl.communication.contract.ICommunicationPartner;
 import de.dsi8.dsi8acl.communication.contract.ICommunicationPartnerListener;
 import de.dsi8.dsi8acl.communication.contract.IServerCommunication;
 import de.dsi8.dsi8acl.communication.contract.IServerCommunicationListener;
 import de.dsi8.dsi8acl.connection.contract.IConnector;
 import de.dsi8.dsi8acl.connection.contract.IConnectorListener;
 import de.dsi8.dsi8acl.connection.contract.IRemoteConnection;
 import de.dsi8.dsi8acl.connection.model.Message;
 import de.dsi8.dsi8acl.exception.ConnectionProblemException;
 
 public class ServerCommunication implements IServerCommunication, ICommunicationPartnerListener {
 	
 	private List<ICommunicationPartner> partners = new ArrayList<ICommunicationPartner>();
 	private final int maxPlayers;
 	private IConnector connector;
 	private final IServerCommunicationListener listener;
 	
 	public ServerCommunication(IServerCommunicationListener listener, IConnector connector, int maxPlayer) {
 		// TODO: Throw Argument Exception if connector is null?
 		this.listener = listener;
 		this.maxPlayers = maxPlayer; 
 	}
 	
 	public void startListen() {
 		if(!hasMaxPlayersConnected()) {
 			connector.listen(); // TODO: add return?
 		}
 	}
 	
 	public void close() {
 		for(ICommunicationPartner partner : partners) {
 			partner.close();
 		}
 		connector.cancel();
 	}
 	
 	public void sendMessageToAll(Message msg) {
 		sendMessageToAll(msg, -1, null);
 	}
 	
 	public void sendMessageToAll(Message msg, CommunicationPartner ignore) {
 		sendMessageToAll(msg, -1, ignore);
 	}
 	
 	public void sendMessageToAll(Message msg, int state) {
 		sendMessageToAll(msg, state, null);
 	}
 	
 	public void sendMessageToAll(Message msg, int state, CommunicationPartner ignore) {
 		for(ICommunicationPartner partner : partners) {
 			if(partner != ignore &&
 			   (state == -1 || state == partner.getState())) {
 				partner.sendMessage(msg);
 			}
 		}
 	}
 
 	@Override
 	public void connectionLost(CommunicationPartner partner,
 			ConnectionProblemException ex) {
 		partners.remove(partner);
 		listener.connectionLost(partner, ex);
 		
 		startListen(); // TODO: correct?
 	}
 	
 	private final IConnectorListener socketConnectorListener = new IConnectorListener() {
 
 		@Override
 		public boolean connectionEstablished(IRemoteConnection connection) {
 			ICommunicationPartner partner = new CommunicationPartner(ServerCommunication.this,
 																	 connection);
 			partners.add(partner);
 			listener.newPartner(partner);
 			partner.initialized();
 			
 			return !hasMaxPlayersConnected();
 		}
 
 		@Override
 		public boolean error(Exception ex) {
 			listener.socketListenerProblem(ex);
 			
 			return false; // TODO: Let the listener deceide 
 		}
 		
 	};
 
 	private ICommunicationPartner getCommunicationPartner(int id) {
 		for (ICommunicationPartner partner : this.partners) {
 			if (partner.getId() == id) {
 				return partner;
 			}
 		}
 		// TODO: Throw Exception
 		return null;
 	}
 	
 	@Override
 	public void sendMessage(int comId, Message msg) {
 		ICommunicationPartner communicationPartner = getCommunicationPartner(comId);
 		if (communicationPartner != null) {
 			communicationPartner.sendMessage(msg);
 		}
 	}
 	
 	private boolean hasMaxPlayersConnected() {
 		return (partners.size() >= maxPlayers);
 	}
 }
