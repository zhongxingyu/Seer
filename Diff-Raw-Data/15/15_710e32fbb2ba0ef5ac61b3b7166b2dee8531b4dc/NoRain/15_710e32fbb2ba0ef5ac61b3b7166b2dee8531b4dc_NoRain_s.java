 package me.ellbristow.norain;
 
 import java.util.HashMap;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.weather.WeatherChangeEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NoRain extends JavaPlugin implements Listener {
 
     private FileConfiguration config;
     private HashMap<String, Boolean> rainWorlds = new HashMap<String, Boolean>();
     
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
         config = getConfig();
         List<World> worlds = getServer().getWorlds();
         int worldCount = worlds.size();
         for (int i = 0; i < worldCount; i++) {
             World thisWorld = worlds.get(i);
             if (thisWorld.getEnvironment().equals(Environment.NORMAL)) {
                 boolean setting = config.getBoolean(thisWorld.getName(), true);
                 rainWorlds.put(thisWorld.getName(), setting);
                 config.set(thisWorld.getName(), setting);
             }
         }
         saveConfig();
     }
     
     @Override
     public void onDisable() {
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onRainStart(WeatherChangeEvent event) {
         if (!event.isCancelled()) {
            if (event.toWeatherState()) {
                 event.setCancelled(true);
             }
         }
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (commandLabel.equalsIgnoreCase("norain")) {
             if (!sender.hasPermission("norain.use")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
             } else {
                 if (args.length == 0) {
                     if (!(sender instanceof Player)) {
                         sender.sendMessage("You must specify a world when using this command from the console!");
                         sender.sendMessage("norain [world] {on|off}");
                         return false;
                     }
                     Player player = (Player) sender;
                     String world = player.getWorld().getName();
                     boolean setting = rainWorlds.get(world);
                     if (setting) {
                         player.sendMessage(ChatColor.GOLD + "Rain is " + ChatColor.RED + "DISABLED" + ChatColor.GOLD + " in the current world!");
                     } else {
                         player.sendMessage(ChatColor.GOLD + "Rain is " + ChatColor.GREEN + "ENABLED" + ChatColor.GOLD + " in the current world!");
                     }
                 } else if (args.length == 1) {
                     String world = args[0];
                     if (getServer().getWorld(world) == null) {
                         sender.sendMessage(ChatColor.RED + "World " + ChatColor.WHITE + world + ChatColor.RED + " could not be found!");
                         return false;
                     }
                     if (!getServer().getWorld(world).getEnvironment().equals(Environment.NORMAL)) {
                         sender.sendMessage(ChatColor.RED + "You can only check rain status in normal worlds!");
                         return false;
                     }
                     boolean setting = rainWorlds.get(world);
                     if (setting) {
                         sender.sendMessage(ChatColor.GOLD + "Rain is " + ChatColor.RED + "DISABLED" + ChatColor.GOLD + " in the current world!");
                     } else {
                         sender.sendMessage(ChatColor.GOLD + "Rain is " + ChatColor.GREEN + "ENABLED" + ChatColor.GOLD + " in the current world!");
                     }
                 } else if (args.length == 2) {
                     String world = args[0];
                     if (getServer().getWorld(world) == null) {
                         sender.sendMessage(ChatColor.RED + "World " + ChatColor.WHITE + world + ChatColor.RED + " could not be found!");
                         return false;
                     }
                     if (!getServer().getWorld(world).getEnvironment().equals(Environment.NORMAL)) {
                         sender.sendMessage(ChatColor.RED + "You can only set rain status in normal worlds!");
                         return false;
                     }
                     if (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off")) {
                         sender.sendMessage(ChatColor.RED + "Setting not recognised!");
                         sender.sendMessage(ChatColor.RED + "/norain [world] {on|off}");
                         return false;
                     }
                     if (args[1].equalsIgnoreCase("on")) {
                         rainWorlds.put(world, true);
                         sender.sendMessage(ChatColor.GOLD + "Rain is " + ChatColor.RED + "DISABLED" + ChatColor.GOLD + " in the current world!");
                         return true;
                     } else {
                        rainWorlds.put(world, true);
                         sender.sendMessage(ChatColor.GOLD + "Rain is " + ChatColor.GREEN + "ENABLED" + ChatColor.GOLD + " in the current world!");
                         return true;
                     }
                 }
             }
         }
         return false;
     }
     
 }
