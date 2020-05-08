 package graindcafe.tribu.Executors;
 
 import graindcafe.tribu.Tribu;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmdIspawn implements CommandExecutor {
 	private Tribu plugin;
 
 	public CmdIspawn(Tribu instance) {
 		plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(!sender.hasPermission("tribu.level.ispawn"))
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
 
 		if (args.length == 1) {
 			if (args[0].equalsIgnoreCase("jump")) {
 
 				player.teleport(plugin.getLevel().getInitialSpawn());
				Tribu.messagePlayer(sender,plugin.getLocale("Message.TeleportedToInitialSpawn"));
 				return true;
 
 			}
 		} else {
 
 			plugin.getLevel().setInitialSpawn(player.getLocation());
			Tribu.messagePlayer(sender,plugin.getLocale("Message.InitialSpawnSet"));
 			return true;
 
 		}
 
 		return false;
 	}
 
 }
