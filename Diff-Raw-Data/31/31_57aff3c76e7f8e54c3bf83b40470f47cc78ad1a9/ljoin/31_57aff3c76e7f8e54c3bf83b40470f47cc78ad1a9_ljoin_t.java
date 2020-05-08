 package us.manniL.xapfeltortexp.LevelUpGames.cmd;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import us.manniL.xapfeltortexp.LevelUpGames.LevelUpGamesMain;
 import us.manniL.xapfeltortexp.LevelUpGames.SubBase;
 
 public class ljoin extends SubBase {
 
 	public ljoin(LevelUpGamesMain pl) {
 		super(pl);
 	}
 
 	@Override
 	public void run(CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("[LevelUpGames] You are not a Player. Join the Server to perform some Commands.");
 			return;
 		}
 		final Player player = (Player) sender;
 		if (args.length > 1) {
 			plugin.toMuchArgs(player);
 			return;
 		}
 		if (!player.hasPermission("LevelUpGames.join")) {
 			plugin.noPerms(player);
 			return;
 		}
 		if (!this.plugin.toggle) {
 			player.sendMessage(plugin.prefix + plugin.gray + "The LevelUpGames arent open yet.");
 			return;
 		}
		if(plugin.VIPPlayers.contains(player.getName())) {
			this.plugin.limit++;
			this.plugin.players.add(player.getName());
			player.sendMessage("" + plugin.limit);
			return;
		}
 		if (this.plugin.limit == this.plugin.HowMuchCanJoin) {
 			player.sendMessage(plugin.prefix + plugin.gray + "The LevelUpGames are full.");
 			return;
 		}
 		if(plugin.players.contains(player.getName())) {
 			player.sendMessage(plugin.prefix + plugin.gray + "You are already in the Waiting List.");
 			return;
 		}
 		if (this.plugin.toggle) {
 			player.sendMessage(plugin.prefix + plugin.gray + "You successful added to the Waiting List.");
			this.plugin.limit++;
 			this.plugin.players.add(player.getName());
			player.sendMessage("" + plugin.limit);
 			return;
 		}
 	}
 
 }
