 package com.Top_Cat.CODMW;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.Top_Cat.CODMW.listeners.CODBlockListener;
 import com.Top_Cat.CODMW.listeners.CODEntityListener;
 import com.Top_Cat.CODMW.listeners.CODInventoryListener;
 import com.Top_Cat.CODMW.listeners.CODPlayerListener;
 import com.Top_Cat.CODMW.objects.CWolf;
 import com.Top_Cat.CODMW.objects.chopper;
 import com.Top_Cat.CODMW.objects.claymore;
 import com.Top_Cat.CODMW.objects.door;
 import com.Top_Cat.CODMW.objects.map;
 import com.Top_Cat.CODMW.objects.player;
 import com.Top_Cat.CODMW.objects.redstone;
 import com.Top_Cat.CODMW.objects.sentry;
 import com.Top_Cat.CODMW.sql.conn;
 
 public class main extends JavaPlugin {
     
     public World currentWorld;
     public Location teamselect;
     public Location prespawn;
     public HashMap<Player, player> players = new HashMap<Player, player>();
     public int gold, diam, tot, minplayers = 1;
     public final CODPlayerListener playerListener = new CODPlayerListener(this);
     public final CODBlockListener blockListener = new CODBlockListener(this);
     public final CODEntityListener entityListener = new CODEntityListener(this);
     public final CODInventoryListener inventoryListener = new CODInventoryListener(this);
     public ArrayList<claymore> clays = new ArrayList<claymore>();
     public HashMap<Wolf, CWolf> wolves = new HashMap<Wolf, CWolf>();
     public ArrayList<sentry> sentries = new ArrayList<sentry>();
     public ArrayList<Player> totele = new ArrayList<Player>();
     public ArrayList<chopper> choppers = new ArrayList<chopper>();
     public final String d = "\u00C2\u00A7";
     door d1,d2,d3,d4;
     public game game;
     public boolean activeGame = false;
     public redstone r;
     Timer t = new Timer();
     public conn sql = new conn();
     map currentMap;
     
     public void clearinv(Player p) {
         PlayerInventory i = p.getInventory();
         i.clear();
         i.clear(39);
         i.clear(38);
         i.clear(37);
         i.clear(36);
     }
     
     public player p(Player p) {
         return players.get(p);
     }
     
     @Override
     public void onDisable() {
         if (game != null) {
             game.destroy();
         }
         System.out.println("Goodbye world!");
     }
     
     public void setDoors() {
         d1.open();
         d2.open();
         d3.open();
         d4.open();
         if (gold > diam) {
             d1.close();
             d2.close();
         } else if (diam > gold) {
             d3.close();
             d4.close();
         }
     }
 
     public class sun extends TimerTask {
 
         @Override
         public void run() {
             currentWorld.setTime(currentMap.time);
             currentWorld.setStorm(currentMap.storm);
             currentWorld.setThundering(false);
         }
         
     }
     
     public void loadmap() {
         String w = "";
         if (currentMap != null) {
             w = " WHERE `Id` != '" + currentMap.id + "'";
         }
         ResultSet _r = sql.query("SELECT * FROM cod_maps" + w + " ORDER BY RAND()");
         try {
             _r.next();
             currentMap = new map(sql, this, _r);
             currentWorld = getServer().createWorld(currentMap.folder, Environment.NORMAL);
             teamselect = new Location(currentWorld, -14, 64, 13, 270, 0);
             prespawn = new Location(currentWorld, -15.5, 64, 2.5, 270, 0);
             
             d1 = new door(currentWorld.getBlockAt(-10, 64, 14));
             d2 = new door(currentWorld.getBlockAt(-9, 64, 14));
             
             d3 = new door(currentWorld.getBlockAt(-10, 64, 11));
             d4 = new door(currentWorld.getBlockAt(-9, 64, 11));
             
             r = new redstone(currentWorld.getBlockAt(-6, 64, 0));
             
             for (Player i : getServer().getOnlinePlayers()) {
                 if (players.containsKey(i)) {
                     i.teleport(prespawn);
                 } else {
                     i.teleport(teamselect);
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void scheduleGame() {
         t.schedule(new startgame(this), 55000);
     }
     
     public class startgame extends TimerTask {
 
         main plugin;
         
         public startgame(main instance) {
             plugin = instance;
         }
         
         @Override
         public void run() {
             if (activeGame == false) {
                 if (players.size() >= minplayers) {
                     game = new game(plugin);
                 } else {
                     scheduleGame();
                 }
             }
         }
         
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof Player) {
             Player p = ((Player) sender);
             if (command.getName().equals("r")) {
                 int arrows = 0;
                 int ammo = 0;
                 for (ItemStack i : p.getInventory().getContents()) {
                     if (i != null) {
                         if (i.getType() == Material.ARROW) {
                             arrows += i.getAmount();
                         }
                         if (i.getType() == Material.FEATHER) {
                             ammo += i.getAmount();
                         }
                     }
                 }
                 int togive = ammo < 15 ? ammo : 15;
                 togive -= arrows;
                 if (togive > 0) {
                     if (arrows == 0) {
                         p.getInventory().setItem(8, new ItemStack(Material.ARROW, togive));
                     } else {
                         p.getInventory().addItem(new ItemStack(Material.ARROW, togive));
                     }
                 }
                 p.getInventory().removeItem(new ItemStack(Material.FEATHER, togive));
                 return true;
             } else if (command.getName().equalsIgnoreCase("s")) {
                 if (args.length == 0) {
                     p.sendMessage(d + "6Gold: " + game.gold + d + "f  " + d + "bDiamond: " + game.diam + d + "f      / 50");
                     return true;
                 } else if (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("g")) {
                     String c = "6";
                     team t = team.GOLD;
                     if (args[0].equalsIgnoreCase("d")) {
                         c = "b";
                         t = team.DIAMOND;
                     }
                     int td = 0;
                     p.sendMessage(d + c + "Player Name | Ki | A | D |");
                     for (player i : players.values()) {
                         if (i.getTeam() == t) {
                             td += i.kill;
                             p.sendMessage(d + c + i.nick + " | " + i.kill + " | " + i.assists + " | " + i.death + " |");
                         }
                     }
                     p.sendMessage(d + c + td + " / 50");
                     return true;
                 }
             } else if (command.getName().equalsIgnoreCase("team") && args[0].equalsIgnoreCase("switch")) {
                 switchplayer(p);
                 return true;
             }
         }
         return false;
     }
     
     public void switchplayer(Player p) {
         if (p(p).getTeam() == team.GOLD && gold > diam) {
             p(p).setTeam(team.DIAMOND);
             gold--;
             diam++;
         } else if (p(p).getTeam() == team.DIAMOND && gold < diam) {
             p(p).setTeam(team.GOLD);
             gold++;
             diam--;
         } else {
             p.sendMessage("Teams cannot be stacked!");
             return;
         }
         p(p).resetScore();
         p.teleport(prespawn);
         p(p).dead = true;
     }
     
     @Override
     public void onEnable() {
         loadmap();
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.INVENTORY_OPEN, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
         
         pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
         
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PAINTING_BREAK, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PAINTING_PLACE, entityListener, Priority.Normal, this);
         
         pm.registerEvent(Event.Type.CUSTOM_EVENT, inventoryListener, Priority.Normal, this);
         
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         
         t.schedule(new sun(), 0, 60000);
         
         setDoors();
         for (Player p : getServer().getOnlinePlayers()) {
             clearinv(p);
             p.sendMessage(d + "9Welcome to The Gigcast's MineCod Server!");
             p.sendMessage(d + "9Please choose your team!");
             p.setHealth(20);
         }
     }
     
 }
