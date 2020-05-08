 //
 //  CMDChatPrivate.java
 //  ChatServer
 //
 //  Created by John McKisson on 4/2/08.
 //  Copyright 2008 __MyCompanyName__. All rights reserved.
 //
 package com.presence.chat.commands;
 
 import java.util.*;
 import java.util.logging.*;
 
 import com.presence.chat.*;
 
 /**
  * Send a private message to another client
  * They may be in a different room
  */
 public class CMDChatPrivate implements Command {
 	private static final Logger log = Logger.getLogger("global");
 
 	public String help() {
 		return "Send a private message to another user";
 	}
 	
 	public String usage() {
 		return String.format(ChatServer.USAGE_STRING, "pmsg <person> <msg>");
 	}
 	
 
 	public boolean execute(ChatClient sender, String[] args) {
 		
 		if (args.length < 2) {
 			sender.sendChat(usage());
 			return true;
 		}
 		
 		String[] chatArgs = args[1].split(" ", 2);
 		
 		if (chatArgs.length < 2) {
 			sender.sendChat(usage());
 			return true;
 		}
 		
 		//Ok we seem to have a good target name and a message, now find that person
 		ChatClient targ = ChatServer.getClientByName(chatArgs[0]);
 		
 		if (targ != null) {
 			if (chatArgs.length < 2) {
 				sender.sendChat("Send them what message?");
 				return true;
 			}
 		
 			//Check gag list
 			if (targ.getAccount().hasGagged(sender.getAccount().getName())) {
 				sender.sendChat(String.format("%s has gagged you!", targ.getName()));
 				return true;
 			}
 
 			String senderStr = String.format("You chat to %s, '%s'", chatArgs[0], chatArgs[1]);
 			String targetStr = String.format("%s chats to you, '%s'", sender.getName(), chatArgs[1]);
 
 			//Send chats
 			targ.sendChat(targetStr);
 			sender.sendChat(senderStr);
 			
 			//Log them too
 			sender.getMessageLog().addEntry(senderStr);
 			targ.getMessageLog().addEntry(targetStr);
 			
 			ChatServer.getStats().pchats++;
 			
 		} else {
			sender.sendChat(String.format("%s chats to you, 'Sorry, I did not find anyone named %s.'", ChatPrefs.getName(), chatArgs[0]));
 		}
 		
 		return true;
 	}
 }
