 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Dispellable;
 import com.herocraftonline.dev.heroes.effects.Harmful;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillRoot extends TargettedSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillRoot(Heroes plugin) {
         super(plugin, "Root");
         setDescription("Roots your target in place");
         setUsage("/skill root <target>");
         setArgumentRange(0, 1);
         setIdentifiers(new String[] { "skill root" });
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DURATION.node(), 5000);
         node.setProperty(Setting.APPLY_TEXT.node(), "%target% was rooted!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "Root faded from %target%!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% was rooted!").replace("%target%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "Root faded from %target%!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
        if (player.equals(target) || hero.getSummons().contains(target)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
 
         //PvP Check
         if (target instanceof Player) {
             EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
             plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
             if (damageEntityEvent.isCancelled()) {
                 Messaging.send(player, "Invalid target!");
                 return false;
             }
         }
 
         long duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 5000);
         RootEffect rEffect = new RootEffect(this, duration);
         
         if (target instanceof Player) {
             plugin.getHeroManager().getHero((Player) target).addEffect(rEffect);
         } else if (target instanceof Creature) {
             plugin.getHeroManager().addCreatureEffect((Creature) target, rEffect);
         } else {
             Messaging.send(player, "Invalid target!");
             return false;
         }
         
         broadcastExecuteText(hero, target);
         return true;
     }
 
     public class RootEffect extends PeriodicEffect implements Dispellable, Harmful {
 
         private static final long period = 100;
 
         private double x, y, z;
 
         public RootEffect(Skill skill, long duration) {
             super(skill, "Root", period, duration);
         }
 
         @Override
         public void apply(Creature creature) {
             super.apply(creature);
             Location location = creature.getLocation();
             x = location.getX();
             y = location.getY();
             z = location.getZ();
 
             broadcast(location, applyText, Messaging.getCreatureName(creature));
         }
         
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
 
             Location location = hero.getPlayer().getLocation();
             x = location.getX();
             y = location.getY();
             z = location.getZ();
 
             Player player = hero.getPlayer();
             broadcast(location, applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
 
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
         
         @Override
         public void remove(Creature creature) {
             super.remove(creature);
             broadcast(creature.getLocation(), expireText, Messaging.getCreatureName(creature));
         }
 
         @Override
         public void tick(Hero hero) {
             super.tick(hero);
 
             Player player = hero.getPlayer();
             Location location = player.getLocation();
             if (location.getX() != x || location.getY() != y || location.getZ() != z) {
                 location.setX(x);
                 location.setY(y);
                 location.setZ(z);
                 location.setYaw(player.getLocation().getYaw());
                 location.setPitch(player.getLocation().getPitch());
                 player.teleport(location);
             }
         }
     }
 }
