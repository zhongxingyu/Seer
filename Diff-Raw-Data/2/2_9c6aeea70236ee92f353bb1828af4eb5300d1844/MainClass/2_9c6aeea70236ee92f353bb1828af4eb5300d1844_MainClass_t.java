 package com.mctoybox.onetimecode;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MainClass extends JavaPlugin {
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length == 0) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("You need to be a player to do that!");
 				return true;
 			}
 			Player p = (Player) sender;
 			if (p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
 				if (!p.hasPermission("otc.use")) {
 					p.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					return false;
 				}
 				BookMeta bMeta = (BookMeta) p.getItemInHand().getItemMeta();
 				if (bMeta.hasAuthor() && bMeta.getAuthor().equals("One Time Code")) {
 					Bukkit.dispatchCommand(getServer().getConsoleSender(), bMeta.getPage(2).replaceAll("%player%", p.getName()));
 					p.setItemInHand(null);
 					p.sendMessage(ChatColor.GREEN + "OneTimeCode book used!");
 					Bukkit.broadcast(ChatColor.GRAY + "" + ChatColor.ITALIC + "[" + p.getName() + " used a one time code!]", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 					Bukkit.broadcast(ChatColor.GRAY + "" + ChatColor.ITALIC + "[Command: " + bMeta.getPage(2) + "]", Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 					
 				}
 				else
 					p.sendMessage(ChatColor.RED + "That is not a OneTimeCode book!");
 			}
 			else
 				p.sendMessage(ChatColor.RED + "That is not a OneTimeCode book!");
 			
 			return true;
 		}
 		if (args.length == 1) {
 			sender.sendMessage(ChatColor.RED + "No command was specified!");
 			return true;
 		}
 		if (!sender.hasPermission("otc.create")) {
 			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
 			return true;
 		}
 		
 		Player p = getServer().getPlayer(args[0]);
 		
 		if (p == null) {
 			sender.sendMessage(ChatColor.RED + "That player could not be found!");
 			return true;
 		}
 		ItemStack newBook = new ItemStack(387);
 		
 		ItemMeta iMeta = newBook.getItemMeta();
 		
 		BookMeta meta = (BookMeta) iMeta;
 		meta.setAuthor("One Time Code");
 		
 		String usage = "To use this book:\nHold it and use /otc";
 		
 		String temp = "";
 		for (int i = 1; i < args.length; i++) {
 			temp += args[i] + " ";
 		}
 		meta.setPages(usage, temp);
 		
 		meta.setTitle("One Time Code");
 		meta.setDisplayName(meta.getTitle());
 		
 		newBook.setItemMeta(meta);
 		
 		p.getInventory().addItem(newBook);
 		if ((sender instanceof Player) && !((Player) sender).equals(p)) {
 			sender.sendMessage(ChatColor.GREEN + "You have granted " + p.getDisplayName() + " a OneTimeCode book!");
 		}
 		else if (!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.GREEN + "You have granted " + p.getDisplayName() + " a OneTimeCode book!");
 		}
 		p.sendMessage(ChatColor.GREEN + "You have receieved a OneTimeCode book!");
 		return true;
 	}
 }
