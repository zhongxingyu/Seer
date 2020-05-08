 package com.test9.irc.engine;
 
 import java.util.ArrayList;
 
 import com.test9.irc.parser.Message;
 
 /**
  * 
  * @author Jared Patton
  *
  */
 public class IRCEventAdapter implements IRCEventListener {
 
 	/**
 	 * Reference to the connection the adapter belongs to.
 	 */
 	private IRCConnection connection;
 	private final String AMBUSH = " ambush ";
 	private ArrayList<String> nicksWithAmbush = new ArrayList<String>();
 	private ArrayList<String> ambushMessages = new ArrayList<String>();
 
 	/**
 	 * Reference to the chat window to access the listener.
 	 */
 
 	public IRCEventAdapter(IRCConnection connection) {
 		this.connection = connection;
 	}
 
 	@Override
 	public void onConnect(Message m) {
 
 
 	}
 
 	@Override
 	public void onDisconnect() {
 
 	}
 
 	@Override
 	public void onError(Message m) {
 		int numCode = Integer.valueOf(m.getCommand());
 		if(numCode == IRCConstants.ERR_NICKNAMEINUSE) {
 			System.out.println("ERR_NICKNAMEINUSE");
 			connection.setNick(connection.getNick()+"_");
 			connection.send("NICK "+ connection.getNick());
 		} else {
 			System.out.println("Error("+m.getCommand()+")"+m.getContent());
 		}
 	}
 
 	@Override
 	public void onInvite() {
 
 	}
 
 	@Override
 	public void onJoin(String connectionName, String host, Message m) {
 
 		if(m.getNickname().equalsIgnoreCase(connection.getNick())) {
 			if(!(m.getContent().equals("")))
 			{
 				System.out.println("i am joining a channel myself.");
 				System.out.println("Joined:"+ m.getContent());
 			}
 
 		} else if(m.getUser().equalsIgnoreCase(connection.getNick())) {
 			if(!(m.getContent().equals("")))
 			{
 				System.out.println("i am joining a channel myself.");
 				System.out.println("Joined:"+ m.getContent());
 			}
 		}else {
 			if(!(m.getParams()[0].equals(""))) {
 				System.out.println("no params[0]");
 				System.out.println("Joined:"+ m.getContent());
 			} else if(m.getContent().equals("")){
 				System.out.println("no content");
 				System.out.println("Joined:"+ m.getContent());
 			}
 			if(!(connection.getUsers().contains(m.getNickname())))
 			{
 				connection.getUsers().add(new User(m.getNickname(), false));
 			}
 		}
 	}
 
 	@Override
 	public void onKick() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMode(Message m) {
 		System.out.println("mode");
 	}
 
 	@Override
 	public void onMode(int two) {
 		System.out.println("mode");
 
 	}
 
 	@Override
 	public void onNick(Message m) {
 		// Set the user's nick in the conneciton user list
 //		connection.getUser(m.getNickname()).setNick(m.getContent());
 
 		// Tell the chat window listener there was a nick change
 		System.out.println("nick change:"+m.getNickname()+" to:"+m.getContent());
 
 		// If it is me
 		// Set my change my nick in the connection
 		if(m.getNickname().equals(connection.getNick()))
 			connection.setNick(m.getContent());
 
 	}
 
 	@Override	
 	public void onNotice(Message m) {
 		// Notify the listener of a new NoticeMessage
 		//		cw.onNotice(connection.getConnectionName(), m.getParams().toString(), m.getContent());
 	}
 
 	@Override
 	public void onPart(Message m) {
 		// If i parted a channel, tell the listener that i parted
 		try {
 			if(m.getNickname().equals(connection.getNick())) {
 				//				cw.onPartChannel(connection.getConnectionName(), m.getParams()[0]);
 			} else /* If someone else parted, notify of a user part*/ {
 				//			cw.onUserPart(connection.getConnectionName(), m.getParams()[0], m.getNickname());
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 		}
 	}
 
 	@Override
 	public void onPing(String line) {
 		System.out.println("got a ping");
 		connection.send("PONG " + line);
 		connection.send("WHO *");
 	}
 
 	@Override
 	public void onPrivmsg(String connectionName, String host, Message m) {
 		if(m.getContent().toLowerCase().contains("$"+connection.getNick())) {
 			String mess = m.getContent();
 			System.out.println("mess:"+mess);
			if(mess.toLowerCase().contains(":")) {
				mess = mess.substring(mess.indexOf(" ")+1, mess.length());
				
 				String ambushNick = mess.substring(0,mess.indexOf(":")).trim();
 				System.out.println("ambushNick:"+ambushNick);
 
				mess = mess.substring(mess.indexOf(":"), mess.length()).trim();
 
 				String ambushMess = mess;
 				System.out.println("ambush message:"+ambushMess.trim());
 
 				nicksWithAmbush.add(ambushNick);
 				ambushMessages.add("<"+m.getNickname()+"> "+mess);
 				connection.send("PRIVMSG "+m.getParams()[0]+" The operation succeeded.");
 			} else if(mess.toLowerCase().contains("help")) {
 				System.out.println(m.getParams()[0]);
				connection.send("PRIVMSG "+m.getParams()[0]+" Ambush syntax: <nick> ':' <message>");
 			} else if(mess.toLowerCase().contains(" join ")){
 				String joinChannel = mess.substring(mess.indexOf(" join ")+6, mess.length()).trim();
 				connection.send("JOIN "+joinChannel);
 			}  else if(mess.toLowerCase().contains(" part ")){
 				String partChannel = mess.substring(mess.indexOf(" part ")+6, mess.length()).trim();
 				connection.send("PART "+partChannel);
 			}
 		} else {
 			int n = 0;
 			while(checkAmbushNicks(m.getNickname()) >= 0){
 				int index = checkAmbushNicks(m.getNickname());
 				if(index >= 0) {
 					if(n == 0) {
 						connection.send("PRIVMSG "+m.getParams()[0]+" "+nicksWithAmbush.get(index)+
 								" While you were out.");
 					}
 					connection.send("PRIVMSG #ecsig " +ambushMessages.get(index));
 					nicksWithAmbush.remove(index);
 					ambushMessages.remove(index);
 					n++;
 				}
 			}
 		}
 	}
 
 	private int checkAmbushNicks(String nick){
 		for(int i = 0; i < nicksWithAmbush.size(); i++) {
 			if(nicksWithAmbush.get(i).equals(nick))
 				return i;
 		}
 		return -1;
 
 	}
 
 	@Override
 	public void onQuit(Message m) {
 		// Someone quit, notify the ChatWindow listener
 		System.out.println("User quit: "+m.getNickname());
 	}
 
 	@Override
 	public void onReply(Message m) {
 				// Get the numerical code of the server reply
 				int numCode = Integer.valueOf(m.getCommand());
 		
 				// If it is a welcome message 001
 				if(numCode == IRCConstants.RPL_WELCOME) {
 					// Notify the chat window listener of a new irc connection
 					connection.joinChannels();
 					// If the numCode is the name list of a channel
 				} //else if(numCode == IRCConstants.RPL_NAMREPLY) {
 		//			//Split up the nicks list
 		//			String[] nicks = m.getContent().split(" ");
 		//
 		//			// Add nicks 1 at a time
 		//			for(String n : nicks)
 		//			{
 		//				// The nick has joined on a certain server at a certain channel
 		//				// It was a user reply
 		//				System.out.println("User join:"+m.getParams()[2]+n);
 		//				// If the nick was not me
 		//				if(!n.equals(connection.getNick()))
 		//					//Add a new user to the Connections user list, false - not me
 		//					connection.getUsers().add(new User(n,false));
 		//				else
 		//					//Add myself ot the Connections user list, true - it was me
 		//					connection.getUsers().add(new User(n,true));
 		//			}
 		//			// If there is a topic message
 		//		} else if(numCode == IRCConstants.RPL_TOPIC) {
 		//			// Notify the chat window listener of a new topic
 		//			cw.onNewTopic(connection.getConnectionName(), m.getParams()[1], m.getContent());
 		//			// Notify the chat window listener that there is a new topic to show the user
 		//			cw.onNewMessage(connection.getConnectionName(), m.getParams()[1],
 		//					"<Topic> " + m.getContent(), "TOPIC");
 		//			// Someone went away
 		//		} else if(numCode == IRCConstants.RPL_NOWAWAY) {
 		//			System.out.println("some user went away");
 		//		} else if(numCode == IRCConstants.RPL_ISUPPORT) {
 		//			cw.onNewMessage(connection.getConnectionName(), connection.getConnectionName(), 
 		//					Arrays.toString(m.getParams())+" "+m.getContent(), "REPLY");
 		//			//List of channels available
 		//		} else if(numCode == IRCConstants.RPL_LIST) {
 		//			cw.onNewMessage(connection.getConnectionName(), 
 		//					connection.getConnectionName(), Arrays.toString(m.getParams()), "REPLY");
 		//		} else if(numCode == IRCConstants.RPL_MOTD) {
 		//			cw.onNewMessage(connection.getConnectionName(), connection.getConnectionName(), m.getContent(), "REPLY");
 		//		} else if(numCode == IRCConstants.RPL_WHOREPLY) {
 		//			char c = m.getParams()[6].charAt(0);
 		//			boolean isAway = Parser.isAway(c);
 		//			cw.onWhoReplyStatus(connection.getConnectionName(),m.getParams()[5], isAway);
 		//
 		//			if(isAway)
 		//				cw.onNewMessage(connection.getConnectionName(), 
 		//						connection.getConnectionName(), m.getParams()[5]+" is now away.", "REPLY");
 		//
 		//		} else if (numCode == IRCConstants.RPL_ENDOFNAMES) {
 		//			connection.send("WHO *");
 		//		} else {
 		//			cw.onNewMessage(connection.getConnectionName(), connection.getConnectionName(),
 		//					"Reply("+m.getCommand()+")"+m.getContent(), "REPLY");
 		//		}
 		//		System.out.println(m.getCommand()+" "+m.getContent());
 	}
 
 	@Override
 	public void onTopic(String connectionName, String host, Message m) {
 		// There is a topic message from the server
 		//		cw.onNewTopic(connectionName, m.getParams()[0], m.getContent());
 	}
 
 	@Override
 	public void onUnknown(String connectionName, String host, Message m) {
 		try{
 			System.out.println(m.getContent());
 		}catch(Exception e){}
 	}
 }
