 package ltguide.ClearInventory;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ClearInventory extends JavaPlugin {
 	public void onDisable() {}
 	
 	public void onEnable() {
 		Logger.getLogger("Minecraft").info("[" + getDescription().getName() + "] v" + getDescription().getVersion() + " enabled");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (command.getName().equalsIgnoreCase("clearinventory") || command.getName().equalsIgnoreCase("cinv")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("This command has no functionality from the console.");
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
			else sender.sendMessage(ChatColor.GRAY + "Usage: /clearinv <main|bar|all>");
 			return true;
 		}
 		return false;
 	}
 }
