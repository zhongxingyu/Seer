 package com.djdch.bukkit.onehundredgenerator;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.generator.ChunkGenerator;
 
 import com.djdch.bukkit.onehundredgenerator.configuration.WorldConfiguration;
 import com.djdch.bukkit.onehundredgenerator.generator.ChunkProviderGenerate;
 import com.djdch.bukkit.onehundredgenerator.listener.WorldListener;
 import com.djdch.bukkit.onehundredgenerator.mc100.WorldChunkManager;
 
 /**
  * Main class of the <b>OneHundredGenerator</b> plugin for Bukkit.
  * <p>
  * Minecraft 1.0.0 world generator.
  * 
  * @author DjDCH
  */
 public class OneHundredGenerator extends JavaPlugin {
     /**
      * Contains the Logger instance.
      */
    protected Logger logger = this.getLogger();
 
     /**
      * Contains the deathListener instance.
      */
     protected final WorldListener worldListener = new WorldListener(this);
 
     /**
 	 *
 	 */
     protected final HashMap<String, WorldConfiguration> worldsSettings = new HashMap<String, WorldConfiguration>();
 
     /**
      * Method execute when the plugin is enable.
      */
     public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this.worldListener, this);
 
        this.logger.info("Version " + getDescription().getVersion() + " enable");
     }
 
     /**
      * Method execute when the plugin is disable.
      */
     public void onDisable() {
        this.logger.info("Version " + getDescription().getVersion() + " disable");
     }
 
     /**
 	 *
 	 */
     public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
         if (this.worldsSettings.containsKey(worldName)) {
             this.logger.info("World '" + worldName + "' enable");
             return ((WorldConfiguration) this.worldsSettings.get(worldName)).getChunkProvider();
         }
 
         WorldConfiguration worldSetting = new WorldConfiguration(this);
 
         if (id != null) {
             if (id.contains("nostructures")) {
                 worldSetting.setLevelStructures(false);
             }
         }
 
         this.worldsSettings.put(worldName, worldSetting);
 
         ChunkProviderGenerate prov = new ChunkProviderGenerate(worldSetting);
 
         this.logger.info("World '" + worldName + "' enable");
         return prov;
     }
 
     /**
      * 
      * @param world
      */
     public void WorldInit(World world) {
         if (this.worldsSettings.containsKey(world.getName())) {
             WorldConfiguration worldSetting = (WorldConfiguration) this.worldsSettings.get(world.getName());
             if (worldSetting.isInit()) {
                 return;
             }
             net.minecraft.server.World workWorld = ((CraftWorld) world).getHandle();
 
             WorldChunkManager chunkManager = new WorldChunkManager(workWorld);
             workWorld.worldProvider.c = chunkManager;
             worldSetting.getChunkProvider().Init(workWorld, chunkManager, workWorld.getSeed(), worldSetting.getLevelStructures());
             worldSetting.setInit(true);
 
             this.logger.info("World '" + world.getName() + "' init");
         }
     }
 }
