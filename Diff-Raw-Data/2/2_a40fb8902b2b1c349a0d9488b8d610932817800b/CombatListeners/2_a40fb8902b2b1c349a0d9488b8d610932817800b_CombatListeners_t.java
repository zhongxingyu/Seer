 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.monstuhs.betterleveling.EventHandlers;
 
 import me.monstuhs.betterleveling.Managers.CombatManager;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 /**
  *
  * @author James
  */
 public class CombatListeners implements Listener {
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerCombat(EntityDamageByEntityEvent event) {
         Entity attacker = event.getDamager();
         Entity defender = event.getEntity();
         int damage = event.getDamage();
         
         //Check for headshot
        if(attacker instanceof Arrow && ((Arrow)attacker).getShooter() instanceof Player && defender instanceof LivingEntity){
             damage = CombatManager.getDamageAfterHeadshotAttempt((Arrow)attacker, (LivingEntity)defender, damage);
             //Change attacker for subsequent checks
             attacker = ((Arrow)attacker).getShooter();
         }        
         
         //Check for crit        
         if(attacker instanceof Player) {
             damage = CombatManager.getDamageAfterCritAttempt((Player)attacker, damage);
         }
         
         //Check for dodge        
         if (defender instanceof Player) {
             damage = CombatManager.getDamageAfterDodgeAttempt((Player)defender, damage);            
         }
         
         
         event.setDamage(damage);
     }
 }
