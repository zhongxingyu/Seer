 package com.turt2live.antishare.worldedit;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.ASUtils;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.enums.RegionKeyType;
 
 public class ASRegionHandler {
 
 	private AntiShare plugin;
 	private boolean hasWorldEdit = false;
 	private ASWorldEdit worldedit;
 	private HashMap<String, ASRegionPlayer> player_information = new HashMap<String, ASRegionPlayer>();
 
 	public ASRegionHandler(AntiShare plugin){
 		this.plugin = plugin;
 		if(plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null){
 			hasWorldEdit = true;
 			worldedit = new ASWorldEdit(plugin);
 		}else{
 			AntiShare.log.warning("[" + plugin.getDescription().getFullName() + "] WorldEdit is not installed!");
 		}
 		load();
 	}
 
 	public void newRegion(CommandSender sender, String gamemodeName, String name){
 		if(!hasWorldEdit){
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "WorldEdit is not installed. No region set.");
 			return;
 		}
 		GameMode gamemode;
 		if(gamemodeName.equalsIgnoreCase("creative") || gamemodeName.equalsIgnoreCase("c") || gamemodeName.equalsIgnoreCase("1")){
 			gamemode = GameMode.CREATIVE;
 		}else if(gamemodeName.equalsIgnoreCase("survival") || gamemodeName.equalsIgnoreCase("s") || gamemodeName.equalsIgnoreCase("0")){
 			gamemode = GameMode.SURVIVAL;
 		}else{
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "I don't know what Game Mode '" + gamemodeName + "' is!");
 			return;
 		}
 		if(!(sender instanceof Player)){
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You are not a player, sorry!");
 			return;
 		}
 		if(worldedit.regionExistsInSelection((Player) sender)){
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "There is a region where you have selected!");
 			return;
 		}
 		if(worldedit.regionNameExists(name)){
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "That region name already exists!");
 			return;
 		}
 		worldedit.newRegion((Player) sender, gamemode, name);
 		ASUtils.sendToPlayer(sender, ChatColor.GREEN + "Region '" + name + "' added.");
 	}
 
 	public void removeRegion(Location location, Player sender){
 		if(isRegion(location)){
 			worldedit.removeRegionAtLocation(location);
 			if(sender != null){
 				ASUtils.sendToPlayer(sender, ChatColor.GREEN + "Region removed.");
 			}
 		}else{
 			if(sender != null){
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "You are not in a GameMode region.");
 			}
 		}
 	}
 
 	public void removeRegion(String name, CommandSender sender){
 		if(!regionNameExists(name)){
 			ASUtils.sendToPlayer(sender, ChatColor.RED + "Region '" + name + "' does not exist.");
 			return;
 		}
 		worldedit.removeRegionByName(name);
 		ASUtils.sendToPlayer(sender, ChatColor.GREEN + "Region removed.");
 	}
 
 	public ASRegion getRegion(Location location){
 		return plugin.storage.getRegion(location);
 	}
 
 	public boolean isRegion(Location location){
 		return plugin.storage.getRegion(location) != null;
 	}
 
 	public boolean regionNameExists(String name){
 		return plugin.storage.getRegionByName(name) != null;
 	}
 
 	public ASRegion getRegionByName(String name){
 		return plugin.storage.getRegionByName(name);
 	}
 
 	public ASRegion getRegionByID(String id){
 		return plugin.storage.getRegionByID(id);
 	}
 
 	public void editRegion(ASRegion region, RegionKeyType key, String value, CommandSender sender){
 		boolean changed = false;
 		switch (key){
 		case NAME:
 			if(regionNameExists(value)){
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "Region name '" + value + "' already exists!");
 			}else{
 				region.setName(value);
 				changed = true;
 			}
 			break;
 		case ENTER_MESSAGE_SHOW:
 			if(ASUtils.getValueOf(value) != null){
 				region.setMessageOptions(ASUtils.getValueOf(value), region.isExitMessageActive());
 				changed = true;
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "Value '" + value + "' is unknown, did you mean 'true' or 'false'?");
 			}
 			break;
 		case EXIT_MESSAGE_SHOW:
 			if(ASUtils.getValueOf(value) != null){
 				region.setMessageOptions(region.isEnterMessageActive(), ASUtils.getValueOf(value));
 				changed = true;
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "Value '" + value + "' is unknown, did you mean 'true' or 'false'?");
 			}
 			break;
 		case INVENTORY:
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "Unsupported"); // TODO
 			break;
 		case SELECTION_AREA:
 			ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "Unsupported"); // TODO
 			break;
 		case GAMEMODE:
 			if(value.equalsIgnoreCase("creative") || value.equalsIgnoreCase("c") || value.equalsIgnoreCase("1")){
 				region.setGameMode(GameMode.CREATIVE);
 				changed = true;
 			}else if(value.equalsIgnoreCase("survival") || value.equalsIgnoreCase("s") || value.equalsIgnoreCase("0")){
 				region.setGameMode(GameMode.SURVIVAL);
 				changed = true;
 			}else{
 				ASUtils.sendToPlayer(sender, ChatColor.RED + "I don't know what Game Mode '" + value + "' is!");
 			}
 			break;
 		}
 		if(changed){
 			ASUtils.sendToPlayer(sender, ChatColor.GREEN + "Region saved.");
 		}
 	}
 
 	public void checkRegion(Player player, Location newLocation, Location fromLocation){
 		ASRegion region = plugin.getRegionHandler().getRegion(newLocation);
 		ASRegionPlayer asPlayer = player_information.get(player.getName());
 		if(asPlayer == null){
 			asPlayer = new ASRegionPlayer(player.getName());
 		}
 		if(region != null){
 			if(!player.getGameMode().equals(region.getGameModeSwitch())
 					&& !player.hasPermission("AntiShare.roam")){
 				player.setGameMode(region.getGameModeSwitch());
 				asPlayer.setLastGameMode(player.getGameMode());
 			}
 			if(asPlayer.getLastRegion() != null){
 				if(!asPlayer.getLastRegion().equals(region)){
 					region.alertEntry(player);
 				}
 			}else{
 				region.alertEntry(player);
 			}
 			asPlayer.setLastRegion(region);
 		}else{ // Left region/is out of region
 			if(asPlayer.getLastRegion() != null){
				if(!asPlayer.getLastGameMode().equals(player.getGameMode())){
 					player.setGameMode(asPlayer.getLastGameMode());
 					asPlayer.setLastGameMode(player.getGameMode());
 				}
 				asPlayer.getLastRegion().alertExit(player);
 				asPlayer.setLastRegion(null);
 			}
 		}
 		if(player_information.containsKey(player.getName())){
 			player_information.remove(player.getName());
 		}
 		player_information.put(player.getName(), asPlayer);
 	}
 
 	public void saveStatusToDisk(){
 		File saveFile = new File(plugin.getDataFolder(), "region_saves.yml");
 		if(saveFile.exists()){
 			saveFile.delete();
 			try{
 				saveFile.createNewFile();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		EnhancedConfiguration listing = new EnhancedConfiguration(saveFile, plugin);
 		listing.load();
 		for(String player : player_information.keySet()){
 			ASRegionPlayer asPlayer = player_information.get(player);
 			listing.set(player + ".gamemode", asPlayer.getLastGameMode().name());
 			listing.set(player + ".region", (asPlayer.getLastRegion() != null) ? asPlayer.getLastRegion().getUniqueID() : "none");
 			listing.save();
 		}
 	}
 
 	public void load(){
 		File saveFile = new File(plugin.getDataFolder(), "region_saves.yml");
 		if(!saveFile.exists()){
 			return;
 		}
 		EnhancedConfiguration listing = new EnhancedConfiguration(saveFile, plugin);
 		listing.load();
 		Set<String> section = listing.getConfigurationSection("").getKeys(false);
 		for(String path : section){
 			String playerName = path;
 			GameMode gamemode = GameMode.valueOf(listing.getString(path + ".gamemode"));
 			ASRegion region = null;
 			if(!listing.getString(path + ".region").equalsIgnoreCase("none")){
 				region = getRegionByID(listing.getString(path + ".region"));
 			}
 			ASRegionPlayer asPlayer = new ASRegionPlayer(playerName);
 			asPlayer.setLastGameMode(gamemode);
 			asPlayer.setLastRegion(region);
 			player_information.put(playerName, asPlayer);
 		}
 	}
 
 	public AntiShare getPlugin(){
 		return plugin;
 	}
 
 	public ASWorldEdit getWorldEditHandler(){
 		return worldedit;
 	}
 
 }
