 package com.test9.irc.engine;
 
 import java.util.Arrays;
 
 import com.test9.irc.display.ChatWindow;
 import com.test9.irc.parser.Message;
 
 /**
  * 
  * @author Jared Patton
  *
  */
 public class IRCEventAdapter implements IRCEventListener {
 
 	private ConnectionEngine owner;
 	private IRCConnection connection;
 	private ChatWindow cw;
 
 	public IRCEventAdapter(ConnectionEngine connectionEngine, IRCConnection connection) {
 		this.owner = connectionEngine;
 		this.connection = connection;
 
 
 		cw = connectionEngine.getCw();
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
 		if(numCode == IRCUtil.ERR_NICKNAMEINUSE) {
 			System.out.println("ERR_NICKNAMEINUSE");
 			connection.setNick(connection.getNick()+"_");
 			connection.send("NICK "+ connection.getNick());
 		}
 
 	}
 
 	@Override
 	public void onInvite() {
 
 	}
 
 	@Override
 	public void onJoin(String host, Message m) {
 
 		if(m.getNickname().equals(connection.getNick())) {
 			if(!(m.getContent().equals("")))
 			{
 				cw.getListener().onJoinChannel(host, m.getContent());
 			}
 
 		} else {
 			if(!(m.getParams()[0].equals(""))) {
 				cw.getListener().onUserJoin(host, m.getParams()[0], m.getNickname(), false);
 			} else if(!(m.getContent().equals(""))){
 				cw.getListener().onUserJoin(host, m.getContent(), m.getNickname(), false);
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
 
 	}
 
 	@Override
 	public void onMode(int two) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onNick(Message m) {
 		connection.getUser(m.getNickname()).setNick(m.getContent());
 
 		cw.getListener().onNickChange(connection.getHost(), m.getNickname(), m.getContent());
 		// If it is me
 		if(m.getNickname().equals(connection.getNick()))
 			connection.setNick(m.getContent());
 
 	}
 
 	@Override	
 	public void onNotice(Message m) {
 		cw.getListener().onNotice(connection.getHost(), m.getParams().toString(), m.getContent());
 
 	}
 
 	@Override
 	public void onPart(Message m) {
 		System.out.println("onPart()");
 		if(m.getNickname().equals(connection.getNick())) {
 			cw.getListener().onPartChannel(connection.getHost(), m.getParams()[0]);
 		} else {
 			cw.getListener().onUserPart(connection.getHost(), m.getParams()[0], m.getNickname());
 		}
 	}
 
 	@Override
 	public void onPing(String line) {
 		connection.send("PONG " + line);
 	}
 
 	@Override
 	public void onPrivmsg(String host, Message m) {
 		if(m.getContent().contains(connection.getNick()))
 			cw.newMessageHighlight(host, m.getParams()[0], m.getNickname(), m.getContent());
 		else
 			if(connection.getUser(m.getNickname())==null)
 			{
 				connection.getUsers().add(new User(m.getNickname(), false));
 			}
 			cw.getListener().onNewPrivMessage(
 					connection.getUser(m.getNickname()), host, m.getParams()[0], 
 					m.getNickname(), m.getContent(), false);		
 	}
 
 	@Override
 	public void onQuit(Message m) {
 		cw.getListener().onUserQuit(connection.getHost(), m.getNickname(), m.getContent());
 	}
 
 	@Override
 	public void onReply(Message m) {
 		int numCode = Integer.valueOf(m.getCommand());
 
 		if(numCode == IRCUtil.RPL_WELCOME) {
 			cw.getIrcConnections().add(connection);
 			//cw.getListener().onJoinServer(m.getServerName());
 
 		} else if(numCode == IRCUtil.RPL_NAMREPLY) {
 			System.out.println("RPL_NAMERPKY");
 			String[] nicks = m.getContent().split(" ");
 			for(String n : nicks)
 			{
 				cw.getListener().onUserJoin(connection.getHost(), m.getParams()[2], n, true);
 				if(!n.equals(connection.getNick()))
 					connection.getUsers().add(new User(n,false));
 				else
 					connection.getUsers().add(new User(n,true));
 			}
 		} else if(numCode == IRCUtil.RPL_TOPIC) {
 			cw.getListener().onNewTopic(connection.getHost(), m.getParams()[1], m.getContent());
 			cw.getListener().onNewMessage(connection.getHost(), m.getParams()[1],
 					"<Topic> " + m.getContent(), "TOPIC");
 		} else if(numCode == IRCUtil.RPL_NOWAWAY) {
 
 		} else if(numCode == IRCUtil.RPL_ISUPPORT) {
 			cw.getListener().onNewMessage(connection.getHost(), connection.getHost(), 
 					Arrays.toString(m.getParams())+" "+m.getContent(), "REPLY");
 		}
 	}
 
 	@Override
 	public void onTopic(String host, Message m) {
 		cw.getListener().onNewTopic(host, m.getParams()[0], m.getContent());
 	}
 
 	@Override
 	public void onUnknown(String host, Message m) {
 		try{
 			cw.getListener().onNewMessage(host, host, m.getContent(), "REPLY");	
 		}catch(Exception e){}
 	}
 }
