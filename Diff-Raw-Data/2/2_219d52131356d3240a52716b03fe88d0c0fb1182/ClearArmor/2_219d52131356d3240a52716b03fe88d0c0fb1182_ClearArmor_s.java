 package com.nullblock.vemacs.cleararmor;
 
 // forked from Wundark/RemoveArmour
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ClearArmor extends JavaPlugin
 {
 	String bad = ChatColor.GOLD + "[ClearArmor] " + ChatColor.RED;
 
 	@SuppressWarnings("deprecation")
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
 	{
 		if(command.getName().equalsIgnoreCase("cleararmor")){
			if(sender.hasPermission("cleararmor.use") && args.length != 1)
 			{
 				Player player = getServer().getPlayer(args[0]);
 
 				if(player != null)
 				{
 					player.getInventory().setArmorContents(new ItemStack[4]); // setting null is a terrible idea
 					player.updateInventory(); // force inventory update to client
 				}			
 				sender.sendMessage(bad + "Cleared the armor inventory of " + player.getDisplayName());
 				return true;
 			}
 		}
 		return false;
 	}
 }
