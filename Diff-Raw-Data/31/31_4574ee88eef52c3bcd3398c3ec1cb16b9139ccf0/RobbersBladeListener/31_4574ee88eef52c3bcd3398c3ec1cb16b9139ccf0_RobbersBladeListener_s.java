 package com.cin316.minezweapons;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.cin316.minezweapons.MineZWeapons;
 
 public class RobbersBladeListener implements Listener{
 	
 	MineZWeapons plugin;
 	
 	public RobbersBladeListener(MineZWeapons plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent event) {
 	    
 		Entity entity = event.getEntity();
 		//Check if the entity getting hit is a player
 		if (entity instanceof Player){
 			
 			//Create the variable for the player (getting hit)
 			Player hurted = (Player)entity;
 			//Check if the event is a Entity damage event (basically)
 			if (event instanceof EntityDamageByEntityEvent){
 				
 				//Get a variable for the cause of the damage
 				EntityDamageByEntityEvent damageCause = (EntityDamageByEntityEvent)event;
 				//Check if the damager is a player
 				if (damageCause.getDamager() instanceof Player){
 					
 					//Get the variable for the player who punched someone
 					Player hitter = (Player)damageCause.getDamager();
 					
					//Check if the hitter is holding a Kikuichimonji and it is a wood sword.
 					if( hitter.getItemInHand().getType().equals(Material.WOOD_SWORD) ){
 						if( hitter.getItemInHand().getItemMeta().hasDisplayName() ){
 							if( hitter.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.ITALIC + "Robber's Blade") ){
 								
 								//Do stuff.
 								ItemStack blade = hitter.getItemInHand();
 								Inventory hurtInventory = hurted.getInventory();
 								ItemStack stolenItem = null;
 								int n = 0;
 								
 								//Iterate through random inventory slots until we find one that is filled.
 								while(stolenItem==null){
 									Random rand = new Random();
 									n = rand.nextInt(hurtInventory.getSize());
 									ItemStack value = hurtInventory.getItem(n);
 										
 									if( value!=null ){ //If value is an Item.
 										stolenItem = value;
 									}
 								}
 								
								//Remove from hurted's inventory.
								hurtInventory.setItem(n, null);
								
								//Add to hitter's inventory.
								hitter.getInventory().addItem(stolenItem);
								
								//Break Robber's Blade.
								blade.setDurability((short) 60);
							
 							}
 						}
 					}
 					
 				}
 				
 			}
 			
 		}
 		
 		
 	}
 
 }
