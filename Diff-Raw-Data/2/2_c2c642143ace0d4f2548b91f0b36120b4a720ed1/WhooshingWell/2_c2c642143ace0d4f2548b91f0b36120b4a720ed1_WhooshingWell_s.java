 package me.ellbristow.WhooshingWell;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import org.bukkit.World.Environment;
 import org.bukkit.*;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WhooshingWell extends JavaPlugin implements Listener {
     
     protected static WhooshingWell plugin;
     protected FileConfiguration config;
     private FileConfiguration portalConfig = null;
     private File portalFile = null;
     
     protected boolean clearInv;
     protected boolean clearArmor;
     protected boolean allowNether;
     protected boolean allowEnd;
     
     protected HashMap<String, Location> destinations = new HashMap<String, Location>();
     
     @Override
     public void onDisable() {
     }
     
     @Override
     public void onEnable () {
         config = getConfig();
         clearInv = config.getBoolean("requireEmptyInventory", false);
         config.set("requireEmptyInventory", clearInv);
         clearArmor = config.getBoolean("requireEmptyArmor", false);
         config.set("requireEmptyArmor", clearArmor);
         allowNether = config.getBoolean("allowNetherWells", true);
         config.set("allowNetherWells", allowNether);
         allowEnd = config.getBoolean("allowEndWells", true);
         config.set("allowEndWells", allowEnd);
         saveConfig();
         ConfigurationSection dests = config.getConfigurationSection("destinations");
         if (dests != null) {
             for (String key : dests.getKeys(false)) {
                 String world = dests.getString(key+".world");
                 if (getServer().getWorld(world) != null) {
                     double x = dests.getDouble(key+".x");
                     double y = dests.getDouble(key+".y");
                     double z = dests.getDouble(key+".z");
                     List<Float> floats = dests.getFloatList(key+".yawpitch");
                     float yaw = floats.get(0);
                     float pitch = floats.get(1);
                     Location loc = new Location(getServer().getWorld(world), x, y, z, yaw, pitch);
                     destinations.put(key, loc);
                 }
             }
         }
         portalConfig = getPortals();
         forceWorldLoads();
         getServer().getPluginManager().registerEvents(this, this);
     }
     
     @Override
     public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (commandLabel.equalsIgnoreCase("ww")) {
             if (args.length == 0) {
                 String version = getDescription().getVersion();
                 sender.sendMessage(ChatColor.GOLD + "WhooshingWell v" + ChatColor.WHITE + version + ChatColor.GOLD + " by " + ChatColor.WHITE + "ellbristow");
                 sender.sendMessage(ChatColor.GOLD + "=============");
                 sender.sendMessage(ChatColor.GOLD + "Require Empty Inventory : " + ChatColor.WHITE + clearInv);
                 sender.sendMessage(ChatColor.GOLD + "Require No Armor          : " + ChatColor.WHITE + clearArmor);
                 sender.sendMessage(ChatColor.GOLD + "Allow Nether Wells          : " + ChatColor.WHITE + allowNether);
                 sender.sendMessage(ChatColor.GOLD + "Allow End Wells              : " + ChatColor.WHITE + allowEnd);
                 return true;
             } else if (args.length == 1) {
                 if ("list".equalsIgnoreCase(args[0])) {
                     if (!sender.hasPermission("whooshingwell.world.list")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     Object[] worlds = getServer().getWorlds().toArray();
                     sender.sendMessage(ChatColor.GOLD + "World List");
                     sender.sendMessage(ChatColor.GOLD + "==========");
                     for (int i = 0; i < worlds.length; i++) {
                         World world = (World)worlds[i];
                         if (!(world.getEnvironment().equals(Environment.NETHER) && !allowNether) && !(world.getEnvironment().equals(Environment.THE_END) && !allowEnd)) {
                             sender.sendMessage(ChatColor.GOLD + world.getName() + ChatColor.GRAY + " ("+i+")");
                         }
                     }
                     for (String key : destinations.keySet()) {
                         Location loc = destinations.get(key);
                         World world = loc.getWorld();
                         if (!(world.getEnvironment().equals(Environment.NETHER) && !allowNether) && !(world.getEnvironment().equals(Environment.THE_END) && !allowEnd)) {
                             sender.sendMessage(ChatColor.YELLOW + key);
                         }
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("toggle")) {
                     if (!sender.hasPermission("whooshingwell.toggle.emptyinv") && !sender.hasPermission("whooshingwell.toggle.emptyarmor")  && !sender.hasPermission("whooshingwell.toggle.allownether") && !sender.hasPermission("whooshingwell.toggle.allowend")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     sender.sendMessage(ChatColor.RED + "You must specify a setting to toggle!");
                     if (sender.hasPermission("whooshingwell.toggle.emptyinv")) {
                         sender.sendMessage(ChatColor.GRAY + "EmptyInv: Players cannot carry inventory through portals");
                     }
                     if (sender.hasPermission("whooshingwell.toggle.emptyarmor")) {
                         sender.sendMessage(ChatColor.GRAY + "EmptyArmor: Players cannot wear armor through portals");
                     }
                     if (sender.hasPermission("whooshingwell.toggle.allownether")) {
                         sender.sendMessage(ChatColor.GRAY + "AllowNether: New wells can lead to Nether worlds");
                     }
                     if (sender.hasPermission("whooshingwell.toggle.allowend")) {
                         sender.sendMessage(ChatColor.GRAY + "AllowEnd: New wells can lean to End worlds");
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("addworld")) {
                     if (!sender.hasPermission("whooshingwell.world.create")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     sender.sendMessage(ChatColor.RED + "You must specify a world name!");
                     sender.sendMessage(ChatColor.RED + "/ww addworld [World Name]");
                     return true;
                 } else if (args[0].equalsIgnoreCase("deleteworld") || args[0].equalsIgnoreCase("delworld")) {
                     if (!sender.hasPermission("whooshingwell.world.delete")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     sender.sendMessage(ChatColor.RED + "You must specify a world name!");
                     sender.sendMessage(ChatColor.RED + "/ww addworld [World Name]");
                     return true;
                 } else if (args[0].equalsIgnoreCase("setdest")) {
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(ChatColor.RED + "This command cannot be run from the console!");
                         return true;
                     }
                     if (!sender.hasPermission("whooshingwell.setdest")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     sender.sendMessage(ChatColor.RED + "You must specify a destination name!");
                     sender.sendMessage(ChatColor.RED + "/ww setdest [Destination Name]");
                     return true;
                 } else if (args[0].equalsIgnoreCase("deldest")) {
                     if (!sender.hasPermission("whooshingwell.deldest")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     sender.sendMessage(ChatColor.RED + "You must specify a destination name!");
                     sender.sendMessage(ChatColor.RED + "/ww deldest [Destination Name]");
                     return true;
                 }
             } else if (args.length == 2) {
                 if (args[0].equalsIgnoreCase("addworld")) {
                     if (!sender.hasPermission("whooshingwell.world.create")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     if (getServer().getWorld(args[1]) != null){
                         sender.sendMessage(ChatColor.RED + "A world called '" + ChatColor.WHITE + args[1] + ChatColor.RED + "' already exists!");
                         return true;
                     } else if (args[1].length() < 4) {
                         sender.sendMessage(ChatColor.RED + "Please use a name with at least 4 characters!");
                         return true;
                     } else {
                         sender.sendMessage(ChatColor.GOLD + "Creating '" + ChatColor.WHITE + args[1] + ChatColor.GOLD + "'!");
                         getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "A new world has been found!");
                         getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "You may experience some lag while we map it!");
                         WorldCreator wc = makeWorld(args[1], sender);
                         getServer().createWorld(wc);
                         return true;
                     }
                 } else if ((args[0].equalsIgnoreCase("deleteworld") || args[0].equalsIgnoreCase("delworld"))) {
                     if (!sender.hasPermission("whooshingwell.world.delete")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     if (getServer().getWorld(args[1]) == null){
                         sender.sendMessage(ChatColor.RED + "There is no world called '" + ChatColor.WHITE + args[1] + ChatColor.RED + "'!");
                         return true;
                     } else if (args[1].equalsIgnoreCase(getServer().getWorlds().get(0).getName())) {
                         sender.sendMessage(ChatColor.RED + "You cannot delete the default world!");
                         return true;
                     } else if (!getServer().getWorld(args[1]).getPlayers().isEmpty()) {
                         sender.sendMessage(ChatColor.RED + "There are players in " + ChatColor.WHITE + args[1] + ChatColor.RED + "!");
                         sender.sendMessage(ChatColor.RED + "All players must leave " + ChatColor.WHITE + args[1] + ChatColor.RED + " to delete it!");
                         return true;
                     } else {
                         getServer().unloadWorld(getServer().getWorld(args[1]), false);
                         sender.sendMessage(ChatColor.GOLD + "Deleting '" + ChatColor.WHITE + args[1] + ChatColor.GOLD + "'!");
                         getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "A world is collapsing!");
                         getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "You may experience some lag while we tidy it!");
                         delete(getWorldDataFolder(args[1]));
                         portalConfig.set(args[1], null);
                         return true;
                     }
                 } else if (args[0].equalsIgnoreCase("setdest")) {
                     if (!(sender instanceof Player)) {
                         sender.sendMessage(ChatColor.RED + "This command cannot be run from the console!");
                         return true;
                     }
                     if (!sender.hasPermission("whooshingwell.setdest")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     if (getServer().getWorld(args[1]) != null) {
                         sender.sendMessage(ChatColor.RED + "You cannot set a destination with the same name as a world!");
                         sender.sendMessage(ChatColor.RED + "/ww setdest [Destination Name]");
                         return true;
                     }
                     Player player = (Player)sender;
                     World world = player.getWorld();
                     if (world.getEnvironment().equals(Environment.NETHER) && !allowNether) {
                         player.sendMessage(ChatColor.RED + "Whooshing wells cannot lead to Nether worlds");
                         return true;
                     } else if (world.getEnvironment().equals(Environment.THE_END) && !allowEnd) {
                         player.sendMessage(ChatColor.RED + "Whooshing wells cannot lead to End worlds");
                         return true;
                     }
                     Location loc = player.getLocation();
                     destinations.put(args[1].toLowerCase(), loc);
                     config.set("destinations."+args[1].toLowerCase()+".world", loc.getWorld().getName());
                     config.set("destinations."+args[1].toLowerCase()+".x", loc.getX());
                     config.set("destinations."+args[1].toLowerCase()+".y", loc.getY());
                     config.set("destinations."+args[1].toLowerCase()+".z", loc.getZ());
                     List<Float> yawpitch = new ArrayList<Float>();
                     yawpitch.add(loc.getYaw());
                     yawpitch.add(loc.getPitch());
                     config.set("destinations."+args[1].toLowerCase()+".yawpitch", yawpitch);
                     saveConfig();
                     player.sendMessage(ChatColor.GOLD + "New destination " + ChatColor.WHITE + args[1] + ChatColor.GOLD + " set!");
                     return true;
                 } else if (args[0].equalsIgnoreCase("deldest")) {
                     if (!sender.hasPermission("whooshingwell.deldest")) {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                         return true;
                     }
                     if (destinations.get(args[1].toLowerCase()) == null) {
                         sender.sendMessage(ChatColor.RED + "Destination "+ChatColor.WHITE +args[1]+ ChatColor.RED+" not found!");
                         return true;
                     }
                     destinations.remove(args[1].toLowerCase());
                     config.set("destinations."+args[1].toLowerCase(), null);
                     sender.sendMessage(ChatColor.GOLD + "Destination " + ChatColor.WHITE + args[1] + ChatColor.GOLD + " deleted!");
                     return true;
                 } else if (args[0].equalsIgnoreCase("toggle")) {
                     if (args[1].equalsIgnoreCase("EmptyInv")) {
                         if (!sender.hasPermission("whooshingwell.toggle.emptyinv")) {
                             sender.sendMessage(ChatColor.RED + "You do not have permission to toggle this setting!");
                             return true;
                         }
                         if (clearInv) {
                             clearInv = false;
                             sender.sendMessage(ChatColor.GOLD + "Players no longer need to empty their inventory to use Whooshing Wells!");
                         } else {
                             clearInv = true;
                             sender.sendMessage(ChatColor.GOLD + "Players must now empty their inventory to use Whooshing Wells!");
                         }
                         config.set("requireEmptyInventory", clearInv);
                         saveConfig();
                         return true;
                     } else if (args[1].equalsIgnoreCase("EmptyArmor")) {
                         if (!sender.hasPermission("whooshingwell.toggle.emptyinv")) {
                             sender.sendMessage(ChatColor.RED + "You do not have permission to toggle this setting!");
                             return true;
                         }
                         if (clearArmor) {
                             clearArmor = false;
                             sender.sendMessage(ChatColor.GOLD + "Players no longer need to remove their armor to use Whooshing Wells!");
                         } else {
                             clearArmor = true;
                             sender.sendMessage(ChatColor.GOLD + "Players must now remove their armor to use Whooshing Wells!");
                         }
                         config.set("requireEmptyArmor", clearArmor);
                         saveConfig();
                         return true;
                     } else if (args[1].equalsIgnoreCase("AllowNether")) {
                         if (!sender.hasPermission("whooshingwell.toggle.allownether")) {
                             sender.sendMessage(ChatColor.RED + "You do not have permission to toggle this setting!");
                             return true;
                         }
                         if (allowNether) {
                             allowNether = false;
                             sender.sendMessage(ChatColor.GOLD + "New wells now CANNOT lead to Nether worlds!");
                         } else {
                             allowNether = true;
                             sender.sendMessage(ChatColor.GOLD + "New wells now CAN lead to Nether worlds!");
                         }
                         config.set("allowNetherWells", allowNether);
                         saveConfig();
                         return true;
                     } else if (args[1].equalsIgnoreCase("AllowEnd")) {
                         if (!sender.hasPermission("whooshingwell.toggle.allowend")) {
                             sender.sendMessage(ChatColor.RED + "You do not have permission to toggle this setting!");
                             return true;
                         }
                         if (allowEnd) {
                             allowEnd = false;
                             sender.sendMessage(ChatColor.GOLD + "New wells now CANNOT lead to End worlds!");
                         } else {
                             allowEnd = true;
                             sender.sendMessage(ChatColor.GOLD + "New wells now CAN lead to End worlds!");
                         }
                         config.set("allowEndWells", allowEnd);
                         saveConfig();
                         return true;
                     }
                     sender.sendMessage(ChatColor.RED + "Sorry! I dont' recognise that toggle option!");
                     return true;
                 }
             }
         }
         sender.sendMessage(ChatColor.RED + "Sorry! That command was not recognised!");
         return true;
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onSignChange(SignChangeEvent event) {
         if (!event.isCancelled()) {
             String line0 = event.getLine(0);
             if ("[ww]".equalsIgnoreCase(line0)) {
                 Player player = event.getPlayer();
                 Block signBlock = event.getBlock();
                 if (!player.hasPermission("whooshingwell.create")) {
                     player.sendMessage(ChatColor.RED + "You do not have permission to create a Whooshing Well!");
                     event.setCancelled(true);
                     dropSign(signBlock);
                     return;
                 }
                 // Player has permission. Check not already a WW
                 if (isWW(signBlock.getLocation())) {
                     player.sendMessage(ChatColor.RED + "There is already a Whooshing Well here!");
                     event.setCancelled(true);
                     dropSign(signBlock);
                     return;
                 }
                 // Not a WW. Check well formation
                 if (!hasWellLayout(signBlock, false) && !hasWellLayout(signBlock, true)) {
                     player.sendMessage(ChatColor.RED + "Incorrect layout for a Whooshing Well!");
                     event.setCancelled(true);
                     dropSign(signBlock);
                     return;
                 }
                 Block[] checkAirBlocks = getAir(signBlock.getLocation().getBlock(), getButtonDirection(signBlock), false);
                 if (checkAirBlocks == null) {
                     checkAirBlocks = getAir(signBlock.getLocation().getBlock(), getButtonDirection(signBlock), true);
                 }
                 if (checkAirBlocks != null) {
                     for (Block airBlock : checkAirBlocks) {
                         if (isWW(airBlock.getLocation())) {
                             player.sendMessage(ChatColor.RED + "There is already a Whooshing Well here!");
                             event.setCancelled(true);
                             dropSign(signBlock);
                             return;
                         }
                     }
                 }
                 // Well OK, Check World Name/ID
                 String line1 = event.getLine(1);
                 String worldName;
                 boolean isDest = false;
                 if (line1.toLowerCase().startsWith("world:")) {
                     String idString = line1.split(":")[1];
                     try {
                         int worldId = Integer.parseInt(idString);
                         World world = getServer().getWorlds().get(worldId);
                         if (world == null) {
                             player.sendMessage(ChatColor.RED + "Could not find World Id " + ChatColor.WHITE + idString + ChatColor.RED + "!");
                             player.sendMessage(ChatColor.RED + "Please make sure line 2 of your sign is a valid world or destination name");
                             player.sendMessage(ChatColor.RED + "or is in the format \"world:[world id]\"!");
                             event.setCancelled(true);
                             dropSign(signBlock);
                             return;
                         } else {
                             worldName = world.getName();
                         }
                     } catch (NumberFormatException e) {
                         player.sendMessage(ChatColor.WHITE + idString + ChatColor.RED + " is not a valid world ID!");
                         player.sendMessage(ChatColor.RED + "Please make sure line 2 of your sign is a valid world or destination name");
                         player.sendMessage(ChatColor.RED + "or is in the format \"world:[world id]\"!");
                         event.setCancelled(true);
                         dropSign(signBlock);
                         return;
                     }
                 } else if (getServer().getWorld(line1) == null) {
                     if (destinations.get(line1.toLowerCase()) != null) {
                         isDest = true;
                         worldName = line1;
                     } else {
                         player.sendMessage(ChatColor.RED + "Could not find a world or destination called '" + ChatColor.WHITE + line1 + ChatColor.RED + "'!");
                         player.sendMessage(ChatColor.RED + "Please make sure line 2 of your sign is a valid world or destination name");
                         player.sendMessage(ChatColor.RED + "or is in the format \"world:[world id]\"!");
                         event.setCancelled(true);
                         dropSign(signBlock);
                         return;
                     }
                 } else {
                     worldName = line1;
                 }
                 
                 World world;
                 if (!isDest) {
                     // World Name/ID OK, check Nether/End restrictions
                     world = getServer().getWorld(worldName);
                 } else {
                     Location dest = destinations.get(line1.toLowerCase());
                     world = dest.getWorld();
                 }
                 
                 if (world.getEnvironment().equals(Environment.NETHER) && !allowNether) {
                     player.sendMessage(ChatColor.RED + "Whooshing Wells cannot lead to Nether worlds!");
                     dropSign(signBlock);
                     return;
                 } else if (world.getEnvironment().equals(Environment.THE_END) && !allowEnd) {
                     player.sendMessage(ChatColor.RED + "Whooshing Wells cannot lead to End worlds!");
                     dropSign(signBlock);
                     return;
                 }
                 
                 // GOOD TO GO!
                 String thisPortal = signBlock.getWorld().getName() + "." + signBlock.getX() + "_" + signBlock.getY() + "_" + signBlock.getZ();
                 Block[] airBlocks = getAir(signBlock, getButtonDirection(signBlock), false);
                 if (airBlocks == null) {
                     airBlocks = getAir(signBlock, getButtonDirection(signBlock), true);
                 }
                 String location = "";
                 for (Block block : airBlocks) {
                     if (!"".equals(location)) {
                         location += ";";
                     }
                     location += block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
                 }
                 portalConfig.set(thisPortal + ".location",location);
                 portalConfig.set(thisPortal + ".destination", worldName);
                 savePortals();
                 player.sendMessage(ChatColor.GOLD + "A new Whooshing Well to '" + ChatColor.WHITE + worldName + ChatColor.GOLD + "' has been created!");
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (!event.isCancelled()) {
             Block block = event.getClickedBlock();
             if (block != null && (block.getType() == Material.STONE_BUTTON || block.getType() == Material.WOOD_BUTTON)) {
                 if (isWWButton(block, true) || isWWButton(block, false) && player.hasPermission("whooshingwell.use")) {
                     String location = getWWFromButton(block);
                     if (location != null) {
                         String[] locations = location.split(";");
                         String[] loc0 = locations[0].split(":");
                         Location loc = new Location(getServer().getWorld(loc0[0]), Integer.parseInt(loc0[1]), Integer.parseInt(loc0[2]), Integer.parseInt(loc0[3]));
                         toggleWW(block);
                     }
                 }
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockPlace (BlockPlaceEvent event) {
         if (!event.isCancelled()) {
             Block block = event.getBlock();
             if (isWW(block.getLocation())) {
                 event.setCancelled(true);
                 event.getPlayer().sendMessage(ChatColor.RED + "You can't place a block there!");
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockBreak (BlockBreakEvent event) {
         if (!event.isCancelled()) {
             Block block = event.getBlock();
             if (block.getTypeId() == 63 || block.getTypeId() == 68) {
                 if (isWW(block.getLocation())) {
                     Player player = event.getPlayer();
                     if (!player.hasPermission("whooshingwell.destroy")) {
                         player.sendMessage(ChatColor.RED + "You do not have permission to break that sign!");
                         event.setCancelled(true);
                     } else {
                         Location signLocation = block.getLocation();
                         String locString = signLocation.getWorld().getName() + "." + (int)Math.floor(signLocation.getX()) + "_" + (int)Math.floor(signLocation.getY()) + "_" + (int)Math.floor(signLocation.getZ()) + ".location";
                         String destString = signLocation.getWorld().getName() + "." + (int)Math.floor(signLocation.getX()) + "_" + (int)Math.floor(signLocation.getY()) + "_" + (int)Math.floor(signLocation.getZ()) + ".destination";
                         String section = signLocation.getWorld().getName() + "." + (int)Math.floor(signLocation.getX()) + "_" + (int)Math.floor(signLocation.getY()) + "_" + (int)Math.floor(signLocation.getZ());
                         portalConfig.set(locString, null);
                         portalConfig.set(destString, null);
                         portalConfig.set(section, null);
                         savePortals();
                         if (getAir(block, getButtonDirection(block), true) != null) {
                             toggleWW(block.getRelative(getButtonDirection(block), 3));
                         }
                         player.sendMessage(ChatColor.GOLD + "Whooshing Well Deactivated!");
                     }
                 }
             } else if (isWWStairs(block)) {
                 Player player = event.getPlayer();
                 player.sendMessage(ChatColor.RED + "Those stairs are protected by a Whooshing Well!");
                 event.setCancelled(true);
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPortalJump (PlayerPortalEvent event) {
         if (!event.isCancelled()) {
             TeleportCause cause = event.getCause();
             if (cause.equals(TeleportCause.END_PORTAL)) {
                 Location fromLoc = event.getFrom();
                 Location footLoc = event.getPlayer().getLocation();
                 Location underFoot = footLoc.clone().subtract(0, 1, 0);
                 Location headLoc = event.getPlayer().getEyeLocation();
                 Location overHead = headLoc.clone().add(0, 1, 0);
                 if (isWW(fromLoc) || isWW(footLoc) || isWW(underFoot) || isWW(headLoc) || isWW(overHead)) {
                     event.setCancelled(true);
                     if (event.getPlayer().hasPermission("whooshingwell.use")) {
                         String destination = "";
                         if (isWW(fromLoc)) {
                             destination = getDestination(fromLoc);
                             togglePortalFromJump(fromLoc);
                         } else if (isWW(footLoc)) {
                             destination = getDestination(footLoc);
                             togglePortalFromJump(footLoc);
                         } else if (isWW(underFoot)) {
                             destination = getDestination(underFoot);
                             togglePortalFromJump(underFoot);
                         } else if (isWW(headLoc)) {
                             destination = getDestination(headLoc);
                             togglePortalFromJump(headLoc);
                         } else if (isWW(overHead)) {
                             destination = getDestination(overHead);
                             togglePortalFromJump(overHead);
                         }
                         if (getServer().getWorld(destination) != null) {
                             if (clearInv && !emptyInventory(event.getPlayer().getInventory())) {
                                 event.getPlayer().sendMessage(ChatColor.RED + "You must have an empty inventory to use a Whooshing Well!");
                                 return;
                             }
                             if (clearArmor && !emptyArmor(event.getPlayer().getInventory())) {
                                 event.getPlayer().sendMessage(ChatColor.RED + "You must not be wearing armor to use a Whooshing Well!");
                                 return;
                             }
                             Location teleportDestination =  getServer().getWorld(destination).getSpawnLocation();
                             event.getPlayer().sendMessage(ChatColor.GOLD + "WHOOSH!");
                             event.getPlayer().teleport(teleportDestination);
                         } else if (destinations.get(destination.toLowerCase()) != null) {
                             Location dest = destinations.get(destination.toLowerCase());
                             event.getPlayer().sendMessage(ChatColor.GOLD + "WHOOSH!");
                             event.getPlayer().teleport(dest);
                         } else {
                             event.getPlayer().sendMessage(ChatColor.RED + "Oh Dear! The other end of this portal no longer exists!");
                         }
                     }
                 } else {
                     String defaultWorld = getServer().getWorlds().get(0).getName();
                     if (!defaultWorld.equals(fromLoc.getWorld().getName()) && !(defaultWorld + "_nether").equals(fromLoc.getWorld().getName()) && !(defaultWorld + "_the_end").equals(fromLoc.getWorld().getName())) {
                         String fromWorld = fromLoc.getWorld().getName();
                         if (!fromWorld.endsWith("_nether") && !fromWorld.endsWith("_the_end")) {
                             World world = getServer().getWorld(getServer().getWorlds().get(0).getName() + "_the_end");
                             Location to = new Location(world,event.getFrom().getX(),event.getFrom().getY(),event.getFrom().getZ());
                             event.setTo(to);
                         }
                     }
                 }
             } else if (cause == TeleportCause.NETHER_PORTAL) {
                 String defaultWorld = getServer().getWorlds().get(0).getName();
                 Location fromLoc = event.getFrom();
                 if (!defaultWorld.equals(fromLoc.getWorld().getName()) && !(defaultWorld + "_nether").equals(fromLoc.getWorld().getName()) && !(defaultWorld + "_the_end").equals(fromLoc.getWorld().getName())) {
                     String fromWorld = fromLoc.getWorld().getName();
                     if (!fromWorld.endsWith("_nether") && !fromWorld.endsWith("_the_end")) {
                         World world = getServer().getWorld(getServer().getWorlds().get(0).getName() + "_nether");
                         Location to = new Location(world,event.getFrom().getX(),event.getFrom().getY(),event.getFrom().getZ());
                         event.setTo(to);
                     }
                 }
             }
         }
     }
     
     private boolean emptyArmor(PlayerInventory inv) {
         if (inv == null) {
             return true;
         }
         ItemStack helmet = inv.getHelmet();
         ItemStack chest = inv.getChestplate();
         ItemStack legs = inv.getLeggings();
         ItemStack boots = inv.getBoots();
         if (helmet != null || chest != null || legs != null || boots != null) {
             return false;
         }
         return true;
     }
     
     private boolean emptyInventory(Inventory inv) {
         if (inv == null) {
             return true;
         }
         ItemStack[] contents = inv.getContents();
         for (ItemStack stack : contents) {
             if (stack != null && stack.getType() != Material.AIR) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean isWW(Location loc) {
         // Check for Sign Location
         String portalCoords = loc.getWorld().getName() + "." + (int)loc.getX() + "_" + (int)loc.getY() + "_" + (int)loc.getZ();
         if (portalConfig.getConfigurationSection(portalCoords) != null) {
             return true;
         } else if (loc.getBlock().getTypeId() == 63 || loc.getBlock().getTypeId() == 68) {
             return false;
         }
         // Check for Portal Location
         ConfigurationSection section = portalConfig.getConfigurationSection(loc.getWorld().getName());
         if (section != null) {
             Object[] configKeys = section.getKeys(false).toArray();
             for (int i = 0; i < configKeys.length; i++) {
                 String key = configKeys[i].toString();
                 String locations = section.getString(key + ".location");
                 for (String location : locations.split(";")) {
                     if (location.equals(loc.getWorld().getName() + ":" + (int)Math.floor(loc.getX()) + ":" + (int)Math.floor(loc.getY()) + ":" + (int)Math.floor(loc.getZ()))) {
                         return true;
                     }
                 }
             }
         }
         // Check Air for WW
         Block[] airBlocks = getAir(loc.getBlock(), getButtonDirection(loc.getBlock()), false);
         if (airBlocks == null) {
             airBlocks = getAir(loc.getBlock(), getButtonDirection(loc.getBlock()), true);
         }
         if (airBlocks != null) {
             if (isWW(airBlocks[0].getLocation())) {
                 return true;
             }
         }
         return false;
     }
     
     private String getDestination(Location loc) {
         ConfigurationSection section = portalConfig.getConfigurationSection(loc.getWorld().getName());
         if (section != null) {
             Object[] configKeys = section.getKeys(false).toArray();
             for (int i = 0; i < configKeys.length; i++) {
                 String key = configKeys[i].toString();
                 String locations = section.getString(key + ".location");
                 for (String location : locations.split(";")) {
                     if (location.equals(loc.getWorld().getName() + ":" + (int)Math.floor(loc.getX()) + ":" + (int)Math.floor(loc.getY()) + ":" + (int)Math.floor(loc.getZ()))) {
                         String dest = section.getString(key + ".destination");
                         try {
                             int worldId = Integer.parseInt(dest);
                             return getServer().getWorlds().get(worldId).getName();
                         } catch (NumberFormatException e) {
                             return section.getString(key + ".destination");
                         }
                     }
                 }
             }
         }
         return "";
     }
     
     private boolean hasWellLayout(Block signBlock, boolean active) {
         // BUTTON
         BlockFace buttonDirection = getButtonDirection(signBlock);
         if (buttonDirection == null) {
             return false;
         }
         Block[] stairBlocks = getStairs(signBlock, buttonDirection);
         if (stairBlocks == null) {
             return false;
         }
         Block[] airBlocks = getAir(signBlock, buttonDirection, active);
         if (airBlocks == null) {
             return false;
         }
         return true;
     }
     
     private BlockFace getButtonDirection(Block signBlock) {
         // Try NORTH
         Block attempt = signBlock.getRelative(BlockFace.NORTH, 3);
         if (attempt.getType() == Material.STONE_BUTTON || attempt.getType() == Material.WOOD_BUTTON) {
             return BlockFace.NORTH;
         }
         // Try SOUTH
         attempt = signBlock.getRelative(BlockFace.SOUTH, 3);
         if (attempt.getType() == Material.STONE_BUTTON || attempt.getType() == Material.WOOD_BUTTON) {
             return BlockFace.SOUTH;
         }
         // Try EAST
         attempt = signBlock.getRelative(BlockFace.EAST, 3);
         if (attempt.getType() == Material.STONE_BUTTON || attempt.getType() == Material.WOOD_BUTTON) {
             return BlockFace.EAST;
         }
         // Try WEST
         attempt = signBlock.getRelative(BlockFace.WEST, 3);
         if (attempt.getType() == Material.STONE_BUTTON || attempt.getType() == Material.WOOD_BUTTON) {
             return BlockFace.WEST;
         }
         return null;
     }
     
     private Block[] getStairs(Block signBlock, BlockFace buttonDirection) {
         if (buttonDirection == BlockFace.NORTH) {
             Block cornerBlock = signBlock.getRelative(BlockFace.EAST, 1);
             Block stair1 = cornerBlock.getRelative(BlockFace.NORTH, 1);
             Block stair2 = cornerBlock.getRelative(BlockFace.NORTH, 2);
             Block stair3 = cornerBlock.getRelative(BlockFace.EAST, 1);
             Block stair4 = cornerBlock.getRelative(BlockFace.EAST, 2);
             Block stair5 = cornerBlock.getRelative(BlockFace.NORTH, 3).getRelative(BlockFace.EAST, 1);
             Block stair6 = cornerBlock.getRelative(BlockFace.NORTH, 3).getRelative(BlockFace.EAST, 2);
             Block stair7 = cornerBlock.getRelative(BlockFace.EAST, 3).getRelative(BlockFace.NORTH, 1);
             Block stair8 = cornerBlock.getRelative(BlockFace.EAST, 3).getRelative(BlockFace.NORTH, 2);
             if (isStairs(stair1) && isStairs(stair2) && isStairs(stair3) && isStairs(stair4) && isStairs(stair5) && isStairs(stair6) && isStairs(stair7) && isStairs(stair8)) {
                 Block[] allStairs = {stair1, stair2, stair3, stair4, stair5, stair6, stair7, stair8};
                 return allStairs;
             } else {
                 return null;
             }
         } else if (buttonDirection == BlockFace.SOUTH) {
             Block cornerBlock = signBlock.getRelative(BlockFace.WEST, 1);
             Block stair1 = cornerBlock.getRelative(BlockFace.SOUTH, 1);
             Block stair2 = cornerBlock.getRelative(BlockFace.SOUTH, 2);
             Block stair3 = cornerBlock.getRelative(BlockFace.WEST, 1);
             Block stair4 = cornerBlock.getRelative(BlockFace.WEST, 2);
             Block stair5 = cornerBlock.getRelative(BlockFace.SOUTH, 3).getRelative(BlockFace.WEST, 1);
             Block stair6 = cornerBlock.getRelative(BlockFace.SOUTH, 3).getRelative(BlockFace.WEST, 2);
             Block stair7 = cornerBlock.getRelative(BlockFace.WEST, 3).getRelative(BlockFace.SOUTH, 1);
             Block stair8 = cornerBlock.getRelative(BlockFace.WEST, 3).getRelative(BlockFace.SOUTH, 2);
             if (isStairs(stair1) && isStairs(stair2) && isStairs(stair3) && isStairs(stair4) && isStairs(stair5) && isStairs(stair6) && isStairs(stair7) && isStairs(stair8)) {
                 Block[] allStairs = {stair1, stair2, stair3, stair4, stair5, stair6, stair7, stair8};
                 return allStairs;
             } else {
                 return null;
             }
         } else if (buttonDirection == BlockFace.EAST) {
             Block cornerBlock = signBlock.getRelative(BlockFace.SOUTH, 1);
             Block stair1 = cornerBlock.getRelative(BlockFace.EAST, 1);
             Block stair2 = cornerBlock.getRelative(BlockFace.EAST, 2);
             Block stair3 = cornerBlock.getRelative(BlockFace.SOUTH, 1);
             Block stair4 = cornerBlock.getRelative(BlockFace.SOUTH, 2);
             Block stair5 = cornerBlock.getRelative(BlockFace.EAST, 3).getRelative(BlockFace.SOUTH, 1);
             Block stair6 = cornerBlock.getRelative(BlockFace.EAST, 3).getRelative(BlockFace.SOUTH, 2);
             Block stair7 = cornerBlock.getRelative(BlockFace.SOUTH, 3).getRelative(BlockFace.EAST, 1);
             Block stair8 = cornerBlock.getRelative(BlockFace.SOUTH, 3).getRelative(BlockFace.EAST, 2);
             if (isStairs(stair1) && isStairs(stair2) && isStairs(stair3) && isStairs(stair4) && isStairs(stair5) && isStairs(stair6) && isStairs(stair7) && isStairs(stair8)) {
                 Block[] allStairs = {stair1, stair2, stair3, stair4, stair5, stair6, stair7, stair8};
                 return allStairs;
             } else {
                 return null;
             }
         }else if (buttonDirection == BlockFace.WEST) {
             Block cornerBlock = signBlock.getRelative(BlockFace.NORTH, 1);
             Block stair1 = cornerBlock.getRelative(BlockFace.WEST, 1);
             Block stair2 = cornerBlock.getRelative(BlockFace.WEST, 2);
             Block stair3 = cornerBlock.getRelative(BlockFace.NORTH, 1);
             Block stair4 = cornerBlock.getRelative(BlockFace.NORTH, 2);
             Block stair5 = cornerBlock.getRelative(BlockFace.WEST, 3).getRelative(BlockFace.NORTH, 1);
             Block stair6 = cornerBlock.getRelative(BlockFace.WEST, 3).getRelative(BlockFace.NORTH, 2);
             Block stair7 = cornerBlock.getRelative(BlockFace.NORTH, 3).getRelative(BlockFace.WEST, 1);
             Block stair8 = cornerBlock.getRelative(BlockFace.NORTH, 3).getRelative(BlockFace.WEST, 2);
             if (isStairs(stair1) && isStairs(stair2) && isStairs(stair3) && isStairs(stair4) && isStairs(stair5) && isStairs(stair6) && isStairs(stair7) && isStairs(stair8)) {
                 Block[] allStairs = {stair1, stair2, stair3, stair4, stair5, stair6, stair7, stair8};
                 return allStairs;
             } else {
                 return null;
             }
         }
         return null;
     }
     
     private Block[] getAir(Block signBlock, BlockFace buttonDirection, boolean active) {
         if (buttonDirection == BlockFace.NORTH) {
             Block cornerBlock = signBlock.getRelative(BlockFace.EAST, 1);
             Block air1 = cornerBlock.getRelative(BlockFace.NORTH, 1).getRelative(BlockFace.EAST, 1);
             Block air2 = cornerBlock.getRelative(BlockFace.NORTH, 1).getRelative(BlockFace.EAST, 2);
             Block air3 = cornerBlock.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.EAST, 1);
             Block air4 = cornerBlock.getRelative(BlockFace.NORTH, 2).getRelative(BlockFace.EAST, 2);
             if ((!active && isAir(air1) && isAir(air2) && isAir(air3) && isAir(air4)) || (active && isEnd(air1) && isEnd(air2) & isEnd(air3) && isEnd(air4))) {
                 Block[] airBlocks = {air1,air2,air3,air4};
                 return airBlocks;
             }
         } else if (buttonDirection == BlockFace.SOUTH) {
             Block cornerBlock = signBlock.getRelative(BlockFace.WEST, 1);
             Block air1 = cornerBlock.getRelative(BlockFace.SOUTH, 1).getRelative(BlockFace.WEST, 1);
             Block air2 = cornerBlock.getRelative(BlockFace.SOUTH, 1).getRelative(BlockFace.WEST, 2);
             Block air3 = cornerBlock.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.WEST, 1);
             Block air4 = cornerBlock.getRelative(BlockFace.SOUTH, 2).getRelative(BlockFace.WEST, 2);
             if ((!active && isAir(air1) && isAir(air2) && isAir(air3) && isAir(air4)) || (active && isEnd(air1) && isEnd(air2) & isEnd(air3) && isEnd(air4))) {
                 Block[] airBlocks = {air1,air2,air3,air4};
                 return airBlocks;
             }
         } else if (buttonDirection == BlockFace.EAST) {
             Block cornerBlock = signBlock.getRelative(BlockFace.SOUTH, 1);
             Block air1 = cornerBlock.getRelative(BlockFace.EAST, 1).getRelative(BlockFace.SOUTH, 1);
             Block air2 = cornerBlock.getRelative(BlockFace.EAST, 1).getRelative(BlockFace.SOUTH, 2);
             Block air3 = cornerBlock.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.SOUTH, 1);
             Block air4 = cornerBlock.getRelative(BlockFace.EAST, 2).getRelative(BlockFace.SOUTH, 2);
             if ((!active && isAir(air1) && isAir(air2) && isAir(air3) && isAir(air4)) || (active && isEnd(air1) && isEnd(air2) & isEnd(air3) && isEnd(air4))) {
                 Block[] airBlocks = {air1,air2,air3,air4};
                 return airBlocks;
             }
         } else if (buttonDirection == BlockFace.WEST) {
             Block cornerBlock = signBlock.getRelative(BlockFace.NORTH, 1);
             Block air1 = cornerBlock.getRelative(BlockFace.WEST, 1).getRelative(BlockFace.NORTH, 1);
             Block air2 = cornerBlock.getRelative(BlockFace.WEST, 1).getRelative(BlockFace.NORTH, 2);
             Block air3 = cornerBlock.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.NORTH, 1);
             Block air4 = cornerBlock.getRelative(BlockFace.WEST, 2).getRelative(BlockFace.NORTH, 2);
             if ((!active && isAir(air1) && isAir(air2) && isAir(air3) && isAir(air4)) || (active && isEnd(air1) && isEnd(air2) & isEnd(air3) && isEnd(air4))) {
                 Block[] airBlocks = {air1,air2,air3,air4};
                 return airBlocks;
             }
         }
         return null;
     }
     
     private boolean isWWStairs(Block stairBlock) {
         if (isWW(stairBlock.getRelative(BlockFace.NORTH).getLocation()) || isWW(stairBlock.getRelative(BlockFace.EAST).getLocation()) || isWW(stairBlock.getRelative(BlockFace.SOUTH).getLocation()) || isWW(stairBlock.getRelative(BlockFace.WEST).getLocation())) {
             return true;
         }
         return false;
     }
     
     private void dropSign(Block sign) {
         sign.setTypeId(0);
         sign.getWorld().dropItem(sign.getLocation(), new ItemStack(323,1));
     }
     
     private boolean isStairs(Block block) {
         if (       block.getTypeId() == 53
                 || block.getTypeId() == 67
                 || block.getTypeId() == 108
                 || block.getTypeId() == 109
                 || block.getTypeId() == 114
                 || block.getTypeId() == 128
                 || block.getTypeId() == 134
                 || block.getTypeId() == 135
                 || block.getTypeId() == 136) {
             return true;
         }
         return false;
     }
     
     private boolean isAir(Block block) {
         if (block.getType() == Material.AIR) {
             return true;
         }
         return false;
     }
     
     private boolean isEnd(Block block) {
         if (block.getType() == Material.ENDER_PORTAL) {
             return true;
         }
         return false;
     }
     
     private boolean isWWButton(Block block, boolean active) {
         // Try NORTH
         Player player = getServer().getPlayer("ellbristow");
         Block attempt = block.getRelative(BlockFace.NORTH, 3);
         if (isWW(attempt.getLocation())) {
             if (hasWellLayout(attempt, active)) {
                 return true;
             }
         }
         //Try SOUTH
         attempt = block.getRelative(BlockFace.SOUTH, 3);
         if (isWW(attempt.getLocation())) {
             if (hasWellLayout(attempt, active)) {
                 return true;
             }
         }
         //Try EAST
         attempt = block.getRelative(BlockFace.EAST, 3);
         if (isWW(attempt.getLocation())) {
             if (hasWellLayout(attempt, active)) {
                 return true;
             }
         }
         //Try WEST
         attempt = block.getRelative(BlockFace.WEST, 3);
         if (isWW(attempt.getLocation())) {
             if (hasWellLayout(attempt, active)) {
                 return true;
             }
         }
         return false;
     }
     
     private void toggleWW(Block block) {
         String locations = getWWFromButton(block);
         if ("".equals(locations)) {
             return;
         }
         String[] locationArray = locations.split(";");
         String[] firstLoc = locationArray[0].split(":");
         Location firstLocation = new Location(block.getWorld(), Integer.parseInt(firstLoc[1]), Integer.parseInt(firstLoc[2]), Integer.parseInt(firstLoc[3]));
         if (block.getWorld().getBlockAt(firstLocation).getType() == Material.AIR || block.getWorld().getBlockAt(firstLocation).getType() == null) {
             // Turn On
             block.getWorld().getBlockAt(firstLocation).setTypeId(119);
             String[] thisLoc = locationArray[1].split(":");
             Location secondLocation = new Location(block.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
             block.getWorld().getBlockAt(secondLocation).setTypeId(119);
             thisLoc = locationArray[2].split(":");
             Location thirdLocation = new Location(block.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
             block.getWorld().getBlockAt(thirdLocation).setTypeId(119);
             thisLoc = locationArray[3].split(":");
             Location fourthLocation = new Location(block.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
             block.getWorld().getBlockAt(fourthLocation).setTypeId(119);
         } else {
             block.getWorld().getBlockAt(firstLocation).setTypeId(0);
             String[] thisLoc = locationArray[1].split(":");
             Location secondLocation = new Location(block.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
             block.getWorld().getBlockAt(secondLocation).setTypeId(0);
             thisLoc = locationArray[2].split(":");
             Location thirdLocation = new Location(block.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
             block.getWorld().getBlockAt(thirdLocation).setTypeId(0);
             thisLoc = locationArray[3].split(":");
             Location fourthLocation = new Location(block.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
             block.getWorld().getBlockAt(fourthLocation).setTypeId(0);
         }
     }
     
     private void togglePortalFromJump(Location loc) {
         ConfigurationSection section = portalConfig.getConfigurationSection(loc.getWorld().getName());
         if (section != null) {
             Object[] configKeys = section.getKeys(false).toArray();
             for (int i = 0; i < configKeys.length; i++) {
                 String key = configKeys[i].toString();
                 String locations = section.getString(key + ".location");
                 for (String location : locations.split(";")) {
                     if (location.equals(loc.getWorld().getName() + ":" + (int)Math.floor(loc.getX()) + ":" + (int)Math.floor(loc.getY()) + ":" + (int)Math.floor(loc.getZ()))) {
                         String[] locationArray = locations.split(";");
                         String[] firstLoc = locationArray[0].split(":");
                         Location firstLocation = new Location(loc.getWorld(), Integer.parseInt(firstLoc[1]), Integer.parseInt(firstLoc[2]), Integer.parseInt(firstLoc[3]));
                         loc.getWorld().getBlockAt(firstLocation).setTypeId(0);
                         String[] thisLoc = locationArray[1].split(":");
                         Location secondLocation = new Location(loc.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
                         loc.getWorld().getBlockAt(secondLocation).setTypeId(0);
                         thisLoc = locationArray[2].split(":");
                         Location thirdLocation = new Location(loc.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
                         loc.getWorld().getBlockAt(thirdLocation).setTypeId(0);
                         thisLoc = locationArray[3].split(":");
                         Location fourthLocation = new Location(loc.getWorld(), Integer.parseInt(thisLoc[1]), Integer.parseInt(thisLoc[2]), Integer.parseInt(thisLoc[3]));
                         loc.getWorld().getBlockAt(fourthLocation).setTypeId(0);
                         return;
                     }
                 }
             }
         }
     }
     
     private String getWWFromButton(Block block) {
         Block attempt = block.getRelative(BlockFace.NORTH, 3);
         if (isWW(attempt.getLocation())) {
             return portalConfig.getString(attempt.getWorld().getName() + "." + attempt.getX() + "_" + attempt.getY() + "_" + attempt.getZ() + ".location");
         }
         //Try SOUTH
         attempt = block.getRelative(BlockFace.SOUTH, 3);
         if (isWW(attempt.getLocation())) {
             return portalConfig.getString(attempt.getWorld().getName() + "." + attempt.getX() + "_" + attempt.getY() + "_" + attempt.getZ() + ".location");
         }
         //Try EAST
         attempt = block.getRelative(BlockFace.EAST, 3);
         if (isWW(attempt.getLocation())) {
             return portalConfig.getString(attempt.getWorld().getName() + "." + attempt.getX() + "_" + attempt.getY() + "_" + attempt.getZ() + ".location");
         }
         //Try WEST
         attempt = block.getRelative(BlockFace.WEST, 3);
         if (isWW(attempt.getLocation())) {
             return portalConfig.getString(attempt.getWorld().getName() + "." + attempt.getX() + "_" + attempt.getY() + "_" + attempt.getZ() + ".location");
         }
         return "";
     }
     
     private boolean delete(File folder) {
         if (folder.isDirectory()) {
             for (File f : folder.listFiles()) {
                 if (!delete(f)) return false;
             }
         }
         return folder.delete();
     }
     
     private File getWorldDataFolder(String worldname) {
         return new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getAbsolutePath() + File.separator + worldname);
     }
     
     public void forceWorldLoads() {
         Object[] configWorlds = portalConfig.getKeys(false).toArray();
         for (int x = 0; x < configWorlds.length; x++) {
             ConfigurationSection section = portalConfig.getConfigurationSection(configWorlds[x].toString());
             if (section != null) {
                 Object[] configKeys = section.getKeys(false).toArray();
                 if (!section.getName().contains("_nether") && !section.getName().contains("_the_end")) {
                     if (getServer().getWorld(section.getName()) == null) {
                         WorldCreator wc = makeWorld(section.getName(), null);
                         getServer().createWorld(wc);
                     }
                 }
                 for (int i = 0; i < configKeys.length; i++) {
                     String key = configKeys[i].toString();
                     String destination = section.getString(key + ".destination");
                    if (getServer().getWorld(destination) == null && destinations.get(destination) == null) {
                         WorldCreator wc = makeWorld(destination, null);
                         getServer().createWorld(wc);
                     }
                 }
             }
         }
     }
     
     private WorldCreator makeWorld(String worldName, CommandSender sender) {
         WorldCreator wc = new WorldCreator(worldName);
         wc.seed(new Random().nextLong());
         wc.environment(World.Environment.NORMAL);
         //wc.generator(this.getName(), sender);
         return wc;
     }
     
     private void loadPortals() {
         if (portalFile == null) {
             portalFile = new File(getDataFolder(),"portals.yml");
         }
         portalConfig = YamlConfiguration.loadConfiguration(portalFile);
     }
 	
     private FileConfiguration getPortals() {
         if (portalConfig == null) {
             loadPortals();
         }
         return portalConfig;
     }
 	
     private void savePortals() {
         if (portalConfig == null || portalFile == null) {
             return;
         }
         try {
             portalConfig.save(portalFile);
         } catch (IOException ex) {
             getLogger().log(Level.SEVERE, "Could not save " + portalFile, ex );
         }
     }
     
 }
