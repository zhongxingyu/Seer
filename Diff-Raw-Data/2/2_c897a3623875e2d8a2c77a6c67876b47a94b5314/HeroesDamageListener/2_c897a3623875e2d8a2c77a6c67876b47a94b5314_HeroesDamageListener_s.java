 package com.herocraftonline.dev.heroes.damage;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroAttackDamageCause;
 import com.herocraftonline.dev.heroes.api.HeroDamageCause;
 import com.herocraftonline.dev.heroes.api.HeroSkillDamageCause;
 import com.herocraftonline.dev.heroes.api.SkillDamageEvent;
 import com.herocraftonline.dev.heroes.api.SkillUseInfo;
 import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
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
 
 public class HeroesDamageListener extends EntityListener {
 
     private Heroes plugin;
     private DamageManager damageManager;
 
     private static final Map<Material, Integer> armorPoints;
 
     private boolean ignoreNextDamageEventBecauseBukkitCallsTwoEventsGRRR = false;
     private boolean ignoreNextDamageEventBecauseWolvesAreOnCrack = true;
 
     public HeroesDamageListener(Heroes plugin, DamageManager damageManager) {
         this.plugin = plugin;
         this.damageManager = damageManager;
     }
 
     @Override
     public void onCreatureSpawn(CreatureSpawnEvent event) {
         LivingEntity entity = (LivingEntity) event.getEntity();
         CreatureType type = event.getCreatureType();
         Integer maxHealth = damageManager.getCreatureHealth(type);
         if (maxHealth != null) {
             entity.setHealth(maxHealth);
         }
     }
 
     private void onEntityDamageCore(EntityDamageEvent event) {
         if (event.isCancelled() || plugin.getConfigManager().getProperties().disabledWorlds.contains(event.getEntity().getWorld().getName()))
             return;
 
         if (ignoreNextDamageEventBecauseBukkitCallsTwoEventsGRRR) {
             ignoreNextDamageEventBecauseBukkitCallsTwoEventsGRRR = false;
             plugin.debugLog(Level.SEVERE, "Detected second projectile damage attack on: " + event.getEntity().toString() + " with event type: " + event.getType().toString());
             return;
         }
 
         if (event.getCause() == DamageCause.SUICIDE && event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
             plugin.getHeroManager().getHero(player).setHealth(0D);
             return;
         }
 
         Entity defender = event.getEntity();
         Entity attacker = null;
         HeroDamageCause heroLastDamage = null;
         DamageCause cause = event.getCause();
         int damage = event.getDamage();
 
         if (cause == DamageCause.PROJECTILE)
             ignoreNextDamageEventBecauseBukkitCallsTwoEventsGRRR = true;
 
         if (damageManager.isSpellTarget(defender)) {
             SkillUseInfo skillInfo = damageManager.getSpellTargetInfo(defender);
             damageManager.removeSpellTarget(defender);
             if (event instanceof EntityDamageByEntityEvent) {
                 if (resistanceCheck(defender, skillInfo.getSkill())) {
                     if (defender instanceof Player)
                         skillInfo.getSkill().broadcast(defender.getLocation(), "$1 has resisted $2", ((Player) defender).getDisplayName(), skillInfo.getSkill().getName());
                     if (defender instanceof Creature)
                         skillInfo.getSkill().broadcast(defender.getLocation(), "$1 has resisted $2", Messaging.getCreatureName((Creature) defender), skillInfo.getSkill().getName());
                     event.setCancelled(true);
                     return;
                 }
                 SkillDamageEvent spellDamageEvent = new SkillDamageEvent(damage, defender, skillInfo);
                 plugin.getServer().getPluginManager().callEvent(spellDamageEvent);
                 if (spellDamageEvent.isCancelled()) {
                     event.setCancelled(true);
                     return;
                 }
                 damage = spellDamageEvent.getDamage();
                 if (defender instanceof Player) {
                     heroLastDamage = new HeroSkillDamageCause(damage, cause, skillInfo.getHero().getPlayer(), skillInfo.getSkill());
                 }
             }
         } else if (cause == DamageCause.ENTITY_ATTACK || cause == DamageCause.ENTITY_EXPLOSION || cause == DamageCause.PROJECTILE) {
             if (event instanceof EntityDamageByEntityEvent) {
                 attacker = ((EntityDamageByEntityEvent) event).getDamager();
                 if (attacker instanceof Player) {
                     Player attackingPlayer = (Player) attacker;
                     Hero hero = plugin.getHeroManager().getHero(attackingPlayer);
                     if (!hero.canEquipItem(attackingPlayer.getInventory().getHeldItemSlot()) || hero.hasEffectType(EffectType.STUN)) {
                         event.setCancelled(true);
                         return;
                     }
                     // Get the damage this player should deal for the weapon they are using
                     damage = getPlayerDamage(attackingPlayer, damage);
                 } else if (attacker instanceof LivingEntity) {
                     CreatureType type = Util.getCreatureFromEntity(attacker);
                     if (type != null) {
                         if (type == CreatureType.WOLF) {
                             if (ignoreNextDamageEventBecauseWolvesAreOnCrack) {
                                 ignoreNextDamageEventBecauseWolvesAreOnCrack = false;
                                 return;
                             } else {
                                 ignoreNextDamageEventBecauseWolvesAreOnCrack = true;
                             }
                         }
                         Integer tmpDamage = damageManager.getCreatureDamage(type);
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
                             Integer tmpDamage = damageManager.getCreatureDamage(type);
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
                     return;
                 }
                 damage = weaponDamageEvent.getDamage();
                 heroLastDamage = new HeroAttackDamageCause(damage, cause, attacker);
             }
         } else if (cause != DamageCause.CUSTOM) {
             Double tmpDamage = damageManager.getEnvironmentalDamage(cause);
             boolean skipAdjustment = false;
             if (tmpDamage == null) {
                 tmpDamage = (double) event.getDamage();
                 skipAdjustment = true;
             }
 
             if (!skipAdjustment) {
                 switch (cause) {
                 case FALL:
                     damage = onEntityFall(event.getDamage(), tmpDamage, defender);
                     break;
                 case SUFFOCATION:
                     damage = onEntitySuffocate(tmpDamage, defender);
                     break;
                 case DROWNING:
                     damage = onEntityDrown(tmpDamage, defender);
                     break;
                 case STARVATION:
                     damage = onEntityStarve(tmpDamage, defender);
                     break;
                 case FIRE:
                 case LAVA:
                 case FIRE_TICK:
                     damage = onEntityFlame(tmpDamage, cause, defender);
                     break;
                 default:
                     damage = (int) (double) tmpDamage;
                     break;
                 }
             }
             if (damage == 0) {
                 event.setCancelled(true);
                 return;
             }
             heroLastDamage = new HeroDamageCause(damage, cause);
         } else {
             heroLastDamage = new HeroDamageCause(damage, cause);
         }
 
         if (defender instanceof Player) {
             Player player = (Player) defender;
             if (player.getNoDamageTicks() > 10 || player.isDead() || player.getHealth() <= 0) {
                 event.setCancelled(true);
                 return;
             }
             final Hero hero = plugin.getHeroManager().getHero(player);
             
             //Loop through the player's effects and check to see if we need to remove them
             if (hero.hasEffectType(EffectType.INVULNERABILITY)) {
                 event.setCancelled(true);
                 return;
             }
             for (Effect effect : hero.getEffects()) {
                 if ((effect.isType(EffectType.ROOT) || effect.isType(EffectType.INVIS)) && !effect.isType(EffectType.UNBREAKABLE))  {
                     hero.removeEffect(effect);
                 }
             }
 
             
             // Party damage & PvPable test
             if (attacker instanceof Player) {
                 // If the players aren't within the level range then deny the PvP
                 int aLevel = plugin.getHeroManager().getHero((Player) attacker).getTieredLevel(false);
                 if (Math.abs(aLevel - hero.getTieredLevel(false)) > plugin.getConfigManager().getProperties().pvpLevelRange) {
                     Messaging.send((Player) attacker, "That player is outside of your level range!");
                     event.setCancelled(true);
                     return;
                 }
                 HeroParty party = hero.getParty();
                 if (party != null && party.isNoPvp()) {
                     if (party.isPartyMember((Player) attacker)) {
                         event.setCancelled(true);
                         return;
                     }
                 }
             }
 
             if (damage == 0) {
                 event.setDamage(0);
                 return;
             }
             int damageReduction = calculateArmorReduction(player.getInventory(), damage);
             damage -= damageReduction;
             if (damage < 0) {
                 damage = 0;
             }
 
             hero.setLastDamageCause(heroLastDamage);
 
             double iHeroHP = hero.getHealth();
             double fHeroHP = iHeroHP - damage;
             // Never set HP less than 0
             if (fHeroHP < 0) {
                 fHeroHP = 0;
             }
 
             // Round up to get the number of remaining Hearts
             int fPlayerHP = (int) (fHeroHP / hero.getMaxHealth() * 20);
             if (fPlayerHP == 0 && fHeroHP > 0)
                 fPlayerHP = 1;
             plugin.debugLog(Level.INFO, damage + " damage done to " + player.getName() + " by " + cause + ": " + iHeroHP + " -> " + fHeroHP + "   |   " + player.getHealth() + " -> " + fPlayerHP);
 
             hero.setHealth(fHeroHP);
 
             // If final HP is 0, make sure we kill the player
             if (fHeroHP == 0) {
                 event.setDamage(200);
             } else {
                 player.setHealth(fPlayerHP + damage);
                 event.setDamage(damage + damageReduction);
 
                 // Make sure health syncs on the next tick
                 Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     @Override
                     public void run() {
                         hero.syncHealth();
                     }
                 }, 1);
             }
 
             HeroParty party = hero.getParty();
             if (party != null && event.getDamage() > 0) {
                 party.update();
             }
 
         } else if (defender instanceof LivingEntity) {
             event.setDamage(damage);
         }
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         Heroes.debug.startTask("HeroesDamageListener.onEntityDamage");
         onEntityDamageCore(event);
         Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
     }
 
     @Override
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         Heroes.debug.startTask("HeroesDamageListener.onEntityRegainHealth");
         if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
             Heroes.debug.stopTask("HeroesDamageListener.onEntityRegainHealth");
             return;
         }
 
         double amount = event.getAmount();
         Player player = (Player) event.getEntity();
         Hero hero = plugin.getHeroManager().getHero(player);
         double maxHealth = hero.getMaxHealth();
         
         // Satiated players regenerate % of total HP rather than 1 HP
         if (event.getRegainReason() == RegainReason.SATIATED) {
             double healPercent = plugin.getConfigManager().getProperties().foodHealPercent;
             amount = maxHealth * healPercent;
         }
         
         double newHeroHealth = hero.getHealth() + amount;
         if (newHeroHealth > maxHealth)
             newHeroHealth = maxHealth;
         int newPlayerHealth = (int) (newHeroHealth / maxHealth * 20);
         hero.setHealth(newHeroHealth);
         
         //Sanity test
         int newAmount = newPlayerHealth - player.getHealth();
         if (newAmount < 0)
             newAmount = 0;
         event.setAmount(newAmount);
         Heroes.debug.stopTask("HeroesDamageListener.onEntityRegainHealth");
     }
 
     /**
      * Returns a percentage adjusted damage value for starvation
      * 
      * @param percent
      * @param entity
      * @return
      */
     private int onEntityStarve(double percent, Entity entity) {
         if (entity instanceof Creature) {
             Integer creatureHealth = damageManager.getCreatureHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null)
                 percent *= creatureHealth;
         } else if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             percent *= hero.getMaxHealth();
         }
         return percent < 1 ? 1 : (int) percent;
     }
 
     /**
      * Returns a percentage adjusted damage value for suffocation
      * 
      * @param percent
      * @param entity
      * @return
      */
     private int onEntitySuffocate(double percent, Entity entity) {
         if (entity instanceof Creature) {
             Integer creatureHealth = damageManager.getCreatureHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null)
                 percent *= creatureHealth;
         } else if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             percent *= hero.getMaxHealth();
         }
         return percent < 1 ? 1 : (int) percent;
     }
 
     /**
      * Returns a percentage adjusted damage value for drowning
      * 
      * @param percent
      * @param entity
      * @return
      */
     private int onEntityDrown(double percent, Entity entity) {
         if (entity instanceof Creature) {
             if (plugin.getEffectManager().creatureHasEffectType((Creature) entity, EffectType.WATER_BREATHING))
                 return 0;
             Integer creatureHealth = damageManager.getCreatureHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null)
                 percent *= creatureHealth;
         } else if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             if (hero.hasEffectType(EffectType.WATER_BREATHING))
                 return 0;
             percent *= hero.getMaxHealth();
         }
         return percent < 1 ? 1 : (int) percent;
     }
 
     /**
      * Adjusts damage for Fire damage events.
      * 
      * @param damage
      * @param cause
      * @param entity
      * @return
      */
     private int onEntityFlame(double damage, DamageCause cause, Entity entity) {
         if (damage == 0)
             return 0;
         if (entity instanceof Player) {
             Hero hero = plugin.getHeroManager().getHero((Player) entity);
             if (hero.hasEffectType(EffectType.RESIST_FIRE)) {
                 return 0;
             }
             if (cause != DamageCause.FIRE_TICK)
                 damage *= hero.getMaxHealth();
         } else if (entity instanceof Creature) {
             if (plugin.getEffectManager().creatureHasEffectType((Creature) entity, EffectType.RESIST_FIRE))
                 return 0;
             if (cause != DamageCause.FIRE_TICK) {
                 Integer creatureHealth = damageManager.getCreatureHealth(Util.getCreatureFromEntity(entity));
                 if (creatureHealth != null)
                     damage *= creatureHealth;
             }
         }
         return damage < 1 ? 1 : (int) damage;
     }
 
     /**
      * Adjusts the damage being dealt during a fall
      * 
      * @param damage
      * @param entity
      * @return
      */
     private int onEntityFall(int damage, double damagePercent, Entity entity) {
         if (damage == 0)
             return 0;
         if (entity instanceof Player) {
             Hero dHero = plugin.getHeroManager().getHero((Player) entity);
             if (dHero.hasEffectType(EffectType.SAFEFALL))
                 return 0;
 
             damage = (int) (damage * damagePercent * dHero.getMaxHealth());
         } else if (entity instanceof Creature) {
             if (plugin.getEffectManager().creatureHasEffectType((Creature) entity, EffectType.SAFEFALL)) 
                 return 0;
 
             Integer creatureHealth = damageManager.getCreatureHealth(Util.getCreatureFromEntity(entity));
             if (creatureHealth != null)
                 damage = (int) (damage * damagePercent * creatureHealth);
         }
         return damage < 1 ? 1 : damage;
     }
 
     private int calculateArmorReduction(PlayerInventory inventory, int damage) {
         ItemStack[] armorContents = inventory.getArmorContents();
 
         int missingDurability = 0;
         int maxDurability = 0;
         int baseArmorPoints = 0;
         boolean hasArmor = false;
 
         for (ItemStack armor : armorContents) {
             Material armorType = armor.getType();
             if (armorPoints.containsKey(armorType)) {
                 short armorDurability = armor.getDurability();
                 // Ignore non-durable items
                 if (armorDurability == -1) {
                     continue;
                 }
                 missingDurability += armorDurability;
                 maxDurability += armorType.getMaxDurability();
                 baseArmorPoints += armorPoints.get(armorType);
                 hasArmor = true;
             }
         }
 
         if (!hasArmor)
             return 0;
 
         double armorPoints = (double) baseArmorPoints * (maxDurability - missingDurability) / maxDurability;
         double damageReduction = 0.04 * armorPoints;
         return (int) (damageReduction * damage);
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
             if (hero.hasEffectType(EffectType.RESIST_FIRE) && skill.isType(SkillType.FIRE))
                 return true;
             else if (hero.hasEffectType(EffectType.RESIST_DARK) && skill.isType(SkillType.DARK))
                 return true;
             else if (hero.hasEffectType(EffectType.LIGHT) && skill.isType(SkillType.LIGHT))
                 return true;
             else if (hero.hasEffectType(EffectType.RESIST_LIGHTNING) && skill.isType(SkillType.LIGHTNING))
                 return true;
             else if (hero.hasEffectType(EffectType.RESIST_ICE) && skill.isType(SkillType.ICE))
                 return true;
         } else if (defender instanceof Creature) {
             EffectManager em = plugin.getEffectManager();
             Creature c = (Creature) defender;
             if (em.creatureHasEffectType(c, EffectType.RESIST_FIRE) && skill.isType(SkillType.FIRE))
                 return true;
             else if (em.creatureHasEffectType(c, EffectType.RESIST_DARK) && skill.isType(SkillType.DARK))
                 return true;
             else if (em.creatureHasEffectType(c, EffectType.LIGHT) && skill.isType(SkillType.LIGHT))
                 return true;
             else if (em.creatureHasEffectType(c, EffectType.RESIST_LIGHTNING) && skill.isType(SkillType.LIGHTNING))
                 return true;
             else if (em.creatureHasEffectType(c, EffectType.RESIST_ICE) && skill.isType(SkillType.ICE))
                 return true;
         }
         return false;
     }
 
     static {
         Map<Material, Integer> aMap = new HashMap<Material, Integer>();
         aMap.put(Material.LEATHER_HELMET, 3);
         aMap.put(Material.LEATHER_CHESTPLATE, 8);
         aMap.put(Material.LEATHER_LEGGINGS, 6);
         aMap.put(Material.LEATHER_BOOTS, 3);
 
         aMap.put(Material.GOLD_HELMET, 3);
         aMap.put(Material.GOLD_CHESTPLATE, 8);
         aMap.put(Material.GOLD_LEGGINGS, 6);
         aMap.put(Material.GOLD_BOOTS, 3);
 
         aMap.put(Material.CHAINMAIL_HELMET, 3);
         aMap.put(Material.CHAINMAIL_CHESTPLATE, 8);
         aMap.put(Material.CHAINMAIL_LEGGINGS, 6);
         aMap.put(Material.CHAINMAIL_BOOTS, 3);
 
         aMap.put(Material.IRON_HELMET, 3);
         aMap.put(Material.IRON_CHESTPLATE, 8);
         aMap.put(Material.IRON_LEGGINGS, 6);
         aMap.put(Material.IRON_BOOTS, 3);
 
         aMap.put(Material.DIAMOND_HELMET, 3);
         aMap.put(Material.DIAMOND_CHESTPLATE, 8);
         aMap.put(Material.DIAMOND_LEGGINGS, 6);
         aMap.put(Material.DIAMOND_BOOTS, 3);
         armorPoints = Collections.unmodifiableMap(aMap);
     }
 }
