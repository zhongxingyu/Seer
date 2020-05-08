 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.PoisonEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillPoison extends TargettedSkill {
 
     private String applyText;
     private String expireText;
     
     public SkillPoison(Heroes plugin) {
         super(plugin, "Poison");
         setDescription("Poisons your target");
         setUsage("/skill poison <target>");
         setArgumentRange(0, 1);
         setIdentifiers(new String[]{"skill poison"});
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("damage", 1);
         node.setProperty("duration", 10000); //in milliseconds
         node.setProperty("period", 2000); //in milliseconds
         node.setProperty("tick-damage", 1);
         node.setProperty("apply-text", "%target% is poisoned!");
         node.setProperty("expire-text", "%target% has recovered from the poison!");
         return node;
     }
     
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, "apply-text", "%target% is poisoned!").replace("%target%", "$1");
         expireText = getSetting(null, "expire-text", "%target% has recovered from the poison!").replace("%target%", "$1");
     }
     
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         
         Player player = hero.getPlayer();
         if (!(target instanceof Player) || target.equals(player)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
         
         Hero targetHero = getPlugin().getHeroManager().getHero((Player) target);
        //Party check
        if (hero.getParty() != null) {
            if (hero.getParty().isPartyMember(targetHero)) {
                Messaging.send(player, "You need a target!");
                return false;
            }
        }
         broadcastExecuteText(hero, target);
         long duration = getSetting(hero.getHeroClass(), "duration", 10000);
         long period = getSetting(hero.getHeroClass(), "period", 2000);
         int tickDamage = getSetting(hero.getHeroClass(), "tick-damage", 1);
        targetHero.addEffect(new PoisonSkillEffect(this, "Poison", duration, period, tickDamage, player));
         return false;
     }
     
     public class PoisonSkillEffect extends PoisonEffect {
 
         public PoisonSkillEffect(Skill skill, String name, long period, long duration, int tickDamage, Player applier) {
             super(skill, name, period, duration, tickDamage, applier);
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
         
     }
 }
