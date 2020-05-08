 package com.test9.irc.display;
 
 import com.test9.irc.engine.IRCConnection;
 import com.test9.irc.engine.User;
 
 public interface Listener {
 	
 	public void onJoinChannel(String server, String channel);
 	public void onJoinServer(String server);
 	public void onLeaveServer(String server);
 	public void onNewIRCConnection(IRCConnection connection);
 	public void onNewHighlight(String host, String string, String string2, String string3);
 	public void onNewMessage(String server, String channel, String message, String command);
 	public void onNewPrivMessage(User user, String server, String channel, String nick, String message, boolean isLocal);
 	public void onNewTopic(String server, String channel, String topic);
 	public void onNewUserMode(String server, String channel, String mode);
 	public void onNickChange(String host, String oldNick, String newNick);
 	public void onNotice(String server, String params, String content);
 	public void onPartChannel(String server, String channel);
	public void onTerminalMessage(String host, String channel, String nick, String message);
 	public void onUserJoin(String server, String channel, String nick, boolean isUserRply);
 	public void onUserPart(String server, String channel, String nick);
 	public void onUserQuit(String server, String nick, String reason);
 	
 
 }
