 package me.sirtyler.VaultSlots;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 
@SuppressWarnings("deprecation")
 public class SignChange extends BlockListener{
 	public static VaultSlots plugin;
 	private static Permission perm;
 	private boolean useDebug = false;
 	private static Log logger;
 	public SignChange(VaultSlots instance) {
 		plugin = instance;
 	}
 	
 	public void onSignChange(SignChangeEvent event) {
 		logger = plugin.log;
 		perm = plugin.permission;
 		Player player = event.getPlayer();
 		if(useDebug) logger.sendDebugInfo("Sign Maker:" + player.getName() + " Checking Permission");
 		if(perm.playerHas(player, "vaultslots.build")) {
 			if(useDebug) logger.sendDebugInfo("Sign User:" + player.getName() + " Check true");
 			if(event.getLine(0).equalsIgnoreCase("[Slots]")) {
 				if(!event.getLine(1).isEmpty()){
 					String slotName = event.getLine(1);
 					event.setLine(0, ChatColor.GREEN + "[Slots]");
 					event.setLine(1, ChatColor.GREEN + slotName);
 					event.setLine(2, ChatColor.BLUE + "-|-|-");
 					if(useDebug) logger.sendDebugInfo("Sign User:" + player.getName() + " Created Slot Machine Type:" + slotName);
 					player.sendMessage(ChatColor.GOLD + "Slot Machine Made.");
 				} else {
 					if(useDebug) logger.sendDebugInfo("Sign User:" + player.getName() + " Did not Create Slot Machine: Missing Type");
 					player.sendMessage(ChatColor.GREEN + "[VaultSlots]: Missing Slot Machine Type.");
 					player.sendMessage(ChatColor.GREEN + "[VaultSlots]: Use /slots help to see Commands and Sign Format.");
 				}
 			}
 		} else {
 			if(useDebug) logger.sendDebugInfo("Sign User:" + player.getName() + " Check false");
 			player.sendMessage(ChatColor.RED + "You Do Not Have Permission for That");
 		}
 	}
 }
