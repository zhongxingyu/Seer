 package me.Batsed.WorldTp;
 
 
 import java.io.File;
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WorldTp extends JavaPlugin {
 	
 	public Config config;
 	PluginDescriptionFile descFile = this.getDescription();
 	
 	private static String ordner = "plugins/WorldTp/saves";
 
 	public void onEnable() {
 		createConfig();
 		System.out.println("[WorldTp] Plugin by Batsed");
 		config = new Config(new File(ordner + File.separator + "Saves.yml"));
         config.setDefaults();
         Config.save();
 	}
 
 	public void onDisable() {
 		Config.save();
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		
 		String sprache1 = this.getConfig().getString("Config.language.Wt");
 		String sprache2 = this.getConfig().getString("Config.language.Leave");
 		String sprache3 = this.getConfig().getString("Config.language.Setspawnpoint");
 		String sprache4 = this.getConfig().getString("Config.language.Setspawnpoint2");
 		String sprache9 = this.getConfig().getString("Config.language.error.NoArgument");
 		String sprache10 = this.getConfig().getString("Config.language.error.TooManyArgument");
 		String sprache11 = this.getConfig().getString("Config.language.error.NoWarpPoint");
 		String sprache5 = this.getConfig().getString("Config.language.Help.Setspawnpoint");
 		String sprache6 = this.getConfig().getString("Config.language.Help.Wt");
 		String sprache7 = this.getConfig().getString("Config.language.Help.Leave");
 		String sprache8 = this.getConfig().getString("Config.language.Help.WorldTp");
 		String sprache12 = this.getConfig().getString("Config.language.error.error");
 		@SuppressWarnings("unused")
 		String sprache13 = this.getConfig().getString("Config.language.error.ConfigError");
 		String sprache14 = this.getConfig().getString("Config.language.error.GamemodeCreativeError");
 		String sprache15 = this.getConfig().getString("Config.language.error.SaveInventoryError");
 		String sprache16 = this.getConfig().getString("Config.language.info");
 		String sprache17 = this.getConfig().getString("Config.language.BlockLeave");
 		String sprache18 = this.getConfig().getString("Config.language.error.ClearInventoryError");
 		String sprache19 = this.getConfig().getString("Config.language.error.activateCommandInvbackError");
 		String sprache20 = this.getConfig().getString("Config.language.error.noPremmissions");
 		String sprache21 = this.getConfig().getString("Config.language.Help.Invback");
 		
 		//OldLocation Config
 		
 		Player p = (Player)sender;
 			
 		//Zum Spawnpoint teleportieren
 		//Teleporting to the spawn point
 		if(cmd.getName().equalsIgnoreCase("wt")) {
 			if(args.length < 1) {
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 				return false;
 			}
 			String spawnpoint = (args[0]);						    				    
 			//Anfang command "/wt info"	    
 			if (spawnpoint.equalsIgnoreCase("info")) {
 				if(p.hasPermission("worldtp.info")) {
 					//Fehlerberprpfung "info"
 					if(args.length < 2) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 						p.sendMessage("usage: <wt> <info> <warpname>");
 						return false;
 					}
 					if(args.length > 2) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
 						p.sendMessage("usage: <wt> <info> <warpname>");
 						return false;
 					}
 					//Hauptquellcode "info"
 					String warpname = (args[1]);
 				    		
 					String clearinvbycommand = this.getConfig().getString("Config."+ warpname +".ClearInvByCommand");
 					String saved = this.getConfig().getString("Config."+ warpname +".SaveInventory");
 					String game = this.getConfig().getString("Config."+ warpname +".GamemodeCreative");
 					String clearinv = this.getConfig().getString("Config."+ warpname +".ClearInventory");
 					String invback = this.getConfig().getString("Config."+ warpname +".activateCommandInvback");
 					String activateinvback = this.getConfig().getString("Config."+ warpname+".activateCommandInvback");
 					String loadinvbycommandleave = this.getConfig().getString("Config."+ warpname +".loadInvByCommandLeave");
 						    
 					if(game == null) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache11);
 						return false;
 					}
 					p.sendMessage(ChatColor.RED + "[WorldTp] ClearInvByCommand = " + ChatColor.AQUA + clearinvbycommand);
 					p.sendMessage(ChatColor.RED + "[WorldTp] SaveInventory = " + ChatColor.AQUA + saved);
 					p.sendMessage(ChatColor.RED + "[WorldTp] GamemodeCreative = " + ChatColor.AQUA + game);
 					p.sendMessage(ChatColor.RED + "[WorldTp] ClearInventory = " + ChatColor.AQUA + clearinv);
 					p.sendMessage(ChatColor.RED + "[WorldTp] activateCommandInvback = " + ChatColor.AQUA + invback);
 					p.sendMessage(ChatColor.RED + "[WorldTp] activateCommandInvback = " + ChatColor.AQUA + activateinvback);
 					p.sendMessage(ChatColor.RED + "[WorldTp] loadInvByCommandLeave = " + ChatColor.AQUA + loadinvbycommandleave);
 					
 					return true;
 				}else{
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 					return true;
 				}
 				
 			}
 			
 			//Anfang Command "/wt reload"
 			if(spawnpoint.equalsIgnoreCase("reload")) {
 				if(p.hasPermission("worldtp.reload")) {
 				
 					//Fehlerberprfung
 					if(args.length < 1) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 						return false;
 					}
 					if(args.length > 1) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
 						return false;
 					}
 					//Hauptquellcode "/wt reload"
 					this.saveConfig();
 					reloadConfig();
 					p.sendMessage(ChatColor.RED + "[WorldTp] Config aktualliesiert");
 					return true;
 					
 				}else{
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 					return true;
 				}
 			}
 			if(spawnpoint.equalsIgnoreCase("list")) {
 				if(p.hasPermission("worldtp.list")) {
 					String warps = Config.configuration.getString(Config.warps);
 					
 					if(warps == null) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Keine Warps verfgbar");
 						return true;
 					}
					p.sendMessage(ChatColor.RED + "[WorldTp] **Warplist**");
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + warps);
 					return true;
 				}else{
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 					return true;
 				}
 			}
 			//Anfang Command "/wt"
 			else{
 				if(p.hasPermission("worldtp.wt")) { 
 					
 					String PlayerName = p.getName();
 					String saved = this.getConfig().getString("Config."+ spawnpoint +".SaveInventory");
 					String game = this.getConfig().getString("Config."+ spawnpoint +".GamemodeCreative");
 					String clearinv = this.getConfig().getString("Config."+ spawnpoint +".ClearInventory");
 					String invback = this.getConfig().getString("Config."+ spawnpoint +".activateCommandInvback");
 					String AnotherWarp = Config.configuration.getString(Config.oldLoc + PlayerName + ".AnotherWarp");
 					String AnotherWarpOn = this.getConfig().getString("Config."+ spawnpoint +".TeleportToAnOtherWarp");
 					String DoubleWarp = Config.configuration.getString(Config.oldLoc + PlayerName + ".doubleWarp");
 					    
 					double locY = this.getConfig().getDouble("Config."+ spawnpoint +".spawn.Y");
 					double locX = this.getConfig().getDouble("Config."+ spawnpoint +".spawn.X");
 					double locZ = this.getConfig().getDouble("Config."+ spawnpoint +".spawn.Z");
 					    
 					//Fehler berprfung
 					if(args.length < 1) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 						p.sendMessage("usage: <wt> <warpname>");
 						return false;
 					}
 					if(args.length > 1) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
 						p.sendMessage("usage: <wt> <warpname>");
 						return false;
 					}
 					if(locX == 0) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache11);
 						return false;
 					}
 					if(AnotherWarpOn.length() > 5) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' hat in der Config bei TeleportToAnOtherWarp einen Fehler");
 						return false;
 					}
 					if(AnotherWarpOn.length() < 4) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' hat in der Config bei TeleportToAnOtherWarp einen Fehler");
 						return false;
 					}
 					if(game.length() > 5) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' " + sprache14);
 						return false;
 					}
 					if(game.length() < 4) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' " + sprache14);
 						return false;
 					}
 					if(saved.length() > 5) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' " + sprache15);
 						return false;
 					}
 					if(saved.length() < 4) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' " + sprache15);
 						return false;
 					}
 					if(clearinv.length() > 5) {
 				    	p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' " + sprache18);
 				    	return false;
 					}
 				    if(clearinv.length() < 4) {
 				    	p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "' " + sprache18);
 				    	return false;
 				    }
 				    if(invback.length() > 5) {
 				    	p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint +"' " + sprache19);
 				    	return false;
 				    }
 				    if(invback.length() < 4) {
 				    	p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint +"' " + sprache19);
 				    	return false;
 				    }
 				    
 				    
 				    //hauptquellcode "wt"
 				    Location loc = new Location(getServer().getWorld(p.getWorld().getName()),locX, locY, locZ);
 				    
 				    if(AnotherWarpOn.length() == 5) {
 				    	if(AnotherWarp == "asdn") {
 				    		p.sendMessage(ChatColor.RED + "[WorldTp] Mehrmals Warpen ist nicht erlaubt");
 				    		return true;
 				    	}
 				    }
 				
 				    if(!(DoubleWarp == "doubleWarpOn")) {
 				    	Config.OldPlayerLocName(p, spawnpoint);				    	
 				    }
 				    
 				    Config.DoubleWarp(p);
 				    
 				    Config.OldPlayerInvbackDoubleFine(p);
 				    Config.TeleportToWarp(p);				    
 				    
 				    p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache1);
 				    
 				    p.teleport(loc);
 				    
 				    if(game.length() == 4) {
 				    	p.setGameMode(GameMode.CREATIVE);
 				    }
 				    if(saved.length() == 4) {  
 				    	return new InventoryManager(cmd, args, p, clearinv).saveToFile(p);
 				    }
 				    if(clearinv.length() == 4) {
 				    	return new InventoryManager(cmd, args, p, clearinv).clearInventory(p);
 				    }
 				    
 				}else{
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 					return false;
 				}
 			}
 		}
 		
 		//Spawnpoint von der Welt setzen
 		//Defining the spawn point for the creative world
 		if(cmd.getName().equalsIgnoreCase("setspawnpoint")) {
 			if(p.hasPermission("worldtp.setspawnpoint")) {
 				if(args.length < 3) {
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 					return false;
 				}
 				if(args.length > 3) {
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
 					return false;
 				}
 				if(args.length == 3) {
 					String spawnName = (args[0]);
 					String save = (args[1]);
 					String game = (args[2]);
 					
 					//Fehlerberprfung
 					if(spawnName.equalsIgnoreCase("info")) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Der Name ist als Warp nich erlaubt");
 						return true;
 					}
 					if(spawnName.equalsIgnoreCase("reload")) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Der Name ist als Warp nich erlaubt");
 						return true;
 					}
 					if(spawnName.equalsIgnoreCase("list")) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Der Name ist als Warp nich erlaubt");
 						return true;
 					}
 					if(spawnName.equalsIgnoreCase("asdi")) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Der Name ist als Warp nicht erlaubt");
 						return true;
 					}
 					if(spawnName.equalsIgnoreCase("finderror")) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Der Name ist als Warp nicht erlaubt");
 						return true;
 					}
 					getConfig().set("Config."+ spawnName +".spawn.X", p.getLocation().getX());
 					getConfig().set("Config."+ spawnName +".spawn.Y", p.getLocation().getY());
 					getConfig().set("Config."+ spawnName +".spawn.Z", p.getLocation().getZ());
             		getConfig().get("Config."+ spawnName +".activateCommandLeave");
             		this.getConfig().set("Config."+ spawnName +".activateCommandLeave", true);
             		getConfig().get("Config."+ spawnName +".ClearInvByCommand");
 					this.getConfig().set("Config."+ spawnName +".ClearInvByCommand", true);
 					getConfig().get("Config."+ spawnName +".TeleportToAnOtherWarp");
 					this.getConfig().set("Config."+ spawnName +".TeleportToAnOtherWarp", true);
 					this.getConfig().getList("Config.Warps."+ spawnName);
 					Config.WarpUp(spawnName);
 				
 	            	if (save.equalsIgnoreCase("nosave")) {
 						getConfig().get("Config."+ spawnName +".SaveInventory");
 						this.getConfig().set("Config."+ spawnName +".SaveInventory", false);
 						getConfig().get("Config."+ spawnName +".activateCommandInvback");
 						this.getConfig().set("Config."+ spawnName +".activateCommandInvback", true);
 						getConfig().get("Config."+ spawnName +".ClearInventory");
 						this.getConfig().set("Config."+ spawnName +".ClearInventory", false);
 						getConfig().get("Config."+ spawnName +".loadInvByCommandLeave");
 						this.getConfig().set("Config."+ spawnName +".loadInvByCommandLeave", false);
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache3);
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnName + "', '" + save + "' " + sprache16 + " '" + game + "'");
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache4);					
 						this.getConfig().options().copyDefaults(true);
 						this.saveConfig();
 					}
 	            	if (save.equalsIgnoreCase("withsave")) {
 						getConfig().get("Config."+ spawnName +".SaveInventory");
 						this.getConfig().set("Config."+ spawnName +".SaveInventory", true);
 						getConfig().get("Config."+ spawnName +".activateCommandInvback");
 						this.getConfig().set("Config."+ spawnName +".activateCommandInvback", false);
 						getConfig().get("Config."+ spawnName +".ClearInventory");
 						this.getConfig().set("Config."+ spawnName +".ClearInventory", true);
 						getConfig().get("Config."+ spawnName +".loadInvByCommandLeave");
 						this.getConfig().set("Config."+ spawnName +".loadInvByCommandLeave", true);
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache3);
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnName + "', '" + save + "' " + sprache16 + " '" + game + "'");
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache4);
 	                
 						this.getConfig().options().copyDefaults(true);
 						this.saveConfig();
 	            	}
 	            	
 	            	if(game.equalsIgnoreCase("1")) {
 	            		getConfig().get("Config."+ spawnName +".GamemodeCreative");
 						this.getConfig().set("Config."+ spawnName +".GamemodeCreative", true);
 						this.getConfig().options().copyDefaults(true);
 						this.saveConfig();
 						return true;
 					}
 					
 					if(game.equalsIgnoreCase("0")) {
 						getConfig().get("Config."+ spawnName +".GamemodeCreative");
 						this.getConfig().set("Config."+ spawnName +".GamemodeCreative", false);
 						this.getConfig().options().copyDefaults(true);
 						this.saveConfig();
 						return true;
 						
 					}else{
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache12);
 						return false;
 					}
 				}else {
 					return false;
 				}
 			}else{
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 				return true;
 			}
 		}
 		//Um hilfe von WorldTp zu bekommen
 		//Getting help from WorldTP
 		if(cmd.getName().equalsIgnoreCase("worldtp")) {
 			if(p.hasPermission("worldtp.worldtp")) {
 					if(args.length == 0) {
 						p.sendMessage(ChatColor.RED + "/setspawnpoint: " + ChatColor.AQUA + sprache5);
 						p.sendMessage(ChatColor.RED + "/wt: " + ChatColor.AQUA + sprache6);
 						p.sendMessage(ChatColor.RED + "/worldtp: " + ChatColor.AQUA + sprache8);
 						p.sendMessage(ChatColor.RED + "/leave: " + ChatColor.AQUA + sprache7);
 						p.sendMessage(ChatColor.RED + "/invback: " + ChatColor.AQUA + sprache21);
 						p.sendMessage(ChatColor.RED + "/wt info: " + ChatColor.AQUA + "Gibt infos ber dein Spawn");
                 
 						return true;
 					}else {
 						return false;
 					}
 				}else{
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 					return true;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("clearinv")) {
				if(args.length < 1) {
					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
					return false;
				}
				if(args.length > 1) {
					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
					return false;
				}
 				if(p.hasPermission("worldtp.clearinv")) {
 					
					String spawnName = (args[0]);
 					String clearinvByCommand = this.getConfig().getString("Config."+ spawnName +".ClearInvByCommand");
 					String clearinv = this.getConfig().getString("Config."+ spawnName +".ClearInventory");
 						
 					//Fehler berprfung
 					if(clearinvByCommand.length() > 5) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint "+ spawnName + "hat in der Config bei ClearInvByCommand einen Fehler");
 							return false;
 					}
 					if(clearinvByCommand.length() < 4) {
 							p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint "+ spawnName + "hat in der Config bei ClearInvByCommand einen Fehler");
 							return false;
 					}
 						
 					//hauptquellcode
 					if(clearinvByCommand.length() == 4) {
 						return new InventoryManager(cmd, args, p, clearinv).clearInventory(p);
 					}
 					if(clearinvByCommand.length() == 5) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Fr diesen spawn ist der Befehl nicht erlaubt");
 						return true;
 					}
 					
 				}else{
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 					return false;
 				}
 		}
 		
 		//Gibt dein Inventar zurck
 		if(cmd.getName().equalsIgnoreCase("invback")) {
 			if(p.hasPermission("worldtp.invback")) {
				if(args.length > 0) {
						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
						return false;
				}
 				if(args.length == 0){
 					String PlayerName = p.getName();
 					String spawnName = Config.configuration.getString(Config.oldLoc + PlayerName + ".LastSpawnPoint");
 					String spawnNameError = Config.configuration.getString(Config.oldLoc + PlayerName + ".LastSpawnPointError");
 					String clearinv = this.getConfig().getString("Config."+ spawnName +".ClearInventory");
 					String invback = this.getConfig().getString("Config."+ spawnName +".activateCommandInvback");
 				
 					if(invback.length() == 5) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Kommando 'invback' fr diesen Spawnpoint deaktieviert");
 						return true;
 					}
 					if(spawnNameError == "asdf") {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Kommando '/invback' schonmal benutzt");
 						return true;
 					}
 					Config.OldPlayerInvbackDouble(p);
 					Config.OldPlayerInvback(p);
 					Config.save();
 					if(invback.length() == 4) {
 						return new InventoryManager(cmd, args, p, clearinv).loadFromFile(p);
 					}
 					return true;
 				}
 			}else{
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 				return true;
 			}
 		}	
 		//Aus der Welt zum alten Standpunkt teleportieren
 		//Teleporting from the world to the old place
 		if(cmd.getName().equalsIgnoreCase("leave")) {
 			if(p.hasPermission("worldtp.leave")) {
 				if(args.length == 0) {
 					String PlayerName = p.getName();
 					String spawnName = Config.configuration.getString(Config.oldLoc + PlayerName + ".LastSpawnPoint");					
 					String clearinv = this.getConfig().getString("Config."+ spawnName +".ClearInventory");
 					String loadInvByLeave = this.getConfig().getString("Config."+ spawnName +".loadInvByCommandLeave");
 					String leave = this.getConfig().getString("Config."+ spawnName +".activateCommandLeave");
 					String s = "asdi";
 					
 					if(spawnName == "asde") {
 						p.sendMessage(ChatColor.RED + "[WorldTp] Da du /invback gemacht hast, kannst du dich nicht mehr zurckporten.");
 						return true;
 					}
 					if(s == spawnName) {
 						p.sendMessage(ChatColor.RED + "[WorldTP] Du kannst dich nur einmal zurck warpen");
 						return true;
 					}
 						
 					if(leave.length() == 5) {
 						p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache17);
 						return false;
 					}
 					
 					if(!(spawnName == "asdi")) {
 						Config.OldPlayerLeave(p);
 						Config.save();
 					}
 					
 					Config.TeleportToWarpLeave(p);
 					Config.DoubleWarpOff(p);
 					
 			    	double LocX = Config.configuration.getDouble(Config.oldLoc + PlayerName + ".spawn.X");
 					double LocY = Config.configuration.getDouble(Config.oldLoc + PlayerName + ".spawn.Y");
 					double LocZ = Config.configuration.getDouble(Config.oldLoc + PlayerName + ".spawn.Z");
 						
 					Location loc = new Location(getServer().getWorld(p.getWorld().getName()),LocX, LocY, LocZ);
 						
 					p.teleport(loc);
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache2);
 					GameMode gamemode = p.getGameMode();
 					if(gamemode == GameMode.CREATIVE) {
 						p.setGameMode(GameMode.SURVIVAL);
 					}
 					if(loadInvByLeave.length() == 4) {
 						return new InventoryManager(cmd, args, p, clearinv).loadFromFile(p);
 					}
 					return true;
 				}else {
 					return false;
 				}
 			}else{
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache20);
 				return true;
 			}
 		}
 		return true;
 		
 	}
     public Config getConfiguration() {
 		return config;
     }
 	public HashMap<Player, Location> oldLocationList = new HashMap<Player, Location>();
 		
 	public void createConfig() {
 		
 		//language
 		String path1 = "Config.language.Wt";
 		this.getConfig().addDefault(path1, "You have been teleported");
 		String path2 = "Config.language.Leave";
 		this.getConfig().addDefault(path2, "You have been teleported back to your old position");
 		String path3 = "Config.language.Setspawnpoint";
 		this.getConfig().addDefault(path3, "You have defined the spawnpoint");
 		String path17 = "Config.language.BlockLeave";
 		this.getConfig().addDefault(path17, "Leave is not permitted for this warp");
 		String path4 = "Config.language.Setspawnpoint2";
 		this.getConfig().addDefault(path4, "Saved");
 		String path16 = "Config.language.info";
 		this.getConfig().addDefault(path16, "in Gamemode");
 		
 		//errors
 		String path9 = "Config.language.error.NoArgument";
 		this.getConfig().addDefault(path9, "Not enough arguments");
 		String path10 = "Config.language.error.TooManyArgument";
 		this.getConfig().addDefault(path10, "Too many arguments");
 		String path11 = "Config.language.error.NoWarpPoint";
 		this.getConfig().addDefault(path11, "Warppoint doesn't exist");
 		String path12 = "Config.language.error.error";
 		this.getConfig().addDefault(path12, "Your input ist not correct");
 		String path13 = "Config.language.error.ConfigError";
 		this.getConfig().addDefault(path13, "The WorldTP config contains an error");
 		String path14 = "Config.language.error.GamemodeCreativeError";
 		this.getConfig().addDefault(path14, "has a config error at GamemodeCreative");
 		String path15 = "Config.language.error.SaveInventoryError";
 		this.getConfig().addDefault(path15, "has a config error at SaveInventory");
 		String path18 = "Config.language.error.ClearInventoryError";
 		this.getConfig().addDefault(path18, "has a config error at ClearInventory");
 		String path19 = "Config.language.error.activateCommandInvbackError";
 		this.getConfig().addDefault(path19, "has a config error at activateCommandInvback");
 		String path20 = "Config.language.error.noPremmissions";
 		this.getConfig().addDefault(path20, "You don't have permission to execute that command");
 		
 		//help
 		String path5 = "Config.language.Help.Setspawnpoint";
 		this.getConfig().addDefault(path5, "Sets the spawn point you reach with /wt");
 		String path6 = "Config.language.Help.Wt";
 		this.getConfig().addDefault(path6, "You go to the spawn point you set");
 		String path7 = "Config.language.Help.Leave";
 		this.getConfig().addDefault(path7, "You leave the creative world");
 		String path8 = "Config.language.Help.WorldTp";
 		this.getConfig().addDefault(path8, "Read the help for WorldTP");
 		String path21 = "Config.language.Help.Invback";
 		this.getConfig().addDefault(path21, "Gives back your inventory");
 		
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	}
 }
