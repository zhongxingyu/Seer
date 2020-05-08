 package LevelUpGames;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class LevelUpGamesCommands implements CommandExecutor {
 	public LevelUpGamesMain plugin;
 	ChatColor red = ChatColor.RED;
 	ChatColor blue = ChatColor.BLUE;
 	ChatColor gray = ChatColor.GRAY;
 
 	String prefix = this.blue + "[" + this.gray + "LevelUpGames" + this.blue + "] ";
 
 	public LevelUpGamesCommands(LevelUpGamesMain plugin) {
 		this.plugin = plugin;
 	}
 
 	private void noPerms(Player player) {
 		player.sendMessage(this.prefix + this.gray + "You dont have Permissions.");
 	}
 
 	private void toMuchArgs(Player player) {
 		player.sendMessage(this.prefix + this.gray + "Wrong Usage. Type /lug for all LevelUpGames Commands.");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("[LevelUpGames] You are not a Player. Join the Server to perform some Commands.");
 			return true;
 		}
 		Player player = (Player) sender;
 		if (cmd.getName().equalsIgnoreCase("LevelUpGames")) {
 			if (args.length == 0) {
 				if (!player.hasPermission("LevelUpGames.commands")) {
 					noPerms(player);
 					return true;
 				}
 				player.sendMessage(this.blue + "=== " + this.prefix + "===");
 				player.sendMessage("");
 				player.sendMessage(this.blue + "/lug toggle " + this.gray + "= Start the LevelUpGames.");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("toggle")) {
 				if (args.length > 1) {
 					toMuchArgs(player);
 					return false;
 				}
 				if (!player.hasPermission("LevelUpgames.toggle")) {
 					noPerms(player);
 					return true;
 				}
 				if (!this.plugin.toggle) {
 					this.plugin.toggle = true;
 					player.sendMessage(this.prefix + this.gray + "You toggled the LevelUpGames to " + this.blue + "on");
 					return true;
 				}
 				if (this.plugin.toggle) {
 					this.plugin.toggle = false;
 					player.sendMessage(this.prefix + this.gray + "You toggled the LevelUpgames to " + this.blue + "off");
 					return true;
 				}
 			}
 			if (args[0].equalsIgnoreCase("join")) {
 				if (args.length > 1) {
 					toMuchArgs(player);
 					return false;
 				}
 				if (!player.hasPermission("LevelUpGames.join")) {
 					noPerms(player);
 					return true;
 				}
 				if (!this.plugin.toggle) {
 					player.sendMessage(this.prefix + this.gray + "The LevelUpGames arent open yet.");
 					return true;
 				}
 				if (this.plugin.limit == this.plugin.HowMuchCanJoin) {
 					player.sendMessage(this.prefix + this.gray + "The LevelUpGames are full.");
 					return true;
 				}
 				if (this.plugin.toggle) {
 					player.sendMessage(this.prefix + this.gray + "You successful added to the Waiting List.");
 					this.plugin.limit += 1;
 					this.plugin.players.add(player.getName());
 					return true;
 				}
 			}
 			if (args[0].equalsIgnoreCase("leave")) {
 				if (args.length > 1) {
 					toMuchArgs(player);
 					return false;
 				}
 				if (!player.hasPermission("LevelUpGames.join")) {
 					noPerms(player);
 					return true;
 				}
 				if (!this.plugin.players.contains(player.getName())) {
 					player.sendMessage(this.prefix + this.gray + "You are not in the LevelUpGames. You cant leave.");
 					return true;
 				}
 				player.teleport(player.getWorld().getSpawnLocation());
 				this.plugin.players.remove(player.getName());
 				this.plugin.limit -= 1;
 				player.sendMessage(this.prefix + this.gray + "You leaved the LevelUpGames.");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("kick")) {
 				if ((args.length > 2) || (args.length == 1)) {
 					toMuchArgs(player);
 					return false;
 				}
 				if (!player.hasPermission("LevelUpGames.kick")) {
 					noPerms(player);
 					return true;
 				}
 				if (args[1].equalsIgnoreCase(player.getName())) {
 					if (this.plugin.players.contains(player.getName())) {
 						player.sendMessage(this.prefix + this.gray + "You cant kick yourself. Please try /lug leave.");
 						return true;
 					}
 					player.sendMessage(this.prefix + this.gray + "You are not in the LevelUpGames and you cant kick yourself.");
 					return true;
 				}
 
 				Player target = Bukkit.getServer().getPlayer(args[1]);
 				if (target == null) {
					player.sendMessage(this.prefix + this.blue + args[1] + this.gray + " not found.");
 					return true;
 				}
 				if (!this.plugin.players.contains(target.getName())) {
 					player.sendMessage(this.prefix + this.gray + "This Player isnt in the LevelUpGames.");
 					return true;
 				}
 				target.teleport(target.getWorld().getSpawnLocation());
 				this.plugin.players.remove(target.getName());
 				this.plugin.limit -= 1;
 				player.sendMessage(this.prefix + this.gray + "You kicked " + this.blue + args[1] + this.gray + " from the LevelUpGames.");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("info")) {
 				if ((args.length > 2) || (args.length == 1)) {
 					toMuchArgs(player);
 					return false;
 				}
 				if (!player.hasPermission("LevelUpGames.info")) {
 					noPerms(player);
 					return true;
 				}
 
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("start")) {
 				if (args.length > 1) {
 					toMuchArgs(player);
 					return false;
 				}
 				if (!player.hasPermission("LevelUpGames.start")) {
 					noPerms(player);
 					return true;
 				}
 				if (!this.plugin.toggle) {
 					player.sendMessage(this.prefix + this.gray + "The LevelUpGames arent started yet.");
 					return true;
 				}
 				if (this.plugin.toggle) {
 					player.sendMessage(this.prefix + this.gray + "You started the LevelUpGames.");
 
 					for (int i = 0; i < this.plugin.players.size(); i++)
 						;
 					this.plugin.toggle = false;
 					this.plugin.start = true;
 					return true;
 				}
 			}
 			if (args[0].equalsIgnoreCase("stop")) {
 				if (args.length > 1) {
 					toMuchArgs(player);
 					return false;
 				}
 				if (!player.hasPermission("LevelUpGames.stop")) {
 					noPerms(player);
 					return true;
 				}
 				if (!this.plugin.start) {
 					player.sendMessage(this.prefix + this.gray + "There are no LevelUpGames yet. So what should I Stop?");
 					return true;
 				}
 				if (this.plugin.start) {
 					this.plugin.start = false;
 					player.sendMessage(this.prefix + this.gray + "You stopped the LevelUpGames.");
 
 					for (int i = 0; i < this.plugin.players.size(); i++) {
 						Bukkit.getServer().getPlayer((String) this.plugin.players.get(i)).teleport(Bukkit.getServer().getPlayer((String) this.plugin.players.get(i)).getWorld().getSpawnLocation());
 					}
 
 					this.plugin.limit = 0;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
