 package org.monstercraft.party.plugin.command.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.monstercraft.party.plugin.PartyAPI;
 import org.monstercraft.party.plugin.command.GameCommand;
 import org.monstercraft.party.plugin.wrappers.Party;
 
 public class Invite extends GameCommand {
 
 	@Override
 	public boolean canExecute(CommandSender sender, String[] split) {
 		return split.length > 2 && split[0].equalsIgnoreCase("party")
 				&& split[1].equalsIgnoreCase("invite");
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String[] split) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("You can't invite to parties!");
 			return true;
 		}
 		Player player = (Player) sender;
 		Party p;
 		if (!PartyAPI.inParty(player)) {
 			player.sendMessage(ChatColor.RED + "You are not in a party!");
 			return true;
 		}
 		if (split.length == 3) {
 			if ((p = PartyAPI.getParty(player)) != null) {
 				if (!p.getOwner().equals(player)) {
 					player.sendMessage(ChatColor.RED
							+ "You are not the party owner!");
 					return true;
 				}
 				Player invite;
 				if ((invite = Bukkit.getPlayer(split[2])) != null) {
 					p.invite(invite);
 					p.sendPartyMessage(ChatColor.GREEN
 							+ invite.getDisplayName()
 							+ " has been invited to the party!");
 					return true;
 				}
 				player.sendMessage(ChatColor.RED + "Player not found!");
 				return true;
 			}
 			player.sendMessage(ChatColor.RED
 					+ "Your party was not found... Say what?!?!");
 			return true;
 		}
 		player.sendMessage(ChatColor.RED + "Invalid command usage!");
 		return true;
 	}
 
 	@Override
 	public String[] getPermission() {
 		return new String[] { "monsterparty.invite" };
 	}
 
 }
