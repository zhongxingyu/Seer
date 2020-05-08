 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillBolt extends TargettedSkill {
 
     private int radius;
 
     public SkillBolt(Heroes plugin) {
         super(plugin);
         name = "Bolt";
         description = "Skill - Bolt";
         usage = "/skill bolt <target>";
         minArgs = 0;
         maxArgs = 1;
         identifiers.add("skill bolt");
     }
 
     @Override
     public void init() {
         super.init();
         maxDistance = config.getInt("max-distance", 20);
         radius = config.getInt("radius", 5);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("max-distance", 20);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         if (target == player) {
             Messaging.send(player, "You need a target.");
             return false;
         }
 
         EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
         plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
         if (damageEntityEvent.isCancelled()) {
             return false;
         }
 
         List<Entity> entityList = target.getNearbyEntities(radius, radius, radius);
         for (Entity n : entityList) {
             if (n instanceof LivingEntity) {
                 if (n != player) {
                    target.getWorld().strikeLightning(n.getLocation());
                 }
             }
         }
         target.getWorld().strikeLightning(target.getLocation());
 
         String targetName = target instanceof Player ? ((Player) target).getName() : target.getClass().getSimpleName().substring(5);
         if (useText != null) {
             notifyNearbyPlayers(player.getLocation().toVector(), useText, player.getName(), name, target == player ? "himself" : targetName);
         }
         return false;
     }
 }
