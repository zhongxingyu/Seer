 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.hero.HeroManager;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillChainLightning extends TargettedSkill {
 
     public SkillChainLightning(Heroes plugin) {
         super(plugin, "ChainLightning");
         setDescription("Calls down a bolt of lightning that bounces to other targets");
         setUsage("/skill chainl <target>");
         setArgumentRange(0, 1);
         setIdentifiers("skill chainlightning", "skill clightning", "skill chainl", "skill clight");
         setTypes(SkillType.LIGHTNING, SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.DAMAGING);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DAMAGE.node(), 6);
         node.setProperty("bounce-damage", 3);
         node.setProperty(Setting.RADIUS.node(), 7);
         node.setProperty("max-bounces", 3);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
 
         int damage = getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 6);
 
         // Damage the first target
         addSpellTarget(target, hero);
         target.getWorld().strikeLightningEffect(target.getLocation());
         target.damage(damage, player);
 
         // Try to bounce
         Set<Entity> previousTargets = new HashSet<Entity>();
         previousTargets.add(target);
         int range = getSetting(hero.getHeroClass(), Setting.RADIUS.node(), 7);
         int bounces = getSetting(hero.getHeroClass(), "max-bounces", 3);
         int maxBounce = bounces + 1;
         boolean keepBouncing = true;
         HeroManager heroManager = plugin.getHeroManager();
         while (bounces > 0 && keepBouncing) {
             for (Entity entity : target.getNearbyEntities(range, range, range)) {
                 keepBouncing = false;
                 if (entity instanceof LivingEntity) {
                     // never bounce back to the player
                    if (entity.equals(player)) {
                         continue;
                    }
                     if (!previousTargets.contains(entity) && checkTarget(target, entity)) {
                         if (target instanceof Player) {
                             Hero tHero = heroManager.getHero((Player) target);
                            tHero.addEffect(new DelayedBolt(this, (maxBounce - bounces) * 250, hero, damage));
                         } else if (target instanceof Creature) {
                            plugin.getEffectManager().addCreatureEffect((Creature) target, new DelayedBolt(this, (maxBounce - bounces) * 500, hero, damage));
                         } else {
                             continue;
                         }
                        keepBouncing = true;
                        break;
                     }
                 }
             }
             bounces -= 1;
         }
         broadcastExecuteText(hero, target);
         return true;
     }
 
     private boolean checkTarget(Entity previousTarget, Entity potentialTarget) {
         Vector v1 = previousTarget.getLocation().toVector();
         Vector v2 = potentialTarget.getLocation().toVector();
         Vector directional = v2.clone().subtract(v1);
         try {
             BlockIterator iter = new BlockIterator(previousTarget.getWorld(), v1, directional, 0, (int) v1.distance(v2));
             while (iter.hasNext()) {
                 if (!Util.transparentBlocks.contains(iter.next().getType()))
                     return false;
             }
         } catch (IllegalStateException e) {
             return false;
         }
         return true;
     }
 
     public class DelayedBolt extends ExpirableEffect {
 
         private final Hero applier;
         private final int bounceDamage;
 
         public DelayedBolt(Skill skill, long duration, Hero applier, int bounceDamage) {
             super(skill, "DelayedBolt", duration);
             this.applier = applier;
             this.bounceDamage = bounceDamage;
             this.types.add(EffectType.HARMFUL);
             this.types.add(EffectType.LIGHTNING);
         }
 
         public Hero getApplier() {
             return applier;
         }
 
         @Override
         public void remove(Creature creature) {
             super.remove(creature);
             addSpellTarget(creature, applier);
             creature.damage(bounceDamage, applier.getPlayer());
             creature.getWorld().strikeLightningEffect(creature.getLocation());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player target = hero.getPlayer();
             addSpellTarget(target, applier);
             target.damage(bounceDamage, applier.getPlayer());
             target.getWorld().strikeLightningEffect(target.getLocation());
         }
     }
 }
