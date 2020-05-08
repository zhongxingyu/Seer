 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 
 public class SkillGills extends PassiveSkill {
 
     public SkillGills(Heroes plugin) {
         super(plugin);
         name = "Gills";
         description = "Negate drowning damage";
         minArgs = 0;
         maxArgs = 0;
     }
 
     public class SkillPlayerListener extends EntityListener {
 
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event.getCause() == DamageCause.DROWNING)) {
                 return;
             }
             if (event.getEntity() instanceof Player) {
                 Player player = (Player) event.getEntity();
                 Hero hero = plugin.getHeroManager().getHero(player);
                 if (hero.getEffects().hasEffect(name)) {
                    event.setDamage(0);
                 }
             }
         }
     }
 }
