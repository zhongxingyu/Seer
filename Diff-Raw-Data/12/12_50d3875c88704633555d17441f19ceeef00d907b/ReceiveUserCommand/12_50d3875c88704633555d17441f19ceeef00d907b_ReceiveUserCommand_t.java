 package org.ukiuni.irc4j.server.command;
 
 import java.util.List;
 
 import org.ukiuni.irc4j.IRCEventHandler;
 import org.ukiuni.irc4j.server.ClientConnection;
 import org.ukiuni.irc4j.server.IRCServer;
 import org.ukiuni.irc4j.server.ServerCommand;
 
 public class ReceiveUserCommand extends ServerCommand {
 
 	@Override
 	public void execute(IRCServer ircServer, ClientConnection selfClientConnection, List<IRCEventHandler> handlers) throws Throwable {
 		String userName = getCommandParameters()[0];
 		if (getCommandParameters().length > 3) {
 			selfClientConnection.getUser().setDescription(getCommandParameters()[3]);
 		}
 		selfClientConnection.getUser().setName(userName);
		if (null != selfClientConnection.getNickName() && !selfClientConnection.isServerHelloSended()) {
 			ircServer.sendServerHello(selfClientConnection);
 		}
 	}
 }
