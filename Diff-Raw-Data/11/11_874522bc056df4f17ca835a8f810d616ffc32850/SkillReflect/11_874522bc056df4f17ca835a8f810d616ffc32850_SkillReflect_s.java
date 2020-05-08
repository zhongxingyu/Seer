 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroEffects;
 import com.herocraftonline.dev.heroes.skill.ActiveEffectSkill;
 
 public class SkillReflect extends ActiveEffectSkill {
 
     public SkillReflect(Heroes plugin) {
         super(plugin);
         name = "Reflect";
         description = "Skill - Reflect";
         usage = "/skill reflect";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill reflect");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
         applyEffect(hero);
         notifyNearbyPlayers(player.getLocation(), useText, playerName, name);
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent)) {
                 return;
             }
 
             EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
             Entity defender = edbe.getEntity();
             Entity attacker = edbe.getDamager();
             if (attacker instanceof LivingEntity && defender instanceof Player) {
                 Player player = (Player) defender;
 
                 HeroEffects effects = plugin.getHeroManager().getHero(player).getEffects();
                 if (effects.hasEffect(name)) {
                     if (attacker instanceof Player) {
                         Player att = (Player) attacker;
                         HeroEffects attEffects = plugin.getHeroManager().getHero(att).getEffects();
                         if (attEffects.hasEffect(name)) {
                             event.setCancelled(true);
                             return;
                         }
                     }
                     LivingEntity atk = (LivingEntity) attacker;
                     atk.damage(event.getDamage(), defender);
                    event.setCancelled(true);
                 }
             }
         }
     }
 }
