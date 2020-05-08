 package me.tehbeard.BeardStat.listeners;
 
 import java.util.List;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.ComplexEntityPart;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.ThrownPotion;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.potion.*;
 
 public class StatEntityListener implements Listener{
 
 
     List<String> worlds;
     private PlayerStatManager playerStatManager;
 
     private final String[] DAMAGELBLS = {"damagedealt","damagetaken"};
     private final String[] KDLBLS = {"kills","deaths"};
 
     public StatEntityListener(List<String> worlds,PlayerStatManager playerStatManager){
         this.worlds = worlds;
         this.playerStatManager = playerStatManager;
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEntityDamage(EntityDamageEvent event) {
 
         if(event.isCancelled()==false && !worlds.contains(event.getEntity().getWorld().getName())){
 
             processEntityDamage(event, DAMAGELBLS);
 
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEntityDeath(EntityDeathEvent event) {
 
         //ignore the blacklisted worlds
         if(worlds.contains(event.getEntity().getWorld().getName())){
             return;
         }
 
         EntityDamageEvent lastCause = event.getEntity().getLastDamageCause();
         if(lastCause != null){
             processEntityDamage(lastCause, KDLBLS);
         }
 
     }
 
     /**
      * ATTACKER NAME, ATTACKED NAME
      * @param event
      * @param category
      */
     private void processEntityDamage(EntityDamageEvent event,String[] category){
         //Initialise base stats
         Entity attacked = event.getEntity();
         DamageCause cause = event.getCause();
         int amount = event.getDamage();
         Entity attacker = null;
         Projectile projectile = null;
 
         //grab the attacker if one exists.
         if(event instanceof EntityDamageByEntityEvent){
             attacker = ((EntityDamageByEntityEvent) event).getDamager();
         }
         //Projectile -> projectile + attacker
         if(attacker instanceof Projectile){
             projectile = (Projectile)attacker;
             attacker = projectile.getShooter();
         }
 
         //dragon fixer
         if(attacker instanceof ComplexEntityPart){
             attacker = ((ComplexEntityPart)attacker).getParent();
         }
         if(attacked instanceof ComplexEntityPart){
             attacked = ((ComplexEntityPart)attacked).getParent();
         }
 
         //get the player
         Player player = attacked instanceof Player ? (Player)attacked : attacker instanceof Player ? (Player)attacker : null;
         Entity other = attacked instanceof Player ? attacker : attacker instanceof Player ? attacked : null;
         int idx = attacker instanceof Player ? 0 : 1;
 
         if(player == null){return;}//kill if no player involved
 
         PlayerStatBlob blob = playerStatManager.getPlayerBlob(player.getName());
 
         //Total damage
         blob.getStat(category[idx], "total").incrementStat(amount);
 
 
        //Damage cause
        if(cause != DamageCause.PROJECTILE){
            blob.getStat(category[idx], cause.toString().toLowerCase().replace("_","")).incrementStat(amount);
        }
         //Entity damage
        if(other !=null && !(other instanceof Player)){
             MetaDataCapture.saveMetaDataEntityStat(blob, category[idx], other, amount);
         }
         //Projectile damage
         if(projectile!=null){
             blob.getStat(category[idx], projectile.getType().toString().toLowerCase().replace("_","")).incrementStat(amount);
         }
 
         //TODO: pvp Damage
         if(attacker instanceof Player  && attacked instanceof Player){
             PlayerStatBlob attackerBlob = playerStatManager.getPlayerBlob(((Player)attacker).getName());
             PlayerStatBlob attackedBlob = playerStatManager.getPlayerBlob(((Player)attacked).getName());

            attackerBlob.getStat(category[0],"pvp").incrementStat(1);
            attackedBlob.getStat(category[1],"pvp").incrementStat(1);
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
 
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
 
         if(event.isCancelled()==false && event.getEntity() instanceof Player && !worlds.contains(event.getEntity().getWorld().getName())){
             int amount = event.getAmount();
             RegainReason reason = event.getRegainReason();
             playerStatManager.getPlayerBlob(((Player)event.getEntity()).getName()).getStat("stats","damagehealed").incrementStat(amount);
             if(reason != RegainReason.CUSTOM){
                 playerStatManager.getPlayerBlob(((Player)event.getEntity()).getName()).getStat("stats","heal" + reason.toString().replace("_", "").toLowerCase()).incrementStat(amount);	
             }
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEntityTame(EntityTameEvent event) {
         if(event.isCancelled()==false && event.getOwner() instanceof Player && !worlds.contains(event.getEntity().getWorld().getName())){
             playerStatManager.getPlayerBlob(((Player)event.getOwner()).getName()).getStat("stats","tame"+event.getEntity().getType().toString().toLowerCase().replace("_", "")).incrementStat(1);
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPotionSplash(PotionSplashEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPotion().getWorld().getName())){
             ThrownPotion potion = event.getPotion();
 
             for(Entity e :event.getAffectedEntities()){
                 if(e instanceof Player){
                     Player p = (Player) e;
                     playerStatManager.getPlayerBlob(p.getName()).getStat("potions","splashhit").incrementStat(1);
                     //added per potion details
                     for(PotionEffect potionEffect : potion.getEffects()){
                         String effect = potionEffect.getType().toString().toLowerCase().replaceAll("_", "");
                         playerStatManager.getPlayerBlob(p.getName()).getStat("potions","splash" + effect).incrementStat(1);
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onBowShoot(EntityShootBowEvent event){
 
         if(event.isCancelled()==false && !worlds.contains(event.getEntity().getWorld().getName())){
             if(event.getEntity() instanceof Player){
                 Player p = (Player) event.getEntity();
 
                 //total shots fired
                 playerStatManager.getPlayerBlob(p.getName()).getStat("bow","shots").incrementStat(1);
 
                 if(event.getBow().containsEnchantment(Enchantment.ARROW_FIRE)){
                     playerStatManager.getPlayerBlob(p.getName()).getStat("bow","fireshots").incrementStat(1);
                 }
 
                 if(event.getBow().containsEnchantment(Enchantment.ARROW_INFINITE)){
                     playerStatManager.getPlayerBlob(p.getName()).getStat("bow","infiniteshots").incrementStat(1);
                 }
 
             }
         }
     }
 }
