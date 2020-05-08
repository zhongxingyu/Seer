 package com.thepastimers.Metrics;
 
 import com.thepastimers.Chat.Chat;
 import com.thepastimers.Database.Database;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.Date;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 2/23/13
  * Time: 7:06 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Metrics extends JavaPlugin implements Listener {
     Database database;
     Chat chat;
 
     @Override
     public void onEnable() {
         getLogger().info("Metrics init");
 
         getServer().getPluginManager().registerEvents(this,this);
         database = (Database)getServer().getPluginManager().getPlugin("Database");
         if (database == null) {
             getLogger().warning("Cannot load Database plugin. Some functionality may not be available");
         }
 
         chat = (Chat)getServer().getPluginManager().getPlugin("Chat");
         if (chat == null) {
             getLogger().warning("Cannot load Chat plugin. Some functionality may not be available");
         }
 
         getLogger().info("Table info: ");
         getLogger().info(PlayerDeath.getTableInfo());
         getLogger().info(PlayerLogin.getTableInfo());
 
         getLogger().info("Metrics init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("Metrics disabled");
     }
 
     public int getDeathCount(String player) {
         if (database == null) {
             return 0;
         }
 
         List<PlayerDeath> deathList = (List<PlayerDeath>)database.select(PlayerDeath.class,"player = '" + database.makeSafe(player) + "'");
 
         if (deathList == null) {
             return 0;
         }
 
         return deathList.size();
     }
 
     public String commonCause(String player) {
         if (database == null) {
             return "";
         }
 
         List<PlayerDeath> deathList = (List<PlayerDeath>)database.select(PlayerDeath.class,"player = '" + database.makeSafe(player) + "'");
 
         if (deathList == null) {
             return "";
         }
 
         Map<String,Integer> causes = new HashMap<String,Integer>();
 
         for (PlayerDeath pd : deathList) {
             String cause = pd.getCause();
 
             if (!cause.equalsIgnoreCase("death")) {
                 Integer count = causes.get(cause);
                 if (count == null) {
                     count = new Integer(0);
                 }
                 count ++;
                 causes.put(cause,count);
             }
         }
 
         int highest = 0;
         String highCause = "";
 
         for (String key : causes.keySet()) {
             int count = causes.get(key);
 
             if (count > highest) {
                 highCause = key;
                 highest = count;
             }
         }
 
         return highCause + "|" + highest;
     }
 
     public long timeSpent(String player) {
         if (database == null) return 0;
 
         List<PlayerLogin> loginList = (List<PlayerLogin>)database.select(PlayerLogin.class,"player = '" + database.makeSafe(player) + "' AND event IN ('login','logout') ORDER BY id ASC");
 
         long total = 0;
         boolean loginSeen = false;
         Timestamp lastLogin = null;
         for (PlayerLogin pl : loginList) {
             if ("login".equalsIgnoreCase(pl.getEvent())) {
                 loginSeen = true;
                 lastLogin = pl.getDate();
             } else if ("logout".equalsIgnoreCase(pl.getEvent())) {
                 if (loginSeen == true) {
                     loginSeen = false;
                     total += (pl.getDate().getTime()-lastLogin.getTime());
                 }
             }
         }
 
         return total;
     }
 
     public String parseDate(long date) {
         long secsInMin = 60;
         long secsInHour = 60*secsInMin;
         long secsInDay = 24*secsInHour;
 
         int days = 0;
         while (date > secsInDay) {
             date -= secsInDay;
             days ++;
         }
 
         int hours = 0;
         while (date > secsInHour) {
             date -= secsInHour;
             hours ++;
         }
 
         int mins = 0;
         while (date > secsInMin) {
             date -= secsInMin;
             mins ++;
         }
 
         return days + " day/s, " + hours + " hour/s, " + mins + " minute/s, " + date + " second/s";
     }
 
     @EventHandler
     public void playerJoin(PlayerJoinEvent event) {
         if (database == null) {
             return;
         }
 
         Player p = event.getPlayer();
 
         int logins = getLoginCount(p.getName());
 
         long time = timeSpent(p.getName());
         time /= 1000;
 
         p.sendMessage("Welcome to Solumcraft!");
         p.sendMessage("You have logged in " + (logins+1) + " time/s and died " + getDeathCount(p.getName()) + " times.");
         p.sendMessage("You have spent " + parseDate(time) + " seconds on this server");
 
         if (logins == 0) {
             p.teleport(getServer().getWorld("world").getSpawnLocation());
             getLogger().info("Teleporting new player " + p.getName() + " to spawn.");
         }
 
         p.sendMessage(ChatColor.GREEN + "Get 2 diamonds each by voting at the following servers:");
         p.sendMessage(ChatColor.GREEN + "www.minecraft-server-list.com/server/127787");
         p.sendMessage(ChatColor.GREEN + "www.mcserverlist.net/servers/516ba260041b26153700019e");
         p.sendMessage(ChatColor.GREEN + "http://minecraftservers.org/server/68085");
         p.sendMessage(ChatColor.GREEN + "http://minecraftservers.net/server/64066/");
     }
 
     public int getLoginCount(String player) {
         if (database == null || player == null) {
             return 1;
         }
 
         List<PlayerLogin> logins = (List<PlayerLogin>)database.select(PlayerLogin.class,"player = '" + database.makeSafe(player) + "' AND event = 'login'");
 
         if (logins == null) {
             // database issues
             return 1;
         }
 
         return logins.size();
     }
 
     @EventHandler
     public void playerDeath(PlayerDeathEvent event) {
         Player player = (Player)event.getEntity();
 
 
         PlayerDeath pd = new PlayerDeath();
 
         String message = event.getDeathMessage();
         if (message == null) message = "";
 
         pd.setCause("death");
         if (message.contains("hit the ground too hard") || message.contains("doomed to fall") || message.contains("fell from a high place")) {
             pd.setCause("Falling");
         } else if (message.contains("shot by Skeleton")) {
             pd.setCause("Skeleton");
         } else if (message.contains("slain by Zombie Pigman")) {
             pd.setCause("Pig Zombie");
         } else if (message.contains("tried to swim in lava")) {
             pd.setCause("Lava");
         } else if (message.contains("blew up")) {
             pd.setCause("Explosion");
         } else if (message.contains("drowned")) {
             pd.setCause("Drowning");
         } else if (message.contains("suffocated in a wall")) {
             pd.setCause("Suffocation");
         } else if (message.contains("slain by Enderman")) {
             pd.setCause("Enderman");
         } else if (message.contains("slain by Spider")) {
             pd.setCause("Spider");
         } else if (message.contains("slain by Zombie")) {
             pd.setCause("Zombie");
         } else if (message.contains("burned to death") || message.contains("burnt to a crisp")) {
             pd.setCause("Fire");
         } else if (message.equalsIgnoreCase(player.getName() + " died")) {
             pd.setCause("Suicide");
         } else if (message.contains("starved to death")) {
             pd.setCause("Starvation");
         } else if (message.contains("blown up by Creeper")) {
             pd.setCause("Creeper");
        } else if (message.contains("shot by")) {
            pd.setCause("Killed");
        } else if (message.contains("slain by")) {
            pd.setCause("Killed");
         } else {
             getLogger().info("Unknown death: " + message);
         }
 
         java.util.Date now = new java.util.Date();
         pd.setDate(new Date(now.getTime()));
         pd.setPlayer(player.getName());
         pd.save(database);
 
         int deathCount = getDeathCount(player.getName());
 
         String causeString = commonCause(player.getName());
 
         String cause = "";
         int count = 0;
 
 
         String[] causeArr = causeString.split("\\|");
         if (causeArr.length >= 2) {
             cause = causeArr[0];
             count = Integer.parseInt(causeArr[1]);
         }
 
         //event.setDeathMessage("");
 
         String output = ChatColor.LIGHT_PURPLE + message + ". You have now died " + deathCount + " time/s. ";
         if (!"".equalsIgnoreCase(cause)) {
             output += "Most common cause of death: " + cause + " (" + count + " times)";
         }
 
         getServer().broadcastMessage(output);
         //player.sendMessage(output);
 
         if (chat != null) {
             chat.saveMessage("Player " + player.getName() + " has died (" + pd.getCause() + ").",":red:Server");
         }
 
         Location l = player.getLocation();
         player.sendMessage("You have died at " + l.getWorld().getName() + "(" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")");
     }
 
     @EventHandler
     public void login(PlayerJoinEvent event) {
         if (database == null) {
             return;
         }
 
 
         Player p = event.getPlayer();
         Location l = p.getLocation();
         PlayerLogin pl = new PlayerLogin();
         pl.setPlayer(p.getName());
         pl.setEvent("login");
         java.util.Date today = new java.util.Date();
         Timestamp stamp = new Timestamp(today.getTime());
         pl.setDate(stamp);
         pl.setX(l.getBlockX());
         pl.setY(l.getBlockY());
         pl.setZ(l.getBlockZ());
 
         pl.save(database);
 
         if (chat != null) {
             chat.saveMessage("Player " + p.getName() + " has joined.",":red:Server");
         }
     }
 
     @EventHandler
     public void logout(PlayerQuitEvent event) {
         if (database == null) {
             return;
         }
 
         Player p = event.getPlayer();
         Location l = p.getLocation();
         PlayerLogin pl = new PlayerLogin();
         pl.setPlayer(p.getName());
         pl.setEvent("logout");
         java.util.Date today = new java.util.Date();
         Timestamp stamp = new Timestamp(today.getTime());
         pl.setDate(stamp);
         pl.setX(l.getBlockX());
         pl.setY(l.getBlockY());
         pl.setZ(l.getBlockZ());
 
         pl.save(database);
 
         if (chat != null) {
             chat.saveMessage("Player " + p.getName() + " has left.",":red:Server");
         }
     }
 }
