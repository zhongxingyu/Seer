 package com.herocraftonline.dev.heroes;
 
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
import org.bukkit.event.entity.EntityTargetEvent;
 
 import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectManager;
 import com.herocraftonline.dev.heroes.effects.common.CombustEffect;
 import com.herocraftonline.dev.heroes.effects.common.SummonEffect;
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
                 } else if (projectile.getShooter() instanceof Skeleton && plugin.getEffectManager().entityHasEffect((LivingEntity) projectile.getShooter(), "Summon")) {
                     SummonEffect sEffect = (SummonEffect) plugin.getEffectManager().getEntityEffect((Skeleton) projectile.getShooter(), "Summon");
                     return sEffect.getSummoner().getPlayer();
                 }
             } else if (damager instanceof LivingEntity) {
                 if (damager instanceof Tameable) {
                     Tameable tamed = (Tameable) damager;
                     if (tamed.isTamed() && tamed.getOwner() instanceof Player)
                         return (Player) tamed.getOwner();
                 }
                 if (plugin.getEffectManager().entityHasEffect((LivingEntity) damager, "Summon")) {
                     SummonEffect sEffect = (SummonEffect) plugin.getEffectManager().getEntityEffect((LivingEntity) damager, "Summon");
                     return sEffect.getSummoner().getPlayer();
                 }
             }
         }
         return null;
     }
 
     private void awardKillExp(Hero attacker, Entity defender) {
         Properties prop = Heroes.properties;
 
         double addedExp = 0;
         ExperienceType experienceType = null;
 
         // If this entity is on the summon map, don't award XP!
         if (attacker.getSummons().contains(defender) || attacker.getPlayer().equals(defender))
             return;
 
         if (defender instanceof Player) {
             // Don't award XP for Players killing themselves
             prop.playerDeaths.put((Player) defender, defender.getLocation());
             addedExp = prop.playerKillingExp;
             experienceType = ExperienceType.PVP;
         } else if (defender instanceof LivingEntity && !(defender instanceof Player)) {
 
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
             if (attacker.hasParty())
                 attacker.getParty().gainExp(addedExp, experienceType, defender.getLocation());
             else if (attacker.canGain(experienceType))
                 attacker.gainExp(addedExp, experienceType);
         }
     }
 
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
         Heroes.debug.startTask("HEntityListener.onEntityDeath");
         Entity defender = event.getEntity();
         Properties prop = Heroes.properties;
         //If this is a disabled world ignore it
         if (prop.disabledWorlds.contains(defender.getWorld().getName())) {
             Heroes.debug.stopTask("HEntityListener.onEntityDeath");
             return;
         }
 
         Player attacker = getAttacker(defender.getLastDamageCause());
         HeroManager heroManager = plugin.getHeroManager();
 
         event.setDroppedExp(0);
 
         if (defender instanceof Player) {
             Player player = (Player) defender;
             Hero heroDefender = heroManager.getHero(player);
             Util.deaths.put(player.getName(), event.getEntity().getLocation());
 
             // check to see if this death was caused by FireTick
             if (attacker == null && heroDefender.hasEffect("Combust")) {
                 attacker = ((CombustEffect) heroDefender.getEffect("Combust")).getApplier();
             }
 
             double multiplier = Heroes.properties.expLoss;
             if (attacker != null)
                 multiplier = Heroes.properties.pvpExpLossMultiplier;
 
             heroDefender.loseExpFromDeath(multiplier);
 
             // Remove any nonpersistent effects
             for (Effect effect : heroDefender.getEffects()) {
                 if (!effect.isPersistent()) {
                     heroDefender.removeEffect(effect);
                 }
             }
         } else if (defender instanceof LivingEntity) {
             EffectManager effectManager = plugin.getEffectManager();
             LivingEntity leDefender = (LivingEntity) defender;
             if (attacker == null && effectManager.entityHasEffect(leDefender, "Combust")) {
                 attacker = ((CombustEffect) effectManager.getEntityEffect(leDefender, "Combust")).getApplier();
             }
             effectManager.clearEntityEffects(leDefender);
         }
 
         if (attacker != null && !attacker.equals(defender)) {
             Hero hero = heroManager.getHero(attacker);
             awardKillExp(hero, defender);
         }
 
         Heroes.debug.stopTask("HEntityListener.onEntityDeath");
     }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled() || !(event.getTarget() instanceof Player)) {
            return;
        }

        Hero hero = plugin.getHeroManager().getHero((Player) event.getTarget());
        if (hero.hasEffect("Invisible") || hero.hasEffect("Invuln")) {
            event.setCancelled(true);
        }
    }
 }
