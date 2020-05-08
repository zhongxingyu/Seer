 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillChainLightning extends TargettedSkill {
 
     private static final Set<Material> transparentBlocks;
 
     static {
         transparentBlocks = new HashSet<Material>();
         transparentBlocks.add(Material.AIR);
         transparentBlocks.add(Material.SNOW);
         transparentBlocks.add(Material.REDSTONE_WIRE);
         transparentBlocks.add(Material.TORCH);
         transparentBlocks.add(Material.REDSTONE_TORCH_OFF);
         transparentBlocks.add(Material.REDSTONE_TORCH_ON);
         transparentBlocks.add(Material.RED_ROSE);
         transparentBlocks.add(Material.YELLOW_FLOWER);
         transparentBlocks.add(Material.SAPLING);
         transparentBlocks.add(Material.LADDER);
         transparentBlocks.add(Material.STONE_PLATE);
         transparentBlocks.add(Material.WOOD_PLATE);
         transparentBlocks.add(Material.CROPS);
         transparentBlocks.add(Material.LEVER);
         transparentBlocks.add(Material.WATER);
         transparentBlocks.add(Material.STATIONARY_WATER);
         transparentBlocks.add(Material.LAVA);
         transparentBlocks.add(Material.STATIONARY_LAVA);
         transparentBlocks.add(Material.RAILS);
         transparentBlocks.add(Material.POWERED_RAIL);
         transparentBlocks.add(Material.DETECTOR_RAIL);
         transparentBlocks.add(Material.DIODE_BLOCK_OFF);
         transparentBlocks.add(Material.DIODE_BLOCK_ON);
         transparentBlocks.add(Material.BED_BLOCK);
         transparentBlocks.add(Material.SUGAR_CANE_BLOCK);
         transparentBlocks.add(Material.STEP);
     }
 
     public SkillChainLightning(Heroes plugin) {
         super(plugin, "ChainLightning");
         setDescription("Calls down a bolt of lightning that bounces to other targets");
         setUsage("/skill chainl [target]");
         setArgumentRange(0, 1);
         setIdentifiers(new String[] { "skill chainlightning", "skill clightning", "skill chainl", "skill clight" });
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
 
         if (target.equals(player) || hero.getSummons().contains(target)) {
             Messaging.send(player, "Invalid target!");
             return false;
         }
 
         // PvP test
         if (target instanceof Player) {
             EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, 0);
             plugin.getServer().getPluginManager().callEvent(damageEntityEvent);
             if (damageEntityEvent.isCancelled()) {
                 Messaging.send(player, "Invalid target!");
                 return false;
             }
         }
         int damage = getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 6);
 
         //Damage the first target
         addSpellTarget(target, hero);
         target.getWorld().strikeLightningEffect(target.getLocation());
         target.damage(damage, player);
         
         //Try to bounce
         Set<Entity> previousTargets = new HashSet<Entity>();
         previousTargets.add(target);
         int range = getSetting(hero.getHeroClass(), Setting.RADIUS.node(), 7);
         int bounces = getSetting(hero.getHeroClass(), "max-bounces", 3);
         int maxBounce = bounces + 1;
         boolean keepBouncing = true;
         while (bounces > 0 && keepBouncing) {
             for (Entity entity : target.getNearbyEntities(range, range, range)) {
                 keepBouncing = false;
                 if (entity instanceof LivingEntity) {
                     //never bounce back to the player
                     if (entity.equals(player)) {
                         continue;
                     }
                     if (!previousTargets.contains(entity) && checkTarget(target, entity)) {
                         if (target instanceof Player) {
                             Hero tHero = plugin.getHeroManager().getHero((Player) target);
                             tHero.addEffect(new DelayedBolt(this, (maxBounce - bounces) * 250, hero, damage));
                         } else if (target instanceof Creature) {
                             plugin.getHeroManager().addCreatureEffect((Creature) target, new DelayedBolt(this, (maxBounce - bounces) * 500, hero, damage));
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
        broadcastExecuteText(hero);
         return true;
     }
 
     private boolean checkTarget(Entity previousTarget, Entity potentialTarget) {
         Vector v1 = previousTarget.getLocation().toVector();
         Vector v2 = potentialTarget.getLocation().toVector();
         Vector directional = v2.clone().subtract(v1);
         try {
             BlockIterator iter = new BlockIterator(previousTarget.getWorld(), v1, directional, 0, (int) v1.distance(v2));
             while (iter.hasNext()) {
                 if (!transparentBlocks.contains(iter.next().getType()))
                     return false;
             }
         } catch (IllegalStateException e) {
             return false;
         }
         return true;
     }
 
     public class DelayedBolt extends ExpirableEffect {
 
         private final Hero applier;
         private final int bounceDamage ;
         
         public DelayedBolt(Skill skill, long duration, Hero applier, int bounceDamage) {
             super(skill, "DelayedBolt", duration);
             this.applier = applier;
             this.bounceDamage = bounceDamage;
             this.types.add(EffectType.HARMFUL);
             this.types.add(EffectType.LIGHTNING);
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player target = hero.getPlayer();
             addSpellTarget(target, applier);
             target.damage(bounceDamage, applier.getPlayer());
             target.getWorld().strikeLightningEffect(target.getLocation());
         }  
 
         @Override
         public void remove(Creature creature) {
             super.remove(creature);
             addSpellTarget(creature, applier);
             creature.damage(bounceDamage, applier.getPlayer());
             creature.getWorld().strikeLightningEffect(creature.getLocation());
         }
 
         public Hero getApplier() {
             return applier;
         }
     }
 }
