 package cz.ogion.ultraitems;
 
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 import org.getspout.spoutapi.SpoutManager;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener {
 	UltraItems plugin;
 	Map<String, ConfigurationNode> config;
 
 	public PlayerListener(UltraItems instance) {
 		plugin = instance;
 	}
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		config = plugin.config;
 		if(event.hasItem()){
 			if (config != null) {
 				Action action = event.getAction();
 				Player player = event.getPlayer();
 				Integer eventitemid = event.getItem().getTypeId();
 				Integer eventitemdata = ((Short) event.getItem().getDurability()).intValue();
 				for(ConfigurationNode item : config.values()) {
 					Integer itemid = item.getInt("item", 0);
 					Integer itemdata = item.getInt("data", 0);
 					ConfigurationNode lclick = item.getNode("lclick");
 					ConfigurationNode rclick = item.getNode("rclick");
 					if(itemid != 0 && itemid.equals(eventitemid) && itemdata.equals(eventitemdata)) {
 						if((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && lclick != null) {
 							if (lclick.getString("action", null) != null) {
 								player.chat(lclick.getString("action"));
 							}
 							if (lclick.getBoolean("consume", false)) {
 								ItemStack is = player.getItemInHand();
 								is.setAmount(is.getAmount() - 1);
 							//	player.setItemInHand(is);
 							}
 							if (lclick.getInt("health", 0) != 0) {
 								player.setHealth(player.getHealth() + lclick.getInt("health", 0));
 							}
 							if (lclick.getInt("hunger", 0) != 0) {
 								player.setFoodLevel(player.getFoodLevel() + lclick.getInt("hunger", 0));
 							}
 							if (lclick.getString("sound", null) != null){
 								SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, lclick.getString("sound"), false, player.getLocation());
 							}
 						} else if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && rclick != null) {
 							if (rclick.getString("action", null) != null) {
 								player.chat(rclick.getString("action"));
 							}
 							if (rclick.getBoolean("consume", false)) {
 								ItemStack is = player.getItemInHand();
 								is.setAmount(is.getAmount() - 1);
 								player.setItemInHand(is);
 							}
 							if (rclick.getInt("health", 0) != 0) {
 								player.setHealth(player.getHealth() + rclick.getInt("health", 0));
 							}
 							if (rclick.getInt("hunger", 0) != 0) {
 								player.setFoodLevel(player.getFoodLevel() + rclick.getInt("hunger", 0));
 							}
 							if (rclick.getString("sound", null) != null){
 								SpoutManager.getSoundManager().playGlobalCustomSoundEffect(plugin, rclick.getString("sound"), false, player.getLocation());
 							}
 							event.setCancelled(true);
 							event.setUseInteractedBlock(Result.DENY);
 							event.setUseItemInHand(Result.DENY);
 						} else {
 						}
 					}
 				}
 			}
 		}
 	}
 }
