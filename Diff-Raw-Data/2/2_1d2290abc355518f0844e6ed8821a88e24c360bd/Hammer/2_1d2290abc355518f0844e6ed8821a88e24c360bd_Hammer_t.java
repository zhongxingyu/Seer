 package com.cole2sworld.ColeBans.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.cole2sworld.ColeBans.framework.GlobalConf;
 import com.cole2sworld.ColeBans.framework.PermissionSet;
 
 public class Hammer implements CBCommand {
 	public String run(String[] args, CommandSender admin) throws Exception {
 		if (!new PermissionSet(admin).canBanhammer) return ChatColor.RED+"You don't have permission to do that.";
 		if (admin instanceof Player) {
			((Player)admin).getInventory().addItem(new ItemStack(Material.valueOf(GlobalConf.get("banhammer.type").asString()), 1, Short.MIN_VALUE));
 			admin.sendMessage(ChatColor.AQUA+"Left click: "+GlobalConf.get("banhammer.leftClickAction").asString().toLowerCase()+"; "+"Right click: "+GlobalConf.get("banhammer.rightClickAction").asString().toLowerCase());
 		} else {
 			return ChatColor.RED+"You don't have an inventory.";
 		}
 		return null;
 	}
 }
