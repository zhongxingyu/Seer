 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Setting;
import com.nijiko.coelho.iConomy.util.Messaging;
 
 public class SkillForcePush extends TargettedSkill {
 
     public SkillForcePush(Heroes plugin) {
         super(plugin, "Forcepush");
         setDescription("Forces your target backwards");
         setUsage("/skill forcepush <target>");
         setArgumentRange(0, 1);
         setIdentifiers(new String[] { "skill forcepush", "skill fpush" });
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("vertical-power", 1);
         node.setProperty("horizontal-power", 1);
         node.setProperty(Setting.DAMAGE.node(), 0);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (target.equals(player) || hero.getSummons().contains(target)) {
             Messaging.send(player, "Invalid target!");
             return false;
         }
         
         //PvP Check
         if (target instanceof Player) {
             EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(player, target, DamageCause.ENTITY_ATTACK, 0);
             Bukkit.getServer().getPluginManager().callEvent(damageEvent);
             if (damageEvent.isCancelled()) {
                 Messaging.send(player, "Invalid target!");
                 return false;
             }
         }
         
         int damage = getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 0);
         if (damage > 0) {
             addSpellTarget(target, hero);
             target.damage(damage, player);
         }
         
         float pitch = player.getEyeLocation().getPitch();
         
         float multiplier = getSetting(hero.getHeroClass(), "horizontal-power", 1) * (90f + pitch) / 40f;
         float vertPower = getSetting(hero.getHeroClass(), "vertical-power", 1);
        Vector v = target.getVelocity().setY(vertPower).add(target.getLocation().getDirection().setY(0).normalize().multiply(multiplier));
         target.setVelocity(v);
 
         broadcastExecuteText(hero, target);
         return true;
     }
 
 }
