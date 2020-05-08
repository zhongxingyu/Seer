 /*
    Copyright 2011 James Cowgill
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package uk.org.cowgill.james.jircd;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import uk.org.cowgill.james.jircd.util.ModeUtils;
 
 /**
  * Represents a client in the server
  * 
  * Clients are users on the server with a nickname, modes and can join channels
  * 
  * This could be a remote client or a local servlet
  * 
  * @author James
  */
 public abstract class Client
 {
 	private static final Logger logger = Logger.getLogger(Client.class);
 	
 	/**
 	 * Modes which are restricted from changing using setMode
 	 */
 	private static long RESTRICTED_MODES;
 	
 	static
 	{
 		RESTRICTED_MODES = ModeUtils.setMode(0,                'B');	//Bot
 		RESTRICTED_MODES = ModeUtils.setMode(RESTRICTED_MODES, 'Z');	//Secure
 	}
 	
 	/**
 	 * List of clients to be closed when close queue is processed
 	 */
 	private static ArrayList<Client> queuedClosures = new ArrayList<Client>();
 	
 	/**
 	 * Reason for this client's closure
 	 */
 	private String queuedCloseReason = null;
 
 	/**
 	 * The client's id
 	 */
 	public IRCMask id;
 
 	/**
 	 * The client's real name
 	 */
 	public String realName = "";
 	
 	/**
 	 * The client's away message or null if the client is not away
 	 */
 	public String awayMsg;
 	
 	/**
 	 * Set of joined channels
 	 */
 	Set<Channel> channels = new HashSet<Channel>();
 	
 	/**
 	 * Set of channels you've been invited
 	 */
 	Set<Channel> invited = new HashSet<Channel>();
 	
 	/**
 	 * Flags used to see what parts of the registration process has been completed
 	 * 
 	 * @see RegistrationFlags
 	 */
 	private int registrationFlags;
 	
 	/**
 	 * This client's IRC user mode
 	 */
 	private long mode;
 	
 	/**
 	 * Flags containing protocol enhancements
 	 * 
 	 * @see ProtocolEnhancments
 	 */
 	private int protocolEnhancements;
 	
 	/**
 	 * True if client is closed
 	 */
 	private boolean closed = false;
 	
 	/**
 	 * Time of login
 	 */
 	private long signonTime;
 
 	//------------------------------------------------
 
 	/**
 	 * Creates a new client and adds it to global collections
 	 * 
 	 * @param id the IRCMask representing this client's id
 	 * @param mode initial mode of the client (allows setting restricted modes)
 	 */
 	public Client(IRCMask id, long mode)
 	{
 		//Set id and mode
 		this.id = id;
 		this.mode = mode;
 		
 		//Add to global collections
 		Server.getServer().clients.add(this);
 	}
 	
 	/**
 	 * Returns true if this client is away
 	 * @return true if this client is away
 	 */
 	public boolean isAway()
 	{
 		return awayMsg != null;
 	}
 
 	/**
 	 * Sends this client's away message to the specified client if there is one
 	 * 
 	 * <p>If this client does not have an away message, nothing is sent
 	 * 
 	 * @param client client to send away message to
 	 */
 	public void sendAwayMsgTo(Client client)
 	{
 		if(isAway())
 		{
 			client.send(client.newNickMessage("301").appendParam(id.nick).appendParam(awayMsg));
 		}
 	}
 	
 	/**
 	 * Gets weather this client has been fully registered
 	 * @return true if this client has been fully registered
 	 */
 	public boolean isRegistered()
 	{
 		return (~registrationFlags & RegistrationFlags.AllFlags) == 0;
 	}
 	
 	/**
 	 * Sets a set of registration flags
 	 * 
 	 * If the client is already registered, this doesn't do anything useful
 	 * 
 	 * @param flags flags to set
 	 */
 	public void setRegistrationFlag(int flags)
 	{
 		registrationFlags |= flags;
 	}
 	
 	/**
 	 * Returns this client's registration flags
 	 * @return registration flags
 	 */
 	public int getRegistrationFlags()
 	{
 		return registrationFlags;
 	}
 	
 	/**
 	 * Sets a protocol enhancement
 	 * 
 	 * @param enhancement protocol enhancement this client now has
 	 */
 	public void setProtocolEnhancement(int enhancement)
 	{
 		protocolEnhancements |= enhancement;
 	}
 	
 	/**
 	 * Returns true if this client has a protocol enhancement
 	 * 
 	 * @param enhancement protocol enhancement to check
 	 * @return true if this client has it
 	 */
 	public boolean hasProtocolEnhancement(int enhancement)
 	{
 		return (protocolEnhancements & enhancement) != 0;
 	}
 	
 	/**
 	 * Event which should be fired after all registration information has been set
 	 */
 	protected void registeredEvent()
 	{
 		//Validate registration
 		if(registrationFlags != (RegistrationFlags.AllFlags - RegistrationFlags.RegComplete))
 		{
 			return;
 		}
 
 		Message msg;
 		Server server = Server.getServer();
 		Config config = server.getConfig();
 		
 		// * Check nick is not registered
 		if(server.clientsByNick.containsKey(id.nick))
 		{
 			msg = Message.newMessageFromServer("433");
 			msg.appendParam("*");
 			msg.appendParam(id.nick);
 			msg.appendParam("Nickname already in use");
 			send(msg);
 			
 			id.nick = null;			
 			registrationFlags &= ~ RegistrationFlags.NickSet;
 			return;
 		}
 		
 		// * Check bans
 		for(Config.Ban ban : config.banNick)
 		{
 			if(IRCMask.wildcardCompare(id.nick, ban.mask))
 			{
 				//Banned
 				msg = Message.newMessageFromServer("432");
 				msg.appendParam("*");
 				msg.appendParam(id.nick);
 				msg.appendParam("Nickname banned: " + ban.reason);
 				send(msg);
 				
 				id.nick = null;			
 				registrationFlags &= ~ RegistrationFlags.NickSet;
 				return;
 			}
 		}
 
 		for(Config.Ban ban : config.banUserHost)
 		{
 			if(IRCMask.wildcardCompare(id.user + "@" + id.host, ban.mask))
 			{
 				//Banned
 				msg = newNickMessage("465");
 				msg.appendParam("Banned: " + ban.reason);
 				send(msg);
 				
 				close("Banned");
 				return;
 			}
 		}
 		
 		// * Check accept lines
 		Config.Accept myAcceptLine = null;
 		
 		for(Config.Accept accept : config.accepts)
 		{
 			if(IRCMask.wildcardCompare(id.user + "@" + id.host, accept.hostMask) ||
 					IRCMask.wildcardCompare(this.getIpAddress(), accept.ipMask))
 			{
 				//Accept using this line
 				myAcceptLine = accept;
 				break;
 			}
 		}
 		
 		if(myAcceptLine == null)
 		{
 			msg = newNickMessage("465");
 			msg.appendParam("No accept lines for your host");
 			send(msg);
 			
 			close("No accept lines for your host");
 			return;
 		}
 		
 		// * Check max ip clones
 		if(isRemote() && !server.ipClonesIncrement(getIpAddress(), myAcceptLine.maxClones))
 		{
 			msg = newNickMessage("465");
 			msg.appendParam("Too many connections from your host");
 			send(msg);
 			
 			close("Too many connections from your host");
 			return;
 		}
 		
 		// * Change default connection class
 		if(!this.changeClass(myAcceptLine.classLine, true))
 		{
 			msg = newNickMessage("465");
 			msg.appendParam("The server is full");
 			send(msg);
 			
 			close("The server is full");
 			return;
 		}
 		
 		// * Mark registered
 		setRegistrationFlag(RegistrationFlags.RegComplete);
 		
 		// * Add to global nick arrays
 		server.clientsByNick.put(id.nick, this);
 		
 		// * Update peek users
 		int clientCount = server.getClientCount();
 		if(clientCount > server.peekClients)
 		{
 			server.peekClients = clientCount;
 		}
 		
 		//Display welcome messages
 		send(this.newNickMessage("001").appendParam("Welcome to the Internet Relay Network " + id.toString()));
 		send(this.newNickMessage("002").appendParam("Your host is " + config.serverName +
 				" running version " + Server.VERSION_STR));
 		send(this.newNickMessage("003").appendParam("This server was created " + server.creationTimeStr));
 		server.getISupport().sendISupportMsgs(this);		//Sends 004 and 005
 		
 		// * Display LUSERS, MOTD and MODE
 		ModuleManager moduleMan = server.getModuleManager();
 		moduleMan.executeCommand(this, new Message("LUSERS"));
 		moduleMan.executeCommand(this, new Message("MOTD"));
 		
 		if(this.mode != 0)
 		{
 			send(new Message("MODE", this).appendParam(id.nick).appendParam(ModeUtils.toString(mode)));
 		}
 		
 		signonTime = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Returns the time the client signed on in mulliseconds since the UNIX Epoch
 	 * 
 	 * @return the time the client signed on
 	 */
 	public long getSignonTime()
 	{
 		return signonTime;
 	}
 	
 	/**
 	 * Requests that this client be closed
 	 * 
 	 * @param quitMsg the string told to other users about why this client is exiting
 	 */
 	public final void close(String quitMsg)
 	{
 		//Send info + close client
 		if(!this.closeForShutdown(quitMsg))
 		{
 			return;
 		}		
 
 		//Generate client collection to send to
 		HashSet<Client> toSendTo = new HashSet<Client>();
 		Set<Client> chanSet;
 
 		for(Channel channel : this.channels)
 		{
 			//Part channel
 			chanSet = channel.partForQuit(this);
 			
 			//Organise sending
 			if(chanSet != null)
 			{
 				toSendTo.addAll(chanSet);
 			}
 		}
 
 		//Send notifications
 		sendTo(toSendTo, new Message("QUIT", this).appendParam(quitMsg));
 
 		//Remove Any Channel Invites
 		for (Channel invite : invited)
 		{
 			invite.invited.remove(this);
 		}
 		
 		//Remove nick from global nick array
 		Server server = Server.getServer();
 		
 		if (isRegistered())
 		{
 			//Remove from clients by nick
 			server.clientsByNick.remove(id.nick);
 			
 			//Ip Clone check
 			if(isRemote())
 			{
 				server.ipClonesDecrement(getIpAddress());
 			}
 		}
 
 		//Remove from global array + operator cache
 		server.operators.remove(this);
 		server.clients.remove(this);
 	}
 	
 	/**
 	 * Closes a client connection but does not bother with freeing resources
 	 * 
 	 * @param quitMsg quit message
 	 * @return whether the close was successful
 	 */
 	boolean closeForShutdown(String quitMsg)
 	{
 		//Shield from multiple closures
 		if (closed)
 		{
 			return false;
 		}
 		closed = true;
 
 		//Send Notification To Client (if it isn't a servlet)
 		if (isRemote())
 		{
 			if(quitMsg == null || quitMsg.length() == 0)
 			{
 				send(Message.newStringFromServer("ERROR :Closing Link"));
 			}
 			else
 			{
 				send(Message.newStringFromServer("ERROR :Closing Link - ") + quitMsg);
 			}
 		}
 
 		//Close Socket
 		closed = this.rawClose();
 		return closed;
 	}
 	
 	/**
 	 * Sets the nickname of this client
 	 * 
 	 * <p>This does not perform any checks whether the user is allowed to change nickname
 	 * 
 	 * @param nick new nickname
 	 * @return false if the nick is in use
 	 */
 	public boolean setNick(String nick)
 	{
 		//Check for same nick
 		if(nick.equalsIgnoreCase(id.nick))
 		{
 			return true;
 		}
 		
 		//Check whether nick is in use
 		Server server = Server.getServer();
 		if(server.clientsByNick.containsKey(nick))
 		{
 			return false;
 		}
 		
 		//Do extra stuff if registered
 		if(isRegistered())
 		{
 			//Generate nick change message
 			Message msg = new Message("NICK", this);
 			msg.appendParam(nick);
 			
 			//Find all members of all joined channels to send to
 			Set<Client> toSendTo = new HashSet<Client>();
 			for(Channel channel : channels)
 			{
 				toSendTo.addAll(channel.getMembers().keySet());
 			}
 			
 			//Send to self also
 			toSendTo.add(this);
 			sendTo(toSendTo, msg);
 			
 			//Change nick
 			server.clientsByNick.remove(id.nick);
 			id.nick = nick;
 			server.clientsByNick.put(nick, this);
 		}
 		else
 		{
 			//Set provisional nick
 			id.nick = nick;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Returns true if this client has been closed
 	 * @return true if this client has been closed
 	 */
 	public boolean isClosed()
 	{
 		return closed;
 	}
 	
 	/**
 	 * Returns the permissions granted to this client
 	 * @return permission mask of this client
 	 */
 	public int getPermissionMask()
 	{
 		//Check oper modes
 		if(isModeSet('o'))
 		{
 			return Server.getServer().getConfig().permissionsOp;
 		}
 		else if(isModeSet('O'))
 		{
 			return Server.getServer().getConfig().permissionsSuperOp;
 		}
 		else
 		{
 			return 0;
 		}	
 	}
 	
 	/**
 	 * Determines whether a client has an extra permission
 	 * @param permission permission to check
 	 * @return true if the client has that permission
 	 */
 	public boolean hasPermission(int permission)
 	{
 		return (getPermissionMask() & permission) != 0;
 	}
 	
 	/**
 	 * Gets the client's mode
 	 * @return mode of the client
 	 */
 	public long getMode()
 	{
 		return mode;
 	}
 	
 	/**
 	 * Gets whether a user mode is set
 	 * 
 	 * @param mode the mode to test
 	 * @return true if the mode is set
 	 */
 	public boolean isModeSet(char mode)
 	{
 		return ModeUtils.isModeSet(this.mode, mode);
 	}
 	
 	/**
 	 * Sets a usermode and tells the client
 	 * 
 	 * @param mode mode to set
 	 * @param adding whether to add the mode (false to delete it)
 	 */
 	public void setMode(char mode, boolean adding)
 	{
 		setMode(mode, adding, null);
 	}
 	
 	/**
 	 * Sets a usermode and tells the client and another person
 	 * 
 	 * @param mode mode to set
 	 * @param adding whether to add the mode (false to delete it)
 	 * @param other additional client to notify
 	 */
 	public void setMode(char mode, boolean adding, Client other)
 	{
 		//Check mode setting
 		if(isModeSet(mode) != adding)
 		{
 			//Change mode
 			String str = (adding ? "+" : "-") + mode;
 			
 			//Check for special modes
 			if(mode == 'o' || mode == 'O')
 			{
 				if(adding)
 				{
 					//Check existing modes
 					if(isModeSet('o') || isModeSet('O'))
 					{
 						//Unset other mode
 						char c;
 						if(mode == 'o')
 						{
 							c = 'O';
 						}
 						else
 						{
 							c = 'o';
 						}
 						
 						this.mode = ModeUtils.clearMode(this.mode, c);
 						str += "-" + c;
 					}
 					else
 					{
 						//Add to oper cache
 						Server.getServer().operators.add(this);
 					}
 	
 					//Log change
 					logger.info(id.toString() + " has set mode " + str);
 				}
 				else
 				{
 					//Remove from oper cache
 					Server.getServer().operators.remove(this);
 				}
 			}
 			else if(ModeUtils.isModeSet(RESTRICTED_MODES, mode))
 			{
 				//Ignore this change
 				return;
 			}
 			
 			//Change mode
 			this.mode = ModeUtils.changeMode(this.mode, mode, adding);
 			
 			Message msg = new Message("MODE", this).appendParam(id.nick).appendParam(str);
 			send(msg);
 			
 			if(other != null && this != other)
 			{
 				other.send(msg);
 			}
 		}
 	}
 	
 	/**
 	 * Returns the channels which this client has joined
 	 * 
 	 * @return channels this client has joined
 	 */
 	public Set<Channel> getChannels()
 	{
 		return Collections.unmodifiableSet(channels);
 	}
 	
 	/**
 	 * Changes the class of this client
 	 * @param clazz Class to change to
 	 * @param defaultClass True to change default class
 	 * @return false if there are not enough links in a class to change
 	 */
 	protected abstract boolean changeClass(ConnectionClass clazz, boolean defaultClass);
 	
 	/**
 	 * Restores this client's class to the default class
 	 * 
 	 * (default class restores override the max links)
 	 */
 	public abstract void restoreClass();
 	
 	/**
 	 * Changes the class of this client
 	 * @param clazz Class to change to
 	 */
 	public final void changeClass(ConnectionClass clazz)
 	{
 		changeClass(clazz, false);
 	}
 	
 	/**
 	 * Returns the ip address for this client
 	 * 
 	 * Servlets always return 127.0.0.1
 	 * 
 	 * @return The ip address of the client
 	 */
 	public abstract String getIpAddress();
 	
 	/**
 	 * Returns true if this client is a remote user
 	 * 
 	 * @return true if this client is a remote user
 	 */
 	public boolean isRemote()
 	{
 		return !ModeUtils.isModeSet(mode, 'B');
 	}
 	
 	/**
 	 * Marks this client for closure after the current client has finished processing
 	 * 
 	 * @param quitStatus the string told to other users about why this client is exiting
 	 */
 	public final void queueClose(String quitStatus)
 	{
		if(queuedCloseReason == null)
 		{
 			queuedCloseReason = quitStatus;
 			queuedClosures.add(this);
 		}
 	}
 	
 	/**
 	 * Gets weather this client is queued for closure
 	 * @return weather this client is queued for closure
 	 */
 	public boolean isQueuedForClose()
 	{
 		return queuedCloseReason != null;
 	}
 	
 	/**
 	 * Processes the close queue - closes all queued clients
 	 */
 	public static void processCloseQueue()
 	{
 		for(Client client : queuedClosures)
 		{
 			client.close(client.queuedCloseReason);
 		}
 		
 		queuedClosures.clear();
 	}
 	
 	/**
 	 * Sends a message to a collection of clients
 	 * 
 	 * @param clients clients to send data to
 	 * @param data data to send
 	 */
 	public static void sendTo(Iterable<? extends Client> clients, Object data)
 	{
 		sendTo(clients, data, null);
 	}
 	
 	/**
 	 * Sends a message to a collection of clients
 	 * 
 	 * @param clients clients to send data to
 	 * @param data data to send
 	 * @param except do not send message to this client
 	 */
 	public static void sendTo(Iterable<? extends Client> clients, Object data, Client except)
 	{
 		//Send strings to remote clients
 		String remoteSend = data.toString();
 		
 		for(Client client : clients)
 		{
 			if(client != except)
 			{
 				if(client.isRemote())
 				{
 					client.send(remoteSend);
 				}
 				else
 				{
 					client.send(data);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Creates a new message from this server with this client's nickname as the first parameter
 	 * 
 	 * @param command command of the message
 	 */
 	public Message newNickMessage(String command)
 	{
 		//If nick is null, use * nickname
 		String nick = (id.nick == null) ? "*" : id.nick;
 		return Message.newMessageFromServer(command).appendParam(nick);
 	}
 	
 	/**
 	 * Sends IRC data to a client (data is converted to string with toString)
 	 * 
 	 * @param data Data to send
 	 */
 	public abstract void send(Object data);
 	
 	/**
 	 * Performs client sepific close routines
 	 * 
 	 * @return Returns true if the close was a sucess. Returns false to abort the close.
 	 */
 	protected abstract boolean rawClose();
 	
 	/**
 	 * Returns the the in milliseconds this cient has been idle for
 	 * 
 	 * @return idle time of this client in milliseconds
 	 */
 	public abstract long getIdleTime();
 }
