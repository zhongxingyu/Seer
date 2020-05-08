 package us.aaronweiss.weisscraft;
 
 import java.util.logging.Logger;
 import org.bukkit.plugin.java.JavaPlugin;
 import us.aaronweiss.weisscraft.constants.ModConstants;
 
 /**
  * The main Bukkit plugin for WeissCraft.
  * @author Aaron Weiss
  * @version 1.0
  */
 public class WeissCraft extends JavaPlugin {
 	public final Logger logger = Logger.getLogger("Minecraft");
  
 	@Override
 	public void onEnable(){
 		if (ModConstants.WORLD_MOD_PREVENTION) {
 			this.getServer().getPluginManager().registerEvents(new WorldModPreventionListener(), this);
 			this.logger.info("[" + this.getDescription().getName() + "] World Mod Prevention enabled.");
 		}
 		if (ModConstants.MUTANT_MOD) {
 			MutantModListener mm = new MutantModListener();
 			this.getServer().addRecipe(mm.getMutantRecipe());
 			this.getServer().getPluginManager().registerEvents(mm, this);
 			this.logger.info("[" + this.getDescription().getName() + "] Mutant Mod enabled.");
 		}
		if (ModConstants.SMART_COMPASS) {
			this.getServer().getPluginManager().registerEvents(new SmartCompassListener(), this);
			this.logger.info("[" + this.getDescription().getName() + "] Smart Compass enabled.");
		}
 		this.logger.info("[" + this.getDescription().getName() + "] WeissCraft (" + this.getDescription().getVersion() + ") loaded.");
 	}
 	
 	@Override
 	public void onDisable(){
 		if (ModConstants.WORLD_MOD_PREVENTION) {
 			this.logger.info("[" + this.getDescription().getName() + "] World Mod Prevention disabled.");
 		}
		if (ModConstants.MUTANT_MOD) {
			this.logger.info("[" + this.getDescription().getName() + "] Mutant Mod disabled.");
		}
 		if (ModConstants.SMART_COMPASS) {
 			this.logger.info("[" + this.getDescription().getName() + "] Smart Compass disabled.");
 		}
 	}
 }
