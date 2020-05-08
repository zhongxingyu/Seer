 package com.herocraftonline.dev.heroes;
 
 import java.util.Set;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.effects.CombustEffect;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.SummonEffect;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.hero.HeroManager;
 import com.herocraftonline.dev.heroes.util.Properties;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class HEntityListener extends EntityListener {
 
     private final Heroes plugin;
 
     public HEntityListener(Heroes plugin) {
         this.plugin = plugin;
     }
 
     private Player getAttacker(EntityDamageEvent event) {
         if (event instanceof EntityDamageByEntityEvent) {
             Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
             if (damager instanceof Player) {
                 return (Player) damager;
             } else if (damager instanceof Projectile) {
                 Projectile projectile = (Projectile) damager;
                 if (projectile.getShooter() instanceof Player) {
                     return (Player) projectile.getShooter();
                } else if (projectile.getShooter() instanceof Skeleton && plugin.getHeroManager().creatureHasEffect((Creature) damager, "Summon")) {
                     SummonEffect sEffect = (SummonEffect) plugin.getHeroManager().getCreatureEffect((Skeleton) projectile.getShooter(), "Summon");
                     return sEffect.getSummoner().getPlayer();
                 }
             } else if (damager instanceof Creature) {
                 if (damager instanceof Tameable) {
                     Tameable tamed = (Tameable) damager;
                     if (tamed.isTamed() && tamed.getOwner() instanceof Player)
                         return (Player) tamed.getOwner();
                 }
                 if (plugin.getHeroManager().creatureHasEffect((Creature) damager, "Summon")) {
                     SummonEffect sEffect = (SummonEffect) plugin.getHeroManager().getCreatureEffect((Creature) damager, "Summon");
                     return sEffect.getSummoner().getPlayer();
                 }
             }
         }
         return null;
     }
 
     private void awardKillExp(Hero attacker, Entity defender) {
         Properties prop = plugin.getConfigManager().getProperties();
 
         HeroClass playerClass = attacker.getHeroClass();
         Set<ExperienceType> expSources = playerClass.getExperienceSources();
 
         double addedExp = 0;
         ExperienceType experienceType = null;
 
         // If this entity is on the summon map, don't award XP!
         if (attacker.getSummons().contains(defender))
             return;
 
         if (defender instanceof Player && expSources.contains(ExperienceType.PVP)) {
             // Don't award XP for Players killing themselves
             if (!defender.equals(attacker)) {
                 prop.playerDeaths.put((Player) defender, defender.getLocation());
                 addedExp = prop.playerKillingExp;
                 experienceType = ExperienceType.PVP;
             }
         } else if (defender instanceof LivingEntity && !(defender instanceof Player) && expSources.contains(ExperienceType.KILLING)) {
 
             // Get the dying entity's CreatureType
             CreatureType type = Util.getCreatureFromEntity(defender);
             if (type != null) {
                 // If EXP hasn't been assigned for this Entity then we stop here.
                 if (!prop.creatureKillingExp.containsKey(type))
                     return;
 
                 addedExp = prop.creatureKillingExp.get(type);
                 experienceType = ExperienceType.KILLING;
 
                 // Check if the kill was near a spawner
                 if (prop.noSpawnCamp && Util.isNearSpawner(defender, prop.spawnCampRadius)) 
                     addedExp *= prop.spawnCampExpMult;
             }
         }
 
         if (experienceType != null && addedExp > 0) {
             attacker.gainExp(addedExp, experienceType);
         }
     }
 
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
         Entity defender = event.getEntity();
         Properties prop = plugin.getConfigManager().getProperties();
         //If this is a disabled world ignore it
         if (prop.disabledWorlds.contains(defender.getWorld().getName()))
             return;
 
         Player attacker = getAttacker(defender.getLastDamageCause());
         HeroManager heroManager = plugin.getHeroManager();
 
         if (!prop.orbExp)
             event.setDroppedExp(0);
 
         if (defender instanceof Player) {
             Hero heroDefender = heroManager.getHero((Player) defender);
             double exp = heroDefender.getExperience();
             int level = prop.getLevel(exp);
 
             // check to see if this death was caused by FireTick
             if (attacker == null && heroDefender.hasEffect("Combust")) {
                 attacker = ((CombustEffect) heroDefender.getEffect("Combust")).getApplier();
             }
 
             if (prop.resetOnDeath) {
                 // Wipe xp if we are in hardcore mode
                 heroDefender.gainExp(-heroDefender.getExperience(), ExperienceType.DEATH, false);
                 heroDefender.changeHeroClass(plugin.getClassManager().getDefaultClass());
             } else {
                 // otherwise just do standard loss
                 int currentLevelExp = prop.getExperience(level);
                 int nextLevelExp = prop.getExperience(level + 1);
                 double expLossPercent = prop.expLoss;
                 if (heroDefender.getHeroClass().getExpLoss() != -1) {
                     expLossPercent = heroDefender.getHeroClass().getExpLoss();
                 }
                 double expLoss = (nextLevelExp - currentLevelExp) * expLossPercent;
                 heroDefender.gainExp(-expLoss, ExperienceType.DEATH, false);
             }
 
             // Remove any nonpersistent effects
             for (Effect effect : heroDefender.getEffects()) {
                 if (!effect.isPersistent()) {
                     heroDefender.removeEffect(effect);
                 }
             }
         } else if (defender instanceof Creature) {
             Creature creatureDefender = (Creature) defender;
             heroManager.clearCreatureEffects(creatureDefender);
             if (attacker == null && heroManager.creatureHasEffect(creatureDefender, "Combust")) {
                 attacker = ((CombustEffect) heroManager.getCreatureEffect(creatureDefender, "Combust")).getApplier();
             }
         }
 
         if (attacker != null && !attacker.equals(defender)) {
             Hero hero = heroManager.getHero(attacker);
             awardKillExp(hero, defender);
         }
     }
 }
