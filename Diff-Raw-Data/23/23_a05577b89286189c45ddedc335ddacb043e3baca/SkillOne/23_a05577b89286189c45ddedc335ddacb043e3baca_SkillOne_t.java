 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillOne extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillOne(Heroes plugin) {
         super(plugin, "One");
         setDescription("Provides a short burst of speed");
         setUsage("/skill one");
         setArgumentRange(0, 0);
         setIdentifiers("skill one");
         setTypes(SkillType.BUFF, SkillType.MOVEMENT, SkillType.SILENCABLE);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set("speed-multiplier", 2);
         node.set(Setting.DURATION.node(), 15000);
         node.set("apply-text", "%hero% gained a burst of speed!");
         node.set("expire-text", "%hero% returned to normal speed!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
        applyText = SkillConfigManager.getRaw(this, "apply-text", "%hero% gained a burst of speed!").replace("%hero%", "$1");
        expireText = SkillConfigManager.getRaw(this, "expire-text", "%hero% returned to normal speed!").replace("%hero%", "$1");
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
 
         int duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 15000, false);
         int multiplier = SkillConfigManager.getUseSetting(hero, this, "speed-multiplier", 2, false);
         if (multiplier > 20) {
             multiplier = 20;
         }
         hero.addEffect(new OneEffect(this, duration, multiplier));
 
         return SkillResult.NORMAL;
     }
 
     public class OneEffect extends ExpirableEffect {
 
         public OneEffect(Skill skill, long duration, int amplifier) {
             super(skill, "One", duration);
             this.types.add(EffectType.DISPELLABLE);
             this.types.add(EffectType.BENEFICIAL);
             addMobEffect(1, (int) (duration / 1000) * 20, amplifier, false);
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
