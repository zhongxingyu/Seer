 package net.siriuser.expmanager.listeners;
 
 import net.siriuser.expmanager.ExpManager;
 import net.siriuser.expmanager.Helper;
 import net.siriuser.expmanager.storage.ConfigurationManager;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class EntityListener implements Listener {
     private ExpManager plugin;
     private ConfigurationManager config;
 
     public EntityListener(final ExpManager plugin) {
         this.plugin = plugin;
         this.config = Helper.getInstance().getConfig();
     }
 
     @EventHandler
     public void EntityDeathEvent (final EntityDeathEvent event) {
         final LivingEntity entity = event.getEntity();
 
        int dropExp = config.getEntitySection().getInt(entity.getType().toString(), -1);
         if (dropExp == -1) {
             dropExp = event.getDroppedExp();
         }
 
         if (Helper.getInstance().getConfig().getDirectExp()) {
             if (entity.getLastDamageCause() == (null)) return;
             final EntityDamageEvent cause = entity.getLastDamageCause();
             if (cause instanceof EntityDamageByEntityEvent) {
                 Entity killer = ((EntityDamageByEntityEvent) cause).getDamager();
 
                 Player killerP;
                 if (killer instanceof Player) {
                     killerP = (Player)killer;
                 } else if (killer instanceof Wolf && ((Wolf) killer).isTamed()) {
                     Wolf killerW = (Wolf)killer;
                     killerP = (Player)killerW.getOwner();
                 } else if (killer instanceof Arrow && ((Arrow) killer).getShooter() instanceof Player) {
                     killerP = (Player) ((Arrow) killer).getShooter();
                 } else {
                     return;
                 }
 
                 killerP.giveExp(dropExp);
                 event.setDroppedExp(0);
             }
         } else {
             event.setDroppedExp(dropExp);
         }
 
         ExpManager.Debug("Entity: " + entity.getType().toString());
     }
 }
