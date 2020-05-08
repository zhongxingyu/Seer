 package com.thepastimers.Plot;
 
 import com.thepastimers.Coord.Coord;
 import com.thepastimers.Coord.CoordData;
 import com.thepastimers.Database.Database;
 import com.thepastimers.Logger.Logger;
 import com.thepastimers.Money.Money;
 import com.thepastimers.Permission.Permission;
 import com.thepastimers.ChestProtect.ChestProtect;
 import com.thepastimers.Worlds.Worlds;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.potion.Potion;
 
 import java.lang.reflect.Method;
 import java.util.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: derp
  * Date: 10/4/12
  * Time: 7:43 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Plot extends JavaPlugin implements Listener {
     Database database;
     Permission permission;
     Coord coord;
     Money money;
     Worlds worlds;
     Logger logger;
     Map<Class,Map<JavaPlugin,Integer>> plotEnterListener;
     Map<Class,Map<JavaPlugin,Integer>> plotLeaveListener;
 
     @Override
     public void onEnable() {
         getLogger().info("Plot init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
 
         if (database == null) {
             getLogger().warning("Warning! Unable to load Database module! Critical failure!");
             getServer().broadcastMessage(ChatColor.RED + "Warning, Plot plugin was unable to connect to Database.");
             getServer().broadcastMessage(ChatColor.RED + "To prevent griefing of protected plots, server is entering lockdown.");
         } else {
             PlotData.createTables(database,getLogger());
             PlotPerms.createTables(database,getLogger());
         }
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
         if (permission == null) {
             getLogger().warning("Unable to load Permission module. Some functionality may not be available.");
         }
 
         coord = (Coord)getServer().getPluginManager().getPlugin("Coord");
         if (coord == null) {
             getLogger().warning("Unable to load Coord plugin. Some functionality may not be available.");
         }
 
         money = (Money)getServer().getPluginManager().getPlugin("Money");
         if (money == null) {
             getLogger().warning("Unable to load Money plugin. some functionality may not be available.");
         }
 
         worlds = (Worlds)getServer().getPluginManager().getPlugin("Worlds");
         if (worlds == null) {
             getLogger().warning("Unable to load Worlds plugin. Some functionality may not be available.");
         }
 
         logger = (Logger)getServer().getPluginManager().getPlugin("Logger");
         if (logger == null) {
             getLogger().warning("Unable to load Logger plugin. Some functionality may not be available.");
         }
 
         plotEnterListener = new HashMap<Class, Map<JavaPlugin,Integer>>();
         plotLeaveListener = new HashMap<Class, Map<JavaPlugin,Integer>>();
 
         getLogger().info("Table info: ");
         //getLogger().info(PlotData.getTableInfo());
         PlotData.refreshCache(database,getLogger());
         //getLogger().info(PlotPerms.getTableInfo());
         PlotPerms.refreshCache(database,getLogger());
         //PlotRent.autoPopulate = true;
         //database.select(PlotRent.class,"1");
         //getLogger().info(PlotRent.getTableInfo());
 
         getLogger().info("Plot init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Plot disable");
     }
 
     public PlotData plotAt(Player p, boolean subPlot) {
         if (p == null) {
             return null;
         }
 
         return plotAt(p.getLocation(),subPlot);
     }
 
     public PlotData plotAt(Location l) {
         PlotData pd = plotAt(l,true);
         if (pd == null) {
             pd = plotAt(l,false);
         }
 
         return pd;
     }
 
     public PlotData plotAt(Location l, boolean subPlot) {
         if (l == null) {
             return null;
         }
 
         return plotAt(l.getX(),l.getZ(),l.getWorld().getName(),subPlot);
     }
 
     public PlotData plotAt(double x, double z, String world, boolean subPlot) {
         return plotAt((int)x,(int)z,world, subPlot);
     }
 
     public PlotData plotAt(int x, int z, String world, boolean subPlot) {
         PlotData pd = PlotData.getPlotAtLocation(x,z,world,subPlot);
 
         return pd;
     }
 
     public boolean plotIntersect(PlotData p1, PlotData p2) {
         if (p1 == null || p2 == null) {
             return false;
         }
 
         if (!p1.getWorld().equalsIgnoreCase(p2.getWorld()) || p1.isSubPlot() != p2.isSubPlot()) {
             return false;
         }
 
         return (plotIntersect(p1,p2.getX1(),p2.getZ1()) || plotIntersect(p1,p2.getX2(),p2.getZ1())
                 || plotIntersect(p1,p2.getX2(),p2.getZ2()) || plotIntersect(p1,p2.getX1(),p2.getZ2()));
     }
 
     public boolean plotIntersect(PlotData p1, int x, int z) {
         if (p1 == null) {
             return false;
         }
 
         return (p1.getX1() <= x && p1.getX2() >= x && p1.getZ1() <= z && p1.getZ2() >= z);
     }
 
     public boolean plotIntersect(int x1, int z1, int x2, int z2, String world, boolean subPlot) {
         if (database == null) {
             return true;
         }
 
         if (plotAt(x1,z1,world,subPlot) != null || plotAt(x1,z2,world,subPlot) != null
                 || plotAt(x2,z2,world,subPlot) != null || plotAt(x2,z1,world,subPlot) != null) {
             return true;
         }
 
         PlotData tmp = new PlotData();
         tmp.setWorld(world);
         tmp.setX1(x1);
         tmp.setX2(x2);
         tmp.setZ1(z1);
         tmp.setZ2(z2);
 
         List<PlotData> data = PlotData.getAllPlotsInWorld(world,subPlot);
 
         for (PlotData pd : data) {
             if (plotIntersect(tmp,pd)) {
                 return true;
             }
         }
 
         return false;
     }
 
     public int getPlotPerms(PlotData pd, String player) {
         if (database == null) {
             return PlotPerms.NONE;
         }
 
         if (pd == null || player == null) {
             return PlotPerms.RESIDENT;
         }
 
         if (pd.getOwner().equalsIgnoreCase(player)) {
             return PlotPerms.OWNER;
         }
 
         PlotPerms permObj = getPlotPermobject(pd,player);
 
         if (permObj == null) {
             return PlotPerms.NONE;
         }
 
         int perm = permObj.getPerm();
 
         return perm;
     }
 
     public PlotPerms getPlotPermobject(PlotData pd, String player) {
         if (database == null) {
             return null;
         }
 
         if (pd == null || player == null) {
             return null;
         }
 
         if (pd.getOwner().equalsIgnoreCase(player)) {
             PlotPerms pp = new PlotPerms();
             pp.setPlot(pd.getId());
             pp.setPerm(PlotPerms.OWNER);
             pp.setPlayer(player);
             return pp;
         }
 
         PlotPerms pp = PlotPerms.getPermsForPlotAndPlayer(pd.getId(),player);
 
         return pp;
     }
 
     public int translatePerm(String perm) {
         if (perm.equalsIgnoreCase("owner")) {
             return PlotPerms.OWNER;
         } else if (perm.equalsIgnoreCase("coowner")) {
             return PlotPerms.COOWNER;
         } else if (perm.equalsIgnoreCase("resident")) {
             return PlotPerms.RESIDENT;
         } else if (perm.equalsIgnoreCase("worker")) {
             return PlotPerms.WORKER;
         } else {
             return PlotPerms.NONE;
         }
     }
 
     public boolean canModifyBlock(Player player, Block block) {
         if (block == null || player == null) {
             return false;
         }
         return canModifyBlock(player.getName(),block);
     }
 
     public boolean canModifyBlock(String player, Block block) {
         if (block == null || player == null) {
             return false;
         }
         return canModifyBlock(player,block.getType(),plotAt(block.getLocation()));
     }
 
     public boolean canModifyBlock(String player, Material material, PlotData pd) {
         if (material == null || player == null) {
             return false;
         }
 
         if (pd == null) {
             return true;
         }
 
         int perm = getPlotPerms(pd,player);
 
         if (perm >= PlotPerms.RESIDENT) {
             return true;
         }
 
         if (perm == PlotPerms.WORKER) {
             return (material == Material.WHEAT || material == Material.PUMPKIN || material == Material.MELON_BLOCK
                     || material == Material.SUGAR_CANE_BLOCK);
         }
 
         return false;
     }
 
     public boolean givePerms(String player, PlotData pd, int perms) {
         if (database == null) {
             return false;
         }
 
         PlotPerms pp = getPlotPermobject(pd,player);
 
         if (pp != null) {
             if (pp.getPerm() == PlotPerms.OWNER) {
                 return false;
             }
         } else {
             pp = new PlotPerms();
             pp.setPlot(pd.getId());
             pp.setPlayer(player);
         }
 
         pp.setPerm(perms);
 
         return pp.save(database);
     }
 
     public boolean removePerms(String player, PlotData pd) {
         if (database == null) {
             return false;
         }
 
         PlotPerms pp = getPlotPermobject(pd,player);
 
         if (pp != null) {
             if (pp.getPerm() == PlotPerms.OWNER) {
                 return false;
             }
         } else {
             return true;
         }
 
         return pp.delete(database);
     }
 
     public void registerPlotEnter(Class c, JavaPlugin plugin) {
         registerPlotEnter(c,plugin,0);
     }
 
     public void registerPlotEnter(Class c, JavaPlugin plugin, int offset) {
         Map<JavaPlugin,Integer> blah = new HashMap<JavaPlugin, Integer>();
         blah.put(plugin,offset);
         plotEnterListener.put(c,blah);
     }
 
     public void registerPlotLeave(Class c, JavaPlugin plugin) {
         registerPlotLeave(c, plugin, 0);
     }
 
     public void registerPlotLeave(Class c, JavaPlugin plugin, int offset) {
         Map<JavaPlugin,Integer> blah = new HashMap<JavaPlugin, Integer>();
         blah.put(plugin,offset);
         if (plotLeaveListener == null) {
             plotLeaveListener = new HashMap<Class, Map<JavaPlugin,Integer>>();
         }
         plotLeaveListener.put(c,blah);
     }
 
     @EventHandler
     public void chestInteract(PlayerInteractEvent event) {
         ChestProtect protect = (ChestProtect)getServer().getPluginManager().getPlugin("ChestProtect");
         if (protect != null) return;
         Player pl = event.getPlayer();
         Block bl = event.getClickedBlock();
        if (bl == null) return;
         PlotData pd = plotAt(bl.getLocation());
 
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             bl = event.getClickedBlock();
             pl = event.getPlayer();
             if (pd != null) {
                 if (!pl.getName().equalsIgnoreCase(pd.getOwner())) {
                     PlotPerms pp = getPlotPermobject(pd,pl.getName());
                     if (pp == null || pp.getPerm() < PlotPerms.RESIDENT) {
                         pl.sendMessage(ChatColor.RED + "You do not have permissions interact with this");
                         event.setCancelled(true);
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void blockBreak(BlockBreakEvent event) {
         if (logger != null) {
             logger.logEvent("plot_BlockBreakEvent");
         }
         Player p = event.getPlayer();
         Block b = event.getBlock();
 
         if (!canModifyBlock(p,b)) {
             p.sendMessage(ChatColor.RED + "You do not have permissions to modify blocks here");
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void paintingBreak(HangingBreakByEntityEvent event) {
         if (logger != null) {
             logger.logEvent("plot_HangingBreakByEntityEvent");
         }
         Entity b = event.getEntity();
 
         Player pl = null;
         if (event.getRemover() instanceof Player) {
             pl = (Player)event.getRemover();
         }
 
         PlotData p = plotAt(b.getLocation());
 
         if (p != null) {
             if (!pl.getName().equalsIgnoreCase(p.getOwner())) {
                 PlotPerms pp = getPlotPermobject(p,pl.getName());
                 if (pp == null || pp.getPerm() < PlotPerms.RESIDENT) {
                     pl.sendMessage(ChatColor.RED + "You do not have permissions to modify hangings here");
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler
     public void blockPlace(BlockPlaceEvent event) {
         if (logger != null) {
             logger.logEvent("plot_BlockPlaceEvent");
         }
         Player p = event.getPlayer();
         Block b = event.getBlock();
 
         if (!canModifyBlock(p,b)) {
             p.sendMessage(ChatColor.RED + "You do not have permissions to modify blocks here");
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void liquid(PlayerBucketEmptyEvent event) {
         if (logger != null) {
         logger.logEvent("plot_PlayerBucketEmptyEvent");
     }
         Player p = event.getPlayer();
         Block b = event.getBlockClicked();
 
         if (!canModifyBlock(p,b)) {
             p.sendMessage(ChatColor.RED + "You do not have permissions to modify blocks here");
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void liquidMoved(BlockFromToEvent event) {
         if (logger != null) {
             logger.logEvent("plot_BlockFromToEvent");
         }
         Location l1 = event.getBlock().getLocation();
         Location l2 = event.getToBlock().getLocation();
 
         PlotData p1 = plotAt(l1);
         PlotData p2 = plotAt(l2);
 
         if ((p1 == null && p2 != null) || (p2 == null && p1 != null)) {
             event.setCancelled(true);
             return;
         }
 
         if (p1 == null && p2 == null) {
             return;
         }
 
         if (p1.getId() != p2.getId()) {
             event.setCancelled(true);
             return;
         }
     }
 
     /*@EventHandler
     public void damageNormal(EntityDamageEvent event) {
         Entity damaged = event.getEntity();
         if (event instanceof EntityDamageByEntityEvent) {
             // handled in another function
             return;
         } else if(damaged instanceof Cow || damaged instanceof Pig || damaged instanceof Sheep
                 || damaged instanceof Chicken || damaged instanceof Horse || damaged instanceof MushroomCow || "CraftAnimals".equalsIgnoreCase(damaged.toString())) {
                 event.setCancelled(true);
         }
     }*/
 
     /*@EventHandler
     public void playerMove(PlayerMoveEvent event) {
         if (logger != null) {
             logger.logEvent("plot_PlayerMoveEvent");
         }
         handleMove(event.getFrom(),event.getTo(),event.getPlayer());
     }*/
 
     private boolean handleMove(Location l, Location l2, Player p) {
         for (Class c : plotEnterListener.keySet()) {
             try {
                 Map<JavaPlugin,Integer> blah = plotEnterListener.get(c);
                 JavaPlugin plugin = (JavaPlugin)blah.keySet().toArray()[0];
                 int offset = blah.get(plugin);
 
                 PlotData p1 = PlotData.getPlotAtLocation(l, true, offset);
                 if (p1 == null) p1 = PlotData.getPlotAtLocation(l, false, offset);
                 PlotData p2 = PlotData.getPlotAtLocation(l2, true, offset);
                 if (p2 == null) p2 = PlotData.getPlotAtLocation(l2, false, offset);
 
                 if (p2 != null && p1 == null) {
                     getLogger().info("here");
                     Class[] argTypes = new Class[] {PlotData.class,Player.class};
                     Method m = c.getDeclaredMethod("handlePlotEnter",argTypes);
                     m.invoke(plugin,p2,p);
                 }
             } catch (Exception e) {
                 getLogger().warning("Unable to call handlePlotEnter for " + c.getName());
             }
         }
 
         for (Class c : plotLeaveListener.keySet()) {
             try {
                 Map<JavaPlugin,Integer> blah = plotLeaveListener.get(c);
                 JavaPlugin plugin = (JavaPlugin)blah.keySet().toArray()[0];
                 int offset = blah.get(plugin);
 
                 PlotData p1 = PlotData.getPlotAtLocation(l, true, offset);
                 if (p1 == null) p1 = PlotData.getPlotAtLocation(l, false, offset);
                 PlotData p2 = PlotData.getPlotAtLocation(l2, true, offset);
                 if (p2 == null) p2 = PlotData.getPlotAtLocation(l2, false, offset);
 
                 if (p1 != null && p2 == null) {
                     Class[] argTypes = new Class[] {PlotData.class,Player.class};
                     Method m = c.getDeclaredMethod("handlePlotLeave",argTypes);
                     m.invoke(plugin,p1,p);
                 }
             } catch (Exception e) {
                 getLogger().warning("Unable to call handlePlotLeave for " + c.getName());
                 e.printStackTrace();
             }
         }
 
         PlotData p1 = plotAt(l,true);
         if (p1 == null) p1 = PlotData.getPlotAtLocation(l, false, 0);
         PlotData p2 = plotAt(l2,true);
         if (p2 == null) p2 = PlotData.getPlotAtLocation(l2, false, 0);
 
         if (p2 != null && p1 == null) {
             p.sendMessage(ChatColor.GREEN + "You are now entering " + p2.getName());
             if (p2.isCreative() && getPlotPerms(p2,p.getName()) > PlotPerms.NONE) {
                 p.setGameMode(GameMode.CREATIVE);
             }
         } else if (p1 != null && p2 == null) {
             p.sendMessage(ChatColor.GREEN + "You are now leaving " + p1.getName());
             if (p1.isCreative()) {
                 if (permission == null || !permission.hasPermission(p.getName(),"creative_all")) {
                     PlayerInventory inv = p.getInventory();
                     ItemStack[] itemList = inv.getContents();
 
                     for (int i=0;i<itemList.length;i++) {
                         itemList[i] = null;
                     }
                     inv.setContents(itemList);
 
                     itemList = inv.getArmorContents();
 
                     for (int i=0;i<itemList.length;i++) {
                         itemList[i] = null;
                     }
                     inv.setArmorContents(itemList);
                 }
                 p.setGameMode(GameMode.SURVIVAL);
             }
         } else if (!(p1 == null && p2 == null)) {
             if (p1.getId() != p2.getId()) {
                 p.sendMessage(ChatColor.GREEN + "You are now leaving " + p1.getName() + " and entering " + p2.getName());
                 if (p1.isCreative() && !p2.isCreative()) {
                     p.setGameMode(GameMode.SURVIVAL);
                 } else if (!p1.isCreative() && p2.isCreative()) {
                     p.setGameMode(GameMode.CREATIVE);
                 }
 
                 for (Class c : plotLeaveListener.keySet()) {
                     try {
                         Map<JavaPlugin,Integer> blah = plotLeaveListener.get(c);
                         JavaPlugin plugin = (JavaPlugin)blah.keySet().toArray()[0];
 
                         Class[] argTypes = new Class[] {PlotData.class,Player.class};
                         Method m = c.getDeclaredMethod("handlePlotLeave",argTypes);
                         m.invoke(plugin,p1,p);
                     } catch (Exception e) {
                         getLogger().warning("Unable to call handlePlotLeave for " + c.getName());
                     }
                 }
 
                 for (Class c : plotEnterListener.keySet()) {
                     try {
                         Map<JavaPlugin,Integer> blah = plotEnterListener.get(c);
                         JavaPlugin plugin = (JavaPlugin)blah.keySet().toArray()[0];
 
                         Class[] argTypes = new Class[] {PlotData.class,Player.class};
                         Method m = c.getDeclaredMethod("handlePlotEnter",argTypes);
                         m.invoke(plugin,p2,p);
                     } catch (Exception e) {
                         getLogger().warning("Unable to call handlePlotEnter for " + c.getName());
                     }
                 }
             }
         }
 
         return true;
     }
 
     /*@EventHandler
     public void playerTeleport(PlayerTeleportEvent event) {
         if (logger != null) {
             logger.logEvent("plot_PlayerTeleportEvent");
         }
         handleMove(event.getFrom(),event.getTo(),event.getPlayer());
     }*/
 
     @EventHandler
     public void damage(EntityDamageByEntityEvent event) {
         if (logger != null) {
             logger.logEvent("plot_EntityDamageByEntityEvent");
         }
         Entity damaged  = event.getEntity();
         Entity damager = event.getDamager();
 
         PlotData pd = plotAt(damaged.getLocation());
 
         if (pd == null) {
             return;
         }
 
         if (damaged instanceof Player) {
             if (damager instanceof Player) {
                 if (!pd.isPvp()) {
                     event.setCancelled(true);
                 }
             } else if (damager instanceof Arrow) {
                 Arrow a = (Arrow)damager;
                 if (a.getShooter() instanceof Player) {
                     if (!pd.isPvp()) {
                         event.setCancelled(true);
                     }
                 } else if (!pd.isPve()) {
                     event.setCancelled(true);
                 }
             } else if (damager instanceof Potion && !pd.isPvp()) {
                 event.setCancelled(true);
             } else if (!pd.isPve()) {
                 event.setCancelled(true);
             }
         } else if(damaged instanceof Cow || damaged instanceof Pig || damaged instanceof Sheep
                 || damaged instanceof Chicken || damaged instanceof Horse || damaged instanceof MushroomCow || "CraftAnimals".equalsIgnoreCase(damaged.toString())) {
             if (damager instanceof Player) {
                 Player p = (Player)damager;
                 int perms = getPlotPerms(pd,p.getName());
 
                 if (perms == PlotPerms.NONE) {
                     event.setCancelled(true);
                 }
             } else {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler
     public void death(PlayerDeathEvent event) {
         if (logger != null) {
             logger.logEvent("plot_PlayerDeathEvent");
         }
         Player p = event.getEntity();
         Location l = p.getLocation();
 
         for (Class c : plotLeaveListener.keySet()) {
             try {
                 Map<JavaPlugin,Integer> blah = plotLeaveListener.get(c);
                 JavaPlugin plugin = (JavaPlugin)blah.keySet().toArray()[0];
                 int offset = blah.get(plugin);
 
                 PlotData p1 = PlotData.getPlotAtLocation(l, true, offset);
                 if (p1 == null) p1 = PlotData.getPlotAtLocation(l, false, offset);
 
                 if (p1 != null) {
                     Class[] argTypes = new Class[] {PlotData.class,Player.class};
                     Method m = c.getDeclaredMethod("handlePlotLeave",argTypes);
                     m.invoke(plugin,p1,p);
                 }
             } catch (Exception e) {
                 getLogger().warning("Unable to call handlePlotLeave for " + c.getName());
                 e.printStackTrace();
             }
         }
     }
 
     @EventHandler
     public void normalDamage(EntityDamageEvent event) {
         if (logger != null) {
             logger.logEvent("plot_EntityDamageEvent");
         }
         if (event.getEntityType() != EntityType.PLAYER) {
             return;
         }
 
         if (event instanceof EntityDamageByEntityEvent) {
             return;
         }
 
         PlotData pd = plotAt(event.getEntity().getLocation());
 
         if (pd == null) {
             return;
         }
 
         if (!pd.isPve()) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void spawn(CreatureSpawnEvent event) {
         EntityType type = event.getEntityType();
         PlotData pd = plotAt(event.getEntity().getLocation());
 
         if (pd == null) {
             return;
         }
 
         if (type == EntityType.BLAZE || type == EntityType.CAVE_SPIDER || type ==  EntityType.CREEPER
                 || type ==  EntityType.ENDERMAN || type ==  EntityType.GHAST || type == EntityType.MAGMA_CUBE
                 || type ==  EntityType.PIG_ZOMBIE || type ==  EntityType.SILVERFISH || type ==  EntityType.SKELETON
                 || type == EntityType.SLIME || type ==  EntityType.SPIDER || type ==  EntityType.ZOMBIE) {
             if (!pd.isPve()) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler
     public void explode(EntityExplodeEvent event) {
         //getLogger().info("Explosion!");
         Location l = event.getLocation();
 
         PlotData plot = plotAt(l);
 
         List<Block> blockList = event.blockList();
         Iterator itor = blockList.iterator();
 
         //getLogger().info(blockList.size() + " were damaged");
 
         while (itor.hasNext()) {
             Block b = (Block)itor.next();
 
             PlotData p = plotAt(b.getLocation());
             if (p != null) {
                 itor.remove();
             }
         }
         //getLogger().info("Now have " + blockList.size());
 
         if (plot != null) {
             if (event.getEntity() instanceof TNTPrimed) {
                     event.setCancelled(true);
             }
             if (event.getEntity() instanceof Creeper && !plot.isPve()) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler
     public void fireSpread(BlockSpreadEvent event) {
         Location l = event.getBlock().getLocation();
 
         PlotData plot = plotAt(l);
         if (plot != null && !plot.isPve()) {
             if (event.getSource().getType() == Material.FIRE) {
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
 
         if (command.equalsIgnoreCase("plot") || command.equalsIgnoreCase("subplot")) {
             if (permission == null || !permission.hasPermission(playerName,"plot_plot") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You don't have permissions for this command (plot_plot)");
                 return true;
             }
 
             boolean subPlot = false;
             PlotData parent = null;
             String subCommand = "";
 
             if (args.length > 0) {
                 subCommand = args[0];
             }
 
             if (command.equalsIgnoreCase("subplot")) {
                 subPlot = true;
                 Player p = (Player)sender;
                 parent = plotAt(p.getLocation());
                 if (parent == null) {
                     sender.sendMessage("You cannot set a subplot unless you are inside a plot");
                     return true;
                 }
 
                 int perms = getPlotPerms(parent,playerName);
                 if (perms < PlotPerms.COOWNER && !"info".equalsIgnoreCase(subCommand)) {
                     sender.sendMessage("You don't have permission to use that command here");
                     return true;
                 }
             }
 
             if (args.length > 0) {
                 subCommand = args[0];
 
                 if (subCommand.equalsIgnoreCase("create") || subCommand.equalsIgnoreCase("check")) {
                     Player p = (Player)sender;
                     if (!permission.hasPermission(playerName,"plot_plotcreate")) {
                         sender.sendMessage("You don't have permissions for this command (plot_plotcreate)");
                         return true;
                     }
                     if (coord == null)  {
                         sender.sendMessage("This functionality is not currently available.");
                         return true;
                     }
 
                     if (parent != null && parent.isSubPlot())  {
                         sender.sendMessage("You cannot create a subplot of a subplot.");
                         return true;
                     }
 
                     boolean override = false;
 
                     if (args.length > 1) {
                         String flag = args[1];
                         if (flag.equalsIgnoreCase("override")) {
                             if (permission.hasPermission(playerName,"override")) {
                                 override = true;
                                 sender.sendMessage(ChatColor.GREEN + "Override enabled");
                             }
                         }
                     }
 
                     List<CoordData> coords = coord.popCoords(playerName,2);
 
                     if (coords.size() < 2) {
                         sender.sendMessage("You need two coords set in order to create a plot");
                         return true;
                     }
 
                     CoordData c1 = coords.get(0);
                     CoordData c2 = coords.get(1);
 
                     int x1 = (int)c1.getX();
                     int z1 = (int)c1.getZ();
                     int x2 = (int)c2.getX();
                     int z2 = (int)c2.getZ();
 
                     if (!"economy".equalsIgnoreCase(p.getWorld().getName()) && !override) {
                         sender.sendMessage(ChatColor.RED + "Protected plots can only be created in the economy world");
                         return true;
                     }
 
                     if (x1 > x2) {
                         int tmp = x1;
                         x1 = x2;
                         x2 = tmp;
                     }
 
                     if (z1 > z2) {
                         int tmp = z1;
                         z1 = z2;
                         z2 = tmp;
                     }
 
                     if (subPlot) {
                         if (plotAt(x1,z1,p.getWorld().getName(),false).getId() != parent.getId()
                                 || plotAt(x2,z2,p.getWorld().getName(),false).getId() != parent.getId()) {
                             sender.sendMessage("This subplot is not entirely within the parent plot.");
                             return true;
                         }
                     }
 
                     if (plotIntersect(x1,z1,x2,z2,p.getWorld().getName(),subPlot)) {
                         sender.sendMessage("There is already a plot intersecting (" + x1 + "," + z1 + ") and (" + x2 + "," + z2 + ")");
                         return true;
                     }
 
                     int width = x2-x1;
                     int height = z2-z1;
 
                     if ((width == 0 || height == 0)
                             || ((width < 100 || height < 100 || width > 100 || height > 100) && !override && !subPlot)) {
                         sender.sendMessage("This plot is " + width + "x" + height + ". It cannot be less then 100x100. It cannot be greater then 100x100");
                         return true;
                     }
 
                     double cost = 0;
 
                     int count = PlotData.getPlayerPlotCount(playerName);
 
                     if (!override && !subPlot) {
                         cost = width*height*8+(width*height)*count*4;
                         if (money == null) {
                             sender.sendMessage("This functionality is currently unavailable");
                         }
                         double bal = money.getBalance(playerName);
 
                         sender.sendMessage("This plot is " + width + "x" + height + " and will cost $" + cost);
 
                         if (bal < cost) {
                             sender.sendMessage("You don't have enough money");
                             return true;
                         }
                     }
 
                     if (!subPlot) {
                         sender.sendMessage("Coords: " + x1 + "," + z1 + " to " + x2 + "," + z2);
                         sender.sendMessage("You are purchasing a plot sized " + width + "x" + height + " and costing $" + cost);
                     } else {
                         sender.sendMessage("Coords: " + x1 + "," + z1 + " to " + x2 + "," + z2);
                         sender.sendMessage("You are creating a plot sized " + width + "x" + height);
                     }
 
                     if (subCommand.equalsIgnoreCase("create")) {
                         sender.sendMessage("Attempting to create plot.");
 
                         if (!override && !subPlot) {
                             if (!money.give(playerName,-cost)) {
                                 sender.sendMessage("Unable to deduct cost from your account.");
                                 return true;
                             } else {
                                 sender.sendMessage("Deducted $" + cost + " from your account.");
                                 getLogger().info(playerName + " purchased plot for $" + cost);
                             }
                         }
 
                         PlotData pd = new PlotData();
                         pd.setX1(x1);
                         pd.setX2(x2);
                         pd.setZ1(z1);
                         pd.setZ2(z2);
                         pd.setName(playerName + "'s plot");
                         pd.setOwner(playerName);
                         pd.setWorld(p.getWorld().getName());
                         pd.setSubPlot(subPlot);
                         if (subPlot) {
                             pd.setParent(parent.getId());
                         }
 
                         if (!pd.save(database)) {
                             sender.sendMessage(ChatColor.RED + "Unable to create plot.");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + "Plot created!");
                         }
                     } else {
                         sender.sendMessage("End plot check");
                     }
                 } else if (subCommand.equalsIgnoreCase("release")) {
                     Player p = (Player)sender;
                     if (!permission.hasPermission(playerName,"plot_plotrelease")) {
                         sender.sendMessage("You don't have permissions for this command (plot_plotrelease)");
                         return true;
                     }
 
                     if (args.length < 2) {
                         sender.sendMessage(ChatColor.RED + "Warning! The /" + command + " release command will REMOVE your plot!");
                         sender.sendMessage(ChatColor.RED + "You will have to recreate it completely (and pay the cost again)");
                         sender.sendMessage(ChatColor.RED + "If you really want to remove it, use /" + command + " release yes");
                         return true;
                     }
 
                     String confirm = args[1];
                     if (!"yes".equalsIgnoreCase(confirm)) {
                         return true;
                     }
 
                     PlotData pd = plotAt(p.getLocation().getBlockX(),p.getLocation().getBlockZ(),p.getWorld().getName(),subPlot);
 
                     if (pd == null) {
                         sender.sendMessage("You are not currently inside a plot.");
                         return true;
                     }
 
                     if (database == null) {
                         sender.sendMessage("This functionality is not currently available");
                         return true;
                     }
 
                     int perms = getPlotPerms(pd,playerName);
                     if (perms < PlotPerms.COOWNER) {
                         sender.sendMessage("You don't have permission to do this");
                         return true;
                     }
 
                     sender.sendMessage("Attempting to remove plot");
 
                     for (Class c : plotLeaveListener.keySet()) {
                         try {
                             Map<JavaPlugin,Integer> blah = plotEnterListener.get(c);
                             JavaPlugin plugin = (JavaPlugin)blah.keySet().toArray()[0];
 
                             Class[] argTypes = new Class[] {PlotData.class,Player.class};
                             Method m = c.getDeclaredMethod("handlePlotLeave",argTypes);
                             m.invoke(plugin,pd,p);
                         } catch (Exception e) {
                             getLogger().warning("Unable to call handlePlotLeave for " + c.getName());
                         }
                     }
 
                     if (!pd.delete(database)) {
                         sender.sendMessage("Unable to remove plot.");
                     } else {
                         sender.sendMessage("Plot removed.");
 
                         List<PlotPerms> data = (List<PlotPerms>)database.select(PlotPerms.class,"plot = " + pd.getId());
 
                         for (PlotPerms pp : data) {
                             pp.delete(database);
                         }
                     }
                 } else if (subCommand.equalsIgnoreCase("info")) {
                     Player p = (Player)sender;
                     if (!permission.hasPermission(playerName,"plot_plotinfo")) {
                         sender.sendMessage("You don't have permissions for this command (plot_plotinfo)");
                         return true;
                     }
 
                     PlotData pd = plotAt(p.getLocation().getBlockX(),p.getLocation().getBlockZ(),p.getWorld().getName(),subPlot);
 
                     if (pd == null) {
                         sender.sendMessage("You are not currently inside a plot.");
                         return true;
                     } else {
                         sender.sendMessage("You are in " + pd.getName() + ", owned by " + pd.getOwner() + ". Bounds: (" + pd.getX1() + "," + pd.getZ1() + ") to (" + pd.getX2() + "," + pd.getZ2() + ")");
                         sender.sendMessage("Pvp: " + pd.isPvp() + ", pve: " + pd.isPve());
                     }
                 } else if (subCommand.equalsIgnoreCase("setperm")) {
                     Player p = (Player)sender;
                     if (!permission.hasPermission(playerName,"plot_plotsetperm")) {
                         sender.sendMessage("You don't have permissions for this command (plot_plotsetperm)");
                         return true;
                     }
 
                     if (args.length > 2) {
                         String player = args[1];
                         int perm = translatePerm(args[2]);
 
                         if (perm == PlotPerms.NONE) {
                             sender.sendMessage("Unrecognized permission");
                             return true;
                         }
 
                         PlotData pd = plotAt(p.getLocation().getBlockX(),p.getLocation().getBlockZ(),p.getWorld().getName(),subPlot);
 
                         if (pd == null) {
                             sender.sendMessage("You are not currently inside a plot.");
                             return true;
                         }
 
                         int perms = getPlotPerms(pd,playerName);
                         if (perms < PlotPerms.COOWNER) {
                             sender.sendMessage("You don't have permission to do this");
                             return true;
                         }
 
                         if (!givePerms(player,pd,perm)) {
                             sender.sendMessage(ChatColor.RED + "Unable to set permissions");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + player + " has been given " + args[2] + " permissions");
                         }
                     } else {
                         sender.sendMessage("/" + command + " setperm <player> <coowner|resident|worker>");
                     }
                 } else if (subCommand.equalsIgnoreCase("removeperm")) {
                     Player p = (Player)sender;
                     if (!permission.hasPermission(playerName,"plot_plotsetperm")) {
                         sender.sendMessage("You don't have permissions for this command (plot_plotsetperm)");
                         return true;
                     }
 
                     if (args.length > 1) {
                         String player = args[1];
 
                         PlotData pd = plotAt(p.getLocation().getBlockX(),p.getLocation().getBlockZ(),p.getWorld().getName(),subPlot);
 
                         if (pd == null) {
                             sender.sendMessage("You are not currently inside a plot.");
                             return true;
                         }
 
                         int perms = getPlotPerms(pd,playerName);
                         if (perms < PlotPerms.COOWNER) {
                             sender.sendMessage("You don't have permission to do this");
                             return true;
                         }
 
                         if (!removePerms(player, pd)) {
                             sender.sendMessage(ChatColor.RED + "Unable to remove permissions");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + player + " has been stripped of permissions");
                         }
                     } else {
                         sender.sendMessage("/" + command + " removeperm <player>");
                     }
                 } else if ("flag".equalsIgnoreCase(subCommand)) {
                     Player p = (Player)sender;
                     if (!permission.hasPermission(playerName,"plot_plotcreate")) {
                         sender.sendMessage("You don't have permissions for this command (plot_plotcreate)");
                         return true;
                     }
 
                     if (args.length > 2) {
                         String flag = args[1];
                         String val = args[2];
 
                         PlotData pd = plotAt(p.getLocation().getBlockX(),p.getLocation().getBlockZ(),p.getWorld().getName(),subPlot);
 
                         if (pd == null) {
                             sender.sendMessage(ChatColor.RED + "You are not currently inside a plot.");
                             return true;
                         }
 
                         int perms = getPlotPerms(pd,playerName);
                         if (perms < PlotPerms.COOWNER) {
                             sender.sendMessage(ChatColor.RED + "You don't have permission to do this");
                             return true;
                         }
 
                         if (!"on".equalsIgnoreCase(val) && !"off".equalsIgnoreCase(val)) {
                             sender.sendMessage("/" + command + " flag " + flag + " <on|off>");
                             return true;
                         }
 
                         if ("pvp".equalsIgnoreCase(flag)) {
                             if ("on".equalsIgnoreCase(val)) {
                                 pd.setPvp(true);
                             } else if ("off".equalsIgnoreCase(val)) {
                                 pd.setPvp(false);
                             }
                             if (pd.save(database)) {
                                 sender.sendMessage(ChatColor.GREEN + "PVP for this plot is now " + val);
                             } else {
                                 sender.sendMessage(ChatColor.RED + "Could not update plot");
                             }
                         } else if ("pve".equalsIgnoreCase(flag)) {
                             if ("on".equalsIgnoreCase(val)) {
                                 pd.setPve(true);
                             } else if ("off".equalsIgnoreCase(val)) {
                                 pd.setPve(false);
                             }
                             if (pd.save(database)) {
                                 sender.sendMessage(ChatColor.GREEN + "PVE for this plot is now " + val);
                             } else {
                                 sender.sendMessage(ChatColor.RED + "Could not update plot");
                             }
                         } else {
                             sender.sendMessage("/" + command + " flag <pvp|pve> <on|off>");
                         }
                     } else {
                         sender.sendMessage("/" + command + " flag <pvp|pve> <on|off>");
                     }
                 } else if ("name".equalsIgnoreCase(subCommand)) {
 
                     Player p = (Player)sender;
                     if (!permission.hasPermission(playerName,"plot_plot")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permissions for this command (plot_plot)");
                         return true;
                     }
 
                     if (args.length > 1) {
                         String name = "";
                         for (int i=1;i<args.length;i++) {
                             name += args[i];
                             if (i < args.length-1) {
                                 name += " ";
                             }
                         }
 
                         PlotData pd = plotAt(p.getLocation().getBlockX(),p.getLocation().getBlockZ(),p.getWorld().getName(),subPlot);
 
                         if (pd == null) {
                             sender.sendMessage(ChatColor.RED + "You are not currently inside a plot.");
                             return true;
                         }
 
                         int perms = getPlotPerms(pd,playerName);
                         if (perms < PlotPerms.COOWNER) {
                             sender.sendMessage(ChatColor.RED + "You don't have permission to do this");
                             return true;
                         }
 
                         pd.setName(name);
                         if (pd.save(database)) {
                             p.sendMessage(ChatColor.GREEN + "Plot name updated to " + name);
                         } else {
                             p.sendMessage(ChatColor.RED + "Unable to update plot");
                         }
                     } else {
                         sender.sendMessage("/" + command + " name <name>");
                     }
                 } else {
                     sender.sendMessage("/" + command + " <create|release|info|check|setperm|removeperm|flag|name>");
                 }
             } else {
                 sender.sendMessage("/" + command + " <create|release|info|check|setperm|removeperm|flag|name>");
             }
         } else if (command.equalsIgnoreCase("reloadplots")) {
             if (permission == null || !permission.hasPermission(playerName,"plot_reload")) {
                 sender.sendMessage("You don't have permissions for this command (plot_reload)");
                 return true;
             }
 
             if (database == null) {
                 sender.sendMessage("Database unavailable, cannot refresh caches now.");
                 return true;
             }
 
             sender.sendMessage("Reloading plot data...");
             PlotData.refreshCache(database,getLogger());
             PlotPerms.refreshCache(database,getLogger());
             sender.sendMessage("Caches refreshed.");
         } else {
             return false;
         }
 
         return true;
     }
 }
