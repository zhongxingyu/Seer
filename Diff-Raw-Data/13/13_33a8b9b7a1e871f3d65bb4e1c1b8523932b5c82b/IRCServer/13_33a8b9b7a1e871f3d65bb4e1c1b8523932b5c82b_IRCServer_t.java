 package plugin.frirc;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 
 import com.db4o.ObjectContainer;
 
 import freenet.client.FetchContext;
 import freenet.client.FetchException;
 import freenet.client.FetchResult;
 import freenet.client.HighLevelSimpleClient;
 import freenet.client.async.ClientGetCallback;
 import freenet.client.async.ClientGetter;
 import freenet.keys.FreenetURI;
 import freenet.node.FSParseException;
 import freenet.node.RequestClient;
 import freenet.pluginmanager.FredPluginTalker;
 import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.pluginmanager.PluginTalker;
 import freenet.support.Logger;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 
 public class IRCServer extends Thread implements FredPluginTalker, ClientGetCallback, RequestClient{  
 	static int PORT=6667; // assign to next available Port.
 	static final String SERVERNAME = "freenetIRCserver";
 	private final int TRY_SPAWN_AGAIN = 3 * 60 * 1000; //ms 
 	
 	private Map<String, ArrayList<FrircConnection>> nickToInput = new HashMap<String, ArrayList<FrircConnection>>();
 	private HashMap<FrircConnection, ArrayList<Message>> outQueue = new HashMap<FrircConnection, ArrayList<Message>>();
 	private HashMap<String, HashSet<String>> channelUsers = new HashMap<String, HashSet<String>>(); 
 	private ArrayList<HashMap<String, String>> ownIdentities = new ArrayList<HashMap<String, String>>(); //list of my own identities
 	private ArrayList<HashMap<String, String>> identities = new ArrayList<HashMap<String, String>>(); //list of all identities
 	
 	private HighLevelSimpleClient hl;
 	private HighLevelSimpleClient low_priority_hl;
 	
 	private PluginRespirator pr;
 	private PluginTalker talker;
 	private HashMap<String, Long> last_channel_spawn = new HashMap<String, Long>(); 
 	
 	private ServerSocket serverSocket;
 
 	public String getNickByCon(FrircConnection con)
 	{
 		for(String nickItem : nickToInput.keySet())
 		{
 			if (nickToInput.get(nickItem).contains(con))
 			{
 				return nickItem;
 			}
 		}
 		return "ERROR COULD NOT ASSOCIATE CONNECTION WITH NICKNAME";
 	}
 
 	/**
 	 * Check whether a nick is in a channel or not
 	 * @param nick
 	 * @param channel
 	 * @return
 	 */
 	
 	public boolean inChannel(String nick, String channel)
 	{
 		//hack to auto-add a '#' in front of a channel if there isn't one (otherwise it's a user???)
 		if (!channel.contains("#")) channel = "#" + channel;
 		
 		/*
 		for(String channelItem : channelUsers.keySet())
 		{
 			for(String userItem : channelUsers.get(channelItem))
 			{
 				System.out.println(channelItem + " - " + userItem);
 			}
 		}
 		*/
 		
 		if (channelUsers.get(channel) != null)
 		{
 			return channelUsers.get(channel).contains(nick);
 		}
 		return false;
 	}
 	
 	/**
 	 * Retrieve the nicks present in the channel
 	 * @param channel
 	 * @return
 	 */
 	
 	public HashSet<String> getNicksInChannel(String channel)
 	{
 		return channelUsers.get(channel);
 	}
 	
 	private synchronized void associateNickWithConnection(String nick,  FrircConnection con)
 	{
 		if (nickToInput.containsKey(nick))
 		{
 			nickToInput.get(nick).add(con);
 		}
 		else
 		{
 			nickToInput.put(nick, new ArrayList<FrircConnection>());
 			nickToInput.get(nick).add(con);
 		}
 	}
 
 	private synchronized void initOutQueue(FrircConnection con)
 	{
 		if (!outQueue.containsKey(con))
 		{
 			outQueue.put(con, new ArrayList<Message>());
 		}
 	}
 
 	/**
 	 * Receive a message from a client, both remote and local clients
 	 * @param con
 	 * @param message
 	 */
 
 	public void message(FrircConnection con, String message)
 	{
 		Message messageObject = new Message(message);
 		initOutQueue(con);
 		message(con, messageObject);
 	}
 
 	public void messageFromFreenet(FrircConnection con, String id, String channel, String message, long timestamp)
 	{
 		//resolve the id to a nickname
 		String nick = getNickByID(id);
 
 		//associate FrircConnection with nickname
 		associateNickWithConnection(nick, con);
 
 		//check if the nickname is in the channel already
 		if (!channelUsers.get(channel).contains(nick))
 		{
 			channelUsers.get(channel).add(nick);
 
 			// not -> simulate join message
 			message(con, new Message("JOIN " + channel));
 		}
 
 		// emulate message coming from the nick
 		message(con, new Message("PRIVMSG " + channel + " :" + message));
 	}
 
 	/**
 	 * Associate a nick with a WoT identity
 	 * @param id
 	 * @return
 	 */
 
 	public String getNickByID(String id)
 	{
 		//check own identities
 		for(HashMap<String,String> identity : ownIdentities)
 		{
 			if (identity.get("ID").equals(id)){
 				return identity.get("nick");
 			}
 		}
 		
 		//check all identities
 		for(HashMap<String,String> identity : identities)
 		{
 			if (identity.get("ID").equals(id)){
 				return identity.get("nick");
 			}
 		}
 		
 		Logger.error(this.getClass(),"Could not resolve ID ("+id+") to a nickname, this means that the WoT identity is unknown to us or the WoT too old.");
 		return "UNRESOLVED";
 	}
 
 	/**
 	 * Retrieve an identity by its nickname
 	 * @param nick
 	 * @return
 	 */
 	
 	public HashMap<String, String> getIdentityByNick(String nick)
 	{
 		for(HashMap<String,String> identity : identities)
 		{
 			if (identity.get("nick").equals(nick)){
 				return identity;
 			}
 		}
 		
 		for(HashMap<String,String> identity : ownIdentities)
 		{
 			if (identity.get("nick").equals(nick)){
 				return identity;
 			}
 		}
 
 		
 		return null; //FIXME, if the user nickname doesn't match A OwnIdentity send a notice or something through the irc server and then quit the connection
 	}
 	
 	
 	/**
 	 * Process and possibly reply to IRC messages
 	 * @param source
 	 * @param messageObject
 	 */
 	
 	public synchronized void message(FrircConnection source, Message messageObject)
 	{
 		initOutQueue(source);
 		
 		
 		//associate nick with connection
 		if (messageObject.getType().equals("NICK") && !messageObject.getNick().equals(""))
 		{
 			associateNickWithConnection(messageObject.getNick(), source);
 		}
 
 
 		else if (messageObject.getType().equals("USER") && !messageObject.getValue().equals(""))
 		{
 			String nick = getNickByCon(source);
 			outQueue.get(source).add(new Message(":" + SERVERNAME + " 001 " + nick + " :Welcome to freenet irc"));
 			outQueue.get(source).add(new Message(":" + SERVERNAME + " 004 " + nick + " " + SERVERNAME + " freenet"));
 			outQueue.get(source).add(new Message(":" + SERVERNAME + " 375 " + nick + " :- Hi!"));
 			outQueue.get(source).add(new Message(":" + SERVERNAME + " 372 " + nick + " :- Welcome!"));
 			outQueue.get(source).add(new Message(":" + SERVERNAME + " 376 " + nick + " :End of /MOTD command"));
 		}
 
 		else if (messageObject.getType().equals("QUIT"))
 		{
 			outQueue.get(source).add(new Message("QUIT"));
 			try {
 				source.getSocket().close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		else if (messageObject.getType().equals("MODE"))
 		{
 			String nick = getNickByCon(source);
 			outQueue.get(source).add(new Message(":" + SERVERNAME + " NOTICE " + nick + " :Modes net supported at this time."));
 		}
 		
 		
 		else if (messageObject.getType().equals("PART"))
 		{
 			String channel = messageObject.getChannel();
 			String nick = getNickByCon(source);
 			
 			//quit all of the listening threads for this channel (threads should check with the service itself?, but only if
 			// the source is a FreenetClientInput  (isRemote)
 			
 			//send confirmation to all local clients in the same channel
 			for(String channelUser : channelUsers.get(channel))
 			{
 				for(FrircConnection connection : nickToInput.get(channelUser))
 				{
 					if (connection.isLocal())
 					{
 						outQueue.get(source).add(new Message(":" + nick + "!" + nick + "@freenet PART " + channel));
 					}
 				}
 			}
 
 			//remove nick from channel
 			channelUsers.get(channel).remove(nick);
 		}
 		
 		
 		
 		/**
 		 * Join a channel
 		 */
 
 		else if (messageObject.getType().equals("JOIN") && !messageObject.getChannel().equals(""))
 		{
 			//retrieve the nick associated with the connection
 			String nick = getNickByCon(source);
 			String channel = messageObject.getChannel();
 
 			//add the user to the channel
 			if (!channelUsers.containsKey(channel))
 			{
 				channelUsers.put(channel, new HashSet<String>());
 			}
 			channelUsers.get(channel).add(nick);
 
 			if (source.getClass().toString().equals("class plugin.frirc.ClientInput"))
 			{
 				//setup listeners to find other users in this channel (currently query FULL wot)
 				getAllIdentities(channel, nick);
 
 				//setup freenet output for the messages from the local joiner
 				setupFreenetPublisher(nick, channel, source);
 			}
 
 
 
 			//inform all clients in the same channel that the user has joined us
 			for(String channelUser: channelUsers.get(messageObject.getChannel()))
 			{
 				for(FrircConnection connection : nickToInput.get(channelUser))
 				{
 					System.out.println("channelUser = " + channelUser);
 					if (connection.isLocal())
 					{
 						System.out.println("Join message sent to " + channelUser + " about " + nick);
 						outQueue.get(connection).add(new Message(":" + nick+"!"+nick + "@freenet" + " JOIN " + channel));
 					}
 				}
 			}
 
 			//inform the joining clients about who is 
 			if (source.getClass().toString().equals("class plugin.frirc.ClientInput"))
 			{
 
 				outQueue.get(source).add(new Message(":" + SERVERNAME + " MODE " + channel + " +nt"));
 				outQueue.get(source).add(new Message(":" + SERVERNAME + " 331 " + nick + " " + channel + " :We eten vandaag hutspot"));
 
 				for(String channelUser: channelUsers.get(messageObject.getChannel()))
 				{
 					outQueue.get(source).add(new Message(":" + SERVERNAME + " 353 " + nick + " = " + channel + " :" + channelUser));
 				}
 
 				outQueue.get(source).add(new Message(":" + SERVERNAME + " 366 " + nick + " " + channel + " :End of /NAMES list"));
 
 
 				//outQueue.get(con).add(new Message("TOPIC " + messageObject.getValue() + ":We eten hutspot vandaag"));
 			}
 
 
 		}
 		
 		/*
 		 * WHO
 		 */
 		
 		else if (messageObject.getType().equals("WHO"))
 		{
 			String nick = getNickByCon(source);
 			String channel = messageObject.getChannel();
 
 			for(String channelUser: channelUsers.get(messageObject.getChannel()))
 			{
 				outQueue.get(source).add(new Message(":" + SERVERNAME + " 352 " + channelUser + " freenet " + channelUser + " H :0 " + channelUser));
 			}
 
 			outQueue.get(source).add(new Message("315 " + nick + " " + channel + " :End of /WHO list."));
 		}
 
 		else if (messageObject.getType().equals("PING"))
 		{
 			outQueue.get(source).add(new Message("PONG " + messageObject.getValue()));
 		}
 
 
 		/**
 		 * Message for channel
 		 */
 
 		else if (messageObject.getType().equals("PRIVMSG")) 
 		{
 			String nick = getNickByCon(source);
 
 			//iterate over all the users(connections) and send them the privmsg, except the originating user!
 
 			for(String channelUser: channelUsers.get(messageObject.getChannel()))
 			{
 				for (FrircConnection out : nickToInput.get(channelUser))
 				{
 					if (	( !source.isLocal() && out.isLocal() && !channelUser.equals(nick)) ||	//deliver locally 
 							( source.isLocal() && !out.isLocal() && channelUser.equals(nick) )		//publish to freenet
 						) 
 					{
 						System.out.println("DEBUG: Message added to some outqueue");
 						outQueue.get(out).add(new Message(":" + nick + "@freenet PRIVMSG " + messageObject.getChannel() + " :" + messageObject.getValue()));
 						
 						break;
 						//TODO: probably some memory leak here...(outQueue never emptied)
 					}
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Method for FreenetClient's that signal them leaving a channel (due to timeout or other)
 	 * @param connection
 	 */
 	
 	public void leaveChannel(FreenetClient connection)
 	{
		//disassociate nick with connection
		nickToInput.remove( getNickByCon(connection) );
		
 		//simulate a leave message from our client
 		message(connection, new Message("PART " + connection.getChannel()));
 	}
 	
 	private static String cleanChannel(String channel)
 	{
 		return channel.replace("#", "");
 	}
 
 	private synchronized void setupWoTListener(HashMap<String, String> identity, String channel)
 	{
 		
 		//check whether identity already has a listening thread
 		if (channelUsers.get(channel) != null && !channelUsers.get(channel).contains(identity.get("nick")))
 		{
 			//set an async fetch for the long-term message (every day or something), if it succeeds setup a thread to follow this identity+channel combo
 			FetchContext fc = low_priority_hl.getFetchContext();
			fc.maxNonSplitfileRetries = 2;
 			fc.followRedirects = true;
 			fc.ignoreStore = true;
 	
 			FreenetURI fetchURI;
 			try {
 				fetchURI = new FreenetURI("SSK@"+identity.get("ID") + "/" + Frirc.NAMESPACE + "-" + cleanChannel(channel) +  "-" + Frirc.currentIndex() + "-0/feed");
 				hl.fetch(fetchURI, 20000, this, this, fc);
 				System.out.println("Trying to see whether a user is publishing at: " + fetchURI);
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (FetchException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 		
 	/**
 	 * Create a new thread that will listen for messages specific to a channel+identity	
 	 * @param requestURI
 	 */
 		
 	private void createChannelIdentityThread(FreenetURI requestURI)	
 	{
 		//extract channel from URI
 		String channel = Frirc.requestURItoChannel(requestURI); 
 			
 		//extract id from URI
 		String id = Frirc.requestURItoID(requestURI);
 		String nick = getNickByID(id);
 
 		//don't create a listening thread for our own messages
 		for(Map<String, String> identity : ownIdentities)
 		{
 			if (identity.get("ID").equals(id)) return;
 		}
 		
 		System.out.println("Found an identity posting channel content! wow! setting up a dedicated thread to follow him/her/it!"); 
 		
 		try {
 			FreenetURI newRequestURI = new FreenetURI("SSK@" + id + "/" + Frirc.NAMESPACE + "-" + cleanChannel(channel));
 
 			//query each identity we know about for the channel (maxretries = 1)
 			//and create a handler to deal with the identity
 			FreenetClientInput input = new FreenetClientInput(null ,this, hl, low_priority_hl, channel, nick);
 			input.setRequestURI(newRequestURI);
 			
 			//init listener
 			input.start();
 
 			//associate FrircConnection with nickname
 			associateNickWithConnection(nick, input);
 
 			//check if the nickname is in the channel already
 			if (!channelUsers.get(channel).contains(nick))
 			{
 				channelUsers.get(channel).add(nick);
 
 				// not -> simulate join message
 				message(input, new Message("JOIN " + channel));
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		} 
 	}
 
 	/**
 	 * Retrieve the personal WoT identities to associate with nicknames
 	 */
 	
 	private void getOwnIdentities(){
 		SimpleFieldSet sfs = new SimpleFieldSet(true);
 		sfs.putOverwrite("Message", "GetOwnIdentities");
 		talker.send(sfs, null);
 	}
 	
 	private synchronized void getAllIdentities(String channel, String nick)
 	{
 		PluginTalker talker;
 		try {
 			talker = pr.getPluginTalker(this, Frirc.WoT_NAMESPACE, channel);
 			SimpleFieldSet sfs = new SimpleFieldSet(true);
 			sfs.putOverwrite("Message", "GetTrustees");
 			sfs.putOverwrite("Identity", getIdentityByNick(nick).get("ID").split(",")[0]); //a personal identity (associated through source) (only pass the ID, not the full SSK)
 			sfs.putOverwrite("Context", ""); //empty means selecting all identities no matter the context
 			talker.send(sfs, null);	//send message to WoT plugin
 			
 			System.out.println("requested identities for identity " + getIdentityByNick(nick).get("ID"));
 		} catch (PluginNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Publish our local channel communication to freenet
 	 * @param con
 	 */
 
 	private void setupFreenetPublisher(String nick, String channel, FrircConnection con)
 	{
 		HashMap<String, String> identity = getIdentityByNick(nick);
 		String insert = "SSK@" + identity.get("insertID") + "/" + Frirc.NAMESPACE + "-" + cleanChannel(channel);
 		String request = "SSK@" + identity.get("ID") + "/" + Frirc.NAMESPACE + "-" + cleanChannel(channel);
 
 		try {
 			FreenetURI insertURI = new FreenetURI(insert);
 			FreenetURI requestURI = new FreenetURI(request);
 
 			try {
 				//create new freenet client output thread
 				FreenetClientOutput output = new FreenetClientOutput(con.getSocket(), this, hl, low_priority_hl, channel, nick);
 				
 				output.setRequestURI(requestURI);
 				output.setInsertURI(insertURI);
 				output.start();
 
 				associateNickWithConnection(nick, output);
 				initOutQueue(output);
 			} catch (TransformerConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (TransformerFactoryConfigurationError e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			Logger.error("ERROR", "Unable to setup freenet output connection");
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized Message getMessageToSend(FrircConnection con)
 	{
 		
 		if (con.isLocal())
 		{
 			for(FrircConnection conItem : outQueue.keySet())
 			{
 				if (outQueue.get(conItem).size() > 0) 
 				{
 					if ( conItem.getSocket() != null && conItem.getSocket().equals(con.getSocket()) && conItem.isLocalClientInput() )
 					{
 							return outQueue.get(conItem).remove(0);
 					}
 				}
 			}
 		}
 		else
 		{
 			for(FrircConnection conItem : outQueue.keySet())
 			{
 				if (outQueue.get(conItem).size() > 0)
 				{
 					if (conItem.getSocket() != null && conItem.getSocket().equals(con.getSocket()))
 					{
 						synchronized (outQueue.get(conItem))
 						{
 							if (outQueue.get(conItem).get(0).getChannel().equals(con.getChannel()) && !con.isFreenetClientInput() )
 							{
 									return outQueue.get(conItem).remove(0);
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public IRCServer(HighLevelSimpleClient hl, HighLevelSimpleClient low_priority_hl, PluginRespirator pr)
 	{
 		this.hl = hl;
 		this.low_priority_hl = low_priority_hl;
 		this.pr = pr;
 		
 		try {
 			this.talker = pr.getPluginTalker(this, Frirc.WoT_NAMESPACE, "WoT");
 		} catch (PluginNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void run()
 	{
 		//try to obtain own identities from WoT
 		getOwnIdentities();
 		
 		try {
 			serverSocket = new ServerSocket(PORT);
 
 			InetAddress  addrs= InetAddress.getLocalHost();         
 			// Or InetAddress  addrs= InetAddress.getByName("localhost");
 			// Or InetAddress  addrs= InetAddress.getByName("127.0.0.1");  
 
 			System.out.println("TCP/Server running on : "+ addrs +" ,Port "+serverSocket.getLocalPort());
 
 			while(true) {
 				// Blocks until a connection occurs:
 				Socket socket = serverSocket.accept();
 				try {
 					new ClientInput(socket, this);  // Handle an incoming Client.
 					new ClientOutput(socket, this);  // Handle an incoming Client.
 				} catch(IOException e) {
 					// If it fails, close the socket,
 					// otherwise the thread will close it:
 					socket.close();
 					serverSocket.close();
 					channelUsers.clear();
 					identities.clear();
 					nickToInput.clear();
 					ownIdentities.clear();
 					outQueue.clear();
 					
 					return;
 				} catch (TransformerConfigurationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ParserConfigurationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TransformerFactoryConfigurationError e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	public void terminate()
 	{
 		if (serverSocket != null)
 		{
 		try {
 			serverSocket.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		}
 	
 		//do something smart with clearing channels and signalling threads to stop listening etc
 	}
 
 	public boolean stopThread()
 	{
 		if (serverSocket == null || serverSocket.isClosed()) return true;
 		return false;
 	}
 	
 	
 	/**
 	 * Method called upon receiving an FCP message, determines message type and calls correct method
 	 */
 	
 	@Override
 	public void onReply(String plugin, String channel, SimpleFieldSet sfs,
 			Bucket arg3) {
 		try {
 			System.out.println("Message received = " + sfs.get("Message"));
 			
 			
 			if (sfs.getString("Message").equals("OwnIdentities"))
 			{
 				addOwnIdentities(sfs);
 			}
 			else if (sfs.getString("Message").equals("Identities"))
 			{
 				addIdentities(sfs, channel);
 			}
 			else
 			{
 				System.out.println("Message received = " + sfs.get("OriginalMessage"));
 				System.out.println("Message received = " + sfs.get("Description"));
 			}
 		} catch (FSParseException e) { //no message field, shouldn't happen
 			e.printStackTrace();
 		}
 		
 	}
 
 	
 	private void addIdentities(SimpleFieldSet sfs, String channel)
 	{
 
 		//clear current identities (requests refresh
 		identities.clear();
 		
 		//iterate over identities and store them (for resolving nick later on)
 		int i = 0;
 		try {
 			while(!sfs.getString("Identity"+i).equals(""))
 			{
 				HashMap<String, String> identity = new HashMap<String,String>();
 				identity.put("ID", sfs.getString("RequestURI"+i).split("/")[0].replace("USK@", ""));
 				identity.put("nick", sfs.getString("Nickname"+i));
 				
 				if (Integer.parseInt(sfs.getString("Value"+i)) >= 0)
 				{
 					identities.add(identity);
 				}
 				
 				i++;
 			}
 		} catch (FSParseException e) { //triggered when we've reached the end of the identity list
 			//e.printStackTrace();
 			//System.out.println("Reached end of identity list");
 		}
 	
 		//keep listening to our trusted identities, all the time
 		//setup threads where needed, loop over just discovered created identities
 		for(HashMap<String, String> identity : identities)
 		{
 			setupWoTListener(identity, channel);
 		}
 		
 		doMaintenance(channel);
 	}
 	
 	/**
 	 * Perform various maintenance tasks for the IRC server
 	 * @param channel
 	 */
 	
 	private void doMaintenance(String channel)
 	{
 		while(true)
 		{
 			if (last_channel_spawn.get(channel) == null || last_channel_spawn.get(channel) < (System.currentTimeMillis() - TRY_SPAWN_AGAIN))
 			{
 				System.out.println("Setting up listeners...");
 				last_channel_spawn.put(channel, System.currentTimeMillis()); //timestamp latest attempt to setup listeners
 
 				//get trust tree for which nick? (an instance of OwnIdentity)
 				String nick = "";
 				for(String nickItem : channelUsers.get(channel))
 				{
 					for(Map<String, String> identity : ownIdentities)
 					{
 						if (identity.get("nick").equals(nickItem))
 						{
 							nick = nickItem;
 						}
 					}
 				}
 				
 				getAllIdentities(channel, nick);
 				return;
 			}
 			
 			
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			//stop listening if the server is no longer running
 			if (stopThread()) return;
 		}
 
 	}
 	
 	/**
 	 * Process the WoT fcp message containing our identities and add them to the local store
 	 * @param sfs
 	 */
 	
 	private void addOwnIdentities(SimpleFieldSet sfs)
 	{
 		int i = 0;
 		try {
 			while(!sfs.getString("Identity"+i).equals(""))
 			{
 				HashMap<String, String> identity = new HashMap<String,String>();
 				identity.put("ID", sfs.getString("RequestURI"+i).split("/")[0].replace("USK@", ""));
 				identity.put("insertID", sfs.getString("InsertURI"+i).split("/")[0].replace("USK@", ""));
 				identity.put("nick", sfs.getString("Nickname"+i));
 
 				ownIdentities.add(identity);
 				System.out.println("Identity added from WoT: " + identity.get("nick") + " (" + identity.get("ID") + ")");
 				++i;
 			}
 		} catch (FSParseException e) { //triggered when we've reached the end of the identity list
 		}
 	}
 
 	/*
 	 * Request client stuff
 	 * 
 	 * (non-Javadoc)
 	 * @see freenet.client.async.ClientGetCallback#onFailure(freenet.client.FetchException, freenet.client.async.ClientGetter, com.db4o.ObjectContainer)
 	 */
 	
 	
 	@Override
 	public void onFailure(FetchException arg0, ClientGetter arg1,
 			ObjectContainer arg2) {
 		
 		System.out.println("Could not find identity listening on channel key: "  + arg1.getURI());
 		
 	}
 
 	/**
 	 * Only success currently handled is a successful poll for a recent identity message in a specific channel 
 	 * TODO: this will need to be extended to something smarter in the future
 	 * @param fr
 	 * @param cg
 	 * @param oc
 	 */
 	
 	@Override
 	public void onSuccess(FetchResult fr, ClientGetter cg, ObjectContainer oc) {
 		createChannelIdentityThread(cg.getURI());
 	}
 
 	@Override
 	public void onMajorProgress(ObjectContainer arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean persistent() {
 		return false;
 	}
 
 	@Override
 	public void removeFrom(ObjectContainer arg0) {
 	}
 
 }
