 package ltguide.ClearInventory;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

 public class ClearInventory extends JavaPlugin {
	private PermissionHandler Permissions;
	
 	public void onDisable() {}
 	
 	public void onEnable() {
 		Logger.getLogger("Minecraft").info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " enabled");
 	}
 	
	public Boolean hasPermission(CommandSender sender, String node) {
		if (!(sender instanceof Player)) return true;
		
		Player player = (Player) sender;
		if (Permissions != null) return Permissions.has(player, node);
		else {
			Plugin test = getServer().getPluginManager().getPlugin("Permissions");
			if (test != null) {
				Permissions = ((Permissions) test).getHandler();
				return Permissions.has(player, node);
			}
		}
		return player.isOp();
	}
	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (command.getName().equalsIgnoreCase("clearinventory") || command.getName().equalsIgnoreCase("cinv")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("This command has no functionality from the console.");
 				return true;
 			}
			if (!hasPermission(sender, "clearinventory.use")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission.");
				return true;
			}
 			
 			PlayerInventory inventory = ((Player) sender).getInventory();
 			String option = (args.length > 0 ? args[0].toLowerCase() : "");
 			
 			Integer firstSlot = -1;
 			Integer lastSlot = inventory.getSize();
 			
 			if (option.equals("all")) firstSlot = 0;
 			else if (option.equals("main")) firstSlot = 9;
 			else if (option.equals("bar")) {
 				firstSlot = 0;
 				lastSlot = 9;
 			}
 			
 			if (firstSlot > -1) {
 				for (Integer i = firstSlot; i < lastSlot; i++)
 					inventory.clear(i);
 				sender.sendMessage(ChatColor.GREEN + "Inventory cleared.");
 			}
			else sender.sendMessage("Usage: /" + command.getName().toLowerCase() + " <main|bar|all>");
 			return true;
 		}
 		return false;
 	}
 }
