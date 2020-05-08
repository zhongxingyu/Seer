 package de.darcade.movemobs;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Horse.Color;
 import org.bukkit.entity.Horse.Style;
 import org.bukkit.entity.Horse.Variant;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class movemobs extends JavaPlugin {
 
 	// String databasedir = this.getDataFolder().getAbsolutePath().toString();
 	String databasedir = "jdbc:sqlite:plugins/MoveMobs/database.sqlite";
 
 	SQLitehandler sqlitehandler = new SQLitehandler(databasedir);
 
 	@Override
 	public void onDisable() {
 		System.out.println("[MoveMobs] plugin disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 		boolean success = (new File(this.getDataFolder().getAbsolutePath()))
 				.mkdirs();
 		if (!success) {
 			System.out.println("[MoveMobs] could not create plugin directory");
 		}
 		sqlitehandler.init();
 		this.createConfig();
 		PluginDescriptionFile descFile = this.getDescription();
 		System.out.println("[MoveMobs] plugin enabled!");
 		System.out.println("Plugin Version: " + descFile.getVersion());
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String cmdLabel, String[] args) {
 		Player p = (Player) sender;
 		
 		String username = p.getDisplayName();
 		
 		//load informations from the config
 		String waiting = this.getConfig().getString("Message.waiting");
 		String triedagain = this.getConfig().getString("Message.alreadydone");
 		String fewargs = this.getConfig().getString("Message.fewargs");
 		String nomob = this.getConfig().getString("Message.nomob");
 		String spawningmob = this.getConfig().getString("Message.spawningmob");
 		
 
 		if (cmd.getName().equalsIgnoreCase("movemob")) {
 			if (p.hasPermission("movemob")) {
 				if (args.length == 0) {
 					p.sendMessage(ChatColor.RED + fewargs);
 					p.sendMessage(ChatColor.RED + "Usage: /movemob [pickup/place]");
 				} else {
 					if (args[0].equalsIgnoreCase("pickup")) {
 						
 						String mob = sqlitehandler.showmob(username);
 						
 						p.sendMessage(ChatColor.GREEN
 								+ waiting);
 
 						if (mob == null) {
 							getServer().getPluginManager().registerEvents(
 									new killlistener(p, this, true), this);
 						} else {
 							if (mob.equalsIgnoreCase("NULL")) {
 								getServer().getPluginManager().registerEvents(
 										new killlistener(p, this, false), this);
 							} else {
 								p.sendMessage(ChatColor.RED
 										+ triedagain);
 							}
 
 						}
 
 					}
 					else if (args[0].equalsIgnoreCase("place")){
 						if (sqlitehandler.showmob(p.getDisplayName()) == null || sqlitehandler.showmob(p.getDisplayName()).equalsIgnoreCase("NULL")) {
 							p.sendMessage(nomob);
 						} else {
 							p.sendMessage(ChatColor.GREEN + spawningmob);
 							if (sqlitehandler.showmob(p.getDisplayName()).equalsIgnoreCase("HORSE")){
 								String[] playershorse = sqlitehandler.gethorse(username);
 								
 								if (playershorse.length == 0)
 									p.sendMessage("FAIL");
 								
 								//playershorse.teleport(p.getLocation());
 								Horse spawnedhorse = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
 								spawnedhorse.setColor(Color.valueOf(playershorse[0]));
 								spawnedhorse.setStyle(Style.valueOf(playershorse[1]));
 								spawnedhorse.setVariant(Variant.valueOf(playershorse[2]));
 							} else {
								p.getWorld().spawnEntity(p.getLocation(), EntityType.fromName(sqlitehandler.showmob(p.getDisplayName())));
 							}
 							sqlitehandler.clearuser(p.getDisplayName());
 						}
 					} else {
 						p.sendMessage("Usage: /movemob [pickup/place]");
 					}
 				}
 			}
 		}
 
 		return true;
 	}
 	
 	
 	
 	private void createConfig() {
 		this.saveDefaultConfig();
 		System.out.println("[MoveMobs] checking config...");
 	}
 }
