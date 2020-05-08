 package net.krinsoft.deathcounter.listeners;
 
 import net.krinsoft.deathcounter.DeathCounter;
 import net.krinsoft.deathcounter.Monster;
import org.bukkit.entity.*;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import java.text.DecimalFormat;
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class EntityListener extends org.bukkit.event.entity.EntityListener {
     private DeathCounter plugin;
     
     public EntityListener(DeathCounter plugin) {
         this.plugin = plugin;
     }
    
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
         String world = event.getEntity().getWorld().getName();
         if (!plugin.validWorld(world)) { return; }
 
         if (!(event.getEntity() instanceof LivingEntity)) { return; }
         // see if the event was an entity killing another entity
         if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
             // cast to entity damage by entity to check the cause of the damage
             EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
             Player killer;
             if (evt.getDamager() instanceof Player) {
                 killer = (Player) evt.getDamager();
             } else if (evt.getDamager() instanceof Projectile) {
                 if (((Projectile)evt.getDamager()).getShooter() instanceof Player) {
                     killer = (Player) ((Projectile)evt.getDamager()).getShooter();
                 } else {
                     return;
                 }
             } else {
                 return;
             }
             Monster monster = Monster.getType(event.getEntity());
             plugin.getManager().getKiller(killer.getName()).update(monster.getName());
             double amount = 0;
             if (plugin.getBank() != null) {
                 try {
                     if (!monster.getCategory().equalsIgnoreCase("players")) {
                         List<Double> range = plugin.getConfig().getDoubleList("economy." + monster.getCategory() + "." + monster.getName());
                         double min = range.get(0);
                         double max = range.get(1);
                         amount = Double.valueOf(new DecimalFormat("#.##").format(min + (Math.random() * ((max - min)))));
                     } else {
                         Player dead = (Player) event.getEntity();
                         List<Double> range = plugin.getConfig().getDoubleList("economy.players.reward");
                         double min = range.get(0);
                         double max = range.get(1);
                         amount = Double.valueOf(new DecimalFormat("#.##").format(min + (Math.random() * ((max - min)))));
                         if (plugin.getConfig().getBoolean("economy.players.percentage")) {
                             double balance = plugin.getBank().getBalance(dead, -1);
                             amount = balance * (amount / 100);
                         }
                         if (plugin.getConfig().getBoolean("economy.players.realism")) {
                             double balance = plugin.getBank().getBalance(dead, -1);
                             if (amount > balance) {
                                 amount = balance;
                             }
                             plugin.getBank().take(dead, amount, -1);
                         }
                     }
                     amount = plugin.diminishReturn(killer, amount);
                     plugin.getBank().give(killer, amount, -1);
                 } catch (NullPointerException e) {
                     plugin.debug(e.getLocalizedMessage() + ": Found null path at 'economy." + monster.getCategory() + "." + monster.getName() + "' in 'config.yml'");
                 } catch (ArrayIndexOutOfBoundsException e) {
                     plugin.debug(e.getLocalizedMessage() + ": Invalid list at 'economy." + monster.getCategory() + "." + monster.getName() + "'");
                 }
             }
             plugin.report(killer, monster, amount);
         }
     }
     
 }
