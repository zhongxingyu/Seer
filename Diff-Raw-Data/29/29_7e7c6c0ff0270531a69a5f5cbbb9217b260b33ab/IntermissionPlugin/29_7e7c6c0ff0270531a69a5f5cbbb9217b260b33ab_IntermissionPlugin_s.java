 package eu.ha3.bukkit.intermission;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*
             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                     Version 2, December 2004 
 
  Copyright (C) 2004 Sam Hocevar <sam@hocevar.net> 
 
  Everyone is permitted to copy and distribute verbatim or modified 
  copies of this license document, and changing it is allowed as long 
  as the name is changed. 
 
             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 
 
   0. You just DO WHAT THE FUCK YOU WANT TO. 
 */
 
 public class IntermissionPlugin extends JavaPlugin
 {
 	public class DisplayNow implements CommandExecutor
 	{
 		@Override
 		public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 		{
 			if (args.length == 0)
 				return false;
 			
 			String vendorName = args[0];
 			
 			if (IntermissionPlugin.this.vendors.containsKey(vendorName)
 				|| IntermissionPlugin.this.vendors.containsKey(vendorName.toLowerCase()))
 			{
 				Vendor vendor = IntermissionPlugin.this.vendors.get(vendorName);
 				if (vendor == null)
 				{
 					vendor = IntermissionPlugin.this.vendors.get(vendorName.toLowerCase());
 				}
 				vendor.displayNow();
 			}
 			else
 			{
 				sender.sendMessage("No such vendor.");
 			}
 			
 			return true;
 		}
 		
 	}
 	
 	public class Reloader implements CommandExecutor
 	{
 		@Override
 		public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 		{
 			if (command.getName().equals("intermission_reloadconfig"))
 			{
 				reload();
 			}
 			else if (command.getName().equals("intermission_reloadmessages"))
 			{
 				reloadMessages();
 			}
 			
 			return true;
 		}
 		
 	}
 	
 	public class Info implements CommandExecutor
 	{
 		@Override
 		public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 		{
			if (!(sender instanceof Player))
				return true;
			
 			if (args.length == 0)
 				return false;
 			
			Player player = (Player) sender;
			ConfigPlayer ply = ensureConfigPlayer(player);
 			String vendorName = args[0];
			
 			if (IntermissionPlugin.this.vendors.containsKey(vendorName)
 				|| IntermissionPlugin.this.vendors.containsKey(vendorName.toLowerCase()))
 			{
				Vendor vendor = IntermissionPlugin.this.vendors.get(vendorName);
 				if (vendor == null)
 				{
 					vendor = IntermissionPlugin.this.vendors.get(vendorName.toLowerCase());
 				}
 				
 				if (vendor.couldPlayerReceive(player))
 				{
 					if (vendor.canPlayerSubscribe(player))
 					{
 						if (vendor.shouldPlayerReceive(ply))
 						{
 							player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
 								getConfig().getString("globalconfig.display.on_info_subbed"), vendor.getName(),
 								vendor.getDescription())));
 						}
 						else
 						{
 							player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
 								getConfig().getString("globalconfig.display.on_info_unsubbed"), vendor.getName(),
 								vendor.getDescription())));
 						}
 					}
 					else
 					{
 						player.sendMessage(ChatColor.translateAlternateColorCodes(
 							'&',
 							String.format(
 								getConfig().getString("globalconfig.display.on_info_forced"), vendor.getName(),
 								vendor.getDescription())));
 					}
 				}
 				else
 				{
 					player.sendMessage(ChatColor.translateAlternateColorCodes(
 						'&', String.format(getConfig().getString("globalconfig.display.on_info_invalid"))));
 				}
 			}
 			
 			return true;
 		}
 		
 	}
 	
 	public class Subscriber implements CommandExecutor
 	{
 		@Override
 		public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 		{
 			if (!(sender instanceof Player))
 				return true;
 			
 			if (args.length == 0)
 				return false;
 			
 			Player player = (Player) sender;
 			ConfigPlayer ply = ensureConfigPlayer(player);
 			
 			String vendorName = args[0];
 			if (IntermissionPlugin.this.vendors.containsKey(vendorName)
 				|| IntermissionPlugin.this.vendors.containsKey(vendorName.toLowerCase()))
 			{
 				Vendor vendor = IntermissionPlugin.this.vendors.get(vendorName);
 				if (vendor == null)
 				{
 					vendor = IntermissionPlugin.this.vendors.get(vendorName.toLowerCase());
 				}
 				
 				if (vendor.canPlayerSubscribe(player))
 				{
 					if (command.getName().equals("intermission_subscribe"))
 					{
 						ply.subscribeTo(vendor.getName());
 						player
 							.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
 								getConfig().getString("globalconfig.display.on_subscribe"), vendor.getName(),
 								vendor.getDescription())));
 					}
 					else if (command.getName().equals("intermission_unsubscribe"))
 					{
 						ply.unsubscribeFrom(vendor.getName());
 						player.sendMessage(ChatColor.translateAlternateColorCodes(
 							'&',
 							String.format(
 								getConfig().getString("globalconfig.display.on_unsubscribe"), vendor.getName(),
 								vendor.getDescription())));
 					}
 				}
 				else
 				{
 					player.sendMessage(ChatColor.translateAlternateColorCodes(
 						'&', String.format(getConfig().getString("globalconfig.display.on_invalid"))));
 				}
 			}
 			else
 			{
 				player.sendMessage(ChatColor.translateAlternateColorCodes(
 					'&', String.format(getConfig().getString("globalconfig.display.on_invalid"))));
 			}
 			
 			return true;
 		}
 		
 	}
 	
 	public class Lister implements CommandExecutor
 	{
 		@Override
 		public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 		{
 			if (sender instanceof ConsoleCommandSender)
 			{
 				StringBuilder sb = new StringBuilder();
 				for (Vendor vendor : IntermissionPlugin.this.vendors.values())
 				{
 					if (vendor.isRunning())
 					{
 						sb.append("&b");
 					}
 					else
 					{
 						sb.append("&c");
 					}
 					sb.append(vendor.getName());
 					sb.append("&f");
 					
 					int enrolled = 0;
 					int total = 0;
 					for (Player ply : getServer().getOnlinePlayers())
 					{
 						if (vendor.couldPlayerReceive(ply))
 						{
 							total = total + 1;
 							if (vendor.shouldPlayerReceive(ensureConfigPlayer(ply)))
 							{
 								enrolled = enrolled + 1;
 							}
 						}
 					}
 					
 					if (vendor.isSubscriptionBased())
 					{
 						sb.append("(" + enrolled + "/" + total + ")");
 					}
 					else
 					{
 						sb.append("(" + total + ")");
 					}
 					sb.append(" ");
 				}
 				
 				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));
 			}
 			else if (sender instanceof Player)
 			{
 				Player player = (Player) sender;
 				ConfigPlayer ply = ensureConfigPlayer(player);
 				
 				StringBuilder sb = new StringBuilder();
 				for (Vendor vendor : IntermissionPlugin.this.vendors.values())
 				{
 					if (vendor.couldPlayerReceive(player))
 					{
 						if (vendor.canPlayerSubscribe(player))
 						{
 							if (ply.hasChosenSubscription(vendor.getName()))
 							{
 								if (ply.isSubscribed(vendor.getName()))
 								{
 									sb.append("&b");
 								}
 								else
 								{
 									sb.append("&c");
 								}
 							}
 							else
 							{
 								if (vendor.shouldPlayerReceive(ply))
 								{
 									sb.append("&3");
 								}
 								else
 								{
 									sb.append("&4");
 								}
 							}
 						}
 						else
 						{
 							sb.append("&7");
 						}
 						sb.append(vendor.getName());
 						sb.append("&f ");
 					}
 				}
 				
 				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));
 			}
 			
 			return true;
 		}
 		
 	}
 	
 	private Map<String, Vendor> vendors;
 	private Map<Player, ConfigPlayer> players;
 	
 	private File playerFolder;
 	
 	@Override
 	public void onEnable()
 	{
 		this.vendors = new LinkedHashMap<String, Vendor>();
 		this.players = new HashMap<Player, ConfigPlayer>();
 		
 		this.playerFolder = new File(getDataFolder(), "players/");
 		this.playerFolder.mkdirs();
 		
 		Subscriber sub = new Subscriber();
 		Reloader reloader = new Reloader();
 		getCommand("intermission_list").setExecutor(new Lister());
 		getCommand("intermission_subscribe").setExecutor(sub);
 		getCommand("intermission_unsubscribe").setExecutor(sub);
 		getCommand("intermission_info").setExecutor(new Info());
 		getCommand("intermission_reloadconfig").setExecutor(reloader);
 		getCommand("intermission_reloadmessages").setExecutor(reloader);
 		getCommand("intermission_displaynow").setExecutor(new DisplayNow());
 		
 		reload();
 	}
 	
 	public void reloadMessages()
 	{
 		if (!getConfig().getBoolean("globalconfig.enable"))
 			return;
 		
 		for (Vendor vendor : this.vendors.values())
 		{
 			vendor.reloadMessages();
 		}
 		
 	}
 	
 	public void reload()
 	{
 		stopVendors();
 		this.vendors.clear();
 		
 		reloadConfig();
 		
 		ConfigurationSection vendorsMapSection = getConfig().getConfigurationSection("vendors");
 		for (String vendorName : vendorsMapSection.getKeys(false))
 		{
 			ConfigurationSection vendorSection = vendorsMapSection.getConfigurationSection(vendorName);
 			Vendor vendor = new Vendor(this, vendorName, vendorSection);
 			this.vendors.put(vendorName, vendor);
 		}
 		
 		if (!getConfig().getBoolean("globalconfig.enable"))
 			return;
 		startVendors();
 	}
 	
 	private void stopVendors()
 	{
 		for (Vendor vendor : this.vendors.values())
 		{
 			vendor.stopRunning();
 		}
 		
 	}
 	
 	private void startVendors()
 	{
 		for (Vendor vendor : this.vendors.values())
 		{
 			vendor.startRunning();
 		}
 		
 	}
 	
 	public void dispatchMessage(Vendor vendor, String message)
 	{
 		for (Player ply : getServer().getOnlinePlayers())
 		{
 			if (vendor.shouldPlayerReceive(ensureConfigPlayer(ply)))
 			{
 				ply.sendMessage(String.format(
 					message, ply.getName(), ply.getDisplayName(), ply.getWorld().getName(), ply
 						.getLocation().getBlockX(), ply.getLocation().getBlockY(), ply.getLocation().getBlockZ()));
 			}
 		}
 		
 		if (vendor.shouldDisplayOnConsole())
 		{
 			getServer().getConsoleSender().sendMessage(
 				String.format(message, "CONSOLE", "CONSOLE", "WORLD", "X", "Y", "Z"));
 		}
 		
 	}
 	
 	public ConfigPlayer ensureConfigPlayer(Player ply)
 	{
 		if (!this.players.containsKey(ply))
 		{
 			this.players.put(ply, new ConfigPlayer(ply, new File(this.playerFolder, ply.getName().toLowerCase()
 				+ ".yml")));
 		}
 		
 		return this.players.get(ply);
 		
 	}
 	
 }
