 package org.ukiuni.irc4j.server.command;
 
 import java.util.List;
 
 import org.ukiuni.irc4j.IRCEventHandler;
 import org.ukiuni.irc4j.server.ClientConnection;
 import org.ukiuni.irc4j.server.IRCServer;
 import org.ukiuni.irc4j.server.ServerCommand;
 
 public class ReceiveModeCommand extends ServerCommand {
 
 	@Override
 	public void execute(IRCServer ircServer, ClientConnection con, List<IRCEventHandler> handlers) throws Throwable {
 		if (getCommandParameters().length == 1) {
 			if (getCommandParameters()[0].startsWith("#")) {
 				con.sendCommand("324 " + con.getNickName() + " " + getCommandParameters()[0] + " +nt");
 			} else {
 				con.sendPrivateCommand("User mode query is not supported now.");
 			}
		} else if (getCommandParameters().length == 2 && (getCommandParameters()[1].equals("+b") || getCommandParameters()[1].equals("+e"))) {
 			if (getCommandParameters()[0].startsWith("#")) {// 368,349
 				if (getCommandParameters()[1].equals("+b")) {
 					con.sendCommand("368 " + con.getNickName() + " " + getCommandParameters()[0] + " :End of channel ban list");
				} else {
 					con.sendCommand("349 " + con.getNickName() + " " + getCommandParameters()[0] + " :End of channel exception list");
 				}
 			} else {
				con.sendCommand("mode setting is +b or +e.");
 			}
 		} else {
 			con.sendPrivateCommand("Specific modes not supported.");
 		}
 	}
 
 }
