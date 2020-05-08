 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillBlaze extends ActiveSkill{
 
     public SkillBlaze(Heroes plugin) {
         super(plugin);
         name = "Blaze";
         description = "Sets everyone around you on fire";
         usage = "/skill blaze";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill blaze");
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("fire-length", 3000);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         List<Entity> entities = hero.getPlayer().getNearbyEntities(5, 5, 5);
         for(Entity n : entities){
            if(n instanceof Player){
                 Player pN = (Player) n;
                 int healamount = getSetting(hero.getHeroClass(), "fire-length", 3000);
                 EntityDamageEvent damageEvent = new EntityDamageEvent(hero.getPlayer(), DamageCause.ENTITY_ATTACK, 0);
                 Bukkit.getServer().getPluginManager().callEvent(damageEvent);
                 if (damageEvent.isCancelled()) {
                     return false;
                 }
                 pN.setFireTicks(healamount);
            }
         }        
         notifyNearbyPlayers(hero.getPlayer().getLocation(), useText, hero.getPlayer().getName(), name);
         return true;
     }
 
 }
