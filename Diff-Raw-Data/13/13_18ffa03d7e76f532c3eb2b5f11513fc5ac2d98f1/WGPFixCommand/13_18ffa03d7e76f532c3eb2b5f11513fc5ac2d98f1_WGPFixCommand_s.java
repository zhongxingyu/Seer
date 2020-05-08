 package asofold.fix.wgp;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class WGPFixCommand implements CommandExecutor{
 	private WGPFix plugin;
 	public WGPFixCommand(WGPFix plugin){
 		this.plugin = plugin;
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if ( !label.equalsIgnoreCase("wgpfix")) return false;
 		if ( args.length == 1 ){
 			if ( args[0].equalsIgnoreCase("reload")){
				if ( plugin.blockListener.getWorldGuard().hasPermission(sender, "wgpfix.reload" )){
 					if (plugin.loadSettings()){
 						sender.sendMessage("WGPFix - Settings reloaded.");
 						return true;
 					}
 					else{
 						sender.sendMessage("WGPFix - Failed to reload settings, fall back to paranoid settings.");
 						return true; // just to prevent showing usage.
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
