 package org.irc4j.server;
 
 import java.util.List;
 
 import org.irc4j.IRCEventHandler;
 import org.irc4j.Message;
 
 public class ReceiveHistoryCommand extends ServerCommand {
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void execute(IRCServer ircServer, ClientConnection selfClientConnection, List<IRCEventHandler> handlers) throws Throwable {
 		if (1 < getCommandParameters().length) {
 			selfClientConnection.sendPrivateCommand("channel and history length must be specified");
 		}
 		String targetChannel = getCommandParameters()[0];
 		ServerChannel channel = ircServer.getChannel(targetChannel);
 		if (channel == null) {
 			selfClientConnection.sendPrivateCommand("No such channel " + targetChannel);
 			return;
 		} else if (!channel.getUserList().contains(selfClientConnection.getUser())) {
 			selfClientConnection.sendPrivateCommand("You are not joined to " + targetChannel);
 			return;
 		}
 		int length = 10;
 		try {
 			length = Integer.valueOf(getCommandParameters()[1]);
 		} catch (Exception e) {
 		}
 		List<Message> messages = channel.getHistory(length);
 		for (Message message : messages) {
			selfClientConnection.sendPrivateCommand(message.getDate().getHours() + ":" + message.getDate().getMinutes() + " " + message.getSenderNickName() + ": " + message.getMessage());
 		}
 
 	}
 
 }
