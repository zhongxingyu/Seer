 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveEffectSkill;
 
 public class SkillSafefall extends ActiveEffectSkill {
 
     public SkillSafefall(Heroes plugin) {
         super(plugin);
         setName("Safefall");
        setDescription("Skill - Safefall");
         setUsage("/skill safefall");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("skill safefall");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
         applyEffect(hero);
         notifyNearbyPlayers(player.getLocation(), getUseText(), playerName, getName());
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || event.getCause() != DamageCause.FALL) {
                 return;
             }
 
             Entity defender = event.getEntity();
             if (defender instanceof Player) {
                 Player player = (Player) defender;
                 Hero hero = plugin.getHeroManager().getHero(player);
                 if (hero.hasEffect(getName())) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
