 package com.Top_Cat.CODMW;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.FileUtil;
 import org.bukkit.util.config.Configuration;
 import org.bukkitcontrib.BukkitContrib;
 
 import com.Top_Cat.CODMW.gamemodes.gamemode;
 import com.Top_Cat.CODMW.gamemodes.team_gm;
 import com.Top_Cat.CODMW.listeners.CODBlockListener;
 import com.Top_Cat.CODMW.listeners.CODEntityListener;
 import com.Top_Cat.CODMW.listeners.CODInputListener;
 import com.Top_Cat.CODMW.listeners.CODInventoryListener;
 import com.Top_Cat.CODMW.listeners.CODPlayerListener;
 import com.Top_Cat.CODMW.listeners.CODWeatherListener;
 import com.Top_Cat.CODMW.objects.Rotation;
 import com.Top_Cat.CODMW.Killstreaks.Killstreaks;
 import com.Top_Cat.CODMW.Killstreaks.killstreak;
 import com.Top_Cat.CODMW.objects.door;
 import com.Top_Cat.CODMW.objects.grenade;
 import com.Top_Cat.CODMW.objects.map;
 import com.Top_Cat.CODMW.objects.player;
 import com.Top_Cat.CODMW.objects.redstone;
 import com.Top_Cat.CODMW.sql.Achievement;
 import com.Top_Cat.CODMW.sql.MCrypt;
 import com.Top_Cat.CODMW.sql.conn;
 import com.Top_Cat.CODMW.vote.GameModes;
 import com.Top_Cat.CODMW.vote.GameTypeVote;
 import com.Top_Cat.CODMW.vote.MapVote;
 import com.Top_Cat.CODMW.vote.Vote;
 
 import java.security.MessageDigest;
 
 public class main extends JavaPlugin {
 
    int minecod_version = 12;
     
     public World currentWorld;
     public Location teamselect;
     public Location prespawn;
     public HashMap<String, map> maps = new HashMap<String, map>();
     public HashMap<Player, player> players = new HashMap<Player, player>();
     public ArrayList<Rotation> map_rot =  new ArrayList<Rotation>();
     public int rot = 0;
     public int gold, diam, tot, minplayers = 0;
     public CODPlayerListener playerListener;
     public CODBlockListener blockListener;
     public CODEntityListener entityListener;
     public CODInventoryListener inventoryListener;
     public CODWeatherListener weatherListener;
     public CODInputListener inputListener;
     public ArrayList<killstreak> ks = new ArrayList<killstreak>();
     public ArrayList<grenade> g = new ArrayList<grenade>();
     public ArrayList<Player> totele = new ArrayList<Player>();
     public final String d = "\u00C2\u00A7";
     door d1, d2, d3, d4;
     public gamemode game;
     public Vote v;
     public boolean activeGame = false;
     public redstone r;
     Timer t = new Timer();
     public conn sql = new conn();
     public map currentMap;
     Random gen = new Random();
     public GameModes gm = GameModes.FFA;
     public String ip;
     public String name = "MineCod Server";
     public String welcome_msg = "Welcome to the MineCod Server!";
     public String join_msg = "$nick has joined the fray";
     Logger log;
     Configuration mainconfig;
     
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
         
         try {
             File directory = new File(getServer().getUpdateFolder());
             if (directory.exists()) {
                 File plugin = new File(directory.getPath(), "MineCod.jar");
                 if (plugin.exists()) {
                     FileUtil.copy(plugin, this.getFile());
                     try {
 						plugin.delete();
 					}
 					catch (SecurityException e1) {}
                 }
             }
         }
         catch (Exception e) {}
         
         System.out.println("Minecod disabled!");
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
             
             updateServerStatus(false);
         }
 
     }
 
     public void preparemap() {
         if (activeGame) { game.destroy(); }
         currentWorld = getServer().createWorld(currentMap.name, Environment.NORMAL);
         currentWorld.setPVP(true);
         currentWorld.setSpawnFlags(true, true);
         for (Entity i : currentWorld.getEntities()) {
             if (i instanceof Item) {
                 i.remove();
             }
         }
         teamselect = new Location(currentWorld, -14, 64, 13, 270, 0);
         prespawn = new Location(currentWorld, -15.5, 64, 2, 270, 0);
 
         d1 = new door(currentWorld.getBlockAt(-10, 64, 14));
         d2 = new door(currentWorld.getBlockAt(-9, 64, 14));
 
         d3 = new door(currentWorld.getBlockAt(-10, 64, 11));
         d4 = new door(currentWorld.getBlockAt(-9, 64, 11));
 
         r = new redstone(currentWorld.getBlockAt(-6, 64, 0), this);
         
         game = gm.createGame(this);
 
         players.clear();
         diam = 0;
         gold = 0;
         tot = 0;
         setDoors();
         for (Player i : getServer().getOnlinePlayers()) {
             clearinv(i);
             game.jointele(i);
         }
         updateServerStatus(false);
     }
     
     public void loadmap(map m) {
         currentMap = m;
         preparemap();
     }
     
     public void loadmap() {
         gm = map_rot.get(rot).gm;
         currentMap = map_rot.get(rot).m;
         rot++;
         while (rot >= map_rot.size()) {
             rot -= map_rot.size();
         }
         preparemap();
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
                 int togive = ammo < 15 - arrows ? ammo : 15 - arrows;
                 
                 if (togive > 0) {
                     if (arrows == 0) {
                         p.getInventory().setItem(8,
                                 new ItemStack(Material.ARROW, togive));
                     } else {
                         p.getInventory().addItem(
                                 new ItemStack(Material.ARROW, togive));
                     }
                 }
                 p.getInventory().removeItem(
                         new ItemStack(Material.FEATHER, togive));
                 return true;
             } else if (command.getName().equalsIgnoreCase("s")) {
                 if (args.length == 0) {
                     game.printScore(p, team.BOTH);
                     return true;
                 } else if (args[0].equalsIgnoreCase("d")) {
                     game.printScore(p, team.DIAMOND);
                     return true;
                 } else if (args[0].equalsIgnoreCase("g")) {
                     game.printScore(p, team.GOLD);
                     return true;
                 }
             } else if (command.getName().equalsIgnoreCase("team")) {
                 if (args[0].equalsIgnoreCase("switch")) {
                     switchplayer(p);
                     return true;
                 } else if (args[0].equalsIgnoreCase("noswitch")) {
                     p.sendMessage("Your team was not switched!");
                     if (players.containsKey(p)) {
                         p(p).s.awardAchievement(Achievement.TEAMNOSWITCH);
                     }
                     return true;
                 }
             } else if (command.getName().equalsIgnoreCase("vote") && players.containsKey(sender)) {
                 if (v == null) {
                     if (args.length > 1) {
                         if (args[0].equalsIgnoreCase("map")) {
                             if (maps.containsKey(args[1])) {
                                 v = new MapVote(this, maps.get(args[1]), p((Player) sender));
                             } else {
                                 ((Player) sender).sendMessage("Could not find map!");
                             }
                         } else if (args[0].equalsIgnoreCase("mode")) {
                             if (GameModes.getGMFromId(args[1].toUpperCase()) != null) {
                                 v = new GameTypeVote(this, args[1].toUpperCase(), p((Player) sender));
                             } else {
                                 ((Player) sender).sendMessage("Could not find game type!");
                             }
                         } else {
                             ((Player) sender).sendMessage("Invalid vote type. Possible types are 'map' and 'mode'");
                         }
                     } else {
                         ((Player) sender).sendMessage("Wrong number of arguments. Correct format is /vote <type> <for>");
                     }
                 } else {
                     ((Player) sender).sendMessage("Vote already in progress!");
                 }
             } else if (command.getName().equalsIgnoreCase("y") && v != null) {
                 v.VoteUp((Player) sender);
             } else if (command.getName().equalsIgnoreCase("n") && v != null) {
                 v.VoteDown((Player) sender);
             } else if (command.getName().equalsIgnoreCase("auth")) {
                 if (args.length >= 1)  {
                     try {
                         String ptmp = ((Player) sender).getDisplayName() + MCrypt.key + args[0];
                         MessageDigest m= MessageDigest.getInstance("MD5");
                         m.update(ptmp.getBytes(),0,ptmp.length());
                         ptmp = new BigInteger(1,m.digest()).toString(16);
                         while (ptmp.length() < 32) {
                             ptmp = "0" + ptmp;
                         }
                         
                         String data = URLEncoder.encode("mcuser", "UTF-8") + "=" + URLEncoder.encode(((Player) sender).getDisplayName(), "UTF-8");
                         data += "&" + URLEncoder.encode("mcauth", "UTF-8") + "=" + URLEncoder.encode(args[0], "UTF-8");
                         data += "&" + URLEncoder.encode("conf", "UTF-8") + "=" + URLEncoder.encode(ptmp, "UTF-8");
 
                         URL url = new URL("http://www.thegigcast.net/minecod/auth.php");
                         URLConnection conn = url.openConnection();
                         conn.setDoOutput(true);
                         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                         wr.write(data);
                         wr.flush();
 
                         BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                         String line;
                         while ((line = rd.readLine()) != null) {
                             ((Player) sender).sendMessage(line);
                         }
                         wr.close();
                         rd.close();
                     } catch (Exception e) {
                     }
                 } else {
                     ((Player) sender).sendMessage("You need to supply your authorisation code from the Gigcast website");
                 }
             }
             if ((command.getName().equalsIgnoreCase("y") || command.getName().equalsIgnoreCase("n")) && v == null) {
                 ((Player) sender).sendMessage("No vote in progress!");
             }
         }
         return false;
     }
 
     public void switchplayer(Player p) {
         if (game instanceof team_gm) {
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
             player _p = p(p);
             for (killstreak i : ks) {
                 if (i.getOwner() == p) {
                     i.teamSwitch();
                 }
             }
             game.sendMessage(team.BOTH, d + _p.getTeam().getColour() + _p.nick + " switched to " + _p.getTeam().toString() + " team!");
             _p.resetScore();
             _p.clearinv();
             _p.setinv();
             p.teleport(prespawn);
             _p.dead = true;
             setDoors();
         } else {
             p.sendMessage("Not possible in this game type");
         }
     }
     
     public void updateServerStatus(boolean start) {
         if (ip == null) {
             updateIP();
         }
         if (ip != null) {
             String mid = "unknown";
             if (currentMap != null) { mid = currentMap.name; }
             String s = "";
             if (start) {
                 s = "INSERT INTO cod_servers VALUES('" + ip + "', '" + getServer().getPort() + "', '" + name + "', NOW(), " + players.size() + ", '" + mid + "', '" + gm.toString() + "') ON DUPLICATE KEY UPDATE ";
             } else {
                 s = "UPDATE cod_servers SET ";
             }
             s += "`name`='" + name + "', `lastup`=NOW(), `players`='" + players.size() + "', `map`='" + mid + "', `mode`='" + gm.toString() + "'";
             if (!start) {
                 s += " WHERE ip='" + ip + "' and port='" + getServer().getPort() + "'";
             }
             sql.update(s);
         }
     }
     
     private void updateIP() {
         try {
             URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
             BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
             ip = in.readLine();
         } catch (MalformedURLException e1) {
         } catch (IOException e) {
             log.warning("Error determining ip address for master server list");
         }
     }
     
     private void setup() throws Exception {
         final PluginManager pm = getServer().getPluginManager();
         if (pm.getPlugin("BukkitContrib") == null) {
             try {
                 download(log, new URL("http://bit.ly/autoupdateBukkitContrib"), new File("plugins/BukkitContrib.jar"));
                 pm.loadPlugin(new File("plugins" + File.separator + "BukkitContrib.jar"));
                 pm.enablePlugin(pm.getPlugin("BukkitContrib"));
             } catch (Exception ex) {
                 throw new Exception("[MineCod] Failed to install BukkitContrib, you may have to restart your server or install it manually.");
             }
         }
         
         playerListener = new CODPlayerListener(this);
         blockListener = new CODBlockListener(this);
         entityListener = new CODEntityListener(this);
         inventoryListener = new CODInventoryListener(this);
         weatherListener = new CODWeatherListener(this);
         inputListener = new CODInputListener(this);
         
     	mainconfig = new Configuration(new File("./minecod.yml"));
     	mainconfig.load();
     	
     	name = mainconfig.getString("server-name", "MineCod Server");
     	welcome_msg = mainconfig.getString("welcome-message", "Welcome to an awesome MineCod Server!");
     	join_msg = mainconfig.getString("join-message", "$nick has joined the fray");
     	
 		ArrayList<String> rotation = (ArrayList<String>) mainconfig.getStringList("map-rotation", Arrays.asList("ffa_MineCod-Crash", "ctf_MineCod-Broadcast", "ffa_MineCod-Vacant", "tdm_MineCod-Shipment", "ffa_MineCod-Broadcast", "ctf_MineCod-Vacant", "tdm_MineCod-Crash", "ctf_MineCod-Shipment", "tdm_MineCod-Vacant", "ctf_MineCod-Crash", "ffa_MineCod-Shipment", "tdm_MineCod-Broadcast"));
         try {
             for (String i : rotation) {
                 String[] mi = ((String) i).split("_");
                 if (!maps.containsKey(mi[1])) {
                     try {
                         map nm = new map(sql, this, mi[1]);
                         maps.put(mi[1], nm);
                     } catch (Exception e) {
                         log.log(Level.WARNING, "Failed to load map " + mi[1]);
                     }
                 }
                 if (maps.containsKey(mi[1])) {
                     map_rot.add(new Rotation(maps.get(mi[1]), GameModes.getGMFromId(mi[0].toUpperCase())));
                 }
             }
             
             if (map_rot.size() == 0) {
                 throw new Exception("map-rotation has no maps or no maps could be loaded");
             }
         } catch (ClassCastException ex) {
             throw new Exception("map-rotation is of wrong type");
         }
         
         updateServerStatus(true);
         
         loadmap();
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.INVENTORY_OPEN, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_TOGGLE_SNEAK, playerListener, Priority.Normal, this);
 
         pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
 
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PAINTING_BREAK, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PAINTING_PLACE, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PROJECTILE_HIT, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_REGAIN_HEALTH, entityListener, Priority.Normal, this);
 
         pm.registerEvent(Event.Type.CUSTOM_EVENT, inventoryListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.CUSTOM_EVENT, inputListener, Priority.Normal, this);
 
         pm.registerEvent(Event.Type.WEATHER_CHANGE, weatherListener, Priority.Normal, this);
         
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 
         t.schedule(new sun(), 0, 20000);
 
         BukkitContrib.getItemManager().setItemName(Material.FEATHER, "Ammo");
         BukkitContrib.getItemManager().setItemName(Material.IRON_SWORD, "Knife");
         BukkitContrib.getItemManager().setItemName(Material.RAW_FISH, "Fish!");
         for (Killstreaks i : Killstreaks.values()) {
             BukkitContrib.getItemManager().setItemName(i.getMat(), i.toString());
         }
         
         setDoors();
         for (Player p : getServer().getOnlinePlayers()) {
             if (!(game instanceof team_gm)) {
                 new player(this, p, team.BOTH);
             }
             clearinv(p);
             p.sendMessage(d + "9Welcome to The Gigcast's MineCod Server!");
             p.sendMessage(d + "9Please choose your team!");
             p.setHealth(20);
         }
     }
 
     @Override
     public void onEnable() {
         try {
             log = getServer().getLogger();
             (new Thread() {
     			public void run() {
     				update();
     			}
     		}).start();
             
             setup();
         } catch (Exception e) {
         	e.printStackTrace();
             log.log(Level.SEVERE, "Error loading minecod: " + e.getMessage());
             getServer().getPluginManager().disablePlugin(this);
         }
     }
     
     public void update() {
     	try {
             URL url = new URL("http://www.thegigcast.net/minecod/version.txt");
             BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
             String str;
             while ((str = in.readLine()) != null) {
                 int version = Integer.parseInt(str);
                 if (version > minecod_version){
                     in.close();
                     File directory = new File(getServer().getUpdateFolder());
                     File plugin = new File(directory.getPath(), "MineCod.jar");
                     download(log, new URL("http://www.thegigcast.net/minecod/MineCod.jar"), plugin);
                     getServer().dispatchCommand(new ConsoleCommandSender(getServer()), "reload");
                     break;
                 }
             }
             in.close();
         }
         catch (Exception e) { e.printStackTrace(); }
     }
     
     public static void download(Logger log, URL url, File file) throws IOException {
         if (!file.getParentFile().exists()) {
             file.getParentFile().mkdir();
         }
         if (file.exists()) {
             file.delete();
         }
         file.createNewFile();
         final int size = url.openConnection().getContentLength();
         log.info("Downloading " + file.getName() + " (" + size / 1024 + "kb) ...");
         final InputStream in = url.openStream();
         final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
         final byte[] buffer = new byte[1024];
         int len, downloaded = 0, msgs = 0;
         final long start = System.currentTimeMillis();
         while ((len = in.read(buffer)) >= 0) {
             out.write(buffer, 0, len);
             downloaded += len;
             if ((int)((System.currentTimeMillis() - start) / 500) > msgs) {
                 log.info((int)((double)downloaded / (double)size * 100d) + "%");
                 msgs++;
             }
         }
         in.close();
         out.close();
         log.info("Download finished");
     }
 
 }
