 package co.proxa.durabilitynotify.listeners;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.ItemStack;
 import co.proxa.durabilitynotify.*;
 
 import java.util.List;
 
 public class CombatListener implements Listener {
 
     private ListManager lm;
     private Armor a;
 
     public CombatListener (ListManager lm, Armor a) {
         this.lm = lm;
         this.a = a;
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     void onPlayerInflictDamage(EntityDamageByEntityEvent event) {
         if (event.getDamager() instanceof Player) {
             Player player = (Player) event.getDamager();
 
             if (!Permissions.hasToolPerms(player) || player.getItemInHand().getType() == Material.AIR) {
                 return;
             }
 
             ItemStack item = player.getItemInHand();
             int usesLeft = Tool.getUsesLeft(item);
 
             if (!LiveNotify.checkLiveNotify(player, item, usesLeft)) {
 
                 List<Integer> notifyList = Tool.getToolList(item);
 
                 if (Tool.isSword(item) && notifyList.contains(usesLeft)) {
                     Notify.createToolWarning(player, item, usesLeft, false);
                 } else {
                    if (notifyList.contains(usesLeft) || notifyList.contains(usesLeft+1)) {
                        Notify.createToolWarning(player, item, usesLeft, true);
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     void onPlayerTakeDamage(EntityDamageEvent event) {
         if (event.getEntity() instanceof Player && event.getDamage() > 0) {
 
             Player player = (Player) event.getEntity();
 
             if (!Permissions.hasArmorPerms(player)) {
                 return;
             }
 
             switch (event.getCause()) {
             case DROWNING:
             case POISON:
             case STARVATION:
             case VOID:
             case FALL:
             case FIRE_TICK:
             case SUFFOCATION:
             case MAGIC:
                 return;
             }
 
            //TODO Live armor notifications?
             a.checkArmor(player);
         }
     }
 }
