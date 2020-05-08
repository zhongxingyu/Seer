 package com.mccraftaholics.warpportals.commands;
 
 import org.bukkit.command.CommandSender;
 
 import com.mccraftaholics.warpportals.bukkit.CommandHandler;
 import com.mccraftaholics.warpportals.bukkit.CommandHandler.CommandHandlerObject;
 import com.mccraftaholics.warpportals.objects.PortalInfo;
 
 public class CmdPortalDelete extends CommandHandlerObject {
 	public static boolean handle(CommandSender sender, String[] args, CommandHandler main) {
 		if (args.length == 1) {
 			try {
 				PortalInfo portal = main.mPortalManager.getPortalInfo(args[0]);
 				if (portal != null) {
 					main.mPortalManager.deletePortal(args[0]);
 				} else {
 					sender.sendMessage(main.mCC + args[0] + " is not a valid Portal name.");
 				}
 			} catch (Exception e) {
 				sender.sendMessage(main.mCC + "Error saving Portal destination");
 			}
 		} else
			sender.sendMessage(main.mCC + "/pdelete [portalName]");
 		return true;
 	}
 }
