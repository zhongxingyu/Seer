 package com.imjake9.simplejail;
 
 import com.imjake9.simplejail.api.JailInfo;
 import com.imjake9.simplejail.api.SimpleJailCommandListener;
 import com.imjake9.simplejail.api.SimpleJailCommandListener.Priority;
 import com.imjake9.simplejail.events.PlayerJailEvent;
 import com.imjake9.simplejail.events.PlayerUnjailEvent;
 import com.imjake9.simplejail.metrics.Metrics;
 import com.imjake9.simplejail.utils.MessageTemplate;
 import com.imjake9.simplejail.utils.Messager;
 import com.imjake9.simplejail.utils.Messaging;
 import com.imjake9.simplejail.utils.Messaging.MessageLevel;
 import com.imjake9.simplejail.utils.SerializableLocation;
 import com.platymuus.bukkit.permissions.Group;
 import com.platymuus.bukkit.permissions.PermissionsPlugin;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import net.milkbowl.vault.permission.Permission;
 import net.milkbowl.vault.permission.plugins.Permission_SuperPerms;
 import org.bukkit.Location;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class SimpleJail extends JavaPlugin {
     
     private static SimpleJail plugin = null;
     
     static {
         ConfigurationSerialization.registerClass(SerializableLocation.class, "BukkitLocation");
     }
     
     public ConsoleCommandSender console;
     private Messager messager;
     private Permission vaultPermissions;
     private PermissionsPlugin bukkitPermissions;
     private PermissionManager pexPermissions;
     private Location jailLoc;
     private Location unjailLoc;
     private String jailGroup;
     private YamlConfiguration jailed;
     private SimpleJailPlayerListener listener;
     private SimpleJailCommandHandler handler;
     
     /**
      * Gets an instance of the plugin. Returns null if not enabled.
      * 
      * @return 
      */
     public static SimpleJail getPlugin() {
         return plugin;
     }
     
     public static Messager getMessager() {
         return plugin.messager;
     }
     
     @Override
     public void onDisable() {
         // Remove instance
         plugin = null;
         this.saveJail();
     }
 
     @Override
     public void onEnable() {
         
         // Register instance
         plugin = this;
         
         // Init messager
         messager = new Messager(this);
         
         // Get console:
         console = this.getServer().getConsoleSender();
         
         // Load configuration:
         this.loadConfig();
         
         // Get permissions plugin:
         this.setupPermissions();
         if(!this.isEnabled()) return;
         
         // Set up handlers
         listener = new SimpleJailPlayerListener(this);
         this.getServer().getPluginManager().registerEvents(listener, this);
         handler = new SimpleJailCommandHandler(this);
         
         // Register auto-unjail code
         this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
             @Override
             public void run() {
                 long currentTime = System.currentTimeMillis();
                 
                 for (Player p : getServer().getOnlinePlayers()) {
                     if (!playerIsJailed(p) || !playerIsTempJailed(p)) continue;
 
                     double tempTime = getTempJailTime(p);
 
                     if (tempTime <= currentTime) {
                         try {
                             unjailPlayer(p.getName());
                         } catch (JailException ex) {
                             // Should never happen
                             ex.printStackTrace();
                         }
                         messager.info(JailMessage.UNTEMPJAILED, p.getName());
                     }
                 }
                 
             }
             
         }, 600, 600);
         
         // Create Metrics handler
         try {
             Metrics metrics = new Metrics(plugin);
             metrics.start();
         } catch (IOException ex) {}
         
     }
     
     /**
      * Sends a player to jail.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param jailee
      * @param jailer
      * @return the JailInfo object associated with this jailing
      * @throws JailException 
      */
     public JailInfo jailPlayer(String jailee, String jailer) throws JailException {
         return this.jailPlayer(jailee, jailer, -1, jailLoc);
     }
     
     /**
      * Sends a player to jail for a specific time.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param jailee
      * @param jailer
      * @param time time in minutes
      * @return the JailInfo object associated with this jailing
      * @throws JailException 
      */
     public JailInfo jailPlayer(String jailee, String jailer, int time) throws JailException {
         return this.jailPlayer(jailee, jailer, time, jailLoc);
     }
     
     /**
      * Sends a player to jail to a location.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param jailee
      * @param jailer
      * @param loc
      * @return the JailInfo object associated with this jailing
      * @throws JailException 
      */
     public JailInfo jailPlayer(String jailee, String jailer, Location loc) throws JailException {
         return this.jailPlayer(jailee, jailer, -1, loc);
     }
     
     /**
      * Sends a player to jail for a specific time and to a location.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param jailee
      * @param jailer
      * @param time time in minutes
      * @param loc
      * @return the JailInfo object associated with this jailing
      * @throws JailException 
      */
     public JailInfo jailPlayer(String jailee, String jailer, int time, Location loc) throws JailException {
         JailInfo info = new JailInfo(jailer, jailee);
         info.setJailLocation(loc);
         return this.jailPlayer(info, time);
     }
     
     /**
      * Sends a player to jail using a provided JailInfo object.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param info the data for use with this jail
      * @return the JailInfo object associated with this jailing
      * @throws JailException 
      */
     public JailInfo jailPlayer(JailInfo info) throws JailException {
         return this.jailPlayer(info, -1);
     }
     
     /**
      * Sends a player to jail for a specific time using a provided JailInfo object.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param info the data for use with this jail
      * @param time time in minutes
      * @return the JailInfo object associated with this jailing
      * @throws JailException 
      */
     public JailInfo jailPlayer(JailInfo info, int time) throws JailException {
         
         // Autocomplete name if player is online:
         Player player = this.getServer().getPlayer(info.getJailee());
         String jailee = player == null || !player.isOnline() ? info.getJailee().toLowerCase() : player.getName().toLowerCase();
         
         // Dispatch event:
         PlayerJailEvent e = new PlayerJailEvent(info, time);
         this.getServer().getPluginManager().callEvent(e);
         
         // If event cancelled, take no action:
         if (e.isCancelled())
             return null;
         
         // Update time to event result
         time = e.getLength();
         
         // Check if player is slready jailed:
         if(jailed.get(jailee) != null && getPlayerStatus(jailee) == JailStatus.JAILED) {
             throw new JailException("Jailed player was sent jail message.", Messaging.fillArgs(JailMessage.ALREADY_IN_JAIL, jailee));
         }
         
         // Put player in jailed group:
         List<String> groupName = this.getGroups(jailee);
         jailed.set(jailee + ".groups", groupName);
         this.setGroups(jailee, Arrays.asList(new String[]{jailGroup}));
         
         // If tempjailing, set up tempjail time:
         if (time > 0) {
             double tempTime = System.currentTimeMillis() + (time * 60000);
            jailed.set(jailee + ".tempTime", tempTime);
         }
         
         // Move player into jail:
         if (player != null && player.isOnline())
             player.teleport(e.getJailLocation());
         else
             jailed.set(jailee + ".status", "pending");
         
         jailed.set(jailee + ".jailer", e.getInfo().getJailer());
         jailed.set(jailee + ".location.jail", new SerializableLocation(e.getInfo().getJailLocation()));
         jailed.set(jailee + ".location.unjail", new SerializableLocation(e.getInfo().getUnjailLocation()));
         jailed.set(jailee + ".data", e.getInfo().getProperties());
         
         this.saveJail();
         
         // Send message to player
         if (player != null && player.isOnline()) {
             if(time <= 0) Messaging.send(JailMessage.JAILED, player);
             else Messaging.send(JailMessage.TEMPJAILED, player, this.prettifyMinutes(time));
         }
         
         return e.getInfo();
         
     }
     
     /**
      * Removes a player from jail.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param name
      * @return the JailInfo object associated with this unjailing
      * @throws JailException 
      */
     public JailInfo unjailPlayer(String name) throws JailException {
        return this.unjailPlayer(name, unjailLoc);
     }
     
     /**
      * Removes a player from jail to a location.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param name
      * @param location
      * @return the JailInfo object associated with this unjailing
      * @throws JailException 
      */
     public JailInfo unjailPlayer(String name, Location loc) throws JailException {
         
         // Autocomplete name if player is online:
         Player player = this.getServer().getPlayer(name);
         name = player == null || !player.isOnline() ? name.toLowerCase() : player.getName().toLowerCase();
         
         // Dispatch event
         PlayerUnjailEvent e = new PlayerUnjailEvent(new JailInfo(jailed.getString(name + ".jailer"), name, getJailLocation(name), loc));
         if (jailed.getConfigurationSection(name + ".data") != null)
             e.getInfo().addProperties(jailed.getConfigurationSection(name + ".data").getValues(true));
         this.getServer().getPluginManager().callEvent(e);
         
         // If event cancelled, take no action:
         if (e.isCancelled())
             return null;
         
         // Check if player is in jail:
         if(jailed.get(name) == null) {
             throw new JailException("Player not in jail was sent unjail message.", Messaging.fillArgs(JailMessage.NOT_IN_JAIL, name));
         }
         
         // Check if player is offline:
         if (player == null || !player.isOnline()) {
             jailed.set(name + ".status", "freed");
             this.saveJail();
             return e.getInfo();
         }
         
         // Move player out of jail:
         player.teleport(e.getUnjailLocation());
         
         this.setGroups(name, jailed.getStringList(name + ".groups"));
         
         jailed.set(name, null);
         
         this.saveJail();
         
         Messaging.send(JailMessage.UNJAILED, player);
         
         return e.getInfo();
         
     }
     
     /**
      * Sets the jail point.
      * 
      * @param loc 
      */
     public void setJail(Location loc) {
         
         jailLoc = loc;
         
         YamlConfiguration config = (YamlConfiguration) this.getConfig();
         config.set("jail.x", (int) jailLoc.getX());
         config.set("jail.y", (int) jailLoc.getY());
         config.set("jail.z", (int) jailLoc.getZ());
         config.set("jail.world", jailLoc.getWorld().getName());
         
         this.saveConfig();
         
     }
     
     /**
      * Sets the unjail point.
      * 
      * @param loc 
      */
     public void setUnjail(Location loc) {
         
         unjailLoc = loc;
         
         YamlConfiguration config = (YamlConfiguration) this.getConfig();
         config.set("unjail.x", (int) unjailLoc.getX());
         config.set("unjail.y", (int) unjailLoc.getY());
         config.set("unjail.z", (int) unjailLoc.getZ());
         config.set("unjail.world", unjailLoc.getWorld().getName());
         
         this.saveConfig();
         
     }
     
     private void loadConfig() {
         // Init config files:
         YamlConfiguration config = (YamlConfiguration) this.getConfig();
         config.options().copyDefaults(true);
         config.addDefault("jailgroup", "Jailed");
         config.addDefault("jail.world", this.getServer().getWorlds().get(0).getName());
         config.addDefault("jail.x", 0);
         config.addDefault("jail.y", 0);
         config.addDefault("jail.z", 0);
         config.addDefault("unjail.world", this.getServer().getWorlds().get(0).getName());
         config.addDefault("unjail.x", 0);
         config.addDefault("unjail.y", 0);
         config.addDefault("unjail.z", 0);
         
         jailed = new YamlConfiguration();
         File f = new File(this.getDataFolder().getPath() + File.separator + "jailed.yml");
         
         try {
             if(!f.exists()) {
                 f.getParentFile().mkdirs();
                 f.createNewFile();
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         
         try {
             jailed.load(f);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
         jailLoc = new Location(
                 this.getServer().getWorld(config.getString("jail.world", this.getServer().getWorlds().get(0).getName())),
                 config.getInt("jail.x", 0),
                 config.getInt("jail.y", 0),
                 config.getInt("jail.z", 0));
         unjailLoc = new Location(
                 this.getServer().getWorld(config.getString("unjail.world", this.getServer().getWorlds().get(0).getName())),
                 config.getInt("unjail.x", 0),
                 config.getInt("unjail.y", 0),
                 config.getInt("unjail.z", 0));
         jailGroup = config.getString("jailgroup", "Jailed");
         
         this.saveConfig();
         
     }
     
     private void setupPermissions() {
         
         Plugin bukkit = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
         Plugin pex = this.getServer().getPluginManager().getPlugin("PermissionsEx");
         Plugin vault = this.getServer().getPluginManager().getPlugin("Vault");
         
         boolean permissionsLoaded = false;
         
         if (bukkitPermissions == null && bukkit != null) {
             bukkitPermissions = (PermissionsPlugin) bukkit;
             permissionsLoaded = true;
         }
         if (pexPermissions == null && pex != null) {
             pexPermissions = PermissionsEx.getPermissionManager();
             permissionsLoaded = true;
         }
         if (vaultPermissions == null && vault != null) {
             RegisteredServiceProvider<Permission> rsp = this.getServer().getServicesManager().getRegistration(Permission.class);
             if (rsp != null) {
                 vaultPermissions = rsp.getProvider();
                 if (!(vaultPermissions instanceof Permission_SuperPerms))
                     permissionsLoaded = true;
             }
         }
         
         if(!permissionsLoaded) {
             messager.severe(JailMessage.PERMISSIONS_NOT_FOUND);
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
     }
     
     /**
      * Gets all jailed players.
      * 
      * Will only get players who are in the jailed.yml and are not freed via status.
      * 
      * @return list of player names
      */
     public List<String> getJailedPlayers() {
         List<String> players = new ArrayList<String>();
         for (String key : jailed.getKeys(false)) {
             if (!playerIsJailed(key)) continue;
             if (getPlayerStatus(key) == JailStatus.FREED) continue;
             players.add(key);
         }
         return players;
     }
     
     /**
      * Sets a custom value for a jailed player. If the player is
      * not in jail, nothing will happen.
      * 
      * @param player
      * @param node
      * @param value 
      */
     public void setJailParameter(Player player, String node, String value) {
         this.setJailParameter(player.getName(), node, value);
     }
     
     /**
      * Sets a custom value for a jailed player. If the player is
      * not in jail, nothing will happen.
      * 
      * @param player
      * @param node
      * @param value 
      */
     public void setJailParameter(String player, String node, String value) {
         if (!this.playerIsJailed(player)) return;
         jailed.set(player.toLowerCase() + ".data." + node, value);
         this.saveJail();
     }
     
     /**
      * Sets a custom value for a jailed player. If the player is
      * not in jail, nothing will happen.
      * 
      * @param player
      * @param node
      * @param value 
      */
     public void setJailParameter(Player player, String node, int value) {
         this.setJailParameter(player.getName(), node, value);
     }
     
     /**
      * Sets a custom value for a jailed player. If the player is
      * not in jail, nothing will happen.
      * 
      * @param player
      * @param node
      * @param value 
      */
     public void setJailParameter(String player, String node, int value) {
         if (!this.playerIsJailed(player)) return;
         jailed.set(player.toLowerCase() + ".data." + node, value);
         this.saveJail();
     }
     
     /**
      * Gets a custom string value associated with a jailed player.
      * If the player is not in jail, it will return null.
      * 
      * @param player
      * @param node
      * @return 
      */
     public String getJailString(Player player, String node) {
         return this.getJailString(player.getName(), node);
     }
     
     /**
      * Gets a custom string value associated with a jailed player.
      * If the player is not in jail, it will return null.
      * 
      * @param player
      * @param node
      * @return 
      */
     public String getJailString(String player, String node) {
         if (!this.playerIsJailed(player)) return null;
         return jailed.getString(player.toLowerCase() + ".data." + node, null);
     }
     
     /**
      * Gets a custom string value associated with a jailed player.
      * If the player is not in jail, it will return -1.
      * 
      * @param player
      * @param node
      * @return 
      */
     public int getJailInt(Player player, String node) {
         return this.getJailInt(player.getName(), node);
     }
     
     /**
      * Gets a custom string value associated with a jailed player.
      * If the player is not in jail, it will return -1.
      * 
      * @param player
      * @param node
      * @return 
      */
     public int getJailInt(String player, String node) {
         if (!this.playerIsJailed(player)) return -1;
         return jailed.getInt(player.toLowerCase() + ".data." + node, -1);
     }
     
     /**
      * Returns the location set to be the jail.
      * 
      * @param player the player to get the location for
      * @return jail location
      */
     public Location getJailLocation(String player) {
         if (player == null || !this.playerIsJailed(player)) return jailLoc;
         if (jailed.get(player.toLowerCase() + ".location.jail") == null) return jailLoc;
         return (Location) jailed.get(player.toLowerCase() + ".location.jail");
     }
     
     /**
      * Returns the location set to be the unjail point.
      * 
      * @param player the player to get the location for
      * @return unjail location
      */
     public Location getUnjailLocation(String player) {
         if (player == null || !this.playerIsJailed(player)) return unjailLoc;
         if (jailed.get(player.toLowerCase() + ".location.unjail") == null) return unjailLoc;
         return (Location) jailed.get(player.toLowerCase() + ".location.unjail");
     }
     
     /**
      * Returns true if the player is in jail.
      * 
      * @param player the player to check for
      * @return 
      */
     public boolean playerIsJailed(Player player) {
         return this.playerIsJailed(player.getName());
     }
     
     /**
      * Returns true if the player is in jail.
      * 
      * @param player the player to check for
      * @return 
      */
     public boolean playerIsJailed(String player) {
         if (jailed.get(player.toLowerCase()) != null && getPlayerStatus(player.toLowerCase()) != JailStatus.FREED)
             return true;
         return false;
     }
     
     /**
      * Returns true if the player is jailed and has a set
      * time limit for jail.
      * 
      * @param player the player to check for
      * @return 
      */
     public boolean playerIsTempJailed(Player player) {
         return this.playerIsTempJailed(player.getName());
     }
     
     /**
      * Returns true if the player is jailed and has a set
      * time limit for jail.
      * 
      * @param player the player to check for
      * @return 
      */
     public boolean playerIsTempJailed(String player) {
         if (!this.playerIsJailed(player))
             return false;
         if (jailed.get(player.toLowerCase() + ".tempTime") != null)
             return true;
         return false;
     }
     
     /**
      * Gets the time in milliseconds (based on System.getCurrentTimeMillis())
      * when a player should be unjailed.
      * 
      * This returns the time a player should be
      * unjailed, not the time remaining. It returns -1
      * if the player is not tempjailed.
      * 
      * @param player the player to check for
      * @return 
      */
     public double getTempJailTime(Player player) {
         return this.getTempJailTime(player.getName());
     }
     
     /**
      * Gets the time in milliseconds (based on System.getCurrentTimeMillis())
      * when a player should be unjailed.
      * 
      * This returns the time a player should be
      * unjailed, not the time remaining. It returns -1
      * if the player is not tempjailed.
      * 
      * @param player the player to check for
      * @return 
      */
     public double getTempJailTime(String player) {
         if (!this.playerIsJailed(player))
             return -1;
         return jailed.getDouble(player.toLowerCase() + ".tempTime", -1);
     }
     
     /**
      * Gets the current jailed status of a player.
      * 
      * @param player the player to check for
      * @return 
      */
     public JailStatus getPlayerStatus(Player player) {
         return this.getPlayerStatus(player.getName());
     }
     
     /**
      * Gets the current jailed status of a player.
      * 
      * @param player the player to check for
      * @return 
      */
     public JailStatus getPlayerStatus(String player) {
         return JailStatus.valueOf(jailed.getString(player.toLowerCase() + ".status", "jailed").toUpperCase());
     }
     
     /**
      * Sets the current jailed status of a player.
      * 
      * @param player the player to check for
      * @return 
      */
     public void setPlayerStatus (Player player, JailStatus status) {
         this.setPlayerStatus(player.getName(), status);
     }
     
     /**
      * Sets the current jailed status of a player.
      * 
      * @param player the player to check for
      * @return 
      */
     public void setPlayerStatus(String player, JailStatus status) {
         jailed.set(player.toLowerCase() + ".status", status.toString());
     }
     
     /**
      * Gets the groups of a particular player. Works for all
      * supported permissions plugins.
      * 
      * @param player
      * @return 
      */
     private List<String> getGroups(String player) {
         if(bukkitPermissions != null) {
             List<Group> groups = bukkitPermissions.getGroups(player);
             List<String> stringGroups = new ArrayList<String>();
             for (Group g : groups) {
                 stringGroups.add(g.getName());
             }
             return stringGroups;
         } else if (pexPermissions != null) {
             PermissionGroup[] groups = pexPermissions.getUser(player).getGroups();
             List<String> stringGroups = new ArrayList<String>();
             for (PermissionGroup g : groups) {
                 stringGroups.add(g.getName());
             }
             return stringGroups;
         } else if (vaultPermissions != null) {
             String[] groups = vaultPermissions.getPlayerGroups(jailLoc.getWorld(), player);
             List<String> stringGroups = Arrays.asList(groups);
             return stringGroups;
         }
         
         return null;
     }
     
     /**
      * Sets the groups for any player. Works with
      * all supported permissions plugins.
      * 
      * @param player
      * @param group 
      */
     private void setGroups(String player, List<String> group) {
         if (bukkitPermissions != null) {
             String params = new String();
             for (String grp : group) {
                 params += grp + ",";
             }
             this.getServer().dispatchCommand(console, "permissions player setgroup " + player + " " + params);
         } else if(pexPermissions != null) {
             pexPermissions.getUser(player).setGroups(group.toArray(new String[0]));
         } else if (vaultPermissions != null) {
             String[] groups = vaultPermissions.getPlayerGroups(jailLoc.getWorld(), player);
             for (String g : groups) {
                 vaultPermissions.playerRemoveGroup(jailLoc.getWorld(), player, g);
             }
             for (String g : group) {
                 vaultPermissions.playerAddGroup(jailLoc.getWorld(), player, g);
             }
         }
     }
     
     /**
      * Converts a number of minutes to a human-readable string.
      * 
      * @param minutes
      * @return 
      */
     public String prettifyMinutes(int minutes) {
         if (minutes == 1) return "one minute";
         if (minutes < 60) return minutes + " minutes";
         if (minutes % 60 == 0) {
             if(minutes / 60 == 1) return "one hour";
             else return (minutes / 60) + " hours";
         }
         int m = minutes % 60;
         int h = (minutes - m) / 60;
         return h + "h" + m + "m";
     }
     
     /**
      * Converts a human-readable string to a number of minutes.
      * 
      * Returns -1 if the string isn't parsable.
      * 
      * @param time
      * @return 
      */
     public int parseTimeString(String time) {
         if(!time.matches("[0-9]*h?[0-9]*m?")) return -1;
         if(time.matches("[0-9]+")) return Integer.parseInt(time);
         if(time.matches("[0-9]+m")) return Integer.parseInt(time.split("m")[0]);
         if(time.matches("[0-9]+h")) return Integer.parseInt(time.split("h")[0]) * 60;
         if(time.matches("[0-9]+h[0-9]+m")) {
             String[] split = time.split("[mh]");
             return (Integer.parseInt(split[0]) * 60) + Integer.parseInt(split[1]);
         }
         return -1;
     }
     
     /**
      * Saves the jailed.yml.
      */
     public void saveJail() {
         try {
             jailed.save(new File(this.getDataFolder().getPath() + File.separator + "jailed.yml"));
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
     
     /**
      * Registers a command listener.
      * 
      * @param listener
      * @param priority 
      */
     public void registerCommandListener(SimpleJailCommandListener listener, Priority priority) {
         handler.addListener(listener, priority);
     }
     
     /**
      * Unregisters a command listener.
      * 
      * @param listener
      * @param priority 
      */
     public void unregisterCommandListener(SimpleJailCommandListener listener, Priority priority) {
         handler.removeListener(listener, priority);
     }
     
     /**
      * Changes the usage message of a SimpleJail command.
      * 
      * @param command
      * @param usage 
      */
     public void setCommandUsage(String command, String usage) {
         PluginCommand cmd = this.getCommand(command);
         cmd.setUsage(usage);
     }
     
     /**
      * Represents a player's jailed status.
      */
     public enum JailStatus {
         JAILED,
         PENDING,
         FREED;
         
         @Override
         public String toString() {
             return this.name().toLowerCase();
         }
     }
     
     /**
      * Manages various SimpleJail messages.
      */
     public enum JailMessage implements MessageTemplate {
         PERMISSIONS_NOT_FOUND (MessageLevel.PLAIN, "ERROR: Could not find permissions plugin."),
         LACKS_PERMISSIONS (MessageLevel.ERROR, "You don't have permission to use that command (%1)."),
         ONLY_PLAYERS (MessageLevel.ERROR, "Only players can use that."),
         JAIL (MessageLevel.SUCCESS, "Player <i>%1</i> sent to jail."),
         UNJAIL (MessageLevel.SUCCESS, "Player <i>%1</i> removed from jail."),
         TEMPJAIL (MessageLevel.SUCCESS, "Player <i>%1</i> jailed for %2."),
         JAILED (MessageLevel.COMPLETE, "You have been jailed!"),
         UNJAILED (MessageLevel.COMPLETE, "You have been removed from jail."),
         TEMPJAILED (MessageLevel.COMPLETE, "You have been jailed for %1."),
         UNTEMPJAILED (MessageLevel.COMPLETE, "Player '%1' auto-unjailed."),
         ALREADY_IN_JAIL (MessageLevel.ERROR, "Player <i>%1</i> is already in jail!"),
         NOT_IN_JAIL (MessageLevel.ERROR, "Player <i>%1</i> is not in jail!"),
         NOT_TEMPJAILED (MessageLevel.ERROR, "Player <i>%1</i> is not tempjailed."),
         JAIL_POINT_SET (MessageLevel.SUCCESS, "Jail point set."),
         UNJAIL_POINT_SET (MessageLevel.SUCCESS, "Unjail point set."),
         INVALID_COORDINATE (MessageLevel.ERROR, "Invalid coordinate."),
         MUST_SPECIFY_TARGET (MessageLevel.ERROR, "You must specify a player."),
         PLAYER_NOT_FOUND (MessageLevel.ERROR, "Couldn't find player '%1'."),
         JAIL_TIME (MessageLevel.COMPLETE, "Remaining jail time: %1."),
         PLAYER_IS_JAILED(MessageLevel.COMPLETE, "You are jailed.");
         
         private MessageLevel level;
         private String format;
         
         JailMessage(MessageLevel level, String format) {
             this.level = level;
             this.format = Messaging.parseStyling(level.getOpeningTag() + format + level.getClosingTag());
         }
         
         /**
          * Gets the message's level.
          *
          * @return level
          */
         @Override
         public MessageLevel getLevel() {
             return level;
         }
         
         /**
          * Gets the raw message as a String.
          * 
          * @return the message
          */
         @Override
         public String getMessage() {
             return format;
         }
     }
 }
