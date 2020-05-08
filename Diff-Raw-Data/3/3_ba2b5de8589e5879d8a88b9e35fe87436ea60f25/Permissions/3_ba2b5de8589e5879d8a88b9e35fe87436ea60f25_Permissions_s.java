 /*
  * Copyright (C) 2013 Connor Monahan
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package me.cmastudios.permissions;
 
 import java.io.File;
 import java.sql.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import me.cmastudios.permissions.commands.*;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Connor Monahan
  */
 public class Permissions extends JavaPlugin {
 
     private Map<Player, PermissionAttachment> attachments = new HashMap();
     protected Connection database;
 
     @Override
     public void onEnable() {
         this.getServer().getPluginManager().registerEvents(new PermissionsListener(this), this);
         for (Player player : this.getServer().getOnlinePlayers()) {
             this.updatePermissions(player);
         }
         this.saveDefaultConfig();
         this.getConfig().options().copyDefaults(false);
         this.connectDatabase();
         this.getCommand("setgroup").setExecutor(new SetGroupCommand(this));
     }
 
     @Override
     public void onDisable() {
         for (Player player : this.getServer().getOnlinePlayers()) {
             this.removeAttachment(player);
         }
         attachments.clear();
         if (this.database != null) {
             try {
                 this.database.close();
             } catch (SQLException ex) {
             }
         }
     }
 
     public void connectDatabase() {
         try {
             if (database != null && !database.isClosed()) {
                 database.close();
             }
         } catch (SQLException ex) {
             this.getLogger().log(Level.SEVERE, "Failed to close existing connection to database", ex);
         }
         try {
             if (this.getConfig().getBoolean("mysql.enabled", false)) {
                 Class.forName("com.mysql.jdbc.Driver").newInstance();
                 this.database = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s",
                         this.getConfig().getString("mysql.host"), this.getConfig().getInt("mysql.port"), this.getConfig().getString("mysql.database")),
                         this.getConfig().getString("mysql.username"), this.getConfig().getString("mysql.password"));
             } else {
                 Class.forName("org.sqlite.JDBC").newInstance();
                 File databaseFile = new File(this.getDataFolder(), "userdb.sl3");
                 this.database = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath());
             }
             try (Statement initStatement = this.database.createStatement()) {
                 initStatement.executeUpdate("CREATE TABLE IF NOT EXISTS playergroups (player VARCHAR(16) PRIMARY KEY, group_name TEXT, expiration_date DATETIME NULL)");
                 try { // Update code
                     initStatement.executeUpdate("ALTER TABLE playergroups ADD COLUMN expiration_date DATETIME NULL");
                 } catch (SQLException ignored) {
                 }
             }
         } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
             this.getLogger().log(Level.SEVERE, "Failed to load database driver", ex);
         } catch (SQLException ex) {
             this.getLogger().log(Level.SEVERE, "Failed to load database", ex);
         }
     }
 
     public Connection getDatabaseConnection() {
         if (this.getConfig().getBoolean("mysql.enabled", false)) {
             try {
                 if (database == null || !database.isValid(1)) {
                     this.connectDatabase();
                 }
             } catch (SQLException ex) {
                 this.connectDatabase();
             }
         }
         if (database == null) {
             this.connectDatabase();
         }
         return database;
     }
 
     public void updatePermissions(Player player) {
         this.removeAttachment(player);
         PermissionAttachment attachment = player.addAttachment(this);
         this.attachments.put(player, attachment);
         Group group = null;
         try {
             group = PlayerGroupDatabase.getGroup(this, player);
         } catch (SQLException ex) {
             this.getLogger().log(Level.SEVERE, "Failed to get group for player " + player.getName(), ex);
         }
         if(group != null) {
             try {
                 Timestamp expirationDate = PlayerGroupDatabase.getExpirationDate(this,player);
                 if(expirationDate!=null && expirationDate.before(new Timestamp(System.currentTimeMillis()))) {
                     group = group.getFallbackGroup(this.getConfig());
                     PlayerGroupDatabase.setGroup(this, player, group, null);
                 }
             } catch (SQLException ex) {
                 this.getLogger().log(Level.SEVERE, "Failed to check rank expiration for player " + player.getName(), ex);
             }
         }
         if (group == null) {
             group = Group.getDefaultGroup(this.getConfig());
             if (group == null) {
                 throw new RuntimeException(new InvalidConfigurationException("There is no default group defined for this server!"));
             }
             try {
                 PlayerGroupDatabase.setGroup(this, player, group, null);
             } catch (SQLException ex) {
                 this.getLogger().log(Level.SEVERE, "Failed to change rank to default for " + player.getName(), ex);
             }
         }
         Map<String, Boolean> playerPermissions = group.getPermissions(this.getConfig(), player.getWorld());
         for (Map.Entry<String, Boolean> entry : playerPermissions.entrySet()) {
             attachment.setPermission(entry.getKey(), entry.getValue());
         }
         player.setDisplayName(String.format("%s%s%s", group.getPrefix(), player.getName(), group.getSuffix()));
     }
 
     public void removeAttachment(Player player) {
         if (attachments.containsKey(player)) {
             player.removeAttachment(attachments.remove(player));
         }
     }
 }
