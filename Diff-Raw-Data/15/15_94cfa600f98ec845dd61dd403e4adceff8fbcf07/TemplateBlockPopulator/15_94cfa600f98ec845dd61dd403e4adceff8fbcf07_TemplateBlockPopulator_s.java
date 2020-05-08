 package com.github.riking.templateworlds.impl.bukkit;
 
 import java.util.Random;
 
 import org.bukkit.Chunk;
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Beacon;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.BrewingStand;
 import org.bukkit.block.Chest;
 import org.bukkit.block.CommandBlock;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Dropper;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Hopper;
 import org.bukkit.block.Jukebox;
 import org.bukkit.block.NoteBlock;
 import org.bukkit.block.Sign;
 import org.bukkit.block.Skull;
 import org.bukkit.entity.Ageable;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Damageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragonPart;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Explosive;
 import org.bukkit.entity.FallingBlock;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Fish;
 import org.bukkit.entity.Hanging;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.IronGolem;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.ItemFrame;
 //import org.bukkit.entity.LeashHitch; // TODO uncomment @ next beta / RB
 import org.bukkit.entity.LightningStrike;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.inventory.EntityEquipment;
 import org.bukkit.inventory.FurnaceInventory;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.potion.PotionEffect;
 
import com.github.riking.templateworlds.plugin.TemplateWorlds;

 public class TemplateBlockPopulator extends BlockPopulator {
     public World templateWorld;
 
     public TemplateBlockPopulator(World world) {
         templateWorld = world;
     }
 
     @Override
     public void populate(World world, Random random, Chunk targetChunk) {
         Chunk sourceChunk = templateWorld.getChunkAt(targetChunk.getX(), targetChunk.getZ());
         ChunkSnapshot sourceSnap = sourceChunk.getChunkSnapshot();
 
         {
             int bx = sourceChunk.getX() << 4;
             int bz = sourceChunk.getZ() << 4;
 
             Block target;
             int px, pz;
             for (int py = 0; py < 256; py++) {
                 for (int x = 0; x < 16; x++) {
                     px = bx + x;
                     for (int z = 0; z < 16; z++) {
                         pz = bz + z;
                         target = targetChunk.getBlock(px, py, pz);
                         target.setTypeIdAndData(sourceSnap.getBlockTypeId(x, py, z), (byte) sourceSnap.getBlockData(x, py, z), false);
                     }
                 }
             }
         }
         {
             Location loc = new Location(world, 0, 0, 0);
             BlockState target;
             for (BlockState source : sourceChunk.getTileEntities()) {
                 source.getLocation(loc);
                 target = targetChunk.getBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getState();
                 if (!source.getClass().equals(target.getClass())) {
                     System.out.println(String.format("[WARNING] TemplateWorlds: BlockState at %d, %d, %d skipped because BlockState classes were different: source %s, target %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), source.getClass().getName(), target.getClass().getName()));
                     continue;
                 }
                 if (source instanceof Beacon) {
                     Beacon so = (Beacon) source;
                     Beacon ta = (Beacon) target;
                     // XXX misses info
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof BrewingStand) {
                     BrewingStand so = (BrewingStand) source;
                     BrewingStand ta = (BrewingStand) target;
                     ta.setBrewingTime(so.getBrewingTime());
                     copyInventory(so.getInventory(), ta.getInventory());
                     ta.getInventory().setIngredient(so.getInventory().getIngredient());
                 } else if (source instanceof Chest) {
                     Chest so = (Chest) source;
                     Chest ta = (Chest) target;
                     // getBlockInventory() ignores neighbors
                     copyInventory(so.getBlockInventory(), ta.getBlockInventory());
                 } else if (source instanceof CommandBlock) {
                     CommandBlock so = (CommandBlock) source;
                     CommandBlock ta = (CommandBlock) target;
                     ta.setCommand(so.getCommand());
                     ta.setName(so.getName());
                 } else if (source instanceof CreatureSpawner) {
                     CreatureSpawner so = (CreatureSpawner) source;
                     CreatureSpawner ta = (CreatureSpawner) target;
                     // XXX misses info
                     ta.setSpawnedType(so.getSpawnedType());
                     ta.setDelay(so.getDelay());
                 } else if (source instanceof Dispenser) {
                     Dispenser so = (Dispenser) source;
                     Dispenser ta = (Dispenser) target;
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof Dropper) {
                     Dropper so = (Dropper) source;
                     Dropper ta = (Dropper) target;
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof Furnace) {
                     Furnace so = (Furnace) source;
                     Furnace ta = (Furnace) target;
                     ta.setBurnTime(so.getBurnTime());
                     ta.setCookTime(so.getCookTime());
                     FurnaceInventory invso = so.getInventory();
                     FurnaceInventory invta = ta.getInventory();
                     invta.setFuel(invso.getFuel());
                     invta.setResult(invso.getResult());
                     invta.setSmelting(invso.getSmelting());
                 } else if (source instanceof Hopper) {
                     Hopper so = (Hopper) source;
                     Hopper ta = (Hopper) target;
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof Jukebox) {
                     Jukebox so = (Jukebox) source;
                     Jukebox ta = (Jukebox) target;
                     ta.setPlaying(so.getPlaying());
                 } else if (source instanceof NoteBlock) {
                     NoteBlock so = (NoteBlock) source;
                     NoteBlock ta = (NoteBlock) target;
                     ta.setRawNote(so.getRawNote());
                 } else if (source instanceof Sign) {
                     Sign so = (Sign) source;
                     Sign ta = (Sign) target;
                     ta.setLine(0, so.getLine(0));
                     ta.setLine(1, so.getLine(1));
                     ta.setLine(2, so.getLine(2));
                     ta.setLine(3, so.getLine(3));
                 } else if (source instanceof Skull) {
                     Skull so = (Skull) source;
                     Skull ta = (Skull) target;
                     ta.setSkullType(so.getSkullType());
                     ta.setRotation(so.getRotation());
                     ta.setOwner(so.getOwner());
                 }
                 target.update();
             }
         }
         {
             Entity[] sourceEntities = sourceChunk.getEntities();
             for (Entity oent : sourceEntities) {
 
                 Entity nent;
                 if (oent instanceof Arrow) {
                     nent = world.spawnArrow(oent.getLocation(), oent.getVelocity(), (float) oent.getVelocity().length(), 0F);
                 } else if (oent instanceof FallingBlock) {
                     FallingBlock o = (FallingBlock) oent;
                     nent = world.spawnFallingBlock(oent.getLocation(), o.getBlockId(), o.getBlockData());
                 } else if (oent instanceof Item) {
                     Item o = (Item) oent;
                     nent = world.dropItem(oent.getLocation(), o.getItemStack());
                     Item n = (Item) nent;
                     n.setPickupDelay(o.getPickupDelay());
                 } else if (oent instanceof LightningStrike) {
                     LightningStrike o = (LightningStrike) oent;
                     if (!o.isEffect()) {
                         nent = world.strikeLightning(o.getLocation());
                     } else {
                         nent = world.strikeLightningEffect(o.getLocation());
                     }
                 } else {
                     try {
                         nent = world.spawnEntity(oent.getLocation(), oent.getType());
                     } catch (IllegalArgumentException e) {
                        TemplateWorlds.getInstance().getLogger().warning("Skipping entity of type " + oent.getType() + ": Cannot instantiate via Bukkit API");
                         continue;
                     }
                 }
 
                 if (nent instanceof Entity) {
                     Entity o = (Entity) oent;
                     Entity n = (Entity) nent;
 
                     n.setFallDistance(o.getFallDistance());
                     n.setFireTicks(o.getFireTicks());
                     n.setLastDamageCause(o.getLastDamageCause());
                     //n.setPassenger(o.getPassenger());
                     n.setTicksLived(o.getTicksLived());
                     n.setVelocity(o.getVelocity());
 
                     // TODO metadata transfer?
                 }
                 if (nent instanceof Ageable) {
                     Ageable o = (Ageable) oent;
                     Ageable n = (Ageable) nent;
                     n.setAge(o.getAge());
                     n.setBreed(o.canBreed());
                     n.setAgeLock(o.getAgeLock());
                     if (o.isAdult()) {
                         n.setAdult();
                     } else {
                         n.setBaby();
                     }
                 }
                 if (nent instanceof Boat) {
                     Boat o = (Boat) oent;
                     Boat n = (Boat) nent;
                     n.setMaxSpeed(o.getMaxSpeed());
                     n.setOccupiedDeceleration(o.getOccupiedDeceleration());
                     n.setUnoccupiedDeceleration(o.getUnoccupiedDeceleration());
                     n.setWorkOnLand(o.getWorkOnLand());
                 }
                 if (nent instanceof Creature) {
                     // NOOP
                     /*
                     Creature o = (Creature) oent;
                     Creature n = (Creature) nent;
 
                     // n.setTarget(o.getTarget());
                     */
                 }
                 if (nent instanceof Creeper) {
                     Creeper o = (Creeper) oent;
                     Creeper n = (Creeper) nent;
 
                     n.setPowered(o.isPowered());
                 }
                 if (nent instanceof Damageable) {
                     Damageable o = (Damageable) oent;
                     Damageable n = (Damageable) nent;
 
                     n.setMaxHealth(o.getMaxHealth());
                     n.setHealth(o.getHealth());
                     // TODO healthScale on beta/RB
                 }
                 if (nent instanceof Enderman) {
                     Enderman o = (Enderman) oent;
                     Enderman n = (Enderman) nent;
 
                     n.setCarriedMaterial(o.getCarriedMaterial());
                 }
                 if (nent instanceof ExperienceOrb) {
                     ExperienceOrb o = (ExperienceOrb) oent;
                     ExperienceOrb n = (ExperienceOrb) nent;
 
                     n.setExperience(o.getExperience());
                 }
                 if (nent instanceof Explosive) {
                     Explosive o = (Explosive) oent;
                     Explosive n = (Explosive) nent;
 
                     n.setYield(o.getYield());
                     n.setIsIncendiary(o.isIncendiary());
                 }
                 if (nent instanceof Fireball) {
                     Fireball o = (Fireball) oent;
                     Fireball n = (Fireball) nent;
 
                     n.setDirection(o.getDirection());
                 }
                 if (nent instanceof Firework) {
                     Firework o = (Firework) oent;
                     Firework n = (Firework) nent;
 
                     n.setFireworkMeta(o.getFireworkMeta());
                 }
                 if (nent instanceof Fish) {
                     Fish o = (Fish) oent;
                     Fish n = (Fish) nent;
 
                     n.setBiteChance(o.getBiteChance());
                 }
                 if (nent instanceof Hanging) {
                     /* NOOP
                     Hanging o = (Hanging) oent;
                     Hanging n = (Hanging) nent;
 
                      */
                 }
                 if (nent instanceof Horse) {
                     Horse o = (Horse) oent;
                     Horse n = (Horse) nent;
 
                     n.setVariant(o.getVariant());
                     n.setColor(o.getColor());
                     n.setStyle(o.getStyle());
                     n.setCarryingChest(o.isCarryingChest());
                     n.setDomestication(o.getDomestication());
                     n.setMaxDomestication(o.getMaxDomestication());
                     n.setJumpStrength(o.getJumpStrength());
                 }
                 if (nent instanceof IronGolem) {
                     IronGolem o = (IronGolem) oent;
                     IronGolem n = (IronGolem) nent;
 
                     n.setPlayerCreated(o.isPlayerCreated());
                 }
                 if (nent instanceof ItemFrame) {
                     ItemFrame o = (ItemFrame) oent;
                     ItemFrame n = (ItemFrame) nent;
 
                     n.setItem(o.getItem());
                     n.setRotation(o.getRotation());
                     n.setFacingDirection(o.getFacing());
                 }
                 //if (nent instanceof LeashHitch) { // TODO uncomment @ next beta / RB
                 //LeashHitch o = (LeashHitch) oent;
                 //LeashHitch n = (LeashHitch) nent;
                 //}
                 if (nent instanceof LivingEntity) {
                     LivingEntity o = (LivingEntity) oent;
                     LivingEntity n = (LivingEntity) nent;
 
                     for (PotionEffect eff : o.getActivePotionEffects()) {
                         n.addPotionEffect(eff, true);
                     }
                     n.setCanPickupItems(o.getCanPickupItems());
                     n.setCustomName(o.getCustomName());
                     n.setCustomNameVisible(o.isCustomNameVisible());
                     if (!(nent instanceof HumanEntity)) {
                         EntityEquipment eo = o.getEquipment();
                         EntityEquipment en = n.getEquipment();
                         en.setArmorContents(eo.getArmorContents());
                         en.setItemInHand(eo.getItemInHand());
                         en.setBootsDropChance(eo.getBootsDropChance());
                         en.setChestplateDropChance(eo.getChestplateDropChance());
                         en.setHelmetDropChance(eo.getHelmetDropChance());
                         en.setItemInHandDropChance(eo.getItemInHandDropChance());
                         en.setLeggingsDropChance(eo.getLeggingsDropChance());
                     }
                     n.setLastDamage(o.getLastDamage());
                     //n.setLeashHolder(o.getLeashHolder());
                     n.setMaximumAir(o.getMaximumAir());
                     n.setMaximumNoDamageTicks(o.getMaximumNoDamageTicks());
                     n.setNoDamageTicks(o.getNoDamageTicks());
                     n.setRemainingAir(o.getRemainingAir());
                     n.setRemoveWhenFarAway(o.getRemoveWhenFarAway());
                 }
                 if (nent instanceof Minecart) {
                     Minecart o = (Minecart) oent;
                     Minecart n = (Minecart) nent;
 
                     n.setDamage(o.getDamage());
                     n.setDerailedVelocityMod(o.getDerailedVelocityMod());
                     n.setFlyingVelocityMod(o.getFlyingVelocityMod());
                     n.setMaxSpeed(o.getMaxSpeed());
                     n.setSlowWhenEmpty(o.isSlowWhenEmpty());
                 }
                 if (nent instanceof Ocelot) {
                     Ocelot o = (Ocelot) oent;
                     Ocelot n = (Ocelot) nent;
 
                     n.setSitting(o.isSitting());
                     n.setCatType(o.getCatType());
                 }
                 if (nent instanceof Painting) {
                     Painting o = (Painting) oent;
                     Painting n = (Painting) nent;
 
                     n.setFacingDirection(o.getFacing(), true);
                     n.setArt(o.getArt(), true);
                 }
                 if (nent instanceof Pig) {
                     Pig o = (Pig) oent;
                     Pig n = (Pig) nent;
 
                     n.setSaddle(o.hasSaddle());
                 }
                 if (nent instanceof PigZombie) {
                     PigZombie o = (PigZombie) oent;
                     PigZombie n = (PigZombie) nent;
 
                     n.setAnger(o.getAnger());
                 }
                 if (nent instanceof Projectile) {
                     Projectile o = (Projectile) oent;
                     Projectile n = (Projectile) nent;
 
                     n.setShooter(o.getShooter());
                     n.setBounce(o.doesBounce());
                 }
                 if (nent instanceof Sheep) {
                     Sheep o = (Sheep) oent;
                     Sheep n = (Sheep) nent;
 
                     n.setColor(o.getColor());
                     n.setSheared(o.isSheared());
                 }
                 if (nent instanceof Skeleton) {
                     Skeleton o = (Skeleton) oent;
                     Skeleton n = (Skeleton) nent;
 
                     n.setSkeletonType(o.getSkeletonType());
                 }
                 if (nent instanceof Slime) {
                     Slime o = (Slime) oent;
                     Slime n = (Slime) nent;
 
                     n.setSize(o.getSize());
                 }
                 if (nent instanceof Tameable) {
                     Tameable o = (Tameable) oent;
                     Tameable n = (Tameable) nent;
 
                     n.setOwner(o.getOwner());
                     //n.setTamed(o.isTamed());
                 }
                 if (nent instanceof ThrownPotion) {
                     ThrownPotion o = (ThrownPotion) oent;
                     ThrownPotion n = (ThrownPotion) nent;
 
                     n.setItem(o.getItem());
                 }
                 if (nent instanceof TNTPrimed) {
                     TNTPrimed o = (TNTPrimed) oent;
                     TNTPrimed n = (TNTPrimed) nent;
 
                     n.setFuseTicks(o.getFuseTicks());
                 }
                 if (nent instanceof Vehicle) {
                     Vehicle o = (Vehicle) oent;
                     Vehicle n = (Vehicle) nent;
 
                     n.setVelocity(o.getVelocity());
                 }
                 if (nent instanceof Villager) {
                     Villager o = (Villager) oent;
                     Villager n = (Villager) nent;
 
                     n.setProfession(o.getProfession());
                 }
                 if (nent instanceof Wolf) {
                     Wolf o = (Wolf) oent;
                     Wolf n = (Wolf) nent;
 
                     n.setCollarColor(o.getCollarColor());
                     n.setAngry(o.isAngry());
                     n.setSitting(o.isSitting());
                 }
                 if (nent instanceof Zombie) {
                     Zombie o = (Zombie) oent;
                     Zombie n = (Zombie) nent;
 
                     n.setBaby(o.isBaby());
                     n.setVillager(o.isVillager());
                 }
             }
         }
 
         // Request cleanup
         sourceChunk = null;
         templateWorld.unloadChunkRequest(targetChunk.getX(), targetChunk.getZ());
     }
 
     public void copyInventory(Inventory source, Inventory dest) {
         dest.setContents(source.getContents());
     }
 }
