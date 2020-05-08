 package com.turt2live.antishare;
 
 import java.io.File;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 
 public class Configuration {
 
 	private AntiShare plugin;
 
 	public Configuration(AntiShare plugin){
 		this.plugin = plugin;
 	}
 
 	public void create(){
 		plugin.getConfig().loadDefaults(plugin.getResource("resources/config.yml"));
 		if(!plugin.getConfig().fileExists() || !plugin.getConfig().checkDefaults()){
 			plugin.getConfig().saveDefaults();
 		}
		plugin.getConfig().clearDefaults();
 		load();
 		// Check the events for spaces (3.1.0)
 		plugin.getConfig().set("events.block_break", (!plugin.getConfig().getString("events.block_break").contains(",") ? plugin.getConfig().getString("events.block_break").replaceAll(" ", ",") : plugin.getConfig().getString("events.block_break")));
 		plugin.getConfig().set("events.block_place", (!plugin.getConfig().getString("events.block_place").contains(",") ? plugin.getConfig().getString("events.block_place").replaceAll(" ", ",") : plugin.getConfig().getString("events.block_place")));
 		plugin.getConfig().set("events.death", (!plugin.getConfig().getString("events.death").contains(",") ? plugin.getConfig().getString("events.death").replaceAll(" ", ",") : plugin.getConfig().getString("events.death")));
 		plugin.getConfig().set("events.drop_item", (!plugin.getConfig().getString("events.drop_item").contains(",") ? plugin.getConfig().getString("events.drop_item").replaceAll(" ", ",") : plugin.getConfig().getString("events.drop_item")));
 		plugin.getConfig().set("events.interact", (!plugin.getConfig().getString("events.interact").contains(",") ? plugin.getConfig().getString("events.interact").replaceAll(" ", ",") : plugin.getConfig().getString("events.interact")));
 		plugin.getConfig().set("events.commands", (!plugin.getConfig().getString("events.commands").contains(",") ? plugin.getConfig().getString("events.commands").replaceAll(" ", ",") : plugin.getConfig().getString("events.commands")));
 		plugin.getConfig().set("other.tracked-blocks", (!plugin.getConfig().getString("other.tracked-blocks").contains(",") ? plugin.getConfig().getString("other.tracked-blocks").replaceAll(" ", ",") : plugin.getConfig().getString("other.tracked-blocks")));
 		save();
 		load();
 	}
 
 	public boolean onlyIfCreative(Player player){
 		if(plugin.getPermissions().has(player, "AntiShare.onlyIfCreative.global", player.getWorld())){
 			return getBoolean("other.only_if_creative", player.getWorld());
 		}else if(plugin.getPermissions().has(player, "AntiShare.onlyIfCreative", player.getWorld())){
 			return true;
 		}
 		return false;
 	}
 
 	public Object get(String path, World world){
 		File worldConfig = new File(plugin.getDataFolder(), world.getName() + "_config.yml");
 		EnhancedConfiguration config = new EnhancedConfiguration(worldConfig, plugin);
 		Object value = null;
 		if(!config.getString(path).equalsIgnoreCase("global")){
 			value = config.get(path);
 		}else{
 			value = plugin.getConfig().get(path);
 		}
 		return value;
 	}
 
 	public boolean getBoolean(String path, World world){
 		File worldConfig = new File(plugin.getDataFolder(), world.getName() + "_config.yml");
 		EnhancedConfiguration config = new EnhancedConfiguration(worldConfig, plugin);
 		boolean value = false;
 		if(!config.getString(path).equalsIgnoreCase("global")){
 			value = config.getBoolean(path);
 		}else{
 			value = plugin.getConfig().getBoolean(path);
 		}
 		return value;
 	}
 
 	public String getString(String path, World world){
 		File worldConfig = new File(plugin.getDataFolder(), world.getName() + "_config.yml");
 		EnhancedConfiguration config = new EnhancedConfiguration(worldConfig, plugin);
 		String value = null;
 		if(!config.getString(path).equalsIgnoreCase("global")){
 			value = config.getString(path);
 		}else{
 			value = plugin.getConfig().getString(path);
 		}
 		return value;
 	}
 
 	public void reload(){
 		load();
 	}
 
 	public void save(){
 		plugin.saveConfig();
 	}
 
 	public void load(){
 		plugin.reloadConfig();
 	}
 }
