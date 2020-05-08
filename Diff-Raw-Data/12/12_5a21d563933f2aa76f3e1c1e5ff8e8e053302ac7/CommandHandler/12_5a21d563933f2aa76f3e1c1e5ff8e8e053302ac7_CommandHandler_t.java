 package me.Kruithne.WolfHunt;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 public class CommandHandler {
 
 	private Output output = null;
	private Permissions permission = null;
 	private Configuration config = null;
 	
 	private enum WolfHuntOperation
 	{
 		help,
 		spawnwolf,
 		getconfig,
 		setconfig,
 		unknown
 	}
 	
	CommandHandler (Output output, Permissions permission, Configuration config)
 	{
 		this.output = output;
 		this.permission = permission;
 		this.config = config;
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
 			this.output.toPlayer(Constants.commandNoParameters, player);
 			this.output.toPlayer(Constants.commandSeeHelp, player);
 			return;
 		}
 
 		switch(GetOperation(arguments[0]))
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
 				this.output.toPlayer(Constants.commandUnknown, player);
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
 		if (this.permission.has(player, Permissions.commandSpawnWolf))
 		{
 			player.getWorld().spawnCreature(player.getLocation(), EntityType.WOLF);
 			this.output.toPlayer(Constants.commandInfoSpawnWolfDone, player);
 		}
 		else
 		{
 			this.output.toPlayer(Constants.commandNoPermission, player);
 			this.output.toPlayer(Constants.commandSeeHelp, player);
 		}
 	}
 
 	private void getConfigOperation(Player player, String[] arguments)
 	{
 		if (this.permission.has(player, Permissions.commandGetConfig))
 		{
 			if (arguments.length > 1)
 			{
 				String configValue = this.config.getConfigValue(arguments[1]);
 				if (configValue != null)
 				{
 					this.output.toPlayer(
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
 					this.output.toPlayer(Constants.commandInfoConfigNoExists, player);
 				}
 			}
 			else
 			{
 				this.output.toPlayer(Constants.commandFormatInvalid, player);
 				this.output.toPlayer(Constants.commandInfoGetConfig, player);
 			}
 		}
 		else
 		{
 			this.output.toPlayer(Constants.commandNoPermission, player);
 			this.output.toPlayer(Constants.commandSeeHelp, player);
 		}
 	}
 
 	private void setConfigOperation(Player player, String[] arguments)
 	{
 		if (this.permission.has(player, Permissions.commandSetConfig))
 		{
 			if (arguments.length > 2)
 			{
 				String configValueCheck = this.config.getConfigValue(arguments[1]);
 				
 				if (configValueCheck != null)
 				{
 					this.config.setConfigValue(arguments[1], arguments[2]);
 					this.config.loadConfiguration();
 					this.output.toPlayer(
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
 					this.output.toPlayer(Constants.commandInfoConfigNoExists, player);
 				}
 		
 			}
 			else
 			{
 				this.output.toPlayer(Constants.commandFormatInvalid, player);
 				this.output.toPlayer(Constants.commandInfoSetConfig, player);
 			}
 		}
 		else
 		{
 			this.output.toPlayer(Constants.commandNoPermission, player);
 			this.output.toPlayer(Constants.commandSeeHelp, player);
 		}
 	}
 
 	private void printCommandHelp(CommandSender sender)
 	{
 		Boolean hasCommand = false;
 		
 		Player player = (Player) sender;
 		
 		this.output.toPlayer(Constants.commandAvailable, player);
 		
 		if (this.permission.has(player, Permissions.commandSpawnWolf))
 		{
 			this.output.toPlayer(Constants.commandInfoSpawnWolf, player);
 			hasCommand = true;
 		}
 		
 		if (this.permission.has(player, Permissions.commandGetConfig))
 		{
 			this.output.toPlayer(Constants.commandInfoGetConfig, player);
 			hasCommand = true;
 		}
 		
 		if (this.permission.has(player, Permissions.commandSetConfig))
 		{
 			this.output.toPlayer(Constants.commandInfoSetConfig, player);
 			hasCommand = true;
 		}
 		
 		if (!hasCommand)
 		{
 			this.output.toPlayer(Constants.commandNoneAvail, (Player) sender);
 		}
 	}
 }
