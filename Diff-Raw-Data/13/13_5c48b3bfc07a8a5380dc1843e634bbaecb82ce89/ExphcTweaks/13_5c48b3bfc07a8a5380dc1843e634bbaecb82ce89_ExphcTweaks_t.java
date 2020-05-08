 package me.exphc.ExphcTweaks;
 
 import java.util.Random;
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
 
 import net.minecraft.server.ThermalExpansionCore;
 
 public class ExphcTweaks extends JavaPlugin implements Listener {
     Logger log = Logger.getLogger("Minecraft");
 
     final public static int JFM_GUTTERING_STRAIGHT = 6284;
     final public static int IC2_REFINED_IRON = 30249;
     final public static int RP2_APPLIANCE = 137;
     final public static byte RP2_PROJECT_TABLE_DAMAGE = 3;
 
     Random random = new Random();
 
     public void onEnable() {
         Bukkit.getServer().getPluginManager().registerEvents(this, this);
 
         try {
             // Recipe for Jammy Furniture Mod gutter to not conflict with WRCBE stone bowl
             // https://github.com/mushroomhostage/exphc/issues/54
             ShapedRecipe gutter = new ShapedRecipe(new ItemStack(JFM_GUTTERING_STRAIGHT, 6));
             gutter.shape("   ", "S S", "SSS");
             gutter.setIngredient('S', Material.STONE);
             Bukkit.addRecipe(gutter);
         } catch (Exception e) {
             log.warning("Not enabling alternate JFM Guttering recipe: "+e);
         }
 

        try {
            // NetherOres missing macerator recipes
            // https://github.com/mushroomhostage/exphc/issues/76
            ic2.api.Ic2Recipes.addMaceratorRecipe(new net.minecraft.server.ItemStack(127, 1, 2), new net.minecraft.server.ItemStack(net.minecraft.server.Block.GOLD_ORE, 2, 1)); // nether gold ore -> 2 x gold ore
            ic2.api.Ic2Recipes.addMaceratorRecipe(new net.minecraft.server.ItemStack(127, 1, 3), new net.minecraft.server.ItemStack(net.minecraft.server.Block.IRON_ORE, 2, 1)); // nether iron ore -> 2 x iron ore
        } catch (Exception e) {
            log.warning("Not adding NetherOres macerator recipes: "+e);
        }


         try {
             // Thermal Expansion sawmill
 
             // Trees++
             int crystalWood = 131;
             cofh.thermalexpansion.core.RecipeManager.masterList.addCutting(crystalWood, 0, new net.minecraft.server.ItemStack(crystalWood, 6, 2)); // crystal log -> 6 crystal plank, no sawdust
             cofh.thermalexpansion.core.RecipeManager.masterList.addCutting(crystalWood, 1, new net.minecraft.server.ItemStack(crystalWood, 6, 3)); // dark crystal log -> 6 dark crystal plank, no sawdust
 
             int newLogs = 172, newLogs2 = 173, newLogs3 = 174, newPlanks = 184;
 
             // first add vanilla planks for all logs
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs, 1, -1));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs2, 1, -1));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs3, 1, -1));
             // then custom planks for specific logs
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs, 1, 1));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 6), new net.minecraft.server.ItemStack(newLogs, 1, 3));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs, 1, 5));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 7), new net.minecraft.server.ItemStack(newLogs, 1, 7));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 8), new net.minecraft.server.ItemStack(newLogs, 1, 9));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 6), new net.minecraft.server.ItemStack(newLogs, 1, 11));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 10), new net.minecraft.server.ItemStack(newLogs, 1, 13));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs, 1, 15));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 6), new net.minecraft.server.ItemStack(newLogs2, 1, 1));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs2, 1, 3));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs2, 1, 5));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs2, 1, 7));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs2, 1, 9));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 0), new net.minecraft.server.ItemStack(newLogs2, 1, 11));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 0), new net.minecraft.server.ItemStack(newLogs2, 1, 13));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(newLogs2, 1, 15));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 0), new net.minecraft.server.ItemStack(newLogs3, 1, 1));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 3), new net.minecraft.server.ItemStack(newLogs3, 1, 3));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 5), new net.minecraft.server.ItemStack(newLogs3, 1, 5));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 4), new net.minecraft.server.ItemStack(newLogs3, 1, 7));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(newPlanks, 6, 9), new net.minecraft.server.ItemStack(newLogs3, 1, 9));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 0), new net.minecraft.server.ItemStack(net.minecraft.server.Block.LOG, 1, 4)); // vanilla id 17, more metadata
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 1), new net.minecraft.server.ItemStack(net.minecraft.server.Block.LOG, 1, 5));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 2), new net.minecraft.server.ItemStack(net.minecraft.server.Block.LOG, 1, 6));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 3), new net.minecraft.server.ItemStack(net.minecraft.server.Block.LOG, 1, 7));
 
             // Bunyan
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(421, 1, -1)); // turnable.wood.alt.id = 421, not normally obtainable
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 0), new net.minecraft.server.ItemStack(422, 1, 0)); /// vanilla.wood.alt.id
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 1), new net.minecraft.server.ItemStack(422, 1, 1));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 2), new net.minecraft.server.ItemStack(422, 1, 2));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6, 3), new net.minecraft.server.ItemStack(422, 1, 3));
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(423, 1, -1)); // quarter logs
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(net.minecraft.server.Block.WOOD, 6), new net.minecraft.server.ItemStack(423, 1, -1)); // quarter logs
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(420, 6, 0), new net.minecraft.server.ItemStack(426, 1, -1)); // other wood
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(420, 6, 1), new net.minecraft.server.ItemStack(426, 1, 1)); // fir log -> fir planks
             addTXLogCuttingRecipe(new net.minecraft.server.ItemStack(420, 6, 2), new net.minecraft.server.ItemStack(426, 1, 2)); // acacia log -> acacia planks
         } catch (Exception e) {
             log.warning("Not adding Thermal Expansion cutting recipes: "+e);
         }
     }
 
    // add thermal expansion sawmill recipe
     public void addTXLogCuttingRecipe(net.minecraft.server.ItemStack output, net.minecraft.server.ItemStack input) {
         net.minecraft.server.ItemStack byproduct = new net.minecraft.server.ItemStack(ThermalExpansionCore.mineralItem, 1, 5); // sawdust
         cofh.thermalexpansion.core.RecipeManager.masterList.addCutting(input.id, input.getData(), output, byproduct);
     }
 
     public void onDisable() {
     }
 
     /*
     // dump tag count on item change
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPlayerItemHeld(PlayerItemHeldEvent event) {
         Player player = event.getPlayer();
         int slot = event.getNewSlot();
         log.info("item held slot =  "+slot+" from "+player);
 
         ItemStack item = player.getInventory().getContents()[slot];
         if (item == null) {
             return;
         }
 
         log.info(" item="+item);
         if (item instanceof CraftItemStack) {
             net.minecraft.server.ItemStack raw = ((CraftItemStack)item).getHandle();
             log.info(" tags="+raw.tag);
             // TODO: dump http://mcportcentral.co.za/wiki/index.php?title=Common_decompilation_errors#Dump_the_contents_of_a_NBTTagCompound
         } else {
             log.info(" not a CraftItemStack");
         }
     }
     */
 
 
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
 
             // AnimalBikes broken in 1.2.5rev4
             if (block != null && item != null && 
                 (item.getTypeId() >= 27016 && item.getTypeId() <= 27033)) { //  AnimalBikes
                 Player player = event.getPlayer();
 
                 player.sendMessage("Sorry, animal bikes are out of service! Tired of giving free rides to everyone.");
                 player.sendMessage("Try making a GraviChestPlate instead.");
             }
         }
     }
 
     @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Entity entity = event.getRightClicked();
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) {
             return;
         }
        
         // Right-clicking ocelot with any Aquaculture fish instantly tames it
         if ((item.getTypeId() >= 23259 &&  // Aquaculture blue gill
             item.getTypeId() <= 23276) ||   // muskellunge
             (item.getTypeId() >= 23294 &&   // pollock
             item.getTypeId() <= 23299)) {   // bagrid
 
             if (entity instanceof Ocelot) {
                 Ocelot cat = (Ocelot)entity;
 
                 if (!cat.isTamed()) {
                     log.info("Aquaculture fish"+item+" from "+player+" tamed ocelot");
                     cat.setOwner(player);
 
                     Ocelot.Type type;
 
                     // Choose random tamed cat type with equal probability
                     // Note this differs from http://www.minecraftwiki.net/wiki/Ocelot#Taming
                     switch(random.nextInt(3)) {
                     case 0: type = Ocelot.Type.BLACK_CAT; break;
                     case 1: type = Ocelot.Type.RED_CAT; break;
                     default:
                     case 2: type = Ocelot.Type.SIAMESE_CAT; break;
                     // Interestingly, you can keep type as wild ocelot but tame it..
                     }
 
                     cat.setCatType(type);
                 }
             }
         }
     }
 
 
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
         if (cmd.getName().equalsIgnoreCase("tell")) {    // TODO: allow op or console
             sender.sendMessage("To privately communicate with a player, try talking into a smartphone (clock)");
             return true;
         } 
         /*
         if (cmd.getName().equalsIgnoreCase("give") ||
             cmd.getName().equalsIgnoreCase("tp")) {
             sender.sendMessage("-");
             return true;
         }*/
 
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
