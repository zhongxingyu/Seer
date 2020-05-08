 package com.censoredsoftware.demigods.command;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.Elements;
 import com.censoredsoftware.demigods.battle.Battle;
 import com.censoredsoftware.demigods.helper.ColoredStringBuilder;
 import com.censoredsoftware.demigods.helper.ListedCommand;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.player.Notification;
 import com.censoredsoftware.demigods.util.Errors;
 import com.censoredsoftware.demigods.util.Randoms;
 import com.censoredsoftware.demigods.util.Unicodes;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 public class DevelopmentCommands extends ListedCommand
 {
 	@Override
 	public Set<String> getCommands()
 	{
 		return Sets.newHashSet("test1", "test2", "test3", "hspawn", "makecharacters", "removechar");
 	}
 
 	@Override
 	public boolean processCommand(CommandSender sender, Command command, String[] args)
 	{
 		if(command.getName().equalsIgnoreCase("removechar")) return removeChar(sender, args);
 		else if(command.getName().equalsIgnoreCase("test1")) return test1(sender, args);
 		else if(command.getName().equalsIgnoreCase("test2")) return test2(sender, args);
 		else if(command.getName().equalsIgnoreCase("test3")) return test3(sender, args);
 		else if(command.getName().equalsIgnoreCase("hspawn")) return hspawn(sender);
 		else if(command.getName().equalsIgnoreCase("makecharacters")) return makeCharacters(sender, args);
 		return false;
 	}
 
 	@Override
 	public List<String> processTab(CommandSender sender, Command command, final String[] args)
 	{
 		return new ArrayList<String>()
 		{
 			{
 				for(Player online : Bukkit.getOnlinePlayers())
 				{
 					DPlayer wrapper = DPlayer.Util.getPlayer(online);
 					if(wrapper.canUseCurrent() && wrapper.getCurrent() != null && wrapper.getCurrent().getName().toLowerCase().startsWith(args[0].toLowerCase())) add(wrapper.getCurrent().getName());
 					else if(online.getName().toLowerCase().startsWith(args[0].toLowerCase())) add(online.getName());
 				}
 			}
 		};
 	}
 
 	private static boolean test1(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
 		for(Battle battle : Battle.Util.getAllActive())
		{
 			battle.end();
		}
 
 		player.sendMessage("All battles disabled!");
 
 		return true;
 	}
 
 	private static boolean test2(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 
		if(Demigods.errorNoise) Errors.triggerError(ChatColor.GREEN + player.getName(), new ColoredStringBuilder().gray("  " + Unicodes.rightwardArrow() + " ").red("Test error.").build());
 
 		return true;
 	}
 
 	private static boolean test3(CommandSender sender, final String[] args)
 	{
 		Player player = (Player) sender;
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 		character.getMeta().clearNotifications();
 
 		if(character != null)
 		{
 			Notification notification1 = Notification.Util.create(Notification.Sender.PLUGIN, character, Notification.Danger.GOOD, 3, "Test1", "This the first notification test ever.");
 			Notification notification2 = Notification.Util.create(Notification.Sender.PLUGIN, character, Notification.Danger.NEUTRAL, 100, "Test2", "This the second notification test ever.");
 			Notification notification3 = Notification.Util.create(Notification.Sender.PLUGIN, character, Notification.Danger.BAD, 1, "Test2", "This the third notification test ever.");
 
 			Notification.Util.sendNotification(character, notification1);
 			Notification.Util.sendNotification(character, notification2);
 			Notification.Util.sendNotification(character, notification3);
 
 			return true;
 		}
 
 		player.sendMessage(ChatColor.RED + "Failed!");
 
 		return true;
 	}
 
 	private static boolean hspawn(CommandSender sender)
 	{
 		Player player = (Player) sender;
 
 		// This SHOULD happen automatically, but bukkit doesn't do this, so we need to.
 
 		if(player.isInsideVehicle() && player.getVehicle() instanceof Horse)
 		{
 			Horse horse = (Horse) player.getVehicle();
 			horse.eject();
 			horse.teleport(player.getWorld().getSpawnLocation());
 			horse.setPassenger(player);
 			player.sendMessage(ChatColor.YELLOW + "Teleported to spawn...");
 		}
 
 		return true;
 	}
 
 	private static boolean makeCharacters(CommandSender sender, final String[] args)
 	{
 		DPlayer player = DPlayer.Util.getPlayer((Player) sender);
 
 		if(args.length != 1 || !isInt(args[0])) return false;
 
 		for(int i = 0; i < Integer.parseInt(args[0]); i++)
 			DCharacter.Util.create(player, Randoms.generateString(7), Elements.Deities.values()[Randoms.generateIntRange(0, 5)].getName());
 
 		sender.sendMessage(args[0] + " characters created.");
 
 		return true;
 	}
 
 	/**
 	 * Check to see if an input string is a float.
 	 * 
 	 * @param string The input string.
 	 * @return True if the string is a float.
 	 */
 	private static boolean isInt(String string)
 	{
 		try
 		{
 			Integer.parseInt(string);
 			return true;
 		}
 		catch(Exception e)
 		{
 			return false;
 		}
 	}
 
 	private static boolean removeChar(CommandSender sender, String[] args)
 	{
 		if(args.length != 1) return false;
 
 		// Define args
 		Player player = Bukkit.getOfflinePlayer(sender.getName()).getPlayer();
 		String charName = args[0];
 
 		if(DPlayer.Util.hasCharName(player, charName))
 		{
 			DCharacter character = DCharacter.Util.getCharacterByName(charName);
 			character.remove();
 
 			sender.sendMessage(ChatColor.RED + "Character removed!");
 		}
 		else sender.sendMessage(ChatColor.RED + "There was an error while removing your character.");
 
 		return true;
 	}
 }
