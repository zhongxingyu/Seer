 package com.herocraftonline.dev.heroes.party;
 
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 
 public class PartyEntityListener extends EntityListener {
     public Heroes plugin;
 
     public PartyEntityListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent initialEvent) {
        plugin.log(Level.INFO, initialEvent.getCause().toString());
         if (initialEvent instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) initialEvent;
             if (subEvent.getEntity() instanceof Player && subEvent.getDamager() instanceof Player) {
                 Player attacker = (Player) subEvent.getDamager();
                 Player defender = (Player) subEvent.getEntity();
                 HeroParty attackParty = plugin.getHeroManager().getHero(attacker).getParty();
 
                 if (attackParty == null) {
                     return;
                 }
                plugin.log(Level.INFO, Boolean.toString(attackParty.isPartyMember(defender)));
                 if (attackParty.isPartyMember(defender) && attackParty.getPvp()) {
                     initialEvent.setCancelled(true);
                 }
             }
         }
     }
 }
