 package us.manniL.xapfeltortexp.LevelUpGames.cmd;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import us.manniL.xapfeltortexp.LevelUpGames.LevelUpGamesMain;
 import us.manniL.xapfeltortexp.LevelUpGames.SubBase;
 
 public class ltoggle extends SubBase {
 
 	public ltoggle(LevelUpGamesMain pl) {
 		super(pl);
 	}
 
 	@Override
 	public void run(CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("[LevelUpGames] You are not a Player. Join the Server to perform some Commands.");
 			return;
 		}
 		final Player player = (Player) sender;
 		if (args[0].equalsIgnoreCase("toggle")) {
 			if (args.length > 1) {
 				plugin.toMuchArgs(player);
 				return;
 			}
 			if (!player.hasPermission("LevelUpgames.toggle")) {
 				plugin.noPerms(player);
 				return;
 			}
			if (!this.plugin.toggle) {
 				this.plugin.toggle = true;
 				player.sendMessage(plugin.prefix + plugin.gray + "You toggled the LevelUpGames to " + plugin.blue + "ON");
 			    plugin.getServer().broadcastMessage(plugin.prefix + plugin.gray + "The LevelUpGames are started! /lug join");
 				return;
 			}
			if (this.plugin.toggle) {
 				this.plugin.toggle = false;
 				player.sendMessage(plugin.prefix + plugin.gray + "You toggled the LevelUpGames to " + plugin.blue + "OFF");
 				return;
 			}
 		}
 	}
 
 }
