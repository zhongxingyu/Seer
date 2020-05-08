 package info.nebtown.PearlXP;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.block.Action;
 
 public class PearlXPListener implements Listener {
 	
 	
 	
 	private static String itemName = "pearl";
 	private static ChatColor textColor = ChatColor.BLUE;
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		ItemStack item = event.getItem();
 		Enchantment enchantment = Enchantment.OXYGEN;
 		
 		int maxLevel = PearlXP.getMaxLevel();
 		int xpStored = item.getEnchantmentLevel(enchantment);
 		
 		Player player = event.getPlayer();
 		
 		if (item != null && item.getTypeId() == PearlXP.getItemId()) {
 			
 			
 			if (item.getAmount() == 1 && (event.getAction() == Action.RIGHT_CLICK_AIR
 					|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
 				
 				
 				
 				if (item.getEnchantmentLevel(enchantment) != 0) {
 					// the item have stored XP
 					
 					event.setCancelled(true); // keep the item !
 					
 					sendInfo("This " + itemName + " is imbued with "
 							+ xpStored + " XP!", player);
 					
 				} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 					
 					event.setCancelled(true); // keep the item !
 					
 					// the item is empty and the player clicked "on is feet"
 					sendInfo("This " + itemName + " is empty.", player);
 				}
 				
 				
 			} else if (event.getAction() == Action.LEFT_CLICK_AIR
 						|| event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				
 				if (item.getAmount() > 1) {
 					sendInfo("To store experience in the " + itemName
 							+ ", please unstack them!", player);
 					return;
 				}
 				
 
 				if (xpStored > 0) {
 					
 					player.giveExp(xpStored);
 					
 					sendInfo("+Restoring " + xpStored + " XP! You now have " 
 								+ player.getTotalExperience() + " XP!", player);
 					
 					sendInfo("Enchent lvl : " + item.getEnchantmentLevel(enchantment), player);
 					item.removeEnchantment(enchantment);
 					
 				} else { // the item is empty
 					
 					// Visual and sound effects
 					player.getWorld().playEffect(player.getEyeLocation(), Effect.ENDER_SIGNAL, 0);
 					player.playEffect(player.getEyeLocation(), Effect.EXTINGUISH, 0);
 					
 					if (player.getTotalExperience() > maxLevel) {
 						
 						
 						item.addUnsafeEnchantment(enchantment, maxLevel);
 						
 						removePlayerXp(maxLevel, player);
 						
 						sendInfo("-Imbued this " + itemName + " with "
 								+ maxLevel + " XP! You have " + player.getTotalExperience() + "XP left!", player);
 					} else {
 						
 						//Get Player XP and store it into the item
 						item.addUnsafeEnchantment(enchantment, player.getTotalExperience()); 
 
 						removePlayerXp(player.getTotalExperience(), player);
 						
 						sendInfo("Imbued this " + itemName + " with "
 								+ item.getEnchantmentLevel(enchantment) + " XP!", player);
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
 	
 	/**
 	 * Remove a number of XP from a given player
 	 * @param xp the XP to remove
 	 * @param p player
 	 */
 	private void removePlayerXp(int xp, Player p) {
 		int currentXp = p.getTotalExperience();
 		
 		// Reset level to fix update bug
 		p.setTotalExperience(0);
		p.setExp(0);
 		p.setLevel(0);
 		
 		p.giveExp(currentXp - xp);
 		
 	}
 	
 } //class
