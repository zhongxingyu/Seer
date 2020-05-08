 package com.minecarts.easterbunny;
 
 import java.util.logging.Level;
 import java.text.MessageFormat;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Date;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import org.bukkit.block.Block;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Item;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import org.bukkit.inventory.ItemStack;
 
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 
 
 public class Plugin extends JavaPlugin implements Listener {
     protected static final int EGG_FREQUENCY = 60 * 5; // in seconds
     protected static final PotionEffect JUMP = new PotionEffect(PotionEffectType.JUMP, 20 * 15, 5);
     protected static final int LONG_GRASS = Material.LONG_GRASS.getId();
     
     protected final Random random = new Random();
     protected Date lastEgg = new Date();
     protected List<Player> pastPlayers = new ArrayList<Player>();
     
     protected final ItemStack[] eggChoices = new ItemStack[]{
         // common choices
         new ItemStack(Material.EGG, 1),
         new ItemStack(Material.EGG, 2),
         new ItemStack(Material.EGG, 3),
         new ItemStack(Material.EGG, 4),
         new ItemStack(Material.EGG, 5),
         new ItemStack(Material.EGG, 6),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.PIG.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.PIG.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.PIG.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.CHICKEN.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.CHICKEN.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.CHICKEN.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.COW.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.COW.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.COW.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SHEEP.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SHEEP.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SHEEP.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SQUID.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SQUID.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SQUID.getTypeId()),
         // rare choices
         new ItemStack(Material.DIAMOND, 1),
         new ItemStack(Material.DIAMOND, 2),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.VILLAGER.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.CREEPER.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SKELETON.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SPIDER.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.ZOMBIE.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.SLIME.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.PIG_ZOMBIE.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.ENDERMAN.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.CAVE_SPIDER.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.BLAZE.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.MAGMA_CUBE.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.WOLF.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.MUSHROOM_COW.getTypeId()),
         new ItemStack(Material.MONSTER_EGG, 1, EntityType.OCELOT.getTypeId())
     };
     
     @Override
     public void onEnable() {
         Bukkit.getPluginManager().registerEvents(this, this);
         
         Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 if(new Date().getTime() - lastEgg.getTime() < EGG_FREQUENCY * 1000) return;
                 
                 List<Player> players = new ArrayList<Player>();
                 List<Item> items = new ArrayList<Item>();
                 for(World world : Bukkit.getWorlds()) {
                     switch(world.getEnvironment()) {
                         // tall grass check below only applies to normal worlds
                         case NORMAL:
                             // stash all players in this world for random selection later
                             players.addAll(world.getPlayers());
                             // stash all egg-like items for throttling spawns
                             for(Item item : world.getEntitiesByClass(Item.class)) {
                                 switch(item.getItemStack().getType()) {
                                     case MONSTER_EGG:
                                     case DIAMOND:
                                         items.add(item);
                                         break;
                                 }
                             }
                             break;
                     }
                 }
                 
                 // remove past players to give others a chance
                 players.removeAll(pastPlayers);
                 
                 if(players.isEmpty()) {
                     debug("SKIP! No new players in normal world(s)");
                     pastPlayers.clear();
                     return;
                 }
                 if(items.size() > 20) {
                     debug("SKIP! Too many items already spawned");
                     return;
                 }
                 
                 // select a random player
                 Player player = players.get(random.nextInt(players.size()));
                 debug("Player {0} selected", player);
                pastPlayers.add(player);
                 
                 World world = player.getWorld();
                 Location loc = player.getLocation();
                 int x = loc.getBlockX();
                 int z = loc.getBlockZ();
                 
                 // get all nearby long grass blocks
                 List<Block> blocks = new ArrayList<Block>();
                 for(int dx = -15; dx <= 15; dx++) { // search within 15 x blocks of player
                     if(dx >= -5 && dx <= 5) continue; // skip if within 5 x blocks from player
                     
                     for(int dz = -15; dz <= 15; dz++) { // search within 15 z blocks of player
                         if(dz >= -5 && dz <= 5) continue; // skip if within 5 z blocks from player
                         
                         for(int y = 63; y <= 127; y++) {
                             if(world.getBlockTypeIdAt(x + dx, y, z + dz) == LONG_GRASS) {
                                 blocks.add(world.getBlockAt(x + dx, y, z + dz));
                             }
                         }
                     }
                 }
                 if(blocks.isEmpty()) {
                     debug("SKIP! No nearby long grass blocks found for player");
                     return;
                 }
                 
                 // select a random long grass block
                 Block block = blocks.get(random.nextInt(blocks.size()));
                 debug("Block {0} selected at {1}", block, block.getLocation());
                 
                 // drop a random egg-like item within the long grass
                 Item item = block.getWorld().dropItem(block.getLocation(), eggChoices[random.nextInt(eggChoices.length)]);
                 debug("Spawned item {0} at {1}", item, item.getLocation());
                 
                 // notify server
                 Bukkit.broadcastMessage(String.format("%sA colorful egg was hidden in the tall grass near %s%s.", ChatColor.GRAY, player.getDisplayName(), ChatColor.GRAY));
                 
                 // update last egg spawn date
                 lastEgg = new Date();
             }
         }, 20 * 60, 20 * 60);
     }
     
     
     // allow players to ride chickens
     @EventHandler
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Player player = event.getPlayer();
         Entity entity = event.getRightClicked();
         
         if(entity instanceof Chicken) {
             entity.setPassenger(player);
             debug("Player {0} mounted chicken {1} at {2}", player, entity, entity.getLocation());
         }
     }
     
     // apply jump effect when hit by an egg
     @EventHandler
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
         if(!(event.getDamager() instanceof Egg)) return;
         if(!(event.getEntity() instanceof LivingEntity)) return;
         
         LivingEntity entity = (LivingEntity) event.getEntity();
         if(!entity.hasPotionEffect(PotionEffectType.JUMP) && entity.addPotionEffect(JUMP)) {
             if(entity instanceof Player) {
                 ((Player) entity).sendMessage(String.format("%sYou feel very...bouncy.", ChatColor.GRAY));
             }
             debug("Applied jump effect to entity {0}", entity);
         }
     }
     
     // cancel fall damage if jump effect is active
     @EventHandler(ignoreCancelled = true)
     public void onEntityDamage(EntityDamageEvent event) {
         if(!(event.getEntity() instanceof LivingEntity)) return;
         
         LivingEntity entity = (LivingEntity) event.getEntity();
         switch(event.getCause()) {
             case FALL:
                 if(entity.hasPotionEffect(PotionEffectType.JUMP)) {
                     event.setCancelled(true);
                     debug("Cancelling fall damage for entity {0}", entity);
                 }
                 break;
         }
     }
     
     
     
     
     public void log(String message) {
         log(Level.INFO, message);
     }
     public void log(Level level, String message) {
         getLogger().log(level, message);
     }
     public void log(String message, Object... args) {
         log(MessageFormat.format(message, args));
     }
     public void log(Level level, String message, Object... args) {
         log(level, MessageFormat.format(message, args));
     }
     
     public void debug(String message) {
         log(Level.FINE, message);
     }
     public void debug(String message, Object... args) {
         debug(MessageFormat.format(message, args));
     }
 }
