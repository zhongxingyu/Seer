 package me.ellbristow.simplespawnliteback;
 
 import java.util.HashMap;
 import me.ellbristow.simplespawnliteback.listeners.PlayerListener;
 import me.ellbristow.simplespawnlitecore.LocationType;
 import me.ellbristow.simplespawnlitecore.SimpleSpawnLiteCore;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleSpawnLiteBack extends JavaPlugin {
 
     private SimpleSpawnLiteBack plugin = this;
     private boolean setHomeWithBeds = false;
     private FileConfiguration config;
     private SimpleSpawnLiteCore ss;
     private String[] backColumns = {"player", "world", "x", "y", "z", "yaw", "pitch"};
     private String[] backDims = {"TEXT NOT NULL PRIMARY KEY", "TEXT NOT NULL", "DOUBLE NOT NULL DEFAULT 0", "DOUBLE NOT NULL DEFAULT 0", "DOUBLE NOT NULL DEFAULT 0", "FLOAT NOT NULL DEFAULT 0", "FLOAT NOT NULL DEFAULT 0"};
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("Sorry! Command " + commandLabel + " cannot be run from the console!");
             return false;
         }
         
         Player player = (Player)sender;
         
        if (commandLabel.equalsIgnoreCase("back") || commandLabel.equalsIgnoreCase("sback")) {
             if (!player.hasPermission("simplespawn.back")) {
                 player.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
                 return false;
             }
 
             Location backLoc = getBackLoc(player.getName());
             ss.simpleTeleport(player, backLoc, LocationType.OTHER);
             return true;
             
         }
         
         return true;
     }
     
     public void setBackLoc(String playerName, Location loc) {
         String world = loc.getWorld().getName();
         double x = loc.getX();
         double y = loc.getY();
         double z = loc.getZ();
         float yaw = loc.getYaw();
         float pitch = loc.getPitch();
         ss.sql.query("INSERT OR REPLACE INTO BackSpawns (player, world, x, y, z, yaw, pitch) VALUES ('" + playerName + "', '" + world + "', " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch + ")");
     }
     
     public Location getBackLoc(String playerName) {
         HashMap<Integer, HashMap<String, Object>> result = ss.sql.select("world, x, y, z, yaw, pitch", "BackSpawns", "player = '" + playerName + "'", null, null);
         Location location;
         if (result == null || result.isEmpty()) {
             // if you haven't used /sethome - first home is your bed
             location = getServer().getOfflinePlayer(playerName).getBedSpawnLocation();
         } else {
             String world = (String) result.get(0).get("world");
             double x = (Double) result.get(0).get("x");
             double y = (Double) result.get(0).get("y");
             double z = (Double) result.get(0).get("z");
             float yaw = Float.parseFloat(result.get(0).get("yaw").toString());
             float pitch = Float.parseFloat(result.get(0).get("pitch").toString());
             location = new Location(getServer().getWorld(world), x, y, z, yaw, pitch);
         }
         return location;
     }
 
     public boolean getSetting(String setting) {
         if (setting.equalsIgnoreCase("SetHomeWithBeds")) return setHomeWithBeds;
         return false;
     }
     
     @Override
     public void saveConfig() {
         ss.saveConfig();
     }
 
     @Override
     public void onDisable() {
         ss.sql.close();
     }
 
     @Override
     public void onEnable() {
         
         ss = SimpleSpawnLiteCore.getPluginLink();
         
         getServer().getPluginManager().registerEvents(new PlayerListener(plugin), plugin);
         
         if (!ss.sql.checkTable("PlayerBacks")) {
             ss.sql.createTable("PlayerBacks", backColumns, backDims);
         }
         
     }
 
 }
