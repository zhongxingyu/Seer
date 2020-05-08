 package com.lamtrung.datrpg;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.SmallFireball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.entity.EntityCombustByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import org.bukkit.inventory.CraftingInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.metadata.FixedMetadataValue;
 
 public class FireStick implements Listener{
 	
 	public DatRPG rpg;
 	
 	public FireStick (DatRPG plugin){
 		this.rpg = plugin;
 		ItemStack item = new ItemStack(Material.STICK);
 		ItemMeta im = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		lore.add("Fire");
 		im.setDisplayName(ChatColor.RED + "Wand of Flames");
 		im.setLore(lore);
 		item.setItemMeta(im);
 		
 		ShapedRecipe fireStick2 = new ShapedRecipe(item);
 		fireStick2.shape("@%#", "###", "###");
 		fireStick2.setIngredient('@', Material.WRITTEN_BOOK);
 		fireStick2.setIngredient('%', Material.STICK);
 		fireStick2.setIngredient('#', Material.REDSTONE);
 		
 		rpg.getServer().addRecipe(fireStick2);
 	}
 	
 	@EventHandler
 	public void onSpell(PlayerInteractEvent event) {
 		if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
 			if (event.hasItem()) {
 				ItemStack item = event.getItem();
 				if (item.hasItemMeta()) {
 					if (item.getItemMeta().getLore().contains("Fire")) {
 						Projectile p = event.getPlayer().launchProjectile(SmallFireball.class);
 						p.setMetadata("human", new FixedMetadataValue(rpg, Boolean.valueOf(true)));
 						ItemMeta im = item.getItemMeta();
 						List<String> lore = im.getLore();
 						
 						
 						int value;
 						try {
 							value = Integer.parseInt(lore.get(1)) - 1;
 							lore.set(1, Integer.toString(value));
 							im.setLore(lore);
 							item.setItemMeta(im);
 						} catch (IndexOutOfBoundsException e) {
 							event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
 							event.setCancelled(true);
 							return;
 						}
 												
 						if (value == 0) {
 							event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
 						}
 					}
 				}
 			}
 		}
 			
 	}
 
 	
 	@EventHandler
 	public void onEntIg(EntityCombustByEntityEvent event) {
 		if (event.getCombuster() instanceof SmallFireball) {
 			if (event.getCombuster().hasMetadata("human")) {
 				if (event.getCombuster().getMetadata("human").get(0).asBoolean() == true) {
 					if (event.getEntity() instanceof Player)
 						event.setCancelled(true);
 					else if (event.getEntity() instanceof LivingEntity)
 						event.setDuration(2);
 				}	
 			}
 		}
 	}
 	
 	@EventHandler
 	public void blockIgnite(BlockIgniteEvent event) {
 		if (event.getIgnitingEntity() instanceof SmallFireball) {
 			if (event.getIgnitingEntity().hasMetadata("human")) {
 				if (event.getIgnitingEntity().getMetadata("human").get(0).asBoolean() == true)
 					event.setCancelled(true);
 			}
 		}
 	}
 	
 	
 	@EventHandler
 	public void livingHit(EntityDamageByEntityEvent event) {
 		if (event.getDamager() instanceof SmallFireball) {
 			if (event.getDamager().hasMetadata("human")) {
				if (event.getDamager().getMetadata("human").get(0).asBoolean() == true) {
					if (event.getEntity() instanceof Player)
						event.setCancelled(true);
					else
						event.setDamage(event.getDamage()*2.5);
				}
 			}
 		}
 			
 	}
 	
 	@EventHandler
	public void onCraft(InventoryClickEvent event) {
 		if (event.getInventory() instanceof CraftingInventory) {
 			if (event.getSlot() == 0) {
 				if (event.getCurrentItem().hasItemMeta()) {
 					if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Wand of Flames")) {
 						BookMeta bookmeta = (BookMeta) event.getView().getTopInventory().getItem(1).getItemMeta();
 						if (bookmeta.getTitle().equalsIgnoreCase("Ardescat")) {
 							if (event.getCurrentItem().hasItemMeta()) {
 								if (event.getCurrentItem().getItemMeta().getLore().contains("Fire") && ((Player) event.getWhoClicked()).getLevel() >= 10) {
 									if (event.isShiftClick()) {
 										event.setCancelled(true);
 										return;
 									}
 									ItemStack item = event.getCurrentItem();
 									ItemMeta im = item.getItemMeta();
 									List<String> lore = im.getLore();
 									
 									lore.add("1000");
 									im.setLore(lore);
 									item.setItemMeta(im);
 									event.setCurrentItem(item);
 									((Player) event.getWhoClicked()).setLevel(((Player) event.getWhoClicked()).getLevel() - 10);
 
 									return;
 								}
 							}
 						}
 						event.setCancelled(true);
 					}
 				}
 			}
 		}
 	}
 
 	
 
 
 }
