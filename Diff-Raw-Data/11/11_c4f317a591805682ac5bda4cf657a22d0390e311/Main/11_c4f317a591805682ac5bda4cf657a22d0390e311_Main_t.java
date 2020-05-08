 package net.dockter.sguide;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.dockter.sguide.guide.GuideManager;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
 	private static Main instance;
 	private FileConfiguration bypass;
 
 	public static Main getInstance() {
 		return instance;
 	}
 
 	@Override
 	public void onDisable() {
 		super.onDisable();
 		GuideManager.disable();
 	}
 
 	@Override
 	public void onEnable() {
 		super.onEnable();
 		instance = this;
 		FileConfiguration config = this.getConfig();
		config.addDefault("PromptTitle", "Info Guide");
 		config.addDefault("TitleX", 190);
 		config.addDefault("DefaultGuide", "Initial");
 		config.options().copyDefaults(true);
 		saveConfig();
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(new GuideListener(this), this);
 		File file = new File(this.getDataFolder() + File.separator + "users.yml");
 		if (!file.exists()) {
 			File init = new File(this.getDataFolder() + File.separator + "guides" + File.separator + "Initial.yml");
 			init.getParentFile().mkdirs();
 			try {
 				init.createNewFile();
 			} catch (IOException ex) {
 				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 			}
 			YamlConfiguration yconf = YamlConfiguration.loadConfiguration(init);
 			yconf.addDefault("Name", "Initial");
 			yconf.addDefault("Date", "Future");
			yconf.addDefault("Text", "Thank you for using Info Guide.");
 			yconf.options().copyDefaults(true);
 			try {
 				yconf.save(init);
 			} catch (IOException ex) {
 				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 			}
 			try {
 				file.createNewFile();
 			} catch (IOException ex) {
 				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 		bypass = YamlConfiguration.loadConfiguration(file);
 	}
 
 	public boolean isBypassing(String name) {
 		return bypass.contains(name) && bypass.getBoolean(name);
 	}
 
 	public boolean canBypass(String name) {
 		return bypass.contains(name);
 	}
 
 	public void setBypass(String name, boolean value) {
 		bypass.set(name, value);
 		try {
 			bypass.save(new File(this.getDataFolder() + File.separator + "users.yml"));
 		} catch (IOException ex) {
 			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 }
