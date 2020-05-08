 package org.ukiuni.irc4j.server.command;
 
 import java.util.List;
 
 import org.ukiuni.irc4j.IRCEventHandler;
 import org.ukiuni.irc4j.User;
 import org.ukiuni.irc4j.server.ClientConnection;
 import org.ukiuni.irc4j.server.IRCServer;
 import org.ukiuni.irc4j.server.ServerCommand;
 
 public class ReceiveNickCommand extends ServerCommand {
 
 	@Override
 	public void execute(IRCServer ircServer, ClientConnection selfClientConnection, List<IRCEventHandler> handlers) throws Throwable {
 		String newNickName = getCommandParameters()[0];
 		if ((!newNickName.equals(selfClientConnection.getNickName())) && ircServer.hasConnection(newNickName) || newNickName.equals(ircServer.getServerName())) {
 			selfClientConnection.sendCommand("433 " + selfClientConnection.getNickName() + " :nickname " + newNickName + " aleady exists.");
 			return;
 		}
 		if (User.isWrongNickName(newNickName)) {
 			selfClientConnection.sendCommand("432 " + selfClientConnection.getNickName() + " :nickname " + newNickName + " has wrong char.");
 			return;
 		}
 
 		//TODO use irc password for registed account
 		/*
 		User registedUser = Database.getInstance().loadUser(newNickName);
 		if (null != registedUser && (null == selfClientConnection.getUser().getPasswordHashed() || !selfClientConnection.getUser().getPasswordHashed().equals(registedUser.getPasswordHashed()))) {
 			selfClientConnection.sendCommand("464 " + selfClientConnection.getNickName() + " :Password incorrect.");
 			return;
 		}*/
 		String oldNickName = selfClientConnection.getNickName();
 
 		selfClientConnection.setNickName(newNickName);
 		if (null != oldNickName) {
 			String newNickCommand = ":" + oldNickName + "!" + selfClientConnection.getUser().getName() + "@" + selfClientConnection.getUser().getHostName() + " NICK :" + selfClientConnection.getNickName();
 			selfClientConnection.send(newNickCommand);
 			ircServer.sendToSameChannelUser(selfClientConnection, newNickCommand);
 		}
 		ircServer.putConnection(selfClientConnection);
		if (null != selfClientConnection.getUser().getName() && !selfClientConnection.isServerHelloSended()) {
 			ircServer.sendServerHello(selfClientConnection);
 		}
 	}
 }
