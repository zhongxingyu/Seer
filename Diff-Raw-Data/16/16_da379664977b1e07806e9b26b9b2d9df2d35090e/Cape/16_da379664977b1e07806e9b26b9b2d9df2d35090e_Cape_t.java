 package com.mcnsa.mcnsacapes.commands;
 
 import org.bukkit.entity.Player;
 
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

 import com.mcnsa.mcnsacapes.MCNSACapes;
 import com.mcnsa.mcnsacapes.util.ColourHandler;
 import com.mcnsa.mcnsacapes.util.Command;
 import com.mcnsa.mcnsacapes.util.CommandInfo;
 
 @CommandInfo(alias = "cape", permission = "setcape", usage = "<player> <cape>", description = "manually sets a player's cape")
 public class Cape implements Command {
 	private static MCNSACapes plugin = null;
 	public Cape(MCNSACapes instance) {
 		plugin = instance;
 	}
 	
 	@Override
 	public Boolean handle(Player player, String sArgs) {
 		// make sure they have the args
 		if(sArgs.trim().length() < 1 || !sArgs.trim().contains(" ")) {
 			return false;
 		}
 		
 		// get the args
 		String[] args = sArgs.trim().split(" ", 2);
 		
 		// make sure the player exists
 		Player target = plugin.getServer().getPlayer(args[0]);
 		if(target == null) {
 			ColourHandler.sendMessage(player, "&cError - could not find the player '&f" + args[0] + "&c' online!");
 			return true;
 		}
		if(!(args[1] == "mod" || args[1] == "custom" || args[1] == "donor")){
			ColourHandler.sendMessage(player,  "&cError - choose either 'mod' 'custom' or 'donor'");
			return true;
		}
 		// and update!
 		plugin.playerManager.setPlayerCape(target, args[1]);
		PermissionUser user = PermissionsEx.getUser(player);
		user.removePermission("mcnsacapes.custom");
		user.removePermission("mcnsacapes.mod");
		user.removePermission("mcnsacapes.donor");
		user.addPermission("mcnsacapes."+args[1]);
 		// notify them
 		ColourHandler.sendMessage(player, "&aSucess! &f" + target.getName() + "&a's cape has been set to: &f" + args[1]);
 		
 		return true;
 	}
 }
