 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Expirable;
 import com.herocraftonline.dev.heroes.effects.Periodic;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillBleed extends TargettedSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillBleed(Heroes plugin) {
         super(plugin);
         setName("Bleed");
         setDescription("Causes your target to bleed");
         setUsage("/skill bleed <target>");
         setMinArgs(0);
         setMaxArgs(1);
         getIdentifiers().add("skill bleed");
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 10000);
         node.setProperty("period", 2000);
         node.setProperty("tick-damage", 1);
         node.setProperty("apply-text", "%target% is bleeding!");
         node.setProperty("expire-text", "%target% has stopped bleeding!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, "apply-text", "%target% is bleeding!").replace("%target%", "$1");
         expireText = getSetting(null, "expire-text", "%target% has stopped bleeding!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (!(target instanceof Player)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
 
         Player targetPlayer = (Player) target;
         Hero targetHero = plugin.getHeroManager().getHero(targetPlayer);
         if (targetHero.equals(hero)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
 
         broadcastExecuteText(hero, target);
 
         long duration = getSetting(hero.getHeroClass(), "duration", 10000);
         long period = getSetting(hero.getHeroClass(), "period", 2000);
         int tickDamage = getSetting(hero.getHeroClass(), "tick-damage", 1);
        targetHero.addEffect(new BleedEffect(this, duration, period, tickDamage));
         return true;
     }
 
     public class BleedEffect extends PeriodicEffect implements Periodic, Expirable {
 
         private final int tickDamage;
 
        public BleedEffect(Skill skill, long duration, long period, int tickDamage) {
             super(skill, "Bleed", period, duration);
             this.tickDamage = tickDamage;
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
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
            player.damage(tickDamage);
         }
     }
 }
