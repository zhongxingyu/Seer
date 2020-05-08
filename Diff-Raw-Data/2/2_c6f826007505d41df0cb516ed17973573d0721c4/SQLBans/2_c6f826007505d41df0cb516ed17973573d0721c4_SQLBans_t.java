 /*
  * SQLBans
  * Copyright 2012 Matt Baxter
  *
  * Google Gson
  * Copyright 2008-2011 Google Inc.
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
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.kitteh.sqlbans.api.Player;
 import org.kitteh.sqlbans.api.SQLBansImplementation;
 import org.kitteh.sqlbans.api.Scheduler;
 import org.kitteh.sqlbans.commands.BanCommand;
 import org.kitteh.sqlbans.commands.KickCommand;
 import org.kitteh.sqlbans.commands.ReloadCommand;
 import org.kitteh.sqlbans.commands.UnbanCommand;
 import org.kitteh.sqlbans.exceptions.SQLBansException;
 
 public class SQLBans {
 
     public static class Messages {
 
         private static String COMMAND_NO_PERMISSION;
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
 
         public static String getCommandNoPermission() {
             return Messages.COMMAND_NO_PERMISSION;
         }
 
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
 
         public static void load(Config config) {
             Messages.COMMAND_NO_PERMISSION = Messages.color(config.getString("messages.command.nopermission"));
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
 
     private final BanCache banCache = new BanCache(this);
 
     private Config config;
 
     private final SQLBansImplementation implementation;
 
     private String serverName;
 
     public SQLBans(SQLBansImplementation implementation) {
         this.implementation = implementation;
 
         try {
             this.load();
         } catch (final SQLBansException e) {
             return;
         }
 
         // Command registration
         this.implementation.registerCommand("ban", new BanCommand(this));
         this.implementation.registerCommand("kick", new KickCommand(this));
         this.implementation.registerCommand("sqlbansreload", new ReloadCommand(this));
         this.implementation.registerCommand("unban", new UnbanCommand(this));
 
         this.getScheduler().repeatingTask(new BackupTask(this), 5, 300);
 
         this.implementation.registerLoginAttemptListening();
     }
 
     public BanCache getBanCache() {
         return this.banCache;
     }
 
     public File getDataFolder() {
         return this.implementation.getDataFolder();
     }
 
     public Logger getLogger() {
         return this.implementation.getLogger();
     }
 
     public Player[] getOnlinePlayers() {
         return this.implementation.getOnlinePlayers();
     }
 
     public Player getPlayer(String name) {
         return this.implementation.getPlayer(name);
     }
 
     public InputStream getResource(String path) throws IOException {
         final URL url = this.getClass().getClassLoader().getResource(path);
         if (url == null) {
             return null;
         }
         final URLConnection urlConnection = url.openConnection();
         urlConnection.setUseCaches(false);
         return urlConnection.getInputStream();
     }
 
     public Scheduler getScheduler() {
         return this.implementation.getScheduler();
     }
 
     public String getServerName() {
         return this.serverName;
     }
 
     public String getVersion() {
         return this.implementation.getVersion();
     }
 
     public void load() throws SQLBansException {
         this.config = new Config(this);
         final StringBuilder builder = new StringBuilder();
         try {
             final BufferedReader reader = new BufferedReader(new InputStreamReader(this.getResource("create.sql")));
             String next;
             while ((next = reader.readLine()) != null) {
                 builder.append(next);
             }
         } catch (final IOException e) {
             new SQLBansException("Could not load default table creation text", e).printStackTrace();
         }
 
         SQLBans.Messages.load(this.config);
         this.serverName = this.config.getString("server-name");
         final String host = this.config.getString("database.host");
         final int port = this.config.getInt("database.port");
         final String db = this.config.getString("database.database");
         final String user = this.config.getString("database.auth.username");
         final String pass = this.config.getString("database.auth.password");
         final String bansTableName = this.config.getString("database.tablenames.bans", "SQLBans_bans");
         final String logTableName = this.config.getString("database.tablenames.log", "SQLBans_log");
        SQLBans.TABLE_CREATE = String.format(builder.toString(), bansTableName, logTableName);
         try {
             SQLHandler.start(this, host, port, user, pass, db, bansTableName, logTableName);
         } catch (final SQLBansException e) {
             this.getLogger().log(Level.SEVERE, "Failure to load, shutting down", e);
             this.implementation.shutdown();
             throw new SQLBansException("Shutdown");
         }
     }
 
     public void processUserData(UserData data, boolean isJoin) {
         if (this.banCache.containsName(data.getName()) || this.banCache.containsIP(data.getIP())) {
             data.disallow(UserData.Result.KICK_BANNED, SQLBans.Messages.getDisconnectRejected());
             return;
         }
         try {
             if (!SQLHandler.canJoin(data.getName())) {
                 data.disallow(UserData.Result.KICK_BANNED, SQLBans.Messages.getDisconnectRejected());
                 this.banCache.addName(data.getName());
             }
             if (!SQLHandler.canJoin(data.getIP())) {
                 data.disallow(UserData.Result.KICK_BANNED, SQLBans.Messages.getDisconnectRejected());
                 this.banCache.addIP(data.getIP());
             }
         } catch (final Exception e) {
             data.disallow(UserData.Result.KICK_OTHER, "Connection error: Please retry.");
             this.getLogger().log(Level.SEVERE, "Severe error on user connect", e);
         }
     }
 
     public void saveResource(String path, boolean replace) throws IOException, SQLBansException {
         InputStream input = null;
         OutputStream output = null;
         try {
             input = this.getResource(path);
             if (input == null) {
                 throw new SQLBansException("Resource not found: " + path);
             }
             final File outputFile = new File(this.getDataFolder(), path);
             final int slashLocation = path.lastIndexOf("/");
             final File outputFolder = slashLocation >= 0 ? new File(this.getDataFolder(), path.substring(0, slashLocation)) : this.getDataFolder();
             outputFolder.mkdirs();
             if (replace || !outputFile.exists()) {
                 output = new FileOutputStream(outputFile);
                 final byte[] buf = new byte[1024];
                 int len;
                 while ((len = input.read(buf)) > 0) {
                     output.write(buf, 0, len);
                 }
             }
         } finally {
             try {
                 if (input != null) {
                     input.close();
                 }
                 if (output != null) {
                     output.close();
                 }
             } catch (final IOException e) {
                 // Moot!
             }
         }
     }
 
     public void sendMessage(Perm permission, String message) {
         this.implementation.sendMessage(permission, message);
     }
 }
