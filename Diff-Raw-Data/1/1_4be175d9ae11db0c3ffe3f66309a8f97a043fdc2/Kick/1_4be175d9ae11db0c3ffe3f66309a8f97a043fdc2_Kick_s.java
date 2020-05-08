 package com.cole2sworld.ColeBans.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.cole2sworld.ColeBans.GlobalConf;
 import com.cole2sworld.ColeBans.Main;
 import com.cole2sworld.ColeBans.framework.PlayerOfflineException;
 /**
  * The Kick command. Handles kicking players through commands.
  *
  */
 final class Kick implements CBCommand {
 	@Override
 	public String run(String[] args, CommandSender admin) {
 		String error = null;
 		if (args.length < 1) error = ChatColor.RED+"You must specify a player, or a player and a reason.";
 		else {
 			String victim = args[0];
 			String reason;
 			StringBuilder reasonBuilder = new StringBuilder();
 			if (args.length > 1) {
 				reasonBuilder.append(args[1]);
 				for (int i = 2; i<args.length; i++) {
 					reasonBuilder.append(" ");
 					reasonBuilder.append(args[i]);
 				}
 				reason = reasonBuilder.toString();
 			} else {
 				reason = null;
 			}
 			try {
 				Main.instance.kickPlayer(victim, reason);
				if (GlobalConf.announceBansAndKicks) Main.instance.server.broadcastMessage(ChatColor.valueOf(GlobalConf.kickColor)+victim+" was kicked! ["+reason+"]");
 			} catch (PlayerOfflineException e) {
 				error = ChatColor.DARK_RED+victim+" is not online!";
 			}
 		}
 		return error;
 	}
 }
