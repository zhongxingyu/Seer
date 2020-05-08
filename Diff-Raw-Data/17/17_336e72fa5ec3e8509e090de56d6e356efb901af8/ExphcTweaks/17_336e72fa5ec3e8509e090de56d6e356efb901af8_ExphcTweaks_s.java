 package me.exphc.ExphcTweaks;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.UUID;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Formatter;
 import java.lang.Byte;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.Material.*;
 import org.bukkit.material.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.command.*;
 import org.bukkit.inventory.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.scheduler.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.*;
 
 import net.minecraft.server.CraftingManager;
 
 import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.craftbukkit.entity.CraftAnimals;
 import org.bukkit.craftbukkit.entity.CraftLivingEntity;
 
 public class ExphcTweaks extends JavaPlugin implements Listener {
     Logger log = Logger.getLogger("Minecraft");
 
     final public static int JFM_GUTTERING_STRAIGHT = 6284;
 
     public void onEnable() {
         Bukkit.getServer().getPluginManager().registerEvents(this, this);
 
        // Recipe for Jammy Furniture Mod gutter to not conflict wiht WRCBE stone bowl
         // https://github.com/mushroomhostage/exphc/issues/54
         ShapedRecipe gutter = new ShapedRecipe(new ItemStack(JFM_GUTTERING_STRAIGHT, 6));
         gutter.shape("   ", "S S", "SSS");
         gutter.setIngredient('S', Material.STONE);
         Bukkit.addRecipe(gutter);
     }
 
     public void onDisable() {
     }
 
 
     /* this doesn't handle outdated server kick message - instead patch nms NetLoginHandler
     @EventHandler(priority=EventPriority.MONITOR) 
     public void onPlayerPreLogin(PlayerPreLoginEvent event) {
         log.info("pre login: " + event + " msg="+event.getKickMessage());
     }
     */
 
     @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             Block block = event.getClickedBlock();
             ItemStack item = event.getItem();
 
             // https://github.com/mushroomhostage/exphc/issues/24 AnimalBikes should be one-time use
             if (block != null && item != null && item.getTypeId() == 567) { //  AnimalBikes
                 Player player = event.getPlayer();
 
                 log.info("[ExphcTweaks] [AB] Player "+player.getName()+" using AnimalBike item "+item.getDurability());
 
                 boolean nearExistingBike = false;
                 List<Entity> entities = player.getNearbyEntities(10, 10, 10);
                 for (Entity entity: entities) {
                     //log.info("near entity " + entity.getEntityId() + " of type " + entity.getType() + " = " + entity.getType().getTypeId());
                     // Bukkit's EntityType only gives us 'UNKNOWN' for modded entities, heh, thanks
                     if (entity.getType() == EntityType.UNKNOWN) {
                         nearExistingBike = true;
                         break;
                     }
                 }
 
                 // If we think the player is _placing_ a bike, use it up, so it isn't infinite.
                 // This heuristic is not perfect and should really be changed in the mod itself (there may
                 // be other bikes placed by other players nearby).
                 if (!nearExistingBike) {
                     log.info("[ExphcTweaks] [AB] Player "+player.getName()+" apparently placed AnimalBike "+item.getDurability()+", clearing hand");
                     player.setItemInHand(null);
                 } else {
                     log.info("[ExphcTweaks] [AB] Player "+player.getName()+" apparently did not place AnimalBike "+item.getDurability()+", not clearing hand");
                 }
             }
         }
     }
 
 
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("tell")) {
             sender.sendMessage("To privately communicate with a player, try talking into a smartphone (clock)");
             return true;
         } 
         if (cmd.getName().equalsIgnoreCase("give") ||
             cmd.getName().equalsIgnoreCase("tp")) {
             sender.sendMessage("-");
             return true;
         }
 
         // Break balloon into item to fix glitches
         if (cmd.getName().equalsIgnoreCase("breakballoon")) {
             if (!(sender instanceof Player)) {
                 // need a player to locate
                 return false;
             }
 
             Player player = (Player)sender;
 
             double r = 5.0;
             List<Entity> entities = player.getNearbyEntities(r, r, r);
             for (Entity entity: entities) {
                 net.minecraft.server.Entity e = ((CraftEntity)entity).getHandle();
 
                 //final int HOT_AIR_BALLOON_ENTITY_ID = 38;
                 //final int HOT_AIR_BALLOON_PROP_ENTITY_ID = 39;
 
                 final int HOT_AIR_BALLOON_ITEM_ID = 3256;
                 final int HOT_AIR_BALLOON_PROPELLED_ITEM_ID = 3257;
 
                 Location location = entity.getLocation();
                 World world = location.getWorld();
 
                 if (e.toString().indexOf("HAB_EntityHotAirBalloonPropelled") != -1) { 
                     player.sendMessage("Breaking propelled hot air balloon");
                     ItemStack drop = new ItemStack(HOT_AIR_BALLOON_PROPELLED_ITEM_ID, 1);
 
                     world.dropItemNaturally(location, drop);
 
                     entity.remove();
                 } else if (e.toString().indexOf("HAB_EntityHotAirBalloon") != -1) {
                     player.sendMessage("Breaking hot air balloon");
 
                     ItemStack drop = new ItemStack(HOT_AIR_BALLOON_ITEM_ID, 1);
 
                     world.dropItemNaturally(location, drop);
 
                     entity.remove();
 
                 }
             }
 
             return true;
         }
 
         // Clear entities
         if (cmd.getName().equalsIgnoreCase("clear")) {
             if (sender instanceof Player && !((Player)sender).isOp()) {
                 return false;
             }
 
             // Clear all item entities
             if (args.length > 0 && args[0].equals("all")) {
                 for (World world: Bukkit.getWorlds()) {
                     List<Entity> all = world.getEntities();
 
                     //sender.sendMessage("World "+world.getName()+" total entities: " + all.size());
                     int items = 0;
                     for (Entity entity: all) {
                         if (entity instanceof Item) { // || entity instanceof CraftLivingEntity || entity instanceof CraftAnimals) {
                             entity.remove();
                             items += 1;
                         }
                         log.info("entity="+entity);
                     }
                     sender.sendMessage("Cleared "+items+" items out of "+all.size()+" entities in "+world.getName());
                 }
                 return true;
             }
 
 
             // Clear item entities near player
             Player player;
             if (args.length < 1) {
                 if (sender instanceof Player) {
                     player = (Player)sender;
                 } else {
                     sender.sendMessage("Must specify player name when using /clear from console");
                     return false;
                 }
             } else {
                 player = Bukkit.getPlayer(args[0]);
             }
             double r = 200.0;
             List<Entity> entities = player.getNearbyEntities(r, r, r);
             int items = 0;
             for (Entity entity: entities) {
                 if (entity instanceof Item) {
                     entity.remove();
                     items += 1;
                 }
             }
             sender.sendMessage("Removed "+items+" items out of "+entities.size()+" entities");
             return true;
         }
 
         // Load a chunk by coordinates
         if (cmd.getName().equalsIgnoreCase("loadchunk")) {
             if (sender instanceof Player && !((Player)sender).isOp()) {
                 return false;
             }
 
             if (args.length < 3) {
                 sender.sendMessage("Must specify world and x and z");
                 return false;
             }
             World world = Bukkit.getWorld(args[0]);
             int x = Integer.parseInt(args[1]);
             int z = Integer.parseInt(args[2]);
 
             sender.sendMessage("Loading "+world.getName()+" x="+x+", z="+z);
 
             world.loadChunk(x, z);
             return true;
         }
 
         if (!(sender instanceof Player)) {
             sender.sendMessage("Must be sent by player");
             return true;
         }
         Player player = (Player)sender;
 
         // Resend chunk to player
         if (cmd.getName().equalsIgnoreCase("chunk")) {
             // See Bananachunk http://forums.bukkit.org/threads/fix-mech-bananachunk-v4-6-stuck-in-a-lag-hole-request-a-chunk-resend-1060.19232/page-7
             World world = player.getWorld();
             Chunk chunk = world.getChunkAt(player.getLocation());
             world.refreshChunk(chunk.getX(), chunk.getZ());
 
             sender.sendMessage("Chunk resent");
 
             return true;
         } else if (cmd.getName().equalsIgnoreCase("dropnether")) {
             World world = player.getWorld();
             if (world.getEnvironment() != World.Environment.NETHER) {
                 sender.sendMessage("This command can only be used in The Nether");
                 return true;
             }
 
             int y = player.getLocation().getBlockY();
             if (y < 127) {
                 sender.sendMessage("This command can only be used when on top of The Nether, not at y="+y);
                 return true;
             }
 
             int x = player.getLocation().getBlockX();
             int z = player.getLocation().getBlockZ();
             do {
                 y -= 1;
                 int id = world.getBlockTypeIdAt(x, y, z);
                 if (id == 0 && world.getBlockTypeIdAt(x, y - 1, z) == 0) {
                     sender.sendMessage("Dropping below to "+(y - 1)+", please standby");
                     player.setNoDamageTicks(20 * 10);
                     player.teleport(new Location(world, x, y - 1, z));
                     return true;
                 }
             } while(y > 5);
             sender.sendMessage("No empty area below, please move and try again");
             return true;
         } else {
             return false;
         }
     }
 }
