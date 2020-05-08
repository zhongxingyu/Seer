 package com.thepastimers.ChestProtect;
 
 import com.thepastimers.Coord.Coord;
 import com.thepastimers.Coord.CoordData;
 import com.thepastimers.Database.Database;
 import com.thepastimers.Permission.Permission;
 import com.thepastimers.Plot.Plot;
 import com.thepastimers.Plot.PlotData;
 import com.thepastimers.Plot.PlotPerms;
 import com.thepastimers.Worlds.Worlds;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.block.Hopper;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.InventoryPickupItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.material.Door;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: derp
  * Date: 10/7/12
  * Time: 4:54 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ChestProtect extends JavaPlugin implements Listener {
     Database database;
     Permission permission;
     Coord coord;
     Plot plot;
     Worlds worlds;
    int MAX_PROTECTIONS = 6;
 
     @Override
     public void onEnable() {
         getLogger().info("ChestProtect init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
 
         if (database == null) {
             getLogger().warning("Warning, unable to load Database plugin. Critical error.");
             getServer().broadcastMessage(ChatColor.RED + "ChestProtect plugin cannot connect to Database");
             getServer().broadcastMessage(ChatColor.RED + "To avoid theft all chests are under lockdown.");
         }
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
 
         if (permission == null) {
             getLogger().warning("Unable to connect to Permission plugin");
         }
 
         coord = (Coord)getServer().getPluginManager().getPlugin("Coord");
 
         if (coord == null) {
             getLogger().warning("Unable to connect to Coord plugin.");
         }
 
         plot = (Plot)getServer().getPluginManager().getPlugin("Plot");
 
         if (plot == null) {
             getLogger().warning("Unable to connect to Plot plugin.");
         }
 
         worlds = (Worlds)getServer().getPluginManager().getPlugin("Worlds");
         if (worlds == null) {
             getLogger().warning("Unable to load Worlds plugin. Some functionality may not be available.");
         }
 
         getLogger().info("Table info: ");
         getLogger().info(ProtectData.getTableInfo());
         ProtectData.refreshCache(database,getLogger());
         getLogger().info(ProtectPerm.getTableInfo());
 
         getLogger().info("ChestProtect init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("ChestProtect disable");
     }
 
     private ProtectData getProtection(int x, int y, int z, String world) {
         if (database == null || world == null) {
             return null;
         }
 
         ProtectData data = ProtectData.getProtectionAt(x,y,z,world);
 
         return data;
     }
 
     private int numberOfProtections(String player) {
         if (database == null || player == null) {
             return MAX_PROTECTIONS;
         }
 
         List<ProtectData> data = (List<ProtectData>)database.select(ProtectData.class,"owner = '" + database.makeSafe(player) + "'");
 
         return data.size();
     }
 
     public boolean isProtected(Block b) {
         if (canBeProtected(b)) {
             if (database == null) {
                 return true;
             }
 
             if (getProtection(b.getX(),b.getY(),b.getZ(),b.getWorld().getName()) != null) {
                 return true;
             } else {
                 if (plot != null) {
                     //getLogger().info("trying to figure out if block is protected");
                     PlotData pd = plot.plotAt(b.getLocation(),true);
                     if (pd == null) {
                         pd = plot.plotAt(b.getLocation());
                     }
                     //getLogger().info("" + pd);
                     if (pd != null && pd.isChestProtect()) {
                         return true;
                     }
                 }
             }
             return false;
         } else {
             return false;
         }
     }
 
     public boolean canUserAccess(Block b, String player) {
         if (isProtected(b)) {
             return true;
         }
 
         if (!canBeProtected(b)) return true;
 
 
         return true;
     }
 
     private ProtectData getProtectionById(int id) {
         if (database == null) {
             return null;
         }
 
         List<ProtectData> data = (List<ProtectData>)database.select(ProtectData.class,"id = " + id);
 
         if (data.size() == 0) {
             return null;
         }
 
         return data.get(0);
     }
 
     public boolean canBeProtected(Block b) {
         return (b.getType() == Material.CHEST || b.getType() == Material.WOOD_DOOR || b.getType() == Material.HOPPER
         || b.getType() == Material.FURNACE || b.getType() == Material.BURNING_FURNACE);
     }
 
     private ProtectData addProtection(Block b, Player owner) {
         if (b == null || owner == null) {
             return null;
         }
 
         if (!canBeProtected(b)) {
             owner.sendMessage("This block cannot be protected.");
             return null;
         }
 
         if (isProtected(b)) {
             owner.sendMessage("This block is already protected");
             return null;
         }
 
         int x = b.getX();
         int y = b.getY();
         int z = b.getZ();
         String world = b.getWorld().getName();
 
         owner.sendMessage("Attempting to protect chest at (" + x + "," + y + "," + z + ")");
         ProtectData pd = new ProtectData();
         pd.setWorld(world);
         pd.setX(x);
         pd.setY(y);
         pd.setZ(z);
         pd.setOwner(owner.getName());
 
         if (!pd.save(database)) {
             owner.sendMessage("Unable to create protection.");
             return null;
         } else {
             owner.sendMessage("Protection created");
             return pd;
         }
     }
 
     private boolean removeProtection(int x, int y, int z, String world) {
         if (database == null || world == null) {
             return false;
         }
 
         List<ProtectData> data = (List<ProtectData>)database.select(ProtectData.class,"x = " + x + " and y = " + y
                 + " and z = " + z + " and world = '" + database.makeSafe(world) + "'");
 
         if (data.size() == 0) {
             return false;
         }
 
         ProtectData d1 = data.get(0);
 
         return d1.delete(database);
     }
 
     public boolean hasPerms(Player p, Block b) {
         return hasPerms(p.getName(),b);
     }
 
     public boolean hasPerms(String player, Block b) {
         return hasPerms(player,b.getLocation());
     }
 
     public boolean hasPerms(String player, Location l) {
         return hasPerms(player,l.getX(), l.getY(),l.getZ(),l.getWorld().getName());
     }
 
     public boolean hasPerms(String player, double x, double y, double z, String world) {
         return hasPerms(player,(int)x,(int)y,(int)z,world);
     }
 
     public boolean hasPerms(String player, int x, int y, int z, String world) {
         if (database == null || player == null || world == null) {
             return false;
         }
 
         ProtectData data = getProtection(x,y,z,world);
 
         if (data == null) {
             if (plot != null) {
                 //getLogger().info("here");
                 PlotData pd = plot.plotAt(x,z,world,true);
                 if (pd == null) {
                     pd = plot.plotAt(x,z,world,false);
                 }
                 if (plot.getPlotPerms(pd,player) >= PlotPerms.RESIDENT) {
                     return true;
                 }
             }
         } else {
             //getLogger().info(data + " is not null");
 
             if (player.equalsIgnoreCase(data.getOwner())) {
                 return true;
             }
 
             List<ProtectPerm> perms = (List<ProtectPerm>)database.select(ProtectPerm.class,"player = '"
                     + database.makeSafe(player) + "' and protect = " + data.getId());
             if (perms.size() != 0) {
                 return true;
             }
         }
 
         return false;
     }
 
     private boolean removePerm(String player, ProtectData data) {
         if (database == null) {
             return false;
         }
 
         if (player == null || data == null) {
             return true;
         }
 
         List<ProtectPerm> perms = (List<ProtectPerm>)database.select(ProtectPerm.class,"player = '"
                 + database.makeSafe(player) + "' and protect = " + data.getId());
 
         if (perms.size() == 0) {
             return true;
         }
 
         return perms.get(0).delete(database);
     }
 
     private boolean addPerm(String player, ProtectData data) {
         if (database == null) {
             return false;
         }
 
         if (player == null || data == null) {
             return true;
         }
 
         if (hasPerms(player,data.getX(),data.getY(),data.getZ(),data.getWorld())) {
             return true;
         }
 
         ProtectPerm perm = new ProtectPerm();
         perm.setPlayer(player);
         perm.setProtect(data.getId());
 
         return perm.save(database);
     }
 
     @EventHandler
     public void playerDoSomething(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             Block b = event.getClickedBlock();
             Player p = event.getPlayer();
 
             if (isProtected(b)) {
                 if (!hasPerms(p.getName(),b.getX(),b.getY(),b.getZ(),b.getWorld().getName())) {
                     event.setCancelled(true);
                     p.sendMessage(ChatColor.RED + "You do not have permission to interact with this");
                 }
             }
         }
     }
 
     @EventHandler
     public void blockBreak(BlockBreakEvent event) {
         Block b = event.getBlock();
         if (isProtected(b)) {
             if (!hasPerms(event.getPlayer().getName(), b.getX(), b.getY(), b.getZ(), b.getWorld().getName())) {
                 event.setCancelled(true);
                 event.getPlayer().sendMessage(ChatColor.RED + "This block cannot be removed until its protection has been removed.");
             }
         }
     }
 
     @EventHandler
     public void tnt(EntityExplodeEvent event) {
         List<Block> blist = event.blockList();
 
         for (Block b : blist) {
             if (isProtected(b)) {
                 event.setCancelled(true);
                 return;
             }
         }
     }
 
     @EventHandler
     public void hopper(InventoryPickupItemEvent event) {
         Inventory inv =  event.getInventory();
         InventoryHolder ih = inv.getHolder();
         if (ih instanceof Chest) {
             Chest c = (Chest)ih;
             Block b = c.getBlock();
             if (isProtected(b) && (plot == null || plot.plotAt(b.getLocation()) == null)) {
                 event.setCancelled(true);
             }
         } else if (ih instanceof DoubleChest) {
             DoubleChest c = (DoubleChest)ih;
             Block b = ((Chest)c.getLeftSide()).getBlock();
             if (isProtected(b) && (plot == null || plot.plotAt(b.getLocation()) == null)) {
                 event.setCancelled(true);
             }
 
             b = ((Chest)c.getRightSide()).getBlock();
             if (isProtected(b) && (plot == null || plot.plotAt(b.getLocation()) == null)) {
                 event.setCancelled(true);
             }
         } else if (ih instanceof Hopper) {
             Hopper c = (Hopper)ih;
             Block b = c.getBlock();
             if (isProtected(b) && (plot == null || plot.plotAt(b.getLocation()) == null)) {
                 event.setCancelled(true);
             }
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         if (worlds != null && worlds.getPlayerWorldType(playerName) == Worlds.VANILLA) {
             return false;
         }
 
         String command = cmd.getName();
 
         if (command.equalsIgnoreCase("protect")) {
             if (permission == null || !permission.hasPermission(playerName,"protect_protect") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You don't have permission to use this command");
                 return true;
             }
             if (args.length > 0) {
                 String subCommand = args[0];
 
                 if (subCommand.equalsIgnoreCase("create")) {
                     if (permission == null || !permission.hasPermission(playerName,"protect_protectcreate")
                             || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage("You don't have permission to do this");
                         return true;
                     }
                     if (coord == null) {
                         sender.sendMessage("This functionality is not currently available.");
                         return true;
                     }
 
                     int protections = numberOfProtections(playerName);
                     if (protections >= MAX_PROTECTIONS) {
                         sender.sendMessage("You are already at the max protections. (" + protections + "/" + MAX_PROTECTIONS + ")");
                         return true;
                     }
 
                     List<CoordData> coords = coord.popCoords(playerName,1);
 
                     if (coords.size() < 1) {
                         sender.sendMessage("You must have 1 coordinate set to do this.");
                         return true;
                     }
 
                     CoordData coord = coords.get(0);
 
                     int x = (int)coord.getX();
                     int y = (int)coord.getY();
                     int z = (int)coord.getZ();
 
                     Player p = (Player)sender;
                     World w = p.getWorld();
 
                    if (!"economy".equalsIgnoreCase(w.getName())) {
                        sender.sendMessage(ChatColor.RED + "Chest protections can only be created in the economy world");
                         return true;
                     }
 
                     Block b = w.getBlockAt(x,y,z);
 
                     if (isProtected(b)) {
                         sender.sendMessage("This block is already protected.");
                         return true;
                     }
 
                     if (b.getType() == Material.CHEST) {
                         ProtectData pd = addProtection(b,p);
 
                         if (pd != null) {
                             ProtectData pd2 = null;
                             b = w.getBlockAt(x+1,y,z);
                             if (b.getType() == Material.CHEST) {
                                 pd2 = addProtection(b,p);
                                 if (pd2 != null) {
                                     pd.setLink(pd2.getId());
                                     pd2.setLink(pd.getId());
                                     pd2.save(database);
                                 }
                             }
                             b = w.getBlockAt(x-1,y,z);
                             if (b.getType() == Material.CHEST) {
                                 pd2 = addProtection(b,p);
                                 if (pd2 != null) {
                                     pd.setLink(pd2.getId());
                                     pd2.setLink(pd.getId());
                                     pd2.save(database);
                                 }
                             }
                             b = w.getBlockAt(x,y,z+1);
                             if (b.getType() == Material.CHEST) {
                                 pd2 = addProtection(b,p);
                                 if (pd2 != null) {
                                     pd.setLink(pd2.getId());
                                     pd2.setLink(pd.getId());
                                     pd2.save(database);
                                 }
                             }
                             b = w.getBlockAt(x,y,z-1);
                             if (b.getType() == Material.CHEST) {
                                 pd2 = addProtection(b,p);
                                 if (pd2 != null) {
                                     pd.setLink(pd2.getId());
                                     pd2.setLink(pd.getId());
                                     pd2.save(database);
                                 }
                             }
                             pd.save(database);
                         }
                     } else if (b.getType() == Material.WOOD_DOOR) {
                         Door d = (Door)b;
 
                         ProtectData pd = addProtection(b,p);
                         if (pd != null) {
                             int y2 = 0;
                             if (d.isTopHalf()) {
                                 y2 = -1;
                             } else {
                                 y2 = 1;
                             }
 
                             b = w.getBlockAt(x,y+y2,z);
 
                             if (b.getType() == Material.WOOD_DOOR) {
                                 ProtectData pd2 = addProtection(b,p);
                                 if (pd2 != null) {
                                     pd.setLink(pd2.getId());
                                     pd2.setLink(pd.getId());
                                     pd2.save(database);
                                 }
                             }
                             pd.save(database);
                         }
                     }
                 } else if (subCommand.equalsIgnoreCase("remove")) {
                     if (permission == null || !permission.hasPermission(playerName,"protect_protectcreate")
                             || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage("You don't have permission to do this");
                         return true;
                     }
                     if (coord == null) {
                         sender.sendMessage("This functionality is not currently available.");
                         return true;
                     }
 
                     List<CoordData> coords = coord.popCoords(playerName,1);
 
                     if (coords.size() < 1) {
                         sender.sendMessage("You must have 1 coordinate set to do this.");
                         return true;
                     }
 
                     CoordData coord = coords.get(0);
 
                     int x = (int)coord.getX();
                     int y = (int)coord.getY();
                     int z = (int)coord.getZ();
 
                     Player p = (Player)sender;
                     World w = p.getWorld();
 
                     Block b = w.getBlockAt(x,y,z);
 
                     if (!isProtected(b)) {
                         sender.sendMessage("This block is not protected.");
                         return true;
                     }
 
                     ProtectData pd = getProtection(x,y,z,w.getName());
 
                     if (!pd.getOwner().equalsIgnoreCase(playerName) && !permission.hasPermission("override",playerName)) {
                         sender.sendMessage("You do not have permission to modify this protection.");
                         return true;
                     }
 
                     // TODO: Remove all permissions on this protection here
 
                     if (!removeProtection(x,y,z,w.getName())) {
                         sender.sendMessage(ChatColor.RED + "Unable to remove protection.");
                     } else {
                         sender.sendMessage("Protection removed.");
                     }
 
                     if (pd.getLink() != -1) {
                         ProtectData pd2 = getProtectionById(pd.getLink());
 
                         if (!removeProtection(pd2.getX(),pd2.getY(),pd2.getZ(),pd2.getWorld())) {
                             sender.sendMessage(ChatColor.RED + "Unable to remove protection.");
                         } else {
                             sender.sendMessage("Protection removed.");
                         }
                     }
                 } else if (subCommand.equalsIgnoreCase("info")) {
                     if (permission == null || !permission.hasPermission(playerName,"protect_protectinfo")
                             || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage("You don't have permission to do this");
                         return true;
                     }
                     if (coord == null) {
                         sender.sendMessage("This functionality is not currently available.");
                         return true;
                     }
 
                     List<CoordData> coords = coord.popCoords(playerName,1);
 
                     if (coords.size() < 1) {
                         sender.sendMessage("You must have 1 coordinate set to do this.");
                         return true;
                     }
 
                     CoordData coord = coords.get(0);
 
                     int x = (int)coord.getX();
                     int y = (int)coord.getY();
                     int z = (int)coord.getZ();
 
                     Player p = (Player)sender;
                     World w = p.getWorld();
 
                     Block b = w.getBlockAt(x,y,z);
 
                     if (!isProtected(b) && b != null) {
                         sender.sendMessage("This block is not protected.");
                         return true;
                     }
 
                     ProtectData pd = getProtection(x,y,z,w.getName());
                     if (pd == null) {
                         sender.sendMessage("This block is not protected");
                         return true;
                     }
 
                     sender.sendMessage("This " + b.getType().name().toLowerCase() + " is protected by " + pd.getOwner());
                 } else if (subCommand.equalsIgnoreCase("allow")) {
                     if (permission == null || !permission.hasPermission(playerName,"protect_protectallow")
                             || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage("You don't have permission to do this");
                         return true;
                     }
 
                     if (args.length > 1) {
                         String player = args[1];
 
                         if (coord == null) {
                             sender.sendMessage("This functionality is not currently available.");
                             return true;
                         }
 
                         List<CoordData> coords = coord.popCoords(playerName,1);
 
                         if (coords.size() < 1) {
                             sender.sendMessage("You must have 1 coordinate set to do this.");
                             return true;
                         }
 
                         CoordData coord = coords.get(0);
 
                         int x = (int)coord.getX();
                         int y = (int)coord.getY();
                         int z = (int)coord.getZ();
 
                         Player p = (Player)sender;
                         World w = p.getWorld();
 
                         Block b = w.getBlockAt(x,y,z);
 
                         if (!isProtected(b)) {
                             sender.sendMessage("This block is not protected.");
                             return true;
                         }
 
                         ProtectData pd = getProtection(x,y,z,w.getName());
 
                         if (!pd.getOwner().equalsIgnoreCase(playerName)) {
                             sender.sendMessage("You don't have permission to modify this protection.");
                             return true;
                         }
 
                         if (!addPerm(player,pd)) {
                             sender.sendMessage("Unable to allow access");
                         } else {
                             sender.sendMessage(player + " now has access to this " + b.getType().name().toLowerCase());
                         }
 
                         if (pd.getLink() != -1) {
                             ProtectData pd2 = getProtectionById(pd.getLink());
                             if (!addPerm(player,pd2)) {
                                 sender.sendMessage("Unable to allow access");
                             } else {
                                 sender.sendMessage(player + " now has access to this " + b.getType().name().toLowerCase());
                             }
                         }
                     } else {
                         sender.sendMessage("/protect allow <player>");
                     }
                 } else if (subCommand.equalsIgnoreCase("deny")) {
                     if (permission == null || !permission.hasPermission(playerName,"protect_protectallow")
                             || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage("You don't have permission to do this");
                         return true;
                     }
 
                     if (args.length > 1) {
                         String player = args[1];
 
                         if (coord == null) {
                             sender.sendMessage("This functionality is not currently available.");
                             return true;
                         }
 
                         List<CoordData> coords = coord.popCoords(playerName,1);
 
                         if (coords.size() < 1) {
                             sender.sendMessage("You must have 1 coordinate set to do this.");
                             return true;
                         }
 
                         CoordData coord = coords.get(0);
 
                         int x = (int)coord.getX();
                         int y = (int)coord.getY();
                         int z = (int)coord.getZ();
 
                         Player p = (Player)sender;
                         World w = p.getWorld();
 
                         Block b = w.getBlockAt(x,y,z);
 
                         if (!isProtected(b)) {
                             sender.sendMessage("This block is not protected.");
                             return true;
                         }
 
                         ProtectData pd = getProtection(x,y,z,w.getName());
 
                         if (!pd.getOwner().equalsIgnoreCase(playerName)) {
                             sender.sendMessage("You don't have permission to modify this protection.");
                             return true;
                         }
 
                         if (!removePerm(player,pd)) {
                             sender.sendMessage("Unable to deny access");
                         } else {
                             sender.sendMessage(player + " no longer has access to this " + b.getType().name().toLowerCase());
                         }
 
                         if (pd.getLink() != -1) {
                             ProtectData pd2 = getProtectionById(pd.getLink());
                             if (!removePerm(player,pd2)) {
                                 sender.sendMessage("Unable to deny access");
                             } else {
                                 sender.sendMessage(player + " no longer has access to this " + b.getType().name().toLowerCase());
                             }
                         }
                     } else {
                         sender.sendMessage("/protect deny <player>");
                     }
                 } else if (subCommand.equalsIgnoreCase("list")) {
                     if (permission == null || !permission.hasPermission(playerName,"protect_protectcreate")
                             || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage("You don't have permission to do this");
                         return true;
                     }
 
                     List<ProtectData> dataList = (List<ProtectData>) database.select(ProtectData.class,"owner = '" + sender.getName() + "'");
 
                     sender.sendMessage("Your protections:");
                     for (ProtectData data : dataList) {
                         sender.sendMessage(data.getWorld() + ": (" + data.getX() + "," + data.getY() + "," + data.getZ() + ")");
                     }
                 }
             } else {
                 sender.sendMessage("/protect <create|remove|allow|deny|info|list>");
             }
         } else {
             return false;
         }
 
         return true;
     }
 }
