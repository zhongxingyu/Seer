 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class SignListener implements Listener {
 
 	// Get Main Class
 	public static BattleNight plugin;
 
 	public SignListener(BattleNight instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 
 		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
 			Block block = event.getClickedBlock();
 			Player player = event.getPlayer();
 			if ((block.getState() instanceof Sign)) {
 				Sign sign = (Sign) block.getState();
 				if ((plugin.BattleClasses.containsKey(sign.getLine(0)))
 						&& (plugin.BattleUsersTeam
 								.containsKey(player.getName()))) {
 					plugin.BattleSigns.put(player.getName(), sign);
 					if (plugin.BattleUsersClass.containsKey(player.getName())) {
 						if (plugin.BattleUsersClass.get(player.getName()) == sign
 								.getLine(0)) {
 							plugin.BattleUsersClass.remove(player.getName());
 							if (sign.getLine(2) == player.getName()) {
 								sign.setLine(2, "");
 								sign.update();
 								player.getInventory().clear();
 								plugin.clearArmorSlots(player);
 							} else if (sign.getLine(3) == player.getName()) {
 								sign.setLine(3, "");
 								sign.update();
 								player.getInventory().clear();
 								plugin.clearArmorSlots(player);
 							} else {
 								player.sendMessage(ChatColor.GRAY
 										+ "[BattleNight] "
 										+ ChatColor.WHITE
 										+ "Please tell developer about this bug (#5017).");
 							}
 						} else {
 							player.sendMessage(ChatColor.GRAY
 									+ "[BattleNight] "
 									+ ChatColor.WHITE
 									+ "You must first remove yourself from the other class!");
 						}
 					} else if (sign.getLine(2).trim().equals("")) {
 						plugin.BattleUsersClass.put(player.getName(),
 								sign.getLine(0));
 						sign.setLine(2, player.getName());
 						sign.update();
 						plugin.giveItems(player);
 					} else if (sign.getLine(3).trim().equals("")) {
 						plugin.BattleUsersClass.put(player.getName(),
 								sign.getLine(0));
 						sign.setLine(3, player.getName());
 						sign.update();
 						plugin.giveItems(player);
 					} else {
 						player.sendMessage(ChatColor.GRAY
 								+ "[BattleNight] "
 								+ ChatColor.WHITE
 								+ "There are too many of this class, pick another class.");
 					}
 				}
 			}
 		}
 	}
 }
