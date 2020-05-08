 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.common.SilenceEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillGarrote extends TargettedSkill {
 
     public SkillGarrote(Heroes plugin) {
         super(plugin, "Garrote");
         setDescription("Damages and silences the target for a short time.");
         setUsage("/skill garrote <target>");
         setArgumentRange(0, 1);
         setIdentifiers("skill garrote");
         setTypes(SkillType.PHYSICAL, SkillType.DEBUFF, SkillType.DAMAGING, SkillType.HARMFUL);
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DAMAGE.node(), 4);
         node.setProperty(Setting.DURATION.node(), 4000);
         node.setProperty(Setting.MAX_DISTANCE.node(), 3);
         return node;
     }
     
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         
         if (player.getItemInHand().getType() != Material.STRING) {
             Messaging.send(player, "You must have a piece of string to use garrote!");
             return false;
         }
         
         if (!hero.hasEffect("Sneak") && !hero.hasEffect("Invisible")) {
             Messaging.send(player, "You must be sneaking or invisible to garrote!");
             return false;
         }
         
         int damage = getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 4);
         target.damage(damage, player);
         if (target instanceof Player) {
             long duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 4000);
             plugin.getHeroManager().getHero((Player) target).addEffect(new SilenceEffect(this, duration));
         }
        broadcastExecuteText(hero);
         return true;
     }
 
 }
