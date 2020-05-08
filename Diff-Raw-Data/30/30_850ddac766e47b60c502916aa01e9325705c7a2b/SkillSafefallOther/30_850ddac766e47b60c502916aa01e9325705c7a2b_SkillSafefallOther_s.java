 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 
 public class SkillSafefallOther extends TargettedSkill {
 
     public SkillSafefallOther(Heroes plugin) {
         super(plugin);
         setName("SafefallOther");
        setDescription("Skill - Safefall");
         setUsage("/skill safefallother <target>");
         setMinArgs(0);
         setMaxArgs(1);
         getIdentifiers().add("skill safefallother");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 5000);
         return node;
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
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (target instanceof Player) {
             Hero newHero = plugin.getHeroManager().getHero((Player) target);
             long duration = getSetting(hero.getHeroClass(), "duration", 5000);
             newHero.applyEffect(getName(), duration);
             notifyNearbyPlayers(player.getLocation(), getUseText(), player.getName(), getName(), target == player ? "himself" : getEntityName(target));
             return true;
         } else {
             return false;
         }
 
     }
 }
