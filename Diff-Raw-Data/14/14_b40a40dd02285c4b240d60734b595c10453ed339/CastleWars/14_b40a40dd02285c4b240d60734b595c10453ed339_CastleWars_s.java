 package com.thepastimers.CastleWars;
 
 import com.thepastimers.Chat.Chat;
 import com.thepastimers.Coord.Coord;
 import com.thepastimers.Coord.CoordData;
 import com.thepastimers.Database.Database;
 import com.thepastimers.Money.Money;
 import com.thepastimers.Pattern.Pattern;
 import com.thepastimers.Permission.Permission;
 import com.thepastimers.Plot.Plot;
 import com.thepastimers.Plot.PlotData;
 import com.thepastimers.Worlds.Worlds;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 8/17/13
  * Time: 12:14 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CastleWars extends JavaPlugin implements Listener {
     Plot plot;
     Database database;
     Permission permission;
     Money money;
     Pattern pattern;
     Chat chat;
     Worlds worlds;
 
     Map<Player,ClaimCastle> claims;
 
     static int MAX_LEVEL = 5;
     // both of these in dollars
     static int INCOME_PER_LEVEL = 50;
     static int COST_PER_LEVEL = 2000;
    static int COST_PER_DEFENSE_LEVEL = 5;
 
     @Override
     public void onEnable() {
         getLogger().info("CastleWars init");
 
         getServer().getPluginManager().registerEvents(this,this);
 
         database = (Database)getServer().getPluginManager().getPlugin("Database");
         if (database == null) {
             getLogger().warning("Unable to load Database plugin. Some functionality may not be available");
         }
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
         if (permission == null) {
             getLogger().warning("Unable to load Permission plugin. Some functionality may not be available");
         }
 
         plot = (Plot)getServer().getPluginManager().getPlugin("Plot");
         if (plot == null) {
             getLogger().warning("Unable to load Plot plugin. Some functionality may not be available");
         } else {
             plot.registerPlotEnter(CastleWars.class,this);
             plot.registerPlotLeave(CastleWars.class,this);
         }
 
         chat = (Chat)getServer().getPluginManager().getPlugin("Chat");
         if (chat == null) {
             getLogger().warning("Unable to load Chat plugin. Some functionality may not be available");
         }
 
         claims = new HashMap<Player, ClaimCastle>();
 
         money = (Money)getServer().getPluginManager().getPlugin("Money");
         if (money == null) {
             getLogger().warning("Unable to load Money plugin. Some functionality may not be available");
         }
 
         worlds = (Worlds)getServer().getPluginManager().getPlugin("Worlds");
         if (worlds == null) {
             getLogger().warning("Unable to load Worlds plugin. Some functionality may not be available.");
         }
 
         pattern = (Pattern)getServer().getPluginManager().getPlugin("Pattern");
         if (pattern == null) {
             getLogger().warning("Unable to load Pattern plugin. Some functionality may not be available");
         }
 
         getLogger().info(CastleData.getTableInfo());
         CastleData.refreshCache(database,getLogger());
         getLogger().info(CastleSpawner.getTableInfo());
         CastleSpawner.refreshCache(database,getLogger());
 
         CastleIncome ci = new CastleIncome(this,database);
         ci.runTaskTimer(this,0,20*60);
     }
 
     @Override
     public void onDisable() {
         getLogger().info("CastleWars disable");
     }
 
     public void handlePlotLeave(PlotData pd,Player p) {
         CastleData cd = CastleData.getCastleForPlot(pd);
         if (cd == null) return;
 
         if (claims.containsKey(p)) {
             ClaimCastle claimCastle = claims.get(p);
             claimCastle.cancel();
 
             boolean anotherPlayer = false;
             for (Player player : claims.keySet()) {
                 if (claims.get(player).getCd().getId() == cd.getId()) {
                     anotherPlayer = true;
                     break;
                 }
             }
 
             if (!anotherPlayer) {
                 List<Entity> entityList = ClaimCastle.getMobsInCastle(this,pd);
                 for (Entity e : entityList) {
                     e.remove();
                 }
             }
         }
     }
 
     @EventHandler
     public void death(PlayerDeathEvent event) {
         Player p = event.getEntity();
         Location l = p.getLocation();
         PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
         if (pd == null) {
             pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
         }
         if (pd == null) return;
 
         CastleData cd = CastleData.getCastleForPlot(pd);
         if (cd == null) return;
 
         if (claims.containsKey(p)) {
             ClaimCastle claimCastle = claims.get(p);
             claimCastle.cancel();
         }
     }
 
     @EventHandler
     public void leave(PlayerQuitEvent event) {
         Player p = event.getPlayer();
 
         if (claims.containsKey(p)) {
             ClaimCastle claimCastle = claims.get(p);
             claimCastle.cancel();
             getLogger().info("Player  " + p.getName() + " is no longer capturing");
         }
     }
 
     @EventHandler
     public void login(PlayerJoinEvent event) {
         Player p = event.getPlayer();
         Location l = p.getLocation();
         PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
         //getLogger().info("join event (" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," + l.getWorld().getName() + ")");
         if (pd == null) {
             pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
         }
         //getLogger().info("Plot: " + pd);
         if (pd == null) return;
 
         CastleData cd = CastleData.getCastleForPlot(pd);
         if (cd == null) return;
 
         if (!claims.containsKey(p)) {
             handlePlotEnter(pd,p);
         }
     }
 
     public void handlePlotEnter(PlotData pd,Player p) {
         CastleData cd = CastleData.getCastleForPlot(pd);
         if (cd == null) return;
         p.sendMessage(ChatColor.GREEN + "You have entered a CastleWars castle. This castle is level " + cd.getLevel());
         if ("Unclaimed".equalsIgnoreCase(cd.getOwner())) {
             p.sendMessage(ChatColor.GREEN + "This castle is unclaimed");
         } else {
             p.sendMessage(ChatColor.GREEN + "This castle is claimed by " + cd.getOwner());
         }
         if (!p.getName().equalsIgnoreCase(cd.getOwner())) {
             Player player = getServer().getPlayer(cd.getOwner());
             if (player != null) {
                 Location l = player.getLocation();
                 PlotData pd2 = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
                 if (pd2 == null) pd2 = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
                 if (pd2 != null) {
                     if (pd2.getId() == pd.getId()) {
                         p.sendMessage(ChatColor.RED + "The owner is currently occupying this castle");
                         return;
                     }
                 }
             }
             int timer = 60*cd.getLevel();
             ClaimCastle cc = new ClaimCastle(this,database,cd,timer,p);
             cc.runTaskTimer(this,20,20);
             claims.put(p,cc);
             int minutes = timer/60;
             p.sendMessage(ChatColor.GREEN + "Remain in this plot for " + minutes + " minutes to capture this castle");
         }
     }
 
     public void createCastle(CastleData cd) {
         if (plot == null || pattern == null || cd == null) return;
         PlotData pd = PlotData.getPlotById(cd.getPlot());
         if (pd == null) {
             return;
         }
 
         String patternStr = "castle_l" + cd.getLevel();
         int c_x = pattern.getXSize(patternStr);
         int c_z = pattern.getZSize(patternStr);
         int p_x = pd.getX2()-pd.getX1();
         int p_z = pd.getZ2()-pd.getZ1();
 
         if (!doesItFit(cd)) return;
         // center the pattern in the plot
         int p_center_x = pd.getX1() + p_x/2;
         int p_center_z = pd.getZ1() + p_z/2;
         int off_x = (p_x/2)-(c_x/2);
         int off_z = (p_z/2)-(c_z/2);
 
         int c_y = cd.getY();
 
         //getLogger().info(pd.getX1() + " " + off_x);
 
         off_x += pd.getX1();
         off_z += pd.getZ1();
 
         getLogger().info("Creating castle " + patternStr + " at (" + off_x + "," + c_y + "," + off_z + ")");
 
         pattern.loadPattern(patternStr,off_x,c_y,off_z,pd.getWorld());
     }
 
     public void regenerateCastle(CastleData cd) {
         removeCastle(cd);
         if (plot == null || pattern == null || cd == null) return;
         PlotData pd = PlotData.getPlotById(cd.getPlot());
         if (pd == null) {
             return;
         }
 
         String patternStr = "castle_l" + cd.getLevel();
         int c_x = pattern.getXSize(patternStr);
         int c_z = pattern.getZSize(patternStr);
         int p_x = pd.getX2()-pd.getX1();
         int p_z = pd.getZ2()-pd.getZ1();
 
         if (!doesItFit(cd)) return;
         // center the pattern in the plot
         int p_center_x = pd.getX1() + p_x/2;
         int p_center_z = pd.getZ1() + p_z/2;
         int off_x = (p_x/2)-(c_x/2);
         int off_z = (p_z/2)-(c_z/2);
 
         int c_y = cd.getY();
 
         //getLogger().info(pd.getX1() + " " + off_x);
 
         off_x += pd.getX1();
         off_z += pd.getZ1();
 
         getLogger().info("Creating castle " + patternStr + " at (" + off_x + "," + c_y + "," + off_z + ")");
 
         pattern.clearThenLoadPattern(patternStr, patternStr, off_x, c_y, off_z, pd.getWorld(), off_x, c_y, off_z, pd.getWorld());
     }
 
     public void upgradeCastle(CastleData cd) {
         removeCastle(cd);
         if (plot == null || pattern == null || cd == null) return;
         PlotData pd = PlotData.getPlotById(cd.getPlot());
         if (pd == null) {
             return;
         }
 
         String patternStr = "castle_l" + cd.getLevel();
         int c_x = pattern.getXSize(patternStr);
         int c_z = pattern.getZSize(patternStr);
         int p_x = pd.getX2()-pd.getX1();
         int p_z = pd.getZ2()-pd.getZ1();
 
         if (!doesItFit(cd)) return;
         // center the pattern in the plot
         int p_center_x = pd.getX1() + p_x/2;
         int p_center_z = pd.getZ1() + p_z/2;
         int off_x = (p_x/2)-(c_x/2);
         int off_z = (p_z/2)-(c_z/2);
 
         String patternStr2 = "castle_l" + (cd.getLevel()-1);
         int c_x2 = pattern.getXSize(patternStr2);
         int c_z2 = pattern.getZSize(patternStr2);
         int p_x2 = pd.getX2()-pd.getX1();
         int p_z2 = pd.getZ2()-pd.getZ1();
 
         // center the pattern in the plot
         int off_x2 = (p_x2/2)-(c_x2/2);
         int off_z2 = (p_z2/2)-(c_z2/2);
 
         int c_y = cd.getY();
 
         //getLogger().info(pd.getX1() + " " + off_x);
 
         off_x += pd.getX1();
         off_z += pd.getZ1();
         off_x2 += pd.getX1();
         off_z2 += pd.getZ1();
 
         //getLogger().info("Replacing castle " + patternStr2 + " at (" + off_x2 + "," + c_y + "," + off_z2 + ") with castle " + patternStr + " at (" + off_x + "," + c_y + "," + off_z + ")");
 
         pattern.clearThenLoadPattern(patternStr2,patternStr,off_x2,c_y,off_z2,pd.getWorld(),off_x,c_y,off_z,pd.getWorld());
     }
 
     private boolean doesItFit(CastleData cd) {
         String patternStr = "castle_l" + cd.getLevel();
         PlotData pd = PlotData.getPlotById(cd.getPlot());
         if (pd == null) {
             return false;
         }
 
         int c_x = pattern.getXSize(patternStr);
         int c_z = pattern.getZSize(patternStr);
         int p_x = pd.getX2()-pd.getX1();
         int p_z = pd.getZ2()-pd.getZ1();
 
         //getLogger().info("plot width: (" + p_x + "," + p_z + "), pattern width (" + c_x + "," + c_z + ")");
 
         return !(c_x > p_x || c_z > p_z);
     }
 
     public void removeCastle(CastleData cd) {
         if (plot == null || pattern == null || cd == null) return;
         PlotData pd = PlotData.getPlotById(cd.getPlot());
         if (pd == null) {
             return;
         }
 
         String patternStr = "castle_l" + cd.getLevel();
         int c_x = pattern.getXSize(patternStr);
         int c_z = pattern.getZSize(patternStr);
         int p_x = pd.getX2()-pd.getX1();
         int p_z = pd.getZ2()-pd.getZ1();
 
         // center the pattern in the plot
         int p_center_x = pd.getX1() + p_x/2;
         int p_center_z = pd.getZ1() + p_z/2;
         int off_x = (p_x/2)-(c_x/2);
         int off_z = (p_z/2)-(c_z/2);
 
         int c_y = cd.getY();
 
         off_x += pd.getX1();
         off_z += pd.getZ1();
 
         //getLogger().info("Removing castle " + patternStr + " at (" + off_x + "," + c_y + "," + off_z + ")");
 
         pattern.clearPattern(patternStr, off_x, c_y, off_z, pd.getWorld());
     }
 
     public void triggerIncome() {
         triggerIncome(false);
     }
 
     public void triggerIncome(boolean debug) {
         if (database == null || money == null) {
             if (debug) getLogger().info("triggerIncome aborting, database or money plugin not found");
             return;
         }
 
         if (debug) {
             getLogger().info("Running triggerIncome");
         }
 
         List<CastleData> castleDataList = (List<CastleData>)database.select(CastleData.class,"");
 
         if (debug) {
             getLogger().info("Got " + castleDataList.size() + " entries");
         }
 
         for (CastleData cd : castleDataList) {
             if (debug) {
                 getLogger().info("Checking castle " + cd.getId() + ". Owner: " + cd.getOwner() + ", level " + cd.getLevel());
             }
 
             List<CastleSpawner> spawners = CastleSpawner.getSpawnersForCastle(cd);
             int spawnerCount = spawners.size();
             int amount = (cd.getLevel()*INCOME_PER_LEVEL - spawnerCount*(cd.getDefenseLevel()+1)*COST_PER_DEFENSE_LEVEL)*100;
             double d_amount = (double)amount;
             d_amount /= 100;
 
             if (debug) {
                 getLogger().info("Sending " + d_amount + " to player");
             }
 
             money.give(cd.getOwner(),d_amount);
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
 
         if (command.equalsIgnoreCase("cw")) {
             if (permission == null || !permission.hasPermission(playerName,"castle_command") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (castle_command)");
                 return true;
             }
 
             if (args.length > 0) {
                 String subCommand = args[0];
 
                 if ("create".equalsIgnoreCase(subCommand)) {
                     if (permission == null || !permission.hasPermission(playerName,"castle_create") || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (castle_create)");
                         return true;
                     }
 
                     Player p = (Player)sender;
                     Location l = p.getLocation();
 
                     if (plot == null) {
                         sender.sendMessage(ChatColor.RED + "This command is not currently available");
                         return true;
                     }
 
                     PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
                     if (pd == null) {
                         pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
                     }
                     if (pd == null) {
                         sender.sendMessage(ChatColor.RED + "You must be inside a plot to do this");
                         return true;
                     }
 
                     if (CastleData.getCastleForPlot(pd) != null) {
                         sender.sendMessage(ChatColor.RED + "There is already a castle on this plot");
                         return true;
                     }
 
                     int p_x = pd.getX2()-pd.getX1();
                     int p_z = pd.getZ2()-pd.getZ1();
 
                     //getLogger().info(pd.getX2() + "-" + pd.getX1() + "=" + p_x);
 
                     // center the pattern in the plot
                     int p_center_x = pd.getX1() + p_x/2;
                     int p_center_z = pd.getZ1() + p_z/2;
                     int c_y = -1;
 
                     // get height (first non-air block)
                     World w = getServer().getWorld(pd.getWorld());
                     //getLogger().info("Checking world " + pd.getWorld());
                     for (int i=l.getBlockY();i>0;i--) {
                         int x = p_center_x;
                         int z = p_center_z;
                         Block b = w.getBlockAt(x,i,z);
                         if (b.getType() != Material.AIR) {
                             c_y = i;
                             //getLogger().info("Found non air block at " + b.getType().name() + " at (" + x + "," + c_y + "," + z + ")");
                             break;
                         }
                     }
 
                     if (c_y == -1) {
                         sender.sendMessage(ChatColor.RED + "Unable to find a suitable block below you");
                         return true;
                     }
 
                     CastleData cd = new CastleData();
                     cd.setPlot(pd.getId());
                     cd.setLevel(1);
                     cd.setOwner("Unclaimed");
                     cd.setY(c_y);
                     cd.setUpgradeTime(new Timestamp(0));
 
                     pd.setOwner("Server");
                     pd.setPve(true);
                     pd.setName("Unclaimed castle");
                     pd.setPvp(true);
 
                     if (!doesItFit(cd)) {
                         sender.sendMessage(ChatColor.RED + "It is not possible to fit a castle into this plot");
                         return true;
                     }
 
                     if (!pd.save(database)) {
                         sender.sendMessage(ChatColor.RED + "Unable to update plot information");
                     } else {
                         if (!cd.save(database)) {
                             sender.sendMessage(ChatColor.RED + "Unable to create Castle");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + "Castle created!");
                             createCastle(cd);
                         }
                     }
                 } else if ("remove".equalsIgnoreCase(subCommand)) {
                     if (permission == null || !permission.hasPermission(playerName,"castle_create") || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (castle_create)");
                         return true;
                     }
 
                     Player p = (Player)sender;
                     Location l = p.getLocation();
 
                     if (plot == null) {
                         sender.sendMessage(ChatColor.RED + "This command is not currently available");
                         return true;
                     }
 
                     PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
                     if (pd == null) {
                         pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
                     }
                     if (pd == null) {
                         sender.sendMessage(ChatColor.RED + "You must be inside a plot to do this");
                         return true;
                     }
 
                     CastleData cd = CastleData.getCastleForPlot(pd);
                     if (cd == null) {
                         sender.sendMessage(ChatColor.RED + "This is not a CastleWars plot");
                         return true;
                     }
 
                     pd.setOwner(p.getName());
                     pd.setPve(false);
                     pd.setPvp(true);
                     pd.setName(p.getName() + "'s plot");
 
                     if (!pd.save(database)) {
                         sender.sendMessage(ChatColor.RED + "Unable to update plot information");
                     } else {
                         if (!cd.delete(database,CastleData.class)) {
                             sender.sendMessage(ChatColor.RED + "Unable to remove Castle");
                         } else {
                             sender.sendMessage(ChatColor.GREEN + "Castle removed!");
                             removeCastle(cd);
                             CastleData.refreshCache(database,getLogger());
                         }
                     }
                 } else if ("upgrade".equalsIgnoreCase(subCommand)) {
                     Player p = (Player)sender;
                     Location l = p.getLocation();
 
                     if (plot == null || money == null) {
                         sender.sendMessage(ChatColor.RED + "This command is not currently available");
                         return true;
                     }
 
                     PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
                     if (pd == null) {
                         pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
                     }
                     if (pd == null) {
                         sender.sendMessage(ChatColor.RED + "You must be inside a plot to do this");
                         return true;
                     }
 
                     CastleData cd = CastleData.getCastleForPlot(pd);
 
                     if (cd == null) {
                         sender.sendMessage(ChatColor.RED + "This is not a castle");
                         return true;
                     }
 
                     if (!cd.getOwner().equalsIgnoreCase(playerName)) {
                         sender.sendMessage(ChatColor.RED + "You do not own this castle");
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
 
                     int cost = cd.getLevel()*COST_PER_LEVEL*100;
                     double d_cost = (double)cost;
                     d_cost /= 100;
 
                     sender.sendMessage(ChatColor.GREEN + "It costs $" + d_cost + " to upgrade this castle.");
 
                     if (!override) {
                         if (money.getBalance(playerName) < d_cost) {
                             sender.sendMessage(ChatColor.RED + "You do not have enough money to upgrade.");
                             return true;
                         }
 
                         if (!money.give(playerName,-d_cost)) {
                             sender.sendMessage(ChatColor.RED + "Unable to deduct cost.");
                             return true;
                         }
                     }
 
                     cd.setLevel(cd.getLevel()+1);
                     if (!doesItFit(cd)) {
                         cd.setLevel(cd.getLevel()-1);
                         sender.sendMessage(ChatColor.RED + "This castle cannot level up that high");
                         return false;
                     }
 
                     if (cd.save(database)) {
                         sender.sendMessage(ChatColor.GREEN + "Castle upgraded. All spawners have been removed");
 
                         CastleSpawner.clearSpawners(getServer().getWorld(pd.getWorld()),cd);
 
                         List<CastleSpawner> spawners = CastleSpawner.getSpawnersForCastle(cd);
                         for (CastleSpawner s : spawners) {
                             s.delete(database);
                         }
 
                         regenerateCastle(cd);
 
                         upgradeCastle(cd);
                     } else {
                         sender.sendMessage(ChatColor.RED + "Unable to upgrade castle");
                     }
                 } else if ("testIncome".equalsIgnoreCase(subCommand)) {
                     if (permission == null || !permission.hasPermission(playerName,"castle_test_income") || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (castle_test_income)");
                         return true;
                     }
 
                     triggerIncome(true);
                 } else if ("rebuild".equalsIgnoreCase(subCommand)) {
                     if (permission == null || !permission.hasPermission(playerName,"castle_create") || playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (castle_create)");
                         return true;
                     }
 
                     Player p = (Player)sender;
                     Location l = p.getLocation();
 
                     if (plot == null) {
                         sender.sendMessage(ChatColor.RED + "This command is not currently available");
                         return true;
                     }
 
                     PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
                     if (pd == null) {
                         pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
                     }
                     if (pd == null) {
                         sender.sendMessage(ChatColor.RED + "You must be inside a plot to do this");
                         return true;
                     }
 
                     CastleData cd = CastleData.getCastleForPlot(pd);
                     if (cd == null) {
                         sender.sendMessage(ChatColor.RED + "This is not a CastleWars plot");
                         return true;
                     }
 
                     CastleSpawner.clearSpawners(getServer().getWorld(pd.getWorld()),cd);
 
                     List<CastleSpawner> spawners = CastleSpawner.getSpawnersForCastle(cd);
                     for (CastleSpawner s : spawners) {
                         s.delete(database);
                     }
 
                     regenerateCastle(cd);
                 } else if ("info".equalsIgnoreCase(subCommand)) {
                     if (playerName.equalsIgnoreCase("CONSOLE")) {
                         sender.sendMessage(ChatColor.RED + "Console cannot use this command");
                         return true;
                     }
 
                     Player p = (Player)sender;
                     Location l = p.getLocation();
 
                     if (plot == null) {
                         sender.sendMessage(ChatColor.RED + "This command is not currently available");
                         return true;
                     }
 
                     PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
                     if (pd == null) {
                         pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
                     }
                     if (pd == null) {
                         sender.sendMessage(ChatColor.RED + "You must be inside a plot to do this");
                         return true;
                     }
 
                     CastleData cd = CastleData.getCastleForPlot(pd);
 
                     List<CastleSpawner> spawners = CastleSpawner.getSpawnersForCastle(cd);
                     int spawnerCount = 0;
                     if (spawners != null) {
                         spawnerCount = spawners.size();
                     }
 
                     sender.sendMessage("Castle data:");
                     sender.sendMessage("Level: " + cd.getLevel());
                     sender.sendMessage("Owned by: " + cd.getOwner());
                     sender.sendMessage("Income: " + (cd.getLevel()*INCOME_PER_LEVEL - spawnerCount*(cd.getDefenseLevel()+1)*COST_PER_DEFENSE_LEVEL));
                     sender.sendMessage("Defense level: " + cd.getDefenseLevel());
                 } else if ("defense".equalsIgnoreCase(subCommand)) {
                     Player p = (Player)sender;
                     Location l = p.getLocation();
 
                     if (plot == null) {
                         sender.sendMessage(ChatColor.RED + "This command is not currently available");
                         return true;
                     }
 
                     PlotData pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),true);
                     if (pd == null) {
                         pd = PlotData.getPlotAtLocation(l.getBlockX(),l.getBlockZ(),l.getWorld().getName(),false);
                     }
                     if (pd == null) {
                         sender.sendMessage(ChatColor.RED + "You must be inside a plot to do this");
                         return true;
                     }
 
                     CastleData cd = CastleData.getCastleForPlot(pd);
 
                     if (args.length > 1) {
                         String subCommand2 = args[1];
                         if ("create".equalsIgnoreCase(subCommand2)) {
                             List<CastleSpawner> spawners = CastleSpawner.getSpawnersForCastle(cd);
                             int spawnerCount = 0;
                             if (spawners != null) {
                                 spawnerCount = spawners.size();
                             }
                             spawnerCount ++;
                             int totalIncome = cd.getLevel()*INCOME_PER_LEVEL - spawnerCount*(cd.getDefenseLevel()+1)*COST_PER_DEFENSE_LEVEL;
 
                             if (totalIncome <= 0) {
                                 sender.sendMessage(ChatColor.RED + "You don't have enough income at this time to place another spawner (each spawner costs $" + ((cd.getDefenseLevel()+1)*COST_PER_DEFENSE_LEVEL) + "/hr at your current defense level)");
                                 return true;
                             }
 
                             CastleSpawner cs = new CastleSpawner();
                             cs.setCastle(cd.getId());
                             cs.setX(l.getBlockX());
                             cs.setY(l.getBlockY()-1);
                             cs.setZ(l.getBlockZ());
 
                             World w = getServer().getWorld(pd.getWorld());
                             Block b = w.getBlockAt(cs.getX(),cs.getY(),cs.getZ());
 
                             if (b.getType() == Material.SMOOTH_STAIRS || b.getType() == Material.LADDER) {
                                 sender.sendMessage(ChatColor.RED + "You cannot place a spawner here");
                                 return true;
                             }
 
                             cs.setPrevBlock(b.getType().name());
 
                             if (cs.save(database)) {
                                 sender.sendMessage(ChatColor.GREEN + "You have created a spawner at (" + cs.getX() + "," + cs.getY() + "," + cs.getZ() + ")");
 
                                 spawners = CastleSpawner.getSpawnersForCastle(cd);
                                 spawnerCount = 0;
                                 if (spawners != null) {
                                     spawnerCount = spawners.size();
                                 }
                                 totalIncome = cd.getLevel()*INCOME_PER_LEVEL - spawnerCount*(cd.getDefenseLevel()+1)*COST_PER_DEFENSE_LEVEL;
 
                                 sender.sendMessage(ChatColor.GREEN + "You now have " + spawnerCount + " spawners and an income of $" + totalIncome + "/hour");
 
                                 b.setType(Material.GOLD_BLOCK);
                             } else {
                                 sender.sendMessage(ChatColor.RED + "Failed to create spawner");
                             }
                         } else if ("remove".equalsIgnoreCase(subCommand2)) {
                             List<CastleSpawner> spawners = CastleSpawner.getSpawnersForCastle(cd);
 
                             for (CastleSpawner cs : spawners) {
                                 if (cs.getX() == l.getBlockX() && cs.getY() == l.getBlockY()-1 && cs.getZ() == l.getBlockZ()) {
                                     if (cs.delete(database)) {
                                         CastleSpawner.refreshCache(database,getLogger());
 
                                         getLogger().info("" +CastleSpawner.castleDataMap.keySet().size());
                                         sender.sendMessage(ChatColor.GREEN + "You have removed a spawner at (" + cs.getX() + "," + cs.getY() + "," + cs.getZ() + ")");
 
                                         World w = getServer().getWorld(pd.getWorld());
                                         Block b = w.getBlockAt(cs.getX(),cs.getY(),cs.getZ());
                                         b.setType(Material.getMaterial(cs.getPrevBlock()));
 
                                         int spawnerCount = spawners.size()-1;
                                         int totalIncome = cd.getLevel()*INCOME_PER_LEVEL - spawnerCount*(cd.getDefenseLevel()+1)*COST_PER_DEFENSE_LEVEL;
 
                                         sender.sendMessage(ChatColor.GREEN + "You now have " + spawnerCount + " spawners and an income of $" + totalIncome + "/hour");
                                     } else {
                                         sender.sendMessage(ChatColor.RED + "Failed to remove spawner");
                                     }
                                     return true;
                                 }
                             }
                             sender.sendMessage(ChatColor.RED + "No spawners were found at this location");
                         }
                     } else {
                         sender.sendMessage("/cs defense <create|list|remove|upgrade|downgrade>");
                     }
                 }
             } else {
                 sender.sendMessage("/cw <create|remove|upgrade|testIncome|rebuild|info|defense>");
             }
         } else if ("reloadCastles".equalsIgnoreCase(command)) {
             if (permission == null || !permission.hasPermission(playerName,"castle_create") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to use this command (castle_create)");
                 return true;
             }
 
             CastleData.refreshCache(database,getLogger());
             CastleSpawner.refreshCache(database,getLogger());
 
             sender.sendMessage(ChatColor.GREEN + "Caches reloaded");
         } else {
             return false;
         }
 
         return true;
     }
 }
