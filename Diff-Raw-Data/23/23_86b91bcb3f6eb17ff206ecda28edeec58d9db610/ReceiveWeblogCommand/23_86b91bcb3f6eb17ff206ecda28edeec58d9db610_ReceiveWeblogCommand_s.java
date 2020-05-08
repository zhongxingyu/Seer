 package org.ukiuni.irc4j.server.command;
 
 import java.util.Date;
 import java.util.List;
 
 import org.ukiuni.irc4j.Conf;
 import org.ukiuni.irc4j.IRCEventHandler;
 import org.ukiuni.irc4j.server.ClientConnection;
 import org.ukiuni.irc4j.server.IRCServer;
 import org.ukiuni.irc4j.server.ServerChannel;
 import org.ukiuni.irc4j.server.ServerCommand;
 import org.ukiuni.irc4j.util.CipherUtil;
 
 public class ReceiveWeblogCommand extends ServerCommand {
 
 	@Override
 	public void execute(IRCServer ircServer, ClientConnection selfClientConnection, List<IRCEventHandler> handlers) throws Throwable {
 		if (1 < getCommandParameters().length) {
 			selfClientConnection.sendPrivateCommand("channel and history length must be specified");
 		}
 		String channelName = getCommandParameters()[0];
 		if (channelName.startsWith(":")) {
 			channelName = channelName.substring(1);
 		}
 		ServerChannel channel = ircServer.getChannel(channelName);
 		if (channel == null) {
 			selfClientConnection.sendPrivateCommand("No such channel " + channelName);
 			return;
		} else if (!channel.getCurrentUserList().contains(selfClientConnection.getUser())) {
 			selfClientConnection.sendPrivateCommand("You are not joined to " + channelName);
 			return;
 		}
 		int length = 10;
 		try {
 			length = Integer.valueOf(getCommandParameters()[1]);
 		} catch (Exception e) {
 		}
 		long messagesId = channel.getMessageMaxId();
 		selfClientConnection.sendPrivateCommand("uri: " + Conf.getHttpServerURL() + "/logs/#" + CipherUtil.encode(channelName + " " + messagesId + " " + length + " " + selfClientConnection.getNickName() + " " + new Date().getTime()));
 
 	}
 
 }
