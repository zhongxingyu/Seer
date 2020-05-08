 package com.test9.irc.engine;
 
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
 
 	public IRCEventAdapter(ConnectionEngine connectionEngine) {
 		this.owner = connectionEngine;
 		connection = owner.getConnection();
 		cw = connectionEngine.getCw();
 	}
 
 	@Override
 	public void onConnect(Message m) {
 
 
 	}
 
 	@Override
 	public void onDisconnect() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onError() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onInvite() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onJoin(String host, Message m) {
 		if(m.getUser().equals(connection.getNick())) {
 			cw.getListener().onJoinChannel(host, m.getContent());
 		} else {
 			cw.getListener().onUserJoin(host, m.getContent(), m.getNickname());
			System.out.println(!(connection.getUsers().contains(m.getNickname())));
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
 		cw.getListener().onNickChange(m.getNickname(), m.getContent());

		connection.getUser(m.getNickname()).setNick(m.getContent());
 	}
 
 	@Override	
 	public void onNotice() {
 		// TODO Auto-generated method stub
 
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
 			cw.getListener().onNewPrivMessage(
 					connection.getUser(m.getNickname()), host, m.getParams()[0], 
 					m.getNickname(), m.getContent());		
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
 			cw.getListener().onJoinServer(m.getServerName());
 
 		} else if(numCode == IRCUtil.RPL_NAMREPLY) {
 			System.out.println("RPL_NAMERPKY");
 			String[] nicks = m.getContent().split(" ");
 			for(String n : nicks)
 			{
 				cw.getListener().onUserJoin(connection.getHost(), m.getParams()[2], n);
 				if(!n.equals(connection.getNick()))
 					connection.getUsers().add(new User(n,false));
 				else
 					connection.getUsers().add(new User(n,true));
 			}
 		} else if(numCode == IRCUtil.RPL_TOPIC) {
 			cw.getListener().onNewTopic(m.getPrefix(), m.getParams()[1], m.getContent());
 			cw.getListener().onNewMessage(m.getPrefix(), m.getParams()[1], "<Topic> " + m.getContent());
 		} else if(numCode == IRCUtil.RPL_NOWAWAY) {
 
 		}
 
 		//}
 
 
 	}
 
 	@Override
 	public void onTopic(String host, Message m) {
 		cw.getListener().onNewTopic(host, m.getParams()[0], m.getContent());
 	}
 
 	@Override
 	public void onUnknown(String host, String line) {
 		cw.getListener().onNewMessage(host, host, line);		
 	}
 }
