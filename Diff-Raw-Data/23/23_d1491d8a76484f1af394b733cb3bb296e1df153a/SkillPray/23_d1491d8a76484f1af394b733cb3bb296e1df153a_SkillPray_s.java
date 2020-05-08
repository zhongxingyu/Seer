 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillPray extends TargettedSkill {
 
     public SkillPray(Heroes plugin) {
         super(plugin);
         setName("Pray");
         setDescription("Heals the target");
         setUsage("/skill pray <target>");
         setMinArgs(0);
         setMaxArgs(1);
         getIdentifiers().add("skill pray");
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("health", 10);
         node.setProperty(SETTING_MAXDISTANCE, 25);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (target instanceof Player) {
             int hpPlus = getSetting(hero.getHeroClass(), "health", 10);
            int targetHealth = target.getHealth();
 
            if (targetHealth >= 20) {
                 Messaging.send(player, "Target is already fully healed.");
                 return false;
             }
 
            if (targetHealth + hpPlus > 20) {
                hpPlus = 20 - targetHealth;
            }
            target.setHealth(target.getHealth() + hpPlus);
             broadcastExecuteText(hero, target);
             return true;
         }
         return false;
     }

 }
