 package com.nijiko.simpleshop;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.simpleshop.util.Downloader;
 
 import com.nijiko.simpleshop.util.Messaging;
 import com.nijiko.simpleshop.util.Misc;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.server.PluginEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*
  * SimpleShop v1.0
  * Copyright (C) 2011  Nijikokun <nijikokun@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
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
 public class SimpleShop extends JavaPlugin {
     public static final Logger log = Logger.getLogger("Minecraft");
 
     public static String name = "SimpleShop";
     public static String codename = "Sen";
    public static String version = "1.41";
 
     public iListen Listener = new iListen(this);
     public static Permissions Permissions;
     public static Misc Misc = new Misc();
     public static iProperty Settings, Items;
     public static String directory = "SimpleShop" + File.separator;
     public static String server_name = "Alfenia", shop_tag = "&d[&fShop&d]&f ", currency = "Coin";
     public static int max_per_purchase = 64, max_per_sale = 64;
     public static boolean utilize_stock = false;
     public static Server Server;
     private String database_type = "flatfile";
     public static String flatfile = directory + "shop.flat";
     public static String sqlite = "jdbc:sqlite:" + directory + "shop.db";
     public static String mysql = "jdbc:mysql://localhost:3306/minecraft";
     public static String mysql_user = "root";
     public static String mysql_pass = "pass";
     public static Timer timer = null;
     public static iConomy iConomy = null;
     public static HashMap<String, String> items;
     public static Wrapper Database = null;
 
     public SimpleShop() { }
 
     public void onDisable() {
         log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") disabled");
     }
 
     public void onEnable() {
         directory = getDataFolder() + File.separator;
         sqlite = "jdbc:sqlite:" + directory + "shop.db";
         flatfile = directory + "shop.flat";
 
         Server = this.getServer();
         Server.getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, new Listener(this), Priority.Monitor, this);
 
         setup();
         setupItems();
         setupPermissions();
         log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " (" + codename + ") loaded");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         try {
             if (sender instanceof Player) {
                 Player player = (Player) sender;
                 String split = "/" + cmd.getName().toLowerCase();
 
                 for (int i = 0; i < args.length; i++) {
                     split = split + " " + args[i];
                 }
 
                 Listener.onPlayerCommand(player, split);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return false;
     }
 
     public void setup() {
         extractDefaultFile(getDataFolder(), "SimpleShop.settings");
         extractDefaultFile(null, "items.db");
 
         // Properties
         Settings = new iProperty(getDataFolder() + File.separator + "SimpleShop.settings");
         Items = new iProperty("items.db");
 
         // Shop settings
         shop_tag = Settings.getString("shop-tag", shop_tag);
         max_per_purchase = Settings.getInt("max-items-per-purchase", max_per_purchase);
         max_per_sale = Settings.getInt("max-items-per-sale", max_per_sale);
         utilize_stock = Settings.getBoolean("use-stock", utilize_stock);
 
         // Database
         database_type = Settings.getString("database-type", database_type);
 
         // MySQL
         mysql = Settings.getString("mysql-db", mysql);
         mysql_user = Settings.getString("mysql-user", mysql_user);
         mysql_pass = Settings.getString("mysql-pass", mysql_pass);
 
         // Connect & Create
         if (database_type.equalsIgnoreCase("mysql")) {
             if(!(new File("lib" + File.separator, "mysql-connector-java-bin.jar").exists())) {
                 Downloader.install("http://mirror.anigaiku.com/Dependencies/mysql-connector-java-bin.jar", "mysql-connector-java-bin.jar");
             }
 
             Database = new Wrapper(Wrapper.Type.MYSQL);
         } else {
             if(!(new File("lib" + File.separator, "sqlitejdbc-v056.jar").exists())) {
                 Downloader.install("http://mirror.anigaiku.com/Dependencies/sqlitejdbc-v056.jar", "sqlitejdbc-v056.jar");
             }
 
             Database = new Wrapper(Wrapper.Type.SQLITE);
         }
     }
 
     public void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (this.Permissions == null) {
             if (test != null) {
                 this.Permissions = (Permissions) test;
                 System.out.println(Messaging.bracketize(name) + " hooked into Permissions.");
             } else {
                 log.info(Messaging.bracketize(name) + " Permission system not enabled. Disabling plugin.");
                 this.getServer().getPluginManager().disablePlugin(this);
             }
         }
     }
 
     public void setupItems() {
         Map mappedItems = null;
         items = new HashMap<String, String>();
 
         try {
             mappedItems = Items.returnMap();
         } catch (Exception ex) {
             log.info(Messaging.bracketize(name + " Flatfile") + " could not grab item list!");
         }
 
         if (mappedItems != null) {
             for (Object item : mappedItems.keySet()) {
                 String id = (String) item;
                 String itemName = (String) mappedItems.get(item);
                 items.put(id, itemName);
             }
         }
     }
 
     private void extractDefaultFile(File location, String name) {
         File actual = new File(location, name);
         if (!actual.exists()) {
             InputStream input = this.getClass().getResourceAsStream("/default/" + name);
             if (input != null) {
                 FileOutputStream output = null;
 
                 try {
                     output = new FileOutputStream(actual);
                     byte[] buf = new byte[8192];
                     int length = 0;
 
                     while ((length = input.read(buf)) > 0) {
                         output.write(buf, 0, length);
                     }
 
                     System.out.println("[SimpleShop] Default setup file written: " + name);
                 } catch (Exception e) {
                     e.printStackTrace();
                 } finally {
                     try {
                         if (input != null) {
                             input.close();
                         }
                     } catch (Exception e) {
                     }
                     try {
                         if (output != null) {
                             output.close();
                         }
                     } catch (Exception e) {
                     }
                 }
             }
         }
     }
 
     private class Listener extends ServerListener {
 
         private SimpleShop plugin;
 
         public Listener(SimpleShop thisPlugin) {
             this.plugin = thisPlugin;
         }
 
         @Override
         public void onPluginEnabled(PluginEvent event) {
             if (plugin.iConomy == null) {
                 Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");
 
                 if (iConomy != null) {
                     if (iConomy.isEnabled()) {
                         plugin.iConomy = (iConomy) iConomy;
                         System.out.println(Messaging.bracketize(plugin.name) + " hooked into iConomy.");
                     }
                 }
             }
 
             if (plugin.Permissions == null) {
                 Plugin Permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
 
                 if (Permissions != null) {
                     if (Permissions.isEnabled()) {
                         plugin.Permissions = (Permissions) Permissions;
                         System.out.println(Messaging.bracketize(plugin.name) + " hooked into Permissions.");
                     }
                 }
             }
         }
     }
 }
