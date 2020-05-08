 package com.alta189.chavaadmin;
 
 import java.util.StringTokenizer;
 
 import com.alta189.chavabot.ChavaManager;
 import com.alta189.chavaperms.ChavaPerms;
 
 public class CommandParser {
 	
 	private static ChavaAdmin core;
 	
 	public CommandParser(ChavaAdmin core) {
 		CommandParser.core = core;
 	}
 
 	public static void parse(String message, String sender) {
 		CommandParser.parse(message, sender, null);
 	}
 
 	public static void parse(String message, String sender, String channel) {
 		if (!message.startsWith("."))
 			return;
 		StringTokenizer tokens = new StringTokenizer(message);
 		String cmd = tokens.nextToken().substring(1);
 		if (cmd.equalsIgnoreCase("disconnect")) {
 			if (ChavaPerms.getPermsManager().hasPerms(sender, "admin.disconnect") || sender.equalsIgnoreCase("alta189")) { // sender.equalsIgnoreCase("alta189") is just for debugging and will be removed shortly
 				if (tokens.hasMoreTokens()) {
 					StringBuilder msg = new StringBuilder();
 					String word = tokens.nextToken();
 					msg.append(word);
 					while (tokens.hasMoreTokens()) {
 						msg.append(" ");
 						word = tokens.nextToken();
 						msg.append(word);
 					}
 				}
 				ChavaManager.getInstance().getChavaBot().disconnect();
 			}
 		} else if (cmd.equalsIgnoreCase("join") || cmd.equalsIgnoreCase("j")) {
 			if (ChavaPerms.getPermsManager().hasPerms(sender, "admin.join")) {
 				String chan = "";
 				while (tokens.hasMoreTokens()) {
 					chan = tokens.nextToken();
 					if (channel != null) {
 						ChavaManager.getInstance().getChavaBot().sendMessage(channel, Responses.JOIN_CHANNEL.replace("%chan%", chan));
 					} else {
 						ChavaManager.getInstance().getChavaBot().sendMessage(sender, Responses.JOIN_CHANNEL.replace("%chan%", chan));
 					}
 					ChavaAdmin.log(Responses.LOG_JOIN.replace("%sender%", sender).replace("%chan%", chan));
 					ChavaManager.getInstance().getChavaBot().joinChannel(chan);
 				}
 			}
 		} else if (cmd.equalsIgnoreCase("part") || cmd.equalsIgnoreCase("p")) {
 			if (ChavaPerms.getPermsManager().hasPerms(sender, "admin.part")) {
 				String chan = "";
 				while (tokens.hasMoreTokens()) {
 					chan = tokens.nextToken();
 					if (channel != null) {
 						ChavaManager.getInstance().getChavaBot().sendMessage(channel, Responses.PART_CHANNEL.replace("%chan%", chan));
 					} else {
 						ChavaManager.getInstance().getChavaBot().sendMessage(sender, Responses.PART_CHANNEL.replace("%chan%", chan));
 					}
 					ChavaAdmin.log(Responses.LOG_PART.replace("%sender%", sender).replace("%chan%", chan));
 					ChavaManager.getInstance().getChavaBot().partChannel(chan);
 				}
 			}
 		} else if (cmd.equalsIgnoreCase("mute")) {
 			if (ChavaPerms.getPermsManager().hasPerms(sender, "admin.mute")) {
 				String chan = "";
 				while (tokens.hasMoreTokens()) {
 					chan = tokens.nextToken();
 					ChavaManager.getInstance().getChavaBot().sendNotice(sender, Responses.MUTE_CHANNEL.replace("%chan%", chan));
 					ChavaAdmin.log(Responses.LOG_MUTE.replace("%sender%", sender).replace("%chan%", chan));
 					core.muteChannel(chan);
 				}
 			}
 		} else if (cmd.equalsIgnoreCase("unmute")) {
 			if (ChavaPerms.getPermsManager().hasPerms(sender, "admin.unmute")) {
 				String chan = "";
 				while (tokens.hasMoreTokens()) {
 					chan = tokens.nextToken();
 					ChavaManager.getInstance().getChavaBot().sendNotice(sender, Responses.MUTE_CHANNEL.replace("%chan%", chan));
 					ChavaAdmin.log(Responses.LOG_UNMUTE.replace("%sender%", sender).replace("%chan%", chan));
					core.muteChannel(chan);
 				}
 			}
 		}
 	}
 
 }
