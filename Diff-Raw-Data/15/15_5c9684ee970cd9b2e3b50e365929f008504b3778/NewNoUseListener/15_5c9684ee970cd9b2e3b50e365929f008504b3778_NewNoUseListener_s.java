 package net.worldoftomorrow.nala.ni.listeners;
 
 import net.worldoftomorrow.nala.ni.EventTypes;
 import net.worldoftomorrow.nala.ni.Log;
 import net.worldoftomorrow.nala.ni.Perms;
 import net.worldoftomorrow.nala.ni.StringHelper;
 import net.worldoftomorrow.nala.ni.Items.Tools;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class NewNoUseListener implements Listener {
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Log.debug("Player Interact Event");
         if (event.isBlockInHand())
             return; // Return if it is a block place event
         Action action = event.getAction();
         if (action.equals(Action.LEFT_CLICK_BLOCK)) {
             this.handleBlockLeftClick(event);
         }
         if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
             this.handleBlockRightClick(event);
         }
     }
 
     private void handleBlockLeftClick(PlayerInteractEvent event) {
         Player p = event.getPlayer();
         Block clicked = event.getClickedBlock();
         ItemStack inHand = event.getItem();
         if (inHand != null) {
             switch (inHand.getType()) {
             case FLINT_AND_STEEL:
                 if (Perms.NOUSE.has(p, inHand)) {
                     event.setCancelled(true);
                     return; // return if it should be cancelled
                 }
                 break; // break if not, it could be cancelled later
             default:
                 break;
             }
         }
 
         switch (clicked.getType()) {
         case LEVER:
         case STONE_BUTTON:
         case WOODEN_DOOR:
             event.setCancelled(this.handleInteract(p, clicked));
             return;
         default:
             break;
         }
     }
 
     private void handleBlockRightClick(PlayerInteractEvent event) {
         Player p = event.getPlayer();
         Block clicked = event.getClickedBlock();
         ItemStack inHand = event.getItem();
 
         if (inHand != null) {
             switch (inHand.getType()) {
             case FLINT_AND_STEEL:
                 if (Perms.NOUSE.has(p, inHand)) {
                     event.setCancelled(true);
                     return; // return if it should be cancelled
                 }
                 break; // break if not; it could be cancelled later
             default:
                 break;
             }
         }
 
         switch (clicked.getType()) {
         case GRASS:
         case DIRT:
             event.setCancelled(this.handleHoe(p, inHand));
             break;
         case LEVER:
         case STONE_BUTTON:
         case WOODEN_DOOR:
         case BREWING_STAND:
         case WORKBENCH:
         case FURNACE:
         case DISPENSER:
         case ENCHANTMENT_TABLE:
             event.setCancelled(this.handleInteract(p, clicked));
             return;
         default:
             break;
         }
     }
 
     private boolean handleHoe(Player p, ItemStack inHand) {
         int id = inHand.getTypeId();
         if (id >= 290 && id <= 294) {
             if (Perms.NOUSE.has(p, inHand)) {
                 StringHelper.notifyPlayer(p, EventTypes.USE, id);
                 StringHelper.notifyAdmin(p, EventTypes.USE, inHand);
                 return true;
             }
         }
         return false;
     }
 
     private boolean handleInteract(Player p, Block clicked) {
         return Perms.NOUSE.has(p, clicked);
     }
 
     @EventHandler
     public void onBowShoot(EntityShootBowEvent event) {
         if (event.getEntity() instanceof Player) {
             Player p = (Player) event.getEntity();
             ItemStack inHand = p.getItemInHand();
             if(Perms.NOUSE.has(p, inHand.getType(), inHand.getDurability())) {
                 event.setCancelled(true);
             }
             // TODO: test
         }
     }
     
     @EventHandler
 	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
 		Player p = event.getPlayer();
 		int bucketID = event.getBucket().getId();
 		if (Perms.NOUSE.has(p, event.getItemStack())) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, bucketID);
 			StringHelper.notifyAdmin(p, EventTypes.USE, event.getItemStack());
 		}
 	}
 
 	@EventHandler
 	public void onBucketFill(PlayerBucketFillEvent event) {
 		Player p = event.getPlayer();
 		int bucketID = event.getBucket().getId();
 		if (Perms.NOUSE.has(p, event.getItemStack())) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, bucketID);
 			StringHelper.notifyAdmin(p, EventTypes.USE, event.getItemStack());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerShear(PlayerShearEntityEvent event) {
 		Player p = event.getPlayer();
 		ItemStack shears = new ItemStack(Material.SHEARS);
 		if (Perms.NOUSE.has(p, shears)) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, shears.getTypeId());
 			StringHelper.notifyAdmin(p, EventTypes.USE, shears);
 		}
 	}
 
 	@EventHandler
 	public void onSwordSwing(EntityDamageByEntityEvent event) {
 		Entity damager = event.getDamager();
 		if (damager instanceof Player) {
 			Player p = (Player) damager;
 			ItemStack stack = new ItemStack(p.getItemInHand());
 			stack.setDurability((short) 0);
 			Material type = stack.getType();
 			if (Tools.isTool(type) && Perms.NOUSE.has(p, stack)) {
 				event.setCancelled(true);
 				StringHelper.notifyPlayer(p, EventTypes.USE, stack.getTypeId());
 				StringHelper.notifyAdmin(p, EventTypes.USE, stack);
 			}
 		}
 	}
 }
