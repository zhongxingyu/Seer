 //作者: tsuttsu305
 //テスト
 package tsuttsu305.Main;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin
 {
 	public static Main plugin;
 	
 
 	
 	Logger logger = Logger.getLogger("Minecraft");
	public String Msg;
	public boolean AllowOP;
 
 
 	public void onEnable()
 	{
 		PluginDescriptionFile pdfFile = getDescription();
 		this.logger.info(pdfFile.getName() + "version" + pdfFile.getVersion() + " is Enabled");
 		getServer().getPluginManager().registerEvents(new Event(), this);
 		
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		//BlockMsg Load
 		Msg = getConfig().getString("BlockMsg");
 		//AllowOP load
 		AllowOP = getConfig().getBoolean("AllowOP");
 		
 		
 		
 		
 	}
 
 	public void onDisable()
 	{
 		PluginDescriptionFile pdfFile = getDescription();
 		this.logger.info(pdfFile.getName() + "version" + pdfFile.getVersion() + " is Disabled");
 	}
 
 	
 }
