 package com.norcode.bukkit.telewarp;
 
 import com.norcode.bukkit.telewarp.commands.*;
 import com.norcode.bukkit.telewarp.commands.home.DelHomeCommand;
 import com.norcode.bukkit.telewarp.commands.home.HomeCommand;
 import com.norcode.bukkit.telewarp.commands.home.HomesCommand;
 import com.norcode.bukkit.telewarp.commands.home.SetHomeCommand;
 import com.norcode.bukkit.telewarp.commands.tpa.TPACommand;
 import com.norcode.bukkit.telewarp.commands.tpa.TPAHereCommand;
 import com.norcode.bukkit.telewarp.commands.tpa.TPANoCommand;
 import com.norcode.bukkit.telewarp.commands.tpa.TPAYesCommand;
 import com.norcode.bukkit.telewarp.commands.warp.DelWarpCommand;
 import com.norcode.bukkit.telewarp.commands.warp.SetWarpCommand;
 import com.norcode.bukkit.telewarp.commands.warp.WarpCommand;
 import com.norcode.bukkit.telewarp.commands.warp.WarpsCommand;
 import com.norcode.bukkit.telewarp.persistence.home.*;
 import com.norcode.bukkit.telewarp.util.ConfigAccessor;
 import com.norcode.bukkit.telewarp.persistence.warp.BaseWarpManager;
 import com.norcode.bukkit.telewarp.persistence.warp.SQLWarpManager;
 import com.norcode.bukkit.telewarp.persistence.warp.Warp;
 import com.norcode.bukkit.telewarp.persistence.warp.YamlWarpManager;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class Telewarp extends JavaPlugin {
     private Pattern validNamePattern = Pattern.compile("^[\\w\\d-]+$");
     public Permission permission = null;
     public Economy economy = null;
     private BaseWarpManager warpManager;
     private BaseHomeManager homeManager;
     private ConfigAccessor messages;
     private SortedSet<Map.Entry<String, Integer>> multipleHomeLimits;
     private SortedSet<Map.Entry<String, Integer>> groupCooldownLimits;
 
     private HashMap<String, Long> cooldowns;
 
     public HomesCommand homesCommand;
     public WarpsCommand warpsCommand;
 
     @Override
     public void onEnable() {
        enableVault();
         saveDefaultConfig();
         getConfig().options().copyDefaults(true);
         saveConfig();
         setGroupLimits();
         messages = new ConfigAccessor(this, "messages.yml");
         messages.getConfig();
         messages.saveDefaultConfig();
         messages.getConfig().options().copyDefaults(true);
         messages.saveConfig();
         if (getConfig().getBoolean("database", false)) {
             this.warpManager = new SQLWarpManager(this);
             this.homeManager = new SQLHomeManager(this);
         } else {
             this.warpManager = new YamlWarpManager(this);
             this.homeManager = new YamlHomeManager(this);
         }
         loadSavedCooldowns();
         getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
         getServer().getPluginCommand("home").setExecutor(new HomeCommand(this));
         getServer().getPluginCommand("sethome").setExecutor(new SetHomeCommand(this));
         getServer().getPluginCommand("delhome").setExecutor(new DelHomeCommand(this));
         getServer().getPluginCommand("tpa").setExecutor(new TPACommand(this));
         getServer().getPluginCommand("tpahere").setExecutor(new TPAHereCommand(this));
         getServer().getPluginCommand("tpayes").setExecutor(new TPAYesCommand(this));
         getServer().getPluginCommand("tpano").setExecutor(new TPANoCommand(this));
         getServer().getPluginCommand("back").setExecutor(new BackCommand(this));
         getServer().getPluginCommand("warp").setExecutor(new WarpCommand(this));
         getServer().getPluginCommand("setwarp").setExecutor(new SetWarpCommand(this));
         getServer().getPluginCommand("delwarp").setExecutor(new DelWarpCommand(this));
         getServer().getPluginCommand("telewarp").setExecutor(new TelewarpCommand(this));
         getServer().getPluginCommand("tp").setExecutor(new TPCommand(this));
 
         homesCommand = new HomesCommand(this);
         warpsCommand = new WarpsCommand(this);
         getServer().getPluginCommand("warps").setExecutor(warpsCommand);
         getServer().getPluginCommand("homes").setExecutor(homesCommand);
 
     }
 
     private void loadSavedCooldowns() {
         ConfigAccessor accessor = new ConfigAccessor(this, "cooldowns.yml");
         accessor.getConfig();
         cooldowns = new HashMap<String, Long>();
         long now = System.currentTimeMillis();
         for (String key: accessor.getConfig().getKeys(false)) {
             long expiry = accessor.getConfig().getLong(key);
             if (expiry > now) {
                 cooldowns.put(key, expiry);
 
             }
             accessor.getConfig().set(key, null);
         }
         accessor.saveConfig();
     }
 
     public void reloadAll() {
         reloadConfig();
         setGroupLimits();
         messages.reloadConfig();
         this.warpManager.reload();
     }
 
     private void setGroupLimits() {
         ConfigurationSection sec = getConfig().getConfigurationSection("multiple-home-limits");
         HashMap<String, Integer> limits = new HashMap<String, Integer>();
         for (String key: sec.getKeys(false)) {
             limits.put(key, sec.getInt(key));
         }
         multipleHomeLimits = entriesSortedByValues(limits, true);
         // cooldowns
         sec = getConfig().getConfigurationSection("group-cooldowns");
         HashMap<String, Integer> cdmap = new HashMap<String, Integer>();
         for (String key: sec.getKeys(false)) {
             cdmap.put(key, sec.getInt(key));
         }
         groupCooldownLimits = entriesSortedByValues(cdmap, false);
 
     }
 
    private void enableVault() {

		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().warning("Vault not found.  Group warmup/cooldown timers and teleportation costs will not be available");
			return;
		}
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
        } else {
			getLogger().warning("No Vault-compatible permissions plugin found. Group warmup/cooldown timers will not be available.");
		}
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
        } else {
			getLogger().warning("No Vault-compatible economy plugin found. teleportation costs will not be available.");
		}

     }
 
     public BaseWarpManager getWarpManager() {
         return warpManager;
     }
 
     public String getMsg(String key, Object... args) {
         String tpl = messages.getConfig().getString(key);
         if (tpl == null) {
             tpl = "[" + key + "] ";
             for (int i=0;i< args.length;i++) {
                 tpl += "{"+i+"}, ";
             }
         }
         return new MessageFormat(ChatColor.translateAlternateColorCodes('&', tpl)).format(args);
     }
 
     public void debug(Object s) {
         if (getConfig().getBoolean("debug", false)) {
             getLogger().info(s.toString());
         }
     }
 
     public String serializeLocation(Location l) {
         if (l == null) {
             return null;
         }
         return String.format("%s;%s;%s;%s;%s;%s", l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
     }
 
     public Location deserializeLocation(String s) {
         String[] parts = s.split(";");
         Location loc = null;
         try {
             World world = getServer().getWorld(parts[0]);
             loc = new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
             if (parts.length >= 5) {
                 loc.setYaw(Float.parseFloat(parts[4]));
             }
             if (parts.length >= 6) {
                 loc.setPitch(Float.parseFloat(parts[5]));
             }
         } catch (ArrayIndexOutOfBoundsException ex) {
         } catch (IllegalArgumentException ex) {
         }
         return loc;
     }
 
 
     public double getCost(Player player, String commandName) {
         double cost = getConfig().getDouble("commands." + commandName.toLowerCase() + ".cost", getConfig().getDouble("defaults.cost"));
         if (player.hasPermission("telewarp.nocost." + commandName.toLowerCase())) {
             return 0;
         } else if (player.hasPermission("telewarp.halfcost.*" + commandName.toLowerCase())) {
             return cost/2.0D;
         }
         return cost;
     }
 
     public long getWarmup(Player player, String commandName) {
         long warmup = getConfig().getLong("commands." + commandName.toLowerCase() + ".warmup", getConfig().getLong("defaults.warmup"));
         if (player.hasPermission("telewarp.nowarmup." + commandName.toLowerCase())) {
             return 0;
         } else if (player.hasPermission("telewarp.halfwarmup." + commandName.toLowerCase())) {
             return warmup/2;
         }
         return warmup;
 
     }
 
     public void setPlayerMeta(Player player, String metaKey, Object value) {
         player.setMetadata(metaKey, new FixedMetadataValue(this, value));
     }
 
     public void onDisable() {
         warpManager.saveAll();
         homeManager.saveAll();
         saveCooldowns();
     }
 
     private void saveCooldowns() {
         ConfigAccessor accessor = new ConfigAccessor(this, "cooldowns.yml");
 
         long now = System.currentTimeMillis();
         for (String key: cooldowns.keySet()) {
             long expiry = cooldowns.get(key);
             if (expiry > now) {
                 accessor.getConfig().set(key, cooldowns.get(key));
             }
         }
         accessor.saveConfig();
     }
 
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> classes = new ArrayList<Class<?>>();
         classes.add(Warp.class);
         classes.add(Home.class);
         classes.add(PlayerHomeName.class);
         return classes;
     }
 
     public void initDB() {
         installDDL();
     }
 
     public BaseHomeManager getHomeManager() {
         return homeManager;
     }
 
     public static Location lookAt(Location loc, Location lookat) {
         //Clone the loc to prevent applied changes to the input loc
         loc = loc.clone();
 
         // Values of change in distance (make it relative)
         double dx = lookat.getX() - loc.getX();
         double dy = lookat.getY() - loc.getY();
         double dz = lookat.getZ() - loc.getZ();
 
         // Set yaw
         if (dx != 0) {
             // Set yaw start value based on dx
             if (dx < 0) {
                 loc.setYaw((float) (1.5 * Math.PI));
             } else {
                 loc.setYaw((float) (0.5 * Math.PI));
             }
             loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
         } else if (dz < 0) {
             loc.setYaw((float) Math.PI);
         }
 
         // Get the distance from dx/dz
         double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
 
         // Set pitch
         loc.setPitch((float) -Math.atan(dy / dxz));
 
         // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
         loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
         loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
 
         return loc;
     }
 
     public int getPlayerMaxHomes(String name) {
         Player p = getServer().getPlayer(name);
         for (Map.Entry<String, Integer> e: multipleHomeLimits) {
             if (permission.playerInGroup(p, e.getKey())) {
                 return e.getValue();
             }
         }
         return getConfig().getInt("default-home-limit", 1);
     }
 
     public long getPlayerCooldown(Player player) {
         for (Map.Entry<String, Integer> e: groupCooldownLimits) {
             if (permission.playerInGroup(player, e.getKey())) {
                 return e.getValue();
             }
         }
         return getConfig().getInt("default-cooldown", 1800000); // 30 mins
     }
 
     static <K,V extends Comparable<? super V>>
     SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map, final boolean reverse) {
         SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                 new Comparator<Map.Entry<K,V>>() {
                     @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                         // never return equals or the group gets removed.
                         if (reverse) {
                             if (e2.getValue() == e1.getValue()) return 1;
                             return e2.getValue().compareTo(e1.getValue());
                         } else {
                             if (e2.getValue() == e1.getValue()) return 1;
                             return e1.getValue().compareTo(e2.getValue());
                         }
                     }
                 }
         );
         sortedEntries.addAll(map.entrySet());
         return sortedEntries;
     }
 
     public Pattern getValidNamePattern() {
         return validNamePattern;
     }
 
     public HashMap<String, Long> getCooldowns() {
         return cooldowns;
     }
 }
