 package com.github.grandmarket.market;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import lib.PatPeter.SQLibrary.*;
 
 public class Market extends JavaPlugin implements Listener {
 	public SQLite dbconn;
 	public Logger logger;
 	public void onEnable() {
 		logger = Logger.getLogger("Minecraft");
 		dbconn = new SQLite(logger, "", "GrandMarket", "./plugins/GrandMarket/");
 		dbconn.open();
 		if(!dbconn.checkTable("settings")) {
 			logger.log(Level.INFO, "GrandMarket: Creating table \"settings\" in database \""+dbconn.name+"\"");
 			dbconn.createTable("CREATE TABLE settings (id INTEGER NOT NULL PRIMARY KEY, setting TEXT, value BLOB)");
 		}
 		try {
 			if(!dbconn.query("SELECT * FROM settings WHERE setting='setup' LIMIT 1").next()) {
 				getLogger().log(Level.WARNING, "GrandMarket: The setup has not been completed.  We recomend that this be done ASAP.");	
 			}
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		getLogger().info("The market plugin has been enabled.");
 	}
 	public void onDisable() {
 		getLogger().info("The market plugin has been disabled.");
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("market")) {
 			if(args.length < 1) {
 				try {
 					ResultSet query = dbconn.query("SELECT * FROM settings WHERE setting='mainText' LIMIT 1");
 					if(query.first()) {
 						sender.sendMessage(query.getString("value"));
 					}
 					else {
 						if(sender.isOp()) {
 							sender.sendMessage("The market is a marketplace where people can buy and sell items.");
 							sender.sendMessage("This message is the default help message.  Change the plugin settings (or go through the setup) to change this message.");
 						}
 					}
 				}
 				catch (SQLException e) {
 					e.printStackTrace();
					getLogger().log(Level.SEVERE, "SQLException onCommand market.main");
 				}
				return true;
 			}
 		}
 		return false;
 	}
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		
 	}
 }
