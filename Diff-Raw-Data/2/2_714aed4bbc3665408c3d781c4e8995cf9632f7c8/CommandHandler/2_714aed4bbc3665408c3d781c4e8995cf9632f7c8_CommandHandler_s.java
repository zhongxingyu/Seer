 package me.Kruithne.WolfHunt;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 public class CommandHandler {
 
 	private WolfHunt wolfHuntPlugin = null;
 	
 	private enum WolfHuntOperation
 	{
 		help,
 		spawnwolf,
 		getconfig,
 		setconfig,
 		unknown
 	}
 	
 	CommandHandler (WolfHunt plugin)
 	{
 		this.wolfHuntPlugin = plugin;
 	}
 	
 	public boolean handleCommand(CommandSender sender, Command command, String[] arguments)
 	{
 		if (!command.getName().equalsIgnoreCase("wolfhunt") && !command.getName().equalsIgnoreCase("wh"))
 			return false;
 			
 		handleOperation((Player) sender, arguments);
 
 		return true;
 	}
 	
 	private void handleOperation(Player player, String[] arguments)
 	{
 		if (arguments.length < 1)
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandNoParameters, player);
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandSeeHelp, player);
 			return;
 		}
 
		switch(GetAction(arguments[0]))
 		{
 			case help:
 				printCommandHelp(player);
 				break;
 				
 			case spawnwolf:
 				spawnWolfOperation(player);
 				break;
 			
 			case getconfig:
 				getConfigOperation(player, arguments);
 				break;
 			
 			case setconfig:
 				setConfigOperation(player, arguments);
 				break;
 
 			default:
 				this.wolfHuntPlugin.outputToPlayer(Constants.commandUnknown, player);
 				break;
 		}
 	}
 	
 	private WolfHuntOperation GetOperation(String argument)
 	{
 		try
 		{
 			return WolfHuntOperation.valueOf(argument.toLowerCase());
 		}
 		catch (IllegalArgumentException e)
 		{
 			return WolfHuntOperation.unknown;
 		}
 	}
 	
 	private void spawnWolfOperation(Player player)
 	{
 		if (this.wolfHuntPlugin.hasPermission("wolfhunt.commandSpawnWolf", player))
 		{
 			player.getWorld().spawnCreature(player.getLocation(), EntityType.WOLF);
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoSpawnWolfDone, player);
 		}
 		else
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandNoPermission, player);
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandSeeHelp, player);
 		}
 	}
 
 	private void getConfigOperation(Player player, String[] arguments)
 	{
 		if (this.wolfHuntPlugin.hasPermission("wolfhunt.commandGetConfig", player))
 		{
 			if (arguments.length > 1)
 			{
 				String configValue = this.wolfHuntPlugin.config.getConfigValue(arguments[1]);
 				if (configValue != null)
 				{
 					this.wolfHuntPlugin.outputToPlayer(
 						String.format(
 							Constants.commandInfoGetConfigReturnFormat, 
 							arguments[1], 
 							configValue
 						), 
 						player
 					);
 				}
 				else
 				{
 					this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoConfigNoExists, player);
 				}
 			}
 			else
 			{
 				this.wolfHuntPlugin.outputToPlayer(Constants.commandFormatInvalid, player);
 				this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoGetConfig, player);
 			}
 		}
 		else
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandNoPermission, player);
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandSeeHelp, player);
 		}
 	}
 
 	private void setConfigOperation(Player player, String[] arguments)
 	{
 		if (this.wolfHuntPlugin.hasPermission("wolfhunt.commandSetConfig", player))
 		{
 			if (arguments.length > 2)
 			{
 				String configValueCheck = this.wolfHuntPlugin.config.getConfigValue(arguments[1]);
 				
 				if (configValueCheck != null)
 				{
 					this.wolfHuntPlugin.config.setConfigValue(arguments[1], arguments[2]);
 					this.wolfHuntPlugin.config.loadConfiguration();
 					this.wolfHuntPlugin.outputToPlayer(
 						String.format(
 							Constants.commandInfoSetConfigDone, 
 							arguments[1], 
 							arguments[2]
 						), 
 						player
 					);
 				}
 				else
 				{
 					this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoConfigNoExists, player);
 				}
 		
 			}
 			else
 			{
 				this.wolfHuntPlugin.outputToPlayer(Constants.commandFormatInvalid, player);
 				this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoSetConfig, player);
 			}
 		}
 		else
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandNoPermission, player);
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandSeeHelp, player);
 		}
 	}
 
 	private void printCommandHelp(CommandSender sender)
 	{
 		Boolean hasCommand = false;
 		
 		Player player = (Player) sender;
 		
 		this.wolfHuntPlugin.outputToPlayer(Constants.commandAvailable, player);
 		
 		if (this.wolfHuntPlugin.hasPermission("wolfhunt.commandSpawnWolf", player))
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoSpawnWolf, player);
 			hasCommand = true;
 		}
 		
 		if (this.wolfHuntPlugin.hasPermission("wolfhunt.commandGetConfig", player))
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoGetConfig, player);
 			hasCommand = true;
 		}
 		
 		if (this.wolfHuntPlugin.hasPermission("wolfhunt.commandSetConfig", player))
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandInfoSetConfig, player);
 			hasCommand = true;
 		}
 		
 		if (!hasCommand)
 		{
 			this.wolfHuntPlugin.outputToPlayer(Constants.commandNoneAvail, (Player) sender);
 		}
 	}
 }
