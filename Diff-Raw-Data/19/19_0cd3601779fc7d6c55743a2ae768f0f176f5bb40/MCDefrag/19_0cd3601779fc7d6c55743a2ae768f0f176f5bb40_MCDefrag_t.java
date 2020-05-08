 package de.javakara.manf.mcdefrag;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.javakara.manf.mcdefrag.listener.PlayerListener;
 import de.javakara.manf.mcdefrag.api.ShedulerFactory;
 import de.javakara.manf.mcdefrag.api.manager.RegionManager;
 import de.javakara.manf.util.ChatTable;
 import de.javakara.manf.util.LanguageComplete;
 import de.javakara.manf.util.SpacerReplace;
 
 @LanguageComplete
 public class MCDefrag extends JavaPlugin{
 	private DefragCommands DefragCMD;
 	public static boolean METRICS_ENABLED = true;
 	
 	@Override
 	public void onEnable(){
 		Language.init(getDataFolder());
 		try {
 			Config.initialise(getConfig(), getDataFolder());
 			Config.load();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		ShedulerFactory.initialize(this);
 		RegionManager.initialise(getDataFolder());
		ChatTable.initColors(Config.getString("default-color"), Config.getStringList("table.colors"));
 		PlayerListener.addWorld(Config.getStringList("worlds"));
 		registerCommands();
 		
 		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
 			
 		if(METRICS_ENABLED){
 			try {
 			    Metrics metrics = new Metrics(this);
 			    metrics.start();
 			} catch (IOException e) {
 			    // Failed to submit the stats :-(
 				System.out.println("Metrics failed to submit stats! D:");
 				System.out.println("Execption: " + e);
 			}
 		}
 	}
 	
 	@Override
 	public void onDisable(){ 
 		ShedulerFactory.finish();
 		try {
 			RegionManager.save();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void registerCommands() {
 		DefragCMD = new DefragCommands();
 		getCommand("defrag").setExecutor(DefragCMD);
 	}
 	
 	public static final String[] getArgumentString(String node,SpacerReplace sr){
 		String msg = Language.getRaw(node);
 		return sr.repl(msg).split("<n>");
 	}
 }
