 package net.nexisonline.spade;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import net.nexisonline.spade.chunkproviders.ChunkProviderDoublePerlin;
 import net.nexisonline.spade.chunkproviders.ChunkProviderFlatGrass;
 import net.nexisonline.spade.chunkproviders.ChunkProviderMountains;
 import net.nexisonline.spade.chunkproviders.ChunkProviderStock;
 import net.nexisonline.spade.chunkproviders.ChunkProviderSurrealIslands;
 import net.nexisonline.spade.chunkproviders.ChunkProviderWat;
 import net.nexisonline.spade.commands.SetWorldGenCommand;
 import net.nexisonline.spade.commands.TP2WorldCommand;
 
 import org.bukkit.ChunkProvider;
 import org.bukkit.World.Environment;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 /**
  * Sample plugin for Bukkit
  *
  * @author Dinnerbone
  */
 public class SpadePlugin extends JavaPlugin {
     private final SpadeWorldListener worldListener = new SpadeWorldListener(this);
 	private HashMap<String,SpadeChunkProvider> chunkProviders = new HashMap<String,SpadeChunkProvider>();
 
 	public double getChunkDistanceToSpawn(String worldName, int x, int z) {
 		try {
 			return getServer().getWorld(worldName).getSpawnLocation().toVector().distanceSquared(new Vector(x*16,0,z*16))/16d;
 		} catch(NullPointerException e) {
			return 0;
 		}
 	}
 	public double getBlockDistanceToSpawn(String worldName, int x, int y, int z) {
 		try {
 			return getServer().getWorld(worldName).getSpawnLocation().toVector().distanceSquared(new Vector(x,y,z));
 		} catch(NullPointerException e) {
			return 0;
 		}
 	}
     public void onEnable() {
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.WORLD_SAVE, worldListener, Event.Priority.Monitor, this);
         
         // Register our commands
         getCommand("setworldgen").setExecutor(new SetWorldGenCommand(this));
         getCommand("tpw").setExecutor(new TP2WorldCommand(this));
         
         registerChunkProviders();
 
         // Load World Settings
         worldListener.loadWorlds();
         
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         
         
     }
     private void registerChunkProviders() {
 		chunkProviders.put("stock", new ChunkProviderStock());
 		chunkProviders.put("flatgrass", new ChunkProviderFlatGrass());
 		chunkProviders.put("mountains", new ChunkProviderMountains());
 		chunkProviders.put("islands", new ChunkProviderSurrealIslands());
 		chunkProviders.put("wat", new ChunkProviderWat(this));
 		chunkProviders.put("doubleperlin", new ChunkProviderDoublePerlin(this));
 	}
 	public void onDisable() {
     }
     
 	public ConfigurationNode loadWorld(String worldName, String cmName, String cpName, ConfigurationNode node) {
 		SpadeChunkProvider cp = chunkProviders.get(cpName);
 		cp.setWorldName(cpName);
 		node=cp.configure(node);
 		getServer().createWorld(worldName, Environment.NORMAL, (new Random()).nextLong(), null, cp);
 		return node;
 	}
 
 }
