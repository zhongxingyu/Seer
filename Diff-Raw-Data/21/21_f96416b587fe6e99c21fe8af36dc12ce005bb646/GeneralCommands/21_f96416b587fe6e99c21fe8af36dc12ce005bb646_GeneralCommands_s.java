 package com.censoredsoftware.demigods.command;
 
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.helper.ListedCommand;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.util.Strings;
 import com.censoredsoftware.demigods.util.Titles;
 import com.censoredsoftware.demigods.util.Unicodes;
 import com.google.common.collect.Sets;
 
 public class GeneralCommands extends ListedCommand
 {
 	@Override
 	public Set<String> getCommands()
 	{
 		return Sets.newHashSet("check", "owner", "binds");
 	}
 
 	@Override
 	public boolean processCommand(CommandSender sender, Command command, String[] args)
 	{
 		if(command.getName().equalsIgnoreCase("check")) return check(sender);
 		else if(command.getName().equalsIgnoreCase("owner")) return owner(sender, args);
 		else if(command.getName().equalsIgnoreCase("binds")) return binds(sender);
 		return false;
 	}
 
 	private boolean check(CommandSender sender)
 	{
 		Player player = (Player) sender;
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 		if(character == null)
 		{
 			player.sendMessage(ChatColor.RED + "You are nothing but a mortal. You have no worthy statistics.");
 			return true;
 		}
 
 		// Define variables
 		int kills = character.getKillCount();
 		int deaths = character.getDeathCount();
 		String charName = character.getName();
 		String deity = character.getDeity().getName();
 		String alliance = character.getAlliance();
 		int favor = character.getMeta().getFavor();
 		int maxFavor = character.getMeta().getMaxFavor();
 		int ascensions = character.getMeta().getAscensions();
 		ChatColor deityColor = character.getDeity().getColor();
 		ChatColor favorColor = Strings.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor());
 
 		// Send the user their info via chat
 		Demigods.message.tagged(sender, "Player Check");
 
 		sender.sendMessage(ChatColor.GRAY + " " + Unicodes.rightwardArrow() + " " + ChatColor.RESET + "Character: " + deityColor + charName);
 		sender.sendMessage(ChatColor.GRAY + " " + Unicodes.rightwardArrow() + " " + ChatColor.RESET + "Deity: " + deityColor + deity + ChatColor.WHITE + " of the " + ChatColor.GOLD + StringUtils.capitalize(alliance) + "s");
 		sender.sendMessage(ChatColor.GRAY + " " + Unicodes.rightwardArrow() + " " + ChatColor.RESET + "Favor: " + favorColor + favor + ChatColor.GRAY + " (of " + ChatColor.GREEN + maxFavor + ChatColor.GRAY + ")");
 		sender.sendMessage(ChatColor.GRAY + " " + Unicodes.rightwardArrow() + " " + ChatColor.RESET + "Ascensions: " + ChatColor.GREEN + ascensions);
 		sender.sendMessage(ChatColor.GRAY + " " + Unicodes.rightwardArrow() + " " + ChatColor.RESET + "Kills: " + ChatColor.GREEN + kills + ChatColor.WHITE + " / Deaths: " + ChatColor.RED + deaths + ChatColor.WHITE);
 
 		return true;
 	}
 
 	private boolean owner(CommandSender sender, String[] args)
 	{
 		// Check permissions
 		if(!sender.hasPermission("demigods.basic")) return Demigods.message.noPermission(sender);
 
 		Player player = (Player) sender;
 		if(args.length < 1)
 		{
 			player.sendMessage(ChatColor.RED + "You must select a character.");
 			player.sendMessage(ChatColor.RED + "/owner <character>");
 			return true;
 		}
 		DCharacter charToCheck = DCharacter.Util.getCharacterByName(args[0]);
		if(charToCheck.getName() == null) player.sendMessage(ChatColor.RED + "That character doesn't exist.");
 		else player.sendMessage(charToCheck.getDeity().getColor() + charToCheck.getName() + ChatColor.YELLOW + " belongs to " + charToCheck.getOfflinePlayer().getName() + ".");
 		return true;
 	}
 
 	private boolean binds(CommandSender sender)
 	{
 		// Check permissions
 		if(!sender.hasPermission("demigods.basic")) return Demigods.message.noPermission(sender);
 
 		// Define variables
 		Player player = (Player) sender;
 		DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 
 		if(character != null && !character.getMeta().getBinds().isEmpty())
 		{
 			player.sendMessage(ChatColor.YELLOW + Titles.chatTitle("Currently Bound Abilities"));
 			player.sendMessage(" ");
 
 			// Get the binds and display info
 			for(String stringBind : character.getMeta().getBinds())
 			{
 				Ability.Bind bind = Ability.Util.loadBind(UUID.fromString(stringBind));
 				player.sendMessage(ChatColor.GREEN + "    " + StringUtils.capitalize(bind.getAbility().toLowerCase()) + ChatColor.GRAY + " is bound to " + (Strings.beginsWithVowel(bind.getRawItem().getType().name()) ? "an " : "a ") + ChatColor.ITALIC + Strings.beautify(bind.getRawItem().getType().name()).toLowerCase() + ChatColor.GRAY + ".");
 			}
 
 			player.sendMessage(" ");
 		}
 		else
 		{
 			player.sendMessage(ChatColor.RED + "You currently have no ability binds.");
 		}
 
 		return true;
 	}
 }
