 package com.ftwinston.Killer.CraftBukkit;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.Plugin;
 
 // a holder for everything that dives into the versioned CraftBukkit code that will break with every minecraft update
 
 public abstract class CraftBukkitAccess
 {
 	public static CraftBukkitAccess createCorrectVersion(Plugin plugin)
 	{
         // Get full package string of CraftServer class, then extract the version name from that 
         // org.bukkit.craftbukkit.versionstring (or for pre-refactor, just org.bukkit.craftbukkit
 		String packageName = plugin.getServer().getClass().getPackage().getName();
         String version = packageName.substring(packageName.lastIndexOf('.') + 1);
         
        if ( version.equals("v1_4_7"))
         	return new v1_4_7(plugin);
         if ( version.equals("v1_4_6"))
         	return new v1_4_6(plugin);
         if ( version.equals("v1_4_5"))
         	return new v1_4_5(plugin);
         
         if ( version.equals("craftbukkit"))
         	plugin.getLogger().warning("Killer minecraft requires at least CraftBukkit 1.4.5-R1.0 to function. Sorry.");
         else
         	plugin.getLogger().warning("This version of Killer minecraft is not compatible with your server's version of CraftBukkit! (" + version + ") Please download a newer version of Killer minecraft.");
         return null;
 	}
 	
 	protected CraftBukkitAccess(Plugin plugin) { this.plugin = plugin; }
 	
 	protected Plugin plugin;
 
 	@SuppressWarnings("rawtypes")
 	protected HashMap regionfiles;
 	protected Field rafField;
 	
 	public abstract String getDefaultLevelName();
 	public abstract YamlConfiguration getBukkitConfiguration();
 	public abstract void saveBukkitConfiguration(YamlConfiguration configuration);
 	public abstract String getServerProperty(String name, String defaultVal);
 	public abstract void setServerProperty(String name, String value);	
 	public abstract void saveServerPropertiesFile();
 		
 	public abstract void sendForScoreboard(Player viewer, String name, boolean show);
 	public abstract void sendForScoreboard(Player viewer, Player other, boolean show);
 	public abstract void forceRespawn(final Player player);
 	
 	public abstract void bindRegionFiles();
 	public void unbindRegionFiles()
 	{
 		regionfiles = null;
 		rafField = null;
 	}
 	
 	public abstract boolean clearWorldReference(String worldName);
 	public abstract void forceUnloadWorld(World world);
 	
 	public abstract void accountForDefaultWorldDeletion(World newDefault);
 	
 	public abstract World createWorld(org.bukkit.WorldType type, Environment env, String name, long seed, ChunkGenerator generator, String generatorSettings, boolean generateStructures);
 	
 	public abstract Location findNearestNetherFortress(Location loc);
 	public abstract boolean createFlyingEnderEye(Player player, Location target);
 }
