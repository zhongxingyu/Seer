 package com.cole2sworld.ColeBans.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.cole2sworld.ColeBans.GlobalConf;
 import com.cole2sworld.ColeBans.Main;
 import com.cole2sworld.ColeBans.framework.PermissionSet;
 import com.cole2sworld.ColeBans.handlers.YAMLBanHandler;
 
 public class Reload extends CBCommand {
 
 	@Override
 	public String run(String[] args, CommandSender admin) {
 		PermissionSet pset = new PermissionSet(admin);
 		if (!pset.canReload) return ChatColor.RED+"You do not have permission to do that.";
 		Main.instance.reloadConfig();
 		GlobalConf.loadConfig();
 		if (Main.instance.banHandler instanceof YAMLBanHandler) {
 			((YAMLBanHandler) Main.instance.banHandler).reload();
 		}
		admin.sendMessage("Configuration reloaded.");
 		return null;
 	}
 
 }
