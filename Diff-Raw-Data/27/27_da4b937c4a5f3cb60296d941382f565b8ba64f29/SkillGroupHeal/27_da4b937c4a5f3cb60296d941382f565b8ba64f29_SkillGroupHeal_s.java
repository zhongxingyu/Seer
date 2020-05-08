 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillGroupHeal extends ActiveSkill {
 
     public SkillGroupHeal(Heroes plugin) {
         super(plugin, "GroupHeal");
         setDescription("Heals all players around you");
         setUsage("/skill groupheal");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill groupheal"});
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("heal-amount", 2);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
        List<Entity> entities = hero.getPlayer().getNearbyEntities(5, 5, 5);
        for (Entity entity : entities) {
            if (entity instanceof Player) {
                Hero target = getPlugin().getHeroManager().getHero((Player) entity);
                int healamount = getSetting(hero.getHeroClass(), "heal-amount", 2);
                target.setHealth(target.getHealth() + healamount);
                target.syncHealth();
             }
         }
         broadcastExecuteText(hero);
         return true;
     }
 }
