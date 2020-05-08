 package me.cyberkitsune.prefixchat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class KitsuneChatCommand implements CommandExecutor {
 
 	private KitsuneChat plugin;
 
 	public KitsuneChatCommand(KitsuneChat plugin) {
 		this.plugin = plugin;
 	}
 
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (sender instanceof Player) {
 			if (command.getName().equalsIgnoreCase("kc")) {
 				if (args.length > 0) {
 					if(args[0].equalsIgnoreCase("party") || args[0].equalsIgnoreCase("p")) {
 						if(args.length > 1) {
 							if(args[1].equalsIgnoreCase("leave"))
 							{
 								plugin.party.leaveParty((Player) sender, false);
 								return true;
 							} else if(args[1].equalsIgnoreCase("list")) {
 								if(plugin.party.isInAParty((Player) sender)) {
 									String playerlist = "";
 									int playerCount = 0;
 									for(Player plr : plugin.party.getPartyMembers(plugin.party.getPartyName((Player) sender))) {
 										playerlist = playerlist + plr.getDisplayName()+", ";
 										playerCount++;
 									}
 									playerlist = playerlist.substring(0, playerlist.length() - 2)+".";
 									sender.sendMessage(ChatColor.YELLOW+"[KitsuneChat] "+playerCount+((playerCount == 1) ? " person " : " people ")+"in the party.");
 									sender.sendMessage(ChatColor.YELLOW+"[KitsuneChat] They are: "+playerlist+".");
 									return true;
 								} else {
 									sender.sendMessage(ChatColor.RED+"[KitsuneChat] You are not in a party!");
 									return true;
 								}
 							}
							plugin.party.changeParty((Player) sender, args[1]);
 							plugin.dataFile.setUserChannel((Player) sender, plugin.getConfig().getString("party.prefix"));
 							sender.sendMessage(ChatColor.YELLOW+"[KitsuneChat] You are now talking in party chat.");
 						} else {
 							sender.sendMessage(ChatColor.RED+"[KitsuneChat] Please choose a party name!");
 						}
 					} else if(args[0].equalsIgnoreCase("leaveparty")) {
 						plugin.party.leaveParty((Player) sender, false);
 					} else if(args[0].equalsIgnoreCase("?")) {
 						printHelp((Player) sender);
 					} else if(args[0].equalsIgnoreCase("invite")) {
 						if(args.length > 1) {
 							if(plugin.party.isInAParty((Player)sender)) {
 								Player target = plugin.getServer().getPlayer(args[1]);
 								if(target != null) {
 									target.sendMessage(ChatColor.GREEN+"[KitsuneChat] "+sender.getName()+" has invited you to a party! Type /kc p "+plugin.party.getPartyName((Player) sender)+" to join!");
 									plugin.party.notifyParty(plugin.party.getPartyName((Player) sender), ChatColor.GREEN+"[KitsuneChat] "+sender.getName()+" invited "+target.getDisplayName()+ChatColor.GREEN+" to the party.");
 								} else {
 									sender.sendMessage(ChatColor.RED+"[KitsuneChat] That player does not exist!");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED+"[KitsuneChat] You aren't in a party!");
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED+"[KitsuneChat] You didn't specify a player!");
 						}
 					} else if(args[0].equalsIgnoreCase("null")) { // Dummy command for the /me full stop.
 						return true;
 					} else {
 						for(String str : plugin.prefixes) {
 								if(args[0].equalsIgnoreCase(str)) {
 									if(sender.hasPermission("kitsunechat.nodefault."+plugin.util.getChannelName(str, false)) && !plugin.util.getChannelName(str, false).equalsIgnoreCase("local")) //Failsafe
 									{
 										sender.sendMessage(ChatColor.RED+"[KitsuneChat] You do not have permission to use "+plugin.util.getChannelName(str, false)+" as your default channel.");
 										sender.sendMessage(ChatColor.RED+"[KitsuneChat] Try prefixing your message with "+plugin.util.getChannelName(str, true)+" instead.");
 										return true;
 									}
 								plugin.dataFile.setUserChannel((Player) sender, str);
 								sender.sendMessage(ChatColor.YELLOW+"[KitsuneChat] Default chat now set to "+plugin.util.getChannelName(str, false));
 								return true;
 								}
 						}
 						sender.sendMessage(ChatColor.RED+"[KitsuneChat] Unknown or missing command. See /kc ? for help.");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED+"[KitsuneChat] Unknown or missing command. See /kc ? for help.");
 				}
 			} else if(command.getName().equalsIgnoreCase("me")) {
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	public void printHelp(Player target) {
 		target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] KitsuneChat - Channeled Chat System Version "+plugin.getDescription().getVersion()+" by CyberKitsune.");
 		target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] KitsuneChat Commands: ");
 		target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] /kc ? - This command. ");
 		target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] /kc party (or p) <name> - Join a party with name <name>. ");
 		target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] /kc party list - Lists who is in your party.");
 		target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] /kc leaveparty - Leaves your current party. ");
 		target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] /kc invite <player> - Invites <player> to your current party.");
 		for(String str : plugin.prefixes) {
 			target.sendMessage(ChatColor.YELLOW+"[KitsuneChat] /kc "+str+" - Change default channel to "+plugin.util.getChannelName(str, false)+".");
 		}
 	}
 
 }
