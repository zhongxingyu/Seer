package tk.morrigan.badb.TestPlugin;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TestPlugin extends JavaPlugin {
 	
 	public static TestPlugin plugin;
 	public transient static PluginDescriptionFile pdfFile;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public final TestPluginCommandExecutor commandExecutor = new TestPluginCommandExecutor(this);
 	public final ServerChatPlayerListener playerListener = new ServerChatPlayerListener(this);
 	
 	@Override
 	public void onEnable() {
 		//getCommand("slap").setExecutor(commandExecutor);
 		//getCommand("slay").setExecutor(commandExecutor);
 		//getCommand("ignite").setExecutor(commandExecutor);
 		//getCommand("extinguish").setExecutor(commandExecutor);
 		//getCommand("explode").setExecutor(commandExecutor);
 		//getCommand("inspect").setExecutor(commandExecutor);
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this.playerListener, this);
 		pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " " + pdfFile.getVersion() + " has been enabled!");
 	}
 	
 	@Override
 	public void onDisable() {
 		pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " has been disabled!");
 	}
 	
 }
