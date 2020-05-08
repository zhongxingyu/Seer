 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.common.InvisibleEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillSmoke extends ActiveSkill {
 
     private String applyText;
     private String expireText;
 
     public SkillSmoke(Heroes plugin) {
         super(plugin, "Smoke");
         setDescription("You completely disappear from view");
         setUsage("/skill smoke");
         setArgumentRange(0, 0);
         setIdentifiers("skill smoke");
         setNotes("Note: Taking damage removes the effect");
         setTypes(SkillType.ILLUSION, SkillType.BUFF, SkillType.COUNTER, SkillType.STEALTHY);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.DURATION.node(), 20000);
         node.set(Setting.APPLY_TEXT.node(), "%hero% vanished in a cloud of smoke!");
         node.set(Setting.EXPIRE_TEXT.node(), "%hero% reappeared!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = SkillConfigManager.getRaw(this, Setting.APPLY_TEXT, "%hero% vanished in a cloud of smoke!").replace("%hero%", "$1");
         expireText = SkillConfigManager.getRaw(this, Setting.EXPIRE_TEXT, "%hero% reappeard!").replace("%hero%", "$1");
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
 
         long duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 20000, false);
         Player player = hero.getPlayer();
         player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.SMOKE, 4);
         hero.addEffect(new InvisibleEffect(this, duration, applyText, expireText));
 
         return SkillResult.NORMAL;
     }
 }
