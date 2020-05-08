 package org.monstercraft.party.plugin.command.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.monstercraft.party.plugin.PartyAPI;
 import org.monstercraft.party.plugin.command.GameCommand;
 import org.monstercraft.party.plugin.wrappers.Party;
 
 public class Join extends GameCommand {
 
 	@Override
 	public boolean canExecute(CommandSender sender, String[] split) {
 		return split.length > 2 && split[0].equalsIgnoreCase("party")
 				&& split[1].equalsIgnoreCase("join");
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String[] split) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("You can't join parties!");
 			return true;
 		}
 		Player player = (Player) sender;
 		Party p;
 		if (PartyAPI.inParty(player)) {
 			player.sendMessage(ChatColor.RED
 					+ "You must leave your current party before joining another!");
 			return true;
 		}
 		if (split.length == 3) {
 			if ((p = PartyAPI.getParty(split[2])) != null) {
 				if (p.isInviteOnly() && !p.isInvited(player)) {
 					player.sendMessage(ChatColor.RED
							+ "Your were not invited to that party! It is invite only!");
 					return true;
 				}
 				if (!p.getPassword().equalsIgnoreCase("")) {
 					player.sendMessage(ChatColor.RED
 							+ "The party you tried to join is password protected!");
 					return true;
 				}
 				p.addMember(player);
 				p.sendPartyMessage(ChatColor.GREEN + player.getDisplayName()
 						+ " has joined the party!");
 				return true;
 			}
 			player.sendMessage(ChatColor.RED
 					+ "No party with that name exists!");
 			return true;
 		} else if (split.length == 4) {
 			if ((p = PartyAPI.getParty(split[2])) != null) {
 				if (p.isInviteOnly() && !p.isInvited(player)) {
 					player.sendMessage(ChatColor.RED
							+ "Your were not invited to that party! It is invite only!");
 					return true;
 				}
 				if (p.getPassword().equalsIgnoreCase("")) {
 					player.sendMessage(ChatColor.RED
 							+ "There is no password on the party you attempted to join!");
 					return true;
 				}
 				if (p.getPassword().equalsIgnoreCase(split[3])) {
 					p.addMember(player);
 					p.sendPartyMessage(ChatColor.GREEN
 							+ player.getDisplayName()
 							+ " has joined the party!");
 					return true;
 				}
 			}
 			player.sendMessage(ChatColor.RED
 					+ "No party with that name exists!");
 			return true;
 		}
 		player.sendMessage(ChatColor.RED + "Invalid command usage!");
 		return true;
 	}
 
 	@Override
 	public String[] getPermission() {
 		return new String[] { "monsterparty.join" };
 	}
 
 }
