 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 
 public class SkillExcavate extends ActiveSkill {
     
     private String applyText;
     private String expireText;
     
     public SkillExcavate(Heroes plugin) {
         super(plugin, "Excavate");
         setDescription("Provides a short buff that increases digging speed, and allows instant breaking of dirt");
         setUsage("/skill excavate");
         setArgumentRange(0, 0);
         setIdentifiers("skill excavate");
         setTypes(SkillType.BUFF, SkillType.EARTH, SkillType.SILENCABLE);
     }
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set("speed-multiplier", 2);
         node.set("duration-per-level", 100);
         node.set("apply-text", "%hero% begins excavating!");
         node.set("expire-text", "%hero% is no longer excavating!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = SkillConfigManager.getRaw(this, "apply-text", "%hero% begins excavating!").replace("%hero%", "$1");
         expireText = SkillConfigManager.getRaw(this, "expire-text", "%hero% is no longer excavating!").replace("%hero%", "$1");
     }
     
     @Override
     public SkillResult use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
 
        int duration = SkillConfigManager.getUseSetting(hero, this, "duration-per-level", 100, false) * hero.getSkillLevel(this);
         int multiplier = SkillConfigManager.getUseSetting(hero, this, "speed-multiplier", 2, false);
         if (multiplier > 20) {
             multiplier = 20;
         }
         hero.addEffect(new ExcavateEffect(this, duration, multiplier));
 
         return SkillResult.NORMAL;
     }
     
     public class ExcavateEffect extends ExpirableEffect {
 
         public ExcavateEffect(Skill skill, long duration, int amplifier) {
             super(skill, "Excavate", duration);
             this.types.add(EffectType.DISPELLABLE);
             this.types.add(EffectType.BENEFICIAL);
             addMobEffect(3, (int) (duration / 1000) * 20, amplifier, false);
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
     
     public class SkillBlockListener extends BlockListener {
         
         @Override
         public void onBlockDamage(BlockDamageEvent event) {
             if (event.isCancelled() || !isExcavatable(event.getBlock().getType()))
                 return;
             
             Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
             if (!hero.hasEffect("Excavate"))
                 return;
             
             //Since this block is excavatable, and the hero has the effect - lets instabreak it
             event.setInstaBreak(true);
         }
     }
     
     private boolean isExcavatable(Material m) {
         switch (m) {
         case DIRT:
         case GRASS:
         case GRAVEL:
         case SAND:
         case CLAY:
         case SNOW_BLOCK:
         case SNOW:
         case SOUL_SAND:
         case SOIL:
         case NETHERRACK:
             return true;
         default: 
             return false;
         }
     }
 }
