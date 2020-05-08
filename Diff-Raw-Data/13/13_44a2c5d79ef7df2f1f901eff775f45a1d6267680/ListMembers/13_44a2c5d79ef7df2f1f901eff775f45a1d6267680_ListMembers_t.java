 package org.monstercraft.party.plugin.command.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.monstercraft.party.plugin.PartyAPI;
 import org.monstercraft.party.plugin.command.GameCommand;
 import org.monstercraft.party.plugin.wrappers.Party;
 
 public class ListMembers extends GameCommand {
 
 	@Override
 	public boolean canExecute(CommandSender sender, String[] split) {
 		return split.length > 1 && split[0].equalsIgnoreCase("party")
 				&& split[1].equalsIgnoreCase("list");
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, String[] split) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("You can't list the players of a party from the console!");
 			return true;
 		}
 		Player player = (Player) sender;
 		Party p;
 		if (PartyAPI.inParty(player)) {
 			if ((p = PartyAPI.getParty(player)) != null) {
 				player.sendMessage(p.listMembers());
 				return true;
 			}
 		} else {
 			player.sendMessage(ChatColor.GREEN + "Green " + ChatColor.WHITE
					+ " are open parties" + ChatColor.RED + " Red"
 					+ ChatColor.WHITE + " are password protected parties and "
					+ ChatColor.BLUE + "Blue" + ChatColor.WHITE
					+ " are invite only parties!");
 			player.sendMessage(PartyAPI.listParties());
 			return true;
 		}
 		player.sendMessage(ChatColor.RED + "Your not in a party!");
 		return true;
 	}
 
 	@Override
 	public String[] getPermission() {
 		return new String[] { "monsterparty.list" };
 	}
 
 }
