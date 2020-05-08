 package org.monstercraft.party.plugin.command.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.monstercraft.party.plugin.command.GameCommand;
 
 /**
  * This class is used for the /party help
  * 
  * @author Jurre1996 <Jurre@koetse.eu>
  * 
  */
 
 public class Help extends GameCommand {
 
 	@Override
 	public boolean canExecute(CommandSender sender, String[] split) {
 		return split.length > 1 && split[0].equalsIgnoreCase("party")
 				&& split[1].equalsIgnoreCase("help");
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String[] split) {
 		Player player = (Player) sender;
		player.sendMessage(ChatColor.RED + "                       MonsterParty Help");
 		player.sendMessage(ChatColor.DARK_PURPLE + "-----------------------------------------------------");
 		player.sendMessage(ChatColor.YELLOW + "/party create [Name] " + ChatColor.AQUA + "- Create a party");
 		player.sendMessage(ChatColor.YELLOW + "/party create [Name] :p[Password] " + ChatColor.AQUA + "- Create Password Protected Party");
 		player.sendMessage(ChatColor.YELLOW + "/party lock " + ChatColor.AQUA + "- Lock your party");
 		player.sendMessage(ChatColor.YELLOW + "/party join [Name] " + ChatColor.AQUA + "- Join a party");
 		player.sendMessage(ChatColor.YELLOW + "/party leave " + ChatColor.AQUA + "- Leave your party");
 		player.sendMessage(ChatColor.YELLOW + "/party list " + ChatColor.AQUA + "- List available parties or members in your party");
 		player.sendMessage(ChatColor.YELLOW + "/party invite [Player] " + ChatColor.AQUA + "- Invite a player to the party [Owner]");
 		player.sendMessage(ChatColor.YELLOW + "/party kick [Player] " + ChatColor.AQUA + "- Kick a player from the part [Owner]");
 		player.sendMessage(ChatColor.YELLOW + "/party teleport [player] " + ChatColor.AQUA + "- Teleport to a party member");
 		player.sendMessage(ChatColor.YELLOW + "/ptp [Player] " + ChatColor.AQUA + "- Teleport to a party member");
 		player.sendMessage(ChatColor.YELLOW + "/pc [msg] " + ChatColor.AQUA + "- Send a message to party chat");
 		player.sendMessage(ChatColor.YELLOW + "/pc " + ChatColor.AQUA + "- Toggle PartyChat");
 		player.sendMessage(ChatColor.DARK_PURPLE + "-----------------------------------------------------");
 		player.sendMessage(ChatColor.WHITE + "Plugin by: " + ChatColor.RED + "Fletch_to_99 " + ChatColor.WHITE + "Help Created by: " + ChatColor.RED + "Jurre1996");
 		player.sendMessage(ChatColor.DARK_PURPLE + "-----------------------------------------------------");
 		return true;
 	}
 
 	@Override
 	public String[] getPermission() {
 		return null;
 	}
 }
