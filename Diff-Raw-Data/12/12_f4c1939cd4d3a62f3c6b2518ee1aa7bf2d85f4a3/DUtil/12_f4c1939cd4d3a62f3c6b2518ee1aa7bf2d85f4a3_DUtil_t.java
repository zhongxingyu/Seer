 package com.legit2.Demigods.Utilities;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.BlockIterator;
 
 import com.legit2.Demigods.DConfig;
 import com.legit2.Demigods.Demigods;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class DUtil
 {
 	public static Demigods plugin;
 	
 	// Define variables
 	private static FileConfiguration customConfig = null;
 	private static File customConfigFile = null;
 	private static String plugin_name = "Demigods";
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	public DUtil(Demigods instance)
 	{
 		plugin = instance;
 	}
 
 	/*
 	 *  getPlugin() : Returns an instance of the plugin.
 	 */
 	public static Demigods getPlugin()
 	{
 		return plugin;
 	}
 	
 	/*
 	 *  getImmortalList() : Gets list of currently immortal players.
 	 */
 	public static ArrayList<Integer> getImmortalList()
 	{		
 		// Define variables
 		ArrayList<Integer> immortalList = new ArrayList<Integer>();
 		HashMap<Integer, HashMap<String, Object>> characters = DDataUtil.getAllChars();
 		
 		for(Entry<Integer, HashMap<String, Object>> character : characters.entrySet())
 		{
 			int charID = character.getKey();
 			HashMap<String, Object> data = character.getValue();
 			
 			if(data.get("char_immortal") != null && DObjUtil.toBoolean(data.get("char_immortal"))) immortalList.add(charID);
 		}
 		
 		return immortalList;
 	}
 
 	/*
 	 *  getOnlinePlayers() : Returns a string array of all online players.
 	 */
 	public static Player[] getOnlinePlayers()
 	{
 		return Bukkit.getOnlinePlayers();
 	}
 	
 	/*
 	 *  areAllied() : Returns true if (String)player is allied with (String)otherPlayer.
 	 */
 	public static boolean areAllied(Player player1, Player player2)
 	{
 		String playerAlliance = DPlayerUtil.getCurrentAlliance(player1);
 		String otherPlayerAlliance = DPlayerUtil.getCurrentAlliance(player2);
 		
 		if(playerAlliance.equals(otherPlayerAlliance)) return true;
 		else return false;
 	}
 	
 	/*
 	 *  customDamage() : Creates custom damage for (LivingEntity)target from (LivingEntity)source with ammount (int)amount.
 	 */
 	public static void customDamage(LivingEntity source, LivingEntity target, int amount, DamageCause cause)
 	{
 		if(target instanceof Player)
 		{
 			if(source instanceof Player)
 			{
 				target.setLastDamageCause(new EntityDamageByEntityEvent(source, target, cause, amount));
 			}
 			else target.damage(amount);
 		}
 		else target.damage(amount);
 	}
 	
 	/*
 	 *  taggedMessage() : Sends tagged message (String)msg to the (CommandSender)sender.
 	 */
 	public static void taggedMessage(CommandSender sender, String msg)
 	{
 		sender.sendMessage(ChatColor.YELLOW + "[" + plugin_name + "]");
 	}
 	
 	/*
 	 *  taggedMessage() : Sends tagged message (String)msg to the (CommandSender)sender.
 	 */
 	public static void customTaggedMessage(CommandSender sender, String title, String msg)
 	{
 		if(msg != null) sender.sendMessage(ChatColor.YELLOW + "[" + plugin_name + "] " + ChatColor.RESET + msg);
 		sender.sendMessage(ChatColor.YELLOW + "[" + title + "]");
 	}
 	
 	/*
 	 *  info() : Sends console message with "info" tag.
 	 */
 	public static void info(String msg)
 	{
 		log.info("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  warning() : Sends console message with "warning" tag.
 	 */
 	public static void warning(String msg)
 	{
 		log.warning("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  severe() : Sends console message with "severe" tag.
 	 */
 	public static void severe(String msg)
 	{
 		log.severe("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  serverMsg() : Send (String)msg to the server chat.
 	 */
 	public static void serverMsg(String msg)
 	{
 		plugin.getServer().broadcastMessage(msg);
 	}
 
 	/*
 	 *  saveCustomConfig() : Saves the custom configuration (String)name to file system.
 	 */
 	public static void saveCustomConfig(String name)
 	{
 		if(customConfig == null || customConfigFile == null) return; 
 		
 		try
 		{
 			getCustomConfig(name).save(customConfigFile);
 		}
 		catch(IOException e)
 		{
 			severe("There was an error when saving file: " + name + ".yml");
 			severe("Error: " + e);
 		}
 	}
 
 	/*
 	 *  saveDefaultCustomConfig() : Saves the defaults for custom configuration (String)name to file system.
 	 */
 	public static void saveDefaultCustomConfig(String name)
 	{
 		customConfigFile = new File(plugin.getDataFolder(), name + ".yml");
 		if(!customConfigFile.exists())
 		{
 			plugin.saveResource(name + ".yml", false);
 		}
 	}
 	
 	/*
 	 *  getCustomConfig() : Grabs the custom configuration file from file system.
 	 */
 	public static FileConfiguration getCustomConfig(String name)
 	{
 		if(customConfig == null)
 		{
 			reloadCustomConfig(name);
 		}
 		return customConfig;
 	}
 	
 	/*
 	 *  reloadCustomConfig() : Reloads the custom configuration (String)name to refresh values.
 	 */
 	public static void reloadCustomConfig(String name)
 	{
 		if(customConfigFile == null)
 		{
 			customConfigFile = new File(plugin.getDataFolder(), name + ".yml");
 		}
 		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
 
 		// Look for defaults in the jar
 		InputStream defConfigStream = plugin.getResource(name + ".yml");
 		if(defConfigStream != null)
 		{
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			customConfig.setDefaults(defConfig);
 		}
 	}
 	
 	/*
 	 *  hasPermission() : Checks if (Player)player has permission (String)permission.
 	 */
 	public static boolean hasPermission(Player player, String permission)
 	{
 		if(player == null) return true;
 		return player.hasPermission(permission);
 	}
 	
 	/*
 	 *  hasPermissionOrOP() : Checks if (Player)player has permission (String)permission, or is OP.
 	 */
 	public static boolean hasPermissionOrOP(Player player, String permission)
 	{
 		if(player == null) return true;
 		if(player.isOp()) return true;
 		return player.hasPermission(permission);
 	}
 	
 	/*
 	 *  noPermission() : Command sender does not have permission to run command.
 	 */
 	public static boolean noPermission(Player player)
 	{
 		player.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
 		return true;
 	}
 	
 	/*
 	 *  noConsole() : Sends a permission denial message to the console.
 	 */
 	public static boolean noConsole(CommandSender sender)
 	{
 		sender.sendMessage("This command can only be executed by a player.");
 		return true;
 	}
 	
 	/*
 	 *  noPlayer() : Sends a permission denial message to the console.
 	 */
 	public static boolean noPlayer(CommandSender sender)
 	{
 		sender.sendMessage("This command can only be executed by the console.");
 		return true;
 	}
 	
 	/*
 	 *  canUseDeity() : Checks is a player can use a specfic deity and returns a message
 	 */
 	public static boolean canUseDeity(Player player, String deity)
 	{		
 		// Check the player for DEITYNAME
 		if(!DCharUtil.hasDeity(DPlayerUtil.getCurrentChar(player), deity))
 		{
 			player.sendMessage(ChatColor.RED + "You haven't claimed " + deity + "! You can't do that!");
 			return false;
 		}
 		else if(!DCharUtil.isImmortal(player))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that, mortal!");
 			return false;
 		}
 		return true;
 	}
 	
 	/*
 	 *  canUseDeitySilent() : Checks is a player can use a specfic deity without returning a message.
 	 */
 	public static boolean canUseDeitySilent(Player player, String deity)
 	{		
 		// Check the player for DEITYNAME
 		if(!DCharUtil.hasDeity(DPlayerUtil.getCurrentChar(player), deity)) return false;
 		else if(!DCharUtil.isImmortal(player)) return false;
 		else return true;
 	}
 	
 	/*
 	 *  canLocationPVP() : Checks if PVP is allowed in (Location)location.
 	 */
     public static boolean canLocationPVP(Location location)
     {
         if(DConfig.getSettingBoolean("allow_skills_anywhere")) return true;
         
         if(canWorldGuardPVP(location)) return true;
         else return false;
     }
     
 	/*
 	 *  canTarget() : Checks if PVP is allowed in (Location)location.
 	 */
     public static boolean canTarget(LivingEntity player, Location location)
     {      
     	if(!(player instanceof Player)) return true;
    	else if(DDataUtil.hasPlayerData((Player) player, "temp_was_PVP")) return true;
    	else return canLocationPVP(location);
     }
 	
     /*
      *  WORLDGUARD SUPPORT START
      */
     @SuppressWarnings("static-access")
     public static boolean canWorldGuardPVP(Location location)
     {
 	    if(DConfig.getSettingBoolean("allow_skills_anywhere")) return true;
 	    if(plugin.WORLDGUARD == null) return true;
 	    
 	    ApplicableRegionSet set = plugin.WORLDGUARD.getRegionManager(location.getWorld()).getApplicableRegions(location);
 	    for (ProtectedRegion region : set)
 		{
 	    	if(region.getId().toLowerCase().contains("nopvp")) return false;
 		}
 	    return true;
     }
 
     @SuppressWarnings("static-access")
     public static boolean canWorldGuardBuild(Player player, Location location)
     {
         if(plugin.WORLDGUARD == null) return true;
         
         return plugin.WORLDGUARD.canBuild(player, location);
     }
 
     @SuppressWarnings("static-access")
     public static boolean canWorldGuardDamage(Location location)
     {
         if(plugin.WORLDGUARD == null) return true;
         
         ApplicableRegionSet set = plugin.WORLDGUARD.getRegionManager(location.getWorld()).getApplicableRegions(location);
         return !set.allows(DefaultFlag.INVINCIBILITY);
     }
             
     /*
      *  WORLDGUARD SUPPORT END
      */
     
     public static LivingEntity autoTarget(Player player)
     {
     	BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0, 100);
         
         while (iterator.hasNext())
         {
             Block item = iterator.next();
             for(Entity entity : player.getNearbyEntities(100, 100, 100))
             {
             	if(entity instanceof LivingEntity)
             	{
 	                int acc = 2;
 	                for(int x = -acc; x < acc; x++)
 	                {
 	                    for(int z = -acc; z < acc; z++)
 	                    {
 	                        for(int y = -acc; y < acc; y++)
 	                        {
 	                            if(entity.getLocation().getBlock().getRelative(x, y, z).equals(item))
 	                            {
 	                                return (LivingEntity) entity;
 	                            }
 	                        }
 	                    }
 	                }
             	}
             }
         }
         return null;
     }
 }
