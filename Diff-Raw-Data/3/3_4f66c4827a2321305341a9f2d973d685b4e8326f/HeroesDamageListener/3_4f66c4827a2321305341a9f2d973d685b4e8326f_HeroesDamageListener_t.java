 package com.herocraftonline.dev.heroes.damage;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.*;
 import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.EffectManager;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Util;
 
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.MobEffectList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftLivingEntity;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 public class HeroesDamageListener extends EntityListener {
 
     private Heroes plugin;
     private DamageManager damageManager;
 
     private Map<UUID, Integer> healthMap = new HashMap<UUID, Integer>();
 
     public HeroesDamageListener(Heroes plugin, DamageManager damageManager) {
         this.plugin = plugin;
         this.damageManager = damageManager;
     }
 
     @Override
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         Heroes.debug.startTask("HeroesDamageListener.onEntityRegainHealth");
         if (event.isCancelled() || !(event.getEntity() instanceof Player) || Heroes.properties.disabledWorlds.contains(event.getEntity().getWorld().getName())) {
             Heroes.debug.stopTask("HeroesDamageListener.onEntityRegainHealth");
             return;
         }
 
         double amount = event.getAmount();
         Player player = (Player) event.getEntity();
         Hero hero = plugin.getHeroManager().getHero(player);
         double maxHealth = hero.getMaxHealth();
 
         // Satiated players regenerate % of total HP rather than 1 HP
         if (event.getRegainReason() == RegainReason.SATIATED) {
             double healPercent = Heroes.properties.foodHealPercent;
             amount = maxHealth * healPercent;
         } else if (event.getRegainReason() == RegainReason.CUSTOM) {
             double healPercent = amount / 20.0;
             amount = hero.getMaxHealth() * healPercent;
         }
 
         double newHeroHealth = hero.getHealth() + amount;
         if (newHeroHealth > maxHealth) {
             newHeroHealth = maxHealth;
         }
         int newPlayerHealth = (int) (newHeroHealth / maxHealth * 20);
         hero.setHealth(newHeroHealth);
 
         //Sanity test
         int newAmount = newPlayerHealth - player.getHealth();
         if (newAmount < 0) {
             newAmount = 0;
         }
         event.setAmount(newAmount);
         Heroes.debug.stopTask("HeroesDamageListener.onEntityRegainHealth");
     }
 
     private int onEntityDamageCore(EntityDamageEvent event, Entity attacker, int damage) {
         // In case bukkit is firing multiple damage events quickly
         if (event.getDamage() == 0) {
             return 0;
         }
 
         if (attacker instanceof Player) {
             Player attackingPlayer = (Player) attacker;
             Hero hero = plugin.getHeroManager().getHero(attackingPlayer);
             if (!hero.canEquipItem(attackingPlayer.getInventory().getHeldItemSlot()) || hero.hasEffectType(EffectType.STUN)) {
                 event.setCancelled(true);
                 return 0;
             }
             // Get the damage this player should deal for the weapon they are using
             damage = getPlayerDamage(attackingPlayer, damage);
         } else if (attacker instanceof LivingEntity) {
             CreatureType type = Util.getCreatureFromEntity(attacker);
             if (type != null && type != CreatureType.WOLF) {
                 Integer tmpDamage = damageManager.getEntityDamage(type);
                 if (tmpDamage != null) {
                     damage = tmpDamage;
                 }
             }
         } else if (attacker instanceof Projectile) {
             Projectile projectile = (Projectile) attacker;
             if (projectile.getShooter() instanceof Player) {
                 attacker = projectile.getShooter();
                 // Allow alteration of player damage
                 damage = getPlayerProjectileDamage((Player) projectile.getShooter(), projectile, damage);
                 damage = (int) Math.ceil(damage / 3.0 * projectile.getVelocity().length());
             } else {
                 attacker = projectile.getShooter();
                 CreatureType type = Util.getCreatureFromEntity(projectile.getShooter());
                 if (type != null) {
                     Integer tmpDamage = damageManager.getEntityDamage(type);
                     if (tmpDamage != null) {
                         damage = tmpDamage;
                     }
                 }
             }
         }
         // Call the custom event to allow skills to adjust weapon damage
         WeaponDamageEvent weaponDamageEvent = new WeaponDamageEvent(damage, (EntityDamageByEntityEvent) event);
         plugin.getServer().getPluginManager().callEvent(weaponDamageEvent);
         if (weaponDamageEvent.isCancelled()) {
             event.setCancelled(true);
             return 0;
         }
         damage = weaponDamageEvent.getDamage();
         if (event.getEntity() instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) event.getEntity());
             hero.setLastDamageCause(new HeroAttackDamageCause(damage, event.getCause(), attacker));
         }
         return damage;
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         Heroes.debug.startTask("HeroesDamageListener.onEntityDamage");
         // Reasons to immediately ignore damage event
         if (event.isCancelled() || Heroes.properties.disabledWorlds.contains(event.getEntity().getWorld().getName())) {
             Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
             return;
         }
 
         Entity defender = event.getEntity();
         Entity attacker = null;
         HeroDamageCause lastDamage = null;
         int damage = event.getDamage();
 
         //Lets figure out who the attacker is
         if (event instanceof EntityDamageByEntityEvent) {
             attacker = ((EntityDamageByEntityEvent) event).getDamager();
         }
 
         if (defender instanceof LivingEntity) {
             if (defender.isDead() || ((LivingEntity) defender).getHealth() <= 0) {
                 Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                 return;
             } else if (defender instanceof Player) {
                 Player player = (Player) defender;
                 if (player.getGameMode() == GameMode.CREATIVE) {
                     Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                     return;
                 }
                 lastDamage = plugin.getHeroManager().getHero((Player) defender).getLastDamageCause();
             }
         }
 
         if (damageManager.isSpellTarget(defender)) {
             damage = onSpellDamage(event, damage, defender);
         } else {
             DamageCause cause = event.getCause();
             switch (cause) {
             case SUICIDE:
                 if (defender instanceof Player) {
                     Player player = (Player) event.getEntity();
                     plugin.getHeroManager().getHero(player).setHealth(0D);
                     if (player.getLastDamageCause() != null && player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                         Entity tempDamager = ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();
                         player.setLastDamageCause(new EntityDamageByEntityEvent(tempDamager, player, DamageCause.ENTITY_ATTACK, 1000));
                         player.damage(1000, tempDamager);
                         event.setDamage(0);
                     } else {
                         event.setDamage(1000); //OVERKILLLLL!!
                     }
                     Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                     return;
                 }
                 break;
             case ENTITY_ATTACK:
             case ENTITY_EXPLOSION:
             case PROJECTILE:
                 damage = onEntityDamageCore(event, attacker, damage);
                 break;
             case FALL:
                 damage = onEntityFall(event.getDamage(), defender);
                 break;
             case SUFFOCATION:
                 damage = onEntitySuffocate(event.getDamage(), defender);
                 break;
             case DROWNING:
                 damage = onEntityDrown(event.getDamage(), defender, event);
                 break;
             case STARVATION:
                 damage = onEntityStarve(event.getDamage(), defender);
                 break;
             case FIRE:
             case LAVA:
             case FIRE_TICK:
                 damage = onEntityFlame(event.getDamage(), cause, defender, event);
                 break;
             default:
                 break;
             }
 
             //Check if one of the Method calls cancelled the event due to resistances etc.
             if (event.isCancelled()) {
                 Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                 if (defender instanceof Player) {
                     plugin.getHeroManager().getHero((Player) defender).setLastDamageCause(lastDamage);
                 }
                 return;
             }
         }
 
         //TODO: figure out how to fix ender-dragons
         if (defender instanceof EnderDragon || defender instanceof ComplexLivingEntity || defender instanceof ComplexEntityPart) {
             event.setDamage(damage);
             Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
             return;
         }
 
         if (defender instanceof Player) {
             Player player = (Player) defender;
             if ((player.getNoDamageTicks() > 10 && damage > 0) || player.isDead() || player.getHealth() <= 0) {
                 event.setCancelled(true);
                 Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                 return;
             }
             final Hero hero = plugin.getHeroManager().getHero(player);
             //check player inventory to make sure they aren't wearing restricted items
             hero.checkInventory();
 
             //Loop through the player's effects and check to see if we need to remove them
             if (hero.hasEffectType(EffectType.INVULNERABILITY)) {
                 event.setCancelled(true);
                 Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                 return;
             }
             for (Effect effect : hero.getEffects()) {
                 if ((effect.isType(EffectType.ROOT) || effect.isType(EffectType.INVIS)) && !effect.isType(EffectType.UNBREAKABLE)) {
                     hero.removeEffect(effect);
                 }
             }
 
            if (attacker instanceof Projectile) {
                attacker = ((Projectile) attacker).getShooter();
            }
 
             // Party damage & PvPable test
             if (attacker instanceof Player) {
                 // If the players aren't within the level range then deny the PvP
                 int aLevel = plugin.getHeroManager().getHero((Player) attacker).getTieredLevel(false);
                 if (Math.abs(aLevel - hero.getTieredLevel(false)) > Heroes.properties.pvpLevelRange) {
                     Messaging.send((Player) attacker, "That player is outside of your level range!");
                     event.setCancelled(true);
                     Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                     return;
                 }
                 HeroParty party = hero.getParty();
                 if (party != null && party.isNoPvp()) {
                     if (party.isPartyMember((Player) attacker)) {
                         event.setCancelled(true);
                         Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                         return;
                     }
                 }
             }
 
             if (damage == 0) {
                 event.setDamage(0);
                 return;
             }
 
             switch (event.getCause()) {
             case FIRE:
             case LAVA:
             case BLOCK_EXPLOSION:
             case CONTACT:
             case ENTITY_EXPLOSION:
             case ENTITY_ATTACK:
             case PROJECTILE:
                 hero.setHealth(hero.getHealth() - (damage * calculateArmorReduction(player.getInventory())));
                 break;
             default:
                 hero.setHealth(hero.getHealth() - damage);
             }
 
             event.setDamage(convertHeroesDamage(damage, player));
             //If the player would drop to 0 but they still have HP left, don't let them die.
             if (hero.getHealth() != 0 && player.getHealth() == 1 && event.getDamage() == 1) {
                 player.setHealth(2);
             }
             // Make sure health syncs on the next tick
             if (hero.getHealth() == 0) {
                 event.setDamage(200);
             } else {
                 Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     @Override
                     public void run() {
                         if (hero.getPlayer().getHealth() == 0 || hero.getPlayer().isDead()) {
                             return;
                         }
                         hero.syncHealth();
                     }
                 }, 1);
             }
 
             //Update the party display
             HeroParty party = hero.getParty();
             if (party != null && damage > 0) {
                 party.update();
             }
 
         } else if (defender instanceof LivingEntity) {
             if ((((CraftLivingEntity) defender).getNoDamageTicks() > 10 && damage > 0) || defender.isDead() || ((LivingEntity) defender).getHealth() <= 0) {
                 event.setCancelled(true);
                 Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
                 return;
             }
             // Do Damage calculations based on maximum health and current health
             final LivingEntity lEntity = (LivingEntity) defender;
             int maxHealth = getMaxHealth(lEntity);
             Integer currentHealth = healthMap.get(lEntity.getUniqueId());
             if (currentHealth == null) {
                 currentHealth = (int) (lEntity.getHealth() / (double) lEntity.getMaxHealth()) * maxHealth;
             }
 
             // Health-Syncing 
             currentHealth -= damage;
             // If the entity would die from damage, set the damage really high, this should kill any entity in MC outright
             if (currentHealth <= 0) {
                 healthMap.remove(lEntity.getUniqueId());
                 damage = 200;
             } else {
                 // Otherwise lets put the entity back into the health mapping
                 healthMap.put(lEntity.getUniqueId(), currentHealth);
                 damage = convertHeroesDamage(damage, (LivingEntity) defender);
                 int newHealth = lEntity.getHealth() - damage;
                 // If newHealth would go negative (or 0) - this happens with High Heroes HP monsters
                 // We don't want them to die at this point, so we need to either increase their current health
                 // or adjust the damage being dealt.  The first check is to see if we can adjust health up by the 
                 // newHealth amount + 1 - if not, then adjust the damage down.
                 if (newHealth <= 0 && lEntity.getHealth() + 1 - newHealth > lEntity.getMaxHealth()) {
                     damage = damage + newHealth - 1;
                     // if damage would go negative lets check if we can set damage to 1 so that we still get the 'knockback' effect
                     // from the damage system.  Otherwise just 0 the damage and handle it all internally
                     if (damage < 1) {
                         if (lEntity.getHealth() + 1 <= lEntity.getMaxHealth()) {
                             lEntity.setHealth(lEntity.getHealth() + 1);
                             damage = 1;
                         } else {
                             damage = 0;
                         }
                     }
                 } else if (newHealth <= 0) {
                     lEntity.setHealth(lEntity.getHealth() + 1 - newHealth);
                 }
 
                 //Only re-sync if the max health for this is different than the 
                 //if (maxHealth != lEntity.getMaxHealth() && damage > 0) {
                 //     Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EntityHealthSync(lEntity));
                 //}
             }
             event.setDamage(damage);
         }
         Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
     }
 
     private int onSpellDamage(EntityDamageEvent event, int damage, Entity defender) {
         SkillUseInfo skillInfo = damageManager.removeSpellTarget(defender);
         if (event instanceof EntityDamageByEntityEvent) {
             if (resistanceCheck(defender, skillInfo.getSkill())) {
                 skillInfo.getSkill().broadcast(defender.getLocation(), "$1 has resisted $2", Messaging.getLivingEntityName((LivingEntity) defender), skillInfo.getSkill().getName());
                 event.setCancelled(true);
                 return 0;
             }
             SkillDamageEvent spellDamageEvent = new SkillDamageEvent(damage, defender, skillInfo);
             plugin.getServer().getPluginManager().callEvent(spellDamageEvent);
             if (spellDamageEvent.isCancelled()) {
                 event.setCancelled(true);
                 return 0;
             }
             damage = spellDamageEvent.getDamage();
             if (defender instanceof Player) {
                 plugin.getHeroManager().getHero((Player) defender).setLastDamageCause(new HeroSkillDamageCause(damage, event.getCause(), skillInfo.getHero().getPlayer(), skillInfo.getSkill()));
             }
         }
         return damage;
     }
 
     /**
      * Returns a percentage adjusted damage value for starvation
      *
      * @param percent
      * @param entity
      * @return
      */
     private int onEntityStarve(double defaultDamage, Entity entity) {
         Double percent = damageManager.getEnvironmentalDamage(DamageCause.STARVATION);
         if (percent == null) {
             if (entity instanceof Player) {
                 Hero hero = plugin.getHeroManager().getHero((Player) entity);
                 hero.setLastDamageCause(new HeroDamageCause((int) defaultDamage, DamageCause.STARVATION));
             }
             return (int) defaultDamage;
         }
         if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             percent *= hero.getMaxHealth();
             hero.setLastDamageCause(new HeroDamageCause((int) (double) percent, DamageCause.STARVATION));
         } else if (entity instanceof LivingEntity) {
             Integer creatureHealth = damageManager.getEntityMaxHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null) {
                 percent *= creatureHealth;
             }
         }
         return percent < 1 ? 1 : (int) (double) percent;
     }
 
     /**
      * Returns a percentage adjusted damage value for suffocation
      *
      * @param percent
      * @param entity
      * @return
      */
     private int onEntitySuffocate(double defaultDamage, Entity entity) {
         Double percent = damageManager.getEnvironmentalDamage(DamageCause.SUFFOCATION);
         if (percent == null) {
             if (entity instanceof Player) {
                 Hero hero = plugin.getHeroManager().getHero((Player) entity);
                 hero.setLastDamageCause(new HeroDamageCause((int) defaultDamage, DamageCause.SUFFOCATION));
             }
             return (int) defaultDamage;
         }
         if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             percent *= hero.getMaxHealth();
             hero.setLastDamageCause(new HeroDamageCause((int) (double) percent, DamageCause.SUFFOCATION));
         } else if (entity instanceof LivingEntity) {
             Integer creatureHealth = damageManager.getEntityMaxHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null) {
                 percent *= creatureHealth;
             }
         }
         return percent < 1 ? 1 : (int) (double) percent;
     }
 
     /**
      * Returns a percentage adjusted damage value for drowning
      *
      * @param percent
      * @param entity
      * @return
      */
     private int onEntityDrown(double defaultDamage, Entity entity, EntityDamageEvent event) {
         Double percent = damageManager.getEnvironmentalDamage(DamageCause.DROWNING);
         if (percent == null) {
             if (entity instanceof Player) {
                 Hero hero = plugin.getHeroManager().getHero((Player) entity);
                 hero.setLastDamageCause(new HeroDamageCause((int) defaultDamage, DamageCause.DROWNING));
             }
             return (int) defaultDamage;
         }
 
         if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             if (hero.hasEffectType(EffectType.WATER_BREATHING)) {
                 event.setCancelled(true);
                 return 0;
             }
             percent *= hero.getMaxHealth();
             hero.setLastDamageCause(new HeroDamageCause((int) (double) percent, DamageCause.DROWNING));
         } else if (entity instanceof LivingEntity) {
             if (plugin.getEffectManager().entityHasEffectType((LivingEntity) entity, EffectType.WATER_BREATHING)) {
                 event.setCancelled(true);
                 return 0;
             }
             Integer creatureHealth = damageManager.getEntityMaxHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null) {
                 percent *= creatureHealth;
             }
         }
         return percent < 1 ? 1 : (int) (double) percent;
     }
 
     /**
      * Adjusts damage for Fire damage events.
      *
      * @param damage
      * @param cause
      * @param entity
      * @return
      */
     private int onEntityFlame(double defaultDamage, DamageCause cause, Entity entity, EntityDamageEvent event) {
         Double damage = damageManager.getEnvironmentalDamage(cause);
         if (damage == null) {
             if (entity instanceof Player) {
                 Hero hero = plugin.getHeroManager().getHero((Player) entity);
                 hero.setLastDamageCause(new HeroDamageCause((int) defaultDamage, cause));
             }
             return (int) defaultDamage;
         }
 
         if (entity instanceof LivingEntity) {
             EntityLiving el = ((CraftLivingEntity) entity).getHandle();
             if (el.hasEffect(MobEffectList.FIRE_RESISTANCE)) {
                 event.setCancelled(true);
                 return 0;
             }
         }
 
         if (damage == 0) {
             return 0;
         }
         if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             if (hero.hasEffectType(EffectType.RESIST_FIRE)) {
                 event.setCancelled(true);
                 return 0;
             }
             if (cause != DamageCause.FIRE_TICK) {
                 damage *= hero.getMaxHealth();
             }
 
             hero.setLastDamageCause(new HeroDamageCause((int) (double) damage, cause));
         } else if (entity instanceof LivingEntity) {
             if (plugin.getEffectManager().entityHasEffectType((LivingEntity) entity, EffectType.RESIST_FIRE)) {
                 event.setCancelled(true);
                 return 0;
             }
             if (cause != DamageCause.FIRE_TICK) {
                 Integer creatureHealth = damageManager.getEntityMaxHealth(Util.getCreatureFromEntity(entity));
                 if (creatureHealth != null) {
                     damage *= creatureHealth;
                 }
             }
         }
         return damage < 1 ? 1 : (int) (double) damage;
     }
 
     /**
      * Adjusts the damage being dealt during a fall
      *
      * @param damage
      * @param entity
      * @return
      */
     private int onEntityFall(int damage, Entity entity) {
         Double damagePercent = damageManager.getEnvironmentalDamage(DamageCause.FALL);
         if (damagePercent == null) {
             return damage;
         }
 
         if (damage == 0) {
             return 0;
         }
         if (entity instanceof Player) {
             Hero dHero = plugin.getHeroManager().getHero((Player) entity);
             if (dHero.hasEffectType(EffectType.SAFEFALL)) {
                 return 0;
             }
 
             damage = (int) (damage * damagePercent * dHero.getMaxHealth());
         } else if (entity instanceof LivingEntity) {
             if (plugin.getEffectManager().entityHasEffectType((LivingEntity) entity, EffectType.SAFEFALL)) {
                 return 0;
             }
 
             Integer creatureHealth = damageManager.getEntityMaxHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null) {
                 damage = (int) (damage * damagePercent * creatureHealth);
             }
         }
         return damage < 1 ? 1 : damage;
     }
 
     private double calculateArmorReduction(PlayerInventory inventory) {
         double percent = 1;
         int armorPoints = 0;
         for (ItemStack armor : inventory.getArmorContents()) {
             if (armor == null) {
                 continue;
             }
             switch (armor.getType()) {
             case LEATHER_HELMET:
             case LEATHER_BOOTS:
             case GOLD_BOOTS:
             case CHAINMAIL_BOOTS:
                 armorPoints += 1;
                 continue;
             case GOLD_HELMET:
             case IRON_HELMET:
             case CHAINMAIL_HELMET:
             case LEATHER_LEGGINGS:
             case IRON_BOOTS:
                 armorPoints += 2;
                 continue;
             case DIAMOND_HELMET:
             case LEATHER_CHESTPLATE:
             case GOLD_LEGGINGS:
             case DIAMOND_BOOTS:
                 armorPoints += 3;
                 continue;
             case CHAINMAIL_LEGGINGS:
                 armorPoints += 4;
                 continue;
             case GOLD_CHESTPLATE:
             case CHAINMAIL_CHESTPLATE:
             case IRON_LEGGINGS:
                 armorPoints += 5;
                 continue;
             case IRON_CHESTPLATE:
             case DIAMOND_LEGGINGS:
                 armorPoints += 6;
                 continue;
             case DIAMOND_CHESTPLATE:
                 armorPoints += 8;
                 continue;
             }
         }
         percent = (25 - armorPoints) / 25D;
         return percent;
     }
 
     private int getPlayerDamage(Player attacker, int damage) {
         ItemStack weapon = attacker.getItemInHand();
         Material weaponType = weapon.getType();
 
         Integer tmpDamage = damageManager.getItemDamage(weaponType, attacker);
         return tmpDamage == null ? damage : tmpDamage;
     }
 
     private int getPlayerProjectileDamage(Player attacker, Projectile projectile, int damage) {
         Integer tmpDamage = damageManager.getProjectileDamage(ProjectileType.valueOf(projectile), attacker);
         return tmpDamage == null ? damage : tmpDamage;
     }
 
     private boolean resistanceCheck(Entity defender, Skill skill) {
         if (defender instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) defender);
             if (hero.hasEffectType(EffectType.RESIST_FIRE) && skill.isType(SkillType.FIRE)) {
                 return true;
             } else if (hero.hasEffectType(EffectType.RESIST_DARK) && skill.isType(SkillType.DARK)) {
                 return true;
             } else if (hero.hasEffectType(EffectType.RESIST_LIGHT) && skill.isType(SkillType.LIGHT)) {
                 return true;
             } else if (hero.hasEffectType(EffectType.RESIST_LIGHTNING) && skill.isType(SkillType.LIGHTNING)) {
                 return true;
             } else if (hero.hasEffectType(EffectType.RESIST_ICE) && skill.isType(SkillType.ICE)) {
                 return true;
             }
         } else if (defender instanceof LivingEntity) {
             EffectManager em = plugin.getEffectManager();
             LivingEntity c = (LivingEntity) defender;
             if (em.entityHasEffectType(c, EffectType.RESIST_FIRE) && skill.isType(SkillType.FIRE)) {
                 return true;
             } else if (em.entityHasEffectType(c, EffectType.RESIST_DARK) && skill.isType(SkillType.DARK)) {
                 return true;
             } else if (em.entityHasEffectType(c, EffectType.LIGHT) && skill.isType(SkillType.LIGHT)) {
                 return true;
             } else if (em.entityHasEffectType(c, EffectType.RESIST_LIGHTNING) && skill.isType(SkillType.LIGHTNING)) {
                 return true;
             } else if (em.entityHasEffectType(c, EffectType.RESIST_ICE) && skill.isType(SkillType.ICE)) {
                 return true;
             }
         }
         return false;
     }
 
     private int convertHeroesDamage(double d, LivingEntity lEntity) {
         int maxHealth = getMaxHealth(lEntity);
         int damage = (int) ((lEntity.getMaxHealth() / (double) maxHealth) * d);
         if (damage == 0) {
             damage = 1;
         }
         return damage;
     }
 
     public int getMaxHealth(LivingEntity lEntity) {
         if (lEntity instanceof Player) {
             return (int) plugin.getHeroManager().getHero((Player) lEntity).getMaxHealth();
         } else {
             Integer maxHP = plugin.getDamageManager().getEntityMaxHealth(Util.getCreatureFromEntity(lEntity));
             return maxHP != null ? maxHP : lEntity.getMaxHealth();
         }
     }
 
     public int getHealth(LivingEntity lEntity) {
         if (lEntity instanceof Player) {
             return (int) plugin.getHeroManager().getHero((Player) lEntity).getHealth();
         } else {
             Integer hp = healthMap.get(lEntity.getUniqueId());
             return hp != null ? hp : getMaxHealth(lEntity);
         }
     }
 
     /*
      * This is just in case entity HP needs to be re-synched currently we don't need it
      * 
     private class EntityHealthSync implements Runnable {
 
         private final LivingEntity lEntity;
         public EntityHealthSync(LivingEntity lEntity) {
             this.lEntity = lEntity;
         }
 
         @Override
         public void run() {
             if (lEntity == null || lEntity.isDead() || lEntity.getHealth() == 0)
                 return;
             int maxMCHP = lEntity.getMaxHealth();
             Integer currentHealth = healthMap.get(lEntity.getEntityId());
             if (currentHealth == null)
                 return;
             double percent =  currentHealth / (double) getMaxHealth(lEntity);
             int newHP = (int) (maxMCHP * percent);
             if (newHP == 0)
                 newHP = 1;
             lEntity.setHealth(newHP);
         }
     }
      */
 }
