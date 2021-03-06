 package org.github.craftfortress2;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import java.util.ArrayList;
 public class CFCommandExecutor implements CommandExecutor {
 	int count = 0;
 	static ArrayList<String> names = new ArrayList<String>();
 	static ArrayList<Player> players = new ArrayList<Player>();
 	static ArrayList<String> teams = new ArrayList<String>();
 	static ArrayList<String> classes = new ArrayList<String>();
 	static ArrayList<GameMode> saveGM = new ArrayList<GameMode>();
	static ArrayList<ItemStack[]> saveItemStack = new ArrayList<ItemStack[]>();
 	private CraftFortress2 cf2;
 	public CFCommandExecutor(CraftFortress2 cf2) {
 		this.cf2 = cf2;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 			if (cmd.getName().equalsIgnoreCase("cfstart")) {
 				if (sender.hasPermission("cf.start")) {
 					CFStart.startGame();
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED+"You don't have permission!");
 					return false;
 				}
 			}
 			if (cmd.getName().equalsIgnoreCase("cfend")) {
 				if (sender.hasPermission("cf.end")) {
 					CFEnd.endGame();
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED+"You don't have permission!");
 					return false;
 				}
 			}
 			if(cmd.getName().equalsIgnoreCase("cfhelp")) {
 				if (sender.hasPermission("cf.help")) {
 					sender.sendMessage(ChatColor.GREEN+"CRAFT FORTRESS 2 HELP");
 					sender.sendMessage(ChatColor.GREEN+"/cfstart - force start a game of CraftFortress");
 					sender.sendMessage(ChatColor.GREEN+"/cfend - force end a game of CraftFortress");
 					sender.sendMessage(ChatColor.GREEN+"/cfjoin <team> <class> - join a team and class.");
 					sender.sendMessage(ChatColor.GREEN+"/cfclass <class> - change your class.");
 					sender.sendMessage(ChatColor.GREEN+"/cfhelp - displays this help");
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED+"You don't have permission!");
 					return false;
 				}
 			}
 			if(cmd.getName().equalsIgnoreCase("cfjoin")) {
 				if (sender.hasPermission("cf.join")){
 					if (args.length>1 && args.length<3){
 						if (args[0].equalsIgnoreCase("blu") || args[0].equalsIgnoreCase("blue")){
 							if (checkCFClasses(args[1])){
 								saveInfo(sender, "blu", args[1], ((Player) sender).getGameMode(), ((Player) sender).getInventory());
 								return true;
 							}else{
 								sender.sendMessage(ChatColor.RED+"That is not a valid class!");
 								return false;
 							}
 						}else if(args[0].equalsIgnoreCase("red")){
 							if (checkCFClasses(args[1])){
 								saveInfo(sender, "red", args[1], ((Player) sender).getGameMode(), ((Player) sender).getInventory());
 								return true;
 							}else{
 								sender.sendMessage(ChatColor.RED+"That is not a valid class!");
 								return false;
 							}
 						}else{
 							sender.sendMessage(ChatColor.RED+"That is not a valid team!");
 							return false;
 						}
 					}else if(args.length>2){
 						sender.sendMessage(ChatColor.RED+"Too many arguments!");
 						return false;
 					}else if(args.length<2){
 						sender.sendMessage(ChatColor.RED+"Not enough arguments!");
 						return false;
 					}
 				}else{
 					sender.sendMessage(ChatColor.RED+"You don't have permission!");
 					return false;
 				}
 			}
 			if(cmd.getName().equalsIgnoreCase("cfclass")) {
 				if (sender.hasPermission("cf.class")) {
 					if (args.length > 0 && args.length < 2) {
 						if (checkCFClasses(args[0])) {
 							changeClass(sender, args[0]);
 							return true;
 						}
 					} else if (args.length > 1) {
 						sender.sendMessage(ChatColor.RED+"Too many arguments!");
 						return false;
 					} else if (args.length < 1) {
 						sender.sendMessage(ChatColor.RED+"Not enough arguments!");
 						return false;
 					}
 				} else if(!sender.hasPermission("cf.class")){
 					sender.sendMessage(ChatColor.RED+"You don't have permission!");
 					return false;
 				}else if (!checkCFClasses(args[0])) {
 					sender.sendMessage(ChatColor.RED+"That is not a valid class!");
 					return false;
 				}
 				return false;
 			}
 			return false;
 		}
 	public void saveInfo(CommandSender sender, String team, String cls, GameMode gm, PlayerInventory pi) {
 		if (players.contains(sender)) {
 			sender.sendMessage(ChatColor.RED+"You already joined the game!");
 			return;
 		}
 		players.add((Player) sender); // saves player
 		names.add(sender.toString()); // saves player name, kinda useless ;)
 		teams.add(team); // saves team
 		classes.add(cls); // saves class
 		saveGM.add(gm); // saves gamemode
		saveItemStack.add(pi.getContents()); // saves inventory as ItemStack[]
 		sender.sendMessage(ChatColor.GREEN+"You joined team " + team + " as " + cls);
 	}
 	public void changeClass(CommandSender sender, String cls) { //Changes your class
 		int index = players.lastIndexOf((Player)sender);
 		if (index == -1) {
 			sender.sendMessage(ChatColor.RED+"You did not join the game!");
 			return;
 		}
 		classes.set(index, cls);
 		sender.sendMessage(ChatColor.GREEN+"Your class has been changed to " + cls);
 	}
 	public static String getClass(Player player) {
 		int index = players.lastIndexOf(player);
 		if (index == -1) {
 			return "-1";
 		}
 		return classes.get(index);
 	}
 	public static String getName(Player[] player, int index) { // This is kinda useless. :O
 		return player[index].toString();
 	}
 	public static String getTeam(Player player) {
 		int index = players.lastIndexOf(player);
 		if (index == -1) {
 			return "-1";
 		}
 		return teams.get(index);
 	}
 	public static boolean checkCFClasses(String str) {
 		if (str.equalsIgnoreCase("scout") || str.equalsIgnoreCase("soldier") || str.equalsIgnoreCase("pyro") || str.equalsIgnoreCase("demoman")
 				|| str.equalsIgnoreCase("heavy") || str.equalsIgnoreCase("engineer") || str.equalsIgnoreCase("sniper")
 				|| str.equalsIgnoreCase("medic") || str.equalsIgnoreCase("spy")) {
 			return true;
 		}
 		return false;
 	}
 	public static boolean isPlaying(Player player) {
 		int index = players.lastIndexOf(player);
 		if (index == -1) {
 			return false;
 		}
 		return true;
 	}
 }
