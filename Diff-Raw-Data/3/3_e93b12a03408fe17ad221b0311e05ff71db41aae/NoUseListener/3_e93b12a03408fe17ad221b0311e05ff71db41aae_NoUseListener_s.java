 package net.worldoftomorrow.nala.ni.listeners;
 
 import net.worldoftomorrow.nala.ni.CustomBlocks;
 import net.worldoftomorrow.nala.ni.EventTypes;
 import net.worldoftomorrow.nala.ni.Perms;
 import net.worldoftomorrow.nala.ni.StringHelper;
 import net.worldoftomorrow.nala.ni.Items.Tools;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class NoUseListener implements Listener {
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
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
         case TNT:
             if (inHand != null && inHand.getType() == Material.FLINT_AND_STEEL) {
                 if (Perms.NOUSE.has(p, clicked)) {
                     event.setCancelled(true);
                     this.notify(p, EventTypes.USE,
                             new ItemStack(clicked.getType()));
                 }
             }
             break;
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
                     this.notify(p, EventTypes.USE, inHand);
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
             break;
         case TNT:
             if (inHand != null && inHand.getType() == Material.FLINT_AND_STEEL) {
                 if (Perms.NOUSE.has(p, clicked)) {
                     event.setCancelled(true);
                     this.notify(p, EventTypes.USE,
                             new ItemStack(clicked.getType()));
                 }
             }
             break;
         default:
             break;
         }
 
         if (CustomBlocks.isCustomBlock(clicked)) {
             event.setCancelled(this.handleInteract(p, clicked));
         }
     }
 
     private boolean handleHoe(Player p, ItemStack inHand) {
         int id = inHand.getTypeId();
         if (id >= 290 && id <= 294) {
             if (Perms.NOUSE.has(p, inHand)) {
                 this.notify(p, EventTypes.USE, inHand);
                 return true;
             }
         }
         return false;
     }
 
     private boolean handleInteract(Player p, Block clicked) {
         if (Perms.NOUSE.has(p, clicked)) {
             this.notify(p, EventTypes.USE, new ItemStack(clicked.getType()));
             return true;
         }
         return false;
     }
 
     @EventHandler
     public void onBowShoot(EntityShootBowEvent event) {
         if (event.getEntity() instanceof Player) {
             Player p = (Player) event.getEntity();
             ItemStack inHand = p.getItemInHand();
             if (Perms.NOUSE.has(p, inHand.getType(), inHand.getDurability())) {
                 event.setCancelled(true);
             }
             // TODO: test
         }
     }
 
     @EventHandler
     public void onBucketEmpty(PlayerBucketEmptyEvent event) {
         Player p = event.getPlayer();
         ItemStack stack = event.getItemStack();
         if (Perms.NOUSE.has(p, stack)) {
             event.setCancelled(true);
             this.notify(p, EventTypes.USE, stack);
         }
     }
 
     @EventHandler
     public void onBucketFill(PlayerBucketFillEvent event) {
         Player p = event.getPlayer();
         ItemStack stack = event.getItemStack();
         if (Perms.NOUSE.has(p, stack)) {
             event.setCancelled(true);
             this.notify(p, EventTypes.USE, stack);
         }
     }
 
     @EventHandler
     public void onPlayerShear(PlayerShearEntityEvent event) {
         Player p = event.getPlayer();
         ItemStack shears = new ItemStack(Material.SHEARS);
         if (Perms.NOUSE.has(p, shears)) {
             event.setCancelled(true);
             this.notify(p, EventTypes.USE, shears);
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
                 this.notify(p, EventTypes.USE, stack);
             }
         }
     }
 
     private void notify(Player p, EventTypes type, ItemStack stack) {
         StringHelper.notifyPlayer(p, type, stack);
         StringHelper.notifyAdmin(p, type, stack);
     }
 }
