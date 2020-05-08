 package net.nexisonline.spade;
 
 import java.util.HashMap;
 
 import net.nexisonline.spade.chunkproviders.ChunkProviderDoublePerlin;
 import net.nexisonline.spade.chunkproviders.ChunkProviderFlatGrass;
 import net.nexisonline.spade.chunkproviders.ChunkProviderMountains;
 import net.nexisonline.spade.chunkproviders.ChunkProviderSurrealIslands;
 import net.nexisonline.spade.chunkproviders.ChunkProviderWat;
 import net.nexisonline.spade.commands.RegenCommand;
 import net.nexisonline.spade.commands.TP2WorldCommand;
 
 import org.bukkit.World.Environment;
 import org.bukkit.event.Event;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 /**
  * Sample plugin for Bukkit
  *
  * @author Dinnerbone
  */
 public class SpadePlugin extends JavaPlugin {
     private final SpadeWorldListener worldListener = new SpadeWorldListener(this);
 	private HashMap<String,SpadeChunkProvider> chunkProviders = new HashMap<String,SpadeChunkProvider>();
 	public HashMap<String, GenerationLimits> genLimits = new HashMap<String, GenerationLimits>();
 	private HashMap<String,SpadeChunkProvider> assignedProviders = new HashMap<String,SpadeChunkProvider>();
     public void onEnable() {
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.WORLD_SAVE, worldListener, Event.Priority.Monitor, this);
         
         // Register our commands
         //getCommand("setworldgen").setExecutor(new SetWorldGenCommand(this));
         getCommand("regen").setExecutor(new RegenCommand(this));
         getCommand("tpw").setExecutor(new TP2WorldCommand(this));
         getCommand("world").setExecutor(new TP2WorldCommand(this));
         
         registerChunkProviders();
 
         // Load World Settings
         worldListener.loadWorlds();
        worldListener.saveWorlds();
         
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
         
         
     }
     private void registerChunkProviders() {
 		chunkProviders.put("stock", null);
 		chunkProviders.put("flatgrass", new ChunkProviderFlatGrass(this));
 		chunkProviders.put("mountains", new ChunkProviderMountains(this));
 		chunkProviders.put("islands", new ChunkProviderSurrealIslands(this));
 		chunkProviders.put("wat", new ChunkProviderWat(this));
 		chunkProviders.put("doubleperlin", new ChunkProviderDoublePerlin(this));
 	}
 	public void onDisable() {
     }
     
 	public ConfigurationNode loadWorld(String worldName, long seed, String cmName, String cpName, ConfigurationNode node) {
 		SpadeChunkProvider cp = chunkProviders.get(cpName);
 		if(cp!=null) {
 			cp.worldName=worldName;
 	
 			if (node == null) {
 				node = Configuration.getEmptyNode();
 			}
 			cp.onLoad(worldName,seed,node);
 		}
 		assignedProviders.put(worldName, cp);
 		getServer().createWorld(worldName, Environment.NORMAL, seed, (ChunkGenerator)cp);
 		return node;
 	}
 	public int getChunkRadius(String worldName) {
 		GenerationLimits gl = genLimits.get(worldName.toLowerCase());
 		if(gl==null)
 			return Integer.MAX_VALUE;
 		
 		if(!gl.enabled)
 			return Integer.MAX_VALUE;
 		
 		return gl.distanceSquared;
 	}
 	
 	public SpadeChunkProvider getProviderFor(String worldName) {
 		return assignedProviders.get(worldName);
 	}
 	
 	public boolean getRound(String worldName) {
 		GenerationLimits gl = genLimits.get(worldName.toLowerCase());
 		if(gl==null)
 			return true;
 		
 		if(!gl.enabled)
 			return true;
 		
 		return gl.round;
 	}
 	public boolean shouldGenerateChunk(String worldName, int x, int z) {
 		GenerationLimits gl = genLimits.get(worldName.toLowerCase());
 		if(gl==null)
 			return true;
 		
 		if(!gl.enabled)
 			return true;
 		
 		if(gl.round) {
 			long d2 = Math.round(Math.pow(x,2)+Math.pow(z,2));
 			return(d2 < gl.distanceSquared);
 		} else {
 			return (x < gl.distance || x < -gl.distance || z < gl.distance || z < -gl.distance);
 		}
 	}
 	public String getNameForClass(SpadeChunkProvider cp) {
 		for(String key:chunkProviders.keySet()) {
 			if(chunkProviders.get(key).getClass().equals(cp.getClass())) {
 				return key;
 			}
 		}
 		return "stock";
 	}
 
 }
