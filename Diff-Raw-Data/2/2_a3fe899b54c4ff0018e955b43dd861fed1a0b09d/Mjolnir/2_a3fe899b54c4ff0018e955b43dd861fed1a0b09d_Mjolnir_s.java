 package it.flaten.mjolnir;
 
 import it.flaten.mjolnir.beans.Event;
 import it.flaten.mjolnir.commands.*;
 import it.flaten.mjolnir.listeners.PlayerListener;
 import it.flaten.mjolnir.storages.NativeStorage;
 import it.flaten.mjolnir.storages.Storage;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The plugin's main class.
  *
  * We extend Bukkit's {@link JavaPlugin} class, and implement our own
  * entry and exit points to handle plugin initialization.
  *
  * @author Jim Flaten
  */
 public class Mjolnir extends JavaPlugin {
     /**
      * Where we'll keep our {@link Storage} implementation.
      *
      * The variable is accessed by various methods that need
      * to communicate directly with the storage.
      */
     private Storage storage;
 
     /**
      * A list of our beans.
      *
      * This can be used by {@link Storage} implementations to see which
      * beans it should expect.
      */
     public static List<Class<?>> databaseClasses = new ArrayList<Class<?>>() {{
         add(Event.class);
     }};
 
     /**
      * Plugin entry point.
      *
      * This method is automatically invoked by the Bukkit server
      * implementation when the plugin is enabled. It handles plugin
      * configuration, storage initialization, Bukkit event bindings,
      * and command bindings.
      */
     @Override
     public void onEnable() {
         this.getLogger().info(" * Configuration...");
 
         /**
          * Copy default configuration.
          *
          * This will copy config.yml from the root of the plugin if no
          * configuration file exists on the server.
          */
         this.saveDefaultConfig();
 
         /**
          * Copy default configuration.
          *
          * This will add missing nodes to the plugin's configuration file
          * from the default configuration file, if any.
          */
         this.getConfig().options().copyDefaults(true);
 
         /**
          * Save the configuration.
          *
          * This will make sure the configuration file reflects what we're
          * working with.
          */
         this.saveConfig();
 
         this.getLogger().info(" * Storage...");
 
         /**
          * Detect and initialize {@link Storage} implementation.
          *
          * If we don't know which implementation the configuration asks for,
          * disable the plugin.
          */
         switch (this.getConfig().getString("storage.method").toLowerCase()) {
             case "native":
                 this.storage = new NativeStorage(this);
                 break;
 
             default:
                 this.getLogger().severe("Unknown storage method!");
                 this.getPluginLoader().disablePlugin(this);
                 return;
         }
 
         /**
          * Create database tables.
          *
          * This will create the database tables required to store our beans,
          * unless they already exist.
          */
         this.storage.createTables();
 
         this.getLogger().info(" * Event handlers...");
 
         /**
          * Register event handlers.
          *
          * These are the classes we use to listen for Bukkit events representing
          * actions on the server.
          */
         this.getServer().getPluginManager().registerEvents(new PlayerListener(this),this);
 
         this.getLogger().info(" * Command handlers...");
 
         /**
          * Register command handlers.
          *
          * These are the classes that handle commands both in the console and
          * in-game.
          */
         this.getCommand("infractions").setExecutor(new InfoCommand(this));
         this.getCommand("tempban").setExecutor(new TempBanCommand(this));
         this.getCommand("ban").setExecutor(new BanCommand(this));
         this.getCommand("tempunban").setExecutor(new TempUnbanCommand(this));
         this.getCommand("unban").setExecutor(new UnbanCommand(this));
     }
 
     /**
      * Plugin exit point.
      *
      * This method is invoked by the Bukkit implementation before the
      * plugin is disabled. Usually before the server shuts down. It
      * releases command and event binds, and shuts down the storage.
      */
     @Override
     public void onDisable() {
         this.getLogger().info(" * Command handlers...");
 
         /**
          * Remove command handlers.
          *
          * Setting executor to null removes the last reference to the
          * handler class instances, disables processing of the commands,
          * and allows the memory to be freed.
          */
         this.getCommand("unban").setExecutor(null);
         this.getCommand("tempunban").setExecutor(null);
         this.getCommand("ban").setExecutor(null);
         this.getCommand("tempban").setExecutor(null);
         this.getCommand("infractions").setExecutor(null);
 
         this.getLogger().info(" * Event handlers...");
 
         /**
          * Unregister from events.
          *
          * Make sure we no longer receive any events before we shut down
          * the {@link Storage} instance.
          */
         HandlerList.unregisterAll(this);
 
         this.getLogger().info(" * Storage...");
 
         /**
          * Shut down the storage instance.
          *
          * What is done here depends on the implementing class. {@link it.flaten.mjolnir.storages.NativeStorage#shutdown()}
          * for example, does nothing.
          */
         this.storage.shutdown();
 
         /**
          * Remove the last reference to the {@link Storage} instance.
          *
          * This allows the memory to be freed.
          */
         this.storage = null;
     }
 
     /**
      * Get the beans used for storage.
      *
      * This information can be used by {@link Storage} implementations to prepare
      * for the types of data they will receive.
      *
      * @return A list of {@link Class}es.
      */
     @Override
     public List<Class<?>> getDatabaseClasses() {
         return Mjolnir.databaseClasses;
     }
 
     /**
      * Create tables required for data storage.
      *
      * This method does nothing more than expose the protected {@link org.bukkit.plugin.java.JavaPlugin#installDDL()}
      * in the JavaPlugin class so that classes implementing {@link Storage} can use
      * it if needed.
      */
     @Override
     public void installDDL() {
         super.installDDL();
     }
 
     /**
      * Get all events for a player.
      *
      * Fetchs all the events stored for the given player.
      *
      * @param player The name of the player whose {@link Event}s to fetch.
      * @return       A {@link List} of {@link Event}s, oldest first.
      */
     public List<Event> getEventHistory(String player) {
         return this.storage.loadEvents(player);
     }
 
     /**
      * Get the active event for a player.
      *
      * Fetches the latest stored {@link Event} for the given player
      * that has not expired. Will return null of no {@link Event} is
      * found.
      *
      * @param player The name of the player whose {@link Event}s to fetch.
      * @return       The active {@link Event}, or null.
      */
     public Event getActiveEvent(String player) {
         return this.storage.loadActiveEvent(player);
     }
 
     /**
      * Build kick message.
      *
      * This method composes the message displayed to players who are kicked,
      * from an Event bean.
      *
      * @param event The {@link Event} that caused this kick.
      * @return      The message the kicked player will see.
      */
     public String buildKickMessage(Event event) {
         String message = this.getConfig().getString("kick.message")
             .replace("<reason>",event.getReason());
 
         if (event.getExpires() > 0) {
             message += this.getConfig().getString("kick.expires.message")
                 .replace("<expires>",new SimpleDateFormat(this.getConfig().getString("kick.expires.format")).format(event.getExpires() * 1000L));
         }
 
         return message;
     }
 
     /**
      * Build broadcast message.
      *
      * This method composes the message displayed to in-game players when a
      * player is kicked, from an {@link Event} bean.
      *
      * @param event The {@link Event} that caused this kick.
      * @return      The message in-game players will see.
      */
     public String buildBroadcastMessage(Event event) {
         String message = this.getConfig().getString("broadcast.message")
             .replace("<player>",event.getPlayer())
             .replace("<op>",event.getOp())
             .replace("<type>",event.getType().toString().toLowerCase())
             .replace("<reason>",event.getReason());
 
         if (event.getExpires() > 0) {
             message += this.getConfig().getString("broadcast.expires.message")
                 .replace("<expires>",new SimpleDateFormat(this.getConfig().getString("broadcast.expires.format")).format(event.getExpires() * 1000L));
         }
 
         return message;
     }
 
     /**
      * Broadcast a message.
      *
      * This will build a broadcast message with {@link #buildBroadcastMessage(it.flaten.mjolnir.beans.Event)} from
      * an {@link Event}, and send it to in-game players with the correct permissions.
      *
      * @param event The {@link Event} used to generate the message.
      */
     public void broadcast(Event event) {
         this.getServer().broadcast(
             ChatColor.GRAY + this.buildBroadcastMessage(event),
             "mjolnir.info"
         );
     }
 
     /**
      * Permanently ban a player.
      *
      * Used to permanently ban and kick a player, with no reason.
      *
      * @param player The name of the player to kick and ban.
      * @param op     The name of the player who executes the ban.
      * @return       The resulting ban {@link Event}.
      */
     public Event banPlayer(String player,String op) {
         return this.kickPlayer(this.storage.saveEvent(player,op,Event.EventType.BAN,"",0));
     }
 
     /**
      * Permanently unban a player.
      *
      * Used to permanently unban a banned player, with no reason.
      *
      * @param player The name of the player to unban.
      * @param op     The name of the player who executes the unban.
      * @return       The resulting unban {@link Event}.
      */
     public Event unbanPlayer(String player,String op) {
         return this.storage.saveEvent(player,op,Event.EventType.UNBAN,"",0);
     }
 
     /**
      * Ban a player.
      *
      * Used to permanently ban and kick a player, with a reason.
      *
      * @param player The name of the player to kick and ban.
      * @param op     The name of the player who executes the ban.
      * @param reason The reason for this ban.
      * @return       The resulting ban {@link Event}.
      */
     public Event banPlayer(String player,String op,String reason) {
         return this.kickPlayer(this.storage.saveEvent(player,op,Event.EventType.BAN,reason,0));
     }
 
     /**
      * Unban a player.
      *
      * Used to permanently unban a banned player, with a reason.
      *
      * @param player The name of the player to unban.
      * @param op     The name of the player who executes the unban.
      * @param reason The reason for this unban.
      * @return       The resulting unban {@link Event}.
      */
     public Event unbanPlayer(String player,String op,String reason) {
         return this.storage.saveEvent(player,op,Event.EventType.UNBAN,reason,0);
     }
 
     /**
      * Temporarily ban a player.
      *
      * Used to temporary ban and kick a player.
      *
      * @param player  The name of the player to ban.
      * @param op      The name of the player who executes the ban.
      * @param expires Length of ban in {@link #parseTime(String)} format.
      * @return        The resulting ban {@link Event}.
      */
     public Event tempBanPlayer(String player,String op,String expires) {
         return this.kickPlayer(this.storage.saveEvent(player,op,Event.EventType.BAN,"",Mjolnir.parseTime(expires)));
     }
 
     /**
      * Temporarily unban a player.
      *
      * Used to temporarily unban a banned player, with no reason.
      *
      * @param player  The name of the player to be unbanned.
      * @param op      The name of the player who executes the unban.
      * @param expires Length of ban in {@link #parseTime(String)} format.
      * @return        The resulting unban {@link Event}.
      */
     public Event tempUnbanPlayer(String player, String op, String expires) {
         return this.storage.saveEvent(player,op,Event.EventType.UNBAN,"",Mjolnir.parseTime(expires));
     }
 
     /**
      * Temporarily ban a player.
      *
      * Used to temporarily ban a player, with a reason.
      *
      * @param player  The name of the player to ban.
      * @param op      The name of the player who executes the ban.
      * @param reason  The reason for this ban.
      * @param expires Length of ban in {@link #parseTime(String)} format.
      * @return        The resulting ban {@link Event}.
      */
     public Event tempBanPlayer(String player,String op,String reason,String expires) {
         return this.kickPlayer(this.storage.saveEvent(player,op,Event.EventType.BAN,reason,Mjolnir.parseTime(expires)));
     }
 
     /**
      * Temporarily unban a player.
      *
      * Used to temporarily unban a player, with a reason.
      *
      * @param player  The name of the player to unban.
      * @param op      The name of the player who executes the unban.
      * @param reason  The reason for this unban.
      * @param expires Length of unban in {@link #parseTime(String)} format.
      * @return        The resulting unban {@link Event}.
      */
     public Event tempUnbanPlayer(String player,String op,String reason,String expires) {
         return this.storage.saveEvent(player,op,Event.EventType.UNBAN,reason,Mjolnir.parseTime(expires));
     }
 
     // Takes a time string in the format "1y 2w 2d 4h 5m 6s"
     // and returns a UNIX timestamp that far in the future.
 
     /**
      * Parse a time period string.
      *
      * Parses a string looking for the following time notations:
      *   1y, where 1 can be any number of years.
      *   1w, where 1 can be any number of weeks.
      *   1d, where 1 can be any number of days.
      *   1h, where 1 can be any number of hours.
      *   1m, where 1 can be any number of minutes.
      *   1s, where 1 can be any number of seconds.
      * It then returns the current UNIX time plus the specified amount of time.
      * <p>
      * If the given time string sums to 0, then 0 is returned.
      *
      * @param time A length of time in the supported format.
      * @return     A UNIX timestamp somewhere in the future, or 0.
      */
     private static int parseTime(String time) {
         int sum = 0;
         String buffer = "";
 
         for (int i = 0; i < time.length(); i++) {
             char c = time.charAt(i);
 
             if (Character.isDigit(c)) {
                 buffer += c;
             } else {
                 int amount = Integer.parseInt(buffer);
 
                 switch (String.valueOf(c)) {
                     case "s": sum += amount; break;
                     case "m": sum += 60 * amount; break;
                     case "h": sum += 60 * 60 * amount; break;
                     case "d": sum += 60 * 60 * 24 * amount; break;
                     case "w": sum += 60 * 60 * 24 * 7 * amount; break;
                     case "y": sum += 60 * 60 * 24 * 365 * amount; break;
                 }
 
                 buffer = "";
             }
         }
 
         if (sum == 0) {
             return 0;
         }
 
         return ((int) (System.currentTimeMillis() / 1000L)) + sum;
     }
 
     /**
      * Kick a player.
      *
      * This methods kicks the player defined in an {@link Event} if online.
      *
      * @param event The {@link Event} to be used.
      * @return      The {@link Event} that was used.
      */
     private Event kickPlayer(Event event) {
         Player player = this.getServer().getPlayerExact(event.getPlayer());
 
         if (player != null) {
             player.kickPlayer(this.buildKickMessage(event));
         }
 
         return event;
     }
 
     /**
      * Check if a player is banned.
      *
      * Fetches the given player's active {@link Event}, and sees if it
      * is a ban. Returns <code>true</code> if it is, and <code>false</code> if it
      * is not or there is no active event.
      *
      * @param player The name of the player to check.
     * @return       Wether or not the given player is banned.
      */
     public boolean isBanned(String player) {
         Event event = this.getActiveEvent(player);
 
         if (event == null || event.getType() == Event.EventType.UNBAN) {
             return false;
         }
 
         return true;
     }
 }
