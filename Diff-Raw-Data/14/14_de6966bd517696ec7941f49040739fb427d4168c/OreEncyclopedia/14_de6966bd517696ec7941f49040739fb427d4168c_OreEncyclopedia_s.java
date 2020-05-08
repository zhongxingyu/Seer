 package k2b6s9j.OreEncyclopedia;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import k2b6s9j.OreEncyclopedia.command.OECommand;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 
 @Mod(modid = "OreEncyclopedia", name = "OreEncylopedia", version = "1.0")
 public class OreEncyclopedia {
 	
 	//Logger
 	public static final Logger oeLog = Logger.getLogger("OreEncyclopedia");
 	
 	//Configuration Booleans
 	public static Boolean logInfo;
 	public static Boolean logEntries;
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
         try
         {
         	logInfo = cfg.get("Features", "logInfo", true, "Should post mod name, copyright, and website in log.").getBoolean(true);
         	logEntries = cfg.get("Features", "logEntries", true, "Should post all of the loaded OreDictionary entries in the log").getBoolean(true);
         }
         catch (Exception e)
         {
             FMLLog.log(Level.SEVERE, e, "OreEncyclopedia had a problem loading it's configuration");
         }
         finally
         {
             if (cfg.hasChanged())
                 cfg.save();
         }
 	}
 
 	@EventHandler
 	public void Init(FMLInitializationEvent event) {
 		if (logInfo) {
 			oeLog.info("OreEncyclopedia");
 			oeLog.info("Copyright Kepler Sticka-Jones 2013");
 			oeLog.info("http://k2b6s9j.com/projects/minecraft/oreencyclopedia");
 		}
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 		if (logEntries) {
			for (String entry : OreDictionary.getOreNames()) {
				oeLog.info("Found an OreDictionary entry titled " + entry);
				for (ItemStack item : OreDictionary.getOres(entry)) {
					oeLog.info(entry + " contains " + item.getDisplayName() + " (" + item.toString() + ")");
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void serverLoad(FMLServerStartingEvent event)
 	{
 		event.registerServerCommand(new OECommand());
 	}
 }
