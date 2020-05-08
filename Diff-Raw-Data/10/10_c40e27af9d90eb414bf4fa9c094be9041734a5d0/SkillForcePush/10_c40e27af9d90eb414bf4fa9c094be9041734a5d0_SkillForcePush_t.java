 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillForcePush extends TargettedSkill {
 
     public SkillForcePush(Heroes plugin) {
         super(plugin, "Forcepush");
         setDescription("Forces your target backwards");
         setUsage("/skill forcepush <target>");
         setArgumentRange(0, 1);
         setIdentifiers("skill forcepush", "skill fpush");
         setTypes(SkillType.FORCE, SkillType.SILENCABLE, SkillType.DAMAGING, SkillType.HARMFUL);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("vertical-power", 1);
         node.setProperty("horizontal-power", 1);
         node.setProperty(Setting.DAMAGE.node(), 0);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         HeroClass heroClass = hero.getHeroClass();
 
         int damage = getSetting(heroClass, Setting.DAMAGE.node(), 0);
         if (damage > 0) {
             addSpellTarget(target, hero);
             target.damage(damage, player);
         }
         
         Location playerLoc = player.getLocation();
         Location targetLoc = target.getLocation();
         
        double distanceSquared = player.getLocation().distanceSquared(target.getLocation());
        double maxDistance = getSetting(heroClass, Setting.MAX_DISTANCE.node(), 15);
        double distAdjustment = 1.0 - distanceSquared / (maxDistance * maxDistance);
        double xDir = targetLoc.getX() - targetLoc.getX();
         double zDir = targetLoc.getZ() - playerLoc.getZ();
         double magnitude = Math.sqrt(xDir * xDir + zDir * zDir);
         double hPower = getSetting(heroClass, "horizontal-power", 1d) * distAdjustment;
        double vPower = getSetting(heroClass, "vertical-power", 1d) * distAdjustment;
         
         Vector v = new Vector(xDir / magnitude * hPower, vPower, zDir / magnitude * hPower);
         target.setVelocity(v);
 
         broadcastExecuteText(hero, target);
         return true;
     }
 
 }
