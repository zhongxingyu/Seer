 package org.ukiuni.irc4j.server.command;
 
 import java.util.List;
 
 import org.ukiuni.irc4j.IRCEventHandler;
 import org.ukiuni.irc4j.server.ClientConnection;
 import org.ukiuni.irc4j.server.IRCServer;
 import org.ukiuni.irc4j.server.ServerChannel;
 import org.ukiuni.irc4j.server.ServerCommand;
 
 public class ReceivePartCommand extends ServerCommand {
 
 	@Override
 	public void execute(IRCServer ircServer, ClientConnection selfClientConnection, List<IRCEventHandler> handlers) throws Throwable {
 		String[] channels = getCommandParameters()[0].split(",");
 		for (String channelName : channels) {
 			ServerChannel channel = selfClientConnection.getJoinedChannel(channelName);
 			if (channelName == null) {
 				selfClientConnection.sendCommand("You're not a member of the channel " + channelName + ", so you can't part it.");
 			} else {
				channel.part(selfClientConnection);
 				selfClientConnection.partFromChannel(channelName);
 			}
 		}
 	}
 }
