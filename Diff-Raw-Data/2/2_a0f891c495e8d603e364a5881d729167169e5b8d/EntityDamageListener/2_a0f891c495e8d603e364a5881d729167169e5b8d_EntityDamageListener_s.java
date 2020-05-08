 package com.github.leoverto.foolsgoldplugin;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class EntityDamageListener implements Listener {
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onEntityBlock(EntityDamageEvent event) {
 		if ((Boolean) FoolsGoldPlugin.hayJumpConfig.get("enabled")) {
 			if (event.getCause().equals(DamageCause.FALL)) {
 				if (event.getEntityType().equals(EntityType.PLAYER)) {
 					Player ePlayer = (Player) event.getEntity();
 					Location playerLocation = ePlayer.getLocation();
 					Location locationBelowPlayer = new Location(ePlayer.getWorld(), playerLocation.getX(), playerLocation.getY() - 1, playerLocation.getZ());
 					Block blockBelowPlayer = locationBelowPlayer.getBlock();
 					
 					if (blockBelowPlayer.getTypeId() == Material.SPONGE.getId()) {
 						Location locBelowBelowPlayer = new Location(ePlayer.getWorld(), playerLocation.getX(), playerLocation.getY() - 2, playerLocation.getZ());
 						Block blockBelowBelowPlayer = locBelowBelowPlayer.getBlock();
 						
 						int amountOfWheat;
 						if (blockBelowBelowPlayer.getTypeId() == Material.SPONGE.getId()) {
 							event.setDamage(0);
 							amountOfWheat = (Integer) FoolsGoldPlugin.hayJumpConfig.get("normalJumpHayAmount");
 						} else {
 							event.setDamage(event.getDamage() / 2);
 							amountOfWheat = (Integer) FoolsGoldPlugin.hayJumpConfig.get("halfJumpHayAmount");
 						}
 						
						if (amountOfWheat == 0) {
 						
 							final World playerWorld = ePlayer.getWorld();
 							List<Integer> thingsToDelete = new ArrayList<Integer>();
 							
 							for (int i = 1; i < amountOfWheat; i++) {
 								ItemStack flyingWheat = new ItemStack(Material.WHEAT, 1);
 								ItemMeta flyingWheatMeta = flyingWheat.getItemMeta();
 								flyingWheatMeta.setDisplayName("Please report this bug #" + i);
 								flyingWheat.setItemMeta(flyingWheatMeta);
 								
 								Entity curWheat = playerWorld.dropItemNaturally(playerLocation, flyingWheat);
 								curWheat.setMetadata("flyingWheatID", new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("FoolsGoldPlugin"), i));
 								thingsToDelete.add(curWheat.getEntityId());
 								FoolsGoldPlugin.itemsToNotPickup.add(curWheat.getEntityId());
 							}
 							
 							final List<Integer> thingsToDeleteFinal = thingsToDelete;
 							
 							
 							new BukkitRunnable(){
 								public void run() {
 									Collection<Item> worldItems = playerWorld.getEntitiesByClass(Item.class);
 									for (Item item : worldItems) {
 										if (thingsToDeleteFinal.contains(item.getEntityId())) {
 											item.remove();
 										}
 									}
 								}
 							}.runTaskLater(Bukkit.getPluginManager().getPlugin("FoolsGoldPlugin"), (Integer) FoolsGoldPlugin.hayJumpConfig.get("hayDecayTicks"));
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
