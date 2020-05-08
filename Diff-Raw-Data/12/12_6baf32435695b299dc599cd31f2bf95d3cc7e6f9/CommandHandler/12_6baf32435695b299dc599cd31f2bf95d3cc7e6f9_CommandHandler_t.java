 package com.cole2sworld.ColeBans.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import com.cole2sworld.ColeBans.framework.PermissionSet;
 /**
  * Static command handler. For handling traditional /&lt;cmd&gt; commands instead of the dynamic /cb &lt;cmd&gt; [args] commands.
  *
  */
 public class CommandHandler {
 	public static boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		String error = ChatColor.YELLOW+"An unspecified error has occurred while running this command.";
 		PermissionSet pset = new PermissionSet(sender);
 		if (cmdLabel.equalsIgnoreCase("ban")) {
 			if (pset.canBan) {
 				String result = new Ban().run(args, sender);
 				if (result != null) return true;
 				else error = result;
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		else if (cmdLabel.equalsIgnoreCase("tempban")) {
 			if (pset.canTempBan) {
 				String result = new Tempban().run(args, sender);
 				if (result != null) return true;
 				else error = result;
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		else if (cmdLabel.equalsIgnoreCase("unban")) {
 			if (pset.canUnBan) {
 				String result = new Unban().run(args, sender);
 				if (result != null) return true;
 				else error = result;
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
		else if (cmdLabel.equalsIgnoreCase("lookup") || cmdLabel.equalsIgnoreCase("check")) {
 			if (pset.canLookup) {
 				String result = new Check().run(args, sender);
 				if (result != null) return true;
 				else error = result;
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		else if (cmdLabel.equalsIgnoreCase("kick")) {
 			if (pset.canKick) {
 				String result = new Kick().run(args, sender);
 				if (result != null) return true;
 				else error = result;
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		sender.sendMessage(error);
 		return true;
 	}
 }
