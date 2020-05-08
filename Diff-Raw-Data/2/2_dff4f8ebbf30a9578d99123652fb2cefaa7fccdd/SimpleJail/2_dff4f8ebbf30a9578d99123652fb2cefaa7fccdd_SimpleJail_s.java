 package com.imjake9.simplejail;
 
 import com.imjake9.simplejail.api.SimpleJailCommandListener;
 import com.imjake9.simplejail.api.SimpleJailCommandListener.Priority;
 import com.imjake9.simplejail.events.PlayerJailEvent;
 import com.imjake9.simplejail.events.PlayerUnjailEvent;
 import com.platymuus.bukkit.permissions.Group;
 import com.platymuus.bukkit.permissions.PermissionsPlugin;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class SimpleJail extends JavaPlugin {
     
     private static final Logger log = Logger.getLogger("Minecraft");
     private static SimpleJail plugin = null;
     
     public ConsoleCommandSender console;
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
     
     @Override
     public void onDisable() {
         // Remove instance
         plugin = null;
         log.info("[SimpleJail] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() +  " disabled.");
     }
 
     @Override
     public void onEnable() {
         
         // Register instance
         plugin = this;
         
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
                         JailMessage.UNTEMPJAILED.print(p.getName());
                     }
                     
                 }
                 
             }
             
         }, 600, 600);
         
         log.info("[SimpleJail] " + this.getDescription().getName() + " v" + this.getDescription().getVersion() + " enabled.");
     }
     
     /**
      * Sends a player to jail.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param name
      * @throws JailException 
      */
     public void jailPlayer(String name) throws JailException {
         this.jailPlayer(name, -1, jailLoc);
     }
     
     /**
      * Sends a player to jail for a specific time.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param name
      * @param time time in minutes
      * @throws JailException 
      */
     public void jailPlayer(String name, int time) throws JailException {
         this.jailPlayer(name, time, jailLoc);
     }
     
     /**
      * Sends a player to jail for a specific time and to a location.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param name
      * @param time time in minutes
      * @param loc
      * @throws JailException 
      */
     public void jailPlayer(String name, int time, Location loc) throws JailException {
         
         // Autocomplete name if player is online:
         Player player = this.getServer().getPlayer(name);
         name = player == null ? name.toLowerCase() : player.getName().toLowerCase();
         
         // Dispatch event:
         PlayerJailEvent e = new PlayerJailEvent(name, loc, time);
         this.getServer().getPluginManager().callEvent(e);
         
         // If event cancelled, take no action:
         if (e.isCancelled())
             return;
         
         // Update time to event result
         time = e.getLength();
         
         // Check if player is slready jailed:
         if(jailed.get(name) != null) {
             throw new JailException("Jailed player was sent jail message.", JailMessage.ALREADY_IN_JAIL.message(name));
         }
         
         // Put player in jailed group:
         List<String> groupName = this.getGroups(name);
         jailed.set(name + ".groups", groupName);
         this.setGroups(name, Arrays.asList(new String[]{jailGroup}));
         
         // If tempjailing, set up tempjail time:
         if (time > 0) {
             double tempTime = System.currentTimeMillis() + (time * 60000);
            jailed.set(name + ".tempTime", tempTime);
         }
         
         // Move player into jail:
         if (player != null)
             player.teleport(e.getJailLocation());
         else
             jailed.set(name + ".status", "pending");
         
         this.saveJail();
         
         // Send message to player
         if (player != null) {
             if(time <= 0) JailMessage.JAILED.send(player);
             else JailMessage.TEMPJAILED.send(player, this.prettifyMinutes(time));
         }
         
     }
     
     /**
      * Removes a player from jail.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param name
      * @throws JailException 
      */
     public void unjailPlayer(String name) throws JailException {
         this.unjailPlayer(name, jailLoc);
     }
     
     /**
      * Removes a player from jail to a location.
      * 
      * Throws a JailException that contains a formatted message,
      * meant to be sent to a player.
      * 
      * @param name
      * @param location
      * @throws JailException 
      */
     public void unjailPlayer(String name, Location loc) throws JailException {
         
         // Autocomplete name if player is online:
         Player player = this.getServer().getPlayer(name);
         name = player == null ? name.toLowerCase() : player.getName().toLowerCase();
         
         // Dispatch event
         PlayerUnjailEvent e = new PlayerUnjailEvent(name, unjailLoc);
         this.getServer().getPluginManager().callEvent(e);
         
         // If event cancelled, take no action:
         if (e.isCancelled())
             return;
         
         // Check if player is in jail:
         if(jailed.get(name) == null) {
             throw new JailException("Player not in jail was sent unjail message.", JailMessage.NOT_IN_JAIL.message(name));
         }
         
         // Check if player is offline:
         if (player == null) {
             jailed.set(name + ".status", "freed");
             return;
         }
         
         // Move player out of jail:
         player.teleport(e.getUnjailLocation());
         
         this.setGroups(name, jailed.getStringList(name + ".groups"));
         
         jailed.set(name, null);
         
         this.saveJail();
         
         JailMessage.UNJAILED.send(player);
         
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
                 permissionsLoaded = true;
             }
         }
         
         if(!permissionsLoaded) {
             JailMessage.PERMISSIONS_NOT_FOUND.print();
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
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
         jailed.set(player.toLowerCase() + "." + node, value);
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
         jailed.set(player.toLowerCase() + "." + node, value);
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
         return jailed.getString(player.toLowerCase() + "." + node, null);
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
         return jailed.getInt(player.toLowerCase() + "." + node, -1);
     }
     
     /**
      * Returns the location set to be the jail.
      * 
      * @return jail location
      */
     public Location getJailLocation() {
         return jailLoc;
     }
     
     /**
      * Returns the location set to be the unjail point.
      * 
      * @return unjail location
      */
     public Location getUnjailLocation() {
         return unjailLoc;
     }
     
     /**
      * Returns true if the player is in jail.
      * 
      * Does not take status into account, so even if a player is freed,
      * this will still return true.
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
      * Does not take status into account, so even if a player is freed,
      * this will still return true.
      * 
      * @param player the player to check for
      * @return 
      */
     public boolean playerIsJailed(String player) {
         if (jailed.get(player.toLowerCase()) != null)
             return true;
         return false;
     }
     
     /**
      * Returns true if the player is unjailed and has a set
      * time limit for unjail.
      * 
      * Does not take status into account, so even if a player is freed,
      * this will still return true.
      * 
      * @param player the player to check for
      * @return 
      */
     public boolean playerIsTempJailed(Player player) {
         return this.playerIsTempJailed(player.getName());
     }
     
     /**
      * Returns true if the player is unjailed and has a set
      * time limit for unjail.
      * 
      * Does not take status into account, so even if a player is freed,
      * this will still return true.
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
        jailed.set(player.toLowerCase() + ".status", status);
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
      * Returns -1 if the string isn't parseable.
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
     public enum JailMessage {
         PERMISSIONS_NOT_FOUND ("ERROR: Could not find permissions plugin."),
         LACKS_PERMISSIONS (ChatColor.RED + "You don't have permission to use that command (%1)."),
         ONLY_PLAYERS (ChatColor.RED + "Only players can use that."),
         JAIL (ChatColor.AQUA + "Player '%1' sent to jail."),
         UNJAIL (ChatColor.AQUA + "Player '%1' removed from jail."),
         TEMPJAIL (ChatColor.AQUA + "Player '%1' jailed for %2."),
         JAILED (ChatColor.AQUA + "You have been jailed!"),
         UNJAILED (ChatColor.AQUA + "You have been removed from jail."),
         TEMPJAILED (ChatColor.AQUA + "You have been jailed for %1."),
         UNTEMPJAILED ("Player '%1' auto-unjailed."),
         ALREADY_IN_JAIL (ChatColor.RED + "Player '%1' is already in jail!"),
         NOT_IN_JAIL (ChatColor.RED + "Player '%1' is not in jail!"),
         NOT_TEMPJAILED (ChatColor.RED + "Player '%1' is not tempjailed."),
         JAIL_POINT_SET (ChatColor.AQUA + "Jail point set."),
         UNJAIL_POINT_SET (ChatColor.AQUA + "Unjail point set."),
         INVALID_COORDINATE (ChatColor.RED + "Invalid coordinate."),
         MUST_SPECIFY_TARGET (ChatColor.RED + "You must specify a player."),
         PLAYER_NOT_FOUND (ChatColor.RED + "Couldn't find player '%1'."),
         JAIL_TIME (ChatColor.AQUA + "Remaining jail time: %1."),
         PLAYER_IS_JAILED(ChatColor.AQUA + "You are jailed.");
         
         private String format;
         
         JailMessage(String format) {
             this.format = format;
         }
         
         /**
          * Gets the message as a String.
          * 
          * @return the message
          */
         String message() {
             return format;
         }
         
         /**
          * Gets the message with arguments filled.
          * 
          * @param args list of arguments
          * @return the message
          */
         String message(String... args) {
             String message = format;
             for(int i = 1; ; i++) {
                 if (message.indexOf("%" + i) > 0) {
                     message = message.replaceAll("%" + i, args[i - 1]);
                 } else break;
             }
             return message;
         }
         
         /**
          * Sends a message.
          * 
          * @param sender reciever
          */
         void send(CommandSender sender) {
             sender.sendMessage(format);
         }
         
         /**
          * Sends a message with arguments.
          * 
          * @param sender reciever
          * @param args list of arguments
          */
         void send(CommandSender sender, String... args) {
             sender.sendMessage(message(args));
         }
         
         /**
          * Prints a message prefixed with [SimpleJail] to the console.
          */
         void print() {
             log.info("[SimpleJail] " + format);
         }
         
         
         /**
          * Prints a message with arguments prefixed with [SimpleJail] to the console.
          * 
          * @param args 
          */
         void print(String... args) {
             log.info("[SimpleJail] " + message(args));
         }
     }
 }
