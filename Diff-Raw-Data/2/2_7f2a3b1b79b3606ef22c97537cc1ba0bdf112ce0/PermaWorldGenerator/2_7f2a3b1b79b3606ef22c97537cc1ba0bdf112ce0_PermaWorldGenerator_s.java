 package com.djdch.bukkit.permaworldgenerator;
 
 import java.util.HashMap;
 
 import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.generator.ChunkGenerator;
 
 import com.djdch.bukkit.permaworldgenerator.configuration.WorldConfiguration;
 import com.djdch.bukkit.permaworldgenerator.generator.ChunkProviderGenerate;
 import com.djdch.bukkit.permaworldgenerator.listener.WorldListener;
 import com.djdch.bukkit.permaworldgenerator.mc100.WorldChunkManager;
 import com.djdch.bukkit.util.Logger;
 
 /**
  * Main class of the <b>PermaWorldGenerator</b> plugin for Bukkit.
  * <p>
 * Permanent world generator plugin for Bukkit.
  * 
  * @author DjDCH
  */
 public class PermaWorldGenerator extends JavaPlugin {
     /**
      * Contains the Logger instance.
      */
     protected final Logger logger = new Logger();
 
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
         this.logger.setName(getDescription().getName());
 
         // Register the plugin events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.WORLD_INIT, this.worldListener, Event.Priority.High, this);
 
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
             workWorld.worldProvider.b = chunkManager;
             worldSetting.getChunkProvider().Init(workWorld, chunkManager, workWorld.getSeed(), worldSetting.getLevelStructures());
             worldSetting.setInit(true);
 
             this.logger.info("World '" + world.getName() + "' init");
         }
     }
 
     /**
      * Accessor who return the logger instance.
      * 
      * @return Logger instance.
      */
     public Logger getLogger() {
         return this.logger;
     }
 }
