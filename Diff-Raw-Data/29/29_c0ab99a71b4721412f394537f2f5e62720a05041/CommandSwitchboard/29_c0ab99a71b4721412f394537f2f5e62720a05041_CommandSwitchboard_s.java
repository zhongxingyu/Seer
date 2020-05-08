 package com.deaboy.manhunt.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.deaboy.manhunt.chat.ChatManager;
 
 /**
  * This class takes the Manhunt command and determines where it should be redirected.
  * @author Deaboy
  *
  */
 public class CommandSwitchboard implements CommandExecutor
 {
 	
 	
 	public CommandSwitchboard()
 	{
 		Bukkit.getPluginCommand("manhunt").setExecutor(this);
 		Bukkit.getPluginCommand("mspawn").setExecutor(this);
 		Bukkit.getPluginCommand("msetspawn").setExecutor(this);
 		Bukkit.getPluginCommand("mworld").setExecutor(this);
 		Bukkit.getPluginCommand("mworlds").setExecutor(this);
 		Bukkit.getPluginCommand("manhuntmode").setExecutor(this);
 		
 		Bukkit.getPluginCommand("mlobby").setExecutor(this);
 		
 		Bukkit.getPluginCommand("mmap").setExecutor(this);
 		Bukkit.getPluginCommand("mzone").setExecutor(this);
 		Bukkit.getPluginCommand("mpoint").setExecutor(this);
 		
 		Bukkit.getPluginCommand("mstartgame").setExecutor(this);
 		Bukkit.getPluginCommand("mstopgame").setExecutor(this);
 		Bukkit.getPluginCommand("mverify").setExecutor(this);
 		Bukkit.getPluginCommand("mcancel").setExecutor(this);
 		Bukkit.getPluginCommand("msettings").setExecutor(this);
 		Bukkit.getPluginCommand("mset").setExecutor(this);
 	}
 	
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command c, String cmd, String[] arguments)
 	{
 		Command command = CommandUtil.parseCommand(c, arguments);
 		
		if (command != null && (command.containsArgument(CommandUtil.arg_help) || command.getArgumentCount() == 0))
 		{
 			Bukkit.dispatchCommand(sender, "help " + cmd);
 			sender.sendMessage(ChatColor.GRAY + command.getTemplate().getUsage());
 		}
 		
 		if (c.getName().equalsIgnoreCase("manhunt"))
 			return HelpCommands.manhunt(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("mspawn"))
 			return WorldCommands.mspawn(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("msetspawn"))
 			return WorldCommands.msetspawn(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("mworld"))
 			return WorldCommands.mworld(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("mworlds"))
 			return WorldCommands.mworlds(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("manhuntmode"))
 			return PlayerCommands.manhuntmode(sender, arguments);
 		
 		
 		
 		if (c.getName().equalsIgnoreCase("mlobby"))
 			return LobbyCommands.mlobby(sender, command);
 		
 		
 		
 		if (c.getName().equalsIgnoreCase("mmap"))
 			return MapCommands.mmap(sender, command);
 		
 		if (c.getName().equalsIgnoreCase("mzone"))
 			return MapCommands.mzone(sender, command);
 		
 		if (c.getName().equalsIgnoreCase("mpoint"))
 			return MapCommands.mpoint(sender, command);
 		
 		
 		
 		if (c.getName().equalsIgnoreCase("mstartgame"))
 			return LobbyCommands.mstartgame(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("mstopgame"))
 			return LobbyCommands.mstopgame(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("mverify"))
 			return mverify(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("mcancel"))
 			return mcancel(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("msettings"))
 			return SettingCommands.msettings(sender, arguments);
 		
 		if (c.getName().equalsIgnoreCase("mset"))
 			return SettingCommands.mset(sender, arguments);
 		
 		return false;
 		
 	}
 	
 	
 	private static boolean mverify(CommandSender sender, String[] arguments)
 	{
 		if (CommandUtil.isVerifying(sender))
 			CommandUtil.executeCommand(sender);
 		else
 			sender.sendMessage(ChatManager.leftborder + "No command to verify.");
 		
 		return true;
 	}
 	
 	private static boolean mcancel(CommandSender sender, String[] arguments)
 	{
 		if (CommandUtil.isVerifying(sender))
 		{
 			CommandUtil.cancelCommand(sender);
 			sender.sendMessage(ChatManager.leftborder + "Cancelled command.");
 		}
 		else
 			sender.sendMessage(ChatManager.leftborder + "No command to cancel.");
 		
 		return true;
 	}
 	
 	
 	
 }
