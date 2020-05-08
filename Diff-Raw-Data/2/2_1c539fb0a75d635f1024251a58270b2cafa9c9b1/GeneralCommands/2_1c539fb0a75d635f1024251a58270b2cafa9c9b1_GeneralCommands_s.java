 package com.censoredsoftware.demigods.command;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.helper.ListedCommand;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.util.Maps2;
 import com.censoredsoftware.demigods.util.Strings;
 import com.censoredsoftware.demigods.util.Titles;
 import com.censoredsoftware.demigods.util.Unicodes;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 public class GeneralCommands extends ListedCommand
 {
 	@Override
 	public Set<String> getCommands()
 	{
 		return Sets.newHashSet("check", "owner", "binds", "leaderboard");
 	}
 
 	@Override
 	public boolean processCommand(CommandSender sender, Command command, String[] args)
 	{
 		if(command.getName().equalsIgnoreCase("check")) return check(sender);
 		else if(command.getName().equalsIgnoreCase("owner")) return owner(sender, args);
 		else if(command.getName().equalsIgnoreCase("binds")) return binds(sender);
 		else if(command.getName().equalsIgnoreCase("leaderboard")) return leaderboard(sender);
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
 		sender.sendMessage(ChatColor.GRAY + " " + Unicodes.rightwardArrow() + " " + ChatColor.RESET + "Kills: " + ChatColor.GREEN + kills + ChatColor.WHITE + " / Deaths: " + ChatColor.RED + deaths);
 
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
 		if(charToCheck == null) player.sendMessage(ChatColor.RED + "That character doesn't exist.");
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
 			for(Map.Entry<String, Object> entry : character.getMeta().getBinds().entrySet())
 				player.sendMessage(ChatColor.GREEN + "    " + StringUtils.capitalize(entry.getKey().toLowerCase()) + ChatColor.GRAY + " is bound to " + (Strings.beginsWithVowel(entry.getValue().toString()) ? "an " : "a ") + ChatColor.ITALIC + Strings.beautify(entry.getValue().toString()).toLowerCase() + ChatColor.GRAY + ".");
 
 			player.sendMessage(" ");
 		}
 		else player.sendMessage(ChatColor.RED + "You currently have no ability binds.");
 
 		return true;
 	}
 
 	private boolean leaderboard(CommandSender sender)
 	{
 		// Define variables
 		List<DCharacter> characters = Lists.newArrayList(DCharacter.Util.getAllUsable());
 		Map<UUID, Double> scores = Maps.newLinkedHashMap();
 		for(int i = 0; i < characters.size(); i++)
 		{
 			DCharacter character = characters.get(i);
			double score = character.getKillCount() / character.getDeathCount();
 			if(score > 0) scores.put(character.getId(), score);
 		}
 
 		// Sort rankings
 		scores = Maps2.sortByValue(scores);
 
 		// Print info
 		sender.sendMessage(" ");
 		sender.sendMessage(ChatColor.YELLOW + Titles.chatTitle("Leaderboard"));
 		sender.sendMessage("    Rankings are determined by kills and deaths.");
 		sender.sendMessage(" ");
 
 		int length = characters.size() > 15 ? 16 : characters.size() + 1;
 		List<Map.Entry<UUID, Double>> list = Lists.newArrayList(scores.entrySet());
 		int count = 0;
 
 		for(int i = list.size() - 1; i >= 0; i--)
 		{
 			count++;
 			Map.Entry<UUID, Double> entry = list.get(i);
 
 			if(count >= length) break;
 			DCharacter character = DCharacter.Util.load(entry.getKey());
 			sender.sendMessage(ChatColor.GRAY + "    " + ChatColor.RESET + count + ". " + character.getDeity().getColor() + character.getName() + ChatColor.RESET + ChatColor.GRAY + " (" + character.getPlayer() + ") " + ChatColor.RESET + "Kills: " + ChatColor.GREEN + character.getKillCount() + ChatColor.WHITE + " / Deaths: " + ChatColor.RED + character.getDeathCount());
 		}
 
 		sender.sendMessage(" ");
 		return true;
 	}
 }
