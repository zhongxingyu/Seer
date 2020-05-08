 package graindcafe.tribu.Executors;
 
 import graindcafe.tribu.Tribu;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmdZspawn implements CommandExecutor {
 	private Tribu plugin;
 
 	public CmdZspawn(Tribu instance) {
 		plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(!sender.hasPermission("tribu.game.zspawn"))
 		{
			Tribu.messagePlayer(sender,plugin.getLocale("Message.Deny"));
 			return true;
 		}
 
 		if (!(sender instanceof Player)) {
 			plugin.LogWarning(plugin.getLocale("Warning.ThisCommandCannotBeUsedFromTheConsole"));
 			return true;
 		}
 		Player player = (Player) sender;
 
 		// Make sure a level is loaded
 		if (plugin.getLevel() == null) {
			Tribu.messagePlayer(player,plugin.getLocale("Message.NoLevelLoaded"));
			Tribu.messagePlayer(player,plugin.getLocale("Message.NoLevelLoaded2"));
 			return true;
 		}
 
 		if (args.length == 2) {
 			// args[0]=args[0].toLowerCase();
 			if (args[0].equalsIgnoreCase("set")) {
 
 				plugin.getLevel().addZombieSpawn(player.getLocation(), args[1]);
				Tribu.messagePlayer(player,plugin.getLocale("Message.SpawnpointAdded"));
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("remove")) {
 
 				plugin.getLevel().removeZombieSpawn(args[1]);
				Tribu.messagePlayer(player,plugin.getLocale("Message.SpawnpointRemoved"));
 				return true;
 
 			} else if (args[0].equalsIgnoreCase("jump")) {
 
 				Location zspawn = plugin.getLevel().getZombieSpawn(args[1]);
 				if (zspawn != null) {
 					player.teleport(zspawn);
					Tribu.messagePlayer(player,String.format(plugin.getLocale("Message.TeleportedToZombieSpawn"), args[1]));
 				} else {
					Tribu.messagePlayer(player,plugin.getLocale("Message.InvalidSpawnName"));
 				}
 				return true;
 
 			}
 		} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
 
 			plugin.getLevel().listZombieSpawns(player);
 			return true;
 		}
 
 		return false;
 	}
 
 }
