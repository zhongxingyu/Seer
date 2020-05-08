 package com.ayan4m1.multiarrow;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.permissions.Permission;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.ayan4m1.multiarrow.arrows.*;
 
 /**
  * Handle events for all Player related events
  * @author ayan4m1
  */
 public class MultiArrowPlayerListener extends PlayerListener {
     private final MultiArrow plugin;
 
     public MultiArrowPlayerListener(MultiArrow instance) {
         plugin = instance;
     }
 
     public void onPlayerInteract(PlayerInteractEvent event) {
     	Player player = event.getPlayer();
     	if (player.getItemInHand().getType() == Material.BOW)
     	{
	    	if (event.getAction() == Action.RIGHT_CLICK_AIR) {
 	    		PlayerInventory inventory = player.getInventory();
 	    		if (inventory.contains(Material.ARROW)) {
 	                ItemStack arrowStack = player.getInventory().getItem(player.getInventory().first(Material.ARROW));
 	                if (arrowStack.getAmount() > 1) {
 	                    arrowStack.setAmount(arrowStack.getAmount() - 1);
 	                } else {
 	                    inventory.clear(inventory.first(Material.ARROW));
 	                }
 	                
 	                if (!plugin.activeArrowType.containsKey(player.getName())) {
 	                	plugin.activeArrowType.put(player.getName(), ArrowType.NORMAL);
 	                }
 	                
 	                event.setCancelled(true);
 	                
 	                Arrow arrow = player.shootArrow();
                 
 	                if (plugin.activeArrowType.get(player.getName()) != ArrowType.NORMAL) {
 		                CustomArrowEffect arrowEffect = null;
 		                switch(plugin.activeArrowType.get(player.getName()))
 		                {
 		                case EXPLOSIVE:
 		                	arrowEffect = new ExplosiveArrowEffect();
 		                	break;
 		                case FIRE:
 		                	arrowEffect = new FireArrowEffect();
 		                	break;
 		                case LIGHTNING:
 		                	arrowEffect = new LightningArrowEffect();
 		                	break;
 		                case SHARP:
 		                	arrowEffect = new SharpArrowEffect();
 		                	break;
 		                case DRILL:
 		                	arrowEffect = new DrillArrowEffect();
 		                	break;
 		                }
 		                
 		                if (arrowEffect != null) {
 		                	plugin.activeArrowEffect.put(arrow, arrowEffect);
 		                }
 	                }
 	            }
	    	} else if (event.getAction() == Action.LEFT_CLICK_AIR) {
 	            if (plugin.activeArrowType.containsKey(player.getName())) {
 	            	int arrowTypeIndex = plugin.activeArrowType.get(player.getName()).ordinal();
 
 	            	//If we are at the end of the enum, loop around
 	            	if (arrowTypeIndex == ArrowType.values().length - 1) {
 	            		arrowTypeIndex = 0;
 	            	} else {
 	            		//Advance to the next arrow type
 	            		arrowTypeIndex++;
 	            	}
 	            	
 	            	plugin.activeArrowType.put(player.getName(), ArrowType.values()[arrowTypeIndex]);
 	            } else {
 	            	plugin.activeArrowType.put(player.getName(), ArrowType.NORMAL);
 	            }
	            
	            player.sendMessage(plugin.activeArrowType.get(player.getName()).toString() + " Arrow Active!");
 	    	}
     	}
     }
 }
 
