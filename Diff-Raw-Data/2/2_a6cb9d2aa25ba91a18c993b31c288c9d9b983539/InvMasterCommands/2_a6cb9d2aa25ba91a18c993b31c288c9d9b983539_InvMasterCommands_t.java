 package com.deaboy.invmaster;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 
 public class InvMasterCommands implements CommandExecutor
 {
 	public InvMasterCommands()
 	{
 		Bukkit.getPluginCommand("inventory").setExecutor(this);
 		Bukkit.getPluginCommand("enderchest").setExecutor(this);
 		Bukkit.getPluginCommand("armor").setExecutor(this);
 		Bukkit.getPluginCommand("listplayers").setExecutor(this);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String c, String[] args)
 	{
 		if (cmd.getName().equalsIgnoreCase("inventory")) // View a player's enderchest.
 		{
 			if (!(sender instanceof Player))
 			{
 				sender.sendMessage("The server cannot do that!");
 				return true;
 			}
 			if (!sender.isOp())
 			{
 				sender.sendMessage("You don't have permission to do that!");
 				return true;
 			}
 			if (args.length != 1)
 			{
 				return false;
 			}
 
 			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
 			Inventory inv = InvMaster.getPlayerInventory(p);
 			
 			if (inv == null)
 			{
 				if (!InvMaster.openPlayerFile(p))
 				{
 					sender.sendMessage("That player does not exist.");
 					return true;
 				}
 				else
 				{
 					inv = InvMaster.getPlayerInventory(p);
 				}
 			}
 			
 			((Player) sender).openInventory(inv);
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("enderchest"))
 		{
 			if (!(sender instanceof Player))
 			{
 				sender.sendMessage("The server cannot do that!");
 				return true;
 			}
 			if (!sender.isOp())
 			{
 				sender.sendMessage("You don't have permission to do that!");
 				return true;
 			}
 			if (args.length != 1)
 			{
 				return false;
 			}
 
 			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
 			Inventory inv = InvMaster.getPlayerEnderchest(p);
 			
 			if (inv == null)
 			{
 				if (!InvMaster.openPlayerFile(p))
 				{
 					sender.sendMessage("That player does not exist.");
 					return true;
 				}
 				else
 				{
 					inv = InvMaster.getPlayerEnderchest(p);
 				}
 			}
 			
 			((Player) sender).openInventory(inv);
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("armor"))
 		{
 			if (!(sender instanceof Player))
 			{
 				sender.sendMessage("The server cannot do that!");
 				return true;
 			}
 			if (!sender.isOp())
 			{
 				sender.sendMessage("You don't have permission to do that!");
 				return true;
 			}
 			if (args.length != 1)
 			{
 				return false;
 			}
 
 			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
 			Inventory inv = InvMaster.getPlayerArmor(p);
 			
 			if (inv == null)
 			{
 				if (!InvMaster.openPlayerFile(p))
 				{
 					sender.sendMessage("That player does not exist.");
 					return true;
 				}
 				else
 				{
 					inv = InvMaster.getPlayerArmor(p);
 				}
 			}
 			
 			((Player) sender).openInventory(inv);
 			
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("listplayers"))
 		{
 			if (!sender.isOp())
 			{
 				sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
 				return true;
 			}
 			
 			int page;
 			int perpage = 8;
 			List<String> names = new ArrayList<String>();
 			
 			if (args.length >= 1)
 			{
 				try
 				{
					page = Integer.parseInt(args[0]);
 					if (page < 1)
 						page = 1;
 				}
 				catch (NumberFormatException e)
 				{
 					page = 1;
 				}
 			}
 			else
 			{
 				page = 1;
 			}
 			
 			page--;
 			
 			File folder = new File(Bukkit.getWorlds().get(0).getWorldFolder() + "/players");
 			if (!folder.isDirectory())
 			{
 				sender.sendMessage("There was a problem listing the files!");
 				return true;
 			}
 			for (File file : folder.listFiles())
 			{
 				if (file.getName().endsWith(".dat"))
 					names.add(file.getName().substring(0, file.getName().length()-4));
 			}
 			
 			if (names.size() == 0)
 			{
 				sender.sendMessage("It appears that no player files exist in the default world's \"players\" directory...");
 				return true;
 			}
 			
 			
 			if (page*perpage > names.size())
 				page = names.size() / perpage;
 			
 			sender.sendMessage("Currently saved player data files (" + (page+1) + "/" + ((names.size()/perpage)+1) + ")");
 			
 			names = names.subList(page*perpage, (page+1)*perpage > names.size() ? names.size() : (page+1)*perpage);
 			
 			for (String name : names)
 			{
 				sender.sendMessage("  " + name);
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 }
