 package info.nebtown.PearlXP;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.block.Action;
 
 public class PearlXPListener implements Listener {
 	
 	public PearlXP instance;
 	
 	
 	private static String itemName = "pearl";
 	private static ChatColor textColor = ChatColor.BLUE;
 
 	public PearlXPListener(PearlXP owner) {
 		instance = owner;
 
 	}
 	
 	
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		ItemStack item = event.getItem();
		//int itemId = instance.getConfig().getInt("itemid");
 		
 		int maxLevel = PearlXP.getMaxLevel();
 		int playerXp;
 		
 		Player player = event.getPlayer();
 		
 		if (item != null && item.getTypeId() == PearlXP.getItemId()) {
 			
 			
 			if (event.getAction() == Action.RIGHT_CLICK_AIR
 					|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				
				event.setCancelled(true);
 				
 				if (item.getDurability() != 0) {
 					sendInfo("This " + itemName + " is imbued with "
 							+ item.getDurability() + " XP!", player);
				} else {
 					sendInfo("This " + itemName + " is empty.", player);
 				}
 				
 				
 			} else if (event.getAction() == Action.LEFT_CLICK_AIR
 						|| event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				
 				if (item.getAmount() > 1) {
 					sendInfo("To store experience in the " + itemName
 							+ ", please unstack them!", player);
 					return;
 				}
 				
 
 				if (item.getDurability() > 0) {
 					
 					player.giveExp(item.getDurability());
 					
 					sendInfo("Restoring " + item.getDurability() + " XP! You now have " 
 								+ player.getTotalExperience() + " XP!", player);
 					
 					item.setDurability((short) 0);
 					
 				} else { // the item is empty
 					
 					if (player.getTotalExperience() > maxLevel) {
 						
 						// remove only the maximum xp
 						playerXp = player.getTotalExperience() - maxLevel;
 						item.setDurability((short) maxLevel);
 						player.setTotalExperience(0);
 						player.setLevel(0);
 						player.giveExp(playerXp);
 						
 						sendInfo("Imbued this " + itemName + " with "
 								+ item.getDurability() + " XP! You have " + player.getTotalExperience() + "XP left!", player);
 					} else {
 						
 						//Get Player XP and Set it to the Item Durability
 						item.setDurability((short) player.getTotalExperience()); 
 
 						player.setTotalExperience(0);
 						player.setLevel(0);
 						
 						sendInfo("Imbued this " + itemName + " with "
 								+ item.getDurability() + " XP!", player);
 					}
 				}
 				
 			}
 		
 		}
 		
 	} //onPlayerInteract
 	
 	/**
 	 * Send the player a information text with the default text color.
 	 * @param s message
 	 * @param p player to inform
 	 */
 	private void sendInfo(String s, Player p) {
 		p.sendMessage(textColor + s);
 	}
 	
 } //class
