 package com.modcrafting.pingmotd;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PingMotd extends JavaPlugin{
 	public final Logger log = Logger.getLogger("Minecraft");
 	public String maindir = "plugins/PingMOTD/";
 	public String newMsg;
 	public String defMsg;
 	public final PingMOTDServerListener serverlistener = new PingMOTDServerListener(this);
 	public void onDisable() {
 		log.log(Level.INFO, "[PingMOTD] disabled.");
 	}
 	protected void createDefaultConfiguration(String name) {
 		File actual = new File(getDataFolder(), name);
 		if (!actual.exists()) {
 
 			InputStream input =
 				this.getClass().getResourceAsStream("/" + name);
 			if (input != null) {
 				FileOutputStream output = null;
 
 				try {
 					output = new FileOutputStream(actual);
 					byte[] buf = new byte[8192];
 					int length = 0;
 					while ((length = input.read(buf)) > 0) {
 						output.write(buf, 0, length);
 					}
 
 					log.log(Level.INFO, getDescription().getName()
 							+ ": Default configuration file written: " + name);
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					try {
 						if (input != null)
 							input.close();
 					} catch (IOException e) {}
 
 					try {
 						if (output != null)
 							output.close();
 					} catch (IOException e) {}
 				}
 			}
 		}
 	}
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		new File(maindir).mkdir();
 		createDefaultConfiguration("config.yml");
 		loadCommands();
 		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.SERVER_LIST_PING, serverlistener, Priority.Highest, this);
 		log.log(Level.INFO,"[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 	private void loadCommands() {
 		getCommand("pingmotdset").setExecutor(new SetMOTD(this));
 		getCommand("pingmotdview").setExecutor(new ViewMOTD(this));
 		getCommand("pingmotd").setExecutor(new Version(this));
 		return;
 		
 	}
 	public String newMsg(){
 		YamlConfiguration Config = (YamlConfiguration) this.getConfig();
 		Random generator = new Random();
 		int randInt = Config.getInt("messagenum", 1);
 		int randomizer = generator.nextInt(randInt) + 1;
 		String intStr = Integer.toString(randomizer);
 		String newMsg = Config.getString("message."+ intStr, defMsg);
 		return newMsg;
 	}
 	public String defMsg(String str){
 		defMsg = str;
 		return defMsg;
 	}
 	
 }
