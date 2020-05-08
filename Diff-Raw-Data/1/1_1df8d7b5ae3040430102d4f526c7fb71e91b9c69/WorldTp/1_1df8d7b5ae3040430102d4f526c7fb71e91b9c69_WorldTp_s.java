 package me.Batsed.WorldTp;
 
 
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
 
 	PluginDescriptionFile descFile = this.getDescription();
 
 	public void onEnable() {
 		loadConfig();
 		System.out.println("[WorldTp] Plugin by Batsed");
 		System.out.println("[WorldTp] activate plugin");
 	}
 	
 	public void onDisable() {
 		System.out.println("[WorldTp] deactivate plugin");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		
 		String sprache1 = this.getConfig().getString("Config.language.Wt");
 		String sprache2 = this.getConfig().getString("Config.language.Leave");
 		String sprache3 = this.getConfig().getString("Config.language.Setspawnpoint");
 		String sprache4 = this.getConfig().getString("Config.language.Setspawnpoint2");
 		String sprache9 = this.getConfig().getString("Config.language.NoArgument");
 		String sprache10 = this.getConfig().getString("Config.language.TooManyArgument");
 		String sprache11 = this.getConfig().getString("Config.language.NoWarpPoint");
 		String sprache5 = this.getConfig().getString("Config.language.Help.Setspawnpoint");
 		String sprache6 = this.getConfig().getString("Config.language.Help.Wt");
 		String sprache7 = this.getConfig().getString("Config.language.Help.Leave");
 		String sprache8 = this.getConfig().getString("Config.language.Help.WorldTp");
 		String sprache12 = this.getConfig().getString("Config.language.error");
 		String sprache13 = this.getConfig().getString("Config.language.ConfigError");
 		String sprache14 = this.getConfig().getString("Config.language.GamemodeCreativeError");
 		String sprache15 = this.getConfig().getString("Config.language.SaveInventoryError");
 		String sprache16 = this.getConfig().getString("Config.language.Options");
 		String sprache17 = this.getConfig().getString("Config.language.BlockLeave");
 		
 		Player p = (Player)sender;
 		
 		String spawnpoint;
 		String spawnName;
 		String game;
 		String save;
 		
 		//Zum Spawnpoint teleportieren
 		//Teleporting to the spawn point
 		if(cmd.getName().equalsIgnoreCase("wt")) {
 			if(args.length < 1) {
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 				return false;
 			}
 			if(args.length > 1) {
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
 				return false;
 			}
 			if(args.length == 1) {
 				spawnpoint = (args[0]);		
 				
 				double locY = this.getConfig().getDouble("Config."+ spawnpoint +".spawn.Y");
 			    double locX = this.getConfig().getDouble("Config."+ spawnpoint +".spawn.X");
 			    double locZ = this.getConfig().getDouble("Config."+ spawnpoint +".spawn.Z");
 			    String saved = this.getConfig().getString("Config."+ spawnpoint +".SaveInventory");
 			    game = this.getConfig().getString("Config."+ spawnpoint +".GamemodeCreative");
 			    
 			    //Fehler berprfung
 			    if(locX == 0) {
 			    	p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache11);
 			    	return false;
 			    }
 			    if(game.length() > 5) {
 					p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "', "+ sprache14);
 					return false;
 			    }
 			    if(game.length() < 4) {
 					p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "', "+ sprache14);
 					return false;
 			    }
 			    if(saved.length() > 5) {
 			    	p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "', "+ sprache15);
 			    	return false;
 			    }
 			    if(saved.length() < 4) {
 			    	p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnpoint + "', "+ sprache15);
 			    	return false;
 			    }
 			    
 			    //hauptquellcode
 			    oldLocationList.put(p, p.getLocation());
 			    Location loc = new Location(getServer().getWorld(p.getWorld().getName()),locX, locY, locZ);
 			    p.teleport(loc);
 			    p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache1);
 				
 				if(game.length() == 4) {
 					p.setGameMode(GameMode.CREATIVE);
 				}
 				if(saved.length() == 4) {  
 					return new InventoryManager(cmd, args, p).saveToFile(p);
 				}
 				return true;
 			}else{
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache13);
 				return false;
 			}
 		}	
 		
 		//Spawnpoint von der Welt setzen
 		//Defining the spawn point for the creative world
 		if(cmd.getName().equalsIgnoreCase("setspawnpoint")) {
 			if(args.length < 3) {
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 				return false;
 			}
 			if(args.length > 3) {
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
 				return false;
 			}
 			if(args.length == 3) {
 				spawnName = (args[0]);
 				save = (args[1]);
 				game = (args[2]);
 				
 				getConfig().set("Config."+ spawnName +".spawn.X", p.getLocation().getX());
                 getConfig().set("Config."+ spawnName +".spawn.Y", p.getLocation().getY());
             	getConfig().set("Config."+ spawnName +".spawn.Z", p.getLocation().getZ());
             	getConfig().get("Config."+ spawnName +".activateCommandLeave");
 				this.getConfig().set("Config."+ spawnName +".activateCommandLeave", true);
 				
             	if (save.equalsIgnoreCase("nosave")) {
 					getConfig().get("Config."+ spawnName +".SaveInventory");
 					this.getConfig().set("Config."+ spawnName +".SaveInventory", false);
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache3);
 					p.sendMessage(ChatColor.RED + "[WorldTp] Spawnpoint '" + spawnName + "', '" + save + "' " + sprache16 + " '" + game + "'");
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache4);					
 					this.getConfig().options().copyDefaults(true);
 					this.saveConfig();
 				}
             	if (save.equalsIgnoreCase("withsave")) {
 					getConfig().get("Config."+ spawnName +".SaveInventory");
 					this.getConfig().set("Config."+ spawnName +".SaveInventory", true);
 					
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
 				}
 				else{
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache12);
 					return false;
 				}
 			}else {
 				return false;
 			}
 		}	
 	
 		//Um hilfe von WorldTp zu bekommen
 		//Getting help from WorldTP
 		if(cmd.getName().equalsIgnoreCase("worldtp")) {
 			if(args.length == 0) {
 				p.sendMessage(ChatColor.RED + "/setspawnpoint: " + ChatColor.AQUA + sprache5);
 				p.sendMessage(ChatColor.RED + "/wt: " + ChatColor.AQUA + sprache6);
 				p.sendMessage(ChatColor.RED + "/worldtp: " + ChatColor.AQUA + sprache8);
 				p.sendMessage(ChatColor.RED + "/leave: " + ChatColor.AQUA + sprache7);
                 
 				return true;
 			}else {
 				return false;
             }
 		}
 		
 		//Gibt dein Inventar zurck
 		if(cmd.getName().equalsIgnoreCase("invback")) {
 			if(args.length == 0){
 				return new InventoryManager(cmd, args, p).loadFromFile(p);
 			}
 			return true;
 		}
 		
 		//Aus der Welt zum alten Standpunkt teleportieren
 		//Teleporting from the world to the old place
 		if(cmd.getName().equalsIgnoreCase("leave")) {
 			if(args.length < 1) {
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache9);
 				return false;
 			}
 			if(args.length > 1) {
 				p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache10);
 				return false;
 			}
 			if(args.length == 1) {
 				spawnName = (args[0]);
 				
 				String leave = this.getConfig().getString("Config."+ spawnName +".activateCommandLeave");
 				
 				if(leave.length() == 5) {
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache17);
 					return false;
 				}
 				
 				if(oldLocationList.containsKey(p)) {
 					
 					p.teleport(oldLocationList.get(p));
 					p.sendMessage(ChatColor.RED + "[WorldTp] " + sprache2);
 					GameMode gamemode = p.getGameMode();
 					if(gamemode == GameMode.CREATIVE) {
 					p.setGameMode(GameMode.SURVIVAL);
 					}
 					return new InventoryManager(cmd, args, p).loadFromFile(p);
 				}
 				return true;
 			}else {
 				return false;
 				}
 		}
 		return true;
 	
 	}
 	public HashMap<Player, Location> oldLocationList = new HashMap<Player, Location>();
 	
 	public void loadConfig(){
 		String path1 = "Config.language.Wt";
 		this.getConfig().addDefault(path1, "You have been teleported");
 		String path2 = "Config.language.Leave";
 		this.getConfig().addDefault(path2, "You have been teleported back to your old position");
 		String path3 = "Config.language.Setspawnpoint";
 		this.getConfig().addDefault(path3, "You have defined the spawnpoint");
 		String path4 = "Config.language.Setspawnpoint2";
 		this.getConfig().addDefault(path4, "Saved");
 		String path9 = "Config.language.NoArgument";
 		this.getConfig().addDefault(path9, "Zu wenig Argumente");
 		String path10 = "Config.language.TooManyArgument";
 		this.getConfig().addDefault(path10, "Zu viele Argumente");
 		String path11 = "Config.language.NoWarpPoint";
 		this.getConfig().addDefault(path11, "Warppoint exestiert nicht");
 		String path5 = "Config.language.Help.Setspawnpoint";
 		this.getConfig().addDefault(path5, "Sets the spawn point you reach with /wt");
 		String path6 = "Config.language.Help.Wt";
 		this.getConfig().addDefault(path6, "You go to the spawn point you set");
 		String path7 = "Config.language.Help.Leave";
 		this.getConfig().addDefault(path7, "You leave the creative world");
 		String path8 = "Config.language.Help.WorldTp";
 		this.getConfig().addDefault(path8, "Read the help for WorldTP");
 		String path12 = "Config.language.error";
 		this.getConfig().addDefault(path12, "Deine Eingaben sind nicht Korekt");
 		String path13 = "Config.language.ConfigError";
 		this.getConfig().addDefault(path13, "In der Config von WorldTp ist ein Fehler");
 		String path14 = "Config.language.GamemodeCreativeError";
 		this.getConfig().addDefault(path14, "hat in der Config bei GamemodeCreative einen Fehler");
 		String path15 = "Config.language.SaveInventoryError";
 		this.getConfig().addDefault(path15, "hat in der Config bei SaveInventory einen Fehler");
 		String path16 = "Config.language.Options";
 		this.getConfig().addDefault(path16, "im Gamemode");
 		String path17 = "Config.language.BlockLeave";
 		this.getConfig().addDefault(path17, "Leave ist fr diese Warp nicht zugelassen");
 		
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	
 	}
 	public static JavaPlugin getPlugin() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 		
 }
