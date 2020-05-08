 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Expirable;
 import com.herocraftonline.dev.heroes.effects.Periodic;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillIcyAura extends ActiveSkill{
 
     private String applyText;
     private String expireText;
     
    public SkillIcyAura(Heroes plugin, String name) {
         super(plugin, "IcyAura");
         setDescription("Triggers an icyaura around you.");
         setUsage("/skill icyaura");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill icyaura"});
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
         applyText = getSetting(null, "apply-text", "%target% is emitting ice!").replace("%target%", "$1");
         expireText = getSetting(null, "expire-text", "%target% has emitting ice!").replace("%target%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
         
         long duration = getSetting(hero.getHeroClass(), "duration", 10000);
         long period = getSetting(hero.getHeroClass(), "period", 2000);
         int tickDamage = getSetting(hero.getHeroClass(), "tick-damage", 1);
         hero.addEffect(new IcyAuraEffect(this, duration, period, tickDamage, hero.getPlayer()));
         return true;
     }
     
     public class IcyAuraEffect extends PeriodicEffect implements Periodic, Expirable {
 
         private final Player applier;
         private final int tickDamage;
 
         public IcyAuraEffect(SkillIcyAura skill, long duration, long period, int tickDamage, Player applier) {
             super(skill, "Bleed", period, duration);
             this.tickDamage = tickDamage;
             this.applier = applier;
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
             Location loc = player.getLocation();
             loc.setY(loc.getY() - 1);
             if(player.getWorld().getBlockAt(loc).getType() == Material.ICE) {
                 player.getWorld().getBlockAt(loc).setType(Material.ICE);
             }
         }
     }
 
 }
