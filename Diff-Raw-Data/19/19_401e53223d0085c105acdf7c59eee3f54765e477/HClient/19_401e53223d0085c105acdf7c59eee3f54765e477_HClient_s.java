 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.hubiquitus.hapi.client;
 
 import java.util.GregorianCalendar;
 import java.util.Random;
 
 import org.hubiquitus.hapi.hStructures.ConnectionError;
 import org.hubiquitus.hapi.hStructures.ConnectionStatus;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HResult;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.hubiquitus.hapi.structures.JabberID;
 import org.hubiquitus.hapi.transport.HTransport;
 import org.hubiquitus.hapi.transport.HTransportCallback;
 import org.hubiquitus.hapi.transport.HTransportOptions;
 import org.hubiquitus.hapi.transport.socketio.HTransportSocketio;
 import org.hubiquitus.hapi.transport.xmpp.HTransportXMPP;
 import org.hubiquitus.hapi.util.HUtil;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * @author j.desousag
  * @version 0.3
  * Hubiquitus client, public api
  */
 
 public class HClient {
 	
 	private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED; /* only connecting, connected, diconnecting, disconnected */
 	@SuppressWarnings("unused")
 	private HOptions options = null;
 	private HTransportOptions transportOptions = null;
 	private HCallback callback = null;
 	private HTransport transport;
 	
 	private TransportCallback transportCallback = new TransportCallback();
 	
 	public HClient() {
 		transportOptions = new HTransportOptions();
 	}
 
 	/**
 	 * Connect to server
 	 * 
 	 * @param publisher - user jid (ie : my_user@domain/resource)
 	 * @param password
 	 * @param callback - client callback to get api notifications
 	 * @param options
 	 */
 	public void connect(String publisher, String password, HCallback callback, HOptions options) {
 		boolean shouldConnect = false;
 		boolean connInProgress = false;
 		boolean disconInProgress = false;
 
 		//synchronize connection status updates to make sure, we have one connect at a time
 		synchronized (this) {
 			if (this.connectionStatus == ConnectionStatus.DISCONNECTED) {
 				shouldConnect = true;
 				
 				//update connection status
 				connectionStatus = ConnectionStatus.CONNECTING;
 			} else if(this.connectionStatus == ConnectionStatus.CONNECTING) {
 				connInProgress = true;
 			} else if(this.connectionStatus == ConnectionStatus.DISCONNECTING) {
 				disconInProgress = true;
 			}
 		}
 		
 		if (shouldConnect) { //if not connected, then connect
 			
 			this.callback = callback;
 			
 			//notify connection
 			this.updateStatus(ConnectionStatus.CONNECTING, ConnectionError.NO_ERROR, null);
 			
 			//fill HTransportOptions
 			try {
 				this.fillHTransportOptions(publisher, password, options);
 			} catch (Exception e) { 
 				//stop connecting if filling error
 				this.updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.JID_MALFORMAT, e.getMessage());
 				return;
 			}
 			
 			//choose transport layer
 			if(options.getTransport().equals("socketio")) {
 				/*if (this.transport != null) { //check if other transport mode connect
 					this.transport.disconnect();
 				}*/
 				if (this.transport == null || (this.transport.getClass() != HTransportSocketio.class)) {
 					this.transport = new HTransportSocketio();
 				}
 				this.transport.connect(transportCallback, this.transportOptions);
 			} else {
 				/*if (this.transport != null) { //check if other transport mode connect
 					this.transport.disconnect();
 				}*/
 				this.transport = new HTransportXMPP();
 				this.transport.connect(transportCallback, this.transportOptions);
 			}
 		} else {
 			if (connInProgress) {
 				updateStatus(ConnectionStatus.CONNECTING, ConnectionError.CONN_PROGRESS, null);
 			} else if (disconInProgress) {
 				//updateStatus(ConnectionStatus.DISCONNECTING, ConnectionError.ALREADY_CONNECTED, null);
 			} else {
 				updateStatus(ConnectionStatus.CONNECTED, ConnectionError.ALREADY_CONNECTED, null);
 			}	
 		}
 	}
 
 	/**
 	 * fill htransport, randomly pick an endpoint from availables endpoints.
 	 * By default it uses options server host to fill serverhost field and as fallback jid domain
 	 * @param publisher - publisher as jid format (my_user@serverhost.com/my_resource)
 	 * @param password
 	 * @param options 
 	 * @throws Exception - in case jid is malformatted, it throws an exception
 	 */
 	public void fillHTransportOptions(String publisher, String password, HOptions options) throws Exception {
 		JabberID jid = new JabberID(publisher);
 		
 		this.transportOptions.setJid(jid);
 		this.transportOptions.setPassword(password);
 		this.transportOptions.setHserver(options.getHserver());
 		
 		//by default we user server host rather than publish host if defined
 		if (options.getServerHost() != null) {
 			this.transportOptions.setServerHost(options.getServerHost());
 		} else { 
 			this.transportOptions.setServerHost(jid.getDomain());
 		}
 		
 		this.transportOptions.setServerPort(options.getServerPort());
 		//for endpoints, pick one randomly and fill htransport options
 		if (options.getEndpoints().size() > 0) {
 			int endpointIndex = HUtil.pickIndex(options.getEndpoints()); 
 			String endpoint = options.getEndpoints().get(endpointIndex);
 			
 			this.transportOptions.setEndpointHost(HUtil.getHost(endpoint));
 			this.transportOptions.setEndpointPort(HUtil.getPort(endpoint));
 			this.transportOptions.setEndpointPath(HUtil.getPath(endpoint));
 		} else {
 			this.transportOptions.setEndpointHost(null);
 			this.transportOptions.setEndpointPort(0);
 			this.transportOptions.setEndpointPath(null);
 		}
 	}
 	
 	/**
 	 * change current status and notify delegate through callback
 	 * @param status - connection status
 	 * @param error - error code
 	 * @param errorMsg - a low level description of the error
 	 */
 	public void updateStatus(ConnectionStatus status, ConnectionError error, String errorMsg) {
 		if (callback != null) {
 			connectionStatus = status;
 			//create structure 
 			HStatus hstatus = new HStatus();
 			hstatus.setStatus(status);
 			hstatus.setErrorCode(error);
 			hstatus.setErrorMsg(errorMsg);
 			
 			try {
 				callback.hCallback("hstatus", hstatus);
 			} catch(Exception e) {
 			}
 			
 			if(status == ConnectionStatus.DISCONNECTED) {
 				callback = null;
 			}
 		} else {
 			System.out.println("Error : " + this.getClass().getName() + " requires a callback");
 		}
 	}
 	
 	/**
 	 * Disconnect
 	 */
 	public void disconnect() {
 		boolean shouldDisconnect = false;
 		boolean connectInProgress = false;
 		synchronized (this) {
 			if (this.connectionStatus == ConnectionStatus.CONNECTED) {
 				shouldDisconnect = true;
				
 				//update connection status
 				connectionStatus = ConnectionStatus.DISCONNECTING;
 			} else if(this.connectionStatus == ConnectionStatus.CONNECTING) {
 				connectInProgress = true;
 			}
 		}
 		
 		if(shouldDisconnect) {
 			updateStatus(ConnectionStatus.DISCONNECTING, ConnectionError.NO_ERROR, null);
 			transport.disconnect();
 		} else if (connectInProgress) {
 			updateStatus(ConnectionStatus.CONNECTING, ConnectionError.CONN_PROGRESS, "Can't disconnect while a connection is in progress");
 		} else {
 			updateStatus(ConnectionStatus.DISCONNECTED, ConnectionError.NOT_CONNECTED, null);
 			//remove callback
 			this.callback = null;
 		}
 		
 		
 	}
 
 	public String command(HCommand cmd) {
 		String reqid = null;
 		if(this.connectionStatus == ConnectionStatus.CONNECTED) {
 			if(cmd == null) {
 				cmd = new HCommand();
 			}
 			reqid = cmd.getReqid();
 			if(reqid == null) {
 				Random rand = new Random();
 				reqid = "javaCmd:" + rand.nextInt();
 				cmd.setReqid(reqid);
 			}
 			if(cmd.getSender() == null) {
 				cmd.setSender(transportOptions.getJid().getFullJID());
 			}
 			if(cmd.getSent() == null) {
 				cmd.setSent(new GregorianCalendar());
 			}
 			transport.sendObject(cmd.toJSON());
 		} else if(callback != null){
 			HStatus hstatus = new HStatus(this.connectionStatus, ConnectionError.NOT_CONNECTED, "Can not send hCommand. Not connected");
 			callback.hCallback("hstatus", hstatus);
 		}
 		return reqid;
 	}
 	
 	/* HTransportCallback functions */
 
 	/**
 	 * @internal
 	 * Class used to get callbacks from transport layer.
 	 */
 	private class TransportCallback implements HTransportCallback {
 
 		/**
 		 * @internal
 		 * see HTransportCallback for more informations
 		 */
 		public void connectionCallback(ConnectionStatus status,
 				ConnectionError error, String errorMsg) {
 			updateStatus(status, error, errorMsg);
 		}
 
 		/**
 		 * @internal
 		 * see HTransportCallback for more information
 		 */
 		@Override
 		public void dataCallback(JSONObject jsonData) {
 			try {
 				String type = jsonData.getString("type");
 				if(type.equalsIgnoreCase("hresult")) {
 					HResult data = new HResult();
 					try {
 						data.fromJSON(jsonData.getJSONObject("data"));
 						callback.hCallback(type, data);
 					} catch (Exception e) {
 						System.out.println("erreur datacallBack : data");
 					}
 				}
 			} catch (JSONException e) {
 				System.out.println("erreur datacallBack");
 			}
 		}
 
 	}
 
 }
