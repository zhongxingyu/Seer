 /*
  * SQLBans
  * Copyright 2012 Matt Baxter
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kitteh.sqlbans;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
 import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.sqlbans.commands.BanCommand;
 import org.kitteh.sqlbans.commands.KickCommand;
 import org.kitteh.sqlbans.commands.ReloadCommand;
 import org.kitteh.sqlbans.commands.UnbanCommand;
 import org.kitteh.sqlbans.exceptions.SQLBansException;
 import org.kitteh.sqlbans.exceptions.SQLBansThreadingException;
 
 public class SQLBans extends JavaPlugin implements Listener {
     public static class Messages {
         private static String DISCONNECT_REJECTED;
         private static String DISCONNECT_KICKED_NOREASON;
         private static String DISCONNECT_KICKED_REASON;
         private static String DISCONNECT_BANNED_NOREASON;
         private static String DISCONNECT_BANNED_REASON;
         private static String INGAME_KICKED_NORMAL_NOREASON;
         private static String INGAME_KICKED_NORMAL_REASON;
         private static String INGAME_KICKED_ADMIN_NOREASON;
         private static String INGAME_KICKED_ADMIN_REASON;
         private static String INGAME_BANNED_NORMAL_NOREASON;
         private static String INGAME_BANNED_NORMAL_REASON;
         private static String INGAME_BANNED_ADMIN_NOREASON;
         private static String INGAME_BANNED_ADMIN_REASON;
         private static String INGAME_UNBANNED_NORMAL;
         private static String INGAME_UNBANNED_ADMIN;
 
         public static String getDisconnectBanned(String reason, String admin) {
             final String ret = reason == null ? Messages.DISCONNECT_BANNED_NOREASON : Messages.DISCONNECT_BANNED_REASON.replace("%reason%", reason);
             return ret.replace("%admin%", admin == null ? "Admin" : admin);
         }
 
         public static String getDisconnectKicked(String reason, String admin) {
             final String ret = reason == null ? Messages.DISCONNECT_KICKED_NOREASON : Messages.DISCONNECT_KICKED_REASON.replace("%reason%", reason);
             return ret.replace("%admin%", admin == null ? "Admin" : admin);
         }
 
         public static String getDisconnectRejected() {
             return Messages.DISCONNECT_REJECTED;
         }
 
         public static String getIngameBanned(String target, String reason, String admin, boolean adminmsg) {
             String ret;
             if (adminmsg) {
                 ret = reason == null ? Messages.INGAME_BANNED_ADMIN_NOREASON : Messages.INGAME_BANNED_ADMIN_REASON.replace("%reason%", reason);
             } else {
                 ret = reason == null ? Messages.INGAME_BANNED_NORMAL_NOREASON : Messages.INGAME_BANNED_NORMAL_REASON.replace("%reason%", reason);
             }
             return ret.replace("%admin%", admin == null ? "Admin" : admin).replace("%target%", target == null ? "Target" : target);
         }
 
         public static String getIngameKicked(String target, String reason, String admin, boolean adminmsg) {
             String ret;
             if (adminmsg) {
                 ret = reason == null ? Messages.INGAME_KICKED_ADMIN_NOREASON : Messages.INGAME_KICKED_ADMIN_REASON.replace("%reason%", reason);
             } else {
                 ret = reason == null ? Messages.INGAME_KICKED_NORMAL_NOREASON : Messages.INGAME_KICKED_NORMAL_REASON.replace("%reason%", reason);
             }
             return ret.replace("%admin%", admin == null ? "Admin" : admin).replace("%target%", target == null ? "Target" : target);
         }
 
         public static String getIngameUnbanned(String target, String admin, boolean adminmsg) {
             final String ret = adminmsg ? Messages.INGAME_UNBANNED_ADMIN : Messages.INGAME_UNBANNED_NORMAL;
             return ret.replace("%admin%", admin == null ? "Admin" : admin).replace("%target%", target == null ? "Target" : target);
         }
 
         public static void load(SQLBans plugin) {
             plugin.checkThread();
             final FileConfiguration def = YamlConfiguration.loadConfiguration(plugin.getResource("config.yml"));
             final FileConfiguration config = plugin.getConfig();
             plugin.getConfig().setDefaults(def);
             Messages.DISCONNECT_REJECTED = Messages.color(config.getString("messages.disconnect.rejected"));
             Messages.DISCONNECT_KICKED_NOREASON = Messages.color(config.getString("messages.disconnect.kicked.noreason"));
             Messages.DISCONNECT_KICKED_REASON = Messages.color(config.getString("messages.disconnect.kicked.reason"));
             Messages.DISCONNECT_BANNED_NOREASON = Messages.color(config.getString("messages.disconnect.banned.noreason"));
             Messages.DISCONNECT_BANNED_REASON = Messages.color(config.getString("messages.disconnect.banned.reason"));
             Messages.INGAME_KICKED_NORMAL_NOREASON = Messages.color(config.getString("messages.ingame.kicked.normal.noreason"));
             Messages.INGAME_KICKED_NORMAL_REASON = Messages.color(config.getString("messages.ingame.kicked.normal.reason"));
             Messages.INGAME_KICKED_ADMIN_NOREASON = Messages.color(config.getString("messages.ingame.kicked.admin.noreason"));
             Messages.INGAME_KICKED_ADMIN_REASON = Messages.color(config.getString("messages.ingame.kicked.admin.reason"));
             Messages.INGAME_BANNED_NORMAL_NOREASON = Messages.color(config.getString("messages.ingame.banned.normal.noreason"));
             Messages.INGAME_BANNED_NORMAL_REASON = Messages.color(config.getString("messages.ingame.banned.normal.reason"));
             Messages.INGAME_BANNED_ADMIN_NOREASON = Messages.color(config.getString("messages.ingame.banned.admin.noreason"));
             Messages.INGAME_BANNED_ADMIN_REASON = Messages.color(config.getString("messages.ingame.banned.admin.reason"));
             Messages.INGAME_UNBANNED_NORMAL = Messages.color(config.getString("messages.ingame.unbanned.normal"));
             Messages.INGAME_UNBANNED_ADMIN = Messages.color(config.getString("messages.ingame.unbanned.admin"));
         }
 
         private static String color(String string) {
             if (string.endsWith("&&")) {
                 string = string.substring(0, string.length() - 2);
             }
             return string.replace("&&", String.valueOf(ChatColor.COLOR_CHAR));
         }
     }
 
     public static String TABLE_CREATE = null;
 
     private static String serverName;
 
     public static String getServerName() {
         return SQLBans.serverName;
     }
 
     private Thread mainThread;
 
     private HashSet<String> bannedCache;
 
     private Object bannedCacheSync;
 
     public void checkThread() {
         if (!Thread.currentThread().equals(this.mainThread)) {
             throw new SQLBansThreadingException();
         }
     }
 
     public void load() {
         this.reloadConfig();
         SQLBans.Messages.load(this);
         final FileConfiguration config = this.getConfig();
         config.setDefaults(YamlConfiguration.loadConfiguration(this.getResource("config.yml")));
         SQLBans.serverName = config.getString("server-name");
         final String host = config.getString("database.host");
         final int port = config.getInt("database.port");
         final String db = config.getString("database.database");
         final String user = config.getString("database.auth.username");
         final String pass = config.getString("database.auth.password");
         final String tableName = config.getString("database.tablename");
         try {
             SQLHandler.start(host, port, user, pass, db, tableName);
         } catch (final SQLBansException e) {
             this.getLogger().log(Level.SEVERE, "Failure to load, shutting down", e);
             this.getServer().getPluginManager().disablePlugin(this);
         }
     }
 
     @Override
     public void onEnable() {
         this.mainThread = Thread.currentThread();
 
         this.bannedCache = new HashSet<String>() {
             private static final long serialVersionUID = 1337L;
 
             @Override
             public boolean add(String string) {
                 return super.add(string.toLowerCase());
             }
 
             @Override
             public boolean contains(Object object) {
                 if (object instanceof String) {
                     return super.contains(((String) object).toLowerCase());
                 } else {
                     return super.contains(object);
                 }
             }
 
             @Override
             public boolean remove(Object object) {
                 if (object instanceof String) {
                     return super.remove(((String) object).toLowerCase());
                 } else {
                     return super.remove(object);
                 }
             }
         };
         this.bannedCacheSync = new Object();
         final File confFile = new File(this.getDataFolder(), "config.yml");
         if (!confFile.exists()) {
             this.saveDefaultConfig();
             this.getLogger().info("SQLBans config established. Edit the config and restart!");
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
 
         final BufferedReader reader = new BufferedReader(new InputStreamReader(this.getResource("create.sql")));
         final StringBuilder builder = new StringBuilder();
         String next;
         try {
             while ((next = reader.readLine()) != null) {
                 builder.append(next);
             }
         } catch (final IOException e) {
             new SQLBansException("Could not load default table creation text", e).printStackTrace();
         }
        SQLBans.TABLE_CREATE = String.format(builder.toString(), getConfig().getString("database.tablename") != null ? getConfig().getString("database.tablename") : "SQLBans_bans");
 
         // Command registration
         this.getCommand("ban").setExecutor(new BanCommand(this));
         this.getCommand("kick").setExecutor(new KickCommand(this));
         this.getCommand("sqlbansreload").setExecutor(new ReloadCommand(this));
         this.getCommand("unban").setExecutor(new UnbanCommand(this));
 
         this.getServer().getPluginManager().registerEvents(this, this);
 
         this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new BackupTask(this), 100, 6000);
 
         this.load();
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
         synchronized (this.bannedCacheSync) {
             if (this.bannedCache.contains(event.getName())) {
                 event.disallow(Result.KICK_BANNED, SQLBans.Messages.getDisconnectRejected());
                 return;
             }
         }
         try {
             if (!SQLHandler.canJoin(event.getName())) {
                 event.disallow(Result.KICK_BANNED, SQLBans.Messages.getDisconnectRejected());
                 synchronized (this.bannedCacheSync) {
                     final String name = event.getName();
                     this.bannedCache.add(name);
                     this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                         public void run() {
                             synchronized (SQLBans.this.bannedCacheSync) {
                                 SQLBans.this.removeCachedBan(name);
                             }
                         }
                     }, 1200);
                 }
             }
         } catch (final Exception e) {
             event.disallow(Result.KICK_OTHER, "Connection error: Please retry.");
             this.getLogger().log(Level.SEVERE, "Severe error on user connect", e);
         }
     }
 
     public void removeCachedBan(String name) {
         this.bannedCache.remove(name);
     }
 
 }
