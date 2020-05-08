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
 
 import java.util.Hashtable;
 import java.util.Random;
 
 import org.hubiquitus.hapi.hStructures.ConnectionError;
 import org.hubiquitus.hapi.hStructures.ConnectionStatus;
 import org.hubiquitus.hapi.hStructures.HAck;
 import org.hubiquitus.hapi.hStructures.HAckValue;
 import org.hubiquitus.hapi.hStructures.HAlert;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HConvState;
 import org.hubiquitus.hapi.hStructures.HJsonObj;
 import org.hubiquitus.hapi.hStructures.HMeasure;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hapi.hStructures.HMessageOptions;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HResult;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.hubiquitus.hapi.hStructures.ResultStatus;
 import org.hubiquitus.hapi.structures.JabberID;
 import org.hubiquitus.hapi.transport.HTransport;
 import org.hubiquitus.hapi.transport.HTransportDelegate;
 import org.hubiquitus.hapi.transport.HTransportOptions;
 import org.hubiquitus.hapi.transport.socketio.HTransportSocketio;
 import org.hubiquitus.hapi.transport.xmpp.HTransportXMPP;
 import org.hubiquitus.hapi.util.HJsonDictionnary;
 import org.hubiquitus.hapi.util.HUtil;
 import org.json.JSONObject;
 
 import exceptions.MissingAttrException;
 
 
 /**
  * @version 0.3
  * Hubiquitus client, public api
  */
 
 public class HClient {
 	
 	private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED; /* only connecting, connected, diconnecting, disconnected */
 	@SuppressWarnings("unused")
 	private HOptions options = null;
 	private HTransportOptions transportOptions = null;
 	private HTransport transport;
 	
 	private HStatusDelegate statusDelegate = null;
 	private HMessageDelegate messageDelegate = null;
 	private Hashtable<String, HCommandDelegate> commandsDelegates = new Hashtable<String, HCommandDelegate>();
 	
 	private TransportDelegate transportDelegate= new TransportDelegate();
 	
 	public HClient() {
 		transportOptions = new HTransportOptions();
 	}
 
 	/**
 	 * Connect to server
 	 * @param publisher - user jid (ie : my_user@domain/resource)
 	 * @param password
 	 * @param callback - client callback to get api notifications
 	 * @param options
 	 */
 	public void connect(String publisher, String password, HOptions options) {
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
 			
 			//notify connection
 			this.notifyStatus(ConnectionStatus.CONNECTING, ConnectionError.NO_ERROR, null);
 			
 			//fill HTransportOptions
 			try {
 				this.fillHTransportOptions(publisher, password, options);
 			} catch (Exception e) { 
 				//stop connecting if filling error
 				this.notifyStatus(ConnectionStatus.DISCONNECTED, ConnectionError.JID_MALFORMAT, e.getMessage());
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
 				this.transport.connect(transportDelegate, this.transportOptions);
 			} else {
 				/*if (this.transport != null) { //check if other transport mode connect
 					this.transport.disconnect();
 				}*/
 				this.transport = new HTransportXMPP();
 				this.transport.connect(transportDelegate, this.transportOptions);
 			}
 		} else {
 			if (connInProgress) {
 				notifyStatus(ConnectionStatus.CONNECTING, ConnectionError.CONN_PROGRESS, null);
 			} else if (disconInProgress) {
 				//updateStatus(ConnectionStatus.DISCONNECTING, ConnectionError.ALREADY_CONNECTED, null);
 			} else {
 				notifyStatus(ConnectionStatus.CONNECTED, ConnectionError.ALREADY_CONNECTED, null);
 			}	
 		}
 	}
 
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
 			notifyStatus(ConnectionStatus.DISCONNECTING, ConnectionError.NO_ERROR, null);
 			transport.disconnect();
 		} else if (connectInProgress) {
 			notifyStatus(ConnectionStatus.CONNECTING, ConnectionError.CONN_PROGRESS, "Can't disconnect while a connection is in progress");
 		} else {
 			notifyStatus(ConnectionStatus.DISCONNECTED, ConnectionError.NOT_CONNECTED, null);
 		}
 		
 		
 	}
 	
 	/**
 	 * Status delegate receive all connection status events.
 	 * @param statusDelgate
 	 */
 	public void onStatus(HStatusDelegate statusDelgate) {
 		this.statusDelegate = statusDelgate;
 	}
 	
 	/**
 	 * Message delegate receive all incoming HMessage
 	 * @param messageDelegate
 	 */
 	public void onMessage(HMessageDelegate messageDelegate) {
 		this.messageDelegate = messageDelegate;
 	}
 	
 	/**
 	 * Get current connection status
 	 * @return
 	 */
 	public ConnectionStatus status() {
 		return this.connectionStatus;
 	}
 	
 	/**
 	 * Used to perform a command on an hubiquitus component : a hserver or a hubot.
 	 * @param cmd - name of the command
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 * @return reqid
 	 */
 	public void command(HCommand cmd, HCommandDelegate commandDelegate) {
 		if(this.connectionStatus == ConnectionStatus.CONNECTED && cmd != null) {
 			String reqid = null;
 			reqid = cmd.getReqid();
 			if(reqid == null) {
 				Random rand = new Random();
 				reqid = "javaCmd:" + rand.nextInt();
 				cmd.setReqid(reqid);
 			}
 			
 			if(cmd.getSender() == null) {
 				cmd.setSender(transportOptions.getJid().getFullJID());
 			}
 			
 			if(cmd.getTransient() == null) {
 				cmd.setTransient(true);
 			}
 			
 			if(cmd.getEntity() != null) {
 				if (commandDelegate != null) {
 					commandsDelegates.put(reqid, commandDelegate);
 				}
 				transport.sendObject(cmd.toJSON());
 			} else {
 				this.notifyResultError(cmd.getReqid(), cmd.getCmd(), ResultStatus.MISSING_ATTR, "Entity not found");
 			}
 		} else if (cmd == null) {
 			this.notifyResultError(null, null, ResultStatus.MISSING_ATTR, "Provided cmd is null", commandDelegate);
 		} else {
 			this.notifyResultError(cmd.getReqid(), cmd.getCmd(), ResultStatus.NOT_CONNECTED, null, commandDelegate);
 		}
 	}
 	
 	/**
 	 * Demands the server a subscription to the channel id.
 	 * The hAPI performs a hCommand of type hsubscribe.
 	 * The server will check if not already subscribed and if authorized and subscribe him.
 	 * @param chid - channel id
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 * @return request id
 	 */
 	public void subscribe(String chid, HCommandDelegate commandDelegate) {
 		HJsonDictionnary params = new HJsonDictionnary();
 		params.put("chid", chid);
 		HCommand cmd = new HCommand(transportOptions.getHserverService(), "hsubscribe", params);
 		this.command(cmd, commandDelegate);
 	}
 	
 	/**
 	 * Demands the server an unsubscription to the channel id.
 	 * The hAPI checks the current publisher’s subscriptions and if he is subscribed performs a hCommand of type hunsubscribe.
 	 * @param chid - channel id
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 * @return request id
 	 */
 	public void unsubscribe(String chid, HCommandDelegate commandDelegate) {
 		HJsonDictionnary params = new HJsonDictionnary();
 		params.put("chid", chid);
 		HCommand cmd = new HCommand(transportOptions.getHserverService(), "hunsubscribe", params);
 		this.command(cmd, commandDelegate);
 	}
 	
 	/**
 	 * Perform a publish operation of the provided hMessage to a channel.
 	 * @param message
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 * @return reqid
 	 */
 	public void publish(HMessage message, HCommandDelegate commandDelegate) {
 		//fill mandatory fields
 		String msgid = message.getMsgid();
 		if(msgid == null) {
 			Random rand = new Random();
 			msgid = "javaCmd:" + rand.nextInt();
 			message.setMsgid(msgid);
 		}
 		
 		String convid = message.getConvid();
 		if(convid == null) {
 			message.setConvid(msgid);
 		}
 				
 		message.setConvid(convid);
 		
 		if(message.getPublisher() == null) {
 			message.setPublisher(this.transportOptions.getJid().getBareJID());
 		}
 		
 		HCommand cmd = new HCommand(transportOptions.getHserverService(), "hpublish", message);
 		this.command(cmd, commandDelegate);				
 	}
 	/**
 	 * Demands the hserver a list of the last messages saved for a dedicated channel.
 	 * The publisher must be in the channel’s participants list.
 	 * 
 	 * Nominal response : an hCallback with an hResult will be performed when the result is available. 
 	 * If the hResult had status 0, the user should expect to receive n calls to hCallback of type
 	 * hMessage where n is equal to the number of messages retrieved with nbLastMsg as an upper limit.
 	 * @warning HResult result type will be a JSonArray if successful
 	 * @param chid - channel id
 	 * @param nbLastMsg
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 * @return request id
 	 */
 	public void getLastMessages(String chid, int nbLastMsg, HCommandDelegate commandDelegate) {
 		HJsonDictionnary params = new HJsonDictionnary();
 		params.put("chid", chid);
 		if(nbLastMsg > 0) {
 			params.put("nbLastMsg", nbLastMsg);
 		}
 		HCommand cmd = new HCommand(transportOptions.getHserverService(), "hgetlastmessages", params);
 		this.command(cmd, commandDelegate);
 	}
 	
 	/**
 	 * @see getLastMessages(String chid, int nbLastMsg) 
 	 * @param chid - channel id
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 * @return request id 
 	 */
 	public void getLastMessages(String chid, HCommandDelegate commandDelegate) {
 		this.getLastMessages(chid,-1, commandDelegate);
 	}
 	
 	/**
 	 * Demands the server a list of the publisher’s subscriptions.	
 	 * 
 	 * Nominal response : a hCallback with a hResult will be performed when the result is available with an array of channel id.
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 * @return request id
 	 */
 	public void getSubscriptions(HCommandDelegate commandDelegate) {
 		HCommand cmd = new HCommand(transportOptions.getHserverService(), "hgetsubscriptions", null);
 		this.command(cmd, commandDelegate);
 	}
 	
 	/**
 	 * Demands to the hserver the list of messages correlated by the convid value on a dedicated channel chid.
 	 * 
 	 * Nominal response : hResult where the status is 0 with an array of hMessage.
 	 * @param chid - Channel id. Mandatory
 	 * @param convid - Conversation id. Mandatory
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 */
 	public void getThread(String chid, String convid, HCommandDelegate commandDelegate) {
 		HJsonDictionnary params = new HJsonDictionnary();
 		String cmdName = "hgetthread";
 		
 		//check mandatory fields
 		if (chid == null || chid.length() <= 0) {
 			notifyResultError(null, cmdName, ResultStatus.MISSING_ATTR, "Chid is missing", commandDelegate);
 			return;
 		}
 		
 		if (convid == null || convid.length() <= 0) {
 			notifyResultError(null, cmdName, ResultStatus.MISSING_ATTR, "Convid is missing", commandDelegate);
 			return;
 		}
 		
 		params.put("chid", chid);
 		params.put("convid", convid);
 		
 		HCommand cmd = new HCommand(transportOptions.getHserverService(), cmdName, params);
 		this.command(cmd, commandDelegate);
 	}
 	
 	/**
 	 * Demands to the hserver the list of convid where there is a hConvState with the status value searched on the channel chid.
 	 * 
 	 * Nominal response : hResult where the status is 0 with an array of convid.
 	 * @param chid - Channel id. Mandatory
 	 * @param status - The status searched. Mandatory
 	 * @param commandDelegate - a delegate notified when the command result is issued. Can be null
 	 */
 	public void getThreads(String chid, String status, HCommandDelegate commandDelegate) {
 		HJsonDictionnary params = new HJsonDictionnary();
 		String cmdName = "hgetthreads";
 		
 		//check mandatory fields
 		if (chid == null || chid.length() <= 0) {
 			notifyResultError(null, cmdName, ResultStatus.MISSING_ATTR, "Chid is missing", commandDelegate);
 			return;
 		}
 		
 		if (status == null || status.length() <= 0) {
 			notifyResultError(null, cmdName, ResultStatus.MISSING_ATTR, "Status is missing", commandDelegate);
 			return;
 		}
 		
 		params.put("chid", chid);
 		params.put("status", status);
 		
 		HCommand cmd = new HCommand(transportOptions.getHserverService(), cmdName, params);
 		this.command(cmd, commandDelegate);
 	}
 	
 	/* Builder */
 	
 	/**
 	 * Helper to create hmessage
 	 * @see HMessage
 	 * @param chid - channel id : mandatory
 	 * @param type
 	 * @param payload
 	 * @param options
 	 * @return hMessage
 	 * @throws MissingAttrException 
 	 */
 	public HMessage buildMessage(String chid, String type, HJsonObj payload, HMessageOptions options) throws MissingAttrException {
 		
 		//check for required attributes
 		if (chid == null || chid.length() <= 0) {
 			throw new MissingAttrException("chid");
 		}
 		
 		//build the message
 		HMessage hmessage = new HMessage();
 
 		hmessage.setChid(chid);
 		hmessage.setType(type);
 		if(options != null) {
 			hmessage.setConvid(options.getConvid());
 			hmessage.setPriority(options.getPriority());
 			hmessage.setRelevance(options.getRelevance());
 			hmessage.setTransient(options.getTransient());
 			hmessage.setLocation(options.getLocation());
 			hmessage.setAuthor(options.getAuthor());
 			hmessage.setHeaders(options.getHeaders());
 		}				
 			
 		if(transportOptions != null && transportOptions.getJid() != null) {
 			hmessage.setPublisher(transportOptions.getJid().getBareJID());
 		} else {
 			hmessage.setPublisher(null);
 		}		
 				
 		hmessage.setPayload(payload);
 
 		return hmessage;
 	}
 	
 	/**
 	 * Helper to create hconvstate
 	 * @param chid - channel id : mandatory
 	 * @param convid - conversation id : mandatory
 	 * @param status - status of the conversation
 	 * @param options
 	 * @return hmessage
 	 * @throws MissingAttrException 
 	 */
 	public HMessage buildConvState(String chid, String convid, String status, HMessageOptions options) throws MissingAttrException {
 		
 		//check for required attributes
 		if (chid == null || chid.length() <= 0) {
 			throw new MissingAttrException("chid");
 		}
 		
 		if (convid == null || convid.length() <= 0) {
 			throw new MissingAttrException("convid");
 		}
 		
 		if (status == null || status.length() <= 0) {
 			throw new MissingAttrException("status");
 		}
 		
 		HMessage hmessage = new HMessage();
 		
 		HConvState hconvstate = new HConvState();
 		hconvstate.setStatus(status);
 		
 		hmessage = buildMessage(chid, "hConvState", hconvstate, options);
 		hmessage.setConvid(convid);
 
 		return hmessage;
 	}
 	
 	/**
 	 * Helper to create hack
 	 * @param chid - channel id : mandatory
 	 * @param ackid : mandatory
 	 * @param ack : mandatory
 	 * @param options
 	 * @return hmessage
 	 * @throws MissingAttrException 
 	 */
 	public HMessage buildAck(String chid, String ackid,HAckValue ack, HMessageOptions options) throws MissingAttrException {
 		//check for required attributes
 		if (chid == null || chid.length() <= 0) {
 			throw new MissingAttrException("chid");
 		}
 		
 		//check for required attributes
 		if (ackid == null || ackid.length() <= 0) {
 			throw new MissingAttrException("ackid");
 		}
 		
 		//check for required attributes
 		if (ack == null) {
 			throw new MissingAttrException("ack");
 		}
 		
 		HMessage hmessage = new HMessage();
 
 		HAck hack = new HAck();
 		hack.setAckid(ackid);
 		hack.setAck(ack);
 		hmessage = buildMessage(chid, "hAck", hack, options);
 
 		return hmessage;
 	}
 	
 	/**
 	 * Helper to create halert
 	 * @param chid - channel id : mandatory
 	 * @param alert : mandatory
 	 * @param options
 	 * @return hmessage
 	 * @throws MissingAttrException 
 	 */
 	public HMessage buildAlert(String chid, String alert, HMessageOptions options) throws MissingAttrException {
 		//check for required attributes
 		if (chid == null || chid.length() <= 0) {
 			throw new MissingAttrException("chid");
 		}
 		
 		//check for required attributes
 		if (alert == null || alert.length() <= 0) {
 			throw new MissingAttrException("chid");
 		}
 		
 		HMessage hmessage = new HMessage();
 		
 		HAlert halert = new HAlert();
 		halert.setAlert(alert);
 		
 		hmessage = buildMessage(chid, "hAlert", halert, options);
 	
 		return hmessage;
 	}
 	
 	/**
 	 * Helper to create hmeasure
 	 * @param chid - channel id : mandatory
 	 * @param value : mandatory
 	 * @param unit : mandatory
 	 * @param options
 	 * @return hmessage
 	 * @throws MissingAttrException 
 	 */
 	public HMessage buildMeasure(String chid, String value, String unit, HMessageOptions options) throws MissingAttrException {
 		//check for required attributes
 		if (chid == null || chid.length() <= 0) {
 			throw new MissingAttrException("chid");
 		}
 				
 		//check for required attributes
 		if (value == null || value.length() <= 0) {
 			throw new MissingAttrException("value");
 		}
 				
 		//check for required attributes
 		if (unit == null || unit.length() <= 0) {
 			throw new MissingAttrException("unit");
 		}
 		
 		HMessage hmessage = new HMessage();
 		
 		HMeasure hmeasure = new HMeasure();
 		hmeasure.setValue(value);
 		hmeasure.setUnit(unit);
 		hmessage = buildMessage(chid, "hMeasure", hmeasure, options);
 	
 		return hmessage;
 	}
 	/* HTransportCallback functions */
 
 	/**
 	 * @internal
 	 * fill htransport, randomly pick an endpoint from availables endpoints.
 	 * By default it uses options server host to fill serverhost field and as fallback jid domain
 	 * @param publisher - publisher as jid format (my_user@serverhost.com/my_resource)
 	 * @param password
 	 * @param options 
 	 * @throws Exception - in case jid is malformatted, it throws an exception
 	 */
 	private void fillHTransportOptions(String publisher, String password, HOptions options) throws Exception {
 		JabberID jid = new JabberID(publisher);
 		
 		this.transportOptions.setJid(jid);
 		this.transportOptions.setPassword(password);
 		this.transportOptions.setHserver(options.getHserver());
 		
 		//by default we user server host rather than publish host if defined
 		if (options.getServerHost() != null ) {
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
 	 * @internal
 	 * change current status and notify delegate through callback
 	 * @param status - connection status
 	 * @param error - error code
 	 * @param errorMsg - a low level description of the error
 	 */
 	private void notifyStatus(ConnectionStatus status, ConnectionError error, String errorMsg) {
 		try {
			if (this.statusDelegate != null) {
 				connectionStatus = status;
 				
 				//create structure 
 				final HStatus hstatus = new HStatus();
 				hstatus.setStatus(status);
 				hstatus.setErrorCode(error);
 				hstatus.setErrorMsg(errorMsg);
 				
 				//return status asynchronously
 				(new Thread(new Runnable() {
 					public void run() {
 						try {
 							statusDelegate.onStatus(hstatus);
 						} catch (Exception e) {
 							// TODO: Add a message to message logger
 							e.printStackTrace();
 						}
 					}
 				})).start();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			// TODO: Add a message to message logger
 		}
 	}
 	
 	/**
 	 * @internal
 	 * notify message delagate of an incoming hmessage
 	 */
 	private void notifyMessage(final HMessage message) {
 		try {
 			if (this.messageDelegate != null) {
 				
 				//return message asynchronously
 				(new Thread(new Runnable() {
 					public void run() {
 						try {
 							messageDelegate.onMessage(message);
 						} catch (Exception e) {
 							// TODO: Add a message to message logger
 							e.printStackTrace();
 						}
 					}
 				})).start();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			// TODO: Add a message to message logger
 		}
 	}
 	
 	/**
 	 * @internal
 	 * notify to a command delegate it's result
 	 */
 	private void notifyResult(final HResult result, final HCommandDelegate commandDelegate) {
 		try {
 			if (commandDelegate != null) {
 				
 				//return result asynchronously
 				(new Thread(new Runnable() {
 					public void run() {
 						try {
 							commandDelegate.onResult(result);
 						} catch (Exception e) {
 							// TODO: Add a message to message logger
 							e.printStackTrace();
 						}
 					}
 				})).start();
 			} else {
 				// TODO: add a message to logger in debug mode to know which results are dropped
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			// TODO: Add a message to message logger
 		}
 	}
 	
 	/**
 	 * @internal
 	 * notify to a command delegate it's result
 	 * internally calls notifyResult(final HResult result, HCommandDelegate commandDelegate) with
 	 * the right delegate
 	 */
 	private void notifyResult(final HResult result) {
 		HCommandDelegate commandDelegate = commandsDelegates.get(result.getReqid());
 		notifyResult(result, commandDelegate);
 	}
 	
 	/**
 	 * Helper function to return a hresult with a payload error
 	 * @param resultstatus
 	 * @param errorMsg
 	 */
 	private void notifyResultError(String reqid, String cmd, ResultStatus resultstatus, String errorMsg) {
 		HJsonDictionnary obj = new HJsonDictionnary(); 
 		obj.put("errorMsg", errorMsg);
 		HResult hresult = new HResult();
 		hresult.setResult(obj);
 		hresult.setStatus(resultstatus);
 		
 		hresult.setReqid(reqid);
 		hresult.setCmd(cmd);
 		
 		this.notifyResult(hresult);
 	}
 	
 	/**
 	 * Helper function to return a hresult with a payload error
 	 * @param resultstatus
 	 * @param errorMsg
 	 */
 	private void notifyResultError(String reqid, String cmd, ResultStatus resultstatus, String errorMsg, HCommandDelegate commandDelegate) {
 		HJsonDictionnary obj = new HJsonDictionnary(); 
 		obj.put("errorMsg", errorMsg);
 		HResult hresult = new HResult();
 		hresult.setResult(obj);
 		hresult.setStatus(resultstatus);
 		
 		hresult.setReqid(reqid);
 		hresult.setCmd(cmd);
 		
 		this.notifyResult(hresult, commandDelegate);
 	}
 	
 
 	/**
 	 * @internal
 	 * Class used to get callbacks from transport layer.
 	 */
 	private class TransportDelegate implements HTransportDelegate {
 
 		/**
 		 * @internal
 		 * see HTransportDelegate for more informations
 		 */
 		public void onStatus(ConnectionStatus status, ConnectionError error, String errorMsg) {
 			notifyStatus(status, error, errorMsg);
 		}
 
 		/**
 		 * @internal
 		 * see HTransportDelegate for more information
 		 */
 		public void onData(String type, JSONObject jsonData) {
 			try {
 				if(type.equalsIgnoreCase("hresult")) {
 					notifyResult(new HResult(jsonData));
 				} else if (type.equalsIgnoreCase("hmessage")) {
 					notifyMessage(new HMessage(jsonData));
 				}
 			} catch (Exception e) {
 				System.out.println("erreur datacallBack");
 			}
 		}
 	}
 
 	
 }
