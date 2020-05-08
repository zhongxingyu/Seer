 package to.joe.strangeweapons.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 import to.joe.strangeweapons.StrangeWeapons;
 import to.joe.strangeweapons.meta.Crate;
 import to.joe.strangeweapons.meta.StrangeWeapon;
 
 public class DestructionListener implements Listener {
 
     public DestructionListener(StrangeWeapons plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @SuppressWarnings("deprecation")
     @EventHandler
     public void onInteract(PlayerInteractEntityEvent event) {
         Player p = event.getPlayer();
         ItemStack item = event.getPlayer().getItemInHand();
         if (event.getRightClicked() instanceof PoweredMinecart && item.getType() == Material.COAL && StrangeWeapon.isStrangeWeapon(p.getItemInHand())) {
             event.setCancelled(true);
             p.updateInventory();
             p.sendMessage(ChatColor.RED + "You may not use that in a powered minecart.");
         }
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         if (Crate.isCrate(event.getItemInHand())) {
             event.setCancelled(true);
             event.getPlayer().sendMessage(ChatColor.RED + "You may not place Steve Co. Supply Crates");
         } else if (event.getItemInHand().getType().isBlock() && StrangeWeapon.isStrangeWeapon(event.getItemInHand())) {
             event.setCancelled(true);
             event.getPlayer().sendMessage(ChatColor.RED + "You may not place strange weapons");
         }
     }
 }
