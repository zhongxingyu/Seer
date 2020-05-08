 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.SkillResult;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.common.CombustEffect;
 import com.herocraftonline.dev.heroes.effects.common.ImbueEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillFireArrow extends ActiveSkill {
 
     public SkillFireArrow(Heroes plugin) {
         super(plugin, "FireArrow");
         setDescription("Shoots a burning arrow");
         setUsage("/skill firearrow");
         setArgumentRange(0, 0);
         setIdentifiers("skill firearrow", "skill farrow");
         setTypes(SkillType.FIRE, SkillType.BUFF);
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(this), Priority.Normal);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.DURATION.node(), 60000); // milliseconds
         node.set("attacks", 1); // How many attacks the buff lasts for.
         node.set(Setting.DAMAGE.node(), 4);
         node.set("fire-ticks", 100);
         return node;
     }
 
     @Override
     public SkillResult use(Hero hero, String[] args) {
         long duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION, 600000, false);
         int numAttacks = SkillConfigManager.getUseSetting(hero, this, "attacks", 1, false);
         hero.addEffect(new FireArrowBuff(this, duration, numAttacks));
         broadcastExecuteText(hero);
         return SkillResult.NORMAL;
     }
 
     public class FireArrowBuff extends ImbueEffect {
 
         public FireArrowBuff(Skill skill, long duration, int numAttacks) {
             super(skill, "FireArrowBuff", duration, numAttacks);
             this.types.add(EffectType.FIRE);
             setDescription("fire");
         }
     }
 
     public class SkillEntityListener extends EntityListener {
 
         private final Skill skill;
 
         public SkillEntityListener(Skill skill) {
             this.skill = skill;
         }
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             Heroes.debug.startTask("HeroesSkillListener");
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Entity projectile = ((EntityDamageByEntityEvent) event).getDamager();
             if (!(projectile instanceof Arrow) || !(((Projectile) projectile).getShooter() instanceof Player)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Player player = (Player) ((Projectile) projectile).getShooter();
             Hero hero = plugin.getHeroManager().getHero(player);
             if (!hero.hasEffect("FireArrowBuff")) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
             
             LivingEntity entity = (LivingEntity) event.getEntity();
             addSpellTarget(entity, hero);
             if (!damageCheck((Player) player, entity)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 event.setCancelled(true);
                 return;
             }
 
             //Get the duration of the fire damage
             int fireTicks = SkillConfigManager.getUseSetting(hero, skill, "fire-ticks", 100, false);
             //Light the target on fire
             entity.setFireTicks(fireTicks);
             checkBuff(hero);
             //Add our combust effect so we can track fire-tick damage
             if (entity instanceof Player) {
                 Hero targetHero = plugin.getHeroManager().getHero((Player) entity);
                 targetHero.addEffect(new CombustEffect(skill, player));
             } else
                 plugin.getEffectManager().addEntityEffect(entity, new CombustEffect(skill, player));
             
 
             Heroes.debug.stopTask("HeroesSkillListener");
         }
 
         private void checkBuff(Hero hero) {
             FireArrowBuff faBuff = (FireArrowBuff) hero.getEffect("FireArrowBuff");
             if (faBuff.hasNoApplications())
                 hero.removeEffect(faBuff);
         }
     }
 }
