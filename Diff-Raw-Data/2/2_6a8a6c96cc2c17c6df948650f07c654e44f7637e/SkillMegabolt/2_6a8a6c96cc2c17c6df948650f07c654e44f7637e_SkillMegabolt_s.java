 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillMegabolt extends TargettedSkill {
 
     public SkillMegabolt(Heroes plugin) {
         super(plugin, "Megabolt");
         setDescription("Calls down multiple bolts of lightning centered on the target.");
         setUsage("/skill mbolt [target]");
         setArgumentRange(0, 1);
         setIdentifiers(new String[] { "skill megabolt", "skill mbolt" });
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DAMAGE.node(), 4);
         node.setProperty(Setting.RADIUS.node(), 5);
         return node;
     }
     
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         if (target.equals(player)) {
             Messaging.send(player, "Invalid target!");
             return false;
         }
         
         // PvP test
         EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
         getPlugin().getServer().getPluginManager().callEvent(damageEntityEvent);
         if (damageEntityEvent.isCancelled()) {
             Messaging.send(player, "Invalid target!");
             return false;
         }
         int range = getSetting(hero.getHeroClass(), Setting.RADIUS.node(), 5);
         int damage = getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 4);
         
         //Damage the first target
         getPlugin().getDamageManager().addSpellTarget(target, hero, this);
         target.getWorld().strikeLightningEffect(target.getLocation());
         target.damage(damage, player);
         
 
         
         for (Entity entity : target.getNearbyEntities(range, range, range)) {
             if (entity instanceof LivingEntity && !entity.equals(player)) {
                 // PvP test
                 damageEntityEvent = new EntityDamageByEntityEvent(player, entity, DamageCause.CUSTOM, 0);
                 getPlugin().getServer().getPluginManager().callEvent(damageEntityEvent);
                 if (damageEntityEvent.isCancelled()) {
                     continue;
                 }
                 getPlugin().getDamageManager().addSpellTarget(entity, hero, this);
                 entity.getWorld().strikeLightningEffect(entity.getLocation());
                 ((LivingEntity) entity).damage(damage, player);
             }
         }
         
        broadcastExecuteText(hero);
         return true;
     }
 
 }
