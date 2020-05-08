 /*
  * MCTrade
  * Copyright (C) 2012 Fogest <http://fogest.net16.net> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package me.fogest.mctrade;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 import me.fogest.mctrade.commands.PlayerCommands;
 import net.milkbowl.vault.Vault;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.permission.Permission;
 
 public class MCTrade extends JavaPlugin {
 	private static MCTrade plugin;
 	private MessageHandler msg;
     public static Economy econ = null;
     public static Permission perms = null;
 
 	public static String mysqlHostname; 
 	public static String mysqlPort;
 	public static String mysqlUsername;
 	public static String mysqlPassword;
 	public static String mysqlDatabase;
 	
 	public static double tax;
 	
 	public static String webAddress;
 	
 	public Updater updater;
 	
 	public static ArrayList<String> moderaters;
 	public static ArrayList<String> admins;
 	
 	public boolean update;
 	
 	public static boolean checkIP;
     
 	public MCTrade() {
 		plugin = this;
 		msg = new MessageHandler("[MCTrade]");
 		moderaters = new ArrayList<String>();
 		admins = new ArrayList<String>();
 	}
 	
 	@Override
 	public void onEnable() {
 		reloadSettings();
 
 		// Registering the command executors
 		getCommand("mctrade").setExecutor(new PlayerCommands(this,msg));
 		DatabaseManager.enableDB();
 		
 		econ = getProvider(Economy.class);
 		perms = getProvider(Permission.class);
 		if(update == true)
 			updater = new Updater(this, "mctrade");
 		
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		} catch (IOException e) {
 		    // Failed to submit the stats :-(
 		}
 		saveToDatabase();
 	}
 	public void onReload() {
 		reloadSettings();
 	}
 	public <T> T getProvider(final Class<T> c) {
         final org.bukkit.plugin.RegisteredServiceProvider<T> provider
             = Bukkit.getServicesManager().getRegistration(c);
         if (provider != null)
             return provider.getProvider();
         return null;
     }
 	public void reloadSettings() {
 		reloadConfig();
 		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
 		mysqlHostname = getConfig().getString("mysql.hostname");
 		mysqlPort = getConfig().getString("mysql.port");
 		mysqlUsername = getConfig().getString("mysql.username");
 		mysqlPassword = getConfig().getString("mysql.password");
 		mysqlDatabase = getConfig().getString("mysql.database");
 		if(getConfig().isSet("two.one") == true) {}
 		
         List<String> modsList = getConfig().getStringList("Web.Access.Moderator");
         for (String s : modsList){
             moderaters.add(s);
         }
         List<String> adminList = getConfig().getStringList("Web.Access.Admin");
         for (String s : adminList){
         	admins.add(s);
         }
         update = getConfig().getBoolean("TradeOptions.VersionNotifications");
         tax = getConfig().getDouble("TradeOptions.Tax");
         webAddress = getConfig().getString("Web.MctradeDirectory");
         checkIP = getConfig().getBoolean("TradeOptions.CheckTradeIP");
 		
 	}
 	public void saveToDatabase() {
 		for(int i = 0;i < moderaters.size();i++) {
 			DatabaseManager.setUserLevelForMod(moderaters.get(i));
 		}
 		for(int i = 0;i < admins.size();i++) {
 			DatabaseManager.setUserLevelForAdmin(admins.get(i));
 		}
 	}
 	public void onDisable() {
 		DatabaseManager.disableDB();
 	}
     public static MCTrade getPlugin(){
         return plugin;
     }
 }
