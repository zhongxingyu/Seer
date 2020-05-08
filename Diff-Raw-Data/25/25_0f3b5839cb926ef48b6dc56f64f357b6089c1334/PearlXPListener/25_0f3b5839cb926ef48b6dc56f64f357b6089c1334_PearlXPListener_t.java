 package PearlXP;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
 
public class PearlXPListener implements Listener {
 	
 	public PearlXP instance;
 	
 	
 	private static String itemName = "pearl";
 

 	public PearlXPListener(PearlXP owner) {
 		instance = owner;
 
 	}
 	
 	
 	
 	@EventHandler
	public void onPlayerInteract (PlayerInteractEvent event) {
 		ItemStack item = event.getItem();
 		int itemId = instance.getConfig().getInt("itemid");
 		
 		int maxLevel;
 		int playerXp;
 		
 		Player ply = event.getPlayer();
 		
 		if (item != null && item.getTypeId() == itemId) {
 			
 			
			if (item.getDurability() > 0 && event.getAction() == Action.RIGHT_CLICK_AIR
					|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 				
 				ply.sendMessage("This " + itemName + " is imbued with "
 						+ item.getDurability() + " XP!");
				event.setCancelled(true);
 				
 			}
 			
			if (event.getAction() == Action.LEFT_CLICK_AIR
					|| event.getAction() == Action.LEFT_CLICK_BLOCK) {
 				
 				if (item.getAmount() > 1) {
 					ply.sendMessage("To store experience in the " + itemName
 							+ ", please unstack them!");
 					return;
 				}
 				
 
 				if (item.getDurability() > 0) {
 					
 					ply.giveExp(item.getDurability());
 					
 					ply.sendMessage("Restoring " + item.getDurability() + " XP! You now have " 
 								+ ply.getTotalExperience() + " XP!");
 					
 					item.setDurability((short) 0);
 					
 				} else { // the item is empty
 					
 					maxLevel = instance.getConfig().getInt("maxlevel");
 					
 					if (ply.getTotalExperience() > maxLevel) {
 						
 						// remove only the maximum xp
 						playerXp = ply.getTotalExperience() - maxLevel;
 						item.setDurability( (short) maxLevel);
 						ply.setTotalExperience(0);
 						ply.setLevel(0);
 						ply.giveExp(playerXp);
 						
 						ply.sendMessage("Imbued this " + itemName + " with "
 								+ item.getDurability() + " XP! You have " + ply.getTotalExperience() + "XP left!" );
 					} else {
 						
 						//Get Player XP and Set it to the Item Durability
 						item.setDurability( (short) ply.getTotalExperience()); 
 
 						ply.setTotalExperience(0);
 						ply.setLevel(0);
 						
 						ply.sendMessage("Imbued this " + itemName + " with "
 								+ item.getDurability() + " XP!");
 					}
 				}
 				
 			}
 		
 		}
 		
 	} //onPlayerInteract
 	
 } //class
