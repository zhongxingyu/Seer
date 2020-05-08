 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroRegainHealthEvent;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillBandage extends TargettedSkill {
 
     public SkillBandage(Heroes plugin) {
         super(plugin, "Bandage");
         setDescription("Bandages your target, restoring $1 health.");
         setUsage("/skill bandage <target>");
         setArgumentRange(0, 1);
         setIdentifiers("skill bandage");
         setTypes(SkillType.HEAL, SkillType.PHYSICAL);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection section = super.getDefaultConfig();
         section.set(Setting.HEALTH.node(), 5);
         section.set(Setting.MAX_DISTANCE.node(), 5);
        section.set(Setting.REAGENT.node(), "PAPER");
         section.set(Setting.REAGENT_COST.node(), 1);
         return section;
     }
 
     @Override
     public SkillResult use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (!(target instanceof Player)) {
             return SkillResult.INVALID_TARGET;
         }
 
         Hero targetHero = plugin.getHeroManager().getHero((Player) target);
         int hpPlus = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH, 5, false);
         double targetHealth = targetHero.getHealth();
 
         if (targetHealth >= targetHero.getMaxHealth()) {
             if (player.equals(targetHero.getPlayer())) {
                 Messaging.send(player, "You are already at full health.");
             } else {
                 Messaging.send(player, "Target is already fully healed.");
             }
             return SkillResult.INVALID_TARGET_NO_MSG;
         }
 
         HeroRegainHealthEvent hrhEvent = new HeroRegainHealthEvent(targetHero, hpPlus, this);
         plugin.getServer().getPluginManager().callEvent(hrhEvent);
         if (hrhEvent.isCancelled()) {
             Messaging.send(player, "Unable to heal the target at this time!");
             return SkillResult.CANCELLED;
         }
 
         targetHero.setHealth(targetHealth + hrhEvent.getAmount());
         targetHero.syncHealth();
 
         // Bandage cures Bleeding!
         for (Effect effect : targetHero.getEffects()) {
             if (effect.isType(EffectType.BLEED)) {
                 targetHero.removeEffect(effect);
             }
         }
 
         broadcastExecuteText(hero, target);
         return SkillResult.NORMAL;
     }
 
     @Override
     public String getDescription(Hero hero) {
         double amount = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH, 5, false);
         return getDescription().replace("$1", amount + "");
     }
 }
