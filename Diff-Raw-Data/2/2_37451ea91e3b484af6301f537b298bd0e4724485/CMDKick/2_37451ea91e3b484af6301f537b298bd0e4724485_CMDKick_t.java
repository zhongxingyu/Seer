 //
 //  CMDKick.java
 //  ChatServer
 //
 //  Created by John McKisson on 4/15/08.
 //  Copyright 2008 __MyCompanyName__. All rights reserved.
 //
 package com.presence.chat.commands;
 
 import java.util.*;
 import java.util.logging.*;
 
 import com.presence.chat.*;
 
 import static com.presence.chat.ANSIColor.*;
 
 public class CMDKick implements Command {
 
 	public String help() {
 		return "Kick a user from the server";
 	}
 	
 	public String usage() {
 		return String.format(ChatServer.USAGE_STRING, "kick <name> [<message>]");
 	}
 
 	public boolean execute(ChatClient sender, String[] args) {
 	
 		if (args.length < 2) {
 			sender.sendChat(usage());
 			return true;
 		}
 
 		String[] kickArgs = args[1].split(" ", 2);
 		
 		String name = kickArgs[0]/*.toLowerCase()*/;
 		
 		//Check if dude is online
 		boolean found = false;
 		ChatClient c = null;
 		Iterator<ChatClient> it = ChatServer.getClients().iterator();
 		
 		while (it.hasNext()) {
 			c = it.next();
 			
 			//if (name.equals(c.getName().toLowerCase())) {
 			if (name.equalsIgnoreCase(c.getName())) {
 				found = true;
 				break;
 			}
 		}
 		
 		if (found) {
 			String kickMsg = "";
 			if (kickArgs.length > 1)
 				kickMsg = kickArgs[1];
 		
 			c.sendChat(String.format("You have been kicked [%s] by %s!", kickMsg, sender.getName()));
 			
			ChatServer.disconnectClient(c);
 			
 			//Global echo
 			ChatServer.echo(String.format("%s[%s%s%s] %s has been kicked [%s%s%s] by %s!", RED, WHT, ChatPrefs.getName(), RED, kickArgs[0], YEL, kickMsg, RED, sender.getName()));
 			
 			ChatServer.getStats().kicks++;
 			
 		} else {
 			sender.sendChat(String.format("There is noone named %s online.", args[1]));
 		}
 		
 		return true;
 	}
 }
