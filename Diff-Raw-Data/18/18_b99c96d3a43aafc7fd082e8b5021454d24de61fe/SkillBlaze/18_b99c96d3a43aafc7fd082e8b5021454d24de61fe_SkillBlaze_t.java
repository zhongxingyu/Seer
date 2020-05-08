 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillBlaze extends ActiveSkill {
 
     public SkillBlaze(Heroes plugin) {
         super(plugin, "Blaze");
         setDescription("Sets everyone around you on fire");
         setUsage("/skill blaze");
         setArgumentRange(0, 0);
         setIdentifiers("skill blaze");
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("fire-length", 3000);
         node.setProperty(Setting.RADIUS.node(), 5);
 
         setTypes(SkillType.FIRE, SkillType.DAMAGING, SkillType.HARMFUL);
 
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         int range = getSetting(hero.getHeroClass(), Setting.RADIUS.node(), 5);
         List<Entity> entities = hero.getPlayer().getNearbyEntities(range, range, range);
         int fireTicks = getSetting(hero.getHeroClass(), "fire-length", 3000);
         for (Entity entity : entities) {
             if (!(entity instanceof LivingEntity)) {
                 continue;
             }
            LivingEntity lEntity = (LivingEntity) entity;
            if (damageCheck(hero.getPlayer(), lEntity))
                continue;
            lEntity.setFireTicks(fireTicks);
         }
         broadcastExecuteText(hero);
         return true;
     }
 }
