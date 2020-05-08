 /*
  *  Copyright 2013 Ivan Molodetskikh.
  *  
  *  This file is part of UnexpectedFishing.
  *
  *  UnexpectedFishing is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  UnexpectedFishing is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with UnexpectedFishing.  If not, see <http://www.gnu.org/licenses/>.
  */
 package unexpectedfishing;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.FallingBlock;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Fish;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.util.Vector;
 
 public class UnexpectedFishingListener implements Listener {
     
     private UnexpectedFishingMain mainInstance;
     private Random random = new Random();
     
     private List<UUID> squidEggIds = new ArrayList<UUID>();
     private List<UUID> squidMobIds = new ArrayList<UUID>();
     private List<UUID> tntIds = new ArrayList<UUID>();
     
     public UnexpectedFishingListener(UnexpectedFishingMain instance) {
         mainInstance = instance;
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerFish(PlayerFishEvent event) {
         if (!mainInstance.config.enabled) {
             return;
         }
         
         if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
             event.setCancelled(true); // Disable the default fish drop.
             
             Player player = event.getPlayer();
             Location playerLoc = player.getLocation();
             Fish hook = event.getHook();
             Location hookLoc = hook.getLocation();
             World world = player.getWorld();
             
             if (mainInstance.config.debugMode) {
                 switch (mainInstance.config.actionId) {
                 case 1:
                     throwItemsWithDelay(world, playerLoc, hookLoc, Material.RAW_FISH, 8, 2);
                     throwMob(world, player, hookLoc, EntityType.ZOMBIE);
                     throwMob(world, player, hookLoc, EntityType.ZOMBIE);
                     break;
                     
                 case 2:
                     throwItemsWithDelay(world, playerLoc, hookLoc, Material.RAW_FISH, 2, 5);
                     throwKittyCannon(world, player, hookLoc);
                     break;
                     
                 case 3:
                     throwSquidEgg(world, playerLoc, hookLoc);
                     break;
                     
                 case 4:
                     throwPlayerIntoWater(world, player, hookLoc);
                     break;
                     
                 case 5:
                     throwTNT(world, playerLoc, hookLoc);
                     break;
                     
                 case 6:
                     throwTrashItems(world, playerLoc, hookLoc);
                     break;
                 
                 case 7:
                     throwRandomWater(world, hookLoc);
                     break;
                     
                 case 8:
                     if (mainInstance.anvilEnabled) {
                         throwAnvil(world, playerLoc, hookLoc);
                         break;
                     }
                     // Falls through
                     
                 case 0:
                 default:
                     throwItem(world, playerLoc, hookLoc, Material.RAW_FISH);
                 }
             } else {
                 // We are not in debug mode.
                 
                 if (random.nextInt(100) < mainInstance.config.chance) {
                     // Some special event should happen.
                     
                     int chanceSum = mainInstance.config.chanceZombies +
                                     mainInstance.config.chanceKittyCannon +
                                     mainInstance.config.chanceSquid +
                                     mainInstance.config.chancePlayerthrow +
                                     mainInstance.config.chanceTNT +
                                     mainInstance.config.chanceTrash +
                                     mainInstance.config.chanceGeyser +
                                     mainInstance.config.chanceAnvil;
                     
                     int randomInt = random.nextInt(chanceSum);
                     
                     chanceSum -= mainInstance.config.chanceZombies;
                     if (randomInt >= chanceSum) {
                         throwItemsWithDelay(world, playerLoc, hookLoc, Material.RAW_FISH, 8, 2);
                         throwMob(world, player, hookLoc, EntityType.ZOMBIE);
                         throwMob(world, player, hookLoc, EntityType.ZOMBIE);
                     } else {
                         chanceSum -= mainInstance.config.chanceKittyCannon;
                         if (randomInt >= chanceSum) {
                             throwItemsWithDelay(world, playerLoc, hookLoc, Material.RAW_FISH, 2, 5);
                             throwKittyCannon(world, player, hookLoc);
                         } else {
                             chanceSum -= mainInstance.config.chanceSquid;
                             if (randomInt >= chanceSum) {
                                 throwSquidEgg(world, playerLoc, hookLoc);
                             } else {
                                 chanceSum -= mainInstance.config.chancePlayerthrow;
                                 if (randomInt >= chanceSum) {
                                     throwPlayerIntoWater(world, player, hookLoc);
                                 } else {
                                     chanceSum -= mainInstance.config.chanceTNT;
                                     if (randomInt >= chanceSum) {
                                         throwTNT(world, playerLoc, hookLoc);
                                     } else {
                                         chanceSum -= mainInstance.config.chanceTrash;
                                         if (randomInt >= chanceSum) {
                                             throwTrashItems(world, playerLoc, hookLoc);
                                         } else {
                                             chanceSum -= mainInstance.config.chanceGeyser;
                                             if (randomInt >= chanceSum) {
                                                 throwRandomWater(world, hookLoc);
                                             } else {
                                                 chanceSum -= mainInstance.config.chanceAnvil;
                                                 if (randomInt >= chanceSum) {
                                                     if (mainInstance.anvilEnabled) {
                                                         throwAnvil(world, playerLoc, hookLoc);
                                                     } else {
                                                         // Anvil is disabled, simulate the default Minecraft behavior.
                                                         throwItem(world, playerLoc, hookLoc, Material.RAW_FISH);
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 } else {
                     // Simulate the default Minecraft behavior.
                     throwItem(world, playerLoc, hookLoc, Material.RAW_FISH);
                 }
             }
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onProjectileHit(ProjectileHitEvent event) {
         Projectile proj = event.getEntity();
        if (proj == null)
            return;
        
         UUID projId = proj.getUniqueId();
         if (squidEggIds.contains(projId)) {
             squidEggIds.remove(projId);
             Squid squid = (Squid) proj.getWorld().spawn(proj.getLocation(), EntityType.SQUID.getEntityClass());
             proj.remove();
             
             squidMobIds.add(squid.getUniqueId());
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onEntityDeath(EntityDeathEvent event) {
         Entity ent = event.getEntity();
        if (ent == null)
            return;
        
         UUID entId = ent.getUniqueId();
         if (squidMobIds.contains(entId)) {
             // One of our squids died.
             squidMobIds.remove(entId);
             ent.getWorld().dropItemNaturally(ent.getLocation(), new ItemStack(Material.RAW_FISH));
             ent.getWorld().dropItemNaturally(ent.getLocation(), new ItemStack(Material.RAW_FISH));
             ent.getWorld().dropItemNaturally(ent.getLocation(), new ItemStack(Material.RAW_FISH));
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onEntityExplode(EntityExplodeEvent event) {
         Entity ent = event.getEntity();
        if (ent == null)
            return;
        
         UUID entId = ent.getUniqueId();
         if (tntIds.contains(entId)) {
             tntIds.remove(entId);
             
             event.setCancelled(true);
             World world = ent.getWorld();
             Location tntLoc = ent.getLocation();
             ent.remove();
             world.createExplosion(tntLoc.getX(), tntLoc.getY(), tntLoc.getZ(), 4.0F, false, false);
             world.dropItemNaturally(tntLoc, new ItemStack(Material.COOKED_FISH, 16));
         }
     }
     
     /** Taken from the EntityFishingHook class. */
     private Vector getFishItemVelocity(Location playerLoc, Location hookLoc) {
         double d5 = playerLoc.getX() - hookLoc.getX();
         double d6 = playerLoc.getY() - hookLoc.getY();
         double d7 = playerLoc.getZ() - hookLoc.getZ();
         double d8 = (double) Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
         double d9 = 0.1D;
 
         return new Vector(d5 * d9, d6 * d9 + (double) Math.sqrt(d8) * 0.08D, d7 * d9);
     }
     
     /** Throws the item of the given material as if it was the caught fish. */
     private Item throwItem(World world, Location playerLoc, Location hookLoc, Material itemMaterial) {
         Item item = world.dropItem(hookLoc, new ItemStack(itemMaterial));
         item.setVelocity(getFishItemVelocity(playerLoc, hookLoc));
         
         return item;
     }
     
     /** Throws a bunch of items with a delay. */
     private void throwItemsWithDelay(final World world, final Location playerLoc, final Location hookLoc, final Material itemMaterial, int numItems, long delay) {
         for (int i = 0; i < numItems; i++) {
             mainInstance.scheduleSyncDelayedTask(new Runnable() {
                 @Override
                 public void run() {
                     throwItem(world, playerLoc, hookLoc, itemMaterial);
                 }
             }, delay * i);
         }
     }
     
     /** Throws a mob into the player. */
     private Creature throwMob(World world, Player player, Location hookLoc, EntityType mobType) {
         Creature mob = (Creature) world.spawn(hookLoc, mobType.getEntityClass());
         mob.setVelocity(getFishItemVelocity(player.getLocation(), hookLoc).multiply(1.5));
         mob.setTarget(player);
         mob.setCanPickupItems(false);
         
         return mob;
     }
     
     private Creature throwMob(World world, Player player, Location hookLoc, EntityType mobType, double velocityMultiplier) {
         Creature mob = (Creature) world.spawn(hookLoc, mobType.getEntityClass());
         mob.setVelocity(getFishItemVelocity(player.getLocation(), hookLoc).multiply(velocityMultiplier));
         mob.setTarget(player);
         mob.setCanPickupItems(false);
         
         return mob;
     }
     
     /** Throws a mob after a delay. */
     private void throwMobWithDelay(final World world, final Player player, final Location hookLoc, final EntityType mobType, long delay) {
         mainInstance.scheduleSyncDelayedTask(new Runnable() {
             @Override
             public void run() {
                 throwMob(world, player, hookLoc, mobType);
             }
         }, delay);
     }
     
     private void throwMobWithDelay(final World world, final Player player, final Location hookLoc, final EntityType mobType, final double velocityMultiplier, long delay) {
         mainInstance.scheduleSyncDelayedTask(new Runnable() {
             @Override
             public void run() {
                 throwMob(world, player, hookLoc, mobType, velocityMultiplier);
             }
         }, delay);
     }
     
     /** Throws a projectile into the player. */
     private Projectile throwProjectile(World world, Location playerLoc, Location hookLoc, EntityType projType) {
         Projectile proj = (Projectile) world.spawn(hookLoc, projType.getEntityClass());
         proj.setVelocity(getFishItemVelocity(playerLoc, hookLoc));
         
         return proj;
     }
     
     /** Throws a projectile after a delay. */
     private void throwProjectileWithDelay(final World world, final Location playerLoc, final Location hookLoc, final EntityType projType, long delay) {
         mainInstance.scheduleSyncDelayedTask(new Runnable() {
             @Override
             public void run() {
                 throwProjectile(world, playerLoc, hookLoc, projType);
             }
         }, delay);
     }
     
     private void throwKittyCannon(final World world, Player player, Location hookLoc) {
         int i = random.nextInt(Ocelot.Type.values().length);
         final Ocelot cat = (Ocelot) throwMob(world, player, hookLoc, EntityType.OCELOT);
         cat.setCatType(Ocelot.Type.values()[i]);
         cat.setTamed(true);
         cat.setBaby();
         
         mainInstance.scheduleSyncDelayedTask(new Runnable() {
             @Override
             public void run() {
                 final Location catLoc = cat.getLocation();
                 cat.remove();
                 world.createExplosion(catLoc.getX(), catLoc.getY(), catLoc.getZ(), 2.0F, false, false);
             }
         }, 20);
     }
     
     private void throwSquidEgg(World world, Location playerLoc, Location hookLoc) {
         Projectile egg = throwProjectile(world, playerLoc, hookLoc, EntityType.EGG);
         squidEggIds.add(egg.getUniqueId());
     }
     
     /** Throws the player into the water! */
     private void throwPlayerIntoWater(World world, Player player, Location hookLoc) {
         Location playerLoc = player.getLocation();
         
         double d5 = hookLoc.getX() - playerLoc.getX();
         double d6 = hookLoc.getY() - playerLoc.getY();
         double d7 = hookLoc.getZ() - playerLoc.getZ();
         double d8 = (double) Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
         double d9 = 0.5F;
 
         Vector velocity = new Vector(d5 * d9, d6 * d9 + (double) Math.sqrt(d8) * 0.16D, d7 * d9);
         player.setVelocity(velocity);
         
         Item fish = world.dropItem(hookLoc, new ItemStack(Material.RAW_FISH, 5));
         fish.setVelocity(new Vector(0, 1, 0));
     }
     
     private void throwTNT(World world, Location playerLoc, Location hookLoc) {
         TNTPrimed tnt = (TNTPrimed) world.spawn(hookLoc, EntityType.PRIMED_TNT.getEntityClass());
         tnt.setVelocity(getFishItemVelocity(playerLoc, hookLoc).multiply(1.1));
         tnt.setFuseTicks(20);
         
         tntIds.add(tnt.getUniqueId());
     }
     
     private void throwTrashItems(World world, Location playerLoc, Location hookLoc) {
         throwItemsWithDelay(world, playerLoc, hookLoc, Material.WATER_LILY, 11, 2);
         throwItemsWithDelay(world, playerLoc, hookLoc, Material.DIRT, 19, 2);
         throwItemsWithDelay(world, playerLoc, hookLoc, Material.VINE, 7, 2);
         throwItemsWithDelay(world, playerLoc, hookLoc, Material.WEB, 9, 2);
         throwItemsWithDelay(world, playerLoc, hookLoc, Material.LONG_GRASS, 11, 2);
     }
     
     private void throwRandomWater(final World world, final Location hookLoc) {
         for (int i = 0; i < 100; i++) {
             mainInstance.scheduleSyncDelayedTask(new Runnable() {
                 @Override
                 public void run() {
                     FallingBlock water = world.spawnFallingBlock(hookLoc, Material.WATER, (byte) 0);
                     water.setVelocity(new Vector((random.nextInt(20) - 10) * 0.05, (random.nextInt(20) - 10) * 0.3, (random.nextInt(20) - 10) * 0.05));
                     water.setDropItem(false);
                 }
             }, i);
             
             if (i % 2 == 0) {
                 mainInstance.scheduleSyncDelayedTask(new Runnable() {
                     @Override
                     public void run() {
                         Item fish = world.dropItem(hookLoc, new ItemStack(Material.RAW_FISH, 2));
                         fish.setVelocity(new Vector((random.nextInt(20) - 10) * 0.05, (random.nextInt(20) - 10) * 0.1, (random.nextInt(20) - 10) * 0.05));
                     }
                 }, i + 20);
             }
         }
         
         mainInstance.scheduleSyncDelayedTask(new Runnable() {
             @Override
             public void run() {
                 Firework fw = (Firework) world.spawn(hookLoc, EntityType.FIREWORK.getEntityClass());
                 FireworkEffect fwSFX = FireworkEffect.builder().trail(true).flicker(false).withColor(Color.GREEN).with(Type.CREEPER).build();
                 FireworkMeta fwMeta = fw.getFireworkMeta();
                 fwMeta.clearEffects();
                 fwMeta.addEffect(fwSFX);
                 fw.setFireworkMeta(fwMeta);
             }
         }, 100);
     }
     
     private void throwAnvil(World world, Location playerLoc, Location hookLoc) {
         double d5 = playerLoc.getX() - hookLoc.getX();
         double d6 = playerLoc.getY() - hookLoc.getY();
         double d7 = playerLoc.getZ() - hookLoc.getZ();
         double d8 = (double) Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
         double d9 = 0.03D;
         
         FallingBlock anvil = world.spawnFallingBlock(hookLoc, Material.ANVIL, (byte) 8);
         anvil.setVelocity(new Vector(d5 * d9, d6 * d9 + (double) Math.sqrt(d8) * 0.32D, d7 * d9));
         anvil.setDropItem(true);
 
         enableFallingBlockDamage(anvil);
     }
     
     private void enableFallingBlockDamage(FallingBlock fallingBlock) {
         Object entityFallingBlock = UnexpectedFishingReflection.craftFallingSand.invokeMethod(fallingBlock, "getHandle", new Object[0]);
         
         if (mainInstance.mcpc) {
             UnexpectedFishingReflection.forgeEntityFallingSand.setFieldValue(entityFallingBlock, "isAnvil", true);
         } else {
             UnexpectedFishingReflection.entityFallingBlock.setFieldValue(entityFallingBlock, "hurtEntities", true);
         }
     }
     
 }
