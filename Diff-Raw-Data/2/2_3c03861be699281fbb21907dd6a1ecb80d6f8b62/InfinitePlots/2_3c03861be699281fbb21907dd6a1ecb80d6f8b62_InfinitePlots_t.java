 package uk.co.jacekk.bukkit.infiniteplots;
 
 import java.io.File;
 
 import org.bukkit.generator.ChunkGenerator;
 
 import uk.co.jacekk.bukkit.baseplugin.BasePlugin;
 import uk.co.jacekk.bukkit.baseplugin.config.PluginConfig;
 
 public class InfinitePlots extends BasePlugin {
 	
 	public void onEnable(){
		super.onEnable(true);
		
 		this.config = new PluginConfig(new File(this.baseDirPath + File.separator + "config.yml"), Config.values(), this.log);
 		
 		if (this.config.getBoolean(Config.PLOTS_RESTRICT_SPAWNING)){
 			this.pluginManager.registerEvents(new RestrictSpawningListener(this), this);
 		}
 		
 		this.pluginManager.registerEvents(new WorldInitListener(this), this);
 	}
 	
 	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id){
 		int size = (Integer) ((id != null && id.matches("[-+]?\\d+(\\.\\d+)?")) ? Integer.parseInt(id) : this.config.getInt(Config.PLOTS_SIZE));
 		int height = this.config.getInt(Config.PLOTS_HEIGHT);
 		
 		byte baseId = (byte) this.config.getInt(Config.BLOCKS_BASE);
 		byte surfaceId = (byte) this.config.getInt(Config.BLOCKS_SURFACE);
 		byte pathId = (byte) this.config.getInt(Config.BLOCKS_PATH);
 		byte wallLowerId = (byte) this.config.getInt(Config.BLOCKS_LOWER_WALL);
 		byte wallUpperId = (byte) this.config.getInt(Config.BLOCKS_UPPER_WALL);
 		
 		return new PlotsGenerator(this, size, height, baseId, surfaceId, pathId, wallLowerId, wallUpperId);
 	}
 	
 }
