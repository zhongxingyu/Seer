 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Dispellable;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillRoot extends TargettedSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillRoot(Heroes plugin) {
         super(plugin, "Root");
         setDescription("Roots your target in place");
         setUsage("/skill root <target>");
         setArgumentRange(0, 1);
         setIdentifiers(new String[]{"skill root"});
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 5000);
         node.setProperty("apply-text", "%target% was rooted!");
         node.setProperty("expire-text", "Root faded from %target%!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, "apply-text", "%target% was rooted!").replace("%target%", "$1");
         expireText = getSetting(null, "expire-text", "Root faded from %target%!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (!(target instanceof Player)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
 
         Player targetPlayer = (Player) target;
         Hero targetHero = getPlugin().getHeroManager().getHero(targetPlayer);
         if (targetHero.equals(hero)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
 
         broadcastExecuteText(hero, target);
 
         long duration = getSetting(hero.getHeroClass(), "duration", 5000);
         targetHero.addEffect(new RootEffect(this, duration));
         return true;
     }
 
     public class RootEffect extends PeriodicEffect implements Dispellable {
 
         private static final long period = 100;
 
         private double x, y, z;
 
         public RootEffect(Skill skill, long duration) {
             super(skill, "Root", period, duration);
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
