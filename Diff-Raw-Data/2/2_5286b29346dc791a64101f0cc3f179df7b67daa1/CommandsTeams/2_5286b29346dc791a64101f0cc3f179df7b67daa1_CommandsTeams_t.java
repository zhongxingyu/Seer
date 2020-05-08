 package com.bendude56.hunted.commands;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.bendude56.hunted.ManhuntPlugin;
 import com.bendude56.hunted.chat.ChatManager;
 import com.bendude56.hunted.games.GameUtil;
 import com.bendude56.hunted.teams.TeamManager;
 import com.bendude56.hunted.teams.TeamManager.Team;
 import com.bendude56.hunted.teams.TeamUtil;
 
 public class CommandsTeams
 {
 	public static void onCommandList(CommandSender sender, String[] args)
 	{
 		TeamManager teams = ManhuntPlugin.getInstance().getTeams();
 		List<String> hunters = teams.getTeamNames(Team.HUNTERS);
 		List<String> prey = teams.getTeamNames(Team.PREY);
 		List<String> spectators = teams.getTeamNames(Team.SPECTATORS);
 		
 		sender.sendMessage(ChatManager.bracket1_ + TeamUtil.getTeamColor(Team.HUNTERS) + hunters.size() + " " + TeamUtil.getTeamName(Team.HUNTERS, true) + "  " + TeamUtil.getTeamColor(Team.PREY) + prey.size() + " " + TeamUtil.getTeamName(Team.PREY, true) + "  " + TeamUtil.getTeamColor(Team.SPECTATORS) + spectators.size() + " " + TeamUtil.getTeamName(Team.SPECTATORS, true) + ChatManager.bracket2_);
 		
 		String msgHunters = "";
 		for (String n : hunters)
 		{
 			msgHunters += TeamUtil.getTeamColor(Team.HUNTERS);
 			msgHunters += n;
 			if (Bukkit.getPlayer(n) == null)
 				msgHunters += ChatColor.GRAY + " (offline)";
 			msgHunters += "  ";
 		}
 		String msgPrey = "";
 		for (String n : prey)
 		{
 			msgPrey += TeamUtil.getTeamColor(Team.PREY);
 			msgPrey += n;
 			if (Bukkit.getPlayer(n) == null)
 				msgPrey += ChatColor.GRAY + " (offline)";
 			else if (Bukkit.getPlayer(n).getWorld() != ManhuntPlugin.getInstance().getWorld())
 				msgPrey += ChatColor.GRAY + " (missing)";
 			msgPrey += "  ";
 		}
 		String msgSpectators = "";
 		for (String n : spectators)
 		{
 			msgSpectators += TeamUtil.getTeamColor(Team.SPECTATORS);
 			msgSpectators += n;
 			msgSpectators += "   ";
 		}
 		
 		if (!msgHunters.isEmpty())
 			sender.sendMessage(msgHunters);
 		if (!msgPrey.isEmpty())
 			sender.sendMessage(msgPrey);
 		if (!msgSpectators.isEmpty())
 			sender.sendMessage(msgSpectators);
 		
 		sender.sendMessage(ChatManager.divider);
 	}
 
 	public static void onCommandQuit(CommandSender sender, String[] args)
 	{
 		String SYNTAX = ChatColor.RED + "Proper syntax is /m quit";
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		Player p;
 		
 		if (sender instanceof Player)
 		{
 			p = (Player) sender;
 		}
 		else
 		{
 			sender.sendMessage(CommandUtil.IS_SERVER);
 			return;
 		}
 		
 		if (args.length != 1)
 		{
 			sender.sendMessage(SYNTAX);
 		}
 		
 		if (plugin.gameIsRunning())
 		{
 			plugin.getGame().onPlayerForfeit(p.getName());
 		}
 		else
 		{
 			String[] array = {"spectate"};
 			CommandsTeams.onCommandSpectate(sender, array);
 		}
 	}
 
 	public static void onCommandHunter(CommandSender sender, String[] args)
 	{
 		String SYNTAX = ChatColor.RED + "Proper syntax is /m spectate [player]";
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		Player p;
 		
 		if (plugin.locked)
 		{
 			sender.sendMessage(CommandUtil.LOCKED);
 			return;
 		}
 		if (plugin.gameIsRunning())
 		{
 			sender.sendMessage(CommandUtil.GAME_RUNNING);
 			return;
 		}
 		
 		if (!sender.isOp())
 		{
 			if (plugin.getSettings().OP_CONTROL.value)
 			{
 				sender.sendMessage(CommandUtil.NO_PERMISSION);
 			}
 			else if (plugin.locked)
 			{
 				sender.sendMessage(CommandUtil.LOCKED);
 			}
 		}
 		
 		if (args.length == 1)
 		{
 			if (sender instanceof Player)
 			{
 				p = (Player) sender;
 			}
 			else
 			{
 				sender.sendMessage(CommandUtil.IS_SERVER);
 				return;
 			}
 		}
 		else if (args.length == 2)
 		{
 			p = Bukkit.getPlayer(args[1]);
 		}
 		else
 		{
 			sender.sendMessage(SYNTAX);
 			return;
 		}
 		
 		if (p == null)
 		{
 			sender.sendMessage(ChatColor.RED + "That player does not exist.");
 			return;
 		}
 		
 		if (p.getWorld() != plugin.getWorld())
 		{
 			sender.sendMessage(CommandUtil.WRONG_WORLD);
 			return;
 		}
 		
 		plugin.getTeams().changePlayerTeam(p, Team.HUNTERS);
 		GameUtil.broadcast(ChatManager.leftborder + Team.HUNTERS.getColor() + p.getName() + ChatColor.WHITE + " has joined team " + Team.HUNTERS.getColor() + Team.HUNTERS.getName(true), Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 		
 	}
 
 	public static void onCommandPrey(CommandSender sender, String[] args)
 	{
 		String SYNTAX = ChatColor.RED + "Proper syntax is /m spectate [player]";
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		Player p;
 		
 		if (plugin.locked)
 		{
 			sender.sendMessage(CommandUtil.LOCKED);
 			return;
 		}
 		if (plugin.gameIsRunning())
 		{
 			sender.sendMessage(CommandUtil.GAME_RUNNING);
 			return;
 		}
 		
 		if (!sender.isOp())
 		{
 			if (plugin.getSettings().OP_CONTROL.value)
 			{
 				sender.sendMessage(CommandUtil.NO_PERMISSION);
 			}
 			else if (plugin.locked)
 			{
 				sender.sendMessage(CommandUtil.LOCKED);
 			}
 		}
 		
 		if (args.length == 1)
 		{
 			if (sender instanceof Player)
 			{
 				p = (Player) sender;
 			}
 			else
 			{
 				sender.sendMessage(CommandUtil.IS_SERVER);
 				return;
 			}
 		}
 		else if (args.length == 2)
 		{
 			p = Bukkit.getPlayer(args[1]);
 		}
 		else
 		{
 			sender.sendMessage(SYNTAX);
 			return;
 		}
 		
 		if (p == null)
 		{
 			sender.sendMessage(ChatColor.RED + "That player does not exist.");
 			return;
 		}
 		
 		if (p.getWorld() != plugin.getWorld())
 		{
 			sender.sendMessage(CommandUtil.WRONG_WORLD);
 			return;
 		}
 		
 		plugin.getTeams().changePlayerTeam(p, Team.PREY);
 		GameUtil.broadcast(ChatManager.leftborder + Team.PREY.getColor() + p.getName() + ChatColor.WHITE + " has joined team " + Team.PREY.getColor() + Team.PREY.getName(true), Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 		
 	}
 
 	public static void onCommandSpectate(CommandSender sender, String[] args)
 	{
 		String SYNTAX = ChatColor.RED + "Proper syntax is /m spectate [player]";
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		Player p;
 		
 		if (plugin.locked)
 		{
 			sender.sendMessage(CommandUtil.LOCKED);
 			return;
 		}
 		if (plugin.gameIsRunning())
 		{
 			sender.sendMessage(CommandUtil.GAME_RUNNING);
 			return;
 		}
 		
 		if (!sender.isOp())
 		{
 			if (plugin.getSettings().OP_CONTROL.value)
 			{
 				sender.sendMessage(CommandUtil.NO_PERMISSION);
 			}
 			else if (plugin.locked)
 			{
 				sender.sendMessage(CommandUtil.LOCKED);
 			}
 		}
 		
 		if (args.length == 1)
 		{
 			if (sender instanceof Player)
 			{
 				p = (Player) sender;
 			}
 			else
 			{
 				sender.sendMessage(CommandUtil.IS_SERVER);
 				return;
 			}
 		}
 		else if (args.length == 2)
 		{
 			p = Bukkit.getPlayer(args[1]);
 		}
 		else
 		{
 			sender.sendMessage(SYNTAX);
 			return;
 		}
 		
 		if (p == null)
 		{
 			sender.sendMessage(ChatColor.RED + "That player does not exist.");
 			return;
 		}
 		
 		if (p.getWorld() != plugin.getWorld())
 		{
 			sender.sendMessage(CommandUtil.WRONG_WORLD);
 			return;
 		}
 		
 		plugin.getTeams().changePlayerTeam(p, Team.SPECTATORS);
		GameUtil.broadcast(ChatManager.leftborder + Team.SPECTATORS.getColor() + p.getName() + ChatColor.WHITE + " has become a " + Team.SPECTATORS.getColor() + Team.SPECTATORS.getName(false), Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 		
 	}
 
 	public static void onCommandLock(CommandSender sender, String[] args)
 	{
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		
 		if (!sender.isOp())
 		{
 			sender.sendMessage(CommandUtil.NO_PERMISSION);
 			return;
 		}
 		
 		if  (plugin.gameIsRunning())
 		{
 			sender.sendMessage(CommandUtil.GAME_RUNNING);
 			return;
 		}
 		
 		plugin.locked = !plugin.locked;
 		sender.sendMessage(ChatColor.GOLD + "Manhunt is " + (plugin.locked ? "LOCKED" : "UNLOCKED") + ".");
 	}
 
 	public static void onCommandKick(CommandSender sender, String[] args)
 	{
 		if (!sender.isOp())
 		{
 			sender.sendMessage(CommandUtil.NO_PERMISSION);
 			return;
 		}
 		
 		if (args.length != 2)
 		{
 			sender.sendMessage(ChatColor.RED + "Proper syntax is /m kick <player>");
 			return;
 		}
 		
 		ManhuntPlugin plugin = ManhuntPlugin.getInstance();
 		
 		if (plugin.getTeams().getTeamOf(args[1]) == null)
 		{
 			sender.sendMessage(ChatColor.RED + "That player does not exist!");
 		}
 		else
 		{
 			Player p = Bukkit.getPlayer(args[1]);
 			OfflinePlayer p2 = Bukkit.getOfflinePlayer(args[1]);
 			
 			if (p != null)
 			{
 				p.kickPlayer("You have been kicked.");
 			}
 			
 			if (plugin.gameIsRunning())
 			{
 				plugin.getGame().timeouts.stopTimeout(p2.getName());
 				plugin.getGame().onPlayerForfeit(p2.getName());
 			}
 			
 			sender.sendMessage(ChatColor.GREEN + p2.getName() + " has been kicked.");
 		}
 	}
 	
 	
 }
