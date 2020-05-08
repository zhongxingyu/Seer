 package com.xpansive.bukkit.expansiveterrain;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ExpansiveTerrain extends JavaPlugin {
     private final String WORLD_NAME = "ExpansiveTerrain";
     private World genWorld = null;
    private static ConfigManager config = new ConfigManager();
 
     public void onDisable() {
     }
 
     public void onEnable() {
         PluginDescriptionFile desc = this.getDescription();
         System.out.println(desc.getName() + " version " + desc.getVersion() + " is enabled!");
         getCommand("expansive").setExecutor(new ExpansiveTerrainCommandExec(this));
     }
 
     public boolean anonymousCheck(CommandSender sender) {
         return sender instanceof Player;
     }
 
     public World getExpansiveTerrainWorld() {
         // Thanks to echurchill on github for pointing this out
         if (genWorld == null) {
             genWorld = Bukkit.getServer().getWorld(WORLD_NAME);
             if (genWorld == null) {
                 WorldCreator worldcreator = new WorldCreator(WORLD_NAME);
                 worldcreator.environment(World.Environment.NORMAL);
                 worldcreator.generator(new ExpansiveTerrainChunkGenerator());
                 genWorld = Bukkit.getServer().createWorld(worldcreator);
             }
         }
         return genWorld;
     }
     
     public static ConfigManager getConfigManager() {
         return config;
     }
 
     @Override
     public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
         config.loadConfig(this, worldName);
         return new ExpansiveTerrainChunkGenerator();
     }
 }
