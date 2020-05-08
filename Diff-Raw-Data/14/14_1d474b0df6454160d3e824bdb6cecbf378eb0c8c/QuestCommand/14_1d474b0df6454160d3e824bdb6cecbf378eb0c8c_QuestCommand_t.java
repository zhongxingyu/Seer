 package me.menexia.terraquest.commands;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import me.menexia.terraquest.TerraQuest;
 import me.mini.Arguments;
 
 public class QuestCommand implements CommandExecutor {
 	private TerraQuest a;
 	public QuestCommand(TerraQuest instance) {
 		a = instance;
 	}
 	
 	ChatColor aqua = ChatColor.AQUA;
 	ChatColor gold = ChatColor.GOLD;
 	ChatColor drkgreen = ChatColor.DARK_GREEN;
 	ChatColor gray = ChatColor.GRAY;
 	ChatColor lightgreen = ChatColor.GREEN;
 	ChatColor red = ChatColor.RED;
 	ChatColor yellow = ChatColor.YELLOW;
 	ChatColor drkgray = ChatColor.DARK_GRAY;
 	ChatColor white = ChatColor.WHITE;
 	public Random random = new Random();
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String zhf, String[] args) {
 		
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("You must be in-game to use TerraQuest.");
 			return true;
 		}
 		
 		Player player = (Player)sender;
 		
 		if (sender.hasPermission("TerraQuest.use")) {
 			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
 				player.sendMessage("Help is shown here!");
 				return true;
 			}
 			
 			if (args.length == 1) {
 				if (args[0].equalsIgnoreCase("create")) {
 					player.sendMessage(ChatColor.GREEN + "Input your quest name:");
 					a.inputWait.put(player, "questname");
 					return true;
 				}
 				
 				if (args[0].equalsIgnoreCase("list")) {
 					player.sendMessage(aqua + "--------- " + gold + " Available quests: " + aqua + " ---------");
 					for (int toShow = 1; toShow < 11; toShow++) {
 						int index = random.nextInt(a.database.getArguments("questdata").getInteger("count")) + 1;
 						int indexCheck = index;
 						if (a.noRepeat.contains(indexCheck) || a.database.getArguments(String.valueOf(indexCheck)).getValue("status").equals(ChatColor.RED + " - Accepted")) continue;
 							a.noRepeat.add(indexCheck);
 							Arguments entry = a.database.getArguments(String.valueOf(indexCheck));
 							player.sendMessage(drkgray + "#" + entry.getValue("id") + " - " + gray + entry.getValue("questname") + ": " + red + entry.getValue("reward"));
 								} // final.. probably.
 								a.noRepeat.clear();
 								return true;
 							}
 							
 				if (args[0].equalsIgnoreCase("check")) {
 					if (a.database.getArguments("onquest").getValue(player.getName()) != null
 							&& !a.database.getArguments("abandon").getValue(player.getName()).equals(0)) {
 						String index = a.database.getArguments("onquest").getValue(player.getName());
 						String questName = a.database.getArguments(index).getValue("questname");
 						String objective = a.database.getArguments(index).getValue("objective");
 						String reward = a.database.getArguments(index).getValue("reward");
 						String playerName = a.database.getArguments(index).getValue("playername");
 						player.sendMessage(drkgreen + "------ " + gold + questName + yellow + " - " + playerName + drkgreen + " ------");
 						player.sendMessage(objective);
 						player.sendMessage(aqua + "Reward: " + reward);
 					} else {
 						player.sendMessage("You do not have a current quest.");
 					}
					return true;
				}
 				
 				if (args[0].equalsIgnoreCase("abandon")) {
 					if (a.database.getArguments("onquest").getValue(player.getName()) != null
 							&& a.database.getArguments("onquest").getInteger(player.getName()) > 0) {
						a.database.setArgument(a.database.getArguments("onquest").getValue(player.getName()), "status", ChatColor.GREEN + " - Free");
 						a.database.getArguments("onquest").setValue("onquest", "0");
						
 						player.sendMessage("You have abandoned your current quest.");
 					} else {
 						player.sendMessage("You must have a quest before abandoning it!");
 					}
 					return true;
 				}
 				
 				try {
 					// Integer id = Integer.parseInt(args[0]); - no need to parse, unless you need to manipulate data in an integer
 					String questName = a.database.getArguments(args[0]).getValue("questname");
 					String objective = a.database.getArguments(args[0]).getValue("objective");
 					String reward = a.database.getArguments(args[0]).getValue("reward");
 					String playerName = a.database.getArguments(args[0]).getValue("playername");
 					int id = a.database.getArguments(args[0]).getInteger("id");
 					
 								player.sendMessage(drkgreen + "------ " + gold + questName + yellow + " - " + playerName + drkgreen + " ------");
 								player.sendMessage(objective);
								player.sendMessage(aqua + "Reward: " + reward);
 								if (a.database.getArguments(String.valueOf(args[0])).getValue("status").equals(red + " - Accepted")) {
 									player.sendMessage(red + "Quest already accepted.");
 								} else {
 									player.sendMessage(gray + "Want to take on this quest? Type: " + lightgreen + "/accept");
 									a.inputWait.put(player, String.valueOf(id));
 								}
 								return true;
 							} catch (Exception e) {
 								player.sendMessage("Unknown console command. Type \"help\" for help.");
 								return true;
 							} // end of try and catch
 						} // end of args.length == 1
 					} // end of permissions checks
 		return false;
 	}
 }
