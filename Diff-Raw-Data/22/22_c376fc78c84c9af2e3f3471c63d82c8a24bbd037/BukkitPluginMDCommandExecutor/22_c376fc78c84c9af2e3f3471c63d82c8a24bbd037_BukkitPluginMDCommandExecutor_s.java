 package io.github.md678685.BukkitPluginMD;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class BukkitPluginMDCommandExecutor implements CommandExecutor {
 	
 	private BukkitPluginMD plugin;
 	
 	public BukkitPluginMDCommandExecutor(BukkitPluginMD plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if (cmd.getName().equalsIgnoreCase("MDcommand")){
 			sender.sendMessage("Successfully ran 'MDcommand'!");
 			return true;
 		} else if (cmd.getName().equalsIgnoreCase("MDcommand2")){
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Player-only command. Please run from in-game player.");
 				return true;
 			} else {
 				Player player = (Player) sender;
 				player.sendMessage("Successfully ran 'MDcommand2'!");
 				return true;
 			}
 		} else if (cmd.getName().equalsIgnoreCase("MDslap")){
 			if (args.length == 1) {
 				String player = args[0];
 				if (!(sender instanceof Player)) {
 					sender.sendMessage("If I had let you slap " + player + ", you would've caused a paradox due to missing a player to slap as.");
 					plugin.broadcastMsg("The console tried to slap " + player + " but failed because he didn't have any arms to slap with.");
 					return true;
 				} else if (Bukkit.getPlayerExact(player) == null) {
 					sender.sendMessage("Hmm... they don't seem to be online. Tell them to join then try again.");
 				}
 				 else {
 					sender.sendMessage("You slapped " + player + "! Now THAT'S something to boast about!");
 					plugin.broadcastMsg(sender + " slapped " + player + " hard in the face! What's " + player + " going to do about that NOW?");
 					return true;
 				}
 			} else {
 				sender.sendMessage("Either you tried to slap too many people or not enough people. Up to two at a time, please.");
 				return true;
 			}
 		}
		return false; 
 	}
 
 }
