 package com.live.toadbomb.QuickTravel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.TabCompleter;
 import org.bukkit.entity.Player;
 
 /**
  * Class which parses the /qt command line and hands control back to the main plugin class. Moved all this stuff
  * here to generate some breathing room in the main class.
  * 
  * @author Mumfrey
  */
 public class QuickTravelCommandHandler implements CommandExecutor, TabCompleter
 {
 	/**
 	 * List of command names, used for tab completion 
 	 */
 	public static final List<String> commands = new ArrayList<String>();
 
 	private QuickTravel plugin;
 	
 	public QuickTravelCommandHandler(QuickTravel plugin)
 	{
 		this.plugin = plugin;
 
 		// Assign all the plugin commands to this
 		for (String command : this.plugin.getDescription().getCommands().keySet())
 		{
 			PluginCommand pluginCommand = this.plugin.getCommand(command);
 			if (pluginCommand != null)
 			{
 				pluginCommand.setExecutor(this);
 //				pluginCommand.setTabCompleter(this.plugin);
 			}
 		}
 	}
 	
 	
 	@Override
 	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
 	{
 		if (args.length > 1)
 		{
 			if ("cfg".equalsIgnoreCase(args[0]) || "config".equalsIgnoreCase(args[0]))
 			{
 				return this.plugin.getOptions().onTabComplete(sender, command, alias, args);
 			}
 		}
 		else if (args.length == 1 && sender instanceof Player)
 		{
 			List<String> completionOptions = new ArrayList<String>();
 			
 			if (args[0].length() == 0)
 			{
 				for (QuickTravelLocation destination : this.plugin.getAvailableDestinations((Player)sender))
 					completionOptions.add(destination.getName());
 				
 				if (sender.hasPermission("qt.admin.*"))
 					completionOptions.addAll(commands);
 			}
 			else 
 			{
 				for (QuickTravelLocation destination : this.plugin.getAvailableDestinations((Player)sender))
 				{
 					if (destination.getName().toLowerCase().startsWith(args[0].toLowerCase()))
 						completionOptions.add(destination.getName());
 				}
 
 				for (String qtCommand : commands)
 				{
 					if (qtCommand.startsWith(args[0].toLowerCase()) && sender.hasPermission("qt.admin." + qtCommand))
 						completionOptions.add(qtCommand);
 				}
 			}
 			
 			return completionOptions;
 		}
 		
 		return plugin.onTabComplete(sender, command, alias, args);
 	}
 
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if (sender == null) return false;
 		
 		if (command.getName().equalsIgnoreCase("qt"))
 		{
 			if (args.length == 0)
 			{
 				this.plugin.listQuickTravels(sender, 1, false);
 				return true;
 			}
 			String commandName = args[0];
 			
 			if (commandName.equalsIgnoreCase("create"))
 			{
 				/* "/qt create" passed Make sure is not being run from console */
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage("[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] You must be a player!");
 					return true;
 				}
 				
 				if (!sender.hasPermission("qt.admin.create"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.createQuickTravel((Player)sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("rename") || commandName.equalsIgnoreCase("name"))
 			{
 				/* "/qt rename" passed */
 				if (!sender.hasPermission("qt.admin.rename"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.renameQuickTravel(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("hidden") || commandName.equalsIgnoreCase("hide") || commandName.equalsIgnoreCase("h"))
 			{
 				/* "/qt hidden" passed */
 				if (!sender.hasPermission("qt.admin.hidden"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelHidden(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("multiplier") || commandName.equalsIgnoreCase("mult"))
 			{
 				/* "/qt rename" passed */
 				if (!sender.hasPermission("qt.admin.multiplier"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelMultiplier(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("welcome") || commandName.equalsIgnoreCase("greeting"))
 			{
 				/* "/qt rename" passed */
 				if (!sender.hasPermission("qt.admin.welcome"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelWelcome(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("type") || commandName.equalsIgnoreCase("t"))
 			{
 				/* "/qt type" passed */
 				if (sender.hasPermission("qt.admin.type"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelType(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("radius") || commandName.equalsIgnoreCase("r"))
 			{
 				/* "/qt radius" passed */
 				if (!sender.hasPermission("qt.admin.radius"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelRadius(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("cuboid"))
 			{
 				/* "/qt cuboid" passed Make sure is not being run from console */
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage("[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] You must be a player!");
 					return true;
 				}
 				if (!sender.hasPermission("qt.admin.cuboid"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelCuboid((Player)sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("move"))
 			{
 				/* "/qt move" passed Make sure is not being run from console */
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage("[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] You must be a player!");
 					return true;
 				}
 				
 				if (!sender.hasPermission("qt.admin.move"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.moveQuickTravel((Player)sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("dest"))
 			{
 				/* "/qt dest" passed Make sure is not being run from console */
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage("[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] You must be a player!");
 					return true;
 				}
 				
 				if (!sender.hasPermission("qt.admin.dest"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelDestination((Player)sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("enable") || commandName.equalsIgnoreCase("e"))
 			{
 				/* "/qt enable" passed */
 				if (!sender.hasPermission("qt.admin.enable"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelEnabled(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("disable"))
 			{
 				/* "/qt disable" passed */
 				if (!sender.hasPermission("qt.admin.disable"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelEnabled(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("delete") || commandName.equalsIgnoreCase("del") || commandName.equalsIgnoreCase("remove"))
 			{
 				/* "/qt delete" passed */
 				if (!sender.hasPermission("qt.admin.delete"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.deleteQuickTravel(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("price") || commandName.equalsIgnoreCase("charge"))
 			{
 				/* "/qt price" passed */
 				if (!sender.hasPermission("qt.admin.price"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelPrice(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("free") || commandName.equalsIgnoreCase("f"))
 			{
 				/* "/qt price" passed */
 				if (!sender.hasPermission("qt.admin.free"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelFree(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("discovery") || commandName.equalsIgnoreCase("discover") || commandName.equalsIgnoreCase("disc") || commandName.equalsIgnoreCase("d"))
 			{
 				/* "/qt discovery" passed */
 				if (!sender.hasPermission("qt.admin.discovery"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelDiscovery(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("perms") || commandName.equalsIgnoreCase("perm") || commandName.equalsIgnoreCase("p"))
 			{
 				/* "/qt perms" passed */
 				if (!sender.hasPermission("qt.admin.perms"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelPermission(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("multiworld") || commandName.equalsIgnoreCase("multi") || commandName.equalsIgnoreCase("m"))
 			{
 				/* "/qt multiworld" passed */
 				if (!sender.hasPermission("qt.admin.multiworld"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelMultiworld(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("cfg") || commandName.equalsIgnoreCase("config"))
 			{
 				/* "/qt multiworld" passed */
 				if (!sender.hasPermission("qt.admin.cfg"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelOption(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("reload"))
 			{
 				/* "/qt multiworld" passed */
 				if (!sender.hasPermission("qt.admin.reload"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				QuickTravel.info("Reloading plugin");
 				sender.sendMessage("Reloading QuickTravel...");
 				
 				this.plugin.onDisable();
 				this.plugin.onEnable();
 				
 				sender.sendMessage("Reload complete");
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("list"))
 			{
 				if (!sender.hasPermission("qt.admin.list"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.listQuickTravels(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("trigger"))
 			{
 				/* "/qt trigger" passed */
 				if (!sender.hasPermission("qt.admin.trigger"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelAutoTrigger(sender, args);
 				return true;
 			}
 			else if (commandName.equalsIgnoreCase("outgoing"))
 			{
 				/* "/qt outgoing" passed */
 				if (!sender.hasPermission("qt.admin.outgoing"))
 				{
 					this.notAuthorised(sender, commandName);
 					return true;
 				}
 				
 				this.plugin.setQuickTravelOutgoingOnly(sender, args);
 				return true;
 			}
 			else if (args.length == 1)
 			{
 				if (!(sender instanceof Player))
 				{
 					sender.sendMessage("[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] You must be a player!");
 					return true;
 				}
 				
 				this.plugin.quickTravel((Player)sender, args[0]);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @param sender
 	 * @param commandName
 	 */
 	private void notAuthorised(CommandSender sender, String commandName)
 	{
 		/* Not authorised */
 		sender.sendMessage("[" + ChatColor.RED + "Error" + ChatColor.WHITE + "] We do not know " + ChatColor.AQUA + commandName + ChatColor.WHITE + "!");
 		this.plugin.listQuickTravels(sender, 1, false);
 	}
 
 	static
 	{
 		// Commands for tab-completion support
 		commands.add("create");                           
 		commands.add("rename");
 		commands.add("type");
 		commands.add("radius");
 		commands.add("cuboid");
 		commands.add("move");
 		commands.add("dest");
 		commands.add("enable");
		commands.add("dissable");
 		commands.add("delete");
 		commands.add("price");
 		commands.add("free");
 		commands.add("discovery");
 		commands.add("perms");
 		commands.add("multiworld");
 		commands.add("list");
 		commands.add("welcome");
 		commands.add("cfg");
 		commands.add("reload");
 		commands.add("multiplier");
 		commands.add("hidden");
 		commands.add("trigger");
 		commands.add("outgoing");
 	}
 }
